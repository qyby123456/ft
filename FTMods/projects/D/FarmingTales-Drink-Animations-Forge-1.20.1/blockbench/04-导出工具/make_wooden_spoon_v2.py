from pathlib import Path
import base64,io,json,math,sys,uuid,zipfile
import numpy as np
from PIL import Image,ImageDraw,ImageFont

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'03-参考素材/果酱小木勺-v2';OUT.mkdir(exist_ok=True)
uid=lambda name:str(uuid.uuid5(uuid.NAMESPACE_URL,'farmingtales/jam-spoon-v2/'+name))

# Three broad cuboids with the same coarse pixel scale as the pack's 16px utensils.
colors=[(122,80,40,255),(162,114,62,255),(78,49,24,255),(185,140,84,255)]
tex=Image.new('RGBA',(16,16),colors[0]);d=ImageDraw.Draw(tex)
d.rectangle((12,12,13,13),fill=colors[2]);d.rectangle((14,12,15,13),fill=colors[0])
d.rectangle((6,3,8,4),fill=colors[1]);d.point((7,2),fill=colors[3])
d.point((6,3),fill=colors[3]);d.point((7,3),fill=colors[2]);d.point((7,4),fill=colors[0])
d.line((7,5,7,10),fill=colors[0]);d.point((7,5),fill=colors[1]);d.point((7,8),fill=colors[1])
tex.save(OUT/'wooden_spoon.png')
parts=[]
def box(name,a,b,material):parts.append(dict(name=name,a=np.array(a,dtype=float),b=np.array(b,dtype=float),material=material))
box('spoon_tip',[-.5,0,0],[.5,.5,1],'wood')
box('spoon_head',[-1.5,0,1],[1.5,.5,3],'wood')
box('spoon_handle',[-.5,0,3],[.5,.5,9],'wood')
def uvrect(part,face):
    x0,y0,z0=part['a'];x1,y1,z1=part['b']
    if face=='up':return [x0+7.5,z0+2,x1+7.5,z1+2]
    if face=='down':return [14,12,15,13]
    return [12,12,13,13]
sprite=Image.new('RGBA',(16,16))
for p in parts:
    a,b=p['a'],p['b']
    for y in range(round(a[2]+2),round(b[2]+2)):
        for x in range(round(a[0]+7.5),round(b[0]+7.5)):sprite.putpixel((x,y),tex.getpixel((x,y)))
sprite.save(OUT/'wooden_spoon_item.png')

elements=[];minecraft=[];shift=np.array([8,8,3])
for part in parts:
    faces={f:dict(uv=uvrect(part,f),texture=0) for f in ['north','east','south','west','up','down']}
    a=(part['a']+shift).tolist();b=(part['b']+shift).tolist()
    element=dict(name=part['name'],uuid=uid(part['name']),type='cube',origin=[8,8.35,9.6],rotation=[0,0,0],
        to=b,box_uv=False,autouv=0,visibility=True,export=True,faces=faces)
    element['from']=a;elements.append(element)
    minecraft.append({'from':a,'to':b,'faces':{f:dict(uv=uvrect(part,f),texture='#wood') for f in faces}})
group=dict(name='spoon',uuid=uid('spoon'),origin=[8,8.35,9.6],rotation=[0,0,0],visibility=True,export=True,color=4)
raw=(OUT/'wooden_spoon.png').read_bytes()
model=dict(meta=dict(format_version='5.0',model_format='free',box_uv=False),name='03-参考素材/果酱小木勺-v2',model_identifier='jam_wooden_spoon',
    credit='紫菜 · 农场物语 · ARR',resolution=dict(width=16,height=16),textures=[dict(name='wooden_spoon.png',id='0',uuid=uid('texture'),
        mode='bitmap',source='data:image/png;base64,'+base64.b64encode(raw).decode(),internal=True,width=16,height=16,uv_width=16,uv_height=16,render_sides='front')],
    elements=elements,groups=[group],outliner=[dict(uuid=group['uuid'],children=[e['uuid'] for e in elements])],animations=[],
    editor_state=dict(mode='edit',tool='move_tool',saved=True,selected_elements=[],selected_groups=[],previews=dict(main=dict(position=[20,25,24],target=[8,8.2,7.2],orthographic=False,zoom=1,angle=None))))
