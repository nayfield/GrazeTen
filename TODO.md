
## Background

This is [GrazeRSS](https://github.com/nayfield/GrazeRSS) inmported into a modern Android Studio.

GrazeRSS is very old - targetSdkVersion 9.

This is back when Android phones had hard menu buttons.  With targetSdkVersion so old, the app can no longer be published to the play store.  

Updating the targetSdkVersion to 29 will break some functionality which must be repaired/ported.

## Walkthrough

Before diving into the code, you should prepare two android virtual devices:
* Nougat 7.0 (API 24)
* Q 10.0 (API 29) 

I used the Pixel 3XL profile. Now run the app on the Nougat phone and take this walkthrough:

1. Accept the license
2. Ignore the settings screen and press 'back'
3. Press the sync button in the action bar
4. Log in with 'testapi/testapi' (you should see articles syncing)
5. Press the menu overflow button (three dots) and change to night mode
6. Open "All Articles" and pick a feed with more than one item
7. long-press on an article and "open in browser"
8. long-press on an article and "share" via messaging

Repeat the walkthrough on the Q device

## Walkthrough results with initial (API 9) build:

* It is possible to do all the steps on Android Nougat
* Android Q has old API warning at first launch
* Android Q does not display menu, cannot do #5
* Android Q crash on #7 and #8


## Tasks to complete

**ALL** tasks are to be fulfilled by github pull requests.  

  * At a minimum you will need to fork the repo, commit and push changes to your fork, and create pull request via the github user interface.  No other contributions will be looked at.


1. Update target SDK to API 29.  
  * Yes this will require some code changes.  
  * Do not just delete code, make the code work with the new SDK.

2. Completing #1 will break walkthrough step #4
  * newer API levels do not allow webview based oauth.  
  * There are plenty of articles on how to fix this on the internet.

3. Recreate the menu button in a supported fashion.

4. Fix the crash behavior on 'share link' or 'open in browser'

