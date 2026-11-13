from pathlib import Path
import hashlib
import importlib.util
import json

root = Path(__file__).resolve().parents[1]
specification = importlib.util.spec_from_file_location('roomset_rebuild', root / 'tools/integrate-style-rebuild.py')
rebuild = importlib.util.module_from_spec(specification)
specification.loader.exec_module(rebuild)
work = rebuild.WORK
signatures = {}
inputs = {}
for style in rebuild.STYLES:
    inputs[style] = {}
    for key, variant in rebuild.VARIANTS.items():
        raw = (work / style / 'roomset' / (key + '.nbt')).read_bytes()
        data = rebuild.decode(raw)
        occupied = rebuild.occupancy(data)
        width, height, depth = variant['size']
        floor = 4 if key == 'spawn/spawn' else 1 if '/layout/' in key else 2
        excluded = set()
        for port in variant['ports']:
            for east, elevation, south in port['cells']:
                for x in range(east - 1, east + 2):
                    for y in range(elevation - 1, elevation + 5):
                        for z in range(south - 1, south + 2):
                            excluded.add((x, y, z))
        features = {point for point in occupied if 0 < point[0] < width - 1 and floor <= point[1] < height - 1 and 0 < point[2] < depth - 1 and point not in excluded}
        assert features, f'{style}/{key}: no interior architecture'
        signatures[style, key] = features
        inputs[style][key] = hashlib.sha256(raw).hexdigest()
pairs = []
issues = []
for key in rebuild.VARIANTS:
    for index, first in enumerate(rebuild.STYLES):
        for second in rebuild.STYLES[index + 1:]:
            before, after = signatures[first, key], signatures[second, key]
            intersection = len(before & after)
            union = len(before | after)
            similarity = intersection / union
            record = {'key': key, 'firstStyle': first, 'secondStyle': second, 'jaccard': round(similarity, 6), 'firstFeatureVoxels': len(before), 'secondFeatureVoxels': len(after), 'symmetricDifference': len(before ^ after)}
            record['fixedCirculationTemplate'] = key.startswith('stairs/')
            pairs.append(record)
            if similarity >= 0.9 and not record['fixedCirculationTemplate']:
                issues.append(record)
report = {'status': 'REVIEW_REQUIRED' if issues else 'PASS', 'method': 'Same-key pairwise interior solid-voxel Jaccard; external wall, roof, foundation and explicit port safety strips removed; material names ignored', 'pairedComparisons': len(rebuild.VARIANTS) * len(rebuild.STYLES) * (len(rebuild.STYLES) - 1) // 2, 'reviewThreshold': 0.9, 'highestPairs': sorted(pairs, key=lambda item: -item['jaccard'])[:30], 'ordinaryRoomIssues': issues, 'inputNbtSha256': inputs, 'limitations': ['Voxel similarity is a review trigger, not a complete visual style judgment', 'Authored staircase circulation may be shared to preserve fixed multi-level ports; staircase pairs remain listed for review']}
(work / 'cross-style-audit.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'status': report['status'], 'comparisons': report['pairedComparisons'], 'ordinaryRoomIssues': issues, 'highestPairs': report['highestPairs'][:8]}, ensure_ascii=False, indent=2))
