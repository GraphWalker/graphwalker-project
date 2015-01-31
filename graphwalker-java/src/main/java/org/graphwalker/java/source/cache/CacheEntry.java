package org.graphwalker.java.source.cache;

import com.google.gson.annotations.Expose;

import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

/**
 * @author Nils Olsson
 */
public final class CacheEntry {

    @Expose
    private long modified;

    @Expose
    private boolean generated;

    public CacheEntry(long modified, boolean generated) {
        this.modified = modified;
        this.generated = generated;
    }

    public FileTime getLastModifiedTime() {
        return FileTime.from(modified, TimeUnit.MILLISECONDS);
    }

    public boolean isGenerated() {
        return generated;
    }
}
