package com.chess.pgn;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.pieces.Pawn;

public class FenUtilities {
    private FenUtilities() {
        throw new RuntimeException("Not instantiable!");
    }

    public static Board createGameFromFEN(String fenString) {
        return null;
    }

    public static String createFENFromBoard(Board board) {
        return calculateBoardText(board) + " "
                + calculateCurrentPlayerText(board) + " "
                + calculateCastleText(board) + " "
                + calculateEnPassantSquare(board) + " "
                + "0 1";
    }

    private static String calculateEnPassantSquare(Board board) {
        final Pawn enPassantPawn = board.getEnPassantPawn();
        if (enPassantPawn != null) {

            return BoardUtils.getPositionAtCoordinate(enPassantPawn.getPiecePosition() - 8 * enPassantPawn.getPieceAlliance().getDirection());
        }
        return "-";
    }

    private static String calculateCastleText(Board board) {
        final StringBuilder builder = new StringBuilder();
        if (board.whitePlayer().isKingSideCastleAvailable()) {
            builder.append("K");
        }
        if (board.whitePlayer().isQueenSideCastleAvailable()) {
            builder.append("Q");
        }
        if (board.blackPlayer().isKingSideCastleAvailable()) {
            builder.append("k");
        }
        if (board.blackPlayer().isQueenSideCastleAvailable()) {
            builder.append("q");
        }
        return builder.toString().isEmpty() ? "-" : builder.toString();
    }

    private static String calculateCurrentPlayerText(Board board) {
        return board.getCurrentPlayer().toString().substring(0, 1).toLowerCase();
    }

    private static String calculateBoardText(Board board) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            final String tileText = board.getTile(i).toString();
            builder.append(tileText);
            if (i % 8 == 0 && i != 0) {
                builder.append("/");
            }
        }
        String resultString = builder.toString();
        for (int i = 1; i <= 8; i++) {
            final String stringToReplace = new String(new char[i]).replace("\0", "-");
            resultString = resultString.replaceAll(stringToReplace, String.valueOf(i));
        }
        return resultString;
    }
}
