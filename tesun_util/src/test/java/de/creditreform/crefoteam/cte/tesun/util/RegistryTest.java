package de.creditreform.crefoteam.cte.tesun.util;

import de.creditreform.crefoteam.ctcbase.sftputil.except.SftpUtilException;
import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpConnection;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RegistryTest {
    @Test
    public void testWinRegistry() throws IOException, InterruptedException {
        List<WinScpRecord> winScpRecordList = new ArrayList<>();
        ProcessBuilder builder = new ProcessBuilder("reg", "query", "HKEY_CURRENT_USER\\SOFTWARE\\Martin Prikryl\\WinSCP 2\\Sessions");
        if (System.getProperty("os.name").equals("Linux")) {
            return;
        }
        Process reg = builder.start();
        try (BufferedReader output = new BufferedReader(new InputStreamReader(reg.getInputStream()))) {
            output.lines().filter(l -> !l.isEmpty()).forEach(winScpKey -> {
                try {
                    WinScpRecord winScpRecord = handleWinScpKey(winScpKey);
                    if (winScpRecord != null) {
                        winScpRecordList.add(winScpRecord);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        String outPath = System.getProperty("user.dir");
        File outFile = new File(outPath).getParentFile().getParentFile().getParentFile();
        outFile = new File(outFile, "Win-SCP-Connections-With-Passwords-" + TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MINUS_MM_MINUS_SS.format(Calendar.getInstance()) + ".csv");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Config;Host;User;Passowrd;Connect Status");
        winScpRecordList.stream().forEach(winScpRecord -> {
            String pwd = winScpRecord.getEncryptedPasswort().contains("Mei") ? "MK2" : winScpRecord.getEncryptedPasswort().contains("cavdark") ? "ck" : winScpRecord.getEncryptedPasswort();
            String strLine = "\n" + winScpRecord.getWinScpConfigName() +
                    ";" + winScpRecord.getHostName() +
                    ";" + winScpRecord.getUserName() +
                    ";" + pwd +
                    ";" + winScpRecord.getConnectionStatus();
            stringBuilder.append(strLine);
            System.out.print(strLine);
        });
        FileUtils.writeStringToFile(outFile, stringBuilder.toString());
        System.out.println("\nOutput-File in \n" + outFile.getAbsolutePath());
        reg.waitFor();
    }

    private WinScpRecord handleWinScpKey(String winScpKey) throws InterruptedException, IOException {
        WinScpRecord winScpRecord = new WinScpRecord(extractConfigNameFromKey(winScpKey));
        ProcessBuilder builder = new ProcessBuilder("reg", "query", winScpKey);
        Process reg = builder.start();
        try (BufferedReader output = new BufferedReader(new InputStreamReader(reg.getInputStream()))) {
            output.lines().filter(l -> !l.isEmpty() && !l.equals(winScpKey)).forEach(key -> {
                String[] split = key.split("  ");
                if (split[2].contains("HostName")) {
                    winScpRecord.setHostName(split[6]);
                }
                if (split[2].contains("UserName")) {
                    winScpRecord.setUserName(split[6]);
                }
                if (split[2].contains("Password")) {
                    winScpRecord.setPasswort(split[6]);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        reg.waitFor();
        return winScpRecord.checkConsistence();
    }

    private String extractConfigNameFromKey(String winScpKey) {
        String[] split = winScpKey.split("\\\\");
        return split[split.length - 1];
    }

    private static class WinScpRecord {
        private final String winScpConfigName;
        private String hostName;
        private String userName;
        private String passwort;
        private String encryptedPasswort;

        private WinScpRecord(String winScpConfigName) {
            this.winScpConfigName = winScpConfigName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public void setPasswort(String passwort) {
            this.passwort = passwort;
            if (hostName != null && userName != null) {
                this.encryptedPasswort = extracted(passwort);
            }
        }

        private String extracted(String passwort) {
            AtomicReference<String> pwd = new AtomicReference<>("");
            String prgName = System.getProperty("user.dir") + "/target/test-classes/winscppasswd.exe";
            ProcessBuilder builder = new ProcessBuilder(prgName, hostName, userName, passwort);
            try {
                Process reg = builder.start();
                try (BufferedReader output = new BufferedReader(new InputStreamReader(reg.getInputStream()))) {
                    pwd.set(output.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                reg.waitFor();
                return pwd.get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public String getWinScpConfigName() {
            return winScpConfigName;
        }

        public String getHostName() {
            return hostName;
        }

        public String getUserName() {
            return userName;
        }

        public String getPasswort() {
            return passwort;
        }

        public String getEncryptedPasswort() {
            return encryptedPasswort;
        }

        public WinScpRecord checkConsistence() {
            if (hostName != null && userName != null && passwort != null) {
                return this;
            }
            return null;
        }

        public String getConnectionStatus() {
            try {
                RestInvokerConfig restInvokerConfig = new RestInvokerConfig(getHostName() + ":22", getUserName(), getEncryptedPasswort());
                try (SftpConnection sftpConnection = SftpConnection
                        .forTrustedHost(restInvokerConfig.getServiceHost(), 22)
                        .setAdditionalAlgo(TestSupportClientKonstanten.ALGORITHM_SSH_RSA)
                        .andUser(restInvokerConfig.getServiceUser(), restInvokerConfig.getServicePassword())) {
                    sftpConnection.connect();
                    // sftpConnection.ls("/home");
                    return "OK";
                } catch (SftpUtilException ex) {
                    return ex.getMessage();
                }
            } catch (Throwable th) {
                return th.getMessage();
            }
        }
    }
}