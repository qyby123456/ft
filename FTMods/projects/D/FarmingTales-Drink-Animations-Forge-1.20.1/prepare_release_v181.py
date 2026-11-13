from pathlib import Path
import zipfile

ROOT=Path(__file__).absolute().parent
readme='''# 农场物语-饮用与检视动画 1.8.1

Minecraft 1.20.1 / Forge 47.4.10 客户端独立模组，支持 53 种酒水，含 29 种杯装饮品。

## 本版调整

红葡萄汁、蓝莓汁、甜浆果汁和白葡萄汁杯沿叶子，以及神秘果酒、樱桃利口酒丝带，增加随容器加速度产生的惯性摆动。停手后短暂回弹再衰减，连接点固定。使用原装饰几何与 UV 的独立副本，没有重画贴图。丝带采用整体绕根部摆动，不是多段布料模拟。

南瓜酒杯木棍增加轻微摆动；椰子酒杯、神秘果酒杯、朗姆酒杯冰块随液面浮动，并限制在杯壁和杯底范围内。游戏中的实时液面和装饰运动会受到行走、转向及动作影响。

全部杯装饮用改为一次连续抬杯，消除中途三段停顿。瓶装酒举嘴饮用的位置降低，手和瓶共同移动。神秘果酒、樱桃利口酒上段瓶颈与装饰相对初始模型下移 0.25 模型像素，比 1.8.0 再下移 0.15 像素。

饮用水检视保留抛起飞行段，缩短接稳后手向后归位的时间，接续奔跑时保留同一转圈方向。这里没有整体加快所有饮品的收回动作。

放到地面或桌面时使用连续的前送、松手、收手轨迹：约 0.205 秒松手、0.5 秒收下；有剩余时接续拿出，整个动作约 1.1 秒。最后一件放下后回到空手。快速连续放置沿用正在进行的手势，不重置到静止姿势。方块仍按原逻辑立即放置，每次正常消耗，放置失败不播放动作。

## 参考与操作

本轮只读研究 HMI 的放置/持物处理，并以 0.1 秒间隔查看用户视频 mmexport1789280220509.mp4 的 7:36–7:51.9 汤类举碗片段，研究连续抬升与停下后的滞后动作。使用原创曲线，未复制参考代码、动画数据或音轨。研究记录在 references/2026-09-14-hmi-soup/analysis.md。

默认 V 自定义食用、G 检视，可在键位设置更改。普通右键饮用仍使用原版动画；成功放置饮品时播放放置动作。自定义食用维持正常移动速度。camera 保持轻微。饮用保留四声吞咽；全部音频与 1.8.0 一致，摇水声保持降低 50%，布料维持轻声。

## Blockbench

blockbench/酒水扩展-v31.zip 包含 106 个工程：53 种饮品 × Steve / 纤细手臂。每个工程包含 idle、draw、put_away、inspect、drink、walk、sprint、jump、place、preview_complete。

安装随包的 cameras.js 后，将预览关联 camera_first_person。手臂、瓶子、camera 和装饰骨骼来自相同运行时代码的 60 fps 采样，经贝塞尔关键帧精简并回读拟合检查。静态模型使用原贴图。液面在 Blockbench 中为酒量代理；实时液体、玩家转头附加惯性和瓶内手部遮挡由游戏计算。

## 验证与恢复

构建：gradlew.bat --offline --gradle-user-home Z:/.gradle-user test build exportDrinkAnimations。82 个单元测试通过。隔离客户端使用 Embeddium、Oculus（关闭光影）、森罗烹饪/烤肉/进食动画及 GeckoLib，窗口在创建前隐藏；检查 16 款纤细手臂、13 款 Steve 手臂，覆盖全部新增装饰及代表杯型、放置、普通右键、四次吞咽和消费衔接。代表动作按 0.1 秒截图复查。完整整合包与用户存档未启动。

verify_release.py 核验 JAR、模型和贴图、原整合包文件哈希、配置及声音一致性、客户端结果和 Blockbench 导出包；通过后 install_release.ps1 安装。详细结果见 verification.json。

1.8.0 JAR、配置和源码备份在 backups/1.8.0-before-motion-refinement。安装记录为 installation-1.8.1.json。重启客户端生效。
'''
instructions='''农场物语-饮用与检视动画 1.8.1 / 酒水扩展-v31

53 种饮品 × Steve / 纤细手臂，共 106 个工程，内嵌原贴图。
每个工程含 idle、draw、put_away、inspect、drink、walk、sprint、jump、place、preview_complete。
place 是放到地面或桌面；put_away 是收回口袋。

在动画模式选择 inspect 或 drink，安装 cameras.js 后关联 camera_first_person 查看第一人称。手臂、容器、camera 和装饰骨骼由模组运行时采样导出，经贝塞尔精简及拟合检查。

本版增加四款果汁杯叶子、两款酒瓶丝带、南瓜杯木棍与三款冰块杯的惯性摆动。骨骼位于 bottle 下，可单独编辑。叶子、丝带和木棍绕固定连接点摆动；冰块跟随液面。游戏中还会受玩家移动和转向影响。
全部杯装饮用改为连续抬杯；瓶酒饮用位置降低。饮用水接稳后加快归位；放置动作约 1.1 秒，包含放下、松手、收手与有剩余时再次拿出。

液面在 Blockbench 中用酒量代理显示；实时液体惯性和瓶内手部遮挡仅在游戏中计算。preview_complete 包含拿出、检视和收回，单独 drink 可查看食用。
V 食用、G 检视，可改键；普通右键饮用保持原版。音频沿用 1.8.0，含四声吞咽、降低 50% 的摇水声和轻声布料。
'''
analysis='''# 1.8.1 动作参考记录

只读参考 HMI 5.1.1 (1.21.11).jar 的 item_pose.lua / hand_pose.lua，重点查看放置的短送出、回弹，以及方块持物对额外俯仰的处理。该文件面向不同游戏版本，未安装到整合包，也没有复制代码。

用户视频 mmexport1789280220509.mp4：7:36.0–7:51.9，每 0.1 秒一帧，共 160 帧，保存在 soup-frames，8 张图板全部查看。可见容器抬起到嘴边是连续路径，临近目标后才明显减速；停下时液体与配料仍有轻微滞后。未提取其动画数据或音轨。

据此制作原创连续抬杯曲线，放置使用节点速度、加速度连续的路径；对配料和叶子、丝带使用加速度驱动的阻尼弹簧。相机保持低幅度，以遵守用户减轻眩晕的要求。

游戏复查：preview/review-v31 下的动作图板以 0.1 秒采样，覆盖杯装连续抬升、木棍/冰块、丝带/叶子、饮用水接稳归位和放置。检查中未见附件飞离、纹理丢失或连续放置使手势突然重启。arm-comparison.jpg 对照两种手臂及瓶颈接缝。截图用于检查可见连续性和几何，并不能代替用户在完整整合包中的手感评价。

客户端范围：16 款纤细手臂、13 款 Steve 手臂，隔离 Forge 客户端，窗口创建前隐藏，光影关闭。未启动完整整合包或用户存档。运行证据在 preview/smoke-slim-1.8.1.log、preview/smoke-wide-1.8.1.log。
'''
(ROOT/'README.md').write_text(readme,encoding='utf-8')
(ROOT/'references/2026-09-14-hmi-soup/analysis.md').write_text(analysis,encoding='utf-8')
(ROOT/'blockbench/使用说明-v31.txt').write_text(instructions,encoding='utf-8')
folder=ROOT/'blockbench/酒水扩展-v31'
(folder/'使用说明.txt').write_text(instructions,encoding='utf-8')
exporter=ROOT/'blockbench/04-导出工具/旧版脚本/export_drinks_v31.py'
lines=exporter.read_text(encoding='utf-8').splitlines()
lines=["shutil.copy2(ROOT/'使用说明-v31.txt',OUT/'使用说明.txt')" if line.startswith("(OUT/'使用说明.txt').write_text(") else line for line in lines]
exporter.write_text('\n'.join(lines)+'\n',encoding='utf-8')
(ROOT/'verify_release.py').write_text('from verify_release_v181 import *\n',encoding='utf-8')
installer=ROOT/'install_release.ps1'
text=installer.read_text(encoding='utf-8-sig')
text=text.replace("$animVersion = '1.8.0'", "$animVersion = '1.8.1'")
text=text.replace('for 1.8.0 before installing','for 1.8.1 before installing')
text=text.replace('1.7.2-before-cup-expansion','1.8.0-before-motion-refinement')
text=text.replace('farmingtales_drink_anim-1.7.2.jar','farmingtales_drink_anim-1.8.0.jar')
text=text.replace('installed 1.7.2 animation','installed 1.8.0 animation')
text=text.replace("client_runs.'cups-slim'",'client_runs.slim')
text=text.replace('installation-1.8.0.json','installation-1.8.1.json')
installer.write_text(text,encoding='utf-8-sig')
with zipfile.ZipFile(ROOT/'blockbench/酒水扩展-v31.zip','w',zipfile.ZIP_DEFLATED,compresslevel=6) as archive:
    for file in sorted(folder.iterdir()):
        if file.is_file():archive.write(file,file.name)
print('Prepared documentation, installer and v31 archive.')
