package fuzs.lockedinslots.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.lockedinslots.LockedInSlots;
import fuzs.lockedinslots.config.ClientConfig;
import fuzs.lockedinslots.config.WorldSlotsStorage;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SlotOverlayHandler {
    public static final int MAX_TRIGGER_TIME = 72_000;

    private static int triggerTime, lastTriggerTime;
    @Nullable
    private static Slot hoveredSlot;

    public static EventResult onRenderTooltip(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, List<ClientTooltipComponent> components, ClientTooltipPositioner positioner) {
        if (triggerTime > 0.0F && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
            if (hoveredSlot != null && hoveredSlot.hasItem() && screen.hoveredSlot == hoveredSlot && screen.getMenu()
                    .getCarried()
                    .isEmpty()) {
                return EventResult.INTERRUPT;
            }
        }

        return EventResult.PASS;
    }

    public static void renderGuiLayer(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Player player = Minecraft.getInstance().gui.getCameraPlayer();
        if (player != null) {
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
            if (getNormalizedTriggerTime(partialTick) > 0.0F) {
                int selectedSlot = Minecraft.getInstance().player.getInventory().getSelectedSlot();
                int posX = guiGraphics.guiWidth() / 2 - 90 + selectedSlot * 20 + 2;
                int posY = guiGraphics.guiHeight() - 16 - 3;
                renderSlotOverlay(guiGraphics, posX, posY, partialTick);
            }
        }
    }

    public static void onAfterRender(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (hoveredSlot != null && getNormalizedTriggerTime(partialTick) > 0.0F) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(screen.leftPos, screen.topPos);
            renderSlotOverlay(guiGraphics, hoveredSlot.x, hoveredSlot.y, partialTick);
            guiGraphics.pose().popMatrix();
        }
    }

    private static void renderSlotOverlay(GuiGraphics guiGraphics, int posX, int posY, float partialTick) {
        float animationProgress = Math.clamp(getNormalizedTriggerTime(partialTick), 0.0F, 1.0F);
        // color kindly stolen from Bedrockify mod's slot highlight :P
        guiGraphics.fill(posX, posY + Mth.floor(16.0F * (1.0F - animationProgress)), posX + 16, posY + 16, 0X8955BA00);
    }

    private static float getNormalizedTriggerTime(float partialTick) {
        return Mth.lerp(partialTick, normalizeTriggerTime(lastTriggerTime), normalizeTriggerTime(triggerTime));
    }

    private static float normalizeTriggerTime(int triggerTime) {
        return Math.clamp(triggerTime / (float) LockedInSlots.CONFIG.get(ClientConfig.class).triggerLockTicks,
                0.0F,
                1.0F);
    }

    public static void onEndClientTick(Minecraft minecraft) {
        if (minecraft.player != null) {
            lastTriggerTime = triggerTime;
            if (isKeyDown(NoSlotInteractionHandler.LOCK_SLOT_KEY_MAPPING)
                    && !LockedInSlots.CONFIG.get(ClientConfig.class).switchLockInstantly()) {
                Slot slot = getHoveredSlot(minecraft.screen, minecraft.player);
                resetTriggerValues(slot);
                if (isValidSlot(slot, minecraft.player)) {
                    incrementTriggerTime(minecraft, slot);
                }
            } else {
                resetTriggerValues(null);
            }
        }
    }

    private static void resetTriggerValues(@Nullable Slot slot) {
        // reset trigger time only when the hovered slot changes
        if (hoveredSlot != slot) {
            triggerTime = 0;
            hoveredSlot = slot;
        }
    }

    private static @Nullable Slot getHoveredSlot(@Nullable Screen screen, Player player) {
        if (screen instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            if (abstractContainerScreen.getMenu().getCarried().isEmpty()) {
                return abstractContainerScreen.hoveredSlot;
            }
        } else if (screen == null) {
            NonNullList<Slot> slots = player.inventoryMenu.slots;
            for (int i = slots.size() - 1; i >= 0; i--) {
                if (slots.get(i).getContainerSlot() == player.getInventory().getSelectedSlot()) {
                    return slots.get(i);
                }
            }
        }

        return null;
    }

    private static boolean isValidSlot(@Nullable Slot slot, Player player) {
        if (slot != null && slot.allowModification(player) && slot.container instanceof Inventory) {
            int containerSlot = unwrapSlot(slot).getContainerSlot();
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

    private static void incrementTriggerTime(Minecraft minecraft, Slot slot) {
        // just make sure we only trigger once when the max time is reached, then set to some arbitrary value, so we do not trigger again
        if (triggerTime < MAX_TRIGGER_TIME
                && ++triggerTime >= LockedInSlots.CONFIG.get(ClientConfig.class).triggerLockTicks) {
            executeTriggerAction(minecraft, slot);
            triggerTime = MAX_TRIGGER_TIME;
        }
    }

    public static void executeTriggerAction(Minecraft minecraft) {
        if (LockedInSlots.CONFIG.get(ClientConfig.class).switchLockInstantly()) {
            Slot slot = getHoveredSlot(minecraft.screen, minecraft.player);
            executeTriggerAction(minecraft, slot);
        }
    }

    private static void executeTriggerAction(Minecraft minecraft, Slot slot) {
        if (isValidSlot(slot, minecraft.player)) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            WorldSlotsStorage.triggerSlotLock(unwrapSlot(slot).getContainerSlot());
        }
    }

    public static Slot unwrapSlot(Slot slot) {
        // creative mode inventory tab uses different slot ids :(
        return slot instanceof CreativeModeInventoryScreen.SlotWrapper slotWrapper ? slotWrapper.target : slot;
    }

    public static boolean isKeyDown(KeyMapping keyMapping) {
        // we need to listen to repeat events for the key press, this is not possible using the key mapping instance
        if (keyMapping.key.getType() == InputConstants.Type.KEYSYM && !keyMapping.isUnbound()) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), keyMapping.key.getValue());
        } else {
            return false;
        }
    }
}
