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

const char* WORDS[] = {"PYTHON", "ORDINATEUR", "RESEAU", "SOCKET", "SERVEUR", "CLIENT", "PENDU"};
const int NB_WORDS = 7;

// Traduit le resultat du jeu en ligne du protocole
std::string format_reply(Outcome o, const Hangman& g) {
    switch (o) {
        case Outcome::Won:     return "WON " + g.secret();
        case Outcome::Lost:    return "LOST " + g.secret();
        case Outcome::Hit:     return "HIT " + g.mask() + " " + std::to_string(g.lives());
        case Outcome::Miss:    return "MISS " + g.mask() + " " + std::to_string(g.lives());
        case Outcome::Already: return "ALREADY " + g.mask() + " " + std::to_string(g.lives());
        case Outcome::Invalid: return "INVALID " + g.mask() + " " + std::to_string(g.lives());
    }
    return "INVALID";
}

// Une partie complete avec un client
void play(Connection& client) {
    Hangman game(WORDS[rand() % NB_WORDS]);
    std::string line;

    client.send_line("STATE " + game.mask() + " " + std::to_string(game.lives()));

    while (client.recv_line(line)) {
        if (line == "QUIT") break;

        Outcome o = (line.size() == 1) ? game.guess(line[0]) : Outcome::Invalid;
        client.send_line(format_reply(o, game));

        if (o == Outcome::Won || o == Outcome::Lost) break;
    }
}

int main() {
    srand(static_cast<unsigned>(time(nullptr)));

    ServerSocket server(PORT);
    if (!server.start()) return 1;

    printf("Serveur pendu en ecoute sur le port %s\n", PORT);

    for (;;) {
        Connection client = server.accept_client();
        if (!client.valid()) continue;
        play(client);   // client se ferme en fin de tour (RAII)
    }
}
