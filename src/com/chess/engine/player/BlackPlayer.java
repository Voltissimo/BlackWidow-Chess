package com.chess.engine.player;


import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlackPlayer extends Player {

    public BlackPlayer(Board board,
                       Collection<Move> whiteStandardLegalMoves,
                       Collection<Move> blackStandardLegalMoves) {
        super(board, blackStandardLegalMoves, whiteStandardLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getBlackPieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.BLACK;
    }

    @Override
    public Player getOpponent() {
        return this.board.whitePlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(Collection<Move> playerLegals, Collection<Move> opponentLegals) {
        List<Move> kingCastles = new ArrayList<>();

        int kingCoordinate = 4;
        int rookQueenCoordinate = 0;
        int rookKingCoordinate = 7;
        // black king side castling
        if (checkCastlingEligible(kingCoordinate, rookKingCoordinate, opponentLegals)) {
            kingCastles.add(new Move.KingSideCastleMove(
                    this.board,
                    this.playerKing,
                    6,
                    (Rook) this.board.getTile(rookKingCoordinate).getPiece(),
                    5
            ));
        }
        // black queen side castling
        if (checkCastlingEligible(kingCoordinate, rookQueenCoordinate, opponentLegals)) {
            kingCastles.add(new Move.QueenSideCastleMove(
                    this.board,
                    this.playerKing,
                    2,
                    (Rook) this.board.getTile(rookQueenCoordinate).getPiece(),
                    3
            ));
        }

        return ImmutableList.copyOf(kingCastles);
    }
}
