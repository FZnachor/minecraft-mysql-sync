package de.ld338.mysqlsync;

import de.ld338.mysqlsync.commands.SyncCommand;
import de.ld338.mysqlsync.tools.MySQL;
import de.ld338.mysqlsync.tools.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MySQLSync extends JavaPlugin implements Listener {

    private static final Logger LOGGER = Bukkit.getLogger();
    private static final String[] TABLES = {
            "inventory", "enderchest", "armor", "achievements", "xp", "stats", "player_state", "player_effects"
    };

    private Connection connection;
    private final Map<Player, Boolean> frozen = new HashMap<>();

    @Override
    public void onEnable() {
        // This is needed to register the events
        // If you are copying code out of this class, you need to add this line to your main class
        Bukkit.getPluginManager().registerEvents(this, this);

        // This command is not needed for the plugin to work, but it is a nice feature to have
        getCommand("sync-mysql").setExecutor(new SyncCommand());

        loadConfig();

        connection = MySQL.getConnection();

        if (getConfig().getBoolean("recreate_tables")) {
            recreateTables();
        }

        for (String table : TABLES) {
            MySQL.createTable(table, "player_uuid VARCHAR(36) PRIMARY KEY, content LONGTEXT");
        }

        // If you edit this Code and want to make an own Table, you can add it here
        // MySQL.createTable("yourtablename", "player_uuid VARCHAR(36) PRIMARY KEY, CONTENTS LONGTEXT");
        // The first argument is the name of the table, the second argument is the columns of the table
        // It's as easy as that
    }

    @Override
    public void onDisable() {
        // Save all player data before shutting down. This is important to prevent data loss
        if (getConfig().getBoolean("sync_to_db")) {
            Bukkit.getOnlinePlayers().forEach(PlayerUtil::saveData);
        }
        MySQL.closeConnection();
    }

    private void loadConfig() {
        // If you are copying code out of this class, you need to add this line to your main class
        // Also don't forget to add the config.yml to your resources folder
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Set the MySQL connection details
        // Don't forget to add these values to your config.yml
        MySQL.setConfig(
                getConfig().getString("mysql.host"),
                getConfig().getInt("mysql.port"),
                getConfig().getString("mysql.database"),
                getConfig().getString("mysql.username"),
                getConfig().getString("mysql.password")
        );
    }

    private void recreateTables() {
        LOGGER.info("Deleting tables...");
        try (PreparedStatement statement = connection.prepareStatement(
                "DROP TABLE IF EXISTS " + String.join(", ", TABLES))) {
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to drop tables", e);
        }
    }


    // If the event is not triggered, make sure you have registered the events in the onEnable method
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Make sure the player's inventory is empty when they join
        Player player = event.getPlayer();
        freezePlayer(player);
        if (getConfig().getBoolean("sync_from_db")) {
            long startTime = System.currentTimeMillis();
            PlayerUtil.loadData(player);
            long endTime = System.currentTimeMillis();
            LOGGER.info(String.format("Loaded data for %s in %dms", player.getName(), (endTime - startTime)));
        }
        unfreezePlayer(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            frozen.remove(player);
        } else {
            if (getConfig().getBoolean("sync_to_db")) {
                long startTime = System.currentTimeMillis();
                PlayerUtil.saveData(player);
                long endTime = System.currentTimeMillis();
                LOGGER.info(String.format("Saved data for %s in %dms", player.getName(), (endTime - startTime)));
            }
        }
    }

    private boolean isFrozen(Player player) {
        return frozen.containsKey(player);
    }

    public void unfreezePlayer(Player player) {
        frozen.remove(player);
    }

    public void freezePlayer(Player player) {
        frozen.put(player, true);
    }

}
