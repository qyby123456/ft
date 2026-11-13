import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import Structure

PEERS = [
    Path('D:/ft工程文件/FT地牢_自然环境第一批第2组_20261104'),
    Path('D:/ft工程文件/FT地牢_首批第3组_冰原霜穴_20261104'),
    ROOT.parent / 'nature-S5-coral-ocean-20261104',
    ROOT.parent / 'nature-batch1-S6',
    Path('D:/ft工程文件/FT地牢_自然环境第7组_蘑菇真菌_20261104'),
    ROOT.parent / 'nature-b1-S8-crystal',
]


def mask(folder):
    metadata = json.loads((folder / 'room.json').read_text(encoding='utf-8-sig'))
    structure = Structure.read(folder / 'room.nbt')
    feet = metadata['entrance'][1]
    width, _, depth = structure.size
    blocked = {'air', 'water', 'lava', 'fire', 'soul_fire', 'cobweb', 'powder_snow', 'lily_pad', 'oak_leaves'}
    clear = {'air', 'cave_air', 'void_air'}
    cells = set()
    for sample_x in range(33):
        for sample_z in range(33):
            block_x = round(sample_x * (width - 1) / 32)
            block_z = round(sample_z * (depth - 1) / 32)
            name = lambda block_y: structure.get((block_x, block_y, block_z)).name.removeprefix('minecraft:')
            if name(feet) in clear and name(feet + 1) in clear and name(feet - 1) not in blocked:
                cells.add((sample_x, sample_z))
    return cells


def main():
    candidates = []
    unavailable = []
    for peer in PEERS:
        files = sorted((peer / 'incoming').glob('*/room.nbt'))
        if not files:
            unavailable.append(str(peer))
        for file in files:
            if (file.parent / 'room.json').is_file():
                candidates.append((file, mask(file.parent), hashlib.sha256(file.read_bytes()).hexdigest()))
    findings = []
    for number in (1, 4, 8):
        folder = ROOT / 'incoming' / f'NAT-B1-G4-{number:02}'
        actual = mask(folder)
        peers = []
        for file, original, sha256 in candidates:
            transforms = []
            current = original
            for rotation in range(4):
                transforms.extend([current, {(32 - block_x, block_z) for block_x, block_z in current}])
                current = {(32 - block_z, block_x) for block_x, block_z in current}
            overlap = max(len(actual & variant) / max(1, len(actual | variant)) for variant in transforms)
            peers.append({'nbt': str(file), 'sha256_at_audit': sha256, 'max_d4_jaccard': round(overlap, 6), 'exact_mask_duplicate': any(actual == variant for variant in transforms)})
        peers.sort(key=lambda entry: entry['max_d4_jaccard'], reverse=True)
        findings.append({'id': folder.name, 'normalized_dry_mask_sha256': hashlib.sha256(json.dumps(sorted(actual)).encode()).hexdigest(), 'exact_duplicate_count': sum(entry['exact_mask_duplicate'] for entry in peers), 'closest_snapshots': peers[:5], 'compared_modules': len(peers)})
    result = {'scope': 'Provisional 33x33 floor-plane dry-clearance mask comparison under rotations and reflections. Not semantic topology equivalence or final full-group audit. Peers may still be changing. Group1 unavailable in this snapshot.', 'group': 4, 'core_modules': findings, 'unavailable_peer_roots': unavailable, 'peer_snapshot_count': len(candidates), 'active_mechanisms': False, 'world_placement_verified': False}
    (ROOT / 'qa/differentiation-audit.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    assert not any(entry['exact_duplicate_count'] for entry in findings), 'Core dry-mask duplicate requires redesign'
    text = '''# 第4组独特设计与重复度审计输入

## 已实现的组级设计

- **核心拓扑**：不是中央十字、环礁环道或换材质空盒。01/04/08把大部分低层地面改成封闭浅水，3格宽连续木栈道按6个折点串联停留区；01/08在中途渡口加东向短支路，形成“曲折主干＋单侧支路”的宏观树形交通。北南不能沿中心轴干走直穿。每条声明中心线、所有门口都由最终NBT校验。
- **垂直层次**：底板y0、浅水y1、干路脚位y2；08另有可达的y3观察台，使用实际石砖楼梯，不是装饰假台阶；树冠在高层提供遮挡地标。入口脚位不因平台提高。
- **机关**：未实现主动机关、红石、锁门、计时洪水或战斗控制；唯一实际空间约束是水域切断直线捷径，引导连续转折。不得把这些预留描述为已实现玩法。
- **战斗节奏预留**：入口短安全段—首次偏移观察—中段渡口/分支—再次折返—出口平台；窄路和转角控制视线，供接入方配置遭遇与退路。当前没有怪物、波次、奖励或实战节奏测试。
- **地标**：01偏北高树岛、04侧向柱列和淹水中轴、08中部古树＋西侧抬高观察台。树岛与干路布局不同于01；09仍作为空箱祭坛终点。
- **调色板**：源石砖主体与实木承重，泥砖作台基，低层灰青浅水、睡莲及暖色蛙灯。新增湿地材质和源palette交集在每间room.json分开列出；差异不只靠配色。

## 原始输入与改造边界

`source-evidence.json` 保存实际解包NBT、尺寸、原始接口和功能语义；本版保留其尺度权威，明确改造其干路/淹水分区。原jigsaw/pool/processor未移植。本组配方独立编写，没有导入、复制或调用其它组配方。支持模块的直廊、90度转廊、北侧3×3接口属于通用契约，不把它们冒称唯一创新。

## 审计证据与限制

`qa/differentiation-audit.json` 对当时可读取的其它组实际room.nbt作33×33干燥净空掩码比较，覆盖旋转和镜像，列出最近候选及其当时SHA。这只是静态几何重复筛查，不是宏观语义图同构证明；不能据此宣布最终全组不重复。第1组未纳入本轮快照，其它组仍可能修改，统一接入侧必须按最终NBT哈希重新制作重复度矩阵。
'''
    (ROOT / 'DESIGN_IDENTITY.md').write_text(text, encoding='utf-8')
    print('Core mask audit: no exact duplicates; compared ' + str(len(candidates)) + ' peer snapshots')


if __name__ == '__main__':
    main()
