package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.TeaMinionEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Boss 相关 Forge 事件：每tick驱动 + 掠夺者蛋快捷召唤 + 死亡/退出清�?*/
public class BossHandler {

    public BossHandler() {
        /* ★ 2026-09-24: 「Boss 多久没打到玩家」的计时器与场地禁飞窗口要显式注册 ——
           本工程的 @Mod.EventBusSubscriber 有过"注解看着在、实际没注册"的前科(见 FTBossMod 注释),
           所以新事件类一律在已注册的类里手动挂上。挂在 BossHandler 的构造函数里是为了
           **不动 FTBossMod.java**(部署版那个文件里有别的线程加的一行 SanguineBond 注册)。 */
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
                net.farmingtales.ftboss.boss.ai.ManorChaseTracker.class);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
                net.farmingtales.ftboss.boss.ai.DoraPursuitClone.class);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            SugarQueenBoss.tick();
            MeatBoss.tick();
            // 兜底�? �?40t 全局清扫一次孤儿血�?�?BossBars)
            var server = e.getServer();
            HardcoreBossSafety.tickGrace(server);
            if (server != null && server.getTickCount() % 40 == 0) {
                BossBars.sweep(server);
                net.farmingtales.ftboss.sound.BossMusic.sweep(server);   // 2026-09-11: BGM ����ֹͣ
                /* 2026-09-13 晚: 重开游戏会把 ManorFight 的静态内存态全丢掉 —— 场上还站着
                   庄园 Boss 却没进度血条、也不轮换。这里就地把它捡回来。 */
                ManorFight.recoverNear(server);
            }
            // 按场上人数缩�?2026-09-11 按用户要�?: �?20t 重算血量上限与伤害倍率
            if (server != null && server.getTickCount() % 20 == 0) {
                BossBars.forEach(server, le -> {
                    net.farmingtales.ftboss.boss.ai.BossScaling.tick(le);
                    net.farmingtales.ftboss.boss.ai.BossEmpower.tick(le);   // 2026-09-11: 强化 4 �?Boss
                });
            }
            // 兜底�? �?10t �?谁打�?的仇恨按�?Boss 头上(用户要求: 攻击攻击他的生物, 而不是只锁最近玩�?
            if (server != null && server.getTickCount() % 10 == 0) {
                net.farmingtales.ftboss.boss.ai.BossAggro.tick(server);
                /* ★ v114 按用户反馈「boss 索敌会锁定创造模式玩家」: 兜底清扫 ——
                   任何 Boss 当前目标若是创造/旁观玩家就立刻丢掉(切模式、原版索敌、仇恨回击都盖得住)。 */
                net.farmingtales.ftboss.boss.BossBars.forEach(server,
                        net.farmingtales.ftboss.boss.ai.BossTargets::dropInvalidTarget);
            }
            // 兜底�? �?200t 清一�?无主屠夫"(旧存档残�?退出未�?区块加载顺序漏网)
            if (server != null && server.getTickCount() % 200 == 0) {
                int n = MeatBoss.purgeStrays(server);
                if (n > 0) FTBossMod.LOGGER.info("[FTBoss] 定期清理无主屠夫实体 {} 只", n);
            }
        }
    }

    /**
     /* 【感染�?2026-09-11 按用户要�?: 玩家**攻击敌人**�? 受到"感染层数"点伤害�?
     * 近战/弓箭/法术只要 source 是玩家就�? 打自己不算�?
     */
    @SubscribeEvent
    public void onPlayerAttackGrubCurse(LivingHurtEvent e) {
        if (e.getEntity().level().isClientSide) return;
        net.minecraft.world.entity.Entity source = sourceCaster(e.getSource());
        if (!(source instanceof net.minecraft.world.entity.LivingEntity attacker)) return;
        if (e.getEntity() == attacker) return;
        var inst = attacker.getEffect(net.farmingtales.ftboss.effect.FTEffects.INFECTION.get());
        if (inst == null) return;
        float dmg = inst.getAmplifier() + 1;
        attacker.invulnerableTime = 0;
        attacker.hurt(attacker.damageSources().magic(), dmg);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBossHurtRetaliate(LivingDamageEvent e) {
        // 2026-09-11 按用户要求: 本模组 Boss 被**任何生物**攻击时, 记住攻击者并回头打它
        if (e.getEntity().level().isClientSide) return;
        /* v115(排查报告 H 顺带): 改用 sourceCaster() 取"真正动手的实体" —— 旧写法对法术/爆炸伤害
           (causing 槽常为 null) 会把"一颗火球"当成攻击者, 于是 Boss 追着投射物记仇、记错对象。 */
        net.farmingtales.ftboss.boss.ai.BossAggro.record(e.getEntity(), sourceCaster(e.getSource()),
                e.getAmount(), e.getEntity().level().getGameTime());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBossChangeTarget(LivingChangeTargetEvent event) {
        if (net.farmingtales.ftboss.boss.ai.DoraPursuitClone.redirectTarget(event)) return;
        if (!(event.getEntity() instanceof net.minecraft.world.entity.Mob boss) || boss.isNoAi()) return;
        var target = BossAggro.highest(boss);
        if (target != null) event.setNewTarget(target);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onConsumeForThreat(LivingEntityUseItemEvent.Finish event) {
        BossAggro.consume(event.getEntity(), event.getItem());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPotionForThreat(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || event.loadedFromDisk()) return;
        /* ★ v172 按用户要求「给全部非庄园 boss 做一个触发 boss 战的成就」：
           非庄园 Boss **进入世界**的那一刻就是"战斗被触发"。
           放在这里而不是逐个召唤道具里调用 —— 八只 Boss 的召唤路径各不相同
           （有的是道具直接 addFreshEntity，糖霜女皇/屠夫走各自的 Boss 大脑），
           而"实体入世界"是它们唯一的公共时刻。
           loadedFromDisk() 已在上面挡掉，所以读档重建的旧 Boss 不会补发进度。 */
        net.farmingtales.ftboss.boss.BossAdvancements.onBossSummoned(event.getEntity());
        if (event.getEntity() instanceof net.minecraft.world.entity.projectile.ThrownPotion potion
                && potion.getOwner() instanceof net.minecraft.world.entity.LivingEntity owner) {
            BossAggro.consume(owner, potion.getItem());
        }
    }

    @SubscribeEvent
    public void onThreatEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide) BossAggro.forgetEntity(event.getEntity().getUUID());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onThreatDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide) BossAggro.forgetEntity(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onThreatServerStopped(ServerStoppedEvent event) {
        BossAggro.clear();
        HardcoreBossSafety.clearGrace();
    }

    /**
     /* 石像外观切换: 手持**对应 Boss 的召唤物**左键石像, 在「上色皮�?�?白膜」之间切换�?
     * <p>石像放下时默认就�?*上色**(每个 Boss 一张专属贴�? �?tools/model3d/gen_statue_skins.cjs);
     * 再左键一次会退回白�?tint=0), 第三次又能切回上�?—�?颜色不再是运行时乘色, 而是换模型�?
     * 切换的那一下会取消事件, 免得把石像敲掉�?
     */
    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (e.getLevel().isClientSide) return;
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        var level = e.getLevel();
        var pos = e.getPos();
        var st = level.getBlockState(pos);
        if (!(st.getBlock() instanceof net.farmingtales.ftboss.block.CandyStatueBlock statue)) return;
        if (statue.ownTint() <= 0) return;   // 通用糖果石像没有"Boss 配色", 不参与切�?
        int tint = tintFor(statue, e.getItemStack());
        if (tint <= 0) {
            // 用了召唤物但不对�? 明确提示, 避免玩家以为是没生效
            if (isSummonItem(e.getItemStack())) {
                sp.displayClientMessage(net.minecraft.network.chat.Component.literal("§c这个召唤物和这座石像不匹配"), true);
                e.setCanceled(true);
            }
            return;
        }
        // 默认(white=false)�?Boss 真实外观 �?左键切到白膜; 再左键切回来
        boolean toWhite = !net.farmingtales.ftboss.block.CandyStatueBlock.isWhite(st)
                && !net.farmingtales.ftboss.block.CandyStatueBlock.isGold(st);
        var half = st.getValue(net.minecraft.world.level.block.DoublePlantBlock.HALF);
        var other = half == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER
                ? pos.above() : pos.below();
        java.util.function.UnaryOperator<net.minecraft.world.level.block.state.BlockState> apply = s ->
                s.setValue(net.farmingtales.ftboss.block.CandyStatueBlock.WHITE, toWhite)
                 .setValue(net.farmingtales.ftboss.block.CandyStatueBlock.GOLD, false)
                 .setValue(net.farmingtales.ftboss.block.CandyStatueBlock.TINT, toWhite ? 0 : tint);
        level.setBlock(pos, apply.apply(st), 3);
        var os = level.getBlockState(other);
        if (os.getBlock() == st.getBlock()) {
            level.setBlock(other, apply.apply(os), 3);
        }
        e.setCanceled(true);   // 切换外观时不要开始挖�?
        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.9f, toWhite ? 0.9f : 1.3f);
        sp.displayClientMessage(net.minecraft.network.chat.Component.literal(
                toWhite ? "§7石像已恢复白膜（再左键一次可重新上色）" : "§a石像已按对应 Boss 的配色上色"), true);
    }

    /** 召唤�?-> 石像档位(必须"对应 Boss"才成�? 档位直接取石像自己的 {@code ownTint}) */
    private static int tintFor(net.farmingtales.ftboss.block.CandyStatueBlock statue,
                               net.minecraft.world.item.ItemStack held) {
        int own = statue.ownTint();
        net.minecraft.world.item.Item want = switch (own) {
            case 1 -> net.farmingtales.ftboss.item.FTBossItems.SUGAR_QUEEN_CRYSTAL.get();
            case 2 -> net.farmingtales.ftboss.item.FTBossItems.MEAT_HEART.get();
            case 3 -> net.farmingtales.ftboss.item.FTBossItems.TEA_SEED.get();
            case 4 -> net.farmingtales.ftboss.item.FTBossItems.WINE_CHARM.get();
            case 5 -> net.farmingtales.ftboss.item.FTBossItems.RAT_BAIT.get();
            case 6 -> net.farmingtales.ftboss.item.FTBossItems.DEFECT_CORE.get();      // 2026-09-12 �? 机器人石�?
            case 7 -> net.farmingtales.ftboss.item.FTBossItems.COMPOST_SUMMON.get();   // 答辩超人石像
            case 8 -> net.farmingtales.ftboss.item.FTBossItems.FORBIDDEN_APPLE.get();  // 2026-09-12: 苹果爵士石像
            case 9 -> net.farmingtales.ftboss.item.FTBossItems.TIDE_BANQUET.get();
            case 10 -> net.farmingtales.ftboss.item.FTBossItems.BENGER_WHISTLE.get();
            default -> null;
        };
        return want != null && held.is(want) ? own : 0;
    }

    /** 是否是本模组的五�?Boss 召唤�?*/
    private static boolean isSummonItem(net.minecraft.world.item.ItemStack held) {
        if (held.is(net.farmingtales.ftboss.item.FTBossItems.BENGER_WHISTLE.get())) return true;
        if (held.is(net.farmingtales.ftboss.item.FTBossItems.TIDE_BANQUET.get())) return true;
        return held.is(net.farmingtales.ftboss.item.FTBossItems.SUGAR_QUEEN_CRYSTAL.get())
                || held.is(net.farmingtales.ftboss.item.FTBossItems.MEAT_HEART.get())
                || held.is(net.farmingtales.ftboss.item.FTBossItems.TEA_SEED.get())
                || held.is(net.farmingtales.ftboss.item.FTBossItems.WINE_CHARM.get())
                || held.is(net.farmingtales.ftboss.item.FTBossItems.RAT_BAIT.get())
                || held.is(net.farmingtales.ftboss.item.FTBossItems.DEFECT_CORE.get())      // 2026-09-12: 机器人召唤物
                || held.is(net.farmingtales.ftboss.item.FTBossItems.COMPOST_SUMMON.get())   // 答辩超人召唤�?
                || held.is(net.farmingtales.ftboss.item.FTBossItems.FORBIDDEN_APPLE.get());  // 2026-09-12: 苹果爵士召唤�?
    }

    /** 鼠群共享血�? 小老鼠受击不扣自身血, 转为扣大老鼠公共血�? 池未归零双方不死 */
    @SubscribeEvent
    public void onRatSwarmHurt(LivingHurtEvent e) {
        if (e.getEntity().level().isClientSide) return;
        if (!(e.getEntity() instanceof RatBossEntity victim)) return;
        if (net.farmingtales.ftboss.boss.ai.RatSwarmEncounter.isDeputy(victim)) return;
        if (!victim.isMinion()) return;
        var lemon = net.farmingtales.ftboss.boss.ai.LemonSacrifice.ratLeader(victim);
        if (lemon != null) {
            e.setCanceled(true);
            victim.setHealth(victim.getMaxHealth());
            lemon.hurt(e.getSource(), e.getAmount() * MINION_DAMAGE_TAKEN);
            return;
        }
        RatBossEntity leader = null;
        for (RatBossEntity r : victim.level().getEntitiesOfClass(RatBossEntity.class,
                victim.getBoundingBox().inflate(160))) {
            if (!r.isMinion() && r.isAlive()) { leader = r; break; }
        }
        if (leader == null) return; // �޴�����: С������������
        e.setCanceled(true);                 // 小老鼠不死
        victim.setHealth(victim.getMaxHealth());
        // 2026-09-11 按用户要�?小鼠受到的伤害减�?: 进公共血池前�?6 �?
        leader.hurt(e.getSource(), e.getAmount() * MINION_DAMAGE_TAKEN);
    }

    /**
     /* 友军伤害免疫(2026-09-11 按用户要�?去除 boss 对自己的友伤")�?
     * <p>本模组的 Boss 与召唤物之间**互相免疫**: 酒桶王炮弹把自己/小酒桶炸到、火焰弹误伤�?
     * 两个 Boss �?AoE 互相打等, 全部取消。玩家打 Boss 不受影响(直接 return)�?
     * <p>注意: 小老鼠的伤害是"转发给大老鼠"�?{@link #onRatSwarmHurt}), 那条链路里攻击者是**玩家**,
     * 所以也会在这里直接放行, 不会被误伤免疫挡住�?
     */
    @SubscribeEvent
    public void onAllyFriendlyFire(LivingHurtEvent e) {
        if (e.getEntity().level().isClientSide) return;
        /* ★ v115 按用户要求「boss 的铁魔法别友伤自己和队友」(排查报告 H):
           ① 阵营判定改用 {@link BossAggro#isBossSide}(按 ftboss 命名空间) —— 旧白名单**漏了
              EasterBossEntity(庄园十人)**、酒桶王分身、苹果系、雪黎的飞刀/汤锅等, 于是青衣的
              铁魔法闪电打到自家队友时两边都判"不是自己人"直接放行(日志: 友伤免疫钩子从未触发);
           ② 施法者改成"双槽 + 递归"取(见 sourceCaster): 铁魔法法术的 causing/direct 两槽都是施法者,
              但 AoE 类有的只填一个, 还有的走 IMagicSummon 召唤主 -> 只取 getEntity() 会漏。 */
        if (!net.farmingtales.ftboss.boss.ai.BossAggro.isBossSide(e.getEntity())) return;   // 受害者不是自家 -> 不管(玩家打 Boss 走这条)
        net.minecraft.world.entity.Entity attacker = sourceCaster(e.getSource());
        if (attacker == null || attacker instanceof net.minecraft.world.entity.player.Player) return;
        if (attacker == e.getEntity()) { e.setCanceled(true); return; }   // 自己打自己
        if (!net.farmingtales.ftboss.boss.ai.BossAggro.isBossSide(attacker)) return;
        e.setCanceled(true);
    }

    /**
     * 从伤害源里尽量取出"真正动手的实体"(v115, 排查报告 H 给的健壮写法)。
     * <p>顺序: {@code getEntity()} -> {@code getDirectEntity()} -> 投射物 owner(递归 4 层)
     * -> 铁魔法 {@code IMagicSummon.getSummoner()}(反射, 缺 mod 安全)。取不到就返回 null(=放行)。
     */
    private static net.minecraft.world.entity.Entity sourceCaster(
            net.minecraft.world.damagesource.DamageSource src) {
        if (src == null) return null;
        net.minecraft.world.entity.Entity e = src.getEntity();
        if (e == null) e = src.getDirectEntity();
        return rootOwner(e, 0);
    }

    private static net.minecraft.world.entity.Entity rootOwner(net.minecraft.world.entity.Entity e, int depth) {
        if (e == null || depth > 4) return e;
        if (e instanceof net.minecraft.world.entity.projectile.Projectile p && p.getOwner() != null) {
            return rootOwner(p.getOwner(), depth + 1);
        }
        try {
            var m = e.getClass().getMethod("getSummoner");     // 铁魔法 IMagicSummon
            Object o = m.invoke(e);
            if (o instanceof net.minecraft.world.entity.Entity owner && owner != e) {
                return rootOwner(owner, depth + 1);
            }
        } catch (Throwable ignored) {
        }
        return e;
    }

    /** 是否属于"本模组 Boss 阵营" —— v115 起统一走 {@link BossAggro#isBossSide}(按 ftboss 命名空间判定):
     *  一次覆盖 8 位主 Boss + 庄园十人 + 全部召唤物与投射物(旧白名单漏了庄园十人等一大票)。 */
    private static boolean isBossSide(net.minecraft.world.entity.Entity e) {
        return net.farmingtales.ftboss.boss.ai.BossAggro.isBossSide(e);
    }

    /** 小老鼠进公共血池前的伤害折�?2026-09-11 按用户要�?小鼠受到的伤害减�?) */
    private static final float MINION_DAMAGE_TAKEN = 0.6F;

    /** 鼠群随机 debuff �?懒加�? 原版 + armamentarium, 取不到的自动跳过) */
    private static MobEffect[] ratDebuffPool;

    private static MobEffect[] ratDebuffPool() {
        if (ratDebuffPool != null) return ratDebuffPool;
        java.util.List<MobEffect> pool = new java.util.ArrayList<>();
        pool.add(MobEffects.POISON);
        pool.add(MobEffects.WEAKNESS);
        pool.add(MobEffects.MOVEMENT_SLOWDOWN);
        pool.add(MobEffects.DIG_SLOWDOWN);
        pool.add(MobEffects.HUNGER);
        /* 2026-09-12 v37 按用户要�? 对玩家的减益不允�?*失明 / 黑暗 / 漂浮**, 已移除三�?*/
        pool.add(MobEffects.UNLUCK);
        pool.add(MobEffects.WITHER);
        final String[][] ARM = {
                {"vulnerable"}, {"fatigue"}, {"sluggish"}, {"fragile"}, {"impotent"}, {"lethargy"},
                {"pure_venom"}, {"blood_loss"}, {"clipped"}, {"damnified"}, {"sedated"}, {"restrained"},
                {"malnourished"}, {"unsatiated"}, {"hypothermia"}, {"vulnerable"}
        };
        for (String[] a : ARM) {
            MobEffect fx = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS
                    .getValue(new net.minecraft.resources.ResourceLocation("armamentarium", a[0]));
            if (fx != null) pool.add(fx);
        }
        ratDebuffPool = pool.toArray(new MobEffect[0]);
        FTBossMod.LOGGER.info("[FTBoss] 鼠群随机debuff池就绪: {} 种", ratDebuffPool.length);
        return ratDebuffPool;
    }

    /** 命中玩家时随机挂一�?debuff(2026-09-11 按用户要�? */
    public static void applyRandomRatDebuff(ServerPlayer p) {
        MobEffect[] pool = ratDebuffPool();
        if (pool.length == 0) return;
        MobEffect fx = pool[p.getRandom().nextInt(pool.length)];
        int amp = p.getRandom().nextInt(2);                 // I~II �?
        int dur = 140 + p.getRandom().nextInt(120);         // 7~13 �?
        p.addEffect(new MobEffectInstance(fx, dur, amp, false, true, true));
        /* ★ v182 按用户要求「优化 boss 文本, 非必要减少文字输出」:
           本来**每次命中**都往行动栏推一句"鼠群撕咬 → XX N 层"。鼠群本体+一群小老鼠一秒能打好几下,
           这句会把行动栏彻底占满; 而它承载的信息**玩家本来就看得到** ——
           上面 addEffect 的第三个参数是"显示图标", 右上角会实时列出被挂上的效果与层数。
           故整条文本移除, 只保留加效果本身(机制不变)。 */
    }

    @SubscribeEvent
    public void onBossHitPlayer(LivingHurtEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && !p.level().isClientSide) {
            var src = e.getSource().getEntity();
            // 2026-09-11 �? 记下"最近威�?, 供连死怜悯归因(被击飞摔�?环境死也能算在这�?Boss 头上)
            net.farmingtales.ftboss.boss.ai.BossMercy.noteThreat(p, src != null ? src : e.getSource().getDirectEntity(),
                    p.level().getGameTime());
            if (SugarQueenBoss.isBoss(src)) {
                p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 1));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
            } else if (MeatBoss.isBoss(src)) {
                // 屠夫命中: 玩家全套debuff + 屠夫吸血回血(炎魔�?; 双持轮换时可能放大伤�?荣耀巨斧)
                e.setAmount(MeatBoss.onMeatHitPlayer(p, e.getAmount()));
            } else if (src instanceof RatBossEntity rat && net.farmingtales.ftboss.boss.ai.RatSwarmEncounter.isDeputy(rat)) {
                net.farmingtales.ftboss.boss.ai.RatSwarmEncounter.applyDeputyDebuff(p);
            } else if (src instanceof RatBossEntity rat && rat.isMinion()) {
                // 2026-09-11 按用户要�? 鼠群(本体与小老鼠都算)**每次击中玩家挂一种随�?debuff** —�?全种类池,
                // 原版负面效果 + armamentarium 的各种减�?易伤/疲劳/迟缓/脆弱/无力/倦�?纯净毒液/流血/断筋/�?
                applyRandomRatDebuff(p);
            }
            /* 2026-09-12 晚按用户要求: **取消"命中即叠�?的「屎？！！�?* —�?
               现在改由答辩超人自己按冷�?*概率施放一�?*(�?CompostBossEntity �?curseCd �?,
               召唤物也不再刷新�? 命中频率大幅下降�?*/
        }
    }

    /**
     * 【Boss 负面效果免疫】—— ★ v176 按用户要求「允许 boss 吃到 debuff」**整体关闭**。
     *
     * <p>历史: 这里原本对全部 Boss(女王/屠夫/沙耶/酒桶王/鼠群/茶芽小妖/故障机器人/
     * 答辩超人/苹果爵士及其分身/边牧犬王)一律 {@code DENY} 掉一切非增益效果。
     *
     * <p>现在改成**直接放行**: 不再调用 {@code setResult(DENY)}, Boss 会正常吃到
     * 玩家施加的减益(虚弱/中毒/凋零/缓慢/破甲等, 以及 armamentarium 那一套)。
     *
     * <p>★ 用户同时要求保留两个例外 —— 「除了 armamentarium 的泰隆真伤和血王子的百分比伤害」。
     * 那两条**不是效果、是伤害类型**, 拦它们的地方在
     * {@link net.farmingtales.ftboss.boss.ai.BossDamageGate#isImmune}(按伤害类型 id 免疫
     * {@code doom/assassination/...} 与 {@code hemorrhage/blood_loss/...}),
     * 与本方法无关, 所以本次改动**不影响**那两个例外。
     *
     * <p>保留开关便于回退: 设回 true 即恢复「Boss 免疫一切负面效果」的旧行为。
     */
    public static final boolean IMMUNE_TO_DEBUFFS = false;

    @SubscribeEvent
    public void onEffectApplicable(net.minecraftforge.event.entity.living.MobEffectEvent.Applicable e) {
        /* v176: 免疫已按用户要求关闭 —— 见 IMMUNE_TO_DEBUFFS 注释。
           下面整段短路, 不再有任何 DENY 分支(旧代码保留, 已不会被走到)。 */
        if (!IMMUNE_TO_DEBUFFS) return;
        if (e.getEffectInstance() == null
                || e.getEffectInstance().getEffect() == null
                || e.getEffectInstance().getEffect().isBeneficial()) {
            return;
        }
        if (e.getEntity() instanceof SugarQueenEntity
                || e.getEntity() instanceof MeatMountainEntity
                || e.getEntity() instanceof TeaTreeBossEntity
                || e.getEntity() instanceof WineBarrelBossEntity
                || e.getEntity() instanceof RatBossEntity
                || e.getEntity() instanceof TeaMinionEntity
                || e.getEntity() instanceof net.farmingtales.ftboss.entity.DefectCoreEntity   // �?6 �?Boss
                || e.getEntity() instanceof net.farmingtales.ftboss.entity.CompostBossEntity   // �?7 �?Boss(答辩超人)
                || e.getEntity() instanceof net.farmingtales.ftboss.entity.AppleBossEntity    // �?8 �?Boss(苹果爵士, 2026-09-12)
                || e.getEntity() instanceof net.farmingtales.ftboss.entity.AppleCloneEntity   // 它的分身也免疫负面效�?
                || e.getEntity() instanceof net.farmingtales.ftboss.entity.BengerEntity) {    // v127 第 9 位 Boss(边牧犬王 benger)
            e.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            // 2026-09-10 按用户要�? 明确屏蔽 armamentarium「血王子」系列的流血 —�?打日志便于核�?
            MobEffect fx = e.getEffectInstance().getEffect();
            var key = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getKey(fx);
            if (key != null && "armamentarium".equals(key.getNamespace())
                    && !armamentariumBlockLogged.contains(key.getPath())) {
                armamentariumBlockLogged.add(key.getPath());
                FTBossMod.LOGGER.info("[FTBoss] 已屏�?armamentarium 效果(Boss 免疫): {}", key);
            }
        }
    }

    /** �Ѽǹ���־�� armamentarium Ч��(����ˢ��) */
    private static final java.util.Set<String> armamentariumBlockLogged = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** 极限模式庄园保护期：Boss 战已中止后，继续拦截 3 秒内的伤害。 */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onHardcoreManorGraceDamage(LivingHurtEvent event) {
        HardcoreBossSafety.cancelManorGraceDamage(event);
    }

    /** 极限模式：死亡结算前中止归属 Boss；庄园战会取消死亡并给予短暂无敌。 */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onHardcorePlayerFatal(LivingDeathEvent event) {
        HardcoreBossSafety.beforePlayerDeath(event);
    }

    /** 2026-09-11 按用户要�? 同一玩家在同一 Boss 下连续死�?5 �?-> �?Boss 离场(无掉�?无经�? */
    @SubscribeEvent
    public void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && !sp.level().isClientSide) {
            // �?死亡当下就把该玩家从**所�?Boss 血�?*里摘�?会发 REMOVE �? —�?
            //   否则�?ServerPlayer 实例会残留在血条里, 客户端血条会"卡在天上"不消�?实测问题)
            BossBars.forgetPlayer(sp);
            if (!sp.server.isHardcore()) net.farmingtales.ftboss.boss.ai.BossMercy.onPlayerDeath(sp);
        }
    }

    @SubscribeEvent
    public void onBossDeath(LivingDeathEvent e) {
        if (e.getEntity() instanceof net.minecraft.world.entity.Mob m && !e.getEntity().level().isClientSide) {
            // ===== "胜利清零"必须�?*确认真的死了**之后 =====
            // 2026-09-11 �? 原来在方法开头就清零连死计数, 但茶树古树是"二命"(tryRevive 会取消死�?,
            // 于是"第一次打死它"也被当成胜利 �?计数被提前清�? 玩家永远攒不�?5 次�?
            if (m instanceof TeaTreeBossEntity tt) {
                // 茶树古树: 二命优先, 真死则发掉落
                if (tt.tryRevive()) {
                    e.setCanceled(true);
                    return;                                  // 没真�? 不清�?
                }
                net.farmingtales.ftboss.boss.ai.BossMercy.onBossDefeated(m);
                tt.dropRewards();
                return;
            }
            if (SugarQueenBoss.tryPopTotem(m)) {
                e.setCanceled(true);                         // 焦糖护符生效: 拦截死亡进入终局形�?
                return;                                      // 没真�? 不清�?
            } else if (MeatBoss.tryPopTotem(m)) {
                e.setCanceled(true);                         // 屠夫回魂: 拦截死亡进入终局
                return;                                      // 没真�? 不清�?
            }
            net.farmingtales.ftboss.boss.ai.BossMercy.onBossDefeated(m);
            SugarQueenBoss.onBossDeath(m);
            MeatBoss.onBossDeath(m);
        }
    }

    /**
     /* 兜底清理: 世界里已存在�?非玩家施�?�?Iron 幽灵�?{@code SpectralHammer})�?
     * 其实体内部是 private Player owner, 怪物施放�?owner=null, 挖掘方块时会以空玩家构�?
     * BlockEvent.BreakEvent �?NPE 崩服。用反射�?owner, 为空则直接移�? 读不到则保守跳过�?
     */
    @SubscribeEvent
    public void onEntityJoin(net.minecraftforge.event.entity.EntityJoinLevelEvent e) {
        net.minecraft.world.entity.Entity ent = e.getEntity();
        // 客户端诊�? 确认客户端到底有没有收到屠夫实体(定位"召唤成功却看不到模型")
        if (e.getLevel().isClientSide()) {
            if (ent instanceof MeatMountainEntity m) {
                FTBossMod.LOGGER.info("[FTBoss][屠夫诊断] 客户端收到屠夫实体 id={} 位置=({}, {}, {}) 血量={}/{}",
                        m.getId(), String.format("%.1f", m.getX()), String.format("%.1f", m.getY()),
                        String.format("%.1f", m.getZ()), m.getHealth(), m.getMaxHealth());
            }
            return;
        }
        // 无主屠夫: 屠夫�?MeatBoss 静态大脑驱�? 凡是不被登记的实体都是残�?旧存档遗�?退出未�? -> 直接清掉,
        // 否则玩家会看�?召唤说已在战斗中, 场上却有个打不动的幽灵屠�?
        // 2026-09-11: 玩家额外召唤�?复制�?是合法实�?cloneButcher), 不能�?
        if (ent instanceof MeatMountainEntity m && !MeatBoss.isBoss(m) && !m.isCloneButcher()) {
            m.hideBossBar();
            m.discard();
            FTBossMod.LOGGER.info("[FTBoss] 已清理无主屠夫实体 位置=({}, {}, {}) 血量={}/{}",
                    String.format("%.1f", m.getX()), String.format("%.1f", m.getY()),
                    String.format("%.1f", m.getZ()), m.getHealth(), m.getMaxHealth());
            return;
        }
        if (!ent.getClass().getName().endsWith("spectral_hammer.SpectralHammer")) return;
        try {
            java.lang.reflect.Field f = ent.getClass().getDeclaredField("owner");
            f.setAccessible(true);
            Object owner = f.get(ent);
            if (owner == null) {
                ent.discard();
                FTBossMod.LOGGER.info("[FTBoss] 已清理无主幽灵锤（防止残留）");
            }
        } catch (Throwable ignored) {
            // 字段名变化等异常: 保守处理, 不动�?
        }
    }

    /** 2026-09-13 晚: 玩家重进存档 -> 把庄园进度血条重新挂上去(否则顶上的进度条会消失) */
    @SubscribeEvent
    public void onLoggedIn(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            net.farmingtales.ftboss.boss.ManorFight.reattach(sp);
        }
    }
    @SubscribeEvent
    public void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            BossBars.forgetPlayer(sp);
            if (SugarQueenBoss.isActive() && sp.getUUID().equals(SugarQueenBoss.ownerIdOf())) {
                SugarQueenBoss.cleanup();
                FTBossMod.LOGGER.info("[FTBoss] 主人退出，糖霜女皇消散");
            }
            if (MeatBoss.isActive() && sp.getUUID().equals(MeatBoss.ownerIdOf())) {
                MeatBoss.cleanup();
                FTBossMod.LOGGER.info("[FTBoss] 主人退出，屠夫消散");
            }
        }
    }

    /** 兜底�? 实体离开世界(区块卸载/换维�?被移�?时立即清掉其血�?
     /*  2026-09-12 �?血条卡�?: 原来只处理了屠夫/鼠群/茶树/酒桶 —�?
     *  **答辩超人(�?7)与苹果爵�?�?8)漏了**, 区块卸载后血条永远留在屏幕上�?*/
    @SubscribeEvent
    public void onEntityLeaveLevel(net.minecraftforge.event.entity.EntityLeaveLevelEvent e) {
        if (e.getEntity() instanceof MeatMountainEntity m) {
            BossBars.untrack(m, m.getBossBar());
        } else if (e.getEntity() instanceof RatBossEntity r) {
            BossBars.untrack(r, r.getBossBar());
        } else if (e.getEntity() instanceof TeaTreeBossEntity t) {
            BossBars.untrack(t, t.getBossBar());
        } else if (e.getEntity() instanceof WineBarrelBossEntity w) {
            BossBars.untrack(w, w.getBossBar());
        } else if (e.getEntity() instanceof net.farmingtales.ftboss.entity.CompostBossEntity c) {
            BossBars.untrack(c, c.getBossBar());                 // 炫彩答辩超人
        } else if (e.getEntity() instanceof net.farmingtales.ftboss.entity.AppleBossEntity a) {
            BossBars.untrack(a, a.getBossBar());                 // 苹果爵士·蜜脆
        } else if (e.getEntity() instanceof net.farmingtales.ftboss.entity.DefectCoreEntity d) {
            BossBars.untrack(d, d.getBossBar());                 // 故障机器�?有访问器, 之前也没进名�?
        }
    }

    /** 兜底�? 玩家死亡重生(旧实�? -> 从所有血条中摘除, 避免残留导致血条卡�?*/
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone e) {
        if (e.getOriginal() instanceof ServerPlayer old) {
            BossBars.forgetPlayer(old);
        }
    }

    /* ==================== 2026-09-12 按用户要求新�? 禁止飞行 + 苹果阵营友伤免疫 ==================== */

    /** 全部 Boss 的实体类�?Boss 战期间禁止玩家飞行用) */
    private static boolean isBossEntity(net.minecraft.world.entity.Entity e) {
        if (e instanceof net.farmingtales.ftboss.entity.TideFishBossEntity) return true;
        return e instanceof SugarQueenEntity
                || e instanceof MeatMountainEntity
                || e instanceof TeaTreeBossEntity
                || e instanceof WineBarrelBossEntity
                || e instanceof RatBossEntity
                || e instanceof net.farmingtales.ftboss.entity.DefectCoreEntity
                || e instanceof net.farmingtales.ftboss.entity.CompostBossEntity
                || e instanceof net.farmingtales.ftboss.entity.AppleBossEntity
                /* 2026-09-12 v29 按用户要�?禁飞": 甜苹果庄园的 9 �?含终章火柴人/迷你分身)也算 Boss */
                || e instanceof net.farmingtales.ftboss.entity.EasterBossEntity;
    }

    /** 每个玩家�?禁止飞行"冷却(避免�?tick 都劈�? */
    private static final java.util.Map<java.util.UUID, Integer> FLY_BAN_CD = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     /* 【禁止飞行�?2026-09-12 按用户要�?: 只要 64 格内有任一 Boss, 玩家一旦起飞就会被
     * �?直接掐断飞行(创造飞�?/ 鞘翅), �?挨一次真实闪�? �?挂上【筋箭断裂】�?
     *
     * <p>关于【筋箭断裂�? 参考包里没有这�?id(TnS 只有 bleeding), 所以这�?*按候�?id 逐个查找**,
     * 找到哪个用哪个并打日�? 全都没找到就退回原�?*虚弱**(并对玩家显示提示), 功能�?禁飞"靠①保证�?
     */
    /* ==================== 2026-09-12 v38 按用户要�? 允许玩家飞行 + 给玩家信标增�?==================== */

    /** 禁飞总开�? **已按用户要求关闭**(true 时才恢复"掐断飞行 + 劈雷 + 筋箭断裂") */
    public static final boolean FLY_BAN_ENABLED = false;

    /** Boss 战期间给玩家�?信标增益" —�?�?Boss 自带的那六项完全一�?*/
    public static final MobEffect[] BEACON_EFFECTS = {
            MobEffects.MOVEMENT_SPEED,      // 速度 II
            MobEffects.DIG_SPEED,           // 急迫 II
            MobEffects.DAMAGE_RESISTANCE,   // 抗性提�?II
            MobEffects.JUMP,                // 跳跃提升 II
            MobEffects.DAMAGE_BOOST,        // 力量 II
            MobEffects.REGENERATION,        // 生命恢复 II
    };

    /**
     * 64 格内有没有【隐藏 Boss（甜苹果庄园的彩蛋十人）】。
     *
     * <p>2026-09-18 按用户反馈「常规 boss 战不应该出现隐藏 boss 战的信标加成」新增。
     * 判据只认 {@link net.farmingtales.ftboss.entity.EasterBossEntity}（迷你分身不算），
     * 与 {@code SparrowSupport} v106 那次修复同一个口径 —— 麻雀当初也是"对任意本模组 Boss 都成立"，
     * 后来被收窄到只认真・彩蛋 Boss。信标增益是同一类遗留，这次一并收窄。
     */
    private static boolean hiddenBossNear(net.minecraft.server.level.ServerLevel sl, ServerPlayer sp) {
        for (net.farmingtales.ftboss.entity.EasterBossEntity boss : sl.getEntitiesOfClass(
                net.farmingtales.ftboss.entity.EasterBossEntity.class, sp.getBoundingBox().inflate(64.0))) {
            if (!boss.isMini()) return true;
        }
        return false;
    }

    /**
     * 给玩家补上信标增益（六项 II 级，12 秒一续，不覆盖玩家自己更好的药水）。
     *
     * <p><b>2026-09-18 起只在隐藏 Boss 战里发</b> —— 调用点加了 {@link #hiddenBossNear} 判定。
     */
    private void applyBeaconBuffs(ServerPlayer sp) {
        net.farmingtales.ftboss.boss.ai.ManorIntroPlayerBuffs.applyFieldBuffs(sp);
    }

    /* ============ 2026-09-19 v159: 死亡瞬间停止进食(solcarrot「死吃」崩溃的护栏) ============ */

    /**
     * 【实测崩溃】2026-09-19 08:08:18 —— 玩家被潮宴鱼王打死的那一瞬间, 嘴里那口**金苹果**
     * 还在死亡动画里继续数, 1.35 秒后吃完 -> 触发 {@code LivingEntityUseItemEvent.Finish};
     * solcarrot(生活调味料 1.15.1) 的 {@code FoodTracker.onFoodEaten} 去取"食物清单"能力,
     * 而该能力对**死人**已经失效 -> 抛 {@code FoodList$FoodListNotFoundException},
     * 被 Forge 事件总线原样抛出 -> 集成服务器当场崩溃。
     * 崩报: {@code crash-reports/crash-2026-09-19_08.08.18-server.txt}
     * —— {@code -- Player being ticked --} 段落 NBT 里 {@code Health:0.0f} +
     * {@code DeathTime:27s} 就是铁证(死人身上在"吃完")。同类崩溃 9-10、9-12 已各出过一次,
     * 与本模组无关(栈里没有 ftboss 帧, 崩报自报 Suspected Mod = solcarrot)。
     *
     * <p>社区修复(Solcarrot Fix / 死吃修复)的做法是在 {@code LivingEntity#eat} 与 solcarrot 的
     * {@code onFoodEaten} 里"No 掉死人"; 这里用 Forge 事件做到**等价效果**, 不必引第三方 mod:
     * 死亡瞬间把嘴里的东西停掉, 那一口就永远不会在死人身上"吃完"。
     *
     * <p><b>为什么必须是 {@code Phase.START}</b>(已反编译核对 Forge 1.20.1 真实调用顺序):
     * {@code Player#tick()} 字节码 offset 1 = {@code ForgeEventFactory.onPlayerPreTick}(=START),
     * offset 140 才是 {@code super.tick()} —— 而"吃完"就发生在 {@code super.tick()} 里
     * ({@code LivingEntity.tick -> updateUsingItem -> completeUsingItem -> eat})。
     * 即 START 阶段**必定早于本 tick 的完成判定**, 不存在"同一 tick 抢先吃完"的窗口;
     * 唯一漏网是"玩家在自己那一 tick 里被火烧/中毒烧死且同一 tick 吃完"这种极端巧合
     * (那种情况只有 mixin 才盖得住, 社区修复用的就是 mixin)。
     *
     * <p>只拦**可食用**物品: solcarrot 的 {@code onFoodEaten} 在取能力之前就先
     * {@code if (!item.isEdible()) return;}(已核对字节码), 药水/弓箭根本走不到那行 ——
     * 顺手也避免了"死人在放箭"的怪现象。
     *
     * <p><b>踩坑记录(勿改)</b>: **不要**改成"取消 {@code LivingEntityUseItemEvent.Tick}"——
     * {@code ForgeEventFactory.onItemUseTick} 在事件被取消时返回 {@code -1}, 而 vanilla 是
     * {@code if (--useItemRemaining <= 0 && !useOnRelease()) completeUsingItem();},
     * 返回 -1 会**立刻吃完**, 反而必崩。
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDeadPlayerStopEating(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        if (!(e.player instanceof ServerPlayer sp) || sp.level().isClientSide) return;
        if (!sp.isUsingItem()) return;
        if (sp.isAlive() && !sp.isDeadOrDying()) return;      // 活着照旧(不影响正常吃饭/喝药)
        var stack = sp.getUseItem();
        if (!stack.isEdible()) return;                        // solcarrot 只对食物取能力
        var id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        FTBossMod.LOGGER.info("[FTBoss] 死亡瞬间停止进食(防 solcarrot 死吃崩溃): {} 停下 {}",
                sp.getName().getString(), id);
        sp.stopUsingItem();
    }

    @SubscribeEvent
    public void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent e) {
        if (e.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (!(e.player instanceof ServerPlayer sp) || sp.level().isClientSide) return;
        if (sp.tickCount % 10 != 0) return;
        if (!(sp.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
        /* ★★★ 2026-09-24 按用户要求新增「场地禁飞」★★★
           触发条件(见 ManorChaseRules): 场上庄园 Boss 连续 120 秒没对玩家造成伤害 ->
           开 60 秒窗口, 窗口内战场 96 格里的玩家不能飞; 窗口一过立刻恢复飞行, Boss 重新倒计时。
           与下面那套常驻禁飞(FLY_BAN_ENABLED=false, 会劈雷+断筋)**完全独立**:
           这里只掐断飞行 + 一条提示, 用户 2026-09-24 明确「不劈雷、不断筋」。 */
        if (net.farmingtales.ftboss.boss.ai.ManorFlyBan.active(sp)
                && net.farmingtales.ftboss.boss.ai.ManorFlyBan.stripFlight(sp)) {
            net.farmingtales.ftboss.boss.ai.ManorFlyBan.notifyPlayer(sp);
        }
        /* 附近�?Boss 才算"Boss �? */
        boolean hiddenNear = hiddenBossNear(sl, sp);
        boolean bossNear = hiddenNear;
        if (FLY_BAN_ENABLED && !bossNear) {
            var box = sp.getBoundingBox().inflate(64.0);
            for (net.minecraft.world.entity.LivingEntity boss : sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, box)) {
                if (isBossEntity(boss)) { bossNear = true; break; }
            }
        }
        if (!bossNear) return;

        /* 2026-09-12 v42 按用户反�?麻雀未生�?: �?*玩家 tick**驱动麻雀判定(这条链路最可靠),
           再加 EasterBossEntity.tick 与事件总线两条, 三处都靠刻戳去重, 不会重复判定�?*/
        try {
            net.farmingtales.ftboss.boss.SparrowSupport.tick(sl.getServer());
        } catch (Throwable ignored) {
        }

        /* ★ 2026-09-18 按用户反馈「常规 boss 战不应该出现隐藏 boss 战的信标加成」：
           这段信标增益从 2026-09-12 v38 起是**无条件**发的（当时注释写的就是"只要在 Boss 战里就一直有"），
           于是打糖霜女皇/屠夫/鼠群这些常规 Boss 时，玩家也会莫名其妙多出六项 II 级增益
           （其中**生命恢复 II** 尤其明显，看上去就像白送了一条命）。
           现在收窄成**只在隐藏 Boss（甜苹果庄园彩蛋十人）战里发** —— 与 SparrowSupport v106 同一个口径。
           注意：这里用 `hiddenBossNear` 而不是上面那个 `bossNear`（后者含全部常规 Boss）。 */
        if (hiddenNear) this.applyBeaconBuffs(sp);

        /* 禁飞已按用户要求关闭; 打开 FLY_BAN_ENABLED 才会执行下面这套 */
        if (!FLY_BAN_ENABLED) return;
        boolean flying = sp.getAbilities().flying || sp.isFallFlying();
        if (!flying) return;

        /* �?掐断飞行 */
        if (sp.getAbilities().flying) {
            sp.getAbilities().flying = false;
            sp.onUpdateAbilities();
        }
        if (sp.isFallFlying()) sp.stopFallFlying();

        /* 冷却: 每个玩家 4 秒最多一次雷�?+ debuff */
        int cd = FLY_BAN_CD.getOrDefault(sp.getUUID(), 0);
        if (cd > sp.tickCount) return;
        FLY_BAN_CD.put(sp.getUUID(), sp.tickCount + 80);

        /* �?一次真实闪�?*/
        var bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
        if (bolt != null) {
            bolt.moveTo(sp.getX(), sp.getY(), sp.getZ());
            sl.addFreshEntity(bolt);
        }
        /* �?筋箭断裂(候�?id 逐个�? */
        MobEffect tendon = findAnyEffect(
                new String[][]{
                        {"tridents_n_stuff", "ruptured_tendon"},
                        {"tridents_n_stuff", "tendon_rupture"},
                        {"tridents_n_stuff", "tendon_break"},
                        {"tridents_n_stuff", "broken_tendon"},
                        {"tridents_n_stuff", "crippled"},
                        {"armamentarium", "ruptured_tendon"},
                        {"armamentarium", "crippled"},
                });
        if (tendon != null) {
            sp.addEffect(new MobEffectInstance(tendon, 200, 0, false, true, true));
        } else {
            sp.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1, false, true, true));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, true, true));
        }
        sp.displayClientMessage(net.minecraft.network.chat.Component.literal("§c【禁止飞行】�?Boss 战期间无法起�?—�?筋箭断裂"), true);
        FTBossMod.LOGGER.info("[FTBoss] fly ban: struck {} (tendon={})", sp.getName().getString(),
                tendon == null ? "fallback(weakness+slowness)" : net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getKey(tendon));
    }

    /**
     /* 找【筋箭断裂�? 先按候�?id �? �?*扫描注册�?*按关键词匹配
     * (armamentarium 的补丁包里没�?lang, 拿不到准�?id, 所以用关键词兜�?�?
     * 关键�? tendon / rupture / cripple / broken / lame / �?
     */
    private static final String[] TENDON_HINTS = {"tendon", "rupture", "cripple", "broken", "lame", "snap"};

    private static MobEffect findAnyEffect(String[][] ids) {
        for (String[] id : ids) {
            MobEffect fx = findEffect(id[0], id[1]);
            if (fx != null) return fx;
        }
        /* 优先�?armamentarium 里找 */
        MobEffect hit = scanForTendon("armamentarium");
        if (hit != null) return hit;
        return scanForTendon(null);
    }

    private static MobEffect scanForTendon(String namespace) {
        try {
            for (var entry : net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getEntries()) {
                var key = entry.getKey().location();
                if (namespace != null && !namespace.equals(key.getNamespace())) continue;
                String p = key.getPath().toLowerCase(java.util.Locale.ROOT);
                for (String hint : TENDON_HINTS) {
                    if (p.contains(hint)) {
                        FTBossMod.LOGGER.info("[FTBoss] 筋箭断裂命中: {}", key);
                        return entry.getValue();
                    }
                }
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] 扫描筋箭断裂失败: {}", t.toString());
        }
        return null;
    }

    private static MobEffect findEffect(String namespace, String path) {
        try {
            return net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(
                    new net.minecraft.resources.ResourceLocation(namespace, path));
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     /* 【苹果阵营友伤免疫�?2026-09-12 按用户要�?: 本体/分身/召唤�?不吃
     * **闪电**�?*爆炸**伤害 —�?这样爵士�?15+ 道天雷与各种自爆都不会误伤自己人�?
     */
    @SubscribeEvent
    public void onAppleFriendlyFire(net.minecraftforge.event.entity.living.LivingHurtEvent e) {
        net.minecraft.world.entity.LivingEntity le = e.getEntity();   // LivingHurtEvent 的实体本来就是?LivingEntity
        if (!net.farmingtales.ftboss.entity.AppleBossEntity.isAppleSide(le)) return;
        var src = e.getSource();
        if (src.is(net.minecraft.tags.DamageTypeTags.IS_LIGHTNING)
                || src.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            e.setCanceled(true);
        }
    }

        /* ==================== 构建版本�?2026-09-12) ====================
       每次改动�?*手动 +1**; 启动时会打进日志, 用来确认"游戏里跑的到底是哪一�?
       (本项目反复出�?测到旧包"的误�? 靠这一行彻底消�? */
    public static final String BUILD_TAG = "easter-v155-sword-flat-face-turkey-display-java";

    static {
        FTBossMod.LOGGER.info("[FTBoss] ===== 载入构建版本：{} =====", BUILD_TAG);
    }

/* ==================== 2026-09-12: 玩家头衔检�?Titles 模组, 反射调用以保持可选依�? ==================== */

    /**
     /* 取玩家当前佩戴的头衔(纯文�?+ id)。Titles �?API:
     * {@code TitleManager.getCapability(player).resolve().getDisplayTitle().getDefaultDisplay()}�?
     * 全程**反射**, 所以没�?Titles 的整合包也不会崩(直接返回 null)�?
     */
    public static String playerTitle(net.minecraft.world.entity.player.Player p) {
        try {
            Class<?> tm = Class.forName("aurilux.titles.common.core.TitleManager");
            Object lazy = tm.getMethod("getCapability", net.minecraft.world.entity.player.Player.class).invoke(null, p);
            if (lazy == null) return null;
            Object opt = lazy.getClass().getMethod("resolve").invoke(lazy);
            Object cap = (opt instanceof java.util.Optional<?> o) ? o.orElse(null) : null;
            if (cap == null) return null;
            Object title = cap.getClass().getMethod("getDisplayTitle").invoke(cap);
            Object disp = title == null ? null : title.getClass().getMethod("getDefaultDisplay").invoke(title);
            Object id = title == null ? null : title.getClass().getMethod("getID").invoke(title);
            /* 头衔名在服务端常常只�?*翻译�?*(显示时才本地�?, 所以把 capability �?NBT 也带�?—�?
               里面含头衔完�?id, 这样即使拿不到中文名, 也能�?id 关键词匹配�?*/
            Object nbt = null;
            try { nbt = cap.getClass().getMethod("serializeNBT").invoke(cap); } catch (Throwable ignored) { }
            return (disp == null ? "" : disp.toString()) + "||" + (id == null ? "" : id.toString())
                    + "||" + (nbt == null ? "" : nbt.toString());
        } catch (Throwable t) {
            return null;
        }
    }

    /** 去掉空白后再做包含判�?头衔文本里常带大量空格对�? */
    private static boolean looseContains(String hay, String needle) {
        if (hay == null) return false;
        return hay.replaceAll("\\s+", "").contains(needle.replaceAll("\\s+", ""));
    }

    /**
     /* 玩家是否戴着指定头衔(关键词任意命中即�?。检测顺�?
     * �?Titles �?displayTitle(反射) �?显示�?/ 自定义名 / 名字 �?玩家持久�?NBT 的字符串(兜底)�?
     */
    public static boolean hasTitle(net.minecraft.world.entity.player.Player p, String... keywords) {
        String[] sources = new String[]{
                playerTitle(p),
                p.getDisplayName().getString(),
                p.getCustomName() != null ? p.getCustomName().getString() : null,
                p.getName().getString(),
                p.getPersistentData().toString(),
        };
        for (String kw : keywords) {
            for (String s : sources) {
                if (looseContains(s, kw)) return true;
            }
        }
        return false;
    }

    /* ==================== 2026-09-12: 背包里是否持有「血腥契约�?==================== */

    /**
     /* 玩家背包(主手/副手/快捷�?背包)里是否持有匹配关键词的物品�?
     * 比对物品�?*注册 id** �?*显示�?*, 任一命中即算�?
     */
    public static boolean hasItem(net.minecraft.world.entity.player.Player p, String... keywords) {
        return matchedItem(p, keywords) != null;
    }

    /**
     /* 返回背包里第一�?*精确命中**的物品描�?id + 显示�?, 没有�?null�?
     * 2026-09-12 改成精确匹配: 之前�?{@code contains("sanguine")} 这类子串,
     * 任何�?血�?/ sanguine"字样的物品都会误�?-> "没拿武器也变�?。现在只�?
     * **完整 id** �?**完整显示�?*(忽略空白后完全相�?�?
     */
    public static String matchedItem(net.minecraft.world.entity.player.Player p, String... exact) {
        try {
            for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
                var st = p.getInventory().getItem(i);
                if (st.isEmpty()) continue;
                var key = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(st.getItem());
                String idStr = key == null ? "" : key.toString();
                String nameStr = st.getHoverName().getString();
                for (String kw : exact) {
                    if (idStr.equalsIgnoreCase(kw) || looseEquals(nameStr, kw)) {
                        return idStr + " (" + nameStr + ") x" + st.getCount();
                    }
                }
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] 背包检测失败? {}", t.toString());
        }
        return null;
    }

    /** 忽略空白后完全相�?*/
    private static boolean looseEquals(String a, String b) {
        if (a == null || b == null) return false;
        return a.replaceAll("\\s+", "").equalsIgnoreCase(b.replaceAll("\\s+", ""));
    }

    /**
     /* 【血腥契约】判�?2026-09-12 第四次收�? 按用�?要检测到玩家持有武器"的要�?:
     * <ul>
     *   <li>只检�?<b>主手 + 副手</b>�?手持武器"语义）—�?不再扫整个背包；
     *       之前扫全背包导致"没拿在手上也算持�?, 看起来就像没检测武器也变身 �?/li>
     *   <li>id 只认**精确** {@code tridents_n_stuff:sanguine_bond}
     *       (模组自带 lang 已确�? item.tridents_n_stuff.sanguine_bond = 血腥契�?;</li>
     *   <li>显示名精确等于「血腥契约�?不用子串)�?/li>
     * </ul>
     */
    public static String matchedContract(net.minecraft.world.entity.player.Player p) {
        try {
            String[] hands = {"mainhand", "offhand"};
            for (int h = 0; h < 2; h++) {
                var st = (h == 0) ? p.getMainHandItem() : p.getOffhandItem();
                if (st == null || st.isEmpty()) continue;
                var key = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(st.getItem());
                String idStr = key == null ? "" : key.toString();
                String nameStr = st.getHoverName().getString();
                boolean hit = idStr.equalsIgnoreCase("tridents_n_stuff:sanguine_bond")
                        || looseEquals(nameStr, "血腥契约");
                if (hit) return hands[h] + "=" + idStr + " (" + nameStr + ") x" + st.getCount();
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] 契约检测失败? {}", t.toString());
        }
        return null;
    }

    /** 手上两件(主手/副手)的描�? 便于日志核对 */
    public static String handsDump(net.minecraft.world.entity.player.Player p) {
        try {
            var m = p.getMainHandItem();
            var o = p.getOffhandItem();
            var km = m.isEmpty() ? null : net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(m.getItem());
            var ko = o.isEmpty() ? null : net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(o.getItem());
            return "main=" + (km == null ? "empty" : km) + ", off=" + (ko == null ? "empty" : ko);
        } catch (Throwable t) {
            return "hands=?";
        }
    }

    /** 背包快照(调试�?: 把非空物品的 id 列出�? 一拿武器就能在日志里看到真�?id */
    public static String inventoryDump(net.minecraft.world.entity.player.Player p) {
        StringBuilder sb = new StringBuilder("[");
        try {
            int n = 0;
            for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
                var st = p.getInventory().getItem(i);
                if (st.isEmpty()) continue;
                var key = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(st.getItem());
                if (n++ > 0) sb.append(", ");
                sb.append(i).append(':').append(key == null ? "?" : key);
                if (n >= 14) { sb.append(", ..."); break; }
            }
        } catch (Throwable ignored) { }
        return sb.append(']').toString();
    }

    /**
     /* 【血腥契约�? {@code tridents_n_stuff:sanguine_bond}(2026-09-12 从整合包 jar 里查�?
     * {@code item.tridents_n_stuff.sanguine_bond = 血腥契约})�?
     * 只保�?*精确 id** �?*精确显示�?* —�?不再�?sanguine 这种子串关键词�?
     */
    public static final String[] BLOODY_CONTRACT = {
            "tridents_n_stuff:sanguine_bond",   // 精确 id
            "血腥契约",                          // 精确显示�?
    };

    public static boolean hasBloodyContract(net.minecraft.world.entity.player.Player p) {
        return hasItem(p, BLOODY_CONTRACT);
    }

    /** 调试�? 头衔解析结果 + 契约命中详情 + **背包物品 id 快照**, 便于排查"为什么没触发/误触�? */
    public static String debugLine(net.minecraft.world.entity.player.Player p) {
        return p.getName().getString()
                + " | title=" + playerTitle(p)
                + " | contract=" + matchedContract(p)
                + " | hands=" + handsDump(p)
                + " | inv=" + inventoryDump(p);
    }
}
