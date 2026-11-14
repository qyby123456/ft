import net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ManorAdaptiveDamageTest {
    private static int assertions;

    public static void main(String[] arguments) throws Exception {
        check(ManorAdaptiveDamage.class.getMethod("onHurt", LivingHurtEvent.class)
                .getAnnotation(SubscribeEvent.class).priority() == EventPriority.HIGH, "before shadow derived damage");
        Player player = new Player();
        EasterBossEntity boss = new EasterBossEntity(0);
        DamageSource attack = new DamageSource(boss, boss);
        near(hit(player, attack, 100), 110, "baseline");
        player.maxHealth = 60;
        for (int index = 0; index < 5; index++) player.effects.add(new MobEffectInstance(new MobEffect(true)));
        near(hit(player, attack, 100), 130, "health and buffs");
        for (int index = 0; index < 7; index++) player.effects.add(new MobEffectInstance(new MobEffect(false)));
        near(hit(player, attack, 100), 130, "negative and neutral effects excluded");
        player.maxHealth = 100;
        for (int index = 0; index < 5; index++) player.effects.add(new MobEffectInstance(new MobEffect(true)));
        near(hit(player, attack, 100), 150, "cap");
        for (int variant = 0; variant <= 10; variant++) {
            boss.variant = variant;
            float expected = switch (variant) { case 0, 1, 4, 6, 7 -> 150; default -> 100; };
            near(hit(player, attack, 100), expected, "variant scope");
        }
        boss.variant = 6;
        for (var segment : net.farmingtales.ftboss.util.DoraSniperDamageRules.TABLE) {
            DamageSource sniper = new DamageSource(boss, boss, segment.type());
            near(hit(player, sniper, segment.amount()), 240, "fixed sniper bypasses adaptive event");
            ManorAdaptiveDamage.hurtSkill(player, boss, sniper, segment.amount());
            near(player.lastDamage, 240, "fixed attributed sniper skill");
            ManorAdaptiveDamage.hurtSkill(player, boss, new DamageSource(null, null, segment.type()), segment.amount());
            near(player.lastDamage, 240, "fixed unowned sniper skill");
        }
        near(hit(player, new DamageSource(boss, boss, "dora_smg_round"), 100), 150, "only sniper types exempt");
        boss.mini = true;
        near(hit(player, attack, 100), 100, "mini excluded");
        boss.mini = false;
        player.creative = true;
        near(hit(player, attack, 100), 100, "creative excluded");
        player.creative = false;
        player.spectator = true;
        near(hit(player, attack, 100), 100, "spectator excluded");
        player.spectator = false;
        player.alive = false;
        near(hit(player, attack, 100), 100, "dead excluded");
        player.alive = true;
        player.world.isClientSide = true;
        near(hit(player, attack, 100), 100, "client excluded");
        player.world.isClientSide = false;
        near(hit(new LivingEntity(), attack, 100), 100, "non-player excluded");
        near(hit(player, new DamageSource(player, player), 100), 100, "player weapon excluded");
        near(hit(player, new DamageSource(new Entity(), null), 100), 100, "unrelated owner excluded");
        Projectile projectile = new Projectile(boss);
        near(hit(player, new DamageSource(boss, projectile), 100), 150, "normal projectile source");
        near(hit(player, new DamageSource(null, projectile), 100), 150, "direct projectile fallback");
        near(hit(player, new DamageSource(new Projectile(projectile), null), 100), 150, "nested projectile");
        Projectile orphan = new Projectile(null);
        near(hit(player, new DamageSource(orphan, orphan), 100), 100, "orphan projectile");
        orphan.owner = orphan;
        near(hit(player, new DamageSource(orphan, orphan), 100), 100, "self owner cycle");
        Projectile other = new Projectile(orphan);
        orphan.owner = other;
        near(hit(player, new DamageSource(orphan, null), 100), 100, "two owner cycle");
        DamageSource unowned = new DamageSource(null, null);
        near(hit(player, unowned, 100), 100, "environment damage unchanged");
        check(ManorAdaptiveDamage.hurtSkill(player, boss, unowned, 100), "unowned skill result");
        near(player.lastDamage, 150, "unowned skill scaled once");
        ManorAdaptiveDamage.hurtSkill(player, boss, attack, 100);
        near(player.lastDamage, 150, "attributed skill scaled once");
        ManorAdaptiveDamage.hurtSkill(player, player, unowned, 100);
        near(player.lastDamage, 100, "player unowned skill unchanged");
        near(hit(player, unowned, hit(player, attack, 100) * 0.5f), 75, "derived shadow extra not scaled twice");
        LivingHurtEvent canceled = new LivingHurtEvent(player, attack, 100);
        canceled.canceled = true;
        ManorAdaptiveDamage.onHurt(canceled);
        near(canceled.getAmount(), 100, "canceled event unchanged");
        player.effects.clear();
        player.maxHealth = 20;
        near(hit(player, attack, 100), 110, "removed buffs update immediately");
        near(hit(new Player(), attack, 100), 110, "independent second player");
        near(hit(player, attack, 0), 0, "zero damage");
        near(hit(player, attack, -1), -1, "negative damage");
        check(Float.isFinite(hit(player, attack, Float.MAX_VALUE)), "no overflow");
        System.out.println("ManorAdaptiveDamageTest: " + assertions + " contract checks passed (stub event runtime)");
    }

    private static float hit(LivingEntity target, DamageSource source, float amount) {
        LivingHurtEvent event = new LivingHurtEvent(target, source, amount);
        ManorAdaptiveDamage.onHurt(event);
        return event.getAmount();
    }

    private static void near(float actual, float expected, String message) {
        check(Math.abs(actual - expected) < 0.001f, message + ": " + actual);
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
}
