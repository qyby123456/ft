/* ============================================================================
 * MiniMax Music 3 → Minecraft 可用 OGG 的生成管线(2026-09-13 v100)
 *
 * 官方接口(已核对 platform.minimax.io 文档):
 *   POST https://api.minimax.io/v1/music_generation
 *   Header: Authorization: Bearer <API_KEY>, Content-Type: application/json
 *   Body:   { model, prompt, lyrics?, is_instrumental?, output_format:'hex'|'url',
 *             audio_setting:{ sample_rate, bitrate, format:'mp3'|'wav'|'pcm' } }
 *   返回:   { base_resp:{status_code,status_msg}, data:{status,audio}, extra_info:{music_duration,...} }
 *
 * ★ 关键工程决定: **要 wav 不要 mp3**
 *   Minecraft 只能播 Ogg Vorbis, 而本机没有 ffmpeg、也没有 mp3 解码器。
 *   wav/pcm 是裸 PCM, 我自己解析(RIFF)→ float32 → wasm-media-encoders 编 OGG,
 *   全程纯 Node, 不需要任何外部二进制。
 *
 * 用法:
 *   set MINIMAX_API_KEY=xxxx
 *   node tools/audio_tools/gen_minimax_music.cjs --prompt "..." --out segment_01_mm.ogg
 *   node tools/audio_tools/gen_minimax_music.cjs --dry-run        # 不用 key, 自检 wav→ogg 通路
 * 参数: --model music-3.0 | music-3.0-free | music-2.6 ...   --lyrics-file <txt>
 *       --song(带人声, 需要歌词)   --url(用 url 输出而非 hex)
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const os = require('os');
const S = require('./synth.cjs');

const API = 'https://api.minimax.io/v1/music_generation';
const KEY = process.env.MINIMAX_API_KEY || process.env.MINIMAX_KEY || '';
const MUSIC_DIR = path.join(__dirname, '..', '..', 'src', 'main', 'resources', 'assets', 'ftboss', 'sounds', 'music');

const argv = process.argv.slice(2);
const arg = (name, dflt) => { const i = argv.indexOf('--' + name); return i >= 0 && argv[i + 1] && !argv[i + 1].startsWith('--') ? argv[i + 1] : dflt; };
const flag = (name) => argv.includes('--' + name);

const MODEL = arg('model', 'music-3.0');
const PROMPT = arg('prompt', 'Dark fantasy symphonic metal boss battle, 120 BPM, D minor, driving drums, low brass, ominous choir, instrumental');
const OUTNAME = arg('out', 'minimax_test.ogg');
const DRY = flag('dry-run');
const SONG = flag('song');                       // 带人声
const LYRICS_FILE = arg('lyrics-file', null);
const USE_URL = flag('url');

/* ---------------- WAV 解析: 任意常见位深 -> 立体声 float32 @44100 ---------------- */
function parseWav(buf) {
    if (buf.length < 44 || buf.toString('ascii', 0, 4) !== 'RIFF' || buf.toString('ascii', 8, 12) !== 'WAVE') {
        throw new Error('不是 RIFF/WAVE 数据(检查 audio_setting.format 是否 wav)');
    }
    let pos = 12, fmt = null, dataOff = -1, dataLen = 0;
    while (pos + 8 <= buf.length) {
        const id = buf.toString('ascii', pos, pos + 4);
        const size = buf.readUInt32LE(pos + 4);
        const body = pos + 8;
        if (id === 'fmt ') {
            fmt = {
                format: buf.readUInt16LE(body),
                channels: buf.readUInt16LE(body + 2),
                sampleRate: buf.readUInt32LE(body + 4),
                bits: buf.readUInt16LE(body + 14),
            };
        } else if (id === 'data') { dataOff = body; dataLen = Math.min(size, buf.length - body); }
        pos = body + size + (size % 2);       // 块按偶数字节对齐
    }
    if (!fmt || dataOff < 0) throw new Error('WAV 缺 fmt/data 块');
    const { format, channels, bits } = fmt;
    const bytesPer = bits / 8;
    const frames = Math.floor(dataLen / (bytesPer * channels));
    const L = new Float32Array(frames), R = new Float32Array(frames);
    const read = (off) => {
        if (format === 3) return buf.readFloatLE(off);                       // IEEE float
        if (bits === 16) return buf.readInt16LE(off) / 32768;
        if (bits === 24) { const v = buf.readIntLE(off, 3); return v / 8388608; }
        if (bits === 32) return buf.readInt32LE(off) / 2147483648;
        if (bits === 8) return (buf.readUInt8(off) - 128) / 128;
        throw new Error('不支持的位深 ' + bits);
    };
    for (let i = 0; i < frames; i++) {
        const base = dataOff + i * bytesPer * channels;
        const l = read(base);
        const r = channels > 1 ? read(base + bytesPer) : l;
        L[i] = l; R[i] = r;
    }
    return { L, R, sampleRate: fmt.sampleRate, channels };
}

