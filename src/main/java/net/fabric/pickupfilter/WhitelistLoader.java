package net.fabric.pickupfilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WhitelistLoader {
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "pickupfilter.json");
    private static final Gson GSON = new Gson();
    private static final Type CONFIG_TYPE = new TypeToken<Config>(){}.getType();

    public static Set<Identifier> loadWhitelist() {
        try {
            if (!CONFIG_FILE.exists()) {
                saveDefault();
            }

            FileReader reader = new FileReader(CONFIG_FILE);
            Config config = GSON.fromJson(reader, CONFIG_TYPE);
            reader.close();

            if (config != null && config.whitelist != null) {
                Set<Identifier> ids = new HashSet<>();
                for (String id : config.whitelist) {
                    Identifier identifier = Identifier.tryParse(id);
                    if (identifier != null) {
                        ids.add(identifier);
                    } else {
                        PickUpFilter.LOGGER.warn("Invalid ID: " + id);
                    }
                }
                return ids;
            }
        } catch (Exception e) {
            PickUpFilter.LOGGER.error("Failed to read pickupfilter.json!", e);
        }
        return Collections.emptySet();
    }

    public static void saveWhitelist(Set<Identifier> whitelist) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            JsonObject root;
            if (Files.exists(CONFIG_FILE.toPath())) {
                String content = Files.readString(CONFIG_FILE.toPath());
                root = gson.fromJson(content, JsonObject.class);
                if (root == null) root = new JsonObject();
            } else {
                root = new JsonObject();
            }

            JsonArray whitelistArray = new JsonArray();
            for (Identifier id : whitelist) {
                whitelistArray.add(id.toString());
            }

            root.add("whitelist", whitelistArray);

            String newJson = gson.toJson(root);
            Files.writeString(CONFIG_FILE.toPath(), newJson);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveDefault() {
        try {
            Config defaultConfig = new Config();
            defaultConfig.whitelist = new String[] {  };
            FileWriter writer = new FileWriter(CONFIG_FILE);
            GSON.toJson(defaultConfig, writer);
            writer.close();
        } catch (Exception e) {
            PickUpFilter.LOGGER.error("Failed to save default pickupfilter.json!", e);
        }
    }

    static class Config {
        public String[] whitelist;
    }
}