package net.fabric.pickupfilter.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabric.pickupfilter.PickUpFilterFabric;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.entity.ItemEntity;
import net.minecraft.registry.Registries;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(ItemEntity.class)
public class ItemEntityMixinFabric {
    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    private void onPickup(PlayerEntity player, CallbackInfo ci) {
        ItemStack stack = ((ItemEntity)(Object)this).getStack();

        if(!PickUpFilterFabric.filterEnabled) {
            return;
        }

        if (!isWhitelisted(stack)) {
            ci.cancel();
        }
    }

    private boolean isWhitelisted(ItemStack stack) {
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return PickUpFilterFabric.WHITELIST.contains(id);
    }
}
