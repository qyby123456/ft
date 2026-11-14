/*
 * ⑤-3 + ⑤-4(2026-09-12):
 *  A) BossMusic 增 COMPOST 组, 把《堆肥·冬夜》接到炫彩答辩超人
 *  B) 修 boss_music_compost 音效解析(对照可用条目重建 sounds.json 里的该条)
 *  C) 物品: 炫彩肥料(可催熟作物) + 炫彩堆肥桶(召唤物)
 *  D) 召唤配方: 3x3 (Create 土豆加农炮 + 骨粉 + 堆肥桶 -> 炫彩堆肥桶)
 *  E) 石像: statue_compost(专属), 用石像生成器产出模型 + 注册 + 战利品表 + lang + 创造栏 + 掉落
 *  生成 Java 一律 **ASCII 字符串**, 中文只进 lang JSON(避免编码问题)
 */
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const ROOT = process.argv[2];
const J = 'src/main/java/net/farmingtales/ftboss';
const A = 'src/main/resources/assets/ftboss';
const D = 'src/main/resources/data/ftboss';
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => { fs.mkdirSync(path.dirname(path.join(ROOT, p)), { recursive: true }); fs.writeFileSync(path.join(ROOT, p), s); };
function rep(rel, a, b, label) {
    const s = read(rel);
    if (s.includes(b)) { console.log('  skip ' + label); return true; }
    if (!s.includes(a)) { console.log('  FAIL ' + label); return false; }
    write(rel, s.replace(a, b));
    console.log('  OK   ' + label);
    return true;
}

/* ---------- A) BossMusic: COMPOST 组 ---------- */
rep(`${J}/sound/BossMusic.java`,
    `    private enum Group { THEATRE, SAYA, GINGERBREAD, BUTCHER, TAVERN, RAT, DEFECT }`,
    `    private enum Group { THEATRE, SAYA, GINGERBREAD, BUTCHER, TAVERN, RAT, DEFECT, COMPOST }`, 'enum COMPOST');
rep(`${J}/sound/BossMusic.java`,
    `            if (boss.getType() == FTEntities.DEFECT_CORE.get()) return Group.DEFECT;`,
    `            if (boss.getType() == FTEntities.DEFECT_CORE.get()) return Group.DEFECT;
            if (boss.getType() == FTEntities.COMPOST_BOSS.get()) return Group.COMPOST;`, 'groupOf COMPOST');
rep(`${J}/sound/BossMusic.java`,
    `            case DEFECT: return FTSounds.BOSS_MUSIC_DEFECT.get();`,
    `            case DEFECT: return FTSounds.BOSS_MUSIC_DEFECT.get();
            case COMPOST: return FTSounds.BOSS_MUSIC_COMPOST.get();`, 'trackEvent COMPOST');
rep(`${J}/sound/BossMusic.java`,
    `                FTSounds.BOSS_MUSIC_DEFECT.get(),`,
    `                FTSounds.BOSS_MUSIC_DEFECT.get(),
                FTSounds.BOSS_MUSIC_COMPOST.get(),`, 'allTrackEvents COMPOST');

/* ---------- B) 修 sounds.json 的 compost 条目(对照可用条目重建) ---------- */
{
    const p = path.join(ROOT, A, 'sounds.json');
    const sj = JSON.parse(fs.readFileSync(p, 'utf8'));
    const refName = Object.keys(sj).find(k => k === 'boss_music_defect_robot');
    const ref = refName ? sj[refName] : { subtitle: 'x', sounds: [{ name: 'ftboss:music/x', stream: true }] };
    sj['boss_music_compost'] = JSON.parse(JSON.stringify(ref));
    sj['boss_music_compost'].sounds[0].name = 'ftboss:music/compost';
    sj['boss_music_compost'].subtitle = 'ftboss.subtitle.boss_music_compost';
    fs.writeFileSync(p, JSON.stringify(sj, null, 2));
    console.log('  OK   sounds.json compost 条目已按可用条目重建 -> ' + JSON.stringify(sj['boss_music_compost'].sounds));
    const ogg = path.join(ROOT, A, 'sounds/music/compost.ogg');
    console.log('  ogg 存在: ' + fs.existsSync(ogg) + '  ' + (fs.existsSync(ogg) ? fs.statSync(ogg).size + ' B' : ''));
}

