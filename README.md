# Procrastaint

This is a todo list made using Kotlin Multiplatform which includes a natural language processor to easily read date and times.

This is still WIP

to format android:  
`./gradlew detektall`  

Building android 
Create a xml file in shared/src/main/res/values/android_client_id.xml
Get an android client key from https://console.cloud.google.com OAuth 2.0 Client IDs in the Credentials page  

Replace the client ID in the following in your created xml file with the below template  
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="android_client_id">
        YOUR_GENERATED_CLIENT_ID
    </string>
</resources>
```