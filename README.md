# FunPlus SDK for Android

## Requirements

* Android Studio 2.2+
* Android API level 16+
* Gradle 2.3+

## Example App

There are several example apps inside the `samples` directory. You can open anyone of them to see an example on how the FunPlus SDK can be integrated.

## Integration

### Add the SDK to Your Project

1. Add the `funplus-android-sdk-<version>.jar` file to your app and set as dependency.
2. Add the `funsdk-default-config-<app>.json` file to your project's `assets` directory and rename it to `funsdk-default-config.json`.

### Add Volley Library

FunPlus SDK uses the [Volley Library](https://developer.android.com/training/volley/index.html?hl=pt-br) to make network requests. Please add the Volley Library to your app if you haven't done so. One way of doing this is to add the following line to the `dependencies` block of the `build.gradle` file:

```groovy
compile 'com.android.volley:volley:1.0.0'
```

### Add Adjust SDK

FunPlus SDK uses the [Adjust SDK](https://github.com/adjust/android_sdk) to tracks some KPI events. Please add the Adjust SDK to your app if you haven't done so. One way of doing this is to add the following line to the`dependencies` block of the `build.gradle` file:

```groovy
compile 'com.adjust.sdk:adjust-android:4.10.2'
```

### Add Google Play Services

FunPlus SDK uses the [Google Advertising ID](https://support.google.com/googleplay/android-developer/answer/6048248?hl=en) to uniquely identify devices. To allow the SDK to use the Google Advertising ID, you must integrate the [Google Play Services](http://developer.android.com/google/play-services/setup.html). If you haven't done this yet, please open the `build.gradle` file of your app and add the following line to the `dependencies` block:

```groovy
compile 'com.google.android.gms:play-services-analytics:9.4.0'
```

### Add Permissions

Add the following permission declarations before the `application` tag in your `AndroidManifest.xml` if they're not present already.

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

### Add Broadcast Receiver

FunPlus SDK need to be notified when network connection changes. Add the following `receiver` tag inside the `application` tag in your `AndroidManifest.xml`. 

```xml
<receiver android:name="com.funplus.sdk.ConnectionChangeReceiver" android:label="NetworkConnection">
     <intent-filter>
         <action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
     </intent-filter>
 </receiver>
```

Adjust SDK wrapped inside the FunPlus SDK requires an `INSTALL_REFERRER` receiver. Add the following `receiver` tag too.

```xml
<receiver
    android:name="com.adjust.sdk.AdjustReferrerReceiver"
    android:exported="true" >
    <intent-filter>
        <action android:name="com.android.vending.INSTALL_REFERRER" />
    </intent-filter>
</receiver>
```

### Install the SDK

Completing all the previous steps, now we're ready to install the FunPlus SDK in your app. Please call the SDK's `install()` method as soon as possible once app starts, usually inside the application's `onCreate()` is a good place to do this.

Contact the FunPlus SDK team to obtain the `appId` and `appKey` pair.

```java
import com.funplus.sdk.FunPlusSDK;
import com.funplus.sdk.SDKEnvironment;

Application application = "{YourApplicationInstance}";
Context context = "{YourAppContext}";
String appId = "{YourAppId}";
String appKey = "{YourAppKey}";
SDKEnvironment env = SDKEnvironment.Production;		// Production/Sandbox

try {
	FunPlusSDK.install(context, appId, appKey, env);
  	FunPlusSDK.registerActivityLifecycleCallbacks(application);
} catch (Exception e) {
  	// Something is wrong?!!
    // Put your error handling codes here.
}
```

## Usage

### The ID Module

The objective of the ID module is to provide a unified ID for each unique user and consequently make it possible to identify users across all FunPlus services (marketing, payment, etc). Note that the ID module can not be treated as an account module, therefore you cannot use this module to complete common account functionalities such as registration and logging in.

**Get an FPID based on a given user ID**

```java
import com.funplus.sdk.FunPlusID;

FunPlusSDK.getFunPlusID().get(externalID, externalIDType, completionHandler);
```

The `get` method is defined as below:

```java
class FunPlusID {...
  
public enum Error {
    UnknownError, SigError, ParseError, NetworkError;
}

public enum ExternalIDType {
    GUID, Email, FacebookID, InAppUserID;
}
                 
public interface FunPlusIDHandler {
	void onSuccess(String fpid);
    void onFailure(Error error);
}
                 
/**
    params externalID:		The in-game user ID.
    params externalIDType:	Type of the in-game user ID.
    params completion:		The completion handler.
 */
public void get(String externalID,
                ExternalIDType externalIDType,
                FunPlusIDHandler completion);
```

**Bind a new user ID to an existing FPID**

```java
import com.funplus.sdk.FunPlusID;

FunPlusSDK.getFunPlusID().bind(fpid, externalID, externalIDType, completionHandler);
```

### The RUM Module

The RUM module monitors user's actions in real-time and uploads collected data to Log Agent.

**Trace a `service_monitoring` event**

```java
FunPlusSDK.getFunPlusRUM().traceServiceMonitoring(...);
```

The `traceServiceMonitoring` method is defined as below:

```java
class FunPlusRUM {...

/**
    params serviceName:		Name of the service.
    params httpUrl:			Requesting URL of the service.
    params httpStatus:		The response status (can be a string).
    params requestSize:		Size of the request body.
    params responseSize:	Size of the response body.
    params httpLatency:		The request duration (in milliseconds).
    params requestTs:		Requesting timestamp.
    params responseTs:		Responding timestamp.
    params requestId:		Identifier of current request.
    params targetUserId:	User ID.
    params gameServerId:	Game server ID.
 */
public void traceServiceMonitoring(String serviceName,
                                   String httpUrl,
                                   String httpStatus,
                                   int requestSize,
                                   int responseSize,
                                   long httpLatency,
                                   long requestTs,
                                   long responseTs,
                                   String requestId,
                                   String targetUserId,
                                   String gameServerId)
```

**Set extra properties to RUM events**

Sometimes you might want to attach extra properties to RUM events. You can set string properties by calling the `setExtraProperty()` method. Note that you can set more than one extra property by calling this method multiple times. Once set, these properties will be stored and attached to every RUM events. You can call the `eraseExtraProperty()` to erase one property.

```java
FunPlusSDK.getFunPlusRUM().setExtraProperty(key, value);
FunPlusSDK.getFunPlusRUM().eraseExtraProperty(key);
```

### The Data Module

The Data module traces client events and uploads them to FunPlus BI System.

**Set extra properties to Data events**

```java
FunPlusSDK.getFunPlusData().setExtraProperty(key, value);
FunPlusSDK.getFunPlusData().eraseExtraProperty(key);
```

## FAQ
