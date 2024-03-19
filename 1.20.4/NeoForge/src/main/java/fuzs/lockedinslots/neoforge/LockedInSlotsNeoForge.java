package fuzs.lockedinslots.neoforge;

import fuzs.lockedinslots.LockedInSlots;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod(LockedInSlots.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LockedInSlotsNeoForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(LockedInSlots.MOD_ID, LockedInSlots::new);
    }
}
