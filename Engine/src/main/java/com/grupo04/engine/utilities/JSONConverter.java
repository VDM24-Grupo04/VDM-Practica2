package com.grupo04.engine.utilities;

import org.json.JSONArray;

import java.util.LinkedList;

public class JSONConverter {
    public static JSONArray convertMatrixToJSONArray(int[][] matrix) {
        JSONArray jsonArray = new JSONArray();
        for (int[] row : matrix) {
            JSONArray innerArray = new JSONArray();
            for (int num : row) {
                innerArray.put(num);
            }
            jsonArray.put(innerArray);
        }
        return jsonArray;
    }

    public static int[][] convertJSONArrayToMatrix(JSONArray jsonArray) {
        int[][] matrix = new int[jsonArray.length()][];
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray innerArray = jsonArray.getJSONArray(i);
            matrix[i] = new int[innerArray.length()];
            for (int j = 0; j < innerArray.length(); j++) {
                matrix[i][j] = innerArray.getInt(j);
            }
        }
        return matrix;
    }

    public static JSONArray convertLinkedListToJSONArray(LinkedList<Integer> list) {
        JSONArray jsonArray = new JSONArray();
        for (Integer value : list) {
            jsonArray.put(value);
        }
        return jsonArray;
    }

    public static LinkedList<Integer> convertJSONArrayToLinkedList(JSONArray jsonArray) {
        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getInt(i));
        }
        return list;
    }
}
