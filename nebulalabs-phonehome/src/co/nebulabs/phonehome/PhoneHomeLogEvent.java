package co.nebulabs.phonehome;

import java.util.Date;

public final class PhoneHomeLogEvent {
	private final long timestamp;
	private final int level;
	private final String tag;
	private final String message;

	public PhoneHomeLogEvent(final long timestamp, final int level,
			final String tag, final String message) {
		this.timestamp = timestamp;
		this.level = level;
		this.tag = tag;
		this.message = message;
	}

	public PhoneHomeLogEvent(final Date when, final int level,
			final String tag, final String message) {
		this(when.getTime(), level, tag, message);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getLevel() {
		return level;
	}

	public String getTag() {
		return tag;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "PhoneHomeLogEvent [timestamp=" + timestamp + ", level=" + level
				+ ", tag=" + tag + ", message=" + message + "]";
	}
}
