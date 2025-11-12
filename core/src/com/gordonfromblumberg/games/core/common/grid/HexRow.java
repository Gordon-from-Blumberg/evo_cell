package com.gordonfromblumberg.games.core.common.grid;

import com.badlogic.gdx.utils.IntMap;

import java.util.Iterator;

public class HexRow implements Iterable<Hex> {
    private final int y;
    final int minX;
    final int maxX;
    final IntMap<Hex> hexes;

    private int iteratorIndex;

    HexRow(int y, int minX, int maxX) {
        this.y = y;
        this.minX = minX;
        this.maxX = maxX;
        this.hexes = new IntMap<>(maxX - minX + 1);
    }

    private final Iterator<Hex> iterator = new Iterator<>() {
        @Override
        public boolean hasNext() {
            return iteratorIndex <= maxX;
        }

        @Override
        public Hex next() {
            return hexes.get(iteratorIndex++);
        }
    };

    @Override
    public Iterator<Hex> iterator() {
        iteratorIndex = minX;
        return iterator;
    }
}