(OUT/'果酱小木勺-v2.bbmodel').write_text(json.dumps(model,ensure_ascii=False,indent=2),encoding='utf-8')
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
        for idx in [0,1,2,0,2,3]:vertices.append([*points[idx],*normal,uv[idx][0]/16,uv[idx][1]/16])
vbo=ctx.buffer(np.array(vertices,dtype='f4').tobytes());vao=ctx.vertex_array(program,[(vbo,'3f 3f 2f','pos','normal','uv')])
texture=ctx.texture((16,16),4,tex.tobytes());texture.filter=(moderngl.NEAREST,moderngl.NEAREST);texture.use()
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
draw.text((58,34),'果酱小木勺',font=font(38),fill=(236,222,198));draw.text((60,88),'简化稿 v2  /  16 × 16 像素风格',font=font(19),fill=(162,171,170))
views=[('整体 · 简单方块轮廓',(9,14,-5),(0,.25,4),(60,150),(820,690),10.2),
    ('勺面 · 少量明暗像素',(0,18,1.4),(0,.25,1.45),(960,170),(450,360),4.2),
    ('侧面 · 半像素厚度',(12,2.8,5),(0,.25,4.2),(900,620),(550,180),3.4)]
for label,eye,target,xy,size,span in views:
    image=render(eye,target,size,span);sheet.paste(image,xy,image)
    draw.text((xy[0],xy[1]-32),label,font=font(21),fill=(196,186,166))
draw.line((60,900,1440,900),fill=(65,70,70),width=2)
draw.text((60,928),'3 个方块  ·  勺头宽 3 px  ·  16 × 16 贴图  ·  适配果酱罐口',font=font(23),fill=(214,204,184))
draw.text((60,976),'勺柄保持直线，勺面用两格深色像素表现凹感。',font=font(19),fill=(148,160,159))
sheet.save(OUT/'木勺预览.png')
(OUT/'使用说明.txt').write_text('果酱小木勺 v2\n作者：紫菜；协议：ARR。\n打开 bbmodel 即可编辑，16×16 贴图已内嵌。spoon 组原点为握持点。\n尺寸以模型像素为单位：长 9，勺头宽 3，勺柄宽 1，厚 0.5。勺面凹感由贴图表现。\nwooden_spoon.json 是配套 Minecraft 模型；安装使用时纹理路径为 farmingtales_drink_anim:jam/wooden_spoon。\n本包为独立模型草稿，尚未接入模组或修改客户端。\n',encoding='utf-8')
license_path=ROOT.parent/'src/main/resources/LICENSE.txt';(OUT/'LICENSE.txt').write_bytes(license_path.read_bytes())
loaded=json.loads((OUT/'果酱小木勺-v2.bbmodel').read_text(encoding='utf-8'))
assert len(loaded['elements'])==len(parts) and len(set(e['uuid'] for e in elements))==len(parts)
assert all(np.all(np.array(e['to'])>np.array(e['from'])) for e in elements)
assert all(0<=v<=16 for e in elements for face in e['faces'].values() for v in face['uv'])
assert np.array(tex)[:,:,3].min()==255
with zipfile.ZipFile(ROOT/'05-打包文件/果酱小木勺-v2.zip','w',zipfile.ZIP_DEFLATED) as z:
    for p in sorted(OUT.iterdir()):
        if p.is_file():z.write(p,p.name)
print('Created spoon:',len(parts),'cuboids; embedded opaque texture; model, preview and ZIP validated.')
