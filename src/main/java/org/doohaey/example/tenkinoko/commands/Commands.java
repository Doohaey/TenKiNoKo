package org.doohaey.example.tenkinoko.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.doohaey.example.tenkinoko.util.enums.Categories;
import org.doohaey.example.tenkinoko.util.enums.Types;

import static org.doohaey.example.tenkinoko.util.ModTranslator.tr;

public class Commands {
    public void runInfo(CommandContext<ServerCommandSource> context, Categories category) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

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
    }

    public void runChange(ServerPlayerEntity player, Types type){
        ServerWorld serverWorld = player.getServerWorld();
        if (type == Types.RAINY || type == Types.THUNDERSTORM || type == Types.CLEAR) {
            runChangeWeather(type, serverWorld);
        } else {
            runChangeTime(type, serverWorld);
        }
    }

    private void runChangeWeather(Types type, ServerWorld serverWorld) {
        switch (type) {
            case RAINY -> serverWorld.setWeather(0,12000,true,false);
            case THUNDERSTORM -> serverWorld.setWeather(0,12000,true,true);
            case CLEAR -> serverWorld.setWeather(12000,0,false,false);
        }
    }

    private void runChangeTime(Types type, ServerWorld serverWorld){
        long time = (serverWorld.getTime() / 24000) * 24000;
        switch (type) {
            case MORNING -> time += 1000;
            case NOON -> time += 6000;
            case EVENING -> time += 11000;
            case MIDNIGHT -> time += 18000;
        }
        serverWorld.setTimeOfDay(time);

    }
}