import datetime
import json
import os
from pathlib import Path
import shutil
import stat
import zipfile
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/boss-common-mod-cache-dedup-stage27-20261113'
CACHE=WORK/'cache/common-mods-sha256-v1'
helper=WORK/'tools/finalize_boss_stage14_resources_v4.py';namespace={'__file__':str(helper)}
exec(helper.read_text(encoding='utf-8').split('parser = argparse.ArgumentParser()')[0],namespace)
load=namespace['load'];sha=namespace['sha'];save=namespace['save'];archive=namespace['archive'];processes=namespace['processes']
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noNewWeaponRuns'];audit=processes();assert not OUT.exists()
release=WORK/'releases/boss-core-stage27-erosion-damage-20261113';deployed=load(release/'deployment-receipt.json');target=Path(deployed['target']);expected='6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0';assert sha(target)==deployed['deployedSha256']==expected
forge=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');forge_sha='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3';assert sha(forge)==forge_sha
paths={};receipts={}
for receipt_path in [release/'archive-receipt.json',WORK/'releases/boss-core-stage25-arcane-lifetime-20261113/archive-receipt.json',WORK/'reports/boss-stage24-natural-phantom-timeline-20261113/archive-receipt.json']:
    receipt=load(receipt_path);assert receipt['failures']==0 and receipt['allFilesReadBackAndHashVerified'] and sha(receipt['archive'])==receipt['archiveSha256'];receipts[str(receipt_path)]=sha(receipt_path)
    with zipfile.ZipFile(receipt['archive']) as packed:manifest=json.loads(packed.read('ARCHIVE_MANIFEST.json'))
    for relative,row in manifest.items():
        path=(ROOT/relative).resolve();cached=CACHE/(row['sha256']+'.jar')
        if path.parent.name!='mods' or path.parent.parent.name not in ['game','server'] or path.suffix!='.jar' or not cached.is_file():continue
        assert path.is_relative_to((WORK/'build').resolve()) and path.drive==ROOT.drive and not path.is_symlink();assert path.is_file() and sha(path)==sha(cached)==row['sha256'] and path.stat().st_size==row['bytes'];paths[str(path)]={**row,'cache':str(cached)}
assert paths;OUT.mkdir();free_before=shutil.disk_usage(ROOT).free
save(OUT/'preflight.json',{'processes':audit,'paths':paths,'archiveReceiptHashes':receipts,'onlyAlreadyArchivedCommonModCopies':True,'targetSha256':expected,'forgeSha256':forge_sha})
linked=0
for name,row in paths.items():
    path=Path(name);cached=Path(row['cache']);assert sha(path)==sha(cached)==row['sha256']
    if path.stat().st_ino!=cached.stat().st_ino:
        temporary=path.with_name(path.name+'.stage27-cache-link.tmp');assert not temporary.exists();os.link(cached,temporary);assert sha(temporary)==row['sha256']
        readonly=not (path.stat().st_mode&stat.S_IWRITE)
        if readonly:assert path.stat().st_nlink==1;os.chmod(path,stat.S_IREAD|stat.S_IWRITE)
        try:os.replace(temporary,path)
        except BaseException:
            if readonly and path.exists():os.chmod(path,stat.S_IREAD)
            raise
        linked+=1
    assert path.stat().st_ino==cached.stat().st_ino and sha(path)==row['sha256'];os.chmod(cached,stat.S_IREAD)
    with (OUT/'journal.jsonl').open('a',encoding='utf-8') as log:log.write(json.dumps({'path':str(path),'cache':str(cached),'contentSha256Unchanged':row['sha256']},ensure_ascii=False)+chr(10))
for name,row in paths.items():assert sha(name)==row['sha256'] and not (Path(name).stat().st_mode&stat.S_IWRITE)
assert sha(target)==expected and sha(forge)==forge_sha
result={'localPathsRetained':len(paths),'duplicateInodesReplacedByIdenticalImmutableHardlinks':linked,'freeBytesBefore':free_before,'freeBytesAfter':shutil.disk_usage(ROOT).free,'everyContentHashAndPathPreserved':True,'worldsSourcesProductAndHistoricalDUntouched':True,'fullBossGoalStatus':'active','weaponsPaused':True}
save(OUT/'dedup-receipt.json',result);packed=archive(WORK/'archives/boss-common-mod-cache-dedup-stage27-20261113.zip',{Path(__file__),helper,OUT/'preflight.json',OUT/'journal.jsonl',OUT/'dedup-receipt.json'});save(OUT/'archive-receipt.json',packed);processes();print(json.dumps({**result,'archiveSha256':packed['archiveSha256']}),flush=True)
