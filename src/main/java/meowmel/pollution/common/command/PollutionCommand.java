package meowmel.pollution.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import meowmel.pollution.api.magic.PollutionAspectMapping;
import meowmel.pollution.api.pollution.PollutionEngine;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

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

    private static BlockPos commandPos(CommandSourceStack source) {
        return BlockPos.containing(source.getPosition());
    }
}
