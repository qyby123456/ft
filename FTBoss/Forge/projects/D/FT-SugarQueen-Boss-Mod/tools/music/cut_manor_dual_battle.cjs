// 从 manor_dual.ogg(双轨坦克曲 4:16) 切出【双轨·战斗高潮】(1:40 → 4:08); 1:40 是选段起点, 不是约 46 秒前奏的结束点
// 用法: node cut_manor_dual_battle.cjs <in.ogg> <out.ogg> [startSec=100] [endSec=248] [quality=0.6]
// 说明: 纯 Node —— @wasm-audio-decoders/ogg-vorbis 解码 -> 切片 -> 写 WAV -> wav2ogg.cjs 编码(不依赖 ffmpeg)
const fs = require('fs');
const path = require('path');
const { OggVorbisDecoder } = require('@wasm-audio-decoders/ogg-vorbis');
const { encode } = require('./wav2ogg.cjs');

function writeWav16(file, chans, sr) {
  const n = chans[0].length, ch = chans.length;
  const dataLen = n * ch * 2;
  const buf = Buffer.alloc(44 + dataLen);
  buf.write('RIFF', 0, 'ascii');
  buf.writeUInt32LE(36 + dataLen, 4);
  buf.write('WAVE', 8, 'ascii');
  buf.write('fmt ', 12, 'ascii');
  buf.writeUInt32LE(16, 16);
  buf.writeUInt16LE(1, 20);              // PCM
  buf.writeUInt16LE(ch, 22);
  buf.writeUInt32LE(sr, 24);
  buf.writeUInt32LE(sr * ch * 2, 28);    // byte rate
  buf.writeUInt16LE(ch * 2, 32);         // block align
  buf.writeUInt16LE(16, 34);
  buf.write('data', 36, 'ascii');
  buf.writeUInt32LE(dataLen, 40);
  let o = 44;
  for (let i = 0; i < n; i++) {
    for (let c = 0; c < ch; c++) {
      const v = Math.max(-1, Math.min(1, chans[c][i]));
      buf.writeInt16LE(Math.round(v * 32767), o); o += 2;
    }
  }
  fs.writeFileSync(file, buf);
  return buf.length;
}

(async () => {
  const [inFile, outFile, startArg, endArg, qArg] = process.argv.slice(2);
  const start = parseFloat(startArg || '100');
  const end = parseFloat(endArg || '248');
  const q = parseFloat(qArg || '0.6');

  const dec = new OggVorbisDecoder();
  await dec.ready;
  const res = await dec.decodeFile(fs.readFileSync(inFile));
  await dec.free();
  const sr = res.sampleRate, ch = res.channelData, n = res.samplesDecoded;
  const i0 = Math.round(start * sr), i1 = Math.min(n, Math.round(end * sr));
  console.log(`in=${inFile} ${(n / sr).toFixed(2)}s sr=${sr} ch=${ch.length}`);
  console.log(`切 [${start}s, ${end}s] -> ${((i1 - i0) / sr).toFixed(2)}s`);

  const sliced = ch.map(a => a.subarray(i0, i1));
  /* 末尾 0.6 秒做一点淡出, 免得接下一首时"啪"一下 */
  const fade = Math.round(0.6 * sr);
  for (const a of sliced) {
    for (let i = 0; i < fade && i < a.length; i++) a[a.length - 1 - i] *= i / fade;
  }
  const wavPath = path.join(path.dirname(outFile), path.basename(outFile, path.extname(outFile)) + '.wav');
  console.log(`wav=${wavPath} ${writeWav16(wavPath, sliced, sr)} B`);

  const wav = { channels: sliced.length, sampleRate: sr, frames: sliced[0].length, data: sliced };
  const ogg = encode(wav, q, { TITLE: path.basename(outFile, path.extname(outFile)), COMMENT: 'manor dual battle part (overture removed)' });
  fs.writeFileSync(outFile, ogg);
  console.log(`ogg=${outFile} ${ogg.length} B  magic=${ogg.toString('ascii', 0, 4)}  quality=${q}`);
})();
