package net.farmingtales.ftdungeonmobs;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class SkeletonMerchantEntity extends Villager implements GeoEntity {
    private static final EntityDataAccessor<Boolean> TRADING = SynchedEntityData.defineId(SkeletonMerchantEntity.class, EntityDataSerializers.BOOLEAN);
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public SkeletonMerchantEntity(EntityType<? extends Villager> type, Level level) {
        super(type, level);
        setNoAi(true);
        setPersistenceRequired();
        setVillagerData(getVillagerData().setProfession(VillagerProfession.TOOLSMITH).setLevel(5));
        MerchantOffers offers = new MerchantOffers();
        offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.BREAD, 16), 999999, 0, 0.0F));
        offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 4), new ItemStack(Items.ARROW, 32), 999999, 0, 0.0F));
        offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.GOLDEN_CARROT, 8), 999999, 0, 0.0F));
        setOffers(offers);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TRADING, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            entityData.set(TRADING, getTradingPlayer() != null);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<SkeletonMerchantEntity>(this, "main", 3,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.skeleton_merchant."
                        + (entityData.get(TRADING) ? "trade" : state.isMoving() ? "walk" : "idle")))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
