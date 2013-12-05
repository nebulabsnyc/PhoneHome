package co.nebulabs.phonehome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

public final class PhoneHomeLogQueue {
	private static final String TAG = PhoneHomeLogQueue.class.getSimpleName();

	private final BlockingQueue<PhoneHomeLogEvent> queuedEvents = new LinkedBlockingQueue<PhoneHomeLogEvent>();
	private Date lastFlush;

	private PhoneHomeLogQueue() {
		final long flushCheckIntervalMillis;

		if (BuildConfig.DEBUG)
			// 10 seconds while debugging
			flushCheckIntervalMillis = 10000;
		else
			// 1 minute in production
			flushCheckIntervalMillis = 300000;

		(new Timer()).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					maybeFlushQueue();
				} catch (RuntimeException ex) {
					Log.e(TAG, "Caught exception flushing log queue", ex);
				}
			}
		}, flushCheckIntervalMillis, flushCheckIntervalMillis);
	}

	private static PhoneHomeLogQueue INSTANCE = null;

	public static synchronized PhoneHomeLogQueue getInstance() {
		if (INSTANCE == null)
			INSTANCE = new PhoneHomeLogQueue();
		return INSTANCE;
	}

	public void enqueue(final Date when, final int level, final String tag,
			final String message) {
		// initialize lastFlush so we know when to start our minimum flush
		// interval
		if (lastFlush == null)
			lastFlush = new Date();

		queuedEvents.add(new PhoneHomeLogEvent(when, level, tag, message));
	}

	private synchronized void maybeFlushQueue() {
		if (queuedEvents.isEmpty())
			return;

		if (lastFlush == null)
			throw new IllegalStateException(
					"lastFlush should have been initialized by this point");

		final Date now = new Date();
		final int batchSize = PhoneHomeConfig.getInstance().getBatchSize();
		final int flushTimeSeconds = PhoneHomeConfig.getInstance()
				.getFlushIntervalSeconds();

		if (queuedEvents.size() >= batchSize
				|| (now.getTime() - lastFlush.getTime()) > flushTimeSeconds * 1000) {
			flushQueue(batchSize);
			lastFlush = now;
		}
	}

	private void flushQueue(final int batchSize) {
		List<PhoneHomeLogEvent> logEventBatch = new ArrayList<PhoneHomeLogEvent>();
		queuedEvents.drainTo(logEventBatch, batchSize);

		if (!logEventBatch.isEmpty()) {
			// don't use our PhoneHomeLogger, lest this queue up another batch
			Log.d(TAG, "Flushing: " + logEventBatch.size() + " log events.");
			PhoneHomeConfig.getInstance().getLogSink()
					.flushLogs(Collections.unmodifiableList(logEventBatch));
		}
	}
}
