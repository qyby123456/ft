"""Read every animation and its bone channels in the local TACZ reference, without executing scripts."""
from pathlib import Path
import collections
import hashlib
import json
import zipfile

ROOT=Path(__file__).resolve().parent
SOURCE=Path('C:/Users/YOUR_USERNAME/Downloads/tacz-1.20.1-1.1.8-hotfix.jar')
OUT=ROOT/'blockbench/03-参考素材/TACZ-1.1.8完整动画研究'
OUT.mkdir(parents=True,exist_ok=True)


def vector(value):
    if isinstance(value,dict):value=value.get('post',value.get('pre'))
    if isinstance(value,(float,int)):return [value]*3
    if isinstance(value,list) and all(isinstance(x,(float,int)) for x in value):return value


def describe(track):
    if not isinstance(track,dict):return dict(constant=track)
    try:[float(t) for t in track]
    except ValueError:return dict(metadata=track)
    frames=sorted(((float(t),v) for t,v in track.items()),key=lambda a:a[0])
    result=dict(keyframes=len(frames),times=[t for t,v in frames],
        interpolation=dict(collections.Counter(v.get('lerp_mode','linear') if isinstance(v,dict) else 'linear' for t,v in frames)))
    result['steps']=[t for t,v in frames if isinstance(v,dict) and 'pre' in v and 'post' in v and v['pre']!=v['post']]
    numeric=[(t,vector(v)) for t,v in frames];numeric=[(t,v) for t,v in numeric if v is not None]
    if numeric:
        result['range']=[[min(v[i] for t,v in numeric),max(v[i] for t,v in numeric)] for i in range(3)]
        intervals=[];holds=[]
        for (a,x),(b,y) in zip(numeric,numeric[1:]):
            if b==a:continue
            speed=sum((j-i)**2 for i,j in zip(x,y))**.5/(b-a)
            if speed>1e-6:intervals.append((speed,a,b))
            elif b-a>=.15:holds.append([a,b])
        result['fastest_intervals']=[dict(start=a,end=b,speed=v) for v,a,b in sorted(intervals,reverse=True)[:3]]
        result['holds']=holds
    return result


files=[];models=[];scripts=[]
with zipfile.ZipFile(SOURCE) as z:
    for name in z.namelist():
        if '/animations/' in name and name.endswith('.json'):
            data=z.read(name);value=json.loads(data)
            folder='default' if '/custom/' not in name else 'guns'
            dest=OUT/'原始动画'/folder/Path(name).name;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(data)
            entry=dict(entry=name,sha256=hashlib.sha256(data).hexdigest(),file=str(dest.relative_to(OUT)),clips=[])
            for clip,animation in value['animations'].items():
                channels={bone:{channel:describe(track) for channel,track in tracks.items()}
                          for bone,tracks in animation.get('bones',{}).items()}
                entry['clips'].append(dict(name=clip,seconds=animation.get('animation_length'),loop=animation.get('loop'),
                    bones=channels,sound_effects=animation.get('sound_effects'),particle_effects=animation.get('particle_effects')))
            files.append(entry)
        elif '/geo_models/' in name and name.endswith('.json'):
            value=json.loads(z.read(name));geometries=value.get('minecraft:geometry',[])
            bones=[dict(name=b['name'],parent=b.get('parent'),pivot=b.get('pivot'),rotation=b.get('rotation'))
                   for g in geometries for b in g.get('bones',[])]
            models.append(dict(entry=name,bones=bones))
        elif '/scripts/' in name and name.endswith('.lua'):
            text=z.read(name).decode('utf-8-sig')
            dest=OUT/'状态机参考'/Path(name).name;dest.parent.mkdir(parents=True,exist_ok=True)
            # Identically named state machines in different namespaces are preserved separately.
            if dest.exists() and dest.read_text(encoding='utf-8')!=text:dest=dest.with_stem(dest.stem+'-'+hashlib.sha256(text.encode()).hexdigest()[:8])
            dest.write_text(text,encoding='utf-8');scripts.append(dict(entry=name,file=str(dest.relative_to(OUT))))
report=dict(source=str(SOURCE),sha256=hashlib.sha256(SOURCE.read_bytes()).hexdigest(),animation_files=len(files),
            clips=sum(len(f['clips']) for f in files),files=files,models=models,scripts=scripts)
(OUT/'全部轨道分析.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Files:',len(files),'clips:',report['clips'],'models:',len(models),'scripts:',len(scripts))
for f in files:
    relevant=[c for c in f['clips'] if c['name'] in ('draw','put_away','inspect','inspect_empty','reload_empty','reload_tactical')]
    print(Path(f['entry']).stem, 'clips='+str(len(f['clips'])), '; '.join(c['name']+'='+str(c['seconds']) for c in relevant))
