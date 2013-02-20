package fr.perigee.java.maven.growl.extension;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.maven.cli.ExecutionEventLogger;
import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionEvent.Type;
import org.apache.maven.execution.ExecutionListener;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.google.code.jgntp.Gntp;
import com.google.code.jgntp.GntpApplicationInfo;
import com.google.code.jgntp.GntpClient;
import com.google.code.jgntp.GntpNotification;
import com.google.code.jgntp.GntpNotificationBuilder;
import com.google.code.jgntp.GntpNotificationInfo;

import fr.perigee.java.growl.plugin.GrowlUtils;

/**
 * A maven execution listener sending various notifications on various build
 * events. When started, it immediatly instanciates a long-term growl client
 * that will be used to send all events.
 *
 * <h1>How to configure</h1>
 * To configure that, there are some useful properties.
 * All of them can be defined in user settings.xml or in project.
 * <h2>Define global settings in settings.xml</h2>
 *
 * That's quite easy ... in a sense :
 *
 * <pre>
 *     <profiles>
 *         <profile>
 *             <id>maven-growl-extension</id>
 *             <properties>
 *                 <!-- see properties below -->
 *             </properties>
 *         </profile>
 *         ...
 *     </profiles>
 *     ...
 *     <activeProfiles>
 *         <activeProfile>maven-growl-extension</activeProfile>
 *     </activeProfiles>
 * </pre>
 * <h2>Define project-local settings</h2>
 * That you already know, no ?
 * <pre>
 *     <properties>
 *         ...
 *         <!-- Your project properties -->
 *         <!-- see properties below -->
 *     </properties>
 * </pre>
 *
 * <h1>Used properties</h1>
 * Few are the used properties
 * <h2>{@value #EVENTS_TYPES}</h2>
 * Contains a list of values (pulled from ExecutionListener.Type) separated by the ',' character.  More info
 * on that parameter can be found in #loadNotifyingEvents
 *
 * <h1>Why an ExecutionListene</h1>
 * In maven, there are other interfaces that could have taken that role, but, as @olamy said once
 * <blockquote>
possible to implement org.apache.maven.eventspy.EventSpy but the interface need to do a lot of instance of
IMHO that's a very crappy design
 * </blockquote>
 * Yes, Olivier, indeed, the current interface allowed me to easily distinguish between extension startup
 * (in #sessionStarted and #projectStarted) and other extensions methods, which simply notify growl according to configuration.
 * 
 * @author ndx
 */
@Component(role = ExecutionListener.class, hint = "growl-notification")
public class GrowlExtension extends AbstractExecutionListener implements Initializable
{
    /**
     * Constant containing the name of the property used to list the event types for which a notification will be sent
     * @see #loadNotifyingEvents(java.util.Properties)
     */
	private static final String EVENTS_TYPES = "maven-growl-extension.events.types";

    /**
     * identifier used for growl application
     */
	public static final String MAVEN_EXTENSION = "maven-growl-extension";

    /**
     * Prefix used for all additionnal headers
     */
	public static final String PREFIX = Gntp.APP_SPECIFIC_HEADER_PREFIX + MAVEN_EXTENSION;

    /**
     * Component used to fire notifications to growl
     */
	private GntpClient client;

	private GntpApplicationInfo applicationInfo;

	@Requirement
	private Logger logger;

    /**
     * the next executor in stack ... existing just because of a feature lack in maven
     * @see http://t.co/xyiEMyVH
     */
	private ExecutionListener delegate;

    /**
     * events for which notifications will be sent.
     * Defaults to
     ExecutionEvent.Type.ProjectStarted,
     ExecutionEvent.Type.ProjectSucceeded,
     ExecutionEvent.Type.ProjectFailed
     */
	private List<Type> notifyingEvents = Arrays.asList(
					// project lifecycle main events
					ExecutionEvent.Type.ProjectStarted, 
					ExecutionEvent.Type.ProjectSucceeded, 
					ExecutionEvent.Type.ProjectFailed
    );

	@Override
	public void initialize() throws InitializationException {
		applicationInfo = Gntp.appInfo(MAVEN_EXTENSION).icon(GrowlUtils.getIcon()).build();
		delegate = new ExecutionEventLogger(logger);
	}

	public void configure(ExecutionListener executionListener) {
		this.delegate = executionListener;
	}

	@Override
	public void sessionStarted(ExecutionEvent event) {
		client = Gntp.client(applicationInfo).build();
		client.register();
	}

	@Override
	public void sessionEnded(ExecutionEvent event) {
		try {
			client.shutdown(1, TimeUnit.SECONDS);
		} catch (Exception e) {
			logger.error("unable to shut down properly growl extension", e);
		}
	}

	@Override
	public void projectStarted(ExecutionEvent event) {
		loadSettings(event);
		delegate.projectStarted(event);
		notify(event);
	}

	@Override
	public void projectFailed(ExecutionEvent event) {
		delegate.projectFailed(event);
		notify(event);
	}

	@Override
	public void projectSucceeded(ExecutionEvent event) {
		delegate.projectSucceeded(event);
		notify(event);
	}
	
	@Override
	public void projectDiscoveryStarted(ExecutionEvent event) {
		delegate.projectDiscoveryStarted(event);
		notify(event);
	}
	
	@Override
	public void projectSkipped(ExecutionEvent event) {
		delegate.projectSkipped(event);
		notify(event);
	}
	
	@Override
	public void forkedProjectFailed(ExecutionEvent event) {
		delegate.forkedProjectFailed(event);
		notify(event);
	}

