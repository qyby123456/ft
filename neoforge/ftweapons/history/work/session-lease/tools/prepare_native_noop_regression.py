import json
import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[3]
work = Path(__file__).resolve().parents[1]
owner = json.loads((root / 'WRITE_OWNER.json').read_text(encoding='utf-8'))
assert owner['activeWriterThreadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
patcher = 'C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe'

def add(relative, text):
    target = root / relative
    assert not target.exists(), str(target)
    chunks = [[]]
    size = 0
    for line in text.splitlines():
        if size + len(line) > 6000 and chunks[-1]:
            chunks.append([])
            size = 0
        chunks[-1].append(line)
        size += len(line) + 1
    previous = []
    for chunk in chunks:
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

original = root / 'batches/413-food-enchantment-removal/proposal/net/farmingtales/ftboss/item/FoodWeaponEvolution.java'
text = original.read_text(encoding='utf-8-sig')
before = '        FoodWeaponData.write(var0, "FTBossFoodEvolution", var1);'
assert text.count(before) == 1
text = text.replace(before, '        if (!java.util.Objects.equals(data(var0), var1)) {\n' + before + '\n        }')
add('work/session-lease/proposal-native/net/farmingtales/ftboss/item/FoodWeaponEvolution.java', text)
builder = (work / 'tools/build_native_repair_melee_noop.py').read_text(encoding='utf-8')
builder = builder.replace('native-repair-melee-noop-candidate.json', 'native-repair-semantic-noop-candidate.json')
add('work/session-lease/tools/build_native_repair_semantic_noop.py', builder)
print('Prepared isolated evolution no-op proposal and builder; historical sources unchanged')
