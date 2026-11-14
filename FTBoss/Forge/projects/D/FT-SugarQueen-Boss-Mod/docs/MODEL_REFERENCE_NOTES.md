# 建模 / 贴图参考资料笔记（YSM + 灾变）

> 2026-09-15 应「参考 ysm 文件夹的全部内容和灾变 mod 的内容」系统整理。
> 只记**能用到本工程**的结论与口径，不复制原始素材。

## 1. Yes Steve Model（`C:\Users\YOUR_USERNAME\OneDrive\桌面\yes_steve_model`）

- 明文 Bedrock geo + png 在 `default/` `steve/` `alex/` `default_boy/` `wine_fox/` `wine_fox_jk/`
  `qingluka/` `宇航员酒狐/` `custom/*` 里；`.ysm` 是加密包，别硬啃。
- 量化工具：`node tools/_archive/model3d/ysm_probe.cjs <model.json>`（贴图尺寸/骨骼/cube 密度/rotation/inflate/逐面 UV/层级深）。

| 模型 | 贴图 | 骨骼 | cube | 带旋转 | 带 inflate | 逐面 UV | 层级深 |
|---|---|---|---|---|---|---|---|
| `default/models/arrow.json`（一支箭，最像"物品"） | 64×64 | 2 | **27** | **25** | 7 | **27（100%）** | 0 |
| `default/models/main.json` | 128×128 | 65 | 172 | 84 | 0 | 0 | 14 |
| `steve/models/main.json` | 64×64 | 61 | 46 | 4 | 13 | 46（100%） | 13 |
| `alex/models/main.json` | 64×64 | 61 | 47 | 4 | 13 | 47（100%） | 13 |
| `default_boy/models/main.json` | 128×128 | 58 | 156 | 81 | 4 | 0 | 10 |
| `wine_fox/models/main.json`（旗舰） | 256×256 | **188** | **412** | **307（75%）** | **139（34%）** | 0 | 15 |
| `wine_fox/models/arm.json` | 256×256 | 13 | 34 | 22 | 20 | 0 | 4 |
| `qingluka/models/main.json` | 64×64 | 87 | 64 | 26 | 0 | 64（100%） | 10 |
| `custom/default_controllers/models/main.json` | 256×256 | 133 | 241 | 149（62%） | 13 | 241（100%） | 17 |

结论：
1. **小物件也照样细分**：一支箭都是 27 个 cube、25 个带旋转、7 个 inflate —— 复杂度不靠"少而大"，靠"多而小"。
2. **旋转块是主力**（旗舰 75% 的 cube 带旋转）：斜杆/绳/羽毛/棱面都靠 ±22.5°/±45° 拼。
3. **`inflate` 用得很凶**（旗舰 139 处）：分层、浮动细节不用手算厚度差。
4. 两种 UV 流派并存：`steve`/`alex`/`qingluka`/`arrow` 是 **100% 逐面 UV**；`default`/`wine_fox` 走 box-UV。
   要精确控制每个面（本工程的需求）就必须逐面 UV。
5. 贴图档位：简单模型 64×64、常规 128×128、旗舰 256×256 —— 按模型在屏幕上的大小定档。

## 2. 灾变 L_Enders Cataclysm 3.31

- **实体模型全部编译进 class**：jar 里 `models/entity/*.json` **0 个**、geo **0 个**、渲染器 class **185 个**
  → 想学"建模结构"读不到；**能读的是贴图**。
- 贴图规模：**236 张实体贴图**（大 Boss **512×512**，如 `entity/monstrosity/netherite_monstrosity.png`；
  其余 256×256）+ **145 张物品贴图**（武器 **128×128**）+ `_layer` 发光分层（如 `laser_gatling_layer.png`）
  + 半透明变体（`soul_render_ghost.png`）。
- **读别人的 mod jar 用 .NET**：`[System.IO.Compression.ZipFile]::OpenRead($jar)` 枚举条目（按 `Length` 排），
  `[System.IO.Compression.ZipFileExtensions]::ExtractToFile($e,$out,$true)` 解单个文件。
  `tar -tvf` 的列宽会变，用正则抠大小容易抠错列（2026-09-15 就抠成了日期列）。

