package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveStatus;
import com.chess.engine.player.MoveTransition;

import java.util.Collection;

public class AlphaBeta implements MoveStrategy {
    /**
     * Evaluator with alpha-beta pruning
     * (slightly) more optimized than MiniMax
     */

    private final BoardEvaluator boardEvaluator;
    private final int depth;

    public AlphaBeta(int searchDepth) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.depth = searchDepth;
    }

    @Override
    public String toString() {
        return "AlphaBeta";
    }



    @Override
    public Move execute(Board board) {
        System.out.println(board.getCurrentPlayer() + " " + toString() + " thinking with DEPTH = " + depth);

        final long startTime = System.currentTimeMillis();

        Move bestMove = null;

        int hi = Integer.MIN_VALUE;
        int lo = Integer.MAX_VALUE;
        int score;

        int numMoves = board.getCurrentPlayer().getLegalMoves().size();
        int moveCount = 0;
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            System.out.printf("move: %d / %d\n", ++moveCount, numMoves);
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus() == MoveStatus.DONE) {
                score = board.getCurrentPlayer().getAlliance().isWhite() ?
                        min(moveTransition.getBoard(), depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE)
                        :
                        max(moveTransition.getBoard(), depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (board.getCurrentPlayer().getAlliance().isWhite() && score >= hi) {
                    hi = score;
                    bestMove = move;
                } else if (board.getCurrentPlayer().getAlliance().isBlack() && score <= lo) {
                    lo = score;
                    bestMove = move;
                }
            }
        }
        final long executionTime = System.currentTimeMillis() - startTime;
        System.out.printf("Total time: %f sec\n", (float) executionTime / 1000);
        return bestMove;
    }

    private int max(Board board, int depth, int alpha, int beta) {
        Collection<Move> moves = board.getCurrentPlayer().getLegalMoves();
        if (depth == 0 || moves.size() == 0) {
            return this.boardEvaluator.evaluate(board, depth);
        }
        int maxScore = Integer.MIN_VALUE;
        for (Move move : moves) {
            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus() == MoveStatus.DONE) {
                int currentScore = min(moveTransition.getBoard(), depth - 1, alpha, beta);
                if (currentScore > maxScore) {
                    maxScore = currentScore;
                }
                if (currentScore > alpha) {
                    alpha = currentScore;
                }
                if (beta <= alpha) {
                    break; // cut off beta
                }
            }
        }
        return maxScore;
    }

    private int min(Board board, int depth, int alpha, int beta) {
        Collection<Move> moves = board.getCurrentPlayer().getLegalMoves();
        if (depth == 0 || moves.size() == 0) {
            return this.boardEvaluator.evaluate(board, depth);
        }
        int minScore = Integer.MAX_VALUE;
        for (Move move : moves) {
            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus() == MoveStatus.DONE) {
                int currentScore = max(moveTransition.getBoard(), depth - 1, alpha, beta);
                if (currentScore < minScore) {
                    minScore = currentScore;
                }
                if (currentScore < beta) {
                    beta = currentScore;
                }
                if (beta <= alpha) {
                    break;  // cut off alpha
                }
            }
        }

        return minScore;
    }
}
