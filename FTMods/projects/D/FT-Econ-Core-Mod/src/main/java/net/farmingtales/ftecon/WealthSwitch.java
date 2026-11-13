package net.farmingtales.ftecon;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 全局开关：与旧 KubeJS 完全同文件同结构。
 * config/farmingtales_wealth_switch.json  ->  {"enabled": true|false}
 * 默认开启。文件不存在/解析失败 = 开启。
 */
public final class WealthSwitch {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_NAME = "farmingtales_wealth_switch.json";
    private static boolean enabled = true;

    private WealthSwitch() {}

    public static void load() {
        try {
            Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
            if (Files.isRegularFile(path)) {
                String text = Files.readString(path, StandardCharsets.UTF_8);
                // {"enabled": true|false}，手写解析避免引 Gson
                boolean v = true;
                int i = text.indexOf("enabled");
                if (i >= 0) {
                    String rest = text.substring(i);
                    v = !rest.contains("false");
                }
                enabled = v;
            } else {
                enabled = true;
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("[FT经济] 开关文件读取失败，默认开启: {}", e.toString());
            enabled = true;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean setEnabled(boolean value) {
        enabled = value;
        try {
            Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
            Files.createDirectories(path.getParent());
            Files.writeString(path, "{\"enabled\":" + value + "}", StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            LOGGER.warn("[FT经济] 开关保存失败: {}", e.toString());
            return false;
        }
    }
}
