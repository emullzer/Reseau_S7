//
// Created by emeric on 04/11/2026.
//
#pragma once
#include <map>
#include <string>
#include "hangman.h"

// Gere les parties en cours, identifiees par session_id
class SessionManager {
public:
    // Cree une partie avec un mot tire au hasard, renvoie son session_id
    std::string create();

    // Renvoie la partie, ou nullptr si la session est inconnue
    Hangman* find(const std::string& sid);

    void remove(const std::string& sid);

private:
    std::map<std::string, Hangman> sessions_;
    int next_id_ = 1;
};
