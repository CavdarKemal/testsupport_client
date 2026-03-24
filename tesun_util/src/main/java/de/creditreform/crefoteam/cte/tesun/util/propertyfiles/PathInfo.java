package de.creditreform.crefoteam.cte.tesun.util.propertyfiles;

import java.io.File;

/**
 * Container für eine Pfadangabe, bestehend aus Basis-Verzeichnis,
 * Unterverzeichnis und vollständigem Pfad
 * Created by ralf on 13.11.14.
 */
public class PathInfo
        implements Comparable<PathInfo> {
    private final String curtomerKey;
    private final String baseDirPath;
    private final String relativeSubDir;
    private final String fullPath;

    public PathInfo(File srcDir, File fileInSubDir) {
        this.baseDirPath = srcDir.getPath();
        this.fullPath = fileInSubDir.getParent();
        if (fullPath == null) {
            throw new IllegalArgumentException("Datei '" + fileInSubDir.getPath() + "' enthält keine Pfadangabe");
        }
        if (fullPath.equals(baseDirPath)) {
            throw new IllegalArgumentException("Verzeichnis '" + fullPath + "' ist kein Unterverzeichnis von '" + baseDirPath + "'");
        }
        if (!fullPath.startsWith(baseDirPath)) {
            throw new IllegalArgumentException("Verzeichnis '" + fullPath + "' ist kein Unterverzeichnis von '" + baseDirPath + "'");
        }
        this.relativeSubDir = fullPath.substring(baseDirPath.length() + 1);
        this.curtomerKey = new File(relativeSubDir).getParent();
        if (curtomerKey == null) {
            throw new IllegalArgumentException("Verzeichnis '" + fullPath + "' ist kein Unterverzeichnis von '" + baseDirPath + "'");
        }
    }

    public String getCurtomerKey() {
        return curtomerKey;
    }

    public String getBaseDirPath() {
        return baseDirPath;
    }

    public String getRelativeSubDir() {
        return relativeSubDir;
    }

    public String getFullPath() {
        return fullPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PathInfo)) return false;

        PathInfo pathInfo = (PathInfo) o;

        if (!baseDirPath.equals(pathInfo.baseDirPath)) return false;
        return fullPath.equals(pathInfo.fullPath);
    }

    @Override
    public int hashCode() {
        int result = baseDirPath.hashCode();
        result = 31 * result + fullPath.hashCode();
        return result;
    }

    @Override
    public int compareTo(PathInfo o) {
        int cmp = this.baseDirPath.compareTo(o.baseDirPath);
        if (cmp == 0) {
            cmp = this.fullPath.compareTo(o.fullPath);
        }
        return cmp;
    }
}
