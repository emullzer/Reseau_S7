//
// Created by emeric on 01/11/2026.
//

#include "hangman.h"
#include <array>
#include <cctype>
#include <string>

enum class Outcome { Hit, Miss, Already, Invalid, Won, Lost };

class Hangman {
public:
    static constexpr int kMaxErrors = 6;

    explicit Hangman(const std::string& secret) : secret_(secret) {}

    Outcome guess(char raw) {
        unsigned char u = static_cast<unsigned char>(raw);
        if (!isalpha(u)) return Outcome::Invalid;

        int idx = toupper(u) - 'A';
        if (tried_[idx]) return Outcome::Already;
        tried_[idx] = true;

        if (secret_.find(static_cast<char>('A' + idx)) != std::string::npos) {
            return all_found() ? Outcome::Won : Outcome::Hit;
        }
        ++errors_;
        return errors_ >= kMaxErrors ? Outcome::Lost : Outcome::Miss;
    }

    // "P _ T H O N"
    std::string mask() const {
        std::string out;
        for (char c : secret_) {
            if (!out.empty()) out += ' ';
            out += tried_[c - 'A'] ? c : '_';
        }
        return out;
    }

    int lives() const { return kMaxErrors - errors_; }
    const std::string& secret() const { return secret_; }

private:
    bool all_found() const {
        for (char c : secret_)
            if (!tried_[c - 'A']) return false;
        return true;
    }

    std::string secret_;
    std::array<bool, 26> tried_{};
    int errors_ = 0;
};
