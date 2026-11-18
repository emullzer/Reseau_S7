//
// Created by emeric on 04/11/2026.
//
#include "session_manager.h"
#include <algorithm>
#include <cstdlib>
#include <random>

#include "connexion_client.h"

namespace {
    const char* WORDS[] = {"PYTHON", "ORDINATEUR", "RESEAU", "SOCKET", "SERVEUR", "CLIENT", "PENDU"};
    const int NB_WORDS = sizeof(WORDS) / sizeof(WORDS[0]);
}

std::string SessionManager::generate_room_code() {
    const char chars[] = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    std::string code;
    do {
        code.clear();
        for (int i = 0; i < 4; ++i) {
            code += chars[rand() % (sizeof(chars) - 1)];
        }
    } while (sessions_.find(code) != sessions_.end());
    return code;
}

std::string SessionManager::create(std::shared_ptr<connexionClient> client, const std::string& player_name) {
    std::string sid = generate_token();
    GameSession session(Hangman(WORDS[rand() % NB_WORDS]), false);
    if (client) session.clients.push_back(std::move(client));
    session.players.push_back(player_name);
    sessions_.emplace(sid, std::move(session));
    return sid;
}

std::string SessionManager::create_room(std::shared_ptr<connexionClient> client, const std::string& player_name) {
    std::string code = generate_room_code();
    GameSession session(Hangman(WORDS[rand() % NB_WORDS]), true);
    if (client) session.clients.push_back(std::move(client));
    session.players.push_back(player_name);
    sessions_.emplace(code, std::move(session));
    return code;
}

bool SessionManager::join_room(const std::string& code, std::shared_ptr<connexionClient> client, const std::string& player_name, std::string& error_msg) {
    auto it = sessions_.find(code);
    if (it == sessions_.end() || !it->second.is_multiplayer) {
        error_msg = "Code de room introuvable";
        return false;
    }
    GameSession& session = it->second;
    if (session.game.is_over()) {
        error_msg = "Cette partie est deja terminee";
        return false;
    }
    if (session.clients.size() >= 2) {
        error_msg = "Cette room est deja pleine (2 joueurs max)";
        return false;
    }
    session.clients.push_back(std::move(client));
    session.players.push_back(player_name);
    return true;
}

/**
- Recherche si la session est deja associee a une partie
- @param sid ID de session
- @return Partie en cours si elle existe, nullptr sinon
 */
GameSession* SessionManager::find(const std::string& sid) {
    auto it = sessions_.find(sid);
    return it == sessions_.end() ? nullptr : &it->second;
}

void SessionManager::remove(const std::string& sid) {
    sessions_.erase(sid);
}

void SessionManager::remove_client(const std::shared_ptr<connexionClient>& client) {
    for (auto it = sessions_.begin(); it != sessions_.end(); ) {
        auto& clients = it->second.clients;
        clients.erase(std::remove(clients.begin(), clients.end(), client), clients.end());
        if (clients.empty()) {
            it = sessions_.erase(it);
        } else {
            if (it->second.current_turn >= clients.size()) {
                it->second.current_turn = 0;
            }
            ++it;
        }
    }
}

/**
 * Cree un token pour pouvoir reprendre la partie si jamais elle est interrompue.
 * @return
 */
std::string SessionManager::generate_token() {
    static const char HEXA[] = "0123456789abcdef";
    std::random_device rd; //TODO peut etre changer de methode, pas assez securise
    std::string token;
    token.reserve(32); // allouer memoire
    for (int i = 0; i < 4; ++i) {
        unsigned int v = rd();
        for (int j = 0; j < 8; ++j) {
            token += HEXA[v & 0xF];
            v >>= 4;
        }
    }
    return token;
}
