# Boss 战组曲 · 音乐规格（24 分钟默认版 / 三段式 / 动态分层）

> 2026-09-13 v99 起生效。用户给出的规格 + **在本工程里必须落实的修正**。
> 本文是"唯一事实来源"：改音乐先改这里。

---

## 0. 三条必须先说清的修正

### ① `.ogg` 由我直接生成，不需要 DAW
本工程已有**纯 Node 的合成 + 编码链**，不依赖 ffmpeg（机器上没有），也不需要你手工导出：

| 文件 | 作用 |
|---|---|
| `tools/audio_tools/synth.cjs` | 合成工具箱：弦乐/铜管/大提琴/合唱/低音/脉冲/持续音/定音鼓/底鼓/小鼓/嗵鼓/吊镲/上升镲/铜锣/冲击/钟/拨弦 + Schroeder 混响 + 立体声延迟 + `encodeOgg()` |
| `tools/audio_tools/compose_segment01.cjs` | 第一段的乐谱与小节级渲染 → 4 条 Stem |
| `tools/audio_tools/compose_epic.cjs` | v82 那首 20 分钟《甜苹果庄园·史诗战斗曲》（保留） |
| `tools/audio_tools/OggInfo.java` | 用**游戏自带 JOrbis** 验"这个 ogg 到底能不能播"（`dur=` 对得上就 OK） |

编码参数：**Ogg Vorbis / 44100 Hz / 立体声 / `vbrQuality 0.5`（≈ 规格里的 Q5）**。

### ② 资源路径的 modid 是 `ftboss`，不是 `elevenbosses`
规格里写的是 `assets/elevenbosses/…`，但**本工程 modid 就是 `ftboss`**（改 modid 会牵动全工程，不做）。
所以实际落地为：

```
src/main/resources/assets/ftboss/sounds/music/segment_01_full_mix.ogg   ← 随包发布（就是那张"第三段"唱片）
src/main/resources/assets/ftboss/sounds/music/segment_01_base.ogg       ┐
src/main/resources/assets/ftboss/sounds/music/segment_01_pulse.ogg      │ 分层 Stem：
src/main/resources/assets/ftboss/sounds/music/segment_01_percussion.ogg │ 2026-09-13 起**不随包发布**
src/main/resources/assets/ftboss/sounds/music/segment_01_bass.ogg       ┘
```
音效同理：`assets/ftboss/sounds/effects/apple_boss_attack.ogg` 等。

> ⚠️ **2026-09-13 变更（用户要求瘦身）**：§3 那套动态分层**还没接线** —— `FTSounds.java` 里没有
> `boss_music_seg01_*` 事件，`sounds.json` 里也没有对应条目，所以那 12 条 Stem 和
> `transition_01/02` 谁都用不到，已从发布包移出（jar 76 MB → 约 38 MB）。
>
> **要恢复**：一条命令重新生成即可，归档副本另在 `_work/11boss-music/stems-archive/`：
> ```
> node tools/audio_tools/compose_segments.cjs --seg 1     # 或 --seg 2 / --seg 3
> ```
> **要正式启用分层**：先在 `FTSounds.java` 注册 `boss_music_segNN_*` 事件、补好 `sounds.json`，
> 再把 4 条 Stem 放回 `sounds/music/`，然后按 §3 的等长/同起点约定渲染。

**事件名**沿用工程既有的 `boss_music_*` 前缀（客户端按这个前缀识别 BGM 并压掉原版音乐）：
`boss_music_seg01_base` / `_pulse` / `_percussion` / `_bass`（**不是** `music.segment_01_base`）。

### ③ "8 小节 = 4 秒"是笔误（你已在原文里自己纠正）
120 BPM / 4/4 → **1 小节 = 2 秒**，8 小节 = **16 秒**。
所以 `transition_01.ogg`(0:04) 只是**播放器侧的淡入淡出时长**，不是音乐单位 —— 本文按"4 秒交叉淡化"处理。

---

## 1. 总规格（已定）

```
总长 24:00 · 3 段 × 8:00 · 120 BPM · 4/4 · D 小调 · 交响金属 + 黑暗奇幻
1 小节 = 2 秒 · 每段 240 小节 = 480 秒
交叉淡化 4 秒（播放器侧）
```

