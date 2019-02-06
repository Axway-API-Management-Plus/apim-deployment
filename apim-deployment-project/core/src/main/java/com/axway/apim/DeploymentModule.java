package com.axway.apim;

import com.google.inject.AbstractModule;

public class DeploymentModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(AxwayClient.class);
		bind(GatewayDeployment.class);
		bind(GatewayDeployment.class);
	}

}
