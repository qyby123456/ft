import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
ARCHIVE = Path('D:/ft工程文件/结构参考_20261103')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import Structure, plain

samples = [
    'lower_middle_chamber/lower_middle_chamber_1',
    'side_lower_chamber/side_lower_chamber_dripstone_left_0',
    'maze_chamber_battle_room/maze_chamber_battle_room_0',
    'parthenon/parthenon_0',
    'top_dome/top_dome_0',
]
records = []
for sample in samples:
    source = ARCHIVE / ('unpacked/better_ocean_monuments/data/betteroceanmonuments/structures/' + sample + '.nbt')
    structure = Structure.read(source)
    records.append(dict(id=sample, path=str(source), sha256=hashlib.sha256(source.read_bytes()).hexdigest(), size=list(structure.size), palette=sorted({block.name for block in structure.blocks.values()}), connectors=[dict(position=list(position), data=plain(data)) for position, data in structure.block_entities.items() if structure.blocks[position].name == 'minecraft:jigsaw']))
mapping = [(0,[19,11,19]),(1,[15,9,15]),(2,[11,7,17]),(3,[25,12,15]),(2,[11,8,17]),(2,[15,8,15]),(3,[17,10,15]),(4,[23,11,23]),(1,[17,10,17])]
modules = [dict(source=records[source_index]['path'], source_sha256=records[source_index]['sha256'], source_size=records[source_index]['size'], size=size, scale=[round(size[axis]/records[source_index]['size'][axis],4) for axis in range(3)]) for source_index,size in mapping]
report = dict(authority='Extracted source NBT geometry and connector evidence, not AI images', samples=records, modules=modules, summaries=[str(ARCHIVE / 'LEARNING_NOTES.md'),str(ARCHIVE / 'SAMPLE_CATALOG.md')], adaptations=['Original FT geometry, reduced non-uniformly to modular footprints from recorded source bounds; no direct source block copying.', 'Retain prismarine/dark-prismarine/sea-lantern palette and axial/column-hall/side-room/dome functional families.', 'YUNG jigsaw interfaces and processors are NOT shipped: map horizontal doors into FT north-centered dry 3x3 metadata ports.', 'No blue stained glass leg-processor markers, kelp jigsaw details, raw loot or mob insertion. Live-coral glass tanks replace processor-flooded interiors.', 'Source scale factors are design reductions, not a claim of geometrically equivalent scaled source templates.'])
(ROOT / 'source-evidence.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print('Decoded five authoritative source NBTs and recorded source hashes, connector data and dimensions.')
