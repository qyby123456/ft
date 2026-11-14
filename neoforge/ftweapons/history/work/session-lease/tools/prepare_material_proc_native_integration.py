import hashlib
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
HELPER=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
namespace={'__file__':str(HELPER)}
helper=HELPER.read_text(encoding='utf-8')
exec(helper[:helper.index('probe=(WORK/')],namespace)
create=namespace['create']
def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
report=WORK/'reports/native-repair-aliased-entity-expected-image-candidate-5f283607.json'
assert digest(report)=='482fd005e8287ef41e48eced0ca5bd740df8b78580526165a2b550d47f666b10'
frozen=load(report)
for name,wanted in frozen['inputHashes'].items():assert digest(name)==wanted,name
arguments=load(Path(frozen['attempt'])/'complete-arguments316.json')
OUTPUT=WORK/'proposal-material-proc-native-integration'
assert not OUTPUT.exists()
INPUTS={}
SOURCES={}
def original(name):
    path=next(Path(value) for value in arguments if value.replace('\\','/').endswith('/'+name+'.java'))
    INPUTS[str(path)]=digest(path)
    return path.read_text(encoding='utf-8')
def write(name,text):
    path=OUTPUT/'net/farmingtales/ftboss/item'/(name+'.java')
    create(path,text)
    SOURCES[name]=str(path)
def swap(text,before,after):
    assert text.count(before)==1,(before[:120],text.count(before))
    return text.replace(before,after)

text=original('FoodWeaponFinalDamage')
start=text.index('    static boolean acceptsOwnerReserveLeaf(')
end=text.index('    static boolean matchesOwnerWriterPublication(',start)
material=text[start:end].replace('acceptsOwnerReserveLeaf','acceptsOwnerReserveMaterialLeaf').replace('ownerNativeLeafAccepted','ownerMaterialLeafAccepted').replace('FoodWeaponFinalDamage.EvolutionProducer','FoodWeaponFinalDamage.MaterialProducer')
text=text[:end]+material+text[end:]
write('FoodWeaponFinalDamage',text)

text=original('FoodWeaponHitPlan')
text=swap(text,'var0xx.getMethodName().equals("acceptsOwnerReserveLeaf")','(var0xx.getMethodName().equals("acceptsOwnerReserveLeaf") || var0xx.getMethodName().equals("acceptsOwnerReserveMaterialLeaf"))')
marker='    private static final class MaterialPowersLeaf extends FoodWeaponHitPlan.TypedLeaf {'
text=swap(text,marker,'''    static boolean materialPowersLeaf(Object leaf) {
        return leaf instanceof MaterialPowersLeaf;
    }

    static OwnerMaterialProcAuthority.Program materialProcProgram(Object leaf) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != OwnerTimerSemanticAuthority.Book.class)
            throw new SecurityException("Original material quote requires its semantic book");
        return leaf instanceof MaterialPowersLeaf material ? material.materialProgram : null;
    }

'''+marker)
text=swap(text,'        private final BeforeMitigation event;\n        private final ServerPlayer player;','        private final BeforeMitigation event;\n        private final OwnerMaterialProcAuthority.Program materialProgram;\n        private final ServerPlayer player;')
text=swap(text,'            this.event = var1;\n            this.player = var2;\n            this.materials = List.copyOf(var3);','            this.event = var1;\n            this.player = var2;\n            this.materials = List.copyOf(var3);\n            this.materialProgram = OwnerMaterialProcAuthority.capture(var2, var1.getSource(), this.materials);')
text=swap(text,'            WeaponMaterialPowers.applyClosedHitSteps(this.event, this.player, this.materials, this.potentialPets, var1, var2);','''            OwnerMaterialProcAuthority.Leaf observation = FoodWeaponSourceLedger.ownerMaterialLeafEntered(var1, var2, this);
            try {
                WeaponMaterialPowers.applyClosedHitSteps(this.event, this.player, this.materials, this.potentialPets, var1, var2);
            } catch (Throwable failure) {
                OwnerMaterialProcAuthority.leave(observation, failure);
                throw failure;
            }
            OwnerMaterialProcAuthority.leave(observation, null);''')
write('FoodWeaponHitPlan',text)

