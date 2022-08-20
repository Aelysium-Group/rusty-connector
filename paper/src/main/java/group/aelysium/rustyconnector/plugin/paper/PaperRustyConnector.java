package group.aelysium.rustyconnector.plugin.paper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.*;
import java.util.*;

public final class PaperRustyConnector extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {

        log("Started Successfully!");
    }

    @Override
    public void onDisable() {
        log("Shutting down...");
    }

    /**
     * Register all of the plugin's commands
     */
    public void registerCommands() {
    }

    /**
     * Register all of the plugin's Event Listeners
     */
    public void registerEvents() {
    }

    /**
     * Register the plugin's config files
     */
    public boolean registerConfigs() {
        return true;
    }

    /**
     * Creates a custom config file using a template from resources/config
     * @param configName The name of the config template to get
     */
    public static JsonObject createCustomConfig(String configName) {
        JavaPlugin plugin = PaperRustyConnector.getProvidingPlugin(PaperRustyConnector.class);
        File customConfigFile = new File(plugin.getDataFolder(), configName); // Load the custom config from the plugins data file
        log("> > " + "Searching for "+configName);
        if (!customConfigFile.exists()) { // Check if the custom config actually exists
            log("> > " + configName + " could not be found. Making it now!");
            plugin.saveResource(configName, false); // If it doesn't, create it
            log("> > " + configName + " was successfully generated!");
        } else {
            log("> > " + configName + " was found!");
        }

        if (customConfigFile.exists()) { // Re-check if the custom config exists
            try {
                Gson gson = new Gson();
                return gson.fromJson(new FileReader(customConfigFile), JsonObject.class);
            } catch (FileNotFoundException e) {
                log("> > " + configName + " could not be loaded!");
            }
        } else {
            log("> > " + configName + " still doesn't exist!");
        }
        return null;
    }

    /**
     * Reload a config
     * @param name The name of the config to reload
     */
    public void reloadConfig(String name) {/*
        File config = new File(getDataFolder(), name);
        Gson gson = new Gson();
        try {
            this.configs.put(name,gson.fromJson(new FileReader(config), JsonObject.class));
        } catch (FileNotFoundException e) {
            log(name + " could not be loaded!");
        }*/
    }

    /**
     * Save a config
     * @param name Name of the config to save to
     * @param data The data to save
     */
    public void saveConfig(String name, Map<String, Object> data) {
        FileConfiguration fileConfiguration;
        File config = new File(getDataFolder(), name);
        fileConfiguration = YamlConfiguration.loadConfiguration(config);
        for(Map.Entry<String, Object> entry : data.entrySet()) {
            fileConfiguration.set(entry.getKey(),entry.getValue());
        }
        try {
            fileConfiguration.save(config);
        } catch (IOException e) {
            log("Failed to save "+name); // shouldn't really happen, but save throws the exception
        }
    }

    /**
     * Sends a String to the log
     * @param log The text to be logged
     */
    public static void log(String log) {
        System.out.println("[ScreenControl] " + log);
    }

    public static InputStream getResourceAsStream(String filename, PaperRustyConnector rustyConnector) {
        return rustyConnector.getClassLoader().getResourceAsStream(filename);
    }
}
