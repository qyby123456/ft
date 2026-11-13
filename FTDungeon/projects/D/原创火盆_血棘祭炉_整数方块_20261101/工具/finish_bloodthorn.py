import base64
import hashlib
import io
import json
import math
import shutil
import zipfile
from collections import Counter, deque
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFont

from build_bloodthorn import ASSETS, DIRECTIONS, GROUPS, IDENTIFIER, ROOT, TEXTURES, geometry, hidden_faces

RENDERS = ROOT / 'work/renders'


def load_json(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def integer_vector(vector):
    return len(vector) == 3 and all(isinstance(value, (int, float)) and math.isfinite(value) and value == round(value) for value in vector)


def geometry_signature(model):
    return {
        element['name']: {
            'from': element['from'], 'to': element['to'],
            'rotation': element.get('rotation', {}).get('angle', 0),
            'shade': element.get('shade', True),
            'faces': {
                name: {'uv': face['uv'], 'texture': face['texture'], 'rotation': face.get('rotation', 0)}
                for name, face in element['faces'].items()
            },
        } for element in model['elements']
    }


def font(size, bold=False):
    return ImageFont.truetype('C:/Windows/Fonts/msyhbd.ttc' if bold else 'C:/Windows/Fonts/msyh.ttc', size)


def make_sheet(prefix, filename, title, footer, tile_size=320, frame=0):
    sheet = Image.new('RGB', (tile_size * 2 + 48, tile_size * 2 + 152), '#252630')
    drawing = ImageDraw.Draw(sheet)
    drawing.text((24, 15), title, font=font(26, True), fill='#e1d3b5')
    for index, (view, label) in enumerate([('front', '正面 FRONT'), ('back', '背面 BACK'), ('left', '左侧 LEFT'), ('right', '右侧 RIGHT')]):
        horizontal = 16 + (index % 2) * (tile_size + 16)
        vertical = 62 + (index // 2) * (tile_size + 34)
        drawing.text((horizontal + 8, vertical), label, font=font(15), fill='#9e9aa6')
        image = Image.open(RENDERS / f'{prefix}_{view}_f{frame:02d}.png').convert('RGB')
        image = image.resize((tile_size, tile_size), Image.Resampling.NEAREST)
        sheet.paste(image, (horizontal, vertical + 24))
    drawing.text((24, sheet.height - 29), footer, font=font(14), fill='#9e9aa6')
    sheet.save(ROOT / '预览' / filename)


def make_previews():
    make_sheet('final', '四视图_血棘祭炉.png', '血棘祭炉 · 暗黑血祭 / 整数方块', '实际导出模型正交投影 · 非 AI 概念图 · 未运行游戏客户端')
    make_sheet('s01_grey', '灰模四视图.png', '血棘祭炉 · 制作前灰模核对', '43 个 Cuboid · 16×16×16 边界 · 几何零旋转')
    for frame in (0, 4, 8, 12):
        make_sheet('key', f'关键帧四视图_f{frame:02d}.png', f'血焰纹理 · 关键帧 {frame:02d}', '仅纹理变化 · 所有几何和枢轴保持整数、不运动', tile_size=240, frame=frame)
    hero = Image.new('RGB', (640, 732), '#252630')
    drawing = ImageDraw.Draw(hero)
    drawing.text((24, 17), '血棘祭炉', font=font(29, True), fill='#e1d3b5')
    drawing.text((260, 26), 'BLOODTHORN BRAZIER', font=font(16), fill='#a94a55')
    hero.paste(Image.open(RENDERS / 'final_hero_f00.png').convert('RGB'), (0, 66))
    drawing.text((24, 708), '黑铁 / 血痕 / 骨齿 / 阶梯血焰    |    16×16×16', font=font(14), fill='#9e9aa6')
    hero.save(ROOT / '预览/成品_血棘祭炉.png')
    frames = [Image.open(RENDERS / f'anim_hero_f{frame:02d}.png').convert('RGB') for frame in range(16)]
    frames[0].save(ROOT / '预览/血焰循环_16帧.gif', save_all=True, append_images=frames[1:], duration=100, loop=0, disposal=2, optimize=False)
    contact = Image.new('RGB', (960, 1092), '#252630')
    drawing = ImageDraw.Draw(contact)
    drawing.text((20, 12), '血焰纹理循环 · 全部 16 帧（每帧 2 ticks）', font=font(25, True), fill='#e1d3b5')
    for frame, image in enumerate(frames):
        horizontal = (frame % 4) * 240
        vertical = 58 + (frame // 4) * 254
        drawing.text((horizontal + 16, vertical), f'{frame:02d}', font=font(14), fill='#9e9aa6')
        contact.paste(image.resize((240, 240), Image.Resampling.NEAREST), (horizontal, vertical + 14))
    contact.save(ROOT / '预览/血焰全帧检查.png')
    environments = Image.new('RGB', (976, 540), '#252630')
    drawing = ImageDraw.Draw(environments)
    drawing.text((20, 12), '背景对照（不是动态光照或实机截图）', font=font(23, True), fill='#e1d3b5')
    environments.paste(frames[0], (0, 58))
    environments.paste(Image.open(RENDERS / 'light_hero_f00.png').convert('RGB'), (496, 58))
    environments.save(ROOT / '预览/深浅背景对照.png')
    for view in ('top', 'underside', 'rear_hero'):
        filename = f'final_{view}_f00.png'
        shutil.copyfile(RENDERS / filename, ROOT / '预览' / filename)


def inspect():
    project_path = ROOT / '可编辑模型/血棘祭炉_暗黑血祭_整数方块.bbmodel'
    runtime_path = ASSETS / 'ftboss/models/block' / (IDENTIFIER + '.json')
    project = load_json(project_path)
    runtime = load_json(runtime_path)
    authored = load_json(ROOT / 'work/authored_final.json')
    reopened = load_json(ROOT / 'work/reopened_editable_export.json')
    roundtrip = load_json(ROOT / 'work/runtime_roundtrip_export.json')
    cubes = project['elements']
    failures = []

    def check(name, condition):
        if not condition:
            failures.append(name)
        return bool(condition)

    results = {}
    results['legacy_java_export_target_selected'] = check('legacy target', project.get('java_block_version') == '1.9.0')
    results['only_positive_integer_cuboids'] = check('cuboid grid', all(
        element.get('type', 'cube') == 'cube'
        and integer_vector(element['from']) and integer_vector(element['to'])
        and all(0 <= minimum < maximum <= 16 for minimum, maximum in zip(element['from'], element['to']))
        and integer_vector(element.get('origin', [0, 0, 0]))
        and element.get('rotation', [0, 0, 0]) == [0, 0, 0]
        and not element.get('inflate', 0)
        for element in cubes
    ))
    results['no_geometry_animation'] = check('no geometry animation', not project.get('animations'))
    results['runtime_integer_cuboids'] = check('runtime grid', all(
        integer_vector(element['from']) and integer_vector(element['to'])
        and element.get('rotation', {}).get('angle', 0) == 0
        and integer_vector(element.get('rotation', {}).get('origin', [0, 0, 0]))
        for element in runtime['elements']
    ))
    results['eleven_fire_cuboids_preserve_shade_false'] = check('legacy fire shading', sum(element.get('shade', True) is False for element in runtime['elements']) == 11)
    results['actual_native_export_matches_authored_geometry_uv'] = check('native export equality', geometry_signature(runtime) == geometry_signature(authored))
    results['saved_editable_reopen_matches_export'] = check('editable reopen equality', geometry_signature(runtime) == geometry_signature(reopened))
    results['runtime_native_import_reexport_matches'] = check('runtime roundtrip equality', geometry_signature(runtime) == geometry_signature(roundtrip))
    results['texture_references_survive_roundtrip'] = check('texture binding', runtime['textures'] == roundtrip['textures'] == reopened['textures'])
    group_lookup = {group['uuid']: group for group in project.get('groups', [])}
    referenced = []
    visited_groups = []
    tree_lines = []

    def walk(node, depth=0):
        if isinstance(node, str):
            referenced.append(node)
            return
        metadata = {**group_lookup.get(node['uuid'], {}), **node}
        visited_groups.append(metadata)
        tree_lines.append('  ' * depth + metadata.get('name', node['uuid']) + '  pivot=' + str(metadata.get('origin', [0, 0, 0])))
        for child in metadata.get('children', []):
            walk(child, depth + 1)

    for node in project['outliner']:
        walk(node)
    results['integer_group_pivots_zero_rotation'] = check('group pivots', len(visited_groups) == 14 and all(
        integer_vector(group.get('origin', [0, 0, 0])) and group.get('rotation', [0, 0, 0]) == [0, 0, 0]
        for group in visited_groups
    ))
    results['each_cube_has_exactly_one_parent'] = check('parentage', Counter(referenced) == Counter(element['uuid'] for element in cubes))
    (ROOT / '骨骼层级.txt').write_text('\n'.join(tree_lines) + '\n\n以上为 Blockbench 编辑层级；Java 方块/物品不执行实体骨骼动作。\n', encoding='utf-8')
    expected_hidden = hidden_faces(geometry())
    expected_face_count = sum(6 - len(value) for value in expected_hidden.values())
    results['hidden_face_removal_conservative'] = check('face pruning', all(
        set(DIRECTIONS) - set(element['faces']) == set(expected_hidden[element['name']]) for element in runtime['elements']
    ))
    results['within_budget'] = check('budget', len(cubes) == 43 and sum(len(element['faces']) for element in runtime['elements']) == expected_face_count <= 300)
    body = Image.open(TEXTURES / (IDENTIFIER + '.png')).convert('RGBA')
    fire = Image.open(TEXTURES / (IDENTIFIER + '_fire.png')).convert('RGBA')
    results['opaque_cuboids_no_cards'] = check('opaque textures', body.getextrema()[3] == (255, 255) and fire.getextrema()[3] == (255, 255))
    results['texture_dimensions'] = check('texture size', body.size == (64, 64) and fire.size == (16, 256))
    animation = load_json(TEXTURES / (IDENTIFIER + '_fire.png.mcmeta'))['animation']
    results['vanilla_texture_animation_16_frames'] = check('animation metadata', animation == {'frametime': 2, 'interpolate': False, 'frames': list(range(16))})
    fire_hashes = [hashlib.sha256(fire.crop((0, frame * 16, 16, (frame + 1) * 16)).tobytes()).hexdigest() for frame in range(16)]
    results['all_16_texture_frames_distinct'] = check('frame uniqueness', len(set(fire_hashes)) == 16)
    embedded_equal = []
    for texture in project['textures']:
        embedded = Image.open(io.BytesIO(base64.b64decode(texture['source'].split(',', 1)[1]))).convert('RGBA')
        external = Image.open(TEXTURES / texture['name']).convert('RGBA')
        embedded_equal.append(embedded.size == external.size and embedded.tobytes() == external.tobytes())
    results['embedded_textures_match_external_png'] = check('embedded PNG equality', all(embedded_equal))
    exact_views = {}
    for view in ('front', 'back', 'left', 'right', 'top', 'underside', 'hero', 'rear_hero'):
        first = Image.open(RENDERS / f'final_{view}_f00.png').convert('RGB')
        second = Image.open(RENDERS / f'roundtrip_{view}_f00.png').convert('RGB')
        exact_views[view] = first.size == second.size and ImageChops.difference(first, second).getbbox() is None
    results['eight_view_roundtrip_pixel_identical'] = check('render equality', all(exact_views.values()))
    rendered_frames = [hashlib.sha256((RENDERS / f'anim_hero_f{frame:02d}.png').read_bytes()).hexdigest() for frame in range(16)]
    results['all_16_visible_frames_distinct'] = check('rendered animation uniqueness', len(set(rendered_frames)) == 16)
    geometry_keys = [tuple(element['from'] + element['to']) for element in cubes]
    results['no_duplicate_geometry'] = check('duplicate geometry', len(geometry_keys) == len(set(geometry_keys)))
    occupied = set()
    structural = set()
    fire_names = {cube.name for cube in geometry() if cube.material == 'fire'}
    for element in cubes:
        for horizontal in range(element['from'][0], element['to'][0]):
            for vertical in range(element['from'][1], element['to'][1]):
                for depth in range(element['from'][2], element['to'][2]):
                    coordinate = (horizontal, vertical, depth)
                    occupied.add(coordinate)
                    if element['name'] not in fire_names:
                        structural.add(coordinate)
    reached = {next(iter(occupied))}
    queue = deque(reached)
    while queue:
        coordinate = queue.popleft()
        for axis in range(3):
            for sign in (-1, 1):
                adjacent = list(coordinate)
                adjacent[axis] += sign
                adjacent = tuple(adjacent)
                if adjacent in occupied and adjacent not in reached:
                    reached.add(adjacent)
                    queue.append(adjacent)
    results['one_connected_voxel_assembly'] = check('connectivity', reached == occupied)
    generic = {name: load_json(ROOT / 'work' / ('audit_' + name + '_generic.json')) for name in ('editable', 'runtime')}
    results['generic_audits_no_errors_warnings'] = check('generic audits', all(not report['errors'] and not report['warnings'] and report['dependencies_complete'] for report in generic.values()))
    results['resource_pack_format_15'] = check('pack format', load_json(ROOT / '运行资源/pack.mcmeta')['pack']['pack_format'] == 15)
    report = {
        'name': '血棘祭炉', 'date': '2026-11-01', 'status': 'PASS' if not failures else 'FAIL',
        'checks': results, 'failures': failures,
        'geometry': {'cuboids': len(cubes), 'faces': expected_face_count, 'hidden_faces_removed': 258 - expected_face_count, 'groups': len(visited_groups), 'bounds': [[0, 0, 0], [16, 16, 16]], 'minimum_dimension': 1, 'rotations': 0, 'plane_elements': 0, 'mesh_elements': 0, 'occupied_voxels': len(occupied), 'structural_voxel_center_height': round(sum(coordinate[1] + 0.5 for coordinate in structural) / len(structural), 3)},
        'textures': {'body': [64, 64], 'fire_strip': [16, 256], 'fire_frame': [16, 16], 'frames': 16, 'ticks_per_frame': 2, 'loop_seconds_at_20tps': 1.6, 'materials': 2, 'total_png_pixels': 8192, 'uncompressed_base_RGBA_bytes_not_measured_VRAM': 32768},
        'native_checks': {'editable_reopen': load_json(ROOT / 'work/native_reopen_evidence.json'), 'runtime_import': load_json(ROOT / 'work/native_runtime_roundtrip_evidence.json')},
        'render_comparison': exact_views,
        'runtime': {'target': 'Minecraft Java 1.20.1 / vanilla JSON cuboids and animated PNG', 'bone_runtime': 'No entity bone animation: hierarchy is editable organization only', 'emission': 'No shader/PBR/dynamic-light dependency; shade=false is not actual block light emission', 'client_tested': False, 'deployed': False, 'block_or_item_registered': False},
        'native_export_target': load_json(ROOT / 'work/native_legacy_target.json'),
        'reference_image': {'dsh_attempted': True, 'dsh_session': 'session-f0fe8140-9ae9-4b86-a7ca-de2ccc3f009d', 'generated': False, 'reason': 'DSH reported no native image interface; all delivered views are actual model renders'},
        'limitations': generic['runtime']['limitations'] + ['Orthographic preview uses nearest-pixel software raycasting, not Minecraft ambient occlusion or client lighting.', 'Integer pivots alone do not validate arbitrary future skeletal motion; no skeletal motion is included.'],
    }
    dependency_files = [project_path] + sorted((ROOT / '运行资源').rglob('*'))
    report['sha256'] = {str(file.relative_to(ROOT)): hashlib.sha256(file.read_bytes()).hexdigest() for file in dependency_files if file.is_file()}
    write_json(ROOT / '验证报告.json', report)
    if failures:
        raise SystemExit('Validation failed: ' + ', '.join(failures))
    return report


def documentation(report):
    (ROOT / 'README.md').write_text('''# 血棘祭炉 · 暗黑血祭 / 严格整数 Cuboid

这是独立新候选，保留上一版，不覆盖任何已有火盆、研究资料或模组资源。

## 打开与查看

- 可编辑主文件：`可编辑模型/血棘祭炉_暗黑血祭_整数方块.bbmodel`
- 成品：`预览/成品_血棘祭炉.png`
- 正交四视图：`预览/四视图_血棘祭炉.png`
- 动效：`预览/血焰循环_16帧.gif`（1.6 秒循环）
- 灰模、全部 16 帧、四组关键帧和深浅背景对照也在 `预览/`。
- 几何和依赖检查：`验证报告.json`；编辑层级：`骨骼层级.txt`。

## 造型与硬约束

黑铁方盆、宽底双级方台、四个短足、前后方颅面饰、四角阶梯骨齿、暗红凝血和红色阶梯血焰。只有轴对齐长方体，整个模型位于 [0,16]×[0,16]×[0,16]；尺寸、几何坐标、立方体枢轴和全部组枢轴均为整数。43 个正体积 Cuboid、14 个层级组、217 个声明面；没有曲面、斜切、mesh、薄片或旋转。41 个完全被实心邻件遮蔽的面已保守删除，部分遮挡面不切碎。

材质为原创像素图，不拷贝模组素材。主体 64×64 图集与每帧 16×16 血焰按 1 像素/模型单位组织，64×64 不表示模型使用 1/4 单位几何。Java JSON 中归一化 UV 的小数也不是非整数几何。GUI/手持场景缩放只属于 display，不改变模型网格。

## 运行边界

沿用 Java 1.20.1 方块/物品模型资源路径。Blockbench 目标显式选择 `1.9 - 1.21.5`（内部值 `1.9.0`，覆盖 1.20.1），不使用其默认 26.3+ 的明暗导出规则。`运行资源/` 是 pack_format 15 的资源候选，命名空间 `ftboss`，标识 `bloodthorn_brazier`。可用其中的模型、父模型和贴图接入模组；**不会自动注册方块或物品**，没有擅自替换现有注册项或添加猜测的 blockstate。整套交付 ZIP 是工程包，不是双击即可获得新方块的模组。

骨骼层级用于 Blockbench 组织和后续维护，原版 Java 方块 JSON 不执行实体骨骼动画。这里只播放原版 PNG `.mcmeta` 纹理循环，几何全程不动，因此没有额外动画穿模或逐帧几何副本。血焰的 `shade:false` 不是实际光源；没有宣称动态照明或着色器发光。

已完成可编辑工程保存重开、实际 Java JSON 原生导入/再导出、8 个角度逐像素一致对照、16 帧与整数结构检查。尚未启动 Minecraft、未部署、未做实机显示或性能测试。

## DSH 四视图

按用户建议实际调用了 DSH。DSH 返回「无可用 image 接口」，没有生成参考 PNG；详见 `参考图/DSH_结果说明.md`。交付的四视图都来自真实灰模/导出模型，不冒充 image 概念图，也没有偷偷改用付费图像 API。
''', encoding='utf-8')
    center = report['geometry']['structural_voxel_center_height']
    (ROOT / '制作说明.md').write_text(f'''# 设计、来源与验证范围

## 原创设计

以宽底方台和厚壁盆体降低视觉重心，主结构占据低位；结构实体单位格的几何质心高度约为 {center}/16（不是材质质量或物理模拟）。四角骨齿用 2×2×2 根块与 1×1×1 尖块构成，不用倒角。前后方颅以六像素宽方头、四像素宽方颌及贴图眼窝表达，不向火盆硬套人形四肢比例。血液只以暗红污痕、滴流和小面积亮红表达；火焰为有体积的轴对齐阶梯块，非交叉透明火片。

## 参考方法，不复制资产

学习用户指定两套研究档案中「暗底—材质中间值—少量亮核」、主体/附属结构的父子分工、少量有轮廓贡献的几何和独立纹理动画的组织方法。沿用 FT Boss 可参考道具所体现的厚重主块、明确材料分区及低精度像素语言；未使用其原图像素。允许参考不等于来源已认证非 AI，本交付不作这种认证。本次造型、UV 和 PNG 从零制作，未复用此前火盆几何、火盆参考图或贴图；旧交付文件保持不动。

研究入口：
- `D:\\ft工程文件\\mod_model_study_7mods_20261031\\README.md`
- `D:\\ft工程文件\\mod_model_study_7mods_20261031\\LEARNING_NOTES.md`
- `D:\\ft工程文件\\mod_model_study_20261031\\README.md`
- `D:\\ft工程文件\\mod_model_study_20261031\\LEARNING_NOTES.md`

## 制作顺序与成本

先定预算 56 Cuboid / 300 面 / 2 材质，再做整数灰模及正交视图，检查后绘制原创 PNG，最后使用 Blockbench Java codec 原生保存/导出并重开核对。最终 43 Cuboid、217 面、2 材质；血迹、眼窝、金属磕痕不另建几何；16 帧血焰只保存一套几何与一条贴图条带。纹理总像素 8192，基础 RGBA 理论字节 32768，不等于 GPU 显存或 FPS 实测。

## 检查证据与限制

`验证报告.json` 列明检查项、SHA-256、真实重开/导入的 UUID、依赖和失败项。generic 审计仅覆盖通用结构与依赖，另外执行本项目 1 单位整数、零旋转、16³ 边界、父子唯一性、正体积、连通装配和贴图帧规则；不把 generic 名称冒充 1.20.1 完整兼容认证。真实 Java 导出与往返导出的 8 个视角在相同软件渲染条件下逐像素一致。

预览采用实际导出 JSON 的正交射线检测、最近邻 PNG 采样和简化方块面明暗；不是 AI 概念图，也不是 Minecraft 实机、PBR、AO 或动态光照测试。所有 16 帧均检查，另附 0/4/8/12 帧四视图。两种背景仅对比轮廓和可读性，不声称是两种游戏光照。静态装配允许结构接合处相交，未包含未来任意骨骼旋转的防穿模保证。

`工具/` 保留本次的独立重建、原生导入导出辅助、离线渲染和验证脚本；`work/` 为中间证据，未塞入交付 ZIP。重新运行构建脚本会重建本候选，请先备份你后来修改的候选主文件，不要用它覆盖用户新编辑。
''', encoding='utf-8')
    (ROOT / '参考图/DSH_结果说明.md').write_text('''# DSH 四视图调用结果

日期：2026-11-01。

实际调用 DSH headless 会话 `session-f0fe8140-9ae9-4b86-a7ca-de2ccc3f009d`，请求先生成同对象、同尺寸的 front/back/left/right 整数方块血祭火盆参考图，禁止读取旧火盆与修改模型。

DSH 如实返回「无可用 image 接口」，没有生成 PNG，没有转用外部 API/CLI、安装软件、索取密钥或制作模型。因此本目录没有虚构的概念图。随后由当前任务独立制作灰模，并用实际导出模型产生可核对的四视图。

本任务未调用 image2.5/wecodex 图像 API，没有修改图像服务或聊天服务配置。详细本地 DSH 运行事件保留在 `work/`，不进入精简交付包。
''', encoding='utf-8')


def package():
    selected = [ROOT / name for name in ['README.md', '制作说明.md', '验证报告.json', '骨骼层级.txt', '参考图/DSH_结果说明.md']]
    selected += [file for folder in ('可编辑模型', '运行资源') for file in (ROOT / folder).rglob('*') if file.is_file()]
    selected += [ROOT / '预览' / name for name in ['成品_血棘祭炉.png', '四视图_血棘祭炉.png', '灰模四视图.png', '血焰循环_16帧.gif', '血焰全帧检查.png', '深浅背景对照.png']]
    selected += [ROOT / '预览' / f'关键帧四视图_f{frame:02d}.png' for frame in (0, 4, 8, 12)]
    selected += [ROOT / '预览' / f'final_{view}_f00.png' for view in ('top', 'underside', 'rear_hero')]
    archive_path = ROOT / '血棘祭炉_整数方块_模型与贴图.zip'
    with zipfile.ZipFile(archive_path, 'w', compression=zipfile.ZIP_DEFLATED) as archive:
        for file in selected:
            archive.write(file, file.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(archive_path) as archive:
        assert archive.testzip() is None
        assert set(archive.namelist()) == {file.relative_to(ROOT).as_posix() for file in selected}
        count = len(archive.namelist())
    evidence = {'archive': str(archive_path), 'entries': count, 'bytes': archive_path.stat().st_size, 'crc_test': 'PASS', 'sha256': hashlib.sha256(archive_path.read_bytes()).hexdigest()}
    write_json(ROOT / '交付包校验.json', evidence)
    return evidence


if __name__ == '__main__':
    validation = inspect()
    make_previews()
    documentation(validation)
    archive = package()
    print(json.dumps({'validation': validation['status'], 'checks': len(validation['checks']), 'geometry': validation['geometry'], 'archive': archive}, ensure_ascii=False, indent=2))
