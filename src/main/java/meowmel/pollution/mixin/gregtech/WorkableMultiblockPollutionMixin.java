package meowmel.pollution.mixin.gregtech;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import meowmel.pollution.api.pollution.MachinePollution;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** All GT multiblocks emit at their exhaust, including machines supplied by other addons. */
@Mixin(value = WorkableMultiblockMachine.class, remap = false)
public abstract class WorkableMultiblockPollutionMixin {
    @Inject(method = "afterWorking", at = @At("TAIL"))
    private void pollution$afterWorking(CallbackInfo ci) {
        var controller = (WorkableMultiblockMachine) (Object) this;
        if (!(controller.getLevel() instanceof ServerLevel level)) return;
        for (var part : controller.getParts()) {
            if (part instanceof IMufflerMachine muffler) {
                MachinePollution.addMufflerPollution(level, part.self().getPos(),
                        muffler.getHazardStrengthPerOperation());
            }
        }
    }
}
