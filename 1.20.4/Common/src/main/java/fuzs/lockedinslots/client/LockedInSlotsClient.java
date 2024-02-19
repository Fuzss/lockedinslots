package fuzs.lockedinslots.client;

import fuzs.lockedinslots.client.handler.NoSlotInteractionHandler;
import fuzs.lockedinslots.client.handler.TriggerLockRenderHandler;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenKeyboardEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenMouseEvents;
import fuzs.puzzleslib.api.client.key.v1.KeyActivationContext;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class LockedInSlotsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerHandlers();
    }

    private static void registerHandlers() {
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class).register(NoSlotInteractionHandler::onBeforeKeyPress);
        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class).register(NoSlotInteractionHandler::onBeforeMouseClick);
        ScreenMouseEvents.beforeMouseScroll(AbstractContainerScreen.class).register(NoSlotInteractionHandler::onBeforeMouseScroll);
        ScreenMouseEvents.beforeMouseDrag(AbstractContainerScreen.class).register(NoSlotInteractionHandler::onBeforeMouseDrag);
        ScreenMouseEvents.beforeMouseRelease(AbstractContainerScreen.class).register(NoSlotInteractionHandler::onBeforeMouseRelease);
        ScreenEvents.afterRender(AbstractContainerScreen.class).register(TriggerLockRenderHandler::onAfterRender);
        ClientTickEvents.START.register(NoSlotInteractionHandler::onStartClientTick);
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(NoSlotInteractionHandler.LOCK_SLOT_KEY_MAPPING, KeyActivationContext.SCREEN);
    }
}
