package org.graphwalker.java.source.cache;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class SimpleCacheTest {

  @Test
  public void get() throws IOException {
    SimpleCache cache = new SimpleCache(Paths.get("./"));
    // create temporary file
    File foo = File.createTempFile("prefix", "suffix");
    foo.deleteOnExit();
    Path key = foo.toPath();
    long lastModified = foo.lastModified();
    cache.add(key, new CacheEntry(lastModified, true));
    CacheEntry cached = cache.get(key);
    assertEquals(lastModified, cached.getLastModifiedTime().toMillis());
    assertEquals(true, cached.isGenerated());

    assertTrue(cache.contains(key));
    
      // Clean up after test
      assertTrue( new File("./cache.json").delete() );
  }
}
