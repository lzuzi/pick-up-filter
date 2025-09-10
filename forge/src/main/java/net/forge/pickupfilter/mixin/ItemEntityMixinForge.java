package net.forge.pickupfilter.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.forge.pickupfilter.PickUpFilterForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixinForge {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void onPickup(Player player, CallbackInfo ci) {
        ItemStack stack = ((ItemEntity)(Object)this).getItem();

        if (!PickUpFilterForge.filterEnabled) {
            return;
        }

        if (!isWhitelisted(stack)) {
            ci.cancel();
        }
    }

    private boolean isWhitelisted(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return PickUpFilterForge.WHITELIST.contains(id);
    }
}
