package net.farmingtales.ftboss.sound;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 自定义音效/音乐注册。
 * <p>BGM 由 {@link BossMusic} 全局调度, 每场战斗只播一首; 具体曲目由开战 Boss 决定:
 * <ul>
 *   <li><b>沙耶(茶树)</b> → {@link #BOSS_MUSIC_SAYA} → {@code music/saya.ogg}
 *       = 自写《茶の唄》(render_saya.cjs, 女声叙事曲, 131s)</li>
 *   <li><b>糖霜女皇(姜饼王)</b> → {@link #BOSS_MUSIC_GINGERBREAD} → {@code music/gingerbread.ogg}
 *       = 自写《木偶剧场·加洛普》(render_puppet.cjs, 115s)</li>
 *   <li><b>屠夫</b> → {@link #BOSS_MUSIC_BUTCHER} → {@code music/butcher.ogg}
 *       = 自写《肉山祭坛》(render_butcher.cjs, 屠宰场工业恐怖)</li>
 *   <li><b>酒桶王</b> → {@link #BOSS_MUSIC_TAVERN} → {@code music/tavern.ogg}
 *       = 自写《醉乡酒馆》(render_tavern.cjs, 酒馆舞曲/越喝越醉)</li>
 *   <li><b>鼠群</b> → {@link #BOSS_MUSIC_RAT} → {@code music/rat.ogg}
 *       = 自写《鼠潮夜行》(render_rat.cjs, 擦擦索索/突然爆发)</li>
 *   <li><b>其余(未登记的 Boss)</b> → 三幕剧 overture/masquerade/finale + 真死时 curtain</li>
 * </ul>
 * <p><b>2026-09-10 起五个 Boss 各有专属单曲</b>, 三幕剧退居兜底。
 */
public final class FTSounds {

    /** 音乐总开关 */
    public static final boolean MUSIC_ENABLED = true;

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FTBossMod.MOD_ID);

    /** 沙耶(茶树Boss) 专属BGM《茶の唄》: 文件 assets/ftboss/sounds/music/saya.ogg */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_SAYA = SOUNDS.register("boss_music_saya",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_saya")));

    /** 糖霜女皇(姜饼王) 专属BGM《木偶剧场·加洛普》: 文件 assets/ftboss/sounds/music/gingerbread.ogg
     *  (2026-09-10: 原先指向 music/saya, 该文件已改作沙耶的专属曲, 故拆成独立文件) */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_GINGERBREAD = SOUNDS.register("boss_music_gingerbread",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_gingerbread")));

    /** 屠夫 专属BGM《肉山祭坛》: 文件 assets/ftboss/sounds/music/butcher.ogg (自写, render_butcher.cjs) */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_BUTCHER = SOUNDS.register("boss_music_butcher",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_butcher")));

    /** 酒桶王 专属BGM《醉乡酒馆》(酒馆风): 文件 assets/ftboss/sounds/music/tavern.ogg (自写, render_tavern.cjs) */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_TAVERN = SOUNDS.register("boss_music_tavern",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_tavern")));

    /** 鼠群 专属BGM《鼠潮夜行》: 文件 assets/ftboss/sounds/music/rat.ogg (自写, render_rat.cjs) */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_RAT = SOUNDS.register("boss_music_rat",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_rat")));

    /** 【故障机器人】专属BGM《故障机器人》: 文件 assets/ftboss/sounds/music/defect_robot.ogg (2026-09-11 新增) */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_DEFECT = SOUNDS.register("boss_music_defect_robot",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_defect_robot")));

    /** 【炫彩答辩超人】专属BGM《堆肥·冬夜》: assets/ftboss/sounds/music/compost.ogg (2026-09-12, 钢琴抒情风) */
    /** 炫彩答辩超人 **二阶段** BGM(升调提速版) */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_COMPOST_P2 = SOUNDS.register("boss_music_compost_p2",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_compost_p2")));

    public static final RegistryObject<SoundEvent> BOSS_MUSIC_COMPOST = SOUNDS.register("boss_music_compost",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_compost")));

    /**
     * 【甜苹果庄园·彩蛋十人】专属**史诗战斗曲**(2026-09-13 v82 按用户要求新作, 约 20 分钟, 整场不切)。
     *
     * <p>文件 {@code assets/ftboss/sounds/music/manor_epic.ogg}, 由
     * {@code tools/audio_tools/compose_epic.cjs} 生成(纯 Node 合成 + WASM Ogg 编码, 机器上没有 ffmpeg)。
     * 结构: 序章 → 主题A → 发展B → 再现A → 危机C → 高潮 → 间歇 → 终局 → 尾声(D 小调为主, 尾部转大调收束)。
     */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MANOR_EPIC = SOUNDS.register("boss_music_manor_epic",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_manor_epic")));

    // ===== 《雪夜傀儡剧场》三幕剧(2026-09-10 导入自 yuletide_dread 素材) =====
    // 文件: assets/ftboss/sounds/music/theatre_*.ogg (51.43s x3 + 8.04s 落幕)
    // 由 BossMusic 调度: 按全场 Boss 最低血量比例推进 序幕 -> 假面圆舞 -> 断线狂宴, 真死时奏落幕钟声

    /** 第一幕·序幕: assets/ftboss/sounds/music/theatre_overture.ogg */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_OVERTURE = SOUNDS.register("boss_music_overture",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_overture")));

    /** 第二幕·假面圆舞: assets/ftboss/sounds/music/theatre_masquerade.ogg */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MASQUERADE = SOUNDS.register("boss_music_masquerade",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_masquerade")));

    /** 第三幕·断线狂宴: assets/ftboss/sounds/music/theatre_finale.ogg */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_FINALE = SOUNDS.register("boss_music_finale",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_finale")));

    /** 落幕钟声: assets/ftboss/sounds/music/theatre_curtain.ogg (8s, Boss 真死时播放一次) */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_CURTAIN = SOUNDS.register("boss_music_curtain",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_curtain")));

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
    /** 【苹果爵士·蜜脆】专属BGM《果园教父》(2026-09-12, 黑帮摇摆爵士 108 BPM) */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_APPLE = SOUNDS.register("boss_music_apple",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_apple")));
    /** 二阶段《果园教父·虫蛀暴走》(132 BPM, 升 2 个半音) */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_APPLE_P2 = SOUNDS.register("boss_music_apple_p2",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_apple_p2")));

    /* ===== 2026-09-13 晚: 按用户要求删掉"之前做的序曲 / 第一段 / 第二段 / 第三段 / 终曲"
     *       那一批试听唱片 —— 11 Boss 连战套曲的规划作废, 整批没用了。
     *       现在只剩样片 6 / 样片 7 两张。 ===== */

    /**
     * 【庄园战 · 演出曲】《庄园·序》≈ 41 s。
     *
     * <p>2026-09-13 晚按用户要求: 庄园部分改成**两首** ——
     * 演出曲负责"弹【挑战】确认 + 30 秒开场秀", 战斗曲负责正式开打之后的整场。
     * 两首都以同一个"庄园主题"动机 D–C–Bb–A 写成, 所以听起来是同一部作品。
     */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MANOR_CUE = SOUNDS.register("boss_music_manor_cue",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_manor_cue")));

    /** 【庄园战 · 战斗曲】《庄园·战》≈ 4:27。多阶段 + "掉坑再堆"(学参考曲 A 的结构)。 */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MANOR_BATTLE = SOUNDS.register("boss_music_manor_battle",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_manor_battle")));

    /** 【庄园战 · 第二首战斗曲】《庄园·鏖战》≈ 3:26（2026-09-14 新增, 学《Bury the Light》: 厚/密/响）。
     *  与《庄园·鏖战》那一份… 见下: 战斗阶段现在由《鏖战》+【双轨·战斗高潮】**轮流播**。 */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MANOR_GUIGNOL = SOUNDS.register("boss_music_manor_guignol",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_manor_guignol")));

    /* ==================== v126 按用户要求「演出直接放双轨坦克曲 + 把它的序曲部分剥出去当战斗曲」 ====================
       原来的流程是「演出放《序》(45 s 的独立曲子) -> 战斗放《战》/《鏖战》」。现在:
       ① **演出阶段直接放《庄园·双轨》全曲(4:16)** —— 它开头 1:40 本来就是铺垫式的"序曲",
          所以那个单独分离出来的《序》不再需要播;
       ② 把《双轨》**1:40 之后的部分**切成一份新的战斗曲 `manor_dual_battle.ogg`(2:28 = 2960 刻),
          战斗里跟《鏖战》轮流 —— 《庄园·战》从此只作唱片(不再参与 BGM 轮换)。 */

    /** 【庄园战 · 演出+双轨坦克曲全曲】4:16 —— 演出阶段放整首 */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MANOR_DUAL = SOUNDS.register("boss_music_manor_dual",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_manor_dual")));

    /** 【庄园战 · 双轨的战斗高潮部分】2:28 —— 《双轨》剥掉 1:40 序曲后的那一份, 与《鏖战》轮流 */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_MANOR_DUAL_BATTLE = SOUNDS.register("boss_music_manor_dual_battle",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_manor_dual_battle")));

    /* ===== 2026-09-13 晚: 旧的样片 6/7(poc6/poc7)已按用户要求退役 ——
     *       它们只是同一份乐谱的两种吉他音色, 现在换成上面这两首正式曲。 ===== */

    /** 【唱片 · 庄园·序】演出曲, 41 s */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_MANOR_CUE = SOUNDS.register("music_disc_manor_cue",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_manor_cue")));

    /** 【唱片 · 庄园·战】战斗曲, 4:27 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_MANOR_BATTLE = SOUNDS.register("music_disc_manor_battle",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_manor_battle")));

    /** 【唱片 · 庄园·双轨】坦克版 + 真吉他版各 0.5 叠放(和游戏内双轨同一混法), 4:27 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_MANOR_DUAL = SOUNDS.register("music_disc_manor_dual",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_manor_dual")));

    /** 【唱片 · 庄园·鏖战】第二首庄园战斗曲(v125 按用户要求「新加入的战斗曲做成唱片」), 3:28.7 = 4173 tick。
     *  和战斗 BGM 用的是**同一个** {@code music/manor_guignol} 音频文件, 只是换一个 SoundEvent
     *  (理由同 v112 那批: 战斗曲结束时会发 ClientboundStopSoundPacket, 共用事件会把唱片机里的也掐掉)。 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_MANOR_GUIGNOL = SOUNDS.register("music_disc_manor_guignol",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_manor_guignol")));

    /** 【Boss 专属曲】《牧野犬王》—— v127 第 9 位主 Boss【边牧犬王 benger】, 3:05（2026-09-14 本会话新写）。
     *  A 多利亚(牧野) → 狂暴段转和声小调; 长嗥动机当钩子; 母带同庄园 v3(Bury the Light 那一版)。 */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_BENGER = SOUNDS.register("boss_music_benger",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_benger")));

    /** 【唱片 · 牧野犬王】边牧犬王专属曲, 185.41 s = **3708 tick**（2026-09-14 按用户要求做成唱片）。
     *  同样**单独注册事件**（不与 BGM 共用）: 战斗开始/结束时 BossMusic 会发停止包,
     *  共用事件会把唱片机里正在放的那张一起掐掉 —— 这条 v112 就踩过。 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_BENGER = SOUNDS.register("music_disc_benger",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_benger")));

    /* ==================== v112 食物系 Boss 全曲唱片(2026-09-13 按用户要求) ====================
       8 位食物系 Boss 的专属 BGM 全部做成唱片 —— 苹果爵士与炫彩答辩超人各 2 首, 共 **10 张**。
       每张仍然**单独注册一个 sound 事件**(不直接复用 boss_music_*): BossMusic 切曲时会往客户端发
       ClientboundStopSoundPacket 把战斗曲掐掉, 唱片若挂在同一个事件上, 一开打/一结束就会把
       唱片机里正在放的同名曲一起掐了。 */
    /** 【唱片·果园教父】苹果爵士·蜜脆 一阶段, 1:39 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_APPLE = SOUNDS.register("music_disc_apple",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_apple")));

    /** 【唱片·果园教父(虫蛀暴走)】苹果爵士·蜜脆 二阶段, 1:22 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_APPLE_P2 = SOUNDS.register("music_disc_apple_p2",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_apple_p2")));

    /** 【唱片·木偶剧场·加洛普】糖霜女皇(姜饼王), 1:55 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_GINGERBREAD = SOUNDS.register("music_disc_gingerbread",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_gingerbread")));

    /** 【唱片·肉山祭坛】屠夫, 2:25 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_BUTCHER = SOUNDS.register("music_disc_butcher",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_butcher")));

    /** 【唱片·茶の唄】沙耶(茶树), 2:11 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_SAYA = SOUNDS.register("music_disc_saya",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_saya")));

    /** 【唱片·醉乡酒馆】酒魔·酒桶王, 2:03 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_TAVERN = SOUNDS.register("music_disc_tavern",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_tavern")));

    /** 【唱片·鼠潮夜行】鼠群, 1:51 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_RAT = SOUNDS.register("music_disc_rat",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_rat")));

    /** 【唱片·故障机器人】故障机器人, 2:36 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_DEFECT = SOUNDS.register("music_disc_defect",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_defect")));

    /** 【唱片·堆肥·强袭】炫彩答辩超人 一阶段, 1:29 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_COMPOST = SOUNDS.register("music_disc_compost",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_compost")));

    /** 【唱片·堆肥·熔毁】炫彩答辩超人 二阶段, 1:25 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_COMPOST_P2 = SOUNDS.register("music_disc_compost_p2",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_compost_p2")));

    public static final RegistryObject<SoundEvent> BOSS_MUSIC_TIDE_FISH = SOUNDS.register("boss_music_tide_fish",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_tide_fish")));
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_TIDE_FISH_P2 = SOUNDS.register("boss_music_tide_fish_p2",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_tide_fish_p2")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_TIDE_FISH = SOUNDS.register("music_disc_tide_fish",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_tide_fish")));
    public static final RegistryObject<SoundEvent> MUSIC_DISC_TIDE_FISH_P2 = SOUNDS.register("music_disc_tide_fish_p2",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "music_disc_tide_fish_p2")));

    private FTSounds() {}
}
