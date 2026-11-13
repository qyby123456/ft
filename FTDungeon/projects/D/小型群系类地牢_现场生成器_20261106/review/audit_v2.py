import hashlib
import json
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parent
COMPONENT = ROOT.parent / "runtime" / "next"


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    ready_path = COMPONENT / "ready.json"
    ready_hash = digest(ready_path)
    ready = json.loads(ready_path.read_text(encoding="utf-8-sig"))
    assert ready["ready"] and ready["runtimeJournalVersion"] == 2
    assert ready["productionPromotion"] is False
    verified = []
    for relative, expected in ready["files"].items():
        path = (COMPONENT / relative).resolve()
        assert path.is_relative_to(COMPONENT.resolve()), relative
        assert digest(path) == expected, relative
        verified.append(relative)
    artifact = COMPONENT / ready["artifact"]
    assert digest(artifact) == ready["artifactSha256"]
    with zipfile.ZipFile(artifact) as archive:
        assert archive.testzip() is None
        names = archive.namelist()
        forbidden = [name for name in names if any(
            token in name.lower()
            for token in ("gametest", "ftbd_payloadtest", "ftbd_berestart", "selftest")
        )]
        assert not forbidden, forbidden
        manifest = json.loads(archive.read("data/biomedungeon/manifest.json"))
        templates = manifest["templates"]
        assert {entry["id"] for entry in templates} == {"s8_05", "s8_06"}
        for entry in templates:
            namespace, identifier = entry["resourceId"].split(":")
            payload = archive.read(f"data/{namespace}/structures/{identifier}.nbt")
            assert hashlib.sha256(payload).hexdigest() == entry["sha256"]
        class_versions = set()
        for name in names:
            if name.endswith(".class"):
                payload = archive.read(name)
                assert payload[:4] == b"\xca\xfe\xba\xbe", name
                class_versions.add(int.from_bytes(payload[6:8], "big"))
        assert max(class_versions) <= 61, class_versions
    evidence = json.loads((COMPONENT / "world-evidence.json").read_text(encoding="utf-8-sig"))
    world_files = evidence["files"]
    for entry in world_files:
        path = (COMPONENT / entry["path"]).resolve()
        assert path.is_relative_to(COMPONENT.resolve()), entry["path"]
        assert path.stat().st_size == entry["bytes"], entry["path"]
        assert digest(path) == entry["sha256"], entry["path"]
    observations = {}
    for name in ("payload-world-final.log", "be-restart-prepare.log", "be-restart-resume.log"):
        lines = (COMPONENT / name).read_text(encoding="utf-8-sig", errors="replace").splitlines()
        observations[name] = [line for line in lines if any(
            marker in line for marker in ("WORLD_PASS", "RESTART_", "required tests passed")
        )]
    assert digest(ready_path) == ready_hash, "Concurrent ready mutation"
    report = {
        "scope": "Independent artifact integrity and packaged template inventory, not independent world acceptance",
        "ready_sha256": ready_hash,
        "verified_component_files": len(verified),
        "verified_saved_world_files": len(world_files),
        "artifact": str(artifact),
        "artifact_sha256": digest(artifact),
        "jar_crc": "PASS",
        "class_major_versions": sorted(class_versions),
        "packaged_template_ids": [entry["id"] for entry in templates],
        "test_fixtures_excluded": True,
        "producer_world_log_observations": observations,
        "five_real_room_command_pool_promoted": False,
        "independent_v2_world_acceptance": "PENDING_INTEGRATION",
        "full_generator_complete": False,
    }
    (ROOT / "v2-handoff-audit.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
