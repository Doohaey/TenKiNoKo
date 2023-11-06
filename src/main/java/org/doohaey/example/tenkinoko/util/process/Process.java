package org.doohaey.example.tenkinoko.util.process;

import net.minecraft.server.network.ServerPlayerEntity;
import org.doohaey.example.tenkinoko.util.enums.Types;

public abstract class Process {
        ServerPlayerEntity player;
        Types type;
        Integer timeLeft;
        public Process(ServerPlayerEntity player, Types type, Integer timeLeft){
            this.player = player;
            this.type = type;
            this.timeLeft = timeLeft;
        }
        public ServerPlayerEntity getPlayer() {
            return this.player;
        }

        public Types getType() {
            return this.type;
        }

        public Integer getTimeLeft() {
            return this.timeLeft;
        }

        public void setPlayer(ServerPlayerEntity player) {
            this.player = player;
        }

        public void setType(Types type) {
            this.type = type;
        }

        public void setTimeLeft(Integer timeLeft) {
            this.timeLeft = timeLeft;
        }
}
