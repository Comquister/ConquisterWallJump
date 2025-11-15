package dev.conquister.walljump.api.events;

import dev.conquister.walljump.player.WPlayer;
import org.jetbrains.annotations.NotNull;

public class WallJumpResetEvent extends WallJumpEvent {

    public WallJumpResetEvent(@NotNull WPlayer who) {
        super(who);
    }

}
