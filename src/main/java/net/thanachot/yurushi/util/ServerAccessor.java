package net.thanachot.yurushi.util;

import net.minecraft.server.MinecraftServer;
import net.thanachot.yurushi.manager.WhitelistManager;

import java.util.Optional;

public class ServerAccessor {

    private static MinecraftServer server;
    private static WhitelistManager whitelistManager;

    private ServerAccessor() {
    }

    public static Optional<MinecraftServer> getServer() {
        return Optional.ofNullable(server);
    }

    public static void setServer(MinecraftServer minecraftServer) {
        server = minecraftServer;
        if (minecraftServer != null) {
            whitelistManager = new WhitelistManager(minecraftServer);
        } else {
            whitelistManager = null;
        }
    }

    public static Optional<WhitelistManager> getWhitelistManager() {
        return Optional.ofNullable(whitelistManager);
    }

    public static boolean isServerAvailable() {
        return server != null;
    }
}
