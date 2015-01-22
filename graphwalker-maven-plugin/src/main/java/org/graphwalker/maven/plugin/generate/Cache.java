package org.graphwalker.maven.plugin.generate;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nils Olsson
 */
public class Cache {

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static final Path path = Paths.get("target", "graphwalker", "generate-cache.json");
    private static final Type type = new TypeToken<HashMap<String, CacheEntry>>() {}.getType();
    private final Map<String, CacheEntry> storage = new HashMap<>();

    public Cache() {
        read();
    }

    private void read() {
        if (Files.exists(path)) {
            try {
                String json = new String(Files.readAllBytes(path), Charsets.UTF_8);
                Map<String, CacheEntry> data = gson.fromJson(json, type);
                storage.putAll(data);
            } catch (IOException e) {
                //
            }
        }
    }

    public void save() {
        try {
            String json = gson.toJson(storage);
            Files.deleteIfExists(path);
            Files.createDirectories(path.getParent());
            Files.write(path, json.getBytes(Charsets.UTF_8));
        } catch (IOException e) {
            //
        }
    }

    public boolean containsKey(Path path) {
        return storage.containsKey(path.toString());
    }

    public CacheEntry get(Path path) {
        return storage.get(path.toString());
    }

    public void put(Path path, CacheEntry entry) {
        storage.put(path.toString(), entry);
    }
}
