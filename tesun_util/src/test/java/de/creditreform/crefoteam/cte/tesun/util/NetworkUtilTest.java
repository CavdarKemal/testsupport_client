package de.creditreform.crefoteam.cte.tesun.util;

import org.junit.Test;

public class NetworkUtilTest {

    @Test
    public void testNetworkUtil() {
        NetworkUtil networkUtil = new NetworkUtil();
        System.out.println("OwnerHostName: " + networkUtil.getOwnerHostName());
        System.out.println("OwnerIp: " + networkUtil.getOwnerIp());
        System.out.println("OwnerMac: " + networkUtil.getOwnerMac());
        System.out.println("OwnerNetworkDeviceName: " + networkUtil.getOwnerNetworkDeviceName());

    }

}
