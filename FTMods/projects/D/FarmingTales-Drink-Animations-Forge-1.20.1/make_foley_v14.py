"""Original bottle Foley made from filtered noise, pressure pulses and damped resonances."""
from pathlib import Path
import json, subprocess, wave
import numpy as np

ROOT=Path(__file__).resolve().parent
OUT=ROOT/'src/main/resources/assets/farmingtales_drink_anim/sounds'
PREVIEW=ROOT/'preview/original-foley'
PREVIEW.mkdir(exist_ok=True);OUT.mkdir(exist_ok=True)
RATE=44100
rng=np.random.default_rng(930714)
report={};waves={}

def band(n,low,high):
    spectrum=np.fft.rfft(rng.normal(0,1,n));freq=np.fft.rfftfreq(n,1/RATE)
    shape=(1-np.exp(-(freq/max(1,low))**4))*np.exp(-(freq/high)**4)
    signal=np.fft.irfft(spectrum*shape,n=n)
    return signal/(np.std(signal)+1e-9)

def bell(t,start,length):
    x=np.clip((t-start)/length,0,1)
    return np.sin(np.pi*x)**2*((t>=start)&(t<=start+length))

def bubble(t,start,freq,decay,gain):
    dt=np.maximum(0,t-start)
    phase=2*np.pi*freq*(dt+.18*dt*dt)
    return (t>=start)*gain*(1-np.exp(-dt*550))*np.exp(-decay*dt)*(np.sin(phase)+.12*np.sin(phase*2.13))

def save(name,signal):
    signal-=np.mean(signal)
    fade=min(300,len(signal)//4)
    signal[:fade]*=np.linspace(0,1,fade)**2;signal[-fade:]*=np.linspace(1,0,fade)**2
    signal*=.68/max(.001,np.max(np.abs(signal)))
    pcm=np.round(np.clip(signal,-1,1)*32767).astype('<i2')
    path=PREVIEW/(name+'.wav')
    with wave.open(str(path),'wb') as wav:
        wav.setnchannels(1);wav.setsampwidth(2);wav.setframerate(RATE);wav.writeframes(pcm.tobytes())
    subprocess.run([str(ROOT.parents[2]/'ffmpeg/bin/ffmpeg.exe'),'-hide_banner','-loglevel','error','-y','-i',str(path),'-c:a','libvorbis','-q:a','5',str(OUT/(name+'.ogg'))],check=True)
    waves[name]=signal
    report[name]={'seconds':len(signal)/RATE,'peak_dbfs':float(20*np.log10(np.max(np.abs(signal)))),
        'rms_dbfs':float(20*np.log10(np.sqrt(np.mean(signal**2)))),'clipped_samples':int(np.sum(np.abs(pcm)>=32767))}

for name,seconds in [('cloth',.44),('cloth_alt',.39)]:
    t=np.arange(round(seconds*RATE))/RATE
    envelope=bell(t,.02,seconds-.06)*( .62+.23*np.sin(t*71)+.15*np.sin(t*127)**2 )
    save(name,.085*band(len(t),700,4700)*envelope+.025*band(len(t),130,850)*bell(t,.025,.25))

t=np.arange(round(.62*RATE))/RATE
traction=bell(t,.015,.56)*(.55+.25*np.sin(2*np.pi*(14*t+15*t*t))**2)
wood=.10*band(len(t),170,2600)*traction
for onset in [.055,.13,.21,.31,.44]:wood+=bubble(t,onset,rng.uniform(410,690),70,.018)
save('cork_pull',wood)
t=np.arange(round(.26*RATE))/RATE
save('cork',.11*band(len(t),90,1300)*(1-np.exp(-t*2000))*np.exp(-t*62)
     +bubble(t,.003,138,39,.19)+bubble(t,.005,470,70,.045)+.018*band(len(t),1400,5400)*bell(t,.015,.055))

for name,seconds in [('gulp',.69),('gulp_alt',.63)]:
    t=np.arange(round(seconds*RATE))/RATE
    liquid=.044*band(len(t),180,1700)*(bell(t,.02,.27)+.72*bell(t,.24,.33))
    liquid+=.012*band(len(t),1100,4300)*bell(t,.07,.46)
    for start,freq,gain in [(.075,330,.038),(.17,245,.046),(.31,420,.018),(.43,175,.036)]:
        liquid+=bubble(t,start,freq*rng.uniform(.92,1.08),rng.uniform(22,35),gain)
    save(name,liquid)

for name,seconds in [('slosh',.83),('slosh_alt',.75)]:
    t=np.arange(round(seconds*RATE))/RATE
    liquid=.035*band(len(t),220,2100)*(bell(t,.02,.37)+.58*bell(t,.32,.36))
    for onset in [.10,.21,.33,.47,.58]:liquid+=bubble(t,onset,rng.uniform(310,860),rng.uniform(30,48),rng.uniform(.012,.025))
    save(name,liquid)

t=np.arange(round(.40*RATE))/RATE
save('swallow',.035*band(len(t),90,820)*(bell(t,.025,.16)+.40*bell(t,.17,.13))
     +bubble(t,.04,118,28,.065)+bubble(t,.15,190,34,.021))

events={}
for name in ['cloth','cork_pull','cork','gulp','slosh','swallow']:
    variants=[name]+([name+'_alt'] if name+'_alt' in waves else [])
    events[name]={'subtitle':'subtitles.farmingtales_drink_anim.'+name,'sounds':['farmingtales_drink_anim:'+v for v in variants]}
(OUT.parent/'sounds.json').write_text(json.dumps(events,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
report['provenance']='Procedurally synthesized from noise and resonances; no Minecraft, TACZ, video or recorded audio samples.'
(PREVIEW/'audio-checks.json').write_text(json.dumps(report,indent=2),encoding='utf-8')

timeline=[(0,'cloth',.3),(.90,'cork_pull',.38),(1.35,'cork',.65),(2.4,'cloth',.18),(2.6,'gulp',.42),(3.8,'gulp_alt',.34),(4.4,'swallow',.42),(5.3,'cloth',.22),(6.45,'cloth_alt',.22)]
mix=np.zeros(round(8.2*RATE))
for start,name,gain in timeline:
    i=round(start*RATE);mix[i:i+len(waves[name])]+=waves[name]*gain*.55
with wave.open(str(PREVIEW/'完整饮用音效试听.wav'),'wb') as wav:
    wav.setnchannels(1);wav.setsampwidth(2);wav.setframerate(RATE);wav.writeframes(np.round(mix*32767).astype('<i2').tobytes())
print('Generated 9 original mono Foley assets, namespaced sound events, audio checks and a drink preview mix.')
