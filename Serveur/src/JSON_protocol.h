//
// Created by emeric on 04/11/2026.
//
#pragma once
#include <memory>
#include <string>
#include "connexion_client.h"
#include "session_manager.h"

//traite une ligne JSON recue et envoie la reponse JSON au(x) client(s) concerne(s)
class JSONProtocol {
public:
    explicit JSONProtocol(SessionManager& sessions) : sessions_(sessions) {}

    void handle(const std::string& line, const std::shared_ptr<connexionClient>& sender);

private:
    SessionManager& sessions_;
};