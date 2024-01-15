package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.gordonfromblumberg.games.core.common.chunk.ChunkManager;

public class CellGrid {
    private static final int[][] NEIGHBORS = new int[][] {
            {0, 1},
            {1, 0},
            {0, -1},
            {-1, 0}
    };
    private static final Queue<Cell> CELL_QUEUE = new Queue<>();

    int width, height;
    int cellSize;
    public final Cell[][] cells;
//    private final ChunkManager<LivingCell> chunkManager;
//    private final Queue<LightSource> lightSources = new Queue<>();

    public CellGrid(int width, int height, int cellSize) {
        this.width = width;
        this.height = height;
        this.cellSize = cellSize;
        this.cells = new Cell[width][height];

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                this.cells[i][j] = new Cell(i, j);
            }
        }

//        this.chunkManager = new ChunkManager<>(width, height, chunkSize);
    }

//    public void updateSunLight(LightDistribution lightDistribution) {
//        final Cell[][] cells = this.cells;
//        final Queue<Cell> cellQueue = CELL_QUEUE;
//        final float lightAbsorption = lightDistribution.getLightAbsorption();
//
//        for (int i = 0, w = width; i < w; ++i) {
//            for (int j = 0, h = height; j < h; ++j) {
//                cells[i][j].sunLight = 0;
//            }
//        }
//
//        for (LightSource lightSource : lightSources) {
//            Cell lightSourceCell = lightSource.cell;
//            int light = nextLight(lightSource.light, lightDistribution);
//            for (Direction dir : Direction.ALL) {
//                Cell neib = getCell(lightSourceCell, dir);
//                if (neib != null && !(neib.object instanceof LightSource)) {
//                    cellQueue.addLast(neib);
//                    if (light > neib.sunLight) {
//                        neib.updateSunLight(light);
//                    }
//                }
//            }
//        }
//
//        while (cellQueue.notEmpty()) {
//            Cell cell = cellQueue.removeFirst();
//            int light = nextLight(cell.getSunLight(), lightDistribution);
//            if (light == cell.getSunLight()) --light;
//            if (light <= 0)
//                continue;
//
//            for (Direction dir : Direction.ALL) {
//                Cell neib = getCell(cell, dir);
//                if (neib != null && !(neib.object instanceof LightSource) && neib.sunLight < light) {
//                    neib.updateSunLight(light);
//                    cellQueue.addLast(neib);
//                }
//            }
//        }
//    }

//    private int nextLight(int light, LightDistribution lightDistribution) {
//        return (int) (light * lightDistribution.getLightAbsorption());
//    }
//
//    public void updateSunLightOld(LightDistribution lightDistribution) {
//        final Cell[][] cells = this.cells;
//        final int[] treeHeights = this.treeHeights;
//        // go from top to bottom and calculate sunlight for each cell
//        // and write to treeHeights the first cell is not under sun
//        for (int i = 0, w = width; i < w; ++i) {
//            Cell[] col = cells[i];
//            boolean treeHeightUpdated = false;
//            for (int h = height, j = h - 1; j >= 0; --j) {
//                Cell cell = col[j];
//                int light = lightDistribution.getLight(i, j);
//
//                if (j == h - 1) {
//                    cell.underSun = true;
//                    cell.updateSunLight(light);
//                } else {
//                    Cell upperCell = cells[i][j + 1];
//                    cell.underSun = upperCell.underSun && upperCell.object == null;
//                    cell.updateSunLight(cell.underSun ? light : upperCell.sunLight - 2);
//
//                    if (!cell.underSun && !treeHeightUpdated) {
//                        treeHeights[i] = j;
//                        treeHeightUpdated = true;
//                    }
//                }
//            }
//            if (!treeHeightUpdated) {
//                treeHeights[i] = -1;
//            }
//        }
//
//        for (int i = 0, w = width; i < w; ++i) {
//            int l = i - 1;
//            if (l < 0) l = w - 1;
//            int r = i + 1;
//            if (r == w) r = 0;
//
//            int treeHeight = treeHeights[l];
//            for (; treeHeight >= 0; --treeHeight) {
//                calcLight(cells[l][treeHeight], cells[i][treeHeight].sunLight - 2, Direction.left);
//            }
//            treeHeight = treeHeights[r];
//            for (; treeHeight >= 0; --treeHeight) {
//                calcLight(cells[r][treeHeight], cells[i][treeHeight].sunLight - 2, Direction.right);
//            }
//        }
//    }

    // by world coords
    public Cell findCell(int x, int y) {
        int cellX = x / cellSize;
        if (cellX < 0 || cellX >= width) {
            return null;
        }
        int cellY = y / cellSize;
        if (cellY < 0 || cellY >= height) {
            return null;
        }
        return cells[cellX][cellY];
    }

    public Cell getCell(Cell cell, Direction dir) {
        int[] dc = NEIGHBORS[dir.getCode()];
        int y = cell.y + dc[1];
        if (y < 0 || y >= height) {
            return null;
        }
        int x = cell.x + dc[0];
        if (x < 0) x = width - 1;
        if (x == width) x = 0;
        return cells[x][y];
    }

    public void addCellObject(LivingCell cellObject, int x, int y) {
        addCellObject(cellObject, cells[x][y]);
    }

    public void addCellObject(LivingCell cellObject, Cell cell) {
//        cellObject.setCell(cell);
        cell.object = cellObject;
    }

//    public void moveCellObjectTo(LivingCell cellObject, Cell target) {
//        Cell old = cellObject.cell;
//        if (old != null) {
//            old.object = null;
//        }
//        cellObject.setCell(target);
//        target.object = cellObject;
//        if (cellObject instanceof TreePart && ((TreePart) cellObject).type == TreePartType.SHOOT) {
//            if (old != null) {
//                chunkManager.removeObject(cellObject, old.x, old.y);
//            }
//            chunkManager.addObject(cellObject, target.x, target.y);
//        }
//    }

//    public void removeCellObject(CellObject cellObject) {
//        Cell cell = cellObject.cell;
//        cellObject.setCell(null);
//        if (cell.object == cellObject) {
//            cell.object = null;
//        }
//        if (cellObject instanceof TreePart && ((TreePart) cellObject).type == TreePartType.SHOOT) {
//            chunkManager.removeObject(cellObject, cell.x, cell.y);
//        }
//        if (cellObject instanceof LightSource) {
//            lightSources.removeValue((LightSource) cellObject, true);
//        }
//    }

    /**
     * x1, x2 may be < 0 and > width
     */
//    public Array<CellObject> findObjectsUnderLine(int x1, int y1, int x2, int y2) {
//        return chunkManager.findObjectsUnderLine(x1, y1, x2, y2);
//    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCellSize() {
        return cellSize;
    }
}
