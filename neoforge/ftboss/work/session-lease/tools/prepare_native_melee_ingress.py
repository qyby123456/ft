import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = Path(__file__).resolve().parents[1]
patcher = 'C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe'

def add(relative, text):
    assert not (root / relative).exists(), relative
    lines = text.splitlines()
    previous = []
    while lines:
        chunk = []
        length = 0
        while lines and length + len(lines[0]) < 6000:
            line = lines.pop(0)
            chunk.append(line)
            length += len(line) + 1
        assert chunk
        if previous:
            body = '*** Update File: ' + relative + '\n@@\n' + ''.join(' ' + line + '\n' for line in previous[-4:])
            suffix = '*** End of File\n'
        else:
            body = '*** Add File: ' + relative + '\n'
            suffix = ''
        patch = '*** Begin Patch\n' + body + ''.join('+' + line + '\n' for line in chunk) + suffix + '*** End Patch\n'
        result = subprocess.run([patcher, '--codex-run-as-apply-patch', patch], cwd=root, capture_output=True)
        assert result.returncode == 0, result.stderr.decode('utf-8', errors='replace')
        previous.extend(chunk)

source = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022/batches/316-native-semantic-alias-read/build/complete713-semantic-alias316-ad24b62e/source-at-complete/0010/FoodWeaponBondHits.java')
text = source.read_text(encoding='utf-8-sig')
before = '            if (var4) {\n                FoodWeaponBondHits.Melee var6 = MELEE.get(var1.getUUID());'
after = '            if (var4 && !(FoodWeaponSourceLedger.actualHurtSnapshot(var2) && "NATIVE_MELEE_ACTUAL_HURT".equals(var2.getString("DamagePurpose")))) {\n                FoodWeaponBondHits.Melee var6 = MELEE.get(var1.getUUID());'
assert text.count(before) == 1
text = text.replace(before, after)
add('work/session-lease/proposal-native/net/farmingtales/ftboss/item/FoodWeaponBondHits.java', text)
builder = (work / 'tools/build_native_repair_semantic_noop.py').read_text(encoding='utf-8')
builder = builder.replace('native-repair-semantic-noop-candidate.json', 'native-repair-melee-ingress-candidate.json')
add('work/session-lease/tools/build_native_melee_ingress.py', builder)
print('Prepared original BondHits scoped actual-melee snapshot resolution and dedicated candidate builder')
