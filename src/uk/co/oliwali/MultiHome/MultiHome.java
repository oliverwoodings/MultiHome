package uk.co.oliwali.MultiHome;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiHome extends JavaPlugin {
	
	public String name;
	public String version;
	public static final Logger log = Logger.getLogger("Minecraft");
	private Permission permissions;

	public void onDisable() {
		sendMessage("info", "Version " + version + " disabled!");
	}

	public void onEnable() {
		name = this.getDescription().getName();
        version = this.getDescription().getVersion();
        permissions = new Permission(this);
        setupDatabase();
        sendMessage("info", "Version " + version + " enabled!");
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
		
		if (prefix.equalsIgnoreCase("home") && permissions.home(player)) {
			if (args.length > 0) {
				String command = args[0];
				
				//Setting home
				if (command.equalsIgnoreCase("set")) {
					Home home = getDatabase().find(Home.class).where().ieq("world", world).ieq("player", name).findUnique();
			        
					if (home != null)
						getDatabase().createSqlUpdate("DELETE FROM multihome WHERE world='" + world + "' AND player='" + name + "'").execute();
					
					home = new Home();
					home.setPlayer(name);
					home.setLocation(player.getLocation());					
			        getDatabase().save(home);
			        sendMessage(player, "`aYour home has been set in `7" + world);
				}
				
				//List homes
				else if (command.equalsIgnoreCase("list")) {
					DecimalFormat round = new DecimalFormat("#,##0.0");
					List<Home> homes = getDatabase().find(Home.class).where().ieq("player", name).findList();
					if (homes.isEmpty()) {
						sendMessage(player, "`cYou do not have any homes set");
			        	sendMessage(player, "`7Use `c/home set`7 to set a home");
					}
					else {
						sendMessage(player, "`aList of homes for: `7" + name);
						for (Home home : homes.toArray(new Home[0]))
							sendMessage(player, "`aWorld:`7 " + home.getWorld() + " `aLocation:`7 " + round.format(home.getX()) + ", " + round.format(home.getY()) + ", " + round.format(home.getZ()));
					}
				}
				
				//Get help
				else if (command.equalsIgnoreCase("help")) {
					sendMessage(player, "`a--------------------`f MultiHome `a--------------------");
					sendMessage(player, "`a/home`7 - Go to your home in the world you are in");
					sendMessage(player, "`a/home set`7 - Set your home in the world you are in");
				}
			}
			//Go home
			else {
				Home home = (Home) getDatabase().find(Home.class).where().ieq("world", world).ieq("player", name).findUnique();
		        if (home == null) {
		        	sendMessage(player, "`cYou do not have a home set in `7" + world);
		        	sendMessage(player, "`7Use `c/home set`7 to set a home");
		        }
		        else {
		        	player.teleport(home.getLocation());
		        	sendMessage(player, "`aWelcome to your home in `7" + world);
		        }
			}
			return true;
			
		}
		return false;
	}
	
	public void sendMessage(Player player, String msg) {
		player.sendMessage(replaceColors(msg));
	}
	
	public void sendMessage(String level, String msg) {
		msg = "[" + name + "] " + msg;
		if (level == "info")
			log.info(msg);
		else
			log.severe(msg);
	}
	
    public String replaceColors(String str) {
        str = str.replace("`c", ChatColor.RED.toString());
        str = str.replace("`4", ChatColor.DARK_RED.toString());
        str = str.replace("`e", ChatColor.YELLOW.toString());
        str = str.replace("`6", ChatColor.GOLD.toString());
        str = str.replace("`a", ChatColor.GREEN.toString());
        str = str.replace("`2", ChatColor.DARK_GREEN.toString());
        str = str.replace("`b", ChatColor.AQUA.toString());
        str = str.replace("`8", ChatColor.DARK_AQUA.toString());
        str = str.replace("`9", ChatColor.BLUE.toString());
        str = str.replace("`1", ChatColor.DARK_BLUE.toString());
        str = str.replace("`d", ChatColor.LIGHT_PURPLE.toString());
        str = str.replace("`5", ChatColor.DARK_PURPLE.toString());
        str = str.replace("`0", ChatColor.BLACK.toString());
        str = str.replace("`8", ChatColor.DARK_GRAY.toString());
        str = str.replace("`7", ChatColor.GRAY.toString());
        str = str.replace("`f", ChatColor.WHITE.toString());
        return str;
    }

}
