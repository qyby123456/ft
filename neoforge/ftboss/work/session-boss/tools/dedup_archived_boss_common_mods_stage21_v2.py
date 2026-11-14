import datetime
import json
import os
import shutil
import stat
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/boss-common-mod-cache-dedup-stage21-20261113'
CACHE=WORK/'cache/common-mods-sha256-v1'
helper=WORK/'tools/finalize_boss_stage14_resources_v4.py';functions={'__file__':str(helper)}
exec(helper.read_text(encoding='utf-8').split('parser = argparse.ArgumentParser()')[0],functions)
load=functions['load'];sha=functions['sha'];save=functions['save'];archive=functions['archive'];processes=functions['processes']
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noNewWeaponRuns'];process_audit=processes();assert OUT.exists() and (OUT/'preflight.json').exists() and (OUT/'journal.jsonl').exists() and not (OUT/'dedup-receipt.json').exists()
release=WORK/'releases/boss-core-stage21-manor-original-timing-20261113';deployed=load(release/'deployment-receipt.json');target=Path(deployed['target']);assert sha(target)==deployed['deployedSha256']=='21af23af5246b284bd5939b9b46735c1a61fc69627a9b86d6811c8c91d0d329d'
forge=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');forge_hash=sha(forge);assert forge_hash=='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'
paths={};archive_inputs={}
for name in ['boss-core-stage19-manor-living-target-20261113','boss-core-stage20-manor-airborne-selection-20261113','boss-core-stage21-manor-original-timing-20261113']:
    folder=WORK/'releases'/name;receipt=load(folder/'archive-receipt.json');assert receipt['failures']==0 and receipt['allFilesReadBackAndHashVerified'] and sha(receipt['archive'])==receipt['archiveSha256'];archive_inputs[str(folder/'archive-receipt.json')]=sha(folder/'archive-receipt.json')
    manifest=load(folder/'copied-mods-restore-manifest.json')
    for relative,row in manifest.items():
        path=(ROOT/relative).resolve();assert path.is_relative_to((WORK/'build').resolve()) and path.parent.name=='mods' and path.parent.parent.name=='server' and path.suffix=='.jar' and not path.name.startswith('ftboss-boss-core-');assert path.is_file() and path.stat().st_size==row['bytes'] and sha(path)==row['sha256'];assert not path.is_symlink();paths[str(path)]={**row,'relative':relative}
assert paths and len({row['sha256'] for row in paths.values()})<len(paths);assert all(Path(name).drive==ROOT.drive for name in paths)
save(OUT/'recovery-start-v2.json',{'priorScript':'tools/dedup_archived_boss_common_mods_stage21_v1.py','priorFailure':'PermissionError WinError5 on read-only destination Iron spellbooks copy;all324original paths/hash bytes re-read intact,43journal paths already linked,one pending verified temp. No game/world/source/formal byte changed.','originalJournalRows':len((OUT/'journal.jsonl').read_text(encoding='utf-8').splitlines()),'recovery':'Reuse only SHA/file-index-verified temp;temporarily clear only independent local read-only duplicate destination for atomic replacement;all cache aliases read-only after validation.'});CACHE.mkdir(parents=True,exist_ok=True);save(OUT/'preflight-v2.json',{'processes':process_audit,'paths':paths,'archiveReceiptHashes':archive_inputs,'targetSha256':sha(target),'forgeSha256':forge_hash,'onlyLocalArchivedCommonModCopies':True,'worldFilesUntouched':True,'inputSourcesAndFileContentsUnchanged':True})
free_before=shutil.disk_usage(ROOT).free;canonical={};linked=0
for name,row in paths.items():
    path=Path(name);wanted=row['sha256'];cached=CACHE/(wanted+'.jar');assert cached.resolve().is_relative_to(WORK.resolve())
    if not cached.exists():os.link(path,cached)
    assert sha(cached)==wanted and cached.stat().st_size==row['bytes'];canonical[wanted]=str(cached)
    if path.stat().st_ino!=cached.stat().st_ino:
        temporary=path.with_name(path.name+'.common-cache-link.tmp')
        if temporary.exists():assert sha(temporary)==wanted and temporary.stat().st_ino==cached.stat().st_ino
        else:os.link(cached,temporary)
        assert sha(temporary)==wanted and sha(path)==wanted
        was_readonly=not (path.stat().st_mode&stat.S_IWRITE)
        if was_readonly:
            assert path.stat().st_nlink==1,'Unexpected shared read-only destination inode'
            os.chmod(path,stat.S_IREAD|stat.S_IWRITE)
        try:os.replace(temporary,path)
        except BaseException:
            if was_readonly and path.exists():os.chmod(path,stat.S_IREAD)
            raise
        linked+=1
    assert path.stat().st_ino==cached.stat().st_ino and sha(path)==wanted
    with (OUT/'journal.jsonl').open('a',encoding='utf-8') as stream:stream.write(json.dumps({'path':str(path),'cache':str(cached),'sha256Unchanged':wanted,'sameLocalFileIndex':True},ensure_ascii=False)+chr(10))
