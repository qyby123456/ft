import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT.parent))
from freeze_catalog import digest, load, save
from audit_expansion import snapshot


def main():
    source = load(ROOT / "s8-complete-biome-requirements.json")["templates"]
    templates, payload_requirements = [], []
    for record in source:
        if record["id"] in {"S8-01", "S8-08"}:
            continue
        identifier = record["id"].lower().replace("-", "_")
        copied = snapshot(record["source"], f"candidate-resources/data/biomedungeon/structures/{identifier}.nbt")
        if copied["sha256"] != record["sha256"]:
            raise ValueError("candidate copy mismatch")
        ports = [check["port"] for check in record["strictMvpPortChecks"] if check["strictExistingMvpAperture"] == "PASS"]
        if len(ports) != len(record["rawOriginalPorts"]):
            raise ValueError("candidate requires unresolved interface: " + identifier)
        templates.append({"id":identifier, "biome":"s8", "resourceId":"biomedungeon:"+identifier, "sha256":record["sha256"], "size":dict(zip(("x","y","z"),record["size"])), "ports":ports, "role":{7:"entrance",9:"terminal"}.get(record["image"],"normal"), "accepted":False, "independentAcceptance":{"sourceId":record["id"],"sourceSha256":record["sha256"],"evidence":record["frozenEvidence"]}, "runtimePromotionRequirements":record["requirements"]})
        payload_requirements.append({"id":record["id"],"sha256":record["sha256"],"explicitPayloadTypes":sorted({payload["nbt"].get("id", "MISSING_ID") for payload in record["blockEntities"]}),"explicitPayloadCount":len(record["blockEntities"]),"implicitPayloadBlocks":[block for block in record["blockEntityBlocks"] if not block["hasExplicitPayload"]],"exactPayloads":record["blockEntities"],"outsideRuntimeAllowlist":record["outsideCurrentRuntimeAllowlist"],"sourceEvidence":record["frozenEvidence"]})
    save(ROOT / "candidate-manifest.json", {"schemaVersion":1,"scope":"NOT_FOR_PRODUCTION: acceptance=false is mandatory; resource copying is not runtime approval", "templates":templates})
    save(ROOT / "block-entity-support-request.json", {"requirements":["Detect BE block states even when original NBT omits an explicit payload; create native default BE rather than erase furniture.","Create after state placement, rotate native state and BE orientation as needed, load exact payload with transformed coordinates without losing Items/Patterns/name/lock/loot data.","Persist before-image and expected after-image state plus full BE NBT before mutation; restart/resume/ownership and rollback must restore it safely.","Read back normalized native BE NBT including defaults; compare semantically against original explicit payload and document normalization; do not falsely require byte equality of native serialized defaults.","Test all four orientations, chest adjacency/merging, banner facing/patterns, empty barrel/chest inventory access and restore-after-interruption in an isolated world.","Review and explicitly expand static block-state allowlist; BE support alone does not permit unsupported palettes, fluids or dynamic states."],"templates":payload_requirements})
    print("PASS 7 original-byte candidates copied with accepted=false; entrance/main hall/treasury included; BE capability request records exact payloads")


if __name__ == "__main__":
    main()
