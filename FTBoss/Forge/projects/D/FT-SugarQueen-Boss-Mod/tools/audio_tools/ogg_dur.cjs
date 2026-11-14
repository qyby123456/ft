// 读 ogg 的真实时长(秒) —— 唱片 RecordItem 的长度参数必须等于它, 否则唱片机会提前判定"放完了"。
// 做法: 不依赖任何库, 直接扫 OggS 页, 取**最大 granule**(= 总 PCM 采样数),
// 采样率从第一页的 identification header 里读。用法: node ogg_dur.cjs <ogg...>
const fs = require('fs');

function sampleRate(buf) {
    // 第一个 OggS 页: 'OggS'(4) ver(1) type(1) granule(8) serial(4) seq(4) crc(4) nseg(1) segtab(n)
    let p = 0;
    if (buf.toString('latin1', 0, 4) !== 'OggS') return 0;
    const nseg = buf.readUInt8(26);
    const payload = 27 + nseg;
    if (buf.readUInt8(payload) !== 1) return 0;                 // packet type 1 = identification
    if (buf.toString('latin1', payload + 1, payload + 7) !== 'vorbis') return 0;
    return buf.readUInt32LE(payload + 12);                      // version(4) + channels(1) + rate(4)
}

function maxGranule(buf) {
    let best = 0;
    for (let i = 0; i + 27 < buf.length; i++) {
        if (buf[i] === 0x4f && buf[i + 1] === 0x67 && buf[i + 2] === 0x67 && buf[i + 3] === 0x53) {
            const g = Number(buf.readBigInt64LE(i + 6));
            if (g > best) best = g;
        }
    }
    return best;
}

let fail = 0;
for (const f of process.argv.slice(2)) {
    try {
        const b = fs.readFileSync(f);
        const rate = sampleRate(b);
        const g = maxGranule(b);
        if (!rate || !g) { console.log(`${f}\t*** 解析失败 (rate=${rate} granule=${g})`); fail++; continue; }
        const secs = g / rate;
        const name = f.replace(/\\/g, '/').split('/').pop();
        console.log(`${name}\t${secs.toFixed(2)}s\t${Math.round(secs * 20)} tick\t${b.length} B`);
    } catch (e) {
        console.log(`${f}\t*** ${e.message}`);
        fail++;
    }
}
process.exit(fail ? 1 : 0);
