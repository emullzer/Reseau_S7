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
constexpr const char* ERR_ALREADY_USED    = "ALREADY_USED";

namespace {
bool valid_request_id(const json& in) {
    if (!in.contains("request_id")) return false;
    const json& r = in.at("request_id");
    if (!r.is_number_integer() || r < 0) return false;
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

std::string state_reply(const json& rid, const std::string& sid, const GameSession& session) {
    const Hangman& g = session.game;
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

void broadcast(GameSession& session, const std::string& msg) {
    for (auto& c : session.clients) {
        if (c && c->valid()) {
            c->send_line(msg);
        }
    }
}

std::string str_field(const json& obj, const char* key) {
    if (obj.contains(key) && obj[key].is_string()) return obj[key].get<std::string>();
    return "";
}

} // namespace

void JSONProtocol::handle(const std::string& line, const std::shared_ptr<connexionClient>& sender) {
    json rid = nullptr;
    json in;
    try {
        in = json::parse(line);
    } catch (const json::parse_error&) {
        sender->send_line(error_reply(rid, ERR_INVALID_MESSAGE, "JSON invalide"));
        return;
    }
    if (!in.is_object()) {
        sender->send_line(error_reply(rid, ERR_INVALID_MESSAGE, "objet JSON attendu"));
        return;
    }
    if (!valid_request_id(in)) {
        sender->send_line(error_reply(nullptr, ERR_INVALID_MESSAGE, "request_id manquant ou invalide"));
        return;
    }
    if (in.contains("request_id")) rid = in["request_id"];
    if (!in.contains("type") || !in["type"].is_string()) {
        sender->send_line(error_reply(rid, ERR_INVALID_MESSAGE, "champ type manquant"));
        return;
    }

    const std::string type = in["type"].get<std::string>();
    const std::string sid = str_field(in, "session_id");
    json payload = (in.contains("payload") && in["payload"].is_object())
                       ? in["payload"] : json::object();

    if (type == "HELLO") {
        std::string name = str_field(payload, "player_name");
        if (name.empty()) {
            sender->send_line(error_reply(rid, ERR_INVALID_MESSAGE, "player_name manquant"));
            return;
        }
        json m = create_message("HELLO", rid);
        m["payload"]["player_name"] = name;
        sender->send_line(m.dump());
        return;
    }

    if (type == "NEW_GAME") {
        std::string name = str_field(payload, "player_name");
        std::string nsid = sessions_.create(sender, name);
        sender->send_line(state_reply(rid, nsid, *sessions_.find(nsid)));
        return;
    }
    if (type == "CHAT") {
        GameSession* session = sessions_.find(sid);
        if (!session) {
            sender->send_line(error_reply(rid, ERR_UNKNOWN_SESSION, "Session inconnue"));
            return;
        }
        std::string name = str_field(payload, "player_name");
        std::string text = str_field(payload, "text");
        if (text.empty()) {
            sender->send_line(error_reply(rid, ERR_INVALID_MESSAGE, "Message vide"));
            return;
        }

        json m = create_message("CHAT", rid);
        m["session_id"] = sid;
        m["payload"]["player_name"] = name.empty() ? "Joueur" : name;
        m["payload"]["text"] = text;

        broadcast(*session, m.dump());
        return;
    }

    if (type == "CREATE_ROOM") {
        std::string name = str_field(payload, "player_name");
        if (name.empty()) {
            sender->send_line(error_reply(rid, ERR_INVALID_MESSAGE, "player_name manquant"));
            return;
        }
        std::string code = sessions_.create_room(sender, name);
        sender->send_line(state_reply(rid, code, *sessions_.find(code)));
        return;
    }

    if (type == "JOIN_ROOM") {
        std::string name = str_field(payload, "player_name");
        std::string code = sid;
        for (char& c : code) c = static_cast<char>(toupper(static_cast<unsigned char>(c)));

        std::string err;
        if (!sessions_.join_room(code, sender, name, err)) {
            sender->send_line(error_reply(rid, ERR_UNKNOWN_SESSION, err));
            return;
        }
        GameSession* session = sessions_.find(code);
        broadcast(*session, state_reply(rid, code, *session));
        return;
    }

    if (type == "QUIT") {
        GameSession* session = sessions_.find(sid);
        if (!session) {
            sender->send_line(error_reply(rid, ERR_UNKNOWN_SESSION, "Session inconnue"));
            return;
        }

        if (session->game.is_over())
            sessions_.remove(sid);
        return;
    }

    if (type == "RESUME") {
        GameSession* session = sessions_.find(sid);
        if (!session) {
            sender->send_line(error_reply(rid, ERR_UNKNOWN_SESSION, "session inconnue"));
            return;
        }
        sender->send_line(state_reply(rid, sid, *session));
        return;
    }

    if (type == "LETTER") {
        GameSession* session = sessions_.find(sid);
        if (!session) {
            sender->send_line(error_reply(rid, ERR_UNKNOWN_SESSION, "session inconnue"));
            return;
        }
        if (session->game.is_over()) {
            sender->send_line(error_reply(rid, ERR_INVALID_STATE, "partie terminee"));
            return;
        }

        if (session->is_multiplayer) {
            if (session->clients.size() < 2) {
                sender->send_line(error_reply(rid, ERR_INVALID_STATE, "En attente d'un deuxieme joueur"));
                return;
            }
            if (session->clients[session->current_turn] != sender) {
                sender->send_line(error_reply(rid, ERR_INVALID_STATE, "Ce n'est pas ton tour"));
                return;
            }
        }

        std::string letter = str_field(payload, "letter");
        if (letter.size() != 1) {
            sender->send_line(error_reply(rid, ERR_INVALID_LETTER, "une seule lettre attendue"));
            return;
        }

        auto r = session->game.guess(letter[0]);
        if (r == Outcome::INVALID_LETTER) {
            sender->send_line(error_reply(rid, ERR_INVALID_LETTER, "Lettre invalide (A-Z)"));
            return;
        }
        if (r == Outcome::ALREADY_USED) {
            sender->send_line(error_reply(rid, ERR_ALREADY_USED, "Lettre deja proposee"));
            return;
        }

        if (session->is_multiplayer && !session->clients.empty()) {
            session->current_turn = (session->current_turn + 1) % session->clients.size();
        }

        broadcast(*session, state_reply(rid, sid, *session));
        return;
    }

    sender->send_line(error_reply(rid, ERR_INVALID_MESSAGE, "type inconnu : " + type));
}