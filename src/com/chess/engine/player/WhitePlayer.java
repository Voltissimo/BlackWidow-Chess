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

import static com.chess.engine.board.Move.*;

public class WhitePlayer extends Player {
    public WhitePlayer(Board board,
                       Collection<Move> whiteStandardLegalMoves,
                       Collection<Move> blackStandardLegalMoves) {
        super(board, whiteStandardLegalMoves, blackStandardLegalMoves);

    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getWhitePieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.WHITE;
    }

    @Override
    public Player getOpponent() {
        return this.board.blackPlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(Collection<Move> playerLegals, Collection<Move> opponentLegals) {
        List<Move> kingCastles = new ArrayList<>();

        int kingCoordinate = 60;
        int rookKingCoordinate = 63;
        int rookQueenCoordinate = 56;
        // white king side castling
        if (checkCastlingEligible(kingCoordinate, rookKingCoordinate, opponentLegals)) {
            kingCastles.add(new KingSideCastleMove(
                    this.board,
                    this.playerKing,
                    62,
                    (Rook) this.board.getTile(rookKingCoordinate).getPiece(),
                    61
            ));
        }
        // white queen side castling
        if (checkCastlingEligible(kingCoordinate, rookQueenCoordinate, opponentLegals)) {
            kingCastles.add(new QueenSideCastleMove(
                    this.board,
                    this.playerKing,
                    58,
                    (Rook) this.board.getTile(rookQueenCoordinate).getPiece(),
                    59
            ));
        }

        return ImmutableList.copyOf(kingCastles);
    }

}
