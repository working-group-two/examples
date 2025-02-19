# Copyright (C) 2025 Cisco Systems, Inc.
load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary", "kt_jvm_library")

package(default_visibility = ["//visibility:public"])

kt_jvm_library(
    name = "lib",
    srcs = glob(["src/main/**/*.kt"]),
    deps = [
        "@maven//:build_buf_gen_wgtwo_wgtwoapis_grpc_kotlin",
        "@maven//:build_buf_gen_wgtwo_wgtwoapis_protocolbuffers_kotlin",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:com_wgtwo_api_auth",
        "@maven//:io_grpc_grpc_netty_shaded",
        "@maven//:io_grpc_grpc_protobuf",
        "@maven//:io_grpc_grpc_stub",
    ],
)

kt_jvm_binary(
    name = "send",
    main_class = "com.wgtwo.example.sms.SendSmsKt",
    runtime_deps = [":lib"],
)

kt_jvm_binary(
    name = "receive",
    main_class = "com.wgtwo.example.sms.ReceiveSmsKt",
    runtime_deps = [":lib"],
)