```
intro.ogg        0:30
segment_01.ogg   8:00      Boss 1～3
transition_01.ogg 0:04
segment_02.ogg   8:00      Boss 4～7
transition_02.ogg 0:04
segment_03.ogg   8:00      Boss 8～11
outro.ogg        1:00
```

> 说明：本文档里的 `segment_XX.ogg` 在工程里是**一组 Stem**（见 §3），不是单文件。

---

## 2. 分段与 Boss 归属

| 段 | 时间 | Boss | 情绪走向 |
|---|---|---|---|
| segment_01 | 0:00–8:00 | 1～3 | 前段压抑 → 中段增强 → 结尾留悬念 |
| segment_02 | 8:00–16:00 | 4～7 | 推进/变奏，加弦乐层 |
| segment_03 | 16:00–24:00 | 8～11 | 终局，合唱 + 最终层 |

### segment_01 分区（240 小节）

| 时间 | 小节 | 段落 | 音乐内容 |
|---|---|---|---|
| 0:00–1:20 | 0–39 | 黑暗序幕 | 只有 base：低音持续音 + 弦乐垫；32 小节起钟声预兆主题 |
| 1:20–2:40 | 40–79 | Boss1 腐化苹果 | + bass（半音符）→ + pulse（大提琴演奏主题 + 八分脉冲） |
| 2:40–4:20 | 80–129 | Boss2 赤焰南瓜 | + percussion（Kick 全拍/Snare 2,4/Tom 1,3）；主题转铜管 |
| 4:20–6:20 | 130–189 | Boss3 深渊胡萝卜 | 全奏 + 合唱垫 + 每 4 小节苹果动机 |
| 6:20–7:20 | 190–219 | 连战升级 | 和声换 `Dm→C→Bb→A`；八分驱动低音 + 双倍鼓点 |
| 7:20–8:00 | 220–239 | 段落收束 | 抽层；末小节 `Dm → A` 属和弦长音，留悬念接第二段 |

---

## 3. 动态分层（Stem）与**技术可行性（已核实）**

> **当前状态（2026-09-13）**：本节是**设计稿**，尚未接线。4 条 Stem 已移出发布包（见开头变更说明），
> 需要时用 `compose_segments.cjs --seg N` 重新生成。下面的技术结论（`TickableSoundInstance` 每 tick
> 回写音量、必须同 tick 起播、必须严格等长）依然有效，接线时照此实现即可。

7 层方案，按"击败 Boss 数"解锁：

| 击败数 | 开启层 |
|---:|---|
| 0 | base（环境） |
| 1 | pulse |
| 2 | percussion |
| 3 | bass |
| 4～5 | strings |
| 6～7 | choir |
| 8～10 | boss |
| 11 | finale |

**第一段只做 4 条**：`base / pulse / percussion / bass`（与上面规格一致）。

### 为什么"中途加层而不重启曲子"能做到
1. **`SoundEngine` 会每 tick 回写 `TickableSoundInstance` 的音量**
   （已用 `javap` 核实：`SoundEngine` 持有 `tickingSounds`，`tick()` 里对 `Channel.setVolume` 赋值）。
   → 只要我们的音频实例实现 `TickableSoundInstance` 并让 `getVolume()` 随时间变化，
   就能**平滑淡入/淡出某一层**，不需要重播。
2. **4 条 Stem 必须在同一 tick 一起起播**（否则位置不齐、会"打架"）。
   所以实现是：**开打时把 4 条全部起播、未解锁的音量为 0**，解锁时再淡入。
   由于只在开场建立一次同步，之后不会漂移。
3. 因此**所有 Stem 必须严格等长、同起点**：`480.000 s`，且第 0 帧对齐。

### 峰值/响度的工程约定（分轨混音的正确做法）
- 每条 Stem **线性**（不做限幅）→ 4 条相加 == 整轨；
- 先渲染一遍**整轨**测出整混峰值 `P`，再用**同一个系数** `0.85/P` 缩放出各 Stem；
- 于是"任意层子集"的峰值都 ≤ 0.85（≈ −1.4 dBFS），满足"峰值不高于 −1 dBTP" ✓。

