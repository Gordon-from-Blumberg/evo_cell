package com.gordonfromblumberg.games.core.evocell.world;

public class WorldStatistic {
    int worldEnergy;
    int worldOrganics;
    int worldMinerals;
    int botCount;
    int maxBotCount;
    int totalBotEnergy;
    int totalBotOrganics;
    int totalBotMinerals;
    int currentMaxBotAge;
    int maxBotAge;
    int maxTotalBotEnergy;
    int maxTotalBotOrganics;
    int maxBotOrganics;
    int maxTotalBotMinerals;
    int maxBotMinerals;
    int maxBotGeneration;

    void resetForNewTurn() {
        totalBotEnergy = 0;
        totalBotOrganics = 0;
        totalBotMinerals = 0;
        botCount = 0;
        currentMaxBotAge = 0;
        maxBotOrganics = 0;
        maxBotMinerals = 0;
        maxBotGeneration = 0;
    }

    void updateMaximums() {
        if (botCount > maxBotCount)
            maxBotCount = botCount;
        if (currentMaxBotAge > maxBotAge)
            maxBotAge = currentMaxBotAge;
        if (totalBotEnergy > maxTotalBotEnergy)
            maxTotalBotEnergy = totalBotEnergy;
        if (totalBotOrganics > maxTotalBotOrganics)
            maxTotalBotOrganics = totalBotOrganics;
        if (totalBotMinerals > maxTotalBotMinerals)
            maxTotalBotMinerals = totalBotMinerals;
    }
}
