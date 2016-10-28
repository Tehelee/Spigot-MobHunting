package com.tehelee.mobHunting;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.md_5.bungee.api.ChatColor;

public class CustomMob
{
	private static final String _name = "DisplayName";
	private static final String _rarity = "Rarity";
	private static final String _reward = "Reward";
	private static final String _multiplier = "Multiplier";
	private static final String _prefix = "Prefix";
	
	private static Map<String,CustomMob> REGISTRY = new HashMap<String,CustomMob>();
	private static Map<String,String> LOOKUP = new HashMap<String,String>();
	
	public static String[] getMobList()
	{
		return LOOKUP.keySet().toArray(new String[0]);
	}
	
	private static String nameToId(String name)
	{
		if (name == null) return null;
		
		String result = ChatColor.stripColor(name.trim()).replaceAll("\'", "").replaceAll("\"", "");
		
		if (result.isEmpty()) return null;
		
		char[] a = result.toCharArray();
		
		result = Character.toString(a[0]);
		
		for (int i = 1; i < a.length; i++)
		{
			if (a[i] == ' ')
				result += Character.toUpperCase(a[++i]);
			else
				result+= Character.toLowerCase(a[i]);
		}
		
		return (result.length() == 0) ? null : result;
	}
	
	public static void writeDefaultConfig()
	{
		File file = new File(Main.instance.getDataFolder(), "//" + "custom_mobs.yml");
		FileConfiguration custom = YamlConfiguration.loadConfiguration(file);
		
		if (custom.getKeys(false).size() == 0)
		{
			String name;
			name = "§6Tehelee's Minion"; 
			custom.set(nameToId(name), new CustomMob(name, "", 3, 3, 100).getConfig());
			
			name = "§6Skeleton King";
			custom.set(nameToId(name), new CustomMob(name, "§6The", 3, 5, 250).getConfig());
		}
		
		try
		{
			custom.save(file);
		}
		catch (IOException e)
		{
			Main.message(null, "Failed to save custom mobs!", true);
		}
	}
	
	public static String addCustomMob(String name, int rarity, double reward, int multiplier, String prefix)
	{
		if ((name == null) || (prefix == null)) return "Invalid custom mob name / prefix information";
		
		String id = nameToId(name);
		
		if (id == null) return "Invalid custom mob name / prefix information";
		
		CustomMob mob = new CustomMob(name, prefix, rarity, multiplier, reward);
		
		File file = new File(Main.instance.getDataFolder(), "//" + "custom_mobs.yml");
		FileConfiguration custom = YamlConfiguration.loadConfiguration(file);
		
		boolean replace = custom.isConfigurationSection(id);
		
		custom.set(id, mob.getConfig());
		
		try
		{
			custom.save(file);
		}
		catch (IOException e)
		{
			Main.message(null, "Failed to save custom mobs!", true);
			
			return "Encountered an error when attempting to add your custom mob.";
		}
		
		LOOKUP.put(name, id);
		REGISTRY.put(name, mob);
		
		return String.format("%1$s%2$s %3$s§r\nRarity: %4$s§r\nReward: §2$%5$.2f§r\nMultiplier: %6$d", (replace ? "Replaced" : "Added"), (prefix.isEmpty() ? "" : " "+prefix), name, MobType.getRarityColorFromInt(rarity)+rarity, reward, multiplier);
	}
	
	public static String removeCustomMob(String name)
	{
		if (!LOOKUP.containsKey(name)) return "Could not find the custom mob \"" + name + "§r\"";
		
		String id = LOOKUP.get(name);
		
		File file = new File(Main.instance.getDataFolder(), "//" + "custom_mobs.yml");
		FileConfiguration custom = YamlConfiguration.loadConfiguration(file);
		
		if (!custom.isConfigurationSection(id)) return "Could not find the custom mob \"" + name + "§r\"";
		
		custom.set(id, null);
		
		REGISTRY.remove(name);
		LOOKUP.remove(name);
		
		return "Removed the custom mob \"" + name + "§r\"";
	}
	
	public static void populateMobs()
	{
		File file = new File(Main.instance.getDataFolder(), "//" + "custom_mobs.yml");
		FileConfiguration custom = YamlConfiguration.loadConfiguration(file);
		
		Set<String> mobs = custom.getKeys(false);
		
		for(String s : mobs)
		{
			if ((s == null) || !custom.isConfigurationSection(s)) continue;
			
			ConfigurationSection mob = custom.getConfigurationSection(s);
			
			String name = mob.getString(_name, null);
			String prefix = mob.getString(_prefix, null);
			int rarity = Math.min(3, mob.getInt(_rarity, -1));
			int multiplier = Math.min(3, mob.getInt(_multiplier, -1));
			double reward = mob.getDouble(_reward, -1);
			
			if ((name == null) || (prefix == null) || (rarity < 0) || (multiplier < 0) || (reward < 0)) continue;
			
			LOOKUP.put(name, s);
			
			REGISTRY.put(name, new CustomMob(name, prefix, rarity, multiplier, reward));
		}
	}
	
	public static CustomMob getByName(String name)
	{
		if (!REGISTRY.containsKey(name)) return null;
		
		return REGISTRY.get(name);
	}
	
	public static CustomMob getByNameIgnoreColor(String name)
	{
		String compare = ChatColor.stripColor(name);
		
		for (String s : REGISTRY.keySet())
		{
			if (ChatColor.stripColor(s).equalsIgnoreCase(compare))
			{
				return REGISTRY.get(s);
			}
		}
		
		return null;
	}
	
	String displayName;
	String prefix;
	int rarity;
	int multiplier;
	double reward;
	
	public CustomMob(String name, String prefix, int rarity, int multiplier, double reward)
	{
		this.displayName = name;
		this.prefix = prefix;
		this.rarity = rarity;
		this.multiplier = multiplier;
		this.reward = reward;
	}
	
	private Map<String,Object> getConfig()
	{
		Map<String,Object> map = new TreeMap<String,Object>();
		
		map.put(_name, this.displayName);
		map.put(_prefix, this.prefix);
		map.put(_rarity, this.rarity);
		map.put(_reward, this.reward);
		map.put(_multiplier, this.multiplier);
		
		return map;
	}
}
