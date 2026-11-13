import copy
from pathlib import Path
import tempfile
import unittest
import zipfile
from unittest.mock import patch

import integrate as subject


class IntegrationTests(unittest.TestCase):
    def setUp(self):
        (subject.HOME / 'test-work').mkdir(exist_ok=True)
        self.temporary = tempfile.TemporaryDirectory(dir=subject.HOME / 'test-work')
        self.base = Path(self.temporary.name)
        self.addCleanup(self.temporary.cleanup)

    def component(self, name='core'):
        base = self.base / name
        subject.save(base / 'source.json', {'fixture': True})
        subject.save(base / 'ready.json', dict(component=name, apiVersion=1, status='READY', files={'source.json': subject.digest(base / 'source.json')}))
        return base

    def manifest(self):
        structures = self.base / 'structures'
        structures.mkdir()
        (structures / 'room.nbt').write_bytes(b'fixture-not-real-nbt')
        room = dict(id='room', resourceId='biomedungeon:room', accepted=True, independentAcceptance={'fixture': True}, sha256=subject.digest(structures / 'room.nbt'), size=dict(x=9, y=5, z=9), ports=[dict(id='door', feet=dict(x=4, y=1, z=0), facing='north', width=3, height=3, medium='dry')])
        return dict(schemaVersion=1, templates=[room]), structures

    def validate(self, value, structures):
        path = self.base / 'manifest.json'
        subject.save(path, value)
        return subject.validate_manifest(path, structures)

    def test_missing_ready_is_waiting(self):
        self.assertEqual('WAITING', subject.check_component(self.base, 'core')['status'])

    def test_file_existence_does_not_imply_ready(self):
        subject.save(self.base / 'source.json', {})
        self.assertEqual('WAITING', subject.check_component(self.base, 'core')['status'])

    def test_hashed_ready_passes(self):
        self.assertEqual('READY', subject.check_component(self.component(), 'core')['status'])

    def test_explicit_waiting_stays_waiting(self):
        base = self.component()
        ready = subject.load(base / 'ready.json')
        ready['status'] = 'WAITING'
        subject.save(base / 'ready.json', ready)
        self.assertEqual('WAITING', subject.check_component(base, 'core')['status'])

    def test_tampered_source_rejected(self):
        base = self.component()
        (base / 'source.json').write_bytes(b'tampered')
        with self.assertRaises(subject.GateError):
            subject.check_component(base, 'core')

    def test_api_mismatch_rejected(self):
        base = self.component()
        ready = subject.load(base / 'ready.json')
        ready['apiVersion'] = 2
        subject.save(base / 'ready.json', ready)
        with self.assertRaises(subject.GateError):
            subject.check_component(base, 'core')

    def test_inventory_required(self):
        with self.assertRaises(subject.GateError):
            subject.inventory(dict(status='READY'))

    def test_inventory_descriptor(self):
        checksum = 'a' * 64
        self.assertEqual({'src/a.java': checksum}, subject.inventory({'sources': [{'path': 'src/a.java', 'sha256': checksum}]}))

    def test_dependency_inventories_do_not_pollute_own_files(self):
        value = {'files': {'README.md': 'a' * 64}, 'dependencies': [{'files': {'README.md': 'b' * 64}}]}
        self.assertEqual({'README.md': 'a' * 64}, subject.inventory(value))

    def test_hashed_inventory_reference(self):
        base = self.component()
        subject.save(base / 'extra.json', {'extra': True})
        subject.save(base / 'SHA256SUMS.json', {'files': [{'path': 'extra.json', 'sha256': subject.digest(base / 'extra.json')}]})
        ready = subject.load(base / 'ready.json')
        ready.update(status='READY_MINIMAL_REAL_POOL', ready=True, inventory='SHA256SUMS.json', inventorySha256=subject.digest(base / 'SHA256SUMS.json'))
        subject.save(base / 'ready.json', ready)
        self.assertIn('extra.json', subject.check_component(base, 'core')['files'])
        subject.save(base / 'SHA256SUMS.json', {})
        with self.assertRaises(subject.GateError):
            subject.check_component(base, 'core')

    def test_jar_packaging_fault_injection(self):
        value, structures = self.manifest()
        project = self.base / 'project'
        manifest = project / 'src/main/resources/data/biomedungeon/manifest.json'
        subject.save(manifest, value)
        expected = 'data/biomedungeon/structures/room.nbt'
        for mode in ('valid', 'corrupt_nbt', 'extra_template', 'wrong_manifest', 'missing_api'):
            jar = self.base / (mode + '.jar')
            with zipfile.ZipFile(jar, 'w') as archive:
                archive.writestr('data/biomedungeon/manifest.json', b'{}' if mode == 'wrong_manifest' else manifest.read_bytes())
                archive.writestr(expected, b'corrupt' if mode == 'corrupt_nbt' else (structures / 'room.nbt').read_bytes())
                if mode == 'extra_template':
                    archive.writestr('data/biomedungeon/structures/unauthorized.nbt', b'extra')
                if mode != 'missing_api':
                    archive.writestr('cn/ft/biomedungeon/core/DungeonApi.class', b'fixture-not-bytecode')
            with self.subTest(mode=mode):
                if mode == 'valid':
                    self.assertEqual(1, subject.verify_jar(jar, project)['templates'])
                else:
                    with self.assertRaises(subject.GateError):
                        subject.verify_jar(jar, project)

    def test_path_escape_rejected(self):
        for relative in ('../runtime/source', str(self.base.resolve()), '..\\source'):
            with self.subTest(relative=relative), self.assertRaises(subject.GateError):
                subject.contained(self.base, relative)

    def test_snapshot_waiting_writes_nothing(self):
        target = self.base / 'staging'
        with self.assertRaises(subject.GateError):
            subject.snapshot([dict(status='WAITING')], target)
        self.assertFalse(target.exists())

    def test_snapshot_exact_hash_and_no_unlisted_files(self):
        base = self.component()
        (base / 'unlisted.txt').write_text('not frozen')
        checks = [subject.check_component(base, 'core')]
        with patch.object(subject, 'ROOT', self.base):
            target = subject.snapshot(checks, self.base / 'staging')
        self.assertEqual(subject.digest(base / 'source.json'), subject.digest(target / 'core/source.json'))
        self.assertFalse((target / 'core/unlisted.txt').exists())

    def test_snapshot_detects_change_after_probe(self):
        base = self.component()
        checks = [subject.check_component(base, 'core')]
        (base / 'source.json').write_bytes(b'changed after probe')
        with patch.object(subject, 'ROOT', self.base), self.assertRaises(subject.GateError):
            subject.snapshot(checks, self.base / 'staging')

    def test_valid_offline_manifest(self):
        value, structures = self.manifest()
        self.assertEqual(1, self.validate(value, structures))

    def test_frozen_acceptance_records(self):
        value, structures = self.manifest()
        record = self.base / 'acceptance.json'
        subject.save(record, {'fixture': 'not independent real acceptance'})
        evidence = dict(sourceSha256=value['templates'][0]['sha256'], unchangedBytes=True,
                        independentFileAudit=dict(snapshot='acceptance.json', sha256=subject.digest(record)),
                        acceptanceRecords=[dict(snapshot='acceptance.json', sha256=subject.digest(record))])
        value['templates'][0]['independentAcceptance'] = evidence
        path = self.base / 'manifest.json'
        subject.save(path, value)
        self.assertEqual(1, subject.validate_manifest(path, structures, self.base))
        subject.save(record, {'tampered': True})
        with self.assertRaises(subject.GateError):
            subject.validate_manifest(path, structures, self.base)

    def test_manifest_fault_injections(self):
        value, structures = self.manifest()
        mutations = {
            'empty': lambda item: item.update(templates=[]),
            'hash': lambda item: item['templates'][0].update(sha256='0' * 64),
            'escape': lambda item: item['templates'][0].update(id='../room'),
            'unaccepted': lambda item: item['templates'][0].update(accepted=False),
            'evidence': lambda item: item['templates'][0].pop('independentAcceptance'),
            'resource': lambda item: item['templates'][0].update(resourceId='file:room'),
            'size': lambda item: item['templates'][0]['size'].update(x=True),
            'medium': lambda item: item['templates'][0]['ports'][0].update(medium='lava'),
            'facing': lambda item: item['templates'][0]['ports'][0].update(facing='up'),
            'feet': lambda item: item['templates'][0]['ports'][0]['feet'].update(y=1.5),
            'width': lambda item: item['templates'][0]['ports'][0].update(width=0),
        }
        for label, mutate in mutations.items():
            candidate = copy.deepcopy(value)
            mutate(candidate)
            with self.subTest(label=label), self.assertRaises(subject.GateError):
                self.validate(candidate, structures)


if __name__ == '__main__':
    (subject.HOME / 'test-work').mkdir(exist_ok=True)
    unittest.main(verbosity=2)
