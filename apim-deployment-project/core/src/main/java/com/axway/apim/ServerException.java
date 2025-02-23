package com.axway.apim;

public class ServerException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	public ServerException(String arg0) {
		super(arg0);
	}

    public ServerException(Throwable arg0) {
        super(arg0);
    }

	public ServerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}



}
