package fuzs.lockedinslots.neoforge.data.client;

import fuzs.lockedinslots.client.handler.NoSlotInteractionHandler;
import fuzs.puzzleslib.neoforge.api.data.v2.client.AbstractSpriteSourceProvider;
import fuzs.puzzleslib.neoforge.api.data.v2.core.ForgeDataProviderContext;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class ModSpriteSourceProvider extends AbstractSpriteSourceProvider {

    public ModSpriteSourceProvider(ForgeDataProviderContext context) {
        super(context);
    }

    @Override
    public void addSpriteSources() {
        this.atlas(new ResourceLocation("gui")).addSource(new SingleFile(NoSlotInteractionHandler.LOCKED_SLOT_LOCATION, Optional.empty()));
    }
}
