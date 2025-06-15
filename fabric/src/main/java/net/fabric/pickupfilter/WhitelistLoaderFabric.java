package net.fabric.pickupfilter;

import net.common.pickupfilter.PickUpFilter;
import net.common.pickupfilter.WhitelistLoader;

import java.util.Set;
import java.util.HashSet;
import java.io.File;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class WhitelistLoaderFabric {
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "pickupfilter.json");

    public static Set<Identifier> loadWhitelist() {
        Set<String> ids = WhitelistLoader.loadWhitelist(CONFIG_FILE);
        Set<Identifier> whitelist = new HashSet<>();
        for (String id : ids) {
            Identifier identifier = Identifier.tryParse(id);
            if (identifier != null) {
                whitelist.add(identifier);
            } else {
                PickUpFilter.LOGGER.warn("Invalid whitelist entry: " + id);
            }
        }
        return whitelist;
    }

    public static void saveWhitelist(Set<Identifier> whitelist) {
        Set<String> ids = new HashSet<>();
        for (Identifier id : whitelist) {
            ids.add(id.toString());
        }
        WhitelistLoader.saveWhitelist(CONFIG_FILE, ids);
    }
}
