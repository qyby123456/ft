import hashlib
import json
import re
from pathlib import Path
work=Path(__file__).resolve().parents[1]
helper=work/'tools/prove_native_ranged_use_head_delta.py'
head=work/'reports/native-ranged-use-head-delta-f4f2deae.json'
assert hashlib.sha256(helper.read_bytes()).hexdigest()==json.loads(head.read_text(encoding='utf-8'))['inputHashes'][str(helper)]
exec(compile(helper.read_text(encoding='utf-8').split('before = load(before_path)')[0],str(helper),'exec'))
before_path=work/'reports/native-repair-ranged-use-candidate-29296443.json'
after_path=work/'reports/native-repair-ranged-use-candidate-529ba4c9.json'
before=load(before_path);after=load(after_path)
for receipt in [before,after]:
    assert receipt['classCount']==1381 and receipt['effectiveSourceCount']==716 and receipt['guardCount']==208
    assert receipt['failures']==0 and not receipt['newClasses']
    for name,wanted in receipt['inputHashes'].items(): assert digest(Path(name))==wanted,name
previous=source_map(before);current=source_map(after)
assert previous.keys()==current.keys()
prefix='net.farmingtales.ftboss.'
behavior={prefix+'item.'+name for name in ['FoodWeaponSourceLedger','OwnerFieldInstallationCatalogue','OwnerFieldInstallationProof','OwnerNativeSemanticAuthority','NativeItemComponentObservation']}
behavior.add(prefix+'mixin.OwnerTridentOriginalCopyMixin')
bindings={prefix+'item.NativeCapabilityInstallationProof170',prefix+'item.NativeStackFieldRequirementProof172'}
assert {key for key in previous if previous[key][1]!=current[key][1]}==behavior|bindings
normalize=lambda text:re.sub('[a-f0-9]{64}','SHA256',text)
for key in bindings: assert normalize(previous[key][1])==normalize(current[key][1])
def remove(text,start,end):
    first=text.index(start);last=text.index(end,first);return text[:first]+text[last:]
ledger=prefix+'item.FoodWeaponSourceLedger';source=current[ledger][1]
block=source[source.index('    public static boolean nativeTridentPickupInventory('):source.index('    private static boolean nativeTridentWriterCustody(')]
for required in ['OwnerFieldCoverageRegistry.nativeTridentCertified()', 'frames.size() == 5', 'frames.size() == 3', 'arrow.getClass() == ThrownTrident.class',
 'arrow.getOwner() == player', 'player.serverLevel().getEntity(arrow.getUUID()) == arrow', 'proposed.operation.thread == Thread.currentThread()',
 'validReadOnly(snapshot, player)', 'lineage.origin == proposed', 'lineage.input == proposed.operation.weapon', 'candidate != copied',
 'candidate != proposed.operation.weapon', 'original.call(inventory, candidate)', 'original.call(picker, entity, count)',
 'slot >= 36', 'receipt.pickupCandidate', 'receipt.pickupOriginalImage', 'receipt.pickupInventory.setItem(receipt.pickupSlot, actual)',
 'OwnerNativeSemanticAuthority.nativeTridentPickupReady', 'if (!returned) throw failure;', 'OwnerRepairGenerationAuthority.unknownMutation(actual)',
 'receipt.pickupInventory = null;', 'receipt.pickupInProgress = false;']:
    assert required in block,required
source=remove(source,'    public static boolean nativeTridentPickupInventory(','    private static boolean nativeTridentWriterCustody(')
source=remove(source,'        private boolean pickupInProgress;','        private boolean profileFieldInitialized;')
assert source==previous[ledger][1]
semantic=prefix+'item.OwnerNativeSemanticAuthority';source=current[semantic][1]
block=source[source.index('    static boolean nativeTridentPickupReady('):source.index('    public static final class WeaponInput {')]
for required in ['caller.getDeclaringClass() != FoodWeaponSourceLedger.class', 'nativeTridentPickupInventory', 'nativeTridentPickupTaken',
 'stack.getCount() != 1', '!input.retired', 'input.thread != Thread.currentThread()', 'input.actual.get() != stack',
 'OwnerRepairGenerationAuthority.matches(stack, generation)', 'observed.nativeWearStorage(true) == input.root',
 'observed.nativeWearStorage(false) == input.components', 'NativeFoundationItemData.image(stack, input.registries)',
 'fields.ftboss$ownerNativeWeaponInput() == input', 'OwnerFullRepairGuard.lease(stack) == lease']:
    assert required in block,required
