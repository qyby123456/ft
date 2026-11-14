package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import java.util.List;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.QizaiConstellation;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class QizaiIronSpells {
    public record Aim(List<Vec3> marks) implements ICastData {
        public Aim { marks = List.copyOf(marks); }
        @Override public void reset() {}
    }

    private QizaiIronSpells() {}

    public static boolean matches(Kind kind) { return kind == Kind.STAR_CROSS || kind == Kind.ROSE_STARFALL; }
    public static int skill(Kind kind) { return kind == Kind.STAR_CROSS ? QizaiRules.STAR_CROSS : QizaiRules.ROSE_STARFALL; }

    public static Aim aim(ServerLevel level, LivingEntity caster, Kind kind) {
        return new Aim(QizaiConstellation.marks(level, caster, skill(kind), BossSpellEffects.aim(caster)));
    }

    public static boolean castBoss(ServerLevel level, EasterBossEntity boss, int kind, List<Vec3> marks) {
        ManorSignatureSpell spell = ManorIronSpells.spell(kind == QizaiRules.STAR_CROSS ? Kind.STAR_CROSS : Kind.ROSE_STARFALL);
        if (!spell.isEnabled()) return false;
        MagicData data = new MagicData(true);
        data.setSyncedData(new SyncedSpellData(boss));
        data.initiateCast(spell, 1, 0, CastSource.MOB, "mainhand");
        data.setAdditionalCastData(new Aim(marks));
        spell.onCast(level, 1, boss, CastSource.MOB, data);
        return true;
    }

    public static void cast(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, MagicData data) {
        Aim aim = data != null && data.getAdditionalCastData() instanceof Aim locked ? locked
                : data != null && data.getAdditionalCastData() instanceof BossSignatureSpell.Aim point
                ? new Aim(QizaiConstellation.marks(level, caster, skill(spell.kind), point.point())) : aim(level, caster, spell.kind);
        float power = BossSpellTuning.playerPower(caster, BossSpellTuning.manorBoss(caster)
                ? ManorBossSpellProfile.power(spell.kind) : spell.getSpellPower(1, caster));
        if (!Float.isFinite(power) || power <= 0) return;
        QizaiConstellation.cast(level, caster, skill(spell.kind), aim.marks(), target -> {
            if (!BossSpellEffects.canTarget(caster, target)) return;
            if (caster instanceof EasterBossEntity && !(target instanceof net.minecraft.world.entity.player.Player)) return;
            target.invulnerableTime = 0;
            ManorSpellEffects.hurt(caster, target, spell, power);
        });
    }

    /**
     * 2026-09-20 v188 按用户要求「允许祁崽使用铁魔法防御」：抽一门**庄园辅助法术**（{@code Kind.support()}，
     * 护盾 / 治疗那一类，不属于它自己的变体）当场给自己这边放出来 —— 与主招共用同一条读条。
     *
     * <p>效果完全走该法术自己的 {@code onCast}（也就是 {@code ManorSpellEffects} 里那套支援分支），
     * 所以护盾值 / 治疗量 / 增益时长与别的庄园 Boss 放同一门法术时**完全一致**，
     * 只有半径会因为祁崽自己的 {@code radiusScale(11) = 2.0} 而翻倍。
     */
    public static boolean castSupport(ServerLevel level, EasterBossEntity boss, String spellId) {
        Kind kind = java.util.Arrays.stream(Kind.values())
                .filter(candidate -> candidate.id.equals(spellId) && candidate.support()
                        && candidate.variant != EasterBossEntity.QIZAI_VARIANT)
                .findFirst().orElse(null);
        if (kind == null) return false;
        ManorSignatureSpell spell = ManorIronSpells.spell(kind);
        if (spell == null || !spell.isEnabled()) return false;
        MagicData data = new MagicData(true);
        data.setSyncedData(new SyncedSpellData(boss));
        data.initiateCast(spell, 1, 0, CastSource.MOB, "offhand");
        /* ★ v191: 辅助法术的"结算中心"给**本体自己**。默认 {@code BossSpellEffects.aim(boss)}
           取的是它正看着的玩家（最多 24 格外），而光环型辅助（灶火盛宴 / 光环圣所）是围着中心
           给**友方**回血/上抗性 —— 中心落在玩家脚下时，等于把奶丢进敌人堆里，一点用都没有。 */
        data.setAdditionalCastData(new BossSignatureSpell.Aim(boss.position()));
        spell.onCast(level, 1, boss, CastSource.MOB, data);
        return true;
    }
}
