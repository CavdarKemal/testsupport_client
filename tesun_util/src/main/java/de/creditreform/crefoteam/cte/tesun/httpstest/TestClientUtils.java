package de.creditreform.crefoteam.cte.tesun.httpstest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.apache.commons.io.FileUtils;

public class TestClientUtils {

    public String user = "tesuntestene";
    public String password = "tesuntestene";
    private List<String> urlsList;
    private final boolean isHttps;

    public TestClientUtils(boolean isHttps, String[] args) {
        this.isHttps = isHttps;
        for (String arg : args) {
            if (arg.startsWith("?")) {
                intro();
            }
            String argValue = arg.substring(2);
            if (arg.startsWith("-u")) {
                this.user = argValue;
            } else if (arg.startsWith("-p")) {
                this.password = argValue;
            } else if (arg.startsWith("-f")) {
                urlsList = readUrlsListFromFile(argValue);
            }
        }
        if (this.urlsList == null) {
            if (isHttps) {
                this.urlsList = Collections.singletonList("https://google.de");
            } else {
                this.urlsList = Collections.singletonList("http://google.de");
            }
        }
    }

    private void intro() {
        System.out.println("Start des Programms mit folgenden optionalen Parameter:");
        System.out.println("\t-uUsername : Benutzername");
        System.out.println("\t-pPasswort : Passwort");
        System.out.println("\t-fDateiname : Dateiname, die die zu testenden URL's enthält");
        System.exit(0);
    }

    private List<String> readUrlsListFromFile(String urlFileName) {
        String urlFile;
        URL fileURL = HttpTestClient.class.getResource("/" + urlFileName);
        if (fileURL != null) {
            urlFile = fileURL.getFile();
        } else {
            urlFile = System.getProperty("user.dir") + "/" + urlFileName;
        }
        List<String> readLines = new ArrayList<>();
        try {
            System.out.printf("Lese zu testende URL's aus der Datei %s\n", urlFile);
            List<String> readLinesAll = FileUtils.readLines(new File(urlFile));
            for (String line : readLinesAll) {
                if (!line.startsWith("#")) {
                    readLines.add(line);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return readLines;
    }

    public void testIt() {
        try {
            for (String theUrl : urlsList) {
                final HttpURLConnection connection;
                URL url = new URL(theUrl);
                if (isHttps) {
                    connection = (HttpsURLConnection) url.openConnection();
                } else {
                    connection = (HttpURLConnection) url.openConnection();
                }
                Authenticator.setDefault(new BasicAuthenticator());
                System.out.println("==============================================================");
                System.out.println("URL: " + connection.getURL());
                System.out.println("User: " + user);
                System.out.println("Passwort: " + password);
                if (isHttps) {
                    printHttpsCertificates((HttpsURLConnection) connection);
                }
                printResult(connection);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void printResult(HttpURLConnection connection) {
        if (connection != null) {
            try {
                System.out.println("Response-Code: " + connection.getResponseCode());
                System.out.println("Response-Content:");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String input;
                while ((input = br.readLine()) != null) {
                    System.out.println(input);
                }
                br.close();
                System.out.println("--------------------------------------------------------------");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void printHttpsCertificates(HttpsURLConnection httpsURLConnection) {
        if (httpsURLConnection != null) {
            try {
                System.out.println("Response Code : " + httpsURLConnection.getResponseCode());
                System.out.println("Cipher Suite : " + httpsURLConnection.getCipherSuite());
                System.out.println("\n");
                Certificate[] certs = httpsURLConnection.getServerCertificates();
                for (Certificate cert : certs) {
                    System.out.println("Cert Type : " + cert.getType());
                    System.out.println("Cert Hash Code : " + cert.hashCode());
                    System.out.println("Cert Public Key Algorithm : " + cert.getPublicKey().getAlgorithm());
                    System.out.println("Cert Public Key Format : " + cert.getPublicKey().getFormat());
                    System.out.println("\n");
                }
            } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private final class BasicAuthenticator extends Authenticator {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, password.toCharArray());
        }
    }
}
