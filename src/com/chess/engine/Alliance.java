package com.chess.engine;


import com.chess.engine.player.BlackPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.WhitePlayer;

public enum Alliance {
    WHITE,
    BLACK;

    public int getDirection() {
        return this == BLACK ? 1 : -1;

    }

    public boolean isBlack() {
        return this == BLACK;
    }

    public boolean isWhite() {
        return this == WHITE;
    }

    public Player choosePlayer(WhitePlayer whitePlayer, BlackPlayer blackPlayer) {
        return this == BLACK ? blackPlayer : whitePlayer;
    }

    public boolean isPawnPromotionSquare(int position) {
        return this == WHITE ? (position / 8 == 0) : (position / 8 == 7);
    }
}
