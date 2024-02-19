package fuzs.lockedinslots.neoforge.client;

import fuzs.lockedinslots.LockedInSlots;
import fuzs.lockedinslots.client.LockedInSlotsClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = LockedInSlots.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LockedInSlotsNeoForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientModConstructor.construct(LockedInSlots.MOD_ID, LockedInSlotsClient::new);
    }
}
