import hashlib
import json
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'build/boss-zicai-curio-neoforge-abi-v2-20261113'
JAVA = Path('C:/Program Files/Zulu/zulu-21/bin')

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def save(name, value):
    with (OUT / name).open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

assert not OUT.exists(), 'Inspect the existing audit before rerunning'
owner = json.loads((ROOT / 'WRITE_OWNER.json').read_text(encoding='utf-8'))
assert owner['githubSourceRemoteRecoveryVerified'] and owner['formalInstancesReadOnly']
assert owner['noNewWeaponRuns'] and owner['weaponValidationPaused']
prior = json.loads((WORK / 'reports/summon-fx-v8-candidate-b74fa0f4.json').read_text(encoding='utf-8'))
curios_candidates = list((Path(prior['attempt']) / 'server/mods').glob('*curios*.jar'))
assert len(curios_candidates) == 1
curios = curios_candidates[0]
context = WORK / 'build/boss-flight-ban-v1/compile-context.json'
classpath = json.loads(context.read_text(encoding='utf-8'))['classpath']
classpath = str(curios) + ';' + classpath
boss = WORK / 'releases/boss-core-stage27-erosion-damage-20261113/ftboss-boss-core-1.21.1-stage27-erosion-damage.jar'
original = ROOT / 'reference/forge-current.jar'
assert sha(boss) == '6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0'
assert sha(original) == 'c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
OUT.mkdir(parents=True)
classes = [
    'top.theillusivec4.curios.api.CuriosApi',
    'top.theillusivec4.curios.api.SlotContext',
    'top.theillusivec4.curios.api.type.capability.ICuriosItemHandler',
    'top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler',
    'top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler',
    'top.theillusivec4.curios.api.event.CurioCanEquipEvent',
    'top.theillusivec4.curios.api.event.CurioCanUnequipEvent',
    'net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Post',
    'net.neoforged.neoforge.event.tick.PlayerTickEvent$Post',
    'net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerRespawnEvent',
    'net.minecraft.world.item.ItemStack',
    'net.minecraft.nbt.CompoundTag',
]
inputs = {str(path): sha(path) for path in [Path(__file__), context, curios, boss, original]}
resolved = {}
for entry in classpath.split(';'):
    path = Path(entry)
    if path.suffix.lower() != '.jar' or not path.is_file():
        continue
    with zipfile.ZipFile(path) as bundle:
        names = set(bundle.namelist())
        selected = [name for name in classes if name not in resolved and name.replace('.', '/') + '.class' in names]
        if selected:
            inputs[str(path)] = sha(path)
        for name in selected:
            payload = bundle.read(name.replace('.', '/') + '.class')
            resolved[name] = {'jar': str(path), 'classSha256': hashlib.sha256(payload).hexdigest(), 'bytes': len(payload)}
assert set(resolved) == set(classes), sorted(set(classes) - set(resolved))
command = [str(JAVA / 'javap.exe'), '-classpath', classpath, '-public', *classes]
save('command.json', command)
with (OUT / 'public-api.txt').open('wb') as stream:
    result = subprocess.run(command, cwd=ROOT, stdout=stream, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
save('terminal.json', {'exitCode': result.returncode})
assert result.returncode == 0, 'Preserve failed ABI audit; inspect public-api.txt'
text = (OUT / 'public-api.txt').read_text(encoding='gb18030', errors='replace')
interesting = [line.strip() for line in text.splitlines() if any(fragment in line for fragment in ['getCuriosInventory(', 'getCurios(', 'getStacks(', 'getSlots(', 'setStackInSlot(', 'getStackInSlot(', 'getSlotContext(', 'getNewDamage(', 'setEquipResult(', 'setResult(', 'getEquipResult(', 'setCanEquip(', 'getCanEquip(', 'setCanUnequip(', 'getCanUnequip(', 'parseOptional(', 'parse(', ' save(', 'saveOptional(', 'lookupProvider(', 'registryAccess('])]
assert any('java.util.Optional<' in line and 'getCuriosInventory(' in line for line in interesting)
assert any('getNewDamage(' in line for line in interesting)
receipt = {'inputs': inputs, 'resolvedPrimaryClassFiles': resolved, 'publicApiLines': interesting, 'scope': 'Local primary NeoForge1.21.1 andCurios9.5.1 ABI inspection only. Does not establish effective gameplay absence,native baseline,candidate behavior or acceptance.', 'failedV1PreflightRetained': 'work/session-boss/build/boss-zicai-curio-neoforge-abi-v1-20261113/preflight-failure.json', 'productChanged': False, 'formalInstancesReadOnly': True, 'weaponsPaused': True, 'fullBossMigrationComplete': False, 'next': 'Use actual integrated-login ServerPlayer,registered variant8 eligible Boss andpublic Curios inventory for positive-damage baseline;then port complete original lifecycle andcodec semantics without private hooks.'}
save('audit-receipt.json', receipt)
print(json.dumps({'classes': len(resolved), 'publicApiLines': interesting, 'productChanged': False}), flush=True)