/* ---------- C) 物品 ---------- */
write(`${J}/item/RainbowFertilizerItem.java`, `package net.farmingtales.ftboss.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Rainbow Fertilizer (2026-09-12): works like bone meal but stronger (also heals a bit of the user). */
public class RainbowFertilizerItem extends Item {

    public RainbowFertilizerItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        ItemStack stack = ctx.getItemInHand();
        if (BoneMealItem.applyBonemeal(stack, level, pos, ctx.getPlayer())) {
            if (ctx.getPlayer() != null) ctx.getPlayer().heal(1.0f);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useOn(ctx);
    }
}
`);
write(`${J}/item/CompostSummonItem.java`, `package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Rainbow Compost Bucket (2026-09-12): summons the Compost Superman in front of the player. */
public class CompostSummonItem extends Item {

    public CompostSummonItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel sl) {
            BlockPos pos = player.blockPosition().relative(player.getDirection(), 4);
            CompostBossEntity boss = FTEntities.COMPOST_BOSS.get().create(sl);
            if (boss == null) {
                FTBossMod.LOGGER.warn("[FTBoss] Compost Superman create() returned null");
                return InteractionResultHolder.fail(stack);
            }
            boss.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot() + 180.0f, 0.0f);
            sl.addFreshEntity(boss);
            sl.playSound(null, boss.getX(), boss.getY(), boss.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.6f, 0.6f);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            FTBossMod.LOGGER.info("[FTBoss] Compost Superman summoned at {}, {}, {}", pos.getX(), pos.getY(), pos.getZ());
        }
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
`);

/* ---------- C2) 注册物品 + 创造栏 + lang ---------- */
{
    const p = `${J}/item/FTBossItems.java`;
    let s = read(p);
    if (!s.includes('RAINBOW_FERTILIZER')) {
        const lines = s.split('\n');
        const last = lines.map((l, i) => l.includes('RegistryObject<Item>') ? i : -1).filter(i => i >= 0).pop();
        lines.splice(last + 1, 0,
            '',
            '    /** Rainbow Fertilizer: drop of the Compost Superman (works like bone meal) */',
            '    public static final RegistryObject<Item> RAINBOW_FERTILIZER = ITEMS.register("rainbow_fertilizer",',
            '            () -> new RainbowFertilizerItem(new Item.Properties()));',
            '',
            '    /** Rainbow Compost Bucket: summons the Compost Superman */',
            '    public static final RegistryObject<Item> COMPOST_SUMMON = ITEMS.register("compost_summon",',
            '            () -> new CompostSummonItem(new Item.Properties().stacksTo(1)));');
        write(p, lines.join('\n'));
        console.log('  OK   FTBossItems 注册两个物品');
    } else console.log('  skip FTBossItems');
}
rep(`${J}/item/FTCreativeTabs.java`,
    `                out.accept(FTBossItems.DEFECT_FRAGMENT.get());`,
    `                out.accept(FTBossItems.DEFECT_FRAGMENT.get());
                out.accept(FTBossItems.RAINBOW_FERTILIZER.get());
                out.accept(FTBossItems.COMPOST_SUMMON.get());`, '创造栏加两物品');
for (const [f, fert, sum] of [['zh_cn.json', '\u70ab\u5f69\u80a5\u6599', '\u70ab\u5f69\u5806\u80a5\u6876'], ['en_us.json', 'Rainbow Fertilizer', 'Rainbow Compost Bucket']]) {
    const p = path.join(ROOT, A, 'lang', f);
    const j = JSON.parse(fs.readFileSync(p, 'utf8'));
    j['item.ftboss.rainbow_fertilizer'] = fert;
    j['item.ftboss.compost_summon'] = sum;
    fs.writeFileSync(p, JSON.stringify(j, null, 2));
}
console.log('  OK   lang 两个物品');

/* ---------- D) 召唤配方 ---------- */
{
    const recipe = {
        type: 'minecraft:crafting_shaped',
        pattern: ['PBP', 'BCB', 'PBP'],
        key: {
            P: { item: 'create:precision_mechanism' },
            B: { item: 'minecraft:bone_meal' },
            C: { item: 'minecraft:composter' }
        },
        result: { item: 'ftboss:compost_summon', count: 1 }
    };
    write(`${D}/recipes/compost_summon.json`, JSON.stringify(recipe, null, 2));
    console.log('  OK   配方 compost_summon.json');
}

