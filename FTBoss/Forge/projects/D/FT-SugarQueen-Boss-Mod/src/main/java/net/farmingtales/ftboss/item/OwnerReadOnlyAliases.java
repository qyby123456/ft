package net.farmingtales.ftboss.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraftforge.common.util.LazyOptional;

/** 排除不携带可变存储的原生只读返回；2026-10-25 修复空能力查询及 UUID 检查误撤销武器来源。 */
public final class OwnerReadOnlyAliases {
    private static final StackWalker CALLER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private OwnerReadOnlyAliases() {}

    public static boolean detached(Object owner, Object value) {
        if (value == LazyOptional.empty()) return true;
        boolean keys = owner instanceof CompoundTag && value instanceof java.util.Set<?>;
        boolean ints = owner instanceof IntArrayTag && value instanceof int[];
        boolean array = ints || owner instanceof ByteArrayTag && value instanceof byte[]
                || owner instanceof LongArrayTag && value instanceof long[];
        if (!keys && !array) return false;
        return CALLER.walk(frames -> frames
                .dropWhile(frame -> frame.getDeclaringClass() == OwnerReadOnlyAliases.class
                        || frame.getDeclaringClass() == OwnerFullRepairGuard.class
                        || frame.getDeclaringClass() == owner.getClass())
                .findFirst().map(frame -> frame.getDeclaringClass() == FoodWeaponSourceLedger.class
                        && frame.getMethodName().equals("appendCanonical")
                        || frame.getDeclaringClass() == FoodWeaponMaterialFingerprint.class
                        && frame.getMethodName().equals("append")
                        || frame.getDeclaringClass() == StringTagVisitor.class
                        && java.util.Set.of("visitCompound", "m_142303_", "visitByteArray", "m_142154_",
                                "visitIntArray", "m_142251_", "visitLongArray", "m_142309_").contains(frame.getMethodName())
                        || ints && frame.getDeclaringClass() == CompoundTag.class
                        && (frame.getMethodName().equals("hasUUID") || frame.getMethodName().equals("m_128403_"))
                        || ints && frame.getDeclaringClass() == NbtUtils.class
                        && (frame.getMethodName().equals("loadUUID") || frame.getMethodName().equals("m_129233_")))
                .orElse(false));
    }
}
