Product: Integration tests for WSO2 ESB Nest connector
Pre-requisites:

- Maven 3.x
- Java 1.6 or above

Tested Platform: 

- Mac OSx 10.9
- WSO2 ESB 4.8.1
		  

STEPS:

1. Make sure the ESB 4.8.1 zip file with latest patches available at "nest\repository\".

2. Add following code block, just after the listeners block (Remove or comment all the other test blocks) in following file - "nest\src\test\resources\testng.xml"

	<test name="Nest-Connector-Test" preserve-order="true" verbose="2">
        <packages>
            <package name="org.wso2.carbon.connector.integration.test.nest"/>
        </packages>
    </test> 

3. Copy proxy files to following location "nest\src\test\resources\artifacts\ESB\config\proxies\nest\"

4. Copy request files to following "nest\src\test\resources\artifacts\ESB\config\restRequests\nest\"

5. Edit the "nest.properties" at nest\src\test\resources\artifacts\connector\config\ using valid and relevant data. Parameters to be changed are mentioned below.

	- proxyDirectoryRelativePath: relative path of the Rest Request files folder from target.
	- requestDirectoryRelativePath: relative path of proxy folder from target.
	- client_id: to get the access token for a particular client_id.
    - client_secret: to get the access token for a particular client_secret.
	- token: the access token.
	
		
6. Following data set can be used for the first testsuite run.

		proxyDirectoryRelativePath=/../src/test/resources/artifacts/ESB/config/proxies/nest/
		requestDirectoryRelativePath=/../src/test/resources/artifacts/ESB/config/restRequests/nest/
 		client_id=900ca954-ccf9-48ba-aef3-51b215e7a8ba
		client_secret=Vb982GHVNJiYpcyB7au0h6H2R
        token=c.Myf0eRufEZiz7cvoH4vpDY8RlRSrNlNdONrpx6iT7ACCAiDE6JqUbUOXa2xedrrRFpOcUqVZTBziXWnCpLxSpMAxnbqWqO87sFulIEWPseoSNQoEDSgnIkojXrQTCy7m6t0GMrCjX6zYNb9B

7. Navigate to "nest\” and run the following command.
     $ mvn clean install