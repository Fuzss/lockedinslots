package fuzs.lockedinslots.mixin.client;

import com.mojang.datafixers.util.Pair;
import fuzs.lockedinslots.client.handler.NoSlotInteractionHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenMixin extends Screen {

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @ModifyVariable(method = "renderSlot", at = @At("STORE"))
    protected Pair<ResourceLocation, ResourceLocation> renderSlot(Pair<ResourceLocation, ResourceLocation> noItemIcon, GuiGraphics guiGraphics, Slot slot) {
        // replace the vanilla item icon in case one is present
        return NoSlotInteractionHandler.getNoItemIcon(slot).orElse(noItemIcon);
    }

    @ModifyVariable(method = "renderSlot", at = @At("LOAD"), ordinal = 1)
    protected boolean renderSlot(boolean skipItemDecorations, GuiGraphics guiGraphics, Slot slot) {
        // render our item icon for slots that have an item in them, vanilla only renders for empty slots which we handle in the other method
        if (slot.isActive() && slot.hasItem()) {
            Optional<Pair<ResourceLocation, ResourceLocation>> optional = NoSlotInteractionHandler.getNoItemIcon(slot);
            if (optional.isPresent()) {
                Pair<ResourceLocation, ResourceLocation> pair = optional.get();
                TextureAtlasSprite textureAtlasSprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(
                        pair.getSecond());
                guiGraphics.blit(slot.x, slot.y, 0, 16, 16, textureAtlasSprite);
            }
        }
        return skipItemDecorations;
    }
}
