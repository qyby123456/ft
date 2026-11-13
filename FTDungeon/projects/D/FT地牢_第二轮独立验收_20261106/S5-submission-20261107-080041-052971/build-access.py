import hashlib
import json
import shutil
import zipfile
from pathlib import Path

root=Path(__file__).resolve().parent
destination=root/'access-package'
pack=destination/'s5access'
namespace=pack/'data/s5access'
functions=namespace/'functions'
structures=namespace/'structures'
functions.mkdir(parents=True,exist_ok=True)
structures.mkdir(parents=True,exist_ok=True)
digest=lambda path:hashlib.sha256(path.read_bytes()).hexdigest()
merged=root/'merged/merged.nbt'
assert digest(merged)=='eb8ac4bccc0829e591cd03310cd6bee221321a6724e822c9368e8c303b729bb7'
parts=json.loads((root/'merged/parts.json').read_text(encoding='utf-8'))
assert parts['source_sha256']==digest(merged)
def text(path,value):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(value.rstrip()+'\n',encoding='utf-8')
def function(name,commands):
    text(functions/(name+'.mcfunction'),'\n'.join(commands))
text(pack/'pack.mcmeta',json.dumps({'pack':{'pack_format':15,'description':'S5 external access, fixed deployment origin 2048 120 1024, current eb8ac4bc'}},ensure_ascii=False,indent=2))
for tag in ['load','tick']:
    text(pack/f'data/minecraft/tags/functions/{tag}.json',json.dumps({'values':[f's5access:{tag}']}))
function('load',['scoreboard objectives add s5a_enter trigger','scoreboard objectives add s5a_exit trigger'])
function('tick',[
    'execute as @a[scores={s5a_enter=1..}] at @s run function s5access:request_enter',
    'execute as @a[scores={s5a_exit=1..}] at @s run function s5access:request_exit',
    'scoreboard players enable @a s5a_enter','scoreboard players enable @a s5a_exit'])
function('request_enter',[
    'execute if dimension minecraft:overworld if entity @s[x=2032.5,y=121,z=1047.5,distance=..2] if block 2048 120 1024 minecraft:barrier if block 2058 129 1047 minecraft:air run tp @s 2058.5 129 1047.5 -90 0',
    'scoreboard players set @s s5a_enter 0'])
function('request_exit',[
    'execute if dimension minecraft:overworld if entity @s[x=2062.5,y=125,z=1093.5,distance=..2] if block 2048 120 1024 minecraft:barrier if block 2032 121 1047 minecraft:air run tp @s 2032.5 121 1047.5 -90 0',
    'scoreboard players set @s s5a_exit 0'])
install=['execute in minecraft:overworld run forceload add 2028 1024 2264 1117']
for index,part in enumerate(parts['parts'],1):
    source=Path(part['path']);assert digest(source)==part['sha256']
    name=f'part_{index:02}'
    shutil.copyfile(source,structures/(name+'.nbt'))
    origin=[2048+part['origin'][0],120+part['origin'][1],1024+part['origin'][2]]
    install.append('execute in minecraft:overworld run place template s5access:'+name+' '+' '.join(map(str,origin)))
shutil.copyfile(root/'access-lobby/access-lobby.nbt',structures/'lobby.nbt')
install+=['execute in minecraft:overworld run place template s5access:lobby 2028 120 1043','execute in minecraft:overworld run forceload remove 2028 1024 2264 1117','function s5access:load','tellraw @a {"text":"S5 access installed: lobby 2032.5 121 1047.5; /trigger s5a_enter; finish /trigger s5a_exit","color":"aqua"}']
function('install',install)
manifest={'merged_sha256':digest(merged),'origin':[2048,120,1024],'external_lobby_feet':[2032.5,121,1047.5],'entry_feet':[2058.5,129,1047.5],'finish_feet':[2062.5,125,1093.5],'barrier_removed':False,'deployed_to_formal_instance':False,'files':[{'path':str(path.relative_to(pack)),'sha256':digest(path)} for path in sorted(pack.rglob('*')) if path.is_file()]}
text(destination/'manifest.json',json.dumps(manifest,ensure_ascii=False,indent=2))
with zipfile.ZipFile(destination/'s5access.zip','w',compression=zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(pack.rglob('*')):
        if path.is_file():archive.write(path,path.relative_to(pack).as_posix())
text(destination/'README.md','''# S5 外部进出方案（待部署）

Java/Forge 1.20.1 数据包；仅打包在 S5 接手续作目录，尚未安装到正式存档。绑定 merged SHA-256 eb8ac4bccc0829e591cd03310cd6bee221321a6724e822c9368e8c303b729bb7，原 NBT 和外围屏障均不修改。

在选定的测试存档 datapacks 中放置 s5access.zip，执行 /reload。安装位置固定为主世界原点 2048 120 1024，建筑占据 x2048..2264、y120..148、z1024..1117；外部密封大厅 x2028..2036、y120..124、z1043..1051。安装函数会覆盖这些区域，应先在空白测试区使用；本交付未执行正式安装。

管理员运行 /function s5access:install，分十块放置建筑，并放置独立密封玻璃大厅。首次将玩家送到大厅：/tp <玩家> 2032.5 121 1047.5。普通玩家在大厅中心两格内运行 /trigger s5a_enter，可抵达建筑入口 2058.5 129 1047.5；走到终点 2062.5 125 1093.5 两格内运行 /trigger s5a_exit，即返回外部大厅。触发器无需玩家 OP；仅主世界指定站位有效，其他地方的请求清零。安装/首次大厅传送由管理员执行。

入口对应 merged 相对坐标 [10,9,23]，终点对应 [14,5,69]；不再使用已封闭的旧外接口 [8,9,23] 和 [12,5,69]。大厅位于屏障以西，内部传送跨越屏障，保留全部 57482 个屏障单元。大厅自身有密闭玻璃壳和明确空气体积，水下也不会靠挖开外壳接入。大厅之外的出生点/主城回程由服主管理，包内只负责大厅与地牢之间的双向传送。

保持默认坐标使用；如需移动，请先重新生成并同步所有命令中的锚点，不能只移动结构。原版饥饿、溺水、真实玩家攀爬与权限插件兼容性不由本包自检认证。
''')
print(json.dumps({'pack':str(pack),'zip':str(destination/'s5access.zip'),'files':len(manifest['files']),'merged_sha256':digest(merged)},ensure_ascii=False))
