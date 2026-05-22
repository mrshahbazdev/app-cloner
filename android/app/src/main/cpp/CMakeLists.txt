cmake_minimum_required(VERSION 3.22.1)

project("titan_clone_native" VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(titan_clone_native SHARED
    io_redirect.cpp
    memory_manager.cpp
    binder_intercept.cpp
    property_redirect.cpp
    proc_maps_filter.cpp
)

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(titan_clone_native
    ${log-lib}
    ${android-lib}
)

target_include_directories(titan_clone_native PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
)
