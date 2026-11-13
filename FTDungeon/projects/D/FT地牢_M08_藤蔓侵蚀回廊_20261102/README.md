# M08「藤蔓侵蚀回廊」独立交付

基于已存在的 image2.5 参考图制作，没有重新付费生图。仅独立交付，**不部署、不写正式存档、不改 FT 核心或共享 incoming、不覆盖其他房间**。

## 交付入口

`out/` 的四件文件：`room.nbt`、`room.json`、`preview.png`、`qa.json`。接入负责人只领取该目录；`native-test/` 是隔离 QA 模组源码，**不可部署**。

- Minecraft Java 1.20.1 / DataVersion 3465，gzip 单一 palette 结构模板。
- 真实尺寸 XYZ：**19×11×23**；北入口脚位 **[9,1,0]**；内部安全脚位 **[9,1,2]**。
- 北口三格宽、三格高，连续完整地板；五格宽中央干路通向南端石台前沿。
- 4807 条显式记录、2016 个非空气方块；7 个显式空桶，无 LootTable、奖励、刷怪笼或实体。
- 完整木石屋顶与有边界的玻璃天窗；没有真水、熔岩、火或坠落陷坑。

NBT SHA-256：`bbc4f702c195e0c787ba57d7d417a1765311b1bcac3cc0fb965db39a5ef81699`。

## 设计对应

参考图：`D:\ft工程文件\image2.5\dungeon-rooms-medium-random-20261102\out\M08-vine-overrun-hall-hero.png`，1536×1024。

原图 SHA-256：`b1b4990e31ee724aad3bbe2185b636bc392d6bd0e79b46190bc4c972be607f81`，与用户指纹和 master manifest 一致。

保留荒废生长主题：苔石/裂纹砖地面、双侧四开间嵌墙柱拱、木梁架、垂藤与常绿叶簇、吊链灯、边缘碎石、空木桶、后侧石台和红金纹样。红金挂饰以贴墙羊毛/陶瓦图案表达，非额外实体。概念图的屋顶破口按非花园契约改为封闭玻璃天窗；不伪造原图的光束或着色器效果。

![实际 NBT 软件剖切预览](out/preview.png)

预览从最终 NBT、Minecraft 原生方块轮廓和已安装原版纹理生成，不是 AI 设计图或客户端截图。只在预览切掉近侧上墙与屋顶，保留梁架；完整结构见 `evidence/preview-full-envelope.png`，四向见 `evidence/four-view-contact-sheet.png`，干路见 `evidence/plan-dry-routes.png`。植物、吊灯与透明模型以原生轮廓包围盒近似，不替代游戏渲染。

## QA 证据

- `qa/static-qa.json`：23 项静态检查，含北接口、净空、五条可往返路线、完整屋顶、植被/吊灯挂点、空容器和连到底板的建筑连接。
- `qa/native-registry-qa.json`：真实 Minecraft 1.20.1 注册表和方块属性，原生 StructureTemplate 加载/保存往返，附渲染使用的原生形状。
- `qa/native-world-qa.json`：Forge 47.4.10 隔离 GameTest，四种旋转实际放置；每快照核对19228条记录；逐非空气块 `canSurvive`、完整地板/屋顶、0.6×1.8玩家碰撞与干路、七个空桶均通过。
- 原生邻居更新、四轮显式原生植被随机 tick，加正常随机刻速度3和200 tick稳定性复查；结构保持精确，允许仍合法的藤蔓生长/常绿叶距离更新，并如实统计差异；不是“活植物永远字节不变”的承诺。
- 四套夹具放置前全体积空闲检查，完成后仅回滚各自已验证原为空气的体积并复核。测试世界只在本工程 `native-test/run/m08-isolated-gametest/` 内。
- 日志保留在 `evidence/forge-gametest-*.log`；最新成功日志、完整文件哈希及 ZIP 指纹分别见 `out/qa.json`、`delivery-manifest.json`、`package-receipt.json`。

## 重跑

本机已缓存 ForgeGradle 6.0.54、Forge 47.4.10、Gradle 8.8；使用 Java17 和已配置 Python/Pillow：

```powershell
& 'D:\ft工程文件\FT地牢_M08_藤蔓侵蚀回廊_20261102\Run-QA.ps1' -Rebuild
```

脚本仅运行独立工程，保留每轮日志，恢复进程 Java/Python 相关环境变量。其他机器需提供 Java17、依赖缓存/联网首次构建以及原版1.20.1纹理JAR路径。正式存档不参与测试。

## 接入与边界

建议模板ID `ftdungeonmobs:image25/m08` 仅供接入负责人分配，不代表已注册或已保留槽位。正式接入前须核查与 M8/M08 的命名兼容；勿覆盖旧房或持久生成标记。

模板显式空气不意味着允许清空世界。首次施工必须检查**完整体积**、边界、已有方块/液体/实体和入口连接；占用时拒绝并诊断。保留已有玩家改建、独立持久标记、幂等、个人返回点和五档难度。

未做 FT 目录注册、从大厅到北口的实际支路、正式核心集成回归、整合包客户端/多人游玩或部署；这些仍由唯一接入负责人完成。`qa/boundary-audit.json` 是本次操作范围核查，不冒充全盘前后哈希证明，也不会回退并行线程的新版本。
