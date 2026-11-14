// 两首 ogg 的"响度包络"对比 + 交叉相关, 用来判断"序曲素材有没有出现在另一首里 / 出现在哪一段"
// 用法: node compare_ogg.cjs <A.ogg> <B.ogg> [hopMs=100] [stepSec=2]
const fs = require('fs');
const { OggVorbisDecoder } = require('@wasm-audio-decoders/ogg-vorbis');

async function decode(file) {
  const dec = new OggVorbisDecoder();
  await dec.ready;
  const res = await dec.decodeFile(fs.readFileSync(file));
  await dec.free();
  return res;
}

function envelope(res, hopMs) {
  const sr = res.sampleRate, ch = res.channelData, n = res.samplesDecoded;
  const hop = Math.round(sr * hopMs / 1000);
  const frames = Math.floor(n / hop);
  const env = new Float64Array(frames);
  for (let f = 0; f < frames; f++) {
    let sum = 0, cnt = 0;
    for (let i = f * hop; i < (f + 1) * hop; i++) {
      for (let c = 0; c < ch.length; c++) { const v = ch[c][i]; sum += v * v; cnt++; }
    }
    env[f] = Math.sqrt(sum / cnt);
  }
  return { env, hopMs, sr, seconds: n / sr };
}

function profile(tag, e, stepSec) {
  const per = Math.max(1, Math.round(stepSec * 1000 / e.hopMs));
  const out = [];
  for (let f = 0; f + per <= e.env.length; f += per) {
    let s = 0;
    for (let k = f; k < f + per; k++) s += e.env[k] * e.env[k];
    const rms = Math.sqrt(s / per);
    out.push(`${(f * e.hopMs / 1000).toFixed(0)}s ${(rms <= 1e-9 ? -120 : 20 * Math.log10(rms)).toFixed(1)}`);
  }
  console.log(`--- ${tag} 包络(每 ${stepSec}s) ---`);
  for (let i = 0; i < out.length; i += 8) console.log('  ' + out.slice(i, i + 8).join('  '));
}

function bestMatches(a, b, maxOffsetSec) {
  const maxOff = Math.round(maxOffsetSec * 1000 / b.hopMs);
  const len = a.env.length;
  const res = [];
  for (let off = 0; off + len <= b.env.length && off <= maxOff; off++) {
    let dot = 0, na = 0, nb = 0;
    for (let i = 0; i < len; i++) {
      const x = a.env[i], y = b.env[off + i];
      dot += x * y; na += x * x; nb += y * y;
    }
    const r = (na > 0 && nb > 0) ? dot / Math.sqrt(na * nb) : 0;
    res.push({ offSec: off * b.hopMs / 1000, r });
  }
  res.sort((p, q) => q.r - p.r);
  return res.slice(0, 6);
}

(async () => {
  const [fa, fb, hopArg, stepArg] = process.argv.slice(2);
  const hopMs = parseFloat(hopArg || '100');
  const stepSec = parseFloat(stepArg || '2');
  const A = await decode(fa);
  const B = await decode(fb);
  const ea = envelope(A, hopMs), eb = envelope(B, hopMs);
  console.log(`A=${fa} ${ea.seconds.toFixed(2)}s   B=${fb} ${eb.seconds.toFixed(2)}s   hop=${hopMs}ms`);
  profile('A', ea, stepSec);
  profile('B', eb, stepSec);
  console.log(`--- A 的包络在 B 里最像的位置(归一化相关, 只看前 130s) ---`);
  for (const m of bestMatches(ea, eb, 130)) {
    console.log(`   B @ ${m.offSec.toFixed(1)}s   r=${m.r.toFixed(4)}`);
  }
})();
