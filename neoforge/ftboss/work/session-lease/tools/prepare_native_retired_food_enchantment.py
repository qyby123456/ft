import hashlib
import json
import re
import difflib
import subprocess
from pathlib import Path
root=Path(__file__).resolve().parents[3]
work=root/'work/session-lease'
parent_path=work/'reports/native-repair-ranged-use-candidate-529ba4c9.json'
parent=json.loads(parent_path.read_text(encoding='utf-8'))
for name,wanted in parent['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,name
arguments=json.loads((Path(parent['attempt'])/'complete-arguments316.json').read_text(encoding='utf-8'))
sources={Path(value).stem:Path(value) for value in arguments if value.endswith('.java')}
helper=work/'tools/prepare_native_frost_ingress_runtime.py'
namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
def block_end(text,start):
    depth=0
    for match in re.finditer(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/|[{}]',text[start:]):
        if match.group()=='{':depth+=1
        elif match.group()=='}':
            depth-=1
            if depth==0:return start+match.end()
    raise AssertionError('Unclosed Java block')
def without_method(text,name):
    matches=list(re.finditer(r'(?m)^    private (?:static )?[^\n(]+?\s+'+re.escape(name)+r'\(',text));assert len(matches)==1,name
    start=matches[0].start();finish=block_end(text,text.index('{',start))
    return text[:start]+text[finish:]
def stage(stem,text):
    original=sources[stem];target=work/'proposal-native'/Path(*re.search(r'^package\s+([^;]+);',text,re.M).group(1).split('.'))/(stem+'.java')
    if target.exists():
        old=target.read_text(encoding='utf-8');assert old==original.read_text(encoding='utf-8-sig'),stem
        differences=list(difflib.unified_diff(old.splitlines(True),text.splitlines(True)))[2:]
        patch='*** Begin Patch\n*** Update File: '+target.relative_to(root).as_posix()+'\n'+''.join(re.sub(r'^@@.*@@.*$', '@@',line) if line.startswith('@@') else line for line in differences)+'*** End Patch\n'
        result=subprocess.run([namespace['patcher'],'--codex-run-as-apply-patch',patch],capture_output=True);assert result.returncode==0,result.stderr
    else:namespace['add'](target.relative_to(root).as_posix(),text)
    changes.append({'source':str(original),'beforeSha256':hashlib.sha256(original.read_bytes()).hexdigest(),'staged':str(target),'stagedSha256':hashlib.sha256(target.read_bytes()).hexdigest()})
changes=[]
source=sources['StatueBlessingCatalog'].read_text(encoding='utf-8-sig')
record=source[source.index('    public static record Definition('):]
text='''package net.farmingtales.ftboss.item;

import java.util.List;

public final class StatueBlessingCatalog {
    private StatueBlessingCatalog() {
    }

    public static List<StatueBlessingCatalog.Definition> pool(String profile) {
        return List.of();
    }

    public static StatueBlessingCatalog.Definition byId(String identity) {
        return null;
    }

    public static List<StatueBlessingCatalog.Definition> all() {
        return List.of();
    }

    static boolean isAdded(String identity) {
        return false;
    }

'''+record
stage('StatueBlessingCatalog',text)
text=sources['ArmorBlessingCatalog'].read_text(encoding='utf-8-sig')
for name in ['rule','add']:text=without_method(text,name)
start=text.index('    static {');finish=block_end(text,text.index('{',start));text=text[:start]+text[finish:]
text=text.replace('new LinkedHashMap<>()','Map.of()')
assert 'rule(' not in text and 'static {' not in text and 'BY_ID.put(' not in text and 'POOLS.put(' not in text
stage('ArmorBlessingCatalog',text)
text=sources['FoodBlessingCombat'].read_text(encoding='utf-8-sig');head=text[:text.index('public final class FoodBlessingCombat')]
text=head+'''public final class FoodBlessingCombat {
    private FoodBlessingCombat() {
    }

    public static boolean eligible(ItemStack stack) {
        return false;
    }

    public static int level(ItemStack stack, String identity) {
        return 0;
    }

    public static void attributes(ItemAttributeModifierEvent event) {
        EquipmentAttributeSlots.attributes(event);
    }

    public static void blessingAttributes(ItemAttributeModifierEvent event, ItemStack stack) {
    }

    public static void blessingAttributes(ItemAttributeModifierEvent event, ItemStack stack, EquipmentSlot slot) {
    }

    public static int crossbowChargeTicks(int ticks, ItemStack stack) {
        return ticks;
    }

    public static void quoteDamage(Builder builder, BeforeMitigation event, ItemStack stack) {
    }

    public static void defense(BeforeMitigation event) {
    }

    public static void killFeast(LivingDeathEvent event) {
    }
}
'''
stage('FoodBlessingCombat',text)
text=sources['StatueBlessingCombat'].read_text(encoding='utf-8-sig');head=text[:text.index('public final class StatueBlessingCombat')];record=text[text.index('    private static record Active('):]
text=head+'''public final class StatueBlessingCombat {
    private StatueBlessingCombat() {
    }

    public static void quote(Builder builder, ServerPlayer player, LivingEntity target, CompoundTag data, long time, ItemStack stack, RandomQuotes random) {
    }

    public static void defense(BeforeMitigation event) {
    }

    public static void kill(LivingDeathEvent event) {
    }

    public static void attributes(ItemAttributeModifierEvent event, ItemStack stack) {
    }

    public static void attributes(ItemAttributeModifierEvent event, ItemStack stack, EquipmentSlot slot) {
    }

'''+record
stage('StatueBlessingCombat',text)
for stem in ['StatueHubScreen','OrdinaryStatueHubScreen']:
    text=sources[stem].read_text(encoding='utf-8-sig');assert text.count('blessingFoodTooltip(')==1
    text=without_method(text,'blessingFoodTooltip');stage(stem,text)
text=sources['StatueMenuNetwork'].read_text(encoding='utf-8-sig')
old='''                    } else if (var0.action == 7) {
                        FoodWeaponEvolution.applyFoodBlessingFromUi(var2, var5, var0.value);
'''
assert text.count(old)==1;text=text.replace(old,'')
old='''                        case 7:
                            FoodWeaponEvolution.ftboss$legacyOrdinary$applyFoodBlessingFromUi(var0, var8, var1.value);
                            return;
'''
assert text.count(old)==1;text=text.replace(old,'')
for anchor,addition in [
 ('        public static void send(BlockPos var0, int var1, int var2) {','\n            if (var1 == 7) return;'),
 ('        public static void handle(StatueMenuNetwork.Action var0, IPayloadContext var1) {','\n            if (var0.action == 7) return;'),
 ('        public static void send(BlockPos pos, int action, int value) {','\n            if (action == 7) return;')]:
    assert text.count(anchor)==1;text=text.replace(anchor,anchor+addition)
assert 'applyFoodBlessingFromUi' not in text
stage('StatueMenuNetwork',text)
positive_path=work/'reports/native-repair-runtime-bow-original-use-529ba4c9.json'
positive=json.loads(positive_path.read_text(encoding='utf-8'));assert positive['probe']['failures']==0
old_resources=Path(positive['attempt'])/'ftboss-unpackaged-proposal/assets/ftboss/lang'
resources=[]
for language in ['en_us','zh_cn']:
    original=old_resources/(language+'.json');values=json.loads(original.read_text(encoding='utf-8'))
    def retired(key):
        return key.startswith(('food_blessing.ftboss.','tooltip.ftboss.food_blessing.','tooltip.ftboss.food_blessings.','message.ftboss.food_blessing.','screen.ftboss.statue_hub.enchant.')) or key in ['screen.ftboss.statue_hub.tab.enchant','screen.ftboss.statue_hub.details.category.blessing','tooltip.ftboss.evolution.blessing_washable','tooltip.ftboss.evolution.blessing_washed']
    removed={key:value for key,value in values.items() if retired(key)}
    kept={key:value for key,value in values.items() if not retired(key)}
    assert len(removed)==85 and len(values)-len(kept)==85
    for key in ['item.ftboss.maque_blessing_feather','tooltip.ftboss.maque_feather.blessing','message.ftboss.maque_feather.blessing']:assert kept[key]==values[key]
    target=work/'proposal-resources/assets/ftboss/lang'/(language+'.json')
    namespace['add'](target.relative_to(root).as_posix(),json.dumps(kept,ensure_ascii=False,indent=2)+'\n')
    resources.append({'original':str(original),'originalSha256':hashlib.sha256(original.read_bytes()).hexdigest(),'staged':str(target),'stagedSha256':hashlib.sha256(target.read_bytes()).hexdigest(),'removedKeys':sorted(removed),'allOtherValuesPreserved':True})
paths=[Path(__file__),helper,parent_path,positive_path]
output=work/'reports/native-retired-food-enchantment-preparation.json'
with output.open('x',encoding='utf-8') as stream:json.dump({'parent':str(parent_path),'sourceChanges':changes,'resourceChanges':resources,'vanillaGlobalEnchantmentsChanged':False,'originalGameplayValuesChanged':False,'storedItemDataDeleted':False,'runtimeAccepted':False,'installable':False,'inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':output.name,'changedSources':len(changes),'translationsRemovedPerLanguage':85,'runtimeAccepted':False}))
