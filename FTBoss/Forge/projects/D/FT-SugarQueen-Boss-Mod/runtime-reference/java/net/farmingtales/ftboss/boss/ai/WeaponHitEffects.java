/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.ArmorShredEffect;
import net.farmingtales.ftboss.effect.BloodRageFakeEffect;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.item.FTBossItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class WeaponHitEffects {
    private static boolean resolving = false;
    private static final Set<String> HIT_DIAG = ConcurrentHashMap.newKeySet();

    private WeaponHitEffects() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        block15: {
            if (resolving || e.getAmount() <= 0.0f || !e.getSource().m_276093_(DamageTypes.f_268464_) || e.getSource().m_7640_() != e.getSource().m_7639_()) {
                return;
            }
            try {
                Projectile pr;
                Entity src = e.getSource().m_7639_();
                if (src == null) {
                    src = e.getSource().m_7640_();
                }
                if (src instanceof Projectile && (pr = (Projectile)src).m_19749_() != null) {
                    src = pr.m_19749_();
                }
                if (!(src instanceof Player)) {
                    return;
                }
                Player p = (Player)src;
                LivingEntity target = e.getEntity();
                if (target == p) {
                    return;
                }
                if (p.m_9236_().f_46443_) {
                    return;
                }
                ItemStack main = p.m_21205_();
                if (main.m_41619_()) {
                    return;
                }
                ResourceLocation id = ForgeRegistries.ITEMS.getKey((Object)main.m_41720_());
                if (id == null) {
                    return;
                }
                if (HIT_DIAG.add(String.valueOf(id))) {
                    FTBossMod.LOGGER.info("[FTBoss][\u547d\u4e2d\u8bca\u65ad] \u73a9\u5bb6 {} \u7528\u4e3b\u624b {} \u547d\u4e2d {}\uff08\u9ed1\u5207={}\uff09", new Object[]{p.m_7755_().getString(), id, target.m_7755_().getString(), FTBossItems.BLACK_CLEAVER.getId().equals((Object)id)});
                }
                if (!FTBossItems.BLACK_CLEAVER.getId().equals((Object)id)) break block15;
                resolving = true;
                try {
                    BloodRageFakeEffect.onHit((LivingEntity)p);
                    ArmorShredEffect.onHit(target);
                    MobEffect eff = (MobEffect)FTEffects.BLOOD_RAGE_FAKE.get();
                    MobEffectInstance inst = eff == null ? null : p.m_21124_(eff);
                    int stacks = inst == null ? 1 : inst.m_19564_() + 1;
                    float raw = (float)p.m_21133_(Attributes.f_22281_);
                    double armor = target.m_21230_();
                    double tough = target.m_21133_(Attributes.f_22285_);
                    double reduction = Math.min(20.0, Math.max(armor / 5.0, armor - (double)raw / (2.0 + tough / 4.0))) / 25.0;
                    float pierced = (float)((double)raw * reduction * 0.5);
                    float trueDmg = raw * BloodRageFakeEffect.trueDamageRatio(stacks - 1);
                    float extra = pierced + trueDmg;
                    if (extra > 0.01f) {
                        target.f_19802_ = 0;
                        target.m_6469_(p.m_269291_().m_269104_((Entity)p, (Entity)p), extra);
                    }
                    FTBossMod.LOGGER.info("[FTBoss][\u9ed1\u5207] {} \u547d\u4e2d {} -> \u8840\u6012 {} \u5c42 / \u7834\u7532 / \u7a7f\u7532+\u771f\u4f24 {} \u70b9 (\u76ee\u6807\u62a4\u7532 {})", new Object[]{p.m_7755_().getString(), target.m_7755_().getString(), stacks, String.format("%.2f", Float.valueOf(extra)), (int)armor});
                }
                finally {
                    resolving = false;
                }
            }
            catch (Throwable t) {
                FTBossMod.LOGGER.warn("[FTBoss][\u9ed1\u5207] \u547d\u4e2d\u7279\u6548\u5f02\u5e38(\u5df2\u5ffd\u7565): {}", (Object)t.toString());
            }
        }
    }
}

