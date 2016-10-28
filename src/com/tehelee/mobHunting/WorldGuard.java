package com.tehelee.mobHunting;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class WorldGuard
{
	public static WorldGuardPlugin plugin;
	
	public WorldGuard()
	{
		WorldGuard.plugin = WGBukkit.getPlugin();
	}
	
	public static boolean doesLocationAllowFlag(Location loc, Flag<?> flag)
	{
		RegionContainer container = WorldGuard.plugin.getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(loc);
		
		if(set.isVirtual()) return true;
	
		return set.testState(null, (StateFlag) flag);
	}
	
	public static boolean doesLocationAllowFlagForPlayer(Location loc, Flag<?> flag, Player p)
	{
		RegionContainer container = WorldGuard.plugin.getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(loc);
		
		if(set.isVirtual()) return true;
		
		return set.testState(WorldGuard.plugin.wrapPlayer(p), (StateFlag) flag);
	}
	
	public static boolean doesLocationAllowMobDamage(Location loc)
	{
		return doesLocationAllowFlag(loc, DefaultFlag.MOB_DAMAGE);
	}
	
	public static boolean doesLocationAllowMobSpawning(Location loc)
	{
		return doesLocationAllowFlag(loc, DefaultFlag.MOB_SPAWNING);
	}
	
	public static boolean doesLocationAllowItemDrops(Location loc)
	{
		return doesLocationAllowFlag(loc, DefaultFlag.ITEM_DROP);
	}
}