text=original('FoodWeaponSourceLedger')
start=text.index('    static boolean ownerNativeLeafAccepted(')
end=text.index('    public static OwnerTimerSemanticAuthority.LeafObservation ownerTimerLeafEntered',start)
material=text[start:end].replace('ownerNativeLeafAccepted','ownerMaterialLeafAccepted').replace('var3 instanceof OwnerNativeLeafFields','FoodWeaponHitPlan.materialPowersLeaf(var3)').replace('acceptsOwnerReserveLeaf','acceptsOwnerReserveMaterialLeaf')
text=text[:end]+material+text[end:]
start=text.index('    public static OwnerTimerSemanticAuthority.LeafObservation ownerTimerLeafEntered')
end=text.index('    public static OwnerNativeSemanticAuthority.Admission ownerNativePutEntered',start)
material=text[start:end].replace('public static OwnerTimerSemanticAuthority.LeafObservation ownerTimerLeafEntered','static OwnerMaterialProcAuthority.Leaf ownerMaterialLeafEntered').replace('OwnerTimerSemanticAuthority.enter(var5, var3, var2)','OwnerMaterialProcAuthority.enter(var5, var3, var4, var2)')
text=text[:end]+material+text[end:]
write('FoodWeaponSourceLedger',text)

text=original('OwnerFieldExpectedAuthority')
text=swap(text,'                                var12.put(var14, FoodWeaponSourceLedger.ownerNativeLeafAccepted(var0, var7, var9, var14));','''                                if (FoodWeaponSourceLedger.ownerMaterialLeafAccepted(var0, var7, var9, var14)) {
                                    var0.timerSemantics.quoteMaterial(var14, var2, var3, var4);
                                    var12.put(var14, false);
                                } else {
                                    var12.put(var14, FoodWeaponSourceLedger.ownerNativeLeafAccepted(var0, var7, var9, var14));
                                }''')
write('OwnerFieldExpectedAuthority',text)

text=original('OwnerTimerSemanticAuthority').replace('.groups','.timerGroups').replace('> groups =','> timerGroups =')
marker='        private final IdentityHashMap<Object, OwnerTimerSemanticAuthority.Group> timerGroups = new IdentityHashMap<>();'
text=swap(text,marker,marker+'''
        private final IdentityHashMap<Object, Object> groups = new IdentityHashMap<>();
        final IdentityHashMap<Object, OwnerMaterialProcAuthority.Group> materialGroups = new IdentityHashMap<>();''')
text=swap(text,'                        this.timerGroups.put(var16.leaf(), var11);','                        this.timerGroups.put(var16.leaf(), var11);\n                        this.groups.put(var16.leaf(), var11);')
text=swap(text,'        void quote(\n','''        void quoteMaterial(Object leaf, Object weapon, Object player, Object source) {
            if (this.frozen || !OwnerTimerSemanticAuthority.caller(OwnerFieldExpectedAuthority.class, "quoteTimers")
                || this.groups.containsKey(leaf) || !FoodWeaponHitPlan.materialPowersLeaf(leaf))
                throw new SecurityException("Unique original material reservation required");
            OwnerMaterialProcAuthority.Program program = FoodWeaponHitPlan.materialProcProgram(leaf);
            OwnerMaterialProcAuthority.Group group = OwnerMaterialProcAuthority.reserve(this, leaf, program, player, source);
            this.materialGroups.put(leaf, group);
            this.groups.put(leaf, group);
        }

        void quote(
''')
text=swap(text,'                if (var3 == Kind.NATIVE_PUT) {','''                if (var3 == Kind.MATERIAL_PROC_WRITE) {
                    return OwnerMaterialProcAuthority.resolve(this, var1, var2, var4, var5, var6);
                }
                if (var3 == Kind.NATIVE_PUT) {''')
text=swap(text,'                OwnerNativeSemanticAuthority.PutBinding var5 = this.materialized.get(var1.expected);','''                if (var1.expected.kind == Kind.MATERIAL_PROC_WRITE) {
                    OwnerMaterialProcAuthority.returned(this, var1, var2);
                    return;
                }
                OwnerNativeSemanticAuthority.PutBinding var5 = this.materialized.get(var1.expected);''')
text=swap(text,'                for (OwnerTimerSemanticAuthority.Group var2 : this.timerGroups.values()) {','''                for (OwnerMaterialProcAuthority.Group material : this.materialGroups.values()) {
                    if (!material.done) return false;
                }
                for (OwnerTimerSemanticAuthority.Group var2 : this.timerGroups.values()) {''')
write('OwnerTimerSemanticAuthority',text)

text=original('FoodWeaponExpectedTrace')
text=swap(text,'        Operation nativeCapture() {','''        Operation materialProcWrite(String key, MaterialProcExpectedPlan.Write write) {
            this.writable();
            if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != OwnerMaterialProcAuthority.class
                || key == null || write == null || !FoodWeaponExpectedTrace.same(this.expected.player, write.playerBefore()))
                throw new SecurityException("Independent material write image required");
            State after = this.expected.replacePlayer(write.playerAfter(), false, false);
            Operation operation = new Operation(this.operations.size(), Kind.MATERIAL_PROC_WRITE, this.expected, this.expected,
                after, "material-proc", key, write.argument(), new Mutation[0], null);
            this.operations.add(operation);
            this.expected = after;
            return operation;
        }

        Operation nativeCapture() {''')
