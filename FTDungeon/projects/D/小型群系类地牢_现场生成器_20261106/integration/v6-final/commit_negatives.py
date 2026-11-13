import copy
import datetime
from pathlib import Path
import shutil
import sys

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
sys.path.insert(0,str(HOME.parent));sys.path.insert(0,str(HOME.parent/'v5-diagnostics'))
import integrate
from faults import Server
from installed import context


def main():
    frozen=integrate.load(HOME/'progress.json');installed=integrate.load(HOME/'installed-progress.json')
    if frozen['jarSha256']!=installed['jarSha256']:raise integrate.GateError('Negative receipt test exact artifact mismatch')
    source=Path(installed['root'])/'q0';completed=integrate.load(source/'result.json')
    if completed['status']=='FAIL':raise integrate.GateError('Completed exact installed source required')
    root=HOME/'receipt-run'/datetime.datetime.now().strftime('%Y%m%d_%H%M%S');root.mkdir(parents=True)
    for name in ('world','libraries','mods'):shutil.copytree(source/name,root/name)
    for name in ('eula.txt','server.properties'):shutil.copyfile(source/name,root/name)
    identity=completed['instanceId'];world=root/'world';receipt=world/'candidate-acceptance'/(identity+'.json')
    result=dict(status='RUNNING',world=str(world),jarSha256=frozen['jarSha256'],cases={},accepted=False,productionPromotion=False,scope='Deliberately invalid receipts only; no acceptance PASS is created by this test')
    server=None
    try:
        server=Server(root,'receipt-negatives');binding=context(server,identity)
        base=dict(schema=1,binding=binding,purpose='ISOLATED_EXPERIMENTAL_JOB_COMMIT_ONLY',independentExecutor='INTEGRATION_NEGATIVE_PROTOCOL_TEST_ONLY',gates={},reports={},unresolvedBlockers=['NEGATIVE_RECEIPT_TEST_NEVER_APPROVAL'])
        for name in ('missing_receipt','wrong_command_hash','wrong_binding','wrong_challenge','missing_gate','unknown_field'):
            trial=copy.deepcopy(base)
            if name=='wrong_binding':trial['binding']['worldIdentity']='0'*64
            if name=='wrong_challenge':trial['binding']['commitChallenge']='INVALID_NEGATIVE_TEST'
            if name=='unknown_field':trial['unrecognized']='NEGATIVE_ONLY'
            if receipt.exists():receipt.unlink()
            if name!='missing_receipt':integrate.save(receipt,trial)
            expected='0'*64 if name in ('wrong_command_hash','missing_receipt') else integrate.digest(receipt)
            offset=len(server.text());server.send('ftbiomedungeon_candidate commit '+identity+' '+expected+' CONFIRM_INDEPENDENT_EVIDENCE_COMMIT')
            text=server.wait(lambda text:'Rejected:' in text[offset:],30);status=server.status(identity)
            if 'active=false tickets=0' not in status or 'phase=COMMITTED' in status:raise integrate.GateError('Invalid receipt activated or committed '+name)
            result['cases'][name]=dict(status='PASS_REFUSED_IDLE_NONCOMMITTED',response=text[offset:].splitlines(),jobStatus=status)
            integrate.save(HOME/'receipt-negative-progress.json',result)
        result['status']='PASS_ALL_INVALID_RECEIPTS_REFUSED'
    except Exception as error:result.update(status='FAIL',error=repr(error))
    finally:
        if server is not None:server.stop();result['process']=server.evidence
        integrate.save(HOME/'receipt-negative-progress.json',result);integrate.save(root/'result.json',result)
    print(result['status'])


if __name__=='__main__':main()
