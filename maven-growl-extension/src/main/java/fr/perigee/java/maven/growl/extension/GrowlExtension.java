package fr.perigee.java.maven.growl.extension;

import org.apache.maven.execution.AbstractExecutionListener;
import org.codehaus.plexus.component.annotations.Component;

/**
 * A maven execution listener sending various notifications on various build events
 * @author ndx
 *
 */
@Component(role = GrowlExtension.class)
public class GrowlExtension extends AbstractExecutionListener {
	public GrowlExtension() {
		
	}
}
