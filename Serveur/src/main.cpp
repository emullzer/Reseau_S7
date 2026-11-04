// main.cpp : demarrage du serveur et lecture des lignes
#include <cstdio>
#include <cstdlib>
#include <ctime>
#include <string>
#include "connexion_client.h"   
#include "socket_server.h"
#include "session_manager.h"
#include "JSON_protocol.h"

#define PORT "8877"

int main() {
    srand(static_cast<unsigned>(time(nullptr)));

    SessionManager sessions;
    JSONProtocol protocol(sessions);

    serverSocket server(PORT);
    if (!server.start()) return 1;
    printf("Serveur pendu en ecoute sur le port %s\n", PORT);

    for (;;) {
        connexionClient client = server.accept_client();
        if (!client.valid()) continue;

        std::string line;
        while (client.recv_line(line)) {
            std::string reply = protocol.handle(line);
            if (!reply.empty() && !client.send_line(reply)) break;
        }
    }
}
