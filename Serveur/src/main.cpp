//
// Created by emeric on 01/11/2026.
//

#include <cstdio>
#include <cstdlib>
#include <ctime>
#include <string>
#include "connexion_client.h"
#include "socket_server.h"
#include "hangman.h"

#define PORT "8877"

const std::string WORDS[] = {"PYTHON", "ORDINATEUR", "RESEAU", "SOCKET", "SERVEUR", "CLIENT", "PENDU"};
const int NB_WORDS = 7;

// TODO CHanger le format en JSON
std::string format_reply(Outcome o, const Hangman& g) {
    switch (o) {
        case Outcome::WIN:     return "WON " + g.secret();
        case Outcome::LOSE:    return "LOST " + g.secret();
        case Outcome::STATE:     return "STATE " + g.mask() + " " + std::to_string(g.lives());
        case Outcome::ALREADY_USED: return "ALREADY_USED " + g.mask() + " " + std::to_string(g.lives());
        case Outcome::INVALID_LETTER: return "INVALID_LETTER " + g.mask() + " " + std::to_string(g.lives());
    }
    return "INVALID_STATE";
}

// Une partie complete avec un client
void play(connexionClient& client) {
    Hangman game(WORDS[rand() % NB_WORDS]);
    std::string line;

    client.send_line("STATE " + game.mask() + " " + std::to_string(game.lives()));

    while (client.recv_line(line)) {
        if (line == "QUIT") break;

        Outcome o = (line.size() == 1) ? game.guess(line[0]) : Outcome::INVALID_LETTER;
        client.send_line(format_reply(o, game));

        if (o == Outcome::WIN || o == Outcome::LOSE) break;
    }
}

int main() {
    srand(static_cast<unsigned>(time(nullptr)));

    serverSocket server(PORT);
    if (!server.start()) return 1;

    printf("Serveur pendu en ecoute sur le port %s\n", PORT);

    for (;;) {
        connexionClient client = server.accept_client();
        if (!client.valid()) continue;
        play(client);   // client se ferme en fin de tour (RAII)
    }
}
