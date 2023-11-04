package org.doohaey.example.tenkinoko.util.process;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.doohaey.example.tenkinoko.util.enums.Types;

import java.util.ArrayList;
import java.util.Objects;

import static org.doohaey.example.tenkinoko.util.ModTranslator.tr;

public class VoteProcess extends Process {
    int yesVote = 1;
    int noVote = 0;
    ArrayList<ServerPlayerEntity> votedPlayers = new ArrayList<>();
    public VoteProcess(ServerPlayerEntity player, Types type, Integer timeLeft) {
        super(player, type, timeLeft);
        initialize(player);
    }
    public void initialize(ServerPlayerEntity player){
        yesVote = 1;
        noVote = 0;
        votedPlayers.clear();
        votedPlayers.add(player);
    }

    public void countYes(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        count(true, context);
    }

    public void countNo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        count(false, context);
    }

    private void count(boolean ballot, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity voter = context.getSource().getPlayerOrThrow();
        if (isAvailableVoter(voter)) {
            if (ballot) {
                yesVote++;
            } else {
                noVote++;
            }
            votedPlayers.add(voter);
        } else {
            voter.sendMessage(tr("vote.voted"));
        }
    }

    private boolean isAvailableVoter(ServerPlayerEntity voter){
        for (ServerPlayerEntity votedPlayer : votedPlayers) {
            if (votedPlayer == voter) return false;
        }
        return true;
    }

    public void showVotingResultInProcess() {
        Text message = tr("vote.add");
        Objects.requireNonNull(player.getServer()).getPlayerManager().broadcast(message,false);
        showVotingResultEventually();
    }

    public boolean showVotingResultEventually() {
        Text message = this.getTextMessage();
        Objects.requireNonNull(player.getServer()).getPlayerManager().broadcast(message, false);
        return true;
    }

    private Text getTextMessage(){
        return tr("vote.result", yesVote, noVote, stringResult());
    }

    private String stringResult(){
        return  (checkResult()) ? tr("vote.passed").toString() : tr("vote.denied").toString();
    }

    private boolean checkResult(){
        return  (yesVote >= 2 * noVote && noVote <= 3);
    }

}
