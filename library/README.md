PhoneHome
=========

This is an Android library project.  To use it, add it as an existing project in Eclipse.  In your project, select "Properties>Android" and add a reference to it as an external library.

Configuration
-------------

To configure PhoneHome, access the PhoneHomeConfig singleton and set properties as in the provided sample (samples/PhoneHomeTest)

    PhoneHomeConfig.getInstance()
    // disable sending log flushing for now
    .enabled(false)
    // wait until we have this many events to flush a batch of logs...
    .batchSize(100)
    // ... or until this many seconds have passed since our last flush
    .flushIntervalSeconds(1800)
    // when developing, log all messages to logcat (everything is flushed to our sink)
    .debugLogLevel(android.util.Log.VERBOSE)
    // in production, only log INFO messages and above to logcat (everything is flushed to our sink)
    .productionLogLevel(android.util.Log.INFO)
    // the actual sink used when it's time to flush logs (required if you ever enable log flushing!)
    .logSink(new ExampleSink(this));

Please note that if you set enabled(true), you must provide a valid PhoneHomeSink to logSink.  The default sink will raise an exception when called.
