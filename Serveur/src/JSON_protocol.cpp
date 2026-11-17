//
// Created by emeric on 04/11/2026.
//
#include "JSON_protocol.h"
#include <nlohmann/json.hpp>

using json = nlohmann::json;

constexpr const char* ERR_INVALID_MESSAGE = "INVALID_MESSAGE";
constexpr const char* ERR_INVALID_LETTER  = "INVALID_LETTER";
constexpr const char* ERR_INVALID_STATE   = "INVALID_STATE";
constexpr const char* ERR_UNKNOWN_SESSION = "UNKOWN_SESSION";
constexpr const char* ERR_ALREADY_USED = "ALREADY_USED";

namespace {
bool valid_request_id(const json& in) {
    if (!in.contains("request_id")) return false;
    const json& r = in.at("request_id");
    if (!r.is_number_integer()|| r < 0) return false;
    return true;
}

json create_message(const std::string& type, const json& rid) {
    json m;
    m["type"] = type;
    m["request_id"] = rid;
    return m;
}

std::string error_reply(const json& rid, const std::string& code, const std::string& text) {
    json m = create_message("ERROR", rid);
    m["code"] = code;
    m["message"] = text;
    return m.dump();
}

std::string state_reply(const json& rid, const std::string& sid, const Hangman& g) {
    std::string status = g.is_won() ? "WIN" : (g.is_lost() ? "LOSE" : "IN_PROGRESS");

    json letters = json::array();
    for (char c : g.tried_letters()) letters.push_back(std::string(1, c));

    json payload;
    payload["status"] = status;
    payload["word"] = g.mask();
    payload["tried_letters"] = letters;
    payload["remaining_attempts"] = g.lives();
    if (g.is_over()) payload["revealed_word"] = g.secret();

    json m = create_message("STATE", rid);
    m["session_id"] = sid;
    m["payload"] = payload;
    return m.dump();
}

std::string str_field(const json& obj, const char* key) {
    if (obj.contains(key) && obj[key].is_string()) return obj[key].get<std::string>();
    return "";
}

} // namespace

std::string JSONProtocol::handle(const std::string& line) {
    json rid = nullptr;
    json in;
    try {
        in = json::parse(line);
    } catch (const json::parse_error&) {
        return error_reply(rid, ERR_INVALID_MESSAGE, "JSON invalide");
    }
    if (!in.is_object())
        return error_reply(rid, ERR_INVALID_MESSAGE, "objet JSON attendu");
    if (!valid_request_id(in))
        return error_reply(nullptr, ERR_INVALID_MESSAGE, "request_id manquant ou invalide");
    if (in.contains("request_id")) rid = in["request_id"];
    if (!in.contains("type") || !in["type"].is_string())
        return error_reply(rid, ERR_INVALID_MESSAGE, "champ type manquant");

    const std::string type = in["type"].get<std::string>();
    const std::string sid = str_field(in, "session_id");
    json payload = (in.contains("payload") && in["payload"].is_object())
                       ? in["payload"] : json::object();

    if (type == "HELLO") {
        std::string name = str_field(payload, "player_name");
        if (name.empty())
            return error_reply(rid, ERR_INVALID_MESSAGE, "player_name manquant");
        json m = create_message("HELLO", rid);
        m["payload"]["player_name"] = name;
        return m.dump();
    }

    if (type == "NEW_GAME") {
        std::string nsid = sessions_.create();
        return state_reply(rid, nsid, *sessions_.find(nsid));
    }

    if (type == "QUIT") {
        std::string sid = str_field(in, "session_id");
        Hangman* game = sessions_.find(sid);
        if (!game)
            return error_reply(rid, ERR_UNKNOWN_SESSION, "Session inconnue");

        if (game->is_over())
            sessions_.remove(sid);
        return "";
    }

    if (type == "RESUME") {
        Hangman* game = sessions_.find(sid);
        if (!game) return error_reply(rid, ERR_UNKNOWN_SESSION, "session inconnue");
        return state_reply(rid, sid, *game);
    }

    if (type == "LETTER") {
        Hangman* game = sessions_.find(sid);
        if (!game) return error_reply(rid, ERR_UNKNOWN_SESSION, "session inconnue");
        if (game->is_over()) return error_reply(rid, ERR_INVALID_STATE, "partie terminee");

        std::string letter = str_field(payload, "letter");
        if (letter.size() != 1)
            return error_reply(rid, ERR_INVALID_LETTER, "une seule lettre attendue");

        auto r = game->guess(letter[0]);
        if (r == Outcome::INVALID_LETTER)
            return error_reply(rid, ERR_INVALID_LETTER, "Lettre invalide (A-Z)");
        if (r == Outcome::ALREADY_USED)
            return error_reply(rid, ERR_ALREADY_USED, "Lettre deja proposee");

        return state_reply(rid, sid, *game);
    }

    return error_reply(rid, ERR_INVALID_MESSAGE, "type inconnu : " + type);
}

