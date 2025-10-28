package fuzs.lockedinslots.config;

import fuzs.lockedinslots.client.handler.SlotOverlayHandler;
import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ClientConfig implements ConfigCore {
    @Config(description = "A slot can only be locked when it is not empty. When empty slots are locked, an item can still be put in.")
    public boolean slotMustNotBeEmpty = false;
    @Config(description = "A slot can only be locked when the contained item cannot be stacked like tools and armor.")
    public boolean itemMustNotBeStackable = false;
    @Config(description = "Support locking any inventory slot, including armor and offhand.")
    public boolean allowLockingAllSlots = true;
    @Config(description = "Maximum time in ticks it takes to switch the lock on a slot.")
    @Config.IntRange(min = 1, max = SlotOverlayHandler.MAX_TRIGGER_TIME)
    public int triggerLockTicks = 12;
    @Config(description = "Transparency value for drawing the locked slot overlay on the gui hotbar.")
    @Config.DoubleRange(min = 0.0, max = 1.0)
    public double guiHotbarOverlayAlpha = 0.6;
    @Config(description = "Show a tooltip hint on how to unlock a locked slot.")
    public boolean unlockSlotHint = true;
    @Config(
            description = {
                    "Disallow collecting items of a type (via double-clicking) and moving all items of a type (via shift+double-clicking) when any items that would be moved are in locked slots.",
                    "This is a workaround for the server picking items from locked slots when using these mechanics, but unfortunately partially breaks the vanilla behavior."
            }
    )
    public boolean preventDoubleClickActionsForLockedItems = false;
}
