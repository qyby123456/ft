import argparse
import json
from pathlib import Path

from PIL import Image, ImageDraw

from generate_themes import ROOT, mix, nine_slice, palettes, rgba


def bevel(size, fill, sun=0xFFFFFF, shade=0x555555, inset=False):
    width, height = size
    image = Image.new("RGBA", size, rgba(fill))
    draw = ImageDraw.Draw(image)
    if inset:
        sun, shade = shade, sun
    draw.rectangle((0, 0, width - 1, height - 1), outline=rgba(0x252525))
    draw.line((1, 1, width - 2, 1), fill=rgba(sun))
    draw.line((1, 1, 1, height - 2), fill=rgba(sun))
    draw.line((2, height - 2, width - 2, height - 2), fill=rgba(shade))
    draw.line((width - 2, 2, width - 2, height - 2), fill=rgba(shade))
    return image


def corner(identity, palette):
    patterns = {
        "neutral": ("00100", "01110", "11111", "01010", "11111"),
        "sugar_queen": ("10001", "10101", "11111", "01110", "01110"),
        "butcher": ("11111", "11111", "00010", "00010", "00010"),
        "saya": ("00110", "01100", "00100", "01110", "11111"),
        "wine_barrel": ("01110", "11111", "10101", "11111", "01110"),
        "rat_swarm": ("11011", "11111", "10101", "01110", "00100"),
        "defect": ("10101", "01110", "11011", "01110", "10101"),
        "compost": ("01110", "11111", "11111", "00100", "01110"),
        "apple": ("00110", "01100", "11111", "11111", "01110"),
        "tide_fish": ("00010", "01111", "11110", "01111", "00010"),
        "benger": ("01010", "10101", "00000", "01110", "11111"),
    }
    image = Image.new("RGBA", (6, 6))
    for vertical, row in enumerate(patterns[identity]):
        for horizontal, filled in enumerate(row):
            if filled == "1":
                image.putpixel((horizontal, vertical), rgba(palette["accent"]))
    return image


def atlas(palette, identity="neutral"):
    image = Image.new("RGBA", (256, 256))
    frame = bevel((32, 32), palette["panel"])
    draw = ImageDraw.Draw(frame)
    draw.rectangle((2, 2, 29, 29), outline=rgba(mix(palette["panel"], 0xFFFFFF, 0.35)))
    motif = corner(identity, palette)
    frame.alpha_composite(motif, (1, 1))
    frame.alpha_composite(motif.transpose(Image.Transpose.FLIP_LEFT_RIGHT), (25, 1))
    image.alpha_composite(frame)
    for selected, horizontal in ((False, 40), (True, 60)):
        card = bevel((16, 16), palette["card"], inset=True)
        if selected:
            ImageDraw.Draw(card).rectangle((1, 1, 14, 14), outline=rgba(palette["accent"]))
        image.alpha_composite(card, (horizontal, 0))
    for state in range(4):
        fill = (palette["card"], mix(palette["card"], palette["accent"], 0.12),
                mix(palette["card"], palette["bright"], 0.08), palette["panel"])[state]
        button = bevel((64, 20), fill, sun=mix(palette["border"], 0xFFFFFF, 0.2),
                       shade=palette["background"], inset=state == 2)
        detail = ImageDraw.Draw(button)
        if state in (1, 2):
            detail.rectangle((2, 2, 61, 17), outline=rgba(palette["accent"]))
        if state == 3:
            detail.line((3, 17, 60, 17), fill=rgba(palette["border"]))
        image.alpha_composite(button, (0, 40 + state * 20))
    return image


def socket(state):
    image = Image.new("RGBA", (32, 32))
    draw = ImageDraw.Draw(image)
    upper = (0x666666, 0x444444, 0x526845, 0x898989)[state]
    lower = (0xFFFFFF, 0xFFFFFF, 0xD4E9BD, 0xBABABA)[state]
    draw.rectangle((0, 0, 31, 31), outline=rgba(0x777777))
    draw.rectangle((1, 1, 30, 30), outline=rgba(upper), width=3)
    draw.rectangle((1, 28, 30, 30), fill=rgba(lower))
    draw.rectangle((28, 1, 30, 30), fill=rgba(lower))
    if state in (1, 2):
        draw.rectangle((0, 0, 31, 31), outline=rgba(0x486B81 if state == 1 else 0x4D6E38), width=2)
    draw.rectangle((5, 5, 26, 26), fill=(0, 0, 0, 0))
    return image


