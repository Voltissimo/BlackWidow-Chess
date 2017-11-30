package com.tests.chess.engine.board;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveStatus;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.ai.AlphaBeta;
import com.chess.engine.player.ai.MiniMax;
import com.chess.engine.player.ai.MoveStrategy;
import com.google.common.collect.Iterables;
import org.junit.Test;

import static org.junit.Assert.*;


// UH ... NO THANK YOU. YOUR UNIT TESTS DIDN'T EVEN HELP ME SORRY.
public class BoardTest {
    @Test
    public void initialBoard() {
        final Board board = Board.createStandardBoard();
        assertEquals(20, board.getCurrentPlayer().getLegalMoves().size());
        assertEquals(20, board.getCurrentPlayer().getOpponent().getLegalMoves().size());
        assertFalse(board.getCurrentPlayer().isInCheck());
        assertFalse(board.getCurrentPlayer().isInCheckMate());
        assertEquals(board.whitePlayer(), board.getCurrentPlayer());
        assertEquals(board.blackPlayer(), board.getCurrentPlayer().getOpponent());
        assertFalse(board.getCurrentPlayer().getOpponent().isInCheck());
        assertFalse(board.getCurrentPlayer().getOpponent().isInCheckMate());

        final Iterable<Piece> allPieces = Iterables.concat(board.getBlackPieces(), board.getWhitePieces());
        final Iterable<Move> allMoves = Iterables.concat(board.whitePlayer().getLegalMoves(), board.blackPlayer().getLegalMoves());
        for (final Move move : allMoves) {
            assertFalse(move.isAttack());
        }

        assertEquals(40, Iterables.size(allMoves));
        assertEquals(32, Iterables.size(allPieces));
        assertEquals(null, board.getTile(35).getPiece());
        assertEquals(35, board.getTile(35).tileCoordinate);
    }

    @Test
    // FYI this is the shortest checkmate
    public void testFoolsMateMiniMax() {
        final Board board = Board.createStandardBoard();
        final MoveTransition t1 = board.getCurrentPlayer()
                .makeMove(Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("f2"),
                        BoardUtils.getCoordinateAtPosition("f3")));
        assertTrue(t1.getMoveStatus() == MoveStatus.DONE);

        final MoveTransition t2 = t1.getBoard()
                .getCurrentPlayer()
                .makeMove(Move.MoveFactory.createMove(t1.getBoard(), BoardUtils.getCoordinateAtPosition("e7"),
                        BoardUtils.getCoordinateAtPosition("e5")));
        assertTrue(t2.getMoveStatus() == MoveStatus.DONE);

        final MoveTransition t3 = t2.getBoard()
                .getCurrentPlayer()
                .makeMove(Move.MoveFactory.createMove(t2.getBoard(), BoardUtils.getCoordinateAtPosition("g2"),
                        BoardUtils.getCoordinateAtPosition("g4")));
        assertTrue(t3.getMoveStatus() == MoveStatus.DONE);

        final MoveStrategy strategy = new MiniMax(4);
        final Move aiMove = strategy.execute(t3.getBoard());
        final Move bestMove = Move.MoveFactory.createMove(t3.getBoard(),
                BoardUtils.getCoordinateAtPosition("d8"),
                BoardUtils.getCoordinateAtPosition("h4"));

         assertEquals(aiMove, bestMove);
    }

    @Test
    public void testFoolsMateAlphaBeta() {
        final Board board = Board.createStandardBoard();
        final MoveTransition t1 = board.getCurrentPlayer()
                .makeMove(Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("f2"),
                        BoardUtils.getCoordinateAtPosition("f3")));

        final MoveTransition t2 = t1.getBoard()
                .getCurrentPlayer()
                .makeMove(Move.MoveFactory.createMove(t1.getBoard(), BoardUtils.getCoordinateAtPosition("e7"),
                        BoardUtils.getCoordinateAtPosition("e5")));

        final MoveTransition t3 = t2.getBoard()
                .getCurrentPlayer()
                .makeMove(Move.MoveFactory.createMove(t2.getBoard(), BoardUtils.getCoordinateAtPosition("g2"),
                        BoardUtils.getCoordinateAtPosition("g4")));

        final MoveStrategy strategy = new AlphaBeta(4);
        final Move aiMove = strategy.execute(t3.getBoard());
        final Move bestMove = Move.MoveFactory.createMove(t3.getBoard(),
                BoardUtils.getCoordinateAtPosition("d8"),
                BoardUtils.getCoordinateAtPosition("h4"));

        assertEquals(bestMove, aiMove);
    }

}