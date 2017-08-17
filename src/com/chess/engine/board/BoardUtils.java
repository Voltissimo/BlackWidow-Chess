package com.chess.engine.board;

import java.util.HashMap;
import java.util.Map;

public class BoardUtils {
    private static final Map<String, Integer> POSITION_TO_COORDINATE = initializePositionToCoordinateMap();

    private static Map<String, Integer> initializePositionToCoordinateMap() {
        final Map<String, Integer> positionToCoordinate = new HashMap<>();
        for (int i = 0; i < 64; i++) {
            positionToCoordinate.put(getPositionAtCoordinate(i), i);
        }
        return positionToCoordinate;
    }

    public static int getCoordinateAtPosition(String position) {
        return POSITION_TO_COORDINATE.get(position);
    }

    public static String getPositionAtCoordinate(int coordinate) {
        int row = coordinate / 8;
        int col = coordinate % 8;
        final String[] RANK_NAME = new String[] {"a", "b", "c", "d", "e", "f", "g", "h"};
        return RANK_NAME[col] + String.valueOf(9 - row - 1);
    }

}
