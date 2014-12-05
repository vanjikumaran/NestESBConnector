package org.wso2.carbon.connector.nest_thermostat;

import org.apache.axis2.context.ConfigurationContext;
import org.jruby.RubyProcess;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.proxy.admin.ProxyServiceAdminClient;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.automation.utils.axis2client.ConfigurationContextProvider;
import org.wso2.carbon.connector.common.ConnectorIntegrationUtil;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.mediation.library.stub.MediationLibraryAdminServiceStub;
import org.wso2.carbon.mediation.library.stub.upload.MediationLibraryUploaderStub;

import javax.activation.DataHandler;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;

public class NestConnectorIntegrationTest extends ESBIntegrationTest {
    private static final String CONNECTOR_NAME = "nest";

    private MediationLibraryUploaderStub mediationLibUploadStub = null;

    private MediationLibraryAdminServiceStub adminServiceStub = null;

    private ProxyServiceAdminClient proxyAdmin;

    private String repoLocation = null;

    private String nestConnectorFileName = "nest.zip";

    private Properties nestConnectorProperties = null;

    private String propertiesFilePath = null;

    private String pathToProxiesDirectory = null;

    private String pathToRequestsDirectory = null;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();

        ConfigurationContextProvider configurationContextProvider = ConfigurationContextProvider.getInstance();
        ConfigurationContext cc = configurationContextProvider.getConfigurationContext();
        mediationLibUploadStub =
                new MediationLibraryUploaderStub(cc, esbServer.getBackEndUrl() + "MediationLibraryUploader");
        AuthenticateStub.authenticateStub("admin", "admin", mediationLibUploadStub);

        adminServiceStub =
                new MediationLibraryAdminServiceStub(cc, esbServer.getBackEndUrl() + "MediationLibraryAdminService");

        AuthenticateStub.authenticateStub("admin", "admin", adminServiceStub);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            repoLocation = System.getProperty("connector_repo").replace("/", "\\");
        } else {
            repoLocation = System.getProperty("connector_repo").replace("/", "/");
        }
        proxyAdmin = new ProxyServiceAdminClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());

        ConnectorIntegrationUtil.uploadConnector(repoLocation, mediationLibUploadStub, nestConnectorFileName);
        log.info("Sleeping for " + 30000 / 1000 + " seconds while waiting for synapse import");
        Thread.sleep(30000);

        adminServiceStub.updateStatus("{org.wso2.carbon.connector}" + CONNECTOR_NAME, CONNECTOR_NAME,
                "org.wso2.carbon.connector", "enabled");

        nestConnectorProperties = ConnectorIntegrationUtil.getConnectorConfigProperties(CONNECTOR_NAME);
        propertiesFilePath = repoLocation + nestConnectorProperties.getProperty("propertiesFilePath");
        pathToProxiesDirectory = repoLocation + nestConnectorProperties.getProperty("proxyDirectoryRelativePath");
        pathToRequestsDirectory = repoLocation + nestConnectorProperties.getProperty("requestDirectoryRelativePath");

    }

    @Override
    protected void cleanup() {
        axis2Client.destroy();
    }

