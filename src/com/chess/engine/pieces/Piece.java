package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.List;

public abstract class Piece {

    private final PieceType pieceType;
    final int piecePosition;
    final Alliance pieceAlliance;
    private final boolean moved;
    private final int cachedHashCode;

    private int hash() {
        return 31 * (31 * (31 * pieceType.hashCode()) + pieceAlliance.hashCode()) + piecePosition;
    }

    public Piece(final PieceType pieceType, final int piecePosition, final Alliance pieceAlliance, final boolean moved) {
        this.moved = moved;
        this.pieceType = pieceType;
        this.piecePosition = piecePosition;
        this.pieceAlliance = pieceAlliance;
        this.cachedHashCode = hash();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Piece)) {
            return false;
        }
        final Piece otherPiece = (Piece) other;
        return pieceAlliance == otherPiece.getPieceAlliance()
                && pieceType == otherPiece.getPieceType()
                && piecePosition == otherPiece.getPiecePosition();
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }

    public abstract List<Move> calculateLegalMoves(final Board board);

    public abstract Piece movePiece(Move move);

    public Alliance getPieceAlliance() {
        return this.pieceAlliance;

    }

    public boolean isMoved() {
        return this.moved;
    }

    public PieceType getPieceType() {
        return this.pieceType;
    }

    public int getPieceValue() {
        return this.getPieceType().pieceValue;
    }

    public enum PieceType {

        PAWN("P", 100),
        KNIGHT("N", 300),
        BISHOP("B", 300),
        ROOK("R", 500),
        QUEEN("Q", 900),
        KING("K", 10000);

        private int pieceValue;
        private String pieceName;

        PieceType(String pieceName, int pieceValue) {
            this.pieceName = pieceName;
            this.pieceValue = pieceValue;
        }

        @Override
        public String toString() {
            return this.pieceName;
        }

        public boolean isKing() {
            return this == KING;
        }
    }

    public int getPiecePosition() {
        return this.piecePosition;
    }

}
