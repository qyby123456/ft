// 在轮转日志（*.log.gz）里搜黑切命中/血怒记录 —— 需要往前翻局时用
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const dir = process.argv[2];
const pats = process.argv.slice(3);
const files = fs.readdirSync(dir).filter(f => f.endsWith('.gz')).sort();

for (const f of files) {
  let text;
  try { text = zlib.gunzipSync(fs.readFileSync(path.join(dir, f))).toString('utf8'); }
  catch (e) { console.log(f + '  解压失败: ' + e.message); continue; }
  const lines = text.split('\n');
  const hits = lines.filter(l => pats.some(p => l.includes(p)));
  console.log('=== ' + f + '  共 ' + lines.length + ' 行，命中 ' + hits.length + ' 条 ===');
  for (const l of hits.slice(-25)) console.log('  ' + l.slice(0, 220));
}
