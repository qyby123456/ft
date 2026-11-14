/*
 * GenPhantomSkin.java —— 生成折耳根「紫色残影」的贴图（2026-09-24）。
 *
 * 做法：读本体皮肤（变体 7 折耳根，64×64 玩家皮），按**亮度**映射成紫色单色 ramp，
 * 保留原图 alpha（透明区域仍然透明）→ 得到一张"同轮廓、纯紫色、带明暗"的残影图。
 *
 * ★ 2026-09-24 第二次改动：**把透明度直接烘进贴图**（{@link #ALPHA}），
 *   因为渲染器里 `RenderSystem.setShaderColor` 是全局状态、而且实体是在批次 flush 时才真正画 ——
 *   在那之前复位就白设了，透明度不可靠。烘进贴图后配合 `RenderType.entityTranslucent`
 *   一定生效，也不再依赖渲染时的全局状态。
 *
 * 用法（JDK 17 单文件模式）：
 *   java tools/model3d/GenPhantomSkin.java <源皮肤png> <输出png>
 * 默认（不带参数）：
 *   src/main/resources/assets/ftboss/textures/entity/easter/zheergeng.png
 *   src/main/resources/assets/ftboss/textures/entity/easter/phantom_zheergen.png
 *
 * 改色/改透明只改下面几个常量；改皮肤就重跑一次。
 */
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public final class GenPhantomSkin {

    /* 紫色 ramp：暗端 -> 亮端（残影的"紫色"就是这个色域）。比第一版提亮一档，暗场地里更看得清。 */
    private static final int DARK_R = 76, DARK_G = 26, DARK_B = 132;
    private static final int LIGHT_R = 226, LIGHT_G = 170, LIGHT_B = 255;
    /** 残影透明度（0~1）：贴图里直接写进 alpha 通道（渲染器不再动 shader color）。 */
    private static final double ALPHA = 0.68;

    public static void main(String[] args) throws Exception {
        File src = new File(args.length > 0 ? args[0]
                : "src/main/resources/assets/ftboss/textures/entity/easter/zheergeng.png");
        File dst = new File(args.length > 1 ? args[1]
                : "src/main/resources/assets/ftboss/textures/entity/easter/phantom_zheergen.png");
        if (!src.isFile()) throw new IllegalArgumentException("源皮肤不存在: " + src.getPath());

        BufferedImage in = ImageIO.read(src);
        if (in == null) throw new IllegalArgumentException("读不出 PNG: " + src.getPath());
        BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);

        int bakedAlpha = (int) Math.round(Math.max(0.0, Math.min(1.0, ALPHA)) * 255.0);
        int painted = 0;
        for (int y = 0; y < in.getHeight(); y++) {
            for (int x = 0; x < in.getWidth(); x++) {
                int argb = in.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {                 // 原图透明处保持透明
                    out.setRGB(x, y, 0);
                    continue;
                }
                int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
                double lum = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0;
                int nr = (int) Math.round(DARK_R + (LIGHT_R - DARK_R) * lum);
                int ng = (int) Math.round(DARK_G + (LIGHT_G - DARK_G) * lum);
                int nb = (int) Math.round(DARK_B + (LIGHT_B - DARK_B) * lum);
                out.setRGB(x, y, (bakedAlpha << 24) | (nr << 16) | (ng << 8) | nb);
                painted++;
            }
        }

        File parent = dst.getParentFile();
        if (parent != null) parent.mkdirs();
        ImageIO.write(out, "png", dst);
        System.out.printf("phantom skin: %s -> %s (%dx%d, %d 个不透明像素重着色为紫色, alpha=%d/255=%.2f)%n",
                src.getName(), dst.getName(), out.getWidth(), out.getHeight(), painted, bakedAlpha, ALPHA);
    }
}
