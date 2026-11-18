//
// Created by emeric on 01/11/2026.
//
#include "connexion_client.h"
#include <sys/socket.h>
#include <unistd.h>
#include <cerrno>

connexionClient::connexionClient(int fd) : fd_(fd) {}

connexionClient::~connexionClient() {
    if (fd_ >= 0) close(fd_);
}

connexionClient::connexionClient(connexionClient&& other) noexcept : fd_(other.fd_) {
    other.fd_ = -1;
}

connexionClient& connexionClient::operator=(connexionClient&& other) noexcept {
    if (this != &other) {
        if (fd_ >= 0) close(fd_);
        fd_ = other.fd_;
        other.fd_ = -1;
    }
    return *this;
}

bool connexionClient::valid() const {
    return fd_ >= 0;
}

bool connexionClient::send_line(const std::string& s) {
    std::string msg = s + "\n";
    std::size_t off = 0;
    while (off < msg.size()) {
        ssize_t n = send(fd_, msg.data() + off, msg.size() - off, MSG_NOSIGNAL);
        if (n < 0 && errno == EINTR) continue;
        if (n <= 0) return false;
        off += static_cast<std::size_t>(n);
    }
    return true;
}

bool connexionClient::recv_line(std::string& line, std::size_t max) {
    line.clear();
    char c;
    for (;;) {
        ssize_t n = recv(fd_, &c, 1, 0);
        if (n < 0 && errno == EINTR) continue;
        if (n <= 0) return false;
        if (c == '\n') return true;
        if (c == '\r') continue;
        if (line.size() >= max) return false;
        line += c;
    }
}
