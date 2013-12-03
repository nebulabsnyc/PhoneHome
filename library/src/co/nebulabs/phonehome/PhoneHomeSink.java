package co.nebulabs.phonehome;

import java.util.List;

public interface PhoneHomeSink {
	/**
	 * Flush log events externally somewhere.
	 * 
	 * You may want to send them to your own server or perhaps to some third party.
	 * @param logEvents
	 */
	public void flushLogs(List<PhoneHomeLogEvent> logEvents);
}
