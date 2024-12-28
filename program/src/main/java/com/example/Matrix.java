package com.example;

public class Matrix {
    private double[][] data;
    private int rows;
    private int cols;

    public Matrix(int rows_, int cols_) {
        data = new double[rows_][cols_];
        rows = rows_;
        cols = cols_;
    }

    public void setValue(int row, int col, double value) {
        data[row][col] = value;
    }

    public double getValue(int row, int col) {
        return data[row][col];
    }

    public int getRows(){
        return rows;
    }

    public int getCols(){
        return cols;
    }

    public void print() {
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                System.out.print(data[i][j] + "\t");
            }
            System.out.println();
        }
    }

}
