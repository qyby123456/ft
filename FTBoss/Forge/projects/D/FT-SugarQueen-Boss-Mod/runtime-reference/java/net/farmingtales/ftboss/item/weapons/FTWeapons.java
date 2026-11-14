/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.ExtraDebuffs;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class FTWeapons {
    static final int REFRESH = 40;
    static final int DURATION = 100;

    private FTWeapons() {
    }

    static MobEffect fx(String ns, String path) {
        return (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(ns, path));
    }

    static void apply(LivingEntity target, String ns, String path, int ticks, int amp) {
        MobEffect e = FTWeapons.fx(ns, path);
        if (e != null) {
            target.m_7292_(new MobEffectInstance(e, ticks, amp, false, false, true));
        }
    }

    public static class DogBeatingStick
    extends SwordItem {
        public static final float BONUS_VS_DOGS = 6.0f;
        public static final double SWEEP_RADIUS = 3.5;
        public static final float SWEEP_DAMAGE = 7.0f;
        public static final int SWEEP_COOLDOWN = 60;

        public DogBeatingStick() {
            super((Tier)FTWeaponTier.INSTANCE, 5, -2.2f, new Item.Properties());
        }

        private static boolean isCanine(LivingEntity e) {
            ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey((Object)e.m_6095_());
            if (key == null) {
                return false;
            }
            String p = key.m_135815_();
            return p.contains("wolf") || p.contains("dog") || p.contains("hound") || p.contains("fox") || p.contains("benger") || p.contains("jackal");
        }

        public boolean m_7579_(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            boolean hit = super.m_7579_(stack, target, attacker);
            if (hit && !target.m_9236_().f_46443_) {
                ExtraDebuffs.bleed(target, 160, 1);
                target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 60, 0, false, false, true));
                if (DogBeatingStick.isCanine(target)) {
                    target.f_19802_ = 0;
                    target.m_6469_(target.m_269291_().m_287172_(), 6.0f);
                    if (attacker instanceof Player) {
                        Player p = (Player)attacker;
                        p.m_5661_((Component)Component.m_237113_((String)"\u00a76\u6253\u72d7\u68d2 \u00a77\u4e13\u6cbb\u6076\u72ac\uff1a\u989d\u5916 6 \u70b9\u771f\u5b9e\u4f24\u5bb3"), true);
                    }
                }
            }
            return hit;
        }

        public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.m_21120_(hand);
            if (player.m_36335_().m_41519_((Item)this)) {
                return InteractionResultHolder.m_19100_((Object)stack);
            }
            player.m_36335_().m_41524_((Item)this, 60);
            if (level instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)level;
                int hits = 0;
                for (LivingEntity e : sl.m_45976_(LivingEntity.class, player.m_20191_().m_82400_(3.5))) {
                    Player other;
                    if (e == player || !e.m_6084_() || e instanceof Player && (other = (Player)e).m_7500_()) continue;
                    e.f_19802_ = 0;
                    e.m_6469_(player.m_269291_().m_269075_(player), 7.0f);
                    e.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 40, 0, false, false, true));
                    e.m_147240_(1.15, e.m_20185_() - player.m_20185_(), e.m_20189_() - player.m_20189_());
                    ++hits;
                }
                sl.m_8767_((ParticleOptions)ParticleTypes.f_123766_, player.m_20185_(), player.m_20186_() + 1.0, player.m_20189_(), 8, 1.6, 0.3, 1.6, 0.02);
                sl.m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_12317_, SoundSource.PLAYERS, 1.2f, 0.9f);
                FTBossMod.LOGGER.info("[FTBoss][\u6253\u72d7\u68d2] {} \u6a2a\u626b\u547d\u4e2d {} \u4e2a\u76ee\u6807", (Object)player.m_7755_().getString(), (Object)hits);
            }
            player.m_21011_(hand, true);
            return InteractionResultHolder.m_19092_((Object)stack, (boolean)level.m_5776_());
        }

        public void m_7373_(ItemStack stack, Level level, List<Component> tip, TooltipFlag flag) {
            tip.add((Component)Component.m_237113_((String)"\u00a76\u547d\u4e2d\uff1a\u6d41\u8840 II + \u7f13\u6162 I 3 \u79d2").m_130940_(ChatFormatting.GRAY));
            tip.add((Component)Component.m_237113_((String)"\u00a7d\u5bf9\u72ac\u7c7b\uff08\u72fc/\u72d7/\u72d0\u2026\uff09\u989d\u5916 6 \u70b9\u771f\u5b9e\u4f24\u5bb3"));
            tip.add((Component)Component.m_237113_((String)"\u00a77\u53f3\u952e\u6a2a\u626b\uff1a\u8eab\u5468 3.5 \u683c\u51fb\u98de + 7 \u4f24\u5bb3\uff08\u51b7\u5374 3 \u79d2\uff09"));
        }
    }

    public static class CheeseWheelMace
    extends DrinkingMace {
        public CheeseWheelMace() {
            super(8, -3.4f, new Item.Properties());
        }

        @Override
        protected int rotatingCount() {
            return 2;
        }

        public void m_7373_(ItemStack stack, Level level, List<Component> tip, TooltipFlag flag) {
            tip.add((Component)Component.m_237113_((String)"\u00a76\u4f4e\u653b\u901f\u91cd\u9524\uff08-3.4\uff09\uff0c\u672c\u6a21\u7ec4\u6700\u9ad8\u4f24\u5bb3").m_130940_(ChatFormatting.GRAY));
            tip.add((Component)Component.m_237113_((String)"\u00a7d\u624b\u6301\uff1a\u9189\u9152 + \u00a7f2 \u00a7d\u4e2a\u968f\u673a\u9152\u7c7b\u589e\u76ca\u8f6e\u66ff\uff08II \u7ea7\uff09"));
            tip.add((Component)Component.m_237113_((String)"\u00a7d\u547d\u4e2d\uff1a35% \u51e0\u7387\u8ba9\u76ee\u6807\u9189\u9152\uff08\u53cd\u80c3+\u8fdf\u7f13\uff09"));
        }
    }

    public static class WineBottleMace
    extends DrinkingMace {
        public WineBottleMace() {
            super(5, -3.2f, new Item.Properties());
        }

        @Override
        protected int rotatingCount() {
            return 3;
        }

        public void m_7373_(ItemStack stack, Level level, List<Component> tip, TooltipFlag flag) {
            tip.add((Component)Component.m_237113_((String)"\u00a76\u4f4e\u653b\u901f\u91cd\u9524\uff08-3.2\uff09").m_130940_(ChatFormatting.GRAY));
            tip.add((Component)Component.m_237113_((String)"\u00a7d\u624b\u6301\uff1a\u9189\u9152 + \u00a7f3 \u00a7d\u4e2a\u968f\u673a\u9152\u7c7b\u589e\u76ca\u8f6e\u66ff\uff08II \u7ea7\uff0c\u6bcf 8 \u79d2\u6362\u4e00\u6279\uff09"));
            tip.add((Component)Component.m_237113_((String)"\u00a7d\u547d\u4e2d\uff1a35% \u51e0\u7387\u8ba9\u76ee\u6807\u9189\u9152\uff08\u53cd\u80c3+\u8fdf\u7f13\uff09"));
        }
    }

    public static abstract class DrinkingMace
    extends SwordItem {
        public static final int ROTATE_TICKS = 160;
        public static final boolean INCLUDE_NEGATIVE = true;
        private static final String[] POOL_NS = new String[]{"brewinandchewin", "brewinandchewin", "brewinandchewin", "minecraft", "minecraft", "minecraft"};
        private static final String[] POOL_PATH = new String[]{"raging", "sweet_heart", "intoxication", "strength", "saturation", "luck"};

        protected DrinkingMace(int damageBonus, float speed, Item.Properties props) {
            super((Tier)FTWeaponTier.INSTANCE, damageBonus, speed, props);
        }

        protected abstract int rotatingCount();

        protected int buffAmplifier() {
            return 1;
        }

        public boolean m_7579_(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            boolean hit = super.m_7579_(stack, target, attacker);
            if (hit && !target.m_9236_().f_46443_ && target.m_217043_().m_188501_() < 0.35f) {
                target.m_7292_(new MobEffectInstance(MobEffects.f_19604_, 160, 0, false, false, true));
                target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 120, 0, false, false, true));
            }
            return hit;
        }

        public void m_6883_(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            super.m_6883_(stack, level, entity, slot, selected);
            if (level.f_46443_ || !selected || !(entity instanceof LivingEntity)) {
                return;
            }
            LivingEntity living = (LivingEntity)entity;
            if (living.f_19797_ % 40 != 0) {
                return;
            }
            FTWeapons.apply(living, "brewinandchewin", "tipsy", 100, 4);
            long phase = level.m_46467_() / 160L;
            Random rnd = new Random(living.m_20148_().getLeastSignificantBits() * 31L + phase * 2654435769L);
            ArrayList<Integer> idx = new ArrayList<Integer>();
            for (int i = 0; i < POOL_PATH.length; ++i) {
                idx.add(i);
            }
            Collections.shuffle(idx, rnd);
            int n = Math.min(this.rotatingCount(), idx.size());
            for (int k = 0; k < n; ++k) {
                MobEffect e;
                int i = (Integer)idx.get(k);
                boolean negative = POOL_PATH[i].equals("intoxication");
                if (negative) {
                    // empty if block
                }
                if ((e = FTWeapons.fx(POOL_NS[i], POOL_PATH[i])) == null) continue;
                living.m_7292_(new MobEffectInstance(e, 200, this.buffAmplifier(), false, false, true));
            }
        }
    }
}