> 响度：规格要 −14 LUFS。我没有 K 加权表，**用 RMS 近似**并在渲染日志里报出，
> 不打"已达标 LUFS"的包票。

---

## 4. 作曲素材（第一段，已实现）

**和声循环 8 小节**（每和弦 2 小节）：

```
| Dm | Dm | Bb | Bb | F | F | C | C |
```

**Boss 切换/连战升级**：`Dm → C → Bb → A`（A 为 D 小调属和弦，制造紧张）

**主题（8 小节，规格原文）**：

```
D4 F4 G4 A4 | A4 G4 F4 D4 | Bb3 D4 F4 G4 | F4 D4 C4 A3
D4 F4 G4 A4 | C5 A4 G4 F4 | E4 F4 G4 A4 | A4 G4 F4 D4
```
节奏：前 4 小节四分音符为主、后 4 小节加八分经过音；进入全奏段后主题翻高八度并加铜管。

**苹果 Boss 动机**：`D4 - A3 - D4 - F4 - E4 - D4`（Boss3 起每 4 小节一次，末音延长）

**打击乐（规格原文）**：
- 基础：`Kick 1,3` / `Snare 2,4`
- Boss 战：`Kick 全拍` / `Snare 2,4` / `Tom 1,3`
- 每 8 小节末过门：`第1拍底鼓 → 第2拍铜锣 → 第3拍反向镲 → 第4拍冲击`
- 全奏段另加：每小节吊镲、4 小节一次的定音鼓

---

## 5. 混音规则（技能 / 死亡）

```
NORMAL  音乐  0 dB
ABILITY 音乐 -6 dB      技能开始 0.15s 淡出, 技能结束 0.8s 恢复
DEATH   音乐 -9 dB      0.25s 淡出, 死亡音效结束后 1.5s 恢复, 持续 2s
```
**不改音频文件**，在客户端音乐控制器里调音量（同 §3 的 `TickableSoundInstance` 通路）。

Boss 死亡时：**不重启音乐** —— 继续当前播放位置 + 解锁新层 + Boss 死亡音效。

---

## 6. 进度

- [x] 总规格 / 分段 / 和声 / 主题 / 打击乐 / 混音规则 —— **已定**
- [x] 合成工具链 `synth.cjs` + 第一段乐谱 `compose_segment01.cjs`
- [x] **segment_01 四条 Stem 出片**（`480.0 s` 整、44100 Hz 立体声、JOrbis 全部可解码）
- [x] 试听整混版 `segment_01_full_mix.ogg`（**只给人听，不注册进游戏**）
- [ ] `sounds.json` + `FTSounds` 注册 4 个 `boss_music_seg01_*` 事件
- [ ] 客户端控制器：同 tick 起播 4 条 + 按"已击败数"淡入 + 技能/死亡闪避
- [ ] 服务端把"已击败数"同步给客户端（自定义包）
- [ ] segment_02 / segment_03（含 strings / choir / boss / finale 三层的素材）
- [ ] 技能音效 `apple_boss_attack / _seed / _dash / _death`
- [ ] intro / outro / transition 三个短文件

### segment_01 实测数据（2026-09-13 15:27）

| Stem | 文件 | 峰值 | RMS | 配平系数 |
|---|---|---:|---:|---:|
| base | 3.46 MB | −14.8 dBFS | −30.5 dB | 2.4× |
| pulse | 3.11 MB | −9.4 dBFS | −26.0 dB | 2.5× |
| percussion | 2.73 MB | −3.4 dBFS | −28.1 dB | 0.85× |
| bass | 3.05 MB | −16.6 dBFS | −27.6 dB | 1.7× |
| **整混(4 层)** | 12.35 MB | **0.85**(≈−1.4 dBFS) | −21.4 dB | 统一 0.633× |

要点：四层 RMS 只差 **4.5 dB**（pulse 领奏、bass 铺底、percussion 支撑、base 垫音），
鼓的**峰值**最高但 RMS 不抢戏 —— 这是瞬态的正常形态。
`STEM_TRIM` 就是调平衡的唯一旋钮（在 `compose_segment01.cjs` 顶部）。