### 借到并已用在暗影刀上的手法
1. **大面积撒细噪点**防"死平"（`stipple()` + `hash2` 保证可重现）。
2. **高饱和"热芯"发光裂纹**：亮芯 + 暗晕 + 从刀背分叉伸向刃口。
3. **每块面板 1px 描边**、**沿板边一排铆钉**（刀背 5 颗）。
4. **发光层**：灾变给每把武器配 `_layer`；本工程按 OptiFine/Iris 的 `_e` 约定生成
   `assets/ftboss/textures/item/shadow_sword_e.png`（只保留紫罗兰高亮：符文/裂纹/宝石）。
   **需要装支持 emissive 的 shaderpack 才看得见**；没装也无害（多一个不被引用的贴图）。
5. **物品贴图布局**：每个部件画成带**硬黑描边**的小图形、平铺不重叠（他们的武器贴图就是这种布局）。

## 3. 农场物语体系里的 3D 食物模型（2026-09-15 增补）

**去哪儿找**（都在实例 `mods/` 里，注意文件名带中文和 `[]`，PowerShell 要用 `-LiteralPath`）：
- `[森罗物语：烹饪] kaleidoscopecookery-1.4.1-forge+mc1.20.1.jar` —— **3D 食物模型 673 个**（最多 26 cube），最值得抄
- `[铁魔法乐事]farmers_spell-1.0.5-1.20.1-all.jar` —— 60 个（`eden_apple_tart` 20 cube，且带 `display`，是"也能当物品"的食物）
- `[农场物语-核心]farmingtales_core-1.0.5.jar` —— **0 个模型**（它只是框架/代码包，别去里面找美术）

**两个代表模型的解剖**：

| 模型 | cube | 逐面 UV | 带旋转 | inflate/shade/tint | 贴图 | 高度 |
|---|---|---|---|---|---|---|
| `farmers_spell:block/eden_apple_tart` | 20 | **20/20** | **19** | 0 / 0 / 0 | 声明 32×32 | y 0..4.8 |
| `kaleidoscope_cookery:block/numbing_spicy_chicken_0` | 26 | **26/26** | **26** | 0 / 0 / 0 | 声明 32×32 | y 0..6.5 |

**五条可直接照搬的规矩**：
1. **大量"小的、带旋转的方块"** —— 食物块基本都是 `2×3×5` 这种小片，绕 y 转 **22.5°**（摆盘时的自然倾斜），
   这才是"看起来像食物而不是积木"的关键；本项目暗影刀那套"±22.5° 旋转块"是同一手法，但食物要用得更凶。
2. **100% 逐面 UV**，不用 box-UV，也**不写** `shade`/`tintindex`/`inflate`（食物模型刻意保持简单）。
3. **贴图很小**：声明 `texture_size` 32×32（实际 PNG 常见 16×16），**调色板 ~20 色、一半像素不透明**——
   低分辨率 + 少颜色反而更贴合 MC 像素风（和我们武器用的 256×256 是两种取向，按用途选）。
4. 食物**高度只有 5~6.5 格**（贴着盘子/碗），x/z 铺开到 8~12 格（盘子的footprint）。
5. 要同时当物品用的食物才写 `display`（7 个槽位都写全）。

**可应用到本工程**：我们那批**平面食物/消耗品图标**（禁果、苹果核、糖霜水晶、奶酪诱惑、酒引、茶树种子、
炫彩肥料、屠夫之心、故障核心碎片、苹果爵士的果梗…）都可以按这套做成 10~26 cube 的 3D 小模型
（`gen_weapons3d.cjs` 的配方表结构可以直接复用，只是加一套"食物角色画法" + 32×32 贴图 + 旋转摆盘）。


## 4. 本工程自己的参照（一直在用）

- `tools/model3d/gen_weapons.cjs`：铺基础色 → 三段色阶明暗（shadeRibbon/Sphere/Ellipse/HBody）→ 叠材质细节 → 1px 外描边。
- `black_cleaver` / `candy_cane` / `blooming_thorns`：**整件画成一张 2D 侧视图** + 大量旋转块
  （注意：`black_cleaver` 的 UV 写在 0..32 像素空间，**超出 Java 的 0..16**，是个历史 bug）。
- `puffer_bat` / `wenxuanlan_flywheel`：部件"六面同一矩形"。
- **公共库** `tools/model3d/lib_itemtex.cjs`：画布（全 clamp）/ 侧盖面装箱器 / 1px 描边 / **四项自检**
  （暖色杂点、正反镜像、浮点噪声、面比例）。范例：`tools/model3d/regen_shadow_sword.cjs`（暗影刀 v13）。
