/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  software.bernie.geckolib.core.animation.AnimationState
 *  software.bernie.geckolib.core.animation.RawAnimation
 */
package net.farmingtales.ftboss.bengeranimation;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.bengeranimation.BengerSkillEffects;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

public final class BengerSkillSync {
    private static final String PREFIX = "benger|";
    private static final Map<BossActionState, Snapshot> STATES = new WeakHashMap<BossActionState, Snapshot>();
    private static final Map<String, RawAnimation> ANIMATIONS = new HashMap<String, RawAnimation>();
    private static final String[] BASICS = new String[]{"snap_right", "snap_left", "rake_left", "rake_right", "shoulder_check", "low_sweep"};

    public static Choice choose(int n, int n2, int n3, int n4, int n5, int n6, boolean bl, int n7, String string) {
        return switch (n) {
            case 1 -> {
                if (n3 > 0) {
                    yield new Choice("clamp_lock", n3);
                }
                yield new Choice("basic", n2);
            }
            case 2 -> new Choice("pack_howl", n2);
            case 3 -> new Choice(n7 > 0 ? "pack_howl" : "fear_roar", n2);
            case 4 -> new Choice("hurt", n2);
            case 7 -> {
                if (n4 > 0) {
                    yield new Choice("rush_charge", n4);
                }
                yield new Choice("pack_pounce", n2);
            }
            case 8 -> new Choice("wide_sweep", n2);
            case 9 -> new Choice("stun_bark", n2);
            case 10 -> new Choice("milk_recover", n2);
            case 0 -> {
                if (n4 > 0) {
                    yield new Choice("rush_charge", n4);
                }
                if (n3 > 0) {
                    yield new Choice("clamp_lock", n3);
                }
                if (n5 > 0) {
                    yield new Choice(bl ? "slam_air" : (string.equals("slam_land") ? "slam_land" : "slam_windup"), Math.max(1, n6));
                }
                yield new Choice("none", 0);
            }
            default -> new Choice("none", 0);
        };
    }

    public static synchronized void afterTick(BossActionState bossActionState2, LivingEntity livingEntity, int n, int n2, int n3, int n4, int n5, int n6, boolean bl, int n7, boolean bl2, boolean bl3, double d, double d2, double d3) {
        boolean bl4;
        Choice choice;
        if (bl2) {
            return;
        }
        Snapshot snapshot = STATES.computeIfAbsent(bossActionState2, bossActionState -> new Snapshot());
        Choice choice2 = choice = bl3 ? new Choice("none", 0) : BengerSkillSync.choose(n, n2, n3, n4, n5, n6, bl, n7, snapshot.role);
        if (choice.role().equals("none") && snapshot.role.equals("slam_land") && bossActionState2.active() && !bl3) {
            BengerSkillEffects.tick(livingEntity, "slam_land", n2, n3, n4, n6, (int)bossActionState2.elapsed(0.0f), snapshot.lastClamp, d, d2, d3);
            snapshot.lastClamp = n3;
            return;
        }
        boolean bl5 = bl4 = !choice.role().equals(snapshot.role) || choice.duration() > snapshot.remaining;
        if (bl4) {
            Object object;
            if (choice.role().equals("basic")) {
                object = "animation.benger.basic." + BASICS[snapshot.basicSequence];
                snapshot.basicSequence = (snapshot.basicSequence + 1) % BASICS.length;
            } else {
                object = choice.role().equals("hurt") ? "animation.benger.hurt" : "animation.benger.skill." + choice.role();
            }
            snapshot.key = object;
            bossActionState2.start((String)(choice.role().equals("none") ? "0" : PREFIX + (String)object), choice.duration());
        }
        BengerSkillEffects.tick(livingEntity, choice.role(), n2, n3, n4, n6, (int)bossActionState2.elapsed(0.0f), snapshot.lastClamp, d, d2, d3);
        snapshot.lastClamp = n3;
        snapshot.role = choice.role();
        snapshot.remaining = choice.duration();
    }

    public static synchronized void landed(BossActionState bossActionState2) {
        Snapshot snapshot = STATES.computeIfAbsent(bossActionState2, bossActionState -> new Snapshot());
        snapshot.role = "slam_land";
        snapshot.key = "animation.benger.skill.slam_land";
        snapshot.remaining = 22;
        bossActionState2.start(PREFIX + snapshot.key, 22);
    }

    public static int validClampTicks(int n, boolean bl, boolean bl2, float f) {
        return n > 0 && bl && !bl2 && f <= 6.0f ? n : 0;
    }

    public static synchronized void beforeClamp(BossActionState bossActionState2, LivingEntity livingEntity, int n, LivingEntity livingEntity2) {
        Snapshot snapshot = STATES.computeIfAbsent(bossActionState2, bossActionState -> new Snapshot());
        snapshot.lastClamp = livingEntity2 == null ? 0 : BengerSkillSync.validClampTicks(n, livingEntity2.m_6084_(), livingEntity2.m_5833_(), livingEntity.m_20270_((Entity)livingEntity2));
    }

    public static boolean apply(BossActionState bossActionState, AnimationState<?> animationState) {
        int n;
        Object object;
        animationState.getController().setAnimationSpeed(1.0);
        Object object2 = animationState.getAnimatable();
        if (object2 instanceof LivingEntity && (object = (LivingEntity)object2).m_21224_()) {
            return false;
        }
        object = bossActionState.name();
        if (!((String)object).startsWith(PREFIX)) {
            return false;
        }
        object2 = ((String)object).substring(PREFIX.length());
        if (((String)object2).contains(".basic.")) {
            n = 10;
        } else {
            switch (((String)object2).substring(((String)object2).lastIndexOf(46) + 1)) {
                case "pack_pounce": {
                    n = 14;
                    break;
                }
                case "slam_windup": 
                case "slam_air": 
                case "hurt": {
                    n = 8;
                    break;
                }
                case "slam_land": {
                    n = 22;
                    break;
                }
                case "clamp_lock": {
                    n = 60;
                    break;
                }
                case "rush_charge": {
                    n = 16;
                    break;
                }
                case "wide_sweep": {
                    n = 10;
                    break;
                }
                default: {
                    n = 20;
                }
            }
        }
        int n2 = n;
        animationState.getController().setAnimationSpeed((double)n2 / (double)Math.max(1, bossActionState.duration()));
        String string2 = ANIMATIONS.computeIfAbsent((String)object2, string -> RawAnimation.begin().thenPlay(string));
        animationState.setAnimation((RawAnimation)string2);
        return true;
    }

    public record Choice(String role, int duration) {
    }

    private static final class Snapshot {
        String role = "none";
        String key = "";
        int remaining;
        int basicSequence;
        int lastClamp;

        private Snapshot() {
        }
    }
}

