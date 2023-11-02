package org.doohaey.example.tenkinoko.util.process;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.doohaey.example.tenkinoko.util.enums.Types;

import java.util.ArrayList;

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

    public void countYes(CommandContext<ServerCommandSource> context){
        count(true, context);
    }

    public void countNo(CommandContext<ServerCommandSource> context){
        count(false, context);
    }

    private void count(boolean ballot, CommandContext<ServerCommandSource> context){
        ServerPlayerEntity voter = context.getSource().getPlayer();
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
        player.getServer().getPlayerManager().broadcast(message,false);
        showVotingResultEventually();
    }

    public boolean showVotingResultEventually() {
        Text message = this.getTextMessage();
        player.getServer().getPlayerManager().broadcast(message, false);
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
