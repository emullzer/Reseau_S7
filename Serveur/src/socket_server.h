//
// Socket d'ecoute : bind, listen, accept
//
#pragma once
#include "connexion_client.h"

class serverSocket {
public:
    explicit serverSocket(const char* port);
    ~serverSocket();

    serverSocket(const serverSocket&) = delete;
    serverSocket& operator=(const serverSocket&) = delete;

    /**
     * Prepare l'ecoute (getaddrinfo, socket, bind, listen).
     * Affiche le message d'erreur en cas de probleme.
     * @return true si l'ecoute est prete, false sinon
     */
    bool start();

    /**
     * Attend un client (accept).
     * @return connexion avec le client. valid() renvoie false en cas d'erreur.
     */
    connexionClient accept_client();

private:
    const char* port_;
    int fd_ = -1;
};
