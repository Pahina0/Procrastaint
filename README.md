# Procrastaint

**Procrastaint** is a todo list built using **Kotlin Multiplatform**. It integrates a [natural language processor](https://github.com/Pahina0/KWhen) to extract dates and times from plain English, letting users quickly create time-based tasks just by writing sentences.

It currently features Google Calendar integration for seamless task syncing.

<div align="center">
  <img src="https://github.com/user-attachments/assets/15949bd8-8fe9-451e-9eac-15824f865c2b" width="200"/>
  <img src="https://github.com/user-attachments/assets/ecfb80ad-4c66-42f2-bf4f-e1a1aa1161d0" width="200"/>
  <img src="https://github.com/user-attachments/assets/5af97831-abe2-4b3c-b166-0fcacd4fdebb" width="200"/>
  <img src="https://github.com/user-attachments/assets/aa9eb9f6-e578-418d-bcc4-7379b29cd356" width="200"/>
</div>

---

## Setup

### Android

Instructions for setting up and building the Android app:

#### Formatting

To format all Kotlin code:

```bash
./gradlew detektall
```

#### OAuth (Android Only)

1. Create a file at:
   `shared/src/main/res/values/android_client_id.xml`

2. Get an Android OAuth 2.0 Client ID from [Google Cloud Console](https://console.cloud.google.com) under **Credentials → OAuth 2.0 Client IDs**

3. Add the following content to the `android_client_id.xml` file:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="android_client_id">
        YOUR_GENERATED_CLIENT_ID
    </string>
</resources>
```

---

### Firebase (Android Only)

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project with package name: `ap.panini.procrastaint`
3. Follow the steps to obtain your `google-services.json` file
4. Place the file inside the `/androidApp` directory
