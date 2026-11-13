from datetime import datetime, timedelta, timezone
from pathlib import Path

from intake_corridors import (INSTANCE, REVIEWER, ROOT, digest, io_path, load, receiver_acceptance,
                              record, require, resolved, save, static_check, verify_file)


REVIEWED = {
    'corridor-M09': {
        'nbt': '5a776d5e6a96b0497aaf0411e289cebbc5d124a0b5b76b1e27897a0fd379abec',
        'bundle': '38f99b7d1d0eb23f9a491349a2e9ec3b4f3f270c9e093e92ab39b99326e1d2f0',
        'render': 'c051d22c2364e3202b8754ff4d29644b7dc2ebd4c519f558ebc2ad392fe6dd98',
        'native': 'dcdcd511ddef5c33bf5d70891c0eb7d2b2a6d3f6b79f879280287a44ee50d6f6',
        'observations': [
            'North interior: gold-banded crying-obsidian piers, repeated stepped ribs and warm candles retained; central route remains level and straight.',
            'South interior: both terminal arches open; rubble and broken rib shoulders stay outside the central three-wide aisle.',
            'West interior: side-wall light seams, supported candle ledges and six rib stations remain distinct, with no side branch.',
            'East interior: reverse side repeats the same straight route; deliberate missing shoulder fragments do not perforate the sealed shell.',
            'North exterior: centered portal and complete roof/foundation visible; outer side panels remain closed.',
            'South exterior: opposite connector remains open at matching height, with no unintended extra doorway.',
            'West exterior: uninterrupted outer wall and roof across the full 41-block length.',
            'East exterior: continuous opposite wall and roof; no opening caused by the display cutaways.',
        ],
        'adaptations': ['Button relief on gold replaces illustrated glyph artwork.', 'Glass and shroomlight replace hazardous concept lava.',
                        'The software renderer makes gold and crying obsidian brighter than the concept image; no shader or client lighting claim.'],
    },
    'corridor-S06': {
        'nbt': 'e63afe337dfafa98704f68c643bfac7b19c3f97a78667aa224c7a195f95fc3f6',
        'bundle': '5b60fa23067d729e5caab6c8d952363d17eb2efc064c462cad995e25f736480a',
        'render': '01703fe294f507baa09d7cb5d09ff8c25e55b432fa90f48e5f5585828e5ebf64',
        'native': 'e15b20985a44fb0890e20e60e5219e55ddbdba86163305a6b7900d02a19bddfd',
        'observations': [
            'North interior: intact stone ribs, supported soul lanterns, moss and vines match the damp-stone concept; dry center path continues to the south opening.',
            'South interior: reverse route is straight and step-free; side basins remain separate from the center foot path.',
            'West interior: shallow water stays in the side channel; posts, chain lanterns and attached vines do not obstruct the route.',
            'East interior: opposite basin and arch rhythm preserved; no additional side passage or stair on the route.',
            'North exterior: sealed roof and full foundation with a centered north connector.',
            'South exterior: opposite connector open at the same foot level, with water contained behind the end margins.',
            'West exterior: continuous mossy stone side wall and roof; interior preview cutaway is display-only.',
            'East exterior: closed opposite side wall and complete roof; no external water stream visible.',
        ],
        'adaptations': ['A dry three-block center causeway replaces the concept flooded walking surface.',
                        'The concept far-end wall is an open south arch to make a genuine through corridor.',
                        'Software lighting is brighter than the concept; neither the rendering nor this review represents modpack client lighting.'],
    },
}


def main():
    import sys
    sys.path.insert(0, str(INSTANCE / 'output/dungeon-expansion-20261101/tools'))
    from nbtio import Structure

    registry = load(load(ROOT / 'latest.json')['registry']['path'])
    for identifier, reviewed in REVIEWED.items():
        entry = next(value for value in registry['corridors'] if value['id'] == identifier)
        require(entry['candidate_sha256'] == reviewed['nbt'] and entry['bundle_sha256'] == reviewed['bundle'], 'Candidate changed after visual review')
        directory = ROOT / 'reviews' / identifier / reviewed['nbt'] / reviewed['bundle']
        work = ROOT / 'work' / (identifier.removeprefix('corridor-').lower() + '-' + reviewed['bundle'][:12])
        require(resolved(work) == resolved(directory), 'Work alias does not point to this review')
        render = load(work / 'renders/latest.json')['manifest']
        native = load(work / 'native/latest.json')['result']
        require(render['sha256'] == reviewed['render'] and native['sha256'] == reviewed['native'], 'Reviewed render/native evidence changed')
        verify_file(render, directory)
        native_result = load(verify_file(native, directory))
        preparations = load(work / 'preparation.json')
        for information in preparations['copied_files']:
            require(digest(information['path']) == information['sha256'], 'Producer/source modified during receiver testing')
        for name, information in entry['files'].items():
            require(digest(information['source']) == information['sha256'], 'Producer delivery changed after freeze')
        original = Structure.read(Path(entry['snapshot']) / 'room.nbt')
        captures = []
        for information in native_result['native_reports']:
            report_path = verify_file(information, directory)
            report = load(report_path)
            capture_path = report_path.parent / ('native-placed-' + report['requested_rotation'].lower() + '.nbt')
            capture = Structure.read(capture_path)
            require(capture.size == original.size and capture.blocks == original.blocks and not capture.entities and not capture.block_entities,
                    'Native capture changed from frozen final structure')
            captures.append({**record(capture_path), 'rotation': report['requested_rotation']})
        static = static_check(Path(entry['snapshot']), entry, entry['reference'])
        save(work / 'receiver-static.json', {**{key: entry[key] for key in ('id', 'candidate_sha256', 'bundle_sha256')}, **static})
        decision = {**{key: entry[key] for key in ('id', 'candidate_sha256', 'bundle_sha256')},
                    'reference_sha256': entry['reference']['sha256'], 'reviewer_thread': REVIEWER,
                    'reviewed_at': datetime.now(timezone(timedelta(hours=8))).isoformat(), 'visual_review_complete': True,
                    'four_view_status': 'PASS', 'retest_status': 'PASS', 'blockers': [], 'observations': reviewed['observations'],
                    'intentional_differences': reviewed['adaptations'], 'render_manifest': render, 'native_result': native,
                    'native_captures': captures, 'receiver_static': record(work / 'receiver-static.json'),
                    'preparation': record(work / 'preparation.json'), 'input_nbt': entry['files']['room.nbt'],
                    'acceptance_scope': 'Approved for isolated candidate integration only; no runtime catalog, hall route, vertical interface, occupancy refusal or idempotence integration accepted.',
                    'deployed': False, 'production_saves_modified': False, 'client_gameplay_verified': False}
        target = directory / 'acceptance.json'
        require(not io_path(target).exists(), 'Acceptance already exists; do not overwrite a prior review')
        save(target, decision)
        validated = receiver_acceptance(entry)
        require(validated is not None and validated['native_tests'] == 4, 'Receiver acceptance evidence failed validation')
        print('ACCEPTED', identifier, entry['slot'], str(target), flush=True)


if __name__ == '__main__':
    main()
