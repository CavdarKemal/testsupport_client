package de.creditreform.crefoteam.cte.tesun.httpstest;

public class HttpsTestClient {

    public static void main(String[] args) {
        TestClientUtils testClient = new TestClientUtils(true, args);
        testClient.testIt();
    }

}
