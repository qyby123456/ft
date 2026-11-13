from pathlib import Path
import base64,io,json,math,sys,uuid,zipfile
import numpy as np
from PIL import Image,ImageDraw,ImageFont

ROOT=Path(__file__).resolve().parent
OUT=ROOT/'果酱小木勺-v1';OUT.mkdir(exist_ok=True)
uid=lambda name:str(uuid.uuid5(uuid.NAMESPACE_URL,'farmingtales/jam-spoon-v1/'+name))

# A hand-painted 32px atlas. Grain runs down the handle; the recess has carved rings.
palette={'base':(154,104,57),'light':(174,124,72),'dark':(121,77,40),'edge':(189,140,85)}
tex=Image.new('RGBA',(32,32),palette['base']+(255,));d=ImageDraw.Draw(tex)
for x in range(16):
    color=palette['light'] if x in (3,7,11) else palette['dark'] if x in (1,5,9,14) else palette['base']
    d.line((x,0,x,31),fill=color,width=1)
for coords in [(5,14,5,19),(6,19,6,25),(9,5,9,10),(10,10,10,15),(2,25,2,30)]:d.line(coords,fill=(138,88,44),width=1)
d.line((6,27,7,29),fill=(193,141,80));d.line((7,29,7,31),fill=(119,75,37))
d.rectangle((16,0,31,15),fill=(130,82,41))
for y in range(16):
    for x in range(16,32):
        radius=math.sqrt(((x-23.5)/6.4)**2+((y-7.5)/7.3)**2)
        shade=(150,96,49) if radius<.80 else (120,74,37)
        if .36<radius<.49:shade=(170,113,59)
        if .68<radius<.78:shade=(111,67,32)
        if .82<radius<.98:shade=(187,130,71)
        tex.putpixel((x,y),shade+(255,))
for y in range(16,32):
    for x in range(16,32):
        color=(131,81,40) if (x+2*y)%7==0 else (154,99,50) if x%3 else (174,117,61)
        tex.putpixel((x,y),color+(255,))
tex.save(OUT/'wooden_spoon.png')

mask=np.zeros((12,12),dtype=bool)
for row in range(12):
    a,b=(4,8) if row in (0,11) else (2,10) if row in (1,10) else (1,11)
    mask[row,a:b]=True
rim=mask.copy()
for z,x in np.argwhere(mask):
    if z>0 and z<11 and x>0 and x<11 and all(mask[z+dz,x+dx] for dz,dx in [(0,1),(0,-1),(1,0),(-1,0)]):rim[z,x]=False
def rectangles(bitmap):
    remaining=bitmap.copy();result=[]
    while remaining.any():
        z,x=np.argwhere(remaining)[0];w=1
        while x+w<12 and remaining[z,x+w]:w+=1
        h=1
        while z+h<12 and remaining[z+h,x:x+w].all():h+=1
        remaining[z:z+h,x:x+w]=False;result.append((int(x),int(z),int(w),int(h)))
    return result

parts=[]
def box(name,a,b,material):parts.append(dict(name=name,a=np.array(a,dtype=float),b=np.array(b,dtype=float),material=material))
for i,(x,z,w,h) in enumerate(rectangles(mask)):box('scoop_floor_'+str(i),[(x-6)/4,0,z/4],[(x+w-6)/4,.1875,(z+h)/4],'floor')
for i,(x,z,w,h) in enumerate(rectangles(rim)):box('scoop_rim_'+str(i),[(x-6)/4,.1875,z/4],[(x+w-6)/4,.625,(z+h)/4],'rim')
box('neck',[-.3125,.1875,2.875],[.3125,.5625,3.65],'handle')
box('handle',[-.3125,.125,3.65],[.3125,.5625,7.875],'handle')
box('handle_end',[-.4375,.125,7.875],[.4375,.5625,8.25],'handle')
box('handle_tip',[-.25,.125,8.25],[.25,.5625,8.5],'handle')

def uvrect(part,face):
    x0,y0,z0=part['a'];x1,y1,z1=part['b']
    if face=='up':
        if part['material']=='floor':return [16+(x0+1.5)*16/3,z0*16/3,16+(x1+1.5)*16/3,z1*16/3]
        return [(x0+1.5)*4+1,z0*3+2,(x1+1.5)*4+1,z1*3+2]
    if face=='down':return [17+(x0+1.5)*4,16+z0*1.65,17+(x1+1.5)*4,16+z1*1.65]
    if face in ('east','west'):return [17+z0*1.65,16+y0*16,17+z1*1.65,16+y1*16]
    return [17+(x0+1.5)*4,16+y0*16,17+(x1+1.5)*4,16+y1*16]

