package co.nebulabs.phonehome;

import java.util.List;

import android.util.Log;

public final class PhoneHomeConfig {
	private boolean isEnabled;
	private PhoneHomeSink logSink;
	private int debugLogLevel;
	private int productionLogLevel;

	private int batchSize;
	private int flushIntervalSeconds;

	private PhoneHomeConfig() {
		this.isEnabled = false;
		this.logSink = new DefaultSink();
		this.debugLogLevel = Log.VERBOSE;
		this.productionLogLevel = Log.INFO;

		this.batchSize = 100;
		this.flushIntervalSeconds = 1800;
	}

	private static volatile PhoneHomeConfig INSTANCE = null;

	public static synchronized PhoneHomeConfig getInstance() {
		if (INSTANCE == null)
			INSTANCE = new PhoneHomeConfig();
		return INSTANCE;
	}

	/**
	 * If enabled == true, you must provide a sink to accept the flushed logs.
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		return isEnabled;
	}

	public PhoneHomeConfig enabled(final boolean isEnabled) {
		this.isEnabled = isEnabled;
		return this;
	}

	/**
	 * Configure the sink for flushing logs externally. Required if PhoneHome
	 * is enabled.
	 * 
	 * @return
	 */
	public PhoneHomeSink getLogSink() {
		return logSink;
	}

	public PhoneHomeConfig logSink(final PhoneHomeSink logSink) {
		this.logSink = logSink;
		return this;
	}

	/**
	 * Lines at and above this log level will be written to logcat in debug
	 * builds.
	 * 
	 * Defaults to Log.VERBOSE.
	 * 
	 * @return
	 */
	public int getDebugLogLevel() {
		return debugLogLevel;
	}

	public PhoneHomeConfig debugLogLevel(final int debugLogLevel) {
		this.debugLogLevel = debugLogLevel;
		return this;
	}

	/**
	 * Lines at and above this log level will be written to logcat in non-debug
	 * builds.
	 * 
	 * Defaults to Log.INFO.
	 * 
	 * @return
	 */
	public int getProductionLogLevel() {
		return productionLogLevel;
	}

	public PhoneHomeConfig productionLogLevel(final int productionLogLevel) {
		this.productionLogLevel = productionLogLevel;
		return this;
	}

	/**
	 * Get and set batch size for log events before flushing. If your sink sends log
	 * lines across a network, larger values send more data in fewer batches.
	 * Smaller values send smaller, more-frequent bursts of data.
	 * 
	 * Defaults to 100 log events.
	 * 
	 * @return
	 */
	public int getBatchSize() {
		return batchSize;
	}

	public PhoneHomeConfig batchSize(final int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	/**
	 * If we haven't sent a batch in IntervalSeconds seconds, flush logs, regardless
	 * of whether we have a full batch. This way, we won't have logs linger on the
	 * client, waiting to be sent.
	 * 
	 * Defaults to 1800 seconds, or 30 minutes.
	 * 
	 * @return
	 */
	public int getFlushIntervalSeconds() {
		return flushIntervalSeconds;
	}

	public PhoneHomeConfig flushIntervalSeconds(final int flushIntervalSeconds) {
		this.flushIntervalSeconds = flushIntervalSeconds;
		return this;
	}

	private static class DefaultSink implements PhoneHomeSink {
		@Override
		public void flushLogs(final List<PhoneHomeLogEvent> logEvents) {
			if (PhoneHomeConfig.getInstance().isEnabled())
				throw new IllegalStateException(
						"You must provide a PhoneHomeSink if PhoneHome is enabled! See PhoneHomeConfig.logSink()");
		}
	}
}