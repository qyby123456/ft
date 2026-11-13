import re
import subprocess
import sys
import uuid
from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(HISTORY / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
binding = LOCAL / 'batches/411-six-weapon-binding/reports/actual-incremental411-experimental.json'
for name, expected in load(binding)['inputHashes'].items():
    assert digest(name) == expected, name
prior = Path(load(binding)['attempt'])
arguments = load(prior / 'complete-arguments316.json')
originals = [Path(value) for value in arguments if value.endswith('.java')]

def block_end(text, start):
    depth = 0
    token = re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/|[{}]')
    for match in token.finditer(text, start):
        if match.group() == '{':
            depth += 1
        elif match.group() == '}':
            depth -= 1
            if depth == 0:
                return match.end()
    raise AssertionError('Unclosed Java block')

changes = []
def replace_body(text, name, body):
    pattern = r'(?m)^    (?:public|private|protected) static [^\n(]+?\s+' + re.escape(name) + r'\('
    matches = list(re.finditer(pattern, text))
    assert len(matches) == 1, name
    start = text.index('{', matches[0].start())
    end = block_end(text, start)
    changes.append({'method': name, 'removedBodySha256': __import__('hashlib').sha256(text[start:end].encode('utf-8')).hexdigest()})
    return text[:start] + '{\n        ' + body + '\n    }' + text[end:]

targets = []
inputs = []
for name in ['FoodWeaponEvolution.java', 'ArmorFoodBlessings.java', 'StatueHubScreen.java', 'OrdinaryStatueHubScreen.java']:
    original = next(path for path in originals if path.name == name)
    inputs.append(original)
    text = original.read_text(encoding='utf-8')
    if name == 'FoodWeaponEvolution.java':
        for method in ['applyFoodBlessing', 'applyFoodBlessingFromUi', 'isBlessingCandidate', 'ftboss$legacyOrdinary$applyFoodBlessing', 'ftboss$legacyOrdinary$applyFoodBlessingFromUi', 'ftboss$legacyOrdinary$isBlessingCandidate', 'hasWashableBlessing']:
            text = replace_body(text, method, 'return false;')
        for method in ['foodBlessingList', 'ftboss$legacyOrdinary$foodBlessingList']:
            text = replace_body(text, method, 'return new ListTag();')
        for method in ['applyFoodBlessingHits', 'foodBlessingDefense', 'foodBlessingKillFeast', 'grindstoneBlessing']:
            text = replace_body(text, method, '')
        for method in ['foodBlessingViews', 'ftboss$legacyOrdinary$foodBlessingViews']:
            text = replace_body(text, method, 'return List.of();')
        for method in ['addFoodBlessingAttributes', 'ftboss$legacyOrdinary$addFoodBlessingAttributes', 'addFoodBlessingTooltip']:
            text = replace_body(text, method, '')
        marker = '        ListTag var8 = foodBlessingList(var6);'
        assert text.count(marker) == 1
        text = text.replace(marker, marker + '\n        if (var8.isEmpty()) return;')
        text = text.replace('import net.minecraft.world.item.enchantment.EnchantmentHelper;\n', '').replace('import net.minecraft.tags.EnchantmentTags;\n', '')
    elif name == 'ArmorFoodBlessings.java':
        for method in ['eligible', 'hasRoom', 'apply']:
            text = replace_body(text, method, 'return false;')
        text = replace_body(text, 'entries', 'return new ListTag();')
        text = replace_body(text, 'views', 'return List.of();')
        for method in ['attributes', 'defend', 'poison', 'tickStart', 'tickEnd', 'damaged']:
            text = replace_body(text, method, '')
        text, count = re.subn(r'@EventBusSubscriber\([\s\S]*?\)\s*(?=public final class ArmorFoodBlessings)', '', text)
        assert count == 1
    else:
        text = re.sub(r'(?m)^\s*case ENCHANT:\s*\n\s*this.renderEnchant\([^\n]+\);\s*\n\s*break;\s*\n', '\n', text)
        for method in ['renderEnchant', 'blessingSlots']:
            match = re.search(r'(?m)^    private [^\n]+\b' + method + r'\(', text)
            assert match is not None, method
            start = text.index('{', match.start())
            end = block_end(text, start)
            text = text[:match.start()] + text[end:]
        marker = 'if (this.section == ' + name.removesuffix('.java') + '.Section.ENCHANT) {'
        while marker in text:
            start = text.index(marker)
            end = block_end(text, text.index('{', start))
            text = text[:start] + text[end:]
        text, count = re.subn(r'(?m)^\s*ENCHANT\("screen.ftboss.statue_hub.tab.enchant"\),\s*\n', '\n', text)
        assert count == 1
        assert 'Section.ENCHANT' not in text and 'case ENCHANT' not in text and 'renderEnchant' not in text and 'blessingSlots' not in text
    target = BATCH / 'proposal/net/farmingtales/ftboss' / ('client/screen' if name.endswith('Screen.java') else 'item') / name
    package = re.search(r'^package ([^;]+);', text).group(1)
    target = BATCH / 'proposal' / package.replace('.', '/') / name
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(text, encoding='utf-8')
    targets.append(target)
attempt = BATCH / 'build' / ('remove-food-enchant413-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
classpath = str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
command = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(attempt / 'prototype-classes'), *map(str, targets)]
save(attempt / 'compile-command.json', command)
with (attempt / 'compile.log').open('wb') as stream:
    terminal = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
save(attempt / 'compile-terminal.json', {'exitCode': terminal.returncode})
assert terminal.returncode == 0, str(attempt / 'compile.log')
save(BATCH / 'reports/actual-enchantment413-compiled-not-adopted.json', {'attempt': str(attempt), 'failures': 0, 'removedFoodEnchantmentBodies': changes, 'twoEnchantSectionsRemoved': True, 'armorBlessingSubscriberRemoved': True, 'legacyProtocolRejectionShellsRetained': True, 'vanillaGlobalEnchantmentsChanged': False, 'cumulativeCandidateUpdated': False, 'actualRuntimeAccepted': False, 'retirementComplete': False, 'remainingWork': ['ASM differential and synthetic lambda identity changes', 'cumulative resource rebinding', 'retired data effect and no-consumption runtime tests', 'protocol/resource residue removal and client GL verification'], 'installable': False, 'inputHashes': {str(path): digest(path) for path in [binding, Path(__file__), *inputs, *targets]}, 'outputHashes': {str(path): digest(path) for path in (attempt / 'prototype-classes').rglob('*.class')}})
print({'compiled': True, 'attempt': str(attempt), 'removedBodies': len(changes), 'cumulativeCandidateUpdated': False})
