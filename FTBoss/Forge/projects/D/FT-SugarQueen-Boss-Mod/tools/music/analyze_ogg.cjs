// 用纯 Node 解码 ogg(基于 @wasm-audio-decoders/ogg-vorbis) 并按时段报能量, 用来定位段落边界
// 用法: node analyze_ogg.cjs <in.ogg> [stepSec=1] [fromSec=0] [toSec=-1]
const fs = require('fs');
const { OggVorbisDecoder } = require('@wasm-audio-decoders/ogg-vorbis');

function db(x) { return x <= 1e-9 ? -120 : 20 * Math.log10(x); }

(async () => {
  const file = process.argv[2];
  const step = parseFloat(process.argv[3] || '1');
  const from = parseFloat(process.argv[4] || '0');
  const toArg = parseFloat(process.argv[5] || '-1');
  const dec = new OggVorbisDecoder();
  await dec.ready;
  const res = await dec.decodeFile(fs.readFileSync(file));
  await dec.free();
  const sr = res.sampleRate;
  const ch = res.channelData;
  const n = res.samplesDecoded;
  const to = toArg > 0 ? Math.min(toArg, n / sr) : n / sr;
  console.log(`file=${file} sr=${sr} ch=${ch.length} seconds=${(n / sr).toFixed(2)}`);
  let peak = 0;
  for (let c = 0; c < ch.length; c++) for (let i = 0; i < n; i++) { const a = Math.abs(ch[c][i]); if (a > peak) peak = a; }
  console.log(`peak=${peak.toFixed(4)} (${db(peak).toFixed(2)} dBFS)`);
  console.log('t(s)   RMS(dB)  峰值(dB)  条形');
  for (let t = from; t < to; t += step) {
    const i0 = Math.round(t * sr), i1 = Math.min(n, Math.round((t + step) * sr));
    if (i1 <= i0) break;
    let sum = 0, cnt = 0, pk = 0;
    for (let i = i0; i < i1; i++) {
      for (let c = 0; c < ch.length; c++) {
        const v = ch[c][i];
        sum += v * v; cnt++;
        const a = Math.abs(v); if (a > pk) pk = a;
      }
    }
    const rms = Math.sqrt(sum / cnt);
    const d = db(rms);
    const bars = Math.max(0, Math.min(60, Math.round((d + 60) * 1.5)));
    console.log(`${t.toFixed(1).padStart(6)}  ${d.toFixed(1).padStart(6)}  ${db(pk).toFixed(1).padStart(7)}  ${'#'.repeat(bars)}`);
  }
})();
