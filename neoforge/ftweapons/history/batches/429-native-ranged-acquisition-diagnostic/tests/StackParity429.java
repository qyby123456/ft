package ftboss.storage308;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.farmingtales.ftboss.transaction.NativeRecipeEvidenceData;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public final class StackParity429 {
    private static byte[] binary(Tag value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        value.write(new DataOutputStream(bytes));
        return bytes.toByteArray();
    }
    public static Map<String, Object> run(ServerStartedEvent event) throws Exception {
        int cases = 0;
        for (Item item : List.of(Items.IRON_SWORD, Items.BOW, Items.CROSSBOW, Items.IRON_AXE, Items.TRIDENT, Items.STICK)) {
            for (int variant = 0; variant < 32; variant++) {
                ItemStack stack = new ItemStack(item, 1 + variant % 3);
                if (stack.isDamageableItem()) stack.setDamageValue(variant);
                CompoundTag data = new CompoundTag();
                data.putString("中文", "配方栈" + variant);
                data.putByteArray("bytes", new byte[]{0, -1, (byte)variant});
                data.putIntArray("ints", new int[]{Integer.MIN_VALUE, variant, Integer.MAX_VALUE});
                data.putLongArray("longs", new long[]{Long.MIN_VALUE, variant, Long.MAX_VALUE});
                data.putDouble("real", variant == 0 ? -0.0 : variant + 0.125);
                CompoundTag collision = new CompoundTag(); collision.putInt("Aa", variant); collision.putInt("BB", -variant);
                data.put("collidingKeys", collision);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
                stack.set(DataComponents.CUSTOM_NAME, Component.literal("配方组件" + variant));
                CompoundTag before = RecipeDataBefore429.stack(stack, event.getServer().registryAccess());
                CompoundTag after = NativeRecipeEvidenceData.stack(stack, event.getServer().registryAccess());
                if (!Arrays.equals(binary(before), binary(after))) throw new AssertionError("Recipe component encoding bytes changed: " + item + " / " + variant);
                cases++;
            }
        }
        for (boolean before : List.of(true, false)) {
            try {
                if (before) RecipeDataBefore429.stack(ItemStack.EMPTY, event.getServer().registryAccess());
                else NativeRecipeEvidenceData.stack(ItemStack.EMPTY, event.getServer().registryAccess());
                throw new AssertionError("Empty recipe stack accepted");
            } catch (IllegalArgumentException rejected) {
                if (!"Empty native recipe item evidence".equals(rejected.getMessage())) throw rejected;
            }
        }
        return Map.of("failures", 0, "componentStackByteParityCases", cases, "emptyStackRejectionPreserved", true,
            "authorityFieldsWritten", false, "fullOpaqueProviderMutationAcceptance", false);
    }
}
