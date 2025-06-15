package net.common.pickupfilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WhitelistLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Set<String> loadWhitelist(File configFile) {
        try {
            if (!configFile.exists()) {
                saveDefault(configFile);
            }

            FileReader reader = new FileReader(configFile);
            Config config = GSON.fromJson(reader, Config.class);
            reader.close();

            if (config != null && config.whitelist != null) {
                Set<String> ids = new HashSet<>();
                Collections.addAll(ids, config.whitelist);
                return ids;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptySet();
    }

    public static void saveWhitelist(File configFile, Set<String> whitelist) {
        try {
            JsonObject root;
            if (Files.exists(configFile.toPath())) {
                String content = Files.readString(configFile.toPath());
                root = GSON.fromJson(content, JsonObject.class);
                if (root == null) root = new JsonObject();
            } else {
                root = new JsonObject();
            }

            JsonArray whitelistArray = new JsonArray();
            for (String id : whitelist) {
                whitelistArray.add(id);
            }
            root.add("whitelist", whitelistArray);

            String newJson = GSON.toJson(root);
            Files.writeString(configFile.toPath(), newJson);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveDefault(File configFile) {
        try {
            Config defaultConfig = new Config();
            defaultConfig.whitelist = new String[] { };
            FileWriter writer = new FileWriter(configFile);
            GSON.toJson(defaultConfig, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class Config {
        public String[] whitelist;
    }
}
