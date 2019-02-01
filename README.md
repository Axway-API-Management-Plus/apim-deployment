# apim-deployment
Axway APIM deployment standalone and maven plugin

## Prerequisites

- Axway AMPLIFY API Management 7.5.3 or above
- JDK 1.8.0_xxx
- Apache Maven 3.3.9 or above 

### Build the project 

	```bash
	$mvn clean install
	```

## API Gateway Deployment Example

- Deploy Fed to all Gateway

```bash
	java -jar gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=finance --fedFile=D:\\api\\finance.fed	--type=fed
	
	java -jar gateway-standalone/target/gateway-standalone-1.0.0.jar -o=deploy -s=https://localhost:8090 -u=admin -p=changeme --g=finance -f=D:\\api\\finance.fed -t=fed
```

- Deploy Fed to specific Gateway

```bash
	java -jar gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=finance --instance=server1 --fedFile=D:\\api\\finance.fed	--type=fed
	
	java -jar gateway-standalone/target/gateway-standalone-1.0.0.jar -o=deploy -s=https://localhost:8090 -u=admin -p=changeme -g=finance -n=server1 -f=D:\\api\\finance.fed -t=fed

```

- Deploy Pol and Env to all Gateway

```bash

	java -jar gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=finance --polFile=D:\\api\\finance.pol --envFile=D:\\api\\finance.env --type=polenv
	
	java -jar gateway-standalone/target/gateway-standalone-1.0.0.jar -o=deploy -s=https://localhost:8090 -u=admin -p=changeme -g=finance -pol=D:\\api\\finance.pol -e=D:\\api\\finance.env -t=polenv

```


- Deploy Pol and env to specific Gateway

```bash
	java -jar gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=finance --instance=server1 --polFile=D:\\api\\finance.pol --envFile=D:\\api\\finance.env	--type=polenv
	
	java -jar gateway-standalone/target/gateway-standalone-1.0.0.jar -o=deploy -s=https://localhost:8090 -u=admin -p=changeme -g=finance -n=server1 -pol=D:\\api\\finance.pol -e=D:\\api\\finance.env -t=polenv
	
```

- Proxy Support 

If API Gateway installed behind proxy use the following system properties

- proxyHost
- proxyPort
- proxyProtocol - http or https

Example

```bash
	java -jar -DproxyHost=10.10.2.2 -DproxyPort=8080 --proxyProtocol=https gateway-standalone/target/gateway-standalone-1.0.0.jar -o=deploy -s=https://localhost:8090 -u=admin -p=changeme -g=finance -n=server1 -f=D:\\api\\finance.fed -t=fed

```
