# apim-deployment
Axway APIM deployment standalone and maven plugin

## Prerequisites

1. Axway AMPLIFY API Management 7.5.3 or above
2. JDK 1.8.0_xxx
3. Apache Maven 3.3.9 or above 
4. Trust API Gateway Admin Node manager domain certificate

- Download ANM certificate from browser
- Import the certificate to java key store
	
	
	```bash
		keytool -import -trustcacerts -keystore "C:\Program Files\Java\jdk1.8.0_111\jre\lib\security\cacerts" -storepass changeit -alias domain -file c:\Users\rnatarajan\Desktop\domain.cer -noprompt
	```	

### Build the project 

	```bash
	$mvn clean install
	```

## API Gateway Deployment Example

- Deploy Fed to all Gateway

```bash
	java -jar gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=finance --fedFile=D:\\api\\finance.fed	--type=fed
```

- Deploy Fed to specific Gateway

```bash
	java -jar gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=finance --instance=server1 --fedFile=D:\\api\\finance.fed	--type=fed
```

- Deploy Pol and Env to all Gateway

```bash
	java -jar gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=finance --polFile=D:\\api\\finance.pol --envFile=D:\\api\\finance.env --type=polenv
```


- Deploy Pol and env to specific Gateway

```bash
	java -jar gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=finance --instance=server1 --polFile=D:\\api\\finance.pol --envFile=D:\\api\\finance.env	--type=polenv
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
