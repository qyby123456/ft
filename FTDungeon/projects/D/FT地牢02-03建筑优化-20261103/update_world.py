from __future__ import annotations

import array
import copy
import csv
import hashlib
import io
import json
import math
import msvcrt
import os
import shutil
import struct
import sys
import time
import zipfile
import zlib
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
INSTANCE = ROOT.parents[1]
WORLD = INSTANCE / "saves/FT_Dungeon_NewBuildings_20261103"
REGION = WORLD / "region/r.0.0.mca"
RELEASE = Path(r"D:\ft工程文件\导出\FT地牢02-03建筑优化-20261103")
sys.path.insert(0, str(INSTANCE / "output/dungeon-expansion-20261101/tools"))
from nbtio import State, Tag, compound, encode_payload, integer, plain, read_payload, read_string
from flat_world import pack_indices


def digest(payload):
    return hashlib.sha256(payload).hexdigest()


def file_digest(path):
    with path.open("rb") as stream:
        return hashlib.file_digest(stream, "sha256").hexdigest()


def fingerprints(folder, lock_bytes=None):
    result = {}
    for path in sorted(folder.rglob("*")):
        if path.is_file():
            result[path.relative_to(folder).as_posix()] = digest(lock_bytes) if path == WORLD / "session.lock" and lock_bytes is not None else file_digest(path)
    return result


def json_bytes(value):
    return (json.dumps(value, ensure_ascii=False, indent=2) + "\n").encode("utf-8")


def read_region(payload):
    chunks = {}
    for index in range(1024):
        location = struct.unpack_from(">I", payload, index * 4)[0]
        if not location:
            continue
        offset, count = location >> 8, location & 255
        start = offset * 4096
        length = struct.unpack_from(">I", payload, start)[0]
        assert offset >= 2 and 1 < length <= count * 4096 - 4
        assert payload[start + 4] == 2
        chunks[index] = {"record": payload[start:start + length + 4], "timestamp": struct.unpack_from(">I", payload, 4096 + index * 4)[0]}
    return chunks


def read_chunk(record):
    stream = io.BytesIO(zlib.decompress(record[5:]))
    assert stream.read(1) == b"\x0a"
    name = read_string(stream)
    root = read_payload(stream, 10)
    assert not name and not stream.read()
    return root


