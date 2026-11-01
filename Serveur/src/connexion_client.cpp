//
// Created by emeric on 01/11/2026.
//
#pragma once
#include <sys/socket.h>
#include <unistd.h>
#include <cerrno>
#include <cstddef>
#include <string>

class connexionClient {
public:
    explicit connexionClient(int fd) : fd_(fd) {}
    ~connexionClient() { if (fd_ >= 0) close(fd_); }

    connexionClient(const connexionClient&) = delete;
    connexionClient& operator=(const connexionClient&) = delete;

    bool valid() const { return fd_ >= 0; }

    /**
     *
     * @param s string a envoyer
     * @return true si envoyé sans problème / false si problème
     */
    bool send_line(const std::string& s) {
        std::string msg = s + "\n";
        size_t off = 0;
        while (off < msg.size()) {
            ssize_t n = send(fd_, msg.data() + off, msg.size() - off, 0);
            if (n < 0 && errno == EINTR) continue;
            if (n <= 0) return false;
            off += static_cast<size_t>(n);
        }
        return true;
    }

    /**
     * Lit jusqu'au '\n' (sans '\r' ni '\n'). false si le client part,
     * erreur, ou ligne plus longue que max
     * @param line
     * @param max
     * @return
    */
    bool recv_line(std::string& line, size_t max = 64) {
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

private:
    int fd_;
};
