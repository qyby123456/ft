package cn.farmingtales.sdmshopaudit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class Bridge {
    private static final Map<String, Field> FIELDS = new ConcurrentHashMap<>();
    private static final String ROOT = "net.sixik.sdmshoprework.";
    private static MethodHandle paymentCount;
    private static final Map<String, MethodHandle> METHODS = new ConcurrentHashMap<>();

    private Bridge() {
    }

    public static Object mainShop() throws ReflectiveOperationException {
        return Class.forName(ROOT + "common.shop.ShopBase").getField("SERVER").get(null);
    }

    public static List<Object> shops() throws ReflectiveOperationException {
        LinkedHashSet<Object> shops = new LinkedHashSet<>();
        Object main = mainShop();
        if (main != null) {
            shops.add(main);
        }
        Object map = Class.forName(ROOT + "common.shop.MultiShop").getField("SHOP_MAP").get(null);
        if (map instanceof Map<?, ?> values) {
            shops.addAll(values.values());
        }
        shops.remove(null);
        return new ArrayList<>(shops);
    }

    public static ServerPlayer player(Object context) throws ReflectiveOperationException {
        Class<?> contextType = Class.forName("dev.architectury.networking.NetworkManager$PacketContext");
        Object player = contextType.getMethod("getPlayer").invoke(context);
        return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    public static boolean editMode(ServerPlayer player) throws ReflectiveOperationException {
        Object data = Class.forName("net.sixik.sdm_economy.api.ICustomData")
                .getMethod("sdm$getCustomData").invoke(player);
        return data instanceof CompoundTag tag && tag.getBoolean("edit_mode");
    }

    public static Object field(Object owner, String name) throws ReflectiveOperationException {
        String key = owner.getClass().getName() + "#" + name;
        Field field = FIELDS.get(key);
        if (field == null) {
            Class<?> cursor = owner.getClass();
            while (cursor != null) {
                try {
                    field = cursor.getDeclaredField(name);
                    field.setAccessible(true);
                    FIELDS.put(key, field);
                    break;
                } catch (NoSuchFieldException exception) {
                    cursor = cursor.getSuperclass();
                }
            }
            if (field == null) {
                throw new NoSuchFieldException(key);
            }
        }
        return field.get(owner);
    }

    public static Object optionalField(Object owner, String name) {
        try {
            return field(owner, name);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    public static CompoundTag serialize(Object owner) throws ReflectiveOperationException {
        Class<?> type = owner.getClass();
        while (!java.lang.reflect.Modifier.isPublic(type.getModifiers()) && type.getSuperclass() != null) {
            type = type.getSuperclass();
        }
        Object result = invoke(type, "serializeNBT", CompoundTag.class, owner, new Class<?>[0]);
        if (!(result instanceof CompoundTag tag)) {
            throw new IllegalStateException("Unexpected SDM serializer result: " + result);
        }
        return tag.copy();
    }

    public static Object invoke(Class<?> type, String name, Class<?> result, Object owner,
                                Class<?>[] parameters, Object... arguments) throws ReflectiveOperationException {
        MethodType signature = MethodType.methodType(result, parameters);
        String key = type.getName() + "#" + name + signature + "#" + (owner == null);
        MethodHandle handle = METHODS.get(key);
        if (handle == null) {
            handle = owner == null ? MethodHandles.publicLookup().findStatic(type, name, signature)
                    : MethodHandles.publicLookup().findVirtual(type, name, signature);
            METHODS.put(key, handle);
        }
        List<Object> values = new ArrayList<>();
        if (owner != null) {
            values.add(owner);
        }
        values.addAll(java.util.Arrays.asList(arguments));
        try {
            return handle.invokeWithArguments(values);
        } catch (RuntimeException | Error exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new ReflectiveOperationException("SDM bridge invocation failed: " + key, exception);
        }
    }

    public static Object entryType(Object entry) throws ReflectiveOperationException {
        return invoke(Class.forName(ROOT + "api.shop.AbstractShopEntry"), "getEntryType",
                Class.forName(ROOT + "api.shop.AbstractShopEntryType"), entry, new Class<?>[0]);
    }

    public static boolean pay(Object entry, ServerPlayer player, long amount) throws ReflectiveOperationException {
        return (boolean) invoke(Class.forName(ROOT + "api.shop.AbstractShopSellerType"), "buy", boolean.class,
                field(entry, "shopSellerType"), new Class<?>[]{Player.class,
                        Class.forName(ROOT + "api.shop.AbstractShopEntry"), long.class}, player, entry, amount);
    }

    public static Object entry(Object packet) throws ReflectiveOperationException {
        Object shop = mainShop();
        UUID tabId = (UUID) field(packet, "tabUUID");
        Object tab = shop.getClass().getMethod("getShopTab", UUID.class).invoke(shop, tabId);
        if (tab == null) {
            return null;
        }
        return tab.getClass().getMethod("getShopEntry", UUID.class)
                .invoke(tab, field(packet, "entryUUID"));
    }

    public static Long paymentBalance(Object entry, ServerPlayer player) throws ReflectiveOperationException {
        Object seller = field(entry, "shopSellerType");
        if (seller == null) {
            return null;
        }
        if (paymentCount == null) {
            paymentCount = MethodHandles.publicLookup().findVirtual(
                    Class.forName(ROOT + "api.shop.AbstractShopSellerType"), "getCount",
                    MethodType.methodType(long.class, Player.class));
        }
        try {
            return (long) paymentCount.invoke(seller, player);
        } catch (RuntimeException | Error exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new ReflectiveOperationException("Cannot read SDM payment balance", exception);
        }
    }
}
