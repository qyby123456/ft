package net.farmingtales.ftecon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 档位/倍率参数配置（config/ftecon-config.json）。
 * 不存在则按默认值生成一份，之后可手改，无需重新编译；重启服务器生效。
 *
 * 字段说明：
 *   tiersMin    累计收入门槛(含)，与 tiersRatio 一一对应
 *   tiersRatio  各档位比例(0~1)
 *   bandMax     复杂度分档上限(按商品基础价)，与 bandFactor 一一对应
 *   bandFactor  各复杂度 factor：<1 降价、>1 涨价(满进度=基础价×factor)
 *   lowGoodsExtra   低级货逐档额外折扣(与 tiersRatio 一一对应；100万/1000万/1亿 各降一截)
 *   lowGoodsFloor   低级货成交倍率保底(默认0.30)
 */
public final class WealthConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_NAME = "ftecon-config.json";

    private static double[] tierMin = { 0, 10000, 100000, 1000000, 10000000, 100000000 };
    private static double[] tierRatio = { 0.0, 0.2, 0.4, 0.6, 0.8, 1.0 };
    private static double[] bandMax = { 10, 30, 80, 999999999 };
    private static double[] bandFactor = { 0.3, 0.65, 0.8, 1.3 }; // v146: 高档 2.0->1.3, 低档 0.45->0.3（用户调参） // 高档(>80)满进度最多×2.0
    // 低级货逐档额外折扣：与 tiersRatio 一一对应(100万/1000万/1亿 段各降一截)
    private static final double[] DEFAULT_LOW_EXTRA = { 1.0, 1.0, 1.0, 0.95, 0.90, 0.80 };
    private static double[] lowExtra = DEFAULT_LOW_EXTRA;
    private static double lowFloor = 0.30;

    private WealthConfig() {}

    public static void load(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        if (Files.isRegularFile(file)) {
            try {
                JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
                double[] tm = readArray(root, "tiersMin");
                double[] tr = readArray(root, "tiersRatio");
                double[] bm = readArray(root, "bandMax");
                double[] bf = readArray(root, "bandFactor");
                if (tm.length > 0 && tm.length == tr.length && bm.length > 0 && bm.length == bf.length) {
                    tierMin = tm;
                    tierRatio = tr;
                    bandMax = bm;
                    bandFactor = bf;
                } else {
                    LOGGER.warn("[FT经济] 配置文件字段长度不匹配，使用默认值");
                }
                lowExtra = readArray(root, "lowGoodsExtra");
                if (lowExtra.length != tierRatio.length) {
                    lowExtra = DEFAULT_LOW_EXTRA;
                    LOGGER.warn("[FT经济] lowGoodsExtra 长度与 tiersRatio 不一致，使用默认逐档折扣");
                }
                lowFloor = readDouble(root, "lowGoodsFloor", 0.30);
                LOGGER.info("[FT经济] 配置已加载: tiers={}, 高档factor={}, 低级货逐档折扣={}",
                        tierRatio.length, bandFactor[bandFactor.length - 1],
                        java.util.Arrays.toString(lowExtra));
                return;
            } catch (Exception e) {
                LOGGER.warn("[FT经济] 配置文件解析失败({})，使用默认值", e.toString());
            }
        }
        writeDefaults(file);
    }

    private static double[] readArray(JsonObject root, String key) {
        try {
            JsonArray arr = root.getAsJsonArray(key);
            double[] out = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                out[i] = arr.get(i).getAsDouble();
            }
            return out;
        } catch (Exception e) {
            return new double[0];
        }
    }

    private static double readDouble(JsonObject root, String key, double def) {
        try {
            if (root.has(key)) return root.get(key).getAsDouble();
        } catch (Exception ignored) {}
        return def;
    }

    private static void writeDefaults(Path file) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, defaultJson(), StandardCharsets.UTF_8);
            LOGGER.info("[FT经济] 已生成默认配置 {}", file.toAbsolutePath());
        } catch (Exception e) {
            LOGGER.warn("[FT经济] 默认配置写入失败: {}", e.toString());
        }
    }

    private static String defaultJson() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("  \"tiersMin\": ").append(jsonArray(tierMin)).append(",\n");
        sb.append("  \"tiersRatio\": ").append(jsonArray(tierRatio)).append(",\n");
        sb.append("  \"bandMax\": ").append(jsonArray(bandMax)).append(",\n");
        sb.append("  \"bandFactor\": ").append(jsonArray(bandFactor)).append(",\n");
        sb.append("  \"lowGoodsExtra\": ").append(jsonArray(lowExtra)).append(",\n");
        sb.append("  \"lowGoodsFloor\": ").append(jsonNum(lowFloor)).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String jsonArray(double[] a) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < a.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(jsonNum(a[i]));
        }
        return sb.append("]").toString();
    }

    private static String jsonNum(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    public static double[] tierMin() { return tierMin; }
    public static double[] tierRatio() { return tierRatio; }
    public static double[] bandMax() { return bandMax; }
    public static double[] bandFactor() { return bandFactor; }
    public static double[] lowExtra() { return lowExtra; }
    public static double lowFloor() { return lowFloor; }
}
