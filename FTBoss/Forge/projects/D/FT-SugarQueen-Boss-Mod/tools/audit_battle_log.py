import collections
import gzip
import hashlib
import json
from pathlib import Path
import re
import sys


def main():
    task = Path(sys.argv[1]).resolve()
    evidence = task / "evidence"
    manifests = []
    documents = {}
    for name in ("latest-233936.log", "2026-10-10-1.log.gz", "debug-1.log.gz"):
        path = evidence / name
        raw = path.read_bytes()
        decoded = gzip.decompress(raw) if name.endswith(".gz") else raw
        prefix = len(decoded) - len(decoded.lstrip(b"\0"))
        text = decoded[prefix:].decode("cp936")
        text = re.sub(r"(--accessToken[\s,=]+)([^\s,\]]+)", r"\1[REDACTED]", text)
        lines = text.splitlines()
        normalized = evidence / (name.replace(".log.gz", "") + ".normalized.log")
        normalized.write_text("\n".join(lines) + "\n", encoding="utf-8")
        manifests.append({"file": name, "sha256": hashlib.sha256(raw).hexdigest(),
                          "compressedBytes": len(raw), "decodedBytes": len(decoded),
                          "leadingNulBytes": prefix, "lineCount": len(lines),
                          "normalized": str(normalized)})
        documents[name] = lines

    lines = documents["2026-10-10-1.log.gz"]
    debug = documents["debug-1.log.gz"]
    def row(number, line):
        timestamp = re.search(r"\d{2}:\d{2}:\d{2}\.\d{3}", line)
        return {"line": number, "time": timestamp.group() if timestamp else None, "text": line}
    battles = []
    entries = {}
    defeated = []
    lag = []
    commands = []
    for number, line in enumerate(lines, 1):
        if "[Server thread/" not in line:
            continue
        if "庄园战场开始" in line:
            battles.append({"start": row(number, line)})
        if "[FTBoss][Manor] 战场" in line and "胜利=" in line and battles:
            battles[-1]["end"] = row(number, line)
        match = re.search(r"\[FTBoss\]\[Manor\] 上场: (.+?) \(", line)
        if match:
            entries[match[1]] = row(number, line)
        match = re.search(r"\[FTBoss\]\[Manor\] 已击败 (.+?) \(", line)
        if match and match[1] in entries:
            start = entries.pop(match[1])
            end = row(number, line)
            def milliseconds(time):
                hours, minutes, seconds = time.split(":")
                return int(hours) * 3600000 + int(minutes) * 60000 + round(float(seconds) * 1000)
            defeated.append({"boss": match[1], "start": start, "end": end,
                             "elapsedMs": (milliseconds(end["time"]) - milliseconds(start["time"])) % 86400000})
        match = re.search(r"Running (\d+)ms or (\d+) ticks behind", line)
        if match:
            lag.append({**row(number, line), "behindMs": int(match[1]), "behindTicks": int(match[2])})
        if re.search(r"\[@: 杀死了", line):
            commands.append(row(number, line))

    markers = {
        "bossBarReflection": "主题血条绘制异常",
        "missingSpeedBootsTexture": "speed_boots_layer_1.png",
        "emfModelLimit": "model attempted creation more than 64 times",
        "genericKillBoss": "被无视无敌的伤害终结",
    }
    groups = {name: [row(number, line) for number, line in enumerate(lines, 1)
                     if marker in line and (name != "emfModelLimit" or "EMF" in line)]
              for name, marker in markers.items()}
    fatal = re.compile(r"VerifyError|NoClassDefFoundError|NoSuchMethodError|Exception ticking|Preparing crash report|This crash report has been saved")
    warnings = collections.Counter()
    for line in lines:
        for name, marker in (("reviveInsteadOfReviveCaps", "called revive instead of reviveCaps"),
                             ("solCarrotAir", "solcarrot"), ("crockPotAir", "Crock Pot")):
            if marker in line and (name == "reviveInsteadOfReviveCaps" or "minecraft:air" in line):
                warnings[name] += 1
    debug_markers = ("unknown string 'ftboss:statue'", "unknown string 'ftboss:butcher_statue'",
                     "loot_tables:irons_spellbooks:chests/catacombs/crypt_loot",
                     "loot_tables:irons_spellbooks:chests/citadel/citadel_tomes", "set_written_book_pages")
    result = {"evidence": manifests, "primaryLog": "2026-10-10-1.normalized.log",
              "battles": battles, "serverLag": lag, "bossDurations": defeated,
              "subSecondBosses": [item for item in defeated if item["elapsedMs"] < 1000],
              "confirmedCommandEvidence": commands,
              "targetSelectionTestEvidence": [row(number, line) for number, line in enumerate(lines, 1)
                                              if "已选择第一个实体" in line], "groups": groups,
              "fatalRuntimeLinkOrTickMarkers": [row(number, line) for number, line in enumerate(debug, 1) if fatal.search(line)],
              "startupResourceExcerpts": [row(number, line) for number, line in enumerate(debug, 1)
                                          if any(marker in line for marker in debug_markers)],
              "repeatedWarnings": dict(warnings),
              "limits": ["Damage source, damage amount and HP deltas are not recorded per hit.",
                         "Current post-session deployed JAR is not proven identical to the session JAR.",
                         "Server backlog does not establish a single freeze or its root cause.",
                         "Nearby kill-command feedback does not establish the cause of every subsequent death."]}
    (task / "log-analysis.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"battles": len(battles), "serverLagWarnings": len(lag),
                      "maximumBacklogMs": max(item["behindMs"] for item in lag),
                      "subSecondBosses": [(item["boss"], item["elapsedMs"]) for item in result["subSecondBosses"]],
                      "fatalMarkers": len(result["fatalRuntimeLinkOrTickMarkers"]),
                      "groups": {name: len(group) for name, group in groups.items()}}, ensure_ascii=False))


if __name__ == "__main__":
    main()
