# iFit Convert

The iFit TCX exports are rather horrible (did they even read the XSD??), so I created a simple utility to run some
cleanup on them so that they look and work better in Garmin Connect.

## What it does

* Updates the Sport to be **Running** or **Biking**, instead of the default of Other
* Fixes the **Average Heart Rate** and **Calories** fields
* Fixes the **Total Distance** so that it matches _what you actually did_ and not what the original workout was programmed for
* Updates the point-by-point distances based on your speed so that your speed/pace chart looks less spiky

## How to use it

1) Download both the TCX and CSV for your activity into the same folder
2) `java -jar ifit-convert.jar <sport> <path to your tcx file>`
    
    `<sport>` should be one of: `running`, `biking`, or `other`
3) Import your resulting TCX into Garmin Connect (or wherever else you like to import them)

There are also two `.bat` files which support drag-and-drop conversion of files on Windows:
1) Make sure the `.bat` files and the `ifit-convert.jar` are in the same folder
   
    (doesn't have to be where your tcx/csv files are)
2) Drag your TCX onto the convert bat for the activity type you're converting
3) Import your resulting TCX

## Notes

This was a small weekend project, so don't judge me on the code organization,
neatness, and lack of tests. If you find this useful, buy me a beer or something. If you
make any tweaks or updates, feel free to create a pull request!


iFit, if you use this, send me a check for doing your job for you! Shame on you for
implementing things like a music feed, when stupid stuff like this is so easy to fix
and will make a lot of people happy.

Oh, and iFit - if you want many more ideas on simple but great ways to enhance your
user experience, ping me. I'll tell you more than you probably want to hear.
