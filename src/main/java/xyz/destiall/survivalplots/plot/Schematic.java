package xyz.destiall.survivalplots.plot;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.hooks.WorldEditHook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPOutputStream;

public class Schematic {
    private Clipboard clipboard;
    private CompletableFuture<Clipboard> board;
    private File file;

    public Schematic(SurvivalPlot plot, final Clipboard clip) {
        this.clipboard = clip;

        for (int x = plot.getMin().getBlockX(); x <= plot.getMax().getX(); x++) {
            for (int y = plot.getMin().getBlockY(); y <= plot.getMax().getY(); y++) {
                for (int z = plot.getMin().getBlockZ(); z <= plot.getMax().getZ(); z++) {
                    BlockVector3 block = BlockVector3.at(x, y, z);
                    try {
                        clipboard.setBlock(block, BukkitAdapter.adapt(plot.getWorld()).getFullBlock(block));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Schematic(File file) {
        if (!file.exists())
            return;

        this.file = file;

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format != null) {
            if (SurvivalPlotsPlugin.getInst().getConfig().getBoolean("async-file-operations")) {
                board = new CompletableFuture<>();
                SurvivalPlotsPlugin.getInst().getScheduler().runTaskAsync(() -> {
                    try (ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()))) {
                        clipboard = reader.read();
                        board.complete(clipboard);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }
            try (ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()))) {
                clipboard = reader.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void save(CompoundTag tag, File file) {
        this.file = file;
        if (SurvivalPlotsPlugin.getInst().getConfig().getBoolean("async-file-operations")) {
            SurvivalPlotsPlugin.getInst().getScheduler().runTaskAsync(() -> {
                try (NBTOutputStream nbtStream = new NBTOutputStream(new GZIPOutputStream(Files.newOutputStream(file.toPath())))) {
                    nbtStream.writeNamedTag("Schematic", tag);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return;
        }
        try (NBTOutputStream nbtStream = new NBTOutputStream(new GZIPOutputStream(Files.newOutputStream(file.toPath())))) {
            nbtStream.writeNamedTag("Schematic", tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeSchematicData(
            final @NonNull Map<String, Tag> schematic,
            final @NonNull Map<String, Integer> palette,
            final @NonNull Map<String, Integer> biomePalette,
            final @NonNull List<CompoundTag> tileEntities,
            final @NonNull ByteArrayOutputStream buffer,
            final @NonNull ByteArrayOutputStream biomeBuffer
    ) {
        schematic.put("PaletteMax", new IntTag(palette.size()));

        Map<String, Tag> paletteTag = new HashMap<>();
        palette.forEach((key, value) -> paletteTag.put(key, new IntTag(value)));

        schematic.put("Palette", new CompoundTag(paletteTag));
        schematic.put("BlockData", new ByteArrayTag(buffer.toByteArray()));
        schematic.put("BlockEntities", new ListTag(CompoundTag.class, tileEntities));

        if (biomeBuffer.size() == 0 || biomePalette.size() == 0) {
            return;
        }

        schematic.put("BiomePaletteMax", new IntTag(biomePalette.size()));

        Map<String, Tag> biomePaletteTag = new HashMap<>();
        biomePalette.forEach((key, value) -> biomePaletteTag.put(key, new IntTag(value)));

        schematic.put("BiomePalette", new CompoundTag(biomePaletteTag));
        schematic.put("BiomeData", new ByteArrayTag(biomeBuffer.toByteArray()));
    }

    private Map<String, Tag> initSchematic(short width, short height, short length) {
        Map<String, Tag> schematic = new HashMap<>();
        schematic.put("Version", new IntTag(2));
        schematic.put(
                "DataVersion",
                new IntTag(WorldEdit
                        .getInstance()
                        .getPlatformManager()
                        .queryCapability(Capability.WORLD_EDITING)
                        .getDataVersion())
        );

        Map<String, Tag> metadata = new HashMap<>();
        metadata.put("WEOffsetX", new IntTag(0));
        metadata.put("WEOffsetY", new IntTag(0));
        metadata.put("WEOffsetZ", new IntTag(0));

        schematic.put("Metadata", new CompoundTag(metadata));

        schematic.put("Width", new ShortTag(width));
        schematic.put("Height", new ShortTag(height));
        schematic.put("Length", new ShortTag(length));

        // The Sponge format Offset refers to the 'min' points location in the world. That's our 'Origin'
        schematic.put("Offset", new IntArrayTag(new int[]{0, 0, 0,}));
        return schematic;
    }

    public @NonNull CompletableFuture<CompoundTag> getCompoundTag(final SurvivalPlot plot) {
        CompletableFuture<CompoundTag> completableFuture = new CompletableFuture<>();
        SurvivalPlotsPlugin.getInst().getScheduler().runTaskAsync(() -> {
            // All positions
            CuboidRegion aabb = WorldEditHook.getRegion(plot);

            final int width = aabb.getWidth();
            int height = aabb.getHeight();
            final int length = aabb.getLength();
            Map<String, Tag> schematic = initSchematic((short) width, (short) height, (short) length);

            Map<String, Integer> palette = new HashMap<>();
            Map<String, Integer> biomePalette = new HashMap<>();

            List<CompoundTag> tileEntities = new ArrayList<>();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * height * length);
            ByteArrayOutputStream biomeBuffer = new ByteArrayOutputStream(width * length);
            // Queue
            SurvivalPlotsPlugin.getInst().getScheduler().runTaskAsync(() -> {
                final BlockVector3 minimum = aabb.getMinimumPoint();
                final BlockVector3 maximum = aabb.getMaximumPoint();

                final int minX = minimum.getX();
                final int minZ = minimum.getZ();
                final int minY = minimum.getY();

                final int maxX = maximum.getX();
                final int maxZ = maximum.getZ();
                final int maxY = maximum.getY();

                new Runnable() {
                    int currentY = minY;
                    int currentX = minX;
                    int currentZ = minZ;

                    @Override
                    public void run() {
                        long start = System.currentTimeMillis();
                        for (; currentY <= maxY; currentY++) {
                            int relativeY = currentY - minY;
                            for (; currentZ <= maxZ; currentZ++) {
                                int relativeZ = currentZ - minZ;
                                for (; currentX <= maxX; currentX++) {
                                    // if too much time was spent here, we yield this task
                                    // note that current(X/Y/Z) aren't incremented, so the same position
                                    // as *right now* will be visited again
                                    if (System.currentTimeMillis() - start > 40) {
                                        SurvivalPlotsPlugin.getInst().getScheduler().runTaskLater(this, 1L);
                                        return;
                                    }
                                    int relativeX = currentX - minX;
                                    BlockVector3 point = BlockVector3.at(currentX, currentY, currentZ);
                                    BaseBlock block = aabb.getWorld().getFullBlock(point);
                                    BlockState state = block.toImmutableState();
                                    if (block.getNbtData() != null) {
                                        Map<String, Tag> values = new HashMap<>(block.getNbtData().getValue());

                                        // Positions are kept in NBT, we don't want that.
                                        values.remove("x");
                                        values.remove("y");
                                        values.remove("z");

                                        values.put("Id", new StringTag(block.getNbtId()));

                                        // Remove 'id' if it exists. We want 'Id'.
                                        // Do this after we get "getNbtId" cos otherwise "getNbtId" doesn't work.
                                        // Dum.
                                        values.remove("id");
                                        values.put("Pos", new IntArrayTag(new int[]{relativeX, relativeY, relativeZ}));

                                        tileEntities.add(new CompoundTag(values));
                                    }
                                    String blockKey = state.getAsString();
                                    int blockId;
                                    if (palette.containsKey(blockKey)) {
                                        blockId = palette.get(blockKey);
                                    } else {
                                        blockId = palette.size();
                                        palette.put(blockKey, palette.size());
                                    }

                                    while ((blockId & -128) != 0) {
                                        buffer.write(blockId & 127 | 128);
                                        blockId >>>= 7;
                                    }
                                    buffer.write(blockId);

                                    if (relativeY > 0) {
                                        continue;
                                    }
                                    BlockVector2 pt = BlockVector2.at(currentX, currentZ);
                                    BiomeType biome = aabb.getWorld().getBiome(pt);
                                    String biomeStr = biome.getId();
                                    int biomeId;
                                    if (biomePalette.containsKey(biomeStr)) {
                                        biomeId = biomePalette.get(biomeStr);
                                    } else {
                                        biomeId = biomePalette.size();
                                        biomePalette.put(biomeStr, biomeId);
                                    }
                                    while ((biomeId & -128) != 0) {
                                        biomeBuffer.write(biomeId & 127 | 128);
                                        biomeId >>>= 7;
                                    }
                                    biomeBuffer.write(biomeId);
                                }
                                currentX = minX; // reset manually as not using local variable
                            }
                            currentZ = minZ; // reset manually as not using local variable
                        }
                        SurvivalPlotsPlugin.getInst().getScheduler().runTaskAsync(() -> {
                            writeSchematicData(schematic, palette, biomePalette, tileEntities, buffer, biomeBuffer);
                            completableFuture.complete(new CompoundTag(schematic));
                        }, plot.getCenter());
                    }
                }.run();
            }, plot.getCenter());
        }, plot.getCenter());
        return completableFuture;
    }

    public Clipboard getClipboard() {
        return this.clipboard;
    }

    public CompletableFuture<Clipboard> getAsyncClipboard() {
        if (board == null)
            return CompletableFuture.completedFuture(clipboard);

        return board;
    }

    public File getFile() {
        return file;
    }
}
