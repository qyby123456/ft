"""Render the delivered bbmodel keyframes with a fixed first-person camera; no game changes."""
from pathlib import Path
import sys,math,json,base64,io,subprocess
import numpy as np
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent/'.preview-deps'))
import moderngl

OUT=ROOT/'双手检视与持握-v5'
model=json.loads((OUT/'农场物语-橙子果酒-双手检视与持握-v5.bbmodel').read_text(encoding='utf-8'))
WIDTH,HEIGHT=1024,576
ctx=moderngl.create_standalone_context()
target=ctx.simple_framebuffer((WIDTH,HEIGHT),components=4,samples=4)
resolve=ctx.simple_framebuffer((WIDTH,HEIGHT),components=4)
program=ctx.program(vertex_shader='''#version 330
in vec3 p;in vec2 uv;in vec3 n;uniform mat4 mvp;uniform mat4 model;out vec2 texcoord;out float shade;
void main(){gl_Position=mvp*vec4(p,1.);texcoord=uv;vec3 norm=normalize(mat3(model)*n);shade=.68+.30*max(0.,dot(norm,normalize(vec3(-.4,1.,.7))));}
''',fragment_shader='''#version 330
uniform sampler2D tex;in vec2 texcoord;in float shade;out vec4 color;
void main(){vec4 c=texture(tex,texcoord);if(c.a<.1)discard;color=vec4(c.rgb*shade,1.);}
''')
background=ctx.program(vertex_shader='''#version 330
in vec2 p;out vec2 v;void main(){v=p;gl_Position=vec4(p,0,1);}
''',fragment_shader='''#version 330
in vec2 v;out vec4 color;void main(){float t=(v.y+1.)*.5;vec3 c=mix(vec3(.075,.095,.11),vec3(.17,.20,.22),t);color=vec4(c,1.);}
''')
bg=ctx.vertex_array(background,[(ctx.buffer(np.array([-1,-1,1,-1,-1,1,1,-1,1,1,-1,1],dtype='f4').tobytes()),'2f','p')])
textures=[]
for t in model['textures']:
    im=Image.open(io.BytesIO(base64.b64decode(t['source'].split(',')[1]))).convert('RGBA')
    tex=ctx.texture(im.size,4,im.tobytes());tex.filter=(moderngl.NEAREST,moderngl.NEAREST);tex.repeat_x=tex.repeat_y=False
    textures.append((tex,im.size))

def translate(v):
    m=np.eye(4);m[:3,3]=v;return m
def rotate(v):
    x,y,z=np.radians(v);cx,sx=math.cos(x),math.sin(x);cy,sy=math.cos(y),math.sin(y);cz,sz=math.cos(z),math.sin(z)
    m=np.eye(4);m[:3,:3]=[[cz*cy,cz*sy*sx-sz*cx,cz*sy*cx+sz*sx],[sz*cy,sz*sy*sx+cz*cx,sz*sy*cx-cz*sx],[-sy,cy*sx,cy*cx]];return m

meshes={};corners={}
for e in model['elements']:
    x0,y0,z0=e['from'];x1,y1,z1=e['to']
    faces={
      'north':[[x0,y1,z0],[x1,y1,z0],[x1,y0,z0],[x0,y0,z0]],
      'south':[[x1,y1,z1],[x0,y1,z1],[x0,y0,z1],[x1,y0,z1]],
      'east':[[x1,y1,z0],[x1,y1,z1],[x1,y0,z1],[x1,y0,z0]],
      'west':[[x0,y1,z1],[x0,y1,z0],[x0,y0,z0],[x0,y0,z1]],
      'up':[[x0,y1,z1],[x1,y1,z1],[x1,y1,z0],[x0,y1,z0]],
      'down':[[x0,y0,z0],[x1,y0,z0],[x1,y0,z1],[x0,y0,z1]]}
    batches={}
    for face,data in e['faces'].items():
        texid=data.get('texture');
        if texid is None:continue
        points=np.array(faces[face],dtype=float);normal=np.cross(points[1]-points[0],points[2]-points[0]);norm=np.linalg.norm(normal)
        if norm<1e-9:continue
        normal/=norm;u0,v0,u1,v1=data['uv'];tw,th=textures[texid][1]
        uv=np.array([[u0,v0],[u1,v0],[u1,v1],[u0,v1]])/[tw,th]
        uv=np.roll(uv,int(data.get('rotation',0)/90),axis=0)
        for i in [0,1,2,0,2,3]:batches.setdefault(texid,[]).append([*points[i],*uv[i],*normal])
    origin=np.array(e.get('origin',[0,0,0]));matrix=translate(origin)@rotate(e.get('rotation',[0,0,0]))@translate(-origin)
    buffers=[]
    for texid,vertices in batches.items():
        vbo=ctx.buffer(np.array(vertices,dtype='f4').tobytes());vao=ctx.vertex_array(program,[(vbo,'3f 2f 3f','p','uv','n')]);buffers.append((texid,vao))
    meshes[e['uuid']]=(matrix,buffers,e['name'])
    corners[e['uuid']]=np.array([[x,y,z,1] for x in [x0,x1] for y in [y0,y1] for z in [z0,z1]])

