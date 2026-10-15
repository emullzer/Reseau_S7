//
// Created by mulle1149u on 15/10/2026.
//

#include "socket_server.h"

#include <functional>
#include <string>
#include <sys/socket.h>

/**
 * Classe qui initialise la socket et toutes les informations nécéssaires à la connexion
 * @param port port pour la socket
 */
socket_server::socket_server(const std::string& port) {
    addrinfo hints{}, *res = nullptr;
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;

    getaddrinfo(NULL, port.c_str(), &hints, &res);
    int s = socket(res->ai_family, res->ai_socktype, res->ai_protocol);

    int yes = 1;
    setsockopt(s, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(yes));
    std::bind(s, res->ai_addr, res->ai_addrlen);
    listen(s, 5);
}
