# S1 苔藓古林 · 第二轮功能房间 11—20

只在本目录制作；未写入 saves、mods、正式世界或旧八组。参考索引见 references.json。

当前图书馆已按真实参考制作完整墙顶 NBT；真实 Lodestone 的 complete.png 为完整外观，cutaway.png 仅是显示副本剖切。不是概念图充当预览。

每间 recipe.mjs 配合 ../common.mjs 可复现；design.json 记录接口、特色与差异。运行 `./qa-room.ps1 11-library` 生成与渲染，再运行 `python check.py 11-library` 做原生静态检查。Python 使用工单指定环境。

QA 使用只读 vanilla 1.20.1 mapped jar 和既有检查类；在各间 qa 目录执行，单 Java 进程 -Xmx1536m。原生注册/属性、StructureTemplate 回读、部分易脱落装饰 canSurvive、0.6×1.8 人体碰撞和空容器属于制作方自检，**不是独立验收**。未运行实际放置、邻居更新、随机/流体 tick、光照、生存或战斗验收；梯子攀爬未获路线模拟认证。

progress.json 记录候选哈希；只有完成当前图真实渲染查看与静态自检才进入下一图。十间完成后才开始合并。
