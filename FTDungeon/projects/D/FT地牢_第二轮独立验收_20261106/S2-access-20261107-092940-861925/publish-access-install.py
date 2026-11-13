import hashlib
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parent
OUTPUT = ROOT / 'access-install-33ceb33f'
RUNTIME = ROOT / 'runtime-access-r4'


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def record(path):
    return {'path':str(path),'relative_path':path.relative_to(ROOT).as_posix(),'sha256':digest(path),'bytes':path.stat().st_size}


def main():
    source = ROOT / 'assembly/s2-desert-functional-combined.nbt'
    source_hash = digest(source)
    assert source_hash == '33ceb33f633cfa6e797cc184cd3c5ff34ce9f0901e9b909da161782ec5cc0e6c'
    originals = read(ROOT / 'artifact-manifest.json')['artifacts']
    supplement = read(ROOT / 'qa/interaction-range-current/supplement-manifest.json')['supplement_artifacts']
    for entry in originals + supplement:
        path=Path(entry['path'])
        assert digest(path)==entry['sha256'] and path.stat().st_size==entry['bytes'],path
    archive=OUTPUT/'s2_desert_access.zip'
    with zipfile.ZipFile(archive) as bundle:
        assert hashlib.sha256(bundle.read('data/s2access/structures/desert_33ceb33f.nbt')).hexdigest()==source_hash
        for relative in ['data/minecraft/tags/functions/load.json','data/minecraft/tags/functions/tick.json']:
            assert json.loads(bundle.read(relative))['values']
    assert digest(archive)==digest(RUNTIME/'server/s2-new-isolated-qa-world/datapacks/s2_desert_access.zip')
    report=read(RUNTIME/'access-runtime.json')
    assert report['pass'] and report['source_sha256']==source_hash and report['roundtrips']==2
    assert report['automatic_load_count']>=1 and report['natural_tick_before_install']>0 and report['natural_tick_after']>report['natural_tick_before_install']
    assert report['barriers_preserved']==38872 and report['installed_records_checked']==166642 and report['block_identity_failures']==0
    assert report['ordinary_permission_level']==0 and not report['ordinary_has_permission_2'] and report['ordinary_install_denied'] and report['repeat_install_no_clear_or_replace']
    assert read(RUNTIME/'exit.json')['exit_code']==0
    validation={'style':'S2','group':'S2','source_sha256':source_hash,'status':'PRODUCER_INSTALL_VALIDATED_PENDING_INDEPENDENT_REVIEW','archive':record(archive),'runtime_report':record(RUNTIME/'access-runtime.json'),'original_declared_files_unchanged':len(originals),'previous_interaction_supplement_files_unchanged':len(supplement),'barriers_preserved':report['barriers_preserved'],'ordinary_permission_level':0,'natural_load_tick':True,'ordinary_trigger_roundtrips':2,'explicit_admin_install_required':True,'automatic_load_does_not_place_or_clear':True,'full_range_clear_documented':True,'production_instance_touched':False,'actual_player_implementation':report['test_player_enrollment'],'scope_correction':'Report scope string retains legacy FakePlayer wording; actual r4 source and test_player_enrollment use native ServerPlayer and native teleport listener, with unattached Connection and no real client. No manual load/tick or player movement after initial setup. Test-only reflection enrolls player for vanilla selectors; not shipped in datapack. Producer isolated test, not parent independent world rerun or human/client survival acceptance.'}
    (OUTPUT/'validation.json').write_text(json.dumps(validation,ensure_ascii=False,indent=2),encoding='utf-8')
    files=[ROOT/name for name in ['build-access-pack.py','prepare-access-runtime.py','publish-access-install.py']]
    files.extend(path for path in OUTPUT.rglob('*') if path.is_file() and path.name!='evidence-manifest.json')
    files.extend(path for path in RUNTIME.iterdir() if path.is_file())
    files.extend(path for path in (RUNTIME/'native-test').iterdir() if path.is_file())
    files.extend(path for path in (RUNTIME/'native-test/src').rglob('*') if path.is_file())
    files.append(RUNTIME/'server/server.properties')
    for name in ['runtime-access-r1','runtime-access-r2','runtime-access-r3']:
        failed=ROOT/name
        files.extend(path for path in failed.iterdir() if path.is_file())
        files.extend(path for path in (failed/'native-test/src').rglob('*') if path.is_file())
    (OUTPUT/'evidence-manifest.json').write_text(json.dumps({'source_sha256':source_hash,'artifacts':[record(path) for path in sorted(set(files))],'excluded':['manifest self hash','server chunks, build outputs and Gradle caches'],'status':'PENDING_INDEPENDENT_REVIEW'},ensure_ascii=False,indent=2),encoding='utf-8')
    print('ACCESS_INSTALL_PUBLISHED',source_hash,'files',len(set(files)))


if __name__=='__main__':
    main()
