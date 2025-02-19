# SMS

The SMS example demonstrates how to send and receive SMS messages.

> [!NOTE]  
> `--target` is optional for all apps and defaults to `sandbox.api.shamrock.wgtwo.com:443`.

## Send SMS from subscriber

Required scopes:

- `sms.text:send_from_subscriber`

```
Usage: bazel run //kotlin/sms:send -- [<options>] <from> <to> <message>...

Options:
  --client-id=<text>                                                                              OAuth 2.0 Client ID [env: CLIENT_ID]
  --client-secret=<text>                                                                          OAuth 2.0 Client Secret [env: CLIENT_SECRET]
  --target=(sandbox.api.shamrock.wgtwo.com:443|api.shamrock.wgtwo.com:443|api.oak.wgtwo.com:443)  API Endpoint [env: ENDPOINT]
  -h, --help
```

```shell
bazel run //kotlin/sms:send {from} {to} Hello, World!
```

## Receive SMS

Required scopes:

- `"events.sms.subscribe"`

```
Usage: bazel run //kotlin/sms:receive -- [<options>]

Options:
  --client-id=<text>                                                                              OAuth 2.0 Client ID [env: CLIENT_ID]
  --client-secret=<text>                                                                          OAuth 2.0 Client Secret [env: CLIENT_SECRET]
  --target=(sandbox.api.shamrock.wgtwo.com:443|api.shamrock.wgtwo.com:443|api.oak.wgtwo.com:443)  API Endpoint [env: ENDPOINT]
  -h, --help
```

```shell
bazel run //kotlin/sms:receive
```
