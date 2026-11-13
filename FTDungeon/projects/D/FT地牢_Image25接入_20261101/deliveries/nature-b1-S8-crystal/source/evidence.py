import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/结构参考_20261103')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import read_nbt, plain

SAMPLES = [
    ('rooms/junction_md', [19, 16, 19], '保留19格交汇房平面和16格高度；多出口；原创晶核替换中心装饰'),
    ('rooms/armoury_md', [13, 8, 13], '保留13x8x13尺寸；武器陈设改为晶体研究书架'),
    ('rooms/crypt_sm', [9, 8, 9], '7x8x7侧室扩为9x8x9以容纳三格接口；墓室改空箱晶簇密室'),
    ('rooms/prison_lg', [19, 8, 19], '保留19x8x19外包尺寸；监狱分格改双排承重柱大厅'),
    ('hallways/hallway_2', [9, 8, 21], '5x8x5走廊保留8格高度；横向加厚到9；纵向四段加端墙到21；双向通道'),
    ('hallways/hallway_turn', [15, 8, 15], '5x8x5转角平面放大3倍；保留8格高度与90度出口；非矩形L壳'),
    ('rooms/armoury_md', [13, 8, 13], '保留13x8x13；改入口守卫龛和休息凳；南北贯通'),
    ('rooms/junction_md', [19, 16, 19], '保留19x16x19交汇外包；中心屋顶开9x9天井；非楼层复制'),
    ('rooms/treasure_room_lg', [19, 16, 19], '保留19x16x19宝藏终点尺度；移除战利品机制，仅保留空箱台座'),
]


def main():
    records = []
    for identifier, output_size, adaptation in SAMPLES:
        original = REFERENCE / 'unpacked/better_strongholds/data/betterstrongholds/structures' / (identifier + '.nbt')
        details = REFERENCE / 'nbt-details/better_strongholds/betterstrongholds' / (identifier + '.json')
        detail = json.loads(details.read_text(encoding='utf-8'))
        template = plain(read_nbt(original)[1])
        assert template['size'] == detail['size']
        connectors = [record for record in template['blocks'] if record.get('nbt', {}).get('id') == 'minecraft:jigsaw']
        palette = template.get('palette') or template['palettes'][0]
        records.append({'template': detail['id'], 'nbt': str(original), 'details': str(details), 'sha256': hashlib.sha256(original.read_bytes()).hexdigest(), 'source_size': template['size'], 'output_size': output_size, 'source_connectors': connectors, 'source_palette': palette, 'adaptation': adaptation, 'palette_mapping': {'stone_bricks': 'deepslate_bricks', 'chiseled_stone_bricks': 'chiseled_deepslate', 'stone_brick_stairs': 'polished_deepslate_stairs', 'accent': 'calcite / amethyst_block / amethyst_cluster', 'lighting': 'sea_lantern'}, 'connector_adaptation': 'Read original directional jigsaw evidence; replace runtime jigsaws with metadata connections and dry 3x3 north entrance at feet y=1 under FT contract. No YUNG processor, pool, traps, entities or loot dependency copied.', 'geometry': 'original procedural construction; not redistributed source geometry'})
    target = ROOT / 'source/metadata/authority.json'
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(records, ensure_ascii=False, indent=2), encoding='utf-8')
    print('Read and verified', len(records), 'source NBT mappings')


if __name__ == '__main__':
    main()
