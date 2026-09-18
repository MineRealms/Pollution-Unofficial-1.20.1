# Aspect Tank feasibility report

Feasibility of porting upstream `MetaTileEntityAspectTank` (957 lines,
`common/metatileentity/storage/`) and its helper `common/lib/GTEssentiaHandler.java`
(224 lines) against the Thaumcraft 4R (`dev.tc4port`, jar
`local-repo/dev/tc4port/thaumcraft-forge/0.1.0-20711`) essentia API.

Method: the upstream tank and helper were read in full; every TC4R essentia/aspect
class used below was inspected with `javap -p` against the TC4R jar (and GTCEu
7.5.3 for the machine side). No code was implemented for this task.

## 1. What upstream does

| # | Feature | Where |
|---|---|---|
| 1 | Single-block tiered storage, 1 aspect + amount, capacities 10 000 (LV) doubling per tier to 2 560 000 (UHV); 9 registered tiers `aspect_tank.lv` .. `aspect_tank.uhv` | `MetaTileEntityAspectTank`, `PollutionMetaTileEntities.ASPECT_TANK[10]` |
| 2 | `IAspectSource` container contract: `getAspects/setAspects`, `doesContainerAccept`, `addToContainer`, `takeFromContainer`, `doesContainerContain(Amount)`, `containerContains`, `isBlocked` | tank |
| 3 | `IEssentiaTransport` tube contract: connectable/input/output per side, suction type/amount, `takeEssentia`/`addEssentia`, essentia type/amount, `minimumSuction` | tank |
| 4 | Aspect filter ("lock") and voiding mode | tank fields + GUI toggles |
| 5 | Import/export item slots that fill from / drain into essentia containers: block jars (`BlockJarItem`), phials (`ItemPhial`) and other aspect-tank items (NBT) | `fillInternalTankFromAspectContainer`, `takeInternalTankToAspectContainer`, `takeFromContainer(ItemStack, boolean)` |
| 6 | Auto-output: push 1 essentia/tick into an adjacent `IAspectSource` (tile or MTE) on the output facing | `pushAspectIntoNearbyHandlers` + `GTEssentiaHandler.addEssentiaToTile/MTE` |
| 7 | Radius search for the nearest essentia source (cached 10 s, sorted by distance, mirror tiles excluded, FX packet per transfer) | `GTEssentiaHandler.addEssentia/getSources` |
| 8 | NBT persistence; item-stack NBT (contents/filter/voiding survive pick-up); custom-data sync for client | `write/readFromNBT`, `write/initFromItemStackData`, `writeCustomData` |
| 9 | GUI: custom `QuantumAspectTankMainWidget` (tank + aspect icon + amount + lock), output/void toggle buttons | `client/gui/QuantumAspectTank/**` |
| 10 | TESR/overlay: `AspectStorageRenderer` (frame, tank fill, aspect icon, amount text) | `client/textures/custom/AspectStorageRenderer` |

## 2. TC4R API inventory (verified with `javap`)

| Class | Relevant API |
|---|---|
| `api.aspect.AspectId` | record of `ResourceLocation`; `parse(String)`, `serialized()` |
| `api.aspect.AspectAmounts` | immutable `Map<AspectId,Integer>`; `EMPTY`, `of`, `amount` |
| `api.aspect.AspectApi` | registry view, `definitions()`, `get`, `contains`, `tooltipName/Description`, `getBlockAspects`, `getItemAspects` |
| `api.aspect.AspectDefinition` | `color()`, `texture()`, `components()`, `primal()` |
| `api.aspect.AspectContainerView` | `visibleAspects()`, `visibleAspectOrder()`, `visibleAspectFilters()` |
| `api.essentia.EssentiaTransport` | directional tube contract: `isConnectable`, `canInputFrom`, `canOutputTo`, `suctionType`, `suctionAmount`, `takeEssentia`, `addEssentia`, `essentiaType`, `essentiaAmount`, `extractableAspect`, `availableEssentia`, `minimumSuction`, `renderExtendedTube`; all transfers take an `EssentiaTransferMode` (`SIMULATE`/`EXECUTE`) |
| `api.essentia.EssentiaSource` | `extractEssentia(AspectId, int, EssentiaTransferMode)` |
| `api.essentia.EssentiaSourceRef` | record `(ResourceKey<Level> dimension, BlockPos position)` |
| `api.essentia.EssentiaApi` | static `add(level, transport, aspect, amount, dir, mode)`, `take(...)`, `findSource(level, pos, aspect, amount, search[, filter])`, `extract(level, pos, aspect, amount, search, mode)`, `extract(level, ref, aspect, amount, mode)` |
| `api.essentia.EssentiaSearch` | `nearby(range)`, `ahead(direction, range)`, `MAX_RANGE` |
| `api.essentia.EssentiaContainerApi` | item containers: `capacity`, `contents`, `insert`, `extract`, `isLabel`, `labelAspect`, `withFilter`; recognises `WardedJarBlockItem` jars and `EssencePhialItem` phials |
| `api.essentia.FilledJarEssentiaApi` | filled jar items: `isJar`, `hasStoredData`, `contents`, `view`, `extract`, `transferToJar` |
| `api.essentia.EssentiaJarView` / `EssentiaMultiStoreView` | views extending `AspectContainerView`; jar adds `filter()` |
| `api.ThaumcraftApiHelper` | `getConnectableTransport(BlockGetter, BlockPos, Direction)` - returns the neighbour BE if it is an `EssentiaTransport` |
| `block.entity.WardedJarBlockEntity` | implements `EssentiaTransport`, `EssentiaSource`, `EssentiaJarView` (jar block) |
| `block.entity.EssentiaReservoirBlockEntity` | implements `EssentiaTransport`, `EssentiaSource`, `AspectContainerView` (large store) |
| `block.entity.MirrorBlockEntity` | implements `EssentiaSource`; essentia mirror exists (`MirrorLinkData`, `essentia_mirror` assets) |
| `api.alchemy.AlembicApi` | `snapshot(level, pos)`, `insertLoaded(level, pos, aspect, amount, mode)` |
| `item.EssencePhialItem` / `item.WardedJarBlockItem` | phial/jar items with aspect NBT |
| `registry.TCCapabilities` | only registers Forge capabilities for fluids/items/energy; **no Forge capability for essentia** |