def decode_states(section):
    states = section.value["block_states"].value
    palette = [State.from_tag(value) for value in states["palette"].value]
    if len(palette) == 1:
        return [palette[0]] * 4096
    bits = max(4, (len(palette) - 1).bit_length())
    per_word = 64 // bits
    mask = (1 << bits) - 1
    packed = states["data"].value
    return [palette[(packed[index // per_word] >> ((index % per_word) * bits)) & mask] for index in range(4096)]


def encode_states(blocks):
    palette = list(dict.fromkeys(blocks))
    data = {"palette": Tag(9, [block.as_tag() for block in palette], 10)}
    if len(palette) > 1:
        lookup = {block: index for index, block in enumerate(palette)}
        data["data"] = pack_indices([lookup[block] for block in blocks], max(4, (len(palette) - 1).bit_length()))
    return Tag(10, data)


def changed_chunks(changes):
    groups = {}
    for room in changes["rooms"]:
        for change in room["changes"]:
            destination = tuple(coordinate + origin for coordinate, origin in zip(change["position"], room["origin"]))
            assert destination[0] // 512 == destination[2] // 512 == 0
            index = (destination[0] // 16) % 32 + ((destination[2] // 16) % 32) * 32
            groups.setdefault(index, []).append((destination, change))
    return groups


def patch_chunks(original, changes):
    records = read_region(original)
    groups = changed_chunks(changes)
    edited = {}
    for index, updates in groups.items():
        tag = read_chunk(records[index]["record"])
        before = copy.deepcopy(tag)
        sections = {entry.value["Y"].value: entry for entry in tag.value["sections"].value}
        decoded = {height: decode_states(section) for height, section in sections.items()}
        original_values = copy.deepcopy(decoded)
        positions = set()
        for destination, change in updates:
            pos_x, pos_y, pos_z = destination
            position = ((pos_y % 16) * 256 + (pos_z % 16) * 16 + pos_x % 16)
            assert (pos_y // 16, position) not in positions
            positions.add((pos_y // 16, position))
            current = decoded[pos_y // 16][position]
            assert current.name == change["from"]["Name"] and all(current.get(key) == value for key, value in change["from"]["Properties"].items()), ("Player or runtime edit conflict", destination, current)
            decoded[pos_y // 16][position] = State(change["to"]["Name"], tuple(sorted(change["to"]["Properties"].items())))
            assert not any(tuple(plain(entity)[name] for name in ("x", "y", "z")) == destination for entity in tag.value["block_entities"].value), ("Unexpected block entity edit", destination)
        for height, section in sections.items():
            if decoded[height] != original_values[height]:
                section.value["block_states"] = encode_states(decoded[height])
            section.value.pop("BlockLight", None)
            section.value.pop("SkyLight", None)
            after = decode_states(section)
            assert after == decoded[height]
            for position, (old, new) in enumerate(zip(original_values[height], after)):
                assert old == new or (height, position) in positions
        tag.value["isLightOn"] = Tag(1, 0)
        tag.value["Heightmaps"] = compound()
        assert plain(tag.value["block_entities"]) == plain(before.value["block_entities"])
        compressed = zlib.compress(b"\x0a\x00\x00" + encode_payload(tag), 6)
        edited[index] = struct.pack(">I", len(compressed) + 1) + b"\x02" + compressed
    header = bytearray(8192)
    output = []
    offset = 2
    for index, record in sorted(records.items()):
        payload = edited.get(index, record["record"])
        count = math.ceil(len(payload) / 4096)
        assert 0 < count < 256
        struct.pack_into(">I", header, index * 4, (offset << 8) | count)
        struct.pack_into(">I", header, 4096 + index * 4, int(time.time()) if index in edited else record["timestamp"])
        output.append(payload.ljust(count * 4096, b"\0"))
        offset += count
    result = bytes(header) + b"".join(output)
    verified = read_region(result)
    assert verified.keys() == records.keys()
    for index in records:
        if index not in edited:
            assert verified[index] == records[index]
        else:
            read_chunk(verified[index]["record"])
    return result, {"edited_chunks": len(edited), "untouched_chunks_preserved": len(records) - len(edited), "changed_blocks": sum(len(room["changes"]) for room in changes["rooms"])}


def readme(changes, native):
    rows = ["# 02、03号建筑优化与逻辑检查", "", "更新日期：2026-11-03。更新目标：" + str(WORLD), "", "## 02 霜雪花园中庭 L2", "", "花坛外移并降低围边，补齐喷泉周围连续双格环路，清理路面覆雪。保留8株花、雪松、冰池与喷泉。冷光限制为10级，叶片保持持久化。", "", "## 03 血棘祭炉祭坛厅 M1", "", "悬笼由4座收敛为2座，统一链条吊挂并整理铁栏连接，释放前场。入口至祭坛采用三格齐平红砖轴线。保留两侧绕行、牢房铁门和双面压板，祭炉加防坠铁栅盖，木梁去皮并补石质节点。", "", "## 验证", "", "- 使用Minecraft 1.20.1、Forge 47.4.10原生放置，对原版和优化版分别检测4个方向，共16次放置。", "- 强制邻接更新、水源流体刻，并观察240个游戏刻。", "- 检测原生方块生存条件、干燥碰撞通路、水池侧壁及底部密封、掉落物、附着物，以及铁门两侧压板信号。", "- 优化后的候选必须无方块类型丢失、无不受支持附着物、无漏水、无掉落，目标通道全部连通。", "- 压板检测直接切换原生压板状态，验证红石信号到门的路径；不冒充玩家踩踏或多人游玩测试。", "- 目前关闭随机刻及火势蔓延，与预览存档设置一致；未做完整整合包客户端的新版画面和战斗验收。", "", "## 更新与回退", "", "仅修改两间建筑中清单列出的方块；其他房间、区块记录、玩家数据、模组和配置保持原状。原模板仍保存在建筑原始文件目录。更新前完整世界备份保存在导出目录，updated-world.zip为优化后副本。", "", "原生报告：qa/forge-native.json。逐块变更：changes.json。", ""]
    for room in changes["rooms"]:
        rows.extend([room["code"] + " " + room["id"] + "：" + str(room["changed_blocks"]) + "处方块优化；SHA256 " + room["candidate_sha256"], ""])
    return "\n".join(rows).encode("utf-8")


def main():
    changes = json.loads((ROOT / "changes.json").read_text(encoding="utf-8"))
    native = json.loads((ROOT / "qa/forge-native.json").read_text(encoding="utf-8"))
    assert native["passed"] and native["ticks_observed"] == 240 and len(native["placements"]) == 16
    for room in changes["rooms"]:
        candidate = ROOT / "candidate/incoming" / room["id"] / "room.nbt"
        assert file_digest(candidate) == room["candidate_sha256"]
        metadata = json.loads(candidate.with_name("room.json").read_text(encoding="utf-8"))
        assert metadata["sha256"] == room["candidate_sha256"] and metadata["source_sha256"] == room["source_sha256"]
    assert WORLD.resolve().parent == (INSTANCE / "saves").resolve()
    lock = WORLD / "session.lock"
    with lock.open("r+b") as handle:
        lock_bytes = handle.read()
        handle.seek(0)
        try:
            msvcrt.locking(handle.fileno(), msvcrt.LK_NBLCK, 3)
        except OSError as failure:
            raise RuntimeError("预览存档正在运行，请保存并退出该世界后再部署；优化文件已保留") from failure
        try:
            before = fingerprints(WORLD, lock_bytes)
            preserved = {str(folder): fingerprints(folder) for folder in [*[(INSTANCE / name) for name in ("mods", "config", "defaultconfigs", "kubejs") if (INSTANCE / name).exists()], *[folder for folder in (INSTANCE / "saves").iterdir() if folder.is_dir() and folder != WORLD]]}
            original = REGION.read_bytes()
            patched, validation = patch_chunks(original, changes)
            RELEASE.mkdir(parents=True, exist_ok=True)
            backup = RELEASE / "before-optimization.zip"
            assert not backup.exists(), "Refusing to overwrite existing backup"
            with zipfile.ZipFile(backup, "x", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
                for relative in sorted(before):
                    if relative == "session.lock":
                        archive.writestr(WORLD.name + "/" + relative, lock_bytes)
                    else:
                        archive.write(WORLD / relative, WORLD.name + "/" + relative)
            with zipfile.ZipFile(backup) as archive:
                assert archive.testzip() is None
                for relative, expected in before.items():
                    with archive.open(WORLD.name + "/" + relative) as stream:
                        assert hashlib.file_digest(stream, "sha256").hexdigest() == expected
            staged_region = ROOT / "patched-region.mca"
            staged_region.write_bytes(patched)
            assert REGION.read_bytes() == original
            assert fingerprints(WORLD, lock_bytes) == before
            for folder, expected in preserved.items():
                assert fingerprints(Path(folder)) == expected, folder
            manifest_path = WORLD / "建筑清单.json"
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            for room in manifest["rooms"]:
                if room["id"] in {entry["id"] for entry in changes["rooms"]}:
                    for source in room["source_files"].values():
                        assert file_digest(Path(source["path"])) == source["sha256"]
            active = {room["id"]: room for room in changes["rooms"]}
            manifest["current_update"] = {"date": "2026-11-03", "rooms": list(active), "report": "02-03原生检测.json", "native_four_rotations_240_ticks": "PASS"}
            staged_files = {manifest_path: None}
            note = readme(changes, native)
            for room in manifest["rooms"]:
                if room["id"] in active:
                    row = active[room["id"]]
                    room["active_optimization"] = {"revision": "20261103", "sha256": row["candidate_sha256"], "changed_blocks": row["changed_blocks"], "native_four_rotations_240_ticks": "PASS", "full_modpack_client_tested": False}
            staged_files[manifest_path] = json_bytes(manifest)
            guide_path = WORLD / "地图说明.md"
            guide = guide_path.read_text(encoding="utf-8")
            heading, remainder = guide.split("\n", 1)
            staged_files[guide_path] = (heading + "\n\n更新：02（L2）、03（M1）已优化，完成独立Forge四朝向240刻实放、液体与门信号检测。详情见02-03优化说明.md；下文未更新的离线统计属于首次部署记录。\n" + remainder).encode("utf-8")
            catalog_path = WORLD / "建筑目录.csv"
            rows = list(csv.DictReader(io.StringIO(catalog_path.read_text(encoding="utf-8-sig"))))
            table = io.StringIO(newline="")
            fieldnames = list(rows[0]) + ["当前模板SHA256", "优化版本"]
            output = csv.DictWriter(table, fieldnames=fieldnames)
            output.writeheader()
            for row in rows:
                identity = row["ID"]
                row["当前模板SHA256"] = active[identity]["candidate_sha256"] if identity in active else row["模板SHA256"]
                row["优化版本"] = "20261103-四朝向240刻通过" if identity in active else "首次部署版本"
                output.writerow(row)
            staged_files[catalog_path] = table.getvalue().encode("utf-8-sig")
            staged_files[WORLD / "02-03优化说明.md"] = note
            staged_files[WORLD / "02-03原生检测.json"] = (ROOT / "qa/forge-native.json").read_bytes()
            staged_files[WORLD / "02-03逐块变更.json"] = (ROOT / "changes.json").read_bytes()
            for identity in active:
                for name in ("room.nbt", "room.json"):
                    destination = WORLD / "建筑优化文件" / identity / name
                    assert not destination.exists()
                    staged_files[destination] = (ROOT / "candidate/incoming" / identity / name).read_bytes()
            modified = []
            try:
                os.replace(staged_region, REGION)
                modified.append(REGION)
                for destination, payload in staged_files.items():
                    destination.parent.mkdir(parents=True, exist_ok=True)
                    pending = destination.with_name(destination.name + ".ft-update")
                    assert not pending.exists()
                    pending.write_bytes(payload)
                    os.replace(pending, destination)
                    modified.append(destination)
                after = fingerprints(WORLD, lock_bytes)
                expected_changes = {destination.relative_to(WORLD).as_posix() for destination in modified}
                actual_changes = {name for name in set(before) | set(after) if before.get(name) != after.get(name)}
                assert actual_changes == expected_changes
                assert REGION.read_bytes() == patched
                for folder, expected in preserved.items():
                    assert fingerprints(Path(folder)) == expected, folder
            except Exception:
                with zipfile.ZipFile(backup) as archive:
                    for destination in reversed(modified):
                        relative = destination.relative_to(WORLD).as_posix()
                        if relative in before:
                            destination.write_bytes(archive.read(WORLD.name + "/" + relative))
                        else:
                            assert destination.resolve().is_relative_to(WORLD.resolve()) and destination.is_file()
                            destination.unlink()
                raise
            report = {"installed": True, "world": str(WORLD), "rooms": list(active), "validation": validation, "source_files_unchanged": True,
                      "other_saves_mods_configs_unchanged": True, "player_data_unchanged": True, "full_modpack_client_tested": False,
                      "native_four_rotations_240_ticks": "PASS", "backup": str(backup), "backup_sha256": file_digest(backup), "modified_files": sorted(expected_changes)}
            (ROOT / "update-report.json").write_bytes(json_bytes(report))
            (RELEASE / "update-report.json").write_bytes(json_bytes(report))
            (ROOT / "README.md").write_bytes(note)
            (RELEASE / "优化说明.md").write_bytes(note)
            shutil.copy2(ROOT / "changes.json", RELEASE / "changes.json")
            shutil.copy2(ROOT / "qa/forge-native.json", RELEASE / "forge-native.json")
            for identity in active:
                target = RELEASE / "优化建筑" / identity
                shutil.copytree(ROOT / "candidate/incoming" / identity, target)
                shutil.copytree(ROOT / "previews" / identity, RELEASE / "预览" / identity)
            shutil.copy2(Path(__file__), RELEASE / "update_world.py")
            shutil.copytree(ROOT / "forge/src/main/java", RELEASE / "检测源代码")
            shutil.copy2(ROOT / "forge-run-03.log", RELEASE / "forge-run.log")
            updated = RELEASE / "updated-world.zip"
            assert not updated.exists()
            with zipfile.ZipFile(updated, "x", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
                for relative in sorted(after):
                    if relative == "session.lock":
                        archive.writestr(WORLD.name + "/" + relative, lock_bytes)
                    else:
                        archive.write(WORLD / relative, WORLD.name + "/" + relative)
            with zipfile.ZipFile(updated) as archive:
                assert archive.testzip() is None
                for relative, expected in after.items():
                    with archive.open(WORLD.name + "/" + relative) as stream:
                        assert hashlib.file_digest(stream, "sha256").hexdigest() == expected
            report.update(updated_world=str(updated), updated_world_sha256=file_digest(updated))
            (ROOT / "update-report.json").write_bytes(json_bytes(report))
            (RELEASE / "update-report.json").write_bytes(json_bytes(report))
            print(json.dumps(report, ensure_ascii=False, indent=2), flush=True)
        finally:
            handle.seek(0)
            msvcrt.locking(handle.fileno(), msvcrt.LK_UNLCK, 3)


if __name__ == "__main__":
    main()

