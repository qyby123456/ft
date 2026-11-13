package cn.farmingtales.qysevent;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

/** Crop classification shared by growth and loot hooks, cached once per block type. */
public final class CropUtil {
    private static final Map<Block, CropProfile> PROFILES = new ConcurrentHashMap<>();

    public static boolean isCrop(BlockState state) {
        return profile(state).crop();
    }

    public static boolean isMature(BlockState state) {
        CropProfile profile = profile(state);
        if (!profile.crop()) return false;
        if (state.getBlock() instanceof CropBlock crop) return crop.isMaxAge(state);
        IntegerProperty age = profile.age();
        return age == null || state.getValue(age) >= profile.maxAge();
    }

    public static IntegerProperty findAgeProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integer
                    && (property.getName().equals("age") || property.getName().equals("stage"))) {
                return integer;
            }
        }
        return null;
    }

    private static CropProfile profile(BlockState state) {
        return PROFILES.computeIfAbsent(state.getBlock(), block -> inspect(block, state));
    }

    private static CropProfile inspect(Block block, BlockState state) {
        IntegerProperty age = findAgeProperty(state);
        boolean knownCrop = block instanceof CropBlock || block instanceof SweetBerryBushBlock
                || block instanceof NetherWartBlock || block instanceof CocoaBlock;
        boolean crop = knownCrop;
        if (!crop) {
            String path = BuiltInRegistries.BLOCK.getKey(block).getPath().toLowerCase(Locale.ROOT);
            boolean cropishName = path.contains("crop") || path.contains("wheat") || path.contains("barley")
                    || path.contains("oat") || path.contains("corn") || path.contains("rice")
                    || path.contains("tomato") || path.contains("grape") || path.contains("berry")
                    || path.contains("tea_plant") || path.contains("coffee_plant") || path.contains("flax")
                    || path.contains("chili") || path.contains("cucumber") || path.contains("soybean")
                    || path.contains("redbean") || path.contains("leek") || path.contains("lettuce")
                    || path.contains("onion") || path.contains("potato") || path.contains("carrot")
                    || path.contains("beetroot");
            crop = cropishName && (block instanceof BushBlock || age != null);
        }

        int maxAge = 0;
        if (age != null) {
            for (int value : age.getPossibleValues()) maxAge = Math.max(maxAge, value);
        }
        return new CropProfile(crop, age, maxAge);
    }

    private record CropProfile(boolean crop, IntegerProperty age, int maxAge) {}

    private CropUtil() {}
}
