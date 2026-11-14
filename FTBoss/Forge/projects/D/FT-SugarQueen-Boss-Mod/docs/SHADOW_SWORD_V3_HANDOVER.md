# 噬影之刃 · 裂瞳 V3 + 裂瞳回响 + 裂瞳投珠 —— 交接文档（第二版）

> 更新：2026-09-15（第二版）。部署哈希 **`BBA3999D8F0DA2B7`**（四路 jar 一致）。
> 第一版见 git 历史/对话；参考资料 `docs/MODEL_REFERENCE_NOTES.md`；工程规则 `AGENTS.md`。

## 一、当前事实（可核对）

| 项 | 值 |
|---|---|
| 几何 | **61 cube / 19 骨骼 / 4 处静态旋转（含 45° 斜切）/ 10 处 inflate** |
| 剑刃 | `blade` 骨骼 **38 cube** = 8 条×4 段 + 三级剑尖 + 2 倒钩 + **1 个 45° 斜切** |
| 贴图 | `shadow_sword.png` 256×256 逐面 UV（含 **5 个独立 UV 岛**）+ `shadow_sword_ready.png`（冷却就绪态）+ `shadow_sword_e.png` |
| 动画 | **8 段**：`idle 4s` `charge 0.8s` `release 0.5s` `recover 0.6s` `cast 0.9s` `cancel 0.2s` `cooldown_ready 0.65s` `ready_idle 2.4s` |
| 骨骼树 | `root → grip_anchor → weapon →` {`handle`(5) `blade`(38) `guard`(1) `pommel`(2)→`crystal_link`(1)→`crystal`(1)}；`guard`→{`claw_left`(1)→`claw_left_tip`(1) `claw_right`(1)→`claw_right_tip`(1) `eye_mount`(4)→`eye`(1)→`pupil`(2)}；`eye`→`cast_socket`(0)→`charge_orb`(1)→`charge_core`(1) |
| Java | `ShadowSwordItem`（GeoItem/技能入口/**ready NBT**/回响边沿+粒子）、`ShadowSwordGeoRenderer`（**ready 贴图切换**）、`ShadowCastManager`（状态机+事件+hook）、`ShadowPearlEntity`（**传送+AOE**） |

## 二、玩法（已上线）

| 功能 | 行为 |
|---|---|
| **裂瞳投珠** | 长按右键蓄力 → 服务器状态机：**第 7 tick 发射**、第 18 tick 结束；提前松手/换物/死亡/登出/换维度 → 取消（`cancel` 动画）；一次使用最多发射一次 |
| **暗影珍珠** | 无重力直线、**扫掠线段判定**（不穿薄墙/小实体）、不穿透、不破坏方块、40t 超时；命中即移除（单次结算） |
| **命中效果** | 直接命中 **8 点**；**把使用者传送到命中点**（安全落点搜索 + 清零坠落距离 + 保留视角 + 传送音）；落点半径 **3.0** 内 **5 点** + **致盲 6 秒**（排除自己与友军）；紫黑碎屑 + `FLASH` + 紫晶破碎音 |
| **裂瞳回响** | 冷却中就绪标记=false（暗紫黑 + `idle`）；冷却完成 → **只播一次** `cooldown_ready` + 一次性 8 个粒子；此后循环 `ready_idle`（魔眼/竖瞳/晶核低幅呼吸）；**ready 标记写在 ItemStack NBT（`ftboss_ready`）** ⇒ 每把剑/展示框/别人手里的剑**各自独立** ✓；就绪时贴图切到 `shadow_sword_ready.png` |
| **冷却** | 发射时起算 **40 tick**；取消冷却 4 tick |

## 三、造型要点

