package uk.co.oliwali.MultiHome;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import uk.co.oliwali.DataLog.util.DataLogAPI;

public class MultiHome extends JavaPlugin {
	
	public String name;
	public String version;
	private Permission permissions;
	public Config config;
	private boolean usingDataLog;

	public void onDisable() {
		Util.info("Version " + version + " disabled!");
	}

	public void onEnable() {
		name = this.getDescription().getName();
        version = this.getDescription().getVersion();
        config = new Config(this);
        permissions = new Permission(this);
        setupDatabase();
        Plugin dl = getServer().getPluginManager().getPlugin("DataLog");
        if (dl != null)
            this.usingDataLog = true;
        Util.info("Version " + version + " enabled!");
	}
	
	private void setupDatabase() {
        try {
            getDatabase().find(Home.class).findRowCount();
        } catch (PersistenceException ex) {
            System.out.println("Installing database for " + getDescription().getName() + " due to first time usage");
            installDDL();
        }
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Home.class);
        return list;
    }
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
		
		String prefix = cmd.getName();
		Player player = (Player) sender;
		String name   = player.getName();
		String world  = player.getWorld().getName();
		Home home;
		
		if (prefix.equalsIgnoreCase("home") && permissions.home(player)) {
			if (args.length > 0) {
				String command = args[0];
				
				//Setting home
				if (command.equalsIgnoreCase("set")) {
					home = getDatabase().find(Home.class).where().ieq("world", world).ieq("player", name).findUnique();
			        
					if (home != null)
						getDatabase().createSqlUpdate("DELETE FROM multihome WHERE world='" + world + "' AND player='" + name + "'").execute();
					
					home = new Home();
					home.setPlayer(name);
					home.setLocation(player.getLocation());					
			        getDatabase().save(home);
			        Util.sendMessage(player, "&aYour home has been set in &7" + config.getAlias(world));
				}
				
				//List homes
				else if (command.equalsIgnoreCase("list")) {
					DecimalFormat round = new DecimalFormat("#,##0.0");
					List<Home> homes = getDatabase().find(Home.class).where().ieq("player", name).findList();
					if (homes.isEmpty()) {
						Util.sendMessage(player, "&cYou do not have any homes set");
			        	Util.sendMessage(player, "&7Use &c/home set&7 to set a home");
					}
					else {
						Util.sendMessage(player, "&aList of homes for: &7" + name);
						for (Home playerHome : homes.toArray(new Home[0]))
							Util.sendMessage(player, "&aWorld:&7 " + config.getAlias(playerHome.getWorld()) + " &aLocation:&7 " + round.format(playerHome.getX()) + ", " + round.format(playerHome.getY()) + ", " + round.format(playerHome.getZ()));
					}
				}
				
				//Get help
				else if (command.equalsIgnoreCase("help")) {
					Util.sendMessage(player, "&a--------------------&f MultiHome &a--------------------");
					Util.sendMessage(player, "&a/home&7 - Go to your home in the world you are in");
					Util.sendMessage(player, "&a/home <world> - Go to your home in the specified world");
					Util.sendMessage(player, "&a/home set&7 - Set your home in the world you are in");
					Util.sendMessage(player, "&a/home list&7 - List all homes you have set");
					if (permissions.admin(player))
						Util.sendMessage(player, "&a/home player <player> [world]&7 - Go to a player's home in a world");
				}
				
				//Go to another players home
				else if (command.equalsIgnoreCase("player") && permissions.admin(player)) {
					if (args.length > 1) {
						String homePlayer = args[1];
						//World specified?
						if (args.length > 2)
							world = args[2];
						home = getDatabase().find(Home.class).where().ieq("player", homePlayer).ieq("world", config.getWorld(world)).findUnique();
						if (home == null)
							Util.sendMessage(player, "&c" + homePlayer + " does not have a home set in &7" + config.getAlias(world));
						else
							goHome(player, home);
					}
					else
						Util.sendMessage(player, "&cPlease provide the name of a player to go to their home!");
				}
				
				//Go home in specified world
				else {
					home = getDatabase().find(Home.class).where().ieq("player", name).ieq("world", config.getWorld(args[0])).findUnique();
					if (home == null) {
						Util.sendMessage(player, "&cYou do not have a home set in &7" + config.getAlias(args[0]));
			        	Util.sendMessage(player, "&7Use &c/home set&7 to set a home");
					}
					else
						goHome(player, home);
				}
				
			}
			
			//Go home
			else {
				home = (Home) getDatabase().find(Home.class).where().ieq("world", world).ieq("player", name).findUnique();
		        if (home == null) {
		        	Util.sendMessage(player, "&cYou do not have a home set in &7" + config.getAlias(world));
		        	Util.sendMessage(player, "&7Use &c/home set&7 to set a home");
		        }
		        else
		        	goHome(player, home);
			}
			return true;
			
		}
		return false;
	}
	
	private void goHome(Player player, Home home) {
		Location loc = home.getLocation();
		player.teleport(loc);
		if (home.getPlayer().equalsIgnoreCase(player.getName())) {
			if (usingDataLog) DataLogAPI.addEntry(this, "Own Home", player, home.getLocation(), "");
			Util.sendMessage(player, "&aWelcome to your home in &7" + config.getAlias(loc.getWorld()));
		}
		else {
			if (usingDataLog) DataLogAPI.addEntry(this, "Other Home", player, home.getLocation(), home.getPlayer());
			Util.sendMessage(player, "&aWelcome to &7" + home.getPlayer() + "&a's home in &7" + config.getAlias(loc.getWorld()));
		}
	}
}
