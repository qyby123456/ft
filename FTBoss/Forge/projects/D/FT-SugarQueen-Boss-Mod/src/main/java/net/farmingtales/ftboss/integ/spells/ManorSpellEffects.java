package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.util.SpellBeamModels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3f;

public final class ManorSpellEffects {
    private static final Map<Kind, DustParticleOptions> COLORS = new EnumMap<>(Kind.class);
    private static final Map<ServerLevel, List<Field>> FIELDS = new WeakHashMap<>();

    static {
        for (Kind kind : Kind.values()) COLORS.put(kind, new DustParticleOptions(new Vector3f(
                (kind.color >> 16 & 255) / 255F, (kind.color >> 8 & 255) / 255F, (kind.color & 255) / 255F), 1.2F));
    }

    private ManorSpellEffects() {}

    public static void warning(ServerLevel level, LivingEntity caster, Kind kind, Vec3 point) {
        if (ZicaiVoidSpells.warning(level, caster, kind, point)) return;
        if (!ManorSpellRules.validPoint(point.distanceToSqr(caster.position()))) return;
        SpellVisuals.show(level, caster, kind.id, Phase.CHARGE, point);
        ring(level, caster, kind, kind.support() || kind == Kind.ORBITAL_GEARS ? caster.position() : point, 2, 14);
        level.sendParticles(COLORS.get(kind), caster.getX(), caster.getY() + 1.2, caster.getZ(), 5, 0.3, 0.4, 0.3, 0);
    }

    public static void cast(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, int rank, Vec3 center) {
        if (ZicaiVoidSpells.cast(level, caster, spell, rank, center)) return;
        if (MaqueFeatherFlights.cast(level, caster, spell, rank, center)) return;
        if (caster instanceof EasterBossEntity boss && boss.isMini()
                && (!YouziCloneSpellProfile.isClone(boss) || !ManorSpellCombat.eligible(boss) || spell.kind.variant != 0)) return;
        if (!ManorSpellRules.validPoint(center.distanceToSqr(caster.position())) || !level.hasChunkAt(BlockPos.containing(center))) return;
        int spellLevel = ManorSpellRules.level(rank);
        boolean clone = YouziCloneSpellProfile.isClone(caster);
        /* 2026-09-19: 玩家用卷轴施法时数值 ×2（Boss/分身自己施法不受影响）。 */
        float damage = BossSpellTuning.playerPower(caster, clone ? YouziCloneSpellProfile.power(spell, caster)
                : BossSpellTuning.manorBoss(caster) ? ManorBossSpellProfile.power(spell.kind) : spell.getSpellPower(spellLevel, caster));
        if (!Float.isFinite(damage) || damage < 0) return;
        SpellVisuals.show(level, caster, spell.kind.id, Phase.RELEASE, center);
        if (BossSpellTuning.manorBoss(caster) && spell.kind == Kind.SHADOW_BRAND) {
            ring(level, caster, spell.kind, center, 3.5, 24);
            for (LivingEntity target : targets(level, caster, center, 3.5)) hurt(caster, target, spell, damage);
            return;
        }
        if (spell.kind.additional()) {
            if (spell.kind.support()) {
                ManorThirdSpellEffects.support(level, caster, spell, spellLevel);
            } else {
                List<Field> fields = FIELDS.computeIfAbsent(level, ignored -> new ArrayList<>());
                fields.removeIf(existing -> existing.caster().equals(caster.getUUID()) && existing.kind() == spell.kind);
                if (fields.size() >= ManorSpellRules.MAX_FIELDS) fields.remove(0);
                fields.add(new Field(caster.getUUID(), spell.kind, center, spellLevel, damage, level.getGameTime(),
                        ManorSparrowCasting.ownerOf(caster, spell.kind)));
            }
            return;
        }
        switch (spell.kind) {
            case EFFIGY_VOLLEY, ARCANE_FUSILLADE, CANDLE_PRISON, BREACH_SHELL, HEARTH_FEAST,
                    ELIXIR_RAIN, ORBITAL_GEARS, SHADOW_BRAND, VOID_ECHO, HALO_SANCTUARY, WHITEOUT_EDICT -> {
                Field field = new Field(caster.getUUID(), spell.kind, center, spellLevel, damage, level.getGameTime(),
                        ManorSparrowCasting.ownerOf(caster, spell.kind));
                List<Field> fields = FIELDS.computeIfAbsent(level, ignored -> new ArrayList<>());
                fields.removeIf(existing -> existing.caster().equals(caster.getUUID()) && existing.kind() == spell.kind);
                if (fields.size() >= ManorSpellRules.MAX_FIELDS) fields.remove(0);
                fields.add(field);
                pulse(level, caster, field, 0);
            }
            case EFFIGY_WARD -> {
                effect(caster, caster, MobEffects.ABSORPTION, clone ? YouziCloneSpellProfile.ABSORPTION_TICKS : 160 + spellLevel * 20,
                        clone ? 0 : (spellLevel - 1) / 2);
                effect(caster, caster, MobEffects.DAMAGE_RESISTANCE,
                        clone ? YouziCloneSpellProfile.RESISTANCE_TICKS : 100 + spellLevel * 20, 0);
                ring(level, caster, spell.kind, caster.position(), 2.5, 28);
            }
            case CRIMSON_OATH -> {
                effect(caster, caster, MobEffects.DAMAGE_BOOST, 100 + spellLevel * 20, spellLevel >= 4 ? 1 : 0);
                effect(caster, caster, MobEffects.DAMAGE_RESISTANCE, 80 + spellLevel * 20, 0);
                effect(caster, caster, MobEffects.MOVEMENT_SPEED, 80 + spellLevel * 20, 0);
                ring(level, caster, spell.kind, caster.position(), 2, 24);
            }
            case WINGSTEP -> {
                effect(caster, caster, MobEffects.MOVEMENT_SPEED, 100 + spellLevel * 20, 1);
                effect(caster, caster, MobEffects.JUMP, 100 + spellLevel * 20, 1);
                effect(caster, caster, MobEffects.SLOW_FALLING, 140 + spellLevel * 20, 0);
                level.sendParticles(ParticleTypes.CLOUD, caster.getX(), caster.getY() + 0.3, caster.getZ(), 24, 0.7, 0.3, 0.7, 0.04);
            }
            case CANDLE_REAP, RENDING_CLEAVE, SEARING_PAN, FOUR_TIDES -> cone(level, caster, spell, center, damage);
            case PRESSURE_LANCE -> ray(level, caster, spell, center, damage, 0.6, true, new HashSet<>());
            case FEATHER_FAN, FROST_NEEDLES -> {
                Vec3 direction = center.subtract(caster.getEyePosition()).normalize();
                int wings = spell.kind == Kind.FEATHER_FAN ? 2 : 1;
                Set<UUID> hit = new HashSet<>();
                for (int offset = -wings; offset <= wings; offset++) {
                    Vec3 end = caster.getEyePosition().add(direction.yRot(offset * 0.16F).scale(ManorSpellRules.RANGE));
                    ray(level, caster, spell, end, damage, 0.55, false, hit);
                }
            }
            case LUMINOUS_REBUKE -> {
                ring(level, caster, spell.kind, caster.position(), 5, 36);
                for (LivingEntity target : targets(level, caster, caster.position(), 5)) {
                    if (hurt(caster, target, spell, damage)) {
                        net.farmingtales.ftboss.util.BossKnockback.knockback(caster, target, 1.1, caster.getX() - target.getX(), caster.getZ() - target.getZ());
                        effect(caster, target, MobEffects.GLOWING, 100, 0);
                    }
                }
            }
        }
    }