def channel(animator,ch,t,default):
    keys=animator.get(ch,[])
    if not keys:return np.array(default,dtype=float)
    if t<=keys[0][0]:return keys[0][1]
    for i in range(1,len(keys)):
        if t<=keys[i][0]+1e-8:
            a,v,mode=keys[i-1];b,w,_=keys[i]
            return v if mode=='step' else v+(w-v)*((t-a)/(b-a))
    return keys[-1][1]

animations={}
for anim in model['animations']:
    channels={}
    for identifier,a in anim['animators'].items():
        d={}
        for k in a['keyframes']:
            xyz=np.array([float(k['data_points'][0].get(c,0)) for c in 'xyz'])
            if k['channel'] in ('position','rotation'):xyz[0]*=-1
            if k['channel']=='rotation':xyz[1]*=-1
            d.setdefault(k['channel'],[]).append((k['time'],xyz,k.get('interpolation','linear')))
        for values in d.values():values.sort(key=lambda k:k[0])
        channels[identifier]=d
    animations[anim['name'].split('.')[-1]]=channels

view=translate([0,-16,-16]);near,far=.1,100
f=1/math.tan(math.radians(70)/2)
projection=np.array([[f/(WIDTH/HEIGHT),0,0,0],[0,f,0,0],[0,0,(far+near)/(near-far),2*far*near/(near-far)],[0,0,-1,0]])
vp=projection@view

def instances(clip,t):
    channels=animations[clip]
    def walk(nodes,parent):
        for node in nodes:
            if isinstance(node,str):
                cube,batches,name=meshes[node];yield node,parent@cube,batches,name
            else:
                a=channels.get(node['uuid'],{});o=np.array(node['origin']);p=channel(a,'position',t,[0,0,0])
                r=channel(a,'rotation',t,[0,0,0])+np.array(node.get('rotation',[0,0,0]));s=channel(a,'scale',t,[1,1,1])
                if min(abs(s))<1e-7:continue
                mat=parent@translate(o+p)@rotate(r)@np.diag([*s,1])@translate(-o)
                yield from walk(node.get('children',[]),mat)
    return list(walk(model['outliner'],np.eye(4)))

