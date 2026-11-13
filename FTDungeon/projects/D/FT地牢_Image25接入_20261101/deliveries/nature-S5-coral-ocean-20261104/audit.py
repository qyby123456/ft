import collections
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import Structure

groups = {
    'G2': Path('D:/ft工程文件/FT地牢_自然环境第一批第2组_20261104'),
    'G4': ROOT.parent / 'nature-batch1-group4-swamp',
    'G6': ROOT.parent / 'nature-batch1-S6',
    'G7': Path('D:/ft工程文件/FT地牢_自然环境第7组_蘑菇真菌_20261104'),
    'G8': ROOT.parent / 'nature-b1-S8-crystal',
}


def signature(path):
    structure = Structure.read(path)
    metadata = json.loads((path.parent / 'room.json').read_text(encoding='utf-8-sig'))
    feet = metadata['entrance'][1]
    width, height, depth = structure.size
    def air(blockX, blockY, blockZ):
        state = structure.blocks.get((blockX, blockY, blockZ))
        return state is None or state.name in ('minecraft:air','minecraft:cave_air','minecraft:void_air')
    mask = [[air(blockX, feet, blockZ) and air(blockX, feet + 1, blockZ) and not air(blockX, feet - 1, blockZ) for blockX in range(width)] for blockZ in range(depth)]
    samples = [[mask[min(depth-1,int((sampleZ+.5)*depth/24))][min(width-1,int((sampleX+.5)*width/24))] for sampleX in range(24)] for sampleZ in range(24)]
    variants = []
    for turn in range(4):
        variants.append(''.join('1' if value else '0' for row in samples for value in row))
        samples = [list(row) for row in zip(*samples[::-1])]
    return dict(path=str(path),sha256=hashlib.sha256(path.read_bytes()).hexdigest(),size=list(structure.size),mask=variants,canonical_hash=hashlib.sha256(min(variants).encode()).hexdigest())


own = [signature(path) for path in sorted((ROOT / 'rooms').glob('*/room.nbt'))]
comparisons = []
inventory = []
for group, folder in groups.items():
    candidates = []
    seen = set()
    for path in sorted(folder.rglob('room.nbt')):
        if not (path.parent / 'room.json').exists() or any(part in ('qa','reviews','four-view-review','native-test','forge','backups') for part in path.relative_to(folder).parts):
            continue
        try:
            candidate = signature(path)
        except (KeyError, ValueError, TypeError):
            continue
        if candidate['sha256'] not in seen:
            candidates.append(candidate)
            seen.add(candidate['sha256'])
    inventory.append(dict(group=group,path=str(folder),count=len(candidates),files=[dict(path=item['path'],sha256=item['sha256']) for item in candidates]))
    for room in own:
        scores = []
        for candidate in candidates:
            similarity = max(sum(first==second for first,second in zip(room['mask'][0],variant))/576 for variant in candidate['mask'])
            scores.append((similarity,candidate))
        if scores:
            score, match = max(scores,key=lambda item:item[0])
            comparisons.append(dict(room=Path(room['path']).parent.name,other_group=group,nearest_path=match['path'],other_sha256=match['sha256'],normalized_air_mask_agreement=round(score,4),same_rotation_normalized_mask=room['canonical_hash']==match['canonical_hash']))
report = dict(scope='Material-independent 24x24 foot/head-air floor mask; compare all quarter-turns. Approximate screening, not a topology-isomorphism or gameplay uniqueness proof.',missing_groups=['G1','G3'],inventory=inventory,comparisons=comparisons,rooms=[{key:value for key,value in item.items() if key!='mask'} for item in own],review_required=[item for item in comparisons if item['normalized_air_mask_agreement']>=.92],global_layout='NOT_ASSEMBLED; module graph proposal is not placed geometry')
(ROOT / 'distinctiveness-audit.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
notes = '''# 第5组差异化设计与重复度审计输入（修订版）

## 已实现的独特核心

| 维度 | 实际结构及可核验位置 | 边界 |
| --- | --- | --- |
| 核心拓扑 | NAT-S5-01 两个沿 Z 错位、X 错开的独立封闭珊瑚礁水箱，形成多段变向绕行与侧向东出口；不再使用单中央景物加四角布景 | 不是其它组中央树/井/祭坛的换皮 |
| 路线模板 | NAT-S5-02 两道相向错位玻璃隔墙：z=5 的左侧隔墙与 z=9 的右侧隔墙，强制左右换边的 S 形行进 | 与普通直通中房不同；全部出口仍可步行 |
| 垂直层次 | NAT-S5-08 西侧三格宽、三阶南向楼梯连接 y=3 平台（脚位 y=4），下层继续贯通西侧接口；中央封闭礁池与高处玻璃采光 | `qa/mezzanine-room/native-qa.json` 验证楼梯到 [3,4,10] 的真实原版碰撞路径；不是新增世界连接出口 |
| 机关 | 目前无开关门、红石、流体开关或机关脚本；密封玻璃隔墙只承担视线与行进约束 | 不把装饰冒称可运行机关，也不擅自添加刷怪/奖励系统 |
| 战斗节奏 | 空间设计意图：双礁绕行多角度观察 → S 形隔墙压缩推进 → 双列柱廊展开 → 上层观景提供观察停顿 | 仅空间节奏，无敌人、波次、DPS 或实机战斗验收 |
| 地标 | 两座错位活珊瑚礁、透视 S 隔墙、西侧观景夹层、封闭玻璃天井、末端石英潮心石 | 不以颜色变化替代这些几何特征 |
| 调色板 | 海晶砖、暗海晶石、海晶石、海光灯来自真实海底神殿；玻璃活珊瑚水景与石英祭台是本组原创改造 | 调色板只作辅助区分 |

## 审计口径

`distinctiveness-audit.json` 记录本组最终 NBT 哈希、已发现其它组 NBT 的绝对路径/快照哈希、去材质脚位通行遮罩的四旋转比较、每房最近邻和 >=0.92 的复核候选。

该算法只比较缩放到 24×24 的“脚和头为空气且有底”的平面栅格，不理解台阶完整碰撞、功能语义或完整空间图；不能用它宣称已证明全组拓扑唯一。标准 3×3 门口、直廊、L 转角是共用接入原语，须在最终矩阵中与核心房间分列，不能将其当作独特性证据。

本次只读核对当前可发现的 G2/G4/G6/G7/G8；G1/G3 尚未纳入。其它组并行修订可能改变快照，最终负责人须用同一终态哈希重算全组矩阵。未拿到终态矩阵前，状态为“已差异化修订，最终重复度审计待验收”，不是“全局去重通过”。

所有几何修改仅限本组，未复用其它组生成脚本或路线模板；来源仍是原海底神殿真实 NBT 的功能、尺度与调色板研究。未部署世界，没有组间实际拼接图；不把拟议的组内游玩顺序冒充已连接的全局路线。
'''
(ROOT / 'DESIGN-DISTINCTIVENESS.md').write_text(notes,encoding='utf-8')
print('Compared',len(comparisons),'room/group pairs; flagged',len(report['review_required']),'for human semantic review.')
