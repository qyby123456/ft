import hashlib
import json
import os
import sys
from collections import Counter, deque
from pathlib import Path
from xml.etree import ElementTree
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
FOLDER = ROOT / 'nature-redo-b10-s1-10'
sys.path.insert(0,str(Path(os.environ['APPDATA'])/'.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure, plain

ROTATIONS = ['none','clockwise_90','clockwise_180','counterclockwise_90']
DIRECTIONS = ['north','east','south','west']


def load(path):
    return json.loads(path.read_text(encoding='utf-8'))


def save(path,value):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')


def record(path):
    return {'path':str(path.resolve()),'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'bytes':path.stat().st_size}


def rotated(point,turns):
    across,vertical,forward = point
    return [(across,vertical,forward),(60-forward,vertical,across),(60-across,vertical,60-forward),(forward,vertical,60-across)][turns]


def captures(source):
    reports = []
    for turns,rotation in enumerate(ROTATIONS):
        path = FOLDER/('native-placed-'+rotation+'.nbt')
        actual = Structure.read(path)
        assert actual.size == source.size and len(actual.blocks) == len(source.blocks)
        assert len(actual.block_entities) == 2 and not actual.entities
        for point,state in source.blocks.items():
            properties = dict(state.properties)
            if properties.get('facing') in DIRECTIONS:
                properties['facing'] = DIRECTIONS[(DIRECTIONS.index(properties['facing'])+turns)%4]
            if properties.get('axis') in ['x','z'] and turns%2:
                properties['axis'] = 'z' if properties['axis']=='x' else 'x'
            before = properties.copy()
            for index,direction in enumerate(DIRECTIONS):
                if direction in before:
                    properties[DIRECTIONS[(index+turns)%4]] = before[direction]
            observed = actual.get(rotated(point,turns))
            assert observed.name==state.name and dict(observed.properties)==properties,str((rotation,point,state,observed))
        for point,payload in source.block_entities.items():
            observed = plain(actual.block_entities[rotated(point,turns)])
            assert observed['id']==plain(payload)['id']=='minecraft:chest'
            assert not any(observed.get(key) for key in ['Items','LootTable','LootTableSeed','Lock'])
        reports.append({'rotation':rotation,**record(path),'exact_rotated_state_match':True,'records':len(source.blocks),'empty_chest_payloads':2,'normalization':'Missing Items becomes empty Items list under vanilla serialization.'})
    return reports


def main():
    metadata = load(FOLDER/'room.json')
    group = load(ROOT/'group.json')
    source = Structure.read(FOLDER/'room.nbt')
    fingerprint = record(FOLDER/'room.nbt')['sha256']
    assert fingerprint == metadata['sha256'] == group['rooms'][0]['sha256']
    assert source.size==(61,13,61) and len(source.blocks)==48373 and source.data_version==3465
    assert len(source.block_entities)==2 and not source.entities
    tests = list(ElementTree.parse(ROOT/'native-tests.xml').iter('testcase'))
    assert len(tests)==4 and all(not any(test.find(tag) is not None for tag in ['failure','error','skipped']) for test in tests)
    assert 'BUILD SUCCESSFUL' in (ROOT/'qa/native-console.log').read_text(encoding='utf-8')
    native = []
    analyses = []
    for rotation in ROTATIONS:
        native_path = FOLDER/('forge-'+rotation+'.json')
        report = load(native_path)
        assert report['status']=='PASS' and report['source_nbt_sha256']==fingerprint
        assert report['initial']==report['settled'] and report['stabilization_ticks']==400 and report['random_tick_speed']==0
        assert report['settled']['empty_containers']==2 and report['settled']['empty_slots']==54
        path = FOLDER/('overview-analysis-'+rotation+'.json')
        analysis = load(path)
        assert analysis['status']=='PASS' and analysis['source_nbt_sha256']==fingerprint
        assert analysis['body_targets']==analysis['reached_targets']==len(metadata['body_route'])==539
        assert analysis['region_anchors']==10 and analysis['closed_roof_columns']==1940
        assert analysis['conservative_lid_air_cells']==54 and analysis['chest_front_reachable'] and analysis['chest_front_ray_clear']
        native.append(record(native_path))
        analyses.append(record(path))
    save(FOLDER/'qa/capture-comparisons.json',{'status':'PASS','source_nbt_sha256':fingerprint,'rotations':captures(source)})
    static = load(FOLDER/'qa/static.json')
    support = load(FOLDER/'qa/support.json')
    render = load(FOLDER/'qa/render.json')
    assert static['status']==support['status']=='PASS' and static['sha256']==support['source_nbt_sha256']==render['source_nbt_sha256']==fingerprint
    assert len(render['views'])==27 and render['source_nbt_unchanged']
    for view in render['views']:
        assert record(FOLDER/view['file'])['sha256']==view['sha256'] and view['source_records']==48373
    reference = Path(metadata['reference'])
    assert record(reference)['sha256']==metadata['reference_sha256']==group['group_fingerprint']
    assembly = load(FOLDER/'shards/ASSEMBLY.json')
    assert assembly['source_nbt_sha256']==fingerprint and assembly['state_mismatches']==assembly['duplicate_cells']==assembly['uncovered_cells']==assembly['block_entity_payload_mismatches']==0
    assert assembly['block_state_cells_compared']==48373 and assembly['source_logical_sha256']==assembly['reassembled_logical_sha256']
    for part in assembly['parts']:
        assert record(Path(part['file']))['sha256']==part['sha256'] and max(part['size'])<=48
    audit = load(ROOT/'audit/summary.json')
    assert not audit['errors'] and not audit['current_group_high_similarity']
    leaves = [state for state in source.blocks.values() if state.name.endswith('_leaves')]
    assert all(dict(state.properties)['persistent']=='true' and int(dict(state.properties)['distance'])<=6 for state in leaves)
    save(FOLDER/'materials.json',{'source_nbt_sha256':fingerprint,'records_including_air':48373,'counts_by_block_id':dict(sorted(Counter(state.name for state in source.blocks.values()).items())),'palette_states':[{'name':name,'properties':dict(properties),'count':count} for (name,properties),count in sorted(Counter((state.name,state.properties) for state in source.blocks.values()).items())],'block_entities':{str(point):plain(payload) for point,payload in source.block_entities.items()},'ordinary_entities':[]})
    route = {tuple(point) for point in metadata['body_route']}
    start = tuple(metadata['entrance'])
    parents = {start:None}
    frontier = deque([start])
    while frontier:
        current = frontier.popleft()
        for candidate in route:
            if candidate not in parents and abs(current[0]-candidate[0])+abs(current[2]-candidate[2])==1 and abs(current[1]-candidate[1])<=.5:
                parents[candidate]=current
                frontier.append(candidate)
    assert len(parents)==len(route)==539
    witnesses = []
    for region in metadata['regions']:
        target = tuple(region['anchor'])
        assert target in parents
        path = []
        current = target
        while current is not None:
            path.append(list(current))
            current = parents[current]
        witnesses.append({'region':region['id'],'bounds':region['bounds'],'target':region['anchor'],'source_grid_witness':list(reversed(path))})
    save(FOLDER/'qa/region-routes.json',{'status':'PASS','source_nbt_sha256':fingerprint,'lower_targets':530,'stair_targets':3,'upper_targets':6,'all_targets':539,'body':{'width':.6,'height':1.8,'max_step':.5},'region_witnesses':witnesses,'complete_target_list':metadata['body_route'],'native_analyses':analyses,'scope':'All stances and adjacent raised/swept edges checked using native collision and floor support in four rotations. Grid witnesses are not player input recordings.'})
    save(FOLDER/'qa/containers-support-boundaries.json',{'source_nbt_sha256':fingerprint,'chests':metadata['containers'],'empty_inventory_slots_total':54,'loot_assigned':False,'conservative_lid_air_cells_total':54,'native_front_rays_and_menu_providers':'PASS_FOUR_ROTATIONS','real_player_interaction':'NOT_RUN','persistent_leaves':len(leaves),'all_leaf_distance_at_most_6':True,'ground_connectivity':'qa/support.json','plant_attachment':'Native canSurvive + unchanged state under forced neighbor/400tick run','roof_y':12,'closed_indoor_roof_columns':1940,'roof_exceptions':metadata['roof_exceptions'],'roof_exception_count':1781,'true_sky_column':[33,2,46],'water_source_cells':static['water_cells'],'water_side_bottom_air_edges':0,'fluid_exposure_whitelist':[],'waterfall_adaptation':'Sealed glazed source-water column; no open flowing waterfall','source_entry':'Permanently open arch; no door blocks/gates, no open-only analysis','random_tick_speed':0})
    save(FOLDER/'qa/cutaway-rules.json',{'source_nbt_sha256':fingerprint,'real_nbt_modified':False,'derived_nbt_written_for_render':False,'translations':[],'full_views':'Every record from full source; no clipping, including all ten region human views, chest details, reverse and exterior views.','non_full_roof_deletion':'All y>=12','plan_mode_extra_deletion':'Only y>=8 and inside one region inclusive bounds and on at least one of its boundary lines x=minX/maxX or z=minZ/maxZ. Includes whatever block state occupies that precise region boundary, not just masonry.','region_boundary_rectangles':[region['bounds'] for region in metadata['regions']],'direction_mode_extra_deletion':'At y>=2: north z<=1 / south z>=59 / west x<=1 / east x>=59, only named side. These are whole-source edge cuts; internal walls remain.','scope':'Display copies only, no geometry translation. Genuine open courtyard distinguished from preview roof removal. 27 Lodestone previews, not Minecraft client screenshots.'})
    save(FOLDER/'qa.json',{'status':'PRODUCER_TECHNICAL_PASS_PENDING_INDEPENDENT_REVIEW','sha256':fingerprint,'static':static,'support':support,'native':native,'specialized_native':analyses,'captures':'qa/capture-comparisons.json','render':'qa/render.json','shards':'shards/ASSEMBLY.json','real_player_interaction':'NOT_RUN','gameplay':'NOT_RUN','deployment':'NOT_RUN','natural_random_ticks':'NOT_CERTIFIED','integrated':False})
    save(FOLDER/'reference-review.json',{'status':'PENDING_INDEPENDENT_REVIEW','reference':record(reference),'source_nbt':record(FOLDER/'room.nbt'),'landmarks':['Northwest small water shrine','West rectangular dining hall/table/crates','Large northeast bookshelf/gold altar hall','Small southwest red-floor chest room','Central open tree courtyard/pool','Southeast separate entry vestibule','Real north-south to east-west elbow corridor and other continuous links'],'source_regions':metadata['regions'],'deviations':metadata['deviations'],'geometry_source':'Independently authored for image10; no pasted prior rooms, no preview montage.'})
    previous = load(ROOT.parent/'nature-redo-b09-s1-09-20261105/SERIAL_QUEUE.json')
    accepted = previous['accepted']+[{'image':'S1-09','sha256':group['previous_accepted_sha256'],'authority':'Independent reviewer 01a1a685-045b-76d2-aa2b-2a3f24520603','evidence_readonly':'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/REVIEW-20261105-S109.md','scope':'Accepted for sequential production. World ticks/real ray-click GUI/player gameplay/deployment NOT_RUN independently.'}]
    save(ROOT/'SERIAL_QUEUE.json',{'group':'S1','accepted':accepted,'current':{'image':'S1-10','sha256':fingerprint,'status':'PENDING_INDEPENDENT_REVIEW'},'next':{'image':'S1-11','authorized_to_start':False},'production_deployed':False})
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',22)
    canvas = Image.new('RGB',(1800,790),'#151d29')
    draw = ImageDraw.Draw(canvas)
    draw.text((25,15),'S1-10 参考 / 完整总源显示剖视 / 完整源庭院人眼 · 待独立复核',font=font,fill='white')
    for column,(path,label) in enumerate([(reference,'唯一参考图'),(FOLDER/'preview.png','同一总NBT；显式去顶/边墙上段'),(FOLDER/'views/courtyard.png','完整源庭院；无裁切')]):
        with Image.open(path) as image:
            image=image.convert('RGB')
            image.thumbnail((590,660))
            canvas.paste(image,(column*600+(600-image.width)//2,85+(660-image.height)//2))
        draw.text((column*600+15,55),label,font=font,fill='#d2e6cf')
    canvas.save(FOLDER/'reference-comparison.png')
    analysis_lines=['# S1-10 完整总览制作与边界','','从单张总览参考重建六个主要区域及四段连接廊。大小、锚点、门洞坐标见room.json。所有建筑在一个61×13×61真实NBT内，绝非仅预览拼图。','西北水景小室接西餐厅，餐厅接东北大厅及南侧转角廊；转角廊连接西南藏宝小室与中央庭院；大厅有独立南短廊入庭院；庭院接东南入口。所有区域从源南入口身体可达。','总声明530地面+3半阶+6台面=539目标，包含10区域锚点及两箱前站位。大厅金台高一格，经feet2→2.5→3两次半步上台。其余同层，非隐藏传送或飞行路线。','两只真实空箱：大厅[47,3,7]及小室[8,2,44]，均向南。各箱上3×3×3空气，共54格，前方可达、射线及菜单提供器通过；尚无战利品，不称有宝藏内容。','1940室内屋顶列Y12完整，1781屋顶豁免列含真正庭院和建筑外草地；[33,2,46]上方到Y12为空，真正天空。屋顶白名单不是漏屋顶免检借口，房间范围与庭院分开列明。','源水全部封边，无侧底直接空气边；小室短瀑是玻璃封闭源水适配。全体支撑6面联通，原生植物存活/状态保持与永久树叶距检查通过。','四向原生400tick、randomTickSpeed=0、强制流体/邻居更新。四向回捕每格原版状态匹配；自然随机刻稳定、实际玩家点击GUI、输入物理玩法、正式部署均NOT_RUN。','27张Lodestone同源图，包括全部10区域完整人眼、两个箱体细节、真实庭院正反、完整壳与俯视/正反剖视。剖视按明确Y和区域边线删除显示副本，没有平移；参考对照图用于比较不是交付几何。','四分片均≤48，位移[0,0,0]/[32,0,0]/[0,0,32]/[32,0,32]；空气必须覆盖。48,373格每个状态及2方块实体和0普通实体离线重组完全相同。分片孤立存活/实际部署未测。','首轮静态发现走廊壳体重叠残墙，已清除内部重叠及打通明确接口；保留最初源NBT/配方history/attempt-01，最终原生四向通过。',f'当前跨组快照{audit["rooms"]}真实总源、{audit["pairs"]}对；本组无高重复候选，分片不按独立房间重复计数，未替代全量玩法终审。','前9张冻结，只交付10待独立复核，不开始11，不部署正式存档。','']
    (ROOT/'ANALYSIS.md').write_text('\n'.join(analysis_lines),encoding='utf-8')
    handover = ROOT/'producer-handover-20261105'
    handover.mkdir(exist_ok=True)
    lines=['# S1-10 制作方交接','','- 状态：制作方技术通过，待独立复核；前01..09接受且冻结，只10不解锁11。',f'- 完整总源room.nbt SHA-256：{fingerprint}','- Java1.20.1，DataVersion3465；61×13×61；48373条含空气记录；六个不同尺寸主要区、四段走廊；0实体、2空箱方块实体。','- 539身体目标原生四向可达，覆盖10区域锚点、转角与两箱前。三宽主要路线；大厅两次半步2→2.5→3。入口永久开敞，不含闭门。','- 4方向各400tick强制流体/邻居更新、randomTickSpeed0。源NBT四份实际回捕逐格旋转/方块实体核对一致；日志与XML齐备。','- 两只真实空箱54槽，无Items/LootTable/Lock，箱上54格空气。原生可达、射线、菜单提供器通过；真实GUI交互未运行，无战利品。','- 1940室内闭顶列，1781明列豁免含庭院/室外。真实庭院天空柱[33,2,46]；水侧底封边、植物及永久叶距、全体支撑通过。','- 四片part-01..04大小32×13×32、29×13×32、32×13×29、29×13×29；位移/顺序/必须覆盖空气写于shards/README.md与ASSEMBLY.json。离线逐格重组48373格全部属性、2方块实体和0普通实体完全等于总源；源和重组逻辑SHA一致。','- 27张同源Lodestone，10区域完整人眼、箱体与正反庭院、完整外壳、俯视及正反剖视；显示删除坐标、相机/图SHA/源SHA/计数可追溯。无显示平移，不以预览拼图冒充建筑。','- source全源码、材料、静态/支撑/各区路线/容器与屋顶水边界、四向原生证据、截图、分片、旧失败源、跨组快照和参考全部列入FILE-MANIFEST.json。','- 自然随机刻稳定、真实玩家交互/物理玩法、实际分片放置和正式部署NOT_RUN，独立复核PENDING。未写正式存档/mods/config，没有付费生图。','']
    (handover/'README.md').write_text('\n'.join(lines),encoding='utf-8')
    files = [path for path in (ROOT/'source').rglob('*') if path.is_file() and '__pycache__' not in str(path)]
    files += [ROOT/name for name in ['ANALYSIS.md','group.json','SERIAL_QUEUE.json','native-tests.xml','audit/summary.json','audit/pairs.json','audit/descriptors.json','audit/similarity-matrix.csv','audit/REPORT.md','audit/high-similarity.json','qa/native-console.log','qa/native-invocation.json']]
    files += [path for path in FOLDER.rglob('*') if path.is_file()]
    files += [ROOT/name for name in ['native-test/src/main/java/net/farmingtales/zonereceiver/ReceiverTests.java','native-test/build.gradle','native-test/settings.gradle','native-test/gradle.properties','native-test/src/main/resources/data/ftzonereceiver/structures/empty.nbt','native-test/src/main/resources/META-INF/mods.toml','native-test/src/main/resources/pack.mcmeta','server/server.properties']]
    files += [path for path in (ROOT/'history').rglob('*') if path.is_file()]
    files += [reference,handover/'README.md']
    files = sorted(set(files),key=str)
    save(handover/'FILE-MANIFEST.json',{'source_nbt_sha256':fingerprint,'manifest_self_excluded':True,'files':[record(path) for path in files]})
    print(json.dumps({'status':'READY_FOR_INDEPENDENT_REVIEW','sha256':fingerprint,'native_tests':4,'body_targets':539,'region_anchors':10,'shards':4,'views':27,'manifest_files':len(files),'next_authorized':False}))


if __name__=='__main__':
    main()
