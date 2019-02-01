package com.axway.apim;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;

public abstract  class AbstractAPImanagerMojo extends AbstractMojo {

	@Parameter
	protected String groupName;
	@Parameter
	protected String instanceName;
	@Parameter
	protected String host;
	@Parameter
	protected int port;
	@Parameter
	protected String protocol;

	@Parameter
	protected String username;

	@Parameter
	protected String password;

	@Parameter
	protected String polFilePath;

	@Parameter
	protected String envFilePath;

	@Parameter
	protected String fedFilePath;

	@Parameter
	protected String type;

	protected Log logger = getLog();
	
	
}
