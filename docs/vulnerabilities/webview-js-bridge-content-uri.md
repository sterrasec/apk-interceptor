# WebView JavaScript Bridge Exposure Via `content://`

This document describes how to use apk-interceptor to test whether a target
WebView loads a local `content://` HTML payload and exposes a JavaScript bridge
to that content.

All package names, class names, bridge names, and hosts are fictional. Replace
them only with values from an application you own or are explicitly authorized
to test.

Fictional target:

| Field | Example value |
| --- | --- |
| App name | ExampleChat |
| Package name | `com.sterrasec.vulnerablechat` |
| WebView Activity | `com.sterrasec.vulnerablechat.feature.web.WebActivity` |
| Bridge object | `exampleBridge` |

## What This Tests

Some Android apps expose JavaScript bridges with `addJavascriptInterface`.
If the same WebView loads attacker-controlled or untrusted content, JavaScript
inside that content may call exposed bridge methods.

apk-interceptor can:

- Create a local HTML payload.
- Serve it through a constrained `content://` provider.
- Grant temporary read access to the target app.
- Self-test the payload syntax in its own WebView.

apk-interceptor cannot directly observe what happens inside the target app's
WebView. You must verify the target-side result through approved logs, a debug
build, or authorized WebView inspection.

## Step 1: Identify The Bridge And Entry Point

During static analysis, identify:

- The Activity that owns the WebView.
- Whether that Activity is exported or reachable through an authorized flow.
- The bridge object name passed to `addJavascriptInterface`.
- The exposed bridge methods.
- Whether the WebView loads Intent data, extras, or deeplink-controlled URLs.

Do not test private or non-exported entry points unless your authorization
explicitly covers the test method.

## Step 2: Create A Harmless Payload

Open apk-interceptor's **Payload** tab.

Example HTML:

```html
<!doctype html>
<html>
  <body>
    <script>
      if (window.exampleBridge) {
        window.exampleBridge.logResult("bridge reachable");
      }
    </script>
  </body>
</html>
```

Set the bridge object name:

```text
exampleBridge
```

Tap **Save Payload**.

## Step 3: Run apk-interceptor Self-Test

Tap **Run Self-Test**.

Expected result:

- The local self-test WebView loads the saved HTML.
- The JavaScript bridge call succeeds against apk-interceptor's own test
  bridge.
- A bridge result appears in the apk-interceptor log.

This confirms the payload syntax. It does not prove the target app is
vulnerable.

## Step 4: Send The Payload To The Target

Open **Sender**.

1. Select **Explicit Activity**.
2. Enter the target package:

   ```text
   com.sterrasec.vulnerablechat
   ```

3. Enter the target WebView Activity class:

   ```text
   com.sterrasec.vulnerablechat.feature.web.WebActivity
   ```

4. Enable **Attach content:// URI**.
5. Keep **FLAG_GRANT_READ_URI_PERMISSION** enabled.
6. Tap **Send Intent**.

## Step 5: Observe Target-Side Behavior

Potentially vulnerable behavior:

- The target WebView loads apk-interceptor's `content://` HTML.
- JavaScript from that content reaches the exposed bridge.
- The bridge returns sensitive data or performs privileged actions.

Non-reproducing behavior:

- The target Activity is not exported.
- The target ignores the Intent data.
- The WebView blocks `content://` loading.
- The bridge is not injected for untrusted content.
- The exposed bridge methods are harmless.

## Evidence To Capture

Capture:

- Static evidence of `addJavascriptInterface`.
- The bridge object name and exposed method names.
- The target entry point that can load untrusted content.
- apk-interceptor Payload and Sender settings.
- Approved target-side logs or debug evidence showing bridge reachability.

Do not use payloads that steal data, execute shell commands, or exfiltrate
results. Keep the payload limited to a harmless reachability check.
