package city.norain.slimefun4;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;

class EnvironmentChecker {
    private static final List<String> UNSUPPORTED_PLUGINS =
            List.of("BedrockTechnology", "SlimefunFix", "SlimefunBugFixer", "Slimefunbookfix", "MiraiMC");

    static boolean checkIncompatiblePlugins(@Nonnull Slimefun sf, @Nonnull Logger logger) {
        List<String> plugins = UNSUPPORTED_PLUGINS.stream()
                .filter(name -> sf.getServer().getPluginManager().getPlugin(name) != null)
                .toList();

        if (plugins.isEmpty()) {
            return false;
        }

        printBorder(logger);
        logger.log(Level.WARNING, "");
        logger.log(Level.WARNING, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        logger.log(Level.WARNING, "检测到不兼容的插件, 已自动禁用 Slimefun!");
        logger.log(Level.WARNING, "插件列表: ", String.join(", ", plugins));
        logger.log(Level.WARNING, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        logger.log(Level.WARNING, "");
        printBorder(logger);
        Bukkit.getPluginManager().disablePlugin(sf);

        return true;
    }

    static boolean checkHybridServer(@Nonnull Slimefun sf, @Nonnull Logger logger) {
        try {
            Class.forName("net.minecraftforge.common.MinecraftForge");
            Class.forName("net.fabricmc.loader.impl.launch.server.FabricServerLauncher");

            printBorder(logger);
            logger.log(Level.WARNING, "");
            logger.log(Level.WARNING, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.log(Level.WARNING, "检测到正在使用混合端, Slimefun 将会被禁用!");
            logger.log(Level.WARNING, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.log(Level.WARNING, "");
            printBorder(logger);

            Bukkit.getPluginManager().disablePlugin(sf);

            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    static void scheduleSlimeGlueCheck(@Nonnull Slimefun sf, @Nonnull Logger logger) {
        Bukkit.getScheduler()
                .runTaskLater(
                        sf,
                        () -> {
                            if (Bukkit.getPluginManager().getPlugin("SlimeGlue") == null) {
                                logger.log(Level.WARNING, "检测到没有安装 SlimeGlue (粘液胶), 你将缺失对一些插件的额外保护检查!");
                                logger.log(Level.WARNING, "下载: https://github.com/Xzavier0722/SlimeGlue");
                            }
                        },
                        300); // 15s
    }

    private static void printBorder(@Nonnull Logger logger) {
        logger.log(Level.WARNING, "#######################################################");
    }
}
