package org.graphwalker.maven.plugin.generate;

import com.google.gson.annotations.Expose;

import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

/**
 * @author Nils Olsson
 */
public class CacheEntry {

    @Expose
    private long modified;

    public CacheEntry(long modified) {
        this.modified = modified;
    }

    public FileTime getLastModifiedTime() {
        return FileTime.from(modified, TimeUnit.MILLISECONDS);
    }
}
