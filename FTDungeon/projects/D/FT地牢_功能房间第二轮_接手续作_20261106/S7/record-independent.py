import hashlib
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parent
reports=[
 Path(r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\lever-revisions-20261107-033714-416096\REVIEW.md'),
 Path(r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\S7-world-evidence-20261107-034143-577825\REVIEW.md')
]
room=ROOT/'S7-20'
summary=json.loads((room/'summary.json').read_text(encoding='utf-8'))
sha=hashlib.sha256(Path(summary['nbtPath']).read_bytes()).hexdigest()
assert sha=='6a7f9c3cb249522fb940e73f870bb7702b9ea5a6f5b8bdf23fb4a4826fe7669e'
assert sha in reports[0].read_text(encoding='utf-8')
evidence={
 'external_reports':[{'path':str(path),'sha256':hashlib.sha256(path.read_bytes()).hexdigest()} for path in reports],
 'S7-20':{'candidate_sha256':sha,'lever':[29,4,12],'state':'face=wall,facing=south','backing':[29,4,11],'backing_block':'minecraft:stone_bricks','external_canSurvive_nonair':11110,'external_failures':0,'external_unsupported':0,'external_route_targets_passed':7,'old_lever_support_issue':'CLOSED_BY_INDEPENDENT_REVIEW','room_visual_or_group_accepted':False},
 'world_evidence':{'independent_inspection':'FROZEN_SOURCE_LOG_CODE_AND_BEFORE_AFTER_NBT_INSPECTION','independent_world_rerun':False,'source_hashes_match':True,'original_coordinates_block_type_changes':0,'sparse_omitted_cells_new_nonair':0,'S7-14_entity_count_before':3,'S7-14_entity_count_after':3,'equipment_report':'2 iron sets, 1 golden set, all 3 iron swords; author observation supported by independent source/log inspection, not independent dynamic entity-payload acceptance'},
 'limits':['totalIssues excludes state_changes','Author fire/fluid scans iterate explicit source records only','Independent before/after comparison covers structure interior, not beyond the test barrier','DoorBlock.setOpen is API toggling, not player input or lever-controlled power','No player chest GUI or full route roundtrip evidence','Single orientation, no final merged-world update run','group_accepted remains false']
}
(ROOT/'independent-evidence-update.json').write_text(json.dumps(evidence,ensure_ascii=False,indent=2),encoding='utf-8')
print('Independent lever closure bound to',sha)
