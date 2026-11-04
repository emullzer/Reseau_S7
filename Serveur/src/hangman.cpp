//
// Created by emeric on 01/11/2026.
//

#include "hangman.h"
#include <cctype>

Hangman::Hangman(const std::string& secret) : secret_(secret) {}

Outcome Hangman::guess(char raw) {
    unsigned char u = static_cast<unsigned char>(raw);
    if (!isalpha(u)) return Outcome::INVALID_LETTER;

    int idx = toupper(u) - 'A';
    if (tried_[idx]) return Outcome::ALREADY_USED;
    tried_[idx] = true;

    if (secret_.find(static_cast<char>('A' + idx)) != std::string::npos) {
        return all_found() ? Outcome::WIN : Outcome::STATE;
    }
    ++errors_;
    return errors_ >= kMaxErrors ? Outcome::LOSE : Outcome::STATE;
}

std::string Hangman::mask() const {
    std::string out;
    for (char c : secret_) {
        if (!out.empty()) out += ' ';
        out += tried_[c - 'A'] ? c : '_';
    }
    return out;
}

int Hangman::lives() const {
    return kMaxErrors - errors_;
}

const std::string& Hangman::secret() const {
    return secret_;
}

bool Hangman::all_found() const {
    for (char c : secret_)
        if (!tried_[c - 'A']) return false;
    return true;
}
std::string Hangman::tried_letters() const {
    std::string out;
    for (int i = 0; i < 26; ++i)
        if (tried_[i]) out += static_cast<char>('A' + i);
    return out;
}

bool Hangman::is_won() const {
    return all_found();
}

bool Hangman::is_lost() const {
    return errors_ >= kMaxErrors;
}

bool Hangman::is_over() const {
    return is_won() || is_lost();
}
