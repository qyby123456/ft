/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 *  net.minecraftforge.event.entity.player.EntityItemPickupEvent
 *  software.bernie.geckolib.animatable.GeoItem
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import java.util.function.Consumer;
import net.farmingtales.ftboss.client.render.GourmetBlankBladeRenderer;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.weapons.GourmetEvolutionWeapon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class GourmetBlankBladeItem
extends Item
implements GeoItem,
GourmetEvolutionWeapon {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.gourmet_blank_blade.idle");
    private static final RawAnimation EVOLVE = RawAnimation.begin().thenPlay("animation.gourmet_blank_blade.evolve");
    private static final RawAnimation SOCKET = RawAnimation.begin().thenPlay("animation.gourmet_blank_blade.socket");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.gourmet_blank_blade.attack");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public GourmetBlankBladeItem() {
        super(new Item.Properties().m_41487_(1).m_41497_(Rarity.EPIC).m_41486_());
        GeoItem.registerSyncedAnimatable((GeoAnimatable)this);
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "blade", 2, state -> state.setAndContinue(IDLE)).triggerableAnim("evolve", EVOLVE).triggerableAnim("socket", SOCKET).triggerableAnim("attack", ATTACK)});
    }

    public static void animateEvolution(ServerPlayer holder, ItemStack stack) {
        Item item = stack.m_41720_();
        if (!(item instanceof GourmetBlankBladeItem)) {
            return;
        }
        GourmetBlankBladeItem blade = (GourmetBlankBladeItem)item;
        blade.triggerAnim((Entity)holder, GeoItem.getOrAssignId((ItemStack)stack, (ServerLevel)holder.m_284548_()), "blade", "evolve");
    }

    public static void animateSocket(ServerPlayer holder, ItemStack stack) {
        Item item = stack.m_41720_();
        if (!(item instanceof GourmetBlankBladeItem)) {
            return;
        }
        GourmetBlankBladeItem blade = (GourmetBlankBladeItem)item;
        blade.triggerAnim((Entity)holder, GeoItem.getOrAssignId((ItemStack)stack, (ServerLevel)holder.m_284548_()), "blade", "socket");
    }

    public static void animateAttack(ServerPlayer holder, ItemStack stack) {
        Item item = stack.m_41720_();
        if (!(item instanceof GourmetBlankBladeItem)) {
            return;
        }
        GourmetBlankBladeItem blade = (GourmetBlankBladeItem)item;
        blade.triggerAnim((Entity)holder, GeoItem.getOrAssignId((ItemStack)stack, (ServerLevel)holder.m_284548_()), "blade", "attack");
    }

    @Override
    public void triggerEvolution(ServerPlayer holder, ItemStack stack) {
        this.triggerAnim((Entity)holder, GeoItem.getOrAssignId((ItemStack)stack, (ServerLevel)holder.m_284548_()), "blade", "evolve");
    }

    @Override
    public void triggerSocket(ServerPlayer holder, ItemStack stack) {
        this.triggerAnim((Entity)holder, GeoItem.getOrAssignId((ItemStack)stack, (ServerLevel)holder.m_284548_()), "blade", "socket");
    }

    @Override
    public void triggerActive(ServerPlayer holder, ItemStack stack) {
        this.triggerAnim((Entity)holder, GeoItem.getOrAssignId((ItemStack)stack, (ServerLevel)holder.m_284548_()), "blade", "evolve");
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public boolean m_5812_(ItemStack stack) {
        return true;
    }

    public boolean m_8120_(ItemStack stack) {
        return stack.m_41613_() == 1;
    }

    public int m_6473_() {
        return 22;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.m_6081_(new ItemStack((ItemLike)Items.f_42393_));
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return EnchantmentHelper.m_44831_((ItemStack)book).keySet().stream().allMatch(enchantment -> this.canApplyAtEnchantingTable(stack, (Enchantment)enchantment));
    }

    public void m_6883_(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        super.m_6883_(stack, level, holder, slot, selected);
        if (!selected || !(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        if (holder instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)holder;
            if (player.f_20911_ && player.f_20913_ == 1 && !player.m_6117_()) {
                GourmetBlankBladeItem.animateAttack(player, stack);
            }
        }
        if (holder.f_19797_ % 12 != 0) {
            return;
        }
        int evolution = FoodWeaponEvolution.evolutionLevel(stack);
        server.m_8767_((ParticleOptions)(evolution > 0 ? ParticleTypes.f_123810_ : ParticleTypes.f_123809_), holder.m_20185_(), holder.m_20186_() + 0.9, holder.m_20189_(), 1, 0.22 + (double)evolution * 0.015, 0.3, 0.22 + (double)evolution * 0.015, 0.01);
    }

    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        Level level = entity.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            if (entity.m_32059_() % 6 == 0) {
                int evolution = FoodWeaponEvolution.evolutionLevel(stack);
                server.m_8767_((ParticleOptions)(evolution > 0 ? ParticleTypes.f_123810_ : ParticleTypes.f_123809_), entity.m_20185_(), entity.m_20186_() + 0.28, entity.m_20189_(), 1 + evolution / 3, 0.18, 0.22, 0.18, 0.015);
            }
        }
        return false;
    }

    public static void playPickupEffect(EntityItemPickupEvent event) {
        Level level;
        if (event.isCanceled() || !(event.getItem().m_32055_().m_41720_() instanceof GourmetEvolutionWeapon) || !((level = event.getEntity().m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        ItemEntity item = event.getItem();
        level2.m_8767_((ParticleOptions)ParticleTypes.f_123810_, item.m_20185_(), item.m_20186_() + 0.45, item.m_20189_(), 26, 0.45, 0.55, 0.45, 0.09);
        level2.m_8767_((ParticleOptions)ParticleTypes.f_123809_, item.m_20185_(), item.m_20186_() + 0.45, item.m_20189_(), 36, 0.55, 0.65, 0.55, 0.18);
        level2.m_5594_(null, item.m_20183_(), SoundEvents.f_144243_, SoundSource.PLAYERS, 0.85f, 0.8f + event.getEntity().m_217043_().m_188501_() * 0.35f);
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        super.m_7373_(stack, level, lines, flag);
        lines.add((Component)Component.m_237115_((String)"tooltip.ftboss.gourmet.zero_base").m_130940_(ChatFormatting.GRAY));
        lines.add((Component)Component.m_237110_((String)"tooltip.ftboss.gourmet.capacity", (Object[])new Object[]{30, 8}).m_130940_(ChatFormatting.AQUA));
        lines.add((Component)Component.m_237115_((String)"tooltip.ftboss.gourmet.use").m_130940_(ChatFormatting.DARK_GRAY));
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private BlockEntityWithoutLevelRenderer renderer;

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GourmetBlankBladeRenderer();
                }
                return this.renderer;
            }
        });
    }
}

