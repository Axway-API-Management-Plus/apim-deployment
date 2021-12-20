
# Amplify APIM Deployment Utilities

API Gateway package and deploy tools ( https://docs.axway.com/bundle/axway-open-docs/page/docs/apim_installation/apigtw_install/install_deploy_tools/index.html)  has the following limitations and user should install the software on Jenkins / other Continuous Integration  machine / create docker image to access the CLI. 

1. Does not provide a maven plugin to deploy fed, pol and env files. 
2. No CLI support for downloading fed from existing gateway. 



Axway Amplify APIM deployment project provides utility to  export and import fed, pol and env artifacts.
The utilities are delivered as standalone CLI and maven plugin for CI tool integration. 

CLI utility uses APIM product APIs (API Gateway API - http://apidocs.axway.com/swagger-ui/index.html?productname=apigateway&productversion=7.7.0&filename=api-gateway-swagger.json) internally to interact with Axway APIM. 

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
	$mvn clean install -Dmaven.test.skip=true
	```
- Ignore maven plugin test project

```bash
$mvn clean install -Dmaven.test.skip=true -pl !apim-deployment-samples\gateway-plugin-deploy-fed,!apim-deployment-samples\gateway-plugin-deploy-polenv,!apim-deployment-samples\gateway-plugin-export-fed,!apim-deployment-samples\gateway-plugin-export-polenv,!apim-deployment-samples\manager-plugin-deploy,!apim-deployment-samples\manager-plugin-export
```
Example for Linux:

```bash
#mvn clean install -Dmaven.test.skip=true -pl \!apim-deployment-samples/gateway-plugin-deploy-fed,\!apim-deployment-samples/gateway-plugin-deploy-polenv,\!apim-deployment-samples/gateway-plugin-export-fed,\!apim-deployment-samples/gateway-plugin-export-polenv,\!apim-deployment-samples/manager-plugin-deploy,\!apim-deployment-samples/manager-plugin-export
```
## Using the tool
You can run the following command to see available options:

```bash
#java -jar apim-deployment-project/gateway-standalone/target/gateway-standalone-1.0.0.jar --help
usage: Gateway Deployment
 -e,--envFile <arg>      Environment File
 -f,--fedFile <arg>      Federation File
 -g,--group <arg>        Domain group name
 -h,--help               Help options
 -n,--instance <arg>     Domain instance name
 -o,--operation <arg>    Name of Operation : export  or deploy
 -p,--password <arg>     Admin Node Manager password
 -pol,--polFile <arg>    PolicyFile File
 -s,--gatewayURL <arg>   Admin Node Manager URL
 -t,--type <arg>         Possbile values: fed, polenv
 -u,--username <arg>     Admin Node Manager username
```
The following sections provide examples of using the tool.

## API Gateway FED, POL and ENV export and deployment Example
In this section you can find examples of the commands to deploy FED, POL and ENV files to your target configuration.

### Deploy a Fed to a group of Gateways
Now you can deploy a FED file to your installation for Axway API Management.For example, 
- for Windows:

```bash
java -jar apim-deployment-project/gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=finance --fedFile=D:\\api\\finance.fed --type=fed
```
- and for Linux:

```bash
java -jar apim-deployment-project/gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=Finance --fedFile=/home/axway/finance.fed --type=fed
```

### Deploy a FED file to a specific Gateway
For example,
- for Windows

```bash
java -jar apim-deployment-project/gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=finance --instance=server1 --fedFile=D:\\api\\finance.fed --type=fed
```
- and for Linux
```bash
java -jar apim-deployment-project/gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=Finance --instance=server1 --fedFile=/home/axway/finance.fed --type=fed
```

### Deploy the POL and ENV files to all Gateways
For example,
- for Windows

```bash
java -jar apim-deployment-project/gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=finance --polFile=D:\\api\\finance.pol --envFile=D:\\api\\finance.env --type=polenv
```
- and for Linux

```bash
java -jar apim-deployment-project/gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=Finance --polFile=/home/axway/finance.pol --envFile=/home/axway/finance.env --type=polenv
```

### Deploy the POL and ENV files to a specific Gateway
For example,
- for Windows:

```bash
java -jar apim-deployment-project/gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=finance --instance=server1 --polFile=D:\\api\\finance.pol --envFile=D:\\api\\finance.env --type=polenv
```
- and for Linux:

```bash
java -jar apim-deployment-project/gateway-standalone/target/gateway-standalone-1.0.0.jar --operation=deploy --gatewayURL=https://localhost:8090 --username=admin --password=changeme --group=Finance --instance=server1 --polFile=/home/axway/finance.pol --envFile=/home/axway/finance.env --type=polenv
```

### Proxy Support 

If API Gateway installed behind a proxy, use the following system properties

- proxyHost
- proxyPort
- proxyProtocol - http or https

Example

```bash
java -jar -DproxyHost=10.10.2.2 -DproxyPort=8080 --proxyProtocol=https apim-deployment-project/gateway-standalone/target/gateway-standalone-1.0.0.jar -o=deploy -s=https://localhost:8090 -u=admin -p=changeme -g=finance -n=server1 -f=D:\\api\\finance.fed -t=fed
```


## Contributing
Please read [Contributing.md](https://github.com/Axway-API-Management-Plus/Common/blob/master/Contributing.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Team

![alt text](https://github.com/Axway-API-Management-Plus/Common/blob/master/img/AxwayLogoSmall.png)
Axway Team
