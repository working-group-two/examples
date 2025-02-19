# Copyright (C) 2025 Cisco Systems, Inc.
load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

package(default_visibility = ["//visibility:public"])

kt_jvm_library(
    name = "common",
    srcs = glob(["src/main/**/*.kt"]),
    deps = [
        "@maven//:com_github_ajalt_clikt_clikt_jvm",
    ],
)
