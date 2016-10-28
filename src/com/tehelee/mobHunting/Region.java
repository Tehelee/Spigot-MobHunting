package com.tehelee.mobHunting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Region
{
	private static Map<World,ArrayList<Region>> REGIONS = new HashMap<World,ArrayList<Region>>();
	
	public static String getRegionList(World world)
	{	
		if (world == null)
		{
			Set<World> worlds = REGIONS.keySet();
			
			if ((worlds == null) || (worlds.size() == 0))
			{
				return "§cNo regions defined.§r";
			}
			else
			{
				String result = "";
				
				boolean isFirst = true;
				
				for (World w : worlds)
				{
					ArrayList<Region> regions = REGIONS.get(w);
					if ((regions == null) || (regions.size() == 0)) continue;
					
					if (isFirst)
					{
						result += "\"§b" + w.getName() + "§r\":";
						isFirst = false;
					}
					else
					{
						result += "\n\"§b" + w.getName() + "§r\":";
					}
					
					for (Region r : regions)
					{
						result += "\n  §6" + r.name + "§r";
					}
					
				}
				
				if (result.isEmpty())
					return "§cNo regions defined.§r";
				else
					return result;
			}
		}
		else
		{
			ArrayList<Region> regions = REGIONS.get(world);
			if ((regions == null) || (regions.size() == 0))
			{
				return "§cNo regions defined in §r" + world.getName() + "§c.§r";
			}
			else
			{
				String result = "\"§b" + world.getName() + "§r\":";
				for (Region r : regions)
				{
					result += "\n  §6" + r.name + "§r";
				}
				
				return result;
			}
		}
	}
	
	public static double getDamageMultiplier(Location loc)
	{
		ArrayList<Region> regions = getRegions(loc);
		
		double damageMod = 1;
		
		if (regions != null)
		{	
			for (Region region : regions)
			{
				if (region.damageMod < damageMod) damageMod = region.damageMod;
			}
		}
		
		return damageMod;
	}
	
	public static double getRewardMultiplier(Location loc)
	{
		ArrayList<Region> regions = getRegions(loc);
		
		double rewardMod = 1;
		
		if (regions != null)
		{
			for (Region region : regions)
			{
				if (region.rewardMod < rewardMod) rewardMod = region.rewardMod;
			}
		}
		
		return rewardMod;
	}
	
	public static ArrayList<Region> getRegions(Location loc)
	{
		if (loc == null) return null;
		
		ArrayList<Region> regions = REGIONS.get(loc.getWorld());
		
		if (regions == null) return null;
		
		ArrayList<Region> containing = new ArrayList<Region>();
		
		for (Region region : regions)
		{
			if (region.contains(loc)) containing.add(region);
		}
		
		if (containing.size() > 0) return containing;
		
		return null;
	}
	
	public static Region lookupRegion(String name)
	{
		Set<World> worlds = REGIONS.keySet();
		
		for (World world : worlds)
		{
			ArrayList<Region> regions = REGIONS.get(world);
			if (regions == null) continue;
			
			for (Region region : regions)
			{
				if (name.equalsIgnoreCase(region.name)) return region;
			}
		}
		
		return null;
	}
	
	public static void populateRegions()
	{
		File file = new File(Main.instance.getDataFolder(), "//" + "regions.yml");
		
		if (!file.exists()) return;
		
		boolean save = false;
		
		FileConfiguration regionsYaml = YamlConfiguration.loadConfiguration(file);
		
		Set<String> keys = regionsYaml.getKeys(false);
		
		if (keys != null)
		{
			for (String key : keys)
			{
				World world = Main.server.getWorld(key);
				
				if (world == null)
				{
					regionsYaml.set(key, null);
					continue;
				}
				
				ConfigurationSection worldYaml = regionsYaml.getConfigurationSection(key); 
				
				Set<String> regionNames = worldYaml.getKeys(false);
				
				ArrayList<Region> regions = new ArrayList<Region>();
				
				if (regionNames != null)
				{
					for (String name : regionNames)
					{
						ConfigurationSection regionYaml = worldYaml.getConfigurationSection(name);
						
						ConfigurationSection min = regionYaml.getConfigurationSection("min");
						ConfigurationSection max = regionYaml.getConfigurationSection("max");
						
						int x1 = min.getInt("x");
						int y1 = min.getInt("y");
						int z1 = min.getInt("z");
						
						int x2 = max.getInt("x");
						int y2 = max.getInt("y");
						int z2 = max.getInt("z");
						
						double damageMod = 1, rewardMod = 1;
						
						if (regionYaml.isConfigurationSection("permissions"))
						{
							ConfigurationSection perms = regionYaml.getConfigurationSection("permissions");
							
							damageMod = perms.getBoolean("enableDamage") ? 1 : 0;
							rewardMod = perms.getBoolean("enableRewards") ? 1 : 0;
							
							regionYaml.set("permissions", null);
							
							if (!regionYaml.isConfigurationSection("modifiers"))
							{
								ConfigurationSection mods = regionYaml.createSection("modifiers");
								
								mods.set("damageMultiplier", damageMod);
								mods.set("rewardMultiplier", rewardMod);
							}
							
							save = true;
						}
						
						if (regionYaml.isConfigurationSection("modifiers"))
						{
							ConfigurationSection mods = regionYaml.getConfigurationSection("modifiers");
							
							damageMod = mods.getDouble("damageMultiplier");
							rewardMod = mods.getDouble("rewardMultiplier");
						}
						
						Region region = new Region(world, x1, y1, z1, x2, y2, z2, name, damageMod, rewardMod);
						
						if (region != null) regions.add(region);
					}
				}
				
				REGIONS.put(world, regions);
			}
			
			if (save)
			{
				try
				{
					regionsYaml.save(file);
				}
				catch (IOException e)
				{
					Main.message(null, "Failed to save regions!", true);
				}
			}
		}
	}
	
	public static void deleteRegion(Region region)
	{
		File file = new File(Main.instance.getDataFolder(), "//" + "regions.yml");
		FileConfiguration regionsYaml = YamlConfiguration.loadConfiguration(file);
		
		ArrayList<Region> regions = REGIONS.get(region.world);
		
		if (regions.contains(region))
		{
			regions.remove(region);
			
			REGIONS.put(region.world, regions);
		}
		
		String world = region.world.getName();
		
		if (!regionsYaml.isConfigurationSection(world)) return;
		
		ConfigurationSection worldYaml = regionsYaml.getConfigurationSection(world);
		
		if (!worldYaml.isConfigurationSection(region.name)) return;
		
		worldYaml.set(region.name, null);
		
		try
		{
			regionsYaml.save(file);
		}
		catch (IOException e)
		{
			Main.message(null, "Failed to save regions!", true);
		}
	}
	
	private static void saveRegion(Region region)
	{
		File file = new File(Main.instance.getDataFolder(), "//" + "regions.yml");
		FileConfiguration regionsYaml = YamlConfiguration.loadConfiguration(file);
		
		String world = region.world.getName();
		
		ConfigurationSection worldYaml, regionYaml, min, max, mods;
		
		worldYaml = regionsYaml.isConfigurationSection(world) ? regionsYaml.getConfigurationSection(world) : regionsYaml.createSection(world); 
		
		regionYaml = worldYaml.isConfigurationSection(region.name) ? worldYaml.getConfigurationSection(region.name) : worldYaml.createSection(region.name);
		
		min = regionYaml.isConfigurationSection("min") ? regionYaml.getConfigurationSection("min") : regionYaml.createSection("min");
		
		max = regionYaml.isConfigurationSection("max") ? regionYaml.getConfigurationSection("max") : regionYaml.createSection("max");
		
		mods = regionYaml.isConfigurationSection("modifiers") ? regionYaml.getConfigurationSection("modifiers") : regionYaml.createSection("modifiers");
		
		if (regionYaml.isConfigurationSection("permissions"))
		{
			regionYaml.set("permissions", null);
		}
		
		min.set("x", region.x1);
		min.set("y", region.y1);
		min.set("z", region.z1);
		
		max.set("x", region.x2);
		max.set("y", region.y2);
		max.set("z", region.z2);
		
		mods.set("damageMultiplier", region.damageMod);
		mods.set("rewardMultiplier", region.rewardMod);
		
		try
		{
			regionsYaml.save(file);
		}
		catch (IOException e)
		{
			Main.message(null, "Failed to save regions!", true);
		}
	}
	
	public static Region createRegion(Location min, Location max, String name, double damageMod, double rewardMod)
	{
		World world = min.getWorld();
		
		ArrayList<Region> list = REGIONS.get(world);
		if (list == null) list = new ArrayList<Region>();
		
		for (Region r : list)
		{
			if (r.name.equalsIgnoreCase(name))
			{
				return null;
			}
		}
		
		if ((min == null) || (max == null) || (name == null) || (name.isEmpty()) || (world != max.getWorld())) return null;
		
		int x1, y1, z1, x2, y2, z2;
		
		if (max.getBlockX() >= min.getBlockX())
		{
			x1 = min.getBlockX();
			x2 = max.getBlockX();
		}
		else
		{
			x1 = max.getBlockX();
			x2 = min.getBlockX();
		}
		
		if (max.getBlockY() >= min.getBlockY())
		{
			y1 = min.getBlockY();
			y2 = max.getBlockY();
		}
		else
		{
			y1 = max.getBlockY();
			y2 = min.getBlockY();
		}
		
		if (max.getBlockZ() >= min.getBlockZ())
		{
			z1 = min.getBlockZ();
			z2 = max.getBlockZ();
		}
		else
		{
			z1 = max.getBlockZ();
			z2 = min.getBlockZ();
		}
		
		Region region = new Region(world, x1, y1, z1, x2, y2, z2, name.replace(' ', '_').toUpperCase(), damageMod, rewardMod);
		
		list.add(region);
		
		REGIONS.put(world, list);
		
		saveRegion(region);
		
		return region;
	}
	
	private int x1,y1,z1,x2,y2,z2;
	
	private World world;
	
	private String name;
	
	private double damageMod = 1, rewardMod = 1;
	
	private Region(World world, int x1, int y1, int z1, int x2, int y2, int z2, String name, double damageMod, double rewardMod)
	{
		this.name = name;
		
		this.world = world;
		
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
		
		this.damageMod = damageMod;
		this.rewardMod = rewardMod;
	}
	
	public boolean contains(Location loc)
	{	
		if ((loc == null) || (loc.getWorld() != this.world)) return false;
		
		double x = Math.floor(loc.getX());
		double y = Math.floor(loc.getY());
		double z = Math.floor(loc.getZ());
		
		boolean contains = true;
		
		contains = contains && (x >= (double)x1) && (x <= (double)x2);
		contains = contains && (y >= (double)y1) && (y <= (double)y2);
		contains = contains && (z >= (double)z1) && (z <= (double)z2);
		
		return contains;
	}
	
	public double getDamageMultiplier()
	{
		return this.damageMod;
	}
	
	public double getRewardMultiplier()
	{
		return this.rewardMod;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setDamageMultiplier(double multiplier)
	{
		if (this.damageMod != multiplier)
		{
			this.damageMod = multiplier;
			
			saveRegion(this);
		}
	}
	
	public void setRewardMultiplier(double multiplier)
	{
		if (this.rewardMod != multiplier)
		{
			this.rewardMod = multiplier;
			
			saveRegion(this);
		}
	}
	
	public String toString()
	{
		return String.format("§e%1$s§r:\n  %2$s\n  %3$s", this.name, this.boundsToString(), this.permsToString());
	}
	
	public String permsToString()
	{	
		return String.format("§cDamage§r §7x§r %1$.2f  §aRewards§r §7x§r %2$.2f§r", this.damageMod, this.rewardMod);
	}
	
	private String boundsToString()
	{
		return String.format("\"§b%1$s§r\": (%2$d, %3$d, %4$d) §7x§r (%5$d, %6$d, %7$d)", world.getName(), x1, y1, z1, x2, y2, z2);
	}
}
