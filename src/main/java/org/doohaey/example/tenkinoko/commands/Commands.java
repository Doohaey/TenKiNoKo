package org.doohaey.example.tenkinoko.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.doohaey.example.tenkinoko.util.enums.Categories;
import org.doohaey.example.tenkinoko.util.enums.Types;

import static org.doohaey.example.tenkinoko.util.ModTranslator.tr;

public class Commands implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
    public int runInfo(CommandContext<ServerCommandSource> context, Categories category){
        ServerPlayerEntity player = context.getSource().getPlayer();

        switch (category){
            case ALL:
                player.sendMessage(tr("commands.info.category.header"));
                player.sendMessage(tr("commands.info.category.weathers"));
            case TIME:
                player.sendMessage(tr("commands.info.category.times"));
                break;
            case WEATHER:
                player.sendMessage(tr("commands.info.category.weathers"));
        }
        return Commands.SINGLE_SUCCESS;
    }

    public int runChange(ServerPlayerEntity player, Types type){
        ServerWorld serverWorld = player.getServerWorld();
        if (type == Types.RAINY || type == Types.THUNDERSTORM || type == Types.CLEAR) {
            return runChangeWeather(player, type, serverWorld);
        } else {
            return runChangeTime(player, type, serverWorld);
        }
    }

    private int runChangeWeather(ServerPlayerEntity player, Types type, ServerWorld serverWorld) {
        switch (type) {
            case RAINY -> serverWorld.setWeather(0,12000,true,false);
            case THUNDERSTORM -> serverWorld.setWeather(0,12000,true,true);
            case CLEAR -> serverWorld.setWeather(12000,0,false,false);
        }
        return Commands.SINGLE_SUCCESS;
    }

    private int runChangeTime(ServerPlayerEntity player, Types type, ServerWorld serverWorld){
        long time = serverWorld.getTime();
        switch (type) {
            case MORNING -> time = 1000;
            case NOON -> time = 6000;
            case EVENING -> time = 11000;
            case MIDNIGHT -> time = 18000;
        }
        serverWorld.setTimeOfDay(time);

        return Commands.SINGLE_SUCCESS;
    }
}