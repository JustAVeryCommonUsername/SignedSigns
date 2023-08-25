package org.tenmillionapples.signedsigns;

import org.bukkit.plugin.java.JavaPlugin;

public final class SignedSigns extends JavaPlugin {
    private static SignedSigns instance;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new Events(), this);
        saveDefaultConfig();

        instance = this;

        new Metrics(this, 19638);
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static SignedSigns getInstance() {
        return instance;
    }
}
