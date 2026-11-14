// 全工程体检: 资源引用完整性 + 语言键 + 模型/贴图/动画对应关系
// 用法: node audit.cjs <工程根目录>
//
// 检查:
//   1) assets 下的贴图/geo/动画/物品模型/方块模型/blockstate: 有没有被任何地方引用
//   2) java 里出现的 translatable("key") / "boss.ftboss.*" 等语言键: 是否在 zh_cn/en_us 中定义
//   3) lang 里定义但代码与资源都没用到的键
//   4) data 下的配方/掉落表: 引用的物品/方块是否存在同名资源
const fs = require('fs');
const path = require('path');

const root = process.argv[2] || '.';
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const data = path.join(root, 'src/main/resources/data/ftboss');
const javaDir = path.join(root, 'src/main/java');

function walk(dir, out = []) {
  if (!fs.existsSync(dir)) return out;
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p, out); else out.push(p);
  }
  return out;
}
const javaFiles = walk(javaDir);
const javaText = javaFiles.map(f => fs.readFileSync(f, 'utf8')).join('\n');
const resFiles = walk(assets);
const dataFiles = walk(data);
const allJsonText = [...resFiles, ...dataFiles].filter(f => f.endsWith('.json') || f.endsWith('.mcmeta'))
  .map(f => fs.readFileSync(f, 'utf8')).join('\n');
const haystack = javaText + '\n' + allJsonText;

const rel = p => path.relative(path.join(root, 'src/main/resources'), p).replace(/\\/g, '/');

/* ---- 开发残留: 这些**按名字后缀**就能确定性判定为开发备份, 单独列出而不是混进"无引用资源" ---- */
const DEV_ARTIFACT = /\.(bak|bak2|orig|old|prev|swp|reorder\.bak|keyorder\.bak|tilted-bak)$|~$/;
const devArtifacts = resFiles.filter(f => DEV_ARTIFACT.test(path.basename(f)));

/* ---- "按约定被引用"的资源: 静态扫描必然看不到, **不是死资源** ----
   1) `_e.png`   —— OptiFine/Iris 的发光层约定 (ETF 也认), 由渲染管线按后缀自动加载;
   2) `_glowmask.png` —— GeckoLib 的 AutoGlowingTexture 约定;
   3) `mob_effect/<id>.png` —— 原版按 MobEffect 注册 id 自动取图;
   4) 充能球 `*_orb.*` / `defect_core_<tint>*` / `easter/<skin>.png` —— 代码里**字符串拼接**出的路径。 */
const CONVENTION = [
  { re: /_e\.png$/, why: 'OptiFine/Iris 发光层约定 (_e)' },
  { re: /_glowmask\.png$/, why: 'GeckoLib AutoGlowingTexture 发光遮罩约定 (_glowmask)' },
  { re: /^textures\/mob_effect\/[^/]+\.png$/, why: '原版按 MobEffect 注册 id 自动取图' },
  { re: /(^|\/)[a-z0-9_]*_orb(\.geo\.json|\.animation\.json|\.png|_glowmask\.png)$/, why: '充能球贴图/模型, 路径由代码拼接' },
  { re: /^textures\/entity\/defect_core_(glass|lightning|frost|ion|dark|base)?(_glowmask)?\.png$/, why: '故障核心按球种变色的贴图, 路径由代码拼接' },
  { re: /^textures\/entity\/easter\/[^/]+\.png$/, why: '彩蛋皮肤, 路径由代码拼接' },
  { re: /^textures\/item\/[^/]+_e\.png$/, why: '物品发光层约定 (_e)' }
];
function conventionWhy(inner) {
  for (const c of CONVENTION) if (c.re.test(inner)) return c.why;
  return null;
}

