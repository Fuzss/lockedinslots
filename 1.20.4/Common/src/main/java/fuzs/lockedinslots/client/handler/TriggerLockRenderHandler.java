package fuzs.lockedinslots.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fuzs.lockedinslots.LockedInSlots;
import fuzs.lockedinslots.config.ClientConfig;
import fuzs.puzzleslib.api.client.gui.v2.screen.ScreenHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.opengl.GL11;

public class TriggerLockRenderHandler {
    public static final ResourceLocation LOCKED_SPRITE_LOCATION = LockedInSlots.id("widget/locked");
    public static final ResourceLocation UNLOCKED_SPRITE_LOCATION = LockedInSlots.id("widget/unlocked");
    public static final int MAX_TRIGGER_TIME = 72_000;
    public static final int CIRCLE_RADIUS = 12;
    private static final float COLOR_RED = 0.0F;
    private static final float COLOR_GREEN = 0.875F;
    private static final float COLOR_BLUE = 0.325F;

    private static Slot hoveredSlot;
    private static float triggerTime;

    public static void onAfterRender(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (TriggerLockRenderHandler.isKeyDown(NoSlotInteractionHandler.LOCK_SLOT_KEY_MAPPING)) {
            Slot hoveredSlot = ScreenHelper.INSTANCE.getHoveredSlot(screen);
            if (TriggerLockRenderHandler.hoveredSlot != hoveredSlot) {
                // reset trigger time when the hovered slot changes
                triggerTime = 0;
                TriggerLockRenderHandler.hoveredSlot = hoveredSlot;
            }
            if (hoveredSlot != null && hoveredSlot.container instanceof Inventory) {
                Minecraft minecraft = ScreenHelper.INSTANCE.getMinecraft(screen);
                incrementTriggerTime(minecraft, partialTick);
                int offsetX = screen.getMenu().getCarried().isEmpty() && hoveredSlot.hasItem() ? 8 : 0;
                // high z offset to render in front of carried item stack
                renderLockTrigger(minecraft, guiGraphics, mouseX - offsetX, mouseY, partialTick, 600);
            }
        } else {
            triggerTime = 0;
        }
    }

    private static ResourceLocation getSpriteForHoveredSlot() {
        return LockedInSlots.CONFIG.get(ClientConfig.class).isSlotLocked(getContainerSlot(hoveredSlot)) ?
                LOCKED_SPRITE_LOCATION :
                UNLOCKED_SPRITE_LOCATION;
    }

    private static void incrementTriggerTime(Minecraft minecraft, float partialTick) {
        if ((triggerTime += partialTick) >= LockedInSlots.CONFIG.get(ClientConfig.class).triggerLockTicks) {
            // just make sure we only trigger once when the max time is reached, then set to some arbitrary value, so we do not trigger again
            if (triggerTime < MAX_TRIGGER_TIME) {
                LockedInSlots.CONFIG.get(ClientConfig.class).triggerSlotLock(getContainerSlot(hoveredSlot));
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                triggerTime = MAX_TRIGGER_TIME;
            }
        }
    }

    public static int getContainerSlot(Slot slot) {
        int index = slot.getContainerSlot();
        // creative mode inventory tab uses different slot ids :(
        if (slot instanceof CreativeModeInventoryScreen.SlotWrapper) {
            if (index >= 36 && index <= 44) {
                // hotbar slots
                index -= 36;
            } else if (index == 45) {
                // offhand slot
                index = 40;
            } else if (index >= 5 && index <= 8) {
                // armor slots
                index = 44 - index;
            }
        }
        return index;
    }

    public static boolean isKeyDown(KeyMapping keyMapping) {
        // we need to listen to repeat events for the key press, this is not possible using the key mapping instance
        if (keyMapping.key.getType() == InputConstants.Type.KEYSYM && keyMapping.key.getValue() != InputConstants.UNKNOWN.getValue()) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keyMapping.key.getValue());
        } else {
            return false;
        }
    }

    /**
     * Copied from Patchouli's <a href="https://github.com/VazkiiMods/Patchouli/blob/1.20.x/Xplat/src/main/java/vazkii/patchouli/client/handler/TooltipHandler.java">TooltipHandler</a>, thanks!
     */
    private static void renderLockTrigger(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int zOffset) {

        RenderSystem.disableDepthTest();

        guiGraphics.fill(0, 0, 0, 0, 0);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        // just some tick counter for the pulsing effect
        float alpha = 0.5F + 0.2F * ((float) Math.cos(minecraft.player.tickCount + partialTick / 10) * 0.5F + 0.5F);
        bufferBuilder.vertex(mouseX, mouseY, zOffset).color(COLOR_RED / 2.0F, COLOR_GREEN / 2.0F, COLOR_BLUE / 2.0F, alpha).endVertex();

        float angles = Math.min(1.0F,
                triggerTime / LockedInSlots.CONFIG.get(ClientConfig.class).triggerLockTicks
        ) * 360.0F;
        for (float f = angles; f >= 0.0F; f--) {
            double rad = (f - 90.0) / 180.0 * Math.PI;
            bufferBuilder.vertex(mouseX + Math.cos(rad) * (float) CIRCLE_RADIUS,
                    mouseY + Math.sin(rad) * (float) CIRCLE_RADIUS,
                    zOffset
            ).color(COLOR_RED, COLOR_GREEN, COLOR_BLUE, 1.0F).endVertex();
        }

        bufferBuilder.vertex(mouseX, mouseY, zOffset).color(COLOR_RED, COLOR_GREEN, COLOR_BLUE, 0.0F).endVertex();
        Tesselator.getInstance().end();
        RenderSystem.disableBlend();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, zOffset);
        guiGraphics.blitSprite(getSpriteForHoveredSlot(), mouseX - 8, mouseY - 8, 16, 16);
        guiGraphics.pose().popPose();

        RenderSystem.enableDepthTest();
    }
}
