package uk.co.oliwali.MultiHome;

import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.configuration.Configuration;

public class Config {
	
	public static HashMap<String, String> aliases = new HashMap<String, String>();
	public static HashMap<String, Integer> warmups = new HashMap<String, Integer>();
	MultiHome plugin;
	Configuration config;

	public Config (MultiHome instance) {
		
		plugin = instance;
		config = plugin.getConfig().getRoot();
		config.options().copyDefaults(true);
		plugin.saveConfig();
		
		//Load aliases into hashmap
		String[] worlds = (String[]) config.getConfigurationSection("aliases").getKeys(false).toArray(new String[0]);
		for (String world : worlds)
			aliases.put(config.getString("aliases." + world), world);
		
		//Load warmup/cooldown
		worlds = (String[]) config.getConfigurationSection("warmups").getKeys(false).toArray(new String[0]);
		for (String world : worlds)
			warmups.put(world.toLowerCase(), config.getInt("warmups." + world));
		
	}
	
	public static String getWorld(String alias) {
		String world = aliases.get(alias);
		if (world == null)
			return alias;
		return world;
	}
	
	public static int getWarmup(String world) {
		if (!warmups.containsKey(world.toLowerCase())) return 0;
		return warmups.get(world.toLowerCase());
	}
	
	public static String getAlias(String world) {
		int i = 0;
		for (String value : aliases.values().toArray(new String[0])) {
			if (value.equalsIgnoreCase(world))
				return aliases.keySet().toArray(new String[0])[i];
			i++;
		}
		return world;
	}
	public static String getAlias(World world) {
		return getAlias(world.getName());
	}
}