// import_item_model.cjs -- 把一个 Blockbench 导出的 "Java Block/Item" 模型导入为 ftboss 物品模型
//
// 用法:
//   node tools/model3d/import_item_model.cjs <input.json> <item_name> [--png <input.png>]
//     [--parent minecraft:item/handheld] [--no-parent] [--keep-bedrock-keys]
//
// 行为:
//   1. 校验输入是 Java Block/Item 形态 (elements + textures, 非 minecraft:geometry/bones)
//   2. 删除 Java 不认识的 Bedrock 专用键: format_version / credit / texture_size / display.on_shelf
//   3. 把贴图引用命名空间化 -> ftboss:item/<item_name>  (faces 里的 #key 引用保持 key 名不变)
//   4. 备份现有模型到 ref/<item_name>_prev.json.bak, 再写入 assets/ftboss/models/item/<item_name>.json
//   5. 可选复制贴图 PNG (--png)
//   6. 自检: UV 是否越界、面引用是否能解析、贴图文件是否存在、display 槽位清单
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..');
const RES = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');
const MODEL_DIR = path.join(RES, 'models', 'item');
const TEX_DIR = path.join(RES, 'textures', 'item');
const REF_DIR = path.join(ROOT, 'ref');

const BEDROCK_ONLY_TOP = ['format_version', 'credit', 'texture_size', 'groups'];
const BEDROCK_ONLY_DISPLAY = ['on_shelf'];
const JAVA_TOP_KEYS = ['parent', 'ambientocclusion', 'display', 'textures', 'elements', 'gui_light', 'overrides'];
const JAVA_DISPLAY_KEYS = ['thirdperson_righthand', 'thirdperson_lefthand', 'firstperson_righthand', 'firstperson_lefthand', 'gui', 'ground', 'fixed', 'head'];

function fail(msg) { console.error('ERROR: ' + msg); process.exit(1); }

const argv = process.argv.slice(2);
const pos = [];
// 默认**不加 parent**: Blockbench 导出的 Java 物品模型自带 display, 不需要父模型。
// ★ 千万不要默认挂 `minecraft:item/handheld` / `item/generated` —— 那条父链的根是 `builtin/generated`,
//   MC 会走"生成式物品"烘焙(读 #layer0), **忽略模型自己的 elements**; 模型里若没有 layer0 就一个面都没有,
//   游戏里表现为**物品完全看不见, 而且日志零报错**(2026-09-15 大肉腿踩过; 隔壁会话那份 3D 备份同样因此被弃用)。
//   要用父模型就显式 --parent, 并且必须同时提供 layer0。
const opt = { png: null, parent: null, keepBedrock: false };
for (let i = 0; i < argv.length; i++) {
  const a = argv[i];
  if (a === '--png') opt.png = argv[++i];
  else if (a === '--parent') opt.parent = argv[++i];
  else if (a === '--no-parent') opt.parent = null;
  else if (a === '--keep-bedrock-keys') opt.keepBedrock = true;
  else pos.push(a);
}
if (pos.length < 2) fail('用法: node tools/model3d/import_item_model.cjs <input.json> <item_name> [--png <in.png>] [--parent <id>|--no-parent]');
const [srcPath, itemName] = pos;
if (!fs.existsSync(srcPath)) fail('找不到输入模型: ' + srcPath);
if (!/^[a-z0-9_]+$/.test(itemName)) fail('item_name 必须是小写字母/数字/下划线: ' + itemName);

const raw = fs.readFileSync(srcPath, 'utf8');
let src;
try { src = JSON.parse(raw); } catch (e) { fail('输入不是合法 JSON: ' + e.message); }

// --- 1. 形态校验 ---
if (src['minecraft:geometry'] || src.bones) fail('这看起来是 Bedrock geometry (minecraft:geometry/bones)，本工具只处理 Java Block/Item 模型');
if (!Array.isArray(src.elements) || src.elements.length === 0) fail('缺少 elements[]，不是 Java Block/Item 模型');
if (!src.textures || typeof src.textures !== 'object') fail('缺少 textures{}');

