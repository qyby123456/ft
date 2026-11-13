import hashlib
import json
from pathlib import Path
import sys

from audit_s8_submission import load


def main():
    root = Path(sys.argv[1]).resolve()
    source = load(root / "independent-corridor-qa/route-input.json")[0]
    manifest = load(root / "ready-for-review.json")
    after = root / "qa/world-revision2/merged-after1200.nbt"
    digest = hashlib.sha256(after.read_bytes()).hexdigest()
    assert digest == load(root / "qa/world-revision2/natural-results.json")["merged"]["post_tick_sha256"]
    assert source["sha256"] == manifest["merged"]["sha256"]
    assert len(source["targets"]) == 886
    inputs = [dict(source, candidate=str(after), sha256=digest, group=group, start=interface["replacement"]) for group, interface in zip((501, 502), manifest["retired_interfaces"])]
    destination = root / "independent-posttick-corridors"
    destination.mkdir()
    (destination / "route-input.json").write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding="utf-8")
    (destination / "vanilla-block-tags.json").write_bytes((root / "independent-qa/vanilla-block-tags.json").read_bytes())
    print("Prepared 886 points from each of the two internal endpoints, frozen after1200 hash", digest)


if __name__ == "__main__":
    main()
