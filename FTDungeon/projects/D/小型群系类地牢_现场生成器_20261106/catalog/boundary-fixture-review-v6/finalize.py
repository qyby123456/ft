import sys
from pathlib import Path

ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent))
from freeze_catalog import digest,load,save


def main():
    lock=load(ROOT/'inputs-lock.json')
    for record in lock['files']:
        if digest(ROOT/record['path'])!=record['sha256']:
            raise ValueError('snapshot changed')
    requirements=load(ROOT/'boundary-requirements.json')
    if requirements['count']!=352 or requirements['nativeDirectionChecks']!=704 or requirements['atomKinds']!={'singleton':192,'open_interval':160}:
        raise ValueError('coverage mismatch')
    files=[{'path':path.relative_to(ROOT).as_posix(),'sha256':digest(path),'bytes':path.stat().st_size} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name not in ('ready.json','progress.json','SHA256SUMS.json') and '__pycache__' not in path.parts]
    save(ROOT/'SHA256SUMS.json',{'algorithm':'SHA-256','files':files})
    ready={'component':'catalog-boundary-fixture-review-v6','ready':True,'status':'STATIC_FIXTURE_ROOT_CAUSE_AND_PRESERVED_BOUNDARY_REQUIREMENTS_READY','rootCause':'null PlacedRoom bounds in unchanged helper, xAtom schema correct','nativeJVMStarted':False,'integrationModified':False,'accepted':False,'worldPass':False,'requirements':352,'directedReplayRequirements':704,'singletonAtoms':192,'openIntervalAtoms':160,'findings':'findings.json','findingsSha256':digest(ROOT/'findings.json'),'boundaryRequirementsSha256':digest(ROOT/'boundary-requirements.json'),'inventorySha256':digest(ROOT/'SHA256SUMS.json'),'failedRunSource':lock['failedRunSourceSelection'],'directIntegrationNotification':'explicitly authorized by coordinator; initial root cause delivered','limits':['static source/log/schema review only','native rerun and full context/shape/step interval proof still required','all full-width3headroom targets unchanged']}
    save(ROOT/'ready.json',ready)
    save(ROOT/'progress.json',{'component':ready['component'],'status':ready['status'],'readySha256':digest(ROOT/'ready.json'),'nativeWorldExecution':False,'integrationWrites':False})
    print('READY static repair advice,352 requirements/704directions preserved; SHA='+digest(ROOT/'ready.json'))


if __name__=='__main__':
    main()
