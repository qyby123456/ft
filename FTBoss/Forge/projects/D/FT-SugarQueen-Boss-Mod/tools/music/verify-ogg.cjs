// verify an ogg file decodes: duration, channels, peak, rms
const fs = require('fs');
const { OggVorbisDecoder } = require('@wasm-audio-decoders/ogg-vorbis');

(async () => {
  const file = process.argv[2];
  const buf = new Uint8Array(fs.readFileSync(file));
  const dec = new OggVorbisDecoder();
  await dec.ready;
  const r = await dec.decodeFile(buf);
  let peak = 0, sum = 0, n = 0;
  for (const ch of r.channelData) {
    for (let i = 0; i < ch.length; i++) { const a = Math.abs(ch[i]); if (a > peak) peak = a; sum += ch[i] * ch[i]; n++; }
  }
  console.log(JSON.stringify({
    file,
    errors: r.errors.map(e => e.message || String(e)),
    channels: r.channelData.length,
    sampleRate: r.sampleRate,
    samplesDecoded: r.samplesDecoded,
    seconds: +(r.samplesDecoded / r.sampleRate).toFixed(2),
    peak: +peak.toFixed(4),
    rms: +Math.sqrt(sum / Math.max(1, n)).toFixed(4),
    fileBytes: buf.length
  }));
  dec.free();
})();
