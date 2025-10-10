package fuzs.lockedinslots.mixin.client;

import fuzs.lockedinslots.client.handler.NoSlotInteractionHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenMixin extends Screen {

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(
            method = "renderSlot", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
            shift = At.Shift.BEFORE,
            ordinal = 0
    ), slice = @Slice(
            from = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;recalculateQuickCraftRemaining()V"
            )
    )
    )
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo callback) {
        // render our item icon for slots that have an item in them,
        // vanilla only renders for empty slots which we handle in the other method
        if (slot.hasItem() && slot.isActive()) {
            NoSlotInteractionHandler.getNoItemIcon(slot)
                    .ifPresent((ResourceLocation resourceLocation) -> guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                            resourceLocation,
                            slot.x,
                            slot.y,
                            16,
                            16));
        }
    }

    @ModifyVariable(method = "renderSlot", at = @At("STORE"))
    protected ResourceLocation renderSlot(ResourceLocation noItemIcon, GuiGraphics guiGraphics, Slot slot) {
        // replace the vanilla item icon in case one is present
        return NoSlotInteractionHandler.getNoItemIcon(slot).orElse(noItemIcon);
    }
}
