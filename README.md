# whiteboard-ide

This repo contains the source code for an Android app built to make the life of a technical interviewer easier.  It was built with Java, and the Azure Computer Vision API from Microsoft.  
## Introduction 
Interviewing candidates for Software Engineer positions has largely stayed the same for a number of years.  The most widely used strategy for testing coding knowledge is the whiteboard interview.  Most questions asked in whiteboard interviews are difficult in nature, but don't require hundreds of lines of code to solve.  This app was created to allow the interviewer to immediately see the output of these smaller chunks of code instead of needing to run through it manually or typing it into an IDE.  
## Purpose
 - Allow interviewers to take a picture of a chunk of code, and immediately translate it into text on their phone
 - Provide editing functionality in case the code is not read properly from the photo
 - Run the code and display the output on the phone
## Challenges
 - Designing the UI, as UI design is not one of my strengths
## What's Next
At the moment, the app supports Java and C++ code.  I want to expand this further to include other languages in the future.  
As well, I would like to try to port this app idea to iOS eventually.  
## Screenshots
<img src="images/HomeScreen.jpg" width="250"> <img src="images/OutputScreen.jpg" width="250"> <img src="images/PictureScreen.jpg" width="250"> 
## Licensing
This project is open source, so please feel free to fork it and add your own features.  
One requirement to run this app is the creation of a "secrets" xml file.  This will house your Azure Computer Vision API key, and Jdoodle API ID/secret.  You must make the file: app\src\main\res\values\secrets.xml, with the following format:  
~~~
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="image_processing_api_key">AZURE_API_KEY_HERE</string>
    <string name="jdoodle_client_id">JDOODLE_API_ID_HERE</string>
    <string name="jdoodle_client_secret">JDOODLE_API_SECRET_HERE</string>
</resources>
~~~
