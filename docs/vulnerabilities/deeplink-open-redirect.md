# Deeplink Open Redirect

This document describes how to use apk-interceptor and adb to verify a deeplink
open redirect during an authorized Android application assessment.

All package names, schemes, hosts, and URLs are fictional. Replace them only
with values from an application you own or are explicitly authorized to test.

Fictional target:

| Field | Example value |
| --- | --- |
| App name | ExampleChat |
| Package name | `com.sterrasec.vulnerablechat` |
| Custom URI scheme | `examplechat` |

## What This Tests

Some apps accept deeplink parameters such as `page`, `url`, `redirect`,
`continue`, or `returnTo`, then navigate to that value. If the app opens an
untrusted URL without adequate validation, the issue may be an open redirect.

apk-interceptor can help prepare and send crafted deeplinks, but it cannot prove
the target app's final navigation by itself. You must observe the target app,
browser, or WebView.

## Step 1: Prepare A Controlled Target Page

Use a local loopback page or an owned in-scope test domain. A local page is
usually enough.

Example local page, saved as `/tmp/redirect_target.html`:

```html
<!doctype html>
<meta charset="utf-8">
<title>Redirect target</title>
<body>
  <h1>Local redirect target</h1>
  <p>If this page opens after the deeplink, the page parameter controlled navigation.</p>
</body>
```

Serve it locally:

```bash
python3 -m http.server 18080 --directory /tmp
adb reverse tcp:8080 tcp:18080
```

The device can now reach:

```text
http://127.0.0.1:8080/redirect_target.html
```

## Step 2: Send A Controlled Deeplink To The Target App

Send the deeplink explicitly to the target package so apk-interceptor does not
receive it instead:

```bash
adb shell "am start -W \
  -a android.intent.action.VIEW \
  -c android.intent.category.BROWSABLE \
  -c android.intent.category.DEFAULT \
  -d 'examplechat://notification?type=match&page=http%3A%2F%2F127.0.0.1%3A8080%2Fredirect_target.html' \
  com.sterrasec.vulnerablechat"
```

## Step 3: Observe The Result

Potentially vulnerable behavior:

- The target app receives the deeplink.
- The app opens the `page` URL in an external browser.
- The app opens the `page` URL in an in-app WebView.

Non-reproducing behavior:

- The target app opens but does not navigate.
- The target app requires authentication before handling the route.
- The app validates the host and blocks the URL.
- The current app version has been fixed.

## Step 4: Capture Evidence

Capture:

- The crafted deeplink with dummy or local target URL.
- The target app receiving the deeplink.
- The browser or WebView opening the controlled target.

Do not use real phishing pages, credential collection pages, or off-scope
domains.

## Step 5: Clean Up

Remove the adb reverse mapping and stop the local server:

```bash
adb reverse --remove tcp:8080
```

## Result Interpretation

This issue is stronger when:

- The redirect target is attacker-controlled.
- The deeplink can be triggered from a browser, email, notification, or another
  app.
- The target app gives the destination credibility by opening it from an
  app-branded flow.

This issue is weaker or not reproduced when:

- The app validates the destination host.
- The app ignores the parameter.
- Navigation only works for trusted internal routes.
