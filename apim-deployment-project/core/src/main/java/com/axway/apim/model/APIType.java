package com.axway.apim.model;

import java.util.List;

public class APIType {

	private List<FrontendAPI> publishedAPIsByNameAndVersionAndPath;
	
	private List<FrontendAPI> publishedAPIsByNameAndVersion;
	
	private List<FrontendAPI> publishedAPIsByName;
	
	private List<FrontendAPI> allAPIByName;

	public List<FrontendAPI> getPublishedAPIsByNameAndVersionAndPath() {
		return publishedAPIsByNameAndVersionAndPath;
	}

	public void setPublishedAPIsByNameAndVersionAndPath(List<FrontendAPI> publishedAPIsByNameAndVersionAndPath) {
		this.publishedAPIsByNameAndVersionAndPath = publishedAPIsByNameAndVersionAndPath;
	}

	public List<FrontendAPI> getPublishedAPIsByName() {
		return publishedAPIsByName;
	}

	public void setPublishedAPIsByName(List<FrontendAPI> publishedAPIsByName) {
		this.publishedAPIsByName = publishedAPIsByName;
	}

	public List<FrontendAPI> getAllAPIByName() {
		return allAPIByName;
	}

	public void setAllAPIByName(List<FrontendAPI> allAPIByName) {
		this.allAPIByName = allAPIByName;
	}

	public List<FrontendAPI> getPublishedAPIsByNameAndVersion() {
		return publishedAPIsByNameAndVersion;
	}

	public void setPublishedAPIsByNameAndVersion(List<FrontendAPI> publishedAPIsByNameAndVersion) {
		this.publishedAPIsByNameAndVersion = publishedAPIsByNameAndVersion;
	}

	
	
}
