package co.nebulabs.phonehome;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import android.util.Log;

public final class PhoneHomeLogger {
	private final String TAG;

	private PhoneHomeLogger(final String tag) {
		this.TAG = tag;
	}

	public static PhoneHomeLogger forTag(final String tag) {
		return new PhoneHomeLogger(tag);
	}

	public static PhoneHomeLogger forClass(final Class<?> cls) {
		return new PhoneHomeLogger(cls.getSimpleName());
	}

	private boolean logToLogcat(final int level) {
		PhoneHomeConfig config = PhoneHomeConfig.getInstance();

		return (BuildConfig.DEBUG) ? level >= config.getDebugLogLevel()
				: level >= config.getProductionLogLevel();
	}

	private boolean logToSink() {
		return PhoneHomeConfig.getInstance().isEnabled();
	}

	public void v(final String msg) {
		if (logToLogcat(Log.VERBOSE))
			Log.v(TAG, msg);

		maybeSendLog(Log.VERBOSE, msg);
	}

	public void v(final String msg, final Throwable throwable) {
		if (logToLogcat(Log.VERBOSE))
			Log.v(TAG, msg, throwable);

		maybeSendLog(Log.VERBOSE, msg, throwable);
	}

	public void d(final String msg) {
		if (logToLogcat(Log.DEBUG))
			Log.d(TAG, msg);

		maybeSendLog(Log.DEBUG, msg);
	}

	public void d(final String msg, final Throwable throwable) {
		if (logToLogcat(Log.DEBUG))
			Log.d(TAG, msg, throwable);

		maybeSendLog(Log.DEBUG, msg, throwable);
	}

	public void i(final String msg) {
		if (logToLogcat(Log.INFO))
			Log.i(TAG, msg);

		maybeSendLog(Log.INFO, msg);
	}

	public void i(final String msg, final Throwable throwable) {
		if (logToLogcat(Log.INFO))
			Log.i(TAG, msg, throwable);

		maybeSendLog(Log.INFO, msg, throwable);
	}

	public void w(final String msg) {
		if (logToLogcat(Log.WARN))
			Log.w(TAG, msg);

		maybeSendLog(Log.WARN, msg);
	}

	public void w(final String msg, final Throwable throwable) {
		if (logToLogcat(Log.WARN))
			Log.w(TAG, msg, throwable);

		maybeSendLog(Log.WARN, msg, throwable);
	}

	public void e(final String msg) {
		if (logToLogcat(Log.ERROR))
			Log.e(TAG, msg);

		maybeSendLog(Log.ERROR, msg);
	}

	public void e(final String msg, final Throwable throwable) {
		if (logToLogcat(Log.ERROR))
			Log.e(TAG, msg, throwable);

		maybeSendLog(Log.ERROR, msg, throwable);
	}

	public void wtf(final String msg) {
		if (logToLogcat(Log.ASSERT))
			Log.wtf(TAG, msg);

		maybeSendLog(Log.ASSERT, msg);
	}

	public void wtf(final String msg, final Throwable throwable) {
		if (logToLogcat(Log.ASSERT))
			Log.wtf(TAG, msg, throwable);

		maybeSendLog(Log.ASSERT, msg, throwable);
	}

	private void maybeSendLog(final int level, final String message) {
		if (logToSink())
			PhoneHomeLogQueue.getInstance().enqueue(new Date(), level, TAG,
					message);
	}

	private void maybeSendLog(final int level, final String message,
			final Throwable throwable) {
		maybeSendLog(level, message + "\n" + getStackTraceAsString(throwable));
	}

	private static String getStackTraceAsString(final Throwable throwable) {
		// lifted from Guava's Throwables.getStackTraceAsString()
		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}
}
