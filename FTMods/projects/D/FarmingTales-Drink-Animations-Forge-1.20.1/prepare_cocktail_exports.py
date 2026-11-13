"""Reuse the complete export pipeline, scoped to the new cocktail profiles."""
from pathlib import Path
ROOT=Path(__file__).absolute().parent
source=(ROOT/'blockbench/04-导出工具/旧版脚本/export_drinks_v52.py').read_text(encoding='utf-8')
source=source.replace('v52','v56').replace('1.8.23','1.8.27').replace('酒水扩展-v56','01-常用工程/鸡尾酒动画-v56')
source=source.replace("if profile['style']=='JAM':continue", "if profile['id'].startswith('farmingtales:'):continue")
source=source.replace('def tex(location,opacity=255):','def tex(location,opacity=255,tint=0xFFFFFF):')
source=source.replace('key=location+str(opacity)','key=location+str(opacity)+str(tint)')
source=source.replace("            if opacity<255:", '''            meta_path=Path(str(path)+'.mcmeta')
            if meta_path.exists():
                meta=json.loads(meta_path.read_text())['animation']
                frame_height=meta.get('height',im.width)
                im=im.crop((0,0,im.width,frame_height))
            im=im.convert('RGBA')
            if tint!=0xFFFFFF:
                channels=list(im.split())
                for channel,shift in enumerate([16,8,0]):
                    gain=(tint>>shift)&255
                    channels[channel]=channels[channel].point(lambda v:round(v*gain/255))
                im=Image.merge('RGBA',channels)
            encoded=io.BytesIO();im.save(encoded,format='PNG');raw=encoded.getvalue()
            if opacity<255:''')
source=source.replace("            r=e.get('rotation',{});rotation=[0,0,0]",'''            e=copy.deepcopy(e)
            r=e.get('rotation',{});rotation=[0,0,0]
            if r.get('rescale'):
                origin=r.get('origin',[0,0,0]);axis='xyz'.index(r['axis'])
                for field in ['from','to']:
                    e[field]=[origin[j]+(e[field][j]-origin[j])/(math.cos(math.radians(r['angle'])) if j!=axis else 1) for j in range(3)]''')
source=source.replace("index=tex(texture_map[value['texture'][1:]],opacity)","index=tex(texture_map[value['texture'][1:]],opacity,0x5555FF if item=='signature_cocktail' and value.get('tintindex')==1 else 0xFFFFFF)")
start=source.index('        liquids=[]')
end=source.index('        skin=',start)
source=source[:start]+'''        liquids=[];liquid_cells=[]
        for i,r in enumerate(profile['liquid']):
            location=r['texture'].replace(':textures/',':')[:-4]
            for x in range(6):
                for z in range(6):
                    name=f'liquid_{i}_{x}_{z}'
                    a=(np.array(r['min'])*16+[8,0,8]);b=(np.array(r['max'])*16+[8,0,8])
                    lo=a.copy();hi=b.copy()
                    for axis,n in [(0,x),(2,z)]:
                        lo[axis]=a[axis]+(b[axis]-a[axis])*n/6
                        hi[axis]=a[axis]+(b[axis]-a[axis])*(n+1)/6
                    center=(lo+hi)*.5
                    g=group(name,BASE+np.array([center[0]-8,lo[1]-4.8,center[2]-8])*scale)
                    liquids.append(g);children[name]=[]
                    faces={f:dict(uv=r['surfaceUV'] if f in ['up','down'] else r['sideUV'],texture='#0',tintindex=1 if item=='signature_cocktail' else -1) for f in ['north','east','south','west','up','down']}
                    cube({'from':lo.tolist(),'to':hi.tolist(),'faces':faces},name+'_volume',name,{'0':location},opacity=184)
                    liquid_cells.append((g,i,(center[0]-8)/16,(center[2]-8)/16))
''' +source[end:]
source=source.replace("            for i,g in enumerate(liquids):add(g,'scale',[[1,f['liquidFractions'][i],1] for f in frames],.015)",'''            for g,i,x,z in liquid_cells:
                r=profile['liquid'][i];heights=[]
                for f in frames:
                    nx,ny,nz=f['liquidNormal']
                    y=(f['liquidLevel']-nx*x-nz*z)/max(.001,ny)
                    heights.append([1,max(0,min(1,(y-r['min'][1])/(r['max'][1]-r['min'][1]))),1])
                add(g,'scale',heights,.015)''')
source=source.replace("Original FarmingTales container assets; 紫菜 / ARR / FarmingTales 1.8.27", "Original cocktail assets; 紫菜 / ARR / FarmingTales 1.8.27")
source += '''
import zipfile
with zipfile.ZipFile(OUT.with_suffix('.zip'),'w',zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(OUT.iterdir()):
        if path.is_file():archive.write(path,path.name)
'''
(ROOT/'blockbench/04-导出工具/export_cocktails_v56.py').write_text(source,encoding='utf-8')
(ROOT/'blockbench/06-使用说明/使用说明-v56.txt').write_text('''鸡尾酒动画 v56 / MOD 1.8.27
作者：紫菜；协议：ARR。
26 款鸡尾酒，Steve 和纤细手臂各一份，共 52 个项目。
取出 0.5 秒，放回 0.45 秒。包含红酒式摇杯检视、饮用及四种变化、呼吸、行走、奔跑、跳跃、放置和第一人称 camera。
叶片、果切、吸管和冰块使用与游戏相同的惯性轨迹，液面以 6×6 小柱近似裁切表面。
保留原模组材质。招牌鸡尾酒在本文件中预览默认蓝色；游戏实时读取每杯配方颜色。
Blockbench 显示原贴图的首帧；神秘鸡尾酒等逐帧变色和闪动在游戏中完整保留。
从文件安装同目录 cameras.js，选择并关联 camera_first_person。
其余物品继续使用已有 v54 和巧克力酱 v55 项目。V 食用、G 检视，可改键；普通右键保持原版。
''',encoding='utf-8')
print('Prepared cocktail-only Blockbench v56 exporter.')
