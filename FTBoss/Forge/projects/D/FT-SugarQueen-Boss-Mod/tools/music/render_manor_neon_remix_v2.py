import argparse
import json
import math
import wave
from functools import lru_cache
from pathlib import Path

import numpy as np

from render_manor_neon_remix import RATE, drum, envelope, hz, noise, oscillator, voice


BPM = 172
BEAT = 60 / BPM
BARS = 176
TAIL = 4
SEED = 20260922
SECTIONS = [
    (0, 16, "Glass before the storm", "piano"),
    (16, 32, "Iron roots", "half"),
    (32, 40, "Pressure rising", "build"),
    (40, 72, "Break the star prison", "breakbeat"),
    (72, 88, "A light still remains", "bridge"),
    (88, 104, "The manor strikes back", "heavy"),
    (104, 112, "Second ignition", "build"),
    (112, 144, "Prismatic counterattack", "breakbeat2"),
    (144, 168, "Dawn over the arena", "final"),
    (168, 176, "Glass after the storm", "outro"),
]
HARMONY = [
    (40, (55, 59, 64, 66)), (36, (55, 60, 64, 67)),
    (43, (55, 59, 62, 69)), (38, (57, 62, 66, 69)),
    (40, (55, 59, 64, 67)), (36, (55, 60, 64, 71)),
    (45, (57, 60, 64, 67)), (35, (54, 59, 63, 66)),
]
PHRASES = [
    ((0, 76, 0.7), (1, 79, 1.2), (2.5, 78, 0.35), (3, 74, 0.65), (4, 76, 1.6), (6.25, 71, 0.55), (7, 74, 0.65)),
    ((0.5, 76, 1.25), (2, 72, 0.65), (3, 74, 0.7), (4.5, 79, 1.15), (6, 76, 1.45)),
    ((0, 79, 1.2), (1.5, 83, 0.6), (2.5, 81, 1.25), (4.25, 79, 0.5), (5, 78, 0.6), (6, 74, 1.55)),
    ((0.25, 78, 0.6), (1, 81, 1.4), (3, 78, 0.55), (4, 76, 0.65), (5, 74, 2.1)),
    ((0, 83, 1.2), (1.5, 81, 0.65), (2.5, 79, 1.15), (4, 78, 0.7), (5, 76, 2.2)),
    ((0.5, 79, 0.65), (1.5, 76, 0.65), (2.5, 74, 0.6), (3.5, 72, 1.25), (5.25, 76, 0.6), (6.25, 79, 1.2)),
    ((0, 81, 1.5), (2, 79, 0.7), (3, 76, 0.6), (4, 72, 1.25), (5.5, 74, 0.7), (6.5, 76, 1.1)),
    ((0.25, 78, 0.65), (1.25, 75, 0.65), (2.25, 71, 1.5), (4.5, 75, 0.7), (5.5, 78, 0.65), (6.5, 75, 1.15)),
]


def sample(beats):
    return int(round(beats * BEAT * RATE))


def cabinet(signal, highpass=90, lowpass=5600):
    size = len(signal)
    frequencies = np.fft.rfftfreq(size, 1 / RATE)
    transfer = (frequencies / np.maximum(frequencies, highpass)) ** 2
    transfer *= 1 / (1 + (frequencies / lowpass) ** 6)
    transfer *= 1 + 0.28 * np.exp(-((frequencies - 1250) / 570) ** 2)
    return np.fft.irfft(np.fft.rfft(signal, axis=0) * transfer[:, None], size, axis=0).astype(np.float32)


@lru_cache(maxsize=256)
def piano(note, length):
    duration = length * BEAT
    timeline = np.arange(int((duration + 1.4) * RATE)) / RATE
    tone = np.zeros((len(timeline), 2), dtype=np.float64)
    frequency = hz(note)
    for partial in range(1, 13):
        inharmonic = math.sqrt(1 + 0.00008 * partial * partial)
        damping = (1.8 / partial ** 0.64) * (220 / frequency) ** 0.18
        shape = np.exp(-timeline / damping) / partial ** 1.65
        for channel, detune in enumerate((0.9995, 1.0005)):
            phase = 2 * np.pi * frequency * partial * inharmonic * detune * timeline
            tone[:, channel] += np.sin(phase) * shape
    hammer = noise(duration + 1.4, 1500, 7000, SEED + note)
    tone += (hammer * np.exp(-timeline / 0.006) * 0.045)[:, None]
    tone *= (1 - np.exp(-timeline * 650))[:, None]
    tone *= np.exp(-np.maximum(0, timeline - duration) / 0.38)[:, None]
    tone[-220:] *= np.linspace(1, 0, 220)[:, None]
    return tone.astype(np.float32)


