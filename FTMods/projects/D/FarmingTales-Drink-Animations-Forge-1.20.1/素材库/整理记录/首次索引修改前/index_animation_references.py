"""Build a reusable one-frame-per-second local reference library; no model training."""
from pathlib import Path
import hashlib, json, math, subprocess, zipfile
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).absolute().parent
OUT=ROOT/'references/2026-09-13';OUT.mkdir(parents=True,exist_ok=True)
FFMPEG='Z:/ffmpeg/bin/ffmpeg.exe';FFPROBE='Z:/ffmpeg/bin/ffprobe.exe'
font=ImageFont.truetype('C:/Windows/Fonts/consola.ttf',16)
records=[]
for name in ['mmexport1789280254830','mmexport1789280220509','mmexport1789280346760']:
    path=Path('E:/zhuomian')/(name+'.mp4')
    meta=json.loads(subprocess.check_output([FFPROBE,'-v','error','-show_entries','format=duration:stream=width,height','-of','json',str(path)]))
    folder=OUT/name;folder.mkdir(exist_ok=True);frames=folder/'seconds';frames.mkdir(exist_ok=True)
    subprocess.run([FFMPEG,'-y','-v','error','-i',str(path),'-vf','fps=1,scale=320:-2','-q:v','3',str(frames/'%04d.jpg')],check=True)
    images=sorted(frames.glob('*.jpg'));pages=[]
    for start in range(0,len(images),36):
        sheet=Image.new('RGB',(1920,1164),(21,24,29));draw=ImageDraw.Draw(sheet)
        for j,p in enumerate(images[start:start+36]):
            x=j%6*320;y=j//6*194;im=Image.open(p);sheet.paste(im,(x,y+22))
            second=start+j;draw.text((x+6,y+3),f'{second//60:02}:{second%60:02} / second {second}',font=font,fill='white')
        page=folder/f'seconds-{start:04d}-{min(start+35,len(images)-1):04d}.jpg';sheet.save(page,quality=88);pages.append(str(page.relative_to(OUT)))
    records.append(dict(file=str(path),sha256=hashlib.sha256(path.read_bytes()).hexdigest(),duration_seconds=float(meta['format']['duration']),frames=len(images),sampling='1 fps',pages=pages))
    print(name,len(images),'one-second frames',flush=True)

jar=Path('C:/Users/YOUR_USERNAME/Downloads/tacz-1.20.1-1.1.8-hotfix.jar');tracks=[]
with zipfile.ZipFile(jar) as z:
    for entry in ['assets/tacz/animations/pistol_default.animation.json','assets/tacz/custom/tacz_default_gun/assets/tacz/animations/glock_17.animation.json','assets/tacz/custom/tacz_default_gun/assets/tacz/animations/ak47.animation.json']:
        model=json.loads(z.read(entry))
        for key,animation in model.get('animations',{}).items():
            if any(n in key for n in ['draw','put_away','inspect','walk','run','sprint']):
                bones=animation.get('bones',{});channels={}
                for bone,spec in bones.items():
                    channels[bone]={channel:list(value) if isinstance(value,dict) else 'constant' for channel,value in spec.items()}
                tracks.append(dict(entry=entry,name=key,length=animation.get('animation_length'),loop=animation.get('loop'),bones=channels))
data=dict(purpose='Reference indexing and timing study, not model training or weight updates',videos=records,tacz=dict(file=str(jar),sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),tracks=tracks))
(OUT/'index.json').write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
lines=['# 动画素材逐秒索引','','本目录保存视频的逐秒缩略图与 TACZ 轨道结构索引，用于后续动作设计。没有训练模型或修改任何 AI 权重。','']
for r in records:
    name=Path(r['file']).stem
    lines += [f'## {name}',f"时长 {r['duration_seconds']:.2f} 秒；共 {r['frames']} 张逐秒缩略图。原视频：{r['file']}",'']
    lines += [f'![{Path(page).stem}]({page})' for page in r['pages']]
(OUT/'逐秒索引.md').write_text('\n'.join(lines),encoding='utf-8')
print('Reference index complete',flush=True)
