package meowmel.pollution.common.lib;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import dev.tc4port.thaumcraft.api.aspect.AspectApi;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.essentia.EssentiaApi;
import dev.tc4port.thaumcraft.api.essentia.EssentiaSearch;
import dev.tc4port.thaumcraft.api.essentia.EssentiaSourceRef;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransferMode;
import dev.tc4port.thaumcraft.api.essentia.EssentiaTransport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Essentia push/pull helpers for Pollution machines, rewritten against the
 * Thaumcraft 4R essentia API.
 *
 * <p>Upstream (1.12.2) {@code GTEssentiaHandler} operated on the Thaumcraft 6
 * {@code IAspectSource} contract, kept its own radius cache
 * ({@code HashMap<WorldCoordinates, ArrayList<WorldCoordinates>>} with a 10 s
 * delay map) and sent a {@code PacketFXEssentiaSource} after every transfer.
 * TC4R splits that contract over {@code EssentiaTransport} (directional
 * add/take), {@code EssentiaSource} (face-less extract) and
 * {@code AspectContainerView} (display), provides a native search
 * ({@code EssentiaApi.findSource}/{@code extract} + {@code EssentiaSearch}) that
 * is mirror-aware and emits its own FX, and uses {@code EssentiaSourceRef}
 * instead of {@code WorldCoordinates}. This class is therefore a semantic port,
 * not a line-by-line one; every deviation is listed below.</p>
 *
 * <p>API mapping (upstream feature -> TC4R API used):</p>
 * <table>
 *   <caption>feature mapping</caption>
 *   <tr><td>{@code addEssentiaToTile}</td>
 *       <td>{@link ThaumcraftApiHelper#getConnectableTransport} +
 *           {@link EssentiaApi#add}</td></tr>
 *   <tr><td>{@code addEssentiaToMTE}</td>
 *       <td>{@link MetaMachine#getMachine} + {@link EssentiaApi#add} (GTCEu
 *           machines have no block entity, so TC4R cannot discover them)</td></tr>
 *   <tr><td>{@code addEssentia} radius scan + 10 s cache</td>
 *       <td>{@link EssentiaApi#findSource} + {@link EssentiaApi#extract} with
 *           {@link EssentiaSearch#nearby(int)}</td></tr>
 *   <tr><td>{@code ignoreMirror} flag</td>
 *       <td>dropped: the native search is mirror-aware and the upstream tank
 *           always passed {@code false}</td></tr>
 *   <tr><td>{@code ext} FX range + {@code PacketFXEssentiaSource}</td>
 *       <td>dropped: TC4R emits its own essentia FX from the native APIs</td></tr>
 *   <tr><td>{@code WorldCoordinates} cache key</td>
 *       <td>{@link EssentiaSourceRef}</td></tr>
 * </table>
 *
 * <p>Deviations:</p>
 * <ul>
 *   <li>Every transfer takes an {@link EssentiaTransferMode}. Callers that only
 *       want to know whether a transfer would succeed must pass
 *       {@link EssentiaTransferMode#SIMULATE}.</li>
 *   <li>{@code addEssentiaToTile} takes a {@link Level} + {@link BlockPos}
 *       instead of a 1.12 {@code MetaTileEntity}, and a {@code facing} that is
 *       the direction from the source block to the neighbour (the helper asks
 *       for the neighbour's face facing back, exactly like TC4R tubes do).</li>
 *   <li>The radius method ({@code addEssentia} upstream) was a <em>push</em>
 *       into the nearest {@code IAspectSource}; the native TC4R equivalent
 *       searches {@code EssentiaSource}s, which can only be <em>pulled</em>
 *       from, so {@link #pullEssentiaFromNearby} is a pull. Upstream's
 *       distance-sorted cache is not ported: {@code EssentiaSearch} already
 *       returns the nearest valid source and the search is cheap enough at the
 *       tank's call rate (one query per tick).</li>
 *   <li>The upstream helper was dead code for the radius scan (only the tank's
 *       per-face push used it). The pull variant is not wired to the tank
 *       either - the tank's input is the jar-style tube drain - so it is kept
 *       for machines that want a radius pull.</li>
 *   <li>Aspect ids are validated with {@link AspectApi#contains} before any
 *       {@link EssentiaApi} call, because the native API throws
 *       {@link IllegalArgumentException} for unregistered aspects.</li>
 * </ul>
 */
public final class GTEssentiaHandler {

    /** Radius used by the native essentia search when a machine pulls. */
    public static final int DEFAULT_SEARCH_RANGE = 8;

    private GTEssentiaHandler() {}

    /**
     * Upstream {@code addEssentiaToTile}: inserts essentia into the TC4R
     * transport occupying {@code pos.relative(facing)} (jar, tube, reservoir,
     * another aspect tank, ...). Returns the amount actually inserted.
     *
     * @param pos    the source block; {@link ThaumcraftApiHelper#getConnectableTransport}
     *               resolves the neighbour itself, so this must not be
     *               pre-offset by {@code facing}
     * @param facing direction from {@code pos} to the target block; the target's
     *               own face is {@code facing.getOpposite()}
     */
    public static int addEssentiaToTile(Level level, BlockPos pos, AspectId aspect, Direction facing,
                                        int amount, EssentiaTransferMode mode) {
        if (amount <= 0 || !isKnown(aspect)) {
            return 0;
        }
        EssentiaTransport transport = ThaumcraftApiHelper.getConnectableTransport(level, pos, facing);
        if (transport == null) {
            return 0;
        }
        return EssentiaApi.add(level, transport, aspect, amount, facing.getOpposite(), mode);
    }

    /**
     * Upstream {@code addEssentiaToMTE}: inserts essentia into a GTCEu
     * {@link MetaMachine} that itself implements {@link EssentiaTransport}.
     *
     * <p>TC4R discovers transports through the block entity, and a
     * {@code MetaMachine} is not one, so this branch is invisible to tubes,
     * golems and mirrors. It is kept for parity with the upstream helper and for
     * machines that expose the contract directly (the port's aspect tank
     * delegates through a custom block entity, so the tile branch above already
     * covers it).</p>
     */
    public static int addEssentiaToMachine(Level level, BlockPos pos, AspectId aspect, Direction facing,
                                           int amount, EssentiaTransferMode mode) {
        if (amount <= 0 || !isKnown(aspect)) {
            return 0;
        }
        MetaMachine machine = MetaMachine.getMachine(level, pos.relative(facing));
        if (!(machine instanceof EssentiaTransport transport)) {
            return 0;
        }
        return EssentiaApi.add(level, transport, aspect, amount, facing.getOpposite(), mode);
    }

    /**
     * Upstream {@code addEssentia}: finds the nearest essentia source in
     * {@code range} blocks and transfers up to {@code amount} of {@code aspect}
     * out of it. The native search is dimension-checked, mirror-aware and sorted
     * by distance; the origin position itself is excluded from the candidates.
     * Returns the amount moved, or - with {@link EssentiaTransferMode#SIMULATE} -
     * the amount that would be moved without mutating the source.
     */
    public static int pullEssentiaFromNearby(Level level, BlockPos pos, AspectId aspect, int amount, int range,
                                             EssentiaTransferMode mode) {
        if (amount <= 0 || range <= 0 || !isKnown(aspect)) {
            return 0;
        }
        EssentiaSearch search = EssentiaSearch.nearby(range);
        Optional<EssentiaSourceRef> found = EssentiaApi.findSource(level, pos, aspect, amount, search,
                source -> !source.position().equals(pos));
        if (found.isEmpty()) {
            return 0;
        }
        EssentiaSourceRef source = found.get();
        int available = EssentiaApi.extract(level, source, aspect, amount, EssentiaTransferMode.SIMULATE);
        if (available <= 0) {
            return 0;
        }
        if (!mode.executes()) {
            return available;
        }
        return EssentiaApi.extract(level, source, aspect, available, EssentiaTransferMode.EXECUTE);
    }

    private static boolean isKnown(AspectId aspect) {
        return aspect != null && AspectApi.contains(aspect);
    }
}
