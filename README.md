# TapToSnap
This app once installed and launched, takes the user to the home screen (HomeActivity) where it allows to begin the game.<br/> 
On tapping the 'Let's Go' button, it fetches the snappable item list from a API call and takes you to Main game screen (MainActivity).<br/>
The MainActivity lists the items on a GridLayout with default background and item name. It also starts the CountDown timer of 2 minutes within which game needs to
be concluded.<br/>
Once tapped on each item on the grid, takes you to CameraActivity, where for the first time, it asks for necessary permissions(Camera, Read/Write storage) to capture the image of the item to be matched.<br/>
When the image is saved on the device, it's matched against the API call, based on which it updates the item background with successs or incorrect image.<br/> 
For incorrect image, it allows to tap again to retake the picture.<br/>
If the all the items are not matched with the captured images within 2 minutes, it times out prompting user to restart the game again. And it continues.
