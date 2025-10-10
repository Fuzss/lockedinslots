package fuzs.lockedinslots.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.lockedinslots.LockedInSlots;
import fuzs.lockedinslots.config.ClientConfig;
import fuzs.lockedinslots.config.WorldSlotsStorage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public class TriggerLockRenderHandler {
    public static final int MAX_TRIGGER_TIME = 72_000;

    private static Slot hoveredSlot;
    private static float triggerTime;

    public static void onAfterRender(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (TriggerLockRenderHandler.isKeyDown(NoSlotInteractionHandler.LOCK_SLOT_KEY_MAPPING)) {
            Slot hoveredSlot = screen.hoveredSlot;
            if (TriggerLockRenderHandler.hoveredSlot != hoveredSlot) {
                // reset trigger time when the hovered slot changes
                triggerTime = 0.0F;
                TriggerLockRenderHandler.hoveredSlot = hoveredSlot;
            }
            if (isLockableSlot(hoveredSlot)) {
                incrementTriggerTime(screen.minecraft, hoveredSlot, partialTick);
                float animationProgress = Math.clamp(
                        triggerTime / LockedInSlots.CONFIG.get(ClientConfig.class).triggerLockTicks, 0.0F, 1.0F);
                if (animationProgress > 0.0F && animationProgress < 1.0F) {
                    if (WorldSlotsStorage.isSlotLocked(hoveredSlot)) {
                        animationProgress = 1.0F - animationProgress;
                    }
                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().translate(screen.leftPos, screen.topPos);
                    int posX = hoveredSlot.x;
                    int posY = hoveredSlot.y + Mth.floor(16.0F * (1.0F - animationProgress));
                    // high z offset to render in front of carried item stack
                    // color kindly stolen from Bedrockify mod's slot highlight :P
                    guiGraphics.fill(posX, posY, posX + 16, posY + Mth.ceil(16.0F * animationProgress), 0X8955BA00);
                    guiGraphics.pose().popMatrix();
                }
            }
        } else {
            triggerTime = 0.0F;
            hoveredSlot = null;
        }
    }

    private static boolean isLockableSlot(@Nullable Slot slot) {
        if (slot != null && slot.container instanceof Inventory) {
            int containerSlot = getContainerSlot(slot);
            if (WorldSlotsStorage.isSlotLocked(containerSlot)) {
                // always allow unlocking locked slots
                return true;
            } else if (containerSlot < Inventory.getSelectionSize()
                    || LockedInSlots.CONFIG.get(ClientConfig.class).allowLockingAllSlots) {
                if (slot.hasItem()) {
                    return !slot.getItem().isStackable()
                            || !LockedInSlots.CONFIG.get(ClientConfig.class).itemMustNotBeStackable;
                } else {
                    return !LockedInSlots.CONFIG.get(ClientConfig.class).slotMustNotBeEmpty;
                }
            }
        }

        return false;
    }

    private static void incrementTriggerTime(Minecraft minecraft, Slot slot, float partialTick) {
        if ((triggerTime += partialTick) >= LockedInSlots.CONFIG.get(ClientConfig.class).triggerLockTicks) {
            // just make sure we only trigger once when the max time is reached, then set to some arbitrary value, so we do not trigger again
            if (triggerTime < MAX_TRIGGER_TIME) {
                WorldSlotsStorage.triggerSlotLock(getContainerSlot(slot));
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                triggerTime = MAX_TRIGGER_TIME;
            }
        }
    }

    public static int getContainerSlot(Slot slot) {
        // creative mode inventory tab uses different slot ids :(
        return slot instanceof CreativeModeInventoryScreen.SlotWrapper slotWrapper ?
                slotWrapper.target.getContainerSlot() : slot.getContainerSlot();
    }

    public static boolean isKeyDown(KeyMapping keyMapping) {
        // we need to listen to repeat events for the key press, this is not possible using the key mapping instance
        if (keyMapping.key.getType() == InputConstants.Type.KEYSYM
                && keyMapping.key.getValue() != InputConstants.UNKNOWN.getValue()) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), keyMapping.key.getValue());
        } else {
            return false;
        }
    }
}
