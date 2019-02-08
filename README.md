
# Amplify APIM Deployment Utilities
Axway Amplify APIM deployment project provides utility to  export and import API, fed, pol and env artifacts.
The utilities are delivered as standalone and maven plugin for CI tool integration. 

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

## API Manger API Export and Deployment

API manger export accepts api name and version as input and json ( contains frontend and backend security credentials, outbound certs and CORS settings) as output. 

API manger deploy operation accepts backend URL, backend Auth, virtual host and outbound cert and deploy to target server. 

### API Manger API Export Example

Possible parameters

- operation* - Name of the operation e.g export
- url* - API Manger URL e.g https://api-env.demo.axway.com:8075
- username* - API manger username
- password* - API manger password
- apiname*  - Name of API deployed on API manager
- version  - Version of API
- artifactlocation* - Location where API export is going to be stored

* denotes mandatory parameters 

Example Command:

```bash
java -jar manager-standalone\target\manager-standalone-1.0.0.jar --operation=export --url=https://api-env.demo.axway.com:8075 --username=apiadmin --passwrod=changeme --apiname=petstore --version=1.0.0 --artifactlocation=d:\api\petstore.json
```

### API Manger API Deployment Example

Possible parameters: 

- operation* - Name of the operation e.g deploy 
- url* - API Manger URL e.g https://api-env.demo.axway.com:8075
- username* - API manger username
- password* - API manger password
- artifactlocation* - Location where API export is available
- orgname* - API manger Developer Organization name
- backendurl - Backend API URL
- outboundcert - Outbound certificate directory e.g d:\api\certs. Directory should contain x509 certificate
- virtualhost - Virtual host for API
- apiconflictupgrade - If apiconflictupgrade flag set to true, api will be upgraded if there is a conflict with name, version and 
- apiunpublishedremove - if apiunpublishedremove flag set to true, unpublished api will be deleted. 
- backendauth - Backend API Authentication e.g

API key:

```json
{
	"parameters": {
		"apiKey": "4249823490238490",
		"apiKeyField": "KeyId",
		"httpLocation": "QUERYSTRING_PARAMETER"
	},
	"type": "apiKey"
}
```
Http Basic Auth:

```json
{
	"parameters": {
		"username": "user2",
		"password": "user2"
	},
	"type": "http_basic"
}
```

Example command for windows

```bash
java -jar apim-deployment-project\manager-standalone\target\manager-standalone-1.0.0.jar --operation=deploy --url=https://api-env.demo.axway.com:8075 --username=apiadmin --passwrod=changeme --orgname=Axway --artifactlocation=d:\api\petstore.json --backendurl=https://prod.demo.axway.com --outboundcert=d:\api\certs --virtualhost=api.demo.axway.com --apiconflictupgrade=false --backendauth={"parameters": {"apiKey": "4249823490238490","apiKeyField": "KeyId","httpLocation": "QUERYSTRING_PARAMETER"},"type": "apiKey"}
```
## Contributing
Please read [Contributing.md](https://github.com/Axway-API-Management-Plus/Common/blob/master/Contributing.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Team

![alt text](https://github.com/Axway-API-Management-Plus/Common/blob/master/img/AxwayLogoSmall.png)
Axway Team