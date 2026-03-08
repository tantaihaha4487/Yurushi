package net.thanachot.yurushi.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.PlayerManager;
import net.thanachot.yurushi.util.PlayerNotificationUtil;
import net.thanachot.yurushi.util.ServerAccessor;
import net.thanachot.yurushi.Yurushi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Unique
    private boolean yurushi$checkedFirstJoin = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo ci) {
        if (yurushi$checkedFirstJoin)
            return;

        yurushi$checkedFirstJoin = true;

        try {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            
            if (ServerAccessor.getServer().isEmpty()) return;
            
            
            MinecraftServer server = ServerAccessor.getServer().get();
            PlayerManager playerManager = server.getPlayerManager();
            
            if (playerManager != null) {
                boolean isFirstJoin = player.getStatHandler() == null || 
                    player.getStatHandler().getStat(net.minecraft.stat.Stats.CUSTOM.getOrCreateStat(
                        net.minecraft.stat.Stats.PLAY_TIME)) == 0;
                
                if (isFirstJoin) {
                    PlayerNotificationUtil.sendFirstJoinGreeting(player.getName().getString(), player.getUuid());
                }
            }
        } catch (Exception e) {
            Yurushi.LOGGER.error("Failed to check first join status", e);
        }
    }
}