/** 线性重采样到 44100(只在接口没按我们要求给 44100 时才走) */
function resample(L, R, from, to) {
    if (from === to) return { L, R };
    const n = Math.round(L.length * to / from);
    const oL = new Float32Array(n), oR = new Float32Array(n);
    for (let i = 0; i < n; i++) {
        const x = i * from / to, i0 = Math.floor(x), i1 = Math.min(L.length - 1, i0 + 1), f = x - i0;
        oL[i] = L[i0] * (1 - f) + L[i1] * f;
        oR[i] = R[i0] * (1 - f) + R[i1] * f;
    }
    console.log(`   (重采样 ${from} -> ${to} Hz)`);
    return { L: oL, R: oR };
}

/** 写一个 16bit WAV(仅给 --dry-run 自检用) */
function writeWav16(file, L, R, sr) {
    const n = L.length, dataLen = n * 4;
    const b = Buffer.alloc(44 + dataLen);
    b.write('RIFF', 0, 'ascii'); b.writeUInt32LE(36 + dataLen, 4); b.write('WAVE', 8, 'ascii');
    b.write('fmt ', 12, 'ascii'); b.writeUInt32LE(16, 16); b.writeUInt16LE(1, 20); b.writeUInt16LE(2, 22);
    b.writeUInt32LE(sr, 24); b.writeUInt32LE(sr * 4, 28); b.writeUInt16LE(4, 32); b.writeUInt16LE(16, 34);
    b.write('data', 36, 'ascii'); b.writeUInt32LE(dataLen, 40);
    for (let i = 0; i < n; i++) {
        b.writeInt16LE(Math.max(-32768, Math.min(32767, Math.round(L[i] * 32767))), 44 + i * 4);
        b.writeInt16LE(Math.max(-32768, Math.min(32767, Math.round(R[i] * 32767))), 46 + i * 4);
    }
    fs.writeFileSync(file, b);
    return b.length;
}

const ERR = {
    1002: '触发限流(RPM 超了), 稍后重试', 1004: '鉴权失败: 没带 / key 不对',
    1008: '余额不足', 1026: '内容被判定敏感', 2013: '参数不合法', 2049: 'API Key 无效',
};