## 3. Feature -> TC4R mapping

| Upstream | TC4R equivalent | Status |
|---|---|---|
| `Aspect` / `AspectList` | `AspectId` / `AspectAmounts` | direct |
| `Aspect.getAspect(tag)` | `AspectId.parse` + `AspectApi.contains/get` | direct |
| `IAspectSource` (add/take/accept/view) | split over `EssentiaTransport` (`addEssentia`/`takeEssentia`), `EssentiaSource` (`extractEssentia`) and `AspectContainerView` (`visibleAspects`); acceptance/filter is part of the implementations | direct but not one interface |
| `IEssentiaTransport` | `EssentiaTransport` | direct (extra `SIMULATE` mode) |
| `AspectList.getAspectsSortedByAmount` etc. | `AspectAmounts.amounts()` map iteration | direct |
| `GTEssentiaHandler.addEssentiaToTile/MTE` | `ThaumcraftApiHelper.getConnectableTransport` + `EssentiaApi.add(..., EXECUTE)` | direct |
| `GTEssentiaHandler` radius scan + cache | `EssentiaApi.findSource/extract` with `EssentiaSearch.nearby(range)` (native search, mirror-aware) | direct, less code |
| `BlockJarItem` / `ItemPhial` container fill/drain | `EssentiaContainerApi` (`capacity/contents/insert/extract`) | direct |
| filled jar items | `FilledJarEssentiaApi` | direct |
| `TileMirrorEssentia` exclusion | TC4R `MirrorBlockEntity` is an `EssentiaSource`; native search handles it | exists (contrary to the task brief) |
| `PacketFXEssentiaSource` FX packet | TC4R `EssentiaSourcePayload` / `EssentiaSourceEffectManager` (emitted by TC4R's own APIs) | no port needed |
| `WorldCoordinates` cache key | `EssentiaSourceRef` | direct |
| `IFastRenderMetaTileEntity` + `AspectStorageRenderer` | no per-machine fast-render hook in GTCEu 7.5.3; needs a `BlockEntityRenderer` (the port already has one for the Mineral Extractor) or an LDLib machine renderer | needs new client code |
| 1.12 `ModularUI` + custom widgets | GTCEu 7.5.3 `IFancyUIMachine`/LDLib; the port's convention for single machines is "no UI" (`SourceChargeMachine` javadoc) | needs new client code |
| item-stack contents (`writeItemStackData`) | GTCEu `saveCustomPersistedData`/`loadCustomPersistedData`; tank-in-tank item stacking has no equivalent (would be custom) | partial |
| `IActiveOutputSide` auto-output | own server tick / custom transport logic | direct |
| `setSuction` (no-op upstream) | `EssentiaTransport` has no setter; tubes compute suction | N/A |

## 4. Key findings and blockers

1. **No Forge capability for essentia.** `TCCapabilities.register` only wires
   fluids/items/energy. TC4R discovers transports by
   `blockEntity instanceof EssentiaTransport` (`ThaumcraftApiHelper.getConnectableTransport`,
   `EssentiaTubeBlockEntity.transportAt`). A GTCEu `MetaMachine` is not a block
   entity, and `MetaMachineBlockEntity` does not implement `EssentiaTransport`,
   so a tank registered the normal way (`REGISTRATE.machine(name, factory)`) is
   **invisible to TC4R tubes, golems, mirrors and alembics**. The tank could only
   push/pull actively through `EssentiaApi`, which loses half of the upstream
   behaviour.
2. **Custom block entity solves it.** `MetaMachineBlockEntity` is public and
   non-final, and `GTRegistrate` exposes a `machine(...)` overload taking a
   `TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity>`
   block-entity factory (GTCEu 7.5.3). The port can register the tank with an
   `AspectTankBlockEntity extends MetaMachineBlockEntity implements EssentiaTransport, EssentiaSource, AspectContainerView`
   that delegates to the machine. Then TC4R discovery works like for any jar/tube.
   `MetaMachineBlock::new` and `MetaMachineItem::new` are public and can fill the
   block/item factory arguments of the same overload.
3. **No direct `IAspectSource`.** The contract has to be split across the three
   TC4R interfaces (or a machine trait). Acceptance/filter semantics are
   implementation-local; `EssentiaJarView.filter()` is the closest public
   contract and is only a view.
4. **Transfer semantics differ.** TC4R transfers are directional and
   simulate/execute; upstream `addToContainer` returns the remainder and
   `takeFromContainer` a boolean. The port must adapt (`SIMULATE` first to
   reserve space, then `EXECUTE`), including the tube "suction" rules if the tank
   is to be drained by tubes.
5. **Client side is not portable as-is.** Upstream GUI/TESR classes live in
   `client/**` (`QuantumAspectTankMainWidget`, `POTextures`,
   `AspectStorageRenderer`) and use 1.12 GTCE APIs. GTCEu 7.5.3 needs an
   LDLib/fancy UI and a BER; TC4R `AspectDefinition.texture()/color()` provides
   the icons/colors.
6. **Item interactions** map well (`EssentiaContainerApi` covers jars and
   phials), except "tank in tank" item stacking and creative pick-block NBT,
   which are custom and can be dropped in a first port.
7. **Registration/recipes**: upstream registered 9 tiers (LV..UHV) with no
   crafting recipe found in the loaders; the port can register the same tiers in
   `common/machine/PollutionMachines.java` (allowed path) and add recipes later.
8. **`GTEssentiaHandler` should not be ported 1:1.** Its scanning/caching/FX is
   replaced by `EssentiaApi.findSource/extract`; keeping the 1.12 cache would
   duplicate native logic and the `WorldCoordinates`/`PacketHandler` types do not
   exist in TC4R.

## 5. Concrete port plan (when scheduled)

1. `common/machine/single/AspectTankMachine extends TieredMachine implements EssentiaTransport, EssentiaSource, AspectContainerView`
   - fields: `AspectId aspect`, `int amount`, `AspectId filter`, `int maxCapacity`,
     `boolean voiding`, `boolean autoOutput`, `Direction outputFacing`;
     `ManagedFieldHolder` for sync; `saveCustomPersistedData`/`loadCustomPersistedData`.
   - server tick: drain/push one operation per tick via
     `ThaumcraftApiHelper.getConnectableTransport` + `EssentiaApi.add/take`
     (output facing), radius pull via `EssentiaApi.findSource(..., EssentiaSearch.nearby(4..8))`,
     item-slot fill/drain via `EssentiaContainerApi`; honour `SIMULATE` before
     mutating state.
   - container methods: `visibleAspects()` = `AspectAmounts.of(aspect, amount)`;
     `extractEssentia`/`addEssentia`/`takeEssentia` implement filter, capacity and
     voiding exactly like upstream `addToContainer`/`takeFromContainer`.
2. `common/machine/single/AspectTankBlockEntity extends MetaMachineBlockEntity implements EssentiaTransport, EssentiaSource`
   - delegates every call to `(AspectTankMachine) getMetaMachine()`; overrides
     `isConnectable/canInputFrom/canOutputTo` to match upstream (all true on the
     single connectable side; upstream used `UP`, the port should use the
     machine facing).
3. Registration in `PollutionMachines` through the extended
   `GTRegistrate.machine(name, definitionFactory, machineFactory, MetaMachineBlock::new, MetaMachineItem::new, AspectTankBlockEntity::new)`
   overload, tiers 1..9, capacities `10_000 << (tier-1)`, names
   `aspect_tank.lv` .. `aspect_tank.uhv`, models under `block/machine/...`.
4. Client (separate client batch): BER for the tank block entity (frame + fill +
   `AspectDefinition.texture()` icon + amount text) and a fancy UI with filter
   lock, void and auto-output toggles; both follow the port's existing
   `MineralExtractorRenderer`/screen patterns. Assets are placeholders until the
   7D asset pass.
5. Tests: GameTest coverage for add/take/extract, filter and item container
   round-trip, mirror/tube interaction once the BE is registered.

## 6. Conclusion

The essentia side is **feasible and well supported** by TC4R (`EssentiaTransport`,
`EssentiaSource`, `EssentiaApi`, `EssentiaContainerApi`, `EssentiaSearch`,
`EssentiaJarView`; mirrors and alembics exist), contrary to the brief's
assumption. The real work is on the GTCEu side: a custom `MetaMachineBlockEntity`
subclass is required for TC4R to see the tank as a transport (the default
machine BE is not discoverable), plus new GUI/TESR code and the split of the
`IAspectSource` contract over three TC4R interfaces.

Because of the custom block entity and the client work, this is **not a trivial
self-contained port** and was not implemented in this task. The plan above is
the minimal viable path: the custom BE plus the storage/transfer machine is
self-contained and can be delivered before the GUI/TESR batch.