for name in canonical.values():os.chmod(name,stat.S_IREAD)
for name,row in paths.items():assert sha(name)==row['sha256'] and not (Path(name).stat().st_mode&stat.S_IWRITE)
assert sha(target)==deployed['deployedSha256'] and sha(forge)==forge_hash
reports=set()
for prefix in ['manor-living-target-v','manor-living-skills-v','manor-airborne-selection-v','manor-original-timing-v','manor-original-timing-skills-v']:
    reports.update((WORK/'reports').glob(prefix+'*.json'))
verified_inputs=0
for path in reports:
    for name,wanted in load(path).get('inputHashes',{}).items():assert sha(name)==wanted,name;verified_inputs+=1
receipt={'completedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'localPathsRetained':len(paths),'uniqueImmutableJars':len(canonical),'duplicateInodesReplacedByAtomicHardlinks':linked,'originalLogicalBytes':sum(row['bytes'] for row in paths.values()),'uniqueBytes':sum(Path(name).stat().st_size for name in canonical.values()),'freeBytesBefore':free_before,'freeBytesAfter':shutil.disk_usage(ROOT).free,'everyPathContentSha256Unchanged':True,'cacheAndAliasedJarsReadOnly':True,'frozenNativeInputReferencesReverified':verified_inputs,'nativeReports':len(reports),'worldFilesDeletedOrModified':False,'formalJarSha256Unchanged':sha(target),'forgeSha256Unchanged':sha(forge),'historicalDModified':False,'canonical':canonical,'fullBossGoalStatus':'active'};save(OUT/'dedup-receipt.json',receipt)
creator=WORK/'tools/prepare_boss_blood_tide_stage10.py';namespace={'__file__':str(creator)};exec(creator.read_text(encoding='utf-8').split('probe=(WORK/')[0],namespace);namespace['create'](OUT/'RESTORE.md',chr(10).join(['# Archived native common dependency cache','','Only previously archived duplicate common mod JAR copies inside C workspace server/mods were atomically replaced by hardlinks to identical local immutable bytes. All original paths and SHA256 contents retained;worlds,sources,formal Boss/Forge JARs and historical D untouched.','Cache and aliases read-only. Do not edit a linked archived JAR in place. For an independent writable copy,use the frozen common-mod restore archive and manifest to create a separate file in a new runtime;do not modify frozen evidence.','Source archives remain complete and independently hash verified. All frozen native input references reverified after maintenance.','Full Boss goal active,weapons paused,same sole C writer.']))
files={Path(__file__),WORK/'tools/dedup_archived_boss_common_mods_stage21_v1.py',helper,OUT/'preflight.json',OUT/'preflight-v2.json',OUT/'recovery-start-v2.json',OUT/'journal.jsonl',OUT/'dedup-receipt.json',OUT/'RESTORE.md'};archived=archive(WORK/'archives/boss-common-mod-cache-dedup-stage21-20261113.zip',files);save(OUT/'archive-receipt.json',archived);print(json.dumps({'localPathsRetained':len(paths),'uniqueImmutableJars':len(canonical),'freeGiBBefore':round(free_before/2**30,2),'freeGiBAfter':round(receipt['freeBytesAfter']/2**30,2),'allHashesUnchanged':True,'archive':archived,'fullBossGoalStatus':'active'}),flush=True)
