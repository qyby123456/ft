import argparse
import hashlib
import json
from pathlib import Path

from audit_s8_submission import load


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    manifest = load(root / "ready-for-review.json")
    original = load(root / "independent-qa/route-input.json")
    inputs = []
    for room, route in zip(manifest["rooms"], original[:-1]):
        assert route["sha256"] == room["sha256"]
        source = root / "maker-world-evidence" / room["id"] / "after-800-ticks.nbt"
        def shifted(point):
            return [point[0] + 2, point[1], point[2] + 2]
        inputs.append(dict(route, candidate=str(source), sha256=hashlib.sha256(source.read_bytes()).hexdigest(), start=shifted(route["start"]), targets=[dict(target, position=shifted(target["position"])) for target in route["targets"]]))
    assert len(inputs) == 10
    destination = root / "independent-after-world-qa"
    destination.mkdir()
    (destination / "route-input.json").write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding="utf-8")
    (destination / "vanilla-block-tags.json").write_bytes((root / "independent-qa/vanilla-block-tags.json").read_bytes())
    print("Prepared ten captured world-result route inputs with x/z +2 capture-origin correction")


if __name__ == "__main__":
    main()
