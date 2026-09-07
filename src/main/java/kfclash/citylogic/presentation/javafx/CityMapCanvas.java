package kfclash.citylogic.presentation.javafx;

import kfclash.citylogic.domain.map.Dimension;
import kfclash.citylogic.domain.map.Grid;
import kfclash.citylogic.domain.map.Point;
import kfclash.citylogic.ports.IBuildingState;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** Dashboard canvas adapted from the demo UI to the citylogic domain APIs. */
public class CityMapCanvas extends Canvas {
    private static final int TILE_SIZE = 40;
    private static final int PADDING = 24;

    private Grid grid;
    private Point selectedPoint;
    private Point hoverPoint;
    private String activeTool = "select";

    public CityMapCanvas() {
        super(800, 620);
        widthProperty().addListener(observable -> redraw());
        heightProperty().addListener(observable -> redraw());
    }

    public void init(Grid grid) {
        this.grid = grid;
        updateCanvasDimensions();
        redraw();
    }

    public void setActiveTool(String tool) {
        activeTool = tool;
        redraw();
    }

    public void setSelectedPoint(Point point) {
        selectedPoint = point;
        redraw();
    }

    public void setHoverPoint(Point point) {
        hoverPoint = point;
        redraw();
    }

    public Point getGridCoordinatesFromPixel(double pixelX, double pixelY) {
        if (grid == null) return null;
        Dimension dimensions = grid.getDimensions();
        double startX = Math.max(PADDING, (getWidth() - dimensions.getWidth() * TILE_SIZE) / 2.0);
        double startY = Math.max(PADDING, (getHeight() - dimensions.getHeight() * TILE_SIZE) / 2.0);
        int x = (int) Math.floor((pixelX - startX) / TILE_SIZE);
        int y = (int) Math.floor((pixelY - startY) / TILE_SIZE);
        if (x < 0 || y < 0 || x >= dimensions.getWidth() || y >= dimensions.getHeight()) return null;
        return new Point(x, y);
    }

    public void redraw() {
        GraphicsContext graphics = getGraphicsContext2D();
        graphics.setFill(Color.web("#0f172a"));
        graphics.fillRect(0, 0, getWidth(), getHeight());
        if (grid == null) return;

        Dimension dimensions = grid.getDimensions();
        double gridWidth = dimensions.getWidth() * TILE_SIZE;
        double gridHeight = dimensions.getHeight() * TILE_SIZE;
        double startX = Math.max(PADDING, (getWidth() - gridWidth) / 2.0);
        double startY = Math.max(PADDING, (getHeight() - gridHeight) / 2.0);

        graphics.setFill(Color.web("#1e293b", 0.65));
        graphics.fillRoundRect(startX - 8, startY - 8, gridWidth + 16, gridHeight + 16, 18, 18);
        for (int x = 0; x < dimensions.getWidth(); x++) {
            for (int y = 0; y < dimensions.getHeight(); y++) {
                double tileX = startX + x * TILE_SIZE;
                double tileY = startY + y * TILE_SIZE;
                graphics.setFill((x + y) % 2 == 0 ? Color.web("#1e293b") : Color.web("#162032"));
                graphics.fillRoundRect(tileX + 1, tileY + 1, TILE_SIZE - 2, TILE_SIZE - 2, 7, 7);
                graphics.setStroke(Color.web("#334155", 0.55));
                graphics.strokeRoundRect(tileX + 1, tileY + 1, TILE_SIZE - 2, TILE_SIZE - 2, 7, 7);
            }
        }
        for (IBuildingState building : grid.getAllBuildings()) drawBuilding(graphics, building, startX, startY);
        if (hoverPoint != null) drawHover(graphics, startX, startY);
        if (selectedPoint != null) {
            double x = startX + selectedPoint.getX() * TILE_SIZE;
            double y = startY + selectedPoint.getY() * TILE_SIZE;
            graphics.setStroke(Color.web("#38bdf8"));
            graphics.setLineWidth(3);
            graphics.strokeRoundRect(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4, 8, 8);
        }
    }

    private void drawBuilding(GraphicsContext graphics, IBuildingState building, double startX, double startY) {
        Point position = building.getPosition();
        Dimension footprint = building.getDescription().getFootprint();
        double x = startX + position.getX() * TILE_SIZE + 3;
        double y = startY + position.getY() * TILE_SIZE + 3;
        double width = footprint.getWidth() * TILE_SIZE - 6;
        double height = footprint.getHeight() * TILE_SIZE - 6;
        String type = building.getType().toLowerCase();
        Color base = type.contains("factory") ? Color.web("#b45309")
                : type.contains("park") ? Color.web("#047857")
                : type.contains("road") ? Color.web("#475569")
                : type.contains("commercial") ? Color.web("#0369a1")
                : type.contains("power") ? Color.web("#6d28d9")
                : Color.web("#15803d");
        Color stroke = building.isPowered() ? base.brighter() : Color.web("#64748b");
        if (!building.isPowered()) base = base.darker().desaturate();
        graphics.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, base.brighter()), new Stop(1, base)));
        graphics.fillRoundRect(x, y, width, height, 10, 10);
        graphics.setStroke(stroke);
        graphics.setLineWidth(2);
        graphics.strokeRoundRect(x, y, width, height, 10, 10);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font("Segoe UI Emoji, Arial", FontWeight.BOLD, footprint.getWidth() > 1 ? 24 : 18));
        graphics.fillText(iconFor(type), x + width / 2, y + height / 2 + 2);
        graphics.setFont(Font.font("System", FontWeight.SEMI_BOLD, 10));
        graphics.setFill(Color.web("#e2e8f0"));
        graphics.fillText(building.getDescription().getName(), x + width / 2, y + height - 6);
        if (!building.isPowered()) {
            graphics.setFill(Color.web("#ef4444"));
            graphics.fillOval(x + width - 14, y + 4, 10, 10);
        }
    }

    private void drawHover(GraphicsContext graphics, double startX, double startY) {
        double x = startX + hoverPoint.getX() * TILE_SIZE;
        double y = startY + hoverPoint.getY() * TILE_SIZE;
        Color color = "demolish".equals(activeTool) ? Color.web("#ef4444") : Color.web("#38bdf8");
        graphics.setFill(color.deriveColor(0, 1, 1, 0.22));
        graphics.fillRoundRect(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4, 8, 8);
        graphics.setStroke(color);
        graphics.setLineWidth(2);
        graphics.strokeRoundRect(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4, 8, 8);
    }

    private static String iconFor(String type) {
        if (type.contains("factory")) return "🏭";
        if (type.contains("park")) return "🌲";
        if (type.contains("road")) return "━";
        if (type.contains("commercial")) return "🏢";
        if (type.contains("power")) return "⚡";
        return "🏠";
    }

    private void updateCanvasDimensions() {
        if (grid == null) return;
        Dimension dimensions = grid.getDimensions();
        setWidth(Math.max(760, dimensions.getWidth() * TILE_SIZE + PADDING * 2));
        setHeight(Math.max(600, dimensions.getHeight() * TILE_SIZE + PADDING * 2));
    }
}