//    /**
//     * Test case for getAccessToken method.
//     * For this method, authorization code must be changed in nest.properties file every time.
//     */
//    @Test(groups = { "wso2.esb" },priority = 1, description = "nest {getAccessToken} integration test.")
//    public void testGetAccessToken() throws Exception {
//String jsonRequestFilePath = pathToRequestsDirectory + "getAccessToken.txt";
//        String methodName = "nest_getAccessToken";
//final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
//        String modifiedJsonString = String.format(jsonString, nestConnectorProperties.getProperty("clientId"), nestConnectorProperties.getProperty("clientSecret"), nestConnectorProperties.getProperty("code"));
//        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
//        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
//try {
//            JSONObject responseConnector = ConnectorIntegrationUtil.sendRequest("POST", getProxyServiceURL(methodName), modifiedJsonString);
//            if(responseConnector.has("access_token") && !responseConnector.getString("access_token").equals("")) {
//                FileInputStream in = new FileInputStream(propertiesFilePath + "nest.properties");
//                Properties props = new Properties();
//                props.load(in);
//                in.close();
//FileOutputStream out = new FileOutputStream(propertiesFilePath + "nest.properties");
//                props.setProperty("token", responseConnector.getString("access_token"));
//                props.store(out, null);
//                out.close();
//            }
// Assert.assertTrue(responseConnector.has("access_token"));
//        } finally {
//            proxyAdmin.deleteProxy(methodName);
//        }
//    }

    /**
     * Test case for getServices(viewCurrentTemperatureFahrenheit) method.
     */
    @Test(groups = {"wso2.esb"}, priority = 2, description = "nest {getServices} integration test.")
    public void testViewCurrentTemperatureFahrenheit() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForThermostats.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewCurrentTemperatureFahrenheit");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "devices/thermostats/" + jo.getString("deviceId") + "/ambient_temperature_f";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewTargetTemperatureFahrenheit) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewTargetTemperatureFahrenheit() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForThermostats.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewTargetTemperatureFahrenheit");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "devices/thermostats/" + jo.getString("deviceId") + "/target_temperature_f";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewHumidity) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewHumidity() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForThermostats.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewHumidity");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);

            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "devices/thermostats/" + jo.getString("deviceId") + "/humidity";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewTemperatureMode) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewTemperatureMode() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForThermostats.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewTemperatureMode");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "devices/thermostats/" + jo.getString("deviceId") + "/hvac_mode";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Negative test case for getServices for thermostats.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test with negative case.")
    public void testGetServicesForThermostatsNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForThermostats_Negative.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        String modifiedJsonString = String.format(jsonString, "viewCurrentTemperatureFahrenheit");
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 404);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewCOAlarmState) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewCOAlarmState() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForSmoke_COAlarms.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewCOAlarmState");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "devices/smoke_co_alarms/" + jo.getString("deviceId") + "/co_alarm_state";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewSmokeAlarmState) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewSmokeAlarmState() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForSmoke_COAlarms.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewSmokeAlarmState");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "devices/smoke_co_alarms/" + jo.getString("deviceId") + "/smoke_alarm_state";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewBatteryHealth) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewBatteryHealth() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForSmoke_COAlarms.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewBatteryHealth");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "devices/smoke_co_alarms/" + jo.getString("deviceId") + "/battery_health";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewManualTestState) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewManualTestState() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForSmoke_COAlarms.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewManualTestState");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "devices/smoke_co_alarms/" + jo.getString("deviceId") + "/is_manual_test_active";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewLastManualTestStatus) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewLastManualTestStatus() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForSmoke_COAlarms.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewLastManualTestStatus");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "devices/smoke_co_alarms/" + jo.getString("deviceId") + "/ui_color_state";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewLastManualTestTimestamp) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewLastManualTestTimestamp() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForSmoke_COAlarms.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewLastManualTestTimestamp");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "devices/smoke_co_alarms/" + jo.getString("deviceId") + "/last_manual_test_time";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewOnlineStatus) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewOnlineStatus() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForSmoke_COAlarms.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewOnlineStatus");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "devices/smoke_co_alarms/" + jo.getString("deviceId") + "/is_online";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewLastConnectionInformation) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewLastConnectionInformation() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForSmoke_COAlarms.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewLastConnectionInformation");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "devices/smoke_co_alarms/" + jo.getString("deviceId") + "/last_connection";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Negative test case for getServices for smoke_co_alarms.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test with negative case.")
    public void testGetServicesForSmokeCOAlarmsNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForSmoke_COAlarms_Negative.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        String modifiedJsonString = String.format(jsonString, "viewOnlineStatus");
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 404);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewThermostats) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewAllThermostats() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForStructures.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewThermostats");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "structures/" + jo.getString("structureId") + "/thermostats";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewSmokeCOAlarms) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewAllSmokeCOAlarms() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForStructures.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewSmokeCOAlarms");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "structures/" + jo.getString("structureId") + "/smoke_co_alarms";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewEnergyEventPeekStart) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewEnergyEventPeekstart() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForStructures.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewEnergyEventPeekStart");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "structures/" + jo.getString("structureId") + "/peak_period_start_time";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewAwayState) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewAwayState() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForStructures.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewAwayState");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);

            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "structures/" + jo.getString("structureId") + "/away";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for getServices(viewPostalCode) method.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test.")
    public void testViewPostalcode() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForStructures.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String modifiedJsonString = String.format(jsonString, "viewPostalCode");
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            String responseConnector = ConnectorIntegrationUtil.sendRequest_String("POST", getProxyServiceURL(methodName), modifiedJsonString);
            String httpMethod = "GET";
            JSONObject jo = new JSONObject(jsonString);
            String parameters = "structures/" + jo.getString("structureId") + "/postal_code";
            String responseDirect = ConnectorIntegrationUtil.sendRestRequest(httpMethod, parameters);
            System.out.println("responseConnector\n" + responseConnector);
            System.out.println("responseDirect\n" + responseDirect);
            Assert.assertTrue(responseConnector.equals(responseDirect));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Negative test case for getServices for structures.
     */
    @Test(groups = {"wso2.esb"}, description = "nest {getServices} integration test with negative case.")
    public void testGetServicesForStructuresNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getServicesForStructures_Negative.txt";
        String methodName = "nest_getServices";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        String modifiedJsonString = String.format(jsonString, "viewAwayState");
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 404);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for setFanTimer method.
     */
    @Test(groups = {"wso2.esb"}, priority = 2, description = "nest {setFanTimer} integration test.")
    public void setFanTimer() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "setFanTimer.txt";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String methodName = "nest_setFanTimer";
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject responseConnector = ConnectorIntegrationUtil.sendRequest("POST", getProxyServiceURL(methodName), jsonString);

            JSONObject jo = new JSONObject(jsonString);
            Assert.assertTrue((responseConnector.has("error") && responseConnector.getString("error").equals("Cannot change fan_timer_active while structure is away")) || responseConnector.getString("fan_timer_active").equals(jo.getString("fanTimerStatus").toString()));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Negative test case for setFanTimer method.
     */
    @Test(groups = {"wso2.esb"}, priority = 2, description = "nest {setFanTimer} negative integration test.")
    public void setFanTimerNegative() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "setFanTimer_Negative.txt";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String methodName = "nest_setFanTimer";
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), jsonString);
            Assert.assertTrue(responseHeader == 400);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for setTargetTemperature method.
     */
    @Test(groups = {"wso2.esb"}, priority = 2, description = "nest {setTargetTemperature} integration test.")
    public void setTargetTemperature() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "setTargetTemperature.txt";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String methodName = "nest_setTargetTemperature";
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject responseConnector = ConnectorIntegrationUtil.sendRequest("POST", getProxyServiceURL(methodName), jsonString);
            JSONObject jo = new JSONObject(jsonString);
            if (jo.getString("scale").toString().toLowerCase().equals("f")) {
                Assert.assertTrue(responseConnector.getString("target_temperature_f").equals(jo.getString("targetTemperature").toString()));
            } else if (jo.getString("scale").toString().toLowerCase().equals("c")) {
                Assert.assertTrue(responseConnector.getString("target_temperature_c").equals(jo.getString("targetTemperature").toString()));
            }
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Negative test case for setTargetTemperature method.
     */
    @Test(groups = {"wso2.esb"}, priority = 2, description = "nest {setTargetTemperature} negative integration test.")
    public void setTargetTemperatureNegative() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "setTargetTemperature_Negative.txt";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String methodName = "nest_setTargetTemperature";
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), jsonString);
            Assert.assertTrue(responseHeader == 400);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for setTemperatureMode method.
     */
    @Test(groups = {"wso2.esb"}, priority = 2, description = "nest {setTemperatureMode} integration test.")
    public void setTemperatureMode() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "setTemperatureMode.txt";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String methodName = "nest_setTemperatureMode";
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject responseConnector = ConnectorIntegrationUtil.sendRequest("POST", getProxyServiceURL(methodName), jsonString);
            JSONObject jo = new JSONObject(jsonString);
            Assert.assertTrue(responseConnector.getString("hvac_mode").equals(jo.getString("temperatureMode").toString()));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Negative test case for setTemperatureMode method.
     */
    @Test(groups = {"wso2.esb"}, priority = 2, description = "nest {setTemperatureMode} negative integration test.")
    public void setTemperatureModeNegative() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "setTemperatureMode_Negative.txt";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String methodName = "nest_setTemperatureMode";
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), jsonString);
            Assert.assertTrue(responseHeader == 400);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for setAwayState method.
     */
    @Test(groups = {"wso2.esb"}, priority = 2, description = "nest {saveTracksOfCurrentUser} integration test.")
    public void setAwayState() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "setAwayState.txt";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String methodName = "nest_setAwayState";
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject responseConnector = ConnectorIntegrationUtil.sendRequest("POST", getProxyServiceURL(methodName), jsonString);

            JSONObject jo = new JSONObject(jsonString);
            Assert.assertTrue(responseConnector.getString("away").equals(jo.getString("awayState").toString()));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Negative test case for setAwayState method.
     */
    @Test(groups = {"wso2.esb"}, priority = 2, description = "nest {setAwayState} negative integration test.")
    public void setAwayStateNegative() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "setAwayState_Negative.txt";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String methodName = "nest_setAwayState";
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), jsonString);
            Assert.assertTrue(responseHeader == 400);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Test case for setETA method.
     */
    @Test(groups = {"wso2.esb"}, priority = 2, description = "nest {setETA} integration test.")
    public void setETA() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "setETA.txt";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String methodName = "nest_setETA";
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject responseConnector = ConnectorIntegrationUtil.sendRequest("POST", getProxyServiceURL(methodName), jsonString);
            JSONObject jo = new JSONObject(jsonString);
            JSONObject o = new JSONObject("{\"eta\":{\"trip_id\":\"myTripHome1024\",\"begin\":\"2014-11-01T22:42:59.000Z\",\"end\":\"2014-11-01T23:42:59.000Z\"}}").getJSONObject("eta");

            Assert.assertTrue((responseConnector.has("error") && responseConnector.getString("error").equals("permission denied")) || (responseConnector.getJSONObject("eta").getString("trip_id").equals(jo.getString("tripId").toString()) && o.getString("estimated_arrival_window_begin").equals(jo.getString("begin").toString()) && o.getString("estimated_arrival_window_end").equals(jo.getString("end").toString())));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /**
     * Negative test case for setETA method.
     */
    @Test(groups = {"wso2.esb"}, priority = 2, description = "nest {setETA} negative integration test.")
    public void setETANegative() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "setETA_Negative.txt";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        String methodName = "nest_setETA";
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), jsonString);
            Assert.assertTrue(responseHeader == 400);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }
}
