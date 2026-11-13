import hashlib
import json
import os
import sys
from collections import Counter, deque
from pathlib import Path
from xml.etree import ElementTree

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
FOLDER = ROOT / 'nature-redo-b08-s1-08'
sys.path.insert(0, str(Path(os.environ['APPDATA']) / '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure

ROTATIONS = ['none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90']
DIRECTIONS = ['north', 'east', 'south', 'west']


def load(path):
    return json.loads(path.read_text(encoding='utf-8'))


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def record(path):
    return {'path': str(path.resolve()), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'bytes': path.stat().st_size}


def captures(source):
    width, height, depth = source.size
    reports = []
    for turns, rotation in enumerate(ROTATIONS):
        path = FOLDER / ('native-placed-' + rotation + '.nbt')
        actual = Structure.read(path)
        assert actual.size == source.size and len(actual.blocks) == len(source.blocks)
        assert not actual.block_entities and not actual.entities
        for point, state in source.blocks.items():
            across, vertical, forward = point
            target = [(across,vertical,forward),(depth-1-forward,vertical,across),(width-1-across,vertical,depth-1-forward),(forward,vertical,width-1-across)][turns]
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
        reports.append({'rotation':rotation, **record(path), 'exact_rotated_final_source_match':True,'records':len(source.blocks)})
    return reports


def main():
    group = load(ROOT / 'group.json')
    metadata = load(FOLDER / 'room.json')
    source = Structure.read(FOLDER / 'room.nbt')
    fingerprint = record(FOLDER / 'room.nbt')['sha256']
    assert fingerprint == group['rooms'][0]['sha256'] == metadata['sha256']
    assert source.size == (35,22,35) and source.data_version == 3465 and len(source.blocks) == 26950
    assert not source.entities and not source.block_entities and not metadata['containers']
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
        native.append(record(path))
        analysis_path = FOLDER / ('courtyard-analysis-' + rotation + '.json')
        analysis = load(analysis_path)
        assert analysis['status'] == 'PASS' and analysis['source_nbt_sha256'] == fingerprint
        assert analysis['body_targets'] == analysis['reached_targets'] == len(metadata['body_route'])
        assert analysis['upper_targets'] == len(metadata['upper_targets']) and analysis['stair_targets'] == 48
        assert analysis['top_air_whitelist'] == 361 and analysis['other_roof_columns'] == 864
        assert not analysis['source_modified_for_analysis']
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
    save(FOLDER / 'materials.json',{'source_nbt_sha256':fingerprint,'records_including_air':26950,'counts_by_block_id':dict(sorted(Counter(state.name for state in source.blocks.values()).items())),'palette_states':[{'name':name,'properties':dict(properties),'count':count} for (name,properties),count in sorted(Counter((state.name,state.properties) for state in source.blocks.values()).items())],'containers':[],'entities':0,'block_entities':0})
    route = {tuple(point) for point in metadata['body_route']}
    start = tuple(metadata['entrance'])
    parents = {start:None}
    frontier = deque([start])
    while frontier:
        current = frontier.popleft()
        for candidate in route:
            if candidate in parents:
                continue
            if abs(current[0]-candidate[0])+abs(current[2]-candidate[2]) == 1 and abs(current[1]-candidate[1]) <= .5:
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
        path.reverse()
        paths.append({'target':target,'source_grid_witness':path})
    save(FOLDER / 'qa/body-routes.json',{'status':'PASS','source_nbt_sha256':fingerprint,'body':{'width':.6,'height':1.8,'max_step':.5},'scope':'All lower lanes, tree sides, pool approach, 48 stair cells and every declared U-gallery target. Native collision/support/sweeps in all four rotations; these paths are grid witnesses, not a client playthrough.','ports':metadata['ports'],'upper_boundary':metadata['upper_boundary'],'target_count':len(paths),'paths':paths,'native_reports':analyses})
    roof = {tuple(point) for point in metadata['roof_exceptions']}
    assert len(roof) == 361
    for across in range(35):
        for forward in range(35):
            assert (source.get((across,21,forward)).name == 'minecraft:air') == ((across,forward) in roof)
    sky = metadata['sky_sample']
    assert all(source.get((sky[0],vertical,sky[2])).name == 'minecraft:air' for vertical in range(2,22))
    fluid_edges = []
    for point,state in source.blocks.items():
        if state.name != 'minecraft:water':
            continue
        for delta in [(1,0,0),(-1,0,0),(0,-1,0),(0,0,1),(0,0,-1)]:
            neighbor = tuple(value+offset for value,offset in zip(point,delta))
            if source.get(neighbor).name == 'minecraft:air':
                fluid_edges.append({'water':point,'air':neighbor})
    assert not fluid_edges and not metadata['fluid_exposure_whitelist']
    save(FOLDER / 'qa/skylight-fluid-boundaries.json',{'status':'PASS','source_nbt_sha256':fingerprint,'true_top_air_columns':metadata['roof_exceptions'],'other_roof_columns':864,'true_sky_sample':sky,'sample_air_y':[2,21],'water_side_bottom_air_edges':fluid_edges,'intentional_fluid_exposure_whitelist':[],'adaptation':'Sealed glazed source-water column adjoining pool, not open flowing waterfall.'})
    cutaway = {'source_nbt_sha256':fingerprint,'true_source_skylight':'x8..26/z8..26/y21 are already air in source; no preview edit required. Crown may cover some columns below.','non_full_roof_cut':'Only display copies hide y>=21, including 864 roof cells already distinguished from 361 real skylight cells.','hero':'Additionally at y>=2 hide x>=33 or z>=33. This is a cutaway, not full source.','directional':'At y>=2 hide north z<=1, south z>=33, west x<=1, east x>=33 respectively.','full':'No changes in full reverse overview, both ground human views, upper-human, stair-detail, pool-detail, true-skylight and all exterior views.','derived_display_nbt_files':[],'analysis_source_modified':False,'traceability':'qa/render.json records sourceSHA, camera, cutmode, displayed records and each screenshotSHA. Only in-memory display copies; source rehashed after render.'}
    save(FOLDER / 'qa/cutaway-rules.json',cutaway)
    save(FOLDER / 'qa.json',{'status':'PRODUCER_TECHNICAL_PASS_PENDING_INDEPENDENT_REVIEW','source_nbt_sha256':fingerprint,'static':static,'support':support,'native_reports':native,'native_body_analysis':analyses,'exact_capture_comparisons':captured,'render_views':16,'scope':'400 forced fluid/neighbor ticks, randomTickSpeed=0; native multi-level AABB, support and half-step edge sweeps. No natural random tick certification, real player interactions, gameplay or deployment.'})
    save(FOLDER / 'reference-review.json',{'status':'PENDING_INDEPENDENT_REVIEW','reference':record(reference),'source_nbt':record(FOLDER/'room.nbt'),'landmarks':['Rooted central large tree with asymmetric crown','Side pool and glazed short-water-column adaptation','Real central skylight','Reachable north-east-south U-gallery with inner fences','Explicit western half-step stair'],'adaptations':metadata['deviations'],'cutaway_rules':cutaway,'next_image_authorized':False})
    previous=load(ROOT.parent/'nature-redo-b07-s1-07-20261105/SERIAL_QUEUE.json')
    accepted=previous['accepted']+[{'image':'S1-07','sha256':group['previous_accepted_sha256'],'authority':'Independent reviewer 01a1a685-045b-76d2-aa2b-2a3f24520603','evidence_readonly':'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S1-07-independent','scope':'Independent static files/native template/body routes, including exact open-only analysis. No natural random ticks, actual interactions, gameplay or deployment certification.'}]
    save(ROOT/'SERIAL_QUEUE.json',{'group':'S1','accepted':accepted,'current':{'image':'S1-08','sha256':fingerprint,'status':'PENDING_INDEPENDENT_REVIEW'},'next':{'image':'S1-09','authorized_to_start':False},'production_deployed':False})
    font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',22)
    canvas=Image.new('RGB',(1800,790),'#151d29')
    draw=ImageDraw.Draw(canvas)
    draw.text((25,15),'S1-08 原图 / 完整源下层人眼 / 完整源上廊人眼 · 待独立复核',font=font,fill='white')
    for column,(path,label) in enumerate([(reference,'参考图'),(FOLDER/'views/player-interior.png','真实完整源下层；无裁切'),(FOLDER/'views/upper-human.png','真实完整源可达上廊；无裁切')]):
        with Image.open(path) as image:
            image=image.convert('RGB')
            image.thumbnail((590,660))
            canvas.paste(image,(column*600+(600-image.width)//2,85+(660-image.height)//2))
        draw.text((column*600+15,55),label,font=font,fill='#d2e6cf')
    canvas.save(FOLDER/'reference-comparison.png')
    handover=ROOT/'producer-handover-20261105'
    handover.mkdir(exist_ok=True)
    lines=['# S1-08 制作方交接','','- 制作方技术通过，待独立逐图复核；只08不解锁09，01..07接受记录见SERIAL_QUEUE.json。',f'- room.nbt SHA-256：{fingerprint}','- Java1.20.1 DataVersion3465；35×22×35；26950条含空气记录；单调色板，所有原版方块，无实体/容器/方块实体。',f'- 下层声明路线{len(metadata["route"])}格，上廊{len(metadata["upper_targets"])}格，半阶48格，总身体目标{len(paths)}格；四口feet2，均3宽3高，详情见room.json。','- 古树四周、池北/东/南、四口、西侧楼梯及北东南U形上廊全部原生身体可达；西侧16级三宽半阶，每级0.5，feet2升至10。未创作西侧连续上廊或上层接口。','- 真天窗顶层361列空气，其余864列屋面完整；[22,2,15]上方到y21全部真实空气。白名单与原生四向检查见qa/skylight-fluid-boundaries.json和courtyard-analysis-*.json。','- 原图短瀑适配为玻璃封闭源水竖槽，池水封边；侧底直接空气边0，意外露白名单空；不称开放流瀑。无真实容器载荷，永久叶丛和垂藤顶承托检查。','- 四方向各400tick强制流体/邻居更新，randomTickSpeed=0；原版0.6×1.8身体、完整承托切片、半阶抬升/平移/下降扫掠检查；四份回捕含栅栏方向旋转逐块匹配。','- 16张源NBT Lodestone图含完整正反人眼/上廊/楼梯/水池/真天窗细节、外部完整壳与显示剖视；非游戏截图。裁切规则独立列明，真实源天窗与显示去顶不混称，源SHA/相机/记录数/图SHA可追溯。','- 首轮原生发现一处柱头压楼梯头部空间，已删除干涉重跑；首轮源NBT/配方/元数据生成脚本/XML/log保留history/attempt-01，不用于验收。','- 源码source/、材料materials.json、参考review文件、全部交接哈希齐备；audit为当前交付快照代理，非全组上下层体验或玩法终审。','- 自然随机刻、实际玩家交互、玩法和正式部署NOT_RUN；未写正式存档/mods/config，不提前09。','']
    (handover/'README.md').write_text('\n'.join(lines),encoding='utf-8')
    files=[path for path in (ROOT/'source').glob('*') if path.is_file()]
    files+=[ROOT/name for name in ['ANALYSIS.md','group.json','SERIAL_QUEUE.json','native-tests.xml','audit/summary.json','audit/pairs.json','audit/descriptors.json','audit/similarity-matrix.csv','audit/REPORT.md','audit/high-similarity.json','qa/native-console.log','qa/native-invocation.json']]
    files+=[path for path in FOLDER.rglob('*') if path.is_file()]
    files+=[ROOT/name for name in ['native-test/src/main/java/net/farmingtales/zonereceiver/ReceiverTests.java','native-test/build.gradle','native-test/settings.gradle','native-test/gradle.properties','native-test/src/main/resources/data/ftzonereceiver/structures/empty.nbt','native-test/src/main/resources/META-INF/mods.toml','native-test/src/main/resources/pack.mcmeta','server/server.properties']]
    files+=[path for path in (ROOT/'history').rglob('*') if path.is_file()]
    files+=[reference,handover/'README.md']
    save(handover/'FILE-MANIFEST.json',{'source_nbt_sha256':fingerprint,'manifest_self_excluded':True,'files':[record(path) for path in files]})
    print(json.dumps({'status':'READY_FOR_INDEPENDENT_REVIEW','sha256':fingerprint,'native_tests':4,'body_targets':len(paths),'upper_targets':len(metadata['upper_targets']),'stair_targets':48,'file_manifest_entries':len(files),'next_image_authorized':False}))


if __name__=='__main__':
    main()
