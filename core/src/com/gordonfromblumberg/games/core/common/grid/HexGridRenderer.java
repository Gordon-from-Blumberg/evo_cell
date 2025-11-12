package com.gordonfromblumberg.games.core.common.grid;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.gordonfromblumberg.games.core.common.utils.Assets;

public class HexGridRenderer {
    /**
     * Offset from left edge of tile to left edge of a hex
     */
    final int xOffset;
    /**
     * Offset from bottom edge of tile to bottom edge of a hex
     */
    final int yOffset;
    private final TextureAtlas atlas;
    private final ObjectMap<String, TextureRegion> regionMap = new ObjectMap<>();

    public HexGridRenderer(int xOffset, int yOffset, String atlasName) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.atlas = Assets.get(atlasName, TextureAtlas.class);
    }

    public HexGridRenderer(String atlasName) {
        this(0, 0, atlasName);
    }

    public void render(HexGrid hexGrid, Batch batch) {
        for (int i = 0, n = hexGrid.layerCount; i < n; ++i) {
            renderLayer(batch, hexGrid, i);
        }
    }

    public void drawHover(HexGrid hexGrid, Hex hex, Batch batch) {
        final TextureRegion region = get("hover");
        if (region != null) {
            float halfWidth = hexGrid.hexWidth / 2f;
            batch.draw(region,
                    hex.x * hexGrid.hexWidth - halfWidth - xOffset + hex.y % 2 * halfWidth,
                    hex.y * hexGrid.rowDist - hexGrid.hexHeight / 2f - yOffset);
        }
    }

    private void renderLayer(Batch batch, HexGrid hexGrid, int layer) {
        final int hexWidth = hexGrid.hexWidth;
        final int hexHeight = hexGrid.hexHeight;
        final float halfWidth = hexWidth / 2f;
        final float halfHeight = hexHeight / 2f;
        final int xOffset = this.xOffset;
        final int yOffset = this.yOffset;
        final float dy = hexGrid.rowDist;

        for (int y = hexGrid.rows.length - 1; y >= 0; --y) {
            final HexRow row = hexGrid.rows[y];
            final IntMap<Hex> hexes = row.hexes;
            for (int x = row.minX, maxX = row.maxX; x <= maxX; ++x) {
                final Hex hex = hexes.get(x);
                final TextureRegion region = get(hex.tiles[layer]);
                if (region != null) {
                    batch.draw(
                            region,
                            x * hexWidth - halfWidth - xOffset + y % 2 * halfWidth,
                            y * dy - halfHeight - yOffset);
                }
            }
        }
    }

    private TextureRegion get(String regionName) {
        if (regionMap.containsKey(regionName))
            return regionMap.get(regionName);

        TextureRegion region = atlas.findRegion(regionName);
        regionMap.put(regionName, region);
        return region;
    }
}
