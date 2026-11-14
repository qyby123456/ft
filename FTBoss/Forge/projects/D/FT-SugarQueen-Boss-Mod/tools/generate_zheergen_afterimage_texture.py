import io
import sys
import zipfile
from pathlib import Path

from PIL import Image


with zipfile.ZipFile(sys.argv[1]) as archive:
    skin = sys.argv[3] if len(sys.argv) > 3 else "zheergeng"
    source = Image.open(io.BytesIO(archive.read("assets/ftboss/textures/entity/easter/" + skin + ".png"))).convert("RGBA")
result = Image.new("RGBA", source.size)
for position_y in range(source.height):
    for position_x in range(source.width):
        red, green, blue, alpha = source.getpixel((position_x, position_y))
        luminance = red * 0.2126 + green * 0.7152 + blue * 0.0722
        brightness = round(150 + luminance * 0.4)
        result.putpixel((position_x, position_y), (brightness, brightness, brightness, alpha))
destination = Path(sys.argv[2])
destination.parent.mkdir(parents=True, exist_ok=True)
result.save(destination)
assert source.getchannel("A").tobytes() == result.getchannel("A").tobytes()
print("PASS generated afterimage mask from FTBoss's own skin; no reference-mod materials:", destination)
