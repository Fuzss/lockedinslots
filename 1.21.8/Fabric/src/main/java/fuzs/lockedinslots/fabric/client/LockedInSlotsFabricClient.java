package fuzs.lockedinslots.fabric.client;

import fuzs.lockedinslots.LockedInSlots;
import fuzs.lockedinslots.client.LockedInSlotsClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;

public class LockedInSlotsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(LockedInSlots.MOD_ID, LockedInSlotsClient::new);
    }
}
