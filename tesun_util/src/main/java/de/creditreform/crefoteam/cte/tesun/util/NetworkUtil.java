package de.creditreform.crefoteam.cte.tesun.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetworkUtil {

    public void printAllOwnerMacs() {
        InetAddress[] ias;
        try {
            ias = InetAddress.getAllByName(getOwnerHostName());
            if (ias != null)
                for (InetAddress ia : ias) {
                    System.out.println(ia.getHostAddress());
                }
        } catch (UnknownHostException e) {
            System.err.println("Unbekannter Hostname");
        }
    }

    public String getOwnerHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getOwnerNetworkDeviceName() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            if (localHost != null) {
                NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
                if (ni != null)
                    return ni.getDisplayName();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getOwnerMac() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            if (localHost != null) {
                NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
                if (ni != null) {
                    byte[] hwa = ni.getHardwareAddress();
                    if (hwa == null)
                        return null;
                    String mac = "";
                    for (int i = 0; i < hwa.length; i++) {
                        mac += String.format("%x:", hwa[i]);
                    }
                    if (mac.length() > 0 && !ni.isLoopback()) {
                        return mac.toLowerCase().substring(0, mac.length() - 1);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getOwnerIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        NetworkUtil nu = new NetworkUtil();
        nu.printAllOwnerMacs();
        System.out.println("Host-Name: " + nu.getOwnerHostName());
        System.out.println("Device-Name: " + nu.getOwnerNetworkDeviceName());
        System.out.println("Mac-Adresse: " + nu.getOwnerMac());
        System.out.println("IP: " + nu.getOwnerIp());
    }
}