// --- 2. 命名空间化贴图引用 ---
const nsTex = {};
for (const [k, v] of Object.entries(src.textures)) {
  nsTex[k] = (typeof v === 'string' && v.includes(':')) ? v : 'ftboss:item/' + itemName;
}
const faceRefs = new Set();
let uvWarn = [];
src.elements.forEach((el, i) => {
  if (el.from && el.from.length !== 3) uvWarn.push('element[' + i + '] from 不是 3 维');
  if (el.to && el.to.length !== 3) uvWarn.push('element[' + i + '] to 不是 3 维');
  Object.entries(el.faces || {}).forEach(([fk, f]) => {
    if (f.texture) faceRefs.add(f.texture);
    if (Array.isArray(f.uv)) {
      if (f.uv.length !== 4) uvWarn.push('element[' + i + '].' + fk + ' uv 不是 4 个值');
      else if (f.uv.some(n => typeof n !== 'number' || n < -0.01 || n > 16.01)) uvWarn.push('element[' + i + '].' + fk + ' uv 超出 0..16: ' + JSON.stringify(f.uv));
    }
    if (f.rotation !== undefined && ![0, 90, 180, 270].includes(f.rotation)) uvWarn.push('element[' + i + '].' + fk + ' uv 旋转非法: ' + f.rotation);
  });
});
for (const r of faceRefs) {
  if (!r.startsWith('#')) { uvWarn.push('面贴图引用没有 # 前缀: ' + r); continue; }
  const key = r.slice(1);
  if (!(key in nsTex)) fail('面引用了未定义的贴图键 #' + key + ' (可用: ' + Object.keys(nsTex).join(',') + ')');
}

// --- 3. 组装 Java 干净的模型 ---
const out = {};
if (opt.parent) out.parent = opt.parent;
if (src.ambientocclusion !== undefined) out.ambientocclusion = src.ambientocclusion;
if (src.gui_light !== undefined) out.gui_light = src.gui_light;
out.textures = nsTex;
out.elements = src.elements;
if (src.display) {
  const d = {};
  for (const [k, v] of Object.entries(src.display)) {
    if (!opt.keepBedrock && BEDROCK_ONLY_DISPLAY.includes(k)) continue;
    if (!JAVA_DISPLAY_KEYS.includes(k)) { if (!opt.keepBedrock) { console.log('  [跳过] 非 Java display 槽位: ' + k); continue; } }
    d[k] = v;
  }
  if (Object.keys(d).length) out.display = d;
}
const droppedTop = Object.keys(src).filter(k => !JAVA_TOP_KEYS.includes(k));
if (opt.keepBedrock) for (const k of droppedTop) out[k] = src[k];

// --- 4. 备份 + 写盘 ---
const outPath = path.join(MODEL_DIR, itemName + '.json');
fs.mkdirSync(MODEL_DIR, { recursive: true });
fs.mkdirSync(REF_DIR, { recursive: true });
if (fs.existsSync(outPath)) {
  const bak = path.join(REF_DIR, itemName + '_prev.json.bak');
  fs.copyFileSync(outPath, bak);
  console.log('已备份原模型 -> ' + path.relative(ROOT, bak) + '  (' + fs.statSync(outPath).size + 'B)');
}
fs.writeFileSync(outPath, JSON.stringify(out, null, 2) + '\n', 'utf8');

// --- 5. 可选贴图 ---
let texInfo = '';
const texPath = path.join(TEX_DIR, itemName + '.png');
if (opt.png) {
  if (!fs.existsSync(opt.png)) fail('找不到贴图: ' + opt.png);
  const old = fs.existsSync(texPath) ? fs.statSync(texPath).size : -1;
  fs.copyFileSync(opt.png, texPath);
  texInfo = '贴图已覆盖 (' + old + 'B -> ' + fs.statSync(texPath).size + 'B)';
} else {
  texInfo = fs.existsSync(texPath) ? '贴图沿用现有 (' + fs.statSync(texPath).size + 'B)' : '!! 贴图缺失: ' + path.relative(ROOT, texPath);
}

// --- 6. 报告 ---
console.log('模型已写入: ' + path.relative(ROOT, outPath) + '  (' + fs.statSync(outPath).size + 'B)');
console.log('  parent      : ' + (out.parent || '(无, display 全权定义)'));
console.log('  gui_light   : ' + (out.gui_light || '(未设)'));
console.log('  textures    : ' + JSON.stringify(out.textures));
console.log('  elements    : ' + out.elements.length + ' 个部件');
console.log('  display 槽位: ' + (out.display ? Object.keys(out.display).join(', ') : '(无)'));
if (droppedTop.length && !opt.keepBedrock) console.log('  已删除的 Bedrock 专用键: ' + droppedTop.join(', ') + '  (Java 不识别)');
console.log('  ' + texInfo);
const allX = [], allY = [], allZ = [];
out.elements.forEach(el => { allX.push(el.from[0], el.to[0]); allY.push(el.from[1], el.to[1]); allZ.push(el.from[2], el.to[2]); });
console.log('  包围盒(模型空间): x ' + Math.min(...allX).toFixed(2) + '..' + Math.max(...allX).toFixed(2)
  + '  y ' + Math.min(...allY).toFixed(2) + '..' + Math.max(...allY).toFixed(2)
  + '  z ' + Math.min(...allZ).toFixed(2) + '..' + Math.max(...allZ).toFixed(2) + '  (物品模型正常范围约 -16..32)');
if (uvWarn.length) { console.log('  !! UV/几何告警 ' + uvWarn.length + ' 条:'); uvWarn.slice(0, 20).forEach(w => console.log('     - ' + w)); }
else console.log('  UV/几何自检: 通过');