font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',23)
small=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',15)
def render(clip,t,caption):
    target.use();ctx.disable(moderngl.DEPTH_TEST|moderngl.CULL_FACE)
    target.clear(.1,.12,.14,1,depth=1);bg.render()
    ctx.enable(moderngl.DEPTH_TEST|moderngl.CULL_FACE)
    for _,mat,batches,_ in instances(clip,t):
        program['model'].write(mat.astype('f4').T.tobytes());program['mvp'].write((vp@mat).astype('f4').T.tobytes())
        for texid,vao in batches:textures[texid][0].use();vao.render()
    ctx.copy_framebuffer(resolve,target)
    im=Image.frombytes('RGBA',(WIDTH,HEIGHT),resolve.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB')
    d=ImageDraw.Draw(im);d.text((25,18),caption,font=font,fill=(239,214,165))
    d.text((25,52),'Blockbench 工程关键帧预览 · 60 fps · 固定第一人称视角',font=small,fill=(176,191,199))
    d.line((WIDTH/2-4,HEIGHT/2,WIDTH/2+4,HEIGHT/2),fill=(126,140,147));d.line((WIDTH/2,HEIGHT/2-4,WIDTH/2,HEIGHT/2+4),fill=(126,140,147))
    return im

def caption(t):
    if t<.38:return '空手伸向右侧口袋'
    if t<1.10:return '握住酒瓶 · 从口袋掏出并回稳'
    if t<1.7:return '握在瓶标附近 · 手掌与袖口方向已修正'
    if t<2.34:return '检视 · 拿近看看'
    if t<3.00:return '左手接住瓶身 · 右手移到瓶底支撑'
    if t<3.52:return '左手向右转动瓶身 · 停顿查看'
    if t<6.46:return '右手托底 · 左手分次转瓶、松手换握'
    if t<7.00:return '左手回正瓶身 · 右手重新握回瓶身右后侧'
    if t<7.75:return '左手退出 · 右手回到原持握位置'
    if t<8.20:return '把酒收回右侧口袋'
    return '瓶子已收好 · 空手回到自然位置'

# Check that grasp/release happens while every bottle vertex is below the view even at 90° vertical FOV.
for clip,t in [('draw',.18),('put_away',.45)]:
    for identifier,mat,_,name in instances(clip,t):
        if name.startswith('bottle_original') or name=='original_yellow_stopper':
            p=(view@mat@corners[identifier].T).T
            assert np.all(p[:,1] < -abs(p[:,2])), (clip,t,name,p.tolist())

shots=[(.20,'空手伸向口袋'),(.48,'从口袋掏出'),(1.30,'正常持握'),(2.30,'拉近查看'),
       (2.80,'左手接住、右手移到瓶底'),(3.25,'左手向右转动'),(4.07,'左手带动转瓶'),(4.34,'松手换握、瓶子停住'),
       (6.80,'回正、右手握回瓶身'),(7.50,'左手退出、回原位'),(8.20,'酒瓶已收入口袋'),(8.8,'空手回位')]
sheet=Image.new('RGB',(1280,3*220),(20,24,29));draw=ImageDraw.Draw(sheet)
for i,(t,label) in enumerate(shots):
    im=render('preview_complete',t,'').crop((0,80,WIDTH,HEIGHT)).resize((320,155))
    x=i%4*320;y=i//4*220;sheet.paste(im,(x,y+40));draw.text((x+10,y+10),label,font=small,fill='white')
sheet.save(OUT/'关键姿态预览.jpg')
render('preview_complete',1.3,'修正后的持握').save(OUT/'持握预览.png')
detail=Image.new('RGB',(1536,3*320),(20,24,29));dd=ImageDraw.Draw(detail)
for i,t in enumerate([.98,1.30,1.60,1.93,2.08,2.30,2.52,2.64,2.76]):
    im=render('inspect',t,f'检视 {t:.2f} s').resize((512,288))
    detail.paste(im,(i%3*512,i//3*320))
detail.save(OUT/'双手接触检查.jpg')
if '--stills-only' in sys.argv:sys.exit(0)

ffmpeg=ROOT.parent.parents[2]/'ffmpeg/bin/ffmpeg.exe'
cmd=[str(ffmpeg),'-hide_banner','-loglevel','error','-y','-f','rawvideo','-pix_fmt','rgb24','-s',f'{WIDTH}x{HEIGHT}','-r','60','-i','-',
     '-an','-c:v','libx264','-crf','18','-pix_fmt','yuv420p','-movflags','+faststart',str(OUT/'双手检视与持握-v5.mp4')]
p=subprocess.Popen(cmd,stdin=subprocess.PIPE)
for i in range(540):
    t=i/60;p.stdin.write(render('preview_complete',t,caption(t)).tobytes())
for clip,length,label in [('draw',.9,'拿出慢放 · 空手探入口袋后掏出酒瓶'),('put_away',.82,'收回慢放 · 收好酒瓶后才返回空手')]:
    for i in range(math.ceil(length*120)):
        p.stdin.write(render(clip,i/120,label).tobytes())
p.stdin.close();assert p.wait()==0
print('Rendered delivered bbmodel at 60 fps, with draw/stow half-speed review. Grasp/release off-screen checks passed.')
