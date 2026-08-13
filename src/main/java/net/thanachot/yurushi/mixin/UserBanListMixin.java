package net.thanachot.yurushi.mixin;

import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.thanachot.yurushi.Yurushi;
import net.thanachot.yurushi.util.PlayerNotificationUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UserBanList.class)
public abstract class UserBanListMixin {

    @Inject(method = "add(Lnet/minecraft/server/players/UserBanListEntry;)Z", at = @At("TAIL"))
    private void onPlayerBanned(UserBanListEntry entry, CallbackInfoReturnable<Boolean> cir) {
        try {
            var profile = entry.getUser();
            if (profile == null)
                return;

            String playerName = profile.name();
            var playerId = profile.id();

            String reason = entry.getReason() != null ? entry.getReason() : "";
            String source = entry.getSource() != null ? entry.getSource() : "";

            PlayerNotificationUtil.sendBanNotification(playerName, playerId, reason, source);
        } catch (Exception e) {
            Yurushi.LOGGER.error("Failed to send ban notification", e);
        }
    }
}
