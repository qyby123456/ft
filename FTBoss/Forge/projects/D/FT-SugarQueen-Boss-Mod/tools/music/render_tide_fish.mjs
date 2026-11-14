import fs from 'node:fs';
import path from 'node:path';
import { createRequire } from 'node:module';
import { pathToFileURL } from 'node:url';
import { spawnSync } from 'node:child_process';

const runtime = path.resolve(process.argv[2]);
const output = path.resolve(process.argv[3]);
const ffmpeg = process.argv[4];
const require = createRequire(path.join(runtime, 'package.json'));
const { MIDIBuilder, BasicMIDI, SoundBankLoader, SpessaSynthProcessor, SpessaSynthSequencer } =
  await import(pathToFileURL(require.resolve('spessasynth_core')).href);
const sampleRate = 44100;
const resolution = 480;
const ticks = beats => Math.round(beats * resolution);
fs.mkdirSync(output, { recursive: true });

function score(enraged) {
  const tempo = enraged ? 144 : 120;
  const bars = enraged ? 56 : 48;
  const builder = new MIDIBuilder({ timeDivision: resolution, format: 1, initialTempo: tempo, name: enraged ? 'Canglin - Raging Tide' : 'Canglin - Royal Tide' });
  for (const name of ['Conductor', 'Horns', 'Strings', 'Harp', 'Flute', 'Bass', 'Timpani', 'Choir', 'Drums']) builder.addTrack(name, 0);
  const instruments = [
    [1, 0, 60, 106, 57], [2, 1, 48, 92, 43], [3, 2, 46, 101, 80],
    [4, 3, 73, 106, 73], [5, 4, 43, 110, 60], [6, 5, 47, 115, 64], [7, 6, 52, 76, 64], [8, 9, 0, 112, 64]
  ];
  for (const [track, channel, program, volume, pan] of instruments) {
    if (channel !== 9) builder.programChange(0, track, channel, program);
    for (const [controller, value] of [[7, volume], [10, pan], [11, 127], [91, 42], [93, 8]]) {
      builder.controllerChange(0, track, channel, controller, value);
    }
  }
  const note = (track, channel, start, duration, pitch, velocity) => {
    builder.noteOn(ticks(start), track, channel, pitch, Math.max(1, Math.min(127, Math.round(velocity))));
    builder.noteOff(ticks(start + duration), track, channel, pitch);
  };
  const chords = [[50, 53, 57, 60], [46, 50, 53, 57], [48, 52, 55, 58], [45, 49, 52, 55]];
  const theme = [
    [[0, 74, 0.75], [0.75, 69, 0.25], [1, 72, 0.5], [1.5, 74, 0.5], [2.5, 77, 0.5], [3, 76, 0.75]],
    [[0, 74, 1], [1.5, 72, 0.5], [2, 69, 0.75], [3, 65, 0.75]],
    [[0, 67, 0.5], [0.75, 72, 0.75], [1.5, 76, 0.5], [2, 74, 0.5], [3, 72, 0.75]],
    [[0, 73, 0.5], [0.5, 76, 0.5], [1.5, 69, 1], [3, 73, 0.75]]
  ];
  for (let bar = 0; bar < bars; bar++) {
    const start = bar * 4;
    const harmony = chords[bar % 4];
    const quiet = bar >= 24 && bar < 27;
    const solo = bar >= 16 && bar < 24;
    const finale = bar >= bars - 12;
    const intensity = quiet ? 0.38 : finale ? 1.05 : 0.9;
    for (const pitch of harmony) note(2, 1, start, 3.7, pitch + 12, 66 * intensity);
    if (!quiet && (enraged || finale)) for (const pitch of harmony.slice(0, 3)) note(7, 6, start, 3.6, pitch + 12, 48);
    for (let step = 0; step < 8; step++) {
      note(3, 2, start + step * 0.5, 0.4, harmony[step % 4] + (step >= 4 ? 24 : 12), quiet ? 57 : 72);
      if (!quiet) note(5, 4, start + step * 0.5, 0.35, harmony[0] - 12 + (step % 4 === 3 ? 7 : 0), 85 + step % 2 * 7);
    }
    if (!quiet) {
      for (let beat = 0; beat < 4; beat++) {
        note(8, 9, start + beat, 0.1, 36, beat % 2 === 0 ? 112 : 76);
        if (beat % 2 === 1) note(8, 9, start + beat, 0.12, 38, 100);
        if (enraged) note(8, 9, start + beat + 0.5, 0.1, 36, 83);
        note(6, 5, start + beat, 0.4, harmony[0] - 12, beat === 0 ? 105 : 68);
      }
      for (let step = 0; step < (enraged ? 16 : 8); step++) note(8, 9, start + step * (enraged ? 0.25 : 0.5), 0.1,
        step % 4 === 0 ? 51 : 42, step % 4 === 0 ? 75 : 48);
      if (bar % 4 === 0) note(8, 9, start, 0.8, 49, 105);
      if (bar % 8 === 7) for (let step = 0; step < 4; step++) note(8, 9, start + 3 + step * 0.25, 0.12, [48, 45, 43, 41][step], 85 + step * 8);
    }
    for (const [beat, pitch, duration] of theme[bar % 4]) {
      const melodyPitch = pitch + (finale && bar % 8 >= 4 ? 12 : 0);
      if (!quiet && !solo) note(1, 0, start + beat, duration, melodyPitch - 12, 102 * intensity);
      note(4, 3, start + beat, duration * 0.92, melodyPitch, quiet ? 52 : 91);
      if (enraged && !quiet && !solo) note(2, 1, start + beat + 0.125, duration * 0.65, melodyPitch, 76);
    }
    if (solo) {
      const run = [0, 1, 2, 3, 2, 1, 3, 2, 1, 0, 2, 1, 3, 2, 1, 0];
      for (let step = 0; step < run.length; step++) note(3, 2, start + step * 0.25, 0.22, harmony[run[step]] + 24, 87);
    }
  }
  for (const [track, channel] of instruments) builder.controllerChange(ticks(bars * 4), track, channel, 123, 0);
  builder.flush();
  const bytes = builder.writeMIDI();
  return { bytes, midi: BasicMIDI.fromArrayBuffer(bytes), seconds: bars * 4 * 60 / tempo + 3, tempo, bars };
}

