package net.thanachot.yurushi.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.thanachot.yurushi.Yurushi;
import net.thanachot.yurushi.util.PlayerNotificationUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.UUID;

@Mixin(BannedPlayerList.class)
public abstract class UserBanListMixin {

    @Inject(method = "add(Lnet/minecraft/server/BannedPlayerEntry;)Z", at = @At("TAIL"))
    private void onPlayerBanned(BannedPlayerEntry entry, CallbackInfoReturnable<Boolean> cir) {
        try {
            Field keyField = entry.getClass().getSuperclass().getDeclaredField("key");
            keyField.setAccessible(true);
            GameProfile profile = (GameProfile) keyField.get(entry);
            if (profile == null)
                return;

            Field nameField = GameProfile.class.getDeclaredField("name");
            Field idField = GameProfile.class.getDeclaredField("id");
            nameField.setAccessible(true);
            idField.setAccessible(true);

            String playerName = (String) nameField.get(profile);
            UUID playerId = (UUID) idField.get(profile);

            String reason = entry.getReason() != null ? entry.getReason() : "";
            String source = entry.getSource() != null ? entry.getSource() : "";

            PlayerNotificationUtil.sendBanNotification(playerName, playerId, reason, source);
        } catch (Exception e) {
            Yurushi.LOGGER.error("Failed to send ban notification", e);
        }
    }
}
