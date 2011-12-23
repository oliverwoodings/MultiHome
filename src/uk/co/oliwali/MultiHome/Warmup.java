package uk.co.oliwali.MultiHome;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Warmup extends TimerTask {
	
	private Home home;
	public Player player;
	private int period;
	public Timer timer = new Timer();
	
	public Warmup(Home home, Player player) {
		this.home = home;
		this.player = player;
		
		//Largest period out of current world and destination world is the one to use
		if (Config.getWarmup(home.getWorld()) > Config.getWarmup(player.getWorld().getName())) period = Config.getWarmup(home.getWorld());
		else period = Config.getWarmup(player.getWorld().getName());
		
		//Check for existing warmups
		for (Warmup warmup : MultiHome.warmups) {
			if (warmup.player == player) {
				Util.sendMessage(player, "&cYou already have a home command warming up!");
				return;
			}
		}
		
		//Add to list and start timer
		MultiHome.warmups.add(this);
        timer.scheduleAtFixedRate(this, 0, 1000);
	}

	public void run() {
		
		//If warmup is done
		if (period == 0) {
			Location loc = home.getLocation();
			player.teleport(loc);
			if (home.getPlayer().equalsIgnoreCase(player.getName())) {
				Util.sendMessage(player, "&aWelcome to your home in &7" + Config.getAlias(loc.getWorld()));
			}
			else {
				Util.sendMessage(player, "&aWelcome to &7" + home.getPlayer() + "&a's home in &7" + Config.getAlias(loc.getWorld()));
			}
			MultiHome.warmups.remove(this);
			timer.cancel();
		}
		//Otherwise decrease period and notify player
		else {
			Util.sendMessage(player, "&a" + period + "...");
			period--;
		}
		
	}

}
