package com.chess.gui;

import com.chess.engine.board.Move;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.chess.gui.Table.*;


class GameHistoryPanel extends JPanel {
    private static final Dimension HISTORY_PANEL_DIMENSION = new Dimension(100, 400);
    private final DataModel model;
    private final JScrollPane scrollPane;

    GameHistoryPanel() {
        this.setLayout(new BorderLayout());
        this.model = new DataModel();
        final JTable table = new JTable(model);
        table.setRowHeight(15);
        scrollPane = new JScrollPane(table);
        scrollPane.setColumnHeaderView(table.getTableHeader());
        scrollPane.setPreferredSize(HISTORY_PANEL_DIMENSION);
        this.add(scrollPane, BorderLayout.CENTER);
        this.setVisible(true);
    }

    void redo(MoveLog moveLog) {
        /*this.model.clear();*/
        for (int i = 0; i < moveLog.size(); i++) {
            final Move move = moveLog.getMoves().get(i);
            final String moveText = moveLog.getMoveTexts().get(i);
            final int currentRow = i / 2;
            if (move.getMovedPiece().getPieceAlliance().isWhite()) {
                this.model.setValueAt(moveText, currentRow, 0);
            } else if (move.getMovedPiece().getPieceAlliance().isBlack()) {
                this.model.setValueAt(moveText, currentRow, 1);
            }
        }

        final JScrollBar verticalScroll = scrollPane.getVerticalScrollBar();
        verticalScroll.setValue(verticalScroll.getMaximum());
    }

    private static class Row {
        private String whiteMove;
        private String blackMove;

        String getWhiteMove() {
            return this.whiteMove;
        }

        String getBlackMove() {
            return this.blackMove;
        }

        void setWhiteMove(String move) {
            this.whiteMove = move;
        }

        void setBlackMove(String move) {
            this.blackMove = move;
        }
    }

    private static class DataModel extends DefaultTableModel {
        private final List<Row> values;
        private static final String[] NAMES = {"White", "Black"};

        DataModel() {
            this.values = new ArrayList<>();
        }

        /*void clear() {
            values.clear();
            setRowCount(0);
        }*/

        @Override
        public int getRowCount() {
            try {
                return this.values.size();
            } catch (NullPointerException e) {
                return 0;
            }
        }

        @Override
        public int getColumnCount() {
            return NAMES.length;  // 2
        }

        @Override
        public Object getValueAt(int row, int col) {
            /*System.out.println("\n");
            for (int i = 0; i < this.values.size(); i++) {
                System.out.printf("===ROW %s\n", String.valueOf(i));  // SO YOU NEED TO DO THIS TO FORMAT AN INT?!
                final Row rowTemp = this.values.get(i);
                System.out.printf("WHITE: %s\n", rowTemp.getWhiteMove());
                System.out.printf("BLACK: %s\n", rowTemp.getBlackMove());
            }*/
            final Row currentRow = this.values.get(row);
            if (col == 0) {
                return currentRow.getWhiteMove();
            } else if (col == 1) {
                return currentRow.getBlackMove();
            } else {
                return null;
            }

        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            final Row currentRow;
            if (this.values.size() <= row) {
                currentRow = new Row();
                this.values.add(currentRow);
            } else {
                currentRow = this.values.get(row);
                if (col == 0) {
                    currentRow.setWhiteMove((String) value);
                    fireTableRowsInserted(row, row);
                } else if (col == 1) {
                    currentRow.setBlackMove((String) value);
                    fireTableCellUpdated(row, col);
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return Move.class;
        }

        @Override
        public String getColumnName(int col) {
            return NAMES[col];
        }
    }
}
