package org.doohaey.example.tenkinoko.commands;

import com.imyvm.economy.EconomyMod;
import com.imyvm.economy.PlayerData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.doohaey.example.tenkinoko.util.enums.Categories;
import org.doohaey.example.tenkinoko.util.enums.Types;
import org.doohaey.example.tenkinoko.util.process.ConfirmProcess;
import org.doohaey.example.tenkinoko.util.process.CoolDownProcess;
import org.doohaey.example.tenkinoko.util.process.VoteProcess;
import org.doohaey.example.tenkinoko.util.process.Process;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.doohaey.example.tenkinoko.util.ModTranslator.tr;

public class CommandRegister implements Command<ServerCommandSource> {
    public static final int basisTicks = 600;
    public static final Commands COMMANDS = new Commands();
    public static VoteProcess VOTE;
    public static HashMap<ServerPlayerEntity, ConfirmProcess> toConfirm = new HashMap<>();
    public static HashMap<ServerPlayerEntity, VoteProcess> toVote = new HashMap<>();
    public static HashMap<ServerPlayerEntity, CoolDownProcess> toCoolDown = new HashMap<>();
    public static int price = 20;

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("tk")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .then(literal("change")
                        .executes(context -> {
                            COMMANDS.runInfo(context, Categories.ALL);
                            return 1;
                        })
                        .then(argument("category", string())
                                .suggests((context, builder) ->{
                                    builder.suggest("weather");
                                    builder.suggest("time");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String cateAux = StringArgumentType.getString(context,"category");
                                    switch (cateAux){
                                        case "weather" -> COMMANDS.runInfo(context, Categories.WEATHER);
                                        case "time" -> COMMANDS.runInfo(context, Categories.TIME);
                                        case "all" -> COMMANDS.runInfo(context, Categories.ALL);
                                        default -> {
                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                            assert player != null;
                                            player.sendMessage(tr("commands.info.exception"));
                                        }
                                    }
                                    return 1;
                                })
                        )
                        .then(argument("type",string())
                                .suggests(((context, builder) -> {
                                    builder.suggest("rainy");
                                    builder.suggest("thunderstorm");
                                    builder.suggest("sunny");
                                    builder.suggest("morning");
                                    builder.suggest("noon");
                                    builder.suggest("evening");
                                    builder.suggest("midnight");
                                 return builder.buildFuture();
                                }))
                                .executes((context -> {
                                    String typeAux = StringArgumentType.getString(context,"type");
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    PlayerData playerData = EconomyMod.data.getOrCreate(player);
                                    if (Objects.equals(typeAux, "rainy") ||
                                            Objects.equals(typeAux,"thunderstorm") ||
                                            Objects.equals(typeAux,"sunny")   ||
                                            Objects.equals(typeAux,"morning") ||
                                            Objects.equals(typeAux,"noon")    ||
                                            Objects.equals(typeAux,"evening") ||
                                            Objects.equals(typeAux,"midnight")) {
                                            if (price <= playerData.getMoney()) {
                                                if (toVote == null && toCoolDown == null) {
                                                    typeAux = typeAux.toUpperCase();
                                                    Types type;
                                                    try {
                                                        type = Types.valueOf(typeAux);
                                                    } catch (IllegalArgumentException e) {
                                                        player.sendMessage(tr("commands.confirm.exception"));
                                                        return Commands.SINGLE_SUCCESS;
                                                    }
                                                    ConfirmProcess confirmProcess = new ConfirmProcess
                                                            (player, type, basisTicks);
                                                    CommandRegister.toConfirm.put(player, confirmProcess);
                                                    player.sendMessage(tr("commands.confirm"));
                                                } else {
                                                    player.sendMessage(tr("commands.confirm.occupied"));
                                                }
                                            } else {
                                                player.sendMessage(tr("commands.lack"));
                                            }
                                        } else {
                                            player.sendMessage(tr("commands.confirm.exception"));
                                        }
                                    return Commands.SINGLE_SUCCESS;
                                }))
                        )
                )
                .then(literal("help")
                        .executes(context -> {
                            COMMANDS.runInfo(context, Categories.ALL);
                            return Commands.SINGLE_SUCCESS;
                        })
                )
                .then(literal("confirm")
                        .executes(confirm -> {
                            ServerPlayerEntity player = confirm.getSource().getPlayerOrThrow();
                            ConfirmProcess process = toConfirm.getOrDefault(player, null);

                            if (process == null){
                                player.sendMessage(tr("commands.confirm.exception.null"));
                                return Commands.SINGLE_SUCCESS;
                            }

                            VOTE = new VoteProcess(process.getPlayer(), process.getType(), basisTicks);
                            CommandRegister.toVote.put(player, VOTE);

                            return Commands.SINGLE_SUCCESS;
                        }))
                .then(literal("cancel")
                        .executes(cancel -> {
                            ServerPlayerEntity player = cancel.getSource().getPlayerOrThrow();
                            ConfirmProcess process = toConfirm.getOrDefault(player, null);

                            if (process == null) {
                                player.sendMessage(tr("commands.confirm.exception.null"));
                                return Commands.SINGLE_SUCCESS;
                            }

                            toConfirm.remove(player);
                            player.sendMessage(tr("commands.confirm.cancel"));

                            return Commands.SINGLE_SUCCESS;
                        }))
                .then(literal("yes")
                        .executes(context -> {
                            VOTE.countYes(context);
                            return Commands.SINGLE_SUCCESS;
                        }))
                .then(literal("no")
                        .executes(context -> {
                            VOTE.countNo(context);
                            return Commands.SINGLE_SUCCESS;
                        }))
        );
    }

    public static void serverTick(MinecraftServer server){
        handleServerTick(server, toConfirm);
        handleServerTick(server, toVote);
        handleServerTick(server, toCoolDown);
    }

    public static <T extends Process> void handleServerTick(MinecraftServer server, HashMap<ServerPlayerEntity, T> todo) {
        HashSet<ServerPlayerEntity> toRemove = new HashSet<>();

        for (ServerPlayerEntity player : todo.keySet()) {
            T _todo = todo.get(player);
            int time = _todo.getTimeLeft();
            if (time > 0) {
                time--;
                _todo.setTimeLeft(time);
                todo.put(player, _todo);
            } else {
                toRemove.add(player);
                if (_todo instanceof VoteProcess) {
                    executeVoteResult(server, player, (VoteProcess) _todo);
                } else if (_todo instanceof ConfirmProcess){
                    notifyTimeout(server, player);
                }
            }
        }

        for (ServerPlayerEntity player : toRemove) {
            todo.remove(player);
        }
    }

    public static void notifyTimeout(MinecraftServer server, ServerPlayerEntity player){
        player.sendMessage(tr("commands.confirm.timeout"));
    }
    public static void executeVoteResult(MinecraftServer server, ServerPlayerEntity player, VoteProcess voteProcess) {
        if (VOTE.votingResult()) {
            Types type = voteProcess.getType();
            COMMANDS.runChange(player, type);
            CoolDownProcess process = new CoolDownProcess(voteProcess.getPlayer(), voteProcess.getType(), basisTicks * 20);
            toCoolDown.put(player, process);
        }
    }
}
