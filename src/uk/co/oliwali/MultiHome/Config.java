package uk.co.oliwali.MultiHome;

import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.util.config.Configuration;

public class Config {
	
	HashMap<String, String> aliases = new HashMap<String, String>();
	MultiHome plugin;
	Configuration config;

	public Config (MultiHome instance) {
		
		this.plugin = instance;
		this.config = plugin.getConfiguration();
		config.load();
		
		//If there are no aliases yet
		if (config.getKeys("aliases") == null) {
			World[] worlds = (World[]) plugin.getServer().getWorlds().toArray(new World[0]);
			for (World world : worlds)
				config.setProperty("aliases." + world.getName(), world.getName());
		}
		
		//Load aliases into hashmap
		String[] worlds = (String[]) config.getKeys("aliases").toArray(new String[0]);
		for (String world : worlds)
			aliases.put(config.getString("aliases." + world), world);
		
		//Attempt save
		if (!config.save())
			Util.log.severe("Error while writing to config.yml");

	}
	
	public String getWorld(String alias) {
		String world = aliases.get(alias);
		if (world == null)
			return alias;
		return world;
	}
	
	public String getAlias(String world) {
		int i = 0;
		for (String value : aliases.values().toArray(new String[0])) {
			if (value.equalsIgnoreCase(world))
				return aliases.keySet().toArray(new String[0])[i];
			i++;
		}
		return world;
	}
	public String getAlias(World world) {
		return getAlias(world.getName());
	}
}