/* ---------- E) 石像 ---------- */
/* E1 生成器加 statue_compost 条目 */
function addBossEntry(file, entry, anchorLine) {
    const p = path.join(ROOT, file);
    let s = fs.readFileSync(p, 'utf8');
    if (s.includes("'statue_compost'")) { console.log('  skip ' + path.basename(file)); return; }
    if (!s.includes(anchorLine)) { console.log('  FAIL 找不到锚点 in ' + path.basename(file)); return; }
    s = s.replace(anchorLine, anchorLine + '\n' + entry);
    fs.writeFileSync(p, s);
    console.log('  OK   ' + path.basename(file) + ' 加 statue_compost');
}
addBossEntry('tools/model3d/gen_statue_real.cjs',
    "  { id: 'statue_compost', label: 'Compost Statue', geo: 'compost_superman.geo.json', tex: 'entity/compost_superman', texW: 512, texH: 512 },",
    "const BOSSES = [");
addBossEntry('tools/model3d/gen_statue_skins.cjs',
    "  { id: 'statue_compost', tint: 7, label: 'Compost Statue', pal: { icon: [150, 118, 74], iconAccent: [232, 74, 190] } },",
    "const BOSSES = [");
/* E2 跑生成器 */
for (const g of ['gen_statue_real.cjs', 'gen_statue_skins.cjs']) {
    const r = spawnSync('node', [g], { cwd: path.join(ROOT, 'tools/model3d'), stdio: 'inherit' });
    console.log(`  ${g}: 退出码 ${r.status}`);
}
/* E3 只拷贝 statue_compost 相关 + 其贴图(避免覆盖别的石像 -> 上次的教训) */
let copied = 0;
for (const base of ['out_statues_real', 'out_statues']) {
    for (const sub of ['models/block', 'blockstates', 'textures/block']) {
        const dir = path.join(ROOT, 'tools/model3d', base, sub);
        if (!fs.existsSync(dir)) continue;
        for (const f of fs.readdirSync(dir)) {
            if (!/^statue_compost/.test(f)) continue;
            const dst = path.join(ROOT, A, sub, f);
            fs.mkdirSync(path.dirname(dst), { recursive: true });
            fs.copyFileSync(path.join(dir, f), dst);
            copied++;
        }
    }
}
console.log(`  石像资源拷贝: ${copied} 个(仅 statue_compost*)`);
/* E4 战利品表 */
try {
    const tpl = path.join(ROOT, D, 'loot_tables/blocks/statue_rat_swarm.json');
    if (fs.existsSync(tpl)) {
        const t = fs.readFileSync(tpl, 'utf8').split('statue_rat_swarm').join('statue_compost');
        write(`${D}/loot_tables/blocks/statue_compost.json`, t);
        console.log('  OK   战利品表 statue_compost.json');
    } else console.log('  FAIL 找不到战利品表模板');
} catch (e) { console.log('  FAIL 战利品表: ' + e.message); }
/* E5 注册 + lang + 创造栏 + 掉落 */
rep(`${J}/block/FTBlocks.java`,
    `    public static final RegistryObject<Block> STATUE_DEFECT = statue("statue_defect", 6);`,
    `    public static final RegistryObject<Block> STATUE_DEFECT = statue("statue_defect", 6);
    /** Compost Superman statue (2026-09-12) */
    public static final RegistryObject<Block> STATUE_COMPOST = statue("statue_compost", 7);`, 'FTBlocks 注册石像');
for (const [f, v] of [['zh_cn.json', '\u77f3\u50cf\u00b7\u70ab\u5f69\u7b54\u8fa9\u8d85\u4eba'], ['en_us.json', 'Statue: Compost Superman']]) {
    const p = path.join(ROOT, A, 'lang', f);
    const j = JSON.parse(fs.readFileSync(p, 'utf8'));
    j['block.ftboss.statue_compost'] = v;
    fs.writeFileSync(p, JSON.stringify(j, null, 2));
}
rep(`${J}/item/FTCreativeTabs.java`,
    `                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_DEFECT.get().asItem());`,
    `                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_DEFECT.get().asItem());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_COMPOST.get().asItem());`, '创造栏石像');
rep(`${J}/boss/ai/BossMercy.java`,
    `            if (t == net.farmingtales.ftboss.entity.FTEntities.DEFECT_CORE.get())`,
    `            if (t == net.farmingtales.ftboss.entity.FTEntities.COMPOST_BOSS.get())
                statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_COMPOST.get().asItem();
            else if (t == net.farmingtales.ftboss.entity.FTEntities.DEFECT_CORE.get())`, '石像掉落');
console.log('⑤-3/⑤-4 完成');
