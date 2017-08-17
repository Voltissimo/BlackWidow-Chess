package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveStatus;
import com.chess.engine.player.MoveTransition;

public class MiniMax implements MoveStrategy {
    private final BoardEvaluator boardEvaluator;
    private final int depth;

    public MiniMax(int searchDepth) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.depth = searchDepth;

    }

    @Override
    public String toString() {
        return "MiniMax";
    }

    @Override
    public Move execute(Board board) {
        final long startTime = System.currentTimeMillis();

        Move bestMove = null;

        int highestValue = Integer.MIN_VALUE;
        int lowestValue = Integer.MAX_VALUE;
        int currentValue;

        System.out.println(board.getCurrentPlayer() + " thinking with DEPTH = " + depth);
        /*int numMoves = board.getCurrentPlayer().getLegalMoves().size();*/
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus() == MoveStatus.DONE) {
                currentValue = board.getCurrentPlayer().getAlliance().isWhite() ?
                        min(moveTransition.getBoard(), depth - 1)
                        :
                        max(moveTransition.getBoard(), depth - 1);
                if (board.getCurrentPlayer().getAlliance().isWhite() && currentValue >= highestValue) {
                    highestValue = currentValue;
                    bestMove = move;
                } else if (board.getCurrentPlayer().getAlliance().isBlack() && currentValue <= lowestValue) {
                    lowestValue = currentValue;
                    bestMove = move;
                }
            }
        }

        final long executionTime = System.currentTimeMillis() - startTime;
        System.out.println(executionTime);

        return bestMove;
    }

    private int min(Board board, int depth) {
        if (depth == 0 /*|| game over*/) {
            return this.boardEvaluator.evaluate(board, depth);
        }
        int lowestValue = Integer.MAX_VALUE;
        for (Move move : board.getCurrentPlayer().getLegalMoves()) {
            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus() == MoveStatus.DONE) {
                int currentValue = max(moveTransition.getBoard(), depth - 1);
                if (currentValue <= lowestValue) {
                    lowestValue = currentValue;
                }
            }
        }
        return lowestValue;
    }

    private int max(Board board, int depth) {
        if (depth == 0 /*|| game over*/) {
            return this.boardEvaluator.evaluate(board, depth);
        }
        int highestValue = Integer.MIN_VALUE;
        for (Move move : board.getCurrentPlayer().getLegalMoves()) {
            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus() == MoveStatus.DONE) {
                int currentValue = min(moveTransition.getBoard(), depth - 1);
                if (currentValue >= highestValue) {
                    highestValue = currentValue;
                }
            }
        }
        return highestValue;
    }
}
