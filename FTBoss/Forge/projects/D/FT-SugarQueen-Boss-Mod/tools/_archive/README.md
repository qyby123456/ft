# tools/_archive — 历史一次性脚本与旧输出

2026-09-12 整理工程时，把 `tools/model3d/` 里**只服务过一次改动**的脚本挪到了这里；
活跃工具仍在 `tools/model3d/`（生成器 `gen_*`、校验器、渲染/预览器、`png.cjs`）。

## 内容

| 目录 | 内容 |
|---|---|
| `model3d/` | 78 个历史脚本：`fix_*`（一次性修模型/贴图）、`wire_*`（把机制接进 Java 的一次性改写）、`check_*`/`*_debug`（临时诊断）、旧版 `preview.cjs`、旧生成器版本（`gen_compost*.cjs` v1~v3、`gen_statues.cjs`）等 |
| `out/` | 旧的生成器输出目录（`out_apple_boss`、`out_statues*`、`out_fly_minion`、`out_green_apple`、`out_compost_*`、`out_poop_minion`），以及 `*_tools` 那份来自 `tools/model3d` 的同名输出 |

## 使用注意

- `model3d/png.cjs`、`defect_core_anims.cjs`、`defect_core_paint.cjs` 是兼容入口，原来的本地 `require` 路径仍可用；实际实现共用 `tools/model3d/` 的权威版本，不再维护两份重复代码。
- 这些兼容入口依赖完整工程目录，不要只复制 `tools/_archive/` 后独立运行。原始三份实现保存在 `D:\ft工程文件\工程合并_20261022` 的共享历史归档中。
- 这里的东西**不代表当前实现**：只作历史参考，不要照着它们去"修"现在的资源。
  要改资源，一律改 `tools/model3d/` 里对应的 `gen_*.cjs` 然后重跑。
