package net.farmingtales.teamlag;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.block.entity.BlockEntity;


public final class ReflectionAccess {
    private static final Field MISSING;
    static {
        Field missing = null;
        try { missing = ReflectionAccess.class.getDeclaredField("MISSING"); }
        catch (NoSuchFieldException exception) { throw new ExceptionInInitializerError(exception); }
        MISSING = missing;
    }
    private static final ClassValue<Object> BLOCK_ENTITY = new ClassValue<>() {
        @Override protected Object computeValue(Class<?> type) {
            for (Class<?> current = type; current != null; current = current.getSuperclass()) {
                for (Field field : current.getDeclaredFields()) {
                    if (BlockEntity.class.isAssignableFrom(field.getType())) { field.setAccessible(true); return field; }
                }
            }
            return MISSING;
        }
    };
    private static final ClassValue<Map<String, Field>> FIELDS = new ClassValue<>() {
        @Override protected Map<String, Field> computeValue(Class<?> type) { return new ConcurrentHashMap<>(); }
    };
    private ReflectionAccess() {}
    public static BlockEntity blockEntity(Object holder) {
        if (holder == null) return null;
        if (holder instanceof BlockEntity entity) return entity;
        Object resolved = BLOCK_ENTITY.get(holder.getClass());
        if (resolved == MISSING) return null;
        try { return (BlockEntity) ((Field) resolved).get(holder); }
        catch (IllegalAccessException | RuntimeException exception) {
            LagQuery.warnOnce(holder.getClass(), "找不到方块实体字段，本次计时已跳过", exception);
            return null;
        }
    }
    public static Object field(Object holder, String name) {
        if (holder == null) return null;
        Field field = resolve(holder.getClass(), name);
        if (field == null) return null;
        try { return field.get(holder); }
        catch (IllegalAccessException | RuntimeException exception) {
            LagQuery.warnOnce(holder.getClass(), "无法读取字段 " + name, exception);
            return null;
        }
    }
    private static Field resolve(Class<?> type, String name) {
        Map<String, Field> byName = FIELDS.get(type);
        Field cached = byName.get(name);
        if (cached != null) return cached == MISSING ? null : cached;
        Field found = MISSING;
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.getName().equals(name)) { field.setAccessible(true); found = field; break; }
            }
            if (found != MISSING) break;
        }
        byName.put(name, found);
        return found == MISSING ? null : found;
    }
}
