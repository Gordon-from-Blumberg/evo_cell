package com.gordonfromblumberg.games.core.evocell.world;

public class WorldStatistic {
    int worldEnergy;
    int worldOrganics;
    int worldMinerals;
    int cellCount;
    int maxCellCount;
    int cellEnergy;
    int cellOrganics;
    int cellMinerals;
    int currentMaxCellAge;
    int maxCellAge;
    int maxTotalCellEnergy;
    int maxTotalCellOrganics;
    int maxCellOrganics;
    int maxTotalCellMinerals;
    int maxCellMinerals;

    void resetForNewTurn() {
        cellEnergy = 0;
        cellOrganics = 0;
        cellMinerals = 0;
        cellCount = 0;
        currentMaxCellAge = 0;
        maxCellOrganics = 0;
        maxCellMinerals = 0;
    }

    void updateMaximums() {
        if (cellCount > maxCellCount)
            maxCellCount = cellCount;
        if (currentMaxCellAge > maxCellAge)
            maxCellAge = currentMaxCellAge;
        if (cellEnergy > maxTotalCellEnergy)
            maxTotalCellEnergy = cellEnergy;
        if (cellOrganics > maxTotalCellOrganics)
            maxTotalCellOrganics = cellOrganics;
        if (cellMinerals > maxTotalCellMinerals)
            maxTotalCellMinerals = cellMinerals;
    }
}
