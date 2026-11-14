import json
import os
from pathlib import Path
import shutil
import stat
import zipfile
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/boss-archived-jar-cache-stage27-20261113'
CACHE=WORK/'cache/archived-jars-sha256-v1'
helper=WORK/'tools/finalize_boss_stage14_resources_v4.py';namespace={'__file__':str(helper)}
exec(helper.read_text(encoding='utf-8').split('parser = argparse.ArgumentParser()')[0],namespace)
load=namespace['load'];sha=namespace['sha'];save=namespace['save'];archive=namespace['archive'];processes=namespace['processes']
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noNewWeaponRuns'];audit=processes();assert not OUT.exists()
deployed=load(WORK/'releases/boss-core-stage27-erosion-damage-20261113/deployment-receipt.json');expected='6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0';assert sha(deployed['target'])==expected
forge=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');forge_sha='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3';assert sha(forge)==forge_sha
paths={};receipts={}
for receipt_path in sorted((WORK/'releases').glob('boss-core-stage*/archive-receipt.json')):
    receipt=load(receipt_path)
    if not receipt.get('allFilesReadBackAndHashVerified'):continue
    assert receipt['failures']==0 and sha(receipt['archive'])==receipt['archiveSha256'];receipts[str(receipt_path)]=sha(receipt_path)
    with zipfile.ZipFile(receipt['archive']) as packed:
        if 'ARCHIVE_MANIFEST.json' not in packed.namelist():continue
        manifest=json.loads(packed.read('ARCHIVE_MANIFEST.json'))
    for relative,row in manifest.items():
        if not relative.startswith('work/session-boss/build/') or not relative.endswith('.jar'):continue
        path=(ROOT/relative).resolve();assert path.is_relative_to((WORK/'build').resolve()) and path.drive==ROOT.drive and not path.is_symlink()
        if str(path) in paths:assert paths[str(path)]['sha256']==row['sha256'] and paths[str(path)]['bytes']==row['bytes']
        assert path.is_file() and sha(path)==row['sha256'] and path.stat().st_size==row['bytes'];paths[str(path)]=row
assert paths;OUT.mkdir();CACHE.mkdir(exist_ok=True);free_before=shutil.disk_usage(ROOT).free
save(OUT/'preflight.json',{'processes':audit,'paths':paths,'archiveReceiptHashes':receipts,'onlyClosedAlreadyArchivedBuildJarCopies':True,'allOriginalArchiveHashesVerified':True,'targetSha256':expected,'forgeSha256':forge_sha})
linked=0;canonical={}
for name,row in paths.items():
    path=Path(name);cached=CACHE/(row['sha256']+'.jar');assert sha(path)==row['sha256']
    if not cached.exists():os.link(path,cached)
    assert sha(cached)==row['sha256'] and cached.stat().st_size==row['bytes'];canonical[row['sha256']]=str(cached)
    if path.stat().st_ino!=cached.stat().st_ino:
        temporary=path.with_name(path.name+'.archived-cache-link.tmp');assert not temporary.exists();os.link(cached,temporary);assert sha(temporary)==row['sha256']
        readonly=not (path.stat().st_mode&stat.S_IWRITE)
        if readonly:assert path.stat().st_nlink==1;os.chmod(path,stat.S_IREAD|stat.S_IWRITE)
        try:os.replace(temporary,path)
        except BaseException:
            if readonly and path.exists():os.chmod(path,stat.S_IREAD)
            raise
        linked+=1
    assert path.stat().st_ino==cached.stat().st_ino and sha(path)==row['sha256'];os.chmod(cached,stat.S_IREAD)
    with (OUT/'journal.jsonl').open('a',encoding='utf-8') as log:log.write(json.dumps({'path':str(path),'cache':str(cached),'sha256Unchanged':row['sha256']},ensure_ascii=False)+chr(10))
for name,row in paths.items():assert sha(name)==row['sha256'] and not (Path(name).stat().st_mode&stat.S_IWRITE)
assert sha(deployed['target'])==expected and sha(forge)==forge_sha
result={'localPathsRetained':len(paths),'uniqueImmutableJars':len(canonical),'duplicateInodesReplacedByIdenticalHardlinks':linked,'freeBytesBefore':free_before,'freeBytesAfter':shutil.disk_usage(ROOT).free,'everyContentHashAndPathPreserved':True,'worldsSourcesFormalProductWeaponAndHistoricalDUntouched':True,'restorationAvailableInVerifiedOriginalMainArchives':True,'fullBossGoalStatus':'active','weaponsPaused':True}
save(OUT/'dedup-receipt.json',result)
creator=WORK/'tools/prepare_boss_blood_tide_stage10.py';creation={'__file__':str(creator)};exec(creator.read_text(encoding='utf-8').split('probe=(WORK/')[0],creation)
creation['create'](OUT/'RESTORE.md',chr(10).join(['# Closed Boss native build JAR cache','','Only JAR paths under work/session-boss/build,already present in fully readback/hash verified closed Boss main archives,were atomically linked to identical local immutable content. Every original path,size and SHA256 retained. No worlds,source,formal product,weapon archive/workspace or historical D changed.','Cache and aliases are read-only. Never edit any archived/shared JAR in place. Future native runs must create independent copies in new attempt directories. To recover writable bytes,extract the exact path from its original verified main archive into a new file.','Archive receipts,all original per-file hashes,the atomic-link journal and final hashes are included. Full Boss migration remains active;weapons remain paused.']))
packed=archive(WORK/'archives/boss-archived-jar-cache-stage27-20261113.zip',{Path(__file__),helper,OUT/'preflight.json',OUT/'journal.jsonl',OUT/'dedup-receipt.json',OUT/'RESTORE.md'});save(OUT/'archive-receipt.json',packed);processes();print(json.dumps({**result,'archiveSha256':packed['archiveSha256']}),flush=True)
