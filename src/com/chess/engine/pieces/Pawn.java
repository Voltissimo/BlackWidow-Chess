package com.chess.engine.pieces;


import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.chess.engine.board.Move.*;


public class Pawn extends Piece {
    private final static int[] CANDIDATE_MOVE_COORDINATE = {7, 8, 9, 16};

    public Pawn(final int piecePosition, final Alliance pieceAlliance, boolean moved) {
        super(PieceType.PAWN, piecePosition, pieceAlliance, moved);
    }

    @Override
    public String toString() {
        return PieceType.PAWN.toString();
    }

    @Override
    public List<Move> calculateLegalMoves(final Board board) {
        List<Move> legalMoves = new ArrayList<>();

        for (int currentCandidateOffset : CANDIDATE_MOVE_COORDINATE) {
            int candidateDestinationCoordinate = this.piecePosition + this.pieceAlliance.getDirection() * currentCandidateOffset;

            if (candidateDestinationCoordinate >= 0 && candidateDestinationCoordinate < 64) {
                if (currentCandidateOffset == 8 && !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                    // move forward by 1
                    if (pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                        // promotion
                        legalMoves.add(new PawnPromotion(new PawnMove(board, this, candidateDestinationCoordinate)));

                    } else {
                        legalMoves.add(new PawnMove(board, this, candidateDestinationCoordinate));
                    }

                    // pawn jump (forward by 2)
                    if (!this.isMoved() && (
                            this.pieceAlliance.isBlack() && this.piecePosition / 8 == 1
                                    || this.pieceAlliance.isWhite() && this.piecePosition / 8 == 6)
                            ) {
                        int candidatePawnJumpDestinationCoordinate = candidateDestinationCoordinate + 8 * this.pieceAlliance.getDirection();
                        if (!board.getTile(candidateDestinationCoordinate).isTileOccupied()
                                && !board.getTile(candidatePawnJumpDestinationCoordinate).isTileOccupied()) {

                            legalMoves.add(new PawnJump(board, this, candidatePawnJumpDestinationCoordinate));
                        }
                    }

                } else if (currentCandidateOffset == 7 || currentCandidateOffset == 9) {
                    // pawn attack
                    int row_offset = this.piecePosition / 8 - candidateDestinationCoordinate / 8;
                    int col_offset = this.piecePosition % 8 - candidateDestinationCoordinate % 8;
                    if (Math.abs(row_offset) == 1 && Math.abs(col_offset) == 1 /*pff java doesn't have a == b == c*/) {
                        if (board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                            Piece targetPiece = board.getTile(candidateDestinationCoordinate).getPiece();
                            if (pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                                legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this, candidateDestinationCoordinate, targetPiece)));
                            } else {
                                if (targetPiece.pieceAlliance != this.pieceAlliance) {
                                    legalMoves.add(new PawnAttackMove(board, this, candidateDestinationCoordinate, targetPiece));
                                }
                            }
                        } else if (board.getEnPassantPawn() != null && !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                            // En passant
                            if (board.getEnPassantPawn().getPiecePosition() == candidateDestinationCoordinate - 8 * this.pieceAlliance.getDirection()) {
                                final Piece pieceOnCandidate = board.getEnPassantPawn();
                                if (this.pieceAlliance != pieceOnCandidate.pieceAlliance) {
                                    legalMoves.add(new PawnEnPassantAttackMove(board, this, candidateDestinationCoordinate, pieceOnCandidate));
                                }
                            }
                        }

                    }
                }
            }

        }
        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Pawn movePiece(Move move) {
        return new Pawn(move.getDestinationCoordinate(), move.getMovedPiece().getPieceAlliance(), true);
    }

    public Piece getPromotionPiece() {
        return new Queen(this.piecePosition, this.pieceAlliance, true);
    }
}
