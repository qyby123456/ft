package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
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

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class XueliWeaponModel extends BakedModelWrapper<BakedModel> {
    private static final int MODEL_DATA = 160417;
    private static final ResourceLocation SKILLET = new ResourceLocation(FTBossMod.MOD_ID, "item/xueli/skillet");
    private static final ResourceLocation ROLLING_PIN = new ResourceLocation(FTBossMod.MOD_ID, "item/xueli/rolling_pin");
    private final ItemOverrides overrides;

    private XueliWeaponModel(BakedModel original, BakedModel weapon) {
        super(original);
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                      @Nullable LivingEntity entity, int seed) {
                boolean heldByXueli = entity instanceof EasterBossEntity boss && boss.getVariant() == EasterBossEntity.XUELI_VARIANT;
                boolean markedForPreview = stack.hasTag() && stack.getTag().getInt("CustomModelData") == MODEL_DATA;
                if (heldByXueli || markedForPreview) return weapon;
                return original.getOverrides().resolve(original, stack, level, entity, seed);
            }
        };
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(SKILLET);
        event.register(ROLLING_PIN);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void bake(ModelEvent.ModifyBakingResult event) {
        wrap(event, new ModelResourceLocation("farmersdelight", "skillet", "inventory"), SKILLET);
        wrap(event, new ModelResourceLocation("bakery", "rolling_pin", "inventory"), ROLLING_PIN);
    }

    private static void wrap(ModelEvent.ModifyBakingResult event, ModelResourceLocation itemId, ResourceLocation weaponId) {
        BakedModel original = event.getModels().get(itemId);
        if (original == null) return;
        BakedModel weapon = event.getModels().get(weaponId);
        if (weapon == null) throw new IllegalStateException("Missing Xueli weapon model: " + weaponId);
        event.getModels().put(itemId, new XueliWeaponModel(original, weapon));
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }
}
