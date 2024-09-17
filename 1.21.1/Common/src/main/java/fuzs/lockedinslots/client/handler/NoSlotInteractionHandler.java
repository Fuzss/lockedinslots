package fuzs.lockedinslots.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import fuzs.lockedinslots.LockedInSlots;
import fuzs.lockedinslots.config.ClientConfig;
import fuzs.lockedinslots.config.WorldSlotsStorage;
import fuzs.puzzleslib.api.client.gui.v2.screen.ScreenHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class NoSlotInteractionHandler {
    public static final String KEY_CATEGORY = "key.categories." + LockedInSlots.MOD_ID;
    private static final String KEY_NAME = "key.lockSlot";
    public static final KeyMapping LOCK_SLOT_KEY_MAPPING = new KeyMapping(KEY_NAME, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final ResourceLocation LOCKED_SLOT_LOCATION = LockedInSlots.id("item/locked_slot");
    public static final String KEY_SLOT_UNLOCK = "screen.slot.unlock";

    private static boolean isHoveringLockedSlot(AbstractContainerScreen<?> screen) {
        return isHoveringLockedSlot(screen, false, false);
    }

    public static boolean isHoveringLockedSlot(AbstractContainerScreen<?> screen, boolean ignoreCarriedStack, boolean mustBeEmpty) {
        Slot hoveredSlot = ScreenHelper.INSTANCE.getHoveredSlot(screen);
        if (hoveredSlot != null && hoveredSlot.container instanceof Inventory) {
            if (mustBeEmpty != hoveredSlot.hasItem() && WorldSlotsStorage.isSlotLocked(hoveredSlot)) {
                if (mustBeEmpty || ignoreCarriedStack || Screen.hasShiftDown() || Screen.hasAltDown() || Screen.hasControlDown()) {
                    return true;
                } else {
                    ItemStack carriedStack = screen.getMenu().getCarried();
                    return carriedStack.isEmpty() || !ItemStack.isSameItemSameTags(hoveredSlot.getItem(), carriedStack);
                }
            }
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
        if (LockedInSlots.CONFIG.get(ClientConfig.class).preventDoubleClickActionsForLockedItems) {
            Slot hoveredSlot = screen.findSlot(mouseX, mouseY);
            if (hoveredSlot != null && screen.doubleclick && button == InputConstants.MOUSE_BUTTON_LEFT && screen.getMenu().canTakeItemForPickAll(ItemStack.EMPTY, hoveredSlot)) {
                Minecraft minecraft = ScreenHelper.INSTANCE.getMinecraft(screen);
                ItemStack itemStack = screen.getMenu().getCarried();
                for (int slot : WorldSlotsStorage.getLockedSlots()) {
                    if (ItemStack.isSameItemSameTags(minecraft.player.getInventory().getItem(slot), itemStack)) {
                        screen.doubleclick = false;
                        screen.lastClickTime = 0L;
                        return EventResult.INTERRUPT;
                    }
                }
            }
        }

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
                    if (!minecraft.player.getInventory().getItem(i).isEmpty() && WorldSlotsStorage.isSlotLocked(i)) {
                        return EventResult.INTERRUPT;
                    }
                }
            }
            if (isHoveringLockedSlot(screen, true, false)) {
                // don't block all keys, so closing via esc or inventory key still works when hovering a locked slot
                if (minecraft.options.keySwapOffhand.matches(key, scanCode) || minecraft.options.keyDrop.matches(key,
                        scanCode
                )) {
                    return EventResult.INTERRUPT;
                }
            }
        }

        return EventResult.PASS;
    }

    public static void onAfterRender(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!LockedInSlots.CONFIG.get(ClientConfig.class).unlockSlotHint) return;
        if (isHoveringLockedSlot(screen, true, true)) {
            Font font = ScreenHelper.INSTANCE.getFont(screen);
            guiGraphics.renderTooltip(font, getKeybindTooltipComponent(), mouseX, mouseY);
        }
    }

    private static Component getKeybindTooltipComponent() {
        return Component.translatable(KEY_SLOT_UNLOCK, Component.keybind(KEY_NAME)).withStyle(ChatFormatting.RED);
    }

    public static void onItemTooltip(ItemStack stack, @Nullable Player player, List<Component> lines, TooltipFlag context) {
        if (!LockedInSlots.CONFIG.get(ClientConfig.class).unlockSlotHint) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            Slot hoveredSlot = ScreenHelper.INSTANCE.getHoveredSlot(abstractContainerScreen);
            if (hoveredSlot != null && hoveredSlot.container instanceof Inventory && hoveredSlot.getItem() == stack) {
                if (abstractContainerScreen.getMenu().getCarried().isEmpty() && WorldSlotsStorage.isSlotLocked(hoveredSlot)) {
                    lines.add(!lines.isEmpty() ? 1 : 0, NoSlotInteractionHandler.getKeybindTooltipComponent());
                }
            }
        }
    }

    public static void onStartClientTick(Minecraft minecraft) {
        if (minecraft.player != null) {
            int slot = minecraft.player.getInventory().selected;
            if (WorldSlotsStorage.isSlotLocked(slot)) {
                while (minecraft.options.keyDrop.consumeClick()) {
                    // NO-OP
                }
            }
        }
    }

    public static Optional<Pair<ResourceLocation, ResourceLocation>> getNoItemIcon(Slot slot) {
        if (slot.container instanceof Inventory && WorldSlotsStorage.isSlotLocked(slot)) {
            return Optional.of(Pair.of(InventoryMenu.BLOCK_ATLAS, LOCKED_SLOT_LOCATION));
        } else {
            return Optional.empty();
        }
    }
}
