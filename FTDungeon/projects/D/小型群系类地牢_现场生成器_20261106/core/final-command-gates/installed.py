import datetime
import json
import os
from pathlib import Path
import re
import shutil
import sys
import time
import zipfile

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
INTEGRATION=HOME.parent.parent/'integration'
sys.path.insert(0,str(INTEGRATION))
sys.path.insert(0,str(INTEGRATION/'v5-diagnostics'))
import integrate
from faults import Server


def context(server,identity):
    offset=len(server.text());server.send('ftbiomedungeon_candidate acceptance-context '+identity)
    text=server.wait(lambda text:'"commitChallenge":' in text[offset:] and '"jarSha256":' in text[offset:],45)
    line=next(line for line in reversed(text[offset:].splitlines()) if '"commitChallenge":' in line and '"jarSha256":' in line)
    return json.loads(line[line.index('{'):])


def main():
    frozen=integrate.load(HOME/'progress.json')
    if frozen['status']!='EXACT_INPUTS_FROZEN_INDEPENDENT_WORLDS_NOT_RUN':
        raise integrate.GateError('Freeze exact runtime ready first; no independent world launched')
    snapshot=Path(frozen['snapshot']);jar=Path(frozen['jar'])
    lock=integrate.load(snapshot/'snapshot-lock.json')
    for name,entry in lock['components'].items():
        for relative,expected in entry['files'].items():
            if integrate.digest(integrate.contained(snapshot/name,relative))!=expected:
                raise integrate.GateError('Frozen source changed before installation')
    if integrate.digest(jar)!=frozen['jarSha256']:
        raise integrate.GateError('Exact candidate JAR changed')
    with zipfile.ZipFile(jar)as archive:
        names=archive.namelist()
        if len(names)!=len(set(names)) or any('GameTests.class' in name for name in names):
            raise integrate.GateError('Release JAR duplicate/test entries')
        required=['cn/ft/biomedungeon/core/next/DungeonEnvelopePlanner.class','cn/ft/biomedungeon/runtime/CommitEvidence.class','data/ftbd_candidate/s8_07_widearch_side_doublegate_v5.nbt']
        if any(name not in names for name in required):
            raise integrate.GateError('Final planner/commit/derived source absent')
        if __import__('hashlib').sha256(archive.read(required[-1])).hexdigest()!='bd7d0661b818164a3617629096580c646d17d95832269e6aca989933dc815ae3':
            raise integrate.GateError('Derived original source mismatch')
    root=HOME/'run'/datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    result=dict(status='RUNNING',root=str(root),snapshot=str(snapshot),jarSha256=frozen['jarSha256'],worldSeed='267611119455246637',anchor=[1000,200,1000],cases={},accepted=False,productionPromotion=False,completeGeneratorReady=False)
    integrate.save(HOME/'installed-progress.json',result)
    for turn in (list(map(int,sys.argv[1:])) or range(4)):
        run=root/('q'+str(turn));run.mkdir(parents=True)
        shutil.copytree(HOME.parent/'run/20261106_132015_129205_installed_132725/libraries',run/'libraries')
        (run/'mods').mkdir();shutil.copyfile(jar,run/'mods'/jar.name)
        (run/'eula.txt').write_text('eula=true\n')
        (run/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-name=world\nlevel-seed=267611119455246637\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\n')
        world=run/'world';world.mkdir();(world/'ALLOW_CANDIDATE_EVIDENCE_WORLD_V5').write_text('PARENT_AUTHORIZED_EIGHT_ORIGINALS_ONE_DERIVED_V5_EVIDENCE_ONLY\n')
        entry=dict(status='RUNNING',ordinal=turn,anchor=[1000,200,1000],worldSeed='267611119455246637',processes=[],independentAcceptance='NOT_YET_DECIDED')
        result['cases'][str(turn)]=entry;integrate.save(HOME/'installed-progress.json',result);server=None
        try:
            server=Server(run,'prepare')
            command='ftbiomedungeon_candidate plan 1000 200 1000 s8 9 '+str(turn);entry['actualPlanCommand']=command;server.send(command)
            text=server.wait(lambda text:re.search(r'DRY RUN (ftde-[a-f0-9]+)',text)is not None or 'Rejected:' in text,120)
            match=re.search(r'DRY RUN (ftde-[a-f0-9]+)',text)
            if match is None:
                raise integrate.GateError('Actual exact original-anchor regression rejected: '+text[-1200:])
            identity=match.group(1);entry['instanceId']=identity
            server.send('ftbiomedungeon_candidate build '+identity+' CONFIRM_ISOLATED_AIRSPACE')
            deadline=time.monotonic()+900
            while time.monotonic()<deadline:
                status=server.status(identity)
                if 'phase=FAILED' in status or 'phase=ROLLBACK_REQUIRED' in status or 'Rejected:' in server.text():
                    raise integrate.GateError('Installed candidate quarantined: '+status)
                if 'active=false tickets=0 stage=PAYLOAD_VERIFIED' in status:
                    entry['payloadStatus']=status;break
                time.sleep(2)
            else:
                raise integrate.GateError('Actual installed payload timeout')
            binding=context(server,identity)
            if binding['jarSha256']!=frozen['jarSha256'] or binding['worldSeed']!='267611119455246637':
                raise integrate.GateError('Installed acceptance context exact binding mismatch')
            integrate.save(run/'acceptance-context.json',binding);entry['binding']=binding
            acceptance=world/'candidate-acceptance';acceptance.mkdir(exist_ok=True)
            incomplete=dict(schema=1,binding=binding,purpose='ISOLATED_EXPERIMENTAL_JOB_COMMIT_ONLY',independentExecutor='INTEGRATION_NEGATIVE_GATE_TEST_NOT_APPROVAL',gates={},reports={},unresolvedBlockers=['NEW_EXACT_ARTIFACT_INDEPENDENT_EVIDENCE_PENDING'])
            receipt=acceptance/(identity+'.json');integrate.save(receipt,incomplete)
            offset=len(server.text());server.send('ftbiomedungeon_candidate commit '+identity+' '+integrate.digest(receipt)+' CONFIRM_INDEPENDENT_EVIDENCE_COMMIT')
            server.wait(lambda text:'Every mandatory gate must be independently decided' in text[offset:],45)
            status=server.status(identity)
            if 'phase=COMMITTED' in status:
                raise integrate.GateError('Incomplete independent evidence falsely committed')
            entry['incompleteReceiptRefused']=True
            offset=len(server.text());server.send('ftbiomedungeon_candidate routeaudit '+identity)
            server.wait(lambda text:'Native route audit attempted=' in text[offset:] or 'Rejected:' in text[offset:],900)
            route=world/'ft_biome_dungeon_candidate_v5/minecraft/overworld'/(identity+'-routes.json')
            report=integrate.load(route);entry['producerPathProposals']=dict(attempted=report['attempted'],passed=report['passed'],report=str(route),sha256=integrate.digest(route),independentReplayPending=True)
            server.send('ftbiomedungeon_candidate evidence '+identity);time.sleep(1)
            server.stop();entry['processes'].append(server.evidence);server=None
            entry.update(status='EXACT_INSTALLED_PAYLOAD_AND_INCOMPLETE_COMMIT_REFUSAL_DONE_INDEPENDENT_GATES_PENDING',phaseCommitted=False)
        except Exception as error:
            entry.update(status='FAIL',error=repr(error))
        finally:
            if server is not None:
                server.stop();entry['processes'].append(server.evidence)
            integrate.save(run/'result.json',entry);integrate.save(HOME/'installed-progress.json',result)
    result['status']='INSTALLED_CASES_COMPLETE_INDEPENDENT_GATES_PENDING' if all(entry['status']!='FAIL' for entry in result['cases'].values()) else 'FAILED_OR_INCOMPLETE'
    integrate.save(HOME/'installed-progress.json',result);print(result['status'])


if __name__=='__main__':
    main()
