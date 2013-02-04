package fr.perigee.java.growl.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import com.google.code.jgntp.Gntp;
import com.google.code.jgntp.GntpApplicationInfo;
import com.google.code.jgntp.GntpClient;
import com.google.code.jgntp.GntpListener;
import com.google.code.jgntp.GntpNotificationInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * Generates a GNTP notification when invoked. This abstract class is to be
 * overwritten numerous times, believe me !
 */
public abstract class AbstractNotifier extends AbstractMojo {
	private static final String MAVEN_ICON = "http://maven.apache.org/images/maven-logo-2.gif";
	
	private URI mavenIconUri;
	/**
	 * The message to output
	 * 
	 * @parameter expression="${growl.message}" default-value="Hello World!"
	 */
	private String message;
	

	/**
	 * Maven project object, used to get project name
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	public MavenProject project;
	private GntpApplicationInfo applicationInfo;


	public final void execute() throws MojoExecutionException {
		try {
			mavenIconUri = new URI(MAVEN_ICON);
			GntpClient client = obtainClient();
			client.register();
			notify(client);
			client.shutdown(getShutdownDelay(), TimeUnit.SECONDS);
		} catch(Exception e) {
			throw new MojoExecutionException("unable to run GNTP client correctly", e);
		}
	}

	/**
	 * Perform effective notification. This method can be overwritten at will ... Default implementation simply outputs the given message with a title being current project
	 * @param client
	 * @throws InterruptedException 
	 */
	protected void notify(GntpClient client) throws InterruptedException {
		client.notify(Gntp.notification(basicNotification(), 
						project.getName()==null ? project.getArtifactId() : project.getName())
						.text(message)
						.withoutCallback()
						.build());
	}

	private GntpNotificationInfo basicNotification() {
		return Gntp.notificationInfo(getApplicationInfo(), "basic notification").icon(mavenIconUri).build();
	}
	
	protected long getNotificationDelay() {
		return 5;
	}

	protected long getShutdownDelay() {
		return 5;
	}

	/**
	 * Obtains a growl client and initialize it with style
	 * 
	 * @return
	 */
	protected final GntpClient obtainClient() {
		return Gntp.client(getApplicationInfo()).listener(getGrowlListener()).build();
	}

	protected final GntpApplicationInfo getApplicationInfo() {
		if(applicationInfo==null) {
			applicationInfo = createApplicationInfo();
		}
		return applicationInfo;
	}

	protected GntpApplicationInfo createApplicationInfo() {
		GntpApplicationInfo created;
		return Gntp.appInfo("maven-growl-plugin").name("Maven growl plugin").icon(mavenIconUri).build();
	}

	protected GntpListener getGrowlListener() {
		return new GntpLoggingListener(getLog(), this);
	}
}
