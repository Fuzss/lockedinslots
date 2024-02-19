package fuzs.lockedinslots.config;

import com.google.common.collect.Lists;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import fuzs.puzzleslib.api.config.v3.ValueCallback;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.List;

public class ClientConfig implements ConfigCore {
    ModConfigSpec.ConfigValue<List<? extends Integer>> lockedSlots;

    @Override
    public void addToBuilder(ModConfigSpec.Builder builder, ValueCallback callback) {
        this.lockedSlots = builder.comment("Slot ids for locked slots, should not be edited manually.")
                .<Integer>defineList("locked_slots", Lists.<Integer>newArrayList(), o -> true);
    }

    public List<? extends Integer> getLockedSlots() {
        return Collections.unmodifiableList(this.lockedSlots.get());
    }

    public void setLockedSlots(List<? extends Integer> lockedSlots) {
        this.lockedSlots.set(Lists.newArrayList(lockedSlots));
    }

    public boolean isSlotLocked(int slot) {
        return this.lockedSlots.get().contains(slot);
    }

    public boolean toggleSlotLock(int slot) {
        List<Integer> list = (List<Integer>) this.lockedSlots.get();
        if (this.isSlotLocked(slot)) {
            list.remove((Integer) slot);
            this.lockedSlots.set(list);
            return false;
        } else {
            list.add((Integer) slot);
            this.lockedSlots.set(list);
            return true;
        }
    }
}
