"""Match existing physical foley to the unchanged drinking sample; leave fabric quiet."""
from pathlib import Path
import hashlib, json, subprocess
import numpy as np

ROOT=Path(__file__).absolute().parent
ASSETS=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
SOUNDS=ASSETS/'sounds'
FF=ROOT.parents[2]/'ffmpeg/bin/ffmpeg.exe'
OUT=SOUNDS/'balanced';OUT.mkdir(exist_ok=True)

def measure(path):
    metadata=json.loads(subprocess.check_output([str(FF.parent/'ffprobe.exe'),'-v','error','-show_entries','stream=duration','-of','json',str(path)]))
    seconds=float(metadata['streams'][0]['duration'])
    raw=subprocess.check_output([str(FF),'-v','error','-i',str(path),'-f','f32le','-ac','1','-ar','48000','-'])
    x=np.frombuffer(raw,dtype='<f4')[:round(seconds*48000)]
    windows=np.array([np.sqrt(np.mean(x[i:i+2400]**2)) for i in range(0,len(x),2400)])
    active=windows[windows>=windows.max()*.25]
    return dict(active_db=float(20*np.log10(np.sqrt(np.mean(active**2)))),
                peak_db=float(20*np.log10(np.max(np.abs(x)))),seconds=seconds)

def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()

if __name__=='__main__':
    reference=measure(SOUNDS/'vanilla/drink.ogg');target=reference['active_db'];report=[]
    for name,source in {
        'liquid1':'vanilla/liquid1.ogg','liquid2':'vanilla/liquid2.ogg',
        'cork':'cork.ogg','cork_pull':'cork_pull.ogg','glass_touch':'inspection/glass_touch.ogg',
        'cap_twist':'vanilla/leather2.ogg','can_open':'container/can_open.ogg',
    }.items():
        src=SOUNDS/source;dst=OUT/(name+'.ogg');before=measure(src);gain=target-before['active_db']
        for attempt in range(5):
            subprocess.run([str(FF),'-v','error','-y','-i',str(src),'-af',
                f'aresample=48000,asetpts=PTS-STARTPTS,volume={gain:.6f}dB,apad=pad_dur=0.01,alimiter=limit=0.80:attack=1:release=30:level=false,atrim=start=0.001:duration={before["seconds"]:.8f},asetpts=PTS-STARTPTS',
                '-ar','48000','-c:a','libvorbis','-q:a','5',str(dst)],check=True)
            after=measure(dst);difference=target-after['active_db']
            if abs(difference)<.20:break
            gain+=difference
        assert abs(target-after['active_db'])<.25,(name,after,target)
        assert after['peak_db']<-.4,(name,'encoded peak',after)
        assert abs(after['seconds']-before['seconds'])<.0001,(name,'duration changed')
        report.append(dict(name=name,source=source,source_sha256=sha(src),output=str(dst),sha256=sha(dst),
                           before=before,after=after,gain_db=gain))
        print(name,round(before['active_db'],2),'->',round(after['active_db'],2),'dB; peak',round(after['peak_db'],2))
    (ROOT/'preview/audio-balance-1.7.1.json').write_text(json.dumps(dict(reference=reference,samples=report),indent=2)+'\n',encoding='utf-8')
