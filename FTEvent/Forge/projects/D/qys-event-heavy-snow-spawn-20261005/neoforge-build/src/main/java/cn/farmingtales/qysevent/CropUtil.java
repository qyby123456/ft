package cn.farmingtales.qysevent;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;
import java.util.Locale;

public final class CropUtil {
    public static boolean isCrop(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock || block instanceof SweetBerryBushBlock || block instanceof NetherWartBlock || block instanceof CocoaBlock) {
            return true;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        String path = id.getPath().toLowerCase(Locale.ROOT);
        boolean cropishName = path.contains("crop") || path.contains("wheat") || path.contains("barley") ||
                path.contains("oat") || path.contains("corn") || path.contains("rice") || path.contains("tomato") ||
                path.contains("grape") || path.contains("berry") || path.contains("tea_plant") ||
                path.contains("coffee_plant") || path.contains("flax") || path.contains("chili") ||
                path.contains("cucumber") || path.contains("soybean") || path.contains("redbean") ||
                path.contains("leek") || path.contains("lettuce") || path.contains("onion") ||
                path.contains("potato") || path.contains("carrot") || path.contains("beetroot");
        return cropishName && (block instanceof BushBlock || findAgeProperty(state) != null);
    }

    public static boolean isMature(BlockState state) {
        if (!isCrop(state)) return false;
        if (state.getBlock() instanceof CropBlock crop) return crop.isMaxAge(state);
        IntegerProperty age = findAgeProperty(state);
        if (age == null) return true;
        int current = state.getValue(age);
        int max = age.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(current);
        return current >= max;
    }

    public static IntegerProperty findAgeProperty(BlockState state) {
        Collection<Property<?>> properties = state.getProperties();
        for (Property<?> property : properties) {
            if (property instanceof IntegerProperty integer &&
                    (property.getName().equals("age") || property.getName().equals("stage"))) {
                return integer;
            }
        }
        return null;
    }

    private CropUtil() {}
}


