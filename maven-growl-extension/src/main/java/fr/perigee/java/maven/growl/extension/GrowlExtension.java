package fr.perigee.java.maven.growl.extension;

import java.util.concurrent.TimeUnit;


import org.apache.maven.cli.ExecutionEventLogger;
import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
import org.codehaus.plexus.component.annotations.Component;

import com.google.code.jgntp.Gntp;
import com.google.code.jgntp.GntpApplicationInfo;
import com.google.code.jgntp.GntpClient;
import com.google.code.jgntp.GntpNotification;
import com.google.code.jgntp.GntpNotificationInfo;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * A maven execution listener sending various notifications on various build events.
 * When started, it immediatly instanciates a long-term growl client that will be
 * used to send all events
 *
 * @author ndx
 */
@Component( role = ExecutionListener.class, hint = "growl-notification" )
public class GrowlExtension
    extends AbstractExecutionListener
    implements Initializable
{
    public static final String MAVEN_EXTENSION = "maven-growl-extension";

    public static final String PREFIX = Gntp.APP_SPECIFIC_HEADER_PREFIX + MAVEN_EXTENSION;

    ;

    private GntpClient client;

    private GntpApplicationInfo applicationInfo;

    @Requirement
    private Logger logger;

    private ExecutionEventLogger delegate;

    public GrowlExtension()
    {

    }


    @Override
    public void initialize()
        throws InitializationException
    {
        applicationInfo = Gntp.appInfo( MAVEN_EXTENSION ).build();
        this.delegate = new ExecutionEventLogger( logger );
    }

    @Override
    public void sessionStarted( ExecutionEvent event )
    {
        client = Gntp.client( applicationInfo ).build();
        client.register();
    }

    @Override
    public void sessionEnded( ExecutionEvent event )
    {
        try
        {
            client.shutdown( 1, TimeUnit.SECONDS );
        }
        catch ( Exception e )
        {
            logger.error( "unable to shut down properly growl extension", e );
        }
    }

    @Override
    public void projectStarted( ExecutionEvent event )
    {
        delegate.projectStarted( event );
        notify( event );
    }

    @Override
    public void projectFailed( ExecutionEvent event )
    {
        delegate.projectFailed( event );
        notify( event );
    }

    @Override
    public void projectSucceeded( ExecutionEvent event )
    {
        delegate.projectSucceeded( event );
        notify( event );
    }

    /**
     * Notify given event at given severity
     *
     * @param event
     */
    private void notify( ExecutionEvent event )
    {
        GntpNotification notification = notificationFor( event );
        if ( notification != null )
        {
            client.notify( notification );
        }
    }

    /**
     * Converts the maven execution event into a growl notification
     *
     * @param event source event
     * @return generated notification
     */
    private GntpNotification notificationFor( ExecutionEvent event )
    {
        try
        {
            GntpNotificationInfo info =
                Gntp.notificationInfo( applicationInfo, event.getMojoExecution().identify() ).build();
            GntpNotification returned =
                Gntp.notification( info, event.getProject().toString() ).header( PREFIX + "event-type",
                                                                                 event.getType().name() ).header(
                    PREFIX + "project-groupId", event.getProject().getGroupId() ).header( PREFIX + "project-artifactId",
                                                                                          event.getProject().getArtifactId() ).header(
                    PREFIX + "project-name", event.getProject().getName() ).header( PREFIX + "project-description",
                                                                                    event.getProject().getDescription() ).header(
                    PREFIX + "mojo-executionId", event.getMojoExecution().getExecutionId() ).header(
                    PREFIX + "mojo-artifactId", event.getMojoExecution().getArtifactId() ).header(
                    PREFIX + "mojo-groupId", event.getMojoExecution().getGroupId() ).header( PREFIX + "mojo-goal",
                                                                                             event.getMojoExecution().getGoal() ).header(
                    PREFIX + "mojo-phase", event.getMojoExecution().getLifecyclePhase() ).build();
            return returned;
        }
        catch ( Exception e )
        {
            logger.warn( "fail to notify growl: " + e.getMessage() );
        }
        return null;
    }
}
