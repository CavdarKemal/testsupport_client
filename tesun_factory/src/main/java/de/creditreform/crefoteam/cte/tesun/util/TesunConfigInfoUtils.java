package de.creditreform.crefoteam.cte.tesun.util;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigExportInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;

import javax.naming.InsufficientResourcesException;
import java.io.File;
import java.io.FileFilter;
import java.util.List;

public class TesunConfigInfoUtils {

    public static String findRelativePathForCustomer(TesunConfigInfo tesunConfigInfo, String customerKey) throws InsufficientResourcesException {
        List<TesunConfigExportInfo> exportPfade = tesunConfigInfo.getExportPfade();
        for (TesunConfigExportInfo exportPfad : exportPfade) {
            final String kundenKuerzel = exportPfad.getKundenKuerzel();
            boolean ok = kundenKuerzel.equalsIgnoreCase(customerKey);
            if (ok) {
                String[] split = exportPfad.getRelativePath().split(":");
                String relativePath = split[2].substring(2);
                if (relativePath.startsWith("/")) {
                    relativePath = relativePath.substring(1);
                }
                return relativePath;
            }
        }
        throw new InsufficientResourcesException("Export-Path für den Kunden '" + customerKey + "' wurde nicht gesetzt!");
    }
    public static String findExportHostForCustomer(TesunConfigInfo tesunConfigInfo, String customerKey) throws InsufficientResourcesException {
        List<TesunConfigExportInfo> exportPfade = tesunConfigInfo.getExportPfade();
        for (TesunConfigExportInfo exportPfad : exportPfade) {
            final String kundenKuerzel = exportPfad.getKundenKuerzel();
            boolean ok = kundenKuerzel.equalsIgnoreCase(customerKey);
            if (ok) {
                String[] split = exportPfad.getRelativePath().split("@");
                String[] split2 = split[1].split("/");
                return split2[0];
            }
        }
        throw new InsufficientResourcesException("Export-Path für den Kunden '" + customerKey + "' wurde nicht gesetzt!");
    }

    public static String findExportUrlForCustomer(TesunConfigInfo tesunConfigInfo, String customerKey) throws InsufficientResourcesException {
        List<TesunConfigExportInfo> exportPfade = tesunConfigInfo.getExportPfade();
        for (TesunConfigExportInfo exportPfad : exportPfade) {
            final String kundenKuerzel = exportPfad.getKundenKuerzel();
            boolean ok = kundenKuerzel.equalsIgnoreCase(customerKey);
            if (ok) {
                return exportPfad.getRelativePath();
            }
        }
        throw new InsufficientResourcesException("Export-Path für den Kunden '" + customerKey + "' wurde nicht gesetzt!");
    }

    public static TesunConfigInfo buildTesunConfigInfoFromDir(String exportsDirPath) {
        TesunConfigInfo tesunConfigInfo = new TesunConfigInfo();
        tesunConfigInfo.setUmgebungsKuerzel("LOC");
        File[] listRoots = new File(exportsDirPath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File theFile) {
                return theFile.isDirectory();
            }
        });
        List<TesunConfigExportInfo> exportPfade = tesunConfigInfo.getExportPfade();
        if (listRoots == null) {
            listRoots = new File[]{};
        }
        for (int i = 0; i < listRoots.length; i++) {
            File theFile = new File(listRoots[i].getName());
            String exportName = theFile.getName();
            TesunConfigExportInfo TesunConfigExportInfo = new TesunConfigExportInfo();
            TesunConfigExportInfo.setKundenKuerzel(exportName);
            TesunConfigExportInfo.setNamedAs(String.format("cteTestclient.%s.exportDir", exportName));
            TesunConfigExportInfo.setRelativePath(theFile.getPath() + File.separator + "export" + File.separator + "delta");
            exportPfade.add(TesunConfigExportInfo);
        }
        return tesunConfigInfo;
    }

}
