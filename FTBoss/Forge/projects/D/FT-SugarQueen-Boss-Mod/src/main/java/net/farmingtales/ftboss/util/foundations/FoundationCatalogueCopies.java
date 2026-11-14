package net.farmingtales.ftboss.util.foundations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

/** 静态食材目录的独立副本；2026-10-20 使用原版 NBT 编解码，避免逐节点 copy 的所有权快照观察开销，不共享可变 Tag。 */
public final class FoundationCatalogueCopies {
    private FoundationCatalogueCopies() {}

    public static CompoundTag copyCatalogue(CompoundTag source) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) { NbtIo.write(source, output); }
            try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
                CompoundTag copied = NbtIo.read(input);
                return source.equals(copied) ? copied : source.copy();
            }
        } catch (IOException exception) {
            return source.copy();
        }
    }
}
