//
// Created by emeric on 04/11/2026.
//
#pragma once
#include <cstddef>
#include <memory>
#include <string>
#include <unordered_map>
#include <vector>
#include "connexion_client.h"
#include "hangman.h"

struct GameSession {
    Hangman game;
    bool is_multiplayer = false;
    std::vector<std::shared_ptr<connexionClient>> clients;
    std::vector<std::string> players;
    std::size_t current_turn = 0;

    explicit GameSession(Hangman g, bool multi = false)
        : game(std::move(g)), is_multiplayer(multi) {}
};

class SessionManager {
public:
    std::string create(std::shared_ptr<connexionClient> client, const std::string& player_name);
    std::string create_room(std::shared_ptr<connexionClient> client, const std::string& player_name);
    bool join_room(const std::string& code, std::shared_ptr<connexionClient> client, const std::string& player_name, std::string& error_msg);

    /**
     * Recherche si la session est deja associee a une partie
     * @param sid ID de session
     * @return Partie en cours si elle existe, nullptr sinon
     */
    GameSession* find(const std::string& sid);

    void remove(const std::string& sid);
    void remove_client(const std::shared_ptr<connexionClient>& client);

    static std::string generate_token();

private:
    std::unordered_map<std::string, GameSession> sessions_;
    int next_id_ = 1;
    std::string generate_room_code();
};