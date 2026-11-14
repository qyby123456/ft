# 茶树Boss 战斗BGM — 生成工具

## 2026-09-21：已撤下《霓虹星狱》

用户明确放弃本轮音乐并确认撤下。当前庄园战斗恢复《鏖战》208.67 秒 →《双轨·战斗高潮》148 秒，合计约5分57秒。
《霓虹星狱》V1/V2均不再进入游戏：已移除事件注册、资源映射、字幕及运行时Ogg。
仍使用单通道状态播放器，每场每首只播放一次，不循环、不叠加，不改开场演出曲。

创作源码、桌面试听文件及V1/V2对照保留备份，不删除。`render_manor_neon_remix.py`、`render_manor_neon_remix_v2.py` 和 `export_manor_neon_remix.py` 仅供历史版本复现。
历史导出器要求含该曲的旧jar，不能对撤下后的当前jar运行。历史文档见 `docs/MANOR_NEON_REMIX_20260921.md` 与 `docs/MANOR_REFERENCE_REMIX_20260921.md`。

## 曲目总览（每场战斗只播一首，由开战 Boss 决定；调度器 `sound/BossMusic.java`）

**2026-09-10 起：五个 Boss 各有专属单曲**，三幕剧退为"未登记 Boss"的兜底。

| 开战 Boss | 曲目 | 文件 | 来源 | 时长 |
|---|---|---|---|---|
| **沙耶**(茶树) | 《茶の唄》 | `music/saya.ogg` | **自写** `render_saya.cjs` (v6) | 131.7s |
| **糖霜女皇**(姜饼王) | 《木偶剧场·加洛普》 | `music/gingerbread.ogg` | **自写** `render_puppet.cjs` (v5) | 115.4s |
| **屠夫** | 《肉山祭坛》 | `music/butcher.ogg` | **自写** `render_butcher.cjs` (v1) | 145.3s |
| **酒桶王** | 《醉乡酒馆》 | `music/tavern.ogg` | **自写** `render_tavern.cjs` | 见下 |
| **鼠群** | 《鼠潮夜行》 | `music/rat.ogg` | **自写** `render_rat.cjs` | 见下 |
| 未登记的 Boss(兜底) | 三幕剧 | — | 导入自 `yuletide_dread-1.0.0-r2.jar`（作者 AutoMods） | — |
| ↳ 序幕(>66%血) | | `music/theatre_overture.ogg` | 同上 | 51.4s |
| ↳ 假面圆舞(>33%血) | | `music/theatre_masquerade.ogg` | 同上 | 51.4s |
| ↳ 断线狂宴 | | `music/theatre_finale.ogg` | 同上 | 51.4s |
| ↳ 落幕钟声(真死) | | `music/theatre_curtain.ogg` | 同上 | 8.0s |

> 五位 Boss 都是**专属单曲**：整场一首放到底、不切幕（`Group.THEATRE` 只给未登记实体兜底）。
> 想改某位 Boss 用哪首曲子：改 `BossMusic.groupOf()` 的分支；
> 新增一首曲子的接线有**五处**，别漏：`sound/FTSounds`、`sounds.json`、`BossMusic` 的
> `Group`/`groupOf`/`trackEvent`/`allTrackEvents`/`describe`、中英 `lang` 字幕键。
> 三首导入素材的原件归档在 `yuletide_dread_ost/`。

---

## A. 沙耶专属《茶の唄》(v6, `render_saya.cjs`)

参考取向：《沙耶の唄》(Saya no Uta) 主题曲的**体裁语汇** —— 缓慢、哀伤、空灵的女声叙事曲。
**参考曲是商业版权作品，这里不复制其旋律**，旋律/和声/配器全部另写。

- **b 小调**、**6/8 摇篮摆动**、四分段速度 126→132→168→120→132→108
- 92 小节 / **131.65 秒** / 2.19 MB / RMS 0.0999 / 峰值 0.607 / 峰均比 15.5 dB（无削波）
- 编制：**女声独唱**（锯齿波过三共振峰，元音可切）· **音乐盒**（非谐分音+齿音瞬态）·
  暗色弦乐铺底 · 柔音钢琴 · **心跳**（不是战鼓）· 低音持续音 drone · 高音钟 · 不谐和音簇 · 锣
