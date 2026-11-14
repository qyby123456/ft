// WAV -> OGG Vorbis (via emscripten libvorbis, Node, no ffmpeg needed)
const fs = require('fs');
const path = require('path');
const libvorbis = require('vorbis-encoder-js').libvorbis;

function readWav(file) {
  const buf = fs.readFileSync(file);
  if (buf.toString('ascii', 0, 4) !== 'RIFF' || buf.toString('ascii', 8, 12) !== 'WAVE') {
    throw new Error('not a RIFF/WAVE file: ' + file);
  }
  let pos = 12, fmt = null, dataOffset = -1, dataLen = 0;
  while (pos + 8 <= buf.length) {
    const id = buf.toString('ascii', pos, pos + 4);
    const size = buf.readUInt32LE(pos + 4);
    const body = pos + 8;
    if (id === 'fmt ') {
      fmt = {
        format: buf.readUInt16LE(body),
        channels: buf.readUInt16LE(body + 2),
        sampleRate: buf.readUInt32LE(body + 4),
        bits: buf.readUInt16LE(body + 14)
      };
    } else if (id === 'data') {
      dataOffset = body; dataLen = size;
    }
    pos = body + size + (size % 2);
  }
  if (!fmt || dataOffset < 0) throw new Error('missing fmt/data chunk');

  const { channels, sampleRate, bits, format } = fmt;
  const frames = Math.floor(dataLen / (channels * (bits / 8)));
  const out = [];
  for (let c = 0; c < channels; c++) out.push(new Float32Array(frames));

  if (format === 1 && bits === 16) {
    for (let i = 0; i < frames; i++) {
      for (let c = 0; c < channels; c++) {
        out[c][i] = buf.readInt16LE(dataOffset + (i * channels + c) * 2) / 32768;
      }
    }
  } else if (format === 3 && bits === 32) {
    for (let i = 0; i < frames; i++) {
      for (let c = 0; c < channels; c++) {
        out[c][i] = buf.readFloatLE(dataOffset + (i * channels + c) * 4);
      }
    }
  } else {
    throw new Error('unsupported wav format=' + format + ' bits=' + bits);
  }
  return { channels, sampleRate, frames, data: out };
}

function encode(wav, quality, tags) {
  const M = libvorbis;
  const enc = M._encoder_init(wav.channels, wav.sampleRate, quality);
  const setTag = M.cwrap('encoder_set_tag', null, ['number', 'string', 'string']);
  setTag(enc, 'ENCODER', 'chatbox-wav2ogg');
  for (const k of Object.keys(tags || {})) setTag(enc, k, String(tags[k]));
  M._encoder_stream_init(enc);

  const chunks = [];
  const BLOCK = 4096;
  for (let off = 0; off < wav.frames; off += BLOCK) {
    const n = Math.min(BLOCK, wav.frames - off);
    const ptrFloats = M._encoder_analysis_buffer(enc, n) >> 2;
    for (let c = 0; c < wav.channels; c++) {
      const dstIndex = M.HEAPU32[ptrFloats + c] >> 2;
      M.HEAPF32.set(wav.data[c].subarray(off, off + n), dstIndex);
    }
    M._encoder_process(enc, n);
    const len = M._encoder_data_len(enc);
    if (len > 0) {
      const ptr = M._encoder_transfer_data(enc);
      chunks.push(Buffer.from(new Uint8Array(M.HEAPU8.subarray(ptr, ptr + len))));
    }
  }
  // flush
  M._encoder_process(enc, 0);
  let len;
  while ((len = M._encoder_data_len(enc)) > 0) {
    const ptr = M._encoder_transfer_data(enc);
    chunks.push(Buffer.from(new Uint8Array(M.HEAPU8.subarray(ptr, ptr + len))));
  }
  M._encoder_clear(enc);
  return Buffer.concat(chunks);
}

if (require.main === module) {
  const [inFile, outFile, q] = process.argv.slice(2);
  const wav = readWav(inFile);
  const t0 = Date.now();
  const ogg = encode(wav, q === undefined ? 0.4 : parseFloat(q), {
    TITLE: path.basename(outFile, path.extname(outFile))
  });
  fs.writeFileSync(outFile, ogg);
  console.log(JSON.stringify({
    input: inFile, output: outFile,
    channels: wav.channels, sampleRate: wav.sampleRate,
    seconds: +(wav.frames / wav.sampleRate).toFixed(2),
    oggBytes: ogg.length, magic: ogg.toString('ascii', 0, 4),
    ms: Date.now() - t0
  }));
}
module.exports = { readWav, encode };
