import hashlib
import json
import sys
from pathlib import Path

work = Path(__file__).resolve().parents[1]
tag = sys.argv[1]
origin = work / 'tools/prepare_native_melee_post_combat_restart.py'
frozen = json.loads((work / 'reports/native-melee-paid-signed-combat-focused-evidence-eef72352.json').read_text(encoding='utf-8'))
assert hashlib.sha256(origin.read_bytes()).hexdigest() == frozen['inputHashes'][str(origin)]
helper = origin.read_text(encoding='utf-8')
helper = helper.replace('melee', 'frost')
helper = helper.replace("['minecraft:iron_sword', 'minecraft:iron_axe']", "['ftboss:frost_queen_staff']")
helper = helper.replace('for (int sample : new int[]{0, 3})', 'for (int sample : new int[]{5})')
helper = helper.replace('sample == 0 ? 1 : 2', '4')
helper = helper.replace('paidSwordAndAxePostCombatContinuationAcrossNewProcess', 'paidFrostNovaPostCombatContinuationAcrossNewProcess')
helper = helper.replace("receipt = work / ('reports/native-repair-runtime-paid-frost-ingress-'", "receipt = work / ('reports/native-repair-runtime-paid-frost-nova-clear-'")
helper = helper.replace("seed_receipt = WORK / 'reports/native-repair-runtime-paid-frost-ingress-", "seed_receipt = WORK / 'reports/native-repair-runtime-paid-frost-nova-clear-")
helper = helper.replace("assert [case['id'] for case in cases] == ['ftboss:frost_queen_staff']", "assert [case['id'] for case in cases] == ['ftboss:frost_queen_staff']\nassert cases[0]['wearAfter'] == 4")
exec(compile(helper, str(__file__), 'exec'))
