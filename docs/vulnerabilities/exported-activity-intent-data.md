# Exported Activity With Untrusted Intent Data

This document describes how to use apk-interceptor to verify whether an exported
Android Activity accepts unsafe Intent data during an authorized assessment.

All package names, class names, and hosts are fictional. Replace them only with
values from an application you own or are explicitly authorized to test.

Fictional target:

| Field | Example value |
| --- | --- |
| App name | ExampleChat |
| Package name | `com.sterrasec.vulnerablechat` |
| Exported Activity | `com.sterrasec.vulnerablechat.feature.web.WebActivity` |

## What This Tests

If an app exports an Activity, another app can launch it directly. If that
Activity trusts caller-controlled Intent data, it may expose protected screens,
load untrusted content, or perform unintended actions.

apk-interceptor's **Sender** tab can test this without creating a custom PoC
app.

## Step 1: Confirm Scope And Export Status

Confirm from the target manifest that the Activity is exported and in scope:

```xml
<activity
    android:name=".feature.web.WebActivity"
    android:exported="true" />
```

Do not test non-exported components unless you are working with a debug build or
another explicitly authorized harness.

## Step 2: Open apk-interceptor Sender

1. Open apk-interceptor.
2. Go to **Sender**.
3. Select **Explicit Activity**.

## Step 3: Enter The Target Component

Package name:

```text
com.sterrasec.vulnerablechat
```

Activity class:

```text
com.sterrasec.vulnerablechat.feature.web.WebActivity
```

## Step 4: Optionally Attach A Local `content://` Payload

If the Activity reads Intent data and may pass it to a WebView:

1. Go to **Payload**.
2. Save a harmless HTML payload.
3. Return to **Sender**.
4. Enable **Attach content:// URI**.
5. Keep **FLAG_GRANT_READ_URI_PERMISSION** enabled.

apk-interceptor's provider serves only one fixed file:

```text
content://<apk-interceptor-application-id>.payload/current.html
```

## Step 5: Send The Intent

Tap **Send Intent**.

Observe the target app.

## Useful Observations

Potentially interesting behavior:

- A protected screen opens without authentication.
- The Activity accepts caller-controlled data.
- The Activity loads a local `content://` payload in a WebView.
- The Activity performs a state-changing action.

Not enough by itself:

- The Activity opens but immediately redirects to login.
- The Activity ignores the supplied Intent data.
- The Activity is not exported in the tested version.

## Evidence To Capture

Capture:

- The manifest entry showing the Activity is exported.
- apk-interceptor Sender settings.
- The target app behavior after launch.
- Any error or access-control bypass result.

Do not include real account data or screenshots containing PII.