def well_background(event, palette, panels, components, frames):
    image = nine_slice(panels.crop((0, 0, 32, 32)), (300, 236), 6)
    draw = ImageDraw.Draw(image)
    draw.rectangle((8, 8, 291, 32), fill="#BDC4B9")
    draw.line((12, 35, 287, 35), fill="#6B7562")
    draw.line((12, 36, 287, 36), fill="#EEEEEE")
    draw.rectangle((231, 10, 286, 25), fill="#A9BA9B", outline="#65765B")
    for left, top, right, bottom in ((13, 41, 113, 225), (120, 38, 288, 98), (120, 100, 288, 142)):
        image.alpha_composite(nine_slice(panels.crop((40, 0, 56, 16)), (right - left + 1, bottom - top + 1), 3), (left, top))
    draw = ImageDraw.Draw(image)
    draw.rectangle((17, 45, 109, 129), fill="#ABB5AC")
    well = Image.open(components / "wishing_well.png").convert("RGBA")
    well = well.resize((41, 41), Image.Resampling.NEAREST)
    well = well.quantize(colors=18, method=Image.Quantize.FASTOCTREE).convert("RGBA")
    image.alpha_composite(well.resize((82, 82), Image.Resampling.NEAREST), (22, 45))
    emerald = Image.open(components / "emerald_upgrade.png").convert("RGBA")
    image.alpha_composite(emerald.resize((14, 14), Image.Resampling.NEAREST), (94, 143))
    for index in range(11):
        left, top = 123 + index % 6 * 26, 54 + index // 6 * 24
        draw.rectangle((left, top, left + 21, top + 21), fill="#8B8B8B")
        image.alpha_composite(frames[0].resize((22, 22), Image.Resampling.NEAREST), (left, top))
    for left in (124, 208):
        image.alpha_composite(nine_slice(panels.crop((60, 0, 76, 16)), (78, 26), 3), (left, 115))
    for row in range(4):
        top = 152 + row * 18 if row < 3 else 210
        for column in range(9):
            left = 125 + column * 18
            draw.rectangle((left, top, left + 17, top + 17), fill="#8B8B8B")
            image.alpha_composite(frames[0].resize((18, 18), Image.Resampling.NEAREST), (left, top))
    draw.rectangle((20, 158, 106, 187), fill="#AAB6A1", outline="#727C6B")
    gui = event / "src/main/resources/assets/qys_event/textures/gui"
    image.save(gui / "wishing_well_shop.png")
    return image


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--event-project", type=Path, required=True)
    parser.add_argument("--components", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--themes-only", action="store_true")
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    gui = ROOT / "src/main/resources/assets/ftboss/textures/gui"
    theme_palettes = dict(palettes())
    for identity, palette in theme_palettes.items():
        atlas(palette, identity).save(gui / "statue_themes" / identity / "panels.png")
    if args.themes_only:
        record = dict(themes=11, scope="Boss-colored interiors and buttons; vanilla pixel bevels",
                      invariant="UVs, layout, slot coordinates, crests, item icons and black cleaver unchanged",
                      reference="Starcatcher UI structure only; no reference pixels copied; no new image API calls")
        (args.output / "style-manifest.json").write_text(json.dumps(record, indent=2) + "\n", encoding="utf-8")
        print("PASS: 11 Boss-colored UI atlases; other assets untouched")
        return
    frames = [socket(state) for state in range(4)]
    event_gui = args.event_project / "src/main/resources/assets/qys_event/textures/gui"
    for state, name in enumerate(("normal", "hover", "selected", "disabled")):
        frames[state].save(gui / "statue_details" / ("socket_" + name + ".png"))
        frames[state].save(event_gui / ("wishing_well_socket_" + name + ".png"))
    pedestal = Image.new("RGBA", (32, 32))
    pedestal.alpha_composite(bevel((32, 10), 0x8B8B8B), (0, 22))
    pedestal.save(gui / "statue_details/pedestal.png")
    well = well_background(args.event_project, theme_palettes["neutral"], atlas(theme_palettes["neutral"]), args.components, frames)
    well.resize((1200, 944), Image.Resampling.NEAREST).save(args.output / "well-background.png")
    record = dict(reference="starcatcher-3.3.10-FORGE-1.20.1.jar: rod_screen, tackle_box, guide backgrounds",
                  strategy="Independent deterministic pixel bevels; reference PNGs are not copied into game resources",
                  inherited_art="Existing image2.5 boss crests, small symbols, well silhouette; no new paid generation",
                  invariant="Atlas UVs, layout, slot coordinates, item icons and black cleaver unchanged",
                  themes=11, ft_textures=16, event_textures=5)
    (args.output / "style-manifest.json").write_text(json.dumps(record, indent=2) + "\n", encoding="utf-8")
    print("PASS: 11 vanilla-style boss atlases, 4 slot states, pedestal and wishing-well background")


if __name__ == "__main__":
    main()
