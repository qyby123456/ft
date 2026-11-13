import hashlib
import json
import os
import sys
from collections import Counter, deque
from pathlib import Path
from xml.etree import ElementTree
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
FOLDER = ROOT / 'nature-redo-b09-s1-09'
sys.path.insert(0, str(Path(os.environ['APPDATA']) / '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure, plain

ROTATIONS = ['none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90']
DIRECTIONS = ['north', 'east', 'south', 'west']


def load(path):
    return json.loads(path.read_text(encoding='utf-8'))


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def record(path):
    return {'path': str(path.resolve()), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'bytes': path.stat().st_size}


def rotate(point, turns):
    across, vertical, forward = point
    return [(across,vertical,forward),(28-forward,vertical,across),(28-across,vertical,28-forward),(forward,vertical,28-across)][turns]


def captures(source):
    reports = []
    for turns, rotation in enumerate(ROTATIONS):
        path = FOLDER / ('native-placed-' + rotation + '.nbt')
        actual = Structure.read(path)
        assert actual.size == source.size and len(actual.blocks) == len(source.blocks)
        assert len(actual.block_entities) == 3 and not actual.entities
        for point, state in source.blocks.items():
            target = rotate(point,turns)
            properties = dict(state.properties)
            if properties.get('facing') in DIRECTIONS:
                properties['facing'] = DIRECTIONS[(DIRECTIONS.index(properties['facing']) + turns) % 4]
            if properties.get('axis') in ['x','z'] and turns % 2:
                properties['axis'] = 'z' if properties['axis'] == 'x' else 'x'
            original = properties.copy()
            for index, direction in enumerate(DIRECTIONS):
                if direction in original:
                    properties[DIRECTIONS[(index + turns) % 4]] = original[direction]
            assert actual.get(target).name == state.name and dict(actual.get(target).properties) == properties, str((rotation,point,state,actual.get(target)))
        payloads = []
        for point, entity in source.block_entities.items():
            content = plain(actual.block_entities[rotate(point,turns)])
            assert content['id'] == plain(entity)['id']
            assert not any(content.get(key) for key in ['Items','LootTable','Lock','Patterns'])
            payloads.append({'position':rotate(point,turns),'payload':content})
        reports.append({'rotation':rotation, **record(path), 'exact_rotated_final_source_match':True,'records':len(source.blocks),'block_entities':payloads,'scope':'Block states exact; vanilla normalization of absent Items/Patterns to empty lists accepted.'})
    return reports


def main():
    group = load(ROOT / 'group.json')
    metadata = load(FOLDER / 'room.json')
    source = Structure.read(FOLDER / 'room.nbt')
    fingerprint = record(FOLDER / 'room.nbt')['sha256']
    assert fingerprint == group['rooms'][0]['sha256'] == metadata['sha256']
    assert source.size == (29,17,29) and source.data_version == 3465 and len(source.blocks) == 14297
    assert not source.entities and len(source.block_entities) == 3 and len(metadata['containers']) == 1
    chest = plain(source.block_entities[(14,5,10)])
    assert chest['id'] == 'minecraft:chest' and not chest.get('Items') and not chest.get('LootTable') and not chest.get('Lock')
    tests = list(ElementTree.parse(ROOT / 'native-tests.xml').iter('testcase'))
    assert len(tests) == 4 and all(not any(test.find(tag) is not None for tag in ['failure','error','skipped']) for test in tests)
    assert 'BUILD SUCCESSFUL' in (ROOT / 'qa/native-console.log').read_text(encoding='utf-8')
    native = []
    analyses = []
    for rotation in ROTATIONS:
        path = FOLDER / ('forge-' + rotation + '.json')
        report = load(path)
        assert report['status'] == 'PASS' and report['source_nbt_sha256'] == fingerprint
        assert report['stabilization_ticks'] == 400 and report['random_tick_speed'] == 0
        assert report['initial'] == report['settled']
        assert report['settled']['empty_containers'] == 1 and report['settled']['empty_slots'] == 27
        native.append(record(path))
        analysis_path = FOLDER / ('treasure-analysis-' + rotation + '.json')
        analysis = load(analysis_path)
        assert analysis['status'] == 'PASS' and analysis['source_nbt_sha256'] == fingerprint
        assert analysis['body_targets'] == analysis['reached_targets'] == len(metadata['body_route']) == 226
        assert analysis['upper_targets'] == 19 and analysis['stair_targets'] == 30
        assert analysis['closed_roof_columns'] == 841 and analysis['conservative_lid_air_cells'] == 27
        assert all(analysis[key] for key in ['chest_front_reachable','chest_front_ray_clear','chest_menu_provider'])
        assert not analysis['source_modified_for_analysis'] and not analysis['loot_assigned']
        analyses.append(record(analysis_path))
    captured = captures(source)
    save(FOLDER / 'qa/capture-comparisons.json',{'status':'PASS','source_nbt_sha256':fingerprint,'rotations':captured})
    static = load(FOLDER / 'qa/static.json')
    support = load(FOLDER / 'qa/support.json')
    render = load(FOLDER / 'qa/render.json')
    assert static['status'] == support['status'] == 'PASS'
    assert static['sha256'] == support['source_nbt_sha256'] == render['source_nbt_sha256'] == fingerprint
    assert render['source_nbt_unchanged'] and len(render['views']) == 16
    for view in render['views']:
        assert record(FOLDER / view['file'])['sha256'] == view['sha256']
    reference = Path(metadata['reference'])
    assert record(reference)['sha256'] == metadata['reference_sha256'] == group['group_fingerprint']
    audit = load(ROOT / 'audit/summary.json')
    assert not audit['errors'] and not audit['current_group_high_similarity']
    save(FOLDER / 'materials.json',{'source_nbt_sha256':fingerprint,'records_including_air':14297,'counts_by_block_id':dict(sorted(Counter(state.name for state in source.blocks.values()).items())),'palette_states':[{'name':name,'properties':dict(properties),'count':count} for (name,properties),count in sorted(Counter((state.name,state.properties) for state in source.blocks.values()).items())],'containers':metadata['containers'],'entities':0,'block_entities':3})
    route = {tuple(point) for point in metadata['body_route']}
    start = tuple(metadata['entrance'])
    parents = {start:None}
    frontier = deque([start])
    while frontier:
        current = frontier.popleft()
        for candidate in route:
            if candidate not in parents and abs(current[0]-candidate[0])+abs(current[2]-candidate[2]) == 1 and abs(current[1]-candidate[1]) <= .5:
                parents[candidate] = current
                frontier.append(candidate)
    assert len(parents) == len(route)
    paths = []
    for target in metadata['body_route']:
        path = []
        current = tuple(target)
        while current is not None:
            path.append(list(current))
            current = parents[current]
        paths.append({'target':target,'source_grid_witness':list(reversed(path))})
    save(FOLDER / 'qa/body-routes.json',{'status':'PASS','source_nbt_sha256':fingerprint,'body':{'width':.6,'height':1.8,'max_step':.5},'lower_targets':177,'stair_targets':30,'upper_targets':19,'all_targets':226,'paths':paths,'native_analyses':analyses,'scope':'Source-grid witnesses supported by native collision and support checks on every stance/edge in four rotations. Not player input recordings.'})
    save(FOLDER / 'qa/container-contract.json',{'source_nbt_sha256':fingerprint,'chest':metadata['containers'][0],'source_payload':chest,'slots':27,'empty':True,'loot_assigned':False,'above_envelope_air_cells':27,'native_front_raycast_and_menu_provider':'PASS_FOUR_ROTATIONS','real_player_interaction':'NOT_RUN','scope':'True chest block entity; 3x3x3 air above; front feet on reachable plinth. Empty container is not delivered treasure loot.'})
    save(FOLDER / 'qa/fluid-roof-boundaries.json',{'source_nbt_sha256':fingerprint,'roof_y':16,'closed_roof_columns':841,'roof_exceptions':[],'skylight':False,'water_source_cells':6,'direct_side_bottom_air_edges':0,'fluid_exposure_whitelist':[],'water_region':[[7,1,23],[9,1,24]],'upper_external_port':False,'ports':metadata['ports'],'native_forced_fluid_ticks':True,'random_tick_speed':0})
    save(FOLDER / 'qa/cutaway-rules.json',{'source_nbt_sha256':fingerprint,'real_nbt_modified':False,'derived_nbt_written':False,'full_views':'mode full: all source records, no clipping','display_only_roof':'non-full modes hide y>=16','display_only_walls':'non-full modes also hide y>=2 and north z<=1 / south z>=27 / west x<=1 / east x>=27; hero hides south and east. Other wall modes hide only named wall.','source_roof_closed':True,'render_manifest':'qa/render.json: cameras, display/source counts, source hash and screenshot hashes','scope':'Lodestone previews, not Minecraft screenshots. Source roof is complete, cutaway is not a skylight.'})
    save(FOLDER / 'qa.json',{'status':'PRODUCER_TECHNICAL_PASS_PENDING_INDEPENDENT_REVIEW','sha256':fingerprint,'static':static,'support':support,'native':native,'specialized_native':analyses,'native_capture_comparisons':'qa/capture-comparisons.json','render':'qa/render.json','real_player_interaction':'NOT_RUN','gameplay':'NOT_RUN','deployment':'NOT_RUN','natural_random_ticks':'NOT_CERTIFIED','integrated':False})
    save(FOLDER / 'reference-review.json',{'status':'PENDING_INDEPENDENT_REVIEW','reference':record(reference),'source_nbt':record(FOLDER/'room.nbt'),'landmarks':['Raised real chest plinth with six half-step rises','Paired solid guardians on left/right','West/east side arches; south entrance','Red/gold back panels and circular stone crest','Perimeter roots, persistent canopy, hanging berries','Small left foreground sealed pool'],'deviations':metadata['deviations'],'guardian_faces':'Carved pumpkin blocks are a vanilla facial adaptation, not stone sculpted faces.','production_only_one_image':True})
    previous = load(ROOT.parent/'nature-redo-b08-s1-08-20261105/SERIAL_QUEUE.json')
    accepted = previous['accepted'] + [{'image':'S1-08','sha256':group['previous_accepted_sha256'],'authority':'Independent reviewer 01a1a685-045b-76d2-aa2b-2a3f24520603','evidence_readonly':'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S1-08-independent/independent-details.json','scope':'Accepted for sequential production. Producer400ticks random0; no natural random tick, actual interaction/gameplay/deployment certification.'}]
    save(ROOT/'SERIAL_QUEUE.json',{'group':'S1','accepted':accepted,'current':{'image':'S1-09','sha256':fingerprint,'status':'PENDING_INDEPENDENT_REVIEW'},'next':{'image':'S1-10','authorized_to_start':False},'production_deployed':False})
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',22)
    canvas = Image.new('RGB',(1800,790),'#151d29')
    draw = ImageDraw.Draw(canvas)
    draw.text((25,15),'S1-09 原图 / 完整源人眼 / 完整源宝箱细节 · 待独立复核',font=font,fill='white')
    for column,(path,label) in enumerate([(reference,'参考图'),(FOLDER/'views/player-interior.png','完整源人眼；无裁切'),(FOLDER/'views/chest-detail.png','真实空箱与上盖空间；无裁切')]):
        with Image.open(path) as image:
            image = image.convert('RGB')
            image.thumbnail((590,660))
            canvas.paste(image,(column*600+(600-image.width)//2,85+(660-image.height)//2))
        draw.text((column*600+15,55),label,font=font,fill='#d2e6cf')
    canvas.save(FOLDER/'reference-comparison.png')
    (ROOT/'ANALYSIS.md').write_text('\n'.join(['# S1-09 宝藏室逐图制作说明','','根据S1-09单张参考图独立编写几何，不复用S1-08庭院建筑。复用通用支撑、渲染、NBT和原生测试工具。','29×17×29；高台顶面Y4、人物feetY5，南侧五宽六排半阶，每排抬升0.5，从feetY2升到5。177地面、30半阶、19台面声明目标合计226。','箱体[14,5,10]朝南，单箱，真实方块实体；Items/LootTable/Lock均缺省，实际27槽为空。前方[14,5,11]原生身体可达并可视线命中；3×3×3箱上空间全空气。真实玩家点击和开盖动画未运行。','双石守卫用方块塑造；南瓜面部是明确的原版近似。两面红旗和红陶板金边、石环、边墙树根与永久叶丛；不含怪物实体。','西东侧口、南入口都是feetY2，三宽至少三高。房顶Y16共841列完整；没有真天窗、连续上廊、上层外口。左前水池只有六格封边源水。','首轮原生失败来自旧通用容器检查仅枚举地面站位，不代表台阶被阻塞。保留首轮XML/log/harness/源NBT，后续把容器射线站位扩展到全部声明body_route，每个站位及可达边另做原生碰撞、承托和0.5步高检查；四向重跑成功，未改NBT。','四旋转各400tick强制邻居/流体更新，randomTickSpeed=0；不得称自然随机刻稳定或独立第二服务器复核。未做玩家实际交互、玩法、正式部署。','16张Lodestone图来自同一冻结NBT；完整图与去顶/去墙显示副本分开标记，裁切坐标及相机/源记录/图SHA追踪详见qa。所有截图非游戏客户端截图。',f'当前跨组快照{audit["rooms"]}份真实NBT、{audit["pairs"]}对；未检出本模块高重复候选，仍不替代全量玩法/视觉独立终审。','只交付09等待独立验收，10未开始；无付费生图，未访问正式存档。','']),encoding='utf-8')
    handover = ROOT/'producer-handover-20261105'
    handover.mkdir(exist_ok=True)
    lines = ['# S1-09 制作方交接','','- 状态：制作方技术通过，待独立逐图复核。只09；01..08已接受，10未解锁。',f'- 冻结room.nbt SHA-256：{fingerprint}','- Java1.20.1 DataVersion3465；29×17×29；14297条含空气记录；原版方块，0实体，1真实空箱+2旗帜方块实体。','- 177下层+30半阶+19台面=226原生身体目标。0.6×1.8碰撞体、承托切片、每步最高0.5的抬升/平移/下降扫掠，三口与箱前均可达。','- 箱体[14,5,10]朝南，Items/LootTable/Lock缺省，27槽为空。箱前站位[14,5,11]视线与菜单提供器通过；箱上27格空气。没有配置战利品，不声称宝藏内容已交付。','- 四方向各400tick，强制流体/邻居更新，randomTickSpeed=0；初始/稳定完全一致。四份实际回捕逐方块旋转匹配，箱向、树干轴、旗向匹配；空Items/Patterns为原版序列化补全。','- 完整屋顶841列，无天窗；六格封闭源水，无侧底空气泄漏；植物顶承托、永久叶丛与全体支撑检查通过。','- 16张同源Lodestone正反人眼/宝箱/双守卫/台阶/水池/四向外壳及剖视。非游戏截图，显示裁切不改NBT。参考对照图、相机、材料表、各图SHA与源SHA齐备。','- 首轮旧地面容器射线检查失败；history/attempt-01保留。改为经过全体身体可达证明的多高度站位检查后四向通过，源NBT未改变。','- 自然随机刻稳定、实际玩家交互、玩法与正式部署NOT_RUN；独立复核PENDING。audit仅当前交付快照代理，不冒充全量终审。','- 交接清单FILE-MANIFEST.json包含源码、原生harness、日志、NBT、材料、参考图及预览；清单自身不自哈希。','']
    (handover/'README.md').write_text('\n'.join(lines),encoding='utf-8')
    files = [path for path in (ROOT/'source').rglob('*') if path.is_file() and '__pycache__' not in str(path)]
    files += [ROOT/name for name in ['ANALYSIS.md','group.json','SERIAL_QUEUE.json','native-tests.xml','audit/summary.json','audit/pairs.json','audit/descriptors.json','audit/similarity-matrix.csv','audit/REPORT.md','audit/high-similarity.json','qa/native-console.log','qa/native-invocation.json']]
    files += [path for path in FOLDER.rglob('*') if path.is_file()]
    files += [ROOT/name for name in ['native-test/src/main/java/net/farmingtales/zonereceiver/ReceiverTests.java','native-test/build.gradle','native-test/settings.gradle','native-test/gradle.properties','native-test/src/main/resources/data/ftzonereceiver/structures/empty.nbt','native-test/src/main/resources/META-INF/mods.toml','native-test/src/main/resources/pack.mcmeta','server/server.properties']]
    files += [path for path in (ROOT/'history').rglob('*') if path.is_file()]
    files += [reference,handover/'README.md']
    files = sorted(set(files),key=str)
    save(handover/'FILE-MANIFEST.json',{'source_nbt_sha256':fingerprint,'manifest_self_excluded':True,'files':[record(path) for path in files]})
    print(json.dumps({'status':'READY_FOR_INDEPENDENT_REVIEW','sha256':fingerprint,'native_tests':4,'body_targets':226,'containers':1,'file_manifest_entries':len(files),'next_image_authorized':False}))


if __name__ == '__main__':
    main()
