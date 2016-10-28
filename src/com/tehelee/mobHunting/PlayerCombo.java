package com.tehelee.mobHunting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class PlayerCombo
{
	private static List<String> killStreakNames = null;
	
	public static void writeDefaultConfig()
	{
		Main.config.addDefault("ComboDecayInMillis", 300000);
		
		if (!Main.config.isList("KillStreakNames"))
		{
			List<String> names = new ArrayList<String>();
			
			names.add(0, "§aKilling Spree§r");
			names.add(1, "§bRampage§r");
			names.add(2, "§dDominating§r");
			names.add(3, "§6Unstoppable§r");
			names.add(4, "§eGodlike§r");
			
			Main.config.addDefault("KillStreakNames", names);
			
			killStreakNames = names;
		}
		else
		{
			killStreakNames = Main.config.getStringList("KillStreakNames");
		}
	}
	
	public UUID uuid;
	public int combo;
	public long lastUpdate;
	
	public PlayerCombo(UUID id)
	{
		uuid = id;
		combo = 0;
		lastUpdate = System.currentTimeMillis();
	}
	
	public PlayerCombo(Player player)
	{
		uuid = player.getUniqueId();
		combo = 0;
		lastUpdate = System.currentTimeMillis();
	}
	
	public Player getPlayer()
	{
		return Main.instance.getServer().getPlayer(uuid);
	}
	
	public boolean isOffline()
	{
		return (null == getPlayer());
	}
	
	public int incrementCombo()
	{
		updateCombo();
		int before = getMultiplier();
		
		combo++;
		
		int after = getMultiplier();
		
		if (after > before)
		{
			String msg = getMultiplierName();
			
			msg = msg.substring(0, 2) + "§l" + msg.substring(2);
			
			Player player = getPlayer();
			if (player != null)
			{
				Main.message(player, msg + "!" + ChatColor.RESET, true);
			}
			Main.message(null, ChatColor.GRAY + ChatColor.stripColor(player.getName() + "has achieved a "+ msg) + ChatColor.RESET, true);
		}
		
		return combo;
	}
	
	private void updateCombo()
	{
		long current = System.currentTimeMillis();
		
		long difference = (current - lastUpdate);
		
		long decay = Main.config.getLong("ComboDecayInMillis", 300000);
		
		if ((decay > 0) && (difference > decay))
		{
			combo = 0;
			
			lastUpdate = current;
		}
	}
	
	public void resetCombo()
	{
		combo = 0;
		lastUpdate = System.currentTimeMillis();
	}
	
	public int getMultiplier()
	{
		return Math.min(killStreakNames.size(), (int) Math.floor(combo / 5.0));
	}
	
	public String getMultiplierName()
	{
		if (killStreakNames == null) return "";
		
		int multiplier = getMultiplier()-1;
		
		if (multiplier < 0) return "";

		if (multiplier >= killStreakNames.size())
			return ("Kill Streak x" + multiplier);
		else
			return killStreakNames.get(multiplier);
	}
}
