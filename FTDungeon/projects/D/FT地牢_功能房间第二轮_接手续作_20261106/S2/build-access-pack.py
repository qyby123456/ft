import hashlib
import json
from pathlib import Path
import shutil
import zipfile

ROOT = Path(__file__).resolve().parent
OUTPUT = ROOT / 'access-install-33ceb33f'
PACK = OUTPUT / 's2_desert_access'
SOURCE = ROOT / 'assembly/s2-desert-functional-combined.nbt'
HASH = hashlib.sha256(SOURCE.read_bytes()).hexdigest()
assert HASH == '33ceb33f633cfa6e797cc184cd3c5ff34ce9f0901e9b909da161782ec5cc0e6c'


def text(relative, content):
    path = PACK / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.rstrip()+'\n', encoding='utf-8')


def data(relative, value):
    text(relative, json.dumps(value, ensure_ascii=False, indent=2))


data('pack.mcmeta', {'pack': {'pack_format': 15, 'description': 'S2 desert access / 1.20.1 / NBT 33ceb33f'}})
data('data/minecraft/tags/functions/load.json', {'values': ['s2access:load']})
data('data/minecraft/tags/functions/tick.json', {'values': ['s2access:tick']})
text('data/s2access/functions/load.mcfunction', '''scoreboard objectives add s2_enter trigger
scoreboard objectives add s2_return trigger
scoreboard objectives add s2_state dummy
scoreboard players add #installed s2_state 0
scoreboard players add #loads s2_state 1
scoreboard players add #ticks s2_state 0''')
text('data/s2access/functions/tick.mcfunction', '''scoreboard players add #ticks s2_state 1
execute if score #installed s2_state matches 1 as @a[tag=!s2inside] at @s if dimension minecraft:overworld run scoreboard players enable @s s2_enter
scoreboard players enable @a[tag=s2inside] s2_return
execute if score #installed s2_state matches 1 as @a[tag=!s2inside,scores={s2_enter=1..}] at @s if dimension minecraft:overworld run function s2access:enter
execute as @a[tag=s2inside,scores={s2_return=1..}] run function s2access:return
scoreboard players set @a[scores={s2_enter=1..}] s2_enter 0
scoreboard players set @a[scores={s2_return=1..}] s2_return 0''')
text('data/s2access/functions/enter.mcfunction', '''execute in minecraft:overworld run tp @s 1027.5 137 1045.5 -90 0
tag @s add s2inside
tellraw @s {"text":"已进入S2砂岩荒漠。返回：/trigger s2_return set 1","color":"gold"}''')
text('data/s2access/functions/return.mcfunction', '''execute in minecraft:overworld run tp @s 1018.5 128 1045.5 90 0
tag @s remove s2inside
tellraw @s {"text":"已返回S2外部接待台。再次进入：/trigger s2_enter set 1","color":"gold"}''')
text('data/s2access/functions/install.mcfunction', '''execute if score #installed s2_state matches 0 in minecraft:overworld run function s2access:install_world
execute if score #installed s2_state matches 1 run tellraw @s {"text":"S2已安装；重复安装被阻止。坐标1024 128 1024，NBT 33ceb33f。","color":"gold"}''')
commands = ['forceload add 1008 1024 1263 1151']
for east in range(1024, 1263, 32):
    for south in range(1024, 1150, 32):
        commands.append(f'fill {east} 128 {south} {min(east+31,1262)} 152 {min(south+31,1149)} minecraft:air replace')