text=swap(text,'        NATIVE_CAPTURE,','        NATIVE_CAPTURE,\n        MATERIAL_PROC_WRITE,')
write('FoodWeaponExpectedTrace',text)

text=original('FoodWeaponOriginalSequence')
text=swap(text,'        void originalCopyBegins(','''        State materialProcCursor() {
            if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != OwnerMaterialProcAuthority.class)
                throw new SecurityException("Actual material observer required");
            return this.expectedCursor;
        }

        void materialProcReturned(FoodWeaponOriginalSequence.Step step, OwnerMaterialProcAuthority.Receipt receipt) {
            if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != OwnerMaterialProcAuthority.class
                || !this.active(step) || step.expected.kind != Kind.MATERIAL_PROC_WRITE || step.phase != 0
                || step.originalReturned || receipt == null || !receipt.accepts(this, step)) {
                this.revoke(step, 1, null);
                return;
            }
            step.entered = true;
            step.directFieldsVerified = true;
            step.originalReturned = true;
            step.phase = 5;
        }

        void originalCopyBegins(''')
write('FoodWeaponOriginalSequence',text)

text=original('OwnerFullRepairGuard')
text=swap(text,'                if (OwnerNativeSemanticAuthority.plannedPassiveRootMutation(var0, var1)) {','''                if (OwnerMaterialProcAuthority.plannedMutation(var0, var1)) {
                    return;
                }

                if (OwnerNativeSemanticAuthority.plannedPassiveRootMutation(var0, var1)) {''')
write('OwnerFullRepairGuard',text)

text=original('WeaponMaterialPowers')
text=swap(text,'        int var7 = WeaponMaterialPowerRules.baseCooldown(var1);','''        OwnerMaterialProcAuthority.acquired(var0, var1, var2, var3, var5, var6);
        int var7 = WeaponMaterialPowerRules.baseCooldown(var1);''')
text=swap(text,'            var6.putLong("Stamp", var3);','            OwnerMaterialProcAuthority.putStamp(var6, "Stamp", var3);')
text=swap(text,'            var6.putInt("Delay", WeaponMaterialPowerRules.cooldown(var1, var2));','            OwnerMaterialProcAuthority.putDelay(var6, "Delay", WeaponMaterialPowerRules.cooldown(var1, var2));')
text=swap(text,'            var5.put(var1.id(), var6);','            OwnerMaterialProcAuthority.putCompound(var5, var1.id(), var6);')
text=swap(text,'            var0.getPersistentData().put("FTBossMaterialPowers", var5);','            OwnerMaterialProcAuthority.putCompound(var0.getPersistentData(), "FTBossMaterialPowers", var5);')
text=swap(text,'            boolean var5 = proc(var2, var3, var4);\n            return var5 && FoodWeaponHitPlan.leafLive(var0, var1);','''            OwnerMaterialProcAuthority.Proc observation = OwnerMaterialProcAuthority.begin(var0, var1, var2, var3, var4);
            boolean var5;
            try {
                var5 = proc(var2, var3, var4);
            } catch (Throwable failure) {
                OwnerMaterialProcAuthority.finished(observation, false, failure);
                throw failure;
            }
            OwnerMaterialProcAuthority.finished(observation, var5, null);
            return var5 && FoodWeaponHitPlan.leafLive(var0, var1);''')
text=swap(text,'    private static boolean proc(ServerPlayer var0, Material var1, int var2) {','''    static int quoteClosedMaterialFood(ServerPlayer player, DamageSource source, Material material) {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != OwnerMaterialProcAuthority.class)
            throw new SecurityException("Original closed material quote required");
        return foodCount(player, source, material);
    }

    private static boolean proc(ServerPlayer var0, Material var1, int var2) {''')
write('WeaponMaterialPowers',text)
planner=WORK/'proposal-material-proc-expected-plan/net/farmingtales/ftboss/item/MaterialProcExpectedPlan.java'
INPUTS[str(planner)]=digest(planner)
write('MaterialProcExpectedPlan',planner.read_text(encoding='utf-8'))
for name,wanted in frozen['inputHashes'].items():assert digest(name)==wanted,name
manifest={'baseCandidate':str(report),'baseCandidateSha256':digest(report),'inputHashes':INPUTS,'sources':SOURCES,
          'productCompileAccepted':False,'nativeIntegrationAccepted':False,'installable':False,'deployed':False}
with (OUTPUT/'preparation.json').open('x',encoding='utf-8') as stream:json.dump(manifest,stream,ensure_ascii=False,indent=2)
print(json.dumps({'sourcesPrepared':len(SOURCES),'output':str(OUTPUT)},ensure_ascii=False),flush=True)
