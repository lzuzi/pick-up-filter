package net.neoforge.pickupfilter;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = PickUpFilterNeoForge.MOD_ID, dist = Dist.CLIENT)
public class PickUpFilterClientNeoForge {
    public PickUpFilterClientNeoForge(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}