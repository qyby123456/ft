# S2-08 砂岩树影跌水庭院

仅第2组第8张候选，待独立复核；第9张未开始。候选为 `incoming/G2B-08/room.nbt`，Java 1.20.1 / DataVersion 3465，33×24×35，完整显式空气包络。

对应原图 `D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-08-courtyard.png`。原图哈希、材料清单、坐标接口与目标点见 room.json / materials.json；设计拆解与近似边界见 ANALYSIS.md。

庭院包含中央阶梯喷泉、实际水方块跌水、大树与树根、露天池岸、U形上下回廊、显式墙栏杆、可攀登楼梯、阶梯拱与花盆藤蔓。北接口脚点Y1，经两级台阶抵达庭院Y3；其余三个接口Y3，上层回廊Y11。

静态检查：原版注册表及属性、StructureTemplate回写、35个带真实原版标签的canSurvive点、22个路线目标的0.6×1.8玩家碰撞体离线检查。路线包括四向接口、绕池、树边、楼梯与上层U廊。全部自检通过后才由 finalize.py 生成清单；复用独立复核工具的输出文件名不代表已获独立验收。

7张Lodestone实际浏览器截图：exterior / reverse / detail 直接加载完整交付NBT；north / south / west / east是只用于观察的剖切副本。derivation.json列出原始哈希、派生哈希、精确移除坐标和规则；qa/lodestone-screenshots.json记录每张图实际加载的NBT与相机。不得将 preview-only 文件用于投放。

动态边界：水源Y8与跌水初态不等于已验证流体稳定；水流扩散、生成新水源、荷叶在更新后的表现未运行。树叶persistent=true，仅降低正常凋零风险；未运行随机刻、火、光照或天气。35个canSurvive点不包括树叶。四片荷叶由水支持，单独检查，未伪装为实体固体连接。详细范围见 qa/dynamic-boundaries.json。

实际游戏放置、邻居更新、服务器流体调度、光照、刷怪、降雪、旋转、镜像、交互均未测试；未部署、未写入存档、未修改整合包或配置。前7张已验收源文件哈希在registration.json逐项复核。

复现：使用已配置node运行技能run-build.mjs，recipe.mjs --out-dir incoming/G2B-08 --name room --data-version 3465；随后运行prepare.py、validate-native.ps1、capture.mjs、finalize.py。finalize.py固定最终NBT哈希，任何改动必须重新完整验证和截图。
