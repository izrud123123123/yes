package com.aurora.visuals.mixin;

import com.aurora.visuals.VisualsConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Client-side time changer: the server's real time is untouched, only what you see changes. */
@Mixin(ClientLevel.ClientLevelData.class)
public class ClientLevelDataMixin {
    @Inject(method = "getDayTime", at = @At("HEAD"), cancellable = true)
    private void aurora$time(CallbackInfoReturnable<Long> cir) {
        VisualsConfig c = VisualsConfig.get();
        if (c.timeEnabled) cir.setReturnValue((long) c.timeOfDay);
    }
}
