//
// Created by emeric on 04/11/2026.
//
#pragma once
#include <string>
#include "session_manager.h"

// Traite une ligne JSON recue et renvoie la reponse JSON ("" = pas de reponse)
class JSONProtocol {
public:
    explicit JSONProtocol(SessionManager& sessions) : sessions_(sessions) {}

    std::string handle(const std::string& line);

private:
    SessionManager& sessions_;
};
