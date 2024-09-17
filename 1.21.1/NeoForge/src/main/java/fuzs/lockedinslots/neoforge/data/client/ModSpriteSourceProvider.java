package fuzs.lockedinslots.neoforge.data.client;

import fuzs.lockedinslots.client.handler.NoSlotInteractionHandler;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.neoforge.api.data.v2.client.AbstractSpriteSourceProvider;
import fuzs.puzzleslib.neoforge.api.data.v2.core.NeoForgeDataProviderContext;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;

import java.util.Optional;

public class ModSpriteSourceProvider extends AbstractSpriteSourceProvider {

    public ModSpriteSourceProvider(NeoForgeDataProviderContext context) {
        super(context);
    }

    @Override
    public void addSpriteSources() {
        this.atlas(ResourceLocationHelper.withDefaultNamespace("gui"))
                .addSource(new SingleFile(NoSlotInteractionHandler.LOCKED_SLOT_LOCATION, Optional.empty()));
    }
}
