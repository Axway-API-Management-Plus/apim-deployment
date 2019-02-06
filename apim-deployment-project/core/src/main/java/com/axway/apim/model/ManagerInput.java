package com.axway.apim.model;

public class ManagerInput {

	private String url; 
	private String username; 
	private String password; 
	private String location; 
	private String orgName;
	private String backendURL; 
	private String outboundCertFolder; 
	private String backendAuthJson; 
	private String virtualHost; 
	boolean apiConflictUpgrade; 
	boolean apiUnpublishedRemove;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getBackendURL() {
		return backendURL;
	}
	public void setBackendURL(String backendURL) {
		this.backendURL = backendURL;
	}
	public String getOutboundCertFolder() {
		return outboundCertFolder;
	}
	public void setOutboundCertFolder(String outboundCertFolder) {
		this.outboundCertFolder = outboundCertFolder;
	}
	public String getBackendAuthJson() {
		return backendAuthJson;
	}
	public void setBackendAuthJson(String backendAuthJson) {
		this.backendAuthJson = backendAuthJson;
	}
	public String getVirtualHost() {
		return virtualHost;
	}
	public void setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
	}
	public boolean isApiConflictUpgrade() {
		return apiConflictUpgrade;
	}
	public void setApiConflictUpgrade(boolean apiConflictUpgrade) {
		this.apiConflictUpgrade = apiConflictUpgrade;
	}
	public boolean isApiUnpublishedRemove() {
		return apiUnpublishedRemove;
	}
	public void setApiUnpublishedRemove(boolean apiUnpublishedRemove) {
		this.apiUnpublishedRemove = apiUnpublishedRemove;
	}

}
