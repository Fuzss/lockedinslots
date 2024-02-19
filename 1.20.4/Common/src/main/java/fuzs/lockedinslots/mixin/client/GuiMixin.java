package fuzs.lockedinslots.mixin.client;

import fuzs.lockedinslots.LockedInSlots;
import fuzs.lockedinslots.client.handler.NoSlotInteractionHandler;
import fuzs.lockedinslots.config.ClientConfig;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
abstract class GuiMixin {
    @Shadow
    private int screenWidth;
    @Shadow
    private int screenHeight;

    @Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", shift = At.Shift.AFTER, ordinal = 0))
    private void renderHotbar(float partialTick, GuiGraphics guiGraphics, CallbackInfo callback) {
        float alpha = (float) LockedInSlots.CONFIG.get(ClientConfig.class).guiHotbarOverlayAlpha;
        if (alpha > 0.0F) {
            for (int slot : LockedInSlots.CONFIG.get(ClientConfig.class).getLockedSlots()) {
                if (slot < Inventory.getSelectionSize()) {
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
                    guiGraphics.blitSprite(NoSlotInteractionHandler.LOCKED_SLOT_LOCATION, this.screenWidth / 2 - 91 + 3 + slot * 20, this.screenHeight - 22 + 3, 16, 16);
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }
    }
}
