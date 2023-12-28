package basicconnect;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletableFuture;

public class BasicConnect {

    static String ciPropValue = System.getProperty("aws.crt.ci");
    static boolean isCI = ciPropValue != null && Boolean.valueOf(ciPropValue);

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        MqttClientConnectionEvents callbacks = new MqttClientConnectionEvents() {
            @Override
            public void onConnectionInterrupted(int errorCode) {
                if (errorCode != 0) {
                    System.out.println("Connection interrupted: " + errorCode + ": " + CRT.awsErrorString(errorCode));
                }
            }

            @Override
            public void onConnectionResumed(boolean sessionPresent) {
                System.out.println("Connection resumed: " + (sessionPresent ? "existing session" : "clean session"));
            }
        };



            /**
             * Create the MQTT connection from the builder
             */
            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath("C:\\Users\\Salem\\Documents\\BasicConnect\\src\\main\\java\\basicconnect\\phone.cert.pem", "C:\\Users\\Salem\\Documents\\BasicConnect\\src\\main\\java\\basicconnect\\phone.private.key");

            builder.withConnectionEventCallbacks(callbacks)
                .withClientId("iotconsole-0af2dcb3-7e68-4b4a-935a-f0a433670471")
                .withEndpoint("a3j5gn8c1p7i99-ats.iot.us-east-1.amazonaws.com")
                .withCleanSession(true)
                .withProtocolOperationTimeoutMs(60000);

            MqttClientConnection connection = builder.build();
            builder.close();

            //////
        // Subscribe to the topic
        CompletableFuture<Integer> subscribed = connection.subscribe("sdk/test/java", QualityOfService.AT_LEAST_ONCE, (message) -> {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            System.out.println("MESSAGE: " + payload);
        });
        subscribed.get();

        //
        String msg = "Hello!";
        // Publish to the topic
        int count = 0;
        while (count++ < 5) {
            CompletableFuture<Integer> published = connection.publish(new MqttMessage("sdk/test/java", msg.getBytes(), QualityOfService.AT_LEAST_ONCE, false));
            published.get();
            Thread.sleep(1000);
        }

}
}