@lru_cache(maxsize=256)
def guitar(note, length, muted, variation):
    duration = length * BEAT
    timeline = np.arange(int((duration + 0.08) * RATE)) / RATE
    result = np.zeros((len(timeline), 2), dtype=np.float64)
    for channel, detune in enumerate((0.9983, 1.0018)):
        pluck = np.zeros(len(timeline))
        for interval, level in ((0, 1), (7, 0.66), (12, 0.28)):
            frequency = hz(note + interval) * detune
            for partial in range(1, 16):
                if frequency * partial > 12000:
                    break
                amplitude = math.sin(partial * np.pi * 0.17) / partial ** 0.83
                phase = 2 * np.pi * frequency * partial * (timeline + variation * 0.0007 + channel * 0.001)
                pluck += level * amplitude * np.sin(phase) * np.exp(-timeline * (1.4 + partial * 0.55))
        shape = envelope(timeline, duration, 0.0018, 0.06, 0.14 if muted else 0.76, 0.07)
        pluck *= shape
        pick = noise(duration + 0.08, 1800, 6500, SEED + note + variation + channel)
        pluck += pick * np.exp(-timeline / 0.009) * 0.08
        result[:, channel] = np.tanh(pluck * 3.8) * 0.8
    result = cabinet(result)
    result[-300:] *= np.linspace(1, 0, 300)[:, None]
    return result


@lru_cache(maxsize=256)
def reese(note, length, movement):
    duration = length * BEAT
    timeline = np.arange(int((duration + 0.05) * RATE)) / RATE
    sub = np.sin(2 * np.pi * hz(note) * timeline)
    mid = oscillator(note + 12, timeline, 0.995, 6) + oscillator(note + 12, timeline, 1.006, 6, 0.3)
    modulator = 0.38 + 0.62 * np.sin(np.pi * timeline / max(duration, 0.1) * movement) ** 2
    tone = sub * 0.84 + np.tanh(mid * 1.8) * modulator * 0.35
    tone *= envelope(timeline, duration, 0.004, 0.16, 0.8, 0.045)
    return np.column_stack((tone, tone)).astype(np.float32)


