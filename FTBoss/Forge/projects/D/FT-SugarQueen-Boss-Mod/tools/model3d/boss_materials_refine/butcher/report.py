import hashlib
import json
import subprocess
import sys
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw

from render import ROOT, RES, ART, WORK, font


def main():
    files = [RES / 'textures/entity/butcher_v3.png', RES / 'textures/item/butcher_fang_charm.png',
             RES / 'textures/item/butcher_material.png', RES / 'textures/item/boss_materials/butcher.png']
    panel = Image.new('RGB', (1200, 700), '#20272c')
    draw = ImageDraw.Draw(panel)
    draw.text((24, 20), 'BUTCHER / ACTUAL ASSET REFERENCES', font=font(25), fill='#eadfc7')
    for index, source in enumerate(files):
        image = Image.open(source).convert('RGBA')
        image.thumbnail((500, 235), Image.Resampling.NEAREST)
        if image.width < 180:
            factor = max(1, 180 // image.width)
            image = image.resize((image.width * factor, image.height * factor), Image.Resampling.NEAREST)
        x, y = 30 + (index % 2) * 600, 80 + (index // 2) * 300
        panel.paste(image, (x, y), image)
        draw.text((x, y + 245), source.name, font=font(17), fill='#bac4c7')
    panel.save(ART / 'actual_references.png')
    mods = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods')
    jar = next(mods.glob('*L_Enders_Cataclysm-3.31.jar'))
    with zipfile.ZipFile(jar) as archive:
        for key in ('koboleton_bone', 'blazing_bone'):
            data = archive.read(f'assets/cataclysm/textures/item/{key}.png')
            target = ART / f'reference_cataclysm_{key}.png'
            target.write_bytes(data)
            image = Image.open(target).convert('RGBA')
            image.resize((256, 256), Image.Resampling.NEAREST).save(ART / f'reference_cataclysm_{key}_enlarged.png')
    report = json.loads((WORK / 'validation.json').read_text(encoding='utf-8'))
    if report['result'] != 'PASS':
        raise RuntimeError('Validation must pass before writing handoff')
    checks = []
    commands = [
        ['node', str(ROOT / 'tools/model3d/lint_java_model.cjs'), str(RES / 'models/item/boss_materials/butcher_material.json'), str(RES / 'models/item/boss_materials/butcher_material_gui.json')],
        ['node', str(ROOT / 'tools/model3d/check_uv_coverage.cjs'), str(RES / 'models/item/boss_materials/butcher_material.json'), str(RES / 'textures/item/boss_materials/butcher.png')],
    ]
    for command in commands:
        result = subprocess.run(command, cwd=ROOT, capture_output=True, text=True, encoding='utf-8')
        checks.append(' '.join(command) + '\n' + result.stdout + result.stderr)
        if result.returncode:
            raise RuntimeError(checks[-1])
    (WORK / 'shared-checks.txt').write_text('\n'.join(checks), encoding='utf-8', newline='\n')
    hashes = report['checks']['asset_sha256']
    handoff = f'''# 屠夫原材料精细重制交付

状态：完成，2026-10-21。ID `ftboss:butcher_material`。本交付是美术资源交付，未构建、未部署、未启动游戏。

## 设计依据与改进

- 已阅读 THREAD_BRIEF.md、根 AGENTS.md / CODE_STYLE.md、implementation-agent.md / DOC_CONSTRAINTS.md、MODEL_REFERENCE_NOTES.md，并参考只读 lib_itemtex.cjs 和成熟暗影刀的切面分层、逐面装箱与三段色阶。
- 实际屠夫使用 `ButcherBossModel.java` 和 `butcher_v3.png`：皮肉暖色、血锈红围裙，既有骨牙符为象牙色配血色结件。参考实际骨牙符和 Cataclysm 的 koboleton_bone / blazing_bone，预览及参考均在自身 art 目录。
- 从旧版 11 个无旋转长方体重制成不对称颌骨原料。骨皮有前后皮质板、下沿骨脊、八边形切口、内凹骨髓和骨屑；两根不等长牙刃有 22.5 度连续外切面、完整厚度和尖端；血色筋膜从正面跨过齿根连接到背面，辅以细纤维与局部残留。
- 最终 71 个部件，37 个合法单轴旋转，426 个独立 UV 岛，128×128 图集。每面固定 2 像素/模型单位，按实际面比例装箱并扩边，骨皮、牙釉质、断面、血髓和筋膜各自配色和笔触。
- 32×32 独立 GUI 图标由同一最终运行时模型渲染后加像素描边；图标与掉落/手持是同一件物品。Forge separate_transforms 入口、GUI 路由和 8 槽 display 原样保留。

## 修改文件

- `src/main/resources/assets/ftboss/models/item/boss_materials/butcher_material.json`
- `src/main/resources/assets/ftboss/textures/item/boss_materials/butcher.png`
- `src/main/resources/assets/ftboss/textures/item/butcher_material.png`
- `art/boss_materials/blockbench/butcher_material.bbmodel`
- 自有工具 `tools/model3d/boss_materials_refine/butcher/`、预览 `art/boss_materials/refine/butcher/` 和本交接目录。

入口与 GUI JSON 未改；语言、注册、掉落、玩法、Boss 实体和其他线程资产未写。修改前六项已有资产已完整备份至 `{WORK / 'backup-before-refine'}`。docs impact: none。

## 可复现命令

在 `{ROOT}` 运行（需要 Python、Pillow、NumPy；本机已提供）：

```powershell
$py = '{sys.executable}'
& $py tools/model3d/boss_materials_refine/butcher/generate.py
& $py tools/model3d/boss_materials_refine/butcher/render.py
& $py tools/model3d/boss_materials_refine/butcher/validate.py
& $py tools/model3d/boss_materials_refine/butcher/report.py
```

generate 只写 butcher 自身模型/图集/bbmodel/清单；render 从最终 src 资源读入，写自身预览和匹配图标；validate 从最终文件检查。未运行共享批量生成器或 Gradle。此目录 renderer 明确实现 Java FaceBakery 面顶点顺序、单轴旋转、rescale、UV 旋转、透明采样与深度测试；本模型 rescale=false。

## 校验与实际查看

- `validation.json`：PASS。合法几何/旋转、资源路由、426 面 UV 范围和比例、固定 2 px/unit、岛无重叠、透明样本为 0、71 部件连通、外露共面重叠为 0。
- Blockbench 的位置、尺寸、旋转和逐面 UV 与运行时逐项一致，内嵌 PNG 与运行时图集 SHA256 相同。
- 共享 lint_java_model.cjs 与 check_uv_coverage.cjs 通过；原始输出在 shared-checks.txt。
- 从最终运行时模型和贴图渲染了正/背/左/右四视图，已通过 view_image 实际查看并修正早期牙侧阶梯感和骨环共面问题；修正后重新生成并实际查看四视图、斜视图和图标对比。当前四个方向无缺块/缺面、悬空、断接、明显 UV 异常和裁切；左右厚度与背部筋膜连接完整。
- 预览与最终模型逐像素重渲染一致，四面包围盒均有足够画布留边。属于离线资产检查，未宣称游戏内验收。

## 最终预览绝对路径

- 四视图：`{ART / 'four_views.png'}`
- 单独四面：`{ART / 'front.png'}`、`{ART / 'back.png'}`、`{ART / 'left.png'}`、`{ART / 'right.png'}`
- 斜视细节：`{ART / 'oblique_detail.png'}`
- 背部斜视：`{ART / 'rear_oblique.png'}`
- 图标旧/新、原尺寸、16 px 槽位对比：`{ART / 'icon_comparison.png'}`
- UV 放大：`{ART / 'uv_atlas_enlarged.png'}`
- 可编辑工程：`{ROOT / 'art/boss_materials/blockbench/butcher_material.bbmodel'}`

## SHA256

| 资源 | SHA256 |
| --- | --- |
'''
    handoff += '\n'.join(f'| `{name}` | `{value}` |' for name, value in hashes.items())
    handoff += '\n\n未解决问题：无已知资产错误；游戏内效果与最终整合发布留给统筹环节。\n'
    (WORK / 'HANDOFF.md').write_text(handoff, encoding='utf-8', newline='\n')
    print(WORK / 'HANDOFF.md')


if __name__ == '__main__':
    main()
