import gzip
import hashlib
import json
from pathlib import Path
import sys

sys.dont_write_bytecode=True
ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent.parent/'integration'))
import verify_saved_world as base


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    lock=load(ROOT/'input-lock.json')
    reference_drift=[]
    for record in lock['files']:
        source=Path(record['path'])
        current=sha(source)
        if current!=record['sha256']:
            reference=ROOT/'references'/source.name
            assert source.name in ('pause_io.py','restart_saved.py') and reference.is_file() and sha(reference)==record['sha256'],'Unexpected immutable input drift: '+record['path']
            reference_drift.append(dict(path=str(source),copiedVersionSha256=record['sha256'],currentOwnerVersionSha256=current,executedCopiedVersion=True))
    for record in lock['copiedInputs']:
        path=Path(record['path'])
        if path.is_relative_to(ROOT/'inputs') or '/libraries/' in path.as_posix() or '/mods/' in path.as_posix():
            assert sha(path)==record['sha256'],'Immutable copied input drift'
    previous=ROOT.parent/'final-runtime-faults/ready.json'
    assert sha(previous)==lock['frozen1115ReadySha256']
    for record in load(previous)['files']:
        assert sha(previous.parent/record['path'])==record['sha256']
    pause=load(ROOT/'pause-io-progress.json')
    negatives=load(ROOT/'receipt-negative-progress.json')
    restarts=load(ROOT/'restart-progress.json')
    assert pause['status']=='PASS_NATIVE_INSTALLED_PAUSE_IO_EXCEPTION_CLEANUP'
    assert negatives['status']=='PASS_ALL_INVALID_RECEIPTS_REFUSED' and len(negatives['cases'])==6
    for name,case in negatives['cases'].items():
        assert case['exactReasonVerified'] and case['expectedReason'] in '\n'.join(case['response']),name
        assert case['unresolvedBlockerRetained']==['NEGATIVE_RECEIPT_TEST_NEVER_APPROVAL']
    assert restarts['status']=='PASS_ALL_FOUR_INSTALLED_SEPARATE_JVM_NATIVE_RESTARTS'
    processes=[pause['process'],negatives['process']]
    reader=base.load_reader(ROOT.parent.parent/'integration/staging/20261106_132015_129205')
    installed=load(ROOT/'installed-progress.json')
    source_anchor=[]
    for turn in range(4):
        case=restarts['cases'][str(turn)]
        assert case['canonicalWorldIdentityVerified']
        run=ROOT/'installed'/('q'+str(turn))
        assert Path(case['canonicalWorld']).resolve()==(run/'world').resolve()
        assert case['binding']['jarSha256']==lock['jarSha256']
        assert case['binding']['worldIdentity']!=installed['cases'][str(turn)]['binding']['worldIdentity'],'Copied world must have its own canonical identity'
        journals=list((run/'world/ft_biome_dungeon_candidate_v5/minecraft/overworld').glob('*.nbt'))
        assert len(journals)==1
        framed=journals[0].read_bytes()
        assert framed[:4]==b'FTD5' and len(framed)-72==int.from_bytes(framed[4:8],'big')
        assert hashlib.sha256(framed[72:]).hexdigest().encode()==framed[8:72]
        journal=base.decode(reader,gzip.decompress(framed[72:]))
        request=json.loads(journal['request'])
        assert request['anchor']==dict(x=1000,y=200,z=1000) and request['instanceOrdinal']==turn
        assert request['worldSeed']==267611119455246637 and journal['phase']=='VERIFYING'
        assert journal['stage']=='PAYLOAD_VERIFIED_ROUTE_INTERACTION_ACCEPTANCE_PENDING' and not journal['receiptHash']
        assert journal['commitChallenge']==case['binding']['commitChallenge']
        source_anchor.append(dict(turn=turn,request=request,journalSha256=sha(journals[0]),canonicalContext=case['binding']))
        before,after=load(Path(case['before'])),load(Path(case['after']))
        assert before['originalStateDifferences']==after['originalStateDifferences']
        assert all(not after[key] for key in ('originalNativeFieldDifferences','escapedFluidOrFire','enclosureErrors'))
        processes.append(case['process'])
    for process in processes:
        assert process['exitCode']==0
        assert Path(process['cwd']).resolve().is_relative_to(ROOT.resolve())
        assert sha(Path(process['log']))==process['logSha256']
        mods=list((Path(process['cwd'])/'mods').glob('*.jar'))
        assert len(mods)==1 and sha(mods[0])==lock['jarSha256']
    summary=dict(status='PASS_FINAL_INSTALLED_PAUSE_IO_SIX_EXACT_RECEIPT_REJECTIONS_FOUR_RESTARTS',jarSha256=lock['jarSha256'],
                 independentExecutor='core-final-command-gates-not-runtime-maker',pauseIo=pause,invalidReceipts=negatives,restarts=restarts,
                 anchor=[1000,200,1000],verifiedSourceRequests=source_anchor,maxOwnedServers=1,originalWorldsAndSnapshotUnchanged=True,
                 readOnlyReferencesChangedByOwnerAfterCopy=reference_drift,
                 frozen1115Unchanged=True,powerloss=False,actualCommitted=False,productionPromotion=False,completeGeneratorReady=False)
    (ROOT/'result.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    files=[dict(path=path.relative_to(ROOT).as_posix(),sha256=sha(path)) for path in sorted(ROOT.rglob('*')) if path.is_file() and path!=ROOT/'ready.json']
    ready=dict(status=summary['status'],jarSha256=lock['jarSha256'],pauseIoPass=True,invalidReceiptCases=6,separateJvmRestarts=4,
               anchor=[1000,200,1000],originalWorldsAndSnapshotUnchanged=True,readOnlyReferenceDriftRecorded=reference_drift,powerloss=False,actualCommitted=False,files=files)
    (ROOT/'ready.json').write_text(json.dumps(ready,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('COMMAND_GATES_READY files='+str(len(files))+' SHA256='+sha(ROOT/'ready.json'),flush=True)


if __name__=='__main__':main()
