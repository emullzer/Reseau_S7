cmake_minimum_required(VERSION 3.16)
project(Serveur)

set(CMAKE_CXX_STANDARD 20)

list(APPEND CMAKE_PREFIX_PATH "/opt/homebrew" "/usr/local")
find_package(nlohmann_json REQUIRED)

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

target_link_libraries(Serveur PRIVATE nlohmann_json::nlohmann_json)