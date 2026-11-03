//
// Created by emeric on 01/11/2026.
//
#pragma once
#include <array>
#include <string>

enum class Outcome { STATE, ALREADY_USED, INVALID_LETTER, WIN, LOSE };

class Hangman {
public:
    static constexpr int kMaxErrors = 6;

    explicit Hangman(const std::string& secret);

    /**
     * Essaie une lettre.
     * @param raw caractere saisi
     * @return resultat du coup
     */
    Outcome guess(char raw);

    /**
     * Mot masque, par exemple "P _ T H O N".
     */
    std::string mask() const;

    /**
     * Nombre de vies restantes.
     */
    int lives() const;

    /**
     * Mot secret complet.
     */
    const std::string& secret() const;

private:
    bool all_found() const;

    std::string secret_;
    std::array<bool, 26> tried_{};
    int errors_ = 0;
};
