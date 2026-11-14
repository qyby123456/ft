import com.jcraft.jorbis.VorbisFile;
import com.jcraft.jorbis.Info;

import java.io.File;

/**
 * 用 Minecraft 自带的 JOrbis(和 net.minecraft.client.sounds.OggAudioStream 同一套库)
 * 真正解析 ogg, 打印声道/采样率/总 PCM/总时长。
 *
 * <p>用途: 判定"自写 ogg 到底是文件坏了, 还是游戏里没播到"。
 * 用法: java -cp "<codecjorbis.jar>;." OggInfo <ogg...>
 */
public final class OggInfo {

    public static void main(String[] args) {
        for (String a : args) {
            File f = new File(a);
            System.out.printf("%-24s %9d B  ", f.getName(), f.length());
            try {
                VorbisFile vf = new VorbisFile(f.getAbsolutePath());
                int streams = vf.streams();
                Info info = vf.getInfo(0);
                long pcm = vf.pcm_total(0);
                float secs = vf.time_total(0);
                System.out.printf("streams=%d ch=%d rate=%d ver=%d pcmTotal=%d dur=%.1fs seekable=%s%n",
                        streams, info.channels, info.rate, info.version, pcm, secs, vf.seekable());
            } catch (Throwable t) {
                System.out.printf("*** PARSE FAILED *** %s: %s%n", t.getClass().getName(), t.getMessage());
            }
        }
    }
}
