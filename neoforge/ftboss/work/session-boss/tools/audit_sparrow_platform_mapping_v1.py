import hashlib
import json
import re
import zipfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'build/boss-sparrow-evasion-platform-audit-v1-20261114'
assert not OUT.exists()
OUT.mkdir()
original = ROOT / 'reference/forge-current.jar'
mcp = Path('C:/Users/qyby/AppData/Roaming/.minecraft/libraries/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/mcp_config-1.20.1-20230612.114412.zip')
official = Path('C:/Users/qyby/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client_mappings.txt')
def sha(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()
assert sha(original) == 'c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
selected = ['net/farmingtales/ftboss/boss/ai/SparrowHitEvasion.class', 'net/farmingtales/ftboss/boss/ai/SparrowHitEvasion$State.class', 'net/farmingtales/ftboss/util/SparrowHitEvasionRules.class']
with zipfile.ZipFile(original) as bundle:
    payloads = {name: bundle.read(name) for name in selected}
needed = {value.decode() for payload in payloads.values() for value in re.findall(rb'[mf]_[0-9]+_', payload)}
lines = official.read_text(encoding='utf-8').splitlines()
classes = {}
for line in lines:
    if line and not line.startswith((' ', '#')) and ' -> ' in line:
        name, obfuscated = line[:-1].split(' -> ')
        classes[name] = obfuscated
primitive = {'void':'V','boolean':'Z','byte':'B','char':'C','short':'S','int':'I','long':'J','float':'F','double':'D'}
def descriptor(name):
    count = 0
    while name.endswith('[]'):
        count += 1
        name = name[:-2]
    return '[' * count + (primitive[name] if name in primitive else 'L' + classes.get(name, name.replace('.', '/')) + ';')
methods = {}
fields = {}
owner = None
for line in lines:
    if line and not line.startswith((' ', '#')) and ' -> ' in line:
        owner = line[:-1].split(' -> ')[1]
    elif line.startswith('    ') and ' -> ' in line:
        declaration, obfuscated = line.strip().split(' -> ')
        declaration = re.sub(r'^\d+:\d+:', '', declaration)
        if '(' in declaration:
            prefix, remainder = declaration.split('(', 1)
            return_type, name = prefix.rsplit(' ', 1)
            arguments = remainder.split(')', 1)[0]
            desc = '(' + ''.join(descriptor(part) for part in arguments.split(',') if part) + ')' + descriptor(return_type)
            methods[(owner, obfuscated, desc)] = name
        else:
            kind, name = declaration.rsplit(' ', 1)
            fields[(owner, obfuscated)] = name
with zipfile.ZipFile(mcp) as bundle:
    mcp_lines = bundle.read('config/joined.tsrg').decode('utf-8').splitlines()
resolved = {}
proof = {}
owner = None
for line in mcp_lines[1:]:
    if not line.startswith('\t'):
        owner = line.split()[0]
    elif line.startswith('\t') and not line.startswith('\t\t'):
        parts = line.strip().split()
        symbol = next((part for part in parts if part in needed), None)
        if symbol is None:
            continue
        name = methods.get((owner, parts[0], parts[1])) if len(parts) > 1 and parts[1].startswith('(') else fields.get((owner, parts[0]))
        assert name is not None, (symbol, owner, parts)
        if symbol in resolved:
            assert resolved[symbol] == name, (symbol, resolved[symbol], name)
        resolved[symbol] = name
        proof.setdefault(symbol, []).append({'declaredObfuscatedOwner': owner, 'mcpMemberLine': line.strip(), 'officialResolvedName': name})
assert set(resolved) == needed, sorted(needed - set(resolved))
report = {'originalArchiveSha256': sha(original), 'originalClassHashes': {name: hashlib.sha256(payload).hexdigest() for name, payload in payloads.items()}, 'mcpConfigSha256': sha(mcp), 'officialMappingSha256': sha(official), 'memberNames': dict(sorted(resolved.items())), 'subsetMappingProof': proof, 'classNames': {'net/minecraft/world/level/pathfinder/BlockPathTypes':'net/minecraft/world/level/pathfinder/PathType'}, 'completeUnmodifiedMappingsNotExported': True, 'resourceFilesNotExported': True}
(OUT / 'mapping-proof.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'usedMemberMappings': len(resolved), 'members': resolved, 'originalClassHashes': report['originalClassHashes']}), flush=True)
