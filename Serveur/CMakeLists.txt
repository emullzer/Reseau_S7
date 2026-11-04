cmake_minimum_required(VERSION 3.16)
project(Serveur)

set(CMAKE_CXX_STANDARD 20)

add_executable(Serveur
        src/socket_server.cpp
        src/socket_server.h
        src/connexion_client.cpp
        src/connexion_client.h
        src/hangman.cpp
        src/hangman.h
        src/main.cpp
        src/session_manager.cpp
        src/session_manager.h
        src/JSON_protocol.cpp
        src/JSON_protocol.h
)
