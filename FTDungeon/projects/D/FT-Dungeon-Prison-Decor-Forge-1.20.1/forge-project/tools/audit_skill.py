import argparse
import hashlib
import json
import shutil
import sys
import zipfile
from pathlib import Path

from export_kit import RESOURCES, ROOT, write_json


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--minecraft-jar', type=Path, required=True)
    parser.add_argument('--skill-scripts', type=Path, required=True)
    parser.add_argument('--stage', type=Path, required=True)
    arguments = parser.parse_args()
    sys.path.insert(0, str(arguments.skill_scripts))
    from audit_model import audit

    stage = arguments.stage.resolve()
    if stage == ROOT or ROOT in stage.parents:
        raise ValueError('Stage outside deliverable required')
    shutil.copytree(RESOURCES / 'assets', stage, dirs_exist_ok=True)
    with zipfile.ZipFile(arguments.minecraft_jar) as archive:
        pending = ['minecraft:block/block', 'minecraft:block/stairs', 'minecraft:block/inner_stairs',
                   'minecraft:block/outer_stairs', 'minecraft:block/slab', 'minecraft:block/slab_top',
                   'minecraft:block/cube_bottom_top']
        visited = set()
        while pending:
            reference = pending.pop()
            if reference in visited:
                continue
            visited.add(reference)
            namespace, location = reference.split(':', 1)
            source_name = f'assets/{namespace}/models/{location}.json'
            data = archive.read(source_name)
            destination = stage / namespace / 'models' / (location + '.json')
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_bytes(data)
            parent = json.loads(data).get('parent')
            if parent and parent != 'builtin/generated' and parent != 'builtin/entity':
                pending.append(parent if ':' in parent else 'minecraft:' + parent)
    paths = sorted((ROOT / 'model_sources').glob('*.bbmodel'))
    paths += sorted((RESOURCES / 'assets/ftdungeonprops/models').rglob('*.json'))
    reports = [audit(path, profile='generic', assets=stage) for path in paths]
    if len(reports) != 83 or any(report['errors'] or report['warnings'] for report in reports):
        raise AssertionError(json.dumps([report for report in reports if report['errors'] or report['warnings']], ensure_ascii=False))
    write_json(ROOT / 'qa/skill-audit.json', {'profile': 'generic',
               'version_authority': 'Forge 47.4.10 native verifier, not the generic skill profile',
               'input_sha256': {path.relative_to(ROOT).as_posix(): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths},
               'reports': reports})
    print(json.dumps({'reports': len(reports), 'errors': 0, 'warnings': 0}))


if __name__ == '__main__':
    main()
