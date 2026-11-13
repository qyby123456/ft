import { readFileSync, writeFileSync, existsSync, readdirSync, mkdirSync } from 'node:fs';
import { createHash } from 'node:crypto';
import { resolve, join, relative } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const work = join(root, 'output/room-expansion-v035');
const plan = JSON.parse(readFileSync(join(work, 'plan.json'), 'utf8'));
const escape = text => String(text).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('"', '&quot;');
const hash = filename => createHash('sha256').update(readFileSync(filename)).digest('hex');
const formal = join(root, 'src/main/resources/data/fteclipsegen/roomsets');
const list = directory => readdirSync(directory, { withFileTypes: true }).flatMap(entry => entry.isDirectory() ? list(join(directory, entry.name)) : [join(directory, entry.name)]);
const snapshot = join(work, 'formal-baseline.json');
if (!existsSync(snapshot)) writeFileSync(snapshot, JSON.stringify(Object.fromEntries(list(formal).map(filename => [relative(formal, filename).replaceAll('\\', '/'), hash(filename)])), null, 2) + '\n');
const before = JSON.parse(readFileSync(snapshot, 'utf8'));
const changes = Object.entries(before).filter(([filename, digest]) => hash(join(formal, filename)) !== digest).map(([filename]) => filename);
if (changes.length) throw new Error('Formal room resources changed: ' + changes.join(', '));
let accepted = 0;
const batches = [];
for (let batch = 1; batch <= 5; batch++) {
  const reportPath = join(work, `batch-${String(batch).padStart(2, '0')}-acceptance.json`);
  const report = existsSync(reportPath) ? JSON.parse(readFileSync(reportPath, 'utf8')) : null;
  const passed = report?.status === 'PASS' && report.rooms.length === 16;
  if (passed) accepted += 16;
  batches.push({ batch, status: passed ? 'ACCEPTED' : 'PENDING', acceptedRooms: passed ? 16 : 0 });
}
const cards = plan.threads.map(style => {
  const rows = style.rooms.map(room => {
    const batchDirectory = `${style.style}/batch-${String(room.batch).padStart(2, '0')}`;
    const staged = existsSync(join(work, batchDirectory, 'freeze.json'));
    const passed = batches[room.batch - 1].status === 'ACCEPTED';
    return `<tr><td>${room.batch}</td><td><strong>${escape(room.name)}</strong><details><summary>建筑提示词</summary><p>${escape(room.prompt)}</p><code>${escape(room.key)} · ${escape(room.baseKey)}</code></details></td><td>${passed ? '父线程验收通过' : staged ? '已冻结，待父线程验收' : '待制作'}</td><td>${staged ? `<a href="${batchDirectory}/four-views.html">四视图</a> · <a href="${batchDirectory}/self-audit.json">自检</a>` : `<a href="${style.style}/batch-${String(room.batch).padStart(2, '0')}-prompt.md">批次提示词</a>`}</td></tr>`;
  }).join('');
  return `<section id="${style.style}"><h2>${escape(style.title.replace('地牢全房独立重建：', ''))}</h2><table><thead><tr><th>批次</th><th>房间与设计</th><th>状态</th><th>资料</th></tr></thead><tbody>${rows}</tbody></table></section>`;
}).join('');
const html = `<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>八风格新增80房间 · 分批制作</title><style>body{margin:0;background:#10161e;color:#e7edf3;font:16px/1.65 system-ui,sans-serif}main{max-width:1320px;margin:auto;padding:40px 28px}h1{font-size:34px}p{color:#becbd7}nav{display:flex;gap:18px;flex-wrap:wrap}a{color:#80ceef}section{margin:34px 0;padding:22px;background:#1a2430;border-radius:14px}table{width:100%;border-collapse:collapse}th,td{border-bottom:1px solid #334353;padding:12px;text-align:left;vertical-align:top}td:nth-child(2){width:56%}details{font-size:14px;color:#bacbd8}code{font-size:12px}.progress{padding:20px;background:#23384a;border-radius:12px;font-size:22px}summary{cursor:pointer}footer{color:#a9bac9}</style><main><h1>八风格 · 新增80个独立房间</h1><p>全部逐房提示词已完成。五批制作，每批八风格各两间；完成四视图、风格与重复度检查后，由父线程复核再推进。此页面展示新增素材制作进度。</p><div class="progress">父线程验收通过：${accepted} / 80 间</div><p>每风格原有27间保留，新增10间采用独立编号。门口、刷怪位和结构尺寸沿用生成器接口，房间建筑按各自用途重新制作。</p><nav>${plan.threads.map(style => `<a href="#${style.style}">${escape(style.title.replace('地牢全房独立重建：', ''))}</a>`).join('')}<a href="全部房间提示词.md">80间完整提示词</a></nav>${cards}<footer>正式0.34房间资源 ${Object.keys(before).length} 个文件 SHA256保持一致。新增素材尚未声明部署；实际生成器接入与发布单独验收。</footer></main></html>`;
mkdirSync(work, { recursive: true });
writeFileSync(join(work, 'index.html'), html);
writeFileSync(join(work, 'progress.json'), JSON.stringify({ schema: 1, accepted, target: 80, batches, formalResourcesUnchanged: true }, null, 2) + '\n');
process.stdout.write(JSON.stringify({ accepted, target: 80, formalFilesUnchanged: Object.keys(before).length }) + '\n');
