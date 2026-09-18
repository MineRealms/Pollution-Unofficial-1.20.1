package meowmel.pollution.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import meowmel.pollution.api.magic.PollutionAspectMapping;
import meowmel.pollution.api.pollution.PollutionEngine;
import meowmel.pollution.compat.tc4r.TC4RBridge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Locale;

public final class PollutionCommand {

    private PollutionCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pollution")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("get").executes(context -> {
                    CommandSourceStack source = context.getSource();
                    double value = PollutionEngine.get(source.getLevel(), commandPos(source));
                    source.sendSuccess(() -> Component.literal(String.format("Chunk pollution: %.4f", value)), false);
                    return 1;
                }))
                .then(Commands.literal("aspects").executes(context -> {
                    CommandSourceStack source = context.getSource();
                    List<String> lines = PollutionAspectMapping.describe();
                    source.sendSuccess(() -> Component.literal("Aspect mapping (" + lines.size() + "):"), false);
                    for (String line : lines) {
                        source.sendSuccess(() -> Component.literal("  " + line), false);
                    }
                    return 1;
                }))
                .then(Commands.literal("vis")
                        .then(Commands.argument("channel", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (VisChannel channel : VisChannel.values()) {
                                        builder.suggest(channel.name().toLowerCase(Locale.ROOT));
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> visCommand(context, VisAction.SIMULATE, 64))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> visCommand(context, VisAction.EXECUTE,
                                                IntegerArgumentType.getInteger(context, "amount"))))))
                .then(Commands.literal("flux")
                        .executes(context -> fluxCommand(context, VisAction.SIMULATE, 64))
                        .then(Commands.literal("scrub")
                                .then(Commands.argument("quanta", IntegerArgumentType.integer(1))
                                        .executes(context -> fluxCommand(context, VisAction.EXECUTE,
                                                IntegerArgumentType.getInteger(context, "quanta"))))))
                .then(Commands.literal("set")
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0D)).executes(context -> {
                            CommandSourceStack source = context.getSource();
                            double amount = DoubleArgumentType.getDouble(context, "amount");
                            PollutionEngine.set(source.getLevel(), commandPos(source), amount);
                            source.sendSuccess(() -> Component.literal(String.format("Chunk pollution set to %.4f", amount)), true);
                            return 1;
                        })))
                .then(Commands.literal("add")
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0D)).executes(context -> {
                            CommandSourceStack source = context.getSource();
                            double amount = DoubleArgumentType.getDouble(context, "amount");
                            double value = PollutionEngine.add(source.getLevel(), commandPos(source), amount);
                            source.sendSuccess(() -> Component.literal(String.format("Chunk pollution: %.4f", value)), true);
                            return 1;
                        })))
                .then(Commands.literal("scrub")
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0D)).executes(context -> {
                            CommandSourceStack source = context.getSource();
                            double amount = DoubleArgumentType.getDouble(context, "amount");
                            double removed = PollutionEngine.scrub(source.getLevel(), commandPos(source), amount);
                            source.sendSuccess(() -> Component.literal(String.format("Scrubbed %.4f pollution", removed)), true);
                            return 1;
                        }))));
    }

    private static int visCommand(CommandContext<CommandSourceStack> context, VisAction action, int amount)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        VisChannel channel;
        try {
            channel = VisChannel.valueOf(StringArgumentType.getString(context, "channel").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Unknown vis channel. Expected one of: aer, terra, ignis, aqua, ordo, perditio"));
            return 0;
        }
        int result = TC4RBridge.drainVis(source.getLevel(), commandPos(source), channel, amount, action);
        String verb = action == VisAction.SIMULATE ? "Drainable" : "Drained";
        source.sendSuccess(() -> Component.literal(verb + " " + result + " " + channel.name() + " vis"), action == VisAction.EXECUTE);
        return result;
    }

    private static int fluxCommand(CommandContext<CommandSourceStack> context, VisAction action, int quanta) {
        CommandSourceStack source = context.getSource();
        int result = TC4RBridge.scrubFlux(source.getLevel(), commandPos(source), quanta, action);
        String verb = action == VisAction.SIMULATE ? "Scrubbable" : "Scrubbed";
        source.sendSuccess(() -> Component.literal(verb + " " + result + " flux (range " + TC4RBridge.FLUX_RANGE + ")"),
                action == VisAction.EXECUTE);
        return result;
    }

    private static BlockPos commandPos(CommandSourceStack source) {
        return BlockPos.containing(source.getPosition());
    }
}
