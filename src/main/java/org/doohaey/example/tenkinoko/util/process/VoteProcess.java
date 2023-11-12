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

    public boolean countYes(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return count(true, context);
    }

    public boolean countNo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return count(false, context);
    }

    private boolean count(boolean ballot, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity voter = context.getSource().getPlayerOrThrow();
        if (isAvailableVoter(voter)) {
            if (ballot) {
                yesVote++;
                voter.sendMessage(tr("vote.yes"));
            } else {
                noVote++;
                voter.sendMessage(tr("vote.no"));
            }
            votedPlayers.add(voter);
            return true;
        } else {
            voter.sendMessage(tr("vote.voted"));
            return false;
        }
    }//返回是否已经投票过，如果是有效的投票再更新投票并显示更新的投票结果。

    private boolean isAvailableVoter(ServerPlayerEntity voter){
        for (ServerPlayerEntity votedPlayer : votedPlayers) {
            if (votedPlayer == voter) return false;
        }
        return true;
    }

    public void showVotingResultInProcess() {
        Text message = tr("vote.add");
        Objects.requireNonNull(player.getServer()).getPlayerManager().broadcast(message,false);
        message = getTextMessage(false);
        Objects.requireNonNull(player.getServer()).getPlayerManager().broadcast(message,false);
    }

    public boolean showVotingResultEventually() {
        Text message = this.getTextMessage(true);
        Objects.requireNonNull(player.getServer()).getPlayerManager().broadcast(message, false);
        return true;
    }

    private Text getTextMessage(boolean isEventually){
         if (isEventually) return tr("vote.result.eventually", yesVote, noVote, stringResult());
         else return tr("vote.result.process", yesVote, noVote);
    }

    private Text stringResult(){
        return  (checkResult()) ? tr("vote.passed") : tr("vote.denied");
    }

    public boolean checkResult(){
        return  (yesVote >= 2 * noVote && noVote <= 3);
    }

}
