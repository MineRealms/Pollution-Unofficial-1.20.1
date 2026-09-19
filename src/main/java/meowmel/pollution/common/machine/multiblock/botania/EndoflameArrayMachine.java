package meowmel.pollution.common.machine.multiblock.botania;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import vazkii.botania.common.block.BotaniaFlowerBlocks;

import java.util.List;

/**
 * Endoflame magical power array: burns furnace fuel to fill mana pool hatches.
 *
 * <p>Upstream origin: {@code MetaTileEntityEndoflameArray} (1.12.2). Every
 * working tick it counts the Endoflame flowers in its input bus, converts every
 * furnace fuel item there into a burn-time buffer and converts the buffer into
 * mana at {@code 1.5 mana per burn tick per flower} ({@code speed} is capped by
 * both the flower count and the remaining buffer), pushing the mana into its
 * mana output pool hatch. The buffer is capped at
 * {@code 1_600_000_000} ticks.</p>
 *
 * <p>Deviations:</p>
 * <ul>
 *   <li>Upstream detected the flower with
 *       {@code ItemBlockSpecialFlower.getType(stack)}; Botania 1.20.1 has no
 *       such item API, so the port compares the item against
 *       {@code BotaniaFlowerBlocks.endoflame.asItem()}.</li>
 *   <li>{@code TileEntityFurnace.getItemBurnTime} is replaced by
 *       {@link ForgeHooks#getBurnTime(ItemStack, RecipeType)}.</li>
 *   <li>Upstream derived its mana handler from
 *       {@code getAbilities(MANA_OUTPUT_POOL)}; the port's
 *       {@link AbstractManaControlMachine} already collects every
 *       {@code IManaHatch} part after the structure forms.</li>
 *   <li>The active state flag is not exposed because modern GT machine UIs
 *       derive activity from the working status; the display keeps the
 *       upstream numbers.</li>
 * </ul>
 */
public class EndoflameArrayMachine extends AbstractManaControlMachine implements IDisplayUIMachine {

    private static final String NBT_FUEL_CACHE = "FuelBurnTime";
    private static final int MAX_TICKS = 1_600_000_000;

    private int flowers;
    private int fuelCount;
    private int fireticks;
    private int speed;
    private long manaOutput;

    private TickableSubscription tickSubscription;

    public EndoflameArrayMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::tickArray);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    private void tickArray() {
        if (!(getLevel() instanceof ServerLevel) || !isFormed()) {
            return;
        }
        int previousFireticks = fireticks;
        ItemBusPartMachine bus = findInputBus();
        if (bus != null) {
            var inventory = bus.getInventory();
            flowers = 0;
            fuelCount = 0;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.is(BotaniaFlowerBlocks.endoflame.asItem())) {
                    flowers += stack.getCount();
                }
                if (burnTime(stack) > 0) {
                    fuelCount += stack.getCount();
                }
            }
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                int time = burnTime(stack);
                if (flowers > 0 && time > 0 && !getManaHandler().isFull()
                        && fireticks <= MAX_TICKS - time) {
                    ItemStack extracted = inventory.extractItem(slot, 1, false);
                    if (!extracted.isEmpty()) {
                        fireticks += burnTime(extracted);
                        fuelCount -= extracted.getCount();
                    }
                }
            }
        }
        if (getManaHandler().isFull()) {
            speed = 0;
            manaOutput = 0L;
        } else {
            speed = Math.min(flowers, fireticks);
            long requestedMana = speed * 3L / 2L;
            long acceptedMana = requestedMana - getManaHandler().addMana(requestedMana);
            manaOutput = acceptedMana;
            if (acceptedMana > 0L && requestedMana > 0L) {
                speed = (int) Math.min(speed,
                        (acceptedMana * speed + requestedMana - 1L) / requestedMana);
                fireticks -= speed;
            } else {
                speed = 0;
            }
        }
        if (fireticks != previousFireticks) {
            markDirty();
        }
    }

    private ItemBusPartMachine findInputBus() {
        for (IMultiPart part : getParts()) {
            if (part.self() instanceof ItemBusPartMachine bus
                    && PartAbility.IMPORT_ITEMS.isApplicable(bus.getBlockState().getBlock())) {
                return bus;
            }
        }
        return null;
    }

    private static int burnTime(ItemStack stack) {
        return stack.isEmpty() ? 0 : ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("pollution.machine.endoflame_array.display.flowers", flowers));
            textList.add(Component.translatable("pollution.machine.endoflame_array.display.fuel_items", fuelCount));
            textList.add(Component.translatable("pollution.machine.endoflame_array.display.fuel_cache",
                    fireticks, MAX_TICKS));
            textList.add(Component.translatable("pollution.machine.endoflame_array.display.output", manaOutput));
            textList.add(Component.translatable("pollution.machine.endoflame_array.display.mana_pool",
                    getMana(), getMaxMana()));
        }
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putInt(NBT_FUEL_CACHE, fireticks);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        fireticks = Math.max(0, Math.min(MAX_TICKS, tag.getInt(NBT_FUEL_CACHE)));
    }

    public static BlockPattern createPattern(MultiblockMachineDefinition definition) {
        return EndoflameArrayPatterns.create(definition);
    }
}
