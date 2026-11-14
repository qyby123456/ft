const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { spawnSync } = require('node:child_process');

const [jar, rendered, output, staging, ffmpeg] = process.argv.slice(2).map(value => path.resolve(value));
const listening = path.join(output, '00_直接试听_mp3');
const added = path.join(output, '01_新增_鱼王双阶段');
const complete = path.join(output, '02_模组内全部音频');
const comparison = path.join(output, '99_对比说明');
for (const directory of [listening, added, complete, comparison, staging]) fs.mkdirSync(directory, { recursive: true });
function run(command, argumentsList) {
  const result = spawnSync(command, argumentsList, { encoding: 'utf8', maxBuffer: 16 * 1024 * 1024 });
  if (result.error || result.status !== 0) throw new Error(result.error?.message || result.stderr || result.stdout);
  return result.stdout;
}
run('tar.exe', ['-xf', jar, '-C', staging, 'assets/ftboss/sounds']);
const soundRoot = path.join(staging, 'assets/ftboss/sounds');
function files(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => entry.isDirectory()
    ? files(path.join(directory, entry.name)) : [path.join(directory, entry.name)]);
}
const inventory = [];
for (const source of files(soundRoot).filter(file => file.endsWith('.ogg')).sort()) {
  const relative = path.relative(soundRoot, source);
  const destination = path.join(complete, relative);
  const mp3 = path.join(listening, relative.replace(/\.ogg$/, '.mp3'));
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  fs.mkdirSync(path.dirname(mp3), { recursive: true });
  const unchanged = fs.existsSync(destination) && fs.existsSync(mp3)
    && fs.readFileSync(source).equals(fs.readFileSync(destination));
  fs.copyFileSync(source, destination);
  if (!unchanged) run(ffmpeg, ['-hide_banner', '-loglevel', 'error', '-y', '-i', source, '-c:a', 'libmp3lame', '-b:a', '320k', '-map_metadata', '-1', mp3]);
  const decoded = JSON.parse(run(process.execPath, [path.join(__dirname, 'verify-ogg.cjs'), destination]));
  if (decoded.errors.length) throw new Error('Invalid audio: ' + relative);
  inventory.push({ file: relative.replaceAll('\\', '/'), seconds: decoded.seconds, peak: decoded.peak, rms: decoded.rms,
    sha256: crypto.createHash('sha256').update(fs.readFileSync(destination)).digest('hex') });
  console.log('Exported', relative, decoded.seconds + 's', 'peak=' + decoded.peak);
}
for (const name of ['tide_fish', 'tide_fish_p2']) {
  for (const extension of ['wav', 'ogg', 'mp3', 'mid', 'json']) fs.copyFileSync(path.join(rendered, name + '.' + extension), path.join(added, name + '.' + extension));
  const original = fs.readFileSync(path.join(added, name + '.ogg'));
  if (!original.equals(fs.readFileSync(path.join(complete, 'music', name + '.ogg')))) throw new Error('Export does not match deployed jar: ' + name);
}
fs.copyFileSync(path.join(__dirname, 'render_tide_fish.mjs'), path.join(added, 'render_tide_fish.mjs'));
fs.copyFileSync(path.resolve(__dirname, '../../docs/tide-fish-rework.md'), path.join(output, '鱼王重制说明.md'));
const jarHash = crypto.createHash('sha256').update(fs.readFileSync(jar)).digest('hex');
fs.writeFileSync(path.join(output, '音频校验清单.json'), JSON.stringify({ jar, jarHash, inventory }, null, 2) + '\n');
fs.writeFileSync(path.join(comparison, '说明.txt'), '旧版鱼王共用三幕剧兜底音乐，没有独立鱼王曲目；本次两首为新增原创曲，未覆盖旧曲。旧曲仍在全量音频目录保留。\n');
const warnings = inventory.filter(entry => entry.peak > 1).map(entry => `${entry.file}: ${entry.peak}`);
fs.writeFileSync(path.join(output, '说明.txt'), [
  '潮宴鱼王双阶段音乐 / 2026-09-17',
  '00_直接试听_mp3：全部音频的 320 kbps MP3，Windows 可直接播放。',
  '01_新增_鱼王双阶段：两首新曲的 WAV、OGG、MP3、MIDI、渲染源码和参数。',
  '02_模组内全部音频：从本次已部署 JAR 原样提取，保留 music/effects 层级。',
  '99_对比说明：旧版鱼王没有独立曲目，使用公共三幕剧。',
  '沧鳞·潮宴：99.00 秒；沧鳞·怒潮：96.33 秒。新曲解码峰值均小于 1.0。',
  '新曲渲染：node render_tide_fish.mjs <含spessasynth_core及MuseScore_General.sf3的目录> <输出目录> <ffmpeg.exe>',
  '已验证 OGG 解码、时长与峰值，未做游戏内主观试听。',
  '原有曲目的过冲按原样保留，不在本次任务修改：' + (warnings.join('；') || '无'),
  'JAR SHA256：' + jarHash,
  ...inventory.map(entry => `${entry.file}\t${entry.seconds}s\tpeak=${entry.peak}\trms=${entry.rms}`)
].join('\n') + '\n');
console.log('Export complete:', output, inventory.length, 'audio files');
