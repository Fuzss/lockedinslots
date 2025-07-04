package fuzs.lockedinslots.neoforge;

import fuzs.lockedinslots.LockedInSlots;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.neoforged.fml.common.Mod;

@Mod(LockedInSlots.MOD_ID)
public class LockedInSlotsNeoForge {

    public LockedInSlotsNeoForge() {
        ModConstructor.construct(LockedInSlots.MOD_ID, LockedInSlots::new);
    }
}
