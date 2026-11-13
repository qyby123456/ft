import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
REVIEW = Path('D:/ft工程文件/FT地牢_第二轮独立验收_20261106/S2-submission-20261107-063435-165422')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def record(path):
    return {'path': str(path), 'sha256': digest(path), 'bytes': path.stat().st_size}


acceptance = json.loads((REVIEW / 'ACCEPTANCE.json').read_text(encoding='utf-8'))
candidate = ROOT / 'assembly/s2-desert-functional-combined.nbt'
assert acceptance['accepted'] and acceptance['verdict'] == 'ACCEPTED'
assert not acceptance['unresolved_required_items']
assert acceptance['source_sha256'] == digest(candidate)
ready = json.loads((ROOT / 'ready-for-review.json').read_text(encoding='utf-8'))
rooms = [{'id': room['id'], 'candidate': record(Path(room['candidate']))} for room in ready['rooms']]
assert all(room['candidate']['sha256'] == original['sha256'] for room, original in zip(rooms, ready['rooms']))
index = {'style': 'S2', 'group': 'S2', 'status': 'INDEPENDENTLY_ACCEPTED_ARCHIVED_NOT_DEPLOYED', 'independent_reviewed_at': acceptance['reviewed_at'], 'candidate': record(candidate), 'rooms': rooms, 'independent_acceptance': record(REVIEW / 'ACCEPTANCE.json'), 'independent_acceptance_explanation': record(REVIEW / 'ACCEPTANCE.md'), 'frozen_base_files': acceptance['base_frozen_files_verified'], 'frozen_interaction_files': acceptance['interaction_supplement_files'], 'frozen_access_files': acceptance['access_files'], 'independent_natural_ticks': acceptance['independent_natural_ticks'], 'independent_waypoints': acceptance['natural_waypoints'], 'unresolved_required_items': [], 'limits': acceptance['limits'], 'deployment': acceptance['deployment'], 'historical_status_notice': 'Earlier ready/progress/manifests remain frozen and retain their pre-acceptance pending status. This new append-only index registers the later independent decision without rewriting the reviewed evidence.', 'preserved_delivery_manifests': [record(ROOT / relative) for relative in ['ready-for-review.json', 'artifact-manifest.json', 'qa/interaction-range-current/supplement-manifest.json', 'access-install-33ceb33f/evidence-manifest.json']]}
(ROOT / 'ACCEPTED-INDEX.json').write_text(json.dumps(index, ensure_ascii=False, indent=2), encoding='utf-8')
text = '# S2 本轮独立验收通过封存索引\n\n状态：INDEPENDENTLY_ACCEPTED_ARCHIVED_NOT_DEPLOYED。独立验收时间：'+acceptance['reviewed_at']+'。\n\n精确合并NBT SHA-256：`'+acceptance['source_sha256']+'`。独立结论：`'+str(REVIEW / 'ACCEPTANCE.json')+'`；说明：`'+str(REVIEW / 'ACCEPTANCE.md')+'`。十房精确哈希及结论文件哈希见同目录ACCEPTED-INDEX.json。\n\n独立审计通过499基础、43交互和56接入冻结证据；独立新世界15612自然刻、4215路点完成并返回；所有本轮必需项已关闭。\n\n本索引为新增登记，原ready/progress/历史清单中的待审状态保留，表示其生成时的历史状态。源NBT、房间、配方、分片、安装包及既有证据不改动、不重复生成。未部署正式实例。本轮范围与限制完整继承独立ACCEPTANCE.json，未扩大为真人客户端、战斗或任意朝向验收。\n'
(ROOT / 'ACCEPTED-INDEX.md').write_text(text, encoding='utf-8')
print('INDEPENDENT_ACCEPTANCE_REGISTERED', acceptance['source_sha256'], len(rooms))
