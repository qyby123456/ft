/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.Multimap
 *  io.redspace.ironsspellbooks.api.registry.AttributeRegistry
 *  io.redspace.ironsspellbooks.item.CastingItem
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 *  net.minecraftforge.event.entity.player.EntityItemPickupEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Vector3f
 *  software.bernie.geckolib.animatable.GeoItem
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package net.farmingtales.ftboss.item.weapons;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.CastingItem;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.farmingtales.ftboss.client.render.QizaiStaffRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class QizaiPrayerStaffItem
extends CastingItem
implements GeoItem {
    private static final double BASE_SPELL_POWER_BONUS = 0.5;
    private static final double BASE_MAX_MANA_BONUS = 300.0;
    private static final UUID SPELL_POWER_UUID = UUID.fromString("d07468dc-f8c6-40ba-9dbc-a97cfdb29a55");
    private static final UUID MAX_MANA_UUID = UUID.fromString("7633e078-e7df-49cc-92fb-d9a806f361ca");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.qizai_staff.idle");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlayAndHold("animation.qizai_staff.charge");
    private static final RawAnimation RELEASE = RawAnimation.begin().thenPlay("animation.qizai_staff.release");
    private static final DustParticleOptions CRIMSON_STAR = new DustParticleOptions(new Vector3f(0.96f, 0.08f, 0.3f), 1.05f);
    private static final DustParticleOptions GOLD_STAR = new DustParticleOptions(new Vector3f(1.0f, 0.66f, 0.2f), 0.82f);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private Multimap<Attribute, AttributeModifier> modifiers;

    public QizaiPrayerStaffItem() {
        super(new Item.Properties().m_41487_(1).m_41497_(Rarity.EPIC).m_41486_());
        GeoItem.registerSyncedAnimatable((GeoAnimatable)this);
    }

    public Multimap<Attribute, AttributeModifier> m_7167_(EquipmentSlot equipmentSlot) {
        if (equipmentSlot != EquipmentSlot.MAINHAND) {
            return super.m_7167_(equipmentSlot);
        }
        if (this.modifiers == null) {
            this.modifiers = ImmutableMultimap.builder().putAll(super.m_7167_(equipmentSlot)).put((Object)((Attribute)AttributeRegistry.SPELL_POWER.get()), (Object)new AttributeModifier(SPELL_POWER_UUID, "Qizai staff spell power", 0.5, AttributeModifier.Operation.ADDITION)).put((Object)((Attribute)AttributeRegistry.MAX_MANA.get()), (Object)new AttributeModifier(MAX_MANA_UUID, "Qizai staff max mana", 300.0, AttributeModifier.Operation.ADDITION)).build();
        }
        return this.modifiers;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "prayer", 0, animationState -> animationState.setAndContinue(IDLE)).triggerableAnim("charge", CHARGE).triggerableAnim("release", RELEASE)});
    }

    public static void animate(LivingEntity livingEntity, boolean bl) {
        Level level = livingEntity.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        level = livingEntity.m_21205_();
        Item item = level.m_41720_();
        if (item instanceof QizaiPrayerStaffItem) {
            QizaiPrayerStaffItem qizaiPrayerStaffItem = (QizaiPrayerStaffItem)item;
            qizaiPrayerStaffItem.triggerAnim((Entity)livingEntity, GeoItem.getOrAssignId((ItemStack)level, (ServerLevel)serverLevel), "prayer", bl ? "charge" : "release");
        }
    }

    public boolean onEntityItemUpdate(ItemStack itemStack, ItemEntity itemEntity) {
        Level level = itemEntity.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (itemEntity.m_32059_() % 8 == 0) {
                double d = (double)itemEntity.m_32059_() * 0.18;
                for (int i = 0; i < 2; ++i) {
                    double d2 = d + (double)i * Math.PI;
                    double d3 = 0.24;
                    serverLevel.m_8767_((ParticleOptions)(i == 0 ? CRIMSON_STAR : GOLD_STAR), itemEntity.m_20185_() + Math.cos(d2) * d3, itemEntity.m_20186_() + 0.28 + Math.sin(d * 0.55) * 0.045, itemEntity.m_20189_() + Math.sin(d2) * d3, 1, 0.0, 0.0, 0.0, 0.0);
                }
                if (itemEntity.m_32059_() % 16 == 0) {
                    serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, itemEntity.m_20185_(), itemEntity.m_20186_() + 0.36, itemEntity.m_20189_(), 1, 0.1, 0.12, 0.1, 0.01);
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onStaffPickup(EntityItemPickupEvent entityItemPickupEvent) {
        Level level;
        if (entityItemPickupEvent.isCanceled() || !(entityItemPickupEvent.getItem().m_32055_().m_41720_() instanceof QizaiPrayerStaffItem) || !((level = entityItemPickupEvent.getEntity().m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        level = entityItemPickupEvent.getItem();
        double d = level.m_20185_();
        double d2 = level.m_20186_() + 0.62;
        double d3 = level.m_20189_();
        double d4 = Math.toRadians(entityItemPickupEvent.getEntity().m_146908_());
        QizaiPrayerStaffItem.drawPickupStar(serverLevel, d, d2, d3, d4);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, d, d2, d3, 14, 0.3, 0.38, 0.3, 0.055);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123809_, d, d2, d3, 22, 0.42, 0.48, 0.42, 0.13);
        serverLevel.m_5594_(null, level.m_20183_(), SoundEvents.f_144243_, SoundSource.PLAYERS, 0.95f, 1.22f + entityItemPickupEvent.getEntity().m_217043_().m_188501_() * 0.18f);
    }

    private static void drawPickupStar(ServerLevel serverLevel, double d, double d2, double d3, double d4) {
        double d5 = Math.cos(d4);
        double d6 = Math.sin(d4);
        for (int i = 0; i < 10; ++i) {
            double d7 = -1.5707963267948966 + (double)i * Math.PI / 5.0;
            double d8 = -1.5707963267948966 + (double)(i + 1) * Math.PI / 5.0;
            double d9 = (i & 1) == 0 ? 0.58 : 0.25;
            double d10 = (i + 1 & 1) == 0 ? 0.58 : 0.25;
            double d11 = Math.cos(d7) * d9;
            double d12 = Math.sin(d7) * d9;
            double d13 = Math.cos(d8) * d10;
            double d14 = Math.sin(d8) * d10;
            for (int j = 0; j < 2; ++j) {
                double d15 = (double)j / 2.0;
                double d16 = d11 + (d13 - d11) * d15;
                double d17 = d12 + (d14 - d12) * d15;
                serverLevel.m_8767_((ParticleOptions)CRIMSON_STAR, d + d5 * d16, d2 + d17, d3 + d6 * d16, 1, 0.0, 0.0, 0.0, 0.0);
            }
            if ((i & 1) != 0) continue;
            serverLevel.m_8767_((ParticleOptions)GOLD_STAR, d + d5 * d11, d2 + d12, d3 + d6 * d11, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public void m_7373_(ItemStack itemStack, Level level, List<Component> list, TooltipFlag tooltipFlag) {
        super.m_7373_(itemStack, level, list, tooltipFlag);
        list.add((Component)Component.m_237115_((String)"tooltip.ftboss.qizai_prayer_staff").m_130940_(ChatFormatting.LIGHT_PURPLE));
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private BlockEntityWithoutLevelRenderer renderer;

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new QizaiStaffRenderer();
                }
                return this.renderer;
            }
        });
    }
}

