package com.tehelee.mobHunting;

import java.io.File;
import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerHead
{
	public static void storePlayerHeadInfo(String name, String display)
	{
		if ((name == null) || (display == null)) return;
		
		File file = new File(Main.instance.getDataFolder(), "//" + "player_heads.yml");
		FileConfiguration heads = YamlConfiguration.loadConfiguration(file);
		
		heads.set(name, display.toString());
		
		try
		{
			heads.save(file);
		}
		catch (IOException e)
		{
			Main.message(null, "Failed to save head for " + name.toString() + "!", true);
		}
	}
	
	public static String getPlayerHeadInfoByName(String name)
	{
		File file = new File(Main.instance.getDataFolder(), "//" + "player_heads.yml");
		FileConfiguration heads = YamlConfiguration.loadConfiguration(file);
		
		return (heads.contains(name)) ? heads.getString(name) : null;
	}
	
	public static ItemStack createPlayerHead(Player player)
	{
		return createPlayerHead(player.getName(), player.getDisplayName());
	}
	
	public static ItemStack createPlayerHead(String name, String displayName)
	{
		storePlayerHeadInfo(name, displayName);
		
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		
		meta.setOwner(name);
		
		meta.setDisplayName(displayName);
		
		head.setItemMeta(meta);
		
		return head;
	}
}
