package net.neoforge.pickupfilter;

import net.common.pickupfilter.PickUpFilter;
import net.common.pickupfilter.WhitelistLoader;

import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.minecraft.resources.ResourceLocation;

public class WhitelistLoaderNeoForge {
    private static final Path CONFIG_PATH = Paths.get("config", "pickupfilter.json");
    private static final File CONFIG_FILE = CONFIG_PATH.toFile();

    public static Set<ResourceLocation> loadWhitelist() {
        Set<String> ids = WhitelistLoader.loadWhitelist(CONFIG_FILE);
        Set<ResourceLocation> whitelist = new HashSet<>();
        for (String id : ids) {
            try {
                ResourceLocation rl = ResourceLocation.tryParse(id);
                if (rl != null) whitelist.add(rl);
            } catch (Exception e) {
                PickUpFilter.LOGGER.warn("Failed to load whitelist at " + id, e);
            }
        }
        return whitelist;
    }

    public static void saveWhitelist(Set<ResourceLocation> whitelist) {
        Set<String> ids = new HashSet<>();
        for (ResourceLocation rl : whitelist) {
            ids.add(rl.toString());
        }
        WhitelistLoader.saveWhitelist(CONFIG_FILE, ids);
    }
}
