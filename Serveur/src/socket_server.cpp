//
// Socket d'ecoute : bind, listen, accept
//
#include "socket_server.h"
#include <netdb.h>
#include <sys/socket.h>
#include <unistd.h>
#include <cstdio>
#include <cstring>

serverSocket::serverSocket(const char* port) : port_(port) {}

serverSocket::~serverSocket() {
    if (fd_ >= 0) close(fd_);
}

bool serverSocket::start() {
    addrinfo hints;
    memset(&hints, 0, sizeof hints);
    hints.ai_family   = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags    = AI_PASSIVE;

    addrinfo* res = nullptr;
    int rc = getaddrinfo(nullptr, port_, &hints, &res);
    if (rc != 0) {
        fprintf(stderr, "getaddrinfo : %s\n", gai_strerror(rc));
        return false;
    }

    fd_ = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
    if (fd_ < 0) {
        perror("socket");
        freeaddrinfo(res);
        return false;
    }

    int yes = 1;
    setsockopt(fd_, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof yes);

    if (bind(fd_, res->ai_addr, res->ai_addrlen) < 0) {
        perror("bind");
        freeaddrinfo(res);
        return false;
    }
    freeaddrinfo(res);

    if (listen(fd_, SOMAXCONN) < 0) {
        perror("listen");
        return false;
    }
    return true;
}

connexionClient serverSocket::accept_client() {
    return connexionClient(accept(fd_, nullptr, nullptr));
}
