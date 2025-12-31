# MeshHelper

MeshHelper is a standalone companion Android app that connects to the official **Meshtastic Android** app via its AIDL interface (`IMeshService`). It allows sending and receiving data over the Meshtastic mesh network while adding custom sensor readings or other helper functionality.

The UI is built with **Jetpack Compose** for a modern and responsive experience.

This project is open-source under the **GPL-3.0** license (see [LICENSE](LICENSE)), matching the license of Meshtastic itself.  

**Important:** This repository does **not** include any Meshtastic source code - it is designed to be used alongside the official Meshtastic-Android project.

## Features

- Connects to the official Meshtastic Android app using the AIDL `IMeshService` interface
- Send and receive packets over the Meshtastic mesh network
- Placeholder/integration point for custom sensors or helper logic
- Clean Jetpack Compose UI
- Runs as a standalone app (does not modify the Meshtastic app)

## Prerequisites

- Android Studio (latest stable recommended)
- Git
- Official Meshtastic Android app installed on your test device/emulator
- Android SDK with API level matching Meshtastic requirements

## Setup Instructions (Manual Integration)

**Note:** MeshHelper currently requires integration into the Meshtastic-Android source tree because it depends on the Meshtastic AIDL files. This is not ideal long-term, but works reliably for now.

1. Clone the official Meshtastic-Android repository
   ```bash
   git clone https://github.com/meshtastic/Meshtastic-Android.git

2. Clone this MeshHelper repository
   ```bash
   git clone https://github.com/YOUR-USERNAME/MeshHelper.git

3. Create a meshhelper directory inside the Meshtastic-Android folder
    ```bash
   cd Meshtastic-Android
   mkdir meshhelper

5. Copy all files from MeshHelper into the new directory (exclude .git)
    ```bash
   cp -r ../MeshHelper/* meshhelper/
   rm -rf meshhelper/.git   # just in case

6. Open settings.gradle.kts in the root of Meshtastic-Android and add :meshhelper to the include() block:
   ```kotlin
   include(
         ":app",
         ":service",
         // ... other modules ...
         ":meshhelper"
         )

7. Open the project in Android Studio

9. Open the Meshtastic-Android folder as the project

10. Sync Gradle, then build & run the :meshhelper module
    - You may need to select the meshhelper module in the run configuration


## Usage

1. Make sure the official Meshtastic app is installed and running on the same device
2. Launch MeshHelper
3. Grant any requested permissions
4. MeshHelper should automatically bind to the Meshtastic service via AIDL
5. Use the interface to send/receive test packets or view your custom sensor data (to come)

**Tip:** Watch Logcat with filter MeshHelper or IMeshService to debug connection issues.

## Troubleshooting

- "Service not found" / binding fails
    → Make sure the official Meshtastic app is installed and running in the background
- Gradle sync errors
  → Make sure you copied all files correctly and :meshhelper is included in settings.gradle.kts
- AIDL class not found
  → Confirm the meshhelper module is placed at the correct path and Gradle was synced

## Future / Better Integration Ideas

- Publish AIDL as a standalone artifact / library (if Meshtastic allows)
- Use reflection or runtime binding (fragile)
- Create a proper plugin system in Meshtastic (long-term wish)

## Contributing
Contributions are welcome!
Especially interested in:
- Cleaner integration methods
- Example sensor implementations
- UI/UX improvements
- Documentation & error handling

## License
GPL-3.0
See LICENSE

## Acknowledgments

Meshtastic project - amazing community & foundation
Everyone working on open mesh networking