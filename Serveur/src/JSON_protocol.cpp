//
// Created by emeric on 04/11/2026.
//
#include "JSON_protocol.h"
#include <nlohmann/json.hpp>

using json = nlohmann::json;

namespace {

json envelope(const std::string& type, const json& rid) {
    json m;
    m["type"] = type;
    m["request_id"] = rid;
    return m;
}

std::string error_reply(const json& rid, const std::string& code, const std::string& text) {
    json m = envelope("ERROR", rid);
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

    json m = envelope("STATE", rid);
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
        return error_reply(rid, "BAD_REQUEST", "JSON invalide");
    }
    if (!in.is_object()) return error_reply(rid, "BAD_REQUEST", "objet JSON attendu");
    if (in.contains("request_id")) rid = in["request_id"];
    if (!in.contains("type") || !in["type"].is_string())
        return error_reply(rid, "BAD_REQUEST", "champ type manquant");

    const std::string type = in["type"].get<std::string>();
    const std::string sid = str_field(in, "session_id");
    json payload = (in.contains("payload") && in["payload"].is_object())
                       ? in["payload"] : json::object();

    if (type == "HELLO") {
        json m = envelope("HELLO", rid);
        m["payload"] = json::object();
        return m.dump();
    }

    if (type == "NEW_GAME") {
        std::string nsid = sessions_.create();
        return state_reply(rid, nsid, *sessions_.find(nsid));
    }

    if (type == "QUIT") {
        sessions_.remove(sid);
        return "";   // pas de reponse prevue pour QUIT
    }

    if (type == "RESUME") {
        Hangman* game = sessions_.find(sid);
        if (!game) return error_reply(rid, "UNKNOWN_SESSION", "session inconnue");
        return state_reply(rid, sid, *game);
    }

    if (type == "LETTER") {
        Hangman* game = sessions_.find(sid);
        if (!game) return error_reply(rid, "UNKNOWN_SESSION", "session inconnue");
        if (game->is_over()) return error_reply(rid, "GAME_OVER", "partie terminee");

        std::string letter = str_field(payload, "letter");
        if (letter.size() != 1)
            return error_reply(rid, "INVALID_LETTER", "une seule lettre attendue");
        if (game->guess(letter[0]) == Outcome::Invalid)
            return error_reply(rid, "INVALID_LETTER", "caractere non valide");

        return state_reply(rid, sid, *game);
    }

    return error_reply(rid, "UNKNOWN_TYPE", "type inconnu : " + type);
}
