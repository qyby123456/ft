"""Pair BB video with the actual development client's original sound cue timings."""
from pathlib import Path
import json,wave,subprocess,zipfile
import numpy as np
from PIL import Image,ImageDraw,ImageFont

root=Path(__file__).resolve().parent.parent
out=root/'blockbench/肘部联动与原创音效-v14'
foley=root/'preview/original-foley';rate=44100
cues=[json.loads(l) for l in (root/'run-smoke/sound-cues-SlimSteve29.jsonl').read_text().splitlines()]
for label,start,end,video in [('饮用',205,352,'拔塞-饮用-收起-换瓶-60fps.mp4'),('检视',80,184,'拿出-双手检视-收回-60fps.mp4')]:
    offset=0 if label=='饮用' else 1.45
    length=7.367 if label=='饮用' else 8.683
    mix=np.zeros(round(length*rate))
    for c in cues:
        if not start<=c['tick']<=end:continue
        sound=c['resource'].split(':')[1]
        with wave.open(str(foley/(sound+'.wav')),'rb') as w:
            data=np.frombuffer(w.readframes(w.getnframes()),dtype='<i2').astype(float)/32768
        data=np.interp(np.arange(round(len(data)/c['pitch']))*c['pitch'],np.arange(len(data)),data)*c['volume']
        i=round(((c['tick']-start)/20+offset)*rate)
        n=min(len(data),len(mix)-i)
        if n>0:mix[i:i+n]+=data[:n]
    wavpath=out/'纤细版预览'/(label+'配音.wav')
    with wave.open(str(wavpath),'wb') as w:
        w.setnchannels(1);w.setsampwidth(2);w.setframerate(rate)
        w.writeframes(np.round(np.clip(mix,-1,1)*32767).astype('<i2').tobytes())
    output=out/'纤细版预览'/(label+'预览-含原创音效.mp4')
    subprocess.run([str(root.parents[2]/'ffmpeg/bin/ffmpeg.exe'),'-hide_banner','-loglevel','error','-y',
        '-i',str(out/'纤细版预览'/video),'-i',str(wavpath),'-map','0:v','-map','1:a','-c:v','copy','-c:a','aac',
        '-b:a','128k','-shortest','-movflags','+faststart',str(output)],check=True)
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',17)
sheet=Image.new('RGB',(1440,2*235),(20,24,29));d=ImageDraw.Draw(sheet)
for i,t in enumerate([1,4,7,11,16,19]):
    img=Image.open(root/f'run-smoke/screenshots/SlimSteve29-replacement-continuity-{t:03}.png').convert('RGB')
    img.thumbnail((480,200));x=i%3*480;y=i//3*235
    sheet.paste(img,(x,y+30));d.text((x+10,y+4),f'新瓶握稳至待机 +{t/20:.2f} 秒',font=font,fill='white')
sheet.save(out/'纤细版预览/游戏内换瓶衔接检查.jpg',quality=95)
(out/'使用说明.txt').write_text('''农场物语-橙子果酒 动画工程 v14 / mod 1.3.7

纤细手臂版：3 像素完整手臂，内嵌玩家当前预览皮肤。
Steve 版：4 像素完整手臂。

在 Blockbench 打开对应 bbmodel。插件菜单选择从文件加载 cameras.js，在动画模式使用 camera_first_person 查看第一人称。
预览 animation.orange_wine.preview_complete；完整饮用与换瓶使用 animation.orange_wine.drink。
right_elbow / left_elbow 是整条方块手臂的父支点，手臂保持完整 12 像素，没有分段弯折或非等比拉伸。

检视每转 90° 停顿 0.45 秒，总长 5.2 秒。食用与换瓶 7.35 秒。按游戏同一套轨迹导出稀疏 Bezier 曲线，camera 有独立动画。
游戏正常右键仍为原版使用；V 热键播放自定义饮用，G 检视，键位可在设置更换。

纤细版预览含 60 fps 动画视频、按实际游戏声音触发时间配制的原创音效、游戏换瓶衔接截图。
Blockbench 中酒液使用占位体示意容量；游戏的液面惯性、流动和波纹由代码计算。实际模组读取玩家自己的皮肤，不会强制替换皮肤。
''',encoding='utf-8')
with zipfile.ZipFile(out.parent/'农场物语-橙子果酒-动画工程-v14.zip','w',zipfile.ZIP_DEFLATED) as archive:
    for path in out.rglob('*'):
        if path.is_file() and path.name not in ('idle-start.png','drink-start.png') and path.suffix!='.wav':
            archive.write(path,path.relative_to(out))
print('Packaged both BB profiles, camera plugin, original Foley previews and replacement review.')
