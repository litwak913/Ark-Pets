/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.utils.ArgPending;
import cn.harryh.arkpets.utils.Logger;
import javafx.application.Application;

import java.util.Objects;

import static cn.harryh.arkpets.Const.*;


/** The entrance of the whole program, also the bootstrap for ArkHomeFX.
 * @see ArkHomeFX
 */
public class DesktopLauncher {
    public static void main (String[] args) {
        ArgPending.argCache = args;
        // Logger
        Logger.initialize(LogConfig.logDesktopPath, LogConfig.logDesktopMaxKeep);
        try {
            Logger.setLevel(Objects.requireNonNull(ArkConfig.getConfig()).logging_level);
        } catch (Exception ignored) {
        }
        new ArgPending(LogConfig.errorArg, args) {
            protected void process(String command, String addition) {
                Logger.setLevel(Logger.ERROR);
            }
        };
        new ArgPending(LogConfig.warnArg, args) {
            protected void process(String command, String addition) {
                Logger.setLevel(Logger.WARN);
            }
        };
        new ArgPending(LogConfig.infoArg, args) {
            protected void process(String command, String addition) {
                Logger.setLevel(Logger.INFO);
            }
        };
        new ArgPending(LogConfig.debugArg, args) {
            protected void process(String command, String addition) {
                Logger.setLevel(Logger.DEBUG);
            }
        };
        Logger.info("System", "Entering the app of DesktopLauncher");

        // If requested to start the core app directly
        new ArgPending("--direct-start", args) {
            protected void process(String command, String addition) {
                EmbeddedLauncher.main(args);
                System.exit(0);
            }
        };
        // Java FX bootstrap
        Application.launch(ArkHomeFX.class, args);
        Logger.info("System", "Exited from DesktopLauncher successfully");
        System.exit(0);
    }
}
