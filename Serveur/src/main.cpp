#include <cstdio>
#include <cstdlib>
#include <ctime>
#include <memory>
#include <mutex>
#include <string>
#include <thread>
#include "connexion_client.h"
#include "socket_server.h"
#include "session_manager.h"
#include "JSON_protocol.h"

#define PORT "5001"

int main() {
    srand(static_cast<unsigned>(time(nullptr)));

    SessionManager sessions;
    JSONProtocol protocol(sessions);
    std::mutex mtx;

    serverSocket server(PORT);
    if (!server.start()) return 1;
    printf("Serveur pendu en ecoute sur le port %s\n", PORT);
    fflush(stdout);

    for (;;) {
        auto client = std::make_shared<connexionClient>(server.accept_client());
        if (!client->valid()) continue;

        std::thread([client, &protocol, &sessions, &mtx]() {
            std::string line;
            while (client->recv_line(line)) {
                std::lock_guard<std::mutex> lock(mtx);
                protocol.handle(line, client);
            }
            std::lock_guard<std::mutex> lock(mtx);
            sessions.remove_client(client);
        }).detach();
    }
}