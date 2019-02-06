package com.axway.apim.model;

import java.util.List;

public class APIType {

	private List<FrondendAPI> publishedAPIsByNameAndVersionAndPath;
	
	private List<FrondendAPI> publishedAPIsByNameAndVersion;
	
	private List<FrondendAPI> publishedAPIsByName;
	
	private List<FrondendAPI> allAPIByName;

	public List<FrondendAPI> getPublishedAPIsByNameAndVersionAndPath() {
		return publishedAPIsByNameAndVersionAndPath;
	}

	public void setPublishedAPIsByNameAndVersionAndPath(List<FrondendAPI> publishedAPIsByNameAndVersionAndPath) {
		this.publishedAPIsByNameAndVersionAndPath = publishedAPIsByNameAndVersionAndPath;
	}

	public List<FrondendAPI> getPublishedAPIsByName() {
		return publishedAPIsByName;
	}

	public void setPublishedAPIsByName(List<FrondendAPI> publishedAPIsByName) {
		this.publishedAPIsByName = publishedAPIsByName;
	}

	public List<FrondendAPI> getAllAPIByName() {
		return allAPIByName;
	}

	public void setAllAPIByName(List<FrondendAPI> allAPIByName) {
		this.allAPIByName = allAPIByName;
	}

	public List<FrondendAPI> getPublishedAPIsByNameAndVersion() {
		return publishedAPIsByNameAndVersion;
	}

	public void setPublishedAPIsByNameAndVersion(List<FrondendAPI> publishedAPIsByNameAndVersion) {
		this.publishedAPIsByNameAndVersion = publishedAPIsByNameAndVersion;
	}

	
	
}
