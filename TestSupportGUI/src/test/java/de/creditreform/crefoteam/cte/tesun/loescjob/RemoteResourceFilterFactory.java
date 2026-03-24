package de.creditreform.crefoteam.cte.tesun.loescjob;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.RemoteResourceFilter;
import net.schmizz.sshj.sftp.RemoteResourceInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class RemoteResourceFilterFactory {
    public static RemoteResourceFilter createRemoteResourceFileFilter(String jvmName, String fileName, Date filterDate) {
        RemoteResourceFilter remoteResourceFilter = resource -> {
//            printResourceInfo(resource);
            boolean contains = checkJvmName(jvmName, resource);
            if (contains && !resource.isDirectory()) {
                contains &= checkEndsWith(fileName, resource);
                if (contains) {
                    boolean before = isBefore(filterDate, resource);
                    contains &= before;
                    return contains;
                }
            }
            return contains;
        };
        return remoteResourceFilter;
    }

    public static RemoteResourceFilter createRemoteResourceDirFilter(String jvmName, Date filterDate) {
        RemoteResourceFilter remoteResourceFilter = resource -> {
            printResourceInfo(resource);
            boolean contains = checkJvmName(jvmName, resource);
            if (contains && resource.isDirectory()) {
                boolean before = isBefore(filterDate, resource);
                contains &= before;
                return contains;
            }
            return false;
        };
        return remoteResourceFilter;
    }

    private static boolean isBefore(Date filterDate, RemoteResourceInfo resource) {
        Date parsedDate = getResourceLastAccessDate(resource);
        boolean before = parsedDate.before(filterDate);
        return before;
    }

    public static Date getResourceLastAccessDate(RemoteResourceInfo resource) {
        FileAttributes attributes = resource.getAttributes();
        long atime = attributes.getMtime();
        Date parsedDate = new Date(1000L * atime);
        return parsedDate;
    }

    private static boolean checkJvmName(String jvmName, RemoteResourceInfo resource) {
        return resource.getPath().contains(jvmName);
    }

    private static boolean checkEndsWith(String fileName, RemoteResourceInfo resource) {
        return resource.getName().endsWith(fileName);
    }

    public static void printResourceInfo(RemoteResourceInfo resource) {
        Date resDate = RemoteResourceFilterFactory.getResourceLastAccessDate(resource);
        String strInfo = String.format("Resource: %s; LastModifed: %s", resource.getPath(), resDate);
        System.out.println(strInfo);
    }

}