- 曲式：摇篮 → 唄 I → **崩坏**（提速到 168、不谐和音簇、走调音乐盒、碎裂的女声）→ **静**（只剩音乐盒与心跳）
  → 唄 II（主题B 高八度高潮）→ 终（摇篮最后一次，渐远）
- 和声用 ♭II(C 那不勒斯) 与 F#7 的 A# 制造压迫；调性 b 小调为全项目首次使用

### 母带要点（踩过的坑）
本曲是"密集持续型"编曲，峰均比天然低于打击乐驱动的曲子：
沿用其它曲目的归一化目标 `1.5/peak` 会把 **1.6% 的持续样本推进 tanh 饱和区**（是 v5 的十几倍），
把女声压扁。已改为 `0.75/peak`，让饱和只当兜底安全阀 —— 饱和样本从 93394 降到 **34**，峰均比 11.9→15.5 dB。
改这一行：`const norm = peak > 0 ? 0.75 / peak : 1;`

### 重新生成
```powershell
cd tools/music
npm install vorbis-encoder-js @wasm-audio-decoders/ogg-vorbis   # 首次（缓存被拒时加 --cache <可写目录>）
node render_saya.cjs saya.wav out            # 合成 → out/saya.wav
node wav2ogg.cjs out/saya.wav out/saya.ogg 0.48 1.0
node verify-ogg.cjs out/saya.ogg
node analyze.cjs out/saya.wav                # 分时段频段占比
```

### 想改什么，改哪里
| 想要的效果 | 改这里 |
|---|---|
| 段落/速度/拍号 | `sec('A', 8, 3, 126)` … `sec('F', 8, 3, 108)`（`beats`=四分音符数；6/8 写 3） |
| 女声旋律 | `SONG_A`（主题A）/ `SONG_B`（主题B），格式 `[拍, midi, 时长拍]` |
| 音乐盒摇篮动机 | `CRADLE`；伴奏琶音在 `mbArp()` |
| 和声 | `CH` 和弦表 + `LOOP` 8 小节循环 |
| 女声音色 | `voice(..., {vowel:'a'\|'o'\|'u'\|'e'\|'i', vibHz, vibCents, glide})`；`VOWEL` 表可改共振峰 |
| 心跳密度 | `pulse(bar, amp, extra)` |
| drone 电平 | 各段 `drone(..., note, amp)`；太低会失去压迫感，太高会糊掉 40-80Hz |
| 响度 | `TARGET_RMS`（现 0.100） |

## B. 姜饼王《木偶剧场·加洛普》(v5, `render_puppet.cjs`)

参考取向：Black Souls II OST - *Theatre of Darkness, the Grand Guignol*（暗黑大剧院／恐怖木偶剧）。

- **e 小调/弗里吉亚**、以三全音 **B♭-E** 为核心、**2/4 加洛普** + **7/8 木偶跛行**
- 168 BPM 定速，末尾加速到 208；半音下行帕萨卡利亚 → 终曲半音上行
- 144 小节 / **115.38 秒** / 2.03 MB / RMS 0.104 / 峰值 0.897 / 峰均比 18.7 dB
- 音色：手摇风琴(calliope，带走音) · 骨木琴 · 鞭响 · 发条棘轮 · 手风琴 · 低音大号 · 三角铁 · 观众嘈杂 · 共振峰尖叫
- 戏剧点：73.7s 全体急停（重击 RMS 0.35 → 74.4s 骤降到 0.06，落差 16dB）

与其余版本的差异：节拍 2/4+7/8（v2/v4=4/4，v3=3/4）；调性 e（v2=a，v3/v4=d）；速度 168 定速+加速。

### 重新生成
```powershell
node render_puppet.cjs puppet.wav out
node wav2ogg.cjs out/puppet.wav out/gingerbread.ogg 0.48 1.0
node verify-ogg.cjs out/gingerbread.ogg
```
低频配平要点：`oomPah()` 低音大号只留一拍 + 高八度短促推进；`galopDrums()` 定音鼓压到根音(≈41Hz)
而非上方八度(82Hz —— 那正是与大号二三次谐波打架的糊区)；`{sparkle:true}` 的三角铁层补 3~6kHz。

---

## C. 屠夫《肉山祭坛》(v1, `render_butcher.cjs`)

屠宰场 · 肉山：工业恐怖，沉重、压迫、打击驱动。

