package de.creditreform.crefoteam.cte.tesun.util.directorytree;

import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;

/**
 * {@link IOFileFilter}, der nur Property-Dateien akzeptiert
 * User: ralf
 * Date: 14.02.14
 * Time: 10:19
 */
public class AcceptPropertyFilesFileFilter
        extends AbstractFileFilter
        implements IOFileFilter {

    @Override
    public boolean accept(File file) {
        return file.isFile() && file.getName().endsWith(".properties");
    }

}
