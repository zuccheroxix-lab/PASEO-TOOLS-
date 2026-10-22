cmake_minimum_required(VERSION 3.22.1)

project("paseosensi_native")

add_library(
    paseosensi_native
    SHARED
    sensitivity_engine.cpp
    sensitivity_bridge_jni.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    paseosensi_native
    ${log-lib}
)

# Standalone CLI demo / test harness executable for PC / Desktop targets
add_executable(
    paseo_sensitivity_cli
    sensitivity_engine_desktop_cli.cpp
    sensitivity_engine.cpp
)
