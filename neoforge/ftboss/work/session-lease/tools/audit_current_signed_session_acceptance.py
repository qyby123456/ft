import datetime
import hashlib
import json
import re
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
OUTPUT=WORK/'reports/final-acceptance-audit-5f283607-20261112'

def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def source_graph(attempt):
    arguments=load(attempt/'complete-arguments316.json')
    sources=[Path(value) for value in arguments if isinstance(value,str) and value.endswith('.java')]
    assert len(sources)==716
    graph={}
    for path in sources:
        content=path.read_bytes();text=content.decode('utf-8-sig')
        package=re.search(r'(?m)^package\s+([A-Za-z0-9_.]+)\s*;',text)
        identity=(package.group(1).replace('.','/') if package else '<default>')+'/'+path.name
        assert identity not in graph,identity
        graph[identity]={'source':str(path),'sha256':hashlib.sha256(content).hexdigest()}
    assert len(graph)==716
    return graph
def class_graph(receipt):
    attempt=Path(receipt['attempt']);classes=attempt/'complete-classes'
    actual={path.relative_to(classes).as_posix():digest(path) for path in classes.rglob('*.class')}
    assert len(actual)==receipt['classCount']==1381
    for relative,wanted in actual.items():assert receipt['inputHashes'][str(classes/relative)]==wanted,relative
    return actual

