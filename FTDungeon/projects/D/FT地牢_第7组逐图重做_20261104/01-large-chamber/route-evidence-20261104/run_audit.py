import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess

EVIDENCE = Path(__file__).resolve().parent
SOURCE = EVIDENCE.parent
EXPECTED = '0cd7e686e01720603490127393db383a6fbae2894b62ed5bc1137d98d621f9f2'
JAVA = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
MAPPED = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
SUPPORT = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--run-dir', type=Path, required=True)
    args = parser.parse_args()
    run = args.run_dir.resolve()
    if run.exists():
        raise SystemExit('Refusing to overwrite an existing run directory; choose a new empty path.')
    template = SOURCE / 'incoming/S7R2-01/room.nbt'
    assert sha(template) == EXPECTED
    run.mkdir(parents=True)
    shutil.copyfile(template, run / 'room.nbt')
    shutil.copyfile(EVIDENCE / 'RouteCollisionAudit.java', run / 'RouteCollisionAudit.java')
    dependencies = [line for line in SUPPORT.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    entries = [str(MAPPED), *dependencies]
    assert all(Path(entry).is_file() for entry in entries)
    classpath = os.pathsep.join(entries)
    (run / 'classpath.txt').write_text(classpath, encoding='utf-8')
    write(run / 'classpath-files.json', [{'path': entry, 'sha256': sha(Path(entry))} for entry in entries])
    (run / 'classes').mkdir()
    compile_command = [str(JAVA / 'javac.exe'), '-encoding', 'UTF-8', '-cp', classpath, '-d', str(run / 'classes'), str(run / 'RouteCollisionAudit.java')]
    execute_command = [str(JAVA / 'java.exe'), '-Xmx1G', '-Djava.awt.headless=true', '-cp', str(run / 'classes') + os.pathsep + classpath, 'RouteCollisionAudit', str(run / 'room.nbt'), str(run / 'route-collision.json')]
    write(run / 'commands.json', {'cwd': str(run), 'compile': compile_command, 'run': execute_command, 'source_sha256': EXPECTED, 'audit_java_sha256': sha(run / 'RouteCollisionAudit.java')})
    for name, command in [('compile', compile_command), ('run', execute_command)]:
        completed = subprocess.run(command, cwd=run, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=240, creationflags=subprocess.CREATE_NO_WINDOW)
        (run / (name + '.log')).write_text(completed.stdout + completed.stderr, encoding='utf-8')
        print(completed.stdout[-2000:] + completed.stderr[-2000:], flush=True)
        if completed.returncode:
            raise SystemExit(completed.returncode)
    report = json.loads((run / 'route-collision.json').read_text(encoding='utf-8'))
    assert report['source_sha256'] == EXPECTED
    assert len(report['target_paths']) == 7
    assert len(report['left_stairs']) == len(report['right_stairs']) == 21
    assert len(report['six_stair_lane_paths']) == 6
    assert sha(template) == EXPECTED == sha(run / 'room.nbt')
    metadata = json.loads((SOURCE / 'incoming/S7R2-01/room.json').read_text(encoding='utf-8'))
    ports = []
    for connection in metadata['connections']:
        block_x, block_y, block_z = connection['feet']
        cells = [[block_x + lateral if connection['direction'] == 'north' else block_x, block_y + level,
                  block_z if connection['direction'] == 'north' else block_z + lateral] for lateral in [-1, 0, 1] for level in [0, 1, 2]]
        ports.append({**connection, 'guaranteed_clear_cells_3x3': cells, 'feet_world_center': [block_x + .5, block_y, block_z + .5],
                      'clearance_scope': '3 lateral block cells x 3 high, not implied clearance outside template; adjacency owner checks outside cells'})
    write(run / 'coordinate-evidence.json', {'source_sha256': EXPECTED, 'coordinates': 'integer block feet unless explicitly named center; +X east +Y up +Z south', 'ports': ports,
          'seven_targets_block_feet': metadata['test_targets'],
          'stair_runs': [{'id': name, 'ascending': [0, 0, -1], 'descending': [0, 0, 1], 'bottom_landing_center': [base + 1.5, 2, 27.5], 'top_landing_center': [base + 1.5, 9, 19.5],
                          'upper_gallery_check_center': [base + 1.5, 9, 18.5], 'all_lane_treads': report[key]} for name, base, key in [('west', 2, 'left_stairs'), ('east', 24, 'right_stairs')]],
          'complete_seven_paths': str(run / 'route-collision.json'), 'step_clearance': 'actual native 0.6 x 1.8 player AABB; exact swept raise/horizontal/lower segments for every edge; no jump, no flying; max step 0.5 < vanilla 0.6',
          'six_complete_stair_lane_paths': report['six_stair_lane_paths'],
          'registration': 'HOLD; supplemental producer evidence only; independent reviewer must reproduce'})
    preview = json.loads((SOURCE / 'qa/lodestone.json').read_text(encoding='utf-8'))
    media = []
    for entry in preview:
        target = Path(entry['displayed_nbt'])
        assert sha(target) == entry['displayed_nbt_sha256']
        assert entry['source_nbt_sha256'] == EXPECTED
        media.append({**entry, 'screenshot_sha256': sha(Path(entry['screenshot'])), 'explicit_label': 'LODESTONE_GPU_PREVIEW_NOT_GAMEPLAY'})
    for path in sorted((SOURCE / 'views').glob('*.png')):
        media.append({'path': str(path), 'sha256': sha(path), 'source_sha256': EXPECTED, 'explicit_label': 'SOFTWARE_BLOCK_RENDER_NOT_LODESTONE_NOT_GAMEPLAY',
                      'source_binding': 'prior verify.py render from source NBT; not re-rendered in this audit'})
    write(run / 'media-bindings.json', {'source_sha256': EXPECTED, 'derivative_record': json.loads((SOURCE / 'views/derivative.json').read_text(encoding='utf-8')), 'media': media})
    write(run / 'RESULT.json', {'status': 'SUPPLEMENTAL_OFFLINE_PASS_PENDING_INDEPENDENT_REVIEW', 'source_sha256': EXPECTED, 'source_unchanged': True,
          'test_scope': 'isolated copy only', 'commands': str(run / 'commands.json'), 'classpath': str(run / 'classpath.txt'), 'classpath_fingerprints': str(run / 'classpath-files.json'),
          'all_seven_full_routes': str(run / 'route-collision.json'), 'coordinates': str(run / 'coordinate-evidence.json'), 'media': str(run / 'media-bindings.json'),
          'world_placement': 'NOT_RUN', 'neighbor_updates': 'NOT_RUN', 'random_ticks': 'NOT_RUN', 'fluid_ticks': 'NOT_RUN', 'next_image_allowed': False, 'registration_accepted': False})
    print('SUPPLEMENT COMPLETE ' + str(run), flush=True)


if __name__ == '__main__':
    main()
