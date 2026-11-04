//
// Created by emeric on 04/11/2026.
//
#include "session_manager.h"
#include <cstdlib>

namespace {
    const char* WORDS[] = {"PYTHON", "ORDINATEUR", "RESEAU", "SOCKET", "SERVEUR", "CLIENT", "PENDU"};
    const int NB_WORDS = sizeof(WORDS);
}

std::string SessionManager::create() {
    std::string sid = "S" + std::to_string(next_id_++);
    sessions_.emplace(sid, Hangman(WORDS[rand() % NB_WORDS]));
    return sid;
}

/**
 * Recherche si la session est deja associee a une partie
 * @param sid ID de session
 * @return Partie en cours si elle existe, nullptr sinon
 */
Hangman* SessionManager::find(const std::string& sid) {
    auto it = sessions_.find(sid);
    return it == sessions_.end() ? nullptr : &it->second;
}

void SessionManager::remove(const std::string& sid) {
    sessions_.erase(sid);
}
