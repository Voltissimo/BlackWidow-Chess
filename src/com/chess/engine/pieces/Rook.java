package com.chess.engine.pieces;


import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.chess.engine.board.Move.*;

public class Rook extends Piece {
    private int[] CANDIDATE_MOVE_VECTOR_COORDINATE = {-8, -1, 1, 8};

    public Rook(int piecePosition, Alliance pieceAlliance, boolean moved) {
        super(PieceType.ROOK, piecePosition, pieceAlliance, moved);
    }

    @Override
    public String toString() {
        return PieceType.ROOK.toString();
    }

    @Override
    public List<Move> calculateLegalMoves (Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for (int candidateCoordinateOffset : CANDIDATE_MOVE_VECTOR_COORDINATE) {
            int candidateDestinationCoordinate = this.piecePosition;

            while (true) {
                candidateDestinationCoordinate += candidateCoordinateOffset;

                int row_offset = this.piecePosition / 8 - candidateDestinationCoordinate / 8;
                int column_offset = this.piecePosition % 8 - candidateDestinationCoordinate % 8;
                boolean exclusion = (row_offset == 0) != (column_offset == 0);  // logical XOR (exclusive OR)

                if (candidateDestinationCoordinate >= 0 && candidateDestinationCoordinate < 64 && exclusion) {
                    final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                    if (!candidateDestinationTile.isTileOccupied()) {
                        legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
                    } else {
                        final Piece pieceAtDestination = candidateDestinationTile.getPiece();
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
    public Rook movePiece(Move move) {
        return new Rook(move.getDestinationCoordinate(), move.getMovedPiece() .getPieceAlliance(), true);
    }
}
