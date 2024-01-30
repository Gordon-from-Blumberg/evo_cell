package com.gordonfromblumberg.games.core.evocell.world;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.ui.UpdatableLabel;
import com.gordonfromblumberg.games.core.common.world.WorldUIRenderer;

import java.util.function.Supplier;

public class GameWorldUIRenderer extends WorldUIRenderer<GameWorld> {

    public GameWorldUIRenderer(SpriteBatch batch, GameWorld world, Supplier<Vector3> viewCoords) {
        super(batch, world, viewCoords);

        final AssetManager assets = Main.getInstance().assets();
        final Skin skin = assets.get("ui/uiskin.json", Skin.class);
        Window worldStatisticWindow = createWorldStatisticWindow(skin);
        stage.addActor(worldStatisticWindow);
    }

    private Window createWorldStatisticWindow(Skin skin) {
        final Window window = new Window("World statistic", skin);
        window.setWidth(300f);
        window.add("Resource");
        window.add("Total in world");
        window.add("Total in cells");

        window.row();
        window.add("Energy");
        window.add(new UpdatableLabel(skin, () -> world.statistic.worldEnergy));
        window.add(new UpdatableLabel(skin, () -> world.statistic.totalCellEnergy));

        window.row();
        window.add("Organics");
        window.add(new UpdatableLabel(skin, () -> world.statistic.worldOrganics));
        window.add(new UpdatableLabel(skin, () -> world.statistic.totalCellOrganics));

        window.row();
        window.add("Minerals");
        window.add(new UpdatableLabel(skin, () -> world.statistic.worldMinerals));
        window.add(new UpdatableLabel(skin, () -> world.statistic.totalCellMinerals));

        window.row().height(10f);
        window.add();
        window.row();
        window.add("Cells");
        window.add(new UpdatableLabel(skin, () -> world.statistic.cellCount));
        return window;
    }
}
