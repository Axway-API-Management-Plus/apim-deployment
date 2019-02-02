package com.axway.apim.model;

public class CACert {
	
	
	private String certBlob;
	private String name;
	private String alias;
	private String subject;
	private String issuer;
	private int version;
	private long notValidBefore;
	private long notValidAfter;
	private String signatureAlgorithm;
	private String sha1Fingerprint;
	private String md5Fingerprint;
	private boolean inbound;
	private boolean outbound;
	
	private boolean expired;
	private boolean notYetValid;
	
	
	
	public String getCertBlob() {
		return certBlob;
	}
	public void setCertBlob(String certBlob) {
		this.certBlob = certBlob;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getIssuer() {
		return issuer;
	}
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public long getNotValidBefore() {
		return notValidBefore;
	}
	public void setNotValidBefore(long notValidBefore) {
		this.notValidBefore = notValidBefore;
	}
	public long getNotValidAfter() {
		return notValidAfter;
	}
	public void setNotValidAfter(long notValidAfter) {
		this.notValidAfter = notValidAfter;
	}
	public String getSignatureAlgorithm() {
		return signatureAlgorithm;
	}
	public void setSignatureAlgorithm(String signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}
	public String getSha1Fingerprint() {
		return sha1Fingerprint;
	}
	public void setSha1Fingerprint(String sha1Fingerprint) {
		this.sha1Fingerprint = sha1Fingerprint;
	}
	public String getMd5Fingerprint() {
		return md5Fingerprint;
	}
	public void setMd5Fingerprint(String md5Fingerprint) {
		this.md5Fingerprint = md5Fingerprint;
	}
	public boolean isInbound() {
		return inbound;
	}
	public void setInbound(boolean inbound) {
		this.inbound = inbound;
	}
	public boolean isOutbound() {
		return outbound;
	}
	public void setOutbound(boolean outbound) {
		this.outbound = outbound;
	}
	public boolean isExpired() {
		return expired;
	}
	public void setExpired(boolean expired) {
		this.expired = expired;
	}
	public boolean isNotYetValid() {
		return notYetValid;
	}
	public void setNotYetValid(boolean notYetValid) {
		this.notYetValid = notYetValid;
	} 
	
	
	
	
}
