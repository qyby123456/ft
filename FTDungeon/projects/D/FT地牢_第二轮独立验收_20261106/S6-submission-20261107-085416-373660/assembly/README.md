# S6-99

状态：自检完成，待独立复核；不是生存通关验收。

NBT：s6-volcanic-connected.nbt；尺寸 207 × 36 × 180；SHA256 5b72d94c89871ebe9c262f92d6c00275ee0c8cfbf32343be96c9cd8e538f1336。

## 真实接口
- west: 脚点 2,4,36，净宽3，净高4

## 视觉核对
已实际查看新合并逐房剖切总览：两翼由东廊连接，19峡谷位于下翼北侧且无模块重叠；207×36×180，20片反拼252421记录和45份BE一致，47553屏障格，走廊3×4全截面空气采样通过。六门双侧原生交互和当前哈希11份隔离世界400刻报告另见qa/runtime/result.json；外部步行接入未认证。

## 与概念图差异
- Two purpose-built wings linked by an east gallery; module local heights preserved.
- Entry vestibule stages inside the barrier envelope; no deployment or exterior world access certified.
- Only the unused final outer portal is glass sealed; all internal interfaces remain open.

## 复现
使用本组lib.mjs与recipe.mjs，技能scripts/run-build.mjs，参数 --data-version 3465。随后运行本组audit.mjs、native.ps1、render.cjs。所有QA哈希绑定当前NBT；原工具报告文件名含independent仅为兼容，不代表独立验收。
