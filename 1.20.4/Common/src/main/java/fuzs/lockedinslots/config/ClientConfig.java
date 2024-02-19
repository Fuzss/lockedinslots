package fuzs.lockedinslots.config;

import com.google.common.collect.Lists;
import fuzs.lockedinslots.client.handler.TriggerLockRenderHandler;
import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import fuzs.puzzleslib.api.config.v3.ValueCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.ServerInfo;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.List;

public class ClientConfig implements ConfigCore {
    ModConfigSpec.ConfigValue<List<? extends String>> lockedSlots;
    @Config(description = "Maximum time in ticks it takes to switch the lock on a slot.")
    @Config.IntRange(min = 1, max = TriggerLockRenderHandler.MAX_TRIGGER_TIME)
    public int triggerLockTicks = 12;
    @Config(description = "Transparency value for drawing the locked slot overlay on the gui hotbar.")
    @Config.DoubleRange(min = 0.0, max = 1.0)
    public double guiHotbarOverlayAlpha = 0.6;

    @Override
    public void addToBuilder(ModConfigSpec.Builder builder, ValueCallback callback) {
        this.lockedSlots = builder.comment("Slot ids for locked slots per world / server. Only for storage, must not be edited manually.")
                .defineList("locked_slots", Lists.newArrayList(), o -> true);
    }

    @Override
    public void afterConfigReload() {
        ConfigCore.super.afterConfigReload();
    }

    public List<? extends Integer> getLockedSlots() {
        // don't allow any changes to the list without us knowing
        return Collections.unmodifiableList(this.lockedSlots.get());
    }

    public boolean isSlotLocked(int slot) {
        return this.lockedSlots.get().contains(slot);
    }

    public void triggerSlotLock(int slot) {
        List<Integer> list = (List<Integer>) this.lockedSlots.get();
        if (this.isSlotLocked(slot)) {
            list.remove((Integer) slot);
        } else {
            list.add(slot);
        }

        // set this to force the config to save
        this.lockedSlots.set(list);
    }

    public static String getLevelKey(Minecraft minecraft) {
        if(minecraft.isLocalServer()) {
            IntegratedServer server = minecraft.getSingleplayerServer();
            if(server != null) {
                key = ((ServerWorldAccessor) server.overworld()).getWorldProperties().getLevelName();
            }
        }else{
            ServerData data = minecraft.getCurrentServer();
            if(data != null) {
                return data.ip;
            }
        }

        return "world";
    }
}
