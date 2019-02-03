package com.axway.apim;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;

public abstract  class AbstractAPImanagerMojo extends AbstractMojo {

	
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
	protected String artifactLocation;
	
	
	

	protected Log logger = getLog();
	
	protected APIManagerWrapper apiManagerWrapper = new APIManagerWrapper();
	
	
}
