package net.farmingtales.chunkdrops;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(ChunkDropsMod.ID)
public final class ChunkDropsMod {
    public static final String ID = "ftchunkdrops";

    public ChunkDropsMod() {
        MinecraftForge.EVENT_BUS.addListener(ChunkDropCommands::register);
    }
}
