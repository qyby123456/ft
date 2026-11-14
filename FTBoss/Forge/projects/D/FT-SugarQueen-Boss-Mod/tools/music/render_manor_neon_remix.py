import argparse
import json
import math
import wave
from functools import lru_cache
from pathlib import Path

import numpy as np


RATE = 44100
BPM = 140
BEAT = 60 / BPM
BARS = 144
TAIL = 3.0
SEED = 20260921
SECTIONS = [
    (0, 8, "Crystal ignition", "intro"),
    (8, 24, "Neon pursuit", "groove"),
    (24, 32, "Prism overload", "build"),
    (32, 64, "Star prison / first drop", "drop"),
    (64, 80, "Suspended garden", "break"),
    (80, 88, "Laser countdown", "build"),
    (88, 120, "Star prison / inverted drop", "drop2"),
    (120, 136, "All prisms unleashed", "final"),
    (136, 144, "Afterglow", "outro"),
]
CHORDS = [
    (38, (62, 65, 69, 76)), (34, (58, 62, 65, 69)),
    (41, (60, 65, 69, 72)), (36, (60, 64, 67, 74)),
    (38, (62, 65, 69, 76)), (34, (58, 62, 65, 69)),
    (43, (62, 67, 70, 74)), (33, (61, 64, 69, 76)),
]
THEMES = [
    (74, 77, 76, 74, 72, 69), (74, 77, 81, 77, 74, 72),
    (77, 81, 79, 77, 76, 72), (79, 76, 74, 72, 67, 72),
    (81, 77, 76, 74, 77, 81), (77, 74, 72, 70, 74, 77),
    (79, 82, 81, 79, 77, 74), (76, 73, 76, 81, 79, 73),
]


def clock(beats):
    return int(round(beats * BEAT * RATE))


def hz(note):
    return 440 * 2 ** ((note - 69) / 12)


@lru_cache(maxsize=256)
def table(note, brightness):
    phase = np.arange(4096, dtype=np.float64) * (2 * np.pi / 4096)
    signal = np.zeros_like(phase)
    harmonics = min(36, int(12000 / hz(note)))
    for harmonic in range(1, harmonics + 1):
        signal += np.sin(phase * harmonic) * math.exp(-harmonic / brightness) / harmonic
    return (signal / max(np.max(np.abs(signal)), 1e-8)).astype(np.float32)


def oscillator(note, timeline, detune=1.0, brightness=12, offset=0):
    phase = np.mod(timeline * hz(note) * detune + offset, 1) * 4096
    index = phase.astype(np.int32)
    fraction = phase - index
    waveform = table(note, brightness)
    return waveform[index] + (waveform[(index + 1) % 4096] - waveform[index]) * fraction


def envelope(timeline, hold, attack, decay, sustain, release):
    shape = np.minimum(1, timeline / attack)
    shape *= sustain + (1 - sustain) * np.exp(-np.maximum(0, timeline - attack) / decay)
    shape *= np.clip((hold + release - timeline) / release, 0, 1)
    return shape


