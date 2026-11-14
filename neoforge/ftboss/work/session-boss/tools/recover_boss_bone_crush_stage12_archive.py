from pathlib import Path
import hashlib
import json

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
original_tool=WORK/'tools/finalize_boss_bone_crush_stage12_v3.py'
source=original_tool.read_text(encoding='utf-8')
cut=source.index("target=Path(previous['target']).resolve()")
prefix=source[:cut]
assert 'release.mkdir()' not in prefix and 'os.replace(temporary,target)' not in prefix
exec(compile(prefix,str(original_tool),'exec'),globals())
release=WORK/'releases/boss-core-stage12-bone-crush-20261112'
deployment=load(release/'deployment-receipt.json')
target=Path(deployment['target']).resolve()
assert target==Path(paused['formalInstancesUnchanged']['neoforge']['path']).resolve()
assert digest(target)==deployment['deployedSha256']==built['jarSha256']=='58002a729b48fba41bd4f4832b967760879114b214b93ff0729dfc8680483dc3'
backup=Path(deployment['backup']);assert digest(backup)==deployment['backupSha256']==built['baseJarSha256']
released=release/'ftboss-boss-core-1.21.1-stage12.jar';assert digest(released)==digest(target)
forge=Path(paused['formalInstancesUnchanged']['forge']['path']);forge_before=digest(forge)
assert forge_before==deployment['forgeSha256Unchanged']==paused['formalInstancesUnchanged']['forge']['sha256']
temporary=target.with_name(target.name+'.stage12.tmp');assert not temporary.exists()
assert not (release/'archive-receipt.json').exists() and not (WORK/'archives/boss-core-stage12-closeout-20261112.zip').exists()
feature=load(release/'feature-verification.json');assert feature['candidateCases']==78 and feature['candidateReceipt']==str(candidate_path)
partial=WORK/'archives/boss-core-stage12-bone-crush-20261112.zip'
with zipfile.ZipFile(partial) as bundle:assert 'ARCHIVE_MANIFEST.json' not in bundle.namelist()
save(release/'archive-failure-recovery.json',{'firstFailure':'Baseline v2 actual probe filename was incorrectly changed to v3 during archive file selection','partialArchive':str(partial),'partialArchiveSha256':digest(partial),'partialArchiveAccepted':False,'deploymentAlreadyHashVerified':True,'deployedSha256':digest(target),'rollbackSha256':digest(backup),'noDeploymentRetried':True,'recoveryScope':'Finish archive and metadata only; original deployment/feature receipts retained','originalFailingTool':str(original_tool),'originalFailingToolSha256':digest(original_tool)})
save(release/'prearchive-recovery-process-audit.json',audit_processes())
start=source.index('files={Path(__file__)')
end=source.index("archived=archive(WORK/'archives/boss-core-stage12-bone-crush-20261112.zip'")
collection=source[start:end]
old="files.add(attempt/'server/actual-bone-crush-probe-v3.json')"
new="files.add(attempt/('server/actual-bone-crush-probe-v2.json' if result is baseline else 'server/actual-bone-crush-probe-v3.json'))"
assert collection.count(old)==1
collection=collection.replace(old,new)
exec(compile(collection,str(original_tool)+'-corrected-file-selection','exec'),globals())
files.add(partial)
complete=WORK/'archives/boss-core-stage12-bone-crush-20261112-complete.zip'
assert not complete.exists()
archived=archive(complete,files)
tail=source[source.index('archived.update('):]
assert 'os.replace(' not in tail and 'shutil.copyfile(' not in tail
exec(compile(tail,str(original_tool)+'-archive-and-closeout-only','exec'),globals())