def render(destination):
    destination.mkdir(parents=True, exist_ok=True)
    count = int(math.ceil((BARS * 4 * BEAT + TAIL) * RATE))
    music = np.zeros((count, 2), dtype=np.float32)
    guitars = np.zeros_like(music)
    low = np.zeros_like(music)
    percussion = np.zeros_like(music)
    effects = np.zeros_like(music)
    duck = np.ones(count, dtype=np.float32)
    hits = {kind: drum(kind) for kind in ("kick", "snare", "hat", "open", "crash", "tom", "clap")}
    generator = np.random.default_rng(SEED)
    events = 0

    def put(bus, beat, waveform, volume, pan=0):
        nonlocal events
        start = sample(beat)
        end = min(count, start + len(waveform))
        if end <= start:
            return
        gains = np.array([math.sqrt(1 - pan), math.sqrt(1 + pan)], dtype=np.float32)
        bus[start:end] += waveform[:end - start] * (volume * gains)
        events += 1

    def hit(kind, beat, volume, pan=0):
        put(percussion, beat, hits[kind], volume, pan)
        if kind == "kick":
            start = sample(beat)
            size = min(sample(0.9), count - start)
            timeline = np.arange(size) / RATE
            curve = 0.3 + 0.7 * (1 - np.exp(-np.maximum(0, timeline - 0.009) / 0.06))
            duck[start:start + size] = np.minimum(duck[start:start + size], curve)

    for first, end, name, mode in SECTIONS:
        print(f"{name}: {first * 4 * BEAT:.2f}s", flush=True)
        for bar in range(first, end):
            local = bar - first
            beat = bar * 4
            chord_index = (local // 2) % 8
            root, chord = HARMONY[chord_index]
            fast = mode in ("breakbeat", "breakbeat2", "final")
            heavy = mode in ("half", "heavy")
            quiet = mode in ("piano", "bridge", "outro")
            if fast:
                kicks = ((0, 1.75, 2.5), (0, 0.75, 2.5, 3.5), (0, 2, 2.75), (0, 1.5, 2.5, 3.75))[bar % 4]
                for position in kicks:
                    hit("kick", beat + position, 0.93 if mode == "final" else 0.85)
                for position in (1, 3):
                    hit("snare", beat + position, 0.67)
                    hit("clap", beat + position + 0.012, 0.17)
                for position in (0.75, 2.25, 2.75):
                    hit("snare", beat + position + 0.016, 0.09 + (bar % 2) * 0.025, -0.12)
                for index in range(16):
                    shift = 0.015 if index % 2 else 0
                    volume = (0.22 if index % 2 == 0 else 0.095) * generator.uniform(0.86, 1.12)
                    hit("hat", beat + index * 0.25 + shift, volume, -0.3 if index % 2 else 0.35)
                hit("open", beat + 1.5, 0.17, 0.28)
                hit("open", beat + 3.5, 0.19, 0.28)
                bass_steps = ((0, 0.65, 0), (0.875, 0.6, 0), (1.75, 0.45, 12), (2.5, 0.55, 0), (3.25, 0.5, 7))
                if mode == "breakbeat2":
                    bass_steps = ((0.25, 1.0, 0), (1.5, 0.5, 0), (2.25, 0.75, 12), (3.25, 0.5, 0))
                for position, length, interval in bass_steps:
                    put(low, beat + position, reese(root - 12 + interval, length, 2 if mode == "breakbeat2" else 1), 0.45)
            elif heavy:
                for position in ((0, 0.75, 1.5, 3.25) if local % 2 else (0, 1.5, 2.75)):
                    hit("kick", beat + position, 0.85)
                hit("snare", beat + 2, 0.76 if mode == "heavy" else 0.62)
                for index in range(8):
                    hit("hat", beat + index * 0.5, 0.2 if index % 2 == 0 else 0.12, 0.3)
                riff = ((0, 0.38, 0), (0.75, 0.3, 0), (1.5, 0.3, 0), (2.5, 0.42, 7), (3.25, 0.55, 0))
                for index, (position, length, interval) in enumerate(riff):
                    put(guitars, beat + position, guitar(root - 12 + interval, length, True, index % 3), 0.25 if mode == "half" else 0.34)
                    put(low, beat + position, reese(root - 12, length, 1), 0.3)
            elif mode == "build":
                density = 0.5 if local < 4 else 0.25 if local < 7 else 0.125
                for position in np.arange(0, 4, density):
                    hit("snare", beat + float(position), 0.16 + local * 0.019)
                if local < 6:
                    for position in (0, 2):
                        hit("kick", beat + position, 0.58)
                put(low, beat, reese(root - 12, 3.4, 1), 0.24)
            elif mode == "bridge" and local >= 8:
                hit("kick", beat, 0.5)
                hit("snare", beat + 2, 0.29)

            if fast and local % 4 == 0 or heavy and local % 8 == 0:
                hit("crash", beat, 0.33, -0.24)
            if fast and bar % 2 == 0:
                put(guitars, beat, guitar(root - 12, 6.7, False, local % 3), 0.09 if mode != "final" else 0.14)
            if bar % 2 == 0:
                for pitch in chord:
                    put(music, beat, voice("pad", pitch, round(7 * BEAT, 6)), 0.042 if fast else 0.06)
            if quiet or mode == "half":
                sequence = (0, 2, 1, 3, 2, 1, 3, 2)
                for index, degree in enumerate(sequence):
                    if mode == "outro" and local >= 4 and index % 2:
                        continue
                    offset = 0.02 * (index % 3)
                    level = (0.2 if index % 2 == 0 else 0.145) * (0.66 if mode == "half" else 1)
                    put(music, beat + index * 0.5 + offset, piano(chord[degree], 1.7), level, (degree - 1.5) * 0.15)
                if bar % 2 == 0:
                    put(music, beat, piano(root, 6), 0.25)
            if bar % 2 == 0 and mode != "build":
                phrase = PHRASES[chord_index]
                if mode == "heavy":
                    phrase = tuple((position, pitch - 12, length) for position, pitch, length in phrase)
                for index, (position, pitch, length) in enumerate(phrase):
                    if quiet or mode == "half":
                        put(music, beat + position + 0.025, piano(pitch, length + 0.55), 0.34 if mode != "half" else 0.24)
                    else:
                        volume = 0.17 if mode == "heavy" else 0.24
                        if mode == "breakbeat2" and local < 8:
                            volume *= 0.68
                        put(music, beat + position, voice("lead", pitch, round(length * BEAT, 6)), volume)
                        if mode == "final" and index % 2 == 0:
                            put(music, beat + position, piano(pitch + 12, length + 0.3), 0.095)
            if fast and local % 4 in (1, 3):
                hook = ((0.5, chord[2] + 12), (0.875, chord[2] + 12), (1.5, chord[1] + 12),
                        (2.5, chord[3] + 12), (3.25, chord[2] + 12))
                for index, (position, pitch) in enumerate(hook):
                    timbre = "bell" if index % 2 == 0 else "pluck"
                    put(music, beat + position, voice(timbre, pitch, round(0.26 * BEAT, 6)),
                        0.17, -0.3 if index % 2 else 0.3)
            if (fast or heavy) and local % 8 == 7:
                for index in range(6):
                    hit("tom" if index % 3 else "snare", beat + 2.5 + index * 0.25, 0.22 + index * 0.022,
                        (index / 5 - 0.5) * 0.8)

    for first, end, name, mode in SECTIONS:
        if mode == "build":
            duration = (end - first) * 4 * BEAT
            timeline = np.arange(int(duration * RATE)) / RATE
            progress = timeline / duration
            ramp = noise(duration, 2000, 12000, SEED + first) * progress ** 1.8
            ramp *= 0.55 + 0.45 * np.sin(2 * np.pi * timeline / BEAT * (1 + progress * 2))
            put(effects, first * 4, np.column_stack((ramp, ramp)), 0.095)
        if mode in ("breakbeat", "breakbeat2", "heavy", "final"):
            duration = 2.4
            timeline = np.arange(int(duration * RATE)) / RATE
            impact = noise(duration, 1000, 8500, SEED + first) * np.exp(-timeline / 0.55)
            put(effects, first * 4, np.column_stack((impact, impact)), 0.1)
    ambience = np.zeros_like(music)
    for delay, gain in ((BEAT * 0.75, 0.12), (BEAT * 1.5, 0.065), (0.077, 0.045), (0.119, 0.035)):
        offset = int(delay * RATE)
        ambience[offset:, 0] += music[:-offset, 1] * gain
        ambience[offset:, 1] += music[:-offset, 0] * gain
    music += ambience
    del ambience
    music *= (0.78 + 0.22 * duck)[:, None]
    low *= duck[:, None]
    guitars *= (0.82 + 0.18 * duck)[:, None]
    music += guitars
    music += low
    music += percussion
    music += effects
    del guitars, low, percussion, effects
    for first, end, name, mode in SECTIONS:
        start = sample(first * 4)
        stop = sample(end * 4)
        if mode in ("piano", "bridge", "outro"):
            initial, final = (0.45, 0.7) if mode == "piano" else (0.5, 0.78) if mode == "bridge" else (0.8, 0.5)
            music[start:stop] *= np.linspace(initial, final, stop - start)[:, None]
        elif mode == "final":
            music[start:stop] *= 1.08
    for bar in (40, 112):
        stop = sample(bar * 4)
        start = stop - sample(1)
        curve = np.zeros(stop - start, dtype=np.float32)
        fade = int(0.02 * RATE)
        curve[:fade] = np.linspace(1, 0, fade)
        music[start:stop] *= curve[:, None]
    music[:int(0.08 * RATE)] *= np.linspace(0, 1, int(0.08 * RATE))[:, None]
    fade_start = sample(172 * 4)
    music[fade_start:] *= np.linspace(1, 0, count - fade_start)[:, None] ** 1.4
    music -= np.mean(music, axis=0)
    music *= 0.82 / max(np.max(np.abs(music)), 1e-8)
    assert np.isfinite(music).all()
    output = destination / "manor_neon_remix_premaster.wav"
    with wave.open(str(output), "wb") as wav:
        wav.setnchannels(2)
        wav.setsampwidth(2)
        wav.setframerate(RATE)
        wav.writeframes(np.rint(music * 32767).astype("<i2").tobytes())
    report = {"title": "庄园·霓虹星狱 — 破晓混编", "version": 2, "bpm": BPM, "halftime_bpm": BPM / 2,
              "key": "E minor", "meter": "4/4", "bars": BARS, "seconds": count / RATE, "seed": SEED,
              "events": events, "source": "Original instrumental; no vocals, choir, formant hooks, reference samples or borrowed melodies",
              "references": ["YARARARA: concise hook concept", "Bury the Light: heavy/light dramatic contrast",
                             "Frame of Mind piano edit: piano-to-fast-breakbeat development"],
              "sections": [{"name": name, "bar": first, "seconds": first * 4 * BEAT,
                            "end_seconds": end * 4 * BEAT,
                            "premaster_rms": float(np.sqrt(np.mean(music[sample(first * 4):sample(end * 4)] ** 2)))}
                           for first, end, name, mode in SECTIONS]}
    (destination / "arrangement.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Rendered {output}: {report['seconds']:.3f}s, {events} events", flush=True)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("output", type=Path)
    render(parser.parse_args().output)