async function render(enraged) {
  const composition = score(enraged);
  const name = enraged ? 'tide_fish_p2' : 'tide_fish';
  fs.writeFileSync(path.join(output, name + '.mid'), new Uint8Array(composition.bytes));
  const font = fs.readFileSync(path.join(runtime, 'MuseScore_General.sf3'));
  const synth = new SpessaSynthProcessor(sampleRate, { eventsEnabled: false });
  synth.soundBankManager.addSoundBank(SoundBankLoader.fromArrayBuffer(font.buffer.slice(font.byteOffset, font.byteOffset + font.byteLength)), 'main');
  await synth.processorInitialized;
  synth.setSystemParameter('autoAllocateVoices', true);
  const sequencer = new SpessaSynthSequencer(synth);
  sequencer.loadNewSongList([composition.midi]);
  sequencer.play();
  const samples = Math.ceil(composition.seconds * sampleRate);
  const left = new Float32Array(samples);
  const right = new Float32Array(samples);
  for (let offset = 0; offset < samples; offset += 128) {
    sequencer.processTick();
    synth.process(left, right, offset, Math.min(128, samples - offset));
  }
  let peak = 0;
  let sum = 0;
  for (let index = 0; index < samples; index++) {
    peak = Math.max(peak, Math.abs(left[index]), Math.abs(right[index]));
    sum += left[index] ** 2 + right[index] ** 2;
  }
  const gain = Math.min(0.84 / Math.max(peak, 0.00001), 0.19 / Math.max(Math.sqrt(sum / (samples * 2)), 0.00001));
  const wave = Buffer.alloc(44 + samples * 4);
  wave.write('RIFF', 0); wave.writeUInt32LE(wave.length - 8, 4); wave.write('WAVEfmt ', 8);
  wave.writeUInt32LE(16, 16); wave.writeUInt16LE(1, 20); wave.writeUInt16LE(2, 22);
  wave.writeUInt32LE(sampleRate, 24); wave.writeUInt32LE(sampleRate * 4, 28);
  wave.writeUInt16LE(4, 32); wave.writeUInt16LE(16, 34); wave.write('data', 36); wave.writeUInt32LE(samples * 4, 40);
  for (let index = 0; index < samples; index++) {
    const fade = Math.min(1, index / (sampleRate * 0.015), (samples - index) / sampleRate);
    wave.writeInt16LE(Math.round(left[index] * gain * fade * 32767), 44 + index * 4);
    wave.writeInt16LE(Math.round(right[index] * gain * fade * 32767), 46 + index * 4);
  }
  const wavPath = path.join(output, name + '.wav');
  fs.writeFileSync(wavPath, wave);
  for (const [extension, codec] of [['ogg', ['-c:a', 'libvorbis', '-q:a', '6']], ['mp3', ['-c:a', 'libmp3lame', '-b:a', '320k']]]) {
    const result = spawnSync(ffmpeg, ['-hide_banner', '-loglevel', 'error', '-y', '-i', wavPath, ...codec, path.join(output, name + '.' + extension)], { encoding: 'utf8' });
    if (result.status !== 0) throw new Error(result.error?.message || result.stderr);
  }
  const report = { name, seconds: samples / sampleRate, ticks: Math.ceil(samples / sampleRate * 20), tempo: composition.tempo,
    bars: composition.bars, samplePeak: peak * gain, rms: Math.sqrt(sum / (samples * 2)) * gain };
  fs.writeFileSync(path.join(output, name + '.json'), JSON.stringify(report, null, 2) + '\n');
  console.log(report);
}

await render(false);
await render(true);
