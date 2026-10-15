cmake_minimum_required(VERSION 3.16)
project(Serveur)

set(CMAKE_CXX_STANDARD 20)

add_executable(Serveur main.cpp
        src/socket_server.cpp
        src/socket_server.h)
