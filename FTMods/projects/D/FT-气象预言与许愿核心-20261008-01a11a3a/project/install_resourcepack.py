import argparse
import hashlib
import json
import os
import re
import shutil
from datetime import datetime
from pathlib import Path


ROOT = Path(__file__).resolve().parent
WORKSPACE = ROOT.parent.parent.resolve()
PACK_NAME = "FT_Instruments_Remodel_20261008"
PACK = WORKSPACE / "resourcepacks" / PACK_NAME
PACK_ID = "file/" + PACK_NAME
OPTIONS = WORKSPACE / "options.txt"
DESCRIPTION = "FT 仪器模型重制 · 天气预报机与预言机"


def payload_files():
    return {file.relative_to(ROOT / "payload"): file for file in (ROOT / "payload").rglob("*") if file.is_file()}


def pack_options(content):
    match = re.search(r"^resourcePacks:([^\r\n]*)", content.decode("utf-8"), re.MULTILINE)
    if not match:
        raise ValueError("Minecraft resourcePacks option not found")
    return match, json.loads(match.group(1))


def verify():
    for relative, source in payload_files().items():
        destination = PACK / relative
        if not destination.is_file() or destination.read_bytes() != source.read_bytes():
            raise ValueError(f"Resource-pack mismatch: {relative}")
    metadata = json.loads((PACK / "pack.mcmeta").read_text(encoding="utf-8"))
    assert metadata["pack"]["pack_format"] == 15
    _, packs = pack_options(OPTIONS.read_bytes())
    assert packs[-1] == PACK_ID and packs.count(PACK_ID) == 1
    return {"pack": str(PACK), "enabled": True, "highest_priority": True, "resources": len(payload_files()),
            "legacy_oracle_compatibility": True, "qys_bosses_jar_modified": False,
            "options_sha256": hashlib.sha256(OPTIONS.read_bytes()).hexdigest()}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--verify-only", action="store_true")
    arguments = parser.parse_args()
    if not PACK.resolve().is_relative_to(WORKSPACE / "resourcepacks"):
        raise ValueError("Resource-pack path escapes intended directory")
    if not arguments.verify_only:
        metadata = PACK / "pack.mcmeta"
        if PACK.exists() and (not metadata.exists() or json.loads(metadata.read_text(encoding="utf-8"))["pack"].get("description") != DESCRIPTION):
            raise ValueError("Existing resource pack does not belong to this task")
        for relative, source in payload_files().items():
            destination = PACK / relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source, destination)
        metadata.write_text(json.dumps({"pack": {"pack_format": 15, "description": DESCRIPTION}}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        before = OPTIONS.read_bytes()
        match, packs = pack_options(before)
        packs = [pack for pack in packs if pack != PACK_ID] + [PACK_ID]
        content = before.decode("utf-8")
        replacement = json.dumps(packs, ensure_ascii=True, separators=(",", ":"))
        updated = (content[:match.start(1)] + replacement + content[match.end(1):]).encode("utf-8")
        if updated != before:
            backup = ROOT / "backups" / ("options.txt." + datetime.now().strftime("%Y%m%d-%H%M%S") + ".bak")
            backup.parent.mkdir(exist_ok=True)
            backup.write_bytes(before)
            temporary = OPTIONS.with_name("options.txt.ft-instruments.tmp")
            if temporary.exists():
                raise ValueError("Options temporary file already exists")
            temporary.write_bytes(updated)
            try:
                if OPTIONS.read_bytes() != before:
                    raise RuntimeError("Another editor changed options.txt; activation aborted")
                os.replace(temporary, OPTIONS)
            finally:
                temporary.unlink(missing_ok=True)
        other_before = re.sub(r"^resourcePacks:[^\r\n]*", "", before.decode("utf-8"), flags=re.MULTILINE)
        other_after = re.sub(r"^resourcePacks:[^\r\n]*", "", OPTIONS.read_bytes().decode("utf-8"), flags=re.MULTILINE)
        assert other_before == other_after
    report = verify()
    (ROOT / "resourcepack-installation.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
