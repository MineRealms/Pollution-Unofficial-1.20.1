# Pollution 1.20.1 port - TC4 (Thaumcraft) completion audit

## 1. Scope: which source files belong to Thaumcraft

| bucket | upstream files | port files |
|---|---|---|
| ae2 | 2 | 1 |
| astral | 57 | 17 |
| bloodmagic | 20 | 1 |
| botania | 82 | 67 |
| core | 67 | 38 |
| gcym | 1 | 3 |
| gtnn | 0 | 5 |
| thaumcraft | 194 | 138 |

java files: upstream 423, port 270

## 2. Machine coverage (TC4 scope only)

- upstream registrations: 110 total, **60 TC4-scoped** (50 other-mod entries excluded)
- port machine ids discovered: 309

### 2.1 matched by identical normalised id: 44
### 2.2 matched by token similarity (renamed in port): 10
### 2.3 MISSING: 6

| upstream id | upstream class | port id | similarity |
|---|---|---|---|
| `flux_promoted_fuel_cell.ev` | MetaTileEntityFluxPromotedFuelCell | `mv_flux_fuel_cell` | 0.75 |
| `flux_promoted_fuel_cell.hv` | MetaTileEntityFluxPromotedFuelCell | `mv_flux_fuel_cell` | 0.75 |
| `flux_promoted_fuel_cell.iv` | MetaTileEntityFluxPromotedFuelCell | `mv_flux_fuel_cell` | 0.75 |
| `flux_promoted_fuel_cell.lv` | MetaTileEntityFluxPromotedFuelCell | `mv_flux_fuel_cell` | 0.75 |
| `flux_promoted_fuel_cell.mv` | MetaTileEntityFluxPromotedFuelCell | `mv_flux_fuel_cell` | 0.75 |
| `pollution_small_node_generator.luv` | MetaTileEntitySmallNodeGenerator | `luv_small_node_generator` | 0.75 |
| `pollution_small_node_generator.uhv` | MetaTileEntitySmallNodeGenerator | `luv_small_node_generator` | 0.75 |
| `pollution_small_node_generator.uv` | MetaTileEntitySmallNodeGenerator | `luv_small_node_generator` | 0.75 |
| `pollution_small_node_generator.zpm` | MetaTileEntitySmallNodeGenerator | `luv_small_node_generator` | 0.75 |
| `vis.` | MetaTileEntityVisGenerator | `mv_vis_generator` | 0.5 |

| upstream id | upstream class |
|---|---|
| `bm_hpca` | MetaTileEntityBMHPCA |
| `flux_clear.` | MetaTileEntityFluxClear |
| `flux_clear.ev` | MetaTileEntityFluxClear |
| `flux_clear.iv` | MetaTileEntityFluxClear |
| `pollution_muffler_hatch.` | MetaTileEntityFluxMuffler |
| `tarot_hatch` | MetaTileEntityTarotHatch |

## 3. Subsystem probes (port tree)

| subsystem | present | evidence files |
|---|---|---|
| Aspects / aspect registry | yes | CompoundAspectRecipes.java, PollutionObjectAspects.java, TCAspectAddons.java |
| Aspect-storage machines (tank/index) | yes | AspectTankBlockEntity.java, AspectTankMachine.java, GTEssentiaHandler.java... |
| Aspect -> GT fluid bridge | yes | GtEssenceSmelterMachine.java, PollutionAspectMapping.java |
| Compound aspect recipes | yes | CompoundAspectRecipes.java, PollutionRecipes.java |
| Vis (aura) generation | yes | PollutionMachines.java, VisGeneratorMachine.java, VisProviderMachine.java |
| Vis hatch (multiblock capability) | yes | PollutionMachines.java, VisHatchMachine.java |
| Vis storage container | yes | ICleanVis.java, NodeFusionReactorMachine.java |
| Node machines | yes | LargeNodeGeneratorMachine.java, NodeFusionReactorMachine.java, NodeProducerMachine.java... |
| Infusion (TC altar in GT) | yes | BotaniaNativeRecipes.java, IndustrialInfusionMachine.java, InfusionRecipes.java... |
| Infused fluid hatch | yes | InfusedFluidHatchMachine.java, MagicMultiblockController.java, PollutionJeiPlugin.java... |
| Essentia smeltery | yes | EssenceSmelterMachine.java, GtEssenceSmelterMachine.java, PollutionMachines.java |
| Essence collector | yes | EssenceCollectorMachine.java, PollutionMachines.java |
| Flux scrubber / muffler | yes | FluxMufflerMachine.java, FluxScrubberMachine.java, PollutionMachines.java |
| Flux fuel cell | yes | FluxFuelCellMachine.java, MachinePollution.java, PollutionMachines.java |
| Warp events engine | yes | Pollution.java, WarpEventHandler.java |
| Warp event implementations | NO | - |
| Flux warp accumulation | yes | FluxWarpManager.java, WarpEventHandler.java |
| Magic sweep (flight + immunity) | NO | - |
| Forge alchemy (metal transmute) | yes | FirstDegreeMaterials.java, ForgeAlchemyRecipes.java, MagicChemicalRecipes.java... |
| Thaumcraft recipe bridging | yes | AERecipes.java, CoilRecipes.java, ForgeAlchemyRecipes.java... |
| TC4R infusion JSON emitter | yes | InfusionRecipes.java |
| Magic energy amplification | yes | MagicAmplificationEngine.java, MagicEnergyAmplification.java, MagicRecipeLogic.java... |
| Cultivated crystal cluster | yes | AstralCrystalNbtHelper.java, CrystalQualityItem.java, InfusionRecipes.java... |
| Alchemical construct / furnace | yes | InfusionRecipes.java |
| Arcane worktable | yes | InfusionRecipes.java |
| Aspect-gated solar plate | yes | PollutionMachines.java, SolarPlateMachine.java |
| Pollution (chunk) engine | yes | PollutionData.java, PollutionEngine.java |
| Machine pollution emission | yes | MachinePollution.java, MachinePollutionEvents.java, MagicRecipeLogic.java |

subsystem probes passed: 26/28
