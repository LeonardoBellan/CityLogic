package test.jfx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TileMapCanvas extends Canvas {

    public enum Tile { ROAD, BUILDING, PARK, WATER, PAVEMENT, HIGHWAY, ROOF }

    private static final Color[] COLORS = {
        Color.web("#4a4a4a"),  // ROAD       - dark asphalt
        Color.web("#7a6a5a"),  // BUILDING   - tan/brown walls
        Color.web("#3a7d44"),  // PARK       - green
        Color.web("#2a5298"),  // WATER      - river/canal
        Color.web("#b0a898"),  // PAVEMENT   - light grey sidewalk
        Color.web("#2b2b2b"),  // HIGHWAY    - darker asphalt
        Color.web("#c0504d"),  // ROOF       - red rooftop
    };

    private final int[][] matrix;
    private final int tileSize;

    public TileMapCanvas(int[][] matrix, int tileSize) {
        super(matrix[0].length * tileSize, matrix.length * tileSize);
        this.matrix   = matrix;
        this.tileSize = tileSize;
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                int    type = matrix[row][col];
                double x    = col * tileSize;
                double y    = row * tileSize;

                // Base fill
                gc.setFill(COLORS[type]);
                gc.fillRect(x, y, tileSize, tileSize);

                // Subtle grid lines
                gc.setStroke(Color.rgb(0, 0, 0, 0.2));
                gc.setLineWidth(0.5);
                gc.strokeRect(x, y, tileSize, tileSize);
            }
        }
    }

    public int getTileSize() { return tileSize; }

    public int[] getTileAt(double canvasX, double canvasY) {
        int col = (int)(canvasX / tileSize);
        int row = (int)(canvasY / tileSize);
        if (row >= 0 && row < matrix.length && col >= 0 && col < matrix[0].length)
            return new int[]{col, row};
        return null;
    }
}