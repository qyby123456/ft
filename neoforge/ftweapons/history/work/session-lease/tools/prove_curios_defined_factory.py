import hashlib
import json
import subprocess
import uuid
from pathlib import Path

work = Path(__file__).resolve().parents[1]

def load(path):
    return json.loads(path.read_text(encoding='utf-8'))

def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def save(path, value):
    assert not path.exists(), str(path)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

runtime_receipt = work / 'reports/native-repair-runtime-curios-defined-factory.json'
runtime = load(runtime_receipt)
attempt = Path(runtime['attempt'])
terminal = load(attempt / 'terminal308.json')
assert runtime['exitCode'] == runtime['probe']['failures'] == 0
assert terminal['stopSent'] and terminal['completeSave']
assert runtime['soleNullReturningJavaTransformerInventoryObserved']
assert runtime['dynamicAttachDisabledForThisCaptureRun']
for name, wanted in {**runtime['inputHashes'], **runtime['jvmDefinitionBoundaryCaptureHashes']}.items():
    assert digest(name) == wanted, name
proof = work / 'build' / ('defined-factory-proof-' + uuid.uuid4().hex[:8])
proof.mkdir()
command = load(attempt / 'compile-command308.json')
classpath = command[command.index('-cp') + 1]
source = work / 'tests/curios-defined-factory/CuriosDefinedFactoryProof.java'
classes = proof / 'classes'
compile_command = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8',
                   '-cp', classpath, '-d', str(classes), str(source)]
save(proof / 'compile-command.json', compile_command)
with (proof / 'compile.log').open('wb') as output:
    compiled = subprocess.run(compile_command, stdout=output, stderr=subprocess.STDOUT)
assert compiled.returncode == 0, str(proof / 'compile.log')
run_command = ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8',
               '-cp', str(classes) + ';' + classpath, 'ftboss.provenance.CuriosDefinedFactoryProof',
               str(attempt / 'jvm-definition-captures'), str(attempt / 'definition-observer-classes'), str(proof / 'proof.json')]
save(proof / 'run-command.json', run_command)
with (proof / 'run.log').open('wb') as output:
    tested = subprocess.run(run_command, stdout=output, stderr=subprocess.STDOUT)
assert tested.returncode == 0, str(proof / 'run.log')
result = load(proof / 'proof.json')
assert result['failures'] == 0 and len(result['negativeProofControlsRejected']) == 4
inputs = {**runtime['inputHashes'], **runtime['jvmDefinitionBoundaryCaptureHashes']}
for path in [runtime_receipt, attempt / 'terminal308.json', attempt / 'server/actual-storage-view308.json',
             attempt / 'runtime308.log', source, Path(__file__), *[path for path in proof.rglob('*') if path.is_file()]]:
    inputs[str(path)] = digest(path)
for name, wanted in inputs.items():
    assert digest(name) == wanted, name
evidence_path = work / 'reports/native-curios-defined-factory-focused-evidence.json'
save(evidence_path, {
    'candidate': str(work / 'reports/native-repair-candidate-2ca083af.json'),
    'runtimeAttempt': str(attempt), 'staticProof': str(proof / 'proof.json'),
    'actualGenuinePaidLeaseAndGenerationRestoredViaOriginalLoad': True,
    'actualRegisteredHiddenProviderExactItemAndOriginalCuriosNest': True,
    'actualOriginalProviderInertCallReturnedNullWithoutChangingLiveAuthority': True,
    'jvmDefinitionBoundaryCapturedAllEightActualRuntimeClasses': True,
    'soleApplicableJavaTransformerAtStartupEveryCaptureAndShutdown': True,
    'observerBytecodeAllTransformReturnsNullAndNoRedefinitionCalls': True,
    'startupCommandAndInheritedOptionsContainNoOtherAgents': True,
    'dynamicAttachDisabledForThisRun': True,
    'fullOriginalFactory33InstructionGraphAndFourConditionalEdgesProved': True,
    'originalLambdaMetafactoryImplementationAndSameItemOperandsProved': True,
    'registryGetterFullGraphAndTrackedOriginalRegistryInstallationProved': True,
    'registrationHooksAndOriginalProviderInsertionOperandsProved': True,
    'negativeProofControlsRejected': result['negativeProofControlsRejected'],
    'definitionBytesSubstitutedWithRawJarOrPostMixinExport': False,
    'scope': 'Actual recorded isolated server definition boundary and original inert provider only; not future transformed binaries or unknown external native agents.',
    'failedCapturePathBoundaryIncident': str(work / 'reports/definition-capture-path-boundary-incident.json'),
    'failedRuntime7503378fExcluded': True,
    'newProductClasses': 0, 'authorityWrittenByReflection': False, 'testMixinUsed': False,
    'authenticatedMultiplayerAccepted': False, 'fullLifecycleAccepted': False,
    'promoted': False, 'installable': False, 'deployed': False, 'inputHashes': inputs
})
prior_path = work / 'reports/native-continuation-acceptance-2ca083af-stage3.json'
prior = load(prior_path)
merged = {**prior['inputHashes'], **inputs, str(prior_path): digest(prior_path), str(evidence_path): digest(evidence_path)}
for name, wanted in merged.items():
    assert digest(name) == wanted, name
remaining = [entry for entry in prior['remainingAcceptance'] if 'loader' not in entry.lower() and 'factory' not in entry.lower()]
remaining.append('Recorded JVM-definition proof is limited to the actual inert Curios provider case; future binaries and actual client/authenticated multiplayer remain unaccepted.')
save(work / 'reports/native-continuation-acceptance-2ca083af-stage4.json', {
    'candidate': prior['candidate'], 'priorFrozenAcceptance': str(prior_path), 'newFocusedEvidence': str(evidence_path),
    'frozenSignedSessionRestartCrashRejectionSuitesReusedWithoutRerun': True,
    'recordedActualJvmDefinitionAndOriginalCuriosInertFactoryAccepted': True,
    'remainingAcceptance': remaining,
    'fullLifecycleAccepted': False, 'fullMigrationAccepted': False, 'promoted': False,
    'installable': False, 'deployed': False, 'inputHashes': merged
})
print(json.dumps({'proof': str(proof), 'focusedHashes': len(inputs), 'stage4Hashes': len(merged), 'failures': 0}, ensure_ascii=True))
