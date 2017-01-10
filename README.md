# FunPlus SDK for Android

## Requirements

* Android Studio 2.2+
* Android API level 16+
* Gradle 2.3+

## Table of Contents

* [Integration](#integration)
  * [Add the SDK to Your Project](#add-the-sdk-to-your-project)
  * [Add Volley Library](#add-volley-library)
  * [Add Google Play Services](#add-google-play-services)
  * [Add Permissions](#add-permissions)
  * [Add Broadcast Receiver](#add-broadcast-receiver)
  * [Install the SDK](#install-the-sdk)
  * [Config the SDK](#config-the-sdk)
* [Usage](#usage)
  * [The ID Module](#the-id-module)
    * [Get an FPID Based on a Given User ID](get-an-fpid-based-on-a-given-user-id)
    * [Bind a New User ID to an Existing FPID](#bind-a-new-user-id-to-an-existing-fpid)
  * [The RUM Module](#the-rum-module)
    - [Trace a Service Monitoring Event](#trace-a-service-monitoring-event)
    - [Set Extra Properties to RUM Events](#set-extra-properties-to-rum-events)
  * [The Data Module](#the-data-module)
    * [Session Starts and Ends](#session-starts-and-ends)
    * [The Payment Event](#the-payment-event)
    * [Trace a Custom Event](#trace-a-custom-event)
    * [Set Extra Properties to Data Events](#set-extra-properties-to-data-events)
* [FAQ](#faq)

## Integration

### Add the SDK to Your Project

Add the `funplus-android-sdk-<version>.jar` file to your app and set as dependency.

### Add Volley Library

FunPlus SDK uses the [Volley Library](https://developer.android.com/training/volley/index.html?hl=pt-br) to make network requests. Please add the Volley Library to your app if you haven't done so. One way of doing this is to add the following line to the `dependencies` block of the `build.gradle` file:

```groovy
compile 'com.android.volley:volley:1.0.0'
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
String rumTag = "{YourRumTag}";
String rumKey = "{YourRumKey}";
SDKEnvironment env = SDKEnvironment.Production;		// Production/Sandbox

FunPlusSDK.install(context, appId, appKey, rumTag, rumKey, env);
FunPlusSDK.registerActivityLifecycleCallbacks(application);
```

### Config the SDK

You may want to override SDK's default config values. In such a case, you need to initialize the SDK in a different way, as the following code snippet illustrates.

```java
import com.funplus.sdk.FunPlusSDK;
import com.funplus.sdk.SDKEnvironment;

Application application = "{YourApplicationInstance}";
Context context = "{YourAppContext}";
String appId = "{YourAppId}";
String appKey = "{YourAppKey}";
String rumTag = "{YourRumTag}";
String rumKey = "{YourRumKey}";
SDKEnvironment env = SDKEnvironment.Production;		// Production/Sandbox

FunPlusConfig funPlusConfig = new FunPlusConfig(context, appId, appKey, rumTag, rumKey, env);

funPlusConfig.setRumUploadInterval(10)
  			.setDataAutoTraceSessionEvents(false)
  			.end();

FunPlusSDK.install(funPlusConfig);
FunPlusSDK.registerActivityLifecycleCallbacks(application);
```

Here's all the config values that can be overrided.

| name                       | type     | description                              |
| -------------------------- | -------- | ---------------------------------------- |
| rumUploadInterval          | Int64    | This value indicates a time interval to trigger a RUM events uploading process. Default is 30. |
| rumSampleRate              | Double   | This value indicates percentage of RUM events to be traced for sampling. Default is 1.0. |
| rumEventWhitelist          | [String] | RUM events in this array will always be traced. Default is an empty array. |
| rumUserWhitelist           | [String] | RUM events produced by users in this array will always be traced. Default is an empty array. |
| rumUserBlacklist           | [String] | RUM events produced by users in this array will never be traced. `rumUesrWhitelist` will be checked before this array. Default is an empty array. |
| dataUploadInterval         | Int64    | This value indicates a time interval to trigger a Data events uploading process. Default is 30. |
| dataAutoTraceSessionEvents | Bool     | If set true, SDK will automatically trace `session_start` and `session_end` events. Default is true. |

## Usage

### The ID Module

The objective of the ID module is to provide a unified ID for each unique user and consequently make it possible to identify users across all FunPlus services (marketing, payment, etc). Note that the ID module can not be treated as an account module, therefore you cannot use this module to complete common account functionalities such as registration and logging in.

#### Get an FPID Based on a Given User ID

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

#### Bind a New User ID to an Existing FPID

```java
import com.funplus.sdk.FunPlusID;

FunPlusSDK.getFunPlusID().bind(fpid, externalID, externalIDType, completionHandler);
```

### The RUM Module

The RUM module monitors user's actions in real-time and uploads collected data to Log Agent.

#### Trace a Service Monitoring Event

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

#### Set Extra Properties to RUM Events

Sometimes you might want to attach extra properties to RUM events. You can set string properties by calling the `setExtraProperty()` method. Note that you can set more than one extra property by calling this method multiple times. Once set, these properties will be stored and attached to every RUM events. You can call the `eraseExtraProperty()` to erase one property.

```java
FunPlusSDK.getFunPlusRUM().setExtraProperty(key, value);
FunPlusSDK.getFunPlusRUM().eraseExtraProperty(key);
```

Keep in mind that a second calling of the `setExtraProperty()` method will override the previous set value.

### The Data Module

The Data module traces client events and uploads them to FunPlus BI System.

#### Session Starts and Ends

Note: If the `dataAutoTraceSessionEvents` configuration field is set to `true`, SDK will trace `session_start` and `session_end` event automatically. Otherwise, you need to call these two methods at correct points to trace session events.

```java
FunPlusSDK.getFunPlusData().traceSessionStart();
FunPlusSDK.getFunPlusData().traceSessionEnd(long sessionLength);
```

#### The Payment Event

```java
FunPlusSDK.getFunPlusData().tracePayment(...);
```

The `tracePayment()` method is defined as below:

```java
class FunPlusData {...

/**
    Shall be called when user purchase some product.
     
    params amount:             Numeric value which corresponds to the cost of the purchase in the monetary unit multiplied by 100.
    params currency:           The 3-letter ISO 4217 resource Code. [ISO4217](http://www.xe.com/iso4217.php)
    params productId:          The ID of the product purchased.
    params productName:        The name of the product purchased (optional).
    params productType:        The type of the product purchased (optional).
    params transactionId:      The unique transaction ID sent back by the payment processor.
    params paymentProcessor:   The payment processor.
    params itemsReceived:      A string of JSON array, consisting of one or more items received.
    params currencyReceived:   A string of JSON array, consisting one or more types of currency received.
 */
public void tracePayment(double amount,
                         String currency,
                         String productId,
                         String productName,
                         String productType,
                         String transactionId,
                         String paymentProcessor,
                         String itemsReceived,
                         String currencyReceived);
```

The `itemsReceived` parameter contains one or more items received. It consists of the following required fields:

- `d_item_id`: The item id
- `d_item_name`: The item name
- `d_item_type`: The type of item e.g. booster, lives, fertilizer
- `m_item_amount`: The number of items received
- `d_item_class`: The item class, one of - consumable or durable

Example: 

```json
"c_items_received": [
  {
    "d_item_id":"4312",
    "d_item_name":"booster_butterfly",
    "d_item_type":"booster",
    "m_item_amount":"1",
    "d_item_class":"consumable"
  }
]
```

The `currencyReceived` parameter contains one or more types of currency received. It consists of the following required fields:

- `m_currency_amount`: The virtual currency amount
- `d_currency_type`: The type of virtual currency.

Example:

```json
"c_currency_received": [
  {
    "d_currency_type":"rc",
    "m_currency_amount":"20"
  },
  {
    "d_currency_type":"coins",
    "m_currency_amount":"2000"
  }
]
```

#### Trace a Custom Event

```java
FunPlusSDK.getFunPlusData().traceCustom(event);
```

Besides those four KPI events, you might want to trace some custom events. Call the `traceCustom()` method to achieve this task.

The event you're passing in to this method is a dictionary. Below is an example:

```json
{
    "app_id": "{YourAppId}",
    "data_version": "2.0",
    "event": "level_up",
    "user_id": "{UserId}",
    "session_id": "{SessionId}",
    "ts": "{Timestamp(millisecond)}",
    "properties": {
        "app_version": "{YourAppId}",
        "os": "{android or ios}",
        "os_version": "{OsVersion}",
        "device": "{DeviceName}",
        "lang": "{LanguageCode, for example: 'en'}",
        "install_ts": "{Timestamp(millisecond)}",
        "other_properties": "..."
    }
```

#### Set Extra Properties to Data Events

```java
FunPlusSDK.getFunPlusData().setExtraProperty(key, value);
FunPlusSDK.getFunPlusData().eraseExtraProperty(key);
```

Keep in mind that a second calling of the `setExtraProperty()` method will override the previous set value.

## FAQ

**Q: Why the hell is the parameter list of  `traceServiceMonitoring()` so long?**

A: Please consult RUM team on that :)

**Q: What is `bindFPID()` for and when should I use it?**

A: In most cases you are not gonna use this method. For cases that one player binds his/her game account to different social accounts, you need to call this method.

Below is an example:

```java
string fpid = FunPlusSDK.getFunPlusID().getFPID("testuser@funplus.com", ExternalIDType.Email, ...);

// When player binds his/her account with Facebook.
FunPlusSDK.getFunPlusID().bindFPID(fpid, "fb1234", ExternalIDType.FacebookID, ...);
```

