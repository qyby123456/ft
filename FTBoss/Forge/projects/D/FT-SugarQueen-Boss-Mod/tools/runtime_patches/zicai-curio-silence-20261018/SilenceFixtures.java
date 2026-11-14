import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SilenceFixtures {
    private static final Map<String, String> SOURCES = new LinkedHashMap<>();

    private static void source(String name, String body) { SOURCES.put(name, body); }

    public static void main(String[] arguments) throws Exception {
        source("net/minecraft/nbt/Tag", "package net.minecraft.nbt; public interface Tag { int TAG_COMPOUND = 10; }");
        source("net/minecraft/nbt/ListTag", """
                package net.minecraft.nbt;
                public class ListTag extends java.util.ArrayList<Tag> implements Tag {
                    @Override public boolean add(Tag tag) { return super.add(tag); }
                    @Override public Tag remove(int index) { return super.remove(index); }
                    public CompoundTag getCompound(int index) { return (CompoundTag) get(index); }
                    public ListTag copy() { ListTag copy = new ListTag(); for (Tag entry : this) copy.add(((CompoundTag) entry).copy()); return copy; }
                }
                """);
        source("net/minecraft/nbt/CompoundTag", """
                package net.minecraft.nbt;
                public class CompoundTag implements Tag {
                    private final java.util.Map<String, Object> data = new java.util.HashMap<>();
                    public CompoundTag getCompound(String key) { return data.get(key) instanceof CompoundTag value ? value : new CompoundTag(); }
                    public ListTag getList(String key, int type) { return data.get(key) instanceof ListTag value ? value : new ListTag(); }
                    public Tag put(String key, Tag value) { Object previous = data.put(key, value); return previous instanceof Tag tag ? tag : null; }
                    public void putString(String key, String value) { data.put(key, value); }
                    public String getString(String key) { return data.get(key) instanceof String value ? value : ""; }
                    public void putInt(String key, int value) { data.put(key, value); }
                    public int getInt(String key) { return data.get(key) instanceof Number value ? value.intValue() : 0; }
                    public void putLong(String key, long value) { data.put(key, value); }
                    public long getLong(String key) { return data.get(key) instanceof Number value ? value.longValue() : 0; }
                    public boolean isEmpty() { return data.isEmpty(); }
                    public void remove(String key) { data.remove(key); }
                    public CompoundTag copy() {
                        CompoundTag copy = new CompoundTag();
                        data.forEach((key, value) -> copy.data.put(key, value instanceof CompoundTag tag ? tag.copy() : value instanceof ListTag list ? list.copy() : value));
                        return copy;
                    }
                }
                """);
        source("net/minecraft/network/chat/MutableComponent", """
                package net.minecraft.network.chat;
                public class MutableComponent implements Component {
                    public String text;
                    public MutableComponent(String text) { this.text = text; }
                    public MutableComponent append(Component other) { text += ((MutableComponent) other).text; return this; }
                    public MutableComponent append(String other) { text += other; return this; }
                }
                """);
        source("net/minecraft/network/chat/Component", """
                package net.minecraft.network.chat;
                public interface Component { static MutableComponent literal(String text) { return new MutableComponent(text); } }
                """);
        source("net/minecraft/util/RandomSource", "package net.minecraft.util; public interface RandomSource { int nextInt(int bound); }");
        source("fixture/FixedRandom", "package fixture; public final class FixedRandom implements net.minecraft.util.RandomSource { private final int value; public FixedRandom(int value) { this.value = value; } public int nextInt(int bound) { return value; } }");
        source("net/minecraft/world/entity/Entity", """
                package net.minecraft.world.entity;
                public class Entity {
                    public java.util.UUID uuid = java.util.UUID.randomUUID();
                    public net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
                    public net.minecraft.util.RandomSource random = new fixture.FixedRandom(0);
                    public java.util.UUID getUUID() { return uuid; }
                    public net.minecraft.nbt.CompoundTag getPersistentData() { return data; }
                }
                """);
        source("net/minecraft/world/entity/LivingEntity", "package net.minecraft.world.entity; public class LivingEntity extends Entity { public net.minecraft.util.RandomSource getRandom() { return random; } }");
        source("net/minecraft/world/entity/projectile/Projectile", """
                package net.minecraft.world.entity.projectile;
                public class Projectile extends net.minecraft.world.entity.Entity {
                    public net.minecraft.world.entity.Entity owner;
                    public net.minecraft.world.entity.Entity getOwner() { return owner; }
                }
                """);
        source("net/minecraft/server/level/ServerLevel", "package net.minecraft.server.level; public class ServerLevel { public long time; public long getGameTime() { return time; } }");
        source("net/minecraft/server/MinecraftServer", """
                package net.minecraft.server;
                public class MinecraftServer {
                    public final net.minecraft.server.level.ServerLevel level = new net.minecraft.server.level.ServerLevel();
                    public net.minecraft.server.level.ServerLevel overworld() { return level; }
                }
                """);
        source("net/minecraft/world/item/ItemStack", """
                package net.minecraft.world.item;
                import net.minecraft.nbt.CompoundTag;
                public class ItemStack {
                    public static final ItemStack EMPTY = new ItemStack("", 0, "");
                    public final String item;
                    public int count;
                    public final String custom;
                    public ItemStack(String item, int count, String custom) { this.item = item; this.count = count; this.custom = custom; }
                    public boolean isEmpty() { return count <= 0 || item.isEmpty(); }
                    public CompoundTag save(CompoundTag tag) { tag.putString("id", item); tag.putInt("Count", count); tag.putString("Custom", custom); return tag; }
                    public static ItemStack of(CompoundTag tag) { return new ItemStack(tag.getString("id"), tag.getInt("Count"), tag.getString("Custom")); }
                    public net.minecraft.network.chat.Component getHoverName() { return net.minecraft.network.chat.Component.literal(item); }
                }
                """);
        source("net/minecraft/world/entity/player/Inventory", """
                package net.minecraft.world.entity.player;
                public class Inventory {
                    public int capacity = 36;
                    public final java.util.List<net.minecraft.world.item.ItemStack> items = new java.util.ArrayList<>();
                    public boolean add(net.minecraft.world.item.ItemStack stack) {
                        if (items.size() >= capacity) return false;
                        items.add(new net.minecraft.world.item.ItemStack(stack.item, stack.count, stack.custom)); stack.count = 0; return true;
                    }
                }
                """);
        source("net/minecraft/world/entity/player/Player", """
                package net.minecraft.world.entity.player;
                public class Player extends net.minecraft.world.entity.LivingEntity {
                    public static final String PERSISTED_NBT_TAG = "PlayerPersisted";
                    public boolean creative, spectator;
                    public final Inventory inventory = new Inventory();
                    public String message = "";
                    public boolean isCreative() { return creative; }
                    public boolean isSpectator() { return spectator; }
                    public Inventory getInventory() { return inventory; }
                    public void displayClientMessage(net.minecraft.network.chat.Component message, boolean actionBar) {
                        this.message = ((net.minecraft.network.chat.MutableComponent) message).text;
                    }
                }
                """);
        source("net/minecraft/server/level/ServerPlayer", """
                package net.minecraft.server.level;
                public class ServerPlayer extends net.minecraft.world.entity.player.Player {
                    public final net.minecraft.server.MinecraftServer server = new net.minecraft.server.MinecraftServer();
                    public final java.util.Map<String, top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler> curios = new java.util.LinkedHashMap<>();
                    public net.minecraft.server.MinecraftServer getServer() { return server; }
                }
                """);
        source("net/farmingtales/ftboss/entity/EasterBossEntity", """
                package net.farmingtales.ftboss.entity;
                public class EasterBossEntity extends net.minecraft.world.entity.LivingEntity {
                    public int variant = 8;
                    public boolean mini, giant;
                    public int getVariant() { return variant; }
                    public boolean isMini() { return mini; }
                    public boolean isGiant() { return giant; }
                }
                """);
        source("net/farmingtales/ftboss/boss/ManorMercy", """
                package net.farmingtales.ftboss.boss;
                public class ManorMercy {
                    public static net.minecraft.world.entity.Entity owner;
                    public static net.minecraft.world.entity.Entity damageOwner() { return owner; }
                }
                """);
        source("net/minecraft/world/damagesource/DamageSource", """
                package net.minecraft.world.damagesource;
                public class DamageSource {
                    public net.minecraft.world.entity.Entity owner, direct;
                    public net.minecraft.world.entity.Entity getEntity() { return owner; }
                    public net.minecraft.world.entity.Entity getDirectEntity() { return direct; }
                }
                """);
        source("net/minecraftforge/eventbus/api/Event", """
                package net.minecraftforge.eventbus.api;
                public class Event {
                    public enum Result { DEFAULT, ALLOW, DENY }
                    public Result result = Result.DEFAULT;
                    public boolean canceled;
                    public boolean isCanceled() { return canceled; }
                    public void setResult(Result result) { this.result = result; }
                }
                """);
        source("net/minecraftforge/eventbus/api/EventPriority", "package net.minecraftforge.eventbus.api; public enum EventPriority { HIGHEST, LOWEST, NORMAL }");
        source("net/minecraftforge/eventbus/api/SubscribeEvent", """
                package net.minecraftforge.eventbus.api;
                @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
                public @interface SubscribeEvent { EventPriority priority() default EventPriority.NORMAL; }
                """);
        source("net/minecraftforge/fml/common/Mod", """
                package net.minecraftforge.fml.common;
                public @interface Mod {
                    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
                    @interface EventBusSubscriber {
                        String modid(); Bus bus(); enum Bus { FORGE }
                    }
                }
                """);
        source("net/minecraftforge/event/TickEvent", """
                package net.minecraftforge.event;
                public class TickEvent {
                    public enum Phase { START, END }
                    public static class PlayerTickEvent {
                        public Phase phase = Phase.END;
                        public net.minecraft.world.entity.player.Player player;
                    }
                }
                """);
        source("net/minecraftforge/event/entity/living/LivingEvent", """
                package net.minecraftforge.event.entity.living;
                public class LivingEvent extends net.minecraftforge.eventbus.api.Event {
                    public net.minecraft.world.entity.LivingEntity entity;
                    public net.minecraft.world.entity.LivingEntity getEntity() { return entity; }
                }
                """);
        source("net/minecraftforge/event/entity/living/LivingDamageEvent", """
                package net.minecraftforge.event.entity.living;
                public class LivingDamageEvent extends LivingEvent {
                    public float amount = 1;
                    public net.minecraft.world.damagesource.DamageSource source = new net.minecraft.world.damagesource.DamageSource();
                    public float getAmount() { return amount; }
                    public net.minecraft.world.damagesource.DamageSource getSource() { return source; }
                }
                """);
        source("net/minecraftforge/event/entity/living/LivingDeathEvent", "package net.minecraftforge.event.entity.living; public class LivingDeathEvent extends LivingEvent {}");
        source("net/minecraftforge/event/entity/player/PlayerEvent", """
                package net.minecraftforge.event.entity.player;
                public class PlayerEvent extends net.minecraftforge.event.entity.living.LivingEvent {
                    @Override public net.minecraft.world.entity.player.Player getEntity() { return (net.minecraft.world.entity.player.Player) entity; }
                    public static class PlayerLoggedInEvent extends PlayerEvent {}
                    public static class PlayerLoggedOutEvent extends PlayerEvent {}
                    public static class PlayerRespawnEvent extends PlayerEvent {}
                }
                """);
        source("net/minecraftforge/event/server/ServerStoppedEvent", "package net.minecraftforge.event.server; public class ServerStoppedEvent {}");
        source("net/minecraftforge/common/util/LazyOptional", """
                package net.minecraftforge.common.util;
                public class LazyOptional<T> {
                    private final T value;
                    public LazyOptional(T value) { this.value = value; }
                    public java.util.Optional<T> resolve() { return java.util.Optional.ofNullable(value); }
                }
                """);
        source("top/theillusivec4/curios/api/type/inventory/IDynamicStackHandler", """
                package top.theillusivec4.curios.api.type.inventory;
                public interface IDynamicStackHandler {
                    int getSlots(); net.minecraft.world.item.ItemStack getStackInSlot(int index);
                    void setStackInSlot(int index, net.minecraft.world.item.ItemStack stack);
                }
                """);
        source("top/theillusivec4/curios/api/type/inventory/ICurioStacksHandler", """
                package top.theillusivec4.curios.api.type.inventory;
                public interface ICurioStacksHandler { IDynamicStackHandler getStacks(); }
                """);
        source("top/theillusivec4/curios/api/type/capability/ICuriosItemHandler", """
                package top.theillusivec4.curios.api.type.capability;
                public interface ICuriosItemHandler { java.util.Map<String, top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler> getCurios(); }
                """);
        source("top/theillusivec4/curios/api/CuriosApi", """
                package top.theillusivec4.curios.api;
                public class CuriosApi {
                    public static net.minecraftforge.common.util.LazyOptional<top.theillusivec4.curios.api.type.capability.ICuriosItemHandler> getCuriosInventory(net.minecraft.world.entity.LivingEntity entity) {
                        return new net.minecraftforge.common.util.LazyOptional<>(entity instanceof net.minecraft.server.level.ServerPlayer player ? () -> player.curios : null);
                    }
                }
                """);
        source("top/theillusivec4/curios/api/SlotContext", """
                package top.theillusivec4.curios.api;
                public record SlotContext(String identifier, net.minecraft.world.entity.LivingEntity entity, int index, boolean cosmetic, boolean visible) {}
                """);
        source("top/theillusivec4/curios/api/event/CurioEquipEvent", """
                package top.theillusivec4.curios.api.event;
                public class CurioEquipEvent extends net.minecraftforge.event.entity.living.LivingEvent {
                    public top.theillusivec4.curios.api.SlotContext context;
                    public top.theillusivec4.curios.api.SlotContext getSlotContext() { return context; }
                }
                """);
        Path root = Path.of(arguments[0]);
        for (var source : SOURCES.entrySet()) {
            Path file = root.resolve(source.getKey() + ".java");
            Files.createDirectories(file.getParent());
            Files.writeString(file, source.getValue() + "\n");
        }
    }
}
