package com.gordonfromblumberg.games.core.common.factory;

import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.TestConfigManager;

public class TestFactory extends AbstractFactory {
    private TestFactory() {
    }

    public static void init() {
        AbstractFactory.instance = new TestFactory();
    }

    @Override
    protected ConfigManager createConfigManager() {
        return new TestConfigManager();
    }
}
