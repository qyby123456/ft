# S6-06 熔岩黑石转角走廊

状态：制作方离线检查通过，待独立复核。只对应S6-06-corner.png，未启动S6-07。没有修改正式存档、模组、配置或注册表。

## 可放置候选

仅根目录room.nbt为交付候选。29×12×29，DataVersion3465，全部10092坐标显式记录，含空气；放置会清除包围盒中的空气区域，应预留完整包围盒。建筑实际为L形占地，东北外部象限在包围盒内为空气。未执行世界放置。

北接口[7,3,0]，东接口[28,3,21]，均为三宽五高、深二格，坐标为本地脚点。地板面y3。先沿x6..8走至z20..22，再沿z20..22向东。内角实体迫使路线转九十度，只有这两个外边界通道。assembly.json提供连接与顺序，world_origin为空。不要把剖面或局部NBT用于放置。

## 证据

- analysis.md：先看图后制定的构图重建解释及差异。
- recipe.mjs：从空结构独立生成的本图几何。仅检查基础设施从既有模块复用。
- qa/static.json：两臂连续三宽、实体内角、双口与其余边界闭合、645列L形顶盖、全部实体连地、40格六面封闭零级熔岩。
- qa/native.json：实际原版1.20.1注册表、StructureTemplate加载/回写、16目标碰撞路线、5盏灯canSurvive、实际材料FireBlock概率全零。
- qa/body-routes.json：0.6宽×1.8高身体、y3平面、半格搜索并每0.1格采样边；整脚掌下方为干燥承重面。16目标可达。
- qa/duplicates.json：与75个本地同类候选比较分类几何，忽略材料、XZ平移/Y轴旋转与镜像，无精确匹配；不等于语义相似度或全局唯一认证。
- qa/predecessor-lock.json：绑定S6-05独立验收报告和冻结NBT，逐项重验此前六目录文件哈希。
- lodestone/screenshots.json：8张真实Lodestone浏览器截图、HTTP实际加载NBT哈希、图片哈希和相机。不是Minecraft游戏截图。仅在内存中暴露原预览相机桥接，无磁盘技能或材料改动。
- lodestone/cutaway-provenance.json：剖面仅删除顶盖与近侧墙的记录，未增加或改状态；西墙切线也移除了熔岩窗，剖面不可用于流体边界检查。
- lodestone/detail-provenance.json：灯龛局部取源[9,1,13]..[14,9,18]，仅平移为6×9×6，无增补。
- manifest.json：全部交接文件（自身除外）的SHA256与字节数。

## 本地预览

完整候选：http://127.0.0.1:5441/?nbt=%2F__minecraft_builder__%2Fbuild.nbt

仅检查剖面：http://127.0.0.1:5442/?nbt=%2F__minecraft_builder__%2Fbuild.nbt

灯龛原始裁切：http://127.0.0.1:5443/?nbt=%2F__minecraft_builder__%2Fbuild.nbt

通过技能目录的scripts/preview.mjs加--nbt绝对路径、--port端口及--no-open可重新开启。check.py static native derivatives duplicates重验本候选；不执行build覆盖已有NBT。capture.cjs要求三预览端口在线；finalize.py核验并更新本目录清单。

## 范围限制

这些是离线静态材料、形状、连接和选点支撑检查。Forge放置、邻居更新、流体/火焰/随机刻、光照传播、旋转、实际交互及热伤害均NOT_RUN。玻璃封闭与内部燃料概率为零不构成外部世界防火认证。图像忠实度和逐图放行等待独立复核。
