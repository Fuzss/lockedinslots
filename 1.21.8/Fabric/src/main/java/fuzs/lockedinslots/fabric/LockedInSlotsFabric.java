package fuzs.lockedinslots.fabric;

import fuzs.lockedinslots.LockedInSlots;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;

public class LockedInSlotsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(LockedInSlots.MOD_ID, LockedInSlots::new);
    }
}
