package com.axway.apim;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

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
	
	@Inject
	protected APIManagerWrapper apiManagerWrapper;

	
	protected void setup(Object obj){
		Injector injector = Guice.createInjector(new DeploymentModule());
		injector.injectMembers(obj);
	}
	
	
}
