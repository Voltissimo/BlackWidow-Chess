package com.chess.engine.pieces;


import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.chess.engine.board.Move.*;

public class Bishop extends Piece {
    private final static int[] CANDIDATE_MOVE_VECTOR_COORDINATE = {-9, -7, 7, 9};

    public Bishop(int piecePosition, Alliance pieceAlliance, boolean moved) {
        super(PieceType.BISHOP, piecePosition, pieceAlliance, moved);
    }

    @Override
    public String toString() {
        return PieceType.BISHOP.toString();
    }

    @Override
    public List<Move> calculateLegalMoves(Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for (int candidateCoordinateOffset : CANDIDATE_MOVE_VECTOR_COORDINATE) {
            int candidateDestinationCoordinate = this.piecePosition;

            while (true) {
                candidateDestinationCoordinate += candidateCoordinateOffset;

                int row_offset = this.piecePosition / 8 - candidateDestinationCoordinate / 8;
                int column_offset = this.piecePosition % 8 - candidateDestinationCoordinate % 8;
                boolean exclusion = Math.abs(row_offset) == Math.abs(column_offset);

                if (candidateDestinationCoordinate >= 0 && candidateDestinationCoordinate < 64 && exclusion) {
                    if (!board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                        legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
                    } else {
                        final Piece pieceAtDestination = board.getTile(candidateDestinationCoordinate).getPiece();
                        final Alliance pieceAlliance = pieceAtDestination.pieceAlliance;

                        if (this.pieceAlliance != pieceAlliance) {
                            legalMoves.add(
                                    new AttackMove(board, this, candidateDestinationCoordinate, pieceAtDestination)
                            );
                        }
                        break;  // regardless of the alliance of the piece
                    }

                } else {
                    break;
                }

            }
        }

        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Bishop movePiece(Move move) {
        return new Bishop(move.getDestinationCoordinate(), move.getMovedPiece() .getPieceAlliance(), true);
    }
}
