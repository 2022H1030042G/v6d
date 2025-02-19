# build vineyard-graph
file(GLOB_RECURSE VINEYARD_MOD_SRCS "${CMAKE_CURRENT_SOURCE_DIR}"
                                    "*.vineyard-mod")

file(GLOB_RECURSE VINEYARD_HEADERS "${CMAKE_CURRENT_SOURCE_DIR}"
                                   "*.h")

find_package(MPI REQUIRED)
find_package(libgrapelite REQUIRED)

if(VINEYARD_MOD_SRCS)
    vineyard_generate(
        OUT_VAR VINEYARD_GENERATES
        VINEYARD_MODULES ${VINEYARD_MOD_SRCS}
    )
else()
    set(VINEYARD_GENERATES)
endif()

if(FALSE AND BUILD_VINEYARD_JAVA AND (VINEYARD_HEADERS OR VINEYARD_GENERATES))
    vineyard_generate_java(
        OUT_VAR VINEYARD_JAVA_GENERATES
        VINEYARD_MODULES ${VINEYARD_HEADERS} ${VINEYARD_GENERATES}
        DEPENDS ${VINEYARD_GENERATES}
    )
else()
    set(VINEYARD_JAVA_GENERATES)
endif()

add_custom_target(vineyard_graph_gen
                  DEPENDS ${VINEYARD_GENERATES}
                  COMMENT "Running code generation for vineyard_graph."
)

add_custom_target(vineyard_graph_java_gen
                  DEPENDS vineyard_graph_gen
                          ${VINEYARD_JAVA_GENERATES}
                  COMMENT "Running java code generation for vineyard_graph."
)

add_dependencies(vineyard_graph_gen vineyard_basic_gen)

file(GLOB_RECURSE GRAPH_SRC_FILES "${CMAKE_CURRENT_SOURCE_DIR}" "fragment/*.cc"
                                                                "loader/*.cc"
                                                                "utils/*.cc"
                                                                "vertex_map/*.cc"
)

add_library(vineyard_graph ${GRAPH_SRC_FILES})
target_add_debuginfo(vineyard_graph)
target_include_directories(vineyard_graph PUBLIC
                                          ${MPI_CXX_INCLUDE_PATH}
)

find_package(Boost COMPONENTS leaf)
if(Boost_LEAF_FOUND)
    target_include_directories(vineyard_graph PUBLIC ${Boost_INCLUDE_DIRS})
else()
    # boost::leaf for error_handling
    add_subdirectory_static("${CMAKE_CURRENT_SOURCE_DIR}/thirdparty/boost-leaf"
                            "${CMAKE_CURRENT_BINARY_DIR}/thirdparty/boost-leaf"
    )
    # install boost leaf
    include(GNUInstallDirs)
    install(DIRECTORY
        "${CMAKE_CURRENT_SOURCE_DIR}/thirdparty/boost-leaf/include/"
        DESTINATION
        ${CMAKE_INSTALL_PREFIX}/include/vineyard/contrib
    )

    target_include_directories(vineyard_graph PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/thirdparty/boost-leaf/include>
        $<INSTALL_INTERFACE:include/vineyard/contrib>
    )
endif()

target_include_directories(vineyard_graph PUBLIC
    $<BUILD_INTERFACE:${LIBGRAPELITE_INCLUDE_DIRS}>
    $<INSTALL_INTERFACE:include>
)

target_link_libraries(vineyard_graph PUBLIC vineyard_client
                                            vineyard_basic
                                            vineyard_io
                                            ${ARROW_SHARED_LIB}
                                            ${MPI_CXX_LIBRARIES}
)
if(${LIBUNWIND_FOUND})
    target_link_libraries(vineyard_graph PUBLIC ${LIBUNWIND_LIBRARIES})
endif()
if(APPLE)
    target_compile_options(vineyard_graph PUBLIC -Wno-unused-result)
endif()

add_dependencies(vineyard_graph vineyard_graph_gen)

add_dependencies(vineyard_codegen vineyard_graph_gen)
if(VINEYARD_JAVA_GENERATES)
    add_dependencies(vineyard_codegen_java vineyard_graph_java_gen)
endif()

install_vineyard_target(vineyard_graph)
install_vineyard_headers("${CMAKE_CURRENT_SOURCE_DIR}")

# add the vineyard-graph-loader target
add_executable(vineyard-graph-loader "${CMAKE_CURRENT_SOURCE_DIR}/tools/graph_loader.cc")
target_link_libraries(vineyard-graph-loader PRIVATE vineyard_graph)
install_vineyard_target(vineyard-graph-loader)

if(BUILD_VINEYARD_TESTS)
    enable_testing()
    file(GLOB TEST_FILES RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}/test" "${CMAKE_CURRENT_SOURCE_DIR}/test/*.cc")
    foreach(f ${TEST_FILES})
        string(REGEX MATCH "^(.*)\\.[^.]*$" dummy ${f})
        set(T_NAME ${CMAKE_MATCH_1})
        message(STATUS "Found unit_test - " ${T_NAME})
        if(BUILD_VINEYARD_TESTS_ALL)
            add_executable(${T_NAME} test/${T_NAME}.cc)
        else()
            add_executable(${T_NAME} EXCLUDE_FROM_ALL test/${T_NAME}.cc)
        endif()
        target_link_libraries(${T_NAME} PRIVATE
                              vineyard_graph
                              ${ARROW_SHARED_LIB}
                              ${MPI_CXX_LIBRARIES})
        if(${LIBUNWIND_FOUND})
            target_link_libraries(${T_NAME} PRIVATE ${LIBUNWIND_LIBRARIES})
        endif()
        add_test(${T_NAME}, ${T_NAME})
        add_dependencies(vineyard_tests ${T_NAME})
    endforeach()
endif()