- **d 小调**、4/4 为主（第 21 小节插 1 小节 3/4 造成"跛行"）、速度 **100 → 112 → 118 →(3/4 处 94)→ 112 → 140 → 158 → 90**
- 75 小节 / **145.34 秒** / 2.52 MB / RMS **0.102** / 峰值 **0.632** / 饱和样本 **0**
- 音色（7 个新写）：剁骨刀（高频刃口瞬态+非谐金属分音+砧板闷体）· 铁砧（10 个非谐分音，跟和声根音）·
  砧板式重击 kick · 纯钝击 meatHit · 链条敲击簇 · 磨刀石（3400Hz 带通+转速起伏）· 滴血点状高音 ·
  低音铜管 brassLow（锯齿过 320/742/1180 三共振峰）· 低音提琴拨弦 · **低八度共振峰低吼 growl**（把女声 `voice()` 降八度+次谐波+27Hz AM 粗糙度）
- 曲式：逼近 → 挥砍动机（切分重音）→ **肉山怒吼**（全奏半音下行）→ **喘息**（只剩滴血与远处摩擦，RMS 0.012，落差约 21 dB）
  → 狂奔起步(140) → 终局三重冲击(158) → 收尾一击
- 和声：挥砍段 Dm Dm B♭ B♭ Gm Gm A7 DmT；怒吼/终局 Dm E♭m Dm A♭ Dm B♭ E♭ DmT（DmT = 减五度 A♭ + A♭/A 小二度音簇）
- 母带要点：打击驱动型峰值天然高，混响后加了一级**线性软拐点限幅器**（0.70 以下增益恒 1.0、零失真；硬上限 0.88），
  再 `0.64 / peak` 归一化 → `satSamples=0`，动态全部留给最后 `TARGET_RMS=0.102` 的 RMS 归一化

### 重新生成
```powershell
node render_butcher.cjs butcher.wav out
node wav2ogg.cjs out/butcher.wav out/butcher.ogg 0.48 1.0
node verify-ogg.cjs out/butcher.ogg
```

---

---

## D. 酒桶王《醉乡酒馆》(v1, `render_tavern.cjs`)

酒馆风：热闹的舞曲，但越喝越醉（走音、速度微飘、三全音潜行）。**用户指定的风格**。

- **G 混合利底亚**（♭VII=F 主打：G-C-G-D7 / Em-C-F-D7）→ 醉意段转同主音 **g 小调 + ♭VI=E♭**；真 oom-pah 律动
- **6/8 吉格**，♩=120 → 132 → 138 → **112→88（逐小节 ±5.5% 微飘）** → 148→156 → 144
- 86 小节 / **123.92 秒** / 1.90 MB / RMS **0.1000** / 峰值 **0.900** / 饱和样本 9（与《茶の唄》同款 `0.75/peak` 保守归一化）
- 音色：**手风琴**（双簧 ±9~13 音分 musette 失谐 + 低音簧 + 起音簧片噪声 + 风箱颤音 + droop 下滑，带全局"醉意"音高游移）·
  口琴/口哨（强奇次波表 + 吹气带通噪声）· Karplus-Strong 拨弦（曼陀林扫弦 / 轮指）· 低音提琴 pizz（1/2/3/4.02 非谐分音 + 手指摩擦噪声）·
  木桶（空心木腔模态+70Hz 闷响，招牌音色）/ 手鼓 / 铃鼓 / 跺脚 / 三层拍手 / 碰杯 / 碎杯（26 个随机分音）· 人群嘈杂与笑声（带通噪声 + 共振峰人声）
- 曲式：开门迎客 → 第一轮舞曲 → 合唱式副歌 → **醉意间奏**（只剩曼陀林轮指 + 踉跄低音，±20 音分走音、速度微飘；
  第 47/48 小节浮出 G+C♯ 三全音低音潜行，再被一记木桶+跺脚+手风琴刺"敲醒"）→ 更快的终段（每 8 小节闪现一次三全音）→ 碰杯 → 碎杯 → 欢笑与尾混响
- 强弱对比：醉意段 5 秒窗 RMS 0.042–0.053 vs 终段 0.093–0.129（≈9 dB）
- 调试记录（值得记）：oom-pah 和弦原本比旋律响约 **6 倍**（已降到同级并把旋律统一 ×1.22）；
  2.6k/5.2k/9.2k 的高频 peaking 会把"全奏落点"叠成孤立尖峰，曾把峰均比拖到 22 dB、导致 RMS 归一化被兜底削到 0.078 —— 收窄高频提升后 finalRms 正好 0.1000，无需兜底。