    static boolean hurt(LivingEntity caster, LivingEntity target, ManorSignatureSpell spell, float damage) {
        if (!BossSpellEffects.canTarget(caster, target)) return false;
        boolean hit = BossSpellTuning.manorBoss(caster)
                ? ManorBossSpellProfile.hurt((EasterBossEntity) caster, target, spell, damage)
                : DamageSources.applyDamage(target, damage, spell.getDamageSource(caster));
        return MaqueFeatherCombat.afterHit(hit, caster, target, spell);
    }

    static void effect(LivingEntity caster, LivingEntity target, MobEffect effect, int ticks, int amplifier) {
        target.addEffect(new MobEffectInstance(effect, BossSpellTuning.buffTicks(caster, effect, ticks), amplifier), caster);
    }

    private static Vec3 clip(ServerLevel level, LivingEntity caster, Vec3 start, Vec3 end) {
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getLocation();
    }

    private static boolean visible(ServerLevel level, LivingEntity caster, Vec3 start, Vec3 end) {
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getType() == HitResult.Type.MISS;
    }

    static List<LivingEntity> targets(ServerLevel level, LivingEntity caster, Vec3 center, double radius) {
        double effectiveRadius = BossSpellTuning.radius(caster, radius);
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(effectiveRadius),
                target -> BossSpellEffects.canTarget(caster, target) && target.distanceToSqr(center) <= effectiveRadius * effectiveRadius
                        && caster.hasLineOfSight(target) && visible(level, caster, center.add(0, 0.3, 0), target.getBoundingBox().getCenter()))
                .stream().sorted(Comparator.comparingDouble(target -> target.distanceToSqr(center)))
                .limit(ManorSpellRules.MAX_TARGETS).toList();
    }

    private static void cone(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, Vec3 center, float damage) {
        Vec3 direction = center.subtract(caster.position()).multiply(1, 0, 1).normalize();
        if (direction.lengthSqr() < 0.01) direction = caster.getLookAngle().multiply(1, 0, 1).normalize();
        double baseRange = spell.kind == Kind.FOUR_TIDES ? 10 : 6;
        double scale = BossSpellTuning.radiusScale(caster);
        double range = baseRange * scale;
        for (int slice = 1; slice <= 4; slice++) {
            Vec3 middle = caster.position().add(direction.scale(range * slice / 4)).add(0, 0.8, 0);
            Vec3 side = new Vec3(-direction.z, 0, direction.x).scale(0.8 + range * slice / 4 * 0.7);
            beam(level, caster, spell.kind, middle.subtract(side), middle.add(side));
        }
        float stolen = 0;
        int outerHits = 0;
        for (LivingEntity target : targets(level, caster, caster.position(), baseRange + 1)) {
            Vec3 offset = target.position().subtract(caster.position());
            if (Math.abs(offset.y) > 3 * scale || !ManorSpellRules.cone(offset.dot(direction) / scale,
                    (offset.x * -direction.z + offset.z * direction.x) / scale, baseRange)) continue;
            float before = target.getHealth();
            if (!hurt(caster, target, spell, damage)) continue;
            if (BossSpellTuning.manorBoss(caster) && spell.kind == Kind.RENDING_CLEAVE
                    && offset.horizontalDistanceSqr() >= Math.pow(net.farmingtales.ftboss.util.GongdiCombatRules.INNER_RADIUS * scale, 2)) {
                outerHits++;
            }
            switch (spell.kind) {
                case CANDLE_REAP -> stolen += Math.max(0, before - target.getHealth()) * 0.15F;
                case RENDING_CLEAVE -> effect(caster, target, FTEffects.ARMOR_SHRED.get(), 60, 0);
                case SEARING_PAN -> { if (!BossSpellTuning.manorBoss(caster)) target.setSecondsOnFire(4); }
                case FOUR_TIDES -> {
                    net.farmingtales.ftboss.util.BossKnockback.knockback(caster, target, 0.9, -direction.x, -direction.z);
                    effect(caster, target, MobEffects.MOVEMENT_SLOWDOWN, 60, 1);
                    target.clearFire();
                }
                default -> { }
            }
        }
        if (stolen > 0) caster.heal(Math.min(6, stolen));
        if (BossSpellTuning.manorBoss(caster) && spell.kind == Kind.RENDING_CLEAVE) {
            ((EasterBossEntity) caster).finishSignatureCleave(outerHits);
        }
    }

    private static void ray(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, Vec3 center,
                            float damage, double width, boolean piercing, Set<UUID> hit) {
        Vec3 start = caster.getEyePosition();
        width = BossSpellTuning.radius(caster, width);
        Vec3 direction = center.subtract(start);
        if (direction.lengthSqr() < 0.001) return;
        Vec3 end = clip(level, caster, start, start.add(direction.normalize().scale(Math.min(ManorSpellRules.RANGE, direction.length() + 0.8))));
        Vec3 segment = end.subtract(start);
        if (segment.lengthSqr() < 0.001) return;
        Vec3 visualEnd = end;
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(width + 1),
                target -> BossSpellEffects.canTarget(caster, target)).stream()
                .sorted(Comparator.comparingDouble(caster::distanceToSqr)).toList();
        int hits = 0;
        for (LivingEntity target : candidates) {
            if (hit.contains(target.getUUID())) continue;
            AABB body = target.getBoundingBox().inflate(width);
            if (!body.contains(start) && body.clip(start, end).isEmpty()) continue;
            if (!caster.hasLineOfSight(target)) continue;
            hit.add(target.getUUID());
            if (hurt(caster, target, spell, damage)) {
                if (spell.kind == Kind.FROST_NEEDLES) {
                    target.setTicksFrozen(Math.max(target.getTicksFrozen(), 120));
                    effect(caster, target, MobEffects.MOVEMENT_SLOWDOWN, 80, 1);
                }
                if (spell.kind == Kind.FEATHER_FAN) effect(caster, target, MobEffects.GLOWING, 80, 0);
            }
            if (!piercing) {
                visualEnd = body.contains(start) ? start : body.clip(start, end).orElse(end);
                break;
            }
            if (++hits >= ManorSpellRules.MAX_TARGETS) break;
        }
        beam(level, caster, spell.kind, start, visualEnd);
    }

    private static void pulse(ServerLevel level, LivingEntity caster, Field field, int age) {
        ManorSignatureSpell spell = ManorIronSpells.spell(field.kind());
        if (field.kind().additional()) {
            ManorThirdSpellEffects.pulse(level, caster, spell, field.rank(), field.center(), field.damage(), age);
            return;
        }
        Vec3 center = field.kind() == Kind.ORBITAL_GEARS ? caster.position() : field.center();
        switch (field.kind()) {
            case EFFIGY_VOLLEY, ARCANE_FUSILLADE -> {
                if (ManorSpellRules.pulse(age, 12, field.kind() == Kind.EFFIGY_VOLLEY ? 3 : 4)) {
                    ray(level, caster, spell, center, field.damage(), field.kind() == Kind.EFFIGY_VOLLEY ? 0.9 : 0.35, false, new HashSet<>());
                }
            }
            case BREACH_SHELL -> {
                if (age < 20 && age % 5 == 0) ring(level, caster, field.kind(), center, 3.5, 20);
                if (age != 20) return;
                SpellVisuals.show(level, caster, field.kind().id, Phase.PULSE, center);
                ring(level, caster, field.kind(), center, 4, 32);
                level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 3, 0.3, 0.3, 0.3, 0);
                for (LivingEntity target : targets(level, caster, center, 4)) {
                    if (hurt(caster, target, spell, field.damage())) {
                        net.farmingtales.ftboss.util.BossKnockback.knockback(caster, target, 0.8, center.x - target.getX(), center.z - target.getZ());
                        effect(caster, target, MobEffects.WEAKNESS, 80, 0);
                    }
                }
            }
            case HEARTH_FEAST, HALO_SANCTUARY -> {
                if (!ManorSpellRules.pulse(age, 20, 4)) return;
                SpellVisuals.show(level, caster, field.kind().id, Phase.PULSE, center);
                ring(level, caster, field.kind(), center, 4, 28);
                for (LivingEntity ally : allies(level, caster, center)) {
                    ally.heal(field.kind() == Kind.HEARTH_FEAST ? 1 + field.rank() * 0.5F : 1);
                    effect(caster, ally, field.kind() == Kind.HEARTH_FEAST ? MobEffects.SATURATION : MobEffects.DAMAGE_RESISTANCE,
                            field.kind() == Kind.HEARTH_FEAST ? 1 : 40, 0);
                }
            }
            default -> {
                int interval = field.kind() == Kind.ORBITAL_GEARS ? 12 : field.kind() == Kind.VOID_ECHO ? 24 : 20;
                int count = field.kind() == Kind.VOID_ECHO ? 2 : field.kind() == Kind.SHADOW_BRAND ? 3 : field.kind() == Kind.CANDLE_PRISON ? 5 : 4;
                if (!ManorSpellRules.pulse(age, interval, count)) return;
                SpellVisuals.show(level, caster, field.kind().id, Phase.PULSE, center);
                double radius = field.kind() == Kind.ORBITAL_GEARS ? 3 : field.kind() == Kind.VOID_ECHO ? 2.5 : 3.5;
                ring(level, caster, field.kind(), center, radius, 24);
                for (LivingEntity target : targets(level, caster, center, radius)) {
                    if (!hurt(caster, target, spell, field.damage())) continue;
                    switch (field.kind()) {
                        case CANDLE_PRISON -> {
                            Vec3 pull = center.subtract(target.position()).multiply(1, 0, 1);
                            if (pull.lengthSqr() > 0.3) {
                                Vec3 impulse = pull.normalize().scale(0.25);
                                net.farmingtales.ftboss.util.BossKnockback.push(caster, target, impulse.x, 0, impulse.z);
                                target.hurtMarked = true;
                            }
                            effect(caster, target, MobEffects.MOVEMENT_SLOWDOWN, 35, 1);
                        }
                        case ELIXIR_RAIN -> {
                            effect(caster, target, MobEffects.POISON, 60, 0);
                            effect(caster, target, MobEffects.WEAKNESS, 60, 0);
                        }
                        case SHADOW_BRAND -> {
                            effect(caster, target, MobEffects.BLINDNESS, 40, 0);
                            effect(caster, target, MobEffects.MOVEMENT_SLOWDOWN, 40, 1);
                        }
                        case WHITEOUT_EDICT -> {
                            effect(caster, target, FTEffects.HEAL_BAN.get(), 60, 0);
                            effect(caster, target, MobEffects.MOVEMENT_SLOWDOWN, 40, 1);
                            target.setTicksFrozen(Math.max(target.getTicksFrozen(), 100));
                        }
                        default -> { }
                    }
                }
            }
        }
    }

    static List<LivingEntity> allies(ServerLevel level, LivingEntity caster, Vec3 center) {
        double radius = 4 * BossSpellTuning.radiusScale(caster);
        List<LivingEntity> result = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius),
                target -> target.isAlive() && !target.isSpectator() && !(target instanceof ArmorStand)
                        && target.distanceToSqr(center) <= radius * radius && caster.hasLineOfSight(target)
                        && (target == caster || SparrowBorrowedSpells.allied(caster, target) && SparrowBorrowedSpells.allied(target, caster)
                        || target instanceof OwnableEntity pet && caster.getUUID().equals(pet.getOwnerUUID()))));
        if (caster.distanceToSqr(center) <= radius * radius && !result.contains(caster)) result.add(0, caster);
        return result.stream().limit(ManorSpellRules.MAX_TARGETS).toList();
    }

    private static void ring(ServerLevel level, LivingEntity caster, Kind kind, Vec3 center, double radius, int points) {
        radius = BossSpellTuning.radius(caster, radius);
        for (int index = 0; index < points; index++) {
            double angle = index * Math.PI * 2 / points;
            level.sendParticles(COLORS.get(kind), center.x + Math.cos(angle) * radius, center.y + 0.15,
                    center.z + Math.sin(angle) * radius, 1, 0, 0.02, 0, 0);
        }
    }

    private static void beam(ServerLevel level, LivingEntity caster, Kind kind, Vec3 start, Vec3 end) {
        SpellVisuals.beam(level, caster, kind.id, start, end);
        if (SpellBeamModels.replaces(SpellVisualRules.find(kind.id), Phase.BEAM)) return;
        int count = Math.max(2, Math.min(32, (int) (start.distanceTo(end) * 2)));
        for (int index = 0; index <= count; index++) {
            Vec3 point = start.lerp(end, index / (double) count);
            level.sendParticles(COLORS.get(kind), point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        List<Field> fields = FIELDS.get(level);
        if (fields == null) return;
        fields.removeIf(field -> {
            long age = level.getGameTime() - field.started();
            Entity owner = level.getEntity(field.caster());
            if (age < 0 || age >= 100 || !(owner instanceof LivingEntity caster) || !caster.isAlive()) return true;
            /* ★ v191 修「借来的辅助法术是空的」：灶火盛宴(HEARTH_FEAST) / 光环圣所(HALO_SANCTUARY)
               这两门是**光环型**（靠 Field 每 20 tick 结算一次），而 Field 的存活判定里有两条
               专门针对"庄园 Boss 自己的招"的规矩：
                 ① 施法者必须 ManorSpellCombat.eligible —— 祁崽(variant 11) 被这条规则**排除**在外
                    （它走自己的 QizaiMagic，不进庄园施法轮换）；
                 ② 光环所属变体必须等于施法者变体 —— 祁崽是 11，借来的是 4/8，也不等。
               两条都不满足 → 光环在生成的下一 tick 就被 removeIf 丢掉，玩家只看得到起手那一下 RELEASE
               特效，回血/抗性一下都没有。这里是"用户点名允许它抽的辅助"，按借用身份放行这两条；
               其它一切照旧（时长 100 tick、区块、法术开关、距离 48 格仍然管着它）。 */
            boolean borrowed = caster instanceof EasterBossEntity borrowedBoss
                    && borrowedBoss.getVariant() == EasterBossEntity.QIZAI_VARIANT && field.kind().support();
            if (!borrowed && caster instanceof EasterBossEntity boss && (!ManorSpellCombat.eligible(boss)
                    || boss.getVariant() != field.kind().variant
                    && !ManorSparrowCasting.validField(level, boss, field.kind(), field.delegator()))) return true;
            if (caster instanceof Player player && player.isSpectator()
                    || caster.distanceToSqr(field.center()) > 48 * 48 || !level.hasChunkAt(BlockPos.containing(field.center()))
                    || !ManorIronSpells.spell(field.kind()).isEnabled()) return true;
            if (age > 0) pulse(level, caster, field, (int) age);
            return false;
        });
        if (fields.isEmpty()) FIELDS.remove(level);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { FIELDS.remove(event.getLevel()); }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { FIELDS.clear(); }

    private record Field(UUID caster, Kind kind, Vec3 center, int rank, float damage, long started, UUID delegator) {}
}
