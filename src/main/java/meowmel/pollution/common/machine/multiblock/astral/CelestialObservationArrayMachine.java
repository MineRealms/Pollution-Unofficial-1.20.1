package meowmel.pollution.common.machine.multiblock.astral;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
public final class CelestialObservationArrayMachine extends AstralProcessingMachine {
    public CelestialObservationArrayMachine(IMachineBlockEntity holder) { super(holder, true); }
    public static BlockPattern createPattern(MultiblockMachineDefinition definition) { return observatory(definition); }
}
