"""Review the runtime-derived tracks through the linked camera, including every second."""
from pathlib import Path
import math,subprocess,sys
import numpy as np
from PIL import Image,ImageDraw,ImageFont
import render_review_v7 as r

out=r.OUT
lengths={a['name'].split('.')[-1]:a['length'] for a in r.model['animations']}
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',17)
r.render('idle',0,'舒适持握 · 独立纤细手臂 · 85% 不透明瓶颈').save(out/'持握预览.png')
for clip,caption in [('inspect','双手检视'),('drink','饮用与换瓶')]:
    times=sorted(set([float(t) for t in range(math.ceil(lengths[clip]))]+[lengths[clip]]+([.8,1.0,1.35,1.5] if clip=='drink' else [.7,1.2,2.6,3.5,4.6])))
    times=[t for t in times if t<=lengths[clip]+1e-7]
    sheet=Image.new('RGB',(1280,math.ceil(len(times)/4)*212),(25,29,32));draw=ImageDraw.Draw(sheet)
    for i,time in enumerate(times):
        frame=r.render(clip,time,'').resize((320,180))
        x=i%4*320;y=i//4*212
        sheet.paste(frame,(x,y+30));draw.text((x+10,y+7),caption+f' {time:.2f}s',font=font,fill='white')
    sheet.save(out/(caption+'-逐秒检查.jpg'),quality=95)
if '--stills-only' not in sys.argv:
    ffmpeg=r.ROOT.parent.parents[2]/'ffmpeg/bin/ffmpeg.exe'
    for clip,title in [('preview_complete','拿出-双手检视-收回'),('drink','拔塞-饮用-收起-换瓶')]:
        command=[str(ffmpeg),'-hide_banner','-loglevel','error','-y','-f','rawvideo','-pix_fmt','rgb24','-s',f'{r.WIDTH}x{r.HEIGHT}','-r','60','-i','-','-an','-c:v','libx264','-crf','18','-pix_fmt','yuv420p','-movflags','+faststart',str(out/(title+'-60fps.mp4'))]
        process=subprocess.Popen(command,stdin=subprocess.PIPE)
        for i in range(math.ceil(lengths[clip]*60)+1):
            t=min(i/60,lengths[clip]);process.stdin.write(r.render(clip,t,title).tobytes())
        process.stdin.close();assert process.wait()==0
print('Reviewed 16 runtime-derived clips; per-second sheets and optional 60 fps videos saved.')
