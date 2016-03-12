package org.graphwalker.java.source.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nils Olsson
 */
public final class SimpleCache implements Cache<Path, CacheEntry> {

  private static final Logger logger = LoggerFactory.getLogger(SimpleCache.class);
  private static final Type type = new TypeToken<HashMap<String, CacheEntry>>() {
  }.getType();
  private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
  private final Path path;
  private final Map<String, CacheEntry> storage = new HashMap<>();

  public SimpleCache(Path path) {
    this.path = path.resolve(Paths.get("cache.json"));
    read();
  }

  private void read() {
    if (Files.exists(path)) {
      try {
        String json = new String(Files.readAllBytes(path), Charset.forName("UTF-8"));
        Map<String, CacheEntry> data = gson.fromJson(json, type);
        storage.putAll(data);
      } catch (IOException e) {
        logger.error(e.getMessage());
        throw new CacheException(e);
      }
    }
  }

  private void save() {
    try {
      String json = gson.toJson(storage);
      Files.createDirectories(path.getParent());
      Files.write(path, json.getBytes(Charset.forName("UTF-8")));
    } catch (IOException e) {
      logger.error(e.getMessage());
      throw new CacheException(e);
    }
  }

  @Override
  public CacheEntry get(Path key) {
    return storage.get(key.toString());
  }

  @Override
  public void add(Path key, CacheEntry value) {
    storage.put(key.toString(), value);
    save();
  }

  @Override
  public boolean contains(Path key) {
    return storage.containsKey(key.toString());
  }
}
