package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.chess.engine.board.Move.*;

public class King extends Piece {
    private static int[] CANDIDATE_MOVE_COORDINATE = {-9, -8, -7, -1, 1, 7, 8, 9};

    public King(int piecePosition, Alliance pieceAlliance, boolean moved) {
        super(PieceType.KING, piecePosition, pieceAlliance, moved);
    }

    @Override
    public String toString() {
        return PieceType.KING.toString();
    }

    @Override
    public List<Move> calculateLegalMoves(Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for (int currentCandidateOffset : CANDIDATE_MOVE_COORDINATE) {
            final int candidateDestinationCoordinate = this.piecePosition + currentCandidateOffset;

            if (candidateDestinationCoordinate >= 0 && candidateDestinationCoordinate < 64) {
                final int rowDiff = Math.abs(this.piecePosition / 8 - candidateDestinationCoordinate / 8);
                final int colDiff = Math.abs(this.piecePosition % 8 - candidateDestinationCoordinate % 8);
                if (!(rowDiff == 1 && colDiff == 1 || (rowDiff == 1 && colDiff == 0 || rowDiff == 0 && colDiff == 1))) {
                    continue;
                }

                final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                if (!candidateDestinationTile.isTileOccupied()) {
                    legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
                } else {
                    if (this.pieceAlliance != candidateDestinationTile.getPiece().getPieceAlliance()) {
                        legalMoves.add(new AttackMove(board, this, candidateDestinationCoordinate, candidateDestinationTile.getPiece()));
                    }
                }
            }
        }

        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public King movePiece(Move move) {
        return new King(move.getDestinationCoordinate(), move.getMovedPiece() .getPieceAlliance(), true);
    }
}
