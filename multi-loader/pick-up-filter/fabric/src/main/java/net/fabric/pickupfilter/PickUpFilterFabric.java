package net.fabric.pickupfilter;

import net.common.pickupfilter.PickUpFilter;
import net.fabricmc.api.ModInitializer;

public class PickUpFilterFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PickUpFilter.init();
    }
}