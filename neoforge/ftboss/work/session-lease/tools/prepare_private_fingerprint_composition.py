import difflib
import hashlib
import importlib.util
import json
import shutil
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
PATCH='C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe'
helper=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper)
module=importlib.util.module_from_spec(spec)
text=helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
parent=WORK/'reports/native-repair-private-timer-image-candidate-acf29373.json'
assert hashlib.sha256(parent.read_bytes()).hexdigest()=='e2b9094b511e3e82c513ed554c3528e850a3cdbd9c6f7b0203e1e2c9d1893db5'
receipt=json.loads(parent.read_text(encoding='utf-8'))
arguments=json.loads((Path(receipt['attempt'])/'complete-arguments316.json').read_text(encoding='utf-8'))
original=next(Path(value) for value in arguments if value.endswith('FoodWeaponMaterialFingerprint.java'))
target=WORK/'proposal-private-fingerprint-composition/net/farmingtales/ftboss/item/FoodWeaponMaterialFingerprint.java'
assert not target.exists()
target.parent.mkdir(parents=True,exist_ok=True)
shutil.copyfile(original,target)
before=original.read_text(encoding='utf-8-sig')
start=before.index('    private static CompoundTag composition(ItemStack var0) {')
end=before.index('    private static CompoundTag semanticTag(ItemStack var0)',start)
body=before[start:end]
for old,new,count in [
    ('CompoundTag var1 = new CompoundTag();','PrivateFingerprint416 var1 = new PrivateFingerprint416();',1),
    ('CompoundTag var14 = new CompoundTag();','PrivateFingerprint416 var14 = new PrivateFingerprint416();',1),
    ('return var1;','return var1.finish();',2),
    ('var25.add(var14);','var25.add(var14.finish());',1),
]:
    body=module.replace(body,old,new,count)
after=before[:start]+body+before[end:]
marker='        private void putInt(String key, int value) { put(key, net.minecraft.nbt.IntTag.valueOf(value)); }'
after=module.replace(after,marker,marker+'\n        private void putBoolean(String key, boolean value) { put(key, net.minecraft.nbt.ByteTag.valueOf(value)); }')
delta=list(difflib.unified_diff(before.splitlines(True),after.splitlines(True),n=3))
patch='*** Begin Patch\n*** Update File: '+target.relative_to(ROOT).as_posix()+'\n'+''.join('@@\n' if line.startswith('@@') else line for line in delta[2:])+'*** End Patch\n'
subprocess.run([PATCH,'--codex-run-as-apply-patch',patch],check=True,stdout=subprocess.DEVNULL)
assert target.read_text(encoding='utf-8-sig')==after
builder=(WORK/'tools/build_private_timer_expectation_image_v2.py').read_text(encoding='utf-8')
builder=module.replace(builder,'reports/native-repair-session-timer-expectation-candidate-73d460d2.json','reports/native-repair-private-timer-image-candidate-acf29373.json')
builder=module.replace(builder,'proposal-private-timer-expectation-v2','proposal-private-fingerprint-composition',2)
builder=module.replace(builder,'reports/native-repair-private-timer-image-v2-candidate.json','reports/native-repair-private-fingerprint-composition-candidate.json')
module.create(WORK/'tools/build_private_fingerprint_composition.py',builder)
output=WORK/'reports/private-fingerprint-composition-preparation-20261112.json'
assert not output.exists()
output.write_text(json.dumps({'parent':str(parent),'rootCause':'Unpublished material fingerprint composition writes Encoding through observed CompoundTag.putString inside original RANGED_SAVE','evidence':'build/repair-runtime-c86bee3a/private-image-failure.log','timerWholeImageAdvanceActuallyReturnedTrue':True,'existingRestrictedFingerprintFactoryReused':True,'changedProductSources':1,'sourceSha256':hashlib.sha256(target.read_bytes()).hexdigest(),'guardsUnchanged':True,'fullSessionAccepted':False,'installable':False,'deployed':False},indent=2)+'\n',encoding='utf-8')
print(json.dumps({'prepared':True,'changedProductSources':1,'existingRestrictedFactoryReused':True}))
