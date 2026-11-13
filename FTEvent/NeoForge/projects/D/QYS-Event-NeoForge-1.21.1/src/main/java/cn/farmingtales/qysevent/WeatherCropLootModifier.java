package cn.farmingtales.qysevent;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.ArrayList;
import java.util.List;

public final class WeatherCropLootModifier extends LootModifier {
    public static final MapCodec<WeatherCropLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, WeatherCropLootModifier::new));

    public WeatherCropLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!WeatherConfig.CROP_YIELD_EFFECTS.get() || !context.hasParam(LootContextParams.BLOCK_STATE)) return generatedLoot;
        BlockState state = context.getParam(LootContextParams.BLOCK_STATE);
        if (!CropUtil.isMature(state)) return generatedLoot;

        double multiplier = WeatherManager.getYieldMultiplier(context.getLevel());
        if (Math.abs(multiplier - 1.0D) < 0.001D) return generatedLoot;

        if (multiplier > 1.0D) {
            double extraFactor = multiplier - 1.0D;
            List<ItemStack> snapshot = new ArrayList<>(generatedLoot);
            for (ItemStack original : snapshot) {
                if (original.isEmpty()) continue;
                double exact = original.getCount() * extraFactor;
                int count = (int) Math.floor(exact);
                if (context.getRandom().nextDouble() < exact - count) count++;
                addCopies(generatedLoot, original, count);
            }
        } else {
            for (int i = generatedLoot.size() - 1; i >= 0; i--) {
                ItemStack stack = generatedLoot.get(i);
                double exact = stack.getCount() * Math.max(0.0D, multiplier);
                int count = (int) Math.floor(exact);
                if (context.getRandom().nextDouble() < exact - count) count++;
                if (count <= 0) generatedLoot.remove(i);
                else stack.setCount(count);
            }
        }
        return generatedLoot;
    }

    private static void addCopies(ObjectArrayList<ItemStack> loot, ItemStack template, int count) {
        int remaining = count;
        while (remaining > 0) {
            ItemStack copy = template.copy();
            int part = Math.min(copy.getMaxStackSize(), remaining);
            copy.setCount(part);
            loot.add(copy);
            remaining -= part;
        }
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}



