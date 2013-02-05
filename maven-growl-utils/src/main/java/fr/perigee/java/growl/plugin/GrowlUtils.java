package fr.perigee.java.growl.plugin;

import java.net.URI;
import java.net.URISyntaxException;

public class GrowlUtils {

	public static final String MAVEN_ICON = "http://maven.apache.org/images/maven-logo-2.gif";

	private static URI mavenIconUri;
	
	public static URI getIcon() {
		if(mavenIconUri==null) {
			try {
				mavenIconUri = new URI(MAVEN_ICON);
			} catch (URISyntaxException e) {
				// URI syntax is correct
			}
		}
		return mavenIconUri;
	}

	public static final int SHUTDOWN_DELAY = 5;
}
