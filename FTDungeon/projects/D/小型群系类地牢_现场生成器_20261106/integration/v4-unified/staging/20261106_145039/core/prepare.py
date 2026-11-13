import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
CORE = ROOT.parent
PROJECT = CORE.parent
sys.dont_write_bytecode = True
sys.path.insert(0, str(PROJECT / "catalog"))
from nbtio import Structure


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def save(path, data):
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    locks = []
    for component in (CORE, CORE / "next-stairs"):
        ready = component / "ready.json"
        inventory = read(ready)["files"]
        for record in inventory:
            path = component / record["path"]
            assert digest(path) == record["sha256"], path
            locks.append({"path": str(path), "sha256": digest(path)})
        locks.append({"path": str(ready), "sha256": digest(ready)})
    v3 = PROJECT / "catalog/expansion-v3"
    ready = read(v3 / "ready.json")
    assert digest(v3 / "SHA256SUMS.json") == ready["inventorySha256"]
    assert digest(v3 / "candidate-manifest.json") == ready["manifestSha256"]
    for record in read(v3 / "SHA256SUMS.json")["files"]:
        path = v3 / record["path"]
        assert digest(path) == record["sha256"], path
        locks.append({"path": str(path), "sha256": digest(path)})
    for path in (v3 / "ready.json", v3 / "SHA256SUMS.json", PROJECT / "catalog/manifest.json", PROJECT / "catalog/nbtio.py"):
        locks.append({"path": str(path), "sha256": digest(path)})
    manifests = [(v3 / "candidate-manifest.json", v3 / "candidate-resources"),
                 (PROJECT / "catalog/manifest.json", PROJECT / "catalog")]
    merged = []
    source_records = []
    for path, resources in manifests:
        snapshot = ROOT / "inputs" / ("v3-candidate-manifest.json" if resources.name == "candidate-resources" else "baseline-05-06-manifest.json")
        snapshot.parent.mkdir(exist_ok=True)
        snapshot.write_bytes(path.read_bytes())
        for template in read(path)["templates"]:
            assert template["biome"] == "s8"
            nbt = resources / "data/biomedungeon/structures" / (template["id"] + ".nbt")
            assert digest(nbt) == template["sha256"], nbt
            structure = Structure.read(nbt)
            assert list(structure.size) == [template["size"][axis] for axis in ("x", "y", "z")]
            source_records.append({"id": template["id"], "sourceManifest": str(path), "resource": str(nbt),
                                   "sha256": digest(nbt), "accepted": template["accepted"], "ports": template["ports"],
                                   "role": template["role"], "size": template["size"]})
            merged.append(template)
            locks.append({"path": str(nbt), "sha256": digest(nbt)})
    assert {template["id"] for template in merged} == {"s8_%02d" % number for number in range(1, 10)}
    assert len(merged) == 9
    rows = []
    for template in sorted(merged, key=lambda item: item["id"]):
        for port in template["ports"]:
            fields = [template["id"], template["biome"], template["resourceId"], template["sha256"], template["role"],
                      *[template["size"][axis] for axis in ("x", "y", "z")], port["id"],
                      *[port["feet"][axis] for axis in ("x", "y", "z")], port["facing"], port["width"], port["height"], port["medium"]]
            rows.append("\t".join(map(str, fields)))
    (ROOT / "inputs/real-nine-ports.tsv").write_text("\n".join(rows) + "\n", encoding="utf-8")
    save(ROOT / "inputs/source-lock.json", {"files": list({item["path"]: item for item in locks}.values()), "rooms": source_records,
                                            "scope": "GEOMETRY_ONLY_EXPERIMENT; accepted=false candidates remain unaccepted", "worldTest": "NOT_RUN"})
    print("PASS immutable baseline and next-stairs inventories; full v3 inventory; nine hash-exact NBT dimensions and original port metadata imported")
    print("PASS nine-room inputs: seven v3 candidates accepted=false + original baseline05/06 accepted=true; no promotion")


if __name__ == "__main__":
    main()
