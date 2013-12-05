PhoneHome
=========

Installation
-------------

This is an Android library project. To use it:

1. Download the library.
2. [Add it as an existing project](http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Ftasks%2Ftasks-importproject.htm) in Eclipse.
3. In your project settings, select Properties > Android and add the PhoneHome library as an external library.

Set up
-------------

Set up the configuration for PhoneHome somewhere in your main activity. PhoneHome must be configured initially, but it can always be changed or turned off later.

The most important configuration options are enable() and logSink(). enable() toggles log flushing to the backend, and logSink() specifies how the logs are flushed. When a batch of logs is ready to be flushed, it’s passed to the flushLog() method of the PhoneHomeSink object specified in logSink().

Here's an example configuration:

    PhoneHomeConfig.getInstance()
        .enabled(false)     // set .enabled(..) to true to enable log flushing
        .batchSize(100)     // wait until we have this many events to flush a batch of logs...
        .flushIntervalSeconds(1800)    // ... or until this many seconds have passed since our last flush
        .debugLogLevel(android.util.Log.VERBOSE)        // when developing, log all messages to logcat (everything is flushed to our sink)
        .productionLogLevel(android.util.Log.INFO)      // in production, only log INFO messages and above to logcat (everything is flushed to our sink)
        // specify the sink that receives flushed logs (required if you ever enable log flushing!)
        .logSink(new PhoneHomeSink() {
            public void flushLogs(final List<PhoneHomeLogEvent> logEvents) {
                // flush the log events to your backend
            }
        });

If you set `enabled(true)`, you must provide a valid `PhoneHomeSink` to `logSink`.  The default sink will raise an exception when called.