- **剑刃**：8 条三级厚度（刃口 0.42/0.62 薄 → 剑面 1.15 → 剑脊 2.00 略厚）+ 中缝**血槽 1.45（凹）**；宽度合计 4.0
- **四段厚度阶梯** `SEGF=[1.00,1.045,1.09,1.14]` → 脊侧最外 x 2.00 / 2.28 / 2.13 / 2.42（**刻意保留的锯齿刻口**，用户明确要求 ✓）
- **两个倒钩**：背侧、静态后掠 22.5°、深 1.3、geo x 1.55~2.55
- **45° 斜尖**：枢轴取剑尖 `tip_b` 的脊角 (2.20, 19.35)，薄板 1.10×0.40×**0.95（与相邻台阶同厚 ⇒ 面齐平）**，`rotation [0,0,-45]` → 旋转后 x 1.14~2.20、y 19.07~20.13（贴脊不凸出、不超高）
- **魔眼**：`eye_mount` 挂 4 条眼眶边条（**不随虹膜缩放**）、虹膜 3.15 深、瞳孔前后两片各厚 0.50
- **护爪**：根厚（h+0.15）尖薄（h−0.45），两段保持 0.1 隐藏重叠
- **柄尾**：宽底座 / 宝石环（前后各露 0.1）/ 通芯基座（与底座重叠 0.05 ⇒ 直连）+ `crystal_link` 短轴
- **聚能球**：`cast_socket`(魔眼前 2.4) → `charge_orb`(1.3³, 45°) → `charge_core`(0.4³)；idle/cancel 缩放归 0 ⇒ 待机不可见

## 四、UV 岛（5 个，全部落在实测空闲区并配画法）

| 部件 | 岛位 (px) | 画法 |
|---|---|---|
| `charge_orb` | 160,80 24×24 | 径向渐变：中心 C6 → C5 → C4 → 外圈 C1 + 描边 |
| `charge_core` | 186,80 16×16 | 中心 C6 → 边缘 C5 |
| `eye_mount` 眼眶 | 206,80 40×24 | 近黑 + 上下受光边 + 两侧过渡 + 凹槽暗紫 |
| `barb_lo/hi` 倒钩 | 200,104 32×24 | 上亮下暗 + 外侧**冷灰 C3**（**别用 C6**，22.5° 斜视会成白点）+ 内侧压暗 + 暗紫裂纹 |
| `tip_bevel` 斜切 | 208,176 32×24 | 冷灰受光斜面 + 上沿细亮边 + 下沿接缝暗 + 中间暗紫 |

## 五、复现链（**9 步，顺序不能换**）

```
node tools/model3d/regen_shadow_sword.cjs          # 静态 Java 模型（8 条剑刃/节带/断续刃口/三级剑尖）
node %TEMP%\switch_shadow_entity.cjs               # 备份静态版 + 换 builtin/entity 存根
node tools/model3d/java_to_geo_rig.cjs shadow_sword shadow_sword shadow_sword ref/shadow_sword_static_v18.json.bak
node tools/model3d/add_cast_skill.cjs              # 聚能骨骼 + cast 动画（会覆写动画文件！）
node tools/model3d/upgrade_cast_v2.cjs             # 施法 v2（渐入/复位/cancel）
node tools/model3d/add_ready_feedback.cjs          # 裂瞳回响：两段动画 + ready 贴图
node tools/model3d/v3_uv_islands.cjs               # 画岛：聚能球/亮核/眼眶
node tools/model3d/v3_barbs.cjs                    # 倒钩（geo 后处理 + 画岛，幂等）
node tools/model3d/v3_tip_bevel2.cjs               # 45° 斜切（geo 后处理 + 自检 + 画岛，幂等）
# 最后：gradle build --rerun-tasks + tools\deploy_jar.ps1 -NoBuild
```

**每次改完必跑**：`check_sword_compat.cjs`（门禁：已有动画逐段不变、允许新增 ✓）→ `check_shadow_struct.cjs`（疑似脱离/稳定性/UV）→ `sword_preview.cjs render`（出图）→ `export_bbmodel.cjs`（Blockbench 工程）。

## 六、工具可信度

