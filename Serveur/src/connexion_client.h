//
// Created by emeric on 01/11/2026.
//

#pragma once
#include <cstddef>
#include <string>

class connexionClient {
public:
    explicit connexionClient(int fd);
    ~connexionClient();

    connexionClient(const connexionClient&) = delete;
    connexionClient& operator=(const connexionClient&) = delete;

    bool valid() const;

    /**
     * Envoie la chaine suivie de '\n'.
     * @param s chaine a envoyer
     * @return true si envoyee sans probleme, false sinon
     */
    bool send_line(const std::string& s);

    /**
     * Lit jusqu'au '\n' (sans '\r' ni '\n').
     * false si le client part, erreur, ou ligne plus longue que max.
     * @param line ligne lue
     * @param max longueur maximale de la ligne
     * @return true si une ligne a ete lue
     */
    bool recv_line(std::string& line, std::size_t max = 64);

private:
    int fd_;
};
