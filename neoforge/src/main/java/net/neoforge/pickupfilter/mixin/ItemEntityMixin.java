package net.neoforge.pickupfilter.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import net.neoforge.pickupfilter.PickUpFilter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void onPickup(Player player, CallbackInfo ci) {
        ItemStack stack = ((ItemEntity)(Object)this).getItem();

        if (!PickUpFilter.filterEnabled) {
            return;
        }

        if (!isWhitelisted(stack)) {
            ci.cancel();
        }
    }

    private boolean isWhitelisted(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return PickUpFilter.WHITELIST.contains(id);
    }
}