| 工具 | 能信 | 不能信 |
|---|---|---|
| `geo_transform.cjs` | 变换/UV 解析（A~F 校准 PASS） | 不含渲染 |
| `sword_preview.cjs` | Z-buffer 渲染、逐像素采样、报告 JSON（**严格校验 cube 字段**） | 无控制器混合/Molang/Box UV |
| `check_shadow_struct.cjs` | 疑似脱离、稳定性、UV 复用组 | AABB 重叠**只是嫌疑**；不要据此删几何 |
| `check_sword_compat.cjs` | 骨骼/pivot/rotation 不变、动画逐段不变 | 不判视觉 |
| `render_geo.cjs` | 快速多视角（画家算法） | 无 z-buffer |

## 七、踩坑清单（**12 条，别重犯**）

1. **Bedrock cube 旋转是数组 `[x,y,z]`**（Java 是 `{origin,axis,angle}`）→ 抄错 = `GeckoLibException` + 资源重载卡死
2. **原点用"握点"**（GeckoLib 武器约定手抓处 = (0,0,0)）；用方块中心(y−8) → 第三人称抬高 3.1 格
3. **父子变换必须"子先作用 → 逐级回根"**（写反 = "护爪看着断开"的假象）
4. **悬浮检测**：只测未旋转 AABB 会漏"旋转后脱离"；只按 AABB 又会误报（瞳孔/晶核/聚能球）→ 用完整父级变换 + 跳过零缩放
5. **斜插半埋薄片从侧面必露小块**（倒角/背侧切面/旋转刀尖）→ 需要斜构件时用**与相邻件同厚的整块**
6. **`rotation` 一定要走 geo 后处理**（`v3_barbs.cjs` / `v3_tip_bevel2.cjs` 那套）✗ **不要走生成器的 `put()`+rotation** —— 枢轴空间不一致会把整块甩到 7 单位外（用户看到"侧边悬浮方块"）
7. **写完先自检再落盘**：新加的旋转件必须"与主体最大重叠体积 ≥ 0.2"、不凸出轮廓、不超高；不满足就 `exit(1)` 拒绝写入（这条已两次救场）
8. **cube 不要写 `name` 等额外字段** ✗ 严格校验的渲染器会直接报错
9. **新开 UV 岛必须同时写画法** ✗（否则采到空白/串色 = "怪色块"）
10. **近白 `#EAD5F5` 要克制**：连续刃口线 = 白色灯带 ✗ → 断续（每 0.36 画 0.16）；倒钩外侧用冷灰
11. **`preview_spin.cjs` 对 `builtin/entity` 模型无效** → 用 `sword_preview.cjs` / `render_geo.cjs`
12. 环境：PS 的 `cd` **不影响 .NET 文件 API 相对路径**（用绝对路径）；`node -e` 双引号会被 PS 吃掉（改 here-string 喂 stdin）；`[ ]` 文件名要 `-LiteralPath`；附件常是"整条消息"→ 按 ```javascript 围栏抽取；`getCurrentItemStack()` 是**实例方法**，不能在静态嵌套类里调用（贴图切换要写在渲染器外层）

## 八、剩余小尾巴

1. `regionOverlaps` 1768 条分类收敛（把"剑身相邻面刻意 0.05 重叠"与意外重叠分开）
2. §8 细节：缠带在侧面的连续性、裂纹分叉（现 3 条偏规整）
3. 服务端冷却表清理（`WAS_READY` 在登出/换维度时移除表项）
4. `cast @0.349s` 聚能球最大时与魔眼/护手有 ≤1.14 轻微嵌入（要更干净就外移 `cast_socket` 0.2，属 §7 冻结项需点头）
5. `handle × eye` 动画重叠最大 0.64（可优化）

## 九、备份（`ref/`）

```
shadow_sword.v3pre.geo.json / .animation.json / .png   V3 前基准（门禁对照）
shadow_sword.before_cast_v2.animation.json              施法 v2 前
shadow_sword.before_ready.animation.json / .png          裂瞳回响前
shadow_sword_static_v18.json.bak                        静态 Java 模型（转 geo 输入）
check_shadow_struct.v1.cjs                              旧结构检查器
```
