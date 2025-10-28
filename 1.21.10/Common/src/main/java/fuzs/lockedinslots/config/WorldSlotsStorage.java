package fuzs.lockedinslots.config;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import fuzs.lockedinslots.LockedInSlots;
import fuzs.lockedinslots.client.handler.SlotOverlayHandler;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.storage.WorldData;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

public final class WorldSlotsStorage {
    private static final Map<String, IntSet> WORLD_SLOTS = Maps.newIdentityHashMap();
    private static final Splitter WORLD_ENTRY_SPLITTER = Splitter.on(':').limit(2);
    private static final Splitter SLOTS_SPLITTER = Splitter.on(',');
    public static final Path DATA_PATH = ModLoaderEnvironment.INSTANCE.getConfigDirectory()
            .resolve("." + LockedInSlots.MOD_ID);

    static {
        load();
    }

    private WorldSlotsStorage() {
        // NO-OP
    }

    public static IntSet getLockedSlots() {
        // don't allow any changes to the collection without us knowing
        return IntSets.unmodifiable(WORLD_SLOTS.getOrDefault(getCurrentWorldKey(), IntSets.emptySet()));
    }

    public static boolean isSlotLocked(Slot slot) {
        return isSlotLocked(SlotOverlayHandler.unwrapSlot(slot).getContainerSlot());
    }

    public static boolean isSlotLocked(int slot) {
        return getLockedSlots().contains(slot);
    }

    public static void triggerSlotLock(int slot) {
        IntSet lockedSlots = WORLD_SLOTS.computeIfAbsent(getCurrentWorldKey(), $ -> new IntAVLTreeSet());
        if (isSlotLocked(slot)) {
            lockedSlots.remove(slot);
        } else {
            lockedSlots.add(slot);
        }
        save();
    }

    private static String getCurrentWorldKey() {
        // similar to Minecraft::archiveProfilingReport
        Minecraft minecraft = Minecraft.getInstance();
        String string;
        if (minecraft.isLocalServer()) {
            WorldData worldData = minecraft.getSingleplayerServer().getWorldData();
            // world name allows for duplicates, only the file name will be different which there is no access to in-game, so add the world seed instead
            string = worldData.getLevelName() + "-" + worldData.worldGenOptions().seed();
        } else {
            ServerData serverData = minecraft.getCurrentServer();
            string = serverData != null ? serverData.name + "-" + serverData.ip : "world";
        }

        return string.intern();
    }

    private static void save() {
        try {
            try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(WorldSlotsStorage.DATA_PATH.toFile()),
                    StandardCharsets.UTF_8))) {
                for (Map.Entry<String, IntSet> entry : WORLD_SLOTS.entrySet()) {
                    if (!entry.getValue().isEmpty()) {
                        printWriter.print(entry.getKey());
                        printWriter.print(':');
                        StringJoiner joiner = new StringJoiner(",");
                        entry.getValue().forEach(integer -> joiner.add(String.valueOf(integer)));
                        printWriter.println(joiner);
                    }
                }
            }
        } catch (Exception exception) {
            LockedInSlots.LOGGER.error("Failed to save file at " + WorldSlotsStorage.DATA_PATH, exception);
        }
    }

    private static void load() {
        WORLD_SLOTS.clear();
        try {
            if (WorldSlotsStorage.DATA_PATH.toFile().exists()) {
                try (BufferedReader bufferedReader = Files.newReader(WorldSlotsStorage.DATA_PATH.toFile(),
                        Charsets.UTF_8)) {
                    bufferedReader.lines().forEach((string) -> {
                        try {
                            Iterator<String> iterator = WORLD_ENTRY_SPLITTER.split(string).iterator();
                            String worldName = iterator.next();
                            String value = iterator.next();
                            IntSet integers = new IntAVLTreeSet();
                            for (String s : SLOTS_SPLITTER.split(value)) {
                                try {
                                    integers.add(Integer.parseInt(s));
                                } catch (NumberFormatException exception) {
                                    LockedInSlots.LOGGER.warn("Invalid integer value for entry {} = {}",
                                            worldName,
                                            s,
                                            exception);
                                }
                            }
                            WORLD_SLOTS.put(worldName.intern(), integers);
                        } catch (Exception var3) {
                            LockedInSlots.LOGGER.warn("Skipping bad entry: {}", string);
                        }

                    });
                }
            }
        } catch (Throwable throwable) {
            LockedInSlots.LOGGER.error("Failed to load file at " + WorldSlotsStorage.DATA_PATH, throwable);
        }
    }
}