elements=[];minecraft=[];shift=np.array([8,8,3])
for part in parts:
    faces={f:dict(uv=uvrect(part,f),texture=0) for f in ['north','east','south','west','up','down']}
    a=(part['a']+shift).tolist();b=(part['b']+shift).tolist()
    element=dict(name=part['name'],uuid=uid(part['name']),type='cube',origin=[8,8.35,9.6],rotation=[0,0,0],
        to=b,box_uv=False,autouv=0,visibility=True,export=True,faces=faces)
    element['from']=a;elements.append(element)
    minecraft.append({'from':a,'to':b,'faces':{f:dict(uv=[v/2 for v in uvrect(part,f)],texture='#wood') for f in faces}})
group=dict(name='spoon',uuid=uid('spoon'),origin=[8,8.35,9.6],rotation=[0,0,0],visibility=True,export=True,color=4)
raw=(OUT/'wooden_spoon.png').read_bytes()
model=dict(meta=dict(format_version='5.0',model_format='free',box_uv=False),name='果酱小木勺-v1',model_identifier='jam_wooden_spoon',
    credit='紫菜 · 农场物语 · ARR',resolution=dict(width=32,height=32),textures=[dict(name='wooden_spoon.png',id='0',uuid=uid('texture'),
        mode='bitmap',source='data:image/png;base64,'+base64.b64encode(raw).decode(),internal=True,width=32,height=32,uv_width=32,uv_height=32,render_sides='front')],
    elements=elements,groups=[group],outliner=[dict(uuid=group['uuid'],children=[e['uuid'] for e in elements])],animations=[],
    editor_state=dict(mode='edit',tool='move_tool',saved=True,selected_elements=[],selected_groups=[],previews=dict(main=dict(position=[20,25,24],target=[8,8.2,7.2],orthographic=False,zoom=1,angle=None))))
(OUT/'果酱小木勺-v1.bbmodel').write_text(json.dumps(model,ensure_ascii=False,indent=2),encoding='utf-8')
(OUT/'wooden_spoon.json').write_text(json.dumps(dict(textures=dict(wood='farmingtales_drink_anim:jam/wooden_spoon',particle='farmingtales_drink_anim:jam/wooden_spoon'),
    elements=minecraft,display=dict(gui=dict(rotation=[35,-45,0],translation=[0,0,0],scale=[1.5,1.5,1.5]))),indent=2),encoding='utf-8')

# Render the actual exported cuboids and atlas, using nearest-neighbour texture sampling.
sys.path.insert(0,str(ROOT.parent/'.preview-deps'))
import moderngl
ctx=moderngl.create_standalone_context();program=ctx.program(vertex_shader='''#version 330
in vec3 pos; in vec3 normal; in vec2 uv;
uniform mat4 mvp; out vec2 texcoord; out float shade;
void main(){gl_Position=mvp*vec4(pos,1);texcoord=uv;shade=.57+.43*max(0,dot(normal,normalize(vec3(-.4,1,.3))));}
''',fragment_shader='''#version 330
uniform sampler2D atlas; in vec2 texcoord; in float shade; out vec4 color;
void main(){vec4 p=texture(atlas,texcoord);color=vec4(p.rgb*shade,p.a);}
''')
def corners(a,b):
    x,y,z=a;X,Y,Z=b
    return {'up':([(x,Y,z),(x,Y,Z),(X,Y,Z),(X,Y,z)],(0,1,0)),
        'down':([(x,y,Z),(x,y,z),(X,y,z),(X,y,Z)],(0,-1,0)),
        'north':([(X,Y,z),(X,y,z),(x,y,z),(x,Y,z)],(0,0,-1)),
        'south':([(x,Y,Z),(x,y,Z),(X,y,Z),(X,Y,Z)],(0,0,1)),
        'west':([(x,Y,z),(x,y,z),(x,y,Z),(x,Y,Z)],(-1,0,0)),
        'east':([(X,Y,Z),(X,y,Z),(X,y,z),(X,Y,z)],(1,0,0))}
vertices=[]
for part in parts:
    for face,(points,normal) in corners(part['a'],part['b']).items():
        u,v,U,V=uvrect(part,face);uv=[(u,v),(u,V),(U,V),(U,v)]
        for idx in [0,1,2,0,2,3]:vertices.append([*points[idx],*normal,uv[idx][0]/32,uv[idx][1]/32])
