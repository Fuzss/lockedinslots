package fuzs.lockedinslots.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import fuzs.lockedinslots.LockedInSlots;
import fuzs.lockedinslots.config.ClientConfig;
import fuzs.puzzleslib.api.client.gui.v2.screen.ScreenHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

import java.util.Optional;

public class NoSlotInteractionHandler {
    public static final String KEY_CATEGORY = "key.categories." + LockedInSlots.MOD_ID;
    public static final KeyMapping LOCK_SLOT_KEY_MAPPING = new KeyMapping("key.lockSlot",
            InputConstants.KEY_J,
            KEY_CATEGORY
    );
    public static final ResourceLocation LOCKED_SLOT_LOCATION = LockedInSlots.id("item/locked_slot");

    public static boolean isHoveringLockedSlot(AbstractContainerScreen<?> screen) {
        Slot hoveredSlot = ScreenHelper.INSTANCE.getHoveredSlot(screen);
        if (hoveredSlot != null && hoveredSlot.container instanceof Inventory) {
            return hoveredSlot.hasItem() &&
                    LockedInSlots.CONFIG.get(ClientConfig.class).isSlotLocked(TriggerLockRenderHandler.getContainerSlot(hoveredSlot));
        }
        return false;
    }

    public static EventResult onBeforeMouseScroll(AbstractContainerScreen<?> screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return isHoveringLockedSlot(screen) ? EventResult.INTERRUPT : EventResult.PASS;
    }

    public static EventResult onBeforeMouseDrag(AbstractContainerScreen<?> screen, double mouseX, double mouseY, int button, double dragX, double dragY) {
        return isHoveringLockedSlot(screen) ? EventResult.INTERRUPT : EventResult.PASS;
    }

    public static EventResult onBeforeMouseClick(AbstractContainerScreen<?> screen, double mouseX, double mouseY, int button) {
        return isHoveringLockedSlot(screen) ? EventResult.INTERRUPT : EventResult.PASS;
    }

    public static EventResult onBeforeMouseRelease(AbstractContainerScreen<?> screen, double mouseX, double mouseY, int button) {
        return isHoveringLockedSlot(screen) ? EventResult.INTERRUPT : EventResult.PASS;
    }

    public static EventResult onBeforeKeyPress(AbstractContainerScreen<?> screen, int key, int scanCode, int modifiers) {
        if (LOCK_SLOT_KEY_MAPPING.matches(key, scanCode)) {
            // this is handled during rendering without taking the key mapping instance into account
            return EventResult.INTERRUPT;
        } else {
            Minecraft minecraft = ScreenHelper.INSTANCE.getMinecraft(screen);
            // prevent swapping slots if the hotbar slot to swap with is locked
            for (int i = 0; i < Inventory.getSelectionSize(); ++i) {
                if (minecraft.options.keyHotbarSlots[i].matches(key, scanCode)) {
                    if (!minecraft.player.getInventory().getItem(i).isEmpty() &&
                            LockedInSlots.CONFIG.get(ClientConfig.class).isSlotLocked(i)) {
                        return EventResult.INTERRUPT;
                    }
                }
            }
            if (isHoveringLockedSlot(screen)) {
                // don't block all keys, so closing via esc or inventory key still works when hovering a locked slot
                if (minecraft.options.keySwapOffhand.matches(key, scanCode) ||
                        minecraft.options.keyDrop.matches(key, scanCode)) {
                    return EventResult.INTERRUPT;
                }
            }
        }

        return EventResult.PASS;
    }

    public static void onStartClientTick(Minecraft minecraft) {
        if (minecraft.player != null) {
            int slot = minecraft.player.getInventory().selected;
            if (LockedInSlots.CONFIG.get(ClientConfig.class).isSlotLocked(slot)) {
                while (minecraft.options.keyDrop.consumeClick()) {
                    // NO-OP
                }
            }
        }
    }

    public static Optional<Pair<ResourceLocation, ResourceLocation>> getNoItemIcon(Slot slot) {
        if (slot.container instanceof Inventory &&
                LockedInSlots.CONFIG.get(ClientConfig.class).isSlotLocked(TriggerLockRenderHandler.getContainerSlot(slot))) {
            return Optional.of(Pair.of(InventoryMenu.BLOCK_ATLAS, LOCKED_SLOT_LOCATION));
        } else {
            return Optional.empty();
        }
    }
}