assert remove(source,'    static boolean nativeTridentPickupReady(','    public static final class WeaponInput {')==previous[semantic][1]
observation=prefix+'item.NativeItemComponentObservation';source=current[observation][1]
line='                || caller.getMethodName().equals("nativeTridentPickupReady") && caller.getDescriptor().equals("(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;Ljava/lang/Object;)Z")\n'
assert source.count(line)==1 and source.replace(line,'')==previous[observation][1]
mixin=prefix+'mixin.OwnerTridentOriginalCopyMixin';source=current[mixin][1]
start=source.index('    @WrapOperation(\n        method = {"tryPickup(')
assert source[:start]+source[source.rindex('\n}')+1:]==previous[mixin][1]
catalogue=prefix+'item.OwnerFieldInstallationCatalogue';source=current[catalogue][1]
start=source.index('        {\n                "net.farmingtales.ftboss.mixin.OwnerTridentOriginalCopyMixin",\n                "ftboss$ownerTridentPickupTaken",')
end=source.index('        {\n                "net.farmingtales.ftboss.mixin.OwnerTridentOriginalCopyMixin",\n                "ftboss$ownerTridentOriginalCopy",',start)
assert source[:start]+source[end:]==previous[catalogue][1]
proof=prefix+'item.OwnerFieldInstallationProof';source=current[proof][1];old=previous[proof][1]
start=source.index('        MethodNode pickup = ownerParent(host, "tryPickup(');end=source.index('\n    }',start)
source=source[:start]+'        return ownerShape(host, parent, List.of(), List.of(), parent.maxLocals);'+source[end:]
source=remove(source,'        int pickupBoundary = baseline.indexOf("native-trident-pickup-parent");','        try {locals = Integer.parseInt(baseline.getFirst().substring(7));}')
start=source.index('        return baseline.equals(ownerShape(host, parent, List.of(new int[]{first, position}), List.of(nativeTridentCopy()), locals))')
end=source.index('\n    static boolean nativeTickSite(',source.index('    private static boolean nativeTridentTakeWrapper(',start))
source=source[:start]+'        return baseline.equals(ownerShape(host, parent, List.of(new int[]{first, position}), List.of(nativeTridentCopy()), locals));\n    }\n'+source[end:]
line='        if (result.equals("Ljava/lang/Boolean;")) expected.add("184:java/lang/Boolean:valueOf:(Z)Ljava/lang/Boolean;:false");\n'
assert source.count(line)==1;source=source.replace(line,'')
assert normalize(source)==normalize(old)
actual_proof=current[proof][1]
for required in ['native-trident-pickup-parent','native-trident-touch-parent','nativeTridentPickupWrapper','nativeTridentTakeWrapper',
 'parent.maxLocals != 4','parent.maxLocals != 5','List.of("58:3", "58:2", "25:0", "25:2", "25:3")',
 'List.of("54:4", "58:3", "58:2", "25:0", "25:2", "25:3", "21:4")',
 'nativeChainBridge(host, dynamic', 'original.signature', 'annotationKeys(wrap, List.of("method", "at", "require"))',
 'nativeTridentTakeOperation()', 'nativeTridentPickupOperation()']:
    assert required in actual_proof,required
start=actual_proof.index('static boolean nativeAssignmentObservation185')
part=actual_proof[start:actual_proof.index('\n    static boolean complete',start)]
names=re.findall('"([^"]+)"',re.search(r'String\[\] names = \{([^}]+)\}',part).group(1))
pins=re.findall('[a-f0-9]{64}',re.search(r'String\[\] hashes = \{([^}]+)\}',part).group(1))
assert len(names)==len(pins)==9
compiled=Path(after['attempt'])/'complete-classes/net/farmingtales/ftboss'
for name,pin in zip(names,pins):
    path=compiled/(name+'.class')
    if path.exists(): assert digest(path)==pin,name
    else:
        assert name=='item/FoodWeaponData'
        assert pin in previous[proof][1]
for path in [Path(__file__),helper,head]:digest(path)
output=work/'reports/native-trident-pickup-source-delta-529ba4c9.json'
result={'failures':0,'candidate':str(after_path),'classes':1381,'sources':716,'guards':208,
 'behaviorChangedSources':sorted(behavior),'hashBindingOnlySources':sorted(bindings),'unchangedSources':708,
 'exactInverseOutsidePickupAndWholeParentProof':True,'allCompiledTimerAssignmentPinsMatch':True,
 'externalFoodWeaponDataPinUnchanged':True,'runtimeAcceptedByThisReceipt':False,
 'installable':False,'deployed':False,'inputHashes':hashes}
with output.open('x',encoding='utf-8') as stream:json.dump(result,stream,ensure_ascii=False,indent=2)
print(json.dumps({'evidence':output.name,'inputHashes':len(hashes),'unrelatedSourcesUnchanged':708}))
