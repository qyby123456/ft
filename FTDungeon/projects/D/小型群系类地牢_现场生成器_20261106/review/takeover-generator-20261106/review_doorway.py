import hashlib
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.dont_write_bytecode = True
sys.path.insert(0, str(ROOT / 'catalog'))
from nbtio import Structure, plain, state


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    base = ROOT / 'catalog/s8-07-doorway-review-v5'
    geometry = json.loads((base / 'geometry-review.json').read_text(encoding='utf-8'))
    original_path = Path(geometry['source'])
    derived_path = base / 'derived-candidate/data/biomedungeon/structures/s8_07_widearch_side_doublegate_v5.nbt'
    original = Structure.read(original_path)
    derived = Structure.read(derived_path)
    ledger = json.loads((base / 'derived-candidate/change-ledger.json').read_text(encoding='utf-8'))
    actual = {position for position in set(original.blocks) | set(derived.blocks)
              if original.get(position) != derived.get(position)}
    expected = {tuple(edit['position']) for edit in ledger['edits']}
    assert digest(original_path) == geometry['sourceSha256'] == ledger['originalSha256']
    assert digest(derived_path) == ledger['derivedSha256']
    assert original.size == derived.size and actual == expected and len(actual) == 20
    assert {position: plain(tag) for position, tag in original.block_entities.items()} == {
        position: plain(tag) for position, tag in derived.block_entities.items()}
    stairs = [position for position, block in original.blocks.items() if block.name.endswith('_stairs')]
    assert len(stairs) == 12 and all(original.get(position) == derived.get(position) for position in stairs)
    assert all(block == derived.get(position) for position, block in original.blocks.items() if position[1] <= 3)
    doors = [(position, block) for position, block in original.blocks.items() if block.name.endswith('_door')]
    assert len(doors) == 4
    assert all(derived.get((position[0] - 3, position[1], position[2])) == block for position, block in doors)
    preserved_be_count = len(original.block_entities)
    for template, label in ((original, 'original'), (derived, 'derived')):
        for position in list(template.blocks):
            if position[1] > 8 or position[2] > 8:
                template.set(position, state('minecraft:air'))
                template.block_entities.pop(position, None)
        template.write(Path(__file__).with_name(label + '-doorway-cutaway.nbt'))
    result = dict(status='INDEPENDENT_CELL_DELTA_PASS_NATIVE_ACCEPTANCE_PENDING',
                  originalSha256=digest(original_path), derivedSha256=digest(derived_path),
                  exactChangedCells=len(actual), preservedStairs=len(stairs), preservedBE=preserved_be_count,
                  originalFloorAndElevationPreserved=True, originalDoorHalvesRelocatedIntact=True,
                  accepted=False, productionPromotion=False,
                  designTradeoff='Central passage is an open arch. The relocated adjacent double door closes only the side passage.',
                  visualScope='Review-only cutaway copies remove y>8 and z>8; never used as gameplay source.')
    Path(__file__).with_name('doorway-delta-review.json').write_text(
        json.dumps(result, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(result))


if __name__ == '__main__':
    main()
