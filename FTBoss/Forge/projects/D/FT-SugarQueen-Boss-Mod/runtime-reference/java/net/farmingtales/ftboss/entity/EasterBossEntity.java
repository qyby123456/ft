/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.IntArrayTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LightningBolt
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobType
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.control.MoveControl
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.ThrownPotion
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.alchemy.PotionUtils
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.common.ForgeMod
 *  net.minecraftforge.common.util.FakePlayer
 *  net.minecraftforge.event.ForgeEventFactory
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.server.ServerLifecycleHooks
 */
package net.farmingtales.ftboss.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.animation.qingyicast.QingyiCastAccess;
import net.farmingtales.ftboss.animation.qingyicast.QingyiCastSync;
import net.farmingtales.ftboss.animation.zicaicast.ZicaiCastAccess;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.balance.ManorExpansionHooks;
import net.farmingtales.ftboss.body.BodyCueAccess;
import net.farmingtales.ftboss.body.BodySignals;
import net.farmingtales.ftboss.boss.ArmorShred;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.BossText;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ImpossibleSparrowSupport;
import net.farmingtales.ftboss.boss.IronSpellBridge;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.boss.SparrowSupport;
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.BossApproach;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.BossDamageTuning;
import net.farmingtales.ftboss.boss.ai.BossHitFeedback;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.BossWalkingSpeed;
import net.farmingtales.ftboss.boss.ai.EnderVortex;
import net.farmingtales.ftboss.boss.ai.ExtraDebuffs;
import net.farmingtales.ftboss.boss.ai.GongdiCastLifecycle;
import net.farmingtales.ftboss.boss.ai.GongdiSkills;
import net.farmingtales.ftboss.boss.ai.LemonImmuneBuffRules;
import net.farmingtales.ftboss.boss.ai.LemonInheritedAbilityRules;
import net.farmingtales.ftboss.boss.ai.LemonSacrifice;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage;
import net.farmingtales.ftboss.boss.ai.ManorBuffCopy;
import net.farmingtales.ftboss.boss.ai.ManorCombatContinuity;
import net.farmingtales.ftboss.boss.ai.ManorCombatLanes;
import net.farmingtales.ftboss.boss.ai.ManorDefense;
import net.farmingtales.ftboss.boss.ai.ManorFinalDamage;
import net.farmingtales.ftboss.boss.ai.ManorFinaleCombat;
import net.farmingtales.ftboss.boss.ai.ManorFiveSkills;
import net.farmingtales.ftboss.boss.ai.ManorGroundSupport;
import net.farmingtales.ftboss.boss.ai.ManorKiting;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.boss.ai.ManorRaidCombat;
import net.farmingtales.ftboss.boss.ai.ManorStatusImmunity;
import net.farmingtales.ftboss.boss.ai.QizaiMagic;
import net.farmingtales.ftboss.boss.ai.ShadowVoidSplit;
import net.farmingtales.ftboss.boss.ai.SparrowDisruption;
import net.farmingtales.ftboss.boss.ai.SparrowHitEvasion;
import net.farmingtales.ftboss.boss.ai.XuanlanEncounterLimits;
import net.farmingtales.ftboss.boss.ai.XuanlanSkyStrike;
import net.farmingtales.ftboss.boss.ai.XueliCrushDamage;
import net.farmingtales.ftboss.boss.ai.XueliMaids;
import net.farmingtales.ftboss.combat.dorabasic.DoraBasicCue;
import net.farmingtales.ftboss.combat.xueli.XueliCombatServer;
import net.farmingtales.ftboss.dandanblink.BlinkTargets;
import net.farmingtales.ftboss.dandanmotion.MotionAccess;
import net.farmingtales.ftboss.dandanmotion.MotionServer;
import net.farmingtales.ftboss.dandannativelights.LightHooks;
import net.farmingtales.ftboss.dandanspeed.SpeedRules;
import net.farmingtales.ftboss.doramotion.DoraMotionAccess;
import net.farmingtales.ftboss.doramotion.DoraMotionSync;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.effect.ShadowBlindEffect;
import net.farmingtales.ftboss.entity.BossBuffImmunity;
import net.farmingtales.ftboss.entity.EasterPearlEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.farmingtales.ftboss.entity.ManorBalanceAccess;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.QingyiTridentEntity;
import net.farmingtales.ftboss.entity.ShadowPhantomEntity;
import net.farmingtales.ftboss.entity.StockpotEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.XuanlanEliteFlywheels;
import net.farmingtales.ftboss.entity.XueliKnifeEntity;
import net.farmingtales.ftboss.entity.ZicaiMeleePassive;
import net.farmingtales.ftboss.entity.ZicaiTargetIsolation;
import net.farmingtales.ftboss.integ.ManorSpellBridge;
import net.farmingtales.ftboss.integ.spells.SparrowBorrowedSpells;
import net.farmingtales.ftboss.item.weapons.QingyiConfluenceTridentItem;
import net.farmingtales.ftboss.lemonmotion.LemonMotionAccess;
import net.farmingtales.ftboss.lemonmotion.LemonMotionServer;
import net.farmingtales.ftboss.lemonmovement.LemonMovementRules;
import net.farmingtales.ftboss.lemonparty.LemonPartyRules;
import net.farmingtales.ftboss.lemonritual.RitualNetwork;
import net.farmingtales.ftboss.maque.support.MaqueSupportHooks;
import net.farmingtales.ftboss.qingyi.multiplayer.MultiplayerHooks;
import net.farmingtales.ftboss.skill.DoraSniperDamage;
import net.farmingtales.ftboss.skill.QingyiConfluenceSkill;
import net.farmingtales.ftboss.skill.QizaiArsenal;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.skill.qizaicrossguard.CrossGuard;
import net.farmingtales.ftboss.skill.xuanlannative.NativeSignals;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.turret.ZhuniStage;
import net.farmingtales.ftboss.util.BossKnockback;
import net.farmingtales.ftboss.util.DoraCombatTempo;
import net.farmingtales.ftboss.util.DoraSniperCycle;
import net.farmingtales.ftboss.util.EasterCombatMath;
import net.farmingtales.ftboss.util.FlywheelCombatRules;
import net.farmingtales.ftboss.util.Fx;
import net.farmingtales.ftboss.util.GongdiCombatRules;
import net.farmingtales.ftboss.util.HiddenBossCastRules;
import net.farmingtales.ftboss.util.ManorArmorBalanceRules;
import net.farmingtales.ftboss.util.ManorBaseMovementSpeed;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.farmingtales.ftboss.util.ManorCombatRules;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.util.ManorMeleeTiming;
import net.farmingtales.ftboss.util.ManorRefinementRules;
import net.farmingtales.ftboss.util.ManorSkillDamageRules;
import net.farmingtales.ftboss.util.PlayerBuffRules;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.farmingtales.ftboss.util.XuanlanLengths;
import net.farmingtales.ftboss.util.XueliDialogueState;
import net.farmingtales.ftboss.util.YouziMiniRules;
import net.farmingtales.ftboss.util.YouziSummonRules;
import net.farmingtales.ftboss.util.ZicaiCombatRules;
import net.farmingtales.ftboss.util.ZicaiDebuffRules;
import net.farmingtales.ftboss.xueli_basic.BasicAccess;
import net.farmingtales.ftboss.xueli_basic.BasicServer;
import net.farmingtales.ftboss.xueli_cast_actions.CastAccess;
import net.farmingtales.ftboss.xueli_cast_actions.CastServer;
import net.farmingtales.ftboss.xueli_multiplayer.CadenceServer;
import net.farmingtales.ftboss.youzi.YouziCombatEvents;
import net.farmingtales.ftboss.youzi.YouziMiniBalance;
import net.farmingtales.ftboss.youzi.YouziSummonScaling;
import net.farmingtales.ftboss.youzi.motion.YouziBodyNetwork;
import net.farmingtales.ftboss.zheergennightfall.NightfallAccess;
import net.farmingtales.ftboss.zheergenphantom.CastReadability;
import net.farmingtales.ftboss.zheergenphantom.ErosionRules;
import net.farmingtales.ftboss.zheergenphantom.PhantomAccess;
import net.farmingtales.ftboss.zheergenphantom.PhantomHooks;
import net.farmingtales.ftboss.zheergenphantom.PhantomSnapshot;
import net.farmingtales.ftboss.zheergenphantom.PhantomTimeline;
import net.farmingtales.ftboss.zheergenremainingbody.SkillBodyDispatch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

public class EasterBossEntity
extends Monster
implements ZicaiCastAccess,
LemonMotionAccess,
BodyCueAccess,
MotionAccess,
DoraMotionAccess,
CastAccess,
NightfallAccess,
QingyiCastAccess,
net.farmingtales.ftboss.qingyi_basic.BasicAccess,
BasicAccess,
PhantomAccess,
ManorBalanceAccess {
    public static final int COUNT = 12;
    public static final int QIZAI_VARIANT = 11;
    private final QizaiMagic qizaiMagic = new QizaiMagic(this);
    private final QizaiArsenal qizaiArsenal = new QizaiArsenal(this);
    public static final int[] MANOR_VARIANTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11};
    public static final int MANOR_COUNT = MANOR_VARIANTS.length;
    public static final boolean MANOR_FIRST_VARIANT_ALLOWED = true;
    public static final int SPARROW_VARIANT = 9;
    public static final int XUELI_VARIANT = 10;
    public static final int DANDAN_VARIANT = 1;
    public static final int ZICAI_VARIANT = 8;
    public static final int ZHEERGEN_VARIANT = 7;
    private static final int DANDAN_WEAPON_MODEL_DATA = 91601;
    public static final int LEMONCAT_VARIANT = 4;
    public static final float LEMON_HEAL_MULTIPLIER = 0.6666667f;
    public static final int QINGYI_VARIANT = 5;
    public static final int LEMONCAT_WEAPON_MODEL_DATA = 160416;
    private static final String LEMONCAT_OFFHAND_ID = "kaleidoscope_cookery:kitchen_shovel";
    public static final double WEAPON_DAMAGE_MULT = 5.0;
    public static final double SKILL_MULT = 3.0;
    public static final double SKILL_RADIUS_MULT = 1.5;
    public static final double BOSS_DAMAGE_MULT = 0.6666666666666666;
    public static final int CHAIN_BUFF_CD = 40;
    private static final MobEffect[] CHAIN_BUFF_POOL = new MobEffect[]{MobEffects.f_19596_, MobEffects.f_19600_, MobEffects.f_19606_, MobEffects.f_19605_, MobEffects.f_19598_, MobEffects.f_19603_, MobEffects.f_19617_, MobEffects.f_19607_, MobEffects.f_19608_, MobEffects.f_19618_, MobEffects.f_19616_, MobEffects.f_19621_, MobEffects.f_19611_, MobEffects.f_19592_, MobEffects.f_19593_, MobEffects.f_19591_};
    private static final MobEffect[] RANDOM_DEBUFF_POOL = new MobEffect[]{MobEffects.f_19613_, MobEffects.f_19597_, MobEffects.f_19599_, MobEffects.f_19614_, MobEffects.f_19615_, MobEffects.f_19612_, MobEffects.f_19590_, MobEffects.f_19619_};
    public static final int INNATE_BUFF_TICKS = 100;
    public static final int INNATE_BUFF_DURATION = 200;
    public static final MobEffect[] INNATE_EFFECTS = new MobEffect[]{MobEffects.f_19596_, MobEffects.f_19606_, MobEffects.f_19605_, MobEffects.f_19598_, MobEffects.f_19603_, MobEffects.f_19600_};
    public static final int NO_TARGET_GRAB_TICKS = 200;
    public static final double NO_TARGET_GRAB_RANGE = 256.0;
    public static final String NO_TARGET_TEXT = "\u54fc\uff0c\u60f3\u9003\uff1f";
    public static final double FLYWHEEL_ATTACK_RANGE = 24.0;
    public static final double BURST_RANGE = 18.0;
    public static final double PEARL_NEAR = 4.0;
    public static final int PEARL_TRIGGER_TICKS = 20;
    public static final int PURGE_DELAY_TICKS = 1200;
    public static final int BREAK_INTERVAL = 60;
    public static final int FOOD_BUFF_INTERVAL = 200;
    public static final int QINGYI_POTION_DEBUFFS = 6;
    public static final boolean[] RANGED = new boolean[]{false, false, true, false, false, false, true, false, false, false, false, true};
    public static final double RANGED_MOVEMENT_SPEED = 0.165;
    public static final int BLOOD_RAGE_MAX = 5;
    public static final double BLOOD_RAGE_PER_STACK = 0.5;
    public static final int BLOOD_RAGE_TICKS = 160;
    public static final double BLOOD_RAGE_LIFESTEAL = 0.15;
    public static final double BLOOD_RAGE_TRUE_DAMAGE = 0.1;
    private static final UUID BLOOD_RAGE_MOD = UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000b2");
    public static final double BLOOD_RAGE_MAX_SPEED_BONUS = 0.4;
    private static final UUID BLOOD_RAGE_SPEED_MOD = UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000b3");
    public static final String[] NAMES = new String[]{"\u67da\u5b50", "\u86cb\u86cb", "\u6735\u62c9", "\u529f\u5e95", "\u67e0\u6aac\u732b", "\u9752\u8863", "\u6587\u7384\u84dd", "\u6298\u8033\u6839", "\u7d2b\u83dc", "\u9ebb\u96c0", "\u96ea\u9ece", "\u7941\u5d3d"};
    public static final String[] SKINS = new String[]{"youzi", "dandan", "duola", "gongdi", "ningmengmao", "qingyi", "wenxuanlan", "zheergeng", "zicai", "maque", "xueli", "qizai"};
    public static final String[] IDS = new String[]{"youzi", "dandan", "duola", "gongdi", "ningmengmao", "qingyi", "wenxuanlan", "zheergeng", "zicai", "maque", "xueli", "qizai"};
    public static final String[] WEAPONS = new String[]{"ftboss:statue_youzi", "minecraft:end_rod", "bren:machine_gun", "ftboss:black_cleaver", "farmersdelight:skillet", "ftboss:qingyi_confluence_trident", "create:flywheel", "ftboss:shadow_sword", "ftboss:puffer_bat", "ftboss:maque_blessing_feather", "farmersdelight:skillet", "ftboss:qizai_prayer_staff"};
    public static final boolean[][] EXTRA = new boolean[][]{{false, false, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, false}};
    public static final int[] STYLES = new int[]{1, 0, 1, 0, 2, 2, 2, 2, 1, 2, 2, 1};
    public static final boolean[] CASTER = new boolean[]{false, false, false, false, false, false, false, true, false, false, false, true};
    public static final int MINI_LIFE_TICKS = 300;
    public static final int MINI_CAP = 6;
    public static final int YOUZI_HITS_PER_SUMMON = 3;
    public static final double MINI_HEALTH = 150.0;
    public static final float YOUZI_MINION_SHARE = 0.1f;
    public static final float MINI_SCALE = 0.55f;
    public static final int MINI_SUMMON_CD = 40;
    public static final int BURST_SHOTS = 32;
    public static final int BURST_INTERVAL = 3;
    public static final double BURST_RATE_MULT = 2.0;
    public static final float BURST_DAMAGE_PHYS = 5.3333335f;
    public static final float BURST_DAMAGE_MAGIC = 5.3333335f;
    public static final float BURST_DAMAGE = 10.666667f;
    public static final double BURST_SPREAD_NEAR = 0.42;
    public static final double BURST_SPREAD_FAR = 0.78;
    public static final int CLEAVE_WINDUP = HiddenBossCastRules.quicker(30);
    public static final double CLEAVE_RADIUS = 11.25;
    public static final int PAN_WINDUP = HiddenBossCastRules.quicker(15);
    public static final double PAN_RADIUS = 6.0;
    public static final float PAN_DAMAGE = 100.8f;
    public static final int SUMMON_LIFE_TICKS = 1800;
    public static final double SUMMON_EXTRA_HEALTH = 100.0;
    public static final int SUMMON_EXTRA_BUFFS = 5;
    public static final String[] FOOD_BOSS_IDS = new String[]{"ftboss:sugar_queen", "ftboss:meat_mountain", "ftboss:tea_tree", "ftboss:wine_barrel", "ftboss:rat_boss"};
    public static final int XUELI_CRUSH_TICKS = 60;
    public static final float XUELI_CRUSH_DAMAGE = 5.0f;
    public static final float XUELI_POT_DAMAGE = 40.0f;
    public static final String XUELI_MELEE_ID = "bakery:rolling_pin";
    public static final float XUELI_ROLLING_PIN_QUOTE_CHANCE = 0.2f;
    public static final int XUELI_ROLLING_PIN_QUOTE_COOLDOWN = 100;
    private long xueliRollingPinQuoteReadyAt = 0L;
    private final XueliDialogueState xueliDialogue = new XueliDialogueState();
    public static final String XUELI_POT_ID = "kaleidoscope_cookery:stockpot";
    private final Map<UUID, Integer> crushTicks = new HashMap<UUID, Integer>();
    private int xueliNext = 3;
    private int bombFuse = 0;
    private boolean bombBlown = false;
    private final XueliMaids xueliMaids = new XueliMaids(this);
    public static final double XUELI_PAN_RADIUS = 4.5;
    public static final float XUELI_PAN_DAMAGE = 30.0f;
    public static final int XUELI_ROLL_TICKS = 30;
    public static final float XUELI_ROLL_DAMAGE = 12.0f;
    public static final int XUELI_ROLL_HIT_CD = 6;
    private int xueliRollTicks = 0;
    private int xueliRollHitCd = 0;
    public static final double XUELI_SLAM_HEIGHT = 12.0;
    public static final int XUELI_SLAM_WARN_TICKS = HiddenBossCastRules.quicker(20);
    public static final double XUELI_SLAM_RADIUS = 8.0;
    public static final float XUELI_SLAM_DAMAGE = 40.0f;
    public static final int XUELI_SLAM_STUN_TICKS = 20;
    public static final int XUELI_SLAM_COMBO = 5;
    public static final int XUELI_SLAM_GAP_TICKS = 20;
    private int xueliSlamWarn = 0;
    private int xueliSlamLeft = 0;
    private int xueliSlamGap = 0;
    private double xueliSlamX;
    private double xueliSlamY;
    private double xueliSlamZ;
    private final Map<UUID, Integer> stunTicks = new HashMap<UUID, Integer>();
    private final Map<UUID, double[]> stunAnchor = new HashMap<UUID, double[]>();
    private final Map<UUID, double[]> stunLaunch = new HashMap<UUID, double[]>();
    public static final String[] QINGYI_WEAPONS = new String[]{"tridents_n_stuff:stormseeker", "tridents_n_stuff:the_desecrator", "tridents_n_stuff:echo_of_the_deep", "tridents_n_stuff:stellaris"};
    public static final String[] QINGYI_LIGHTNING_SPELLS = new String[]{"irons_spellbooks:chain_lightning", "irons_spellbooks:lightning_lance", "irons_spellbooks:thunderstorm", "irons_spellbooks:lightning_bolt"};
    public static final int QINGYI_SPELL_CD = 80;
    public static final int QINGYI_SPELL_LVL_MIN = 5;
    public static final int QINGYI_SPELL_LVL_MAX = 7;
    public static final float QINGYI_SPELL_FALLBACK_DAMAGE = 64.8f;
    private int qingyiSpellCd = 80;
    public static final int QINGYI_SWAP_TICKS = 160;
    public static final int QINGYI_TRIDENT_STREAK = 5;
    public static final int QINGYI_RAIN_COUNT = 12;
    public static final double QINGYI_RIPTIDE_RADIUS = 3.75;
    public static final double QINGYI_CD_MULT = 0.3;
    public static final int QINGYI_STELLARIS_INDEX = 3;
    public static final double QINGYI_DAMAGE_MULT = 2.25;
    public static final double QINGYI_ATK_BONUS = 1.0;
    public static final double QINGYI_ARMOR_BONUS = 8.0;
    public static final double QINGYI_DAMAGE_TAKEN_MULT = 0.7;
    private static final UUID QINGYI_FLAT_ATK_MOD = UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000d1");
    private static final UUID QINGYI_FLAT_ARMOR_MOD = UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000d2");
    public static final double QINGYI_STACK_DAMAGE = 1.0;
    public static final double QINGYI_STACK_ARMOR = 1.0;
    public static final double QINGYI_STACK_HEALTH = 1.0;
    public static final int QINGYI_MAX_STACKS = 200;
    public static final int QINGYI_MAX_ARMOR_STACKS = 20;
    public static final int QINGYI_MAX_HEALTH_STACKS = 20;
    public static final int QINGYI_ONSUMMON_BUFFS = 6;
    public static final int QINGYI_ONSUMMON_TICKS = 600;
    public static final int QINGYI_CHARGE_MAX_AMP = 4;
    private static final UUID QINGYI_ATK_MOD = UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000c1");
    private static final UUID QINGYI_ARMOR_MOD = UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000c2");
    private static final UUID QINGYI_HEALTH_MOD = UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000c3");
    public static final double GRIND_RADIUS = 6.25;
    public static final float GRIND_DAMAGE = 24.0f;
    public static final double VENT_RANGE = 10.75;
    public static final float VENT_DAMAGE = 80.0f;
    public static final int FLYWHEEL_SHOTS = 6;
    public static final int FLYWHEEL_INTERVAL = 8;
    public static final int FLYWHEEL_VOLLEY_TICKS = 400;
    private int flywheelVolleyCd = 400;
    private int openingFlywheelsGranted;
    private int openingFlywheelsRetry;
    public static final double BEAM_COMBO_MIN = 8.0;
    public static final double BEAM_COMBO_MAX = 16.0;
    public static final float BEAM_STRIKE_DAMAGE = 60.0f;
    public static final double BEAM_STRIKE_RADIUS = 2.6;
    public static final double FLYWHEEL_DAMAGE_STEP = 0.1;
    public static final double FLYWHEEL_DAMAGE_CAP = 5.0;
    public static final double XUANLAN_DAMAGE_MULT = 4.19;
    public static final double XUANLAN_ATK_BONUS = 0.3;
    private static final UUID XUANLAN_ATK_MOD = UUID.fromString("6f1a2d3c-2222-4a2b-9c3d-0000000000c6");
    public static final double MANOR_ATK_BONUS = 0.25;
    public static final int GONGDI_VARIANT = 3;
    private static final UUID MANOR_ATK_MOD = UUID.fromString("6f1a2d3c-3333-4a2b-9c3d-0000000000d7");
    public static final double VENT_KNOCKBACK = 0.98;
    public static final double BEAM_KNOCKBACK = 0.63;
    public static final double XUANLAN_BEAM_KNOCKBACK = 1.5;
    public static final double XUANLAN_BEAM_LIFT = 0.35;
    public static final int XUANLAN_BEAM_CD = 320;
    public static final int XUANLAN_BEAM_LOCK = 20;
    public static final int XUANLAN_BEAM_CHARGE = 20;
    public static final int XUANLAN_BEAM_FIRE = 40;
    public static final int XUANLAN_BEAM_PULSE = 10;
    public static final int XUANLAN_BEAM_STUN = 60;
    public static final double XUANLAN_BEAM_LEN = 80.0;
    public static final double XUANLAN_BEAM_RADIUS = 4.0;
    private int xuanlanBeamCd = 160;
    private final XuanlanSkyStrike xuanlanSky = new XuanlanSkyStrike(this);
    private int xuanlanBeamLock = 0;
    private int xuanlanBeamCharge = 0;
    private int xuanlanBeamFire = 0;
    private int xuanlanBeamPulse = 0;
    private double xlBeamDx = 0.0;
    private double xlBeamDy = 0.0;
    private double xlBeamDz = 1.0;
    private final Set<UUID> xlBeamPunished = new HashSet<UUID>();
    public static final int DORA_VARIANT = 2;
    public static final String DORA_SMG_ID = "ftboss:dora_nova_launcher";
    public static final String DORA_FOCUS_ID = "bren:machine_gun";
    public static final int RELOAD_TICKS = 60;
    public static final String DUOLA_MELEE_ID = "ftboss:dora_lance";
    public static final float SPEAR_CHARGE_DAMAGE = 6.6666665f;
    public static final double SPEAR_KNOCKBACK = 1.25;
    public static final double SPEAR_KNOCKBACK_UP = 0.3;
    public static final double BURST_KNOCKBACK = 0.55;
    public static final double BURST_KNOCKBACK_UP = 0.1;
    public static final float SHADOW_DAMAGE = 112.0f;
    public static final float SHADOW_VOID_DAMAGE = 10.0f;
    public static final double SHADOW_DAMAGE_CUT = 0.7;
    public static final double SHADOW_VOID_HALF = 0.5;
    public static final int SHADOW_DARKNESS_TICKS = 300;
    public static final int SHADOW_DARKNESS_ON_HIT = 300;
    public static final double POISON_RADIUS = 6.75;
    public static final float POISON_DAMAGE = 56.0f;
    public static final float ENDER_SWAP_DAMAGE = 64.0f;
    public static final float DANDAN_RUSH_DAMAGE = 28.0f;
    public static final int DANDAN_RUSH_HITS = 6;
    public static final int DANDAN_RUSH_INTERVAL = 5;
    private int dandanRush = 0;
    private int dandanRushTick = 0;
    public static final float YOUZI_BURST_DAMAGE = 72.0f;
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Boolean> DATA_MINI = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Boolean> DATA_IMPOSSIBLE = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Boolean> DATA_GIANT = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<CompoundTag> DATA_COMBAT_ANIMATION = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private static final EntityDataAccessor<CompoundTag> DATA_SKILL_CUE = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private static final EntityDataAccessor<CompoundTag> DATA_QIZAI_ARSENAL = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    private static final EntityDataAccessor<Boolean> DATA_DORA_RELOADING = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Boolean> DATA_XUANLAN_SKY_HIDDEN = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Integer> DATA_DORA_SPEAR_TICKS = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Boolean> DATA_MANOR_INTRO_HIDDEN = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    public static final int POISON_WINDUP = HiddenBossCastRules.quicker(20);
    public static final int XUELI_PAN_WINDUP = HiddenBossCastRules.quicker(12);
    public static final int SHADOW_STRIKE_WINDUP = HiddenBossCastRules.quicker(10);
    public static final int CUE_RELEASE_TICKS = 12;
    private int poisonWind;
    private int xueliPanWind;
    private int shadowStrikeWind;
    private UUID shadowStrikeTarget;
    public static final double SHADOW_PHANTOM_CHANCE = 0.35;
    public static final int SHADOW_PHANTOM_CHANCE_PERCENT = 35;
    public static final int PHANTOM_ECHO_MIN_DELAY = 0;
    public static final int PHANTOM_ECHO_MAX_DELAY = 100;
    public static final int PHANTOM_LINGER = 20;
    public static final double PHANTOM_ECHO_DAMAGE_MULT = 0.25;
    public static final double PHANTOM_ECHO_RANGE = 16.0;
    public static final String PHANTOM_SKILL_SHADOW_STRIKE = "shadow_strike";
    private int phantomEchoTicks = -1;
    private String phantomEchoSkill;
    private UUID phantomEchoTarget;
    private double phantomEchoX;
    private double phantomEchoY;
    private double phantomEchoZ;
    private final ServerBossEvent bossBar;
    private int actionTicks = 0;
    private int castTicks = 0;
    private int skillCd = 60;
    private int miniLife = -1;
    private int youziHits = 0;
    private int burstShots = 0;
    private int burstHits = 0;
    private double burstAcc = 0.0;
    private int reloadTicks = 0;
    private int spearHitCd = 0;
    private int doraSpearLeft;
    private int doraSpearGap;
    private UUID doraSpearTarget;
    private int burstTotalShots = 0;
    private double doraSkillProgress = 0.0;
    private double doraReloadProgress = 0.0;
    private final DoraCombatTempo doraTempo = new DoraCombatTempo();
    private final DoraSniperCycle doraSniper = new DoraSniperCycle();
    private Vec3 doraSniperDirection = Vec3.f_82478_;
    private boolean doraSniperNext;
    private final GongdiSkills gongdiSkills = new GongdiSkills(this);
    private final ManorFiveSkills manorFiveSkills = new ManorFiveSkills(this);
    private int cleaveWind = 0;
    public static final float CLEAVE_ATK_BONUS_PERCENT = 0.2f;
    public static final int CLEAVE_ATK_BOOST_TICKS = 100;
    private int cleaveAtkT = 0;
    private static final UUID CLEAVE_ATK_MOD = UUID.fromString("6f1a2d3c-3333-4a2b-9c3d-0000000000e1");
    private int panWind = 0;
    private int qingyiIndex = 0;
    private int qingyiTimer = 160;
    private final QingyiConfluenceSkill qingyiConfluence = new QingyiConfluenceSkill(this);
    private boolean qingyiConfluenceNext = true;
    private int lastMiniSummon = -10000;
    private int lastYouziLog = -10000;
    private int flywheelShots = 0;
    private int flywheelTimer = 0;
    private int noTargetTicks = 0;
    private int bloodRage = 0;
    private double flywheelDamageMul = 1.0;
    private int meleeLockTicks = 0;
    private int playerHitIdleTicks = 0;
    private int pearlRushCd = 0;
    private boolean haleyFlying = false;
    private int purgeTimer = 0;
    private int digT = 0;
    private int riptideTicks = 0;
    private int riptideHitCd = 0;
    private int potionDelay = 0;
    private int qingyiTridentHits = 0;
    private int qingyiRain = 0;
    private int qingyiRainTimer = 0;
    private boolean qingyiAlt = false;
    private int qingyiTridentStacks = 0;
    private int qingyiFallingTridentCooldown = 0;
    private int qingyiOnSummonLogCd = 0;
    private boolean chainGuard = false;
    private int lastChainBuff = -10000;
    private boolean quietBuffs = false;
    private boolean flywheelMaxed = false;
    private int sparrowLife = 0;
    private final List<UUID> summons = new ArrayList<UUID>();
    private int lemonStep = 1;
    private final LemonSacrifice lemonSacrifice = new LemonSacrifice(this);
    private int summonLife = 0;
    private int stuckTicks = 0;
    private Vec3 riptideDir = Vec3.f_82478_;
    private final boolean[] extraBones = new boolean[4];
    private final ManorKiting impossibleKiting = new ManorKiting();
    private UUID manorSummonerId;
    public static final int WEAPON_ENCH_LEVEL = 8;
    private static final String[] WEAPON_ENCH_IDS = new String[]{"sharpness", "smite", "fire_aspect", "sweeping", "unbreaking", "mending"};
    public static final String[] MINI_WEAPONS = new String[]{"armamentarium:dragonslayer", "armamentarium:icebreaker", "armamentarium:greataxe_of_glory"};
    public static final String[] ARMOR_IDS = new String[]{"cataclysm:ignitium_helmet", "cataclysm:ignitium_chestplate", "cataclysm:ignitium_leggings", "cataclysm:ignitium_boots"};
    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public static final String[] ARMOR_IDS_ADAMANTINE = new String[]{"armamentarium:adamantine_helmet", "armamentarium:adamantine_chestplate", "armamentarium:adamantine_leggings", "armamentarium:adamantine_boots"};
    public static final int ARMOR_ENCH_LEVEL = 8;
    public static final int APPLE_BUFF_TICKS = 1200;
    public static final double[] APPLE_STAGES = new double[]{0.75, 0.5, 0.25};
    public static final double MIN_DAMAGE_PERCENT = 0.015;
    public static final double MAX_DAMAGE_PERCENT = 0.1;
    private int appleStage = 0;
    public static final double HIT_FEEDBACK_KNOCKBACK = 0.45;
    private static long lastYouziTick = -10000L;
    private int sparrowCacheAt = -9999;
    private boolean sparrowCacheVal = false;
    private float clampHpFor = -1.0f;
    private float clampLo = 0.0f;
    private float clampHi = 0.0f;
    public static final float MAGIC_DAMAGE_REDUCTION = 0.4f;
    private int voidFalls = 0;
    private int voidSafeTicks = 0;
    private static final int VOID_STREAK_WINDOW = 600;
    public static final int SPARROW_HEAL_TICKS = 60;
    public static final int SPARROW_CLEANSE_TICKS = 100;
    public static final float SPARROW_DAMAGE_SHARE = 0.5f;
    public static final float SPARROW_NOHEAL_CHANCE = 0.1f;
    public static final int SPARROW_SHARE_TICKS = 20;
    public static final double SPARROW_HEAL_PERCENT = 0.0;
    public static final int SPARROW_BUFFS = 3;
    public static final int SPARROW_LIFE_TICKS = 1800;
    public static final double SPARROW_TELEPORT_RANGE = 10.0;
    public static final int SPARROW_TELEPORT_CD = 60;
    private long lastSparrowTeleport = -10000L;
    public static final float LEMON_BORROW_DAMAGE = 78.4f;
    public static final int XUELI_FROST_PER_HIT = 90;
    public static final int XUELI_FROST_OVER = 60;
    public static final int XUELI_KNIFE_COUNT = 3;
    public static final int XUELI_CLONE_COUNT = 2;
    public static final int XUELI_CLONE_CD = 600;
    private int xueliCloneCd = 120;
    public static final int XUELI_CLONE_FUSE = 100;
    public static final double XUELI_CLONE_HEALTH = 80.0;
    public static final double XUELI_BOMB_RADIUS = 4.5;
    public static final float XUELI_BOMB_DAMAGE = 26.0f;
    public static final float XUELI_BOMB_DAMAGE_ON_DEATH = 14.0f;
    public static final double HUNT_RANGE = 160.0;
    public static final double HUNT_START = 20.0;
    public static final double HUNT_TELEPORT = 72.0;
    public static final int HUNT_TELEPORT_CD = 120;
    private int huntTeleportCd = 0;
    private static MobEffect[] foodBuffPoolCache;
    private static boolean foodBuffPoolLogged;
    private static final String[][] ZICAI_DEBUFF_IDS;
    private static List<MobEffect> ZICAI_POOL;
    public static final int ZICAI_PER_APPLY = 2;
    public static final int ZICAI_DEBUFF_TICKS = 300;
    public static final int ZICAI_DEBUFF_MAX_AMP = 4;
    private static final Set<MobEffect> ZICAI_BLOCKED;
    private static final String[] ZICAI_BLOCK_KEYWORDS;
    private static final String[] ZICAI_BLOCK_NAMESPACES;
    private static final String[] ZICAI_BLOCK_IDS;
    public static final int DANDAN_BEACON_PERIOD = 10;
    public static final double DANDAN_BEACON_RANGE = 96.0;
    public static final double DANDAN_ATK_MAX = 200.0;
    public static final double DANDAN_ATK_PER_HIT = 5.0;
    private static final UUID DANDAN_ATK_MOD;
    private int dandanHits = 0;
    public static final int DANDAN_VORTEX_CD = 400;
    public static final int DANDAN_VORTEX_TICKS = 80;
    public static final double DANDAN_VORTEX_RADIUS = 21.0;
    public static final double DANDAN_VORTEX_PULL = 0.26;
    public static final double DANDAN_VORTEX_LIFT = 0.12;
    private int dandanVortexCd = 120;
    private int dandanVortexTicks = 0;
    private int dandanVortexLogCd = 0;
    private int dandanVortexPulls = 0;
    private double dandanVortexX;
    private double dandanVortexY;
    private double dandanVortexZ;
    private Entity dandanVortexEntity = null;
    private final Map<UUID, Long> beaconBanNotice = new HashMap<UUID, Long>();
    public static final int SHADOW_STREAK_HITS = 25;
    public static final int SHADOW_STREAK_TIMEOUT = 600;
    private int shadowStreak = 0;
    private boolean shadowVoidMode = false;
    private long shadowLastHitTick = 0L;
    public static final boolean PER_BOSS_DROPS = false;
    public static final int MINI_GRACE_TICKS = 6000;
    private static final EntityDataAccessor ftboss$zicaiCast;
    private static final EntityDataAccessor DATA_DORA_MOTION_V1;
    private static final EntityDataAccessor DATA_XUELI_CAST_ACTIONS;
    private static final EntityDataAccessor ftboss$qingyiCast;
    private static final EntityDataAccessor DATA_QINGYI_BASIC_ACTION;
    private static final EntityDataAccessor DATA_XUELI_BASIC;

    public static boolean isManorVariant(int n) {
        for (int n2 : MANOR_VARIANTS) {
            if (n2 != n) continue;
            return true;
        }
        return false;
    }

    private void applyCleaveAtkBoost() {
        AttributeInstance attributeInstance = this.m_21051_(Attributes.f_22281_);
        if (attributeInstance == null) {
            return;
        }
        attributeInstance.m_22120_(CLEAVE_ATK_MOD);
        attributeInstance.m_22118_(new AttributeModifier(CLEAVE_ATK_MOD, "ftboss.cleave_boost", attributeInstance.m_22115_() * (double)0.2f, AttributeModifier.Operation.ADDITION));
        this.cleaveAtkT = 100;
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u529f\u5e95\u300c\u65ad\u7f6a\u65a9\u300d\u653b\u51fb\u529b +{}% \u6301\u7eed {} \u79d2", (Object)20, (Object)5);
    }

    public boolean isFlywheelMaxed() {
        return this.flywheelMaxed;
    }

    public double flywheelDamageMul() {
        return this.flywheelDamageMul;
    }

    public LemonSacrifice lemonSacrifice() {
        return this.lemonSacrifice;
    }

    public boolean m_147207_(MobEffectInstance mobEffectInstance, Entity entity) {
        if (this.lemonSacrifice != null && this.lemonSacrifice.capturesEffects() && mobEffectInstance.m_19544_().m_19486_()) {
            return this.lemonSacrifice.grant(mobEffectInstance);
        }
        return super.m_147207_(mobEffectInstance, entity);
    }

    public EasterBossEntity(EntityType<? extends EasterBossEntity> entityType, Level level) {
        super(entityType, level);
        this.f_21364_ = 180;
        this.bossBar = new ServerBossEvent(this.m_5446_(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track((Entity)this, this.bossBar);
    }

    public void m_7910_(float f) {
        if (this.isImpossibleChallenge()) {
            super.m_7910_(SpeedRules.apply(f * this.getChallengeDifficulty().movementMultiplier(this.getVariant()), this));
            this.m_21564_(f);
            return;
        }
        super.m_7910_(SpeedRules.apply(BossWalkingSpeed.speed((Mob)this, f) * (this.isMini() ? 1.0f : this.getChallengeDifficulty().movementMultiplier(this.getVariant())), this));
        BossWalkingSpeed.normalizeInput((Mob)this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 1500.0).m_22268_(Attributes.f_22284_, 6.0).m_22268_(Attributes.f_22281_, 60.0).m_22268_(Attributes.f_22279_, 0.13).m_22268_(Attributes.f_22278_, 0.85).m_22268_((Attribute)ForgeMod.STEP_HEIGHT_ADDITION.get(), 2.4).m_22268_(Attributes.f_22277_, 64.0).m_22268_(Attributes.f_22280_, 0.4);
    }

    protected void m_8099_() {
        this.f_21345_.m_25352_(0, (Goal)new FloatGoal((Mob)this));
        this.f_21345_.m_25352_(1, (Goal)new PursueGoal(1.35));
        this.f_21345_.m_25352_(2, (Goal)new PlayerReachMeleeGoal(this, 1.2, true));
        this.f_21346_.m_25352_(1, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
        this.f_21346_.m_25352_(3, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, this::isValidMobTarget));
    }

    private boolean isValidMobTarget(LivingEntity livingEntity) {
        if (livingEntity == null || livingEntity == this || !livingEntity.m_6084_()) {
            return false;
        }
        if (livingEntity instanceof Player) {
            return false;
        }
        if (livingEntity instanceof EasterBossEntity) {
            return false;
        }
        if (XueliMaids.isGuard((Entity)livingEntity)) {
            return false;
        }
        ResourceLocation resourceLocation = EntityType.m_20613_((EntityType)livingEntity.m_6095_());
        return resourceLocation == null || !"ftboss".equals(resourceLocation.m_135827_());
    }

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(DATA_VARIANT, (Object)0);
        this.f_19804_.m_135372_(DATA_MINI, (Object)false);
        this.f_19804_.m_135372_(DATA_IMPOSSIBLE, (Object)false);
        this.f_19804_.m_135372_(DATA_GIANT, (Object)false);
        this.f_19804_.m_135372_(DATA_COMBAT_ANIMATION, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_SKILL_CUE, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_QIZAI_ARSENAL, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_DORA_RELOADING, (Object)false);
        this.f_19804_.m_135372_(DATA_XUANLAN_SKY_HIDDEN, (Object)false);
        this.f_19804_.m_135372_(DATA_MANOR_INTRO_HIDDEN, (Object)false);
        this.f_19804_.m_135372_(DATA_DORA_SPEAR_TICKS, (Object)0);
        this.f_19804_.m_135372_(ftboss$zicaiCast, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_DORA_MOTION_V1, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_XUELI_CAST_ACTIONS, (Object)new CompoundTag());
        this.f_19804_.m_135372_(ftboss$qingyiCast, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_QINGYI_BASIC_ACTION, (Object)new CompoundTag());
        this.f_19804_.m_135372_(DATA_XUELI_BASIC, (Object)new CompoundTag());
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    public Component m_7755_() {
        return Component.m_237113_((String)NAMES[this.getVariant()]);
    }

    public int getVariant() {
        return Math.max(0, Math.min(11, (Integer)this.f_19804_.m_135370_(DATA_VARIANT)));
    }

    public UUID getManorSummonerId() {
        return this.manorSummonerId;
    }

    public void setManorSummonerId(UUID uUID) {
        this.manorSummonerId = uUID;
    }

    public boolean canTeleportWith(LivingEntity livingEntity) {
        return ManorCombatContinuity.canTeleport(this, livingEntity);
    }

    public boolean canTeleportToFightMember(ServerPlayer serverPlayer) {
        return serverPlayer != null && serverPlayer.m_9236_() == this.m_9236_() && serverPlayer.m_6084_() && !serverPlayer.m_5833_();
    }

    private ServerPlayer manorSummoner() {
        Level level;
        if (this.manorSummonerId == null || !((level = this.m_9236_()) instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        level = serverLevel.m_7654_().m_6846_().m_11259_(this.manorSummonerId);
        return this.canTeleportWith((LivingEntity)level) ? level : null;
    }

    public boolean isMini() {
        return (Boolean)this.f_19804_.m_135370_(DATA_MINI);
    }

    public boolean isImpossibleChallenge() {
        return (Boolean)this.f_19804_.m_135370_(DATA_IMPOSSIBLE);
    }

    public ManorDifficulty getChallengeDifficulty() {
        return this.isImpossibleChallenge() ? ManorDifficulty.IMPOSSIBLE : ManorDifficulty.NORMAL;
    }

    public void setChallengeDifficulty(ManorDifficulty manorDifficulty) {
        boolean bl = this.isImpossibleChallenge();
        this.f_19804_.m_135381_(DATA_IMPOSSIBLE, (Object)(manorDifficulty == ManorDifficulty.IMPOSSIBLE ? 1 : 0));
        if (!bl && this.isImpossibleChallenge()) {
            this.flywheelVolleyCd = manorDifficulty.acceleratedTicks(this.flywheelVolleyCd);
        }
        this.refreshWalkingSpeed();
    }

    public void refreshWalkingSpeed() {
        AttributeInstance attributeInstance = this.m_21051_(Attributes.f_22279_);
        if (attributeInstance != null) {
            attributeInstance.m_22100_(ManorBaseMovementSpeed.scale(LemonMovementRules.walkingSpeed(YouziMiniRules.speed(this.isImpossibleChallenge() ? 0.33 : (this.isMini() ? (RANGED[this.getVariant()] ? 0.165 : 0.33) : 0.1), this.getVariant(), this.isMini()), this), this.isMini()));
        }
    }

    public void setMini(boolean bl) {
        this.f_19804_.m_135381_(DATA_MINI, (Object)bl);
        this.refreshWalkingSpeed();
        this.manorFiveSkills.refreshAttackBonus();
        if (!bl) {
            return;
        }
        this.miniLife = 300;
        this.m_21530_();
        this.f_21364_ = 0;
        AttributeInstance attributeInstance = this.m_21051_(Attributes.f_22276_);
        if (attributeInstance != null) {
            attributeInstance.m_22100_(YouziMiniRules.health(150.0, this.getVariant()));
        }
        this.m_21153_((float)YouziMiniRules.health(150.0, this.getVariant()));
    }

    public boolean isGiant() {
        return (Boolean)this.f_19804_.m_135370_(DATA_GIANT);
    }

    public void setGiant(boolean bl) {
        this.f_19804_.m_135381_(DATA_GIANT, (Object)bl);
    }

    public void setVariant(int n) {
        AttributeInstance attributeInstance;
        AttributeInstance attributeInstance2;
        this.qizaiMagic.forceCancel();
        this.qizaiArsenal.cancel();
        this.xuanlanSky.cancel();
        this.qingyiConfluence.cancel();
        this.qingyiConfluenceNext = true;
        this.xueliDialogue.restore(0, this.m_9236_().m_46467_());
        this.xueliRollingPinQuoteReadyAt = 0L;
        this.gongdiSkills.reset();
        this.manorFiveSkills.reset();
        this.qingyiRain = 0;
        this.qingyiFallingTridentCooldown = 0;
        this.riptideTicks = 0;
        this.potionDelay = 0;
        this.f_19804_.m_135381_(DATA_VARIANT, (Object)Math.max(0, Math.min(11, n)));
        this.manorFiveSkills.refreshAttackBonus();
        this.clearDoraCombat();
        this.doraSniperNext = false;
        this.bossBar.m_6456_((Component)Component.m_237113_((String)("\u00a7c" + NAMES[this.getVariant()])));
        this.bossBar.m_6451_(BossEvent.BossBarColor.RED);
        this.refreshExtraBones();
        this.appleStage = 0;
        this.equipWeapon();
        this.equipArmor();
        if (this.getVariant() == 5) {
            this.qingyiIndex = 0;
            this.qingyiTimer = 160;
        }
        this.qingyiTridentStacks = 0;
        this.syncQingyiTridentBuffs();
        this.applyQingyiFlatBuffs();
        this.refreshWalkingSpeed();
        AttributeInstance attributeInstance3 = this.m_21051_(Attributes.f_22281_);
        if (attributeInstance3 != null) {
            attributeInstance3.m_22120_(XUANLAN_ATK_MOD);
            if (this.getVariant() == 6) {
                attributeInstance3.m_22118_(new AttributeModifier(XUANLAN_ATK_MOD, "ftboss.xuanlan_atk", 0.3, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        if ((attributeInstance2 = this.m_21051_(Attributes.f_22281_)) != null) {
            attributeInstance2.m_22120_(MANOR_ATK_MOD);
            if (EasterBossEntity.isManorVariant(this.getVariant()) && this.getVariant() != 3) {
                attributeInstance2.m_22118_(new AttributeModifier(MANOR_ATK_MOD, "ftboss.manor_atk", 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        this.bloodRage = 0;
        AttributeInstance attributeInstance4 = this.m_21051_(Attributes.f_22281_);
        if (attributeInstance4 != null) {
            attributeInstance4.m_22120_(BLOOD_RAGE_MOD);
        }
        if ((attributeInstance = this.m_21051_(Attributes.f_22279_)) != null) {
            attributeInstance.m_22120_(BLOOD_RAGE_SPEED_MOD);
        }
        this.flywheelDamageMul = this.getVariant() == 6 ? 4.19 : 1.0;
        this.flywheelMaxed = false;
        this.openingFlywheelsGranted = 0;
        this.openingFlywheelsRetry = 0;
        this.xueliRollTicks = 0;
        this.xueliRollHitCd = 0;
        this.crushTicks.clear();
        this.shadowStreak = 0;
        this.shadowVoidMode = false;
        this.beaconBanNotice.clear();
        this.xueliCloneCd = 120;
        this.dandanHits = 0;
        AttributeInstance attributeInstance5 = this.m_21051_(Attributes.f_22281_);
        if (attributeInstance5 != null) {
            attributeInstance5.m_22120_(DANDAN_ATK_MOD);
        }
        this.lemonStep = 1;
        if (this.xueliSlamWarn > 0) {
            this.m_20242_(false);
        }
        this.xueliSlamWarn = 0;
        this.xueliSlamLeft = 0;
        this.xueliSlamGap = 0;
        this.stunTicks.clear();
        this.stunAnchor.clear();
        this.stunLaunch.clear();
        this.xuanlanBeamLock = 0;
        this.xuanlanBeamCharge = 0;
        this.xuanlanBeamFire = 0;
        this.xuanlanBeamPulse = 0;
        this.xuanlanBeamCd = 160;
        this.xlBeamPunished.clear();
        this.dandanVortexTicks = 0;
        this.dandanVortexCd = 120;
        this.dandanVortexPulls = 0;
        if (this.dandanVortexEntity != null) {
            this.dandanVortexEntity.m_146870_();
            this.dandanVortexEntity = null;
        }
        CastServer.reset(this);
    }

    private static boolean isNoStealBuff(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance == null || mobEffectInstance.m_19544_() == null) {
            return true;
        }
        ResourceLocation resourceLocation = ForgeRegistries.MOB_EFFECTS.getKey((Object)mobEffectInstance.m_19544_());
        return resourceLocation != null && "herbalbrews".equals(resourceLocation.m_135827_()) && "renewal".equals(resourceLocation.m_135815_());
    }

    private static boolean isNoStripBuff(MobEffectInstance mobEffectInstance) {
        if (EasterBossEntity.isNoStealBuff(mobEffectInstance)) {
            return true;
        }
        ResourceLocation resourceLocation = ForgeRegistries.MOB_EFFECTS.getKey((Object)mobEffectInstance.m_19544_());
        if (resourceLocation == null || !"ftboss".equals(resourceLocation.m_135827_())) {
            return false;
        }
        String string = resourceLocation.m_135815_();
        return "blood_rage".equals(string) || "blood_rage_fake".equals(string);
    }

    public void copyBuffsFrom(EasterBossEntity easterBossEntity) {
        if (easterBossEntity == null || easterBossEntity == this) {
            return;
        }
        for (MobEffectInstance mobEffectInstance : easterBossEntity.m_21220_()) {
            this.m_7292_(new MobEffectInstance(mobEffectInstance.m_19544_(), mobEffectInstance.m_19557_(), mobEffectInstance.m_19564_(), mobEffectInstance.m_19571_(), false, false));
        }
    }

    private void shareBuffsToMinis() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        for (EasterBossEntity easterBossEntity : serverLevel.m_45976_(EasterBossEntity.class, this.m_20191_().m_82400_(48.0))) {
            if (easterBossEntity == this || !easterBossEntity.isMini() || easterBossEntity.getVariant() != this.getVariant()) continue;
            easterBossEntity.copyBuffsFrom(this);
        }
    }

    public void equipMiniWeapon(int n) {
        try {
            String string = MINI_WEAPONS[Math.floorMod(n, MINI_WEAPONS.length)];
            Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(string));
            if (item == null || item == Items.f_41852_) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] \u5206\u8eab\u6b66\u5668\u53d6\u4e0d\u5230: {}", (Object)string);
                return;
            }
            ItemStack itemStack = new ItemStack((ItemLike)item);
            for (Enchantment enchantment : EasterBossEntity.buildEnchants(WEAPON_ENCH_IDS)) {
                if (enchantment == null) continue;
                itemStack.m_41663_(enchantment, 8);
            }
            itemStack.m_41784_().m_128379_("Unbreakable", true);
            this.m_21008_(InteractionHand.MAIN_HAND, itemStack);
            this.m_21409_(EquipmentSlot.MAINHAND, 0.0f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u67da\u5b50\u5206\u8eab\u88c5\u5907\u6b66\u5668: {}", (Object)string);
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u5206\u8eab\u6b66\u5668\u88c5\u8f7d\u5931\u8d25: {}", (Object)throwable.toString());
        }
    }

    public void equipWeapon() {
        try {
            ItemStack itemStack;
            ItemStack itemStack2 = this.m_21206_();
            if (new ResourceLocation(DUOLA_MELEE_ID).equals((Object)ForgeRegistries.ITEMS.getKey((Object)itemStack2.m_41720_()))) {
                this.m_21008_(InteractionHand.OFF_HAND, ItemStack.f_41583_);
            }
            if (itemStack2.m_41782_() && itemStack2.m_41783_().m_128451_("CustomModelData") == 160416 && new ResourceLocation(LEMONCAT_OFFHAND_ID).equals((Object)ForgeRegistries.ITEMS.getKey((Object)itemStack2.m_41720_()))) {
                this.m_21008_(InteractionHand.OFF_HAND, ItemStack.f_41583_);
            }
            String string = WEAPONS[this.getVariant()];
            if (this.getVariant() == 2) {
                string = DORA_SMG_ID;
            }
            if (this.getVariant() == 6) {
                string = "ftboss:wenxuanlan_flywheel";
            }
            if (string == null || string.isEmpty()) {
                this.m_21008_(InteractionHand.MAIN_HAND, ItemStack.f_41583_);
                return;
            }
            Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(string));
            ItemStack itemStack3 = itemStack = item == null ? ItemStack.f_41583_ : new ItemStack((ItemLike)item);
            if (!itemStack.m_41619_()) {
                for (Enchantment enchantment : EasterBossEntity.buildEnchants(WEAPON_ENCH_IDS)) {
                    if (enchantment == null) continue;
                    itemStack.m_41663_(enchantment, 8);
                }
                itemStack.m_41784_().m_128379_("Unbreakable", true);
                if (this.getVariant() == 1 && itemStack.m_150930_(Items.f_42001_)) {
                    itemStack.m_41784_().m_128405_("CustomModelData", 91601);
                }
                if (this.getVariant() == 4) {
                    itemStack.m_41784_().m_128405_("CustomModelData", 160416);
                }
            }
            this.m_21008_(InteractionHand.MAIN_HAND, itemStack);
            this.m_21409_(EquipmentSlot.MAINHAND, 0.0f);
            if (this.getVariant() == 10 || this.getVariant() == 4 || this.getVariant() == 2) {
                ItemStack itemStack4;
                String string2 = this.getVariant() == 2 ? DUOLA_MELEE_ID : (this.getVariant() == 4 ? LEMONCAT_OFFHAND_ID : XUELI_MELEE_ID);
                Item item2 = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(string2));
                ItemStack itemStack5 = itemStack4 = item2 == null ? ItemStack.f_41583_ : new ItemStack((ItemLike)item2);
                if (!itemStack4.m_41619_()) {
                    for (Enchantment enchantment : EasterBossEntity.buildEnchants(WEAPON_ENCH_IDS)) {
                        if (enchantment == null) continue;
                        itemStack4.m_41663_(enchantment, 8);
                    }
                    itemStack4.m_41784_().m_128379_("Unbreakable", true);
                    if (this.getVariant() == 4) {
                        itemStack4.m_41784_().m_128405_("CustomModelData", 160416);
                    }
                }
                this.m_21008_(InteractionHand.OFF_HAND, itemStack4);
                this.m_21409_(EquipmentSlot.OFFHAND, 0.0f);
            }
            if (this.getVariant() == 4) {
                FTBossMod.LOGGER.info("[FTBoss][LemonCat] weapon models CMD={} main={} off={}", new Object[]{160416, this.m_21205_(), this.m_21206_()});
            }
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} \u88c5\u5907\u6b66\u5668: \u76ee\u6807={} \u5b9e\u9645\u4e3b\u624b={} \u9644\u9b54 lv{} \u5171 {} \u6761, \u65e0\u6cd5\u7834\u574f=true", new Object[]{NAMES[this.getVariant()], string, this.m_21205_(), 8, this.m_21205_().m_41785_().size()});
        }
        catch (Throwable throwable) {
            this.m_21008_(InteractionHand.MAIN_HAND, ItemStack.f_41583_);
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u88c5\u8f7d\u6b66\u5668\u5931\u8d25: {}", (Object)throwable.toString());
        }
    }

    private String[] armorFor() {
        return this.getVariant() == 3 ? ARMOR_IDS_ADAMANTINE : ARMOR_IDS;
    }

    private static Enchantment[] enchantsFor(EquipmentSlot equipmentSlot) {
        switch (equipmentSlot) {
            case HEAD: {
                return EasterBossEntity.buildEnchants("protection", "fire_protection", "blast_protection", "projectile_protection", "thorns", "respiration", "aqua_affinity", "unbreaking", "mending");
            }
            case CHEST: {
                return EasterBossEntity.buildEnchants("protection", "fire_protection", "blast_protection", "projectile_protection", "thorns", "unbreaking", "mending");
            }
            case LEGS: {
                return EasterBossEntity.buildEnchants("protection", "fire_protection", "blast_protection", "projectile_protection", "thorns", "swift_sneak", "unbreaking", "mending");
            }
            case FEET: {
                return EasterBossEntity.buildEnchants("protection", "fire_protection", "blast_protection", "projectile_protection", "thorns", "feather_falling", "depth_strider", "frost_walker", "soul_speed", "unbreaking", "mending");
            }
        }
        return new Enchantment[0];
    }

    private static Enchantment[] buildEnchants(String ... stringArray) {
        ArrayList<Enchantment> arrayList = new ArrayList<Enchantment>();
        for (String string : stringArray) {
            Enchantment enchantment = (Enchantment)ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(string));
            if (enchantment != null) {
                arrayList.add(enchantment);
                continue;
            }
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u627e\u4e0d\u5230\u9644\u9b54 {} (\u8df3\u8fc7)", (Object)string);
        }
        return arrayList.toArray(new Enchantment[0]);
    }

    public void equipArmor() {
        this.equipArmor(true);
    }

    private void equipArmor(boolean bl) {
        int n = 0;
        String[] stringArray = this.armorFor();
        for (int i = 0; i < ARMOR_SLOTS.length; ++i) {
            EquipmentSlot equipmentSlot = ARMOR_SLOTS[i];
            ItemStack itemStack = ItemStack.f_41583_;
            try {
                Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(stringArray[i]));
                if (item != null && item != Items.f_41852_) {
                    itemStack = new ItemStack((ItemLike)item);
                    for (Enchantment enchantment : EasterBossEntity.enchantsFor(equipmentSlot)) {
                        if (enchantment == null) continue;
                        itemStack.m_41663_(enchantment, 8);
                    }
                    itemStack.m_41784_().m_128379_("Unbreakable", true);
                }
            }
            catch (Throwable throwable) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] \u62a4\u7532\u88c5\u8f7d\u5931\u8d25 {}: {}", (Object)stringArray[i], (Object)throwable.toString());
            }
            this.m_8061_(equipmentSlot, itemStack);
            this.m_21409_(equipmentSlot, 0.0f);
            if (itemStack.m_41619_()) continue;
            ++n;
        }
        if (bl) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} \u7a7f{}\u7532: {}/4 \u4ef6, \u9644\u9b54 lv{}, \u65e0\u6cd5\u7834\u574f={}", new Object[]{NAMES[this.getVariant()], this.getVariant() == 3 ? "\u4e9a\u5f53" : "\u817e\u708e", n, 8, n > 0});
        }
    }

    private void tickAppleStages() {
        if (this.isMini()) {
            return;
        }
        if (this.getVariant() == 9) {
            return;
        }
        double d = this.m_21223_() / Math.max(1.0f, this.m_21233_());
        while (this.appleStage < APPLE_STAGES.length && d <= APPLE_STAGES[this.appleStage]) {
            ++this.appleStage;
            this.eatEnchantedGoldenApple();
        }
    }

    public void eatEnchantedGoldenApple() {
        if (this.m_9236_().f_46443_) {
            return;
        }
        Object object = this.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        this.m_7292_(new MobEffectInstance(MobEffects.f_19617_, 2400, 3, false, true, true));
        this.m_7292_(new MobEffectInstance(MobEffects.f_19605_, 100, 1, false, true, true));
        object = new StringBuilder();
        for (MobEffect mobEffect : INNATE_EFFECTS) {
            this.m_7292_(new MobEffectInstance(mobEffect, 1200, 1, false, true, true));
            ((StringBuilder)object).append(mobEffect.m_19482_().getString()).append(' ');
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123748_, this.m_20185_(), this.m_20186_() + 1.4, this.m_20189_(), 30, 0.5, 0.7, 0.5, 0.08);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123750_, this.m_20185_(), this.m_20186_() + 1.8, this.m_20189_(), 8, 0.4, 0.4, 0.4, 0.03);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11912_, SoundSource.HOSTILE, 1.4f, 0.9f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12321_, SoundSource.HOSTILE, 1.2f, 1.0f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] {} \u5403\u4e0b\u9644\u9b54\u91d1\u82f9\u679c: \u4f24\u5bb3\u5438\u6536 IV 2 \u5206\u949f + \u751f\u547d\u6062\u590d II 5 \u79d2 + 6 \u4e2a\u4e8c\u7ea7\u589e\u76ca: {}", (Object)NAMES[this.getVariant()], (Object)((StringBuilder)object).toString().trim());
    }

    private void refreshExtraBones() {
        boolean[] blArray = EXTRA[this.getVariant()];
        for (int i = 0; i < 4; ++i) {
            this.extraBones[i] = blArray[i];
        }
    }

    public boolean[] getExtraBones() {
        if (this.f_19797_ % 20 == 0) {
            this.refreshExtraBones();
        }
        return this.extraBones;
    }

    public int getActionTicks() {
        CompoundTag compoundTag = (CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION);
        return compoundTag.m_128471_("cast") ? 0 : Math.max(0, compoundTag.m_128451_("duration") - (int)this.getCombatAnimationAge(0.0f));
    }

    public float getActionProgress() {
        return this.getActionProgress(0.0f);
    }

    public float getActionProgress(float f) {
        CompoundTag compoundTag = (CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION);
        return EasterCombatMath.progress(this.getCombatAnimationAge(f), compoundTag.m_128451_("duration"), compoundTag.m_128471_("impact"));
    }

    public float getCombatAnimationAge(float f) {
        return (float)(this.m_9236_().m_46467_() - ((CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION)).m_128454_("start")) + f;
    }

    public float getCombatAnimationWeight(float f) {
        if (!this.m_6084_()) {
            return 0.0f;
        }
        CompoundTag compoundTag = (CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION);
        if (compoundTag.m_128471_("cast")) {
            return EasterCombatMath.castWeight(this.getCombatAnimationAge(f), compoundTag.m_128451_("duration"));
        }
        return EasterCombatMath.weight(this.getCombatAnimationAge(f), compoundTag.m_128451_("duration"), compoundTag.m_128471_("impact"));
    }

    public int getWeaponStyle() {
        return STYLES[this.getVariant()];
    }

    public boolean isDoraReloading() {
        return (Boolean)this.f_19804_.m_135370_(DATA_DORA_RELOADING);
    }

    public int getDoraSpearTicks() {
        return (Integer)this.f_19804_.m_135370_(DATA_DORA_SPEAR_TICKS);
    }

    public boolean isCastingLike() {
        CompoundTag compoundTag = (CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION);
        return compoundTag.m_128471_("cast") && this.getCombatAnimationAge(0.0f) < (float)compoundTag.m_128451_("duration");
    }

    public int getCastStyle() {
        return ((CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION)).m_128451_("style");
    }

    private void syncCombatAnimation(int n, boolean bl, int n2, boolean bl2) {
        BasicServer.cancel(this, bl);
        if (this.m_9236_().f_46443_) {
            return;
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128356_("start", this.m_9236_().m_46467_());
        compoundTag.m_128405_("duration", Math.max(1, n));
        compoundTag.m_128379_("cast", bl);
        compoundTag.m_128405_("style", n2);
        compoundTag.m_128379_("impact", bl2);
        LemonMotionServer.stamp(this, compoundTag);
        MotionServer.carry(this, compoundTag);
        this.f_19804_.m_135381_(DATA_COMBAT_ANIMATION, (Object)compoundTag);
    }

    public void playAction(int n) {
        this.actionTicks = Math.max(1, n);
        this.castTicks = 0;
        this.syncCombatAnimation(n, false, this.getWeaponStyle(), false);
    }

    private void playImpactAction(int n) {
        this.playAction(n);
        this.syncCombatAnimation(n, false, this.getWeaponStyle(), true);
    }

    public void playCast(int n, int n2) {
        this.castTicks = Math.max(1, n);
        this.actionTicks = 0;
        this.syncCombatAnimation(n, true, n2, false);
    }

    public CompoundTag getSkillCue() {
        return (CompoundTag)this.f_19804_.m_135370_(DATA_SKILL_CUE);
    }

    public void setQizaiCue(CompoundTag compoundTag) {
        if (this.getVariant() == 11) {
            this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)compoundTag);
        }
        CrossGuard.cue(this);
    }

    public CompoundTag getQizaiArsenalCue() {
        return (CompoundTag)this.f_19804_.m_135370_(DATA_QIZAI_ARSENAL);
    }

    public void setQizaiArsenalCue(CompoundTag compoundTag) {
        this.f_19804_.m_135381_(DATA_QIZAI_ARSENAL, (Object)compoundTag);
    }

    public boolean castQizaiArsenal(Vec3 vec3) {
        ServerLevel serverLevel;
        Level level = this.m_9236_();
        return level instanceof ServerLevel && this.qizaiArsenal.cast(serverLevel = (ServerLevel)level, vec3);
    }

    public boolean isQizaiProtectedCasting() {
        return ManorBalanceHooks.crossProtected(this.qizaiMagic != null && this.qizaiMagic.protectedCast(), this);
    }

    public boolean isQizaiUninterruptibleCasting() {
        return this.qizaiMagic != null && this.qizaiMagic.uninterruptible();
    }

    public void playParallelSpell(int n) {
        if (this.skillCommitted() || this.actionTicks > 0 || this.castTicks > 0) {
            return;
        }
        this.syncCombatAnimation(n, true, this.getWeaponStyle(), false);
        CompoundTag compoundTag = ((CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION)).m_6426_();
        compoundTag.m_128379_("parallelSpell", true);
        this.f_19804_.m_135381_(DATA_COMBAT_ANIMATION, (Object)compoundTag);
    }

    public void finishParallelSpell() {
        if (((CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION)).m_128471_("parallelSpell")) {
            this.f_19804_.m_135381_(DATA_COMBAT_ANIMATION, (Object)new CompoundTag());
        }
    }

    public boolean isGongdiBloodRageFull() {
        return this.bloodRage >= 5;
    }

    public void onGongdiAbilityHit(ServerLevel serverLevel, LivingEntity livingEntity, float f) {
        this.bloodRageOnHit(livingEntity, f);
        if (!livingEntity.m_6084_()) {
            return;
        }
        ArmorShred.addStack(livingEntity, serverLevel.m_46467_());
        this.addBloodRage(serverLevel, livingEntity);
    }

    public float gongdiMeleeDamage(LivingEntity livingEntity) {
        return GongdiCombatRules.meleeDamage(this.m_21133_(Attributes.f_22281_), EnchantmentHelper.m_44833_((ItemStack)this.m_21205_(), (MobType)livingEntity.m_6336_()));
    }

    public void finishSignatureCleave(int n) {
        this.applyCleaveAtkBoost();
        this.m_5634_(GongdiCombatRules.outerHeal(this.m_21233_() - this.m_21223_(), n));
        if (n >= 2) {
            this.m_7292_(new MobEffectInstance(MobEffects.f_19600_, 480, 1));
        }
    }

    public void onSignatureCleaveHit(ServerLevel serverLevel, LivingEntity livingEntity, float f) {
        this.gongdiSkills.onAbilityHit(serverLevel, livingEntity, f);
    }

    public float manorSkillDamage(LivingEntity livingEntity, float f, double d) {
        if (this.isMini() || !ManorFiveCombatRules.hasExpandedSkills(this.getVariant()) || livingEntity == null) {
            return f;
        }
        float f2 = ManorSkillDamageRules.damage(this.m_21133_(Attributes.f_22281_), EnchantmentHelper.m_44833_((ItemStack)this.m_21205_(), (MobType)livingEntity.m_6336_()), f, d);
        return LemonInheritedAbilityRules.allySkillDamage((LivingEntity)this, f2);
    }

    public void beginGongdiCast(String string, int n, double d, Vec3 vec3) {
        if (!GongdiCastLifecycle.canBegin(this)) {
            return;
        }
        this.playCast(n, 3);
        this.beginSkillCue(this.m_20182_(), d, n, true);
        CompoundTag compoundTag = this.getSkillCue().m_6426_();
        compoundTag.m_128359_("gongdiSkill", string);
        compoundTag.m_128347_("directionX", vec3.f_82479_);
        compoundTag.m_128347_("directionZ", vec3.f_82481_);
        this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)compoundTag);
    }

    public void finishGongdiCast() {
        if (!GongdiCastLifecycle.canFinish(this)) {
            GongdiCastLifecycle.cancel(this);
            return;
        }
        this.playImpactAction(18);
        this.releaseSkillCue(this.m_20182_());
    }

    public void resetGongdiComboCooldown() {
        this.skillCd = Math.min(this.skillCd, 30);
    }

    public void beginManorFiveCast(int n, int n2, Vec3 vec3, double d, boolean bl) {
        this.playCast(n, this.getWeaponStyle());
        this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)new CompoundTag());
        if (bl) {
            this.beginSkillCue(vec3, d, n2, false);
        }
        if (!this.castsWithoutStopping()) {
            this.m_21573_().m_26573_();
        }
    }

    public void finishManorFiveCast(Vec3 vec3) {
        QingyiCastSync.finishNative(this);
        MotionServer.finish(this);
        this.playImpactAction(12);
        this.releaseSkillCue(vec3);
        SkillBodyDispatch.finish(this);
    }

    public void cancelManorFiveCast() {
        net.farmingtales.ftboss.qingyilight.LightHooks.cancel(this);
        QingyiCastSync.cancelNative(this);
        SkillBodyDispatch.captureCancel(this);
        net.farmingtales.ftboss.zheergenfx.LightHooks.cancel(this);
        MotionServer.clear(this);
        LightHooks.cancel(this, 1);
        this.actionTicks = 0;
        this.castTicks = 0;
        this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)new CompoundTag());
        this.f_19804_.m_135381_(DATA_COMBAT_ANIMATION, (Object)new CompoundTag());
        SkillBodyDispatch.finish(this);
    }

    private void beginSkillCue(Vec3 vec3, double d, int n, boolean bl) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128356_("start", this.m_9236_().m_46467_());
        compoundTag.m_128405_("duration", n);
        compoundTag.m_128347_("x", vec3.f_82479_);
        compoundTag.m_128347_("y", vec3.f_82480_);
        compoundTag.m_128347_("z", vec3.f_82481_);
        compoundTag.m_128347_("radius", d);
        compoundTag.m_128379_("follow", bl);
        this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)compoundTag);
    }

    private void releaseSkillCue(Vec3 vec3) {
        CompoundTag compoundTag = this.getSkillCue().m_6426_();
        if (compoundTag.m_128456_()) {
            return;
        }
        compoundTag.m_128356_("start", this.m_9236_().m_46467_());
        compoundTag.m_128405_("duration", compoundTag.m_128441_("gongdiSkill") ? 18 : 12);
        compoundTag.m_128347_("x", vec3.f_82479_);
        compoundTag.m_128347_("y", vec3.f_82480_);
        compoundTag.m_128347_("z", vec3.f_82481_);
        compoundTag.m_128379_("follow", false);
        compoundTag.m_128379_("released", true);
        this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)compoundTag);
    }

    private void syncBeamCue(Vec3 vec3, Vec3 vec32, double d, int n, boolean bl, boolean bl2) {
        boolean bl3;
        CompoundTag compoundTag = this.getSkillCue();
        boolean bl4 = bl3 = !compoundTag.m_128471_("beam") || compoundTag.m_128471_("firing") != bl || compoundTag.m_128471_("released") != bl2;
        if (!bl3 && !bl2 && this.f_19797_ % 2 != 0) {
            return;
        }
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.m_128356_("start", bl3 || bl2 ? this.m_9236_().m_46467_() : compoundTag.m_128454_("start"));
        compoundTag2.m_128405_("duration", n);
        compoundTag2.m_128347_("x", vec3.f_82479_);
        compoundTag2.m_128347_("y", vec3.f_82480_);
        compoundTag2.m_128347_("z", vec3.f_82481_);
        compoundTag2.m_128347_("endX", vec32.f_82479_);
        compoundTag2.m_128347_("endY", vec32.f_82480_);
        compoundTag2.m_128347_("endZ", vec32.f_82481_);
        compoundTag2.m_128347_("radius", d);
        compoundTag2.m_128379_("beam", true);
        compoundTag2.m_128379_("firing", bl);
        compoundTag2.m_128379_("released", bl2);
        this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)compoundTag2);
    }

    private boolean skillCommitted() {
        return this.qizaiMagic.busy() || this.isXuanlanSkyCasting() || this.doraSniper.busy() || this.manorFiveSkills.isCasting() || this.dandanRush > 0 || this.qingyiConfluence.busy() || this.getVariant() == 5 && (this.riptideTicks > 0 || this.potionDelay > 0 || this.qingyiRain > 0) || this.gongdiSkills.isCasting() || this.cleaveWind > 0 || this.panWind > 0 || this.poisonWind > 0 || this.xueliPanWind > 0 || this.shadowStrikeWind > 0 || this.xuanlanBeamBusy() || this.xueliSlamWarn > 0;
    }

    private boolean combatPlayer(LivingEntity livingEntity) {
        if (!ManorCombatContinuity.present((Entity)this, livingEntity)) {
            return false;
        }
        if (ManorFinaleCombat.manages((LivingEntity)this)) {
            return ManorFinaleCombat.validTarget((LivingEntity)this, livingEntity);
        }
        if (!ManorLivingTargets.valid((Entity)this, livingEntity)) {
            return false;
        }
        return livingEntity.m_6084_() && !ManorLivingTargets.creative(livingEntity) && !livingEntity.m_5833_();
    }

    private Vec3 clipSkillRay(Vec3 vec3, Vec3 vec32) {
        return this.m_9236_().m_45547_(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this)).m_82450_();
    }

    private void tickSkillWindups(ServerLevel serverLevel) {
        CompoundTag compoundTag;
        this.qizaiMagic.tick(serverLevel);
        if (this.skillCommitted() && !this.castsWithoutStopping()) {
            this.m_21573_().m_26573_();
        }
        this.qingyiConfluence.tick(serverLevel);
        this.manorFiveSkills.tick(serverLevel);
        if (this.getVariant() == 3) {
            this.gongdiSkills.tick(serverLevel);
        }
        if (this.cleaveWind > 0 && --this.cleaveWind == 0) {
            this.executeCleave();
        }
        if (this.panWind > 0 && --this.panWind == 0) {
            this.executePanFlip();
        }
        if (this.poisonWind > 0 && --this.poisonWind == 0) {
            this.executePoisonBurst(serverLevel);
        }
        if (this.xueliPanWind > 0 && --this.xueliPanWind == 0) {
            this.executeXueliPanSlap(serverLevel);
        }
        if (this.shadowStrikeWind > 0 && --this.shadowStrikeWind == 0) {
            this.executeShadowStrike(serverLevel);
        }
        if (!(compoundTag = this.getSkillCue()).m_128456_() && compoundTag.m_128471_("released") && serverLevel.m_46467_() - compoundTag.m_128454_("start") > (long)compoundTag.m_128451_("duration")) {
            this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)new CompoundTag());
        }
    }

    private void cancelSkillWindups() {
        net.farmingtales.ftboss.qingyilight.LightHooks.cancel(this);
        QingyiCastSync.clear(this);
        SkillBodyDispatch.captureCancel(this);
        net.farmingtales.ftboss.zheergenfx.LightHooks.cancel(this);
        MotionServer.clear(this);
        LightHooks.cancel(this, 1);
        boolean bl = this.qizaiMagic.uninterruptible();
        this.qizaiMagic.cancel();
        this.qizaiArsenal.cancel();
        this.xuanlanSky.cancel();
        this.doraSpearLeft = 0;
        this.doraSpearGap = 0;
        this.doraSpearTarget = null;
        this.f_19804_.m_135381_(DATA_DORA_SPEAR_TICKS, (Object)0);
        this.qingyiConfluence.cancel();
        this.cancelDoraSniper();
        this.manorFiveSkills.cancel();
        this.qingyiRain = 0;
        this.riptideTicks = 0;
        this.potionDelay = 0;
        this.gongdiSkills.reset();
        this.cleaveWind = 0;
        this.panWind = 0;
        this.poisonWind = 0;
        this.xueliPanWind = 0;
        this.shadowStrikeWind = 0;
        this.shadowStrikeTarget = null;
        this.xuanlanBeamLock = 0;
        this.xuanlanBeamCharge = 0;
        this.xuanlanBeamFire = 0;
        this.xlBeamPunished.clear();
        if (this.xueliSlamWarn > 0 || this.xueliSlamLeft > 0) {
            this.m_20242_(false);
        }
        this.xueliSlamWarn = 0;
        this.xueliSlamLeft = 0;
        this.xueliSlamGap = 0;
        if (!bl && !this.getSkillCue().m_128456_()) {
            this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)new CompoundTag());
        }
        if (!bl && !((CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION)).m_128456_()) {
            this.f_19804_.m_135381_(DATA_COMBAT_ANIMATION, (Object)new CompoundTag());
            this.actionTicks = 0;
            this.castTicks = 0;
        }
        SkillBodyDispatch.finish(this);
    }

    public void prepareArenaTransfer() {
        MotionServer.clear(this);
        LightHooks.cancel(this, 3);
        this.qizaiMagic.forceCancel();
        this.cancelSkillWindups();
        this.dandanRush = 0;
        this.xueliRollTicks = 0;
        this.stunTicks.clear();
        this.stunAnchor.clear();
        this.stunLaunch.clear();
        this.crushTicks.clear();
        this.m_20242_(false);
    }

    public boolean isPerformingSkill() {
        return this.skillCommitted() || this.m_21525_();
    }

    public boolean allowsParallelMelee() {
        return this.getVariant() == 5 && !this.isMini() && !this.isGiant();
    }

    protected float m_6108_() {
        return 0.96f;
    }

    public boolean castsWithoutStopping() {
        return !this.movementLocked();
    }

    private boolean movementLocked() {
        return this.qizaiMagic.busy() || this.isXuanlanSkyCasting() || this.doraSniper.busy() || this.xuanlanBeamBusy() || this.dandanRush > 0 || this.riptideTicks > 0 || this.reloadTicks > 0 || this.xueliRollTicks > 0 || this.xueliSlamWarn > 0 || this.xueliSlamLeft > 0;
    }

    private boolean blocksMelee() {
        return ManorCombatLanes.blocksMelee(!this.allowsParallelMelee() && (this.movementLocked() || this.gongdiSkills.isCasting() || this.cleaveWind > 0 || this.panWind > 0 || this.xueliPanWind > 0 || this.shadowStrikeWind > 0), this, this.movementLocked());
    }

    public boolean youziPresent() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        return serverLevel.m_46467_() - lastYouziTick <= 20L;
    }

    public static boolean youziPresent(MinecraftServer minecraftServer) {
        if (minecraftServer == null) {
            return false;
        }
        return minecraftServer.m_129783_().m_46467_() - lastYouziTick <= 20L;
    }

    private boolean sparrowNearby() {
        for (EasterBossEntity easterBossEntity : this.m_9236_().m_45976_(EasterBossEntity.class, this.m_20191_().m_82400_(48.0))) {
            if (easterBossEntity == this || !easterBossEntity.m_6084_() || easterBossEntity.getVariant() != 9) continue;
            return true;
        }
        return false;
    }

    private boolean sparrowCached() {
        if (this.f_19797_ - this.sparrowCacheAt >= 10) {
            this.sparrowCacheVal = this.sparrowNearby();
            this.sparrowCacheAt = this.f_19797_;
        }
        return this.sparrowCacheVal;
    }

    private void refreshClampBounds() {
        float f = this.m_21233_();
        if (f != this.clampHpFor) {
            this.clampHpFor = f;
            this.clampLo = (float)((double)f * 0.015);
            this.clampHi = (float)((double)f * 0.1);
        }
    }

    public int buffAmp(int n) {
        return n + (this.sparrowCached() ? 1 : 0);
    }

    public static void supportSparrow(ServerLevel serverLevel, EasterBossEntity easterBossEntity) {
        if (easterBossEntity == null) {
            return;
        }
        easterBossEntity.m_20331_(false);
        easterBossEntity.m_21557_(false);
        easterBossEntity.m_6593_((Component)Component.m_237113_((String)"\u00a7e\u9ebb\u96c0"));
        easterBossEntity.m_20340_(true);
        easterBossEntity.m_21530_();
        AttributeInstance attributeInstance = easterBossEntity.m_21051_(Attributes.f_22276_);
        float f = ImpossibleSparrowSupport.health(easterBossEntity);
        if (attributeInstance != null) {
            attributeInstance.m_22100_((double)f);
        }
        easterBossEntity.m_21153_(f);
        EasterBossEntity easterBossEntity2 = easterBossEntity;
        MaqueSupportHooks.arrival((Entity)easterBossEntity2);
        SparrowSupport.noteSparrow(easterBossEntity2);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, easterBossEntity.m_20185_(), easterBossEntity.m_20186_() + 1.0, easterBossEntity.m_20189_(), 30, 0.5, 0.6, 0.5, 0.06);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u9ebb\u96c0\u8f6c\u4e3a\u652f\u63f4\u4f4d: variant={} \u540d\u5b57={} \u8840\u91cf={}", new Object[]{easterBossEntity.getVariant(), easterBossEntity.m_7755_().getString(), (int)f});
    }

    public boolean m_7327_(Entity entity) {
        LivingEntity livingEntity;
        float f;
        if (!ManorCombatContinuity.canAttack(this, entity)) {
            return false;
        }
        if (this.blocksMelee()) {
            return false;
        }
        if (this.getVariant() == 5 && !this.qingyiConfluence.busy() && !this.isCastingLike()) {
            QingyiConfluenceTridentItem.animate((LivingEntity)this, "attack");
        }
        if (!this.m_9236_().f_46443_ && entity instanceof Player) {
            Player player = (Player)entity;
            player.f_19802_ = 0;
        }
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)entity;
            f = livingEntity2.m_21223_();
        } else {
            f = 0.0f;
        }
        float f2 = f;
        net.farmingtales.ftboss.qingyi_basic.BasicServer.strike(this, entity);
        BasicServer.strike(this, entity);
        boolean bl = super.m_7327_(entity);
        if (bl && !this.m_9236_().f_46443_ && entity instanceof LivingEntity) {
            livingEntity = (LivingEntity)entity;
            this.lemonSacrifice.onMeleeHit(livingEntity, Math.max(0.0f, f2 - livingEntity.m_21223_()));
        }
        if (bl && !this.m_9236_().f_46443_ && this.getVariant() == 3 && entity instanceof LivingEntity) {
            livingEntity = (LivingEntity)entity;
            this.bloodRageOnHit(livingEntity, Math.max(0.0f, f2 - livingEntity.m_21223_()));
        }
        if (bl && !this.m_9236_().f_46443_ && entity instanceof Player) {
            livingEntity = (Player)entity;
            if (this.getVariant() == 2) {
                this.doraKnockback(livingEntity, 1.25, 0.3);
                this.doraHit(livingEntity);
            } else {
                this.knockbackPlayer(livingEntity);
            }
            this.chaseAfter((Player)livingEntity);
            if (this.getVariant() == 1) {
                this.stripOneBuff((Player)livingEntity);
            }
            if (this.getVariant() == 1) {
                this.dandanRampAttack();
            }
            if (this.getVariant() == 10) {
                this.startCrush(livingEntity);
            }
            if (this.getVariant() == 10) {
                EasterBossEntity.applyXueliFrost(livingEntity);
            }
        }
        if (bl) {
            this.playMeleeImpactAction();
        }
        if (bl && !this.m_9236_().f_46443_ && entity instanceof LivingEntity) {
            livingEntity = (LivingEntity)entity;
            this.onWeaponHit(livingEntity);
        }
        return bl;
    }

    private void knockbackPlayer(LivingEntity livingEntity) {
        double d = livingEntity.m_20185_() - this.m_20185_();
        double d2 = livingEntity.m_20189_() - this.m_20189_();
        double d3 = Math.max(0.1, Math.sqrt(d * d + d2 * d2));
        boolean bl = !this.isMini() && ManorFiveCombatRules.hasExpandedSkills(this.getVariant());
        double d4 = bl ? 1.5 : 1.0;
        double d5 = bl ? 0.55 : 0.42;
        BossKnockback.push((Entity)this, (Entity)livingEntity, d / d3 * d4, d5, d2 / d3 * d4);
        livingEntity.f_19864_ = true;
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_(), livingEntity.m_20189_(), SoundEvents.f_12314_, SoundSource.HOSTILE, 1.0f, 0.9f);
        }
    }

    private void chaseAfter(Player player) {
        this.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 30, 2, false, false, false));
        this.m_6710_((LivingEntity)player);
        this.m_21563_().m_24960_((Entity)player, 60.0f, 60.0f);
    }

    private void stripOneBuff(Player player) {
        ArrayList<MobEffectInstance> arrayList = new ArrayList<MobEffectInstance>();
        for (MobEffectInstance mobEffectInstance2 : player.m_21220_()) {
            if (mobEffectInstance2.m_19544_() == null || !mobEffectInstance2.m_19544_().m_19486_() || EasterBossEntity.isNoStripBuff(mobEffectInstance2)) continue;
            arrayList.add(mobEffectInstance2);
        }
        if (arrayList.isEmpty()) {
            return;
        }
        MobEffectInstance mobEffectInstance = (MobEffectInstance)arrayList.get(this.f_19796_.m_188503_(arrayList.size()));
        if (!player.m_21195_(mobEffectInstance.m_19544_())) {
            return;
        }
        BossText.flavorWithHint(player, ZicaiCombatRules.stripFlavor(this.getVariant()), "\u4f60\u88ab\u62bd\u6389\u4e86\u4e00\u4e2a\u589e\u76ca\uff1a" + mobEffectInstance.m_19544_().m_19482_().getString(), "\u00a75\u3010\u672b\u5f71\u6270\u52a8\u3011\u00a77\u5931\u53bb \u00a7c" + mobEffectInstance.m_19544_().m_19482_().getString());
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            MobEffectInstance mobEffectInstance2;
            mobEffectInstance2 = (ServerLevel)level;
            mobEffectInstance2.m_8767_((ParticleOptions)ParticleTypes.f_123765_, player.m_20185_(), player.m_20186_() + 1.2, player.m_20189_(), 18, 0.35, 0.5, 0.35, 0.05);
            mobEffectInstance2.m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11852_, SoundSource.HOSTILE, 1.0f, 1.4f);
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u589e\u76ca\u5265\u79bb\uff1a\u73a9\u5bb6 {}\uff0c\u589e\u76ca {}", (Object)player.m_7755_().getString(), (Object)mobEffectInstance.m_19544_().m_19482_().getString());
    }

    private void onWeaponHit(LivingEntity livingEntity) {
        ZicaiMeleePassive.apply(this, livingEntity);
        net.farmingtales.ftboss.qingyilight.LightHooks.melee(this, livingEntity);
        net.farmingtales.ftboss.zheergenfx.LightHooks.melee(this, livingEntity);
        LightHooks.melee(this, livingEntity);
        NativeSignals.hit(this, livingEntity);
        switch (this.getVariant()) {
            case 6: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 100, 2));
                livingEntity.m_9236_().m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), SoundEvents.f_11998_, SoundSource.HOSTILE, 1.0f, 0.9f);
                break;
            }
            case 7: {
                ServerLevel serverLevel;
                ServerPlayer serverPlayer;
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19615_, 160, 1));
                ShadowBlindEffect.applyTo(livingEntity, 300);
                if (livingEntity instanceof ServerPlayer) {
                    serverPlayer = (ServerPlayer)livingEntity;
                    Level level = livingEntity.m_9236_();
                    this.applyShadowVoid((LivingEntity)serverPlayer, level instanceof ServerLevel ? (serverLevel = (ServerLevel)level) : null);
                }
                livingEntity.m_9236_().m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), SoundEvents.f_215750_, SoundSource.HOSTILE, 0.9f, 1.4f);
                serverLevel = livingEntity.m_9236_();
                if (!(serverLevel instanceof ServerLevel)) break;
                serverPlayer = serverLevel;
                serverPlayer.m_8767_((ParticleOptions)ParticleTypes.f_123765_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 16, 0.3, 0.4, 0.3, 0.04);
                break;
            }
            case 8: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 160, 1));
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 120, 1));
                livingEntity.m_9236_().m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), SoundEvents.f_12291_, SoundSource.HOSTILE, 1.0f, 0.8f);
                Level level = livingEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break;
                ServerLevel serverLevel = (ServerLevel)level;
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123753_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 14, 0.3, 0.4, 0.3, 0.06);
                break;
            }
            case 5: {
                QingyiConfluenceSkill.mark(livingEntity, 160);
                switch (this.qingyiIndex) {
                    case 0: {
                        livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 160, 1));
                        livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 160, 1));
                        break;
                    }
                    case 1: {
                        livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19615_, 160, 1));
                        break;
                    }
                    case 2: {
                        livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19599_, 160, 1));
                        ExtraDebuffs.bleed(livingEntity, 160, 1);
                        break;
                    }
                    case 3: {
                        livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19619_, 160, 0));
                        livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 160, 2));
                        break;
                    }
                }
                livingEntity.m_9236_().m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), SoundEvents.f_12514_, SoundSource.HOSTILE, 1.0f, 0.8f + (float)this.qingyiIndex * 0.15f);
                Level level = livingEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break;
                ServerLevel serverLevel = (ServerLevel)level;
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123808_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 12, 0.3, 0.4, 0.3, 0.15);
                break;
            }
            case 4: {
                livingEntity.m_20254_(5);
                ExtraDebuffs.blueFire(livingEntity, 100);
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 160, 1));
                livingEntity.m_9236_().m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), SoundEvents.f_11937_, SoundSource.HOSTILE, 1.1f, 1.2f);
                Level level = livingEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break;
                ServerLevel serverLevel = (ServerLevel)level;
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123744_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 14, 0.3, 0.4, 0.3, 0.03);
                break;
            }
            case 3: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19615_, 160, 1));
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 160, 1));
                livingEntity.m_9236_().m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), SoundEvents.f_12313_, SoundSource.HOSTILE, 0.9f, 0.7f);
                Level level = this.m_9236_();
                if (!(level instanceof ServerLevel)) break;
                ServerLevel serverLevel = (ServerLevel)level;
                if (!(livingEntity instanceof Player)) break;
                Player player = (Player)livingEntity;
                int n = ArmorShred.addStack((LivingEntity)player, serverLevel.m_46467_());
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123798_, player.m_20185_(), player.m_20186_() + 1.0, player.m_20189_(), 6, 0.25, 0.3, 0.25, 0.02);
                player.m_5661_((Component)Component.m_237113_((String)String.format("\u00a7c\u62a4\u7532\u4fb5\u8680 %d/%d \u00a77(%d \u62a4\u7532)\u00a78 \u00b7 10 \u79d2", n, 5, (int)(-5.0 * (double)n))), true);
                if (n >= 5) {
                    this.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 160, 1));
                    serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12363_, SoundSource.HOSTILE, 1.1f, 1.3f);
                    FTBossMod.LOGGER.info("[FTBoss][Easter] \u529f\u5e95 \u7834\u7532\u53e0\u6ee1 {} \u5c42 -> \u81ea\u8eab\u901f\u5ea6 II 8 \u79d2", (Object)n);
                }
                this.addBloodRage(serverLevel, (LivingEntity)player);
                this.gongdiSkills.onHit((LivingEntity)player);
                break;
            }
            case 2: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 200, 1));
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19599_, 200, 1));
                livingEntity.m_9236_().m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), SoundEvents.f_12347_, SoundSource.HOSTILE, 0.8f, 1.3f);
                break;
            }
            case 1: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19612_, 160, 1));
                livingEntity.m_9236_().m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_(), livingEntity.m_20189_(), SoundEvents.f_11852_, SoundSource.HOSTILE, 0.9f, 1.45f);
                Level level = livingEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break;
                ServerLevel serverLevel = (ServerLevel)level;
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 22, 0.4, 0.6, 0.4, 0.25);
                break;
            }
            case 0: {
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 200, 1));
                livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 200, 1));
                livingEntity.m_9236_().m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), SoundEvents.f_12313_, SoundSource.HOSTILE, 0.7f, 0.6f);
                this.youziAddHit(livingEntity, "\u6b66\u5668\u547d\u4e2d");
                break;
            }
        }
    }

    private void summonMiniClones(ServerLevel serverLevel) {
        EasterBossEntity easterBossEntity;
        int n = YouziSummonScaling.playerCount(serverLevel, this);
        int n2 = YouziSummonRules.cap(n);
        int n3 = YouziSummonRules.count(n);
        int n4 = 0;
        for (EasterBossEntity easterBossEntity2 : serverLevel.m_45976_(EasterBossEntity.class, this.m_20191_().m_82400_(40.0))) {
            if (!easterBossEntity2.isMini() || !easterBossEntity2.m_6084_() || easterBossEntity2.getVariant() != this.getVariant()) continue;
            ++n4;
        }
        if (n4 >= n2) {
            this.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 200, 1));
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u67da\u5b50\u5206\u8eab\u5df2\u8fbe\u4e0a\u9650 {} \u4e2a -> \u6539\u4e3a\u81ea\u8eab\u901f\u5ea6 II", (Object)n4);
            return;
        }
        int n5 = 0;
        for (int i = 0; i < n3 && n4 + n5 < n2 && (easterBossEntity = (EasterBossEntity)((EntityType)FTEntities.EASTER_BOSS.get()).m_20615_((Level)serverLevel)) != null; ++i) {
            double d = this.f_19796_.m_188500_() * Math.PI * 2.0;
            double d2 = this.m_20185_() + Math.cos(d) * 2.6;
            double d3 = this.m_20189_() + Math.sin(d) * 2.6;
            easterBossEntity.m_7678_(d2, this.m_20186_(), d3, this.f_19796_.m_188501_() * 360.0f, 0.0f);
            easterBossEntity.setVariant(this.getVariant());
            easterBossEntity.setMini(true);
            easterBossEntity.equipMiniWeapon(this.f_19796_.m_188503_(MINI_WEAPONS.length));
            easterBossEntity.copyBuffsFrom(this);
            easterBossEntity.m_6710_(this.m_5448_());
            easterBossEntity.setManorSummonerId(this.manorSummonerId);
            ManorFightMembers.inherit((Entity)this, (Entity)easterBossEntity);
            EasterBossEntity easterBossEntity3 = easterBossEntity;
            YouziBodyNetwork.summoned(serverLevel.m_7967_((Entity)easterBossEntity3), this, easterBossEntity3);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123759_, d2, this.m_20186_() + 0.9, d3, 20, 0.35, 0.6, 0.35, 0.05);
            ++n5;
        }
        if (n5 > 0) {
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12644_, SoundSource.HOSTILE, 1.1f, 1.5f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u67da\u5b50\u300c\u81ea\u6211\u53e0\u5f71\u300d\u53ec\u51fa {} \u4e2a\u8ff7\u4f60\u5206\u8eab(\u7b2c {} \u6b21\u547d\u4e2d\u89e6\u53d1)", (Object)n5, (Object)this.youziHits);
        }
    }

    private static boolean isMagicDamage(DamageSource damageSource) {
        try {
            String string;
            String string2;
            if (damageSource.m_269533_(DamageTypeTags.f_268731_)) {
                return true;
            }
            Optional optional = damageSource.m_269150_().m_203543_();
            if (optional.isPresent() && ((string2 = ((ResourceKey)optional.get()).m_135782_().m_135815_().toLowerCase(Locale.ROOT)).contains("magic") || string2.contains("spell") || string2.contains("sonic"))) {
                return true;
            }
            string2 = damageSource.m_19385_();
            if (string2 != null && ((string = string2.toLowerCase(Locale.ROOT)).contains("magic") || string.contains("spell"))) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    private static boolean isVoidDamage(DamageSource damageSource) {
        try {
            return damageSource.m_276093_(DamageTypes.f_268724_) || "outOfWorld".equals(damageSource.m_19385_());
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public static boolean isRealPlayerDamage(DamageSource damageSource) {
        return EasterBossEntity.realAttacker(damageSource) != null;
    }

    public static ServerPlayer realAttacker(DamageSource damageSource) {
        Projectile projectile;
        Entity entity = damageSource.m_7639_();
        if (entity == null) {
            entity = damageSource.m_7640_();
        }
        int n = 0;
        while (entity instanceof Projectile) {
            projectile = (Projectile)entity;
            if (n >= 8) break;
            Entity entity2 = projectile.m_19749_();
            if (entity2 == null || entity2 == entity) {
                return null;
            }
            entity = entity2;
            ++n;
        }
        return entity instanceof ServerPlayer && !((projectile = (ServerPlayer)entity) instanceof FakePlayer) ? projectile : null;
    }

    private void rescueFromVoid() {
        ManorFight.rescue(this);
    }

    private boolean blocksCombatDamage() {
        return HiddenBossIntro.isActing(this.m_20148_()) || this.m_20147_() && !ManorFight.isFinaleMember((LivingEntity)this);
    }

    public boolean isXuanlanSkyCasting() {
        return this.xuanlanSky != null && this.xuanlanSky.active();
    }

    public boolean isXuanlanSkyHidden() {
        return (Boolean)this.f_19804_.m_135370_(DATA_XUANLAN_SKY_HIDDEN);
    }

    public void setXuanlanSkyHidden(boolean bl) {
        this.f_19804_.m_135381_(DATA_XUANLAN_SKY_HIDDEN, (Object)bl);
    }

    public boolean isManorIntroHidden() {
        return (Boolean)this.f_19804_.m_135370_(DATA_MANOR_INTRO_HIDDEN);
    }

    public void setManorIntroHidden(boolean bl) {
        this.f_19804_.m_135381_(DATA_MANOR_INTRO_HIDDEN, (Object)bl);
    }

    public void m_5634_(float f) {
        if (this.isXuanlanSkyCasting()) {
            return;
        }
        if (this.getVariant() == 4) {
            f *= 0.6666667f;
        }
        super.m_5634_(f);
    }

    public void m_21153_(float f) {
        if (this.isXuanlanSkyCasting() && f > this.m_21223_()) {
            return;
        }
        super.m_21153_(f);
    }

    public boolean m_6469_(DamageSource damageSource, float f) {
        ServerPlayer serverPlayer;
        boolean bl = damageSource.m_276093_(DamageTypes.f_286979_);
        if (!bl && this.isQizaiProtectedCasting()) {
            return false;
        }
        if (!bl && this.isXuanlanSkyCasting()) {
            return false;
        }
        if (!bl && this.blocksCombatDamage()) {
            return false;
        }
        if (!this.m_9236_().f_46443_) {
            if (!ManorCombatRules.allowsDamage(EasterBossEntity.isVoidDamage(damageSource), bl, EasterBossEntity.isRealPlayerDamage(damageSource))) {
                return false;
            }
            if (!(bl || Float.isFinite(f) && !(f <= 0.0f))) {
                return false;
            }
        }
        if (!this.m_9236_().f_46443_ && EasterBossEntity.isThornsDamage(damageSource)) {
            return false;
        }
        if (!this.m_9236_().f_46443_ && this.getVariant() == 10 && !this.isMini() && !bl && this.xueliMaids.shielded()) {
            this.xueliMaids.onBlocked(damageSource);
            return false;
        }
        if (!this.m_9236_().f_46443_ && f > 0.0f && EasterBossEntity.isMagicDamage(damageSource)) {
            f *= 0.6f;
        }
        if (!this.m_9236_().f_46443_ && BossDamageGate.isImmune(damageSource)) {
            return false;
        }
        if (!this.m_9236_().f_46443_ && this.isImpossibleChallenge() && !bl && (f = ManorFinalDamage.legacyGate((LivingEntity)this, damageSource, f)) <= 0.0f) {
            return false;
        }
        if (!this.m_9236_().f_46443_ && (serverPlayer = EasterBossEntity.realAttacker(damageSource)) != null) {
            ManorFight.attackedBy(this, serverPlayer);
        }
        if (!this.m_9236_().f_46443_ && f > 0.5f) {
            this.youziAddHit(null, "\u88ab\u51fb\u4e2d");
        }
        if (!this.m_9236_().f_46443_ && this.m_9236_() instanceof ServerLevel && (ManorFight.isManaged((LivingEntity)this) || this.getVariant() == 9)) {
            boolean bl2 = this.manorHurt(damageSource, f);
            if (bl2 && this.isImpossibleChallenge()) {
                BossDamageGate.onHit((LivingEntity)this);
            }
            if (bl2) {
                this.hitFeedback(damageSource);
            }
            return bl2;
        }
        boolean bl3 = super.m_6469_(damageSource, ManorDefense.reduceAfterSharing(this.sparrowShare(f, damageSource), this, damageSource));
        if (bl3 && this.isImpossibleChallenge()) {
            BossDamageGate.onHit((LivingEntity)this);
        }
        if (bl3) {
            this.hitFeedback(damageSource);
        }
        return bl3;
    }

    private void hitFeedback(DamageSource damageSource) {
        Vec3 vec3;
        SparrowHitEvasion.confirmedHit(this, damageSource);
        ManorDefense.confirmedHit(this, damageSource);
        if (this.m_9236_().f_46443_) {
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        Entity entity = level = damageSource == null ? null : damageSource.m_7639_();
        if (level != null && level != this && (vec3 = this.m_20182_().m_82546_(level.m_20182_())).m_82556_() > 1.0E-4) {
            Vec3 vec32 = vec3.m_82541_();
            BossKnockback.push((Entity)this, vec32.f_82479_ * 0.45, 0.12, vec32.f_82481_ * 0.45);
            this.f_19864_ = true;
        }
        this.m_6053_(1.0f);
    }

    private static boolean isThornsDamage(DamageSource damageSource) {
        if (damageSource == null) {
            return false;
        }
        String string = damageSource.m_19385_();
        return string != null && string.contains("thorns");
    }

    private boolean manorHurt(DamageSource damageSource, float f) {
        if (damageSource != null && damageSource.m_276093_(DamageTypes.f_286979_)) {
            this.m_21153_(0.0f);
            this.m_6667_(damageSource);
            this.m_146870_();
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} \u88ab\u65e0\u89c6\u65e0\u654c\u7684\u4f24\u5bb3\u7ec8\u7ed3({}) -> \u7acb\u5373\u79fb\u9664", (Object)NAMES[this.getVariant()], (Object)damageSource.m_19385_());
            return true;
        }
        if (this.m_21224_()) {
            return false;
        }
        if (this.f_19802_ > 0) {
            return false;
        }
        float[] fArray = ManorFinalDamage.settle(this, damageSource, f);
        if (fArray == null) {
            return false;
        }
        float f2 = fArray[0];
        float f3 = fArray[1];
        this.f_19802_ = 10;
        this.f_20916_ = 10;
        this.f_20917_ = 10;
        if (f2 > 0.0f) {
            EasterBossEntity easterBossEntity = this;
            float f4 = this.m_21223_() - f2;
            ManorRaidCombat.beforeHealthWrite((LivingEntity)easterBossEntity, f4, damageSource);
            easterBossEntity.m_21153_(f4);
        }
        BossHitFeedback.confirmed((LivingEntity)this, damageSource, f2 + f3, f2 <= 0.0f && f3 > 0.0f);
        this.tickAppleStages();
        if (this.m_21223_() <= 0.0f) {
            this.m_21153_(0.0f);
            this.m_6667_(damageSource);
            this.m_146870_();
        }
        return true;
    }

    public boolean m_7301_(MobEffectInstance mobEffectInstance) {
        String string;
        ResourceLocation resourceLocation;
        if (BossBuffImmunity.blocked(mobEffectInstance)) {
            return ManorStatusImmunity.allowed(false, (LivingEntity)this, mobEffectInstance);
        }
        if (this.lemonSacrifice != null && this.lemonSacrifice.active() && !mobEffectInstance.m_19544_().m_19486_()) {
            return ManorStatusImmunity.allowed(false, (LivingEntity)this, mobEffectInstance);
        }
        MobEffect mobEffect = mobEffectInstance.m_19544_();
        if (mobEffect != null && (resourceLocation = ForgeRegistries.MOB_EFFECTS.getKey((Object)mobEffect)) != null && ((string = resourceLocation.m_135815_().toLowerCase(Locale.ROOT)).contains("bleed") || string.contains("hemorrhage") || string.contains("blood_loss"))) {
            return ManorStatusImmunity.allowed(false, (LivingEntity)this, mobEffectInstance);
        }
        return ManorStatusImmunity.allowed(super.m_7301_(mobEffectInstance), (LivingEntity)this, mobEffectInstance);
    }

    public void onBuffGainedHook(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance == null || mobEffectInstance.m_19544_() == null) {
            return;
        }
        if (this.quietBuffs) {
            return;
        }
        if (this.chainGuard || this.m_9236_().f_46443_) {
            return;
        }
        if (!mobEffectInstance.m_19544_().m_19486_()) {
            return;
        }
        if (!this.sparrowNearby()) {
            return;
        }
        if (this.f_19797_ - this.lastChainBuff < 40) {
            return;
        }
        this.lastChainBuff = this.f_19797_;
        this.onBuffGained();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void onBuffGained() {
        this.chainGuard = true;
        try {
            Level level;
            MobEffect mobEffect;
            int n;
            int n2 = n = this.f_19796_.m_188503_(CHAIN_BUFF_POOL.length);
            for (int i = 0; i < 8 && n2 == n; ++i) {
                n2 = this.f_19796_.m_188503_(CHAIN_BUFF_POOL.length);
            }
            this.m_7292_(new MobEffectInstance(CHAIN_BUFF_POOL[n], 300, this.buffAmp(1), true, false, false));
            this.m_7292_(new MobEffectInstance(CHAIN_BUFF_POOL[n2], 300, this.buffAmp(1), true, false, false));
            Player player = this.nearestPlayerForBuff();
            String string = "(\u9644\u8fd1\u65e0\u73a9\u5bb6)";
            if (player != null) {
                mobEffect = RANDOM_DEBUFF_POOL[this.f_19796_.m_188503_(RANDOM_DEBUFF_POOL.length)];
                player.m_7292_(new MobEffectInstance(mobEffect, 200, 1));
                string = mobEffect.m_19482_().getString();
            }
            if ((level = this.m_9236_()) instanceof ServerLevel) {
                mobEffect = (ServerLevel)level;
                mobEffect.m_8767_((ParticleOptions)ParticleTypes.f_123767_, this.m_20185_(), this.m_20186_() + 1.2, this.m_20189_(), 12, 0.4, 0.5, 0.4, 0.15);
                mobEffect.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11887_, SoundSource.HOSTILE, 0.8f, 1.5f);
            }
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} \u8fde\u9501\u589e\u76ca: \u989d\u5916 +{} / +{} ; \u7ed9\u73a9\u5bb6 {} \u6302\u4e0a {}", new Object[]{NAMES[this.getVariant()], CHAIN_BUFF_POOL[n].m_19482_().getString(), CHAIN_BUFF_POOL[n2].m_19482_().getString(), player == null ? "(\u65e0)" : player.m_7755_().getString(), string});
        }
        finally {
            this.chainGuard = false;
        }
    }

    public void runQuietBuffs(Runnable runnable) {
        this.quietBuffs = true;
        try {
            runnable.run();
        }
        finally {
            this.quietBuffs = false;
        }
    }

    private Player nearestPlayerForBuff() {
        Player player = null;
        double d = 1024.0;
        for (Player player2 : this.m_9236_().m_6907_()) {
            double d2;
            if (!player2.m_6084_() || !((d2 = player2.m_20280_((Entity)this)) < d)) continue;
            d = d2;
            player = player2;
        }
        return player;
    }

    private void sparrowTick() {
        if (XuanlanEncounterLimits.stopSupport(this)) {
            return;
        }
        Object object = this.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        BossBarVisibility.visible(this.bossBar, false);
        this.m_20242_(false);
        if (this.m_21255_()) {
            this.m_20115_(7, false);
        }
        if (this.m_5448_() != null) {
            this.m_6710_(null);
        }
        if (!this.isImpossibleChallenge() && ++this.sparrowLife > 1800) {
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 20, 0.3, 0.4, 0.3, 0.05);
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12188_, SoundSource.HOSTILE, 1.0f, 1.6f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u9ebb\u96c0\u652f\u63f4\u7ed3\u675f, \u98de\u8d70\u4e86");
            EasterBossEntity easterBossEntity = this;
            MaqueSupportHooks.departure((Entity)easterBossEntity);
            easterBossEntity.m_146870_();
            return;
        }
        object = XuanlanEncounterLimits.supportTarget(SparrowHitEvasion.prepare(this.supportedBoss(), this));
        SparrowDisruption.tick(this, (EasterBossEntity)object);
        SparrowBorrowedSpells.tick(this, (EasterBossEntity)object);
        if (object == null) {
            return;
        }
        double d = this.m_20270_((Entity)object);
        if (SparrowHitEvasion.evadeSupport(this.impossibleKiting, this)) {
            this.m_21563_().m_24960_((Entity)object, 30.0f, 30.0f);
        } else if (d > 5.0) {
            this.m_21573_().m_5624_((Entity)object, 1.15);
        } else {
            this.m_21573_().m_26573_();
            this.m_21563_().m_24960_((Entity)object, 30.0f, 30.0f);
        }
        if (this.f_19797_ % 60 == 0) {
            if (!this.isCastingLike()) {
                this.playCast(HiddenBossCastRules.quicker(24), 4);
            }
            this.beginSkillCue(object.m_20182_(), 2.0, 12, false);
            this.releaseSkillCue(object.m_20182_());
            float f = 0.0f;
            StringBuilder stringBuilder = new StringBuilder();
            ((EasterBossEntity)object).runQuietBuffs(() -> this.lambda$sparrowTick$0((EasterBossEntity)object, f, stringBuilder));
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123750_, object.m_20185_(), object.m_20186_() + 2.0, object.m_20189_(), 8, 0.4, 0.4, 0.4, 0.03);
            serverLevel.m_6263_(null, object.m_20185_(), object.m_20186_(), object.m_20189_(), SoundEvents.f_12188_, SoundSource.HOSTILE, 1.0f, 1.4f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u9ebb\u96c0\u652f\u63f4: {} \u83b7\u5f97 {} \u4e2a\u968f\u673a\u589e\u76ca\uff08\u4e0d\u518d\u7ed9\u4e88\u767e\u5206\u6bd4\u6cbb\u7597\uff09: {}", new Object[]{NAMES[((EasterBossEntity)object).getVariant()], 3, stringBuilder.toString().trim()});
        }
        if (this.f_19797_ % 100 == 0) {
            this.cleanseOtherBoss(serverLevel, (EasterBossEntity)object);
        }
        if (this.f_19797_ % 20 == 0) {
            this.shareBuffsWith((EasterBossEntity)object);
        }
    }

    private void shareBuffsWith(EasterBossEntity easterBossEntity) {
        if (easterBossEntity == null || !easterBossEntity.m_6084_() || easterBossEntity.m_21224_()) {
            return;
        }
        int n = 0;
        for (MobEffectInstance mobEffectInstance : new ArrayList(easterBossEntity.m_21220_())) {
            MobEffectInstance mobEffectInstance2;
            if (mobEffectInstance.m_19544_() == null || !mobEffectInstance.m_19544_().m_19486_() || (mobEffectInstance2 = this.m_21124_(mobEffectInstance.m_19544_())) != null && mobEffectInstance2.m_19564_() >= mobEffectInstance.m_19564_() && mobEffectInstance2.m_19557_() > 60) continue;
            MaqueSupportHooks.empower((LivingEntity)this, new MobEffectInstance(mobEffectInstance.m_19544_(), 300, mobEffectInstance.m_19564_(), true, false, false), (Entity)easterBossEntity, 5);
            ++n;
        }
        Object object = new int[]{0};
        easterBossEntity.runQuietBuffs(() -> this.lambda$shareBuffsWith$1(easterBossEntity, (int[])object));
        if (n > 0 || object[0] > 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u9ebb\u96c0\u4e0e {} \u5171\u4eab\u589e\u76ca: \u9ebb\u96c0\u5f97\u5230 {} \u4e2a, {} \u5f97\u5230 {} \u4e2a", new Object[]{NAMES[easterBossEntity.getVariant()], n, NAMES[easterBossEntity.getVariant()], (int)object[0]});
        }
    }

    private EasterBossEntity nearbySparrow() {
        EasterBossEntity easterBossEntity = null;
        double d = 2304.0;
        for (EasterBossEntity easterBossEntity2 : this.m_9236_().m_45976_(EasterBossEntity.class, this.m_20191_().m_82400_(48.0))) {
            double d2;
            if (easterBossEntity2 == this || easterBossEntity2.getVariant() != 9 || !easterBossEntity2.m_6084_() || easterBossEntity2.m_21224_() || !((d2 = easterBossEntity2.m_20280_((Entity)this)) < d)) continue;
            d = d2;
            easterBossEntity = easterBossEntity2;
        }
        return easterBossEntity;
    }

    private float sparrowShare(float f, DamageSource damageSource) {
        if (f <= 0.0f || this.getVariant() == 9 || this.isMini()) {
            return f;
        }
        Object object = this.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return f;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = this.nearbySparrow();
        if (object == null || ((EasterBossEntity)object).blocksCombatDamage()) {
            return f;
        }
        float f2 = f * 0.5f;
        MaqueSupportHooks.intercept((Entity)this, (Entity)object, f2);
        float f3 = object.m_21223_() - f2;
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123798_, object.m_20185_(), object.m_20186_() + 1.0, object.m_20189_(), 6, 0.3, 0.4, 0.3, 0.03);
        if (f3 <= 0.0f) {
            if (object.m_20147_()) {
                ((EasterBossEntity)object).m_21153_(1.0f);
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u9ebb\u96c0\u5728\u65e0\u654c\u671f(\u6f14\u51fa/\u7ec8\u7ae0)\u66ff {} \u5206\u644a\u4e86 {} \u70b9, \u4e0d\u6b7b", (Object)NAMES[this.getVariant()], (Object)String.format("%.1f", Float.valueOf(f2)));
                return f - f2;
            }
            ((EasterBossEntity)object).m_21153_(0.0f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u9ebb\u96c0\u66ff {} \u5206\u644a\u4f24\u5bb3\u65f6\u9635\u4ea1(\u8fd9\u4e00\u4e0b\u5206\u644a\u4e86 {} \u70b9)", (Object)NAMES[this.getVariant()], (Object)String.format("%.1f", Float.valueOf(f2)));
            ((EasterBossEntity)object).m_6667_(damageSource == null ? this.m_269291_().m_269264_() : damageSource);
            object.m_146870_();
        } else {
            ((EasterBossEntity)object).m_21153_(f3);
        }
        return f - f2;
    }

    private float youziMinionShare(float f, DamageSource damageSource) {
        if (f <= 0.0f || this.getVariant() != 0 || this.isMini()) {
            return f;
        }
        Object object = this.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return f;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = new ArrayList();
        for (EasterBossEntity easterBossEntity : serverLevel.m_45976_(EasterBossEntity.class, this.m_20191_().m_82400_(64.0))) {
            if (easterBossEntity == this || !easterBossEntity.isMini() || easterBossEntity.getVariant() != 0 || !easterBossEntity.m_6084_() || easterBossEntity.m_21224_() || easterBossEntity.blocksCombatDamage()) continue;
            object.add(easterBossEntity);
        }
        if (object.isEmpty()) {
            return f;
        }
        float f2 = f * 0.1f;
        float f3 = f2 / (float)object.size();
        Iterator iterator = object.iterator();
        while (iterator.hasNext()) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)iterator.next();
            float f4 = easterBossEntity.m_21223_() - f3;
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123798_, easterBossEntity.m_20185_(), easterBossEntity.m_20186_() + 1.0, easterBossEntity.m_20189_(), 4, 0.3, 0.4, 0.3, 0.03);
            if (f4 <= 0.0f) {
                if (easterBossEntity.m_20147_()) {
                    EasterBossEntity easterBossEntity2 = easterBossEntity;
                    YouziCombatEvents.sharedDamage(this, easterBossEntity2);
                    easterBossEntity2.m_21153_(1.0f);
                    continue;
                }
                EasterBossEntity easterBossEntity3 = easterBossEntity;
                YouziCombatEvents.sharedDamage(this, easterBossEntity3);
                easterBossEntity3.m_21153_(0.0f);
                easterBossEntity.m_6667_(damageSource == null ? this.m_269291_().m_269264_() : damageSource);
                easterBossEntity.m_146870_();
                continue;
            }
            EasterBossEntity easterBossEntity4 = easterBossEntity;
            YouziCombatEvents.sharedDamage(this, easterBossEntity4);
            easterBossEntity4.m_21153_(f4);
        }
        return f - f2;
    }

    private EasterBossEntity supportedBoss() {
        EasterBossEntity easterBossEntity = null;
        double d = 1024.0;
        for (EasterBossEntity easterBossEntity2 : this.m_9236_().m_45976_(EasterBossEntity.class, this.m_20191_().m_82400_(32.0))) {
            double d2;
            if (easterBossEntity2 == this || !easterBossEntity2.m_6084_() || easterBossEntity2.m_21224_() || easterBossEntity2.isMini() || easterBossEntity2.getVariant() == 9 || !((d2 = easterBossEntity2.m_20280_((Entity)this)) < d)) continue;
            d = d2;
            easterBossEntity = easterBossEntity2;
        }
        return easterBossEntity;
    }

    private void cleanseOtherBoss(ServerLevel serverLevel, EasterBossEntity easterBossEntity) {
        Object object = null;
        for (Object object2 : serverLevel.m_45976_(EasterBossEntity.class, this.m_20191_().m_82400_(32.0))) {
            if (object2 == this || !object2.m_6084_() || ((EasterBossEntity)object2).getVariant() == 9 || object2 == easterBossEntity || !EasterBossEntity.hasDebuff((LivingEntity)object2)) continue;
            object = object2;
            break;
        }
        if (object == null) {
            object = easterBossEntity;
        }
        int n = 0;
        for (MobEffectInstance mobEffectInstance : new ArrayList(object.m_21220_())) {
            if (mobEffectInstance.m_19544_() == null || mobEffectInstance.m_19544_().m_19486_()) continue;
            MaqueSupportHooks.cleanse((LivingEntity)object, mobEffectInstance.m_19544_(), (Entity)this);
            ++n;
        }
        if (n > 0) {
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123748_, object.m_20185_(), object.m_20186_() + 1.5, object.m_20189_(), 20, 0.5, 0.6, 0.5, 0.05);
            serverLevel.m_6263_(null, object.m_20185_(), object.m_20186_(), object.m_20189_(), SoundEvents.f_12188_, SoundSource.HOSTILE, 1.0f, 1.8f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u9ebb\u96c0\u51c0\u5316: \u6e05\u6389 {} \u7684 {} \u4e2a\u51cf\u76ca", (Object)NAMES[((EasterBossEntity)object).getVariant()], (Object)n);
        }
    }

    private static boolean hasDebuff(LivingEntity livingEntity) {
        for (MobEffectInstance mobEffectInstance : livingEntity.m_21220_()) {
            if (mobEffectInstance.m_19544_() == null || mobEffectInstance.m_19544_().m_19486_()) continue;
            return true;
        }
        return false;
    }

    private void youziAddHit(LivingEntity livingEntity, String string) {
        if (this.getVariant() != 0 || this.isMini()) {
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        ++this.youziHits;
        int n = this.youziHits % 3;
        if (n != 0) {
            return;
        }
        int n2 = this.f_19797_ - this.lastMiniSummon;
        if (n2 < 40) {
            if (this.f_19797_ - this.lastYouziLog >= 100) {
                this.lastYouziLog = this.f_19797_;
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u67da\u5b50\u53e0\u5f71\u5df2\u6ee1\u4f46\u51b7\u5374\u4e2d({}/{} tick), \u7d2f\u8ba1\u547d\u4e2d {}", new Object[]{n2, 40, this.youziHits});
            }
            return;
        }
        this.lastMiniSummon = this.f_19797_;
        if (livingEntity != null) {
            livingEntity.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 72.0f);
        }
        YouziCombatEvents.resonance(this, livingEntity);
        this.summonMiniClones(serverLevel);
    }

    /*
     * Unable to fully structure code
     */
    public void m_8119_() {
        block116: {
            block115: {
                if (ZhuniStage.hold(this)) {
                    return;
                }
                CrossGuard.stabilize(this);
                ManorCombatContinuity.repair(this);
                super.m_8119_();
                ManorGroundSupport.tick(this);
                if (this.m_9236_().f_46443_) {
                    ManorFinaleCombat.tick(this);
                    QingyiCastSync.rainTick(this, this.qingyiRain);
                    return;
                }
                if (this.getVariant() == 2) {
                    this.doraCombatTick();
                }
                if (this.m_21224_()) {
                    this.qizaiMagic.forceCancel();
                    this.cancelSkillWindups();
                    ManorFinaleCombat.tick(this);
                    QingyiCastSync.rainTick(this, this.qingyiRain);
                    return;
                }
                var2_1 = this.m_9236_();
                if (var2_1 instanceof ServerLevel) {
                    var1_3 = (ServerLevel)var2_1;
                    if (this.getVariant() == 6 && this.xuanlanSky.tick(var1_3)) {
                        this.bossBar.m_142711_(Math.max(0.0f, Math.min(1.0f, this.m_21223_() / this.m_21233_())));
                        BossMusic.request(var1_3, (LivingEntity)this);
                        ManorFinaleCombat.tick(this);
                        QingyiCastSync.rainTick(this, this.qingyiRain);
                        return;
                    }
                }
                if (!this.m_21525_()) break block115;
                var3_5 = this.m_9236_();
                if (!(var3_5 instanceof ServerLevel)) ** GOTO lbl-1000
                var2_1 = (ServerLevel)var3_5;
                if (this.qizaiMagic.uninterruptible()) {
                    this.qizaiMagic.tick((ServerLevel)var2_1);
                } else lbl-1000:
                // 2 sources

                {
                    this.cancelSkillWindups();
                }
                break block116;
            }
            var2_1 = this.m_9236_();
            if (var2_1 instanceof ServerLevel) {
                var1_3 = (ServerLevel)var2_1;
                this.tickSkillWindups(var1_3);
            }
        }
        var2_1 = this.m_9236_();
        if (var2_1 instanceof ServerLevel) {
            var1_3 = (ServerLevel)var2_1;
            this.qizaiArsenal.tick(var1_3, this.qizaiMagic.skyCasting(), this.qizaiMagic.busy());
        }
        if ((var2_1 = this.m_9236_()) instanceof ServerLevel) {
            var1_3 = (ServerLevel)var2_1;
            this.tickQingyiFallingTrident(var1_3);
        }
        if (this.getVariant() == 0 && !this.isMini() && this.m_6084_() && (var2_1 = this.m_9236_()) instanceof ServerLevel) {
            var1_3 = (ServerLevel)var2_1;
            EasterBossEntity.lastYouziTick = var1_3.m_46467_();
        }
        var1_4 = HiddenBossIntro.isActing(this.m_20148_());
        var2_2 = ManorFight.isFinaleMember((LivingEntity)this);
        if (!this.isQizaiUninterruptibleCasting() && ManorCombatRules.shouldThaw(this.m_21525_(), this.m_20147_(), var1_4, var2_2)) {
            if (++this.stuckTicks > 200) {
                this.stuckTicks = 0;
                this.m_20331_(false);
                this.m_21557_(false);
                FTBossMod.LOGGER.info("[FTBoss][Easter] {} {} \u6e05\u9664\u8fc7\u671f\u51bb\u7ed3\u72b6\u6001", (Object)EasterBossEntity.NAMES[this.getVariant()], (Object)this.m_20148_());
            }
        } else {
            this.stuckTicks = 0;
        }
        if (this.isMini()) {
            if (this.bombFuse > 0 && (var4_7 = this.m_9236_()) instanceof ServerLevel) {
                var3_5 = (ServerLevel)var4_7;
                this.bombFuseTick((ServerLevel)var3_5);
            }
            if (this.m_213877_() || this.m_21224_()) {
                ManorFinaleCombat.tick(this);
                QingyiCastSync.rainTick(this, this.qingyiRain);
                return;
            }
            BossBarVisibility.visible(this.bossBar, false);
            if (--this.miniLife <= 0) {
                var4_7 = this.m_9236_();
                if (var4_7 instanceof ServerLevel) {
                    var3_5 = (ServerLevel)var4_7;
                    var3_5.m_8767_((ParticleOptions)ParticleTypes.f_123796_, this.m_20185_(), this.m_20186_() + 0.6, this.m_20189_(), 18, 0.3, 0.5, 0.3, 0.08);
                    var3_5.m_8767_((ParticleOptions)ParticleTypes.f_123810_, this.m_20185_(), this.m_20186_() + 0.8, this.m_20189_(), 8, 0.25, 0.4, 0.25, 0.05);
                    var3_5.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12019_, SoundSource.HOSTILE, 0.8f, 1.7f);
                    FTBossMod.LOGGER.info("[FTBoss][Easter] {} \u8ff7\u4f60\u5206\u8eab {} \u6d88\u6563 @{},{},{}", new Object[]{EasterBossEntity.NAMES[this.getVariant()], this.m_20148_(), (int)this.m_20185_(), (int)this.m_20186_(), (int)this.m_20189_()});
                }
                this.m_146870_();
                ManorFinaleCombat.tick(this);
                QingyiCastSync.rainTick(this, this.qingyiRain);
                return;
            }
            if (this.actionTicks > 0) {
                --this.actionTicks;
            }
            if (this.castTicks > 0) {
                --this.castTicks;
            }
            ManorFinaleCombat.tick(this);
            QingyiCastSync.rainTick(this, this.qingyiRain);
            return;
        }
        if (this.f_19797_ == 1) {
            this.equipWeapon();
            this.equipArmor();
        }
        PhantomHooks.tick(this);
        if (this.phantomEchoTicks > 0 && --this.phantomEchoTicks == 0 && (var4_8 = this.m_9236_()) instanceof ServerLevel) {
            var3_5 = (ServerLevel)var4_8;
            this.firePhantomEcho((ServerLevel)var3_5);
        }
        if (this.f_19797_ % 200 == 0) {
            this.equipArmor(false);
        }
        this.xueliCrushTick();
        if (this.f_19797_ % 10 == 0) {
            this.dandanBeaconBan();
        }
        this.shadowStreakTick();
        var4_8 = this.m_9236_();
        if (var4_8 instanceof ServerLevel) {
            var3_5 = (ServerLevel)var4_8;
            this.dandanVortexTick((ServerLevel)var3_5);
            ShadowVoidSplit.maintainTick(this, (ServerLevel)var3_5);
        }
        if (!this.isMini() && (var4_8 = this.m_9236_()) instanceof ServerLevel) {
            var3_5 = (ServerLevel)var4_8;
            BossMusic.request((ServerLevel)var3_5, (LivingEntity)this);
        }
        if (!this.m_21525_() && !this.skillCommitted() && this.getVariant() != 9 && this.xueliSlamWarn <= 0 && !ManorFight.isFinaleMember((LivingEntity)this) && (var4_8 = this.m_9236_()) instanceof ServerLevel) {
            var3_5 = (ServerLevel)var4_8;
            this.huntStrayPlayer((ServerLevel)var3_5);
        }
        this.tickXueliDialogue();
        if (this.xueliRollTicks > 0) {
            this.xueliRollTick();
        }
        if (this.bombFuse > 0 && (var4_8 = this.m_9236_()) instanceof ServerLevel) {
            var3_5 = (ServerLevel)var4_8;
            this.bombFuseTick((ServerLevel)var3_5);
        }
        if (this.getVariant() == 10 && !this.isMini()) {
            this.xueliMaids.tick();
        }
        if (this.getVariant() == 10 && (var4_8 = this.m_9236_()) instanceof ServerLevel) {
            var3_5 = (ServerLevel)var4_8;
            this.xueliSlamTick((ServerLevel)var3_5);
            this.xueliStunTick((ServerLevel)var3_5);
            this.xueliCloneTick((ServerLevel)var3_5);
        }
        if ((var4_8 = this.m_9236_()) instanceof ServerLevel) {
            var3_5 = (ServerLevel)var4_8;
            this.xueliStunTick((ServerLevel)var3_5);
        }
        if (ManorFight.isFinaleMember((LivingEntity)this)) {
            if (!this.m_21525_()) {
                this.m_21557_(true);
            }
            ManorFinaleCombat.tick(this);
            QingyiCastSync.rainTick(this, this.qingyiRain);
            return;
        }
        if (this.f_19797_ % 100 == 0) {
            for (MobEffect var6_32 : EasterBossEntity.INNATE_EFFECTS) {
                if (var6_32 == MobEffects.f_19596_ && this.getVariant() == 6) continue;
                this.m_7292_(new MobEffectInstance(var6_32, 200, this.buffAmp(1), true, false, false));
            }
            if (this.getVariant() == 3) {
                var3_5 = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("armamentarium", "adamantine_effect"));
                if (var3_5 != null) {
                    this.m_7292_(new MobEffectInstance(var3_5, 200, 3, true, false, false));
                } else {
                    this.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 200, this.buffAmp(3), true, false, false));
                    this.m_7292_(new MobEffectInstance(MobEffects.f_19617_, 200, this.buffAmp(2), true, false, false));
                }
                var4_10 = this.m_21051_(Attributes.f_22278_);
                if (var4_10 != null && var4_10.m_22115_() < 0.9) {
                    var4_10.m_22100_(0.9);
                }
                if (this.f_19797_ % 600 == 0) {
                    FTBossMod.LOGGER.info("[FTBoss][Easter] \u529f\u5e95 \u4e9a\u5f53\u7532\u5957\u88c5\u6548\u679c: {}", (Object)(var3_5 == null ? "\u624b\u52a8\u7b49\u6548(\u6297\u6027III+\u5438\u6536II)" : "armamentarium:adamantine_effect"));
                }
            }
        }
        if (ManorBuffCopy.isCopyTick(this.f_19797_)) {
            this.syncPlayerBuffs();
        }
        if (this.getVariant() == 0 && !this.isMini() && this.f_19797_ % 60 == 0) {
            this.shareBuffsToMinis();
        }
        HiddenBossIntro.pumpAll();
        ManorFight.pumpAll();
        if (this.actionTicks > 0) {
            --this.actionTicks;
        }
        if (this.castTicks > 0) {
            --this.castTicks;
        }
        if (this.cleaveAtkT > 0 && --this.cleaveAtkT == 0) {
            var3_5 = this.m_21051_(Attributes.f_22281_);
            if (var3_5 != null) {
                var3_5.m_22120_(EasterBossEntity.CLEAVE_ATK_MOD);
            }
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u529f\u5e95\u300c\u65ad\u7f6a\u65a9\u300d\u653b\u51fb\u529b\u52a0\u6210\u7ed3\u675f");
        }
        this.bossBar.m_142711_(Math.max(0.0f, Math.min(1.0f, this.m_21223_() / this.m_21233_())));
        if (!this.m_9236_().f_46443_ && this.m_20186_() < (double)this.m_9236_().m_141937_() - 8.0) {
            this.rescueFromVoid();
            if (this.m_213877_()) {
                ManorFinaleCombat.tick(this);
                QingyiCastSync.rainTick(this, this.qingyiRain);
                return;
            }
        } else if (this.voidFalls > 0 && ++this.voidSafeTicks > 600) {
            this.voidFalls = 0;
        }
        BossBarVisibility.visible(this.bossBar, this.m_6084_() != false && (this.m_20147_() == false || this.m_21525_() == false));
        this.purgeTick();
        this.dandanRushTick();
        if (!this.isMini() && (var4_11 = this.m_9236_()) instanceof ServerLevel) {
            var3_5 = (ServerLevel)var4_11;
            var4_11 = this.m_5448_();
            if (var4_11 != null && var4_11.m_6084_()) {
                if (this.digT > 0) {
                    --this.digT;
                } else {
                    this.digT = 60;
                    this.digTick((ServerLevel)var3_5);
                }
            } else {
                this.digT = 0;
            }
        }
        if (this.getVariant() == 4 && this.f_19797_ % 200 == 0) {
            this.foodBuffTick();
        }
        if (this.getVariant() == 4 && !this.isMini() && this.f_19797_ % 40 == 0) {
            LemonImmuneBuffRules.purge((LivingEntity)this);
        }
        if (this.riptideTicks > 0 || this.potionDelay > 0) {
            this.qingyiTick();
        }
        if (this.reloadTicks > 0) {
            this.reloadTick();
        }
        if (this.qingyiRain > 0 && --this.qingyiRainTimer <= 0) {
            this.qingyiRainTimer = ManorBalanceHooks.tridentInterval(2, this, "rain");
            --this.qingyiRain;
            var4_12 = this.m_9236_();
            if (var4_12 instanceof ServerLevel) {
                var3_5 = (ServerLevel)var4_12;
                var4_12 = this.m_5448_();
                if (var4_12 != null && var4_12.m_6084_()) {
                    this.dropStarTrident((ServerLevel)var3_5, (LivingEntity)var4_12);
                } else {
                    this.qingyiRain = 0;
                }
            }
        }
        var3_6 = this.m_21525_();
        if (this.getVariant() == 4) {
            var5_18 = this.m_9236_();
            if (var5_18 instanceof ServerLevel) {
                var4_13 = (ServerLevel)var5_18;
                this.tryHalfHealthLemonSacrifice(var4_13);
            }
            this.lemonSacrifice.tick();
        }
        if (!var3_6 && this.getVariant() == 6 && (var5_19 = this.m_9236_()) instanceof ServerLevel) {
            var4_14 = (ServerLevel)var5_19;
            this.grantOpeningFlywheels(var4_14);
        }
        if (!var3_6 && this.getVariant() == 9) {
            this.sparrowTick();
        }
        try {
            var4_15 = ServerLifecycleHooks.getCurrentServer();
            if (var4_15 != null) {
                SparrowSupport.tick(var4_15);
            }
        }
        catch (Throwable var4_16) {
            // empty catch block
        }
        if (this.getVariant() == 4 && this.summonLife > 0 && --this.summonLife <= 0) {
            var5_20 = this.m_9236_();
            this.clearSummons((ServerLevel)(var5_20 instanceof ServerLevel != false ? (var4_15 = (ServerLevel)var5_20) : null));
        }
        var4_15 = this.m_5448_();
        if (this.skillCd > 0) {
            if (this.getVariant() == 2) {
                this.doraSkillProgress += 1.0 / this.doraTempo.cooldownMultiplier();
                var5_21 = (int)(this.doraSkillProgress + 1.0E-9);
                this.doraSkillProgress -= (double)var5_21;
                this.skillCd = Math.max(0, this.skillCd - var5_21);
            } else {
                --this.skillCd;
            }
        } else {
            this.doraSkillProgress = 0.0;
        }
        if (!(this.getVariant() != 3 || var3_6 || this.m_20147_() || this.isGiant() || var2_2 || this.skillCommitted() || this.skillCd > 0 || !(var4_15 instanceof LivingEntity) || !this.gongdiSkills.validTarget(var5_22 = (LivingEntity)var4_15, 16.875))) {
            this.skillCd = Math.max(1, ManorBossCombatRules.gongdiSkillCooldown());
            this.castSkill((LivingEntity)var4_15);
        }
        if (this.getVariant() != 3 && this.getVariant() != 11 && !var3_6 && !this.skillCommitted() && var4_15 != null && var4_15.m_6084_() && (this.getVariant() != 2 || this.burstShots <= 0 && this.reloadTicks <= 0) && (ManorCombatLanes.spellReady(this) || this.actionTicks <= 0 && this.castTicks <= 0) && this.skillCd <= 0) {
            var5_23 = 160 + this.f_19796_.m_188503_(120);
            if (this.getVariant() == 1) {
                var5_23 /= 4;
            } else if (this.getVariant() == 2 || this.getVariant() == 6) {
                var5_23 /= 2;
            } else if (this.getVariant() == 0 || this.getVariant() == 4) {
                var5_23 = 80 + this.f_19796_.m_188503_(40);
            } else if (this.getVariant() == 10) {
                var5_23 = (int)((double)var5_23 * 0.6);
            } else if (this.getVariant() == 5) {
                var5_23 = (int)((double)var5_23 * 0.3);
            }
            if (this.xuanlanBeamBusy()) {
                this.skillCd = 10;
            } else {
                this.skillCd = CadenceServer.skill(ManorBossCombatRules.skillCooldown(this.getVariant(), var5_23), this);
                this.castSkill((LivingEntity)var4_15);
            }
        }
        if (this.burstShots > 0) {
            this.burstAcc += 4.0 * this.doraTempo.attackSpeedMultiplier() * this.getChallengeDifficulty().attackRate();
            var5_24 = 6;
            while (this.burstShots > 0 && this.burstAcc >= (double)var5_24) {
                this.burstAcc -= (double)var5_24;
                this.pumpBurst();
            }
        }
        if (!var3_6 && this.getVariant() == 5 && --this.qingyiTimer <= 0) {
            this.qingyiTimer = 160;
            var6_32 = this.m_9236_();
            if (var6_32 instanceof ServerLevel) {
                var5_25 = (ServerLevel)var6_32;
                this.rotateQingyiWeapon(var5_25);
            }
        }
        if (!var3_6 && this.getVariant() == 5 && (var6_32 = this.m_9236_()) instanceof ServerLevel) {
            var5_26 = (ServerLevel)var6_32;
            this.qingyiLightningTick(var5_26);
        }
        if (!var3_6 && this.getVariant() == 6 && this.f_19797_ % 20 == 0 && (var6_32 = this.m_9236_()) instanceof ServerLevel) {
            var5_27 = (ServerLevel)var6_32;
            this.flywheelGrind(var5_27);
            v0 = var6_33 = this.countFlywheels(var5_27) >= 15;
            if (var6_33 != this.flywheelMaxed) {
                this.flywheelMaxed = var6_33;
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u6587\u7384\u84dd\u98de\u8f6e{} -> \u653b\u901f {}", (Object)(var6_33 != false ? "\u5df2\u53e0\u6ee1 25 \u4e2a" : "\u672a\u6ee1"), (Object)(var6_33 != false ? "2 tick/\u6b21(\u7ea6\u6bcf\u79d2 10 \u4e2a)" : "5 tick/\u6b21(\u7ea6\u6bcf\u79d2 4 \u4e2a)"));
            }
        }
        if (!var3_6 && this.getVariant() == 6 && (var6_34 = this.m_9236_()) instanceof ServerLevel) {
            var5_28 = (ServerLevel)var6_34;
            this.xuanlanBeamTick(var5_28);
        }
        if (!var3_6 && this.getVariant() == 6 && !this.xuanlanBeamBusy() && --this.flywheelVolleyCd <= 0 && (var6_35 = this.m_9236_()) instanceof ServerLevel) {
            var5_29 = (ServerLevel)var6_35;
            this.flywheelVolleyCd = this.getChallengeDifficulty().acceleratedTicks(400);
            this.flywheelVolley(var5_29);
        }
        if (this.flywheelShots > 0) {
            if (--this.flywheelTimer <= 0 && (var6_36 = this.m_9236_()) instanceof ServerLevel) {
                var5_30 = (ServerLevel)var6_36;
                this.throwFlywheel(var5_30);
            }
        }
        if (!var3_6) {
            var5_31 = this.m_5448_();
            if (var5_31 instanceof Player && (var6_37 = (Player)var5_31).m_6084_()) {
                this.noTargetTicks = 0;
            } else if (++this.noTargetTicks >= 200) {
                this.noTargetTicks = 0;
                if (this.getVariant() == 9) {
                    var9_39 = this.m_9236_();
                    if (var9_39 instanceof ServerLevel) {
                        var8_40 = (ServerLevel)var9_39;
                        this.teleportToAlly(var8_40);
                    }
                } else {
                    var8_41 = this.m_9236_();
                    if (var8_41 instanceof ServerLevel) {
                        var7_42 = (ServerLevel)var8_41;
                        if (!ManorFight.isManaged((LivingEntity)this)) {
                            this.grabLaggard((ServerLevel)var7_42);
                        }
                    }
                }
            }
            if (this.getVariant() == 9 && (var7_42 = this.m_9236_()) instanceof ServerLevel) {
                var6_38 = (ServerLevel)var7_42;
                this.teleportToAlly(var6_38);
            }
            if (!(this.isMini() || this.isGiant() || EasterBossEntity.RANGED[this.getVariant()] || this.getVariant() == 9)) {
                this.pearlTick();
            }
        }
        if (!this.m_9236_().f_46443_ && !var3_6) {
            if (this.playerHitIdleTicks < 0x7FFFFFFF) {
                ++this.playerHitIdleTicks;
            }
            this.manorFlyBanTick();
            this.haleyFlightTick();
        }
        ManorFinaleCombat.tick(this);
        QingyiCastSync.rainTick(this, this.qingyiRain);
    }

    private void teleportToAlly(ServerLevel serverLevel) {
        EasterBossEntity easterBossEntity = this.supportedBoss();
        if (easterBossEntity == null) {
            return;
        }
        double d = Math.sqrt(this.m_20280_((Entity)easterBossEntity));
        if (d <= 10.0) {
            return;
        }
        if (serverLevel.m_46467_() - this.lastSparrowTeleport < 60L) {
            return;
        }
        this.lastSparrowTeleport = serverLevel.m_46467_();
        double d2 = this.f_19796_.m_188500_() * Math.PI * 2.0;
        double d3 = easterBossEntity.m_20185_() + Math.cos(d2) * 3.0;
        double d4 = easterBossEntity.m_20189_() + Math.sin(d2) * 3.0;
        double d5 = this.m_20185_();
        double d6 = this.m_20186_();
        double d7 = this.m_20189_();
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, d5, d6 + 1.0, d7, 26, 0.4, 0.7, 0.4, 0.28);
        Vec3 vec3 = ManorSafeTeleport.find(serverLevel, (Entity)this, new Vec3(d3, easterBossEntity.m_20186_(), d4), 6);
        if (vec3 == null) {
            return;
        }
        ManorSafeTeleport.move(serverLevel, (Entity)this, vec3);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 26, 0.4, 0.7, 0.4, 0.28);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11852_, SoundSource.HOSTILE, 1.1f, 1.4f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u9ebb\u96c0 \u4f20\u9001\u5230\u53cb\u519b {} \u8eab\u8fb9 (\u4f20\u9001\u524d {} \u683c)", (Object)NAMES[easterBossEntity.getVariant()], (Object)((int)d));
    }

    private void grabLaggard(ServerLevel serverLevel) {
        LivingEntity livingEntity = BossAggro.highest((LivingEntity)this);
        if (livingEntity == null) {
            livingEntity = ManorCombatContinuity.fightPlayer(this);
        }
        if (livingEntity == null || livingEntity.m_20280_((Entity)this) >= 65536.0) {
            return;
        }
        double d = this.f_19796_.m_188500_() * Math.PI * 2.0;
        double d2 = this.m_20185_() + Math.cos(d) * 3.0;
        double d3 = this.m_20189_() + Math.sin(d) * 3.0;
        double d4 = livingEntity.m_20185_();
        double d5 = livingEntity.m_20186_();
        double d6 = livingEntity.m_20189_();
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, d4, d5 + 1.0, d6, 30, 0.4, 0.7, 0.4, 0.3);
        Vec3 vec3 = ManorSafeTeleport.find(serverLevel, (Entity)livingEntity, new Vec3(d2, this.m_20186_(), d3), 8);
        if (vec3 == null) {
            return;
        }
        ManorSafeTeleport.move(serverLevel, (Entity)livingEntity, vec3);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, d2, this.m_20186_() + 1.0, d3, 30, 0.4, 0.7, 0.4, 0.3);
        serverLevel.m_6263_(null, d4, d5, d6, SoundEvents.f_11852_, SoundSource.HOSTILE, 1.3f, 1.2f);
        serverLevel.m_6263_(null, d2, this.m_20186_(), d3, SoundEvents.f_11852_, SoundSource.HOSTILE, 1.3f, 0.8f);
        livingEntity.m_213846_((Component)Component.m_237113_((String)"\u00a7c\u00a7l\u54fc\uff0c\u60f3\u9003\uff1f"));
        for (Player player : serverLevel.m_6907_()) {
            if (player == livingEntity || !(player.m_20280_((Entity)this) <= 4096.0)) continue;
            Component.m_237113_((String)("\u00a7c" + NAMES[this.getVariant()] + "\u00a77\uff1a\u00a7c\u00a7l\u54fc\uff0c\u60f3\u9003\uff1f"));
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] {} 15 \u79d2\u672a\u9501\u5b9a\u5230\u73a9\u5bb6 -> \u628a {} \u6293\u56de\u8eab\u8fb9 @{},{},{}", new Object[]{NAMES[this.getVariant()], livingEntity.m_7755_().getString(), (int)d2, (int)this.m_20186_(), (int)d3});
        this.breakAround(serverLevel, 3, 3);
    }

    private void breakAround(ServerLevel serverLevel, int n, int n2) {
        int n3 = this.breakBlocks(serverLevel, n, n2);
        if (n3 > 0) {
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12555_, SoundSource.HOSTILE, 1.2f, 0.9f);
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] {} \u6293\u4eba\u65f6\u7838\u5f00\u5468\u8fb9 {}x{} (\u811a\u4e0b\u90a3\u5c42\u4e0d\u7838, \u5f80\u4e0a {} \u5c42) \u65b9\u5757: {} \u4e2a", new Object[]{NAMES[this.getVariant()], n * 2 + 1, n * 2 + 1, n2, n3});
    }

    private void digTick(ServerLevel serverLevel) {
        int n = this.breakBlocks(serverLevel, 2, 1);
        if (n <= 0) {
            return;
        }
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12555_, SoundSource.HOSTILE, 0.9f, 1.1f);
        if (this.f_19797_ % 300 < 60) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} \u8ffd\u51fb\u5543\u5730 5x5: {} \u4e2a\u65b9\u5757", (Object)NAMES[this.getVariant()], (Object)n);
        }
    }

    private int breakBlocks(ServerLevel serverLevel, int n, int n2) {
        if (!ForgeEventFactory.getMobGriefingEvent((Level)serverLevel, (Entity)this)) {
            return 0;
        }
        int n3 = (int)Math.floor(this.m_20185_());
        int n4 = (int)Math.floor(this.m_20186_());
        int n5 = (int)Math.floor(this.m_20189_());
        int n6 = (int)Math.floor(this.m_20191_().f_82292_);
        int n7 = 0;
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = 0; k <= n2; ++k) {
                    boolean bl;
                    float f;
                    BlockPos blockPos = new BlockPos(n3 + i, n4 + k, n5 + j);
                    BlockState blockState = serverLevel.m_8055_(blockPos);
                    if (blockState.m_60795_() || (f = blockState.m_60800_((BlockGetter)serverLevel, blockPos)) > 5.0f || blockState.m_155947_()) continue;
                    boolean bl2 = f < 0.0f;
                    boolean bl3 = bl = blockPos.m_123342_() >= n4 && blockPos.m_123342_() <= n6;
                    if (bl2 && !bl || !bl2 && !blockState.canEntityDestroy((BlockGetter)serverLevel, blockPos, (Entity)this) || !ForgeEventFactory.onEntityDestroyBlock((LivingEntity)this, (BlockPos)blockPos, (BlockState)blockState) || (double)blockPos.m_123342_() < this.m_20186_() && blockPos.m_123342_() == n4) continue;
                    boolean bl4 = serverLevel.m_7740_(blockPos, false, (Entity)this, 512);
                    if (!bl4 && bl2) {
                        bl4 = serverLevel.m_7731_(blockPos, Blocks.f_50016_.m_49966_(), 3);
                    }
                    if (!bl4) continue;
                    ++n7;
                }
            }
        }
        return n7;
    }

    private void syncPlayerBuffs() {
        Object object = null;
        double d = 2304.0;
        for (Object object2 : this.m_9236_().m_6907_()) {
            double d2;
            if (!object2.m_6084_() || !((d2 = object2.m_20280_((Entity)this)) < d)) continue;
            d = d2;
            object = object2;
        }
        if (object == null) {
            return;
        }
        int n = 0;
        for (MobEffectInstance mobEffectInstance : List.copyOf(object.m_21220_())) {
            MobEffectInstance mobEffectInstance2 = ManorBuffCopy.copyOf(mobEffectInstance);
            if (mobEffectInstance2 == null || !MaqueSupportHooks.copy((LivingEntity)this, mobEffectInstance2, (Entity)object)) continue;
            ++n;
        }
        if (n > 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} \u540c\u6b65\u4e86 {} \u540d\u73a9\u5bb6\u7684 {} \u4e2a\u589e\u76ca\u6548\u679c\uff08\u6301\u7eed {} \u79d2\uff0c\u51b7\u5374 {} \u79d2\uff09", new Object[]{NAMES[this.getVariant()], 1, n, 30, 15});
            if (this.getVariant() == 9 && this.f_19796_.m_188501_() < 0.1f && object instanceof ServerPlayer) {
                Object object2;
                object2 = (ServerPlayer)object;
                MaqueSupportHooks.healLock((ServerPlayer)object2, (Entity)this);
            }
        }
    }

    private void castSkill(LivingEntity livingEntity) {
        if (this.getVariant() == 3) {
            if (!(this.m_9236_() instanceof ServerLevel)) {
                return;
            }
            if (this.gongdiSkills.tryStart((ServerLevel)this.m_9236_(), livingEntity)) {
                return;
            }
            this.skillCd = Math.min(this.skillCd, 10);
            return;
        }
        if (ManorLivingTargets.airborne(livingEntity)) {
            if (this.getVariant() == 0) {
                this.manorFiveSkills.tryStart((ServerLevel)this.m_9236_(), livingEntity);
                return;
            }
            if (this.getVariant() == 1) {
                this.manorFiveSkills.tryStart((ServerLevel)this.m_9236_(), livingEntity);
                return;
            }
            if (this.getVariant() == 4) {
                this.manorFiveSkills.tryStart((ServerLevel)this.m_9236_(), livingEntity);
                return;
            }
            if (this.getVariant() == 7) {
                this.manorFiveSkills.tryStart((ServerLevel)this.m_9236_(), livingEntity);
                return;
            }
            if (this.getVariant() == 2) {
                this.skillDoraGun((ServerLevel)this.m_9236_(), livingEntity);
                return;
            }
            if (this.getVariant() == 5) {
                this.skillStarTridentRain((ServerLevel)this.m_9236_(), livingEntity);
                return;
            }
            if (this.getVariant() == 6) {
                this.skillFlywheelVolley((ServerLevel)this.m_9236_(), livingEntity);
                return;
            }
            if (this.getVariant() == 10) {
                this.skillXueliKnives((ServerLevel)this.m_9236_(), livingEntity);
                return;
            }
            ManorLivingTargets.airbornePearl(this);
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (this.getVariant() == 4 && this.lemonSacrifice.trySacrifice(this.summons)) {
            this.summons.clear();
            this.summonLife = 0;
            return;
        }
        if (this.getVariant() == 5) {
            if (this.qingyiConfluenceNext) {
                this.qingyiConfluence.start(serverLevel, livingEntity);
                if (this.qingyiConfluence.busy()) {
                    this.qingyiConfluenceNext = false;
                    return;
                }
            } else {
                this.qingyiConfluenceNext = true;
            }
        }
        if (this.manorFiveSkills.tryStart(serverLevel, livingEntity)) {
            return;
        }
        if (this.getVariant() == 1) {
            this.skillEnderSwap(serverLevel, livingEntity);
            return;
        }
        if (this.getVariant() == 2) {
            this.skillDoraGun(serverLevel, livingEntity);
            return;
        }
        if (this.getVariant() == 3) {
            this.gongdiSkills.tryStart(serverLevel, livingEntity);
            return;
        }
        if (this.getVariant() == 4) {
            boolean bl = this.lemonSacrifice.canSummonHelpers();
            if (this.aliveSummons() == 0 && bl) {
                this.skillSummonFoodBosses(serverLevel, livingEntity);
            } else if (!bl) {
                if ((this.lemonStep++ & 1) == 0) {
                    this.skillPanFlip(serverLevel, livingEntity);
                } else {
                    this.skillLemonBorrowed(serverLevel, livingEntity);
                }
            } else {
                switch (this.lemonStep % 3) {
                    case 0: {
                        this.skillSummonFoodBosses(serverLevel, livingEntity);
                        break;
                    }
                    case 1: {
                        this.skillPanFlip(serverLevel, livingEntity);
                        break;
                    }
                    default: {
                        this.skillLemonBorrowed(serverLevel, livingEntity);
                    }
                }
                ++this.lemonStep;
            }
            return;
        }
        if (this.getVariant() == 5) {
            if (this.qingyiAlt) {
                this.qingyiAlt = false;
                this.skillQingyi(serverLevel, livingEntity);
            } else {
                this.qingyiAlt = true;
                this.skillStarTridentRain(serverLevel, livingEntity);
            }
            return;
        }
        if (this.getVariant() == 6) {
            double d = Math.sqrt(this.m_20280_((Entity)livingEntity));
            if (d >= 8.0 && d <= 16.0) {
                this.skillBeamCombo(serverLevel, livingEntity);
            } else if (this.countFlywheels(serverLevel) >= 15) {
                this.skillSteamVent(serverLevel, livingEntity);
            } else {
                this.skillFlywheelVolley(serverLevel, livingEntity);
            }
            return;
        }
        if (this.getVariant() == 7) {
            this.skillShadowStrike(serverLevel, livingEntity);
            return;
        }
        if (this.getVariant() == 10 && !this.isMini()) {
            if (this.xueliSlamWarn > 0 || this.xueliSlamLeft > 0 || this.xueliSlamGap > 0) {
                return;
            }
            switch (this.xueliNext) {
                case 0: {
                    this.skillXueli(serverLevel, livingEntity);
                    break;
                }
                case 1: {
                    this.skillXueliPanSlap(serverLevel, livingEntity);
                    break;
                }
                case 2: {
                    this.skillXueliRoll(serverLevel, livingEntity);
                    break;
                }
                case 3: {
                    this.skillXueliKnives(serverLevel, livingEntity);
                    break;
                }
                case 4: {
                    if (this.skillXueliSlam(serverLevel, livingEntity)) break;
                    this.skillXueliPanSlap(serverLevel, livingEntity);
                    break;
                }
                case 5: {
                    if (this.xueliMaids.canTrigger()) {
                        this.xueliMaids.trigger();
                        break;
                    }
                    this.skillXueliKnives(serverLevel, livingEntity);
                    break;
                }
                default: {
                    this.skillXueliClones(serverLevel, livingEntity);
                }
            }
            this.xueliNext = (this.xueliNext + 1) % 7;
            return;
        }
        if (this.getVariant() == 8) {
            this.skillPoisonBurst(serverLevel, livingEntity);
            return;
        }
        if (CASTER[this.getVariant()]) {
            this.playCast(HiddenBossCastRules.quicker(50), 0);
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11867_, SoundSource.HOSTILE, 1.4f, 0.9f);
            for (LivingEntity livingEntity2 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
                if (!(livingEntity2.m_20280_((Entity)this) <= 49.0) || !livingEntity2.m_6084_()) continue;
                livingEntity2.f_19802_ = 0;
                livingEntity2.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), this.manorSkillDamage(livingEntity2, (float)((this.getVariant() == 0 ? 14.0 : 10.0) * 0.6666666666666666), 1.5));
                BossKnockback.push((Entity)livingEntity2, 0.0, this.getVariant() == 0 ? 0.7 : 0.5, 0.0);
                livingEntity2.f_19864_ = true;
            }
        } else {
            this.playAction(26);
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12317_, SoundSource.HOSTILE, 1.4f, 0.8f);
            double d = livingEntity.m_20185_() - this.m_20185_();
            double d2 = livingEntity.m_20189_() - this.m_20189_();
            for (LivingEntity livingEntity3 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
                if (!(livingEntity3.m_20280_((Entity)this) <= 25.0) || !livingEntity3.m_6084_()) continue;
                livingEntity3.f_19802_ = 0;
                livingEntity3.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), this.manorSkillDamage(livingEntity3, this.getVariant() == 0 ? 22.4f : 16.0f, 1.5));
                BossKnockback.knockback(livingEntity3, this.getVariant() == 0 ? 1.2 : 0.8, -d, -d2);
                livingEntity3.f_19864_ = true;
            }
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] {} \u4f7f\u7528\u4e86\u770b\u5bb6\u6280\u80fd(variant={})", (Object)NAMES[this.getVariant()], (Object)this.getVariant());
    }

    private void dandanRushTick() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (this.dandanRush <= 0) {
            return;
        }
        if (--this.dandanRushTick > 0) {
            return;
        }
        this.dandanRushTick = 5;
        --this.dandanRush;
        level = BlinkTargets.rushTarget(this);
        if (!BlinkTargets.allowed(this, (LivingEntity)level)) {
            this.dandanRush = 0;
            return;
        }
        Vec3 vec3 = level.m_20182_().m_82546_(this.m_20182_());
        double d = Math.max(0.001, vec3.m_82553_());
        double d2 = level.m_20185_() - vec3.f_82479_ / d * 2.5;
        double d3 = level.m_20189_() - vec3.f_82481_ / d * 2.5;
        double d4 = level.m_20186_();
        Vec3 vec32 = BlinkTargets.find(serverLevel, (Entity)this, new Vec3(d2, d4, d3), 4, this);
        if (vec32 == null) {
            this.dandanRush = 0;
            return;
        }
        LightHooks.move(serverLevel, (Entity)this, vec32);
        MotionServer.rush(this);
        this.dandanRushTick = BlinkTargets.rushed(this);
        LightHooks.rush(this, this.m_5448_(), this.dandanRush);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 20, 0.3, 0.6, 0.3, 0.2);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11852_, SoundSource.HOSTILE, 1.0f, 1.25f);
        level.f_19802_ = 0;
        if (level.m_6469_(serverLevel.m_269111_().m_269104_((Entity)this, (Entity)this), this.manorSkillDamage((LivingEntity)level, 28.0f, 1.5))) {
            if (level instanceof Player) {
                this.stripOneBuff((Player)level);
            }
            if (this.dandanRush == 0 && level instanceof Player) {
                Player player = (Player)level;
                this.knockbackPlayer((LivingEntity)player);
            }
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u86cb\u86cb\u8fde\u77ac: {} \u5403 {} \u70b9\u9b54\u6cd5\u4f24\u5bb3(\u5269 {} \u4e0b)", new Object[]{level.m_7755_().getString(), 28, this.dandanRush});
    }

    private void skillEnderSwap(ServerLevel serverLevel, LivingEntity livingEntity) {
        double[][] dArrayArray;
        if (!BlinkTargets.allowed(this, livingEntity = BlinkTargets.swapTarget(this, livingEntity))) {
            return;
        }
        this.playCast(30, 2);
        double d = this.m_20185_();
        double d2 = this.m_20186_();
        double d3 = this.m_20189_();
        double d4 = livingEntity.m_20185_();
        double d5 = livingEntity.m_20186_();
        double d6 = livingEntity.m_20189_();
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, d, d2 + 1.0, d3, 30, 0.4, 0.7, 0.4, 0.3);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123789_, d4, d5 + 1.0, d6, 20, 0.3, 0.6, 0.3, 0.15);
        Vec3 vec3 = BlinkTargets.find(serverLevel, (Entity)this, new Vec3(d4, d5, d6), 4, this);
        Vec3 vec32 = BlinkTargets.find(serverLevel, (Entity)livingEntity, new Vec3(d, d2, d3), 4, this);
        if (vec3 == null || vec32 == null) {
            return;
        }
        LightHooks.move(serverLevel, (Entity)this, vec3);
        LightHooks.move(serverLevel, (Entity)livingEntity, vec32);
        boolean bl = true;
        livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19612_, 300, 1));
        this.dandanRush = 6;
        this.dandanRushTick = 0;
        MotionServer.swap(this);
        BlinkTargets.swapped(this);
        this.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 360, 1));
        this.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 180, 0));
        this.m_21563_().m_24960_((Entity)livingEntity, 40.0f, 40.0f);
        for (double[] dArray : dArrayArray = new double[][]{{d, d2 + 1.0, d3}, {this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_()}, {livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_()}}) {
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, dArray[0], dArray[1], dArray[2], 30, 0.4, 0.7, 0.4, 0.3);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123789_, dArray[0], dArray[1], dArray[2], 16, 0.3, 0.5, 0.3, 0.15);
        }
        serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11852_, SoundSource.HOSTILE, 1.2f, 1.25f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11852_, SoundSource.HOSTILE, 1.2f, 0.8f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u86cb\u86cb\u300c\u70db\u5f71\u7f6e\u6362(\u6362\u4f4d)\u300d: \u6362\u4f4d\u6210\u529f={} \u76ee\u6807={} \u73b0\u4f4d\u7f6e {},{},{}", new Object[]{bl, livingEntity.m_7755_().getString(), (int)this.m_20185_(), (int)this.m_20186_(), (int)this.m_20189_()});
    }

    private void skillSuppress(ServerLevel serverLevel, LivingEntity livingEntity) {
        if (this.doraSniper.busy() || this.reloadTicks > 0 || this.burstShots > 0 || !this.equipDoraGun(false)) {
            DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
            return;
        }
        this.doraSniperNext = true;
        this.burstTotalShots = this.doraTempo.burstShots(32);
        this.playAction((int)Math.ceil((double)(this.burstTotalShots * 3) / (2.0 * this.doraTempo.attackSpeedMultiplier() * this.getChallengeDifficulty().attackRate())) + 6);
        this.burstShots = this.burstTotalShots;
        this.burstAcc = 0.0;
        this.burstHits = 0;
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11846_, SoundSource.HOSTILE, 1.1f, 0.9f);
        FTBossMod.LOGGER.info("[FTBoss][Dora] \u51b2\u950b\u67aa\u300c\u538b\u5236\u5c04\u51fb\u300d\u5f00\u706b: {} \u53d1 x {} \u4f24\u5bb3, \u76ee\u6807={}", new Object[]{this.burstTotalShots, Float.valueOf(10.666667f), livingEntity.m_7755_().getString()});
        DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
    }

    private void skillCleave(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.cleaveWind = this.getChallengeDifficulty().acceleratedTicks(CLEAVE_WINDUP);
        this.playCast(this.cleaveWind, 1);
        this.beginSkillCue(this.m_20182_(), 16.875, this.cleaveWind, true);
        CompoundTag compoundTag = this.getSkillCue().m_6426_();
        compoundTag.m_128359_("gongdiSkill", "decimate");
        compoundTag.m_128347_("innerRadius", 6.75);
        this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)compoundTag);
        if (livingEntity instanceof LivingEntity) {
            LivingEntity livingEntity2 = livingEntity;
            Component.m_237113_((String)"\u00a7c\u529f\u5e95\uff1a\u5927\u6740\u56db\u65b9 \u00a77\u00b7 \u8d34\u8fd1\u5185\u5708\u6216\u9000\u51fa\u7ea2\u5708\uff0c\u963b\u6b62\u5916\u5203\u56de\u8840");
        }
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12558_, SoundSource.HOSTILE, 1.0f, 0.7f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12376_, SoundSource.HOSTILE, 1.0f, 0.8f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u529f\u5e95\u300c\u65ad\u7f6a\u65a9\u300d\u5f00\u59cb\u84c4\u529b {} tick(\u534a\u5f84 {} \u683c)", (Object)this.cleaveWind, (Object)11);
    }

    private void executeCleave() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        double d = this.m_21133_(Attributes.f_22281_);
        this.playImpactAction(18);
        this.releaseSkillCue(this.m_20182_());
        serverLevel.m_5594_(null, this.m_20183_(), SoundEvents.f_12317_, SoundSource.HOSTILE, 1.7f, 0.6f);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123766_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 12, 2.0, 0.4, 2.0, 0.0);
        int n = 0;
        for (Player player : serverLevel.m_6907_()) {
            if (!this.gongdiSkills.validTarget((LivingEntity)player, 16.875)) continue;
            Vec3 vec3 = player.m_20182_().m_82546_(this.m_20182_());
            boolean bl = vec3.m_165925_() >= 45.5625;
            float f = GongdiCombatRules.meleeDamage(d, EnchantmentHelper.m_44833_((ItemStack)this.m_21205_(), (MobType)player.m_6336_()));
            float f2 = GongdiCombatRules.cleaveDamage(f, bl);
            float f3 = player.m_21223_();
            player.f_19802_ = 0;
            if (!player.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), f2) || !bl) continue;
            this.gongdiSkills.onAbilityHit(serverLevel, (LivingEntity)player, Math.max(0.0f, f3 - player.m_21223_()));
            ++n;
        }
        this.applyCleaveAtkBoost();
        float f = GongdiCombatRules.outerHeal(this.m_21233_() - this.m_21223_(), n);
        if (f > 0.0f) {
            this.m_5634_(f);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123750_, this.m_20185_(), this.m_20186_() + 1.8, this.m_20189_(), 6, 0.4, 0.4, 0.4, 0.0);
        }
        if (n >= 2) {
            this.m_7292_(new MobEffectInstance(MobEffects.f_19600_, 480, 1));
        }
        FTBossMod.LOGGER.info("[FTBoss][Gongdi] decimate outerHits={} healing={}", (Object)n, (Object)Float.valueOf(f));
    }

    private void skillLemonBorrowed(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.playAction(30);
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        switch (this.f_19796_.m_188503_(4)) {
            case 0: {
                double d = this.m_20185_();
                double d2 = this.m_20186_();
                double d3 = this.m_20189_();
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123750_, d, d2 + 0.8, d3, 24, 1.6, 0.5, 1.6, 0.05);
                int n = 0;
                for (LivingEntity livingEntity2 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
                    if (!livingEntity2.m_6084_() || livingEntity2.m_20275_(d, d2, d3) > 25.0) continue;
                    livingEntity2.f_19802_ = 0;
                    livingEntity2.m_6469_(this.m_269291_().m_269104_((Entity)this, (Entity)this), this.manorSkillDamage(livingEntity2, 78.4f, 1.5));
                    livingEntity2.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 100, 1));
                    ++n;
                }
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u67e0\u6aac\u732b\u501f\u62db\u300c\u751c\u54c1\u7206\u88c2\u300d\u547d\u4e2d {} \u4eba", (Object)n);
                break;
            }
            case 1: {
                livingEntity.f_19802_ = 0;
                livingEntity.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), this.manorSkillDamage(livingEntity, 117.600006f, 2.0));
                ExtraDebuffs.bleed(livingEntity, 160, 1);
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123797_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 18, 0.4, 0.5, 0.4, 0.12);
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u67e0\u6aac\u732b\u501f\u62db\u300c\u5241\u780d\u300d-> {}", (Object)livingEntity.m_7755_().getString());
                break;
            }
            case 2: {
                double d = this.m_20185_();
                double d4 = this.m_20186_();
                double d5 = this.m_20189_();
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123769_, d, d4 + 0.8, d5, 40, 2.4, 0.6, 2.4, 0.08);
                int n = 0;
                for (LivingEntity livingEntity3 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
                    if (!livingEntity3.m_6084_() || livingEntity3.m_20275_(d, d4, d5) > 36.0) continue;
                    livingEntity3.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 160, 2));
                    livingEntity3.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 120, 1));
                    ++n;
                }
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u67e0\u6aac\u732b\u501f\u62db\u300c\u8336\u96fe\u300d\u547d\u4e2d {} \u4eba", (Object)n);
                break;
            }
            default: {
                livingEntity.f_19802_ = 0;
                livingEntity.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), this.manorSkillDamage(livingEntity, 78.4f, 1.5));
                BossKnockback.push((Entity)livingEntity, (livingEntity.m_20185_() - this.m_20185_()) * 0.6, 0.5, (livingEntity.m_20189_() - this.m_20189_()) * 0.6);
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123762_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 16, 0.4, 0.5, 0.4, 0.06);
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u67e0\u6aac\u732b\u501f\u62db\u300c\u9152\u6876\u300d-> {}", (Object)livingEntity.m_7755_().getString());
            }
        }
    }

    private void skillPanFlip(ServerLevel serverLevel, LivingEntity livingEntity) {
        if (ManorSpellBridge.replace(this, livingEntity, "searing_pan")) {
            return;
        }
        this.panWind = this.getChallengeDifficulty().acceleratedTicks(PAN_WINDUP);
        this.playCast(this.panWind, 3);
        this.beginSkillCue(this.m_20182_(), 6.0, this.panWind, true);
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11784_, SoundSource.HOSTILE, 1.2f, 1.3f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11705_, SoundSource.HOSTILE, 0.9f, 1.1f);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123744_, this.m_20185_(), this.m_20186_() + 0.8, this.m_20189_(), 30, 1.6, 0.3, 1.6, 0.02);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u67e0\u6aac\u732b\u300c\u731b\u706b\u7ffb\u9505\u300d\u8d77\u6cb9\u82b1, {} tick \u540e\u7ffb\u9505", (Object)this.panWind);
    }

    private void executePanFlip() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        this.playImpactAction(16);
        this.releaseSkillCue(this.m_20182_());
        double d = this.m_20185_();
        double d2 = this.m_20186_();
        double d3 = this.m_20189_();
        serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11913_, SoundSource.HOSTILE, 0.9f, 1.3f);
        serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11937_, SoundSource.HOSTILE, 1.4f, 0.8f);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123745_, d, d2 + 0.5, d3, 24, 2.0, 0.2, 2.0, 0.06);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123756_, d, d2 + 0.4, d3, 16, 1.2, 0.2, 1.2, 0.0);
        int n = 0;
        for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
            if (!this.combatPlayer(livingEntity) || livingEntity.m_20275_(d, d2, d3) > 36.0) continue;
            livingEntity.f_19802_ = 0;
            livingEntity.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), this.manorSkillDamage(livingEntity, 100.8f, 2.0));
            Vec3 vec3 = livingEntity.m_20182_().m_82546_(this.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
            BossKnockback.push((Entity)livingEntity, vec3.f_82479_ * 0.9, 1.65, vec3.f_82481_ * 0.9);
            livingEntity.f_19864_ = true;
            livingEntity.m_20254_(18);
            ExtraDebuffs.blueFire(livingEntity, 360);
            livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19612_, 480, 1));
            ++n;
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u67e0\u6aac\u732b\u300c\u731b\u706b\u7ffb\u9505\u300d\u547d\u4e2d {} \u4eba ({} \u4f24\u5bb3 + \u629b\u98de + \u70b9\u71c3)", (Object)n, (Object)Float.valueOf(100.8f));
    }

    private void clearDoraCombat() {
        this.cancelDoraSniper();
        this.clearDoraTempo();
        if (this.burstShots > 0 || this.reloadTicks > 0) {
            this.actionTicks = 0;
            this.f_19804_.m_135381_(DATA_COMBAT_ANIMATION, (Object)new CompoundTag());
        }
        this.burstShots = 0;
        this.burstAcc = 0.0;
        this.reloadTicks = 0;
        this.spearHitCd = 0;
        this.doraSpearLeft = 0;
        this.doraSpearGap = 0;
        this.doraSpearTarget = null;
        this.doraSkillProgress = 0.0;
        this.doraReloadProgress = 0.0;
        this.f_19804_.m_135381_(DATA_DORA_RELOADING, (Object)false);
        this.f_19804_.m_135381_(DATA_DORA_SPEAR_TICKS, (Object)0);
        DoraMotionSync.clear(this);
    }

    private boolean equipDoraGun(boolean bl) {
        if (this.getVariant() != 2 || this.m_9236_().f_46443_) {
            return false;
        }
        String string = bl ? DORA_FOCUS_ID : DORA_SMG_ID;
        Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(string));
        if (item == null || item == Items.f_41852_) {
            return false;
        }
        if (this.m_21205_().m_150930_(item)) {
            return true;
        }
        ItemStack itemStack = new ItemStack((ItemLike)item);
        for (Enchantment enchantment : EasterBossEntity.buildEnchants(WEAPON_ENCH_IDS)) {
            if (enchantment == null) continue;
            itemStack.m_41663_(enchantment, 8);
        }
        itemStack.m_41784_().m_128379_("Unbreakable", true);
        this.m_21008_(InteractionHand.MAIN_HAND, itemStack);
        this.m_21409_(EquipmentSlot.MAINHAND, 0.0f);
        return true;
    }

    private void skillDoraGun(ServerLevel serverLevel, LivingEntity livingEntity) {
        LivingEntity livingEntity2;
        if (!(livingEntity instanceof LivingEntity) || !this.combatPlayer(livingEntity2 = livingEntity) || this.isMini() || this.m_21525_() || this.doraSniper.busy() || this.burstShots > 0 || this.reloadTicks > 0) {
            return;
        }
        double d = this.m_20280_((Entity)livingEntity);
        if (!DoraSniperCycle.inRange(d) || !this.m_142582_((Entity)livingEntity)) {
            this.skillCd = 10;
            return;
        }
        if (this.doraSniperNext || d > 324.0) {
            this.startDoraSniper(serverLevel, livingEntity2);
        } else {
            this.skillSuppress(serverLevel, livingEntity);
        }
    }

    private Vec3 doraMuzzle() {
        return new Vec3(this.m_20185_(), this.m_20188_() - 0.15, this.m_20189_());
    }

    private void aimDoraSniper(LivingEntity livingEntity) {
        Vec3 vec3 = livingEntity.m_20191_().m_82399_().m_82546_(this.doraMuzzle());
        this.doraSniperDirection = vec3.m_82556_() > 1.0E-6 ? vec3.m_82541_() : this.m_20154_();
    }

    private void startDoraSniper(ServerLevel serverLevel, LivingEntity livingEntity) {
        if (!this.equipDoraGun(true) || !this.doraSniper.begin(livingEntity.m_20148_(), this.isImpossibleChallenge())) {
            return;
        }
        this.doraSniperNext = false;
        this.aimDoraSniper(livingEntity);
        this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)new CompoundTag());
        this.playCast(this.getChallengeDifficulty().acceleratedTicks(DoraSniperCycle.CHARGE_TICKS), 1);
        this.m_21573_().m_26573_();
        this.syncDoraSniperCue(false);
        DoraMotionSync.sniper(this);
        serverLevel.m_5594_(null, this.m_20183_(), SoundEvents.f_11739_, SoundSource.HOSTILE, 1.0f, 1.2f);
        Component.m_237115_((String)"message.ftboss.dora_sniper.charge");
        FTBossMod.LOGGER.info("[FTBoss][Dora] \u805a\u80fd\u67aa\u84c4\u529b\u72d9\u51fb: {} tick, \u6700\u540e {} tick \u9501\u5b9a\u65b9\u5411, \u76ee\u6807={}", new Object[]{this.getChallengeDifficulty().acceleratedTicks(DoraSniperCycle.CHARGE_TICKS), this.getChallengeDifficulty().acceleratedTicks(DoraSniperCycle.CHARGE_TICKS) - this.getChallengeDifficulty().acceleratedTicks(DoraSniperCycle.TRACK_TICKS), livingEntity.m_7755_().getString()});
    }

    private void syncDoraSniperCue(boolean bl) {
        Vec3 vec3 = this.doraMuzzle();
        Vec3 vec32 = this.clipSkillRay(vec3, vec3.m_82549_(this.doraSniperDirection.m_82490_(36.0)));
        int n = bl ? 12 : DoraSniperCycle.CHARGE_TICKS;
        this.syncBeamCue(vec3, vec32, 0.72, this.getChallengeDifficulty().acceleratedTicks(n), bl, bl);
        CompoundTag compoundTag = this.getSkillCue().m_6426_();
        compoundTag.m_128379_("doraSniper", true);
        compoundTag.m_128379_("doraLocked", this.doraSniper.locked());
        this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)compoundTag);
    }

    private void tickDoraSniper(ServerLevel serverLevel, LivingEntity livingEntity) {
        DoraSniperCycle.Step step = this.doraSniper.tick(livingEntity.m_20148_(), livingEntity.m_9236_() == this.m_9236_() && DoraSniperCycle.inRange(this.m_20280_((Entity)livingEntity)));
        if (step == DoraSniperCycle.Step.CANCELLED || step == DoraSniperCycle.Step.FINISHED) {
            this.cancelDoraSniper();
            return;
        }
        this.m_21573_().m_26573_();
        this.m_20334_(0.0, this.m_20184_().f_82480_, 0.0);
        if (step == DoraSniperCycle.Step.TRACKING || step == DoraSniperCycle.Step.LOCK) {
            this.aimDoraSniper(livingEntity);
        }
        Vec3 vec3 = this.doraMuzzle().m_82549_(this.doraSniperDirection.m_82490_(36.0));
        this.m_21563_().m_24950_(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, 90.0f, 90.0f);
        if (step == DoraSniperCycle.Step.LOCK) {
            serverLevel.m_5594_(null, this.m_20183_(), (SoundEvent)SoundEvents.f_12216_.get(), SoundSource.HOSTILE, 1.2f, 1.8f);
            Component.m_237115_((String)"message.ftboss.dora_sniper.lock");
        }
        if (step == DoraSniperCycle.Step.FIRE) {
            this.fireDoraSniper(serverLevel);
        } else if (this.doraSniper.charging()) {
            this.syncDoraSniperCue(false);
        }
    }

    private void fireDoraSniper(ServerLevel serverLevel) {
        LivingEntity livingEntity22;
        Vec3 vec3 = this.doraMuzzle();
        Vec3 vec32 = this.clipSkillRay(vec3, vec3.m_82549_(this.doraSniperDirection.m_82490_(36.0)));
        LivingEntity livingEntity3 = null;
        double d = vec3.m_82557_(vec32);
        for (LivingEntity livingEntity22 : serverLevel.m_6443_(LivingEntity.class, new AABB(vec3, vec32).m_82400_(1.0), livingEntity -> livingEntity != this && livingEntity.m_6084_() && !livingEntity.m_5833_())) {
            AABB aABB = livingEntity22.m_20191_().m_82400_(0.72);
            Vec3 vec33 = aABB.m_82390_(vec3) ? vec3 : (Vec3)aABB.m_82371_(vec3, vec32).orElse(null);
            if (vec33 == null || !(vec3.m_82557_(vec33) < d)) continue;
            d = vec3.m_82557_(vec33);
            vec32 = vec33;
            livingEntity3 = livingEntity22;
        }
        boolean bl = false;
        if (livingEntity3 instanceof LivingEntity && this.combatPlayer(livingEntity22 = (LivingEntity)livingEntity3) && !this.m_7307_((Entity)livingEntity22) && this.m_142582_((Entity)livingEntity22) && (bl = DoraSniperDamage.hurt(livingEntity22, (LivingEntity)this))) {
            this.doraKnockback(livingEntity22, 1.1, 0.16);
            this.doraHit(livingEntity22);
        }
        if (!bl) {
            this.doraTempo.miss();
        }
        this.syncDoraSniperCue(true);
        DoraMotionSync.fire(this);
        livingEntity22 = this.getSkillCue().m_6426_();
        livingEntity22.m_128347_("endX", vec32.f_82479_);
        livingEntity22.m_128347_("endY", vec32.f_82480_);
        livingEntity22.m_128347_("endZ", vec32.f_82481_);
        this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)livingEntity22);
        this.playAction(this.getChallengeDifficulty().acceleratedTicks(12));
        WeaponFx.send(serverLevel, WeaponFxRules.Kind.DORA_IMPACT, WeaponFxRules.Theme.ARC, vec32, vec32, 0.85, this.m_146908_());
        ManorExpansionHooks.bullet(this, serverLevel, this.m_5448_());
        serverLevel.m_5594_(null, this.m_20183_(), SoundEvents.f_12521_, SoundSource.HOSTILE, 0.9f, 1.6f);
        FTBossMod.LOGGER.info("[FTBoss][Dora] \u805a\u80fd\u67aa\u72d9\u51fb\u91ca\u653e: \u547d\u4e2d={}, \u5c04\u7a0b={}", (Object)bl, (Object)36.0);
    }

    private void cancelDoraSniper() {
        boolean bl = this.doraSniper.busy() || this.getSkillCue().m_128471_("doraSniper");
        this.doraSniper.reset();
        this.doraSniperDirection = Vec3.f_82478_;
        if (!bl) {
            DoraMotionSync.cancelSniper(this);
            return;
        }
        this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)new CompoundTag());
        this.f_19804_.m_135381_(DATA_COMBAT_ANIMATION, (Object)new CompoundTag());
        this.actionTicks = 0;
        this.castTicks = 0;
        if (!this.m_9236_().f_46443_ && this.getVariant() == 2 && this.m_6084_()) {
            this.equipDoraGun(false);
        }
        DoraMotionSync.cancelSniper(this);
    }

    private void clearDoraTempo() {
        boolean bl = this.doraTempo.stacks() > 0;
        this.doraTempo.reset();
        this.m_21195_((MobEffect)FTEffects.LETHAL_TEMPO.get());
        if (bl && this.getVariant() == 2) {
            this.bossBar.m_6456_((Component)Component.m_237113_((String)("\u00a7c" + NAMES[2])));
        }
    }

    private void doraHit(LivingEntity livingEntity) {
        Level level;
        if (this.getVariant() != 2 || !this.combatPlayer(livingEntity) || this.m_21525_() || this.isMini() || !((level = this.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        int n = this.doraTempo.stacks();
        int n2 = this.doraTempo.hit(livingEntity.m_20148_(), serverLevel.m_46467_());
        if (n2 <= 0) {
            this.m_21195_((MobEffect)FTEffects.LETHAL_TEMPO.get());
            this.bossBar.m_6456_((Component)Component.m_237113_((String)("\u00a7c" + NAMES[2])));
            return;
        }
        this.m_7292_(new MobEffectInstance((MobEffect)FTEffects.LETHAL_TEMPO.get(), 120, n2 - 1, false, true, true));
        this.bossBar.m_6456_((Component)Component.m_237113_((String)("\u00a7c" + NAMES[2] + " \u00a76[\u81f4\u547d\u8282\u594f " + n2 + "/6]")));
        if (n != n2) {
            Vec3 vec3 = this.m_20182_().m_82520_(0.0, 1.0, 0.0);
            WeaponFx.send(serverLevel, WeaponFxRules.Kind.DORA_IMPACT, WeaponFxRules.Theme.GOLD, vec3, vec3, 0.52, this.m_146908_());
            FTBossMod.LOGGER.info("[FTBoss][Dora] \u81f4\u547d\u8282\u594f {}/6: \u653b\u901fx{}, \u51b7\u5374x{}, \u8fde\u5c04{}\u53d1", new Object[]{n2, this.doraTempo.attackSpeedMultiplier(), this.doraTempo.cooldownMultiplier(), this.doraTempo.burstShots(32)});
        }
    }

    private void doraCombatTick() {
        int n;
        LivingEntity livingEntity;
        LivingEntity livingEntity2 = this.m_5448_();
        if (this.m_21224_() || this.m_21525_() || this.isMini() || !(livingEntity2 instanceof LivingEntity) || !this.combatPlayer(livingEntity = livingEntity2)) {
            this.clearDoraCombat();
            DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
            return;
        }
        int n2 = this.doraTempo.stacks();
        this.doraTempo.tick(this.m_9236_().m_46467_());
        if (!(n2 <= 0 || this.doraTempo.stacks() != 0 && this.m_21023_((MobEffect)FTEffects.LETHAL_TEMPO.get()))) {
            this.clearDoraTempo();
        }
        if ((n = this.getDoraSpearTicks()) > 0) {
            this.f_19804_.m_135381_(DATA_DORA_SPEAR_TICKS, (Object)(n - 1));
        }
        if (this.spearHitCd > 0) {
            --this.spearHitCd;
        }
        this.tickDoraSpear((ServerLevel)this.m_9236_(), livingEntity);
        if (this.doraSniper.busy()) {
            this.tickDoraSniper((ServerLevel)this.m_9236_(), livingEntity);
            DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
            return;
        }
        if (this.burstShots > 0 && this.actionTicks < 4) {
            this.playAction(8);
        }
        DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
    }

    private void tickDoraSpear(ServerLevel serverLevel, LivingEntity livingEntity) {
        if (!new ResourceLocation(DUOLA_MELEE_ID).equals((Object)ForgeRegistries.ITEMS.getKey((Object)this.m_21206_().m_41720_())) || !this.m_142582_((Entity)livingEntity) || this.m_20280_((Entity)livingEntity) > 100.0 || this.doraSpearTarget != null && !this.doraSpearTarget.equals(livingEntity.m_20148_())) {
            this.doraSpearLeft = 0;
            this.doraSpearGap = 0;
            this.doraSpearTarget = null;
            return;
        }
        if (this.doraSpearLeft <= 0) {
            if (this.spearHitCd > 0) {
                return;
            }
            this.doraSpearLeft = 3;
            this.doraSpearTarget = livingEntity.m_20148_();
            this.doraSpearGap = 0;
        }
        if (this.doraSpearGap-- > 0) {
            return;
        }
        Vec3 vec3 = livingEntity.m_20191_().m_82399_().m_82546_(this.m_146892_());
        this.f_19804_.m_135381_(DATA_DORA_SPEAR_TICKS, (Object)4);
        this.m_6674_(InteractionHand.OFF_HAND);
        this.spearChargeHit(serverLevel, vec3);
        DoraMotionSync.spear(this);
        serverLevel.m_5594_(null, this.m_20183_(), SoundEvents.f_12317_, SoundSource.HOSTILE, 0.8f, 1.2f);
        --this.doraSpearLeft;
        this.doraSpearGap = this.getChallengeDifficulty().acceleratedTicks(4) - 1;
        if (this.doraSpearLeft <= 0) {
            this.spearHitCd = this.getChallengeDifficulty().acceleratedTicks((int)Math.ceil(30.0 * this.doraTempo.cooldownMultiplier()));
            this.doraSpearTarget = null;
        } else if (!this.doraSniper.busy() && this.m_20280_((Entity)livingEntity) > 16.0) {
            Vec3 vec32 = vec3.m_82542_(1.0, 0.0, 1.0).m_82541_().m_82490_(1.125);
            this.m_20334_(vec32.f_82479_, this.m_20184_().f_82480_, vec32.f_82481_);
            this.f_19812_ = true;
        }
    }

    private void startReload(ServerLevel serverLevel) {
        this.cancelDoraSniper();
        this.reloadTicks = 60;
        this.doraReloadProgress = 0.0;
        this.burstAcc = 0.0;
        this.actionTicks = 0;
        this.f_19804_.m_135381_(DATA_COMBAT_ANIMATION, (Object)new CompoundTag());
        this.f_19804_.m_135381_(DATA_DORA_RELOADING, (Object)true);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11841_, SoundSource.HOSTILE, 1.2f, 0.9f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12517_, SoundSource.HOSTILE, 1.2f, 1.0f);
        FTBossMod.LOGGER.info("[FTBoss][Dora] \u53cc\u6301\u88c5\u5f39\u51b2\u523a: \u57fa\u7840{}tick, \u51b7\u5374x{}", (Object)60, (Object)this.doraTempo.cooldownMultiplier());
        DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
    }

    private void reloadTick() {
        Vec3 vec3;
        if (this.reloadTicks <= 0) {
            DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            this.reloadTicks = 0;
            DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        this.doraReloadProgress += this.getChallengeDifficulty().attackRate() / this.doraTempo.cooldownMultiplier();
        int n = (int)(this.doraReloadProgress + 1.0E-9);
        this.doraReloadProgress -= (double)n;
        this.reloadTicks = Math.max(0, this.reloadTicks - n);
        LivingEntity livingEntity = this.m_5448_();
        if (livingEntity != null && livingEntity.m_6084_() && (vec3 = livingEntity.m_20182_().m_82546_(this.m_20182_())).m_82556_() > 1.0) {
            Vec3 vec32 = vec3.m_82541_();
            this.m_20334_(vec32.f_82479_ * 1.05, Math.max(0.05, vec32.f_82480_ * 0.3), vec32.f_82481_ * 1.05);
            this.f_19812_ = true;
            this.m_146922_((float)Math.toDegrees(Math.atan2(-vec32.f_82479_, vec32.f_82481_)));
            this.f_19859_ = this.m_146908_();
        }
        if (this.reloadTicks <= 0) {
            this.finishReload(serverLevel);
        }
        DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
    }

    private void finishReload(ServerLevel serverLevel) {
        this.reloadTicks = 0;
        this.doraReloadProgress = 0.0;
        this.f_19804_.m_135381_(DATA_DORA_RELOADING, (Object)false);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11842_, SoundSource.HOSTILE, 1.2f, 1.2f);
        FTBossMod.LOGGER.info("[FTBoss][Dora] \u51b2\u950b\u67aa\u88c5\u5f39\u5b8c\u6210\uff0c\u4fdd\u6301\u957f\u67aa\u526f\u624b\uff0c\u4e0b\u4e00\u8f6e\u53ef\u5207\u6362\u805a\u80fd\u67aa\u72d9\u51fb");
        DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
    }

    private int aliveSummons() {
        return this.aliveSummonBosses();
    }

    public int aliveSummonBosses() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return 0;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        int n = 0;
        for (UUID uUID : new HashSet<UUID>(this.summons)) {
            if (!this.lemonSacrifice.countsAsSummonedBoss(serverLevel.m_8791_(uUID))) continue;
            ++n;
        }
        return n;
    }

    private boolean tryHalfHealthLemonSacrifice(ServerLevel serverLevel) {
        if (!this.lemonSacrifice.halfHealthFallbackReady()) {
            return false;
        }
        int n = this.aliveSummonBosses();
        if (n < LemonPartyRules.absorbMinimum(this)) {
            this.skillSummonFoodBosses(serverLevel, this.m_5448_());
        }
        if (!this.lemonSacrifice.trySacrifice(this.summons)) {
            return false;
        }
        this.summons.clear();
        this.summonLife = 0;
        FTBossMod.LOGGER.info("[FTBoss][Lemon] \u534a\u8840\u4fdd\u5e95\u732e\u796d\u5df2\u89e6\u53d1: \u539f\u6709\u52a9\u6218={}\uff0c\u5df2\u6309\u5f53\u524d\u4eba\u6570\u53ec\u9f50\u52a9\u6218\u5e76\u5b8c\u6210\u732e\u796d", (Object)n);
        return true;
    }

    private void skillSummonFoodBosses(ServerLevel serverLevel, LivingEntity livingEntity) {
        if (!this.lemonSacrifice.canSummonHelpers()) {
            return;
        }
        LemonMotionServer.summon(this, HiddenBossCastRules.quicker(45), 3);
        this.clearSummons(serverLevel);
        ArrayList<EntityType> arrayList = new ArrayList<EntityType>();
        arrayList.add((EntityType)FTEntities.SUGAR_QUEEN.get());
        arrayList.add((EntityType)FTEntities.MEAT_MOUNTAIN.get());
        arrayList.add((EntityType)FTEntities.TEA_TREE.get());
        arrayList.add((EntityType)FTEntities.WINE_BARREL.get());
        arrayList.add((EntityType)FTEntities.RAT_BOSS.get());
        int n = 0;
        for (int i = 0; i < this.lemonSacrifice.summonCount(); ++i) {
            try {
                if (arrayList.isEmpty()) break;
                EntityType entityType = (EntityType)arrayList.remove(this.f_19796_.m_188503_(arrayList.size()));
                Entity entity = entityType.m_20615_((Level)serverLevel);
                if (entity == null) {
                    FTBossMod.LOGGER.warn("[FTBoss][Easter] \u53ec\u5524\u98df\u7269\u7cfb Boss \u5931\u8d25(create \u8fd4\u56de null): {}", (Object)entityType);
                    continue;
                }
                double d = this.f_19796_.m_188500_() * Math.PI * 2.0;
                double d2 = this.m_20185_() + Math.cos(d) * 3.5;
                double d3 = this.m_20189_() + Math.sin(d) * 3.5;
                entity.m_7678_(d2, this.m_20186_(), d3, this.f_19796_.m_188501_() * 360.0f, 0.0f);
                if (entity instanceof Mob) {
                    Object object;
                    Mob mob = (Mob)entity;
                    mob.m_6593_((Component)Component.m_237113_((String)"\u00a7c\u00a7l").m_7220_(EasterBossEntity.summonBossName(entityType)).m_7220_((Component)Component.m_237113_((String)"\u00a77\u00b7\u52a9\u6218")));
                    mob.m_20340_(true);
                    AttributeInstance attributeInstance = mob.m_21051_(Attributes.f_22276_);
                    if (attributeInstance != null) {
                        attributeInstance.m_22100_(attributeInstance.m_22115_() + 100.0);
                        mob.m_21153_((float)attributeInstance.m_22135_());
                    }
                    if (mob instanceof SugarQueenEntity) {
                        object = (SugarQueenEntity)mob;
                        ((SugarQueenEntity)object).setCloneQueen(true);
                    }
                    if (mob instanceof MeatMountainEntity) {
                        object = (MeatMountainEntity)mob;
                        ((MeatMountainEntity)((Object)object)).setCloneButcher(true);
                    }
                    mob.m_21530_();
                    if (livingEntity != null) {
                        mob.m_6710_(livingEntity);
                    }
                    object = this.applySummonBuffs(mob);
                    FTBossMod.LOGGER.info("[FTBoss][Easter] \u52a9\u6218\u5f3a\u5316: {} \u8840\u91cf +{} -> {}, {} \u4e2a\u589e\u76ca: {}", new Object[]{entityType.m_20676_().getString(), 100, attributeInstance != null ? (int)attributeInstance.m_22135_() : -1, 5, object});
                }
                BossMusic.markNoBgm(entity);
                LemonSummonRewards.mark(entity);
                ManorFightMembers.inherit((Entity)this, entity);
                if (this.lemonSacrifice.used()) {
                    LemonSummonRewards.markPostSacrificeHelper(entity, this.m_20148_());
                }
                if (!serverLevel.m_7967_(entity)) continue;
                this.summons.add(entity.m_20148_());
                RitualNetwork.summoned((LivingEntity)this, entity);
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123748_, d2, this.m_20186_() + 1.0, d3, 25, 0.5, 0.7, 0.5, 0.08);
                serverLevel.m_6263_(null, d2, this.m_20186_(), d3, SoundEvents.f_11862_, SoundSource.HOSTILE, 1.3f, 0.9f);
                ++n;
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u67e0\u6aac\u732b\u53ec\u5524: {} @{},{},{}", new Object[]{entityType.m_20676_().getString(), (int)d2, (int)this.m_20186_(), (int)d3});
                continue;
            }
            catch (Throwable throwable) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] \u53ec\u5524\u98df\u7269\u7cfb Boss \u5f02\u5e38: {}", (Object)throwable.toString());
            }
        }
        this.summonLife = n > 0 ? 1800 : 0;
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u67e0\u6aac\u732b\u558a\u6765 {} \u4f4d\u98df\u7269\u7cfb Boss \u52a9\u6218(\u6301\u7eed {} \u79d2)", (Object)n, (Object)90);
    }

    private String applySummonBuffs(Mob mob) {
        int n;
        int n2;
        int n3;
        int[] nArray = new int[CHAIN_BUFF_POOL.length];
        for (n3 = 0; n3 < nArray.length; ++n3) {
            nArray[n3] = n3;
        }
        for (n3 = nArray.length - 1; n3 > 0; --n3) {
            n2 = this.f_19796_.m_188503_(n3 + 1);
            n = nArray[n3];
            nArray[n3] = nArray[n2];
            nArray[n2] = n;
        }
        StringBuilder stringBuilder = new StringBuilder();
        n2 = Math.min(5, nArray.length);
        for (n = 0; n < n2; ++n) {
            MobEffect mobEffect = CHAIN_BUFF_POOL[nArray[n]];
            mob.m_7292_(new MobEffectInstance(mobEffect, 1800, this.buffAmp(1), true, true, true));
            stringBuilder.append(mobEffect.m_19482_().getString()).append(' ');
        }
        return stringBuilder.toString().trim();
    }

    private static Component summonBossName(EntityType<?> entityType) {
        if (entityType == FTEntities.SUGAR_QUEEN.get()) {
            return Component.m_237115_((String)"boss.ftboss.sugar_queen");
        }
        if (entityType == FTEntities.MEAT_MOUNTAIN.get()) {
            return Component.m_237115_((String)"boss.ftboss.butcher");
        }
        if (entityType == FTEntities.TEA_TREE.get()) {
            return Component.m_237115_((String)"boss.ftboss.saya");
        }
        if (entityType == FTEntities.WINE_BARREL.get()) {
            return Component.m_237115_((String)"boss.ftboss.wine_barrel");
        }
        if (entityType == FTEntities.RAT_BOSS.get()) {
            return Component.m_237115_((String)"boss.ftboss.rat_swarm");
        }
        return Component.m_237115_((String)"entity.ftboss.apple_boss");
    }

    public boolean onPostSacrificeHelperDefeated(Entity entity) {
        if (entity == null || this.getVariant() != 4 || !this.lemonSacrifice.used()) {
            return false;
        }
        this.summons.remove(entity.m_20148_());
        if (this.aliveSummons() == 0) {
            this.summonLife = 0;
        }
        return this.lemonSacrifice.recordPostSacrificeHelperDefeat();
    }

    private void clearSummons(ServerLevel serverLevel) {
        if (serverLevel != null && !this.summons.isEmpty()) {
            for (UUID uUID : new ArrayList<UUID>(this.summons)) {
                LivingEntity livingEntity;
                Entity entity = serverLevel.m_8791_(uUID);
                if (!(entity instanceof LivingEntity) || !(livingEntity = (LivingEntity)entity).m_6084_()) continue;
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 20, 0.4, 0.6, 0.4, 0.05);
                livingEntity.m_146870_();
            }
        }
        this.summons.clear();
        this.summonLife = 0;
    }

    private void rotateQingyiWeapon(ServerLevel serverLevel) {
        this.qingyiIndex = (this.qingyiIndex + 1) % QINGYI_WEAPONS.length;
        String string = QINGYI_WEAPONS[this.qingyiIndex];
        if (!(this.m_21205_().m_41720_() instanceof QingyiConfluenceTridentItem)) {
            this.equipWeapon();
        }
        switch (this.qingyiIndex) {
            case 0: {
                this.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 160, 1));
                break;
            }
            case 1: {
                this.m_7292_(new MobEffectInstance(MobEffects.f_19600_, 160, 1));
                break;
            }
            case 2: {
                this.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 160, 1));
                break;
            }
            case 3: {
                this.m_7292_(new MobEffectInstance(MobEffects.f_19605_, 160, 0));
                break;
            }
        }
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11887_, SoundSource.HOSTILE, 1.0f, 1.2f);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123809_, this.m_20185_(), this.m_20186_() + 1.2, this.m_20189_(), 40, 0.6, 0.7, 0.6, 0.4);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u9752\u8863\u300c\u56db\u8c61\u8f6e\u8f6c\u300d\u7b2c {} \u4ef6 -> {} (\u5b9e\u9645\u4e3b\u624b={})", new Object[]{this.qingyiIndex + 1, string, this.m_21205_()});
    }

    private void flywheelGrind(ServerLevel serverLevel) {
        double d = this.m_20185_();
        double d2 = this.m_20186_();
        double d3 = this.m_20189_();
        int n = 0;
        for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
            if (!livingEntity.m_6084_() || livingEntity.m_20275_(d, d2, d3) > 39.0625) continue;
            livingEntity.f_19802_ = 0;
            livingEntity.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 100.56f);
            livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 40, 1));
            ++n;
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123797_, d, d2 + 0.9, d3, 10, 0.9, 0.5, 0.9, 0.12);
        if (n > 0) {
            this.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 40, 1));
            serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11998_, SoundSource.HOSTILE, 1.0f, 1.5f);
        }
    }

    private void skillSteamVent(ServerLevel serverLevel, LivingEntity livingEntity) {
        if (ManorSpellBridge.replace(this, livingEntity, "pressure_lance")) {
            return;
        }
        this.playCast(HiddenBossCastRules.quicker(28), 2);
        BodySignals.vent(this);
        NativeSignals.vent(this);
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        Vec3 vec3 = this.m_20154_();
        Vec3 vec32 = this.m_20182_().m_82520_(0.0, 1.0, 0.0);
        for (double d = 1.0; d <= 10.75; d += 0.5) {
            Vec3 vec33 = vec32.m_82549_(vec3.m_82490_(d));
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, vec33.f_82479_, vec33.f_82480_, vec33.f_82481_, 4, 0.2, 0.2, 0.2, 0.05);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123755_, vec33.f_82479_, vec33.f_82480_, vec33.f_82481_, 1, 0.15, 0.15, 0.15, 0.02);
        }
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11937_, SoundSource.HOSTILE, 1.4f, 0.7f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11932_, SoundSource.HOSTILE, 0.9f, 0.8f);
        int n = 0;
        for (LivingEntity livingEntity2 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
            Vec3 vec33;
            double d;
            if (!livingEntity2.m_6084_() || (d = (vec33 = livingEntity2.m_20182_().m_82520_(0.0, (double)livingEntity2.m_20206_() * 0.5, 0.0).m_82546_(vec32)).m_82553_()) > 10.75 || d < 0.2 || vec33.m_82541_().m_82526_(vec3) < 0.82) continue;
            livingEntity2.f_19802_ = 0;
            livingEntity2.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), this.manorSkillDamage(livingEntity2, 335.2f, 1.5));
            BossKnockback.push((Entity)this, (Entity)livingEntity2, vec3.f_82479_ * 0.98, 0.22, vec3.f_82481_ * 0.98);
            livingEntity2.f_19864_ = true;
            livingEntity2.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 360, 1));
            livingEntity2.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 360, 1));
            ++n;
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u6587\u7384\u84dd\u300c\u84b8\u6c7d\u6cc4\u538b\u300d\u547d\u4e2d {} \u4eba ({} \u4f24\u5bb3 + \u51fb\u9000)", (Object)n, (Object)Float.valueOf(80.0f));
    }

    private void skillFlywheelVolley(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.playAction(34);
        BodySignals.flywheel(this);
        this.flywheelShots = 6;
        this.flywheelTimer = 0;
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12019_, SoundSource.HOSTILE, 1.0f, 0.7f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u6587\u7384\u84dd\u300c\u98de\u8f6e\u56de\u65cb\u300d: \u5373\u5c06\u6295\u51fa {} \u4e2a\u8ffd\u8e2a\u98de\u8f6e", (Object)6);
    }

    private void grantOpeningFlywheels(ServerLevel serverLevel) {
        if (this.getVariant() != 6 || this.isMini() || this.isGiant() || this.m_21525_() || !this.m_6084_() || ManorFight.isFinaleMember((LivingEntity)this) || this.openingFlywheelsGranted >= 10) {
            return;
        }
        if (this.openingFlywheelsRetry > 0) {
            --this.openingFlywheelsRetry;
            return;
        }
        this.openingFlywheelsGranted = FlywheelCombatRules.openingProgress(this.openingFlywheelsGranted, this.countFlywheels(serverLevel));
        while (this.openingFlywheelsGranted < 10) {
            FlywheelProjectileEntity flywheelProjectileEntity = new FlywheelProjectileEntity((Level)serverLevel, (LivingEntity)this);
            flywheelProjectileEntity.beginOrbit(Math.PI * 2 * (double)this.openingFlywheelsGranted / 10.0);
            ManorFightMembers.inherit((Entity)this, (Entity)flywheelProjectileEntity);
            if (!serverLevel.m_7967_((Entity)flywheelProjectileEntity)) {
                this.openingFlywheelsRetry = 20;
                return;
            }
            ++this.openingFlywheelsGranted;
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u6587\u7384\u84dd\u5f00\u5c40\u98de\u8f6e\u5c31\u7eea: {} \u4e2a", (Object)10);
    }

    private void throwFlywheel(ServerLevel serverLevel) {
        this.flywheelTimer = this.getChallengeDifficulty().acceleratedTicks(8);
        --this.flywheelShots;
        if (this.countFlywheels(serverLevel) >= 15) {
            XuanlanEliteFlywheels.summon(this, serverLevel);
            this.flywheelShots = XuanlanEliteFlywheels.remainingVolley(this, serverLevel);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u6587\u7384\u84dd\u98de\u8f6e\u5df2\u8fbe\u4e0a\u9650 {} \u4e2a -> \u505c\u6b62\u6295\u63b7", (Object)15);
            return;
        }
        try {
            FlywheelProjectileEntity flywheelProjectileEntity = new FlywheelProjectileEntity((Level)serverLevel, (LivingEntity)this);
            double d = Math.toRadians((double)this.m_146908_() + (this.f_19796_.m_188500_() - 0.5) * 50.0);
            double d2 = -Math.sin(d);
            double d3 = Math.cos(d);
            flywheelProjectileEntity.m_6034_(this.m_20185_() + d2 * 0.8, this.m_20188_() - 0.15, this.m_20189_() + d3 * 0.8);
            flywheelProjectileEntity.m_6686_(d2 * 0.55, 0.05, d3 * 0.55, 1.1f, 0.0f);
            ManorFightMembers.inherit((Entity)this, (Entity)flywheelProjectileEntity);
            serverLevel.m_7967_((Entity)flywheelProjectileEntity);
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12520_, SoundSource.HOSTILE, 1.0f, 1.4f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u6587\u7384\u84dd\u6295\u51fa\u98de\u8f6e (\u8fd8\u5269 {} \u4e2a)", (Object)this.flywheelShots);
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u6295\u63b7\u98de\u8f6e\u5931\u8d25: {}", (Object)throwable.toString());
            this.flywheelShots = 0;
        }
    }

    private void flywheelVolley(ServerLevel serverLevel) {
        int n = 0;
        try {
            for (FlywheelProjectileEntity flywheelProjectileEntity2 : serverLevel.m_6443_(FlywheelProjectileEntity.class, this.m_20191_().m_82400_(48.0), flywheelProjectileEntity -> flywheelProjectileEntity.m_19749_() == this && flywheelProjectileEntity.isOrbiting())) {
                flywheelProjectileEntity2.launchAgain();
                ++n;
            }
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u98de\u8f6e\u9f50\u5c04\u5f02\u5e38: {}", (Object)throwable.toString());
        }
        if (n > 0) {
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12518_, SoundSource.HOSTILE, 1.2f, 0.8f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u6587\u7384\u84dd: {} \u4e2a\u98de\u8f6e\u9f50\u5c04!", (Object)n);
        }
    }

    private void skillBeamCombo(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.playImpactAction(20);
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        this.beamStrike(serverLevel, livingEntity);
        BodySignals.combo(this);
        this.flywheelVolley(serverLevel);
        double d = this.flywheelDamageMul;
        this.flywheelDamageMul = Math.min(5.0, this.flywheelDamageMul + 0.1);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u6587\u7384\u84dd \u5149\u675f\u8fde\u62db\u5b8c\u6210: \u98de\u8f6e\u653b\u51fb\u529b x{} -> x{}", (Object)String.format("%.2f", d), (Object)String.format("%.2f", this.flywheelDamageMul));
    }

    private void beamStrike(ServerLevel serverLevel, LivingEntity livingEntity) {
        Vec3 vec3 = new Vec3(this.m_20185_(), this.m_20188_() - 0.15, this.m_20189_());
        Vec3 vec32 = new Vec3(livingEntity.m_20185_(), livingEntity.m_20188_(), livingEntity.m_20189_());
        Vec3 vec33 = vec3;
        Vec3 vec34 = vec32;
        Vec3 vec35 = (vec32 = this.clipSkillRay(vec33, XuanlanLengths.extend(vec33, vec34))).m_82546_(vec3);
        double d = vec35.m_82553_();
        if (d < 0.2) {
            return;
        }
        Vec3 vec36 = vec35.m_82541_();
        this.syncBeamCue(vec3, vec32, 2.6, 12, true, true);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123745_, vec32.f_82479_, vec32.f_82480_, vec32.f_82481_, 14, 0.3, 0.3, 0.3, 0.05);
        serverLevel.m_6263_(null, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, SoundEvents.f_11736_, SoundSource.HOSTILE, 1.5f, 1.5f);
        serverLevel.m_6263_(null, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, SoundEvents.f_12521_, SoundSource.HOSTILE, 1.1f, 1.6f);
        int n = 0;
        for (LivingEntity livingEntity2 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this, XuanlanLengths.segmentBounds(vec3, vec32, 2.6))) {
            Vec3 vec37;
            double d2;
            if (!this.combatPlayer(livingEntity2) || !this.m_142582_((Entity)livingEntity2) || (d2 = (vec37 = livingEntity2.m_20182_().m_82520_(0.0, (double)livingEntity2.m_20206_() * 0.5, 0.0).m_82546_(vec3)).m_82526_(vec36)) < 0.0 || d2 > d || vec37.m_82546_(vec36.m_82490_(d2)).m_82553_() > 2.6 + (double)livingEntity2.m_20205_() * 0.5) continue;
            livingEntity2.f_19802_ = 0;
            livingEntity2.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), this.manorSkillDamage(livingEntity2, 251.4f, 2.0));
            BossKnockback.push((Entity)this, (Entity)livingEntity2, vec36.f_82479_ * 0.63, 0.18, vec36.f_82481_ * 0.63);
            livingEntity2.f_19864_ = true;
            ++n;
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u6587\u7384\u84dd\u300c\u5149\u675f\u6253\u51fb\u300d\u547d\u4e2d {} \u4eba ({} \u4f24\u5bb3)", (Object)n, (Object)String.format("%.1f", Float.valueOf(60.0f)));
    }

    private int countFlywheels(ServerLevel serverLevel) {
        try {
            return serverLevel.m_6443_(FlywheelProjectileEntity.class, this.m_20191_().m_82400_(48.0), flywheelProjectileEntity -> flywheelProjectileEntity.m_19749_() == this).size();
        }
        catch (Throwable throwable) {
            return 0;
        }
    }

    private void addBloodRage(ServerLevel serverLevel, LivingEntity livingEntity) {
        MobEffect mobEffect;
        boolean bl;
        AttributeInstance attributeInstance;
        if (this.bloodRage < 5) {
            ++this.bloodRage;
        }
        if ((attributeInstance = this.m_21051_(Attributes.f_22281_)) != null) {
            attributeInstance.m_22120_(BLOOD_RAGE_MOD);
            attributeInstance.m_22118_(new AttributeModifier(BLOOD_RAGE_MOD, "ftboss.blood_rage", 0.5 * (double)this.bloodRage, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        AttributeInstance attributeInstance2 = this.m_21051_(Attributes.f_22279_);
        boolean bl2 = bl = this.bloodRage >= 5;
        if (attributeInstance2 != null) {
            attributeInstance2.m_22120_(BLOOD_RAGE_SPEED_MOD);
            if (bl) {
                attributeInstance2.m_22118_(new AttributeModifier(BLOOD_RAGE_SPEED_MOD, "ftboss.blood_rage_speed", 0.4, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        if ((mobEffect = (MobEffect)FTEffects.BLOOD_RAGE.get()) != null) {
            this.m_7292_(new MobEffectInstance(mobEffect, 800, Math.max(0, this.bloodRage - 1), false, true, true));
        }
        double d = 0.5 * (double)this.bloodRage * 100.0;
        String string = bl ? String.format(" \u00b7 \u79fb\u901f +%.0f%%", 40.0) : "";
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123798_, this.m_20185_(), this.m_20186_() + 1.2, this.m_20189_(), 8, 0.3, 0.4, 0.3, 0.05);
        if (livingEntity != null) {
            ManorLivingTargets.tell(livingEntity, (Component)Component.m_237113_((String)String.format("\u00a7c\u529f\u5e95 \u8840\u6012 %d/%d \u00a77(+%.0f%% \u653b\u51fb\u529b \u00b7 \u5438\u8840 %.0f%% \u00b7 \u771f\u5b9e\u4f24\u5bb3 %.0f%%%s)", this.bloodRage, 5, d, 15.0, 10.0, string)), true);
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u529f\u5e95 \u8840\u6012 {}/{} (+{}% \u653b\u51fb, \u5438\u8840 {}%, \u771f\u5b9e\u4f24\u5bb3 {}%{})", new Object[]{this.bloodRage, 5, (int)d, 15, 10, string});
    }

    private void bloodRageOnHit(LivingEntity livingEntity, float f) {
        float f2;
        if (this.bloodRage <= 0 || livingEntity == null || f <= 0.0f) {
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        float f3 = (float)((double)f * 0.15);
        if (f3 > 0.0f && this.m_21223_() < this.m_21233_()) {
            this.m_5634_(f3);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123750_, this.m_20185_(), this.m_20186_() + 1.7, this.m_20189_(), 2, 0.3, 0.3, 0.3, 0.0);
        }
        if ((f2 = (float)((double)f * 0.1)) > 0.0f) {
            livingEntity.f_19802_ = 0;
            livingEntity.m_6469_(BossDamageTuning.trueDamage(serverLevel, (Entity)this), f2);
        }
    }

    public static void applyXueliFrost(LivingEntity livingEntity) {
        if (livingEntity == null || livingEntity.m_9236_().f_46443_) {
            return;
        }
        String string = livingEntity.m_7755_().getString();
        if (!livingEntity.m_142079_()) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u7ec6\u96ea\u8df3\u8fc7 -> {} (canFreeze=false)", (Object)string);
            return;
        }
        int n = livingEntity.m_146891_();
        int n2 = livingEntity.m_146888_();
        livingEntity.m_146917_(Math.min(n2 + 90, n + 60));
        int n3 = livingEntity.m_146888_();
        livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 60, 0, false, true, true));
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u7ec6\u96ea -> {} (\u8fdb\u5ea6 {} -> {} / \u9608\u503c {}{})", new Object[]{string, n2, n3, n, n3 >= n ? ", \u5df2\u51bb\u5b9e" : ""});
    }

    private void startCrush(LivingEntity livingEntity) {
        this.crushTicks.put(livingEntity.m_20148_(), 60);
        Component.m_237113_((String)"\u00a76\u96ea\u9ece \u00a77\u7684\u64c0\u9762\u6756\u538b\u4f4f\u4e86\u4f60: \u00a7c\u7c89\u788e\u8f6e \u00a77(60 tick \u00d7 5 \u70b9)");
        Level level = this.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_(), livingEntity.m_20189_(), SoundEvents.f_11998_, SoundSource.HOSTILE, 1.2f, 0.6f);
            if (serverLevel.m_46467_() >= this.xueliRollingPinQuoteReadyAt && this.f_19796_.m_188501_() < 0.2f) {
                this.xueliRollingPinQuoteReadyAt = serverLevel.m_46467_() + 100L;
                this.sayXueliDialogue("\u6469\u6258\u7f57\u62c9\u54d2\uff01");
            }
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece \u64c0\u9762\u6756 -> {} \u5403\u5230\u7c89\u788e\u8f6e {} tick x {} \u70b9", new Object[]{livingEntity.m_7755_().getString(), 60, 5});
        XueliCombatServer.crush(this, livingEntity);
    }

    private void xueliCrushTick() {
        if (this.crushTicks.isEmpty()) {
            return;
        }
        Object object = this.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = this.crushTicks.entrySet().iterator();
        while (object.hasNext()) {
            Map.Entry entry = (Map.Entry)object.next();
            Player player = serverLevel.m_46003_((UUID)entry.getKey());
            int n = (Integer)entry.getValue() - 1;
            if (player == null || !player.m_6084_() || player.m_5833_() || n <= 0) {
                object.remove();
                continue;
            }
            entry.setValue(n);
            player.f_19802_ = 0;
            XueliCrushDamage.hurt((LivingEntity)player, this.crushDamageSource(serverLevel), 5.0f);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123797_, player.m_20185_(), player.m_20186_() + 0.6, player.m_20189_(), 2, 0.25, 0.2, 0.25, 0.02);
            if (n % 10 != 0) continue;
            serverLevel.m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11998_, SoundSource.HOSTILE, 0.7f, 0.5f);
        }
    }

    private DamageSource crushDamageSource(ServerLevel serverLevel) {
        return new DamageSource((Holder)serverLevel.m_9598_().m_175515_(Registries.f_268580_).m_246971_(DamageTypes.f_268724_), (Entity)this, (Entity)this);
    }

    private void skillXueli(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.playImpactAction(20);
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        try {
            StockpotEntity stockpotEntity = new StockpotEntity((Level)serverLevel, (LivingEntity)this);
            double d = livingEntity.m_20185_() - this.m_20185_();
            double d2 = livingEntity.m_20188_() - (this.m_20188_() - 0.2);
            double d3 = livingEntity.m_20189_() - this.m_20189_();
            stockpotEntity.m_6034_(this.m_20185_(), this.m_20188_() - 0.2, this.m_20189_());
            stockpotEntity.m_6686_(d, d2 + 2.0, d3, 1.05f, 1.0f);
            ManorFightMembers.inherit((Entity)this, (Entity)stockpotEntity);
            CastServer.spawned(serverLevel.m_7967_((Entity)stockpotEntity), this, "stockpot_throw");
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12520_, SoundSource.HOSTILE, 1.1f, 0.8f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece \u6254\u51fa\u6c64\u9505 -> {}", (Object)livingEntity.m_7755_().getString());
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u96ea\u9ece\u6295\u6c64\u9505\u5931\u8d25: {}", (Object)throwable.toString());
        }
    }

    private void skillXueliKnives(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.playImpactAction(16);
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        int n = 0;
        try {
            for (int i = 0; i < 3; ++i) {
                XueliKnifeEntity xueliKnifeEntity = new XueliKnifeEntity((Level)serverLevel, (LivingEntity)this);
                xueliKnifeEntity.m_6034_(this.m_20185_(), this.m_20188_() - 0.2, this.m_20189_());
                double d = ((double)i - 1.0) * 0.09;
                double d2 = livingEntity.m_20185_() - this.m_20185_();
                double d3 = livingEntity.m_20188_() - (this.m_20188_() - 0.2);
                double d4 = livingEntity.m_20189_() - this.m_20189_();
                double d5 = Math.max(0.1, Math.sqrt(d2 * d2 + d4 * d4));
                double d6 = d2 / d5;
                double d7 = d4 / d5;
                xueliKnifeEntity.m_6686_(d6 * Math.cos(d) - d7 * Math.sin(d), d3 / d5 + 0.06, d6 * Math.sin(d) + d7 * Math.cos(d), 2.4f, 0.0f);
                ManorFightMembers.inherit((Entity)this, (Entity)xueliKnifeEntity);
                CastServer.spawned(serverLevel.m_7967_((Entity)xueliKnifeEntity), this, "knife_throw");
                ++n;
            }
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12520_, SoundSource.HOSTILE, 1.0f, 1.9f);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123797_, this.m_20185_(), this.m_20188_(), this.m_20189_(), 10, 0.3, 0.3, 0.3, 0.12);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u300c\u7981\u7597\u98de\u5200\u300d\u63b7\u51fa {} \u628a -> {}", (Object)n, (Object)livingEntity.m_7755_().getString());
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u96ea\u9ece\u63b7\u98de\u5200\u5931\u8d25: {}", (Object)throwable.toString());
        }
    }

    private void xueliCloneTick(ServerLevel serverLevel) {
        if (this.xueliCloneCd > 0) {
            --this.xueliCloneCd;
        }
        if (this.xueliCloneCd > 0) {
            return;
        }
        if (this.m_21525_() || this.skillCommitted() || this.xueliSlamLeft > 0 || this.xueliSlamGap > 0 || this.castTicks > 0 || this.actionTicks > 0) {
            return;
        }
        if (this.aliveXueliClones(serverLevel) > 0) {
            return;
        }
        this.xueliCloneCd = 600;
        this.skillXueliClones(serverLevel, this.m_5448_());
    }

    private int aliveXueliClones(ServerLevel serverLevel) {
        int n = 0;
        for (EasterBossEntity easterBossEntity : serverLevel.m_45976_(EasterBossEntity.class, this.m_20191_().m_82400_(64.0))) {
            if (easterBossEntity == this || !easterBossEntity.isMini() || easterBossEntity.getVariant() != 10 || !easterBossEntity.m_6084_() || easterBossEntity.m_21224_()) continue;
            ++n;
        }
        return n;
    }

    private void skillXueliClones(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.playCast(HiddenBossCastRules.quicker(30), 0);
        int n = 0;
        for (int i = 0; i < 2; ++i) {
            try {
                EasterBossEntity easterBossEntity = (EasterBossEntity)((EntityType)FTEntities.EASTER_BOSS.get()).m_20615_((Level)serverLevel);
                if (easterBossEntity == null) continue;
                double d = this.f_19796_.m_188500_() * Math.PI * 2.0;
                double d2 = this.m_20185_() + Math.cos(d) * 3.0;
                double d3 = this.m_20189_() + Math.sin(d) * 3.0;
                easterBossEntity.m_7678_(d2, this.m_20186_(), d3, this.f_19796_.m_188501_() * 360.0f, 0.0f);
                easterBossEntity.setVariant(10);
                easterBossEntity.setManorSummonerId(this.manorSummonerId);
                easterBossEntity.setMini(true);
                easterBossEntity.m_6593_((Component)Component.m_237113_((String)"\u00a7b\u96ea\u9ece\u7684\u5206\u8eab"));
                easterBossEntity.m_20340_(true);
                AttributeInstance attributeInstance = easterBossEntity.m_21051_(Attributes.f_22276_);
                if (attributeInstance != null) {
                    attributeInstance.m_22100_(80.0);
                }
                easterBossEntity.m_21153_(80.0f);
                easterBossEntity.m_21557_(false);
                easterBossEntity.bombFuse = 100;
                if (livingEntity != null) {
                    easterBossEntity.m_6710_(livingEntity);
                }
                BossBarVisibility.visible(easterBossEntity.getBossBar(), false);
                BossBars.untrack((Entity)easterBossEntity, easterBossEntity.getBossBar());
                ManorFightMembers.inherit((Entity)this, (Entity)easterBossEntity);
                boolean bl = serverLevel.m_7967_((Entity)easterBossEntity);
                CastServer.spawned(bl, this, "clone_call");
                if (!bl) continue;
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, d2, this.m_20186_() + 0.6, d3, 18, 0.4, 0.4, 0.4, 0.06);
                ++n;
                continue;
            }
            catch (Throwable throwable) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] \u96ea\u9ece\u53ec\u5206\u8eab\u5931\u8d25: {}", (Object)throwable.toString());
            }
        }
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11862_, SoundSource.HOSTILE, 1.3f, 1.1f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u300c\u81ea\u7206\u5206\u8eab\u300d\u53ec\u51fa {} \u4e2a(\u5f15\u4fe1 {} \u79d2)", (Object)n, (Object)5);
        if (n > 0) {
            this.sayXueliDialogue("\u542f\u52a8\u540e\u5907\u9690\u85cf\u80fd\u6e90\uff01");
        }
    }

    private void bombFuseTick(ServerLevel serverLevel) {
        if (this.bombFuse <= 0) {
            XueliCombatServer.fuse(this, this.bombFuse);
            return;
        }
        if (--this.bombFuse > 0) {
            XueliCombatServer.fuse(this, this.bombFuse);
            return;
        }
        this.detonate(serverLevel, false);
        XueliCombatServer.fuse(this, this.bombFuse);
    }

    private void detonate(ServerLevel serverLevel, boolean bl) {
        if (this.bombBlown) {
            return;
        }
        this.bombBlown = true;
        XueliCombatServer.burst(this, bl);
        double d = bl ? 3.375 : 4.5;
        float f = bl ? 14.0f : 26.0f;
        double d2 = this.m_20185_();
        double d3 = this.m_20186_() + 0.7;
        double d4 = this.m_20189_();
        if (serverLevel != null) {
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123812_, d2, d3, d4, 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.m_6263_(null, d2, d3, d4, SoundEvents.f_11913_, SoundSource.HOSTILE, 1.5f, 0.85f);
            Fx.particles((Level)serverLevel, d2, d3, d4, 36, (ParticleOptions)ParticleTypes.f_123813_, 2.0, 0.12);
            Fx.particles((Level)serverLevel, d2, d3, d4, 18, (ParticleOptions)ParticleTypes.f_123755_, 1.5, 0.06);
            int n = 0;
            for (Player player : serverLevel.m_6907_()) {
                double d5;
                if (!player.m_6084_() || player.m_5833_() || (d5 = Math.sqrt(player.m_20275_(d2, d3, d4))) > d) continue;
                player.f_19802_ = 0;
                player.m_6469_(this.m_269291_().m_269036_((Entity)this, (Entity)this), (float)((double)f * Math.max(0.25, 1.0 - d5 / d)) * 0.6666667f);
                EasterBossEntity.applyXueliFrost((LivingEntity)player);
                BossKnockback.knockback((LivingEntity)player, 1.1, d2 - player.m_20185_(), d4 - player.m_20189_());
                ++n;
            }
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u5206\u8eab\u81ea\u7206({}) \u547d\u4e2d {} \u4eba (\u57fa\u51c6 {} \u4f24\u5bb3, \u534a\u5f84 {} \u683c)", new Object[]{bl ? "\u88ab\u6253\u6b7b\u00b7\u5f31\u5316" : "\u5f15\u4fe1\u5230\u70b9", n, String.format("%.1f", (double)f * 0.6666666666666666), (int)d});
        }
        this.m_146870_();
    }

    private void skillXueliPanSlap(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.xueliPanWind = this.getChallengeDifficulty().acceleratedTicks(XUELI_PAN_WINDUP);
        this.playCast(this.xueliPanWind, 1);
        CastServer.nativeAction(this, "pan_cast", "charge", this.xueliPanWind);
        this.beginSkillCue(this.m_20182_(), 4.5, this.xueliPanWind, true);
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11669_, SoundSource.HOSTILE, 0.7f, 1.4f);
        XueliCombatServer.charge(this, 3);
    }

    private void executeXueliPanSlap(ServerLevel serverLevel) {
        CastServer.nativeAction(this, "pan_cast", "release", 16);
        this.playImpactAction(16);
        this.releaseSkillCue(this.m_20182_());
        XueliCombatServer.impact(this, 4);
        double d = this.m_20185_();
        double d2 = this.m_20186_();
        double d3 = this.m_20189_();
        serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_12317_, SoundSource.HOSTILE, 1.6f, 0.7f);
        serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11668_, SoundSource.HOSTILE, 1.1f, 1.4f);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123766_, d, d2 + 1.0, d3, 14, 2.2, 0.4, 2.2, 0.0);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_175821_, d, d2 + 0.6, d3, 24, 1.8, 0.3, 1.8, 0.06);
        int n = 0;
        for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
            if (!this.combatPlayer(livingEntity) || livingEntity.m_20275_(d, d2, d3) > 20.25) continue;
            livingEntity.f_19802_ = 0;
            livingEntity.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 30.0f);
            EasterBossEntity.applyXueliFrost(livingEntity);
            livingEntity.m_20254_(4);
            double d4 = livingEntity.m_20185_() - d;
            double d5 = livingEntity.m_20189_() - d3;
            double d6 = Math.max(0.1, Math.sqrt(d4 * d4 + d5 * d5));
            BossKnockback.push((Entity)livingEntity, d4 / d6 * 0.8, 0.3, d5 / d6 * 0.8);
            ++n;
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u300c\u5e73\u5e95\u9505\u6a2a\u626b\u300d\u547d\u4e2d {} \u4eba ({} \u4f24\u5bb3 + \u70b9\u71c3 4 \u79d2)", (Object)n, (Object)30);
    }

    private void skillXueliRoll(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.playAction(22);
        this.xueliRollTicks = 30;
        this.xueliRollHitCd = 0;
        this.sayXueliDialogue("\u538b\u8def\u673a\u6765\u54af\uff5e");
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12363_, SoundSource.HOSTILE, 1.1f, 1.3f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12070_, SoundSource.HOSTILE, 0.9f, 1.6f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u300c\u64c0\u9762\u6756\u538b\u8def\u51b2\u950b\u300d\u5f00\u59cb({} tick)", (Object)30);
        CastServer.nativeAction(this, "roll_cast", "hold", 30);
    }

    private void xueliRollTick() {
        Vec3 vec3;
        if (this.xueliRollTicks <= 0) {
            XueliCombatServer.roll(this, this.xueliRollTicks);
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            this.xueliRollTicks = 0;
            XueliCombatServer.roll(this, this.xueliRollTicks);
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        --this.xueliRollTicks;
        level = this.m_5448_();
        if (level != null && level.m_6084_() && (vec3 = level.m_20182_().m_82546_(this.m_20182_())).m_82556_() > 1.0) {
            Vec3 vec32 = vec3.m_82541_();
            this.m_20334_(vec32.f_82479_ * 0.95, Math.max(0.05, vec32.f_82480_ * 0.28), vec32.f_82481_ * 0.95);
            this.f_19812_ = true;
            this.m_146922_((float)Math.toDegrees(Math.atan2(vec32.f_82479_, vec32.f_82481_)));
            this.f_19859_ = this.m_146908_();
        }
        if (this.xueliRollHitCd > 0) {
            --this.xueliRollHitCd;
        }
        if (this.xueliRollHitCd <= 0) {
            int n = 0;
            for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
                if (!livingEntity.m_6084_() || livingEntity.m_20280_((Entity)this) > 9.0) continue;
                livingEntity.f_19802_ = 0;
                livingEntity.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 12.0f);
                BossKnockback.push((Entity)livingEntity, (livingEntity.m_20185_() - this.m_20185_()) * 0.35, 0.25, (livingEntity.m_20189_() - this.m_20189_()) * 0.35);
                EasterBossEntity.applyXueliFrost(livingEntity);
                this.startCrush(livingEntity);
                ++n;
            }
            if (n > 0) {
                this.xueliRollHitCd = 6;
            }
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123797_, this.m_20185_(), this.m_20186_() + 0.4, this.m_20189_(), 4, 0.3, 0.2, 0.3, 0.06);
        if (this.xueliRollTicks <= 0) {
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11669_, SoundSource.HOSTILE, 1.0f, 0.8f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece \u538b\u8def\u51b2\u950b\u7ed3\u675f");
        }
        XueliCombatServer.roll(this, this.xueliRollTicks);
    }

    private boolean skillXueliSlam(ServerLevel serverLevel, LivingEntity livingEntity) {
        LivingEntity livingEntity2 = this.pickSlamVictim(livingEntity);
        if (livingEntity2 == null) {
            return false;
        }
        this.xueliSlamLeft = 5;
        this.xueliSlamGap = 0;
        this.launchXueliSlam(serverLevel, livingEntity2);
        return true;
    }

    private LivingEntity pickSlamVictim(LivingEntity livingEntity) {
        if (ManorLivingTargets.valid((Entity)this, livingEntity)) {
            return livingEntity;
        }
        return ManorCombatContinuity.fightPlayer(this);
    }

    private void launchXueliSlam(ServerLevel serverLevel, LivingEntity livingEntity) {
        if (!this.canTeleportWith(livingEntity)) {
            return;
        }
        int n = 5 - this.xueliSlamLeft + 1;
        this.xueliSlamX = livingEntity.m_20185_();
        this.xueliSlamY = livingEntity.m_20186_();
        this.xueliSlamZ = livingEntity.m_20189_();
        this.xueliSlamWarn = XUELI_SLAM_WARN_TICKS;
        this.playCast(XUELI_SLAM_WARN_TICKS, 3);
        CastServer.nativeAction(this, "slam_cast", "charge", this.xueliSlamWarn);
        this.beginSkillCue(new Vec3(this.xueliSlamX, this.xueliSlamY, this.xueliSlamZ), 8.0, XUELI_SLAM_WARN_TICKS, false);
        XueliCombatServer.charge(this, 7);
        this.m_21573_().m_26573_();
        this.m_20242_(true);
        this.m_20334_(0.0, 0.0, 0.0);
        this.f_19812_ = true;
        this.m_6021_(this.xueliSlamX, this.xueliSlamY + 12.0, this.xueliSlamZ);
        this.m_146926_(90.0f);
        this.f_19789_ = 0.0f;
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11852_, SoundSource.HOSTILE, 1.5f, 0.6f);
        serverLevel.m_6263_(null, this.xueliSlamX, this.xueliSlamY, this.xueliSlamZ, SoundEvents.f_11736_, SoundSource.HOSTILE, 0.8f, 1.7f);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123789_, this.m_20185_(), this.m_20186_(), this.m_20189_(), 40, 0.5, 0.5, 0.5, 0.25);
        String cfr_ignored_0 = "\u00a7b\u3010\u5929\u964d\u77ac\u7838 " + n + "/5\u3011\u00a77\u96ea\u9ece\u7a9c\u4e0a\u534a\u7a7a \u2014\u2014 \u00a7c" + (double)XUELI_SLAM_WARN_TICKS / 20.0 + " \u79d2\u540e\u7838\u843d, \u5feb\u79bb\u5f00\u96ea\u5708!";
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u300c\u5929\u964d\u77ac\u7838\u300d\u7b2c {}/{} \u53d1\u8d77\u624b: \u60ac\u505c {} tick, \u843d\u70b9 ({}, {}, {})", new Object[]{n, 5, XUELI_SLAM_WARN_TICKS, (int)this.xueliSlamX, (int)this.xueliSlamY, (int)this.xueliSlamZ});
    }

    private void xueliSlamTick(ServerLevel serverLevel) {
        if (this.xueliSlamWarn <= 0) {
            this.xueliSlamGapTick(serverLevel);
            return;
        }
        this.m_21573_().m_26573_();
        this.m_20242_(true);
        this.m_20334_(0.0, 0.0, 0.0);
        this.f_19812_ = true;
        this.m_6021_(this.xueliSlamX, this.xueliSlamY + 12.0, this.xueliSlamZ);
        this.m_146926_(90.0f);
        this.f_19789_ = 0.0f;
        int n = this.xueliSlamWarn;
        float f = 1.0f - (float)n / (float)XUELI_SLAM_WARN_TICKS;
        if (n % 4 == 0) {
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_175821_, this.m_20185_(), this.m_20186_() + 0.6, this.m_20189_(), 6, 0.5, 0.3, 0.5, 0.02);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, this.m_20185_(), this.m_20186_() - 1.2, this.m_20189_(), 2, 0.3, 0.4, 0.3, 0.02);
        }
        if (n % 20 == 0 || n <= 5 && n % 2 == 0) {
            serverLevel.m_6263_(null, this.xueliSlamX, this.xueliSlamY, this.xueliSlamZ, SoundEvents.f_11871_, SoundSource.HOSTILE, 1.4f, 0.7f + f * 0.7f);
        }
        if (--this.xueliSlamWarn <= 0) {
            this.xueliSlamDown(serverLevel);
        }
    }

    private void xueliSlamGapTick(ServerLevel serverLevel) {
        if (this.xueliSlamGap <= 0) {
            return;
        }
        if (--this.xueliSlamGap > 0) {
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_175821_, this.m_20185_(), this.m_20186_() + 0.2, this.m_20189_(), 4, 1.2, 0.15, 1.2, 0.01);
            return;
        }
        if (this.xueliSlamLeft <= 0) {
            return;
        }
        LivingEntity livingEntity = this.pickSlamVictim(this.m_5448_());
        if (livingEntity == null) {
            this.xueliSlamLeft = 0;
            this.xueliSlamGap = 0;
            this.m_20242_(false);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u300c\u5929\u964d\u77ac\u7838\u300d\u8fde\u7838\u4e2d\u65ad(\u573a\u4e0a\u6ca1\u6709\u76ee\u6807)");
            return;
        }
        this.launchXueliSlam(serverLevel, livingEntity);
    }

    private void xueliSlamDown(ServerLevel serverLevel) {
        boolean bl;
        CastServer.nativeAction(this, "slam_cast", "release", 14);
        this.playImpactAction(14);
        this.releaseSkillCue(new Vec3(this.xueliSlamX, this.xueliSlamY, this.xueliSlamZ));
        XueliCombatServer.impact(this, 8);
        this.xueliSlamWarn = 0;
        this.m_20242_(false);
        this.f_19789_ = 0.0f;
        this.m_6021_(this.xueliSlamX, this.xueliSlamY, this.xueliSlamZ);
        this.m_146926_(0.0f);
        double d = this.xueliSlamX;
        double d2 = this.xueliSlamY;
        double d3 = this.xueliSlamZ;
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123812_, d, d2 + 0.4, d3, 1, 0.0, 0.0, 0.0, 0.0);
        Fx.particles((Level)serverLevel, d, d2 + 0.4, d3, 60, (ParticleOptions)ParticleTypes.f_175821_, 8.0, 0.35);
        Fx.particles((Level)serverLevel, d, d2 + 0.4, d3, 28, (ParticleOptions)ParticleTypes.f_123796_, 3.2, 0.12);
        Fx.particles((Level)serverLevel, d, d2 + 0.4, d3, 22, (ParticleOptions)ParticleTypes.f_123797_, 2.6, 0.25);
        serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11913_, SoundSource.HOSTILE, 1.8f, 0.55f);
        serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11668_, SoundSource.HOSTILE, 1.6f, 0.5f);
        int n = 0;
        for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
            if (!this.combatPlayer(livingEntity) || livingEntity.m_20275_(d, d2, d3) > 64.0) continue;
            livingEntity.f_19802_ = 0;
            livingEntity.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 40.0f);
            EasterBossEntity.applyXueliFrost(livingEntity);
            this.stunPlayer(livingEntity, 20);
            ++n;
        }
        int n2 = 5 - this.xueliSlamLeft + 1;
        if (this.xueliSlamLeft > 0) {
            --this.xueliSlamLeft;
        }
        boolean bl2 = bl = this.xueliSlamLeft > 0;
        String string = n > 0 ? "\u00a7b\u3010\u5929\u964d\u77ac\u7838 " + n2 + "/5\u3011\u00a77\u7838\u4e2d " + n + " \u4eba \u2014\u2014 \u00a7c\u7729\u6655 1 \u79d2!" + (String)(bl ? " \u00a77\u8fd8\u6709 " + this.xueliSlamLeft + " \u53d1!" : "") : "\u00a7b\u3010\u5929\u964d\u77ac\u7838 " + n2 + "/5\u3011\u00a77\u7838\u7a7a\u4e86" + (String)(bl ? " \u00a77\u8fd8\u6709 " + this.xueliSlamLeft + " \u53d1!" : "");
        this.xueliSlamGap = bl ? 20 : 0;
        if (n2 == 1) {
            this.sayXueliDialogue("\u4e4c\u9e26\u5750\u98de\u673a\uff01");
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u300c\u5929\u964d\u77ac\u7838\u300d\u7b2c {}/{} \u53d1\u7838\u843d: \u547d\u4e2d {} \u4eba ({} \u4f24\u5bb3 + \u7729\u6655 {} \u79d2, \u534a\u5f84 {} \u683c), \u5269\u4f59 {} \u53d1", new Object[]{n2, 5, n, 40, 1, 8, this.xueliSlamLeft});
    }

    private void stunPlayer(LivingEntity livingEntity, int n) {
        if (!this.canTeleportWith(livingEntity)) {
            return;
        }
        this.stunPlayerAny(livingEntity, n, "\u96ea\u9ece\u7684\u7838\u51fb");
    }

    private void stunPlayerAny(LivingEntity livingEntity, int n, String string) {
        if (livingEntity == null || !livingEntity.m_6084_() || livingEntity.m_5833_()) {
            return;
        }
        this.stunTicks.merge(livingEntity.m_20148_(), n, Math::max);
        this.stunAnchor.put(livingEntity.m_20148_(), new double[]{livingEntity.m_20185_(), livingEntity.m_20186_(), livingEntity.m_20189_()});
        XueliCombatServer.stunTell(livingEntity, (Component)Component.m_237113_((String)("\u00a7b" + string + "\u00a7c\u8ba9\u4f60\u773c\u5192\u91d1\u661f \u2014\u2014 \u00a7c\u7729\u6655 " + n / 20 + " \u79d2")), true, this);
        XueliCombatServer.stun(this, livingEntity, n);
    }

    private void stunPlayerWithLaunch(LivingEntity livingEntity, int n, String string, double d, double d2, double d3) {
        if (livingEntity == null || !livingEntity.m_6084_() || livingEntity.m_5833_()) {
            return;
        }
        this.stunPlayerAny(livingEntity, n, string);
        this.stunLaunch.put(livingEntity.m_20148_(), new double[]{d, d2, d3});
    }

    private void applyStunLaunch(LivingEntity livingEntity) {
        if (livingEntity == null) {
            return;
        }
        double[] dArray = this.stunLaunch.remove(livingEntity.m_20148_());
        if (dArray == null || !livingEntity.m_6084_() || livingEntity.m_5833_()) {
            return;
        }
        BossKnockback.push((Entity)this, (Entity)livingEntity, dArray[0], dArray[1], dArray[2]);
        livingEntity.f_19864_ = true;
        ManorLivingTargets.tell(livingEntity, (Component)Component.m_237113_((String)"\u00a7c\u7729\u6655\u7ed3\u675f \u2014\u2014 \u00a77\u4f60\u88ab\u3010\u8d85\u80fd\u5149\u675f\u3011\u51fb\u98de\uff01"), true);
        Level level = livingEntity.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, livingEntity.m_20185_(), livingEntity.m_20186_() + 0.2, livingEntity.m_20189_(), 14, 0.4, 0.15, 0.4, 0.08);
        }
    }

    private void xueliStunTick(ServerLevel serverLevel) {
        if (this.stunTicks.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, Integer>> iterator = this.stunTicks.entrySet().iterator();
        while (iterator.hasNext()) {
            Vec3 vec3;
            Map.Entry<UUID, Integer> entry = iterator.next();
            LivingEntity livingEntity = ManorLivingTargets.resolve((Level)serverLevel, entry.getKey());
            int n = entry.getValue() - 1;
            if (livingEntity == null || !livingEntity.m_6084_() || livingEntity.m_5833_() || n <= 0) {
                this.stunAnchor.remove(entry.getKey());
                this.applyStunLaunch(livingEntity);
                iterator.remove();
                continue;
            }
            entry.setValue(n);
            double[] dArray = this.stunAnchor.get(livingEntity.m_20148_());
            if (dArray != null && livingEntity instanceof ServerPlayer) {
                vec3 = (ServerPlayer)livingEntity;
                if (vec3.f_8906_ != null) {
                    vec3.f_8906_.m_9774_(dArray[0], dArray[1], dArray[2], livingEntity.m_146908_(), livingEntity.m_146909_());
                }
            }
            vec3 = livingEntity.m_20184_();
            livingEntity.m_20334_(0.0, Math.min(0.0, vec3.f_82480_), 0.0);
            livingEntity.f_19864_ = true;
            livingEntity.m_6858_(false);
            livingEntity.m_5810_();
            livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 15, 6, false, false, true));
            double d = (double)livingEntity.f_19797_ * 0.35;
            for (int i = 0; i < 3; ++i) {
                double d2 = d + (double)i * 2.0943951023931953;
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123797_, livingEntity.m_20185_() + Math.cos(d2) * 0.55, livingEntity.m_20186_() + (double)livingEntity.m_20206_() + 0.3, livingEntity.m_20189_() + Math.sin(d2) * 0.55, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    private void xuanlanAimAtTarget() {
        LivingEntity livingEntity = this.m_5448_();
        if (livingEntity == null || !livingEntity.m_6084_()) {
            return;
        }
        double d = this.m_20185_();
        double d2 = this.m_20188_();
        double d3 = this.m_20189_();
        double d4 = livingEntity.m_20186_() + (double)livingEntity.m_20206_() * 0.5;
        double d5 = livingEntity.m_20185_() - d;
        double d6 = d4 - d2;
        double d7 = livingEntity.m_20189_() - d3;
        double d8 = Math.max(0.001, Math.sqrt(d5 * d5 + d6 * d6 + d7 * d7));
        this.xlBeamDx = d5 / d8;
        this.xlBeamDy = d6 / d8;
        this.xlBeamDz = d7 / d8;
    }

    public boolean xuanlanBeamBusy() {
        return this.xuanlanBeamLock > 0 || this.xuanlanBeamCharge > 0 || this.xuanlanBeamFire > 0;
    }

    private int xuanlanBeamPreFireTicks() {
        return this.getChallengeDifficulty().acceleratedTicks(20) + this.getChallengeDifficulty().acceleratedTicks(20);
    }

    private boolean xuanlanBeamTick(ServerLevel serverLevel) {
        LivingEntity livingEntity;
        LivingEntity livingEntity2 = this.m_5448_();
        if (this.xuanlanBeamBusy() && (livingEntity2 == null || !livingEntity2.m_6084_() || livingEntity2 instanceof LivingEntity && !this.combatPlayer(livingEntity = livingEntity2))) {
            this.cancelSkillWindups();
            this.xuanlanBeamCd = 20;
            return false;
        }
        double d = this.m_20185_();
        double d2 = this.m_20188_();
        double d3 = this.m_20189_();
        if (this.xuanlanBeamFire > 0) {
            this.m_21573_().m_26573_();
            this.xuanlanBeamFx(serverLevel, d, d2, d3);
            if (--this.xuanlanBeamPulse <= 0) {
                this.xuanlanBeamPulse = 10;
                this.xuanlanBeamPunish(serverLevel, d, d2, d3);
            }
            if (--this.xuanlanBeamFire <= 0) {
                this.releaseSkillCue(new Vec3(d, d2, d3));
                serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11738_, SoundSource.HOSTILE, 1.6f, 0.6f);
                this.xuanlanBeamCd = 320;
            }
            return true;
        }
        if (this.xuanlanBeamCharge > 0) {
            this.m_21573_().m_26573_();
            this.xuanlanBeamFx(serverLevel, d, d2, d3);
            if (this.xuanlanBeamCharge % 5 == 0) {
                double d4 = 1.0 - (double)this.xuanlanBeamCharge / (double)Math.max(1, this.getChallengeDifficulty().acceleratedTicks(20));
                serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11739_, SoundSource.HOSTILE, 1.1f, 0.7f + (float)d4 * 0.7f);
            }
            if (--this.xuanlanBeamCharge <= 0) {
                this.xuanlanBeamFire = 40;
                this.xuanlanBeamPulse = 10;
                serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11736_, SoundSource.HOSTILE, 1.6f, 1.4f);
                this.tellNearby(serverLevel, "\u00a7b\u00a7l\u3010\u8d85\u80fd\u5149\u675f\u3011\u00a7r\u00a7c\u53d1\u5c04\uff01\u00a77(\u88ab\u7167\u5230\u4f1a\u7729\u6655 + \u6e05\u7a7a\u589e\u76ca)");
            }
            return true;
        }
        if (this.xuanlanBeamLock > 0) {
            this.m_21573_().m_26573_();
            this.xuanlanAimAtTarget();
            this.xuanlanBeamFx(serverLevel, d, d2, d3);
            if (this.xuanlanBeamLock % 6 == 0) {
                serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11739_, SoundSource.HOSTILE, 0.7f, 0.6f);
            }
            if (--this.xuanlanBeamLock <= 0) {
                this.xuanlanBeamCharge = this.getChallengeDifficulty().acceleratedTicks(20);
                this.tellNearby(serverLevel, "\u00a7b\u3010\u8d85\u80fd\u5149\u675f\u3011\u00a77\u9501\u5b9a\u5b8c\u6210 \u2014\u2014 \u00a7c\u65b9\u5411\u56fa\u5b9a, \u4fa7\u79fb\u8eb2\u5f00\uff01\u00a77(" + (double)this.xuanlanBeamCharge / 20.0 + " \u79d2\u540e\u53d1\u5c04)");
                serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_12376_, SoundSource.HOSTILE, 1.4f, 0.8f);
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u6587\u7384\u84dd\u300c\u8d85\u80fd\u5149\u675f\u300d\u9501\u5b9a {} tick \u5b8c\u6210, \u524d\u6447 {} tick(\u65b9\u5411\u5df2\u51bb\u7ed3, \u4e0d\u518d\u8ddf\u5934)", (Object)this.getChallengeDifficulty().acceleratedTicks(20), (Object)this.xuanlanBeamCharge);
            }
            return true;
        }
        if (this.xuanlanBeamCd > 0) {
            --this.xuanlanBeamCd;
        }
        if (this.xuanlanBeamCd <= 0 && !this.skillCommitted() && this.actionTicks <= 0 && this.castTicks <= 0) {
            LivingEntity livingEntity3 = this.m_5448_();
            if (livingEntity3 != null && livingEntity3.m_6084_()) {
                this.xuanlanAimAtTarget();
                this.xuanlanBeamLock = this.getChallengeDifficulty().acceleratedTicks(20);
                this.xlBeamPunished.clear();
                this.playCast(this.xuanlanBeamPreFireTicks() + 40, 1);
                BodySignals.beam(this);
                this.xuanlanBeamFx(serverLevel, d, d2, d3);
                this.tellNearby(serverLevel, "\u00a7b\u3010\u8d85\u80fd\u5149\u675f\u3011\u00a77\u6587\u7384\u84dd\u5f00\u59cb\u9501\u5b9a\u4f60 \u2014\u2014 \u00a7c\u5148\u522b\u6025, \u9501\u5b9a\u7ed3\u675f\u540e\u8fd8\u6709\u524d\u6447, \u90a3\u65f6\u4fa7\u79fb\uff01");
                serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_12376_, SoundSource.HOSTILE, 1.0f, 0.6f);
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u6587\u7384\u84dd\u300c\u8d85\u80fd\u5149\u675f\u300d\u5f00\u59cb\u9501\u5b9a {} tick -> \u524d\u6447 {} tick -> \u5f15\u5bfc {} tick(\u65e0\u4f24\u5bb3; \u547d\u4e2d {} \u79d2\u7729\u6655 + \u6e05\u7a7a\u589e\u76ca; \u524d\u6447\u4e0e\u5f15\u5bfc\u671f\u95f4\u4e0d\u8ddf\u5934)", new Object[]{this.xuanlanBeamLock, this.getChallengeDifficulty().acceleratedTicks(20), 40, 3});
                return true;
            }
            this.xuanlanBeamCd = 20;
        }
        return false;
    }

    private void xuanlanBeamFx(ServerLevel serverLevel, double d, double d2, double d3) {
        Vec3 vec3 = new Vec3(d, d2, d3);
        Vec3 vec32 = this.clipSkillRay(vec3, vec3.m_82520_(this.xlBeamDx * 80.0, this.xlBeamDy * 80.0, this.xlBeamDz * 80.0));
        boolean bl = this.xuanlanBeamFire > 0;
        int n = bl ? 40 : this.xuanlanBeamPreFireTicks();
        this.syncBeamCue(vec3, vec32, 4.0, n, bl, false);
    }

    private void xuanlanBeamPunish(ServerLevel serverLevel, double d, double d2, double d3) {
        Vec3 vec3 = new Vec3(d, d2, d3);
        Vec3 vec32 = this.clipSkillRay(vec3, vec3.m_82520_(this.xlBeamDx * 80.0, this.xlBeamDy * 80.0, this.xlBeamDz * 80.0));
        double d4 = vec3.m_82554_(vec32);
        int n = 0;
        for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this, XuanlanLengths.segmentBounds(vec3, vec32, 4.0))) {
            double d5;
            if (!this.combatPlayer(livingEntity) || this.xlBeamPunished.contains(livingEntity.m_20148_()) || !this.m_142582_((Entity)livingEntity)) continue;
            Vec3 vec33 = livingEntity.m_20182_().m_82520_(0.0, (double)livingEntity.m_20206_() * 0.5, 0.0).m_82492_(d, d2, d3);
            double d6 = vec33.f_82479_ * this.xlBeamDx + vec33.f_82480_ * this.xlBeamDy + vec33.f_82481_ * this.xlBeamDz;
            if (d6 < 0.0 || d6 > d4 || (d5 = vec33.m_82492_(this.xlBeamDx * d6, this.xlBeamDy * d6, this.xlBeamDz * d6).m_82553_()) > 4.0 + (double)livingEntity.m_20205_() * 0.5) continue;
            this.xlBeamPunished.add(livingEntity.m_20148_());
            this.stunPlayerWithLaunch(livingEntity, 60, "\u8d85\u80fd\u5149\u675f", this.xlBeamDx * 1.5, 0.35, this.xlBeamDz * 1.5);
            int n2 = PlayerBuffRules.clearBuffs(livingEntity);
            Component.m_237113_((String)("\u00a77\u4f60\u7684\u589e\u76ca\u88ab\u3010\u8d85\u80fd\u5149\u675f\u3011\u6e05\u7a7a " + n2 + " \u4e2a \u2014\u2014 \u00a7c\u7729\u6655\u7ed3\u675f\u540e\u4f1a\u88ab\u51fb\u98de"));
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_175830_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.2, livingEntity.m_20189_(), 18, 0.35, 0.6, 0.35, 0.06);
            ++n;
        }
        serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_215771_, SoundSource.HOSTILE, 1.2f, 1.3f);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_235902_, d + this.xlBeamDx * 3.0, d2 + this.xlBeamDy * 3.0, d3 + this.xlBeamDz * 3.0, 1, 0.0, 0.0, 0.0, 0.0);
        if (n > 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u6587\u7384\u84dd\u300c\u8d85\u80fd\u5149\u675f\u300d\u8109\u51b2\u547d\u4e2d {} \u4eba: \u7729\u6655 {} \u79d2 + \u6e05\u7a7a\u589e\u76ca(\u672c\u62db\u5df2\u65e0\u4f24\u5bb3)", (Object)n, (Object)3);
        }
    }

    private boolean canSpeakXueliDialogue() {
        return this.getVariant() == 10 && !this.isMini() && !this.isGiant() && this.m_6084_() && !this.m_21525_() && !ManorFight.isFinaleMember((LivingEntity)this);
    }

    private void tickXueliDialogue() {
        ServerLevel serverLevel;
        Level level;
        block7: {
            block6: {
                level = this.m_9236_();
                if (!(level instanceof ServerLevel)) break block6;
                serverLevel = (ServerLevel)level;
                if (this.canSpeakXueliDialogue()) break block7;
            }
            return;
        }
        LivingEntity livingEntity = this.m_5448_();
        if (!(livingEntity instanceof Player) || !(level = (Player)livingEntity).m_6084_() || level.m_7500_() || level.m_5833_() || level.m_9236_() != serverLevel || this.m_20280_((Entity)level) > 4096.0) {
            return;
        }
        long l = serverLevel.m_46467_();
        String string = this.xueliDialogue.nextStage(l, this.m_21223_() / Math.max(1.0f, this.m_21233_()));
        if (string == null && this.xueliDialogue.randomReady(l) && this.actionTicks <= 0 && this.castTicks <= 0 && !this.skillCommitted() && this.xueliRollTicks <= 0 && this.xueliSlamLeft <= 0) {
            string = this.xueliDialogue.randomLine(this.f_19796_.m_188503_(this.xueliDialogue.randomChoiceCount()));
            int n = 400;
            int n2 = 700;
            this.xueliDialogue.scheduleRandom(l, n + this.f_19796_.m_188503_(n2 - n + 1));
        }
        if (string != null) {
            this.sayXueliDialogue(string);
        }
    }

    private void sayXueliDialogue(String string) {
        ServerLevel serverLevel;
        block3: {
            block2: {
                Level level = this.m_9236_();
                if (!(level instanceof ServerLevel)) break block2;
                serverLevel = (ServerLevel)level;
                if (this.canSpeakXueliDialogue()) break block3;
            }
            return;
        }
        this.tellNearby(serverLevel, "\u00a7b\u96ea\u9ece\uff1a\u00a7f" + string);
        this.xueliDialogue.deferRandom(serverLevel.m_46467_());
    }

    public void onXueliShieldActivated() {
        this.sayXueliDialogue("\u6728\u5927\u6728\u5927\u6728\u5927\u6728\u5927\u6728\u5927\uff01");
        CastServer.nativeAction(this, "maid_call", "instant", 12);
    }

    private void tellNearby(ServerLevel serverLevel, String string) {
        MutableComponent mutableComponent = Component.m_237113_((String)string);
        for (Player player : serverLevel.m_6907_()) {
            if (player.m_20280_((Entity)this) > 4096.0) continue;
            player.m_5661_((Component)mutableComponent, true);
        }
    }

    private void huntStrayPlayer(ServerLevel serverLevel) {
        ServerPlayer serverPlayer;
        if (ManorFinaleCombat.manages((LivingEntity)this)) {
            ManorFinaleCombat.huntTarget(this, serverLevel);
            return;
        }
        if (this.huntTeleportCd > 0) {
            --this.huntTeleportCd;
        }
        if ((serverPlayer = ManorCombatContinuity.fightPlayer(this)) == null) {
            return;
        }
        double d = serverPlayer.m_20280_((Entity)this);
        if (d >= 25600.0) {
            return;
        }
        double d2 = Math.sqrt(d);
        if (d2 <= 20.0) {
            return;
        }
        LivingEntity livingEntity = this.m_5448_();
        if (livingEntity == null || !livingEntity.m_6084_() || livingEntity != serverPlayer) {
            this.m_6710_((LivingEntity)serverPlayer);
        }
        this.m_21563_().m_24960_((Entity)serverPlayer, 30.0f, 30.0f);
        this.m_21573_().m_5624_((Entity)serverPlayer, 1.6);
        if (d2 > 72.0 && this.huntTeleportCd <= 0) {
            if (ManorFight.isManaged((LivingEntity)this)) {
                return;
            }
            this.huntTeleportCd = 120;
            double d3 = this.f_19796_.m_188500_() * Math.PI * 2.0;
            double d4 = serverPlayer.m_20185_() + Math.cos(d3) * 3.0;
            double d5 = serverPlayer.m_20189_() + Math.sin(d3) * 3.0;
            double d6 = this.m_20185_();
            double d7 = this.m_20186_();
            double d8 = this.m_20189_();
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, d6, d7 + 1.0, d8, 30, 0.4, 0.7, 0.4, 0.3);
            Vec3 vec3 = ManorSafeTeleport.find(serverLevel, (Entity)this, new Vec3(d4, serverPlayer.m_20186_(), d5), 8);
            if (vec3 == null) {
                return;
            }
            ManorSafeTeleport.move(serverLevel, (Entity)this, vec3);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 30, 0.4, 0.7, 0.4, 0.3);
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11852_, SoundSource.HOSTILE, 1.2f, 0.9f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] {} \u79bb\u73a9\u5bb6 {} \u683c -> \u4e3b\u52a8\u95ea\u73b0\u8ffd\u51fb", (Object)NAMES[this.getVariant()], (Object)((int)d2));
        }
    }

    public float qingyiDamageBonus() {
        return this.getVariant() == 5 && !this.isMini() ? (float)((double)this.qingyiTridentStacks * 1.0) : 0.0f;
    }

    public int qingyiTridentStacks() {
        return this.qingyiTridentStacks;
    }

    private void applyQingyiFlatBuffs() {
        AttributeInstance attributeInstance;
        boolean bl = this.getVariant() == 5 && !this.isMini();
        AttributeInstance attributeInstance2 = this.m_21051_(Attributes.f_22281_);
        if (attributeInstance2 != null) {
            attributeInstance2.m_22120_(QINGYI_FLAT_ATK_MOD);
            if (bl) {
                attributeInstance2.m_22118_(new AttributeModifier(QINGYI_FLAT_ATK_MOD, "ftboss.qingyi_atk", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        if ((attributeInstance = this.m_21051_(Attributes.f_22284_)) != null) {
            attributeInstance.m_22120_(QINGYI_FLAT_ARMOR_MOD);
            if (bl) {
                attributeInstance.m_22118_(new AttributeModifier(QINGYI_FLAT_ARMOR_MOD, "ftboss.qingyi_armor", 8.0, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    private void syncQingyiTridentBuffs() {
        AttributeInstance attributeInstance;
        AttributeInstance attributeInstance2;
        int n = this.getVariant() == 5 && !this.isMini() ? this.qingyiTridentStacks : 0;
        int n2 = ManorRefinementRules.armorSteps(n, 20);
        int n3 = Math.min(n, 20);
        AttributeInstance attributeInstance3 = this.m_21051_(Attributes.f_22281_);
        if (attributeInstance3 != null) {
            attributeInstance3.m_22120_(QINGYI_ATK_MOD);
            if (n > 0) {
                attributeInstance3.m_22118_(new AttributeModifier(QINGYI_ATK_MOD, "ftboss.qingyi_trident_atk", (double)n * 1.0, AttributeModifier.Operation.ADDITION));
            }
        }
        if ((attributeInstance2 = this.m_21051_(Attributes.f_22284_)) != null) {
            attributeInstance2.m_22120_(QINGYI_ARMOR_MOD);
            if (n2 > 0) {
                attributeInstance2.m_22118_(new AttributeModifier(QINGYI_ARMOR_MOD, "ftboss.qingyi_trident_armor", (double)n2 * 1.0, AttributeModifier.Operation.ADDITION));
            }
        }
        if ((attributeInstance = this.m_21051_(Attributes.f_22276_)) != null) {
            attributeInstance.m_22120_(QINGYI_HEALTH_MOD);
            if (n3 > 0) {
                attributeInstance.m_22118_(new AttributeModifier(QINGYI_HEALTH_MOD, "ftboss.qingyi_trident_health", (double)n3 * 1.0, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    private void onQingyiTridentSummoned() {
        int n;
        int n2;
        int n3;
        if (this.getVariant() != 5 || this.isMini()) {
            return;
        }
        if (this.qingyiTridentStacks < 200) {
            ++this.qingyiTridentStacks;
            this.syncQingyiTridentBuffs();
            if (this.m_21223_() < this.m_21233_()) {
                this.m_21153_(Math.min(this.m_21233_(), this.m_21223_() + 1.0f));
            }
        }
        int n4 = Math.min(4, Math.max(0, this.qingyiTridentStacks - 1));
        MobEffect mobEffect = (MobEffect)FTEffects.QINGYI_CHARGE.get();
        if (mobEffect != null) {
            this.m_7292_(new MobEffectInstance(mobEffect, 600, n4, false, true, true));
        }
        int[] nArray = new int[CHAIN_BUFF_POOL.length];
        for (n3 = 0; n3 < nArray.length; ++n3) {
            nArray[n3] = n3;
        }
        for (n3 = nArray.length - 1; n3 > 0; --n3) {
            n2 = this.f_19796_.m_188503_(n3 + 1);
            n = nArray[n3];
            nArray[n3] = nArray[n2];
            nArray[n2] = n;
        }
        StringBuilder stringBuilder = new StringBuilder();
        n2 = Math.min(6, nArray.length);
        for (n = 0; n < n2; ++n) {
            MobEffect mobEffect2 = CHAIN_BUFF_POOL[nArray[n]];
            this.m_7292_(new MobEffectInstance(mobEffect2, 600, this.buffAmp(1), true, true, true));
            stringBuilder.append(mobEffect2.m_19482_().getString()).append(' ');
        }
        if (--this.qingyiOnSummonLogCd <= 0) {
            this.qingyiOnSummonLogCd = 20;
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u9752\u8863\u53ec\u5524\u4e09\u53c9\u621f -> \u5c42\u6570 {}/{} (\u5168\u90e8\u4f24\u5bb3 +{}, \u62a4\u7532 +{}, \u8840\u91cf\u4e0a\u9650 +{}) | \u5145\u80fd {} \u7ea7 + {} \u4e2a\u589e\u76ca {} \u79d2: {}", new Object[]{this.qingyiTridentStacks, 200, (int)((double)this.qingyiTridentStacks * 1.0), (int)((double)ManorRefinementRules.armorSteps(this.qingyiTridentStacks, 20) * 1.0), (int)((double)Math.min(this.qingyiTridentStacks, 20) * 1.0), n4 + 1, n2, 30, stringBuilder.toString().trim()});
        }
    }

    private void qingyiLightningTick(ServerLevel serverLevel) {
        Object object;
        if (this.qingyiSpellCd > 0) {
            --this.qingyiSpellCd;
        }
        if (this.qingyiSpellCd > 0) {
            return;
        }
        LivingEntity livingEntity = this.m_5448_();
        if (livingEntity instanceof Player && !BossTargets.valid(object = (Player)livingEntity)) {
            this.m_6710_(null);
            return;
        }
        if (!this.allowsParallelMelee() || this.m_21525_() || !this.m_6084_() || this.m_20147_() || ManorFight.isFinaleMember((LivingEntity)this) || livingEntity == null || !livingEntity.m_6084_() || livingEntity.m_9236_() != serverLevel || this.m_7307_((Entity)livingEntity) || livingEntity.m_7307_((Entity)this) || this.m_20280_((Entity)livingEntity) > 1024.0 || !this.m_142582_((Entity)livingEntity)) {
            return;
        }
        this.qingyiSpellCd = 80;
        object = QINGYI_LIGHTNING_SPELLS[this.f_19796_.m_188503_(QINGYI_LIGHTNING_SPELLS.length)];
        int n = 5 + this.f_19796_.m_188503_(3);
        this.playParallelSpell(HiddenBossCastRules.quicker(40));
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        boolean bl = IronSpellBridge.cast(serverLevel, (LivingEntity)this, (String)object, n);
        if (!bl) {
            LightningBolt lightningBolt;
            try {
                lightningBolt = (LightningBolt)EntityType.f_20465_.m_20615_((Level)serverLevel);
                if (lightningBolt != null) {
                    lightningBolt.m_6027_(livingEntity.m_20185_(), livingEntity.m_20186_(), livingEntity.m_20189_());
                    lightningBolt.m_20874_(false);
                    ManorFightMembers.inherit((Entity)this, (Entity)lightningBolt);
                    serverLevel.m_7967_((Entity)lightningBolt);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (livingEntity instanceof Player) {
                lightningBolt = (Player)livingEntity;
                lightningBolt.f_19802_ = 0;
                lightningBolt.m_6469_(this.m_269291_().m_269104_((Entity)this, (Entity)this), this.manorSkillDamage((LivingEntity)lightningBolt, 64.8f + this.qingyiDamageBonus(), 1.75));
                lightningBolt.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 60, 2));
                lightningBolt.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 60, 1));
            } else {
                livingEntity.m_6469_(this.m_269291_().m_269104_((Entity)this, (Entity)this), this.manorSkillDamage(livingEntity, 64.8f + this.qingyiDamageBonus(), 1.75));
            }
        }
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12090_, SoundSource.HOSTILE, 1.6f, 1.2f);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_175830_, this.m_20185_(), this.m_20186_() + 1.6, this.m_20189_(), 30, 0.6, 0.6, 0.6, 0.25);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u9752\u8863\u300c\u94c1\u9b54\u6cd5\u95ea\u7535\u300d{} lv{} \u76ee\u6807={} \u771f\u65bd\u6cd5={}", new Object[]{object, n, livingEntity.m_7755_().getString(), bl});
    }

    private void pearlTick() {
        Player player;
        if (ManorLivingTargets.airbornePearl(this)) {
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (this.pearlRushCd > 0) {
            --this.pearlRushCd;
        }
        if (!this.canTeleportWith((LivingEntity)(level = this.m_5448_())) || level instanceof Player && (player = (Player)level).m_7500_()) {
            this.meleeLockTicks = 0;
            return;
        }
        if (this.m_20280_((Entity)level) <= 16.0) {
            this.meleeLockTicks = 0;
            return;
        }
        if (ManorFight.isManaged((LivingEntity)this)) {
            int n;
            int n2 = n = this.getVariant() == 1 ? 200 : 300;
            if (this.playerHitIdleTicks < n) {
                this.meleeLockTicks = 0;
                return;
            }
            if (this.pearlRushCd > 0) {
                return;
            }
        } else {
            int n = this.getVariant() == 1 ? 10 : 20;
            this.meleeLockTicks = Math.min(n, this.meleeLockTicks + 1);
            if (this.meleeLockTicks < n) {
                return;
            }
        }
        if (this.skillCommitted() || this.movementLocked()) {
            return;
        }
        this.meleeLockTicks = 0;
        this.pearlRushCd = this.getVariant() == 1 ? 120 : 200;
        this.pearlRush(serverLevel, (LivingEntity)level);
    }

    public void onPlayerHitLanded() {
        this.playerHitIdleTicks = 0;
    }

    public int playerHitIdleTicks() {
        return this.playerHitIdleTicks;
    }

    private void manorFlyBanTick() {
    }

    private void haleyFlightTick() {
        if (this.haleyFlying) {
            this.haleyFlying = false;
            this.m_20242_(false);
            this.f_21342_ = new MoveControl((Mob)this);
            Vec3 vec3 = this.m_20184_();
            this.m_20334_(vec3.f_82479_, Math.min(0.0, vec3.f_82480_), vec3.f_82481_);
            this.f_19864_ = true;
        }
    }

    private boolean hasHaleyGlide() {
        return false;
    }

    private void pearlRush(ServerLevel serverLevel, LivingEntity livingEntity) {
        if (!this.canTeleportWith(livingEntity)) {
            return;
        }
        try {
            EasterPearlEntity easterPearlEntity = new EasterPearlEntity((Level)serverLevel, (LivingEntity)this);
            easterPearlEntity.m_6034_(this.m_20185_(), this.m_20188_() - 0.2, this.m_20189_());
            double d = livingEntity.m_20185_() - this.m_20185_();
            double d2 = livingEntity.m_20188_() - this.m_20188_();
            double d3 = livingEntity.m_20189_() - this.m_20189_();
            double d4 = Math.max(0.1, Math.sqrt(d * d + d2 * d2 + d3 * d3));
            easterPearlEntity.m_6686_(d / d4, d2 / d4 + 0.12, d3 / d4, 1.5f, 0.0f);
            ManorFightMembers.inherit((Entity)this, (Entity)easterPearlEntity);
            serverLevel.m_7967_((Entity)easterPearlEntity);
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u6295\u672b\u5f71\u73cd\u73e0\u5931\u8d25: {}", (Object)throwable.toString());
        }
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11857_, SoundSource.HOSTILE, 1.2f, 0.8f);
        double d = this.f_19796_.m_188500_() * Math.PI * 2.0;
        double d5 = livingEntity.m_20185_() + Math.cos(d) * 2.2;
        double d6 = livingEntity.m_20189_() + Math.sin(d) * 2.2;
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 30, 0.4, 0.7, 0.4, 0.3);
        LightHooks.teleport(this, d5, livingEntity.m_20186_(), d6, true);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 30, 0.4, 0.7, 0.4, 0.3);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11852_, SoundSource.HOSTILE, 1.3f, 1.0f);
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] {} \u8ffd\u51fb\u8ddd\u79bb\u8d85\u8fc7 4 \u683c -> \u672b\u5f71\u73cd\u73e0\u77ac\u79fb\u5230\u8eab\u8fb9", (Object)NAMES[this.getVariant()]);
    }

    private void purgeTick() {
        Level level2;
        boolean bl = false;
        for (Object object : this.m_21220_()) {
            if (object.m_19544_() == null || object.m_19544_().m_19486_()) continue;
            bl = true;
            break;
        }
        if (!bl) {
            this.purgeTimer = 0;
            return;
        }
        if (this.purgeTimer <= 0) {
            this.purgeTimer = 1200;
            return;
        }
        if (--this.purgeTimer > 0) {
            return;
        }
        int n = 0;
        for (Level level2 : new ArrayList(this.m_21220_())) {
            if (level2.m_19544_() == null || level2.m_19544_().m_19486_()) continue;
            this.m_21195_(level2.m_19544_());
            ++n;
        }
        level2 = this.m_9236_();
        if (level2 instanceof ServerLevel) {
            Object object;
            object = (ServerLevel)level2;
            object.m_8767_((ParticleOptions)ParticleTypes.f_123767_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 40, 0.6, 0.8, 0.6, 0.25);
            object.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11736_, SoundSource.HOSTILE, 1.0f, 1.4f);
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] {} \u4e2d\u51cf\u76ca 2 \u79d2 -> \u51c0\u5316\u81ea\u8eab {} \u4e2a\u51cf\u76ca(\u589e\u76ca\u4fdd\u7559)", (Object)NAMES[this.getVariant()], (Object)n);
    }

    private void foodBuffTick() {
        ManorBalanceHooks.food(this);
    }

    static MobEffect[] foodBuffPool() {
        if (foodBuffPoolCache != null) {
            return foodBuffPoolCache;
        }
        ArrayList<MobEffect> arrayList = new ArrayList<MobEffect>(List.of(MobEffects.f_19618_, MobEffects.f_19605_, MobEffects.f_19617_, MobEffects.f_19600_, MobEffects.f_19596_, MobEffects.f_19606_, MobEffects.f_19598_, MobEffects.f_19603_, MobEffects.f_19616_, MobEffects.f_19607_, MobEffects.f_19611_, MobEffects.f_19608_, MobEffects.f_19592_, MobEffects.f_19593_, MobEffects.f_19591_, MobEffects.f_19621_));
        String[][] stringArrayArray = new String[][]{{"vinery", "armor_effect"}, {"vinery", "health_effect"}, {"farmersdelight", "comfort"}, {"farmersdelight", "nourishment"}, {"farm_and_charm", "satiation"}, {"kaleidoscope_cookery", "satiated_shield"}};
        int n = 0;
        for (String[] stringArray : stringArrayArray) {
            MobEffect mobEffect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(stringArray[0], stringArray[1]));
            if (mobEffect == null || !mobEffect.m_19486_() || arrayList.contains(mobEffect)) continue;
            arrayList.add(mobEffect);
            ++n;
        }
        int n2 = 0;
        Iterator iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            if (!LemonImmuneBuffRules.isForbidden((MobEffect)iterator.next())) continue;
            iterator.remove();
            ++n2;
        }
        foodBuffPoolCache = arrayList.toArray(new MobEffect[0]);
        if (!foodBuffPoolLogged) {
            foodBuffPoolLogged = true;
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u67e0\u6aac\u732b\u98df\u7269\u589e\u76ca\u6c60\u5c31\u7eea: {} \u79cd(\u5176\u4e2d\u6574\u5408\u5305\u9152/\u98df\u7269\u7cfb {} \u79cd, \u5df2\u5254\u9664\u6253\u4e0d\u6b7b\u7c7b {} \u79cd)", new Object[]{foodBuffPoolCache.length, n, n2});
        }
        return foodBuffPoolCache;
    }

    private void skillQingyi(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.playImpactAction(20);
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        this.throwQingyiTrident(serverLevel, livingEntity);
        this.applyQingyiTridentEffect(serverLevel, livingEntity);
        Vec3 vec3 = livingEntity.m_20182_().m_82546_(this.m_20182_());
        vec3 = new Vec3(vec3.f_82479_, 0.0, vec3.f_82481_);
        this.riptideDir = vec3.m_82556_() < 1.0E-4 ? Vec3.f_82478_ : vec3.m_82541_();
        this.riptideTicks = 12;
        net.farmingtales.ftboss.qingyilight.LightHooks.dash(this, this.riptideDir, this.riptideTicks);
        this.riptideHitCd = 0;
        this.potionDelay = 0;
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12519_, SoundSource.HOSTILE, 1.4f, 1.0f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u9752\u8863\u300c\u6295\u621f + \u6fc0\u6d41\u51b2\u523a\u300d\u5f00\u59cb(\u5f53\u524d\u6b66\u5668={})", (Object)this.m_21205_());
    }

    private void qingyiTick() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            this.riptideTicks = 0;
            this.potionDelay = 0;
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (this.riptideTicks > 0) {
            LivingEntity livingEntity;
            --this.riptideTicks;
            level = this.riptideDir;
            this.m_20334_(level.f_82479_ * 1.15, 0.1, level.f_82481_ * 1.15);
            this.f_19812_ = true;
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123769_, this.m_20185_(), this.m_20186_() + 0.6, this.m_20189_(), 12, 0.4, 0.3, 0.4, 0.2);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123797_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 8, 0.3, 0.3, 0.3, 0.1);
            if (this.riptideHitCd > 0) {
                --this.riptideHitCd;
            }
            if (this.riptideTicks % 6 == 0 && this.canTeleportWith(livingEntity = this.m_5448_())) {
                double d = this.f_19796_.m_188500_() * Math.PI * 2.0;
                double d2 = livingEntity.m_20185_() + Math.cos(d) * 2.2;
                double d3 = livingEntity.m_20189_() + Math.sin(d) * 2.2;
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 20, 0.3, 0.5, 0.3, 0.25);
                net.farmingtales.ftboss.qingyilight.LightHooks.teleport(this, d2, livingEntity.m_20186_(), d3, true);
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 20, 0.3, 0.5, 0.3, 0.25);
                serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11852_, SoundSource.HOSTILE, 0.9f, 1.3f);
            }
            if (this.riptideHitCd <= 0) {
                int n = 0;
                for (LivingEntity livingEntity2 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
                    if (!this.combatPlayer(livingEntity2) || livingEntity2.m_20280_((Entity)this) > 14.0625) continue;
                    livingEntity2.f_19802_ = 0;
                    if (!livingEntity2.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), this.manorSkillDamage(livingEntity2, 18.0f + this.qingyiDamageBonus(), 1.5))) continue;
                    BossKnockback.push((Entity)livingEntity2, level.f_82479_ * 1.45, 0.95, level.f_82481_ * 1.45);
                    livingEntity2.f_19864_ = true;
                    ++n;
                }
                if (n > 0) {
                    this.riptideHitCd = 5;
                }
            }
            if (this.riptideTicks == 0) {
                this.potionDelay = 6;
            }
            return;
        }
        if (this.potionDelay > 0 && --this.potionDelay <= 0 && (level = this.m_5448_()) != null && level.m_6084_()) {
            this.throwDebuffPotion(serverLevel, (LivingEntity)level);
        }
    }

    private void throwQingyiTrident(ServerLevel serverLevel, LivingEntity livingEntity) {
        try {
            ItemStack itemStack = this.qingyiStellarisStack();
            if (itemStack.m_41619_()) {
                itemStack = new ItemStack((ItemLike)Items.f_42713_);
            }
            QingyiTridentEntity qingyiTridentEntity = new QingyiTridentEntity((Level)serverLevel, (LivingEntity)this, itemStack, this.qingyiIndex);
            qingyiTridentEntity.m_6034_(this.m_20185_(), this.m_20188_() - 0.15, this.m_20189_());
            double d = livingEntity.m_20185_() - this.m_20185_();
            double d2 = livingEntity.m_20188_() - this.m_20188_();
            double d3 = livingEntity.m_20189_() - this.m_20189_();
            double d4 = Math.max(0.1, Math.sqrt(d * d + d2 * d2 + d3 * d3));
            qingyiTridentEntity.m_6686_(d / d4, d2 / d4 + 0.05, d3 / d4, 3.0f, 0.05f);
            ManorFightMembers.inherit((Entity)this, (Entity)qingyiTridentEntity);
            if (!net.farmingtales.ftboss.qingyilight.LightHooks.spawnTrident(serverLevel, this, qingyiTridentEntity)) {
                return;
            }
            this.onQingyiTridentSummoned();
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12520_, SoundSource.HOSTILE, 1.3f, 0.9f);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123808_, this.m_20185_(), this.m_20188_(), this.m_20189_(), 14, 0.3, 0.3, 0.3, 0.1);
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u9752\u8863\u6295\u63b7\u4e09\u53c9\u621f\u5931\u8d25: {}", (Object)throwable.toString());
        }
    }

    private ItemStack qingyiStellarisStack() {
        try {
            Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(QINGYI_WEAPONS[3]));
            if (item != null) {
                return new ItemStack((ItemLike)item);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return this.m_21205_().m_41777_();
    }

    private void applyQingyiTridentEffect(ServerLevel serverLevel, LivingEntity livingEntity) {
        try {
            switch (this.qingyiIndex) {
                case 0: {
                    LightningBolt lightningBolt = (LightningBolt)EntityType.f_20465_.m_20615_((Level)serverLevel);
                    if (lightningBolt != null) {
                        lightningBolt.m_6027_(livingEntity.m_20185_(), livingEntity.m_20186_(), livingEntity.m_20189_());
                        lightningBolt.m_20874_(true);
                        ManorFightMembers.inherit((Entity)this, (Entity)lightningBolt);
                        serverLevel.m_7967_((Entity)lightningBolt);
                    }
                    livingEntity.m_6469_(this.m_269291_().m_269104_((Entity)this, (Entity)this), this.manorSkillDamage(livingEntity, 45.0f + this.qingyiDamageBonus(), 1.5));
                    livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 300, 1));
                    break;
                }
                case 1: {
                    livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19615_, 300, 2));
                    livingEntity.m_6469_(this.m_269291_().m_269104_((Entity)this, (Entity)this), this.manorSkillDamage(livingEntity, 36.0f + this.qingyiDamageBonus(), 1.5));
                    break;
                }
                case 2: {
                    livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19599_, 300, 1));
                    BossKnockback.push((Entity)livingEntity, 0.0, 0.9, 0.0);
                    break;
                }
                case 3: {
                    livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19619_, 300, 0));
                    livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 300, 2));
                    break;
                }
            }
            serverLevel.m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_(), livingEntity.m_20189_(), SoundEvents.f_12521_, SoundSource.HOSTILE, 1.3f, 1.0f);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123808_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 24, 0.4, 0.6, 0.4, 0.2);
            net.farmingtales.ftboss.qingyilight.LightHooks.tridentHit(this, livingEntity, this.qingyiIndex);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u9752\u8863\u4e09\u53c9\u621f\u5fc5\u89e6\u53d1\u6548\u679c: \u7b2c {} \u4ef6 ({})", (Object)(this.qingyiIndex + 1), (Object)QINGYI_WEAPONS[this.qingyiIndex]);
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u4e09\u53c9\u621f\u6548\u679c\u5f02\u5e38: {}", (Object)throwable.toString());
        }
    }

    public void onTridentHit(LivingEntity livingEntity, int n) {
        Object object;
        net.farmingtales.ftboss.qingyilight.LightHooks.tridentHit(this, livingEntity, n);
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        float f = this.qingyiDamageBonus();
        f += (float)(this.m_21133_(Attributes.f_22281_) * 1.25);
        f = this.manorSkillDamage(livingEntity, f, 1.5);
        if (f > 0.0f) {
            livingEntity.f_19802_ = 0;
            livingEntity.m_6469_(this.m_269291_().m_269104_((Entity)this, (Entity)this), f);
        }
        if (n == 0) {
            try {
                object = (LightningBolt)EntityType.f_20465_.m_20615_((Level)serverLevel);
                if (object != null) {
                    object.m_6027_(livingEntity.m_20185_(), livingEntity.m_20186_(), livingEntity.m_20189_());
                    object.m_20874_(false);
                    ManorFightMembers.inherit((Entity)this, (Entity)object);
                    net.farmingtales.ftboss.qingyilight.LightHooks.directBolt(serverLevel, (Entity)object, this);
                }
                livingEntity.f_19802_ = 0;
                livingEntity.m_6469_(this.m_269291_().m_269104_((Entity)this, (Entity)this), 32.0f);
            }
            catch (Throwable throwable) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] \u53ec\u5524\u95ea\u7535\u5931\u8d25: {}", (Object)throwable.toString());
            }
        }
        ++this.qingyiTridentHits;
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u9752\u8863\u4e09\u53c9\u621f\u547d\u4e2d\u73a9\u5bb6(\u7b2c {} \u4ef6): \u8fde\u51fb {}/{}", new Object[]{n + 1, this.qingyiTridentHits, 5});
        if (this.qingyiTridentHits >= 5) {
            this.qingyiTridentHits = 0;
            net.farmingtales.ftboss.qingyilight.LightHooks.combo(this, livingEntity);
            object = new ArrayList<MobEffect>(Arrays.asList(RANDOM_DEBUFF_POOL));
            Collections.shuffle(object, new Random(this.f_19796_.m_188505_()));
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 4 && i < object.size(); ++i) {
                livingEntity.m_7292_(new MobEffectInstance((MobEffect)object.get(i), 300, 1));
                stringBuilder.append(((MobEffect)object.get(i)).m_19482_().getString()).append(' ');
            }
            ManorLivingTargets.tell(livingEntity, (Component)Component.m_237113_((String)("\u00a75\u3010\u661f\u621f\u8fde\u51fb\u3011\u00a77\u4f60\u88ab\u6302\u4e0a 4 \u4e2a\u51cf\u76ca: " + stringBuilder)), true);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_235898_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 30, 0.4, 0.6, 0.4, 0.05);
            serverLevel.m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_(), livingEntity.m_20189_(), SoundEvents.f_12557_, SoundSource.HOSTILE, 1.3f, 0.8f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u9752\u8863\u4e09\u53c9\u621f {} \u8fde\u51fb\u8fbe\u6210 -> {} \u83b7\u5f97 4 \u4e2a\u51cf\u76ca: {}", new Object[]{5, livingEntity.m_7755_().getString(), stringBuilder.toString().trim()});
        }
    }

    private void skillStarTridentRain(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.playCast(36, 2);
        QingyiCastSync.startRain(this);
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        this.qingyiRain = 12;
        this.qingyiRainTimer = 0;
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11862_, SoundSource.HOSTILE, 1.5f, 0.8f);
        serverLevel.m_6263_(null, livingEntity.m_20185_(), livingEntity.m_20186_(), livingEntity.m_20189_(), SoundEvents.f_12521_, SoundSource.HOSTILE, 1.2f, 1.4f);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, livingEntity.m_20185_(), livingEntity.m_20186_() + 10.0, livingEntity.m_20189_(), 60, 3.0, 1.0, 3.0, 0.15);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u9752\u8863\u300c\u6052\u661f\u4e09\u53c9\u621f\u96e8\u300d: \u7a7a\u4e2d\u53ec\u5524 {} \u628a\u6052\u661f\u4e09\u53c9\u621f -> {}", (Object)12, (Object)livingEntity.m_7755_().getString());
    }

    private void dropStarTrident(ServerLevel serverLevel, LivingEntity livingEntity) {
        try {
            ItemStack itemStack = ItemStack.f_41583_;
            Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation("tridents_n_stuff", "stellaris"));
            if (item != null) {
                itemStack = new ItemStack((ItemLike)item);
            }
            if (itemStack.m_41619_()) {
                itemStack = new ItemStack((ItemLike)Items.f_42713_);
            }
            QingyiTridentEntity qingyiTridentEntity = new QingyiTridentEntity((Level)serverLevel, (LivingEntity)this, itemStack, 3);
            double d = this.f_19796_.m_188500_() * Math.PI * 2.0;
            double d2 = 1.0 + this.f_19796_.m_188500_() * 4.5;
            double d3 = livingEntity.m_20185_() + Math.cos(d) * d2;
            double d4 = livingEntity.m_20189_() + Math.sin(d) * d2;
            double d5 = livingEntity.m_20186_() + 12.0;
            qingyiTridentEntity.m_6034_(d3, d5, d4);
            qingyiTridentEntity.m_6686_(livingEntity.m_20185_() - d3, livingEntity.m_20186_() + 0.6 - d5, livingEntity.m_20189_() - d4, 1.8f, 0.12f);
            ManorFightMembers.inherit((Entity)this, (Entity)qingyiTridentEntity);
            if (!net.farmingtales.ftboss.qingyilight.LightHooks.spawnTrident(serverLevel, this, qingyiTridentEntity)) {
                return;
            }
            this.onQingyiTridentSummoned();
            QingyiCastSync.rainPulse(this, this.qingyiRain);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, d3, d5, d4, 10, 0.2, 0.4, 0.2, 0.02);
            serverLevel.m_6263_(null, d3, d5, d4, SoundEvents.f_12520_, SoundSource.HOSTILE, 1.0f, 1.5f);
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u6052\u661f\u4e09\u53c9\u621f\u6295\u653e\u5931\u8d25: {}", (Object)throwable.toString());
            this.qingyiRain = 0;
        }
    }

    private void tickQingyiFallingTrident(ServerLevel serverLevel) {
        MultiplayerHooks.tick(this);
        LivingEntity livingEntity = this.m_5448_();
        if (!this.allowsParallelMelee() || this.m_21525_() || !this.m_6084_() || this.m_20147_() || ManorFight.isFinaleMember((LivingEntity)this) || !BossTargets.valid(livingEntity) || livingEntity.m_9236_() != serverLevel || this.m_7307_((Entity)livingEntity) || livingEntity.m_7307_((Entity)this) || this.m_20280_((Entity)livingEntity) > 1024.0 || !this.m_142582_((Entity)livingEntity)) {
            this.qingyiFallingTridentCooldown = 0;
            return;
        }
        if (this.qingyiFallingTridentCooldown > 0) {
            --this.qingyiFallingTridentCooldown;
        }
        if (this.qingyiFallingTridentCooldown > 0) {
            return;
        }
        this.dropStarTrident(serverLevel, livingEntity);
        this.qingyiFallingTridentCooldown = ManorBalanceHooks.tridentInterval(40, this, "passive");
    }

    private void throwDebuffPotion(ServerLevel serverLevel, LivingEntity livingEntity) {
        try {
            MobEffect mobEffect;
            MobEffect[] mobEffectArray = new MobEffect[]{MobEffects.f_19613_, MobEffects.f_19597_, MobEffects.f_19599_, MobEffects.f_19614_, MobEffects.f_19615_, MobEffects.f_19612_, MobEffects.f_19590_, MobEffects.f_19619_};
            ArrayList<MobEffect> arrayList = new ArrayList<MobEffect>(Arrays.asList(mobEffectArray));
            Collections.shuffle(arrayList, new Random(this.f_19796_.m_188505_()));
            ArrayList<MobEffectInstance> arrayList2 = new ArrayList<MobEffectInstance>();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 6 && i < arrayList.size(); ++i) {
                mobEffect = (MobEffect)arrayList.get(i);
                arrayList2.add(new MobEffectInstance(mobEffect, 120, 0));
                stringBuilder.append(mobEffect.m_19482_().getString()).append(' ');
            }
            ItemStack itemStack = new ItemStack((ItemLike)Items.f_42736_);
            PotionUtils.m_43552_((ItemStack)itemStack, arrayList2);
            itemStack.m_41714_((Component)Component.m_237113_((String)"\u00a75\u9752\u8863\u7684\u6df7\u6bd2\u836f\u5242"));
            mobEffect = new ThrownPotion((Level)serverLevel, (LivingEntity)this);
            mobEffect.m_37446_(itemStack);
            mobEffect.m_6034_(this.m_20185_(), this.m_20188_() - 0.15, this.m_20189_());
            double d = livingEntity.m_20185_() - this.m_20185_();
            double d2 = livingEntity.m_20188_() - this.m_20188_();
            double d3 = livingEntity.m_20189_() - this.m_20189_();
            double d4 = Math.max(0.1, Math.sqrt(d * d + d2 * d2 + d3 * d3));
            mobEffect.m_6686_(d / d4, d2 / d4 + 0.12, d3 / d4, 0.9f, 1.0f);
            ManorFightMembers.inherit((Entity)this, (Entity)mobEffect);
            net.farmingtales.ftboss.qingyilight.LightHooks.spawnPotion(serverLevel, (Entity)mobEffect);
            serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12437_, SoundSource.HOSTILE, 1.2f, 1.0f);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u9752\u8863\u6254\u51fa\u6df7\u6bd2\u836f\u5242: {}", (Object)stringBuilder.toString().trim());
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u9752\u8863\u6254\u836f\u6c34\u5931\u8d25: {}", (Object)throwable.toString());
        }
    }

    private boolean safeAddDebuff(LivingEntity livingEntity, MobEffect mobEffect, int n) {
        if (mobEffect == null || ZICAI_BLOCKED.contains(mobEffect)) {
            return false;
        }
        try {
            int n2 = ZicaiDebuffRules.safeAmplifier(mobEffect, Math.min(4, Math.max(0, n)));
            if (n2 < 0) {
                return false;
            }
            if (livingEntity.m_21023_(mobEffect)) {
                livingEntity.m_21195_(mobEffect);
            }
            return livingEntity.m_7292_(new MobEffectInstance(mobEffect, 300, n2));
        }
        catch (Throwable throwable) {
            ZICAI_BLOCKED.add(mobEffect);
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u7d2b\u83dc\u51cf\u76ca {} \u65bd\u52a0\u5931\u8d25({}) \u2014\u2014 \u5df2\u62c9\u9ed1, \u672c\u573a\u4e0d\u518d\u4f7f\u7528", (Object)ForgeRegistries.MOB_EFFECTS.getKey((Object)mobEffect), (Object)throwable.toString());
            return false;
        }
    }

    private static int zicaiAmpFor(LivingEntity livingEntity, MobEffect mobEffect) {
        int n = mobEffect == MobEffects.f_19615_ || mobEffect == MobEffects.f_19614_ ? 2 : 1;
        MobEffectInstance mobEffectInstance = livingEntity.m_21124_(mobEffect);
        if (mobEffectInstance == null) {
            return n;
        }
        return Math.min(4, Math.max(n, mobEffectInstance.m_19564_() + 1));
    }

    private static boolean zicaiAllowed(MobEffect mobEffect) {
        if (mobEffect == null) {
            return false;
        }
        if (mobEffect.m_19486_()) {
            return false;
        }
        if (mobEffect.m_19483_() != MobEffectCategory.HARMFUL) {
            return false;
        }
        if (mobEffect.m_8093_()) {
            return false;
        }
        ResourceLocation resourceLocation = ForgeRegistries.MOB_EFFECTS.getKey((Object)mobEffect);
        if (resourceLocation == null) {
            return false;
        }
        String string = resourceLocation.toString();
        for (String string2 : ZICAI_BLOCK_IDS) {
            if (!string.equals(string2)) continue;
            return false;
        }
        for (String string2 : ZICAI_BLOCK_NAMESPACES) {
            if (!resourceLocation.m_135827_().equals(string2)) continue;
            return false;
        }
        String string3 = resourceLocation.m_135815_().toLowerCase(Locale.ROOT);
        String string4 = mobEffect.m_19482_().getString();
        for (String string5 : ZICAI_BLOCK_KEYWORDS) {
            if (!string3.contains(string5) && !string4.toLowerCase(Locale.ROOT).contains(string5)) continue;
            return false;
        }
        return true;
    }

    private static synchronized List<MobEffect> zicaiPool() {
        if (ZICAI_POOL != null) {
            return ZICAI_POOL;
        }
        ArrayList<MobEffect> arrayList = new ArrayList<MobEffect>();
        int n = 0;
        for (Map.Entry entry : ForgeRegistries.MOB_EFFECTS.getEntries()) {
            MobEffect mobEffect = (MobEffect)entry.getValue();
            if (arrayList.contains(mobEffect) || !ForgeRegistries.MOB_EFFECTS.getKey((Object)mobEffect).m_135827_().equals("armamentarium") || !EasterBossEntity.zicaiAllowed(mobEffect)) continue;
            arrayList.add(mobEffect);
            ++n;
        }
        for (String[] stringArray : ZICAI_DEBUFF_IDS) {
            MobEffect mobEffect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(stringArray[0], stringArray[1]));
            if (mobEffect == null || arrayList.contains(mobEffect) || !EasterBossEntity.zicaiAllowed(mobEffect)) continue;
            arrayList.add(mobEffect);
        }
        for (Map.Entry entry : ForgeRegistries.MOB_EFFECTS.getEntries()) {
            MobEffect mobEffect = (MobEffect)entry.getValue();
            if (arrayList.contains(mobEffect) || !EasterBossEntity.zicaiAllowed(mobEffect)) continue;
            arrayList.add(mobEffect);
        }
        ZICAI_POOL = arrayList;
        StringBuilder stringBuilder = new StringBuilder();
        for (MobEffect mobEffect : arrayList) {
            stringBuilder.append(mobEffect.m_19482_().getString()).append('/');
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u7d2b\u83dc\u6cb3\u8c5a\u51cf\u76ca\u6c60 {} \u79cd(\u5176\u4e2d armamentarium {} \u79cd, \u5df2\u5254\u9664\u5931\u660e/\u9ed1\u6697/\u98d8\u6d6e/\u5730\u56fe\u7981\u7528/\u4e2d\u6027/\u77ac\u95f4\u7c7b/\u94c1\u9b54\u6cd5): {}", new Object[]{arrayList.size(), n, stringBuilder.toString()});
        return arrayList;
    }

    @Override
    public void ftboss$applyPufferSingle(ServerLevel serverLevel, LivingEntity livingEntity) {
        StringBuilder stringBuilder;
        List<MobEffect> list;
        if (livingEntity instanceof Player) {
            this.stripOneBuff((Player)livingEntity);
        }
        if ((list = EasterBossEntity.zicaiPool()).isEmpty()) {
            return;
        }
        ArrayList<StringBuilder> arrayList = new ArrayList<StringBuilder>(2);
        int n = 0;
        int n2 = Math.min(2, list.size());
        while (arrayList.size() < n2 && n++ < 128) {
            stringBuilder = list.get(this.f_19796_.m_188503_(list.size()));
            if (arrayList.contains(stringBuilder)) continue;
            arrayList.add(stringBuilder);
        }
        stringBuilder = new StringBuilder();
        int n3 = 0;
        for (MobEffect mobEffect : arrayList) {
            int n4;
            if (!this.safeAddDebuff(livingEntity, mobEffect, n4 = EasterBossEntity.zicaiAmpFor(livingEntity, mobEffect))) continue;
            ++n3;
            MutableComponent mutableComponent = Component.m_237110_((String)"potion.withAmplifier", (Object[])new Object[]{mobEffect.m_19482_(), Component.m_237115_((String)("enchantment.level." + (livingEntity.m_21124_(mobEffect).m_19564_() + 1)))});
            stringBuilder.append(mutableComponent.getString()).append(' ');
        }
        if (n3 <= 0) {
            return;
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123763_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 20, 0.4, 0.5, 0.4, 0.03);
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            player.m_5661_((Component)Component.m_237113_((String)("\u00a72\u7d2b\u83dc \u00a77\u7ed9\u4f60\u6302\u4e86: \u00a7c" + stringBuilder.toString().trim())), true);
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u7d2b\u83dc\u6cb3\u8c5a\u7ed9 {} \u6302\u4e86 {} \u79cd\u51cf\u76ca: {}", new Object[]{livingEntity.m_7755_().getString(), n3, stringBuilder.toString().trim()});
    }

    private boolean canShadowStrike(LivingEntity livingEntity) {
        if (this.canTeleportWith(livingEntity)) {
            return true;
        }
        if (!(livingEntity instanceof ServerPlayer)) {
            return false;
        }
        ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
        if (!this.combatPlayer((LivingEntity)serverPlayer) || serverPlayer.m_9236_() != this.m_9236_()) {
            return false;
        }
        return ManorFight.isPlayerFighting((Player)serverPlayer);
    }

    private void skillShadowStrike(ServerLevel serverLevel, LivingEntity livingEntity) {
        if (ManorSpellBridge.replace(this, livingEntity, "shadow_brand")) {
            return;
        }
        if (!this.canShadowStrike(livingEntity)) {
            return;
        }
        this.playCast(CastReadability.strikeWindup(this.getChallengeDifficulty().acceleratedTicks(SHADOW_STRIKE_WINDUP), this), 1);
        double d = this.m_20185_();
        double d2 = this.m_20186_();
        double d3 = this.m_20189_();
        Vec3 vec3 = livingEntity.m_20154_().m_82490_(-1.0);
        double d4 = livingEntity.m_20185_() + vec3.f_82479_ * 2.2;
        double d5 = livingEntity.m_20189_() + vec3.f_82481_ * 2.2;
        boolean bl = net.farmingtales.ftboss.zheergenfx.LightHooks.teleport(this, d4, livingEntity.m_20186_(), d5, true);
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11852_, SoundSource.HOSTILE, 1.0f, 0.7f);
        for (double[] dArray : new double[][]{{d, d2 + 1.0, d3}, {this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_()}}) {
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123765_, dArray[0], dArray[1], dArray[2], 30, 0.6, 0.8, 0.6, 0.05);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123762_, dArray[0], dArray[1], dArray[2], 20, 0.4, 0.6, 0.4, 0.03);
        }
        if (this.f_19796_.m_188500_() < 0.35) {
            this.spawnShadowPhantom(serverLevel, d, d2, d3, livingEntity, PHANTOM_SKILL_SHADOW_STRIKE);
        }
        this.shadowStrikeTarget = livingEntity.m_20148_();
        this.shadowStrikeWind = CastReadability.strikeWindup(this.getChallengeDifficulty().acceleratedTicks(SHADOW_STRIKE_WINDUP), this);
        this.beginSkillCue(this.m_20182_(), 4.0, this.shadowStrikeWind, true);
        SkillBodyDispatch.strikeStart(this);
        net.farmingtales.ftboss.zheergenfx.LightHooks.strike(this, false);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12376_, SoundSource.HOSTILE, 0.7f, 1.5f);
    }

    private void executeShadowStrike(ServerLevel serverLevel) {
        boolean bl;
        LivingEntity livingEntity;
        LivingEntity livingEntity2;
        Entity entity = this.shadowStrikeTarget == null ? null : serverLevel.m_8791_(this.shadowStrikeTarget);
        this.shadowStrikeTarget = null;
        if (!(entity instanceof LivingEntity) || !(livingEntity2 = (LivingEntity)entity).m_6084_() || livingEntity2 instanceof LivingEntity && !this.combatPlayer(livingEntity = livingEntity2)) {
            this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)new CompoundTag());
            SkillBodyDispatch.strikeEnd(this);
            return;
        }
        this.playImpactAction(14);
        this.releaseSkillCue(this.m_20182_());
        net.farmingtales.ftboss.zheergenfx.LightHooks.strike(this, true);
        boolean bl2 = bl = livingEntity2.m_20280_((Entity)this) <= 16.0 && this.m_142582_((Entity)livingEntity2);
        if (bl) {
            this.applyShadowStrikeEffect(serverLevel, livingEntity2);
        }
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_215750_, SoundSource.HOSTILE, 1.1f, 1.3f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u6298\u8033\u6839\u300c\u6697\u5f71\u7a81\u88ad\u300d: \u547d\u4e2d={} ({} \u4f24\u5bb3)", (Object)bl, (Object)Float.valueOf(112.0f));
        SkillBodyDispatch.strikeEnd(this);
    }

    private void applyShadowStrikeEffect(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.applyShadowStrikeEffect(serverLevel, livingEntity, 1.0, false);
    }

    private void applyShadowStrikeEffect(ServerLevel serverLevel, LivingEntity livingEntity, double d, boolean bl) {
        ShadowBlindEffect.applyTo(livingEntity, 300);
        float f = (float)((double)this.manorSkillDamage(livingEntity, 78.4f, 2.0) * d);
        if (bl) {
            livingEntity.f_19802_ = 0;
            ManorAdaptiveDamage.hurtSkill(livingEntity, (Entity)this, this.m_269291_().m_269341_(), f);
        } else {
            float f2 = (float)((double)f * 0.5 * 1.5);
            float f3 = f - f2;
            livingEntity.f_19802_ = 0;
            livingEntity.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), f3);
            livingEntity.f_19802_ = 0;
            ManorAdaptiveDamage.hurtSkill(livingEntity, (Entity)this, this.m_269291_().m_269341_(), f2);
        }
        if (livingEntity instanceof LivingEntity) {
            LivingEntity livingEntity2 = livingEntity;
            this.knockbackPlayer(livingEntity2);
        }
        livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19615_, 360, 2));
        if (livingEntity instanceof LivingEntity) {
            LivingEntity livingEntity3 = livingEntity;
            this.applyShadowVoid(livingEntity3, serverLevel);
        }
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_235898_, livingEntity.m_20185_(), livingEntity.m_20186_() + 1.0, livingEntity.m_20189_(), 20, 0.3, 0.5, 0.3, 0.05);
    }

    public void onManorFiveCastStart(ManorFiveCombatRules.Skill skill) {
        QingyiCastSync.startNative(this, skill);
        if (this.getVariant() != 7 || this.isMini() || skill == null) {
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        level = this.m_5448_();
        try {
            double d = this.m_20185_();
            double d2 = this.m_20186_();
            double d3 = this.m_20189_();
            if (level != null && this.canShadowStrike((LivingEntity)level)) {
                Vec3 vec3 = level.m_20154_().m_82490_(-1.0);
                boolean bl = net.farmingtales.ftboss.zheergenfx.LightHooks.teleport(this, level.m_20185_() + vec3.f_82479_ * 2.2, level.m_20186_(), level.m_20189_() + vec3.f_82481_ * 2.2, true);
                this.m_21563_().m_24960_((Entity)level, 60.0f, 60.0f);
                if (bl) {
                    serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11852_, SoundSource.HOSTILE, 1.0f, 0.7f);
                    for (double[] dArray : new double[][]{{d, d2 + 1.0, d3}, {this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_()}}) {
                        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123765_, dArray[0], dArray[1], dArray[2], 30, 0.6, 0.8, 0.6, 0.05);
                        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123762_, dArray[0], dArray[1], dArray[2], 20, 0.4, 0.6, 0.4, 0.03);
                    }
                }
            }
            if (this.f_19796_.m_188500_() < 0.35) {
                this.spawnShadowPhantom(serverLevel, d, d2, d3, (LivingEntity)level, skill.id());
            }
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u6298\u8033\u6839\u4f20\u9001/\u7559\u5f71\u5931\u8d25(\u5df2\u5ffd\u7565, \u4e0d\u5d29\u670d): {}", (Object)throwable.toString());
        }
    }

    private void spawnShadowPhantom(ServerLevel serverLevel, double d, double d2, double d3, LivingEntity livingEntity, String string) {
        try {
            double d4;
            double d5;
            ShadowPhantomEntity shadowPhantomEntity = (ShadowPhantomEntity)((EntityType)FTEntities.SHADOW_PHANTOM.get()).m_20615_((Level)serverLevel);
            if (shadowPhantomEntity == null) {
                return;
            }
            int n = 0 + PhantomTimeline.readableDelay(this.f_19796_.m_188503_(101));
            double d6 = 1.0;
            double d7 = 0.0;
            if (livingEntity != null) {
                d5 = livingEntity.m_20185_() - d;
                double d8 = Math.sqrt(d5 * d5 + (d4 = livingEntity.m_20189_() - d3) * d4);
                if (d8 > 1.0E-4) {
                    d6 = -d4 / d8;
                    d7 = d5 / d8;
                } else {
                    d6 = 0.0;
                    d7 = 1.0;
                }
            } else {
                d5 = this.f_19796_.m_188500_() * Math.PI * 2.0;
                d6 = Math.cos(d5);
                d7 = Math.sin(d5);
            }
            d5 = d + d6 * 0.9;
            d4 = d3 + d7 * 0.9;
            shadowPhantomEntity.m_7678_(d5, d2, d4, this.m_146908_(), 0.0f);
            shadowPhantomEntity.setLife(n + 20);
            if (!serverLevel.m_7967_((Entity)shadowPhantomEntity)) {
                return;
            }
            this.phantomEchoTicks = n;
            this.phantomEchoSkill = string;
            this.phantomEchoTarget = livingEntity == null ? null : livingEntity.m_20148_();
            this.phantomEchoX = d5;
            this.phantomEchoY = d2;
            this.phantomEchoZ = d4;
            net.farmingtales.ftboss.zheergenfx.LightHooks.phantom(this, this.phantomEchoX, this.phantomEchoY, this.phantomEchoZ, this.phantomEchoTicks);
            PhantomHooks.capture(this);
            serverLevel.m_6263_(null, d5, d2, d4, SoundEvents.f_11852_, SoundSource.HOSTILE, 0.8f, 0.5f);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123789_, d5, d2 + 1.0, d4, 30, 0.5, 0.9, 0.5, 0.08);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u6298\u8033\u6839\u300c\u6697\u5f71\u7a81\u88ad\u300d\u7559\u4e0b\u7d2b\u8272\u6b8b\u5f71 @{},{},{} (\u8bb0\u5f55\u6280\u80fd={}, {} tick \u540e\u590d\u73b0\u4e00\u6b21, \u4f24\u5bb3={}% \u771f\u4f24)", new Object[]{(int)d5, (int)d2, (int)d4, string, n, 25});
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u6298\u8033\u6839\u6b8b\u5f71\u751f\u6210\u5931\u8d25(\u5df2\u5ffd\u7565, \u4e0d\u5d29\u670d): {}", (Object)throwable.toString());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void firePhantomEcho(ServerLevel serverLevel) {
        net.farmingtales.ftboss.zheergenfx.LightHooks.echo(this, this.phantomEchoX, this.phantomEchoY, this.phantomEchoZ);
        try {
            boolean bl;
            LivingEntity livingEntity;
            Object object;
            double d = this.phantomEchoX;
            double d2 = this.phantomEchoY;
            double d3 = this.phantomEchoZ;
            String string = this.phantomEchoSkill;
            LivingEntity livingEntity2 = null;
            if (this.phantomEchoTarget != null && (object = serverLevel.m_8791_(this.phantomEchoTarget)) instanceof LivingEntity && (livingEntity = (LivingEntity)object).m_6084_()) {
                livingEntity2 = livingEntity;
            }
            if (livingEntity2 == null) {
                double d4 = 576.0;
                for (LivingEntity livingEntity3 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
                    double d5;
                    if (!this.combatPlayer(livingEntity3) || !((d5 = livingEntity3.m_20275_(d, d2, d3)) < d4)) continue;
                    d4 = d5;
                    livingEntity2 = livingEntity3;
                }
            }
            boolean bl2 = bl = livingEntity2 != null && livingEntity2.m_20275_(d, d2, d3) <= 256.0 && this.m_142582_((Entity)livingEntity2);
            if (PHANTOM_SKILL_SHADOW_STRIKE.equals(string)) {
                if (bl) {
                    this.applyShadowStrikeEffect(serverLevel, livingEntity2, 0.25, true);
                }
                serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_215750_, SoundSource.HOSTILE, 0.8f, 1.5f);
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_235898_, d, d2 + 1.0, d3, 24, 0.4, 0.6, 0.4, 0.05);
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u6298\u8033\u6839\u6b8b\u5f71\u91cd\u653e\u300c{}\u300d: \u547d\u4e2d={} ({}% \u771f\u4f24)", new Object[]{string, bl, 25});
            } else {
                object = ManorFiveCombatRules.Skill.byId(string);
                int n = object == null ? 0 : this.manorFiveSkills.echoCast(serverLevel, (ManorFiveCombatRules.Skill)((Object)object), new Vec3(d, d2, d3), livingEntity2, 0.25, true);
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u6298\u8033\u6839\u6b8b\u5f71\u91cd\u653e\u300c{}\u300d: \u6210\u529f\u547d\u4e2d {} \u4eba ({}% \u771f\u4f24)", new Object[]{string, n, 25});
            }
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u6298\u8033\u6839\u6b8b\u5f71\u590d\u73b0\u5931\u8d25(\u5df2\u5ffd\u7565, \u4e0d\u5d29\u670d): {}", (Object)throwable.toString());
        }
        finally {
            this.phantomEchoTicks = -1;
            this.phantomEchoSkill = null;
            this.phantomEchoTarget = null;
        }
    }

    private void applyShadowVoid(LivingEntity livingEntity, ServerLevel serverLevel) {
        if (livingEntity == null || livingEntity.m_5833_() || ManorLivingTargets.creative(livingEntity)) {
            return;
        }
        MobEffect mobEffect = (MobEffect)FTEffects.SHADOW_EROSION.get();
        if (mobEffect == null) {
            return;
        }
        MobEffectInstance mobEffectInstance = livingEntity.m_21124_(mobEffect);
        int n = mobEffectInstance == null ? 0 : mobEffectInstance.m_19564_() + 1;
        int n2 = Math.min(5, n + 1);
        livingEntity.m_7292_(new MobEffectInstance(mobEffect, 200, n2 - 1, false, true, true));
        if (n2 > n) {
            int n3 = (int)ErosionRules.unscaledDamage(4.0f * (float)n2);
            ManorLivingTargets.tell(livingEntity, (Component)Component.m_237113_((String)("\u00a75\u6298\u8033\u6839 \u00a77\u7684\u6697\u5f71\u7f20\u4f4f\u4e86\u4f60: \u00a7d\u865a\u7a7a\u4fb5\u8680 \u00a77" + n2 + " \u5c42 \u00a77(\u6bcf\u79d2 " + n3 + " \u70b9\u865a\u7a7a\u4f24\u5bb3, \u7a7f\u7532)")), true);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u6298\u8033\u6839\u300c\u865a\u7a7a\u4fb5\u8680\u300d-> {} \u53e0\u5230 {} \u5c42(\u6bcf\u79d2 {} \u70b9\u865a\u7a7a\u4f24\u5bb3)", new Object[]{livingEntity.m_7755_().getString(), n2, n3});
        }
    }

    private void dandanRampAttack() {
        ++this.dandanHits;
        AttributeInstance attributeInstance = this.m_21051_(Attributes.f_22281_);
        if (attributeInstance == null) {
            return;
        }
        double d = attributeInstance.m_22115_();
        double d2 = Math.max(0.0, 300.0 - d);
        double d3 = Math.min(d2, (double)this.dandanHits * 5.0);
        attributeInstance.m_22120_(DANDAN_ATK_MOD);
        attributeInstance.m_22118_(new AttributeModifier(DANDAN_ATK_MOD, "ftboss.dandan_ramp", d3, AttributeModifier.Operation.ADDITION));
        LightHooks.ramp(this);
    }

    private void dandanBeaconBan() {
        if (this.getVariant() != 1 || this.isMini()) {
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        long l = serverLevel.m_46467_();
        for (Player player : serverLevel.m_6907_()) {
            if (!player.m_6084_() || player.m_5833_() || player.m_20280_((Entity)this) > 9216.0 && !ManorFight.isPlayerFighting(player)) continue;
            int n = 0;
            Object object = new ArrayList(player.m_21220_()).iterator();
            while (object.hasNext()) {
                MobEffectInstance mobEffectInstance = (MobEffectInstance)object.next();
                if (!mobEffectInstance.m_19571_()) continue;
                LightHooks.removeAmbient((LivingEntity)player, mobEffectInstance.m_19544_());
                ++n;
            }
            if (n <= 0 || (object = this.beaconBanNotice.get(player.m_20148_())) != null && l - (Long)object < 100L) continue;
            if (this.beaconBanNotice.size() > 64) {
                this.beaconBanNotice.entrySet().removeIf(entry -> l - (Long)entry.getValue() >= 100L);
            }
            this.beaconBanNotice.put(player.m_20148_(), l);
            player.m_5661_((Component)Component.m_237113_((String)("\u00a7e\u86cb\u86cb \u00a77\u538b\u5236\u4e86\u4f60\u7684 \u00a7b\u4fe1\u6807\u52a0\u6210 \u00a77(" + n + " \u9879)")), true);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u86cb\u86cb\u4fe1\u6807\u538b\u5236 -> {} (\u6458\u6389 {} \u9879 ambient \u6548\u679c)", (Object)player.m_7755_().getString(), (Object)n);
        }
    }

    private void dandanVortexTick(ServerLevel serverLevel) {
        if (this.getVariant() != 1 || this.isMini()) {
            return;
        }
        if (this.dandanVortexTicks > 0) {
            int n = 0;
            double d = Double.MAX_VALUE;
            for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
                double d2;
                double d3;
                double d4;
                double d5;
                if (!this.canTeleportWith((LivingEntity)serverPlayer) || !serverPlayer.m_6084_() || serverPlayer.m_5833_() || !BossTargets.valid((Player)serverPlayer) || (d5 = (d4 = this.dandanVortexX - serverPlayer.m_20185_()) * d4 + (d3 = this.dandanVortexZ - serverPlayer.m_20189_()) * d3 + (d2 = this.dandanVortexY - serverPlayer.m_20186_()) * d2) > 441.0) continue;
                double d6 = Math.sqrt(d4 * d4 + d3 * d3);
                double d7 = Math.min(0.26, Math.max(0.0, d6 - 0.2));
                double d8 = serverPlayer.m_20185_();
                double d9 = serverPlayer.m_20189_();
                if (d6 > 1.0E-4 && d7 > 0.0) {
                    d8 += d4 / d6 * d7;
                    d9 += d3 / d6 * d7;
                }
                double d10 = serverPlayer.m_20186_();
                double d11 = this.dandanVortexY + 1.0 - serverPlayer.m_20186_();
                if (d11 > 0.0) {
                    d10 += Math.min(0.12, d11);
                }
                serverPlayer.f_8906_.m_9774_(d8, d10, d9, serverPlayer.m_146908_(), serverPlayer.m_146909_());
                serverPlayer.f_19864_ = true;
                serverPlayer.f_19789_ = 0.0f;
                ++n;
                d = Math.min(d, Math.sqrt(d5));
            }
            this.dandanVortexPulls += n;
            double d12 = (double)this.f_19797_ * 0.35;
            for (int i = 0; i < 10; ++i) {
                double d13 = d12 + (double)i * 0.6283185307179586;
                double d14 = 21.0 * (1.0 - (double)(this.f_19797_ % 40) / 40.0);
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123760_, this.dandanVortexX + Math.cos(d13) * d14, this.dandanVortexY + 0.4 + Math.sin(d13 * 2.0) * 0.4, this.dandanVortexZ + Math.sin(d13) * d14, 2, 0.0, 0.0, 0.0, 0.05);
            }
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123789_, this.dandanVortexX, this.dandanVortexY + 1.0, this.dandanVortexZ, 6, 0.5, 0.6, 0.5, 0.05);
            if (--this.dandanVortexLogCd <= 0) {
                this.dandanVortexLogCd = 20;
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u86cb\u86cb\u865a\u7a7a\u6f29\u6da1 \u5438\u5f15\u4e2d: {} \u540d\u73a9\u5bb6(\u6700\u8fd1 {} \u683c), \u5269 {} tick", new Object[]{n, d == Double.MAX_VALUE ? "-" : String.format("%.1f", d), this.dandanVortexTicks});
            }
            if (--this.dandanVortexTicks <= 0) {
                if (this.dandanVortexEntity != null) {
                    this.dandanVortexEntity.m_146870_();
                    this.dandanVortexEntity = null;
                }
                serverLevel.m_6263_(null, this.dandanVortexX, this.dandanVortexY, this.dandanVortexZ, SoundEvents.f_12287_, SoundSource.HOSTILE, 1.0f, 0.7f);
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u86cb\u86cb\u865a\u7a7a\u6f29\u6da1 \u7ed3\u675f: \u7d2f\u8ba1\u5438\u5f15 {} \u4eba\u6b21", (Object)this.dandanVortexPulls);
            }
            return;
        }
        if (this.dandanVortexCd > 0) {
            --this.dandanVortexCd;
            return;
        }
        LivingEntity livingEntity = this.m_5448_();
        if (livingEntity == null || !livingEntity.m_6084_() || !BossTargets.valid(livingEntity)) {
            livingEntity = null;
            double d = 576.0;
            for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
                double d15;
                if (!BossTargets.valid((Player)serverPlayer) || !((d15 = serverPlayer.m_20280_((Entity)this)) < d)) continue;
                d = d15;
                livingEntity = serverPlayer;
            }
        }
        if (livingEntity == null) {
            this.dandanVortexCd = 20;
            return;
        }
        this.dandanVortexX = this.m_20185_();
        this.dandanVortexY = this.m_20186_() + 0.2;
        this.dandanVortexZ = this.m_20189_();
        this.dandanVortexTicks = 80;
        this.dandanVortexCd = 400;
        this.dandanVortexLogCd = 20;
        this.dandanVortexPulls = 0;
        MotionServer.vortex(this);
        LightHooks.vortex(this, this.dandanVortexX, this.dandanVortexY, this.dandanVortexZ, this.dandanVortexTicks);
        this.dandanVortexEntity = EnderVortex.spawn(serverLevel, (LivingEntity)this, this.dandanVortexX, this.dandanVortexY, this.dandanVortexZ, 100);
        serverLevel.m_6263_(null, this.dandanVortexX, this.dandanVortexY, this.dandanVortexZ, SoundEvents.f_11860_, SoundSource.HOSTILE, 1.2f, 0.8f);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123812_, this.dandanVortexX, this.dandanVortexY + 2.0, this.dandanVortexZ, 1, 0.0, 0.0, 0.0, 0.0);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u86cb\u86cb\u300c\u865a\u7a7a\u6f29\u6da1\u300d\u8d77\u624b @({}, {}, {}) \u5438\u5f15\u534a\u5f84 {} \u683c / {} tick, \u672b\u5f71\u5b88\u536b\u5b9e\u4f53={}", new Object[]{String.format("%.1f", this.dandanVortexX), String.format("%.1f", this.dandanVortexY), String.format("%.1f", this.dandanVortexZ), 21, 80, this.dandanVortexEntity != null});
    }

    public boolean shadowVoidMode() {
        return this.shadowVoidMode;
    }

    public void onShadowHitPlayer(long l) {
        ++this.shadowStreak;
        this.shadowLastHitTick = l;
        if (!this.shadowVoidMode && this.shadowStreak >= 25) {
            this.shadowVoidMode = true;
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u6298\u8033\u6839 \u8fde\u7eed\u547d\u4e2d\u73a9\u5bb6 {} \u6b21 -> \u6b64\u540e\u5b83\u5bf9\u73a9\u5bb6\u7684\u4f24\u5bb3 50% \u8f6c\u4e3a\u865a\u7a7a", (Object)this.shadowStreak);
        } else if (this.shadowStreak % 10 == 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u6298\u8033\u6839 \u8fde\u7eed\u547d\u4e2d\u73a9\u5bb6 {} \u6b21{}", (Object)this.shadowStreak, (Object)(this.shadowVoidMode ? "(\u5df2\u8f6c\u865a\u7a7a)" : ""));
        }
        net.farmingtales.ftboss.zheergenfx.LightHooks.voidMode(this);
    }

    private void shadowStreakTick() {
        if (this.shadowStreak <= 0) {
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (serverLevel.m_46467_() - this.shadowLastHitTick > 600L) {
            this.shadowStreak = 0;
        }
    }

    private void skillPoisonBurst(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.poisonWind = this.getChallengeDifficulty().acceleratedTicks(POISON_WINDUP);
        this.playCast(this.poisonWind, 0);
        this.beginSkillCue(this.m_20182_(), 6.75, this.poisonWind, true);
        this.m_21563_().m_24960_((Entity)livingEntity, 60.0f, 60.0f);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12291_, SoundSource.HOSTILE, 1.0f, 0.8f);
    }

    private void executePoisonBurst(ServerLevel serverLevel) {
        this.playImpactAction(16);
        this.releaseSkillCue(this.m_20182_());
        double d = this.m_20185_();
        double d2 = this.m_20186_();
        double d3 = this.m_20189_();
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123763_, d, d2 + 0.6, d3, 28, 2.3, 0.25, 2.3, 0.04);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123753_, d, d2 + 0.4, d3, 16, 1.8, 0.2, 1.8, 0.04);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123759_, d, d2 + 1.0, d3, 30, 1.4, 0.6, 1.4, 0.06);
        serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_12290_, SoundSource.HOSTILE, 1.4f, 0.7f);
        serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11774_, SoundSource.HOSTILE, 1.2f, 0.9f);
        int n = 0;
        for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
            if (!this.combatPlayer(livingEntity) || livingEntity.m_20275_(d, d2, d3) > 45.5625) continue;
            livingEntity.f_19802_ = 0;
            livingEntity.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 56.0f);
            livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 600, 2));
            livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 360, 1));
            ZicaiTargetIsolation.apply(this, serverLevel, livingEntity);
            ++n;
        }
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u7d2b\u83dc\u300c\u6bd2\u96fe\u7206\u53d1\u300d\u547d\u4e2d {} \u4eba ({} \u4f24\u5bb3 + \u4e2d\u6bd2 III 10 \u79d2)", (Object)n, (Object)Float.valueOf(56.0f));
    }

    private void pumpBurst() {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            this.burstShots = 0;
            DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        --this.burstShots;
        level = this.m_5448_();
        if (level == null || !level.m_6084_()) {
            this.burstShots = 0;
            DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
            return;
        }
        if (this.m_20280_((Entity)level) > 324.0) {
            this.burstShots = 0;
            this.doraTempo.miss();
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u6735\u62c9\u76ee\u6807\u8d85\u51fa\u5c04\u7a0b {} \u683c -> \u505c\u706b", (Object)18);
            DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
            return;
        }
        Vec3 vec3 = new Vec3(this.m_20185_(), this.m_20188_() - 0.15, this.m_20189_());
        Vec3 vec32 = new Vec3(level.m_20185_(), level.m_20186_() + (double)level.m_20206_() * 0.5, level.m_20189_());
        Vec3 vec33 = new Vec3(vec32.f_82479_ - vec3.f_82479_, 0.0, vec32.f_82481_ - vec3.f_82481_);
        Vec3 vec34 = vec33.m_82556_() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(-vec33.f_82481_, 0.0, vec33.f_82479_).m_82541_();
        double d = Math.sqrt(this.m_20280_((Entity)level));
        double d2 = Mth.m_14008_((double)((d - 4.0) / 14.0), (double)0.0, (double)1.0);
        double d3 = 0.42 + 0.36000000000000004 * d2;
        Vec3 vec35 = vec32.m_82549_(vec34.m_82490_((this.f_19796_.m_188500_() * 2.0 - 1.0) * d3));
        BlockHitResult blockHitResult = this.m_9236_().m_45547_(new ClipContext(vec3, vec35, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this));
        boolean bl = blockHitResult.m_6662_() == HitResult.Type.MISS;
        Vec3 vec36 = bl ? vec35 : blockHitResult.m_82450_();
        boolean bl2 = bl && level.m_20191_().m_82400_(0.06).m_82371_(vec3, vec36).isPresent();
        WeaponFx.send(serverLevel, WeaponFxRules.Kind.DORA_TRACER, WeaponFxRules.Theme.ARC, vec3, vec36, 0.12, this.m_146908_());
        ManorExpansionHooks.bullet(this, serverLevel, (LivingEntity)level);
        this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)DoraBasicCue.stamp(this.getSkillCue(), 0, this.m_9236_().m_46467_(), this.getVariant()));
        DoraMotionSync.shot(this);
        serverLevel.m_6263_(null, this.m_20185_(), this.m_20186_() + 1.2, this.m_20189_(), SoundEvents.f_11928_, SoundSource.HOSTILE, 0.65f, 1.7f);
        if (bl2) {
            level.f_19802_ = 0;
            boolean bl3 = level.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 5.3333335f * 1.25f);
            level.f_19802_ = 0;
            boolean bl4 = level.m_6469_(this.m_269291_().m_269104_((Entity)this, (Entity)this), 5.3333335f * 1.25f);
            if (bl3 || bl4) {
                ++this.burstHits;
                if (level instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity)level;
                    this.doraKnockback(livingEntity, 0.55, 0.1);
                    this.doraHit(livingEntity);
                }
            } else {
                this.doraTempo.miss();
            }
            WeaponFx.send(serverLevel, WeaponFxRules.Kind.DORA_IMPACT, WeaponFxRules.Theme.GOLD, vec36, vec36, 0.34, this.m_146908_());
        } else {
            this.doraTempo.miss();
        }
        if (this.burstShots <= 0) {
            if (this.burstHits >= 5) {
                level.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 240, 2));
                ExtraDebuffs.bleed((LivingEntity)level, 160, 1);
                FTBossMod.LOGGER.info("[FTBoss][Easter] \u6735\u62c9\u538b\u5236\u751f\u6548: \u547d\u4e2d {} \u53d1 -> \u76ee\u6807\u7f13\u6162 III 4 \u79d2 + \u6d41\u8840 8 \u79d2", (Object)this.burstHits);
            }
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u6735\u62c9\u300c\u538b\u5236\u5c04\u51fb\u300d\u7ed3\u675f: \u547d\u4e2d {}/{}", (Object)this.burstHits, (Object)this.burstTotalShots);
            if (this.getVariant() == 2) {
                this.startReload(serverLevel);
            }
        }
        DoraMotionSync.capture(this, this.burstShots, this.reloadTicks, this.doraReloadProgress, this.doraSniper.busy());
    }

    protected void m_7472_(DamageSource damageSource, int n, boolean bl) {
        super.m_7472_(damageSource, n, bl);
    }

    public void m_6667_(DamageSource damageSource) {
        ServerLevel serverLevel;
        Level level;
        this.lemonSacrifice.onRemoval(Entity.RemovalReason.KILLED);
        this.cancelDoraSniper();
        this.manorFiveSkills.cancel();
        this.gongdiSkills.reset();
        if (!this.m_9236_().f_46443_ && this.bombFuse > 0 && !this.bombBlown && (level = this.m_9236_()) instanceof ServerLevel) {
            serverLevel = (ServerLevel)level;
            this.detonate(serverLevel, true);
        }
        super.m_6667_(damageSource);
        BossBars.untrack((Entity)this, this.bossBar);
        this.bossBar.m_7706_();
        if (!this.m_9236_().f_46443_ && (level = this.m_9236_()) instanceof ServerLevel) {
            serverLevel = (ServerLevel)level;
            BossMusic.release(serverLevel, (LivingEntity)this);
        }
        if (!this.m_9236_().f_46443_ && (level = this.m_9236_()) instanceof ServerLevel) {
            serverLevel = (ServerLevel)level;
            if (this.getVariant() == 10 && !this.isMini()) {
                this.xueliMaids.reset();
            }
            if (!(this.getVariant() != 0 && this.getVariant() != 10 || this.isMini())) {
                this.clearMinis(serverLevel);
            }
        }
    }

    private void clearMinis(ServerLevel serverLevel) {
        int n = 0;
        for (EasterBossEntity easterBossEntity : serverLevel.m_45976_(EasterBossEntity.class, this.m_20191_().m_82400_(128.0))) {
            if (easterBossEntity == this || !easterBossEntity.isMini() || easterBossEntity.getVariant() != this.getVariant()) continue;
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, easterBossEntity.m_20185_(), easterBossEntity.m_20186_() + 0.6, easterBossEntity.m_20189_(), 18, 0.3, 0.5, 0.3, 0.08);
            if (!this.m_20148_().equals(ManorFightMembers.ownerId((Entity)easterBossEntity))) continue;
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123762_, easterBossEntity.m_20185_(), easterBossEntity.m_20186_() + 0.4, easterBossEntity.m_20189_(), 8, 0.25, 0.3, 0.25, 0.03);
            easterBossEntity.m_146870_();
            ++n;
        }
        if (n > 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u67da\u5b50\u672c\u4f53\u9635\u4ea1/\u9000\u573a -> {} \u4e2a\u8ff7\u4f60\u5206\u8eab\u968f\u4e3b\u6d88\u6563", (Object)n);
        }
    }

    public void m_142687_(Entity.RemovalReason removalReason) {
        Level level;
        this.qizaiMagic.forceCancel();
        this.qizaiArsenal.cancel();
        if (removalReason.m_146965_()) {
            this.xuanlanSky.cancel();
        }
        this.lemonSacrifice.onRemoval(removalReason);
        this.cancelDoraSniper();
        this.manorFiveSkills.cancel();
        this.gongdiSkills.reset();
        if (!this.m_9236_().f_46443_ && removalReason.m_146965_() && (level = this.m_9236_()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.clearSummons(serverLevel);
            if (this.getVariant() == 10 && !this.isMini()) {
                this.xueliMaids.reset();
            }
            if (!(this.getVariant() != 0 && this.getVariant() != 10 || this.isMini())) {
                this.clearMinis(serverLevel);
            }
        }
        BossBars.untrack((Entity)this, this.bossBar);
        super.m_142687_(removalReason);
    }

    public void m_7380_(CompoundTag compoundTag) {
        super.m_7380_(compoundTag);
        compoundTag.m_128359_("ManorDifficulty", this.getChallengeDifficulty().id());
        compoundTag.m_128405_("variant", this.getVariant());
        compoundTag.m_128379_("ManorMini", this.isMini());
        compoundTag.m_128405_("ManorMiniLife", this.miniLife);
        compoundTag.m_128405_("ManorBombFuse", this.bombFuse);
        compoundTag.m_128379_("ManorBombBlown", this.bombBlown);
        if (this.manorSummonerId != null) {
            compoundTag.m_128362_("ManorSummoner", this.manorSummonerId);
        } else {
            compoundTag.m_128473_("ManorSummoner");
        }
        compoundTag.m_128405_("manorFiveRotation", this.manorFiveSkills.rotation());
        compoundTag.m_128405_("qingyiTridentStacks", this.qingyiTridentStacks);
        compoundTag.m_128405_("xueliNext", this.xueliNext);
        compoundTag.m_128405_("xueliDialogueProgress", this.xueliDialogue.progress());
        if (this.getVariant() == 10 && !this.isMini()) {
            this.xueliMaids.save(compoundTag);
        }
        compoundTag.m_128379_("qingyiAlt", this.qingyiAlt);
        compoundTag.m_128379_("qingyiConfluenceNext", this.qingyiConfluenceNext);
        compoundTag.m_128379_("doraSniperNext", this.doraSniperNext);
        compoundTag.m_128405_("lemonStep", this.lemonStep);
        compoundTag.m_128405_("lemonSummonLife", this.summonLife);
        ListTag listTag = new ListTag();
        for (UUID uUID : this.summons) {
            listTag.add((Object)NbtUtils.m_129226_((UUID)uUID));
        }
        compoundTag.m_128365_("lemonSummons", (Tag)listTag);
        compoundTag.m_128405_("openingFlywheelsGranted", this.openingFlywheelsGranted);
        this.xuanlanSky.save(compoundTag);
    }

    public void m_7378_(CompoundTag compoundTag) {
        super.m_7378_(compoundTag);
        this.setChallengeDifficulty(ManorDifficulty.fromId(compoundTag.m_128461_("ManorDifficulty")));
        if (compoundTag.m_128441_("variant")) {
            this.setVariant(compoundTag.m_128451_("variant"));
        }
        UUID uUID = this.manorSummonerId = compoundTag.m_128403_("ManorSummoner") ? compoundTag.m_128342_("ManorSummoner") : null;
        if (compoundTag.m_128471_("ManorMini")) {
            this.f_19804_.m_135381_(DATA_MINI, (Object)true);
            this.miniLife = Math.max(1, Math.min(300, compoundTag.m_128451_("ManorMiniLife")));
            this.bombFuse = Math.max(0, compoundTag.m_128451_("ManorBombFuse"));
            this.bombBlown = compoundTag.m_128471_("ManorBombBlown");
        }
        EasterBossEntity easterBossEntity = this;
        YouziMiniBalance.restore(easterBossEntity);
        easterBossEntity.refreshWalkingSpeed();
        this.manorFiveSkills.restoreRotation(compoundTag.m_128451_("manorFiveRotation"));
        if (compoundTag.m_128441_("qingyiTridentStacks")) {
            this.qingyiTridentStacks = Math.max(0, Math.min(200, compoundTag.m_128451_("qingyiTridentStacks")));
            this.syncQingyiTridentBuffs();
        }
        if (compoundTag.m_128441_("xueliNext")) {
            this.xueliNext = Math.floorMod(compoundTag.m_128451_("xueliNext"), 7);
        }
        this.doraSniperNext = compoundTag.m_128471_("doraSniperNext");
        this.lemonStep = compoundTag.m_128441_("lemonStep") ? Math.floorMod(compoundTag.m_128451_("lemonStep"), 3) : 1;
        this.summonLife = Math.max(0, Math.min(1800, compoundTag.m_128451_("lemonSummonLife")));
        this.summons.clear();
        for (Tag tag : compoundTag.m_128437_("lemonSummons", 11)) {
            IntArrayTag intArrayTag;
            if (!(tag instanceof IntArrayTag) || (intArrayTag = (IntArrayTag)tag).m_128648_().length != 4) continue;
            this.summons.add(NbtUtils.m_129233_((Tag)intArrayTag));
        }
        this.openingFlywheelsGranted = compoundTag.m_128441_("openingFlywheelsGranted") ? FlywheelCombatRules.openingProgress(compoundTag.m_128451_("openingFlywheelsGranted"), 0) : 10;
        this.xueliDialogue.restore(compoundTag.m_128451_("xueliDialogueProgress"), this.m_9236_().m_46467_());
        this.xueliMaids.load(compoundTag);
        if (compoundTag.m_128441_("qingyiAlt")) {
            this.qingyiAlt = compoundTag.m_128471_("qingyiAlt");
        }
        if (compoundTag.m_128441_("qingyiConfluenceNext")) {
            this.qingyiConfluenceNext = compoundTag.m_128471_("qingyiConfluenceNext");
        }
        this.applyQingyiFlatBuffs();
        this.xuanlanSky.load(compoundTag);
        if (!this.haleyFlying && this.xueliSlamWarn <= 0 && this.xueliSlamLeft <= 0) {
            this.m_20242_(false);
        }
    }

    public boolean m_6785_(double d) {
        return false;
    }

    public static boolean spawn(ServerLevel serverLevel, int n, double d, double d2, double d3, float f) {
        if (!SummonGate.isAllowedDim((Level)serverLevel)) {
            return false;
        }
        EntityType entityType = (EntityType)FTEntities.EASTER_BOSS.get();
        EasterBossEntity easterBossEntity = (EasterBossEntity)entityType.m_20615_((Level)serverLevel);
        if (easterBossEntity == null) {
            return false;
        }
        easterBossEntity.m_7678_(d, d2, d3, f, 0.0f);
        easterBossEntity.setVariant(n);
        serverLevel.m_7967_((Entity)easterBossEntity);
        return true;
    }

    public void dropWeapon() {
        String string = WEAPONS[this.getVariant()];
        if (string == null || string.isEmpty()) {
            return;
        }
        try {
            Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(string));
            if (item != null) {
                this.m_19983_(new ItemStack((ItemLike)item));
            }
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u6389\u843d\u6b66\u5668\u5931\u8d25 {}: {}", (Object)string, (Object)throwable.toString());
        }
    }

    private /* synthetic */ void lambda$shareBuffsWith$1(EasterBossEntity easterBossEntity, int[] nArray) {
        for (MobEffectInstance mobEffectInstance : new ArrayList(this.m_21220_())) {
            MobEffectInstance mobEffectInstance2;
            if (mobEffectInstance.m_19544_() == null || !mobEffectInstance.m_19544_().m_19486_() || (mobEffectInstance2 = easterBossEntity.m_21124_(mobEffectInstance.m_19544_())) != null && mobEffectInstance2.m_19564_() >= mobEffectInstance.m_19564_() && mobEffectInstance2.m_19557_() > 60) continue;
            MaqueSupportHooks.empower((LivingEntity)easterBossEntity, new MobEffectInstance(mobEffectInstance.m_19544_(), 300, mobEffectInstance.m_19564_(), true, false, false), (Entity)this, 5);
            nArray[0] = nArray[0] + 1;
        }
    }

    private /* synthetic */ void lambda$sparrowTick$0(EasterBossEntity easterBossEntity, float f, StringBuilder stringBuilder) {
        for (int i = 0; i < 3; ++i) {
            MobEffect mobEffect = CHAIN_BUFF_POOL[this.f_19796_.m_188503_(CHAIN_BUFF_POOL.length)];
            MaqueSupportHooks.empower((LivingEntity)easterBossEntity, new MobEffectInstance(mobEffect, 300, easterBossEntity.buffAmp(1), true, false, false), (Entity)this, 3);
            stringBuilder.append(mobEffect.m_19482_().getString()).append(' ');
        }
    }

    static {
        foodBuffPoolLogged = false;
        ZICAI_DEBUFF_IDS = new String[][]{{"armamentarium", "pure_venom"}, {"minecraft", "wither"}, {"minecraft", "poison"}, {"tridents_n_stuff", "bleeding"}, {"legendary_monsters", "bleeding"}};
        ZICAI_BLOCKED = ConcurrentHashMap.newKeySet();
        ZICAI_BLOCK_KEYWORDS = new String[]{"blind", "darkness", "levitation", "gravity", "minimap", "world_map", "worldmap", "waypoint", "radar", "cave_maps", "wm_", "\u5730\u56fe", "\u96f7\u8fbe", "\u8def\u5f84\u70b9"};
        ZICAI_BLOCK_NAMESPACES = new String[]{"irons_spellbooks", "rajswordmod"};
        ZICAI_BLOCK_IDS = new String[]{"rajswordmod:holy_fire"};
        DANDAN_ATK_MOD = UUID.fromString("6f1c9d2a-3b47-4c58-9e21-7a0d5b8c4e13");
        ftboss$zicaiCast = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
        DATA_DORA_MOTION_V1 = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
        DATA_XUELI_CAST_ACTIONS = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
        ftboss$qingyiCast = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
        DATA_QINGYI_BASIC_ACTION = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
        DATA_XUELI_BASIC = SynchedEntityData.m_135353_(EasterBossEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135042_);
    }

    public final float ftboss$reduceManorDamage(DamageSource damageSource, float f) {
        f = this.m_21161_(damageSource, f);
        return this.m_6515_(damageSource, f);
    }

    public final EasterBossEntity ftboss$manorSparrow() {
        return this.nearbySparrow();
    }

    public final float ftboss$shareManorDamage(float f, DamageSource damageSource) {
        return this.youziMinionShare(this.sparrowShare(f, damageSource), damageSource);
    }

    public final boolean ftboss$blocksManorDamage() {
        return this.blocksCombatDamage();
    }

    @Override
    public CompoundTag zicaiCastState() {
        return ((CompoundTag)this.f_19804_.m_135370_(ftboss$zicaiCast)).m_6426_();
    }

    @Override
    public void zicaiCastState(CompoundTag compoundTag) {
        this.f_19804_.m_135381_(ftboss$zicaiCast, (Object)compoundTag.m_6426_());
    }

    @Override
    public CompoundTag lemonMotionData() {
        return (CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION);
    }

    public void ftboss$nativeXuanlanCue(CompoundTag compoundTag) {
        if (compoundTag != null) {
            ((Entity)this).f_19804_.m_135381_(DATA_SKILL_CUE, (Object)compoundTag);
        }
    }

    @Override
    public CompoundTag ftboss$bodyRead() {
        return (CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION);
    }

    @Override
    public void ftboss$bodyWrite(CompoundTag compoundTag) {
        this.f_19804_.m_135381_(DATA_COMBAT_ANIMATION, (Object)compoundTag);
    }

    @Override
    public CompoundTag dandanMotionData() {
        return (CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION);
    }

    @Override
    public void dandanMotionData(CompoundTag compoundTag) {
        this.f_19804_.m_135381_(DATA_COMBAT_ANIMATION, (Object)compoundTag);
    }

    @Override
    public CompoundTag doraMotionState() {
        return (CompoundTag)this.f_19804_.m_135370_(DATA_DORA_MOTION_V1);
    }

    @Override
    public void doraMotionState(CompoundTag compoundTag) {
        this.f_19804_.m_135381_(DATA_DORA_MOTION_V1, (Object)compoundTag);
    }

    @Override
    public CompoundTag ftboss$getXueliCastActions() {
        return (CompoundTag)this.f_19804_.m_135370_(DATA_XUELI_CAST_ACTIONS);
    }

    @Override
    public void ftboss$setXueliCastActions(CompoundTag compoundTag) {
        this.f_19804_.m_135381_(DATA_XUELI_CAST_ACTIONS, (Object)compoundTag);
    }

    @Override
    public final /* synthetic */ CompoundTag ftboss$nightfallRead() {
        return (CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION);
    }

    @Override
    public final /* synthetic */ void ftboss$nightfallWrite(CompoundTag compoundTag) {
        this.f_19804_.m_135381_(DATA_COMBAT_ANIMATION, (Object)compoundTag);
    }

    public int getMeleeAttackInterval() {
        int n = ManorBossCombatRules.meleeInterval(this.getVariant());
        int n2 = this.allowsParallelMelee() ? 10 : (n > 0 ? n : 20);
        return ZicaiCombatRules.interval(CadenceServer.melee(Math.max(1, this.getChallengeDifficulty().acceleratedTicks(n2)), this), this.getVariant());
    }

    private void playMeleeImpactAction() {
        if (EasterBossEntity.armMeleeAnimationDisabled()) {
            return;
        }
        CompoundTag compoundTag = (CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION);
        if (!ManorMeleeTiming.canRestart(this.actionTicks, this.castTicks, this.isCastingLike(), compoundTag.m_128471_("melee"))) {
            return;
        }
        this.playImpactAction(ManorMeleeTiming.recoveryTicks(this.getMeleeAttackInterval(), this.isImpossibleChallenge()));
        compoundTag = ((CompoundTag)this.f_19804_.m_135370_(DATA_COMBAT_ANIMATION)).m_6426_();
        compoundTag.m_128379_("melee", true);
        this.f_19804_.m_135381_(DATA_COMBAT_ANIMATION, (Object)compoundTag);
    }

    @Override
    public CompoundTag qingyiCastState() {
        return ((CompoundTag)this.f_19804_.m_135370_(ftboss$qingyiCast)).m_6426_();
    }

    @Override
    public void qingyiCastState(CompoundTag compoundTag) {
        this.f_19804_.m_135381_(ftboss$qingyiCast, (Object)compoundTag.m_6426_());
    }

    @Override
    public CompoundTag ftboss$getQingyiBasic() {
        return (CompoundTag)this.f_19804_.m_135370_(DATA_QINGYI_BASIC_ACTION);
    }

    @Override
    public void ftboss$setQingyiBasic(CompoundTag compoundTag) {
        this.f_19804_.m_135381_(DATA_QINGYI_BASIC_ACTION, (Object)compoundTag);
    }

    @Override
    public CompoundTag ftboss$getXueliBasic() {
        return (CompoundTag)this.f_19804_.m_135370_(DATA_XUELI_BASIC);
    }

    @Override
    public void ftboss$setXueliBasic(CompoundTag compoundTag) {
        this.f_19804_.m_135381_(DATA_XUELI_BASIC, (Object)compoundTag);
    }

    @Override
    public PhantomSnapshot ftboss$phantomSnapshot() {
        return new PhantomSnapshot(this.phantomEchoTicks, this.phantomEchoSkill, this.phantomEchoTarget, this.phantomEchoX, this.phantomEchoY, this.phantomEchoZ);
    }

    @Override
    public void ftboss$phantomRestore(PhantomSnapshot phantomSnapshot) {
        this.phantomEchoTicks = phantomSnapshot.delay();
        this.phantomEchoSkill = phantomSnapshot.skill();
        this.phantomEchoTarget = phantomSnapshot.target();
        this.phantomEchoX = phantomSnapshot.horizontal();
        this.phantomEchoY = phantomSnapshot.vertical();
        this.phantomEchoZ = phantomSnapshot.depth();
    }

    @Override
    public void ftboss$phantomClear() {
        this.phantomEchoTicks = -1;
        this.phantomEchoSkill = null;
        this.phantomEchoTarget = null;
    }

    @Override
    public void ftboss$phantomReplay(ServerLevel serverLevel) {
        this.firePhantomEcho(serverLevel);
    }

    public static final boolean armMeleeAnimationDisabled() {
        return true;
    }

    public void m_6478_(MoverType moverType, Vec3 vec3) {
        super.m_6478_(moverType, CrossGuard.movement(this, moverType, vec3));
    }

    private void doraKnockback(LivingEntity livingEntity, double d, double d2) {
        Vec3 vec3 = new Vec3(livingEntity.m_20185_() - this.m_20185_(), 0.0, livingEntity.m_20189_() - this.m_20189_());
        if (vec3.m_82556_() < 1.0E-6) {
            Vec3 vec32 = this.m_20154_();
            vec3 = new Vec3(vec32.f_82479_, 0.0, vec32.f_82481_);
            if (vec3.m_82556_() < 1.0E-6) {
                vec3 = new Vec3(0.0, 0.0, 1.0);
            }
        }
        vec3 = vec3.m_82541_();
        livingEntity.m_5997_(vec3.f_82479_ * d, d2, vec3.f_82481_ * d);
        livingEntity.f_19864_ = true;
    }

    private void spearChargeHit(ServerLevel serverLevel, Vec3 vec3) {
        this.f_19804_.m_135381_(DATA_SKILL_CUE, (Object)DoraBasicCue.stamp(this.getSkillCue(), 1, this.m_9236_().m_46467_(), this.getVariant()));
        Vec3 vec32 = vec3.m_82556_() < 1.0E-6 ? this.m_20154_() : vec3.m_82541_();
        Vec3 vec33 = this.m_146892_();
        Vec3 vec34 = this.clipSkillRay(vec33, vec33.m_82549_(vec32.m_82490_(6.0)));
        int n = 0;
        for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
            if (!this.combatPlayer(livingEntity) || this.m_7307_((Entity)livingEntity) || !this.m_142582_((Entity)livingEntity) || !livingEntity.m_20191_().m_82400_(0.45).m_82390_(vec33) && livingEntity.m_20191_().m_82400_(0.45).m_82371_(vec33, vec34).isEmpty()) continue;
            livingEntity.f_19802_ = 0;
            if (!livingEntity.m_6469_(this.m_269291_().m_269333_((LivingEntity)this), 6.6666665f * 1.25f)) continue;
            livingEntity.m_5997_(vec32.f_82479_ * 1.25, 0.3, vec32.f_82481_ * 1.25);
            livingEntity.f_19864_ = true;
            livingEntity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 100, 1));
            this.doraHit(livingEntity);
            ++n;
        }
        if (n <= 0) {
            return;
        }
        this.f_19804_.m_135381_(DATA_DORA_SPEAR_TICKS, (Object)4);
        FTBossMod.LOGGER.info("[FTBoss][Dora] \u516d\u683c\u4e09\u8fde\u957f\u67aa\u7a81\u523a: \u547d\u4e2d {} \u4eba x {} \u4f24\u5bb3", (Object)n, (Object)6);
    }

    public void prepareManorEntryBuffs() {
        this.syncPlayerBuffs();
    }

    public boolean isManorMaidShielded() {
        return this.xueliMaids.shielded();
    }

    void applyPufferDebuffs(ServerLevel serverLevel, LivingEntity livingEntity) {
        ManorBalanceHooks.zicai(this, serverLevel, livingEntity);
    }

    public int m_21212_() {
        return ManorArmorBalanceRules.armor(this.getVariant(), this.isMini(), this.isGiant(), super.m_21212_());
    }

    private class PursueGoal
    extends Goal {
        private final double speed;
        private final BossApproach approach = new BossApproach();

        PursueGoal(double speed) {
            this.speed = speed;
            this.m_7021_(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean m_8036_() {
            if (EasterBossEntity.this.haleyFlying) {
                return false;
            }
            if (EasterBossEntity.this.m_21525_() || EasterBossEntity.this.movementLocked() || EasterBossEntity.this.getVariant() == 9) {
                return false;
            }
            LivingEntity t = EasterBossEntity.this.m_5448_();
            return BossTargets.valid(t);
        }

        public boolean m_8045_() {
            return this.m_8036_();
        }

        public boolean m_183429_() {
            return true;
        }

        public void m_8041_() {
            this.approach.reset((Mob)EasterBossEntity.this);
            EasterBossEntity.this.impossibleKiting.reset(EasterBossEntity.this);
        }

        public void m_8037_() {
            double retreat;
            double resume;
            double stop;
            boolean dora;
            LivingEntity t = EasterBossEntity.this.m_5448_();
            if (t == null) {
                return;
            }
            EasterBossEntity.this.m_21563_().m_24960_((Entity)t, 30.0f, 30.0f);
            if (EasterBossEntity.this.isImpossibleChallenge()) {
                if (EasterBossEntity.this.isMini() || EasterBossEntity.this.isGiant()) {
                    if (EasterBossEntity.this.m_20280_((Entity)t) > 9.0) {
                        EasterBossEntity.this.m_21573_().m_5624_((Entity)t, this.speed);
                    } else {
                        EasterBossEntity.this.m_21573_().m_26573_();
                    }
                    return;
                }
                EasterBossEntity.this.impossibleKiting.tick(EasterBossEntity.this, t, EasterBossEntity.this.allowsParallelMelee() ? 1.5 : this.speed, ManorLivingTargets.preferRanged(RANGED[EasterBossEntity.this.getVariant()], EasterBossEntity.this));
                return;
            }
            boolean ranged = ManorLivingTargets.preferRanged(RANGED[EasterBossEntity.this.getVariant()], EasterBossEntity.this);
            boolean bl = dora = EasterBossEntity.this.getVariant() == 2;
            double d = ranged ? (dora ? 6.0 : 10.0) : (stop = 2.3);
            double d2 = ranged ? (dora ? 9.0 : 14.0) : (resume = 2.8);
            double d3 = ranged && EasterBossEntity.this.doraSpearLeft <= 0 ? (dora ? 2.5 : 6.0) : (retreat = 0.0);
            double navSpeed = EasterBossEntity.this.allowsParallelMelee() ? 1.5 : (EasterBossEntity.this.getVariant() == 6 ? 0.9 : (ranged ? this.speed : 1.2));
            this.approach.tick((Mob)EasterBossEntity.this, t, navSpeed, stop, resume, retreat, dora ? 4.0 : 8.0);
        }
    }

    private static class PlayerReachMeleeGoal
    extends MeleeAttackGoal {
        private final EasterBossEntity boss;
        private int qingyiMeleeCooldown;

        PlayerReachMeleeGoal(EasterBossEntity boss, double speed, boolean followTarget) {
            super((PathfinderMob)boss, speed, followTarget);
            this.boss = boss;
            this.m_7021_(EnumSet.noneOf(Goal.Flag.class));
        }

        protected double m_6639_(LivingEntity target) {
            return ZicaiCombatRules.reachSquared(this.boss.allowsParallelMelee() ? 12.25 : 9.0, this.boss.getVariant());
        }

        protected int m_25566_() {
            return this.boss.getMeleeAttackInterval();
        }

        public void m_8037_() {
            LivingEntity target;
            if (this.qingyiMeleeCooldown > 0) {
                --this.qingyiMeleeCooldown;
            }
            if ((target = this.boss.m_5448_()) != null && !this.boss.blocksMelee()) {
                this.m_6739_(target, this.boss.m_20280_((Entity)target));
            }
            net.farmingtales.ftboss.qingyi_basic.BasicServer.validate(this.boss, this.boss.blocksMelee());
            BasicServer.validate(this.boss, this.boss.blocksMelee());
        }

        /*
         * WARNING - void declaration
         */
        protected void m_6739_(LivingEntity livingEntity, double d) {
            void target;
            void distanceSquared;
            BasicServer.prepare(this.boss, livingEntity, d, this.qingyiMeleeCooldown, this.m_25566_(), this.m_6639_(livingEntity));
            net.farmingtales.ftboss.qingyi_basic.BasicServer.prepare(this.boss, livingEntity, d, this.qingyiMeleeCooldown, this.m_25566_(), this.m_6639_(livingEntity), this.boss.blocksMelee());
            if (this.qingyiMeleeCooldown > 0 || distanceSquared > this.m_6639_((LivingEntity)target) || !this.boss.m_142582_((Entity)target)) {
                return;
            }
            this.qingyiMeleeCooldown = this.m_25566_();
            this.boss.m_6674_(InteractionHand.MAIN_HAND);
            if (this.boss.m_7327_((Entity)target) && this.boss.isImpossibleChallenge()) {
                this.boss.impossibleKiting.afterMelee(this.boss);
            }
        }

        public boolean m_8036_() {
            ManorCombatContinuity.repair(this.boss);
            if (ManorCombatLanes.rangedBlocksMelee(RANGED[this.boss.getVariant()], this.boss)) {
                return false;
            }
            if (this.boss.getVariant() == 9) {
                return false;
            }
            return !this.boss.m_21525_() && BossTargets.valid(this.boss.m_5448_());
        }

        public boolean m_8045_() {
            return this.m_8036_();
        }

        public void m_8056_() {
        }

        public void m_8041_() {
            net.farmingtales.ftboss.qingyi_basic.BasicServer.cancel(this.boss);
            BasicServer.cancel(this.boss, true);
        }
    }
}

