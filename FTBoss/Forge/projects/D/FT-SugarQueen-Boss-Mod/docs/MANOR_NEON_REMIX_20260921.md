# 庄园·霓虹星狱 / Neon Star Prison

撤下状态：用户已确认放弃本轮音乐，V1/V2均不再进入游戏，恢复原两首庄园战斗曲。以下均为历史记录，试听与源码仅作备份。

本文记录V1历史版本。用户提供三首参考并明确不要人声后，已制作V2纯器乐版；当前参数和验收见 `MANOR_REFERENCE_REMIX_20260921.md`。V1源码与音频保留对照，不再作为当前游戏内版本。

原创电子战斗曲，以 EDM/电子混音的编排方式为参考，不使用商业歌曲录音、外部采样或现成旋律。

## 编曲

- 140 BPM，D 小调，4/4；144 小节，尾音 3 秒，实际 249.857 秒。
- 0:00 水晶引子；0:14 鼓组/低音进入；0:41 第一段蓄势。
- 0:55 第一高潮，宽立体声合成器主旋律；1:50 半拍水晶间奏。
- 2:17 二次蓄势；2:31 低八度与反向旋律变奏；3:26 完整主题收束；3:53 渐弱结束。
- 底鼓触发低音侧链，拍间低音与合成器分开留位；高潮前加入短暂静音停顿，不用长静音凑时长。
- 程序合成乐谱与音色由 `tools/music/render_manor_neon_remix.py` 保存，随机种子固定为 20260921。

## 接线

新增 `ftboss:boss_music_manor_neon_remix`，流式播放 `music/manor_neon_remix.ogg`，中英文字幕已提供。
作为庄园原两首战斗曲之后的第三首，总音乐约 606.53 秒。没有改变单通道播放器、网络包上限或开场歌曲。
新增三曲顺播/自然结束测试，更新真实服务器播放列表和三曲网络包往返测试。

## 母带与格式

原始合成输出 44.1 kHz 双声道 PCM。ffmpeg 母带滤波：

```
highpass=f=28,equalizer=f=280:t=q:w=0.8:g=-1.5,loudnorm=I=-15:TP=-2:LRA=9:measured_I=-17.23:measured_TP=-1.68:measured_LRA=10.70:measured_thresh=-27.66:offset=-0.16:linear=false
```

输出 24-bit WAV；Ogg Vorbis 使用 `-q:a 6`，保持 44100 Hz 双声道。
成品 Ogg 解码：11018700 samples/channel、249.857 秒、峰值 0.8300、RMS 0.1670、无解码错误。
ffmpeg 过采样测得 Ogg 真峰 -1.62 dBTP，综合响度 -15.07 LUFS，LRA 6.1 LU；未削波。
试听 MP3 从游戏 Ogg 编码，320 kbps、`volume=0dB`，非从归一化 WAV 再增益；解码最大电平 -1.6 dBFS。

## 验证与导出

`checkManorNeonMusic` 检查 Ogg 完整分页、双声道、采样率、实际时长、事件注册、流式资源及播放顺序。
完整 `build` 包含三曲顺播和不叠加回归测试；真实服务器测试入口为 `tools/verification/manor_music_gametest.gradle`。
桌面导出分为直接试听 MP3、本次新增音频及源码、指定 jar 内全部原始音频、原两曲对照四部分。
部署状态由本次 `_work/manor-neon-remix-20260921/DELIVERY.md` 和桌面 `说明.txt` 记录，不将未部署构建误写为已安装。
