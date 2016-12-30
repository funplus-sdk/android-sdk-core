# Changelog

### v4.0.3

* Bugfix: sychronized getter methods for `FunPlusFactory`.
* Feature: builder pattern for `FunPlusConfig`.
* Feature: ability to turn off auto tracing of session events.

### v4.0.2-alpha.3

* Bugfix: should not stopTimer() when activity destroying.

### v4.0.2-alpha.2

* Bugfix: same label for data kpi and custom tracker.

### v4.0.2-alpha.1

* Modify upload interval: 5s for sandbox and 10s for production.
* Modify max queue size to 2000.

### v4.0.2-alpha.0

* Bugfix: wrong behavior for `traceNewUser()`.
* Remove method `startSessionForNewUserId()`.

### v4.0.1

* Increase SDK logs uploading interval.
* Set maximum queue size for SDK logs.

### v4.0.1-alpha.2

* Bugfix: data_version=2.0
* Add install timestamp to data events.
* Remove `currencyReceivedType` from payment events.
* Fields correction in payment events.
* Automatically trace `new_user` events.

### v4.0.1-alpha.1

* `traceCustomEventWithNameAndProperties()`
* Increase uploading interval.
* Set maximum data queue size.
* Remove call stacks from INFO events.

### v4.0.1-alpha.0

- Remove Adjust SDK.
- Deprecate the `ConfigManager` class.
- New `install` API.

### v4.0.0-alpha.4

* Unity bridge.

### v4.0.0-alpha.3

* Add carrier info to RUM events.

### v4.0.0-alpha.2

* Move callbacks of data uploading out of main thread.


