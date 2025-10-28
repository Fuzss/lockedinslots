package fuzs.lockedinslots.client;

import fuzs.lockedinslots.LockedInSlots;
import fuzs.lockedinslots.client.handler.NoSlotInteractionHandler;
import fuzs.lockedinslots.client.handler.SlotOverlayHandler;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.GuiLayersContext;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.*;
import fuzs.puzzleslib.api.client.key.v1.KeyActivationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class LockedInSlotsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(NoSlotInteractionHandler::onBeforeKeyPress);
        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class)
                .register(NoSlotInteractionHandler::onBeforeMouseClick);
        ScreenMouseEvents.beforeMouseScroll(AbstractContainerScreen.class)
                .register(NoSlotInteractionHandler::onBeforeMouseScroll);
        ScreenMouseEvents.beforeMouseDrag(AbstractContainerScreen.class)
                .register(NoSlotInteractionHandler::onBeforeMouseDrag);
        ScreenMouseEvents.beforeMouseRelease(AbstractContainerScreen.class)
                .register(NoSlotInteractionHandler::onBeforeMouseRelease);
        ScreenEvents.afterRender(AbstractContainerScreen.class).register(NoSlotInteractionHandler::onAfterRender);
        ItemTooltipCallback.EVENT.register(NoSlotInteractionHandler::onItemTooltip);
        ClientTickEvents.START.register(NoSlotInteractionHandler::onStartClientTick);
        ScreenEvents.afterRender(AbstractContainerScreen.class).register(SlotOverlayHandler::onAfterRender);
        RenderTooltipCallback.EVENT.register(SlotOverlayHandler::onRenderTooltip);
        ClientTickEvents.END.register(SlotOverlayHandler::onEndClientTick);
    }

    @Override
    public void onRegisterGuiLayers(GuiLayersContext context) {
        context.registerGuiLayer(GuiLayersContext.HOTBAR,
                LockedInSlots.id("slot_overlay"),
                SlotOverlayHandler::renderGuiLayer);
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(NoSlotInteractionHandler.LOCK_SLOT_KEY_MAPPING,
                KeyActivationHandler.of()
                        .withGameHandler((Minecraft minecraft) -> {
                            // NO-OP
                        })
                        .withScreenHandler((Class<AbstractContainerScreen<?>>) (Class<?>) AbstractContainerScreen.class,
                                (AbstractContainerScreen<?> screen) -> {
                                    // NO-OP
                                }));
    }
}