@lru_cache(maxsize=768)
def voice(kind, note, duration):
    release = {"pad": 1.0, "bell": 0.9, "lead": 0.12, "pluck": 0.22, "bass": 0.04}[kind]
    timeline = np.arange(int((duration + release) * RATE), dtype=np.float64) / RATE
    frequency = hz(note)
    if kind == "bell":
        phase = 2 * np.pi * frequency * timeline
        tone = np.sin(phase + 1.7 * np.exp(-timeline * 18) * np.sin(phase * 2))
        tone += 0.18 * np.sin(phase * 3) * np.exp(-timeline * 12)
        tone *= (1 - np.exp(-timeline * 800)) * np.exp(-timeline * 4)
        shape = envelope(timeline, duration, 0.002, 0.3, 0.8, release)
        return np.column_stack((tone * shape, tone * shape)).astype(np.float32)
    if kind == "bass":
        tone = 0.76 * np.sin(2 * np.pi * frequency * timeline)
        tone += 0.38 * oscillator(note, timeline, brightness=5)
        tone += 0.08 * np.sin(2 * np.pi * frequency * timeline * 2)
        shape = envelope(timeline, duration, 0.004, 0.15, 0.65, release)
        tone = np.tanh(tone * 1.7) * shape
        return np.column_stack((tone, tone)).astype(np.float32)
    sound = np.zeros((len(timeline), 2), dtype=np.float64)
    detunes = (-0.006, -0.0031, -0.001, 0, 0.0013, 0.0035, 0.0063)
    if kind == "pluck":
        detunes = (-0.002, 0, 0.002)
    vibrato = 0.00009 * np.sin(timeline * 2 * np.pi * 5.1) * np.minimum(timeline * 3, 1)
    for index, detune in enumerate(detunes):
        pan = (index / max(1, len(detunes) - 1) - 0.5) * 1.6
        brightness = 15 if kind == "lead" else 8 if kind == "pluck" else 3.5
        tone = oscillator(note, timeline + vibrato, 1 + detune, brightness, index * 0.173)
        sound[:, 0] += tone * math.cos((pan + 1) * np.pi / 4)
        sound[:, 1] += tone * math.sin((pan + 1) * np.pi / 4)
    sound /= math.sqrt(len(detunes))
    if kind == "lead":
        shape = envelope(timeline, duration, 0.008, 0.15, 0.66, release)
    elif kind == "pluck":
        shape = envelope(timeline, duration, 0.003, 0.12, 0.13, release)
    else:
        shape = envelope(timeline, duration, 0.32, 0.8, 0.7, release)
    return (sound * shape[:, None]).astype(np.float32)


def noise(seconds, low, high, seed):
    generator = np.random.default_rng(seed)
    size = int(seconds * RATE)
    spectrum = np.fft.rfft(generator.normal(0, 1, size))
    frequencies = np.fft.rfftfreq(size, 1 / RATE)
    spectrum *= np.minimum(1, (frequencies / max(1, low)) ** 3)
    spectrum *= np.exp(-(frequencies / high) ** 4)
    signal = np.fft.irfft(spectrum, size)
    return signal / max(np.std(signal), 1e-8)


def drum(kind):
    duration = {"kick": 0.48, "snare": 0.32, "clap": 0.24, "hat": 0.09,
                "open": 0.24, "crash": 1.9, "tom": 0.28}[kind]
    timeline = np.arange(int(duration * RATE)) / RATE
    if kind == "kick":
        phase = 2 * np.pi * (48 * timeline + 115 * 0.022 * (1 - np.exp(-timeline / 0.022)))
        tone = np.sin(phase) * np.exp(-timeline / 0.14)
        tone += noise(duration, 2200, 8500, SEED) * np.exp(-timeline / 0.005) * 0.13
        tone = np.tanh(tone * 1.9) * np.minimum(timeline * 2500, 1)
    elif kind == "tom":
        phase = 2 * np.pi * (100 * timeline + 55 * 0.04 * (1 - np.exp(-timeline / 0.04)))
        tone = np.sin(phase) * np.exp(-timeline / 0.065)
    elif kind in ("snare", "clap"):
        tone = noise(duration, 900, 10000, SEED + 3)
        if kind == "clap":
            shape = sum(np.where(timeline >= onset, np.exp(-np.maximum(0, timeline - onset) / 0.009), 0)
                        for onset in (0, 0.012, 0.026))
            shape += 0.38 * np.exp(-timeline / 0.055)
            tone *= shape * 0.35
        else:
            tone = 0.42 * tone * np.exp(-timeline / 0.064)
            tone += 0.52 * np.sin(2 * np.pi * 186 * timeline) * np.exp(-timeline / 0.033)
    else:
        tone = noise(duration, 6000, 14500, SEED + 7)
        decay = {"hat": 0.014, "open": 0.07, "crash": 0.42}[kind]
        tone *= np.exp(-timeline / decay) * 0.26
        if kind == "crash":
            tone += 0.025 * np.sin(2 * np.pi * 4310 * timeline) * np.exp(-timeline / 0.3)
    tone *= np.minimum(timeline * 4000, 1) * np.minimum((duration - timeline) * 1000, 1)
    return np.column_stack((tone, tone)).astype(np.float32)


