#!/usr/bin/env node
// 运行时资源清场: 把 `src/main/resources` 里的**开发备份文件**移到 `_dev_backup/resources-<日期>/`。
//
// 用法:
//   node dev_resource_cleanup.cjs --check        # 只列清单 (默认)
//   node dev_resource_cleanup.cjs --move         # 真正搬走 (保留原相对路径, 不删除)
//   node dev_resource_cleanup.cjs --check --json
//
// 为什么必须清 (2026-09-16 实测, 不是推测):
//   打开上一版 `build/libs/qys-bosses.jar` 可见里面真的带着 8 个开发备份:
//     assets/ftboss/models/item/shadow_sword.json.{before_grip_fix,grip,tune}.bak
//     assets/ftboss/models/item/turkey_legs.json.tilted-bak
//     assets/ftboss/textures/item/shadow_sword{,_ready}.png.{dedens,lumfix}.bak
//   共 61,724 B。原因是 `.gitignore` 里的 `*.bak` 只挡 git, 挡不住 Gradle 的 processResources。
//   本轮又新增 8 个 `.reorder.bak` / `.keyorder.bak`, 合计 566,444 B 会随下一次构建进包。
//
// 本脚本的边界 (按规格"检查器不得自动修改正式资源"):
//   - 只处理**文件名后缀**能确定性判定为开发残留的: .bak / .tilted-bak / .orig / .old / ~
//   - **只搬运不删除**: 目标目录保留原相对路径, 随时可搬回。
//   - 绝不处理正式资源 (尤其 *_e.png 与 *_glowmask.png 是**正式**发光资源, 不在名单里)。
//   - 不猜测"未引用"就去删: 那类只在报告里列为 review, 由人确认。
const fs = require('fs');
const path = require('path');

const argv = process.argv.slice(2);
const doMove = argv.includes('--move');
const asJson = argv.includes('--json');

const ROOT = path.resolve(__dirname, '../..');
const RES = path.join(ROOT, 'src/main/resources');
const stamp = new Date().toISOString().slice(0, 10);
const DEST = path.join(ROOT, '_dev_backup', 'resources-' + stamp);

// 只按"文件名后缀"判定, 不做内容/引用分析 (那属于 review, 不属于自动动作)
const PATTERNS = [
  /\.bak$/, /\.bak2$/, /\.tilted-bak$/,
  /\.orig$/, /\.old$/, /\.prev$/,
  /~$/, /\.swp$/
];

function walk(dir, out = []) {
  if (!fs.existsSync(dir)) return out;
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const q = path.join(dir, e.name);
    if (e.isDirectory()) walk(q, out); else out.push(q);
  }
  return out;
}

const files = walk(RES);
const hits = files.filter(f => PATTERNS.some(re => re.test(path.basename(f))));

const report = hits.map(f => {
  const rel = path.relative(RES, f).replace(/\\/g, '/');
  return { rel, size: fs.statSync(f).size, from: f, to: path.join(DEST, rel) };
});

if (doMove) {
  for (const r of report) {
    fs.mkdirSync(path.dirname(r.to), { recursive: true });
    fs.renameSync(r.from, r.to);
  }
}

if (asJson) {
  console.log(JSON.stringify({ dest: DEST, moved: doMove, count: report.length, totalBytes: report.reduce((a, r) => a + r.size, 0), files: report }, null, 2));
} else {
  console.log(`运行时资源目录: ${path.relative(ROOT, RES)}`);
  console.log(`开发残留备份 ${report.length} 个, 共 ${report.reduce((a, r) => a + r.size, 0)} B`);
  for (const r of report) console.log(`  ${String(r.size).padStart(8)}  ${r.rel}`);
  if (doMove) {
    console.log(`\n已搬运到: ${path.relative(ROOT, DEST)} (保留原相对路径, 未删除)`);
  } else if (report.length) {
    console.log('\n(只读模式; 加 --move 才会搬走)');
  } else {
    console.log('\n运行时资源目录干净, 无开发残留 ✓');
  }
}