/* ---------------- 主流程 ---------------- */
(async function main() {
    if (DRY) {
        console.log('【自检模式】不用 API key, 只为验证 "音频 -> OGG" 这条通路');
        console.log('① 用本地合成器造一段 20 秒测试音(模拟 MiniMax 返回的 wav)…');
        const sr = 44100, secs = 20, n = sr * secs;
        const bus = new S.Bus(n);
        for (let bar = 0; bar < 10; bar++) {
            const T = bar * 2;
            S.kick(bus, T, 0.9, 0.25); S.snare(bus, T + 1, 0.8, 0.3);
            S.strings(bus, T, 1.9, S.freq(['D', 'F', 'Bb', 'C'][bar % 4], 3), 0.7, 0.4, 0.5);
            S.brass(bus, T, 1.8, S.freq(['D', 'F', 'Bb', 'C'][bar % 4], 4), 0.6, 0.42, 0.4);
        }
        bus.finish(2.4, true);
        const wav = path.join(os.tmpdir(), 'mm_dryrun.wav');
        const sz = writeWav16(wav, bus.L, bus.R, sr);
        console.log(`   造出 ${path.basename(wav)} (${(sz / 1048576).toFixed(2)} MB)`);
        console.log('② 当成 API 响应来解析 + 转 OGG…');
        const parsed = parseWav(fs.readFileSync(wav));
        const rs = resample(parsed.L, parsed.R, parsed.sampleRate, S.SR);
        const out = path.join(os.tmpdir(), 'mm_dryrun.ogg');
        const r = await S.encodeOgg(out, rs.L, rs.R, { vbrQuality: 0.5 });
        console.log(`   ✓ ${out}  ${(r.size / 1048576).toFixed(2)} MB  峰值 ${r.peak.toFixed(3)}  RMS ${r.rms.toFixed(4)}`);
        console.log('   (拿 tools/audio_tools/OggInfo.java 验一下 dur 对不对就行)');
        return;
    }

    if (!KEY) {
        console.error('缺少 API key: 请先设置环境变量 MINIMAX_API_KEY, 例如');
        console.error('  $env:MINIMAX_API_KEY="sk-..."   (PowerShell)');
        console.error('key 在 https://platform.minimax.io/user-center/basic-information/interface-key');
        process.exit(2);
    }

    const body = {
        model: MODEL,
        prompt: PROMPT,
        is_instrumental: !SONG,
        output_format: USE_URL ? 'url' : 'hex',
        audio_setting: { sample_rate: 44100, bitrate: 256000, format: 'wav' },
    };
    if (SONG) {
        if (!LYRICS_FILE) { console.error('--song 需要 --lyrics-file <txt>(可用 [verse]/[chorus] 等结构标签)'); process.exit(2); }
        body.lyrics = fs.readFileSync(LYRICS_FILE, 'utf8');
    }
    console.log(`POST ${API}\n  model=${MODEL} instrumental=${!SONG} format=wav 44100/256k out=${USE_URL ? 'url' : 'hex'}`);
    const t0 = Date.now();
    const res = await fetch(API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + KEY },
        body: JSON.stringify(body),
    });
    const txt = await res.text();
    let json;
    try { json = JSON.parse(txt); } catch { console.error(`HTTP ${res.status} 返回非 JSON:\n${txt.slice(0, 800)}`); process.exit(1); }
    const code = json?.base_resp?.status_code;
    if (code !== 0) {
        console.error(`生成失败: status_code=${code} ${json?.base_resp?.status_msg || ''}  ${ERR[code] || ''}`);
        process.exit(1);
    }
    const info = json.extra_info || {};
    console.log(`  成功(${((Date.now() - t0) / 1000).toFixed(1)}s)  时长 ${((info.music_duration || 0) / 1000).toFixed(1)}s  ` +
        `${info.music_sample_rate}Hz ${info.music_channel}ch  ${(info.music_size / 1048576).toFixed(2)} MB`);

    let audio;
    if (USE_URL) {
        const url = json.data.audio;
        console.log(`  下载 ${url.slice(0, 80)}…`);
        audio = Buffer.from(await (await fetch(url)).arrayBuffer());
    } else {
        audio = Buffer.from(json.data.audio, 'hex');
    }
    console.log(`  拿到音频 ${(audio.length / 1048576).toFixed(2)} MB, 首 4 字节 ${audio.toString('ascii', 0, 4)}`);

    const parsed = parseWav(audio);
    const rs = resample(parsed.L, parsed.R, parsed.sampleRate, S.SR);
    const out = path.join(MUSIC_DIR, OUTNAME);
    const r = await S.encodeOgg(out, rs.L, rs.R, { vbrQuality: 0.5 });
    console.log(`✓ ${out}\n  ${(r.size / 1048576).toFixed(2)} MB  峰值 ${r.peak.toFixed(3)}  RMS ${r.rms.toFixed(4)}  时长 ${(rs.L.length / S.SR).toFixed(1)}s`);
})();
