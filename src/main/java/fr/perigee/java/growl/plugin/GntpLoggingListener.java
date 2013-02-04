package fr.perigee.java.growl.plugin;

import org.apache.maven.plugin.logging.Log;

import com.google.code.jgntp.GntpErrorStatus;
import com.google.code.jgntp.GntpListener;
import com.google.code.jgntp.GntpNotification;

/**
 * Simple GTNP listener that logs all in maven (but at sensible default levels)
 * @author ndx
 *
 */
public class GntpLoggingListener implements GntpListener {

	private Log log;
	private AbstractNotifier source;

	public GntpLoggingListener(Log log, AbstractNotifier source) {
		this.log = log;
		this.source = source;
	}

	public void onRegistrationSuccess() {
		log.debug("successfully registered "+source);
	}

	public void onNotificationSuccess(GntpNotification notification) {
		log.debug("successfully notified "+notification+" from "+source);
	}

	public void onClickCallback(GntpNotification notification) {
	}

	public void onCloseCallback(GntpNotification notification) {
	}

	public void onTimeoutCallback(GntpNotification notification) {
	}

	public void onRegistrationError(GntpErrorStatus status, String description) {
		log.warn("unable to register from "+source+" Growl status is "+status+ " with message "+description);
	}

	public void onNotificationError(GntpNotification notification, GntpErrorStatus status, String description) {
		log.warn("unable to send notification "+notification+" from "+source+" Growl status is "+status+ " with message "+description);
		log.warn("\n============================================================\n"+
				notification.getTitle()+
				"============================================================\n"+
				notification.getText());
	}

	public void onCommunicationError(Throwable t) {
		log.warn("there was a communication problem", t);
	}

}
