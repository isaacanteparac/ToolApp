# ToolApp

Act as a Senior Android Developer. I need you to create a complete Android application in Kotlin using Jetpack Compose that solves physical hardware/software limitations and acts as a smart screenshot manager.

Here are the functional requirements for the application:

---

### FEATURE 1: Floating Volume Controller Overlay (Accessibility/Overlay)

1. **Problem:** Physical volume buttons are broken.
2. **Behavior:**
   - Create a subtle, floating overlay strip/edge handle positioned precisely on the side edge of the screen (where the physical volume keys usually are).
   - Swiping UP on this floating area increases system media volume; swiping DOWN decreases it.
   - Show a visual feedback indicator (custom volume bar or toast) when adjusting volume.
3. **Technical Requirements:**
   - Use an Android `Foreground Service` combined with `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
   - Handle permissions: `SYSTEM_ALERT_WINDOW` (Display over other apps).
   - Provide an On/Off toggle in the main UI to start/stop this service.

---

### FEATURE 2: Automatic Categorized Screenshot Manager

1. **Problem:** Screenshots clutter the gallery in a single folder.
2. **Behavior:**
   - Detect whenever a new screenshot is taken on the device.
   - Identify which application was open on the screen at the exact moment of the capture.
   - Automatically move (or copy) the screenshot file into a specific subfolder based on the app's name (e.g., `Pictures/Screenshots/WhatsApp/`, `Pictures/Screenshots/Instagram/`).
3. **Technical Requirements:**
   - Use a `ContentObserver` on `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` to detect newly added screenshots.
   - Determine the foreground app using `UsageStatsManager` (`PACKAGE_USAGE_STATS` permission).
   - Use `MediaStore` API / Scoped Storage (compatible with Android 10 to 14+) to safely categorize files.

---

### FEATURE3: In-App Screenshot Viewer & Metadata Description Manager (NEW)

1. **Gallery & App Categorization View:**
   - Display a gallery tab in the app where screenshots are grouped by their category/app folder.
2. **Interactive Detail & Zoom View:**
   - Tapping a screenshot opens a full-screen viewer.
   - Implement smooth pinch-to-zoom, pan, and double-tap zoom capabilities (e.g., using `Coil` or Jetpack Compose `transformable` / `graphicsLayer` gestures).
3. **Movie-Style Description & EXIF Metadata Support:**
   - Provide a movie-style UI overlay at the bottom of the detailed image view (e.g., title, category tag, timestamp, and a custom text field for "Movie-style Synopsis / Description").
   - Allow the user to edit and save this description.
   - **Crucial:** Save the entered description directly into the image's EXIF metadata tag (`ExifInterface.TAG_IMAGE_DESCRIPTION` or `TAG_USER_COMMENT`) so the text stays permanently embedded inside the JPEG/PNG file itself without losing standard file portability.

---

### UI & PERMISSIONS DASHBOARD (Main Screen):

- Clean Jetpack Compose UI with Bottom Navigation (Tabs: Settings/Service Control, Screenshot Gallery).
- Status & Permissions Checklist showing if required permissions are granted:
  1. Overlay Permission (`SYSTEM_ALERT_WINDOW`)
  2. Usage Stats Access (`PACKAGE_USAGE_STATS`)
  3. Storage / Media Read-Write Permissions (`READ_MEDIA_IMAGES` / Scoped Storage)
  4. Foreground Service Notification Permission
- Direct buttons/links to take the user to System Settings for missing permissions.
- Toggle switches to enable/disable "Volume Floating Edge" and "Auto-Categorize Screenshots" independently.

---

Please provide:

1. Complete `AndroidManifest.xml` with all necessary permissions, services, and queries.
2. Step-by-step modular code architecture (MainActivity, OverlayService, ScreenshotObserverService, EXIFHelper, Jetpack Compose UI components).
3. Production-ready Kotlin code including image zoom handling and EXIF metadata writing using `androidx.exifinterface.media.ExifInterface`.
