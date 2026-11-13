import pathlib
from PIL import Image, ImageDraw

ROOT=pathlib.Path(__file__).parent
folders=sorted(ROOT.glob('[12][0-9]-*'))
for view in ['complete','cutaway','function-detail']:
    canvas=Image.new('RGB',(1500,1700),'#182028')
    draw=ImageDraw.Draw(canvas)
    for index,folder in enumerate(folders):
        path=folder/(view+'.png')
        if not path.exists():
            continue
        image=Image.open(path).convert('RGB')
        image.thumbnail((500,310))
        east=(index%3)*500
        south=(index//3)*410
        canvas.paste(image,(east,south+30))
        draw.text((east+12,south+10),folder.name+' / '+view,fill='white')
    canvas.save(ROOT/(view+'-contact.jpg'),quality=94)
