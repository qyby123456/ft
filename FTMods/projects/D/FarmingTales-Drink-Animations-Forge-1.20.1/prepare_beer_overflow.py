"""Paint three held mugs with Barley's source overflow silhouette and their own foam colors."""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent
TEXTURES = ROOT / 'src/main/resources/assets/farmingtales_drink_anim/textures/letsdo/brewery'
# Original Barley pixels: cup-front dribble plus its continuation over the two rim UV strips.
PIXELS = ((9,0),(10,0),(9,1),(10,1),(10,2),(5,2),(5,3),(3,5),(4,5))
BARLEY = ((255,241,212),(255,225,165),(255,216,139),(255,205,104),(255,181,32))
# All colors are sampled from each beer's existing top foam. No glass/wood colors are reused.
PALETTES = {
    'beer_wheat': ((254,255,226),(255,227,134),(255,212,75),(255,212,75),(255,188,53)),
    'beer_nettle': ((255,255,255),(246,249,240),(246,248,239),(241,245,231),(240,244,229)),
    'beer_haley': ((194,76,84),(164,56,63),(156,53,60),(144,49,56),(127,43,49)),
}

def paint():
    reference = Image.open(TEXTURES / 'beer_barley_1.png').convert('RGBA')
    sheet = Image.new('RGB',(900,560),'#272a30')
    draw = ImageDraw.Draw(sheet)
    for i,(name,palette) in enumerate(PALETTES.items()):
        # particle is the untouched original atlas; rebuilding stays deterministic.
        original = Image.open(TEXTURES / (name + '_particle.png')).convert('RGBA')
        foam = Image.open(TEXTURES / (name + '_foam.png')).convert('RGBA')
        foam_colors = {foam.getpixel((x,y))[:3] for y in range(1,5) for x in range(1,5)}
        assert set(palette) <= foam_colors
        result = original.copy()
        mapping = dict(zip(BARLEY,palette))
        for point in PIXELS:
            result.putpixel(point,mapping[reference.getpixel(point)[:3]]+(255,))
        result.save(TEXTURES / (name + '_1.png'))
        draw.text((i*300+12,12),name+' / top foam + rim + front',fill='white')
        for row,(label,tile) in enumerate((('foam',foam.crop((1,1,5,5))),
                                          ('before',original.crop((6,0,12,8))),
                                          ('after',result.crop((6,0,12,8))))):
            y=40+row*170;draw.text((i*300+12,y),label,fill='white')
            scaled=tile.resize((tile.width*18,tile.height*18),Image.Resampling.NEAREST)
            sheet.paste(scaled,(i*300+90,y),scaled)
    sheet.save(ROOT/'preview/beer-overflow-after.png')
    print('Painted Wheat, Nettle and Haley held mugs: nine original-resolution overflow pixels each.')

if __name__=='__main__':
    paint()