assert not OUTPUT.exists()
assert load(ROOT/'WRITE_OWNER.claim')['threadId']==load(ROOT/'WRITE_OWNER.json')['activeWriterThreadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
current_path=WORK/'reports/native-repair-aliased-entity-expected-image-candidate-5f283607.json'
assert digest(current_path)=='482fd005e8287ef41e48eced0ca5bd740df8b78580526165a2b550d47f666b10'
current=load(current_path);current_classes=class_graph(current);current_sources=source_graph(Path(current['attempt']))
OUTPUT.mkdir()
inputs={str(Path(__file__)):digest(Path(__file__)),str(current_path):digest(current_path)}
baselines=[('six_representative_paid_repair','native-six-family-paid-repair-restart-focused-evidence.json','6fd4a25ad9f78acd0bbc38fd17caf827116e9b8b257ff9924f19038a5b1f30a4'),('trident_pickup_restart','native-trident-paid-signed-pickup-restart-focused-evidence-529ba4c9.json','d18bc7175be839a6709ab51e1c2997676aa91007620f3a3201ce2163c3d63a50'),('retired_food_closure','native-retired-food-closure-focused-evidence-99c65f41.json','6a92fdd28a36f30335eeb5ed718c93eff887085f7b199d467005d9641bb53bbe'),('material_crossbow_restart','native-material-crossbow-paid-signed-restart-focused-evidence-41d445cf.json','866acf80492824c8284c9e7b77a1288abd7ca9a1a2b39ec042fa8fcf49203f06')]
deltas=[]
for label,name,wanted in baselines:
    evidence_path=WORK/'reports'/name;assert digest(evidence_path)==wanted
    evidence=load(evidence_path);candidate_path=Path(evidence['candidate']);candidate=load(candidate_path)
    inputs[str(evidence_path)]=wanted;inputs[str(candidate_path)]=digest(candidate_path)
    before_classes=class_graph(candidate);before_sources=source_graph(Path(candidate['attempt']))
    assert set(before_classes)==set(current_classes) and set(before_sources)==set(current_sources)
    changed_classes=[{'class':relative,'beforeSha256':before_classes[relative],'currentSha256':actual} for relative,actual in sorted(current_classes.items()) if before_classes[relative]!=actual]
    changed_sources=[{'name':name,'before':before_sources[name],'current':value} for name,value in sorted(current_sources.items()) if before_sources[name]['sha256']!=value['sha256']]
    row={'baselineScope':label,'historicalEvidence':str(evidence_path),'historicalEvidenceSha256':wanted,'baselineCandidate':str(candidate_path),'baselineCandidateSha256':digest(candidate_path),'baselineAttempt':candidate['attempt'],'currentCandidate':str(current_path),'classGraphsHashVerifiedAgainstFrozenReceipts':True,'classCount':1381,'effectiveSourceCount':716,'unchangedClasses':1381-len(changed_classes),'unchangedEffectiveSources':716-len(changed_sources),'changedClasses':changed_classes,'changedEffectiveSources':changed_sources,'fullHistoricalInputGraphOrRuntimeRerun':False,'historicalAcceptanceAutomaticallyPromoted':False,'interpretation':'Exact inventory delta only. Historical focused acceptance remains valid for its original candidate and scope. Each changed behavior needs bounded semantic review or required new runtime before reusing it for current candidate. Equal graph entries do not expand old fixture coverage.'}
    save(OUTPUT/(label+'-exact-delta.json'),row);deltas.append({key:row[key] for key in ['baselineScope','historicalEvidence','baselineCandidate','unchangedClasses','unchangedEffectiveSources']});deltas[-1].update(changedClasses=len(changed_classes),changedEffectiveSources=len(changed_sources),report=str(OUTPUT/(label+'-exact-delta.json')))

def gate(identifier,requirement,status,evidence,missing):return {'id':identifier,'requirement':requirement,'status':status,'authoritativeEvidence':evidence,'remainingEvidenceRequired':missing}
requirements=[
 gate('genuine_nonnull_lease_payment_ancestry','Real nonnull lease with original START/payment/CLAIM, signed continuation, epoch/generation coherence and consumed replay denial','historical_focused_pass_current_delta_pending',[str(WORK/'reports/native-six-family-paid-repair-restart-focused-evidence.json'),str(WORK/'reports/native-repair-runtime-exact-melee-configured-paid-849680bb.json'),str(OUTPUT/'six_representative_paid_repair-exact-delta.json')],'Preserve genuine paid seed and prove unchanged authority path/current-candidate scope. No reflection-created authority or retired-state resets.'),
 gate('full_nineteen_original_sessions','Complete signed Sessions for all nineteen configured originals, exact original leaf identities, semantic groups, cursor, exception/foreign controls and original committed post-combat saves','live_original_runtime_pending',[str(WORK/'build/repair-runtime-bc621ceb'),str(WORK/'tests/native-full-nineteen-signed-dd1ad841/PositiveWeapon436.java'),str(WORK/'reports/aliased-entity-expected-image-live-checkpoint-20261112/progress.json')],'Wait on existing supervisor tool session38677; require actual final original observer result and normal save. Prior first-three iteration completion is not whole-nineteen acceptance.'),
 gate('required_restart_and_withdrawal','Required actual new-JVM restart and provider/foreign mutation authority withdrawal on the complete current original Session chain','current_full_chain_unproven',[str(WORK/'reports/native-six-family-paid-repair-restart-focused-evidence.json'),str(WORK/'reports/native-trident-paid-signed-pickup-restart-focused-evidence-529ba4c9.json'),str(OUTPUT/'trident_pickup_restart-exact-delta.json')],'Complete current nineteen result first; then required independent-process continuation, consumed replay and provider mutation checks. Failed worlds never become restart seeds.'),
 gate('six_family_variants','Sword/bow/crossbow/axe/trident/staff retained families and remaining registered subclasses/external provider variants, native/non-native wear paths','historical_representatives_only',[str(WORK/'reports/native-six-family-registered-census-evidence.json'),str(WORK/'SIX_FAMILY_PAID_REPAIR_ACCEPTANCE.md'),str(OUTPUT/'material_crossbow_restart-exact-delta.json')],'The103-item census is not103-item lifecycle acceptance. Eight staff lack original native durability; external/subclass and specified lifecycle variants remain separate.'),
 gate('ten_external_recipes','Ten external recipes and required provider withdrawal/recipe compatibility without altering original global rules','not_proven_by_current_receipts',[str(WORK/'RESUMED_GOAL_20261112.md')],'Locate exact original ten-recipe specification and accepted scope before final verification. Frozen6104-output baseline or general CRC coverage alone does not prove ten integration cases.'),
 gate('retired_features_and_protocol','Remove unsupported-family old adaptation and retired food-enchantment gameplay/catalogue/UI/protocol/translation while preserving legacy stored data, vanilla rules and Independent Event','historical_focused_pass_current_delta_pending',[str(WORK/'reports/native-retired-food-closure-focused-evidence-99c65f41.json'),str(OUTPUT/'retired_food_closure-exact-delta.json')],'Bound current changes before reuse. Compatibility no-op ABI shells are deliberate; symbol absence alone is not behavior proof.'),
 gate('boss_server_parity','Original Boss rewards/base items independently retained, skills/rotation/phases/summon scheduling and full battles faithfully migrated','incremental_server_evidence_only',[str(ROOT/'work/session-boss/REMAINING_BOSS_CHECKPOINT_20261112.md'),str(ROOT/'work/session-boss/releases/boss-core-stage12-bone-crush-20261112'),str(ROOT/'work/session-boss/candidates/boss-core-stage13-summon-fx-20261112/archive-receipt.json')],'Stage13 directed clone fixture is not authenticated natural ingress/full battle. Stage03 positive authenticated kill, remaining fight/rotation parity and original reward flow remain unproven.'),
 gate('independent_packaging','Independent Boss core preserves original rewards/base definitions; separate paused weapon JAR scope and valid runtime dependencies','weapon_recovery_container_only',[str(ROOT/'work/session-boss/archives/weapons-paused-8199dcc2-boss-priority-20261112/archive-receipt.json')],'Recovery snapshot is not accepted installable addon. Never install it or claim complete separation acceptance based only on a compressed class container.'),
 gate('client_dedicated_multiplayer_and_gl','Real client and dedicated server, authenticated multiplayer, full combat, four GL directions and isolated-instance closure','final_acceptance_missing',[str(WORK/'RESUMED_GOAL_20261112.md'),str(ROOT/'work/session-boss/REMAINING_BOSS_CHECKPOINT_20261112.md')],'Actual authenticated handshakes, client particle delivery/rendering/motion/GL and complete battles still required. Synthetic scratch EmbeddedChannel is not authenticated multiplayer.'),
 gate('formal_readonly_and_single_writer','Use sole C workspace; D and formal instances read-only; no deployment/overwrite or writer handoff','continuously_required',[str(ROOT/'AGENTS.md'),str(ROOT/'WRITE_OWNER.claim'),str(WORK/'reports/goal-continuation-after-stage13-20261112/preflight.json')],'Recheck formal hashes and owned process identity at terminal boundaries; retain same current native run and original scope.')]
for path in [WORK/'RESUMED_GOAL_20261112.md',WORK/'SIX_FAMILY_PAID_REPAIR_ACCEPTANCE.md',ROOT/'work/session-boss/REMAINING_BOSS_CHECKPOINT_20261112.md',WORK/'tests/native-full-nineteen-signed-dd1ad841/PositiveWeapon436.java']:
    inputs[str(path)]=digest(path)
summary={'createdAt':datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat(),'previousGoalTurnClassification':'progress','currentWaitVerifiedByToolSession':38677,'currentCandidate':str(current_path),'currentCandidateSha256':digest(current_path),'frozenCandidateClassGraphVerified':1381,'scopeSources':['Latest explicit active user objective',str(ROOT/'AGENTS.md'),str(WORK/'RESUMED_GOAL_20261112.md'),str(ROOT/'work/session-boss/REMAINING_BOSS_CHECKPOINT_20261112.md')],'historicalPauseStatusSupersededOnlyForLatestNamedPrerequisites':True,'gates':requirements,'historicalExactDeltas':deltas,'allRequirementsAchieved':False,'fullSignedSessionAccepted':False,'fullLifecycleAccepted':False,'goalStatus':'active','installable':False,'deployed':False,'inputHashes':inputs}
save(OUTPUT/'requirements-and-evidence.json',summary)
print(json.dumps({'audit':str(OUTPUT/'requirements-and-evidence.json'),'gates':len(requirements),'deltas':deltas,'allRequirementsAchieved':False,'ownedRuntimeReused':True,'deployed':False}),flush=True)
