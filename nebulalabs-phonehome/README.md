PhoneHome
=========

Installation
-------------

This is an Android library project. To use it:

1. Download the library.
2. [Add it as an existing project](http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Ftasks%2Ftasks-importproject.htm) in Eclipse.
3. In your project settings, select Properties > Android and add the PhoneHome library as an external library.

Setup
-------------

Configure PhoneHome in the `onCreate()` of your main activity (the one associated with the `android.intent.action.MAIN` intent). PhoneHome must be configured initially, but it can always be changed or turned off later.

The most important configuration options are `enable()` and `logSink()`. `enable()` toggles log flushing to the backend, and `logSink()` specifies how the logs are flushed. When a batch of logs is ready to be flushed, it’s passed to the `flushLogs()` method of the PhoneHomeSink object specified in `logSink()`.

Here's an example configuration:

    PhoneHomeConfig.getInstance()
        // enable or disable log flushing
        .enabled(false)
        // wait for this many log events before flushing ...
        .batchSize(100)
        // ... or until this many seconds have passed between flushes
        .flushIntervalSeconds(1800)
        // when developing, log all messages to logcat, then flush everything to the sink
        .debugLogLevel(android.util.Log.VERBOSE)
        // in production, only log INFO messages and above to logcat, then flush everything to the sink
        .productionLogLevel(android.util.Log.INFO)
        // specify the sink that receives flushed logs. Required if you enable log flushing
        .logSink(new PhoneHomeSink() {
            public void flushLogs(final List<PhoneHomeLogEvent> logEvents) {
                // flush log events to your backend
            }
        });

If you set `enabled(true)`, you must provide a valid `PhoneHomeSink` to `logSink`.  The default sink will raise an exception when called.

Collection
-------------
Using PhoneHome's logger instead of Android's system logger is easy.

First, here’s a typical pattern that uses Android’s system logger:

    public class MyClass {
        private static final TAG = “MyClass”;

        MyClass() {
            Log.d(TAG, “Debug!”);
            Log.i(TAG, “Info!”);
            Log.e(TAG, “Oh dear.”);
        }
    }

PhoneHome’s logger works similarly. First, construct a `PhoneHomeLogger` instance for each `TAG`. (Now, you don’t have to type the first parameter over and over.) Then, use the PhoneHomeLogger instance as you would Android’s system logger:

    public class MyClass {
        private static final PhoneHomeLogger Log = PhoneHomeLogger.forClass(MyClass .class);

        MyClass() {
            Log.d(“Debug!”);
            Log.i(“Info!”);
            Log.e(“Oh dear.”);
        }
    }

Eligibility
-------------
To avoid collecting unnecessary logs (and save your users' batteries and data plans!), we recommend checking if a user matches a particular criteria set before flushing logs. We’ve included an example of this in the [example app and backend](https://github.com/nebulabsnyc/PhoneHome/tree/master/example>). Once you've determined whether a user should phone logs home, enabling, disabling, or re-enabling PhoneHome is as easy as:

    boolean isEligible =...; // determine eligibility
    PhoneHomeConfig.getInstance()
        .enabled(isEligible);

Shipment
-------------
When a batch of logs is ready, it's passed to your `PhoneHomeSink` object. From there, you choose what to do, though typically, we think you'll want to send it to your backend with a network request. Since there isn’t a standard Android networking library and backend APIs are different, you’ll want to work it in to your existing patterns for network requests and API calls. Here’s an [example](https://github.com/nebulabsnyc/PhoneHome/tree/master/example/backend) of how you might do with this with the AndroidHttpClient.

We recommend specifying the device configuration associated with log events to simplify de-duping. One strategy is sending along the Android device model, SDK version, app versionCode, username, and/or other identifying information with the log events. After all, logs from a misbehaving device aren't very helpful if you can't tell from which device they came.

Our [example app and backend](https://github.com/nebulabsnyc/PhoneHome/tree/master/example) show one way to send device information with the requests.

Display
-------------
We built a barebones, Bootstrap’d web dashboard to display logs we collected, but you can just as easily look at the lines, by user and device, in your database. In the sample backend application, the logs are stored in the `logcat_events` table.

Similarly, you could set user-log configurations using a simple web form or by editing the database directly.
