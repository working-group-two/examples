# Examples

Sample applications for the Working Group Two APIs.

## Setup

This repo uses Bazel for building and running the examples.
To install Bazel, follow the instructions [here](https://docs.bazel.build/versions/main/install.html).

### macOS

```shell
brew install bazel
```

### Linux

```shell
curl -Lo bazel https://github.com/bazelbuild/bazelisk/releases/latest/download/bazelisk-linux-amd64
chmod +x bazel
sudo mv bazel /usr/local/bin/
```

## Kotlin

### SMS

- [Send SMS from subscriber](kotlin/sms/README.md#send-sms-from-subscriber)
- [Receive SMS](kotlin/sms/README.md#receive-sms)
