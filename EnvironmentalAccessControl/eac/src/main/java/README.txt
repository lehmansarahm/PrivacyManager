To export to Unity:
    - Select "eac" module in Android project view
    - Select "Make module 'eac'" from Android Studio "Build" menu
    - When build completes, navigate to "eac" directory under project root folder
        - Open "build" folder
        - Open "outputs" then "aar"
        - Copy "eac-release.aar" to Desktop
    - Change ".aar" to ".zip", then extract
    - Locate "classes.jar" and rename to "EnvironmentalAccessControl.jar"
    - Copy jar to "Assets/Plugins" under Unity project root folder