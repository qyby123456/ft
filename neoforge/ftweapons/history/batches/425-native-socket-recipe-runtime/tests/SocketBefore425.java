package ftboss.storage308;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import net.farmingtales.ftboss.item.NativeFoundationItemData;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import static net.farmingtales.ftboss.item.FoodWeaponSourceLedger.boundProfile;
public final class SocketBefore425 {
    public static String socketFingerprint(ItemStack var0, String var1) {
        CompoundTag var2 = new CompoundTag();
        CompoundTag var3 = NativeFoundationItemData.element(var0, "FTBossFoodEvolution");
        ListTag var4 = new ListTag();
        if (var3 != null) {
            for (Tag var6 : var3.getList("Sockets", 10)) {
                CompoundTag var7 = (CompoundTag)var6;
                CompoundTag var8 = new CompoundTag();
                var8.putString("Type", var7.getString("Type"));
                var8.putString("Item", var7.getString("Item"));
                if (var7.contains("Tier", 3)) {
                    var8.putInt("Tier", var7.getInt("Tier"));
                }

                var4.add(var8);
            }
        }

        var2.put("Sockets", var4);
        var2.putInt("Count", var4.size());
        var2.putString("Profile", boundProfile(var0));
        var2.putString("Family", var1);
        return fingerprint(var2);
    }

    private static String fingerprint(Tag value) {
        try {
            java.lang.reflect.Method method = FoodWeaponSourceLedger.class.getDeclaredMethod("fingerprint", Tag.class);
            method.setAccessible(true);
            return (String)method.invoke(null, value);
        } catch (java.lang.reflect.InvocationTargetException failure) {
            if (failure.getCause() instanceof RuntimeException runtime) throw runtime;
            if (failure.getCause() instanceof Error error) throw error;
            throw new IllegalStateException(failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException(failure);
        }
    }
}
