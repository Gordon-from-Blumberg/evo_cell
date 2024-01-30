package com.gordonfromblumberg.games.core.evocell.world;

public class WorldStatistic {
    int worldEnergy;
    int worldOrganics;
    int worldMinerals;
    int cellCount;
    int maxCellCount;
    int totalCellEnergy;
    int totalCellOrganics;
    int totalCellMinerals;
    int currentMaxCellAge;
    int maxCellAge;
    int maxTotalCellEnergy;
    int maxTotalCellOrganics;
    int maxCellOrganics;
    int maxTotalCellMinerals;
    int maxCellMinerals;

    void resetForNewTurn() {
        totalCellEnergy = 0;
        totalCellOrganics = 0;
        totalCellMinerals = 0;
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
        if (totalCellEnergy > maxTotalCellEnergy)
            maxTotalCellEnergy = totalCellEnergy;
        if (totalCellOrganics > maxTotalCellOrganics)
            maxTotalCellOrganics = totalCellOrganics;
        if (totalCellMinerals > maxTotalCellMinerals)
            maxTotalCellMinerals = totalCellMinerals;
    }
}
