# Custom URI Scheme Hijacking

This document describes how to use apk-interceptor to verify custom URI scheme
hijacking during an authorized Android application assessment.

All package names, schemes, hosts, users, messages, and URLs are fictional.
Replace them only with values from an application you own or are explicitly
authorized to test.

Fictional target:

| Field | Example value |
| --- | --- |
| App name | ExampleChat |
| Package name | `com.sterrasec.vulnerablechat` |
| Custom URI scheme | `examplechat` |
| In-app web host | `app.examplechat.test` |

## What This Tests

Android custom URI schemes such as `examplechat://` are not ownership-verified.
Another installed app can register the same scheme and may appear as a resolver
candidate when the user opens a matching deeplink.

apk-interceptor can act as that second app and show the full URI and query
parameters it receives.

## Step 1: Build apk-interceptor

Build apk-interceptor with the assessed custom scheme:

```bash
./build-interceptor.sh \
  --scheme examplechat \
  --app-id com.sterrasec.assessment.interceptor
```

Install it alongside the target app:

```bash
adb install ./out/apk-interceptor-examplechat-debug.apk
```

## Step 2: Confirm Both Apps Can Handle The Scheme

Query Android's resolver candidates:

```bash
adb shell cmd package query-activities --brief \
  -a android.intent.action.VIEW \
  -c android.intent.category.BROWSABLE \
  -c android.intent.category.DEFAULT \
  -d 'examplechat://test?probe=1'
```

Expected result:

- apk-interceptor appears as one resolver candidate.
- The target app appears as another resolver candidate.

This proves scheme collision reachability. It does not yet prove sensitive data
exposure.

## Step 3: Trigger A Dummy Notification-Style Deeplink

Send a deeplink with placeholder values:

```bash
adb shell "am start -W \
  -a android.intent.action.VIEW \
  -c android.intent.category.BROWSABLE \
  -c android.intent.category.DEFAULT \
  -d 'examplechat://notification?type=message&page=https%3A%2F%2Fapp.examplechat.test%2Fchat%2Ftest&text=test-notification&userName=test-user&userMessage=test-message&userImageUrl=https%3A%2F%2Fexample.invalid%2Fuser.jpg'"
```

If Android shows an app chooser, select apk-interceptor.

## Step 4: Verify Received Parameters

Open apk-interceptor's **Interceptor** tab and expand the `RECEIVED` log entry.

Expected parameter names:

```text
type = message
page = https://app.examplechat.test/chat/test
text = test-notification
userName = test-user
userMessage = test-message
userImageUrl = https://example.invalid/user.jpg
```

## Step 5: Test Browser-Link Triggering

This checks whether a normal browser link can trigger the custom scheme.

Create a local HTML page and save it as `/tmp/examplechat_deeplink_test.html`:

```html
<!doctype html>
<meta charset="utf-8">
<title>ExampleChat deeplink test</title>
<body>
  <p>Dummy browser-link test</p>
  <a href="examplechat://notification?type=message&page=https%3A%2F%2Fapp.examplechat.test%2Fchat%2Ftest&text=test-notification&userName=test-user&userMessage=test-message&userImageUrl=https%3A%2F%2Fexample.invalid%2Fuser.jpg">
    Open ExampleChat deeplink
  </a>
</body>
```

Serve it locally and expose it to the device through adb reverse:

```bash
python3 -m http.server 18080 --directory /tmp
adb reverse tcp:8080 tcp:18080
```

Open it on the device:

```bash
adb shell am start -W \
  -a android.intent.action.VIEW \
  -d 'http://127.0.0.1:8080/examplechat_deeplink_test.html'
```

Tap **Open ExampleChat deeplink**, then select apk-interceptor if the chooser
appears.

Clean up:

```bash
adb reverse --remove tcp:8080
```

## Step 6: Check Real App Flows With Test Accounts

Use only test accounts and test data. Candidate flows:

- Message notification tap
- Match or invite notification tap
- Email campaign link
- Passwordless sign-in link
- Share or referral link
- Web campaign link that opens the app

Record only parameter names and value types. Do not store real message text,
profile names, image URLs, access tokens, or PII.

## Result Interpretation

Higher-impact evidence:

- A realistic user action produces an Android chooser.
- apk-interceptor receives user-specific parameters.
- Parameters include message previews, profile data, account identifiers, or
  session-related tokens.

Lower-impact evidence:

- Only artificial adb commands produce the URI.
- The real flow uses explicit intents and never shows a chooser.
- The received URI contains only static routing values.

Custom scheme collision alone is usually a platform property. It becomes more
meaningful when the app places sensitive values in the URI or trusts untrusted
deeplink parameters.
