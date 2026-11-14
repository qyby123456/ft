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

binding = LOCAL / 'batches/408-native-composition-binding/reports/actual-incremental408-experimental.json'
diagnostic = LOCAL / 'batches/409-native-composition-runtime/reports/actual-first-rejection409-diagnostic.json'
for receipt in [binding, diagnostic]:
    for name, expected in load(receipt)['inputHashes'].items():
        assert digest(name) == expected, name
baseline = load(LOCAL / 'baseline.json')
for family in ['forge', 'neoforge']:
    assert digest(LOCAL / baseline[family]['snapshot']) == baseline[family]['sha256']
prior = Path(load(binding)['attempt'])
arguments = load(prior / 'complete-arguments316.json')
original = Path(next(value for value in arguments if value.endswith('FoodWeaponEvolution.java')))
text = original.read_text(encoding='utf-8')
changed = []
for name, result in [('isEvolutionExcluded', 'true'), ('ftboss$legacyOrdinary$isEvolutionExcluded', 'true'),
        ('weaponCandidate', 'false'), ('ftboss$legacyOrdinary$weaponCandidate', 'false')]:
    marker = 'boolean ' + name + '(ItemStack var0) {'
    assert text.count(marker) == 1, name
    text = text.replace(marker, marker + '\n        if (!WeaponSystemScope410.supports(var0)) return ' + result + ';')
    changed.append(name)
for name in ['nativeProfile', 'ftboss$legacyOrdinary$nativeProfile']:
    marker = 'FoodWeaponEvolution.Profile ' + name + '(ItemStack var0) {'
    assert text.count(marker) == 1, name
    text = text.replace(marker, marker + '\n        if (!WeaponSystemScope410.supports(var0)) return null;')
    changed.append(name)
proposal = BATCH / 'proposal/net/farmingtales/ftboss/item'
proposal.mkdir(parents=True, exist_ok=True)
(proposal / 'FoodWeaponEvolution.java').write_text(text, encoding='utf-8')
scope = '''package net.farmingtales.ftboss.item;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

public final class WeaponSystemScope410 {
    private WeaponSystemScope410() {}

    public static boolean supports(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof BowItem
            || stack.getItem() instanceof CrossbowItem || stack.getItem() instanceof AxeItem
            || stack.getItem() instanceof TridentItem) return true;
        for (Class<?> type = stack.getItem().getClass(); type != null; type = type.getSuperclass()) {
            if (type.getName().equals("io.redspace.ironsspellbooks.item.CastingItem")) return true;
        }
        return FourWeaponFoundationAdapter.matchesType(stack, "bow")
            || FourWeaponFoundationAdapter.matchesType(stack, "crossbow")
            || FourWeaponFoundationAdapter.matchesType(stack, "axe")
            || FourWeaponFoundationAdapter.matchesType(stack, "trident")
            || FourWeaponFoundationAdapter.matchesType(stack, "staff");
    }
}
'''
(proposal / 'WeaponSystemScope410.java').write_text(scope, encoding='utf-8')
policy = '''package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class WeaponAttachmentPolicy {
    private WeaponAttachmentPolicy() {}

    public static boolean excluded(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!WeaponSystemScope410.supports(stack)) return true;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && SwordCompatibilityRules.attachmentsExcluded(id.toString());
    }
}
'''
(proposal / 'WeaponAttachmentPolicy.java').write_text(policy, encoding='utf-8')
attempt = BATCH / 'build' / ('six-scope410-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
classpath = str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
sources = list(proposal.glob('*.java'))
command = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(attempt / 'prototype-classes'), *map(str, sources)]
save(attempt / 'compile-command.json', command)
with (attempt / 'compile.log').open('wb') as stream:
    terminal = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
save(attempt / 'compile-terminal.json', {'exitCode': terminal.returncode})
assert terminal.returncode == 0, str(attempt / 'compile.log')
save(BATCH / 'reports/actual-scope410-compiled-not-adopted.json', {
    'attempt': str(attempt), 'failures': 0, 'retainedFamilies': ['sword', 'bow', 'crossbow', 'axe', 'trident', 'staff'],
    'changedEvolutionMethods': changed, 'legacyToolScopeRemovedAtAdmission': True,
    'cumulativeCandidateUpdated': False, 'actualRuntimeAccepted': False, 'enchantmentRemovalComplete': False,
    'remainingWork': ['legacy UI and direct packet paths', 'enchantment removal', 'cumulative authority rebinding', 'actual six-family runtime and negative tool checks'],
    'installable': False, 'inputHashes': {str(path): digest(path) for path in [binding, diagnostic, LOCAL / 'baseline.json', original, Path(__file__), *sources]},
    'outputHashes': {str(path): digest(path) for path in (attempt / 'prototype-classes').rglob('*.class')}})
print({'compiled': True, 'attempt': str(attempt), 'sixFamilies': True, 'cumulativeCandidateUpdated': False})