commands += ['fill 1014 127 1041 1022 133 1049 minecraft:air replace', 'fill 1014 127 1041 1022 127 1049 minecraft:cut_sandstone', 'setblock 1018 127 1045 minecraft:chiseled_sandstone', 'execute store success score #placed s2_state run place template s2access:desert_33ceb33f 1024 128 1024 none none 1.0 0', 'execute if score #placed s2_state matches 1 run scoreboard players set #installed s2_state 1', 'forceload remove 1008 1024 1263 1151']
text('data/s2access/functions/install_world.mcfunction', '\n'.join(commands))
target = PACK / 'data/s2access/structures/desert_33ceb33f.nbt'
target.parent.mkdir(parents=True, exist_ok=True)
shutil.copyfile(SOURCE, target)
readme = '''# S2 1.20.1安装、外部进入与返回

绑定合并NBT SHA-256：`'''+HASH+'''`。原十房、屏障、家具、目标、合并与15片均不修改。数据包内结构是同一文件的逐字节副本。

需要Minecraft Java 1.20.1（pack_format 15），可用于原版或Forge；不依赖KubeJS或额外模组。安装操作仅限管理员。此包固定使用主世界，不能与其他S2同名scoreboard/namespace共存。

1. 在服务器关闭时，将s2_desert_access.zip放进目标存档的datapacks目录，或将同名展开目录放入该处（两者只选一个）。重新启动服务器，load与tick由原版标签自然执行。已有在线世界可使用管理员/reload加载，但隔离验算采用开服自动加载。
2. **先备份并专门预留空地。** 安装会清空主世界闭区间x=1024..1262、y=128..152、z=1024..1149，共239×25×126=752850格，包括源NBT未显式记录的空间。另清空接待台x=1014..1022、y=127..133、z=1041..1049并重建y127地板。不要在这些区域保留建筑、方块实体、装饰、其他结构、玩家或生物；函数不搬迁/删除实体。整体预留包围盒为[1014,127,1024]..[1262,152,1149]。每条fill最多25600格，低于默认32768上限。
3. 安装临时强制加载主世界x=1008..1263、z=1024..1151共128区块，并在结束移除。该范围必须专用于本次安装、事先没有其他forceload；否则会移除已有强加载声明。预留结构空间及接待台须没有其他地形/建筑干涉。安装只在首次#installed=0运行，不会在load、tick或再次调用时自动重建/清空。
4. 管理员执行一次`/function s2access:install`，再以`/scoreboard players get #placed s2_state`和`/scoreboard players get #installed s2_state`确认均为1。若place失败则installed保持0；处理失败后才重试。安装非事务性，填充/清空不能自动回滚，因此必须预留空地并备份。不要绕过外层函数直接调用install_world；仅安装当前未安装的新世界实例。
5. 普通生存玩家（无需OP）在主世界执行`/trigger s2_enter set 1`，自然下一刻传送到入口[1027.5,137,1045.5]并标记s2inside。包默认允许主世界任意玩家进入，未配置费用、队伍或战斗限制。外围屏障完整保留，不开洞。
6. 场内玩家执行`/trigger s2_return set 1`，自然下一刻返回主世界外部接待台[1018.5,128,1045.5]并清除标签。这是固定公共返回点，不记录原位置；再次使用enter可重入。返回支持已标记玩家位于其他维度。tick重新启用可用trigger；s2inside标签及记分板随存档保存。死亡/离线不自动返回，重生后仍可用return。

接待台在结构西侧，x=1014..1022，结构从x1024开始；二者不重叠。接待台地板y127，站立y128，上方净空6格。管理者可用/tp到接待台查看；普通玩家通过trigger进入/返回，不依赖外部步道或破坏屏障。放置模板本身没有旋转、镜像或随机完整度删块。

卸载：先让所有s2inside玩家使用return，再移除数据包；否则不能通过trigger离开屏障结构。停用包不删除建筑。保留备份及本包；需要恢复时重新启用包。本文不指示修改正式实例或生成器；本次交付只在独立测试目录验算。

隔离验证证据见同目录validation.json及runtime-access-r4。runtime-access-r1未将FakePlayer加入PlayerList，@a无法选中；r2修改getPlayers只读视图失败；r3入列后发现FakePlayer的连接传送桩不执行原生tp，均作为测试装置失败保留。最终测试采用原生ServerPlayer、ServerGamePacketListenerImpl及无真实客户端的Connection，并通过仅限测试代码的反射入列，使原版@a可以选中。普通玩家权限仍为0，未手动启用trigger或调用load/tick，初始外部站位之后仅数据包tp驱动坐标改变。该测试代码不在安装数据包内，用户不需要模组或反射。原报告scope字段若含沿用的FakePlayer字样，以同报告test_player_enrollment字段和实际源码为准：r4实际为原生ServerPlayer，无真实客户端；validation.json已明确此范围。制作方实服测试不等于父线程独立复跑或真人客户端生存验收。
'''
(OUTPUT / 'INSTALL.md').write_text(readme, encoding='utf-8')
archive = OUTPUT / 's2_desert_access.zip'
with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as bundle:
    for path in sorted(PACK.rglob('*')):
        if path.is_file():
            info = zipfile.ZipInfo(path.relative_to(PACK).as_posix(), date_time=(2026,11,7,0,0,0))
            info.compress_type = zipfile.ZIP_DEFLATED
            bundle.writestr(info, path.read_bytes())
(OUTPUT / 'package-manifest.json').write_text(json.dumps({'style':'S2','source_sha256':HASH,'origin':[1024,128,1024],'structure_size':[239,25,126],'clear_inclusive':{'min':[1024,128,1024],'max':[1262,152,1149]},'external_pad_clear_inclusive':{'min':[1014,127,1041],'max':[1022,133,1049]},'entry':[1027.5,137,1045.5],'return':[1018.5,128,1045.5],'files':[{'path':str(path),'relative_path':path.relative_to(OUTPUT).as_posix(),'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'bytes':path.stat().st_size} for path in sorted([archive,OUTPUT/'INSTALL.md',*filter(Path.is_file,PACK.rglob('*'))])]},ensure_ascii=False,indent=2),encoding='utf-8')
print('ACCESS_PACK_BUILT',HASH,archive)
