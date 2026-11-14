// 频谱体检: 分时段输出各频段能量占比 + 谱心
const fs = require('fs');

function readWav(file) {
  const buf = fs.readFileSync(file);
  let pos = 12, fmt = null, dataOffset = -1, dataLen = 0;
  while (pos + 8 <= buf.length) {
    const id = buf.toString('ascii', pos, pos + 4);
    const size = buf.readUInt32LE(pos + 4);
    const body = pos + 8;
    if (id === 'fmt ') fmt = { ch: buf.readUInt16LE(body + 2), sr: buf.readUInt32LE(body + 4), bits: buf.readUInt16LE(body + 14) };
    else if (id === 'data') { dataOffset = body; dataLen = size; }
    pos = body + size + (size % 2);
  }
  const frames = Math.floor(dataLen / (fmt.ch * fmt.bits / 8));
  const L = new Float32Array(frames), R = new Float32Array(frames);
  for (let i = 0; i < frames; i++) {
    L[i] = buf.readInt16LE(dataOffset + i * fmt.ch * 2) / 32768;
    if (fmt.ch > 1) R[i] = buf.readInt16LE(dataOffset + i * fmt.ch * 2 + 2) / 32768;
  }
  return { L, R, sr: fmt.sr, frames };
}

// 迭代 FFT (radix-2, 就地)
function fft(re, im) {
  const n = re.length;
  for (let i = 1, j = 0; i < n; i++) {
    let bit = n >> 1;
    for (; j & bit; bit >>= 1) j ^= bit;
    j ^= bit;
    if (i < j) { let t = re[i]; re[i] = re[j]; re[j] = t; t = im[i]; im[i] = im[j]; im[j] = t; }
  }
  for (let len = 2; len <= n; len <<= 1) {
    const ang = -2 * Math.PI / len;
    for (let i = 0; i < n; i += len) {
      for (let k = 0; k < len / 2; k++) {
        const wr = Math.cos(ang * k), wi = Math.sin(ang * k);
        const ur = re[i + k], ui = im[i + k];
        const vr = re[i + k + len / 2] * wr - im[i + k + len / 2] * wi;
        const vi = re[i + k + len / 2] * wi + im[i + k + len / 2] * wr;
        re[i + k] = ur + vr; im[i + k] = ui + vi;
        re[i + k + len / 2] = ur - vr; im[i + k + len / 2] = ui - vi;
      }
    }
  }
}

const BANDS = [[0, 40], [40, 80], [80, 160], [160, 320], [320, 640], [640, 1280], [1280, 2560], [2560, 5120], [5120, 11025]];

const file = process.argv[2];
const w = readWav(file);
const W = 8192;
const times = [];
for (let t = 0; t + W / w.sr < w.frames / w.sr; t += 10) times.push(t);

const rows = [];
for (const t of times) {
  const off = Math.round(t * w.sr);
  const re = new Float64Array(W), im = new Float64Array(W);
  for (let i = 0; i < W; i++) {
    const win = 0.5 - 0.5 * Math.cos(2 * Math.PI * i / (W - 1));
    re[i] = ((w.L[off + i] + w.R[off + i]) / 2) * win;
  }
  fft(re, im);
  const mag = new Float64Array(W / 2);
  let total = 0, centNum = 0;
  for (let k = 1; k < W / 2; k++) {
    const m = Math.hypot(re[k], im[k]);
    mag[k] = m * m;
    total += mag[k];
    centNum += mag[k] * (k * w.sr / W);
  }
  const bands = BANDS.map(([lo, hi]) => {
    let s = 0;
    for (let k = Math.max(1, Math.ceil(lo * W / w.sr)); k <= Math.min(W / 2 - 1, Math.floor(hi * W / w.sr)); k++) s += mag[k];
    return +(100 * s / total).toFixed(1);
  });
  rows.push({ t, centroidHz: Math.round(centNum / total), bands });
}
console.log('bands(%): ' + BANDS.map(b => b[0] + '-' + b[1]).join(' | '));
for (const r of rows) console.log(String(r.t).padStart(4) + 's  cen=' + String(r.centroidHz).padStart(5) + '  ' + r.bands.map(b => String(b).padStart(5)).join(' '));
