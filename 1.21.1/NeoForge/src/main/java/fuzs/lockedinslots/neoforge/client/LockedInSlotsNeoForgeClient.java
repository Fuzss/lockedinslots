package fuzs.lockedinslots.neoforge.client;

import fuzs.lockedinslots.LockedInSlots;
import fuzs.lockedinslots.client.LockedInSlotsClient;
import fuzs.lockedinslots.data.client.ModLanguageProvider;
import fuzs.lockedinslots.neoforge.data.client.ModSpriteSourceProvider;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = LockedInSlots.MOD_ID, dist = Dist.CLIENT)
public class LockedInSlotsNeoForgeClient {

    public LockedInSlotsNeoForgeClient() {
        ClientModConstructor.construct(LockedInSlots.MOD_ID, LockedInSlotsClient::new);
        DataProviderHelper.registerDataProviders(LockedInSlots.MOD_ID, ModLanguageProvider::new, ModSpriteSourceProvider::new);
    }
}
