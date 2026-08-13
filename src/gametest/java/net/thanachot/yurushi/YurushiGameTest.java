package net.thanachot.yurushi;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.server.players.UserWhiteListEntry;
import net.thanachot.yurushi.util.PlayerNotificationUtil;
import net.thanachot.yurushi.util.ServerAccessor;

public class YurushiGameTest {
    @GameTest
    public void initializesServerStateAndRegistersReloadCommand(GameTestHelper helper) {
        var server = helper.getLevel().getServer();

        if (ServerAccessor.getServer().orElse(null) != server) {
            helper.fail("Yurushi did not retain the running server");
            return;
        }
        var reloadNode = server.getCommands().getDispatcher().findNode(
                java.util.List.of("yurushi", "reload"));
        if (reloadNode == null) {
            helper.fail("/yurushi reload was not registered");
            return;
        }
        if (!reloadNode.canUse(server.createCommandSourceStack())) {
            helper.fail("Server command source lost the reload permission fallback");
            return;
        }
        if (!PlayerNotificationUtil.isFirstJoin(0) || PlayerNotificationUtil.isFirstJoin(1)) {
            helper.fail("First-join classification changed");
            return;
        }

        helper.succeed();
    }

    @GameTest
    public void mutatesWhitelistAndExecutesBanHook(GameTestHelper helper) {
        var playerList = helper.getLevel().getServer().getPlayerList();
        var profile = NameAndId.createOffline("YurushiGameTest");

        playerList.getWhiteList().add(new UserWhiteListEntry(profile));
        if (!playerList.getWhiteList().isWhiteListed(profile)) {
            helper.fail("Whitelist add did not persist");
            return;
        }

        playerList.getWhiteList().remove(profile);
        if (playerList.getWhiteList().isWhiteListed(profile)) {
            helper.fail("Whitelist remove did not persist");
            return;
        }

        playerList.getBans().add(new UserBanListEntry(profile));
        if (!playerList.getBans().isBanned(profile)) {
            helper.fail("Ban list add or Yurushi ban hook failed");
            return;
        }
        playerList.getBans().remove(profile);

        helper.succeed();
    }
}
