import argparse
import collections
import csv
import hashlib
import html
import importlib.util
import json
import shutil
import subprocess
import zipfile
from pathlib import Path


PROJECT = Path(__file__).resolve().parents[1]
SPEC = importlib.util.spec_from_file_location("skill_optimizer", PROJECT / "tools/model3d/optimize_skill_meshes.py")
OPTIMIZER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(OPTIMIZER)


def digest(file):
    return hashlib.sha256(file.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--work", required=True, type=Path)
    parser.add_argument("--runtime", required=True, type=Path)
    parser.add_argument("--jar-tool", required=True, type=Path)
    parser.add_argument("--sync-source", action="store_true")
    arguments = parser.parse_args()
    work = arguments.work.resolve()
    overlay = work / "overlay"
    report = json.loads((work / "mesh-audit.json").read_text(encoding="utf-8"))
    visual = json.loads((work / "previews/visual-checks.json").read_text(encoding="utf-8"))
    manifest = json.loads((work / "previews/manifest.json").read_text(encoding="utf-8"))
    checked = {record["id"] for record in visual["records"]}
    if checked != {record["id"] for record in manifest} or visual["views"] != ["front", "back", "left", "right"]:
        raise ValueError("Incomplete four-view mesh coverage")
    maximum_visual_error = max(max(record["meanAbsoluteError"]) for record in visual["records"])
    if maximum_visual_error > 2.0:
        raise ValueError("Visual difference exceeds two RGB levels per channel averaged over a view")
    replacements = {file.relative_to(overlay).as_posix(): file for file in overlay.rglob("*.json")}
    baseline = work / "package-baseline.jar"
    shutil.copy2(arguments.runtime, baseline)
    with zipfile.ZipFile(work / "baseline.jar") as analyzed, zipfile.ZipFile(baseline) as current:
        for name in replacements:
            if analyzed.read(name) != current.read(name):
                raise ValueError("Concurrent runtime model change: " + name)
        for texture in (work / "previews/assets").rglob("*.png"):
            name = texture.relative_to(work / "previews").as_posix()
            if texture.read_bytes() != current.read(name):
                raise ValueError("Concurrent runtime texture change: " + name)
        for name, file in replacements.items():
            resource = json.loads(file.read_text(encoding="utf-8"))
            for mesh in OPTIMIZER.process_resource(resource):
                if mesh["before"] != mesh["after"]:
                    raise ValueError("Optimizer is not idempotent: " + name)
            if "/maque_arcane/" in name:
                for mesh in OPTIMIZER.optimize_maque_density(resource):
                    if mesh["before"] != mesh["after"]:
                        raise ValueError("Density optimization is not idempotent: " + name)
        if arguments.sync_source:
            for name, file in replacements.items():
                source = PROJECT / "src/main/resources" / name
                original = current.read(name)
                if source.read_bytes() not in (original, file.read_bytes()):
                    raise ValueError("Concurrent source model change: " + name)
            for name, file in replacements.items():
                source = PROJECT / "src/main/resources" / name
                backup = work / "source-before" / name
                if not backup.exists():
                    backup.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(source, backup)
                source.write_bytes(file.read_bytes())
    candidate = work / "qys-bosses.jar"
    shutil.copy2(baseline, candidate)
    subprocess.run([str(arguments.jar_tool), "--update", "--file", str(candidate), "-C", str(overlay), "."], check=True)
    with zipfile.ZipFile(baseline) as before, zipfile.ZipFile(candidate) as after:
        names = before.namelist()
        if set(names) != set(after.namelist()) or len(after.namelist()) != len(set(after.namelist())):
            raise ValueError("JAR entry set changed or duplicates introduced")
        changed = [name for name in names if before.read(name) != after.read(name)]
        if set(changed) != set(replacements):
            raise ValueError("Unexpected changed JAR entries")
        for name, file in replacements.items():
            if after.read(name) != file.read_bytes():
                raise ValueError("Preview/staged/packaged geometry mismatch")
        unchanged_classes = sum(name.endswith(".class") for name in names)
    totals = collections.defaultdict(collections.Counter)
    for row in report["meshes"]:
        totals[row["resource"]]["before"] += row["before"]
        totals[row["resource"]]["after"] += row["after"]
    with (work / "mesh-counts.csv").open("w", newline="", encoding="utf-8-sig") as stream:
        writer = csv.writer(stream)
        writer.writerow(["资源", "网格路径", "拓扑类型", "优化前面数", "优化后面数", "减少"])
        for row in report["meshes"]:
            writer.writerow([row["resource"], row["path"], row["type"], row["before"], row["after"], row["before"] - row["after"]])
    rows = "".join(f"<tr><td>{html.escape(name)}</td><td>{counts['before']:,}</td><td>{counts['after']:,}</td><td>{counts['before'] - counts['after']:,}</td></tr>" for name, counts in totals.items())
    (work / "report.html").write_text("<!doctype html><meta charset='utf-8'><title>技能模型面数审计</title>"
        "<style>body{font:15px system-ui;margin:32px;background:#101822;color:#ddd}td,th{padding:6px 14px;text-align:left;border-bottom:1px solid #345}a{color:#8ce}</style>"
        "<h1>技能模型面数审计与优化</h1><p>完整扫描已安装 JAR 的模型资源；包含备用/旧网格和非庄园共用资源，并不等于单帧同时提交的面数。"
        "三角面、四边面与 Bedrock 方块面分别按源拓扑计数；不同 LOD 不叠加为单个技能运行负载。</p>"
        "<p>无损合面保留 UV、材质、透明度、骨骼、枢轴和部件边界。麻雀仅复用已有低面数曲面，保留主细节部件、折射材质和更小的远景 LOD；"
        "逐部件包围盒最大偏差不超过 0.004 格。低面数网格及缺少安全合面条件的模型保留原样，不进行随机删面。</p>"
        "<p><a href='previews/index.html'>四视图及关键帧对比</a> · <a href='mesh-counts.csv'>全部网格面数 CSV</a></p>"
        "<table><tr><th>资源</th><th>优化前</th><th>优化后</th><th>减少</th></tr>" + rows + "</table>", encoding="utf-8")
    receipt = {"analysis_baseline_sha256": report["baseline_sha256"], "package_baseline_sha256": digest(baseline),
               "candidate_sha256": digest(candidate), "changed_resources": sorted(changed), "unchanged_classes": unchanged_classes,
               "source_synced": arguments.sync_source, "checked_meshes": len(manifest), "checked_animation_states": len(visual["records"]),
               "maximum_preview_mean_absolute_error": maximum_visual_error, "docs_impact": "none"}
    (work / "build-report.json").write_text(json.dumps(receipt, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(receipt, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