vbo=ctx.buffer(np.array(vertices,dtype='f4').tobytes());vao=ctx.vertex_array(program,[(vbo,'3f 3f 2f','pos','normal','uv')])
texture=ctx.texture((32,32),4,tex.tobytes());texture.filter=(moderngl.NEAREST,moderngl.NEAREST);texture.use()
ctx.enable(moderngl.DEPTH_TEST)
def render(eye,target,size,span):
    width,height=size;fbo=ctx.simple_framebuffer((width*2,height*2),components=4);fbo.use();fbo.clear(.105,.125,.14,0)
    eye=np.array(eye);target=np.array(target);forward=target-eye;forward/=np.linalg.norm(forward)
    up=np.array([0.,1,0]) if abs(forward[1])<.99 else np.array([0.,0,-1])
    right=np.cross(forward,up);right/=np.linalg.norm(right);up=np.cross(right,forward)
    view=np.eye(4);view[:3,:3]=np.array([right,up,-forward]);view[:3,3]=-view[:3,:3]@eye
    proj=np.diag([2/(span*width/height),2/span,-2/100,1.]);proj[2,3]=-1
    program['mvp'].write((proj@view).T.astype('f4').tobytes());vao.render()
    im=Image.frombytes('RGBA',(width*2,height*2),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM)
    return im.resize(size,Image.Resampling.LANCZOS)

font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
sheet=Image.new('RGB',(1500,1050),(27,32,36));draw=ImageDraw.Draw(sheet)
draw.text((58,34),'果酱小木勺',font=font(38),fill=(236,222,198));draw.text((60,88),'模型草稿 v1  /  实际模型与像素贴图渲染',font=font(19),fill=(162,171,170))
views=[('整体 · 浅凹勺头',(9,14,-5),(0,.25,4),(60,150),(820,690),10.2),
    ('勺面 · 木纹与凹槽',(0,18,1.4),(0,.25,1.45),(960,170),(450,360),4.2),
    ('侧面 · 薄边与勺柄',(12,2.8,5),(0,.25,4.2),(900,620),(550,180),3.4)]
for label,eye,target,xy,size,span in views:
    image=render(eye,target,size,span);sheet.paste(image,xy,image)
    draw.text((xy[0],xy[1]-32),label,font=font(21),fill=(196,186,166))
draw.line((60,900,1440,900),fill=(65,70,70),width=2)
draw.text((60,928),'勺头宽 2.5 px  ·  总长 8.5 px  ·  32 × 32 木纹贴图  ·  适配 4 px 罐口',font=font(23),fill=(214,204,184))
draw.text((60,976),'独立 spoon 握点，可用于后续挖取果酱与送入口中的动作。',font=font(19),fill=(148,160,159))
sheet.save(OUT/'木勺预览.png')
(OUT/'使用说明.txt').write_text('果酱小木勺 v1\n作者：紫菜；协议：ARR。\n打开 bbmodel 即可编辑，32×32 贴图已内嵌。spoon 组原点为握持点。\n尺寸以模型像素为单位：长 8.5，勺头宽 2.5，勺柄宽 0.625。勺槽深 0.4375。\nwooden_spoon.json 是配套 Minecraft 模型；安装使用时纹理路径为 farmingtales_drink_anim:jam/wooden_spoon。\n本包为独立模型草稿，尚未接入模组或修改客户端。\n',encoding='utf-8')
license_path=ROOT.parent/'src/main/resources/LICENSE.txt';(OUT/'LICENSE.txt').write_bytes(license_path.read_bytes())
loaded=json.loads((OUT/'果酱小木勺-v1.bbmodel').read_text(encoding='utf-8'))
assert len(loaded['elements'])==len(parts) and len(set(e['uuid'] for e in elements))==len(parts)
assert all(np.all(np.array(e['to'])>np.array(e['from'])) for e in elements)
assert all(0<=v<=32 for e in elements for face in e['faces'].values() for v in face['uv'])
assert np.array(tex)[:,:,3].min()==255
with zipfile.ZipFile(ROOT/'果酱小木勺-v1.zip','w',zipfile.ZIP_DEFLATED) as z:
    for p in sorted(OUT.iterdir()):
        if p.is_file():z.write(p,p.name)
print('Created spoon:',len(parts),'cuboids; embedded opaque texture; model, preview and ZIP validated.')