	@Override
	public void forkedProjectSucceeded(ExecutionEvent event) {
		delegate.forkedProjectSucceeded(event);
		notify(event);
	}
	
	@Override
	public void forkedProjectStarted(ExecutionEvent event) {
		delegate.forkedProjectStarted(event);
		notify(event);
	}
	
	@Override
	public void forkFailed(ExecutionEvent event) {
		delegate.forkFailed(event);
		notify(event);
	}

	@Override
	public void forkSucceeded(ExecutionEvent event) {
		delegate.forkSucceeded(event);
		notify(event);
	}
	
	@Override
	public void forkStarted(ExecutionEvent event) {
		delegate.forkStarted(event);
		notify(event);
	}
	
	@Override
	public void mojoFailed(ExecutionEvent event) {
		delegate.mojoFailed(event);
		notify(event);
	}

	@Override
	public void mojoSucceeded(ExecutionEvent event) {
		delegate.mojoSucceeded(event);
		notify(event);
	}
	
	@Override
	public void mojoStarted(ExecutionEvent event) {
		delegate.mojoStarted(event);
		notify(event);
	}
	
	
	@Override
	public void mojoSkipped(ExecutionEvent event) {
		delegate.mojoSkipped(event);
		notify(event);
	}

	private void loadSettings(ExecutionEvent event) {
		Properties properties;
		if(event.getProject()!=null && (properties = event.getProject().getProperties())!=null) {
			// some properties may change the way notification are sent. try to read them ...
			loadNotifyingEvents(properties);
		}
	}

	/**
	 * Read the {@value #EVENTS_TYPES} property and set the events sending notifications from that property.
	 * This is done by changing the value of {@link #notifyingEvents}
	 * @param properties
	 */
	private void loadNotifyingEvents(Properties properties) {
		if(properties.containsKey(EVENTS_TYPES)) {
			List<ExecutionEvent.Type> eventsSendingNotifications = new LinkedList<ExecutionEvent.Type>();
			String types = properties.getProperty(EVENTS_TYPES);
			for(String type : types.split(",")) {
				try {
					eventsSendingNotifications.add(ExecutionEvent.Type.valueOf(type));
				} catch(Exception e) {
					logger.warn("impossible to find an ExecutionEvent.Type for String \""+type+"\"\n" +
							"As a reminder, possible values should be separated by the ',' character\n" +
							"And possible values are "+ExecutionEvent.Type.values(), e);
				}
			}
			logger.debug("using as events sending notifications "+eventsSendingNotifications);
			this.notifyingEvents = eventsSendingNotifications;
		}
	}

	/**
	 * Notify given event at given severity
	 * 
	 * @param event
	 */
	private void notify(ExecutionEvent event) {
		if(notifyingEvents.contains(event.getType())) {
			GntpNotification notification = notificationFor(event);
			if (notification != null) {
				try {
					client.notify(notification, 1, TimeUnit.SECONDS);
				} catch(Exception e) {
					logger.warn("unable to growl notification\n"+notification.getTitle()+"\n"+notification.getText(), e);
				}
			}
		}
	}

	/**
	 * Converts the maven execution event into a growl notification
	 * 
	 * @param event
	 *            source event
	 * @return generated notification
	 */
	private GntpNotification notificationFor(ExecutionEvent event) {
		try {
			String text = buildNotificationText(event);
			GntpNotificationInfo info = Gntp.notificationInfo(applicationInfo, text).build();
			return buildNotification(event, info);
		} catch (Exception e) {
			logger.warn("fail to notify growl for event "+event+": " + e.getMessage());
		}
		return null;
	}

	private GntpNotification buildNotification(ExecutionEvent event, GntpNotificationInfo info) {
		GntpNotificationBuilder builder;
		if (event.getProject() == null) {
			builder = Gntp.notification(info, "maven-growl-extension without project");
		} else {
			builder = Gntp.notification(info, event.getProject().toString());
			builder = builder.header(PREFIX + "project-groupId", event.getProject().getGroupId())
							.header(PREFIX + "project-artifactId", event.getProject().getArtifactId())
							.header(PREFIX + "project-name", event.getProject().getName())
							.header(PREFIX + "project-description", event.getProject().getDescription());
		}
		if (event.getType() != null) {
			builder = builder.header(PREFIX + "event-type", event.getType().name());
		}
		if (event.getMojoExecution() != null) {
			builder = builder.header(PREFIX + "mojo-executionId", event.getMojoExecution().getExecutionId())
							.header(PREFIX + "mojo-artifactId", event.getMojoExecution().getArtifactId())
							.header(PREFIX + "mojo-groupId", event.getMojoExecution().getGroupId())
							.header(PREFIX + "mojo-goal", event.getMojoExecution().getGoal())
							.header(PREFIX + "mojo-phase", event.getMojoExecution().getLifecyclePhase());
		}
		return builder.text(buildNotificationText(event)).build();
	}

	private String buildNotificationText(ExecutionEvent event) {
		StringBuilder notificationText = new StringBuilder();
		notificationText.append(event.getType().name()).append("\n");
		if(event.getSession()!=null) {
			notificationText.append("in session ").append(event.getSession().toString()).append("\n");
		}
		if(event.getProject()!=null) {
			notificationText.append("in project ").append(event.getProject().toString());
		}
		if(event.getMojoExecution()!=null) {
			notificationText.append("with mojo ").append(event.getMojoExecution().identify());
		}
		String text = notificationText.toString();
		logger.debug(text);
		return text;
	}
}
