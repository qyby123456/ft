import hashlib
import json
import os
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LIBRARY = Path('D:/ft工程文件/结构参考_20261103')
sys.path.insert(0, str(Path(os.environ['APPDATA']) / '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure, plain


def digest(content):
    return hashlib.sha256(content).hexdigest()


def record(path):
    content = path.read_bytes()
    return {'path': str(path.resolve()), 'sha256': digest(content), 'bytes': len(content)}


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    jar = next(Path('D:/ft工程文件').glob('*YungsBetterStrongholds-1.20-Forge-4.0.3.jar'))
    jar_record = record(jar)
    assert jar_record['sha256'] == '054b0a1be86510ed6bd05de66a29200f13f5603c0c9eb7e62074ddc3d19cb0e4'
    sources = ['rooms/junction_md', 'rooms/armoury_md', 'rooms/crypt_sm', 'rooms/mess', 'hallways/hallway_2', 'hallways/hallway_turn', 'hall_doorways/hall_doorway', 'starts/junction_lg', 'rooms/treasure_room_lg']
    template_root = LIBRARY / 'unpacked/better_strongholds/data/betterstrongholds/structures'
    templates = {}
    with zipfile.ZipFile(jar) as archive:
        for identifier in sources:
            template = template_root / (identifier + '.nbt')
            archive_entry = 'data/betterstrongholds/structures/' + identifier + '.nbt'
            assert archive.read(archive_entry) == template.read_bytes(), 'Unpacked NBT differs from original archive'
            structure = Structure.read(template)
            details = LIBRARY / ('nbt-details/better_strongholds/betterstrongholds/' + identifier + '.json')
            summary = json.loads(details.read_text(encoding='utf-8'))
            assert tuple(summary['size']) == structure.size
            connectors = []
            for point, entity in structure.block_entities.items():
                encoded = plain(entity)
                if encoded.get('id') == 'minecraft:jigsaw':
                    connectors.append({'pos': list(point), 'state': dict(structure.get(point).properties), 'nbt': encoded})
            assert len(connectors) == len(summary['connectors'])
            templates[identifier] = {**record(template), 'archive_entry': archive_entry, 'archive_matches': True, 'size': list(structure.size), 'records': len(structure.blocks), 'connectors': connectors, 'palette': sorted({state.name for state in structure.blocks.values()}), 'details': record(details)}
    documents = [LIBRARY / name for name in ['AGENTS.md', 'README.md', 'LEARNING_NOTES.md', 'SAMPLE_CATALOG.md', 'EVIDENCE.md']]
    documents += [LIBRARY / ('unpacked/better_strongholds/data/betterstrongholds/worldgen/' + name) for name in ['structure/stronghold.json', 'template_pool/starts.json', 'template_pool/hallway.json', 'template_pool/hall_doorway.json', 'template_pool/terminators.json', 'processor_list/main.json']]
    documents += [LIBRARY / 'decompiled/better_strongholds/com/yungnickyoung/minecraft/betterstrongholds/world/processor/AirProcessor.java']
    records = [record(document) for document in documents]
    junction = templates['rooms/junction_md']['size']
    armoury = templates['rooms/armoury_md']['size']
    crypt = templates['rooms/crypt_sm']['size']
    mess = templates['rooms/mess']['size']
    hall = templates['hallways/hallway_2']['size']
    turn = templates['hallways/hallway_turn']['size']
    start = templates['starts/junction_lg']['size']
    treasure = templates['rooms/treasure_room_lg']['size']
    parameters = {
        '01': {'source': 'rooms/junction_md', 'size': junction, 'rule': 'Keep measured 19x16x19 traffic-junction envelope; FT entrance/ports are normalized, not original YUNG jigsaws.'},
        '02': {'source': 'rooms/armoury_md', 'size': [armoury[0] + 2, armoury[1] + 2, armoury[2] + 2], 'rule': 'Add two cells per dimension to the measured 13x8x13 medium envelope for plant recesses and floor/roof separation.'},
        '03': {'source': 'rooms/crypt_sm', 'size': [crypt[0] + 2, crypt[1] + 2, crypt[2] + 2], 'rule': 'Expand the measured small dead-end 7x8x7 by two per dimension; explicitly add an optional FT side connection.'},
        '04': {'source': 'rooms/mess', 'size': [mess[2] + 2, mess[1] + 4, mess[0] + 4], 'rule': 'Rotate the measured 13x8x19 communal hall footprint, add side aisles and overhead beam clearance; four FT exits are an adaptation.'},
        '05': {'source': 'hallways/hallway_2', 'size': [hall[0] + 4, hall[1] + 2, hall[2] * 4 + 3], 'rule': 'Repeat four measured 5-block corridor units, add three joint cells and four cells for two root/pond wall recesses.'},
        '06': {'source': 'hallways/hallway_turn', 'size': [turn[0] * 3, turn[1] + 2, turn[2] * 3], 'rule': 'Triple the measured 5x5 corner module footprint for 3-wide dry passage and distinct corner/root bay; normalize turn to north/east.'},
        '07': {'source': 'rooms/armoury_md', 'secondary_source': 'hall_doorways/hall_doorway', 'size': [armoury[0] + 6, armoury[1] + 2, armoury[2]], 'rule': 'Use measured medium envelope plus six horizontal cells for two guard bays; separate doorway from room semantics as the source pools do.'},
        '08': {'source': 'starts/junction_lg', 'size': [start[0], start[1] // 2 + 2, start[2]], 'rule': 'Keep measured 31x31 start-junction footprint, reduce multilevel envelope to one storey; add explicitly declared courtyard skylight instead of claiming source has one.'},
        '09': {'source': 'rooms/treasure_room_lg', 'size': treasure, 'rule': 'Keep measured 19x16x19 large treasure envelope, remove encounter/loot runtime; add one FT north entrance and empty container.'},
    }
    manifest = {'source_jar': jar_record, 'templates': templates, 'documents': records, 'authority': 'Original unpacked NBT plus structure/pool/processor data and cited analysis; image references are secondary theme references only.', 'runtime': 'Source uses yungsapi:yung_jigsaw and custom processors. Output is original procedural vanilla geometry with FT-compatible ports, NOT drop-in YUNG jigsaw content and NOT a copy of source NBT geometry.', 'excluded': ['No source entities, spawners, command blocks, jigsaws, loot tables, processor placeholders, TNT or infested blocks are transferred.', 'Red stained glass in source main processor can become TNT; do not treat placeholder colors as decorative palette.', 'Pool weights, priorities, counts and depth constraints are recorded as research only; no claim that FT generation executes YUNG rules.'], 'parameters': parameters}
    manifest['input_fingerprint'] = digest(json.dumps(manifest, sort_keys=True, ensure_ascii=False).encode('utf-8'))
    save(ROOT / 'evidence/input-manifest.json', manifest)
    save(ROOT / 'evidence/parameters.json', parameters)
    lines = ['# 第一批第一组：权威输入与适配边界', '', '先读本清单，再执行 source/build.mjs。旧的纯图片候选已弃用；后续构建必须校验本清单内所有文件哈希。', '', '## 原始证据', '', f'- 源 JAR：`{jar}`', f'- 源 JAR SHA-256：`{jar_record["sha256"]}`', '- 9 个原始 NBT 已逐字节对照源 JAR；实际坐标、尺寸、调色板和 Jigsaw 标签从 NBT 解码，非图片推断。', '- 这是基于真实尺度和模块语义的原创程序化适配，不是原建筑完整复刻，不复制原始方块布局。', '', '## 模块划分（当前聊天独占这 9 个 ID）', '', '| ID | 真实输入 | 原始尺寸 | 输出尺寸 | 明确适配 |', '|---|---|---|---|---|']
    for number, parameter in parameters.items():
        lines.append(f'| nature-b1-s1-{number} | betterstrongholds:{parameter["source"]} | {templates[parameter["source"]]["size"]} | {parameter["size"]} | {parameter["rule"]} |')
    lines += ['', '## 查阅路径与 SHA-256', '']
    for entry in records:
        lines.append(f'- `{entry["path"]}` — `{entry["sha256"]}`')
    lines += ['', '## 兼容性与连接', '', '- 源接口的 name/target/pool/orientation/joint/final_state 原样记录于 input-manifest.json，不能按名称忽略方向、碰撞、深度及处理器。', '- 输出不包含 Jigsaw，以 room.json 的 3×3 北入口、相同 feetY=2 和相反方向端口作 FT 接口；这是显式转换规则，不宣称原模组接口直接兼容。', '- 苔石/裂石砖调色依据源 main.json 的石砖替换规则；木根、持久树叶、荧光藤及封闭水池是用户森林主题覆盖，不伪称原件原有。', '- 不执行解包模组类、函数或 worldgen，不修改原 JAR；不生成付费图片，不写正式存档。', '- 统一接入、碰撞拒绝、幂等、大厅接通及多人游玩仍需接收方验证。', '']
    (ROOT / 'evidence/INPUTS.md').write_text('\n'.join(lines), encoding='utf-8')
    print('AUDITED_INPUTS', len(templates), 'NBT', len(records), 'documents', manifest['input_fingerprint'])


if __name__ == '__main__':
    main()