### 重新生成
```powershell
node render_tavern.cjs tavern.wav out
node wav2ogg.cjs out/tavern.wav out/tavern.ogg 0.48 1.0
node verify-ogg.cjs out/tavern.ogg
```

---

## E. 鼠群《鼠潮夜行》(v1, `render_rat.cjs`)

偷偷摸摸 / 擦擦索索 / 数量众多 / 突然爆发；本曲是**打击与噪声驱动**。

- **a 小调 + 半音阶色彩**；和声循环 Am F Dm E / Am C B♭ **C♯** / Dm E F E（C♯ 提供那不勒斯式压迫）
- **6/8 碎步**（不等距重音错位，像很多小脚不齐），♩=144 → 152 → 160 →（150 静默）→ **168→184 加速** → 138
- 90 小节 / **111.81 秒** / 1.92 MB / RMS **0.0995** / 峰值 **0.936**
- 音色：**拨奏碎步 pizzicato**（极短促、颗粒分明 = 小爪子）· 玩具钢琴 · 口哨 · 沙锤/摩擦噪声 ·
  高频"吱吱"上滑点缀 · 定音鼓与滚奏 · 镲 · 锣 · 闷击 thud · **心跳低音** · 军鼓滚奏 · 弦乐震音 drone · 音簇推挤 clusterPush
- 曲式：索索试探（稀疏到几乎没有节奏）→ 小跑 → **尖啸**（高频群奏）→ **全体重击 → 静默**（只剩远处吱吱与一声心跳低音）→
  **鼠潮**（全奏急速、半音上行）→ 消散（远去渐弱）
- 与其它四首刻意错开语汇：不用音乐盒摇篮曲（沙耶）、不用加洛普（姜饼王）、不用酒馆舞曲（酒桶王）
- **母带踩坑（值得记）**：本曲峰均比约 19dB（干颗粒型）。
  ① 沿用《茶の唄》的直接 trim 会把峰值冲到 1.66–1.84 靠 clamp 削波；
  ② 一度把 `capPeak` 写成 `min(0.75, TARGET*1.22*ratio)`，ratio≈0.06 时算出 **0.0074** → 整曲先衰减 130 倍再被 trim 抬回来，峰值反爆 1.71；
  ③ 真正管用的是**分段落自动配平**（逐段按写谱意图定目标 RMS，实测段增益 P:1.21 Q:0.94 R:0.72 H:0.63 S:0.49 T:0.46 U:0.76）
     ＋末尾 3ms 前瞻 / 70ms 释放的 peak tamer（阈值 0.88）—— 把 12dB 的动态留在音乐里，而不是整曲抬 6dB；
  ④ 定音鼓原先按"记谱再低八度"发 37Hz，实测**一条 37.7Hz 正弦独占全曲 30% 能量、0–40Hz 占 73%**，
     听不清还抢走全部响度余量 —— 改为直接发 82–110Hz(E2–A2)，tremDrone 整体上移八度，并加 44Hz 高通。

### 重新生成
```powershell
node render_rat.cjs rat.wav out
node wav2ogg.cjs out/rat.wav out/rat.ogg 0.48 1.0
node verify-ogg.cjs out/rat.ogg
```

## 历史版本（脚本都还在）
`render.cjs` 暗色氛围版 167s → `render_epic.cjs` 激昂版 130s → `render_guignol.cjs` 黑暗剧院版 113s
→ `render_requiem.cjs` 安魂弥撒版 154s → `render_puppet.cjs` 木偶剧场加洛普 115s → **`render_saya.cjs` 茶の唄 132s**

## 生效开关与排错
- 音乐总开关：`FTSounds.MUSIC_ENABLED`
- 播/停/切幕：服务端 `sound/BossMusic`（按开战 Boss 定曲目组）
- 战斗期间压掉原版背景音乐 + 客户端无 Boss 时兜底停乐：`client/BossMusicClient`
- **换过音乐文件后若游戏里听不见**：关闭游戏 → 跑实例目录的 `FTBoss-清理资源缓存.bat`（清 `lightspeed-cache/`）→ 再启动。
  日志里 `File ftboss:sounds/music/xxx.ogg does not exist` 就是 Lightspeed 旧索引，不是文件真的缺失。