/* ---- 1) 资源引用 ---- */
// 贴图要求"完整路径"级引用(带 ftboss: 命名空间), 否则 butcher.png 会被 butcher_cleaver 之类误判为已引用
function isTexture(inner) { return inner.startsWith('textures/'); }
const unreferenced = [];
const byConvention = [];
for (const f of resFiles) {
  const r = rel(f);                       // assets/ftboss/...
  const inner = r.replace(/^assets\/ftboss\//, '');
  if (DEV_ARTIFACT.test(path.basename(f))) continue;      // 开发残留走 [11] 单独报
  const conv = conventionWhy(inner);
  if (conv) { byConvention.push(`${r}   ← ${conv}`); continue; }
  /* 由原版/加载器**按固定名字**加载的文件 —— 静态扫描永远找不到"引用", 这不是死资源。 */
  if (inner === 'lang/zh_cn.json' || inner === 'lang/en_us.json' ||
      inner === 'sounds.json' || inner === 'pack.mcmeta' ||
      inner.endsWith('/pack.mcmeta')) {
    byConvention.push(`${r}   ← 由加载器按固定名读取`);
    continue;
  }
  const base = path.basename(f);
  const stem = base.replace(/\.(png|json|ogg|mcmeta)$/, '');
  const dir = path.dirname(inner);
  let cands;
  // 充能球的五套资源、以及故障核心"按球种变色"的 5 张核心变体贴图, 都是**代码里动态拼接路径**引用的, 静态扫描看不到 -> 先判掉
  if (/(^|\/)(geo|animations|textures\/entity)\/[a-z0-9_]*_orb(\.geo\.json|\.animation\.json|\.png|_glowmask\.png)$/.test(inner)
      || /^textures\/entity\/defect_core_(glass|lightning|frost|ion|dark)(_glowmask)?\.png$/.test(inner)) {
    cands = [inner, base, 'ChargedOrbGeoModel', 'DefectCoreGeoModel'];
  } else if (isTexture(inner)) {
    const innerNoExt = inner.replace(/\.(png|jpg)$/, '');
    const short = innerNoExt.replace(/^textures\//, '');   // 物品/方块模型里写的是 ftboss:item/xxx
    cands = [`ftboss:${innerNoExt}`, innerNoExt, `ftboss:${short}`, short];
  } else if (inner === 'lang/zh_cn.json' || inner === 'lang/en_us.json' || inner === 'sounds.json' || inner === 'pack.mcmeta') {
    cands = [inner, base];   // 这些由原版按固定名加载
  } else {
    cands = [inner, `ftboss:${inner}`, stem, `${dir}/${stem}`, `ftboss:${dir}/${stem}`];
  }
  const hit = cands.some(c => c && c.length > 3 && haystack.includes(c));
  if (!hit) unreferenced.push(r);
}

/* ---- 2) 代码里的语言键 vs lang 文件 ---- */
const zh = JSON.parse(fs.readFileSync(path.join(assets, 'lang/zh_cn.json'), 'utf8'));
const en = JSON.parse(fs.readFileSync(path.join(assets, 'lang/en_us.json'), 'utf8'));
const keysInCode = new Set();
/* ★ 只认**完整的字面量键**。旧版这里报出 `enchantment.level.` 与 `potion.withAmplifier` 两个假键,
   它们其实是代码里做字符串拼接的前缀/片段 (如 "enchantment.level." + n), 并不是真实语言键。
   判据: 键必须以字母/数字/下划线结尾, 且至少含一个 `.` 之后再有实义段, 末尾不能是 `.`。 */
const KEY_OK = /^[A-Za-z0-9_]+(\.[A-Za-z0-9_]+)+$/;
const addKey = k => { if (KEY_OK.test(k)) keysInCode.add(k); };
for (const m of javaText.matchAll(/translatable\(\s*"([^"]+)"/g)) addKey(m[1]);
for (const m of javaText.matchAll(/Component\.translatable\(\s*"([^"]+)"/g)) addKey(m[1]);
for (const m of javaText.matchAll(/(?:setName|setCustomName|hoverName|literal)\(\s*Component\.translatable\(\s*"([^"]+)"/g)) addKey(m[1]);
// 资源文件里的 key(如 sounds.json 的 subtitle)
for (const m of allJsonText.matchAll(/"(ftboss\.[a-z0-9_.]+)"/gi)) addKey(m[1]);
for (const m of allJsonText.matchAll(/"(item\.ftboss\.[a-z0-9_.]+|block\.ftboss\.[a-z0-9_.]+|entity\.ftboss\.[a-z0-9_.]+|boss\.ftboss\.[a-z0-9_.]+)"/gi)) addKey(m[1]);
/* 被上面过滤掉的"疑似拼接片段"单独留痕, 方便人工确认 (不算缺失键) */
const keyFragments = new Set();
for (const m of javaText.matchAll(/translatable\(\s*"([^"]+)"/g)) if (!KEY_OK.test(m[1])) keyFragments.add(m[1]);

const missingZh = [...keysInCode].filter(k => !(k in zh)).sort();
const missingEn = [...keysInCode].filter(k => !(k in en)).sort();

/* ★ 关键修正 (2026-09-16): "代码里用到但本 mod 的 lang 里没有" **不等于缺失**。
   像 `Component.translatable("potion.withAmplifier", ...)` 用的是**原版**键 —— 它由原版
   `assets/minecraft/lang/*.json` 提供, 本 mod 当然没有。旧版把它报成"缺失语言键"是假问题。
   正确判据: 只有当该键**既不在本 mod 的 lang、也不在原版 lang** 时才报缺失。
   原版 lang 从 Gradle 缓存的 client jar 里取 (找不到就退化为"不判定原版键")。 */
let vanillaKeys = null;
const VANILLA_JARS = [
  path.join(process.env.USERPROFILE || '', '.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client-extra.jar'),
  path.join(process.env.USERPROFILE || '', '.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar')
];
function loadVanillaKeys() {
  const { execFileSync } = require('child_process');
  for (const jar of VANILLA_JARS) {
    if (!fs.existsSync(jar)) continue;
    try {
      const tmp = path.join(require('os').tmpdir(), 'ftboss-vanilla-en_us.json');
      if (!fs.existsSync(tmp)) {
        execFileSync('tar', ['-xf', jar, '-C', require('os').tmpdir(), 'assets/minecraft/lang/en_us.json']);
        const src = path.join(require('os').tmpdir(), 'assets/minecraft/lang/en_us.json');
        if (!fs.existsSync(src)) continue;
        fs.copyFileSync(src, tmp);
      }
      return new Set(Object.keys(JSON.parse(fs.readFileSync(tmp, 'utf8'))));
    } catch { /* 下一个候选 */ }
  }
  return null;
}
vanillaKeys = loadVanillaKeys();
const isVanilla = k => vanillaKeys ? vanillaKeys.has(k) : false;
const reallyMissingZh = missingZh.filter(k => !isVanilla(k));
const reallyMissingEn = missingEn.filter(k => !isVanilla(k));
const vanillaOnly = missingZh.filter(k => isVanilla(k));
const unusedKeys = Object.keys(zh).filter(k => !haystack.includes(k)).sort();

/* ---- 3) data: 配方/掉落表引用的物品 ---- */
const dataIssues = [];
for (const f of dataFiles.filter(f => f.endsWith('.json'))) {
  const t = fs.readFileSync(f, 'utf8');
  const j = JSON.parse(t);
  const results = [];
  if (j.result && j.result.item) results.push(j.result.item);
  if (j.pools) for (const p of j.pools) for (const e of (p.entries || [])) if (e.name) results.push(e.name);
  for (const r of results) {
    if (!r.startsWith('ftboss:')) continue;
    const id = r.slice('ftboss:'.length);
    const asItem = path.join(assets, 'models/item', id + '.json');
    const asBlock = path.join(assets, 'blockstates', id + '.json');
    if (!fs.existsSync(asItem) && !fs.existsSync(asBlock)) dataIssues.push(`${rel(f)} -> 引用了不存在的 ftboss:${id}`);
  }
}

/* ---- 4) 死代码: 无用 import + 只用过一次的 private 成员(定义处) ---- */
const deadImports = [], deadMembers = [];
for (const f of javaFiles) {
  const t = fs.readFileSync(f, 'utf8');
  const body = t.replace(/^import .*$/gm, '');
  for (const m of t.matchAll(/^import\s+(?:static\s+)?([\w.]+);/gm)) {
    const simple = m[1].split('.').pop();
    if (simple === '*') continue;
    const uses = (body.match(new RegExp('\\b' + simple.replace(/[$]/g, '\\$') + '\\b', 'g')) || []).length;
    if (uses === 0) deadImports.push(`${path.relative(root, f).replace(/\\/g, '/')}: ${m[1]}`);
  }
  for (const m of t.matchAll(/^\s*private\s+(?:static\s+)?(?:final\s+)?[\w<>\[\],.?\s]+?\s+(\w+)\s*(?:=|;|\()/gm)) {
    const name = m[1];
    if (['serialVersionUID'].includes(name)) continue;
    const uses = (t.match(new RegExp('\\b' + name + '\\b', 'g')) || []).length;
    if (uses <= 1) deadMembers.push(`${path.relative(root, f).replace(/\\/g, '/')}: ${name}`);
  }
}

/* ---- 5) 非法模型写法: item 模型的 textures.* 必须是**字符串** ----
   方块模型的面可以写 {"texture": "...", "tintindex": 0}, 但 item 模型(继承 item/generated)的
   textures.layer0 只能是字符串。写成对象会让客户端在 JsonElement.getAsString 抛
   UnsupportedOperationException(JsonObject) → 该物品模型直接变 missing(实测就是石像图标丢失)。 */
const badModels = [];
for (const f of resFiles.filter(f => /models[\\/]item[\\/].*\.json$/.test(f))) {
  try {
    const j = JSON.parse(fs.readFileSync(f, 'utf8'));
    for (const [k, v] of Object.entries(j.textures || {})) {
      if (v !== null && typeof v === 'object') {
        badModels.push(`${rel(f)}: textures.${k} 是对象 —— item 模型只接受字符串(tintindex 由原版按层号 0 自动给)`);
      }
    }
  } catch (e) {
    badModels.push(`${rel(f)}: JSON 解析失败 ${e.message}`);
  }
}

/* ---- 6) 方块有 blockstate 却没有战利品表 -> 挖掉什么都不掉(实测石像就是这么漏的) ---- */
const noLoot = [];
const lootDir = path.join(data, 'loot_tables/blocks');
for (const f of resFiles.filter(f => /blockstates[\\/][^\\/]+\.json$/.test(f))) {
  const id = path.basename(f, '.json');
  if (!fs.existsSync(path.join(lootDir, id + '.json'))) noLoot.push(`${id}（缺 data/ftboss/loot_tables/blocks/${id}.json, 挖掉不会掉落自身）`);
}

/* ---- 7) 方块模型不要引用 textures/entity/** ----
   本整合包装了 ETF/EMF(entity_texture_features/entity_model_features) + ModernFix 动态资源 + Lightspeed,
   方块模型直接引用 `ftboss:entity/xxx` 会渲染成**紫黑 missing**(而且日志里没有任何 missing texture 报错)。
   方块贴图必须复制一份到 `textures/block/` 再引用。 */
const entityTexInBlock = [];
for (const f of resFiles.filter(f => /models[\\/]block[\\/].*\.json$/.test(f))) {
  let j;
  try { j = JSON.parse(fs.readFileSync(f, 'utf8')); } catch { continue; }
  for (const [k, v] of Object.entries(j.textures || {})) {
    if (typeof v === 'string' && /^[a-z0-9_]+:entity\//.test(v)) {
      entityTexInBlock.push(`${rel(f)}: textures.${k} = ${v}（方块模型引用 entity 贴图 → 会紫黑，应复制到 textures/block/）`);
    }
  }
}

/* ---- 输出 ---- */
console.log('===== 全工程体检 =====');
console.log(`java ${javaFiles.length} 个 / 资源 ${resFiles.length} 个 / 数据 ${dataFiles.length} 个`);
console.log(`\n[1] 未被任何 java/资源 引用的资源文件 (${unreferenced.length} 个):`);
for (const u of unreferenced) console.log('    ' + u);
console.log(`\n[2] 代码用到但 zh_cn 缺失的语言键 (${reallyMissingZh.length} 个)${vanillaKeys ? '' : '  ⚠ 未取到原版 lang, 无法区分原版键'}:`);
for (const k of reallyMissingZh) console.log('    ' + k);
console.log(`\n[3] 代码用到但 en_us 缺失的语言键 (${reallyMissingEn.length} 个):`);
for (const k of reallyMissingEn) console.log('    ' + k);
if (vanillaOnly.length) {
  console.log(`\n[3b] 由**原版**提供、本 mod 无需定义的语言键 (${vanillaOnly.length} 个, 不是问题):`);
  for (const k of vanillaOnly) console.log('    ' + k);
}
console.log(`\n[4] lang 里定义但没人用的键 (${unusedKeys.length} 个):`);
for (const k of unusedKeys) console.log('    ' + k);
console.log(`\n[5] data 里引用不到资源的条目 (${dataIssues.length} 个):`);
for (const k of dataIssues) console.log('    ' + k);
console.log(`\n[6] 无用的 import (${deadImports.length} 个):`);
for (const k of deadImports) console.log('    ' + k);
console.log(`\n[7] 疑似只用了一次的 private 成员 (${deadMembers.length} 个, 可能是死代码, 也可能是被反射/序列化调用):`);
for (const k of deadMembers) console.log('    ' + k);
console.log(`\n[8] 非法 item 模型写法 (${badModels.length} 个):`);
for (const k of badModels) console.log('    ' + k);
console.log(`\n[9] 有 blockstate 但缺战利品表的方块 (${noLoot.length} 个):`);
for (const k of noLoot) console.log('    ' + k);
console.log(`\n[10] 方块模型引用 entity 贴图(会紫黑) (${entityTexInBlock.length} 个):`);
for (const k of entityTexInBlock) console.log('    ' + k);
console.log(`\n[11] 开发残留文件混在运行时资源里 (${devArtifacts.length} 个) —— 必须清出, 否则会被打进 jar:`);
for (const f of devArtifacts) console.log('    ' + rel(f));
if (devArtifacts.length) console.log('    → 用 tools/model3d/dev_resource_cleanup.cjs --move 搬到 _dev_backup/ (只搬运不删除)');
console.log(`\n[12] 按"命名约定"被引用的资源 (${byConvention.length} 个, **不是死资源**, 静态扫描看不到属正常):`);
for (const k of byConvention) console.log('    ' + k);
if (keyFragments.size) {
  console.log(`\n[13] 疑似字符串拼接的语言键片段 (${keyFragments.size} 个, 已从缺失键统计中剔除, 请人工确认):`);
  for (const k of keyFragments) console.log('    ' + JSON.stringify(k));
}
console.log(`\n汇总: 无引用资源 ${unreferenced.length} / 缺键 zh ${reallyMissingZh.length} en ${reallyMissingEn.length} / 冗余键 ${unusedKeys.length} / data 问题 ${dataIssues.length} / 无用import ${deadImports.length} / 疑似死成员 ${deadMembers.length} / 非法模型 ${badModels.length} / 缺战利品表 ${noLoot.length} / entity贴图误用 ${entityTexInBlock.length} / 开发残留 ${devArtifacts.length}`);
