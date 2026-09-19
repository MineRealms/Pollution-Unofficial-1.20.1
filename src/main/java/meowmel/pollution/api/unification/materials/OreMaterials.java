package meowmel.pollution.api.unification.materials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import meowmel.pollution.Pollution;
import meowmel.pollution.api.unification.PollutionMaterials;
import net.minecraft.resources.ResourceLocation;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

/**
 * Ore materials of the Pollution port.
 *
 * <p>Port of upstream {@code meowmel.pollution.api.unification.materials.OreMaterials}
 * (14 materials). Ported: 11 materials whose base components all exist in
 * GTCEu 7.5.3. Adaptations and skips, all verified against the 7.5.3 binary:</p>
 * <ul>
 *   <li>{@code Syrmorite}, {@code Octine}, {@code Valonite} are already
 *       registered by {@link SubstrateMaterials} with the port's simplified
 *       shapes (dust/gem, used by the substrate chemistry recipes); their
 *       upstream ingot/ore/tool definitions are not re-registered. Only
 *       {@code .fluid()} was added to those three so the forge-alchemy
 *       transmutations can output them.</li>
 *   <li>{@code setTooltips(...)} has no GTCEu 7.5.3 equivalent (confirmed
 *       against the binary), the upstream tooltip strings are dropped.</li>
 *   <li>{@code MaterialToolProperty} / {@code rotorStats(float,float,int)} are
 *       1.12 GTCE signatures; 7.5.3 uses {@code ToolProperty.Builder} and
 *       {@code rotorStats(int,int,float,int)}. The upstream values cannot be
 *       transplanted 1:1 and would need rebalancing, so tool/rotor stats are
 *       skipped.</li>
 *   <li>{@code GENERATE_DOUBLE_PLATE} does not exist in 7.5.3 and is
 *       dropped.</li>
 *   <li>{@code Materials.Chrome} was renamed to {@code Chromium} in GTCEu.</li>
 * </ul>
 */
public final class OreMaterials {

    private OreMaterials() {}

    public static void register() {
        // 痂壳晶 Scabyst
        PollutionMaterials.Scabyst = new Material.Builder(id("scabyst"))
                .color(0x53C58D)
                .gem().fluid().ore(true)
                .components(Iron, 1, Silicon, 4, Oxygen, 8,
                        PollutionMaterials.InfusedFire, 5,
                        PollutionMaterials.InfusedEarth, 5,
                        PollutionMaterials.InfusedOrder, 10)
                .iconSet(MaterialIconSet.GEM_HORIZONTAL)
                .flags(GENERATE_PLATE, GENERATE_GEAR, GENERATE_ROD,
                        DECOMPOSITION_BY_CENTRIFUGING, GENERATE_BOLT_SCREW,
                        GENERATE_FRAME, GENERATE_DENSE)
                .formula("((SiO2)4Fe)(IgTerOrd2)5)4", true)
                .buildAndRegister();

        // 固焰煤 FlameCoal
        PollutionMaterials.FlameCoal = new Material.Builder(id("flame_coal"))
                .gem(1, 2400).ore(2, 1, true)
                .color(0xFF6347).iconSet(MaterialIconSet.LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE,
                        EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Carbon, 1, PollutionMaterials.InfusedFire, 7,
                        PollutionMaterials.InfusedEarth, 3)
                .buildAndRegister();

        // 哑泽锡 DumbTin
        PollutionMaterials.DumbTin = new Material.Builder(id("dumb_tin"))
                .ingot(1)
                .liquid(505)
                .plasma()
                .ore(true)
                .color(0xDCDCDC)
                .flags(MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                        GENERATE_FINE_WIRE)
                .components(Tin, 1, PollutionMaterials.InfusedWater, 5,
                        PollutionMaterials.InfusedAir, 2)
                .cableProperties(GTValues.V[GTValues.LV], 4, 1)
                .itemPipeProperties(4096, 0.8f)
                .buildAndRegister();

        // 铄世金 MeltGold
        PollutionMaterials.MeltGold = new Material.Builder(id("melt_gold"))
                .ingot()
                .liquid(1337)
                .ore(true)
                .color(0xFFE650).iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_RING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_FOIL)
                .components(Gold, 1, PollutionMaterials.InfusedFire, 5,
                        PollutionMaterials.InfusedAir, 7)
                .cableProperties(GTValues.V[GTValues.HV], 6, 2)
                .fluidPipeProperties(1671, 50, true, true, false, false)
                .buildAndRegister();

        // 镇渊铅 AuthorityLead
        PollutionMaterials.AuthorityLead = new Material.Builder(id("authority_lead"))
                .ingot(1)
                .liquid(600)
                .ore(true)
                .color(0x8C648C)
                .flags(MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                        GENERATE_FINE_WIRE)
                .components(Lead, 1, PollutionMaterials.InfusedFire, 5,
                        PollutionMaterials.InfusedOrder, 7)
                .cableProperties(GTValues.V[GTValues.LV], 16, 2)
                .fluidPipeProperties(1600, 64, true)
                .buildAndRegister();

        // 深红银矿 Pyrargyrite
        PollutionMaterials.Pyrargyrite = new Material.Builder(id("pyrargyrite"))
                .ore(true)
                .dust().fluid()
                .color(0x8B4726)
                .components(Silver, 3, Antimony, 1, Sulfur, 3,
                        PollutionMaterials.InfusedFire, 5, PollutionMaterials.InfusedAir, 7)
                .buildAndRegister();

        // 冥晶锌 PlutoZinc
        PollutionMaterials.PlutoZinc = new Material.Builder(id("pluto_zinc"))
                .ore(true)
                .dust().fluid()
                .color(0x8B2252)
                .components(Zinc, 2, Antimony, 1, Oxygen, 1,
                        PollutionMaterials.InfusedWater, 5, PollutionMaterials.InfusedEarth, 7)
                .buildAndRegister();

        // 龙石 Dragonstone
        PollutionMaterials.Dragonstone = new Material.Builder(id("dragonstone"))
                .gem().ore()
                .color(0xF274D3)
                .iconSet(MaterialIconSet.SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();

        // 妖精尘 PixieDust
        PollutionMaterials.PixieDust = new Material.Builder(id("pixie_dust"))
                .dust().ore()
                .color(0xF7B4EC)
                .iconSet(MaterialIconSet.SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();

        // 精灵石英 ElvenQuartz
        PollutionMaterials.ElvenQuartz = new Material.Builder(id("elven_quartz"))
                .gem().ore()
                .color(0xB8F1EA)
                .iconSet(MaterialIconSet.SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .buildAndRegister();

        // 琥珀 Amber
        PollutionMaterials.Amber = new Material.Builder(id("amber"))
                .gem().ore()
                .color(0xE8A62D)
                .iconSet(MaterialIconSet.SHINY)
                .components(Carbon, 10, Hydrogen, 16, Oxygen, 1)
                .buildAndRegister();
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Pollution.MOD_ID, name);
    }
}
