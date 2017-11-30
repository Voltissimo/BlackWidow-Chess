package com.chess.gui;


import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveStatus;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.ai.AlphaBeta;
/*import com.chess.engine.player.ai.MiniMax;*/
import com.chess.engine.player.ai.MoveStrategy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static javax.swing.SwingUtilities.isLeftMouseButton;

public class Table extends Observable {
    private Board chessBoard;
    private BoardPanel boardPanel;
    private final MoveLog moveLog;
    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecesPanel takenPiecesPanel;
    private GameSetup gameSetup;

    private Tile sourceTile;
    private Tile destinationTile;
    private Piece humanMovedPiece;
    /*private BoardDirection boardDirection;*/

    private static final Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);
    private static final Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
    private static final Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);

    private static final Color LIGHT_TILE_COLOR = Color.decode("#FFFACD");
    private static final Color DARK_TILE_COLOR = Color.decode("#593E1A");

    private static boolean dispatched = false;

    static final String DEFAULT_PIECE_IMAGES_PATH = "art/pieces/simple/";
    private boolean highlightLegalMoves;

    private static final Table INSTANCE = new Table();

    public static Table get() {
        return INSTANCE;
    }

    public void show() {
        Table.get().moveLog.clear();
        Table.get().gameHistoryPanel.redo(moveLog);
        Table.get().takenPiecesPanel.redo(moveLog);
        Table.get().boardPanel.drawBoard(chessBoard);
    }

    private GameSetup getGameSetup() {
        return this.gameSetup;
    }

    private Board getGameBoard() {
        return this.chessBoard;
    }

    private void setupUpdate(GameSetup gameSetup) {
        this.gameSetup = gameSetup;
    }

    private static class TableGameAIWatcher implements Observer {
        @Override
        public void update(Observable o, Object arg) {
            if (Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().getCurrentPlayer())
                    && !Table.get().getGameBoard().getCurrentPlayer().isInCheckMate()
                    && !Table.get().getGameBoard().getCurrentPlayer().isInStaleMate()) {
                if (!dispatched) {
                    dispatched = true;
                    // create an AI thread
                    final AIThinkTank thinkTank = new AIThinkTank();
                    thinkTank.execute();
                }
            }

            if (Table.get().getGameBoard().getCurrentPlayer().isInCheckMate()) {
                JOptionPane.showMessageDialog(Table.get().boardPanel, Table.get().getGameBoard().getCurrentPlayer().getOpponent().toString() + " WON");
            }

            if (Table.get().getGameBoard().getCurrentPlayer().isInStaleMate()) {
                JOptionPane.showMessageDialog(Table.get().boardPanel, "STALEMATE");
            }
        }
    }

    void updateGameBoard(Board board) {
        this.chessBoard = board;
    }

    private void moveMadeUpdate(PlayerType playerType) {
        setChanged();
        notifyObservers(playerType);
    }

    private static class AIThinkTank extends SwingWorker<Move, String> {
        private AIThinkTank() {

        }

        @Override
        protected Move doInBackground() throws Exception {
            final MoveStrategy alphaBeta = new AlphaBeta(Table.get().gameSetup.getSearchDepth());
            /*final MoveStrategy miniMax = new MiniMax(Table.get().gameSetup.getSearchDepth());*/

            return alphaBeta.execute(Table.get().getGameBoard());
            /*return miniMax.execute(Table.get().getGameBoard());*/
        }

        @Override
        protected void done() {
            try {
                final Move bestMove = get();
                final Board newBoard = Table.get().getGameBoard().getCurrentPlayer().makeMove(bestMove).getBoard();
                Table.get().updateGameBoard(newBoard);
                Table.get().moveLog.addMove(bestMove, Table.get().getGameBoard());
                Table.get().gameHistoryPanel.redo(Table.get().moveLog);
                Table.get().takenPiecesPanel.redo(Table.get().moveLog);
                Table.get().boardPanel.drawBoard(newBoard);
                Table.get().moveMadeUpdate(PlayerType.COMPUTER);
                dispatched = false;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

    }

    private Table() {
        JFrame gameFrame = new JFrame("Voltissimo (Proto)");
        gameFrame.setLayout(new BorderLayout());
        gameFrame.setJMenuBar(createTableMenuBar());
        gameFrame.setSize(OUTER_FRAME_DIMENSION);

        this.chessBoard = Board.createStandardBoard();
        this.boardPanel = new BoardPanel();
        /*this.boardDirection = BoardDirection.NORMAL;*/
        this.moveLog = new MoveLog();
        this.gameHistoryPanel = new GameHistoryPanel();
        this.takenPiecesPanel = new TakenPiecesPanel();
        this.gameSetup = new GameSetup(gameFrame, true);

        this.addObserver(new TableGameAIWatcher());

        this.highlightLegalMoves = true;

        BoardPanel boardPanel = new BoardPanel();
        gameFrame.add(boardPanel, BorderLayout.CENTER);
        gameFrame.add(this.takenPiecesPanel, BorderLayout.WEST);
        gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
        gameFrame.setVisible(true);
    }

    private JMenuBar createTableMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferencesMenu());
        tableMenuBar.add(createOptionsMenu());
        return tableMenuBar;
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");

        final JMenuItem openPGN = new JMenuItem("Load PGN File");
        openPGN.addActionListener(e -> System.out.println("open!"));
        fileMenu.add(openPGN);

        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenuItem);

        return fileMenu;
    }

    private JMenu createPreferencesMenu() {
        final JMenu preferencesMenu = new JMenu("Preferences");
        /*final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
        flipBoardMenuItem.addActionListener(e -> {
            boardDirection = boardDirection.getOpposite();
            boardPanel.drawBoard(chessBoard);
        });
        preferencesMenu.add(flipBoardMenuItem);*/

        preferencesMenu.addSeparator();
        final JCheckBoxMenuItem legalMoveHighlighterCheckBox = new JCheckBoxMenuItem("Highlight Legal Moves", true);
        legalMoveHighlighterCheckBox.addActionListener(e -> highlightLegalMoves = legalMoveHighlighterCheckBox.isSelected());
        preferencesMenu.add(legalMoveHighlighterCheckBox);

        return preferencesMenu;
    }

    private JMenu createOptionsMenu() {
        final JMenu optionsMenu = new JMenu("Options");

        final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game");
        setupGameMenuItem.addActionListener((e) -> {
            Table.get().getGameSetup().promptUser();
            Table.get().setupUpdate(Table.get().getGameSetup());

        });
        optionsMenu.add(setupGameMenuItem);

        return optionsMenu;
    }
    /*private enum BoardDirection {
        NORMAL {
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return boardTiles;
            }

            @Override
            BoardDirection getOpposite() {
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return Lists.reverse(boardTiles);
            }

            @Override
            BoardDirection getOpposite() {
                return NORMAL;
            }
        };

        abstract List<TilePanel> traverse(List<TilePanel> boardTiles);

        abstract BoardDirection getOpposite();

    }*/

    enum PlayerType {
        HUMAN,
        COMPUTER
    }

    static class MoveLog {
        private final List<Move> moves;
        private final List<String> moveTexts;

        MoveLog() {
            this.moves = new ArrayList<>();
            this.moveTexts = new ArrayList<>();
        }

        List<Move> getMoves() {
            return this.moves;
        }

        List<String> getMoveTexts() {
            return this.moveTexts;
        }

        void addMove(Move move, Board board) {
            this.moves.add(move);
            this.moveTexts.add(move.toString() + calculateCheckAndCheckMateHash(board));
        }

        int size() {
            return this.moves.size();
        }

        private String calculateCheckAndCheckMateHash(Board board) {
            if (board.getCurrentPlayer().isInCheckMate()) {
                return "#";
            } else if (board.getCurrentPlayer().isInCheck()) {
                return "+";
            } else {
                return "";
            }
        }

        private void clear() {
            this.moves.clear();
            this.moveTexts.clear();
        }


        /*public void clear() {
            this.moves.clear();
        }*/

        /*public Move removeMove(int index) {
            return this.moves.remove(index);
        }*/

        /*public boolean removeMove(Move move) {
            return this.moves.remove(move);
        }*/

    }


    private class BoardPanel extends JPanel {
        final List<TilePanel> boardTiles;

        BoardPanel() {
            super(new GridLayout(8, 8));
            this.boardTiles = new ArrayList<>();

            for (int i = 0; i < 64; i++) {
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
                add(tilePanel);
            }

            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();
        }

        private void drawBoard(Board board) {
            removeAll();
            for (TilePanel tilePanel : boardTiles) {
                tilePanel.drawTile(board);
                add(tilePanel);
            }
            validate();
            repaint();
        }
    }

    private class TilePanel extends JPanel {
        private final int tileId;

        TilePanel(BoardPanel boardPanel, int tileId) {
            super(new GridBagLayout());
            this.tileId = tileId;
            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (isLeftMouseButton(e)) {
                        if (sourceTile == null) {
                            // first click
                            sourceTile = chessBoard.getTile(tileId);
                            humanMovedPiece = sourceTile.getPiece();
                            if (humanMovedPiece == null) {
                                sourceTile = null;
                            }
                        } else {
                            // second click
                            destinationTile = chessBoard.getTile(tileId);
                            final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.tileCoordinate, destinationTile.tileCoordinate);
                            final MoveTransition transition = chessBoard.getCurrentPlayer().makeMove(move);
                            // prevent player to make move while bot is thinking
                            if (!Table.get().gameSetup.isAIPlayer(chessBoard.getCurrentPlayer())) {
                                if (transition.getMoveStatus() == MoveStatus.DONE) {
                                    chessBoard = transition.getBoard();
                                    moveLog.addMove(move, chessBoard);

                                    // reset
                                    sourceTile = null;
                                    humanMovedPiece = null;
                                    destinationTile = null;
                                } else {
                                    // choose a new tile
                                    sourceTile = chessBoard.getTile(tileId);
                                    humanMovedPiece = sourceTile.getPiece();
                                    if (humanMovedPiece == null) {
                                        sourceTile = null;
                                    }
                                }
                            }
                        }
                        SwingUtilities.invokeLater(() -> {
                            gameHistoryPanel.redo(moveLog);
                            gameHistoryPanel.redo(moveLog); // Move history doesn't update for white moves if removed ╮(￣▽￣"")╭
                            takenPiecesPanel.redo(moveLog);

                            if (gameSetup.isAIPlayer(chessBoard.getCurrentPlayer())) {
                                Table.get().moveMadeUpdate(PlayerType.HUMAN);
                            }

                            boardPanel.drawBoard(chessBoard);
                        });
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });

            validate();
        }

        private void assignTilePieceIcon(final Board board) {
            this.removeAll();
            if (board.getTile(this.tileId).isTileOccupied()) {
                try {
                    final Piece pieceOnTile = board.getTile(this.tileId).getPiece();
                    final BufferedImage image = ImageIO.read(new File(DEFAULT_PIECE_IMAGES_PATH
                            + pieceOnTile.getPieceAlliance().toString().substring(0, 1)
                            + pieceOnTile.toString() + ".gif"));
                    add(new JLabel(new ImageIcon(image)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void highlightLegalMoves(Board board) {
            final List<Integer> addedCoordinateList = new ArrayList<>();
            if (highlightLegalMoves) {
                for (Move move : getPieceLegalMoves(board)) {
                    if (move.getDestinationCoordinate() == this.tileId
                            && !addedCoordinateList.contains(move.getDestinationCoordinate())
                            && chessBoard.getCurrentPlayer().makeMove(move).getMoveStatus() == MoveStatus.DONE) {
                        addedCoordinateList.add(move.getDestinationCoordinate());
                        try {
                            add(new JLabel(new ImageIcon(ImageIO.read(new File("art/misc/green_dot.png")))));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private Collection<Move> getPieceLegalMoves(Board board) {
            if (humanMovedPiece != null && humanMovedPiece.getPieceAlliance() == board.getCurrentPlayer().getAlliance()) {
                if (humanMovedPiece.getPieceType().isKing()) {
                    Collection<Move> kingCastles = board.getCurrentPlayer().getKingCastles();
                    return ImmutableList.copyOf(Iterables.concat(humanMovedPiece.calculateLegalMoves(board), kingCastles));
                }
                return humanMovedPiece.calculateLegalMoves(board);
            }
            return Collections.emptyList();
        }

        private void assignTileColor() {
            int numberRow = this.tileId / 8;
            int numberCol = this.tileId % 8;
            setBackground((numberRow + numberCol) % 2 == 0 ? LIGHT_TILE_COLOR : DARK_TILE_COLOR);

        }

        private void drawTile(Board board) {
            assignTileColor();
            assignTilePieceIcon(board);
            highlightLegalMoves(board);
            validate();
            repaint();
        }
    }
}
