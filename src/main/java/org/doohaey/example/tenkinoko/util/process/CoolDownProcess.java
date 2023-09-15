package org.doohaey.example.tenkinoko.util.process;

import net.minecraft.server.network.ServerPlayerEntity;
import org.doohaey.example.tenkinoko.util.enums.Types;

public class CoolDownProcess extends Process {
    public CoolDownProcess(ServerPlayerEntity player, Types type, Integer timeLeft) {
        super(player, type, timeLeft);
    }
}