def render(destination):
    destination.mkdir(parents=True, exist_ok=True)
    count = int(math.ceil((BARS * 4 * BEAT + TAIL) * RATE))
    music = np.zeros((count, 2), dtype=np.float32)
    bass = np.zeros_like(music)
    drums = np.zeros_like(music)
    effects = np.zeros_like(music)
    duck = np.ones(count, dtype=np.float32)
    hits = {name: drum(name) for name in ("kick", "snare", "clap", "hat", "open", "crash", "tom")}
    events = 0

    def put(bus, position, signal, volume, pan=0):
        nonlocal events
        start = clock(position)
        stop = min(count, start + len(signal))
        if stop <= start:
            return
        gains = np.array([math.sqrt(1 - pan), math.sqrt(1 + pan)], dtype=np.float32)
        bus[start:stop] += signal[:stop - start] * (volume * gains)
        events += 1

    def note(kind, position, pitch, length, volume, pan=0):
        put(bass if kind == "bass" else music, position, voice(kind, pitch, round(length * BEAT, 6)), volume, pan)

    def hit(kind, position, volume, pan=0):
        put(drums, position, hits[kind], volume, pan)
        if kind == "kick":
            start = clock(position)
            size = min(int(BEAT * RATE), count - start)
            timeline = np.arange(size) / RATE
            curve = 0.24 + 0.76 * (1 - np.exp(-np.maximum(0, timeline - 0.014) / 0.105))
            duck[start:start + size] = np.minimum(duck[start:start + size], curve)

    for first, end, name, mode in SECTIONS:
        print(f"{name}: {first * 4 * BEAT:.2f}s", flush=True)
        for bar in range(first, end):
            local = bar - first
            beat = bar * 4
            chord_index = (bar // 2) % 8
            root, chord = CHORDS[chord_index]
            hot = mode in ("drop", "drop2", "final")
            pulse = hot or mode == "groove" or (mode == "outro" and local < 4)
            if pulse:
                for step in range(4):
                    if bar % 8 == 7 and step == 3 and mode != "groove":
                        continue
                    hit("kick", beat + step, 0.85)
                    hit("open", beat + step + 0.5, 0.29, 0.25)
                    hit("hat", beat + step + 0.25, 0.16, -0.42)
                    hit("hat", beat + step + 0.77, 0.12, 0.48)
                for step in (1, 3):
                    hit("clap", beat + step, 0.39)
                    hit("snare", beat + step, 0.28)
                positions = (0.5, 1.5, 2.5, 3.5) if mode != "drop2" else (0.5, 0.875, 1.5, 2.25, 2.5, 3.5)
                for index, step in enumerate(positions):
                    octave = 12 if hot and index == len(positions) - 1 and bar % 2 else 0
                    note("bass", beat + step, root + octave, 0.32, 0.43 if hot else 0.32)
                if hot and local % 8 == 0:
                    hit("crash", beat, 0.27, -0.18)
            elif mode == "build":
                density = 0.5 if local < 4 else 0.25 if local < 7 else 0.125
                for step in np.arange(0, 4, density):
                    hit("snare", beat + float(step), 0.13 + local * 0.022)
                if local < 6:
                    for step in (0, 2):
                        hit("kick", beat + step, 0.55)
                note("bass", beat, root, 3.3, 0.22)
            elif mode == "break" and local >= 8:
                hit("kick", beat, 0.55)
                hit("snare", beat + 2, 0.21)
                note("bass", beat, root, 2.5, 0.19)

            if bar % 2 == 0:
                for pitch in chord:
                    note("pad", beat, pitch - 12, 7.1, 0.055 if hot else 0.082)
            if mode != "outro" or local < 5:
                arpeggio = (0, 2, 1, 3, 2, 1, 3, 2) if mode != "drop2" else (3, 2, 1, 2, 0, 1, 2, 1)
                for index, degree in enumerate(arpeggio):
                    if mode == "intro" and local < 4 and index % 2:
                        continue
                    pitch = chord[degree] + (12 if hot or mode == "build" else 0)
                    note("pluck", beat + index * 0.5, pitch, 0.27, 0.09 if hot else 0.14,
                         -0.42 if index % 2 == 0 else 0.42)
            if hot:
                motif = THEMES[chord_index]
                positions = (0, 0.75, 1.5, 2, 2.75, 3.5)
                if bar % 2:
                    motif = (motif[3], motif[4], motif[5], motif[2], motif[1], motif[0])
                    positions = (0.25, 1, 1.75, 2.5, 3, 3.5)
                for index, position in enumerate(positions):
                    length = 0.38 if index % 3 else 0.58
                    pitch = motif[index]
                    if mode == "drop2" and local < 16:
                        pitch -= 12
                    note("lead", beat + position, pitch, length, 0.14 if mode != "final" else 0.16)
                    if mode == "final" and index % 2 == 0:
                        note("bell", beat + position, pitch + 12, 0.5, 0.08, -0.2)
            elif bar % 2 == 0:
                for index, position in enumerate((0, 1.5, 3, 5.5)):
                    note("bell", beat + position, THEMES[chord_index][index], 0.65, 0.2 if mode == "break" else 0.15,
                         -0.18 if index % 2 == 0 else 0.18)
            if pulse and bar % 8 == 7:
                for index in range(4):
                    hit("tom" if index % 2 == 0 else "snare", beat + 3 + index * 0.25, 0.2 + index * 0.025,
                        (index - 1.5) * 0.25)

    for bar in (24, 80, 116):
        duration = 8 * 4 * BEAT if bar != 116 else 4 * 4 * BEAT
        timeline = np.arange(int(duration * RATE)) / RATE
        progress = timeline / duration
        low = noise(duration, 1200, 4500, SEED + bar)
        high = noise(duration, 7000, 14500, SEED + bar + 1)
        tone = low * (1 - progress) + high * progress
        tone *= progress ** 1.6 * (0.55 + 0.45 * np.sin(2 * np.pi * timeline / BEAT * (1 + progress * 3)))
        tone += np.sin(2 * np.pi * (210 * timeline + 80 * timeline ** 2)) * progress * 0.07
        put(effects, bar * 4, np.column_stack((tone, tone)), 0.12)
    for bar in (32, 64, 88, 120, 136):
        duration = 2.2
        timeline = np.arange(int(duration * RATE)) / RATE
        tone = noise(duration, 1700, 10000, SEED + bar) * np.exp(-timeline / 0.6)
        put(effects, bar * 4, np.column_stack((tone, tone)), 0.095)

    wet = np.zeros_like(music)
    for delay, gain in ((0.75 * BEAT, 0.17), (1.5 * BEAT, 0.10), (2.25 * BEAT, 0.05), (0.083, 0.08), (0.131, 0.06)):
        offset = int(delay * RATE)
        wet[offset:, 0] += music[:-offset, 1] * gain
        wet[offset:, 1] += music[:-offset, 0] * gain
    music += wet
    del wet
    music *= (0.58 + duck * 0.42)[:, None]
    bass *= duck[:, None]
    music += bass
    music += drums
    music += effects
    del bass, drums, effects
    for bar in (32, 88, 120):
        stop = clock(bar * 4)
        start = stop - clock(0.75)
        curve = np.zeros(stop - start, dtype=np.float32)
        fade = int(0.025 * RATE)
        curve[:fade] = np.linspace(1, 0, fade)
        music[start:stop] *= curve[:, None]
    music[:int(0.06 * RATE)] *= np.linspace(0, 1, int(0.06 * RATE))[:, None]
    fade_start = clock(140 * 4)
    music[fade_start:] *= np.linspace(1, 0, count - fade_start)[:, None] ** 1.3
    music -= np.mean(music, axis=0)
    music *= 0.82 / max(np.max(np.abs(music)), 1e-8)
    assert np.isfinite(music).all()
    output = destination / "manor_neon_remix_premaster.wav"
    with wave.open(str(output), "wb") as wav:
        wav.setnchannels(2)
        wav.setsampwidth(2)
        wav.setframerate(RATE)
        wav.writeframes(np.rint(music * 32767).astype("<i2").tobytes())
    report = {"title": "庄园·霓虹星狱", "bpm": BPM, "key": "D minor", "meter": "4/4",
              "bars": BARS, "seconds": count / RATE, "seed": SEED, "events": events,
              "source": "Original composition and procedural synthesis; no external samples",
              "sections": [{"name": name, "bar": first, "seconds": first * 4 * BEAT,
                            "end_seconds": end * 4 * BEAT,
                            "premaster_rms": float(np.sqrt(np.mean(music[clock(first * 4):clock(end * 4)] ** 2)))}
                           for first, end, name, mode in SECTIONS]}
    (destination / "arrangement.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Rendered {output}: {report['seconds']:.3f}s, {events} events", flush=True)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("output", type=Path)
    render(parser.parse_args().output)
