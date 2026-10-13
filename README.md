# Community Connect (Community Events)

**New Android mini-project for Wednesday class exercise (30 marks).** A beginner-friendly Kotlin application where you can add, browse and view community events.

## Features
- Events list and event detail screen
- Add form with required-field and date validation
- Firebase **Cloud Firestore** saves/loads events online (NoSQL)
- Firebase **Storage** uploads a selected event image (object/blob storage)
- **Two REST API buttons:** one fetches a sample post from JSONPlaceholder; the other connects to the included Visual Studio ASP.NET Core API on port 5092 and displays example events. Both are separate from Firestore.
- Helpful errors when Firebase, internet or form input fails

## Open in Android Studio
1. Extract the ZIP. In Android Studio choose **File > Open** and select the **CommunityEvents** folder (the one containing `settings.gradle.kts`).
2. Let Android Studio sync Gradle. Install Android SDK 35 if asked. Use **JDK 17** for Gradle. This project specifies Gradle 8.9; if Android Studio does not recognize the wrapper without `gradle-wrapper.jar`, choose its installed Gradle distribution in Settings > Build Tools > Gradle, or generate a wrapper with a local Gradle 8.9 install (`gradle wrapper`).
3. Run on a device or Android emulator with internet access. You can test the **public REST API** before setting up Firebase. For the local API, also run the included **CommunityEventsAPI** project in Visual Studio.

## Configure Firebase (required for online events and image uploads)
1. In [Firebase Console](https://console.firebase.google.com), create a Firebase project.
2. Add an Android app with package name **`com.example.communityevents`**.
3. Open Firebase **Project settings > General > Your apps** and copy **App ID** and **Web API Key**. From **Project settings** obtain the **Project ID** and **Storage bucket** name as shown by Firebase. (You do **not** need a `google-services.json` file: this project uses manual FirebaseOptions.)
4. Replace the four `YOUR_...` placeholders in `app/src/main/res/values/strings.xml` with your actual values. App ID usually begins `1:`. Storage bucket resembles `your-project.firebasestorage.app` or `your-project.appspot.com` depending on your project.
5. In Firebase Console create a **Cloud Firestore database**. Create a collection named `events` (or the app will create it when you save an event). Allow read/write for your test app using appropriate test rules (below).
6. Enable **Firebase Storage** and create its default bucket. **Firebase Storage may require enabling billing / Blaze plan**; review its current console requirements and costs before proceeding. This exercise needs at least one actual uploaded image.
7. Launch the app, add an event (with an image), then return to the event list and open the details.

### Temporary development rules (ONLY for a controlled classroom demo)
In Firestore > Rules, for an individual classroom demo you may temporarily use:
```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /events/{eventId} {
      allow read, write: if request.time < timestamp.date(2026, 10, 20);
    }
  }
}
```
And Firebase Storage > Rules:
```js
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /event_images/{imageName} {
      allow read, write: if request.time < timestamp.date(2026, 10, 20);
    }
  }
}
```
These rules allow **public access until 20 October 2026** and are **not safe for a real deployment**. Restrict them to trusted users with Firebase Authentication for any long-term app. Delete demo resources or disable public access immediately after your presentation. Do not put private information into test events.

## Wednesday presentation (30 marks)
1. **UI (5):** Show event list, Add Event and View Details.
2. **NoSQL (6):** Add an event, then show it in Firestore Console and on the app's events list.
3. **Blob/object storage (4):** Choose an event photo, save, show image in Firebase Storage and open it in Event Details.
4. **REST API (5):** Tap **Test Public REST API** to receive a JSON post. Then run the included Visual Studio API and tap **Test My Visual Studio API** to receive a JSON array of community events. Explain that both use HTTP GET; only the second is your own API.
5. **Validation (3):** Save with an empty title, then an invalid date; show field errors with no crash.
6. **GitHub (3):** Push project files with `README.md` and screenshots of the event list, event details and Firestore console. Never push service account secrets.
7. **Presentation (4):** Demonstrate the new project and explain each feature.

## Push to GitHub
Create an empty GitHub repo, then run (replace the URL with your own):
```sh
git init
git add .
git commit -m "Create Community Connect Android mini project"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/CommunityEvents.git
git push -u origin main
```
Add your real screenshots to `screenshots/` and commit again. Screenshots aren't included because the app has not been run yet.

## Technology
Android Studio, Kotlin, XML layouts, Firebase Cloud Firestore, Firebase Storage, Glide, HTTP GET/JSONPlaceholder, GitHub.

## Notes
Firebase is **not preconnected**: replace configuration placeholders and set up your own project. An emulator and working internet connection are required. Build and device test have **not** been performed in this environment.

## Using BOTH APIs
The ZIP includes two folders:
- **CommunityEvents** — open this folder in Android Studio.
- **CommunityEventsAPI** — open `CommunityEventsAPI.csproj` in Visual Studio 2022 (.NET 8).

The public API is online: `https://jsonplaceholder.typicode.com/posts/1`.
Your own API runs on the PC at `http://localhost:5092/api/events`, which the standard Android emulator accesses at `http://10.0.2.2:5092/api/events`. Run Visual Studio at the same time as the emulator. The sample API events use **in-memory storage**; they are not the Firestore events. Saving and displaying actual events online still needs Firebase setup.

**HTTP warning:** `usesCleartextTraffic=true` is set only to allow the local classroom HTTP server. Switch to a secure HTTPS service for any production use.
