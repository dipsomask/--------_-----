package com.example;

import java.util.ArrayList;

public class MatrixManager {
    private ArrayList<Matrix> matrices;

    public MatrixManager() {
        matrices = new ArrayList<>();
    }

    // Метод для добавления нового двумерного массива
    public void addMatrix(Matrix matrix) {
        matrices.add(matrix);
    }

    public Matrix getMatrix(int index) {
        if (index >= 0 && index < matrices.size()) {
            return matrices.get(index);
        } else {
            throw new IndexOutOfBoundsException("Matrix index out of bounds");
        }
    }

    public int getCount() {
        return matrices.size();
    }

    public void clear(){
        matrices.clear();
    }
}
