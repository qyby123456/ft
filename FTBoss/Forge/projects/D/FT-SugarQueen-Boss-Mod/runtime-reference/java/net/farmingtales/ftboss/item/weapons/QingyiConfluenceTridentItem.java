/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.Multimap
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.stats.Stats
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.AbstractArrow$Pickup
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.TridentItem
 *  net.minecraft.world.item.UseAnim
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
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
import java.util.List;
import java.util.function.Consumer;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.render.QingyiConfluenceTridentRenderer;
import net.farmingtales.ftboss.entity.QingyiConfluenceTridentEntity;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.farmingtales.ftboss.skill.QingyiConfluenceSkill;
import net.farmingtales.ftboss.util.QingyiConfluenceRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class QingyiConfluenceTridentItem
extends TridentItem
implements GeoItem {
    private static final String SKILL_USE = "ftboss:qingyi_skill_use";
    private static final String SKILL_READY_AT = "ftboss:qingyi_skill_ready_at";
    private static final String CONTROLLER = "confluence";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.qingyi_confluence.idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.qingyi_confluence.attack");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("animation.qingyi_confluence.charge");
    private static final RawAnimation RELEASE = RawAnimation.begin().thenPlay("animation.qingyi_confluence.release");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private final Multimap<Attribute, AttributeModifier> modifiers = ImmutableMultimap.builder().put((Object)Attributes.f_22281_, (Object)new AttributeModifier(f_41374_, "Weapon modifier", (double)(16.0f + FTWeaponTier.INSTANCE.m_6631_()), AttributeModifier.Operation.ADDITION)).put((Object)Attributes.f_22283_, (Object)new AttributeModifier(f_41375_, "Weapon modifier", (double)-2.8f, AttributeModifier.Operation.ADDITION)).build();

    public QingyiConfluenceTridentItem() {
        super(new Item.Properties().m_41503_(FTWeaponTier.INSTANCE.m_6609_()).m_41497_(Rarity.EPIC).m_41486_());
        GeoItem.registerSyncedAnimatable((GeoAnimatable)this);
    }

    public Multimap<Attribute, AttributeModifier> m_7167_(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.modifiers : super.m_7167_(slot);
    }

    public int m_6473_() {
        return FTWeaponTier.INSTANCE.m_6601_();
    }

    public boolean m_6832_(ItemStack stack, ItemStack ingredient) {
        return FTWeaponTier.INSTANCE.m_6282_().test(ingredient) || super.m_6832_(stack, ingredient);
    }

    private static boolean isSkillUse(ItemStack stack) {
        return stack.m_41782_() && stack.m_41783_().m_128471_(SKILL_USE);
    }

    private static void clearSkillUse(ItemStack stack) {
        if (stack.m_41782_()) {
            stack.m_41783_().m_128473_(SKILL_USE);
        }
    }

    private static long skillCooldown(Player player) {
        return QingyiConfluenceRules.skillCooldownRemaining(player.m_9236_().m_46467_(), player.getPersistentData().m_128454_(SKILL_READY_AT));
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, CONTROLLER, 0, state -> state.setAndContinue(IDLE)).triggerableAnim("attack", ATTACK).triggerableAnim("charge", CHARGE).triggerableAnim("release", RELEASE)});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static void animate(LivingEntity holder, String action) {
        ItemStack stack = holder.m_21205_();
        Item item = stack.m_41720_();
        if (item instanceof QingyiConfluenceTridentItem) {
            QingyiConfluenceTridentItem item2 = (QingyiConfluenceTridentItem)item;
            item = holder.m_9236_();
            if (item instanceof ServerLevel) {
                ServerLevel level = (ServerLevel)item;
                item2.triggerAnim((Entity)holder, GeoItem.getOrAssignId((ItemStack)stack, (ServerLevel)level), CONTROLLER, action);
            }
        }
    }

    public static void cancel(LivingEntity holder, ItemStack stack) {
        Item item = stack.m_41720_();
        if (item instanceof QingyiConfluenceTridentItem) {
            QingyiConfluenceTridentItem item2 = (QingyiConfluenceTridentItem)item;
            if (holder.m_9236_() instanceof ServerLevel && stack.m_41782_() && stack.m_41783_().m_128425_("GeckoLibID", 99)) {
                long instanceId = GeoItem.getId((ItemStack)stack);
                item2.stopTriggeredAnim((Entity)holder, instanceId, CONTROLLER, "charge");
                item2.stopTriggeredAnim((Entity)holder, instanceId, CONTROLLER, "release");
            }
        }
    }

    public void onStopUsing(ItemStack stack, LivingEntity user, int remaining) {
        if (!QingyiConfluenceTridentItem.isSkillUse(stack)) {
            return;
        }
        QingyiConfluenceTridentItem.clearSkillUse(stack);
        QingyiConfluenceTridentItem.cancel(user, stack);
    }

    public void m_6883_(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.m_6883_(stack, level, entity, slot, selected);
        if (selected && entity instanceof Player) {
            Player player = (Player)entity;
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                GeoItem.getOrAssignId((ItemStack)stack, (ServerLevel)serverLevel);
                if (player.f_20911_ && player.f_20913_ == 1 && !player.m_6117_()) {
                    QingyiConfluenceTridentItem.animate((LivingEntity)player, "attack");
                }
            }
        }
    }

    public boolean m_7579_(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.m_7579_(stack, target, attacker);
        if (hit && !attacker.m_9236_().f_46443_ && QingyiConfluenceSkill.canTarget(attacker, target)) {
            QingyiConfluenceSkill.mark(target, 40);
        }
        return hit;
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        QingyiConfluenceTridentItem.clearSkillUse(stack);
        if (!player.m_6084_() || player.m_5833_() || stack.m_41773_() >= stack.m_41776_() - 1) {
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        if (hand != InteractionHand.MAIN_HAND || !player.m_6144_()) {
            InteractionResultHolder result = super.m_7203_(level, player, hand);
            if (!level.f_46443_) {
                FTBossMod.LOGGER.info("[FTBoss][QingyiUse] throw start player={} result={} duration={}", new Object[]{player.m_7755_().getString(), result.m_19089_(), player.m_21212_()});
            }
            return result;
        }
        if (player.m_36335_().m_41519_((Item)this) || QingyiConfluenceTridentItem.skillCooldown(player) > 0L) {
            if (!level.f_46443_) {
                player.m_5661_((Component)Component.m_237110_((String)"tooltip.ftboss.qingyi_confluence_trident.cooldown", (Object[])new Object[]{(QingyiConfluenceTridentItem.skillCooldown(player) + 19L) / 20L}), true);
            }
            return InteractionResultHolder.m_19100_((Object)stack);
        }
        stack.m_41784_().m_128379_(SKILL_USE, true);
        player.m_6672_(hand);
        QingyiConfluenceTridentItem.animate((LivingEntity)player, "charge");
        if (!level.f_46443_) {
            FTBossMod.LOGGER.info("[FTBoss][QingyiUse] skill start player={} duration={}", (Object)player.m_7755_().getString(), (Object)player.m_21212_());
        }
        return InteractionResultHolder.m_19096_((Object)stack);
    }

    public int m_8105_(ItemStack stack) {
        return QingyiConfluenceTridentItem.isSkillUse(stack) ? QingyiConfluenceRules.useTicks() : 72000;
    }

    public UseAnim m_6164_(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    public void m_5929_(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (!QingyiConfluenceTridentItem.isSkillUse(stack) || !(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        int elapsed = this.m_8105_(stack) - remaining + 1;
        if (elapsed == 32) {
            QingyiConfluenceTridentItem.animate(user, "release");
        }
        if (elapsed < 32 && elapsed % 4 == 0) {
            QingyiConfluenceSkill.chargeFx(serverLevel, user, elapsed);
        }
    }

    public void m_5551_(ItemStack stack, Level level, LivingEntity user, int remaining) {
        Player player;
        if (QingyiConfluenceTridentItem.isSkillUse(stack)) {
            QingyiConfluenceTridentItem.clearSkillUse(stack);
            QingyiConfluenceTridentItem.cancel(user, stack);
            if (!level.f_46443_) {
                FTBossMod.LOGGER.info("[FTBoss][QingyiUse] skill cancelled player={} remaining={}", (Object)user.m_7755_().getString(), (Object)remaining);
            }
            return;
        }
        if (!(user instanceof Player) || !(player = (Player)user).m_6084_() || player.m_5833_() || player.m_21120_(player.m_7655_()) != stack || !QingyiConfluenceRules.canThrow(remaining)) {
            return;
        }
        if (EnchantmentHelper.m_44932_((ItemStack)stack) > 0) {
            super.m_5551_(stack, level, user, remaining);
            return;
        }
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        stack.m_41622_(1, (LivingEntity)player, holder -> holder.m_21190_(player.m_7655_()));
        QingyiConfluenceTridentEntity trident = new QingyiConfluenceTridentEntity((Level)serverLevel, (LivingEntity)player, stack);
        trident.m_37251_((Entity)player, player.m_146909_(), player.m_146908_(), 0.0f, 2.5f, 1.0f);
        AbstractArrow.Pickup pickup = trident.f_36705_ = player.m_150110_().f_35937_ ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
        if (!serverLevel.m_7967_((Entity)trident)) {
            return;
        }
        if (!player.m_150110_().f_35937_) {
            player.m_150109_().m_36057_(stack);
        }
        FTBossMod.LOGGER.info("[FTBoss][QingyiUse] thrown player={} entity={} chargeTicks={}", new Object[]{player.m_7755_().getString(), trident.m_19879_(), 72000 - remaining});
        player.m_36246_(Stats.f_12982_.m_12902_((Object)this));
        serverLevel.m_6263_(null, trident.m_20185_(), trident.m_20186_(), trident.m_20189_(), SoundEvents.f_12520_, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public ItemStack m_5922_(ItemStack stack, Level level, LivingEntity user) {
        Player player;
        ServerLevel serverLevel;
        block5: {
            block4: {
                boolean skill = QingyiConfluenceTridentItem.isSkillUse(stack);
                QingyiConfluenceTridentItem.clearSkillUse(stack);
                if (!skill) {
                    return stack;
                }
                if (!(level instanceof ServerLevel)) break block4;
                serverLevel = (ServerLevel)level;
                if (user instanceof Player && (player = (Player)user).m_6084_() && !player.m_5833_() && player.m_21205_() == stack && !player.m_36335_().m_41519_((Item)this) && QingyiConfluenceTridentItem.skillCooldown(player) <= 0L) break block5;
            }
            return stack;
        }
        QingyiConfluenceSkill.burst(serverLevel, (LivingEntity)player, QingyiConfluenceSkill.aim(player));
        FTBossMod.LOGGER.info("[FTBoss][QingyiUse] skill released player={}", (Object)player.m_7755_().getString());
        player.getPersistentData().m_128356_(SKILL_READY_AT, level.m_46467_() + 160L);
        stack.m_41622_(3, (LivingEntity)player, holder -> holder.m_21190_(InteractionHand.MAIN_HAND));
        return stack;
    }

    public void m_7373_(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        super.m_7373_(stack, level, lines, flag);
        lines.add((Component)Component.m_237115_((String)"tooltip.ftboss.qingyi_confluence_trident.elements").m_130940_(ChatFormatting.AQUA));
        lines.add((Component)Component.m_237115_((String)"tooltip.ftboss.qingyi_confluence_trident.throw").m_130940_(ChatFormatting.AQUA));
        lines.add((Component)Component.m_237115_((String)"tooltip.ftboss.qingyi_confluence_trident.skill").m_130940_(ChatFormatting.GOLD));
        lines.add((Component)Component.m_237115_((String)"tooltip.ftboss.qingyi_confluence_trident.cancel").m_130940_(ChatFormatting.GRAY));
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private QingyiConfluenceTridentRenderer renderer;

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new QingyiConfluenceTridentRenderer();
                }
                return this.renderer;
            }
        });
    }
}

