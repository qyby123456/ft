import argparse
import hashlib
import json
import shutil
import subprocess
import zipfile
from pathlib import Path


def run(command, cwd=None):
    result = subprocess.run(command, cwd=cwd, capture_output=True)
    if result.returncode:
        raise RuntimeError(result.stderr.decode("utf-8", errors="replace"))
    return result.stdout.decode("utf-8", errors="replace")


def export(args):
    root = Path(__file__).resolve().parents[2]
    destination = args.output.resolve()
    arrangement = json.loads((args.audio / "arrangement.json").read_text(encoding="utf-8"))
    listen = destination / "00_直接试听_mp3"
    added = destination / "01_新增_庄园霓虹星狱"
    complete = destination / "02_模组内全部音频"
    comparison = destination / "99_对比_庄园原曲"
    for folder in (listen, added, complete, comparison):
        folder.mkdir(parents=True, exist_ok=True)
    for name in ("manor_neon_remix.wav", "manor_neon_remix.ogg", "arrangement.json"):
        shutil.copy2(args.audio / name, added / name)
    for name in ("render_manor_neon_remix.py", "render_manor_neon_remix_v2.py", "export_manor_neon_remix.py"):
        shutil.copy2(root / "tools/music" / name, added / name)
    entries = []
    prefix = "assets/ftboss/sounds/"
    with zipfile.ZipFile(args.jar) as archive:
        for name in sorted(archive.namelist()):
            if not name.startswith(prefix) or not name.endswith(".ogg"):
                continue
            relative = Path(name[len(prefix):])
            target = (complete / relative).resolve()
            if not target.is_relative_to(complete.resolve()):
                raise ValueError(f"Unsafe archive entry: {name}")
            target.parent.mkdir(parents=True, exist_ok=True)
            content = archive.read(name)
            target.write_bytes(content)
            measured = json.loads(run(["node", str(root / "tools/music/verify-ogg.cjs"), str(target)], root / "tools/music"))
            if measured["errors"]:
                raise ValueError(f"Decode failed: {name}")
            measured["sha256"] = hashlib.sha256(content).hexdigest()
            measured["relative"] = relative.as_posix()
            entries.append(measured)
            if relative.parts[0] == "music":
                title = "庄园-霓虹星狱" if target.stem == "manor_neon_remix" else target.stem
                mp3 = listen / f"{title}.mp3"
                run([str(args.ffmpeg), "-hide_banner", "-y", "-i", str(target), "-af", "volume=0dB",
                     "-c:a", "libmp3lame", "-b:a", "320k", str(mp3)])
            if target.stem in ("manor_guignol", "manor_dual_battle"):
                shutil.copy2(target, comparison / target.name)
                shutil.copy2(listen / f"{target.stem}.mp3", comparison / f"{target.stem}.mp3")
            print(f"Verified {relative}: {measured['seconds']}s, peak={measured['peak']}", flush=True)
    new = next(entry for entry in entries if entry["relative"] == "music/manor_neon_remix.ogg")
    if new["peak"] >= 1 or new["channels"] != 2 or abs(new["seconds"] - arrangement["seconds"]) > 0.01:
        raise ValueError("New track failed audio quality checks")
    if new["sha256"] != hashlib.sha256((added / "manor_neon_remix.ogg").read_bytes()).hexdigest():
        raise ValueError("Audition copy does not match packaged track")
    durations = run(["node", str(root / "tools/audio_tools/ogg_dur.cjs")]
                    + [str(complete / entry["relative"]) for entry in entries])
    (destination / "音频时长核对.txt").write_text(durations, encoding="utf-8-sig")
    (destination / "音频校验.json").write_text(json.dumps(entries, ensure_ascii=False, indent=2), encoding="utf-8")
    status = "已由官方脚本同步四处目标" if args.deployed else "已构建，尚未部署到实例；请以本次交付说明为准"
    lines = [f"庄园战斗曲：{arrangement['title']}", "",
             f"原创钢琴/电子摇滚/DnB纯音乐；{arrangement['bpm']} BPM，{arrangement['key']}，4/4，{arrangement['seconds']:.3f}秒。",
             "不采样或复制参考曲，旋律、和声编排和合成音色均由附带源码生成。",
             "参考取向：YARARARA的短句记忆点、Bury the Light的轻重反差、Frame of Mind钢琴混编的钢琴转高速碎拍。",
             "纯器乐：没有人声、合唱、人声切片、拟人声共振峰音色或原曲采样；短句改用水晶拨弦/电子钟音。",
             "播放顺序：庄园·鏖战 → 双轨·战斗高潮 → 霓虹星狱V2，总计约10分06秒。",
             "同一场战斗各播一次，顺序续播，不叠加、不循环，不改开场演出曲。", "",
             "00_直接试听_mp3：全部音乐320 kbps MP3；新曲为‘庄园-霓虹星狱.mp3’。",
             "01_新增_庄园霓虹星狱：新曲WAV母带、游戏Ogg、完整可复现合成源码及段落时间表。",
             "02_模组内全部音频：从本次指定构建jar逐字节导出，保留music/effects结构。",
             "99_对比_庄园原曲：原两首庄园曲供风格比较，并非被替换或废弃。", "",
             "99_对比_霓虹星狱V1：本次重做前的140 BPM四拍电子版；未继续加入游戏播放列表。",
             f"部署状态：{status}。", f"来源jar：{args.jar.resolve()}",
             f"jar SHA256：{hashlib.sha256(args.jar.read_bytes()).hexdigest().upper()}",
             f"新曲：Ogg解码峰值{new['peak']}，RMS {new['rms']}，无削波或解码错误。",
             "MP3直接由Ogg以0dB增益编码，保持游戏电平；未对旧音频重新母带处理。",
             "旧曲部分已有峰值超过1，原样导出并在清单保留读数，不属于本次新增曲削波。",
             "自动化构建/解码检查不代替实际游戏声卡、音量和主观试听验收。", "", f"全量音频：{len(entries)}份", ""]
    lines.extend(f"{entry['relative']} | {entry['seconds']}s | {entry['channels']}ch | peak {entry['peak']} | errors 0" for entry in entries)
    (destination / "说明.txt").write_text("\n".join(lines), encoding="utf-8-sig")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--audio", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--ffmpeg", required=True, type=Path)
    parser.add_argument("--deployed", action="store_true")
    export(parser.parse_args())
