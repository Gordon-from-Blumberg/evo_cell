package com.gordonfromblumberg.games.core.common.grid;

import com.badlogic.gdx.utils.Array;
import com.gordonfromblumberg.games.core.common.graph.Edge;
import com.gordonfromblumberg.games.core.common.graph.Graph;
import com.gordonfromblumberg.games.core.common.utils.CollectionUtils;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.function.IntUnaryOperator;

public class HexGrid implements Graph<Hex>, Iterable<HexRow> {
    static final float xIntersection = 0.5f;
    static final int[] dy = {0, 1, 1, 0, -1, -1};
    static final IntUnaryOperator[] dx = {y -> 1, y -> y % 2, y -> y % 2 - 1, y -> -1, y -> y % 2 - 1, y -> y % 2};

    final int hexWidth;
    final int hexHeight;
    final float incline;
    /**
     * Distance by 'y' between hex centres in related rows
     */
    final float rowDist;
    final float yIntersection;

    final HexRow[] rows;
    int hexCount;
    final int layerCount;
    int minX, maxX;
    private int iteratorIndex = 0;

    public HexGrid(int hexWidth, int hexHeight, float incline, int rowCount, int layerCount) {
        this.hexWidth = hexWidth;
        this.hexHeight = hexHeight;
        this.incline = incline;
        this.rowDist = hexHeight - incline;
        this.yIntersection = incline / this.rowDist;
        this.rows = new HexRow[rowCount];
        this.layerCount = layerCount;
    }

    public HexRow[] getRows() {
        return rows;
    }

    public Hex getHex(int x, int y) {
        if (y < 0 || y >= rows.length)
            return null;
        HexRow row = rows[y];
        if (x < row.minX || x > row.maxX)
            return null;
        return row.hexes.get(x);
    }

    public int getHexCount() {
        return hexCount;
    }

    public Hex findHex(float x, float y) {
        int minX, maxX, minY, maxY;

        float kx = (x + hexWidth / 2f) / hexWidth;
        if (kx - (maxX = (int)kx) < xIntersection)
            minX = maxX - 1;
        else
            minX = maxX;

        float ky = (y + hexHeight / 2f) / rowDist;
        if (ky - (maxY = (int)ky) < yIntersection)
            minY = maxY - 1;
        else
            minY = maxY;

        Hex hex = null;
        float minD2 = hexHeight * 5;
        float d2 = minD2;
        for (int j = minY; j >= 0 && j <= maxY && j < rows.length; ++j) {
            HexRow row = rows[j];
            for (int i = minX; i <= maxX; ++i) {
                Hex curHex = row.hexes.get(i);
                if (curHex != null && ((d2 = dist2(curHex, x, y)) < minD2) || hex == null) {
                    hex = curHex;
                    minD2 = d2;
                }
            }
        }

        return hex;
    }

    public void removeEdges(Hex hex) {
        for (int i = 0; i < 6; ++i) {
            if (hex.edges[i] != null) {
                Hex neib = hex.edges[i].hex;
                neib.edges[(i + 3) % 6] = null;
                hex.edges[i] = null;
            }
        }
    }

    public void createEdge(Hex hex1, Hex hex2, float weight) {
        for (int i = 0; i < 6; ++i) {
            int nx = dx[i].applyAsInt(hex1.y) + hex1.x;
            int ny = dy[i] + hex1.y;
            if (hex2 == getHex(nx, ny)) {
                hex1.edges[i] = new HexEdge(hex2, weight);
                hex2.edges[(i + 3) % 6] = new HexEdge(hex1, weight);
                return;
            }
        }
    }

    public void save(ByteBuffer bb) {
        // todo
    }

    public static HexGrid load(ByteBuffer bb) {
        int hexWidth = bb.getInt();
        int hexHeight = bb.getInt();
        float incline = bb.getFloat();
        int rowCount = bb.getInt();
        int layerCount = bb.getInt();
        final HexGrid grid = new HexGrid(hexWidth, hexHeight, incline, rowCount, layerCount);
        // todo
        return grid;
    }

    public void setWeight(Hex hex1, Hex hex2, float weight) {
        for (int i = 0; i < 6; ++i) {
            if (hex1.edges[i] != null && hex1.edges[i].hex == hex2) {
                hex1.edges[i].weight = weight;
                hex2.edges[(i + 3) % 6].weight = weight;
                return;
            }
        }
    }

    public float getWeight(Hex hex1, Hex hex2) {
        for (int i = 0; i < 6; ++i) {
            if (hex1.edges[i] != null && hex1.edges[i].hex == hex2) {
                return hex1.edges[i].weight;
            }
        }
        return Float.NaN;
    }

    public int getDir(Hex from, Hex to) {
        for (int i = 0; i < 6; ++i) {
            if (from.edges[i] != null && from.edges[i].hex == to) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void next(Hex node, Array<Edge<Hex>> out) {
        CollectionUtils.addNonNull(out, node.edges);
    }

    public Edge<Hex> next(Hex node, int dir) {
        return node.edges[dir];
    }

    public Hex nextHex(Hex node, int dir) {
        if (node.edges[dir] != null)
            return node.edges[dir].hex;

        int nx = dx[dir].applyAsInt(node.y) + node.x;
        int ny = dy[dir] + node.y;
        return getHex(nx, ny);
    }

    @Override
    public void prev(Hex node, Array<Edge<Hex>> out) {
        CollectionUtils.addNonNull(out, node.edges);
    }

    public float getWorldX(Hex hex) {
        final float halfWidth = hexWidth / 2f;
        return hex.x * hexWidth + hex.y % 2 * halfWidth;
    }

    public float getWorldY(Hex hex) {
        return hex.y * rowDist;
    }

    public int getHexWidth() {
        return hexWidth;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return rows.length - 1;
    }

    private final Iterator<HexRow> iterator = new Iterator<>() {
        @Override
        public boolean hasNext() {
            return iteratorIndex < rows.length;
        }

        @Override
        public HexRow next() {
            return rows[iteratorIndex++];
        }
    };

    @Override
    public Iterator<HexRow> iterator() {
        iteratorIndex = 0;
        return iterator;
    }

    void calcBounds() {
        int hexCount = 0;
        int minX = rows[0].minX, maxX = rows[0].maxX;
        for (HexRow row : rows) {
            hexCount += row.maxX - row.minX + 1;
            if (row.minX < minX) minX = row.minX;
            if (row.maxX > maxX) maxX = row.maxX;
        }
        this.hexCount = hexCount;
        this.minX = minX;
        this.maxX = maxX;
    }

    void createEdges(float weight) {
        for (HexRow row : rows) {
            for (int x = row.minX, max = row.maxX; x <= max; ++x) {
                Hex hex = row.hexes.get(x);

                for (int i = 0; i < 6; ++i) {
                    int nx = dx[i].applyAsInt(hex.y) + x;
                    int ny = dy[i] + hex.y;
                    Hex nHex = getHex(nx, ny);
                    if (nHex != null) {
                        hex.edges[i] = new HexEdge(nHex, weight);
                    }
                }
            }
        }
    }

    private float dist2(Hex hex, float x, float y) {
        float dx = getWorldX(hex) - x;
        float dy = getWorldY(hex) - y;
        return dx * dx + dy * dy;
    }
}
