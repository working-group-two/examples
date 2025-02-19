# SMS

The SMS example demonstrates how to send and receive SMS messages.

## Send SMS from subscriber

Required scopes:
- `sms.text:send_from_subscriber`

```shell
export CLIENT_ID=YOUR_CLIENT_ID
export CLIENT_SECRET=YOUR_CLIENT_SECRET

bazel run //kotlin/sms:send
```

## Receive SMS

Required scopes:
- `"events.sms.subscribe"`

```shell
export CLIENT_ID=YOUR_CLIENT_ID
export CLIENT_SECRET=YOUR_CLIENT_SECRET

bazel run //kotlin/sms:receive
```