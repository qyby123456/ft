package net.farmingtales.ftboss.effect;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public class ShadowBlindEffect extends MobEffect {

    /** 一次施加给的默认时长(3 秒; 折耳根在交战中每 5 tick 会补一次) */
    public static final int TICKS = 60;
    /** 施加时给的等级(和原版失明 II 对齐, 只用于显示) */
    public static final int AMPLIFIER = 1;

    public ShadowBlindEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x140A1E);
    }

    @Override
    public boolean isBeneficial() {
        return true;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRemove(MobEffectEvent.Remove event) {
        if (!(event.getEntity() instanceof Player player) || !player.isAlive()) return;
        MobEffect effect = event.getEffect();
        if (effect != FTEffects.SHADOW_BLIND.get() && effect != MobEffects.BLINDNESS && effect != MobEffects.DARKNESS) return;
        var instance = player.getEffect(FTEffects.SHADOW_BLIND.get());
        if (instance != null && (instance.isInfiniteDuration() || instance.getDuration() > 0)) {
            event.setCanceled(true);
        }
    }

    public static void applyTo(LivingEntity target, int ticks) {
        if (target.level().isClientSide || !target.isAlive() || ticks == 0 || ticks < -1) return;
        if (target instanceof Player player) {
            player.addEffect(new MobEffectInstance(FTEffects.SHADOW_BLIND.get(), ticks, AMPLIFIER, false, true, true));
            synchronize(player);
        } else {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, ticks, AMPLIFIER));
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, ticks, 0));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!event.player.level().isClientSide && event.player.isAlive()) synchronize(event.player);
    }

    private static void synchronize(Player player) {
        MobEffectInstance shroud = player.getEffect(FTEffects.SHADOW_BLIND.get());
        if (shroud == null || !shroud.isInfiniteDuration() && shroud.getDuration() <= 0) return;
        companion(player, MobEffects.BLINDNESS, shroud.getDuration(), AMPLIFIER);
        companion(player, MobEffects.DARKNESS, shroud.getDuration(), 0);
    }

    private static void companion(Player player, MobEffect effect, int duration, int amplifier) {
        MobEffectInstance current = player.getEffect(effect);
        if (current != null && current.getAmplifier() >= amplifier
                && (current.isInfiniteDuration() || duration != -1 && current.getDuration() >= duration)) return;
        MobEffectInstance companion = new MobEffectInstance(effect, duration, amplifier, false, false, false);
        player.addEffect(companion);
    }

    /** 每 tick 都要跑(取消疾跑必须每 tick 检查) */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        /* 原版失明会让玩家不能疾跑 —— 我们这条效果自己补上同样的限制 */
        if (entity instanceof Player p && p.isSprinting()) {
            p.setSprinting(false);
        }
    }
}
