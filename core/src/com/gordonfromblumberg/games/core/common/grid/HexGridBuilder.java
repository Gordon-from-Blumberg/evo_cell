package com.gordonfromblumberg.games.core.common.grid;

public class HexGridBuilder {
    private int width;
    private int height;
    private int hexWidth;
    private int hexHeight;
    private float incline;
    private int layerCount;
    private GridShape shape;
    private float weight;

    private HexGridBuilder() { }

    public static HexGridBuilder start() {
        return new HexGridBuilder();
    }

    public HexGridBuilder rect(int width, int height) {
        this.width = width;
        this.height = height;
        this.shape = GridShape.rect;
        return this;
    }

    public HexGridBuilder hexParams(int width, int height, float incline) {
        this.hexWidth = width;
        this.hexHeight = height;
        this.incline = incline;
        return this;
    }

    public HexGridBuilder layers(int count) {
        this.layerCount = count;
        return this;
    }

    public HexGridBuilder weight(float weight) {
        this.weight = weight;
        return this;
    }

    public HexGrid build() {
        final int width = this.width;
        final HexGrid hexGrid = new HexGrid(hexWidth, hexHeight, incline, height, layerCount);
        final int[] minMaxX = new int[2];
        for (int y = 0; y < height; ++y) {
            shape.getMinMaxX(width, y, minMaxX);
            HexRow row = new HexRow(y, minMaxX[0], minMaxX[1]);
            hexGrid.rows[y] = row;

            for (int x = row.minX, max = row.maxX; x <= max; ++x) {
                Hex hex = new Hex(x, y, layerCount);
                row.hexes.put(x, hex);
            }
        }
        hexGrid.calcBounds();
        hexGrid.createEdges(weight);
        return hexGrid;
    }

    private enum GridShape {
        rect,
        hex;

        void getMinMaxX(int width, int y, int[] out) {
            switch (this) {
                case rect -> {
                    out[0] = 0;
                    out[1] = width - 1;
                }
                case hex -> {
                    // todo
                }
            }
        }
    }
}
