package net.neoforge.pickupfilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WhitelistLoader {
    private static final Logger LOGGER = LogManager.getLogger("PickUpFilter");
    private static final Path CONFIG_PATH = Paths.get("config", "pickupfilter.json");
    private static final File CONFIG_FILE = CONFIG_PATH.toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type CONFIG_TYPE = new TypeToken<Config>(){}.getType();

    public static Set<ResourceLocation> loadWhitelist() {
        try {
            if (!CONFIG_FILE.exists()) {
                saveDefault();
            }

            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                Config config = GSON.fromJson(reader, CONFIG_TYPE);
                if (config != null && config.whitelist != null) {
                    Set<ResourceLocation> ids = new HashSet<>();
                    for (String id : config.whitelist) {
                        ResourceLocation identifier = ResourceLocation.tryParse(id);
                        if (identifier != null) {
                            ids.add(identifier);
                        } else {
                            LOGGER.warn("Invalid ID in whitelist: " + id);
                        }
                    }
                    return ids;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read pickupfilter.json!", e);
        }
        return Collections.emptySet();
    }

    public static void saveWhitelist(Set<ResourceLocation> whitelist) {
        try {
            JsonObject root;

            if (Files.exists(CONFIG_FILE.toPath())) {
                String content = Files.readString(CONFIG_FILE.toPath());
                root = GSON.fromJson(content, JsonObject.class);
                if (root == null) root = new JsonObject();
            } else {
                root = new JsonObject();
            }

            JsonArray whitelistArray = new JsonArray();
            for (ResourceLocation id : whitelist) {
                whitelistArray.add(id.toString());
            }
            root.add("whitelist", whitelistArray);

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save pickupfilter.json!", e);
        }
    }

    private static void saveDefault() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            Config defaultConfig = new Config();
            defaultConfig.whitelist = new String[] {};
            GSON.toJson(defaultConfig, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save default pickupfilter.json!", e);
        }
    }

    private static class Config {
        public String[] whitelist;
    }
}
