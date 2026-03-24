package de.creditreform.crefoteam.cte.tesun.httpstest;

public class HttpTestClient {

    public static void main(String[] args) {
        TestClientUtils testClient = new TestClientUtils(false, args);
        testClient.testIt();
    }
}
