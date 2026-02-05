package net.thanachot.yurushi.manager;

import net.minecraft.server.MinecraftServer;

import java.util.Optional;

public class ServerAccessor {

    private static MinecraftServer server;
    private static WhitelistManager whitelistManager;

    private ServerAccessor() {
    }

    public static void setServer(MinecraftServer minecraftServer) {
        server = minecraftServer;
        if (minecraftServer != null) {
            whitelistManager = new WhitelistManager(minecraftServer);
        } else {
            whitelistManager = null;
        }
    }

    public static Optional<MinecraftServer> getServer() {
        return Optional.ofNullable(server);
    }

    public static Optional<WhitelistManager> getWhitelistManager() {
        return Optional.ofNullable(whitelistManager);
    }

    public static boolean isServerAvailable() {
        return server != null;
    }
}
