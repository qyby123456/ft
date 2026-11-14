import hashlib
import json
from pathlib import Path
from PIL import Image
from generate_themes import ASSETS, OUTPUT, ROOT, nine_slice, palettes


def luminance(color):
    values = [(color >> shift & 255) / 255 for shift in (16, 8, 0)]
    linear = [value / 12.92 if value <= 0.04045 else ((value + 0.055) / 1.055) ** 2.4 for value in values]
    return sum(value * weight for value, weight in zip(linear, (0.2126, 0.7152, 0.0722)))


def contrast(first, second):
    light, dark = sorted((luminance(first), luminance(second)), reverse=True)
    return (light + 0.05) / (dark + 0.05)


def main():
    records = []
    crests, frames = set(), set()
    sizes = ((12, 13), (18, 18), (20, 20), (36, 15), (68, 20), (116, 26), (312, 148), (490, 300))
    for identity, palette in palettes():
        directory = ASSETS / identity
        emblem = Image.open(directory / "crest.png").convert("RGBA")
        atlas = Image.open(directory / "panels.png").convert("RGBA")
        assert emblem.size == (32, 32)
        assert atlas.size == (256, 256)
        assert set(emblem.getchannel("A").get_flattened_data()) == {0, 255}
        assert set(atlas.getchannel("A").get_flattened_data()) == {0, 255}
        crests.add(hashlib.sha256(emblem.tobytes()).hexdigest())
        frames.add(hashlib.sha256(atlas.crop((0, 0, 32, 32)).tobytes()).hexdigest())
        assert len(set(atlas.crop((6, 6, 26, 26)).get_flattened_data())) == 1, identity + " noisy frame center"
        assert len(set(atlas.crop((43, 3, 53, 13)).get_flattened_data())) == 1, identity + " noisy card center"
        states = [atlas.crop((0, 40 + state * 20, 64, 60 + state * 20)) for state in range(4)]
        assert len({hashlib.sha256(state.tobytes()).hexdigest() for state in states}) == 4
        assert contrast(palette["text"], palette["card"]) >= 7
        assert contrast(palette["muted"], palette["card"]) >= 4.5
        for size in sizes:
            for state in states:
                image = nine_slice(state, size, 3)
                assert image.size == size
                if min(size) >= 12:
                    assert image.getpixel((size[0] // 2, size[1] // 2))[3] == 255
        records.append({"id": identity, "text_contrast": round(contrast(palette["text"], palette["card"]), 2),
                        "muted_contrast": round(contrast(palette["muted"], palette["card"]), 2), "button_states": 4})
    assert len(records) == len(crests) == len(frames) == 11
    source_directory = ROOT / "src/main/java/net/farmingtales/ftboss/client"
    theme_source = (source_directory / "StatueUiTheme.java").read_text(encoding="utf-8")
    assert "statue.ownTint()" in theme_source
    assert "DoubleBlockHalf.UPPER" in theme_source and "position.below()" in theme_source
    assert "getValue(CandyStatueBlock.WHITE)" not in theme_source and "getValue(CandyStatueBlock.GOLD)" not in theme_source
    files = [*source_directory.glob("Statue*Theme*.java"), *Path(__file__).parent.glob("*.py"), Path(__file__).parent / "ThemeContractCheck.java"]
    for filename in files:
        contents = filename.read_bytes()
        assert not contents.startswith(b"\xef\xbb\xbf"), filename
        assert b"\r" not in contents and b"\t" not in contents, filename
        assert contents.endswith(b"\n") and not contents.endswith(b"\n\n"), filename
        assert all(line == line.rstrip() for line in contents.splitlines()), filename
    report = {"themes": records, "sizes_checked_per_button_state": [list(size) for size in sizes],
              "scope": "asset, geometry and source checks; not an in-game interaction test"}
    (OUTPUT / "asset-checks.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print("PASS: 11 unique crests/frames, 44 button states, 352 resized buttons, quiet interiors, contrast and source format")


if __name__ == "__main__":
    main()
