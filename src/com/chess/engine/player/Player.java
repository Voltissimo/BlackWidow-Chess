package com.chess.engine.player;


import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Player {

    protected final Board board;
    final King playerKing;
    private final Collection<Move> legalMoves;
    private boolean isInCheck;
    public boolean isCastled;

    Player(final Board board,
           final Collection<Move> legalMoves,
           final Collection<Move> opponentLegalMoves) {
        this.board = board;
        this.playerKing = establishKing();
        this.legalMoves = ImmutableList.copyOf(Iterables.concat(legalMoves, calculateKingCastles(legalMoves, opponentLegalMoves)));
        this.isInCheck = !Player.calculateAttacksOnTile(this.playerKing.getPiecePosition(), opponentLegalMoves).isEmpty();
        this.isCastled = false;
    }

    private King getPlayerKing() {
        return playerKing;
    }

    public Collection<Move> getLegalMoves() {
        return legalMoves;
    }

    private static Collection<Move> calculateAttacksOnTile(int piecePosition, Collection<Move> moves) {
        final List<Move> attackMoves = new ArrayList<>();
        for (final Move move : moves) {
            if (piecePosition == move.getDestinationCoordinate()) {
                attackMoves.add(move);
            }
        }
        return ImmutableList.copyOf(attackMoves);
    }


    private King establishKing() {
        for (final Piece piece : getActivePieces()) {
            if (piece.getPieceType().isKing()) {
                return (King) piece;
            }
        }
        throw new RuntimeException("Invalid board");
    }

    private boolean isMoveLegal(Move move) {
        return !(move instanceof Move.NullMove) && this.legalMoves.contains(move);
    }


    public boolean isInCheckMate() {
        return this.isInCheck && !hasEscapeMoves();
    }

    public boolean isInCheck() {
        return this.isInCheck && hasEscapeMoves();
    }

    private boolean hasEscapeMoves() {
        for (Move move : this.legalMoves) {
            MoveTransition transition = makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                return true;
            }
        }
        return false;
    }

    public boolean isInStaleMate() {
        return !this.isInCheck && !hasEscapeMoves();
    }

    public MoveTransition makeMove(Move move) {
        if (!isMoveLegal(move)) {
            return new MoveTransition(this.board, /*move, */MoveStatus.ILLEGAL_MOVE);
        }

        Board transitionBoard = move.execute();

        Collection<Move> kingAttacks = Player.calculateAttacksOnTile(
                transitionBoard.getCurrentPlayer().getOpponent().getPlayerKing().getPiecePosition(),
                transitionBoard.getCurrentPlayer().getLegalMoves()
        );

        if (!kingAttacks.isEmpty()) {
            return new MoveTransition(this.board, /*move, */MoveStatus.LEAVES_PLAYER_IN_CHECK);
        }

        return new MoveTransition(transitionBoard, /*move, */MoveStatus.DONE);
    }

    public boolean isKingSideCastleAvailable() {
        return this.playerKing.isKingSideCastleAvailable();
    }

    public boolean isQueenSideCastleAvailable() {
        return this.playerKing.isQueenSideCastleAvailable();
    }

    private boolean checkTransitionMoves(List<Integer> transitionTilesCoordinates, Collection<Move> opponentMoves) {
        for (int tileCoordinate : transitionTilesCoordinates) {
            if (this.board.getTile(tileCoordinate).isTileOccupied()
                    || !calculateAttacksOnTile(tileCoordinate, opponentMoves).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    boolean checkCastlingEligible(
            int kingCoordinate,
            int rookCoordinate,
            Collection<Move> opponentMoves) {
        Tile kingTile = this.board.getTile(kingCoordinate);
        Tile rookTile = this.board.getTile(rookCoordinate);

        List<Integer> transitionTilesCoordinates = new ArrayList<>();
        int lo, hi;
        if (rookCoordinate < kingCoordinate) {
            lo = rookCoordinate;
            hi = kingCoordinate;
        } else {
            lo = kingCoordinate;
            hi = rookCoordinate;
        }
        for (int coordinate = lo + 1; coordinate < hi; coordinate++) {
            transitionTilesCoordinates.add(coordinate);
        }
        /*System.out.printf("%s -> %s: %s\n", String.valueOf(kingCoordinate), String.valueOf(rookCoordinate), String.valueOf(kingTile.isTileOccupied() && !kingTile.getPiece().isMoved() && !this.isInCheck()
                && rookTile.isTileOccupied() && !rookTile.getPiece().isMoved() && calculateAttacksOnTile(rookCoordinate, opponentMoves).isEmpty()
                && checkTransitionMoves(transitionTilesCoordinates, opponentMoves)));*/

        return kingTile.isTileOccupied() && !kingTile.getPiece().isMoved() && !this.isInCheck()
                && rookTile.isTileOccupied() && !rookTile.getPiece().isMoved() && calculateAttacksOnTile(rookCoordinate, opponentMoves).isEmpty()
                && checkTransitionMoves(transitionTilesCoordinates, opponentMoves);

    }

    public abstract Collection<Piece> getActivePieces();

    public abstract Alliance getAlliance();

    public abstract Player getOpponent();

    protected abstract Collection<Move> calculateKingCastles(
            Collection<Move> playerLegals,
            Collection<Move> opponentLegals
    );

    public Collection<Move> getKingCastles() {
        return calculateKingCastles(legalMoves, getOpponent().legalMoves);
    }

}
