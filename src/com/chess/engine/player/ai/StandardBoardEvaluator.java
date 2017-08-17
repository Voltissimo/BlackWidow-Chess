package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;

public final class StandardBoardEvaluator implements BoardEvaluator {

    private static final int CHECK_BONUS = 50/*100*/;
    private static final int CHECKMATE_BONUS = Integer.MAX_VALUE;
    private static final int DEPTH_BONUS_AMPLIFIER = 100;
    private static final int CASTLE_BONUS = 50;

    @Override
    public int evaluate(Board board, int depth) {
        return scorePlayer(board.whitePlayer(), depth)
                - scorePlayer(board.blackPlayer(), depth);
    }

    private int scorePlayer(Player player, int depth) {
        return pieceValue(player) + mobility(player) * 4/*5*/ + check(player) + checkMate(player, depth) + castle(player);
    }

    private static int castle(Player player) {
        return player.isCastled ? CASTLE_BONUS : 0;
    }

    private static int checkMate(Player player, int depth) {
        return player.getOpponent().isInCheckMate() ? CHECKMATE_BONUS * depthBonus(depth) : 0;
    }

    private static int depthBonus(int depth) {
        return depth == 0 ? 1 : DEPTH_BONUS_AMPLIFIER * depth;
    }

    private static int check(Player player) {
        return player.getOpponent().isInCheck() ? CHECK_BONUS : 0;
    }

    private static int mobility(Player player) {
        return player.getLegalMoves().size();
    }

    private static int pieceValue(Player player) {
        int pieceValueScore = 0;
        for (Piece piece : player.getActivePieces()) {
            pieceValueScore += piece.getPieceValue();
        }
        return pieceValueScore;
    }

}
