package com.axway.apim.model;

import java.util.Properties;


public class AuthenticationProfile {

	private String name = "_default";

	private String isDefault = "true";

	private Properties parameters;
	
	private AuthType type;

	public AuthenticationProfile() {
		super();
	}

	public String getName() {
		return name;
	}


	public String getIsDefault() {
		return isDefault;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}

	public Properties getParameters() {
		return parameters;
	}

	public void setParameters(Properties parameters) {
		this.parameters = parameters;
	}

	public AuthType getType() {
		return type;
	}

	public void setType(AuthType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "AuthenticationProfile [name=" + name + ", isDefault=" + isDefault + ", parameters=" + parameters
				+ ", type=" + type + "]";
	}
}
