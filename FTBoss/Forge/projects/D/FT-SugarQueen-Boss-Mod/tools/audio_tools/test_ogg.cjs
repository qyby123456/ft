/* 测试: 用 wasm-media-encoders 生成 1 秒正弦波的 ogg, 验证可用性 */
const fs = require('fs');
const path = require('path');
const { createOggEncoder } = require('wasm-media-encoders');

(async () => {
    const sr = 44100, secs = 1;
    const n = sr * secs;
    const L = new Float32Array(n), R = new Float32Array(n);
    for (let i = 0; i < n; i++) {
        const v = Math.sin(2 * Math.PI * 440 * i / sr) * 0.4;
        L[i] = v; R[i] = v;
    }
    const enc = await createOggEncoder();
    enc.configure({ sampleRate: sr, vbrQuality: 3, channels: 2 });
    const parts = [];
    parts.push(enc.encode([L, R]));
    parts.push(enc.finalize());
    const buf = Buffer.concat(parts.map((u) => Buffer.from(u)));
    const out = path.join(__dirname, 'test_tone.ogg');
    fs.writeFileSync(out, buf);
    const magic = buf.slice(0, 4).toString('ascii');
    console.log('写出', out, buf.length, 'bytes, magic =', JSON.stringify(magic),
        magic === 'OggS' ? '✓ 合法 Ogg 容器' : '✗ 不是 Ogg');
})();
