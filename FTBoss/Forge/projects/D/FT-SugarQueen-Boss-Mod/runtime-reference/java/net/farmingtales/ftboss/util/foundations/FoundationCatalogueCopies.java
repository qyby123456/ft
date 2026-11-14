/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtIo
 */
package net.farmingtales.ftboss.util.foundations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

public final class FoundationCatalogueCopies {
    private FoundationCatalogueCopies() {
    }

    public static CompoundTag copyCatalogue(CompoundTag compoundTag) {
        CompoundTag compoundTag2;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (Closeable closeable = new DataOutputStream(byteArrayOutputStream);){
            NbtIo.m_128941_((CompoundTag)compoundTag, (DataOutput)((Object)closeable));
        }
        closeable = new DataInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        try {
            CompoundTag compoundTag3 = NbtIo.m_128928_((DataInput)((Object)closeable));
            compoundTag2 = compoundTag.equals((Object)compoundTag3) ? compoundTag3 : compoundTag.m_6426_();
        }
        catch (Throwable throwable) {
            try {
                try {
                    ((FilterInputStream)closeable).close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException iOException) {
                return compoundTag.m_6426_();
            }
        }
        ((FilterInputStream)closeable).close();
        return compoundTag2;
    }
}

