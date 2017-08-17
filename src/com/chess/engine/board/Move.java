package com.chess.engine.board;


import com.chess.engine.pieces.Pawn;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;

import static com.chess.engine.board.Board.*;

public abstract class Move {
    final Board board;
    final Piece movedPiece;
    final int destinationCoordinate;

    @Override
    public int hashCode() {
        return 31 * this.destinationCoordinate + this.movedPiece.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Move)) {
            return false;
        }
        Move otherMove = (Move) other;
        return getCurrentCoordinate() == otherMove.getCurrentCoordinate()
                && getDestinationCoordinate() == otherMove.getDestinationCoordinate()
                && getMovedPiece().equals(otherMove.getMovedPiece());
    }

    private Move(final Board board, final Piece movedPiece, final int destinationCoordinate) {
        this.board = board;
        this.movedPiece = movedPiece;
        this.destinationCoordinate = destinationCoordinate;
    }

    public int getCurrentCoordinate() {
        return this.movedPiece.getPiecePosition();
    }

    public int getDestinationCoordinate() {
        return destinationCoordinate;
    }

    public Piece getMovedPiece() {
        return this.movedPiece;
    }

    public boolean isAttack() {
        return false;
    }

    public boolean isCastlingMove() {
        return false;
    }

    public Piece getAttackedPiece() {
        return null;
    }

    public Board execute() {
        final Builder builder = new Builder();

        for (Piece piece : this.board.getCurrentPlayer().getActivePieces()) {
            if (!this.movedPiece.equals(piece)) {
                builder.setPiece(piece);
            } else {
                builder.setPiece(piece.movePiece(this));
            }
        }

        for (Piece piece : this.board.getCurrentPlayer().getOpponent().getActivePieces()) {
            builder.setPiece(piece);
        }
        builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());

        return builder.build();
    }

    public static final class MajorMove extends Move {

        public MajorMove(final Board board, final Piece movedPiece, final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public String toString() {
            return movedPiece.getPieceType().toString() + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }

    }


    public static final class PawnMove extends Move {

        public PawnMove(Board board, Piece movedPiece, int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }


    public static final class PawnJump extends Move {

        public PawnJump(Board board, Piece movedPiece, int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public Board execute() {
            Builder builder = new Builder();
            for (Piece piece : this.board.getCurrentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece)) {
                    builder.setPiece(piece);
                }
            }

            for (Piece piece : this.board.getCurrentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }

            final Pawn movedPawn = (Pawn) this.movedPiece.movePiece(this);
            builder.setPiece(movedPawn);
            builder.setEnPassantPawn(movedPawn);
            builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());

            return builder.build();
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static class PawnPromotion extends Move {

        final Move decoratedMove;
        final Pawn promotedPawn;

        public PawnPromotion(final Move decoratedMove) {
            super(decoratedMove.board, decoratedMove.movedPiece, decoratedMove.destinationCoordinate);
            this.decoratedMove= decoratedMove;
            this.promotedPawn = (Pawn) decoratedMove.getMovedPiece();
        }

        @Override
        public String toString() {
            return decoratedMove.toString();
        }

        @Override
        public Board execute() {
            final Board pawnMovedBoard = this.decoratedMove.execute();
            final Builder builder = new Builder();
            for (final Piece piece : pawnMovedBoard.getCurrentPlayer().getActivePieces()) {
                if (!this.promotedPawn.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (final Piece piece : pawnMovedBoard.getCurrentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }
            builder.setPiece(this.promotedPawn.getPromotionPiece().movePiece(this));
            builder.setMoveMaker(pawnMovedBoard.getCurrentPlayer().getAlliance());
            return builder.build();
        }

        @Override
        public Piece getAttackedPiece() {
            return this.decoratedMove.getAttackedPiece();
        }

        /*private PawnPromotion(Board board, int destinationCoordinate) {
            super(board, destinationCoordinate);
        }*/
    }


    static abstract class CastleMove extends Move {

        private Rook castleRook;
        private int castleRookDestination;

        private CastleMove(Board board,
                           Piece movedPiece,
                           int destinationCoordinate,
                           Rook castleRook,
                           int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate);
            this.castleRook = castleRook;
            this.castleRookDestination = castleRookDestination;
        }

        @Override
        public Board execute() {
            Builder builder = new Builder();
            for (final Piece piece : this.board.getCurrentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece) && !this.castleRook.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (final Piece piece : this.board.getCurrentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }
            builder.setPiece(this.movedPiece.movePiece(this));  // King
            builder.setPiece(new Rook(this.castleRookDestination, this.castleRook.getPieceAlliance(), true));
            builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());
            Board newBoard = builder.build();
            newBoard.getCurrentPlayer().getOpponent().isCastled = true;
            return newBoard;
        }

        @Override
        public boolean isCastlingMove() {
            return true;
        }

    }

    public static final class KingSideCastleMove extends CastleMove {

        public KingSideCastleMove(Board board,
                                  Piece movedPiece,
                                  int destinationCoordinate,
                                  Rook castleRook,
                                  int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookDestination);
        }

        @Override
        public String toString() {
            return "O-O";
        }

    }


    public static final class QueenSideCastleMove extends CastleMove {

        public QueenSideCastleMove(Board board,
                                   Piece movedPiece,
                                   int destinationCoordinate,
                                   Rook castleRook,
                                   int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookDestination);
        }

        @Override
        public String toString() {
            return "O-O-O";
        }
    }

    public static class AttackMove extends Move {
        final Piece attackedPiece;

        public AttackMove(
                final Board board,
                final Piece movedPiece,
                final int destinationCoordinate,
                final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate);
            this.attackedPiece = attackedPiece;
        }

        @Override
        public Board execute() {
            final Builder builder = new Builder();

            for (Piece piece : this.board.getCurrentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (Piece piece : this.board.getCurrentPlayer().getOpponent().getActivePieces()) {
                if (!this.attackedPiece.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());

            return builder.build();
        }

        @Override
        public boolean isAttack() {
            return true;
        }

        @Override
        public Piece getAttackedPiece() {
            return this.attackedPiece;
        }

        @Override
        public String toString() {
            return movedPiece.getPieceType().toString() + "x" + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static class PawnAttackMove extends AttackMove {

        public PawnAttackMove(Board board, Piece movedPiece, int destinationCoordinate, Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.getMovedPiece().getPiecePosition()).substring(0, 1)
                    + "x"
                    + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static final class PawnEnPassantAttackMove extends PawnAttackMove {

        public PawnEnPassantAttackMove(Board board, Piece movedPiece, int destinationCoordinate, Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public int getCurrentCoordinate() {
            return this.movedPiece.getPiecePosition();
        }

        @Override
        public int getDestinationCoordinate() {
            return this.destinationCoordinate;
        }

        @Override
        public Board execute() {
            final Builder builder = new Builder();

            for (Piece piece : this.board.getCurrentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece)) {
                    builder.setPiece(piece);
                }
            }

            for (Piece piece : this.board.getCurrentPlayer().getOpponent().getActivePieces()) {
                if (this.attackedPiece.equals(piece)) {
                    continue;
                }
                builder.setPiece(piece);
            }
            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());

            return builder.build();
        }

    }

    public static final class NullMove extends Move {

        NullMove() {
            super(null, null, -1);
        }

        @Override
        public Board execute() throws RuntimeException {
            throw new RuntimeException("Null move cannot be executed");
        }
    }


    public static class MoveFactory {


        private MoveFactory() {
            throw new RuntimeException("Not instantiable");
        }

        public static Move createMove(Board board, int currentCoordinate, int destinationCoordinate) {
            for (Move move : board.getAllLegalMoves()) {
                /*System.out.println(String.valueOf(currentCoordinate) + " => " + String.valueOf(destinationCoordinate));*/
                if (move.getCurrentCoordinate() == currentCoordinate
                        && move.getDestinationCoordinate() == destinationCoordinate) {
                    return move;
                }
            }
            return new NullMove();
        }
    }
}
