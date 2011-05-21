# GroupMe Android SDK

This open source Android library allows you to integrate GroupMe into your Android application.

Except as otherwise noted, the GroupMe Android SDK is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

# Environment

* Get your Android development environment set up by following instructions here: (http://developer.android.com/sdk/index.html)
* Get this SDK by pulling it from Github: git clone git@github.com:groupme/groupme-android-sdk.git

# Components

The project contains the following three components:

* demo - A sample application demonstrating how to integrate the GroupMe Client Library
* sdk - The source and resources for the GroupMe Client Library
* tests - Unit tests for the GroupMe Client Library

# API Client ID and Client Secret

The Client ID and Client Secret are provided to the GroupMeConnect object when first initialized. The demo application provided uses the strings.xml file to hold these values, but
application developers are free to store the values using other methods. It is important to keep this values out of public repositories or anywhere else they could be publicly 
human readable.

# Integration into your Android application

The GroupMe Client Library is an Android library project. This allows Android applications to reference the library and the build process will take care of managing resources and class files.

* If you build your project with ant you can set the project reference in the default.properties file. See the example below:
    android.library.reference.1=/SOME/PATH/groupme-android-sdk/sdk

    *NOTE* When setting the reference for the GroupMe Client Library you need to point to the sdk directory within the project.

* If you are using Eclipse or Intellij you can add a reference to the project just like any other Android library project.

# Using the Client Library

The GroupMe Client Library comes with Activity classes that are ready to use out of the box. In addition to the Activity classes, the GroupMe Client Library contains APIs to make all the necessary calls manually if you wish to implement a slightly different user interface.

* In the AndroidManifest.xml file of the GroupMe Client Library you'll see permissions that should be defined in your application's AndroidManifest.xml file. If you wish to use any of the provided Activities in the library you'll need to copy and paste the <activity> tags into your Application's AndroidManifest.xml file. 

# Using the built in Activity classes.

There are a few Activity classes in the Client Library that allow application developer's to get up and running quickly without needing to implement large amounts of code. At this time,
not all the features supported by the GroupMe Client Library are available as built in Activity classes. This is on the short todo list but for now the functionality can be achieved by 
using the Direct API Requests method. The code found in the demo app and all adapters found in the sdk are available for application developers to use freely in their application.

# Making Direct API Requests

You can also make requests directly against the GroupMe API using the GroupMeConnect object.

An example:
    
    Bundle params = new Bundle();
    params.putString("client_id", your_client_id);
    params.putString("token", your_client_token);
    
    GroupMeRequest request = new GroupMeRequest();
    request.setRequest(GROUP_ME_API_URL, HttpUtils.METHOD_GET);
    request.setRequestListener(requestListener);
    request.setParams(params);
    
    request.start();
    
The token is stored in the "groupme.prefs" SharedPreferences under the key "token".

# Other Resources

* For more information on what API calls are available, please visit https://github.com/groupme/client-library-api
