package com.chess.engine.player;


public enum MoveStatus {
    DONE,
    ILLEGAL_MOVE,
    LEAVES_PLAYER_IN_CHECK;
    boolean isDone() {
        return this == DONE;
    }

}
