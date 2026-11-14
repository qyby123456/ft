import json
import subprocess
import sys

from generate import ROOT, RES, ART, WORK


def main():
    validation = json.loads((WORK / 'validation.json').read_text(encoding='utf-8'))
    if validation['result'] != 'PASS':
        raise RuntimeError('Relic validation must pass before handoff')
    commands = [
        ['node', str(ROOT / 'tools/model3d/lint_java_model.cjs'), str(RES / 'models/item/boss_relics/butcher_fang_charm.json'), str(RES / 'models/item/boss_relics/butcher_fang_charm_gui.json')],
        ['node', str(ROOT / 'tools/model3d/check_uv_coverage.cjs'), str(RES / 'models/item/boss_relics/butcher_fang_charm.json'), str(RES / 'textures/item/boss_relics/butcher_fang_charm.png')],
    ]
    outputs = []
    for command in commands:
        result = subprocess.run(command, cwd=ROOT, capture_output=True, text=True, encoding='utf-8')
        outputs.append(' '.join(command) + '\n' + result.stdout + result.stderr)
        if result.returncode:
            raise RuntimeError(outputs[-1])
    (WORK / 'shared-checks.txt').write_text('\n'.join(outputs), encoding='utf-8', newline='\n')
    report = f'''# 屠夫骨牙符精细重制交付

状态：完成，2026-10-21。ID `ftboss:butcher_fang_charm`，名称“屠夫骨牙符”。材料任务已先完成并交接，随后依 RELIC_FOLLOWUP.md 完成此遗物。

## 设计依据

- 实际旧图标为金色佩戴环、血红护套与象牙色牙坠，不把成品饰品改成原料或 Boss 雕像。
- 对应 Boss 真实渲染来源为 `ButcherBossModel.java` 与 `textures/entity/butcher_v3.png`，选用血锈红、暖骨色、旧黄铜色。沿用已完成 butcher_material 的牙釉质、骨质断面色阶及血色筋膜的视觉家族。
- 新结构为八边形锻造佩戴环、钢制转接环/黄铜插销、带衬里的血色皮革护套、金属包边和双面铆钉、前后回绕绑带、带刻纹的骨牌，以及抛光斜切骨牙。保留原图标的环/护套/牙形辨识，增加加工工艺。
- 骨牙由统一收尖内芯和连续合法 22.5° 切面组成，不依赖几个台阶方块。金属独立高光/磨损边，皮革独立缝线，骨质独立暖白/阴影；正背材质分别绘制。

## 文件范围

- `src/main/resources/assets/ftboss/models/item/butcher_fang_charm.json`：由平面入口改为 `forge:separate_transforms`。
- `src/main/resources/assets/ftboss/models/item/boss_relics/butcher_fang_charm.json`：非 GUI 三维运行模型。
- `src/main/resources/assets/ftboss/models/item/boss_relics/butcher_fang_charm_gui.json`：独立 GUI 图标模型。
- `src/main/resources/assets/ftboss/textures/item/boss_relics/butcher_fang_charm.png`：128×128 逐面 UV 图集。
- `src/main/resources/assets/ftboss/textures/item/butcher_fang_charm.png`：与最终模型同件的 32×32 像素图标。
- `art/boss_relics/blockbench/butcher_fang_charm.bbmodel`：可编辑工程。
- 专属 `tools/model3d/boss_relics_refine/butcher/`、`art/boss_relics/refine/butcher/` 与本任务目录。

修改前旧入口和旧图标已备份到 `{WORK / 'backup-before-refine'}`。其余四个运行/源工程文件此前不存在。未写其他 Boss、共享生成器/渲染器/验证器、docs、政策文件、Java 或 JAR。

## 可复现命令

在 `{ROOT}` 运行（Python + Pillow + NumPy）：

```powershell
$py = '{sys.executable}'
& $py tools/model3d/boss_relics_refine/butcher/generate.py
& $py tools/model3d/boss_relics_refine/butcher/render.py
& $py tools/model3d/boss_relics_refine/butcher/validate.py
& $py tools/model3d/boss_relics_refine/butcher/report.py
```

遗物工具只写本遗物；只读复用本线程已交付的 `tools/model3d/boss_materials_refine/butcher/` 中 UV 装箱器、Java 模型软件光栅器与 OBB/共面检查方法。两个专属工具目录一起保留即可复现。未运行 `gen_boss_relics.cjs` 或任何共享批量生成器。

## 校验

- 最终资产检查 PASS：72 个部件、30 个合法单轴旋转、432 面/独立 UV 岛、固定 2 px/模型单位；UV 全在 0..16，比例错误 0、岛重叠 0、透明采样异常 0。
- OBB 连通检查 72/72，悬空部件 0；修复一枚背铆钉和一道包边后，外露共面重叠 0。
- Blockbench 每元素坐标、尺寸、旋转、枢轴、逐面 UV 与运行模型一致；内嵌 PNG 与运行图集哈希相同。
- 入口、模型引用、粒子贴图和 GUI 路由正确；真正 elements 模型无 generated/handheld 父级，GUI 独立用 generated。8 个 display 槽齐全。
- 共享 `lint_java_model.cjs` 与 `check_uv_coverage.cjs` 通过，原始输出见 shared-checks.txt。
- 玩法 Java / Curios 文件与开工哈希一致，本遗物两条中文名/效果文本保持原值（“屠夫骨牙符”“屠戮之牙：+3 攻击伤害”）。语言库期间出现其他线程的并发更新，已保留，不以全语言文件哈希冻结并行工作。
- `validation.json` 是完整资产哈希与检查记录。未改注册 ID、Curios 槽位/效果、随机词缀/NBT、掉落或其他玩法引用。docs impact: none。

## 最终运行时四视图实际复核

- 从最终 `src/main/resources` 的 JSON/PNG 读取，软件预览正确处理逐面 UV、FaceBakery 顶点顺序、实际元素旋转、rescale、透明采样和深度剔除；本模型只用 z 轴 0/±22.5/±45°，rescale=false。
- 已使用图像查看工具实际查看最终正面、背面、左侧、右侧四视图，另查看斜视细节、图标对比及原料/遗物关联图。看得见正面刻纹、层叠护套、双面铆钉、转接扣件；四个方向无缺面、断连、悬空细节、明显 UV 拉伸、错误遮挡或画布裁切，左右厚度符合相同物品。
- 图标与模型一致，包含放大、原始 32 px 和 16 px 槽位对照；不是把 UV 图集当图标。
- 已重渲染并与保存四视图逐像素比较一致，确保预览不是旧版本。

## 最终预览与源工程绝对路径

- 四视图：`{ART / 'four_views.png'}`
- 分面：`{ART / 'front.png'}`、`{ART / 'back.png'}`、`{ART / 'left.png'}`、`{ART / 'right.png'}`
- 斜视细节：`{ART / 'oblique_detail.png'}`
- 背部斜视：`{ART / 'rear_oblique.png'}`
- 图标对比：`{ART / 'icon_comparison.png'}`
- 材料/遗物关联：`{ART / 'material_relic_relationship.png'}`
- UV 图集：`{ART / 'uv_atlas_enlarged.png'}`
- Blockbench：`{ROOT / 'art/boss_relics/blockbench/butcher_fang_charm.bbmodel'}`

## SHA256

| 资源 | SHA256 |
| --- | --- |
'''
    report += '\n'.join(f'| `{path}` | `{digest}` |' for path, digest in validation['asset_sha256'].items())
    report += '\n\n未解决问题：无已知资产错误。未全量 Gradle、未构建 JAR、未部署、未启动游戏；游戏内验证与统一整合发布留给统筹。\n'
    (WORK / 'HANDOFF.md').write_text(report, encoding='utf-8', newline='\n')
    print('Relic HANDOFF.md and shared-checks.txt written')


if __name__ == '__main__':
    main()
