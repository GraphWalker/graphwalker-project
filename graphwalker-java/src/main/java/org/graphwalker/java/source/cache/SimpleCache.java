package org.graphwalker.java.source.cache;

/*-
 * #%L
 * GraphWalker Java
 * %%
 * Copyright (C) 2005 - 2017 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
