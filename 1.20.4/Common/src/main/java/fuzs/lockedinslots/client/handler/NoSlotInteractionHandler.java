package fuzs.lockedinslots.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import fuzs.lockedinslots.LockedInSlots;
import fuzs.lockedinslots.config.ClientConfig;
import fuzs.puzzleslib.api.client.gui.v2.screen.ScreenHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

import java.util.Optional;

public class NoSlotInteractionHandler {
    public static final String KEY_CATEGORY = "key.categories." + LockedInSlots.MOD_ID;
    public static final String KEY_NAME = "key.lockSlot";
    public static final KeyMapping LOCK_SLOT_KEY_MAPPING = new KeyMapping(KEY_NAME,
            InputConstants.KEY_L,
            KEY_CATEGORY
    );
    public static final ResourceLocation LOCKED_SLOT_LOCATION = LockedInSlots.id("item/locked_slot");
    public static final String KEY_SLOT_UNLOCK = "screen.slot.unlock";

    private static boolean isHoveringLockedSlot(AbstractContainerScreen<?> screen) {
        return isHoveringLockedSlot(screen, false);
    }

    public static boolean isHoveringLockedSlot(AbstractContainerScreen<?> screen, boolean isEmpty) {
        Slot hoveredSlot = ScreenHelper.INSTANCE.getHoveredSlot(screen);
        if (hoveredSlot != null && hoveredSlot.container instanceof Inventory) {
            return isEmpty != hoveredSlot.hasItem() &&
                    LockedInSlots.CONFIG.get(ClientConfig.class).isSlotLocked(hoveredSlot.getContainerSlot());
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
        Minecraft minecraft = ScreenHelper.INSTANCE.getMinecraft(screen);
        if (LOCK_SLOT_KEY_MAPPING.matches(key, scanCode)) {
            Slot hoveredSlot = ScreenHelper.INSTANCE.getHoveredSlot(screen);
            if (hoveredSlot != null && hoveredSlot.container instanceof Inventory) {
                Holder.Reference<SoundEvent> soundEvent;
                if (LockedInSlots.CONFIG.get(ClientConfig.class).toggleSlotLock(hoveredSlot.getContainerSlot())) {
                    soundEvent = SoundEvents.UI_BUTTON_CLICK;
                } else {
                    soundEvent = SoundEvents.UI_BUTTON_CLICK;
                }
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1.0F));
                return EventResult.INTERRUPT;
            } else {
                return EventResult.PASS;
            }
        } else {
            for (int i = 0; i < Inventory.getSelectionSize(); ++i) {
                if (minecraft.options.keyHotbarSlots[i].matches(key, scanCode)) {
                    if (!minecraft.player.getInventory().getItem(i).isEmpty()) {
                        return EventResult.INTERRUPT;
                    }
                }
            }
            return isHoveringLockedSlot(screen) ? EventResult.INTERRUPT : EventResult.PASS;
        }
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
                LockedInSlots.CONFIG.get(ClientConfig.class).isSlotLocked(slot.getContainerSlot())) {
            return Optional.of(Pair.of(InventoryMenu.BLOCK_ATLAS, LOCKED_SLOT_LOCATION));
        } else {
            return Optional.empty();
        }
    }

    public static void onAfterRender(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringLockedSlot(screen, true)) {
            Font font = ScreenHelper.INSTANCE.getFont(screen);
            guiGraphics.renderTooltip(font, Component.translatable(KEY_SLOT_UNLOCK, Component.keybind(KEY_NAME).withStyle(
                    ChatFormatting.LIGHT_PURPLE)), mouseX, mouseY);
        }
    }
}
