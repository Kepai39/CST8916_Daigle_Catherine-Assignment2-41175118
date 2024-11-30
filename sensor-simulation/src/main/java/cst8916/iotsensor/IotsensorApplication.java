package cst8916.iotsensor;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IotsensorApplication {
	/*
	public static void main(String[] args) {

		//SpringApplication.run(IotsensorApplication.class, args);
	}
	*/

	private static final String[] LOCATIONS = {"Dow's Lake", "Fifth Avenue", "NAC"};
    private static final int D2C_MESSAGE_TIMEOUT_MILLISECONDS = 10000;

    /**
     * Connection string for IoT Hub
     */
    private static final String IOTHUB_CONN_STR = System.getenv("IOTHUB_CONN_STR");

    /**
     * How many requests for this IoT simulator to send
     */
    private static final String NUM_REQUESTS_SEND = System.getenv("NUM_REQUESTS_SEND");

    public static void main(String[] args) {
        IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        if (IOTHUB_CONN_STR != null) {
            if (IOTHUB_CONN_STR.isBlank()) {
                System.out.println("IoT Hub Connection String is blank or null");
            }
            System.out.println("IoT Hub Connection String is blank or null");
            return;
        }
        int numRequests = 0;
        try {
            if (NUM_REQUESTS_SEND != null) {
                numRequests = Integer.parseInt(NUM_REQUESTS_SEND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Environment variables OK.");

        ModuleClient client = new ModuleClient(IOTHUB_CONN_STR, protocol);
        client.setMessageCallback(new MessageCallback(), null);
        client.setConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());
        try {
            client.open(true);
        } catch (IotHubClientException e) {
            System.out.println("Could not open connection.");
            e.printStackTrace();
            return;
        }
        System.out.println("Sending messages...");
        for (int i = 0; i < numRequests; i++) {
            String[] msgs = {randomizeIoTData(LOCATIONS[0]), randomizeIoTData(LOCATIONS[1]), randomizeIoTData(LOCATIONS[2])};

            // prepare message
            for (int y = 0; y < 3; y++) {
                Message msg = new Message(msgs[y]);
                msg.setContentType("application/json");
                msg.setMessageId(java.util.UUID.randomUUID().toString());
                msg.setExpiryTime(D2C_MESSAGE_TIMEOUT_MILLISECONDS);
                System.out.println(msgs[y]);
                try {
                    client.sendEvent(msg, D2C_MESSAGE_TIMEOUT_MILLISECONDS);
                } catch (IllegalStateException | InterruptedException | IotHubClientException e) {
                    e.printStackTrace();
                    return;
                }
                System.out.println("Message sent.");
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * Randomizes data and returns JSON string with randomized IoT Data. Temperatures are within bounds of -30 to 0 (surfaceTemperature) or -20 to 10 (externalTemperature).
     * @return String of JSON with randomized IoT Data
     */
    private static String randomizeIoTData(String location) {
        Random random = new Random();
        int iceThickness = random.ints(10, 41).findFirst().getAsInt();
        double surfaceTemp = Math.random() * (0 + 30) - 30;
        int snowAccumulation = random.ints(0, 10).findFirst().getAsInt();
        double externalTemp = Math.random() * (10 + 20) - 20;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); 
        String timestamp = sdf.format(date);
        return "{\"location\": \""+location+"\", "
            + "\"iceThickness\": "+iceThickness+", "
            + "\"surfaceTemperature\": "+surfaceTemp+", "
            + "\"snowAccumulation\": "+snowAccumulation+", "
            + "\"externalTemperature\": "+externalTemp+", "
            + "\"timestamp\": "+timestamp+"}";
    }

    /**
     * Referenced from {@link https://github.com/Azure/azure-iot-sdk-java/blob/main/iothub/device/iot-device-samples/send-receive-module-sample/src/main/java/samples/com/microsoft/azure/sdk/iot/SendReceiveModuleSample.java}
     */
    protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback {

        @Override
        public void onStatusChanged(ConnectionStatusChangeContext connectionStatusChangeContext) {
            IotHubConnectionStatus status = connectionStatusChangeContext.getNewStatus();
            IotHubConnectionStatusChangeReason sChangeReason = connectionStatusChangeContext.getNewStatusReason();
            Throwable throwable = connectionStatusChangeContext.getCause();

            System.out.println("===== Connection =====");
            System.out.println("Status update: " + status);
            System.out.println("Status reason: " + sChangeReason);
            System.out.println("Status throwable: " + (throwable == null ? "null" : throwable.getMessage()));
            System.out.println("======================\n");

            if (throwable != null)
                throwable.printStackTrace();
            
            if (status == IotHubConnectionStatus.DISCONNECTED) {
                System.out.println("Disconnected and not retrying.");
            } else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING) {
                System.out.println("Disconnected and retrying.");
            } else if (status == IotHubConnectionStatus.CONNECTED) {
                System.out.println("Connection made.");
            }


        }
        
    }

    /**
     * From {@link https://github.com/Azure/azure-iot-sdk-java/blob/main/iothub/device/iot-device-samples/send-receive-module-sample/src/main/java/samples/com/microsoft/azure/sdk/iot/SendReceiveModuleSample.java}
     */
    protected static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult onCloudToDeviceMessageReceived(Message msg, Object context)
        {
            System.out.println(
                "Received message with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
            return IotHubMessageResult.COMPLETE;
        }
    }
}
