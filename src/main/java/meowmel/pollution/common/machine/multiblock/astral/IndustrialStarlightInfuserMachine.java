package meowmel.pollution.common.machine.multiblock.astral;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
public final class IndustrialStarlightInfuserMachine extends AstralProcessingMachine {
    public IndustrialStarlightInfuserMachine(IMachineBlockEntity holder) { super(holder, false); }
    public static BlockPattern createPattern(MultiblockMachineDefinition definition) { return infuser(definition); }
}
