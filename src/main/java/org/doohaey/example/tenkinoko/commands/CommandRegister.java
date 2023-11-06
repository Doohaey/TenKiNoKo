package org.doohaey.example.tenkinoko.commands;

import com.imyvm.economy.EconomyMod;
import com.imyvm.economy.PlayerData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.doohaey.example.tenkinoko.util.ModConfig;
import org.doohaey.example.tenkinoko.util.enums.Categories;
import org.doohaey.example.tenkinoko.util.enums.Types;
import org.doohaey.example.tenkinoko.util.process.ConfirmProcess;
import org.doohaey.example.tenkinoko.util.process.CoolDownProcess;
import org.doohaey.example.tenkinoko.util.process.VoteProcess;
import org.doohaey.example.tenkinoko.util.process.Process;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import static net.minecraft.server.command.CommandManager.literal;
import static org.doohaey.example.tenkinoko.util.ModTranslator.tr;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class CommandRegister {
    public static final int basisTicks = 600;
    public static final Commands COMMANDS = new Commands();
    public static VoteProcess VOTE;
    public static HashMap<ServerPlayerEntity, ConfirmProcess> toConfirm = new HashMap<>();
    public static HashMap<ServerPlayerEntity, VoteProcess> toVote = new HashMap<>();
    public static HashMap<ServerPlayerEntity, CoolDownProcess> toCoolDown = new HashMap<>();
    public static long price = (long) (20 * (1 + ModConfig.TAX_RESTOCK.getValue()));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("tk")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .then(buildChangeCommand())
                .then(literal("help")
                        .executes(context -> {
                            COMMANDS.runInfo(context, Categories.ALL);
                            return SINGLE_SUCCESS;
                        })
                )
                .then(literal("confirm")
                        .executes(confirm -> {
                            ServerPlayerEntity player = confirm.getSource().getPlayerOrThrow();
                            ConfirmProcess process = toConfirm.getOrDefault(player, null);

                            if (process == null){
                                player.sendMessage(tr("commands.confirm.exception.null"));
                                return SINGLE_SUCCESS;
                            }

                            VOTE = new VoteProcess(process.getPlayer(), process.getType(), basisTicks);
                            CommandRegister.toVote.put(player, VOTE);
                            toConfirm.clear();

                            return SINGLE_SUCCESS;
                        }))
                .then(literal("cancel")
                        .executes(cancel -> {
                            ServerPlayerEntity player = cancel.getSource().getPlayerOrThrow();
                            ConfirmProcess process = toConfirm.getOrDefault(player, null);

                            if (process == null) {
                                player.sendMessage(tr("commands.confirm.exception.null"));
                                return SINGLE_SUCCESS;
                            }

                            toConfirm.remove(player);
                            player.sendMessage(tr("commands.confirm.cancel"));

                            return SINGLE_SUCCESS;
                        }))
                .then(literal("yes")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            if (!toVote.isEmpty()) {
                                VOTE.countYes(context);
                                player.sendMessage(tr("vote.success"));
                                VOTE.showVotingResultInProcess();
                            } else {
                                player.sendMessage(tr("vote.null"));
                            }
                            return SINGLE_SUCCESS;
                        }))
                .then(literal("no")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            if (!toVote.isEmpty()) {
                                VOTE.countNo(context);
                                player.sendMessage(tr("vote.no"));
                                VOTE.showVotingResultInProcess();
                            } else {
                                player.sendMessage(tr("vote.null"));
                            }
                            return SINGLE_SUCCESS;
                        }))
                .then(literal("passCoolDown")
                        .requires(source -> source.hasPermissionLevel(4))
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            toCoolDown.clear();
                            Text message = tr("cd.clear");
                            Objects.requireNonNull(player.getServer()).getPlayerManager().broadcast(message,false);
                            return SINGLE_SUCCESS;
                        }))
        );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildChangeCommand() {
        LiteralArgumentBuilder<ServerCommandSource> change = literal("change");
        for (Types type : Types.values()) {
            String subCommandName = type.name().toLowerCase();
            change.then(literal(subCommandName)
                    .executes(context -> {
                        handleConfirm(context, type);
                        return SINGLE_SUCCESS;
                    }));
        }
        return change;
    }
    public static void handleConfirm(CommandContext<ServerCommandSource> context, Types type) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        PlayerData playerData = EconomyMod.data.getOrCreate(player);

        if (price <= playerData.getMoney()) {
            if (toVote.isEmpty() && toCoolDown.isEmpty()) {
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
    }
    public static void serverTick(){
        handleServerTick(toConfirm);
        handleServerTick(toVote);
        handleServerTick(toCoolDown);
    }

    protected static <T extends Process> void handleServerTick(HashMap<ServerPlayerEntity, T> todo) {
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
                    executeVoteResult(player, (VoteProcess) _todo);
                } else if (_todo instanceof ConfirmProcess){
                    notifyTimeoutConfirm(player);
                } else if (_todo instanceof CoolDownProcess) {
                    notifyTimeoutCoolDown(player);
                }
            }
        }

        for (ServerPlayerEntity player : toRemove) {
            todo.remove(player);
        }
    }

    private static void notifyTimeoutConfirm(ServerPlayerEntity player){
        player.sendMessage(tr("commands.confirm.timeout"));
    }

    private static void notifyTimeoutCoolDown(ServerPlayerEntity player){
        player.sendMessage(tr("commands.cd.timeout"));
    }
    private static void executeVoteResult(ServerPlayerEntity player, VoteProcess voteProcess) {
        boolean passed = VOTE.showVotingResultEventually();
        if (passed) {
            Types type = voteProcess.getType();
            COMMANDS.runChange(player, type);
            CoolDownProcess process = new CoolDownProcess(voteProcess.getPlayer(), voteProcess.getType(), basisTicks * 20);
            toCoolDown.put(player, process);

            Text message = getTextMessage(player, type);
            PlayerData sourceData = EconomyMod.data.getOrCreate(player);
            sourceData.addMoney(-price);
            Objects.requireNonNull(player.getServer()).getPlayerManager().broadcast(message,false);
        }
    }

    private static Text getTextMessage(ServerPlayerEntity player, Types types){
        return tr("money.cost", player, types, price);
    }
}
