import hashlib
import json
import sys
from pathlib import Path


work = Path(__file__).resolve().parents[1]
origin = work / 'tools/prepare_native_melee_post_combat_restart.py'
frozen = json.loads((work / 'reports/native-melee-paid-signed-combat-focused-evidence-eef72352.json').read_text(encoding='utf-8'))
assert hashlib.sha256(origin.read_bytes()).hexdigest() == frozen['inputHashes'][str(origin)]
helper = origin.read_text(encoding='utf-8').replace('melee', 'crossbow')
helper = helper.replace("['minecraft:iron_sword', 'minecraft:iron_axe']", "['minecraft:crossbow']")
helper = helper.replace('for (int sample : new int[]{0, 3})', 'for (int sample : new int[]{2})')
helper = helper.replace('sample == 0 ? 1 : 2', '1')
helper = helper.replace('paidSwordAndAxePostCombatContinuationAcrossNewProcess', 'paidCrossbowPostCombatContinuationAcrossNewProcess')
helper = helper.replace('paid-crossbow-ingress-', 'paid-crossbow-combat-')
helper = helper.replace('run_native_paid_crossbow_ingress_', 'run_native_paid_crossbow_combat_')
helper = helper.replace("assert [case['id'] for case in cases] == ['minecraft:crossbow']", "assert [case['id'] for case in cases] == ['minecraft:crossbow']\nassert cases[0]['wearAfter'] == 1")
exec(compile(helper, str(__file__), 'exec'))