---

## 7. 备选路线：MiniMax Music 3（2026-09-13 v100 试接）

用户要求"试着使用 minimax music3 音乐模型"。**接口已核对官方文档并实测可达**，管线脚本已写好，
**只差一把 API key**。脚本：`tools/audio_tools/gen_minimax_music.cjs`。

### 接口事实（来自官方文档，非记忆）
```
POST https://api.minimax.io/v1/music_generation
Header: Authorization: Bearer <API_KEY>
Body:   { model, prompt, lyrics?, is_instrumental, output_format:'hex'|'url',
          audio_setting:{ sample_rate:16000|24000|32000|44100, bitrate:…256000, format:'mp3'|'wav'|'pcm' } }
返回:   { base_resp:{status_code,status_msg}, data:{status,audio}, extra_info:{music_duration,…} }
模型:   music-3.0(推荐, 付费/Token Plan, RPM120) / music-2.6 / music-cover
        music-3.0-free / music-2.6-free / music-cover-free (RPM 3)
错误码: 1002 限流 / 1004 鉴权 / 1008 余额 / 1026 敏感 / 2013 参数 / 2049 key 无效
```
- 本机实测：`HTTP 200`、建连 65 ms，无 key 时返回 `1004`（= 文档说的"没带密钥"）→ **网络通**。
- 本机**没有** MiniMax key（环境变量为空，工程/游戏目录里也没有）。

### ⚠️ 文档里的服务调整通知（很重要）
> 自 **2026-08-20** 起：付费音乐 API **不再对新用户开放**（老付费用户可继续）；
> **免费档 `music-3.0-free` / `music-2.6-free` / `music-cover-free` 已下线**。
> 官方给的替代：MiniMax Audio 网页/App，或**开源的 MiniMax Music 3 权重**
> https://huggingface.co/MiniMaxAI/MiniMax-Music3

（当前游戏内时间是 2026-09-13，**已过这个分界点**，所以免费档多半用不了。）

### 两个必须知道的工程结论
1. **要 `wav`，不要 `mp3`**：Minecraft 只能播 Ogg Vorbis，而本机没有 ffmpeg 也没有 mp3 解码器。
   所以请求 `format='wav'`，然后**纯 Node** 解析 RIFF → float32 → `wasm-media-encoders` 编 OGG。
   这条通路**已用 `--dry-run` 跑通并验过**：16bit/44.1k/立体声 WAV → OGG `dur=20.0s`、JOrbis 可解码 ✓。
2. **模型给不出 Stem**：接口返回的是**成品立体声混音**，没有分轨。
   所以 §3 的"7 层 / 按击败 Boss 数加层"**用这条路实现不了** ——
   只能退化成"生成 N 段不同强度的整轨再交叉淡化"（且每段独立生成，**播放位置无法对齐**）。
   要保住动态分层，就得走本工程的**自写合成器**（`synth.cjs` 天生分轨）。

### 一旦拿到 key 就能一条命令出片
```powershell
$env:MINIMAX_API_KEY="sk-..."
node tools/audio_tools/gen_minimax_music.cjs --model music-3.0 `
     --prompt "Dark fantasy symphonic metal boss battle, 120 BPM, D minor, ..." `
     --out segment_01_mm.ogg
# 带人声: --song --lyrics-file lyrics.txt
```
脚本会自动：hex/url 取音频 → 解析 WAV → 需要时重采样到 44100 → 编 OGG(vbrQuality 0.5)
→ 落到 `assets/ftboss/sounds/music/` → 打印时长/峰值/RMS。

### 路线对比
| 路线 | 分轨 | 连续性 | 需要什么 |
|---|---|---|---|
| **自写合成器**（当前） | ✅ 真 Stem | ✅ 8 分钟一整块 | 什么都不需要，已在跑 |
| MiniMax API | ❌ 成品混音 | ⚠️ 需拼接多段 | **一把老付费 key**（新用户已关） |
| 开源 MiniMax-Music3 | ❌ | ⚠️ | 本地 GPU + 数 GB 权重（本机无 GPU，HF 下载多半也超时） |
