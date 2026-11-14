package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.ManorWeaponCatalog;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

/** 朵拉与庄园创造栏标记的 Bren 机枪使用地脉新星模型；2026-09-17，不修改普通 Bren 或弹匣判定。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DoraWeaponModel extends BakedModelWrapper<BakedModel> {
    private static final int DORA_VARIANT = 2;
    private static final ModelResourceLocation MACHINE_GUN = new ModelResourceLocation("bren", "machine_gun", "inventory");
    private static final ResourceLocation LAUNCHER = new ResourceLocation(FTBossMod.MOD_ID, "item/dora_nova_launcher_v1");
    private final ItemOverrides overrides;

    private DoraWeaponModel(BakedModel original, BakedModel launcher) {
        super(original);
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                      @Nullable LivingEntity entity, int seed) {
                boolean manorAppearance = stack.hasTag() && ManorWeaponCatalog.DORA_APPEARANCE.equals(
                        stack.getTag().getString(ManorWeaponCatalog.APPEARANCE_TAG));
                if (manorAppearance || entity instanceof EasterBossEntity boss && boss.getVariant() == DORA_VARIANT) {
                    return launcher;
                }
                return original.getOverrides().resolve(original, stack, level, entity, seed);
            }
        };
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(LAUNCHER);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void bake(ModelEvent.ModifyBakingResult event) {
        BakedModel original = event.getModels().get(MACHINE_GUN);
        if (original == null) return;
        BakedModel launcher = event.getModels().get(LAUNCHER);
        if (launcher == null) throw new IllegalStateException("Missing Dora nova launcher model: " + LAUNCHER);
        event.getModels().put(MACHINE_GUN, new DoraWeaponModel(original, launcher));
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }
}
