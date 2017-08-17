package com.chess.engine.pieces;


import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.chess.engine.board.Move.*;


public class Queen extends Piece {
    private final static int[] CANDIDATE_MOVE_VECTOR_COORDINATE = {-9, -8, -7, -1, 1, 7, 8, 9};


    public Queen(int piecePosition, Alliance pieceAlliance, boolean moved) {
        super(PieceType.QUEEN, piecePosition, pieceAlliance, moved);
    }

    @Override
    public String toString() {
        return PieceType.QUEEN.toString();
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
                boolean exclusion =
                        Math.abs(row_offset) == Math.abs(column_offset)        // Bishop
                                || (row_offset == 0) != (column_offset == 0);  //  Rook

                if (candidateDestinationCoordinate >= 0 && candidateDestinationCoordinate < 64 && exclusion) {
                    final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                    if (!candidateDestinationTile.isTileOccupied()) {
                        legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
                    } else {
                        final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                        final Alliance pieceAlliance = pieceAtDestination.getPieceAlliance();

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
    public Queen movePiece(Move move) {
        return new Queen(move.getDestinationCoordinate(), move.getMovedPiece() .getPieceAlliance(), true);
    }
}
