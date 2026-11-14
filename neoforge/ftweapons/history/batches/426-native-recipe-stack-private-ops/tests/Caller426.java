import com.google.gson.GsonBuilder;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class Caller426 {
    private static Method helper() throws Exception {
        return Class.forName("net.farmingtales.ftboss.item.NativeExpectedFieldObservation")
            .getDeclaredMethod("recipeStackForRead426", ItemStack.class, RegistryAccess.class);
    }

    public static CompoundTag stack(ItemStack stack, RegistryAccess registries) throws Exception {
        return (CompoundTag)helper().invoke(null, stack, registries);
    }

    public static void main(String[] arguments) throws Throwable {
        int rejected = 0;
        try { helper().invoke(null, null, null); throw new AssertionError("Foreign reflective recipe encoding accepted"); }
        catch (InvocationTargetException denied) { if (!(denied.getCause() instanceof IllegalStateException)) throw denied; rejected++; }
        try { MethodHandles.lookup().unreflect(helper()).invokeWithArguments(Arrays.asList(null, null)); throw new AssertionError("Foreign method-handle recipe encoding accepted"); }
        catch (IllegalStateException denied) { rejected++; }
        try { stack(null, null); throw new AssertionError("Foreign exact-name encoding accepted"); }
        catch (InvocationTargetException denied) { if (!(denied.getCause() instanceof IllegalStateException)) throw denied; rejected++; }
        Files.writeString(Path.of(arguments[0]), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
            "failures", 0, "foreignCallerRejections", rejected, "actualRecipeParityTested", false, "installable", false)));
    }
}
