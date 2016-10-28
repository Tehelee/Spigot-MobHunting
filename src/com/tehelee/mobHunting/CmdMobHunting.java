package com.tehelee.mobHunting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Rabbit.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.NBTTagList;
import net.minecraft.server.v1_10_R1.NBTTagString;
import net.minecraft.server.v1_10_R1.PacketDataSerializer;
import net.minecraft.server.v1_10_R1.PacketPlayOutCustomPayload;

import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;

public class CmdMobHunting implements CommandExecutor
{
	private static Map<Player,Pair<Location,Location>> positions = new HashMap<Player,Pair<Location,Location>>();
	
	private static Hashtable<String,String> bookColors = new Hashtable<String,String>();
	
	static
	{
		bookColors.put("§f", "§8");
		bookColors.put("§a", "§2");
		bookColors.put("§b", "§3");
		bookColors.put("§d", "§5");
	}
	
	public static ItemStack createBook(String title, String author, String... pages)
	{
		ItemStack is = new ItemStack(Material.WRITTEN_BOOK, 1);
		net.minecraft.server.v1_10_R1.ItemStack nmsis = CraftItemStack.asNMSCopy(is);
		NBTTagCompound bd = new NBTTagCompound();
		bd.setString("title", title);
		bd.setString("author", author);
		NBTTagList bp = new NBTTagList();
		for(String text : pages) {
			bp.add(new NBTTagString(text));
		}
		bd.set("pages", bp);
		nmsis.setTag(bd);
		is = CraftItemStack.asBukkitCopy(nmsis);
		return is;
	}
	
	public static void openBook(ItemStack book, Player p)
	{
		int slot = p.getInventory().getHeldItemSlot();
		ItemStack old = p.getInventory().getItem(slot);
		p.getInventory().setItem(slot, book);

		ByteBuf buf = Unpooled.buffer(256);
		buf.setByte(0, (byte)0);
		buf.writerIndex(1);

		PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(buf));
		((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
		p.getInventory().setItem(slot, old);
	}
	
	public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	  List<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list);
	  return list;
	}
	
	public static void openChallengeBook(Player p)
	{
		Hashtable<String,Integer> progress = MobHuntingListener.getChallengeProgress(p.getUniqueId());
		
		List<String> bookPages = new ArrayList<String>();
		
		String bookText = "";
		
		int entry = 0;
		
		List<String> registry = asSortedList(progress.keySet());
		
		for (String id : registry)
		{
			Pair<MobType,Integer> info = MobType.getFromRegistryId(id);
			
			int lvl, min = 0, max = 0, current = progress.get(id);
			
			if (current == 0) continue;
			
			for (lvl = 0; lvl < MobHuntingListener.challengeThresholds.length; lvl++)
			{
				if (current < MobHuntingListener.challengeThresholds[lvl])
				{
					max = MobHuntingListener.challengeThresholds[lvl];
					
					if (lvl > 0) min = MobHuntingListener.challengeThresholds[lvl-1];
					
					break;
				}
			}
			
			bookText += String.format("%1$s§l%2$s\n§8Lvl §0%3$2s§8:  §0%4$4s §8/ §0%5$s\n", bookColors.get(info.a.getRarityColor(info.b)), info.a.getDisplayName(info.b), Integer.toString(lvl+1), Integer.toString(current-min), Integer.toString(max));
			
			if (++entry >= 7)
			{
				entry = 0;
				bookPages.add(bookText);
				bookText = "";
			}
		}
		
		ItemStack book = createBook("Mob Hunter Challenges", p.getDisplayName(), bookPages.toArray(new String[0]));
		
		openBook(book, p);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] params)
	{
		if (params.length > 0)
		{
			List<String> arguments = new ArrayList<String>();
			boolean begin, end, inside = false;
			for (int i = 0; i < params.length; i++)
			{
				int length = params[i].length();
				begin = (params[i].charAt(0) == '"');
				end = (params[i].charAt(length-1) == '"');
				
				String clean = params[i].substring(begin ? 1 : 0, end ? length-1 : length).replace('&', '§');
				
				if (inside)
					arguments.set(i-1, arguments.get(i-1) + " " + clean);
				else
					arguments.add(clean);
				
				if (!inside && begin && !end) inside = true;
				if (inside && !begin && end) inside = false;
			}

			String[] args = arguments.toArray(new String[0]);
			
			Player player = (sender instanceof Player) ? (Player)sender : null;
			
			World world = (player != null) ? player.getWorld() : null;
			
			Location location = (player != null) ? player.getLocation() : null;
		
			if (args[0].equalsIgnoreCase("reload"))
			{
				if (sender.hasPermission(HelpText.PermissionPrefix + "reload"))
				{
					Main.message(sender, "Reloading...", true);
					
					
					Main.reload(sender);
				}
				else
				{
					Main.message(sender, HelpText.MissingPermission);
				}
			}
			else if ((player != null) && (args[0].equalsIgnoreCase("progress") || args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("challenge")))
			{
				openChallengeBook(player);
			}
			else if (args[0].equalsIgnoreCase("heads") && (player != null))
			{
				if (sender.hasPermission(HelpText.PermissionPrefix + "heads"))
				{
					world.dropItemNaturally(location, PlayerHead.createPlayerHead(player));
						
					MobType[] mobs = MobType.values();
					
					int headCount = 1;
					
					for(MobType mob : mobs)
					{
						world.dropItemNaturally(location, mob.createSkull());
						
						headCount++;
						
						switch(mob)
						{
						case CREEPER:
							world.dropItemNaturally(location, mob.createSkull(1));
							headCount++;
							break;
							
						case GUARDIAN:
							world.dropItemNaturally(location, mob.createSkull(1));
							headCount++;
							break;
							
						case OCELOT:
							world.dropItemNaturally(location, mob.createSkull(1));
							world.dropItemNaturally(location, mob.createSkull(2));
							world.dropItemNaturally(location, mob.createSkull(3));
							headCount+=3;
							break;
						
						case RABBIT:
							world.dropItemNaturally(location, mob.createSkull(1));
							world.dropItemNaturally(location, mob.createSkull(2));
							world.dropItemNaturally(location, mob.createSkull(3));
							world.dropItemNaturally(location, mob.createSkull(4));
							world.dropItemNaturally(location, mob.createSkull(5));
							world.dropItemNaturally(location, mob.createSkull(99));
							headCount+=6;
							break;
						
						case SKELETON:
							world.dropItemNaturally(location, mob.createSkull(1));
							world.dropItemNaturally(location, mob.createSkull(2));
							headCount+=2;
							break;
							
						case ZOMBIE:
							world.dropItemNaturally(location, mob.createSkull(1));
							world.dropItemNaturally(location, mob.createSkull(6));
							headCount+=2;
							break;
							
						default:
							break;
						}
					}
					
					Main.message(sender, "Spawned " + headCount + " mob heads, and your player head.", true);
				}
				else
				{
					Main.message(sender, HelpText.MissingPermission);
				}
			}
			else if (args[0].equalsIgnoreCase("tehelee") && (player != null))
			{
				if (sender.hasPermission(HelpText.PermissionPrefix + "*"))
				{
					Rabbit e = (Rabbit) world.spawnEntity(location, EntityType.RABBIT);
					
					e.setRabbitType(Type.THE_KILLER_BUNNY);
					
					e.setCustomName("§6Tehelee's Minion");
					e.setCustomNameVisible(true);
					
					Main.message(sender, "You summoned §6Tehelee's Minion§r! Ahhh run away!", true);
				}
				else
				{
					Main.message(sender, HelpText.MissingPermission);
				}
			}
			else if (args[0].equalsIgnoreCase("region") && (player != null))
			{
				if (player.hasPermission(HelpText.PermissionPrefix + "region"))
				{
					if ((args.length == 1) || args[1].equalsIgnoreCase("help") || args[1].equals("?"))
					{
						Main.message(player, "/" + label + " region [pos1 | pos2 | create | remove | set | list | info]");
					}
					else if (args[1].equalsIgnoreCase("pos1"))
					{
						Pair<Location,Location> bounds = positions.get(player);
						
						if (bounds == null) bounds = new Pair<Location,Location>(null, null);
						
						bounds.a = player.getLocation();
						
						positions.put(player, bounds);
						
						Main.message(player, String.format("Region position #1 set: (%1$d,%2$d,%3$d)", bounds.a.getBlockX(), bounds.a.getBlockY(), bounds.a.getBlockZ()));
					}
					else if (args[1].equalsIgnoreCase("pos2"))
					{
						Pair<Location,Location> bounds = positions.get(player);
						
						if (bounds == null) bounds = new Pair<Location,Location>(null, null);
						
						bounds.b = player.getLocation();
						
						positions.put(player, bounds);
						
						Main.message(player, String.format("Region position #2 set: (%1$d,%2$d,%3$d)", bounds.b.getBlockX(), bounds.b.getBlockY(), bounds.b.getBlockZ()));
					}
					else if (args[1].equalsIgnoreCase("create"))
					{
						Pair<Location,Location> bounds = positions.get(player);
						
						if ((bounds == null) || ((bounds.a == null) && (bounds.b == null)))
						{
							Main.message(player, "§cYou must specify a region first!§r\n/" + label + " region pos1\n/" + label + " region pos2");
						}
						else if (bounds.a == null)
						{
							Main.message(player, "§cYou must complete a region first!§r\n/" + label + " region pos1");
						}
						else if (bounds.b == null)
						{
							Main.message(player, "§cYou must complete a region first!§r\n/" + label + " region pos2");
						}
						else if (bounds.a.distance(bounds.b) < 1)
						{
							Main.message(player, "§cYou must specify a non-zero region first!§r\n/" + label + " region pos1\n/" + label + " region pos2");
						}
						else
						{
							if (args.length != 5)
							{
								Main.message(player, "/" + label + " create \"name\" [damage multipler] [reward multipler]");
							}
							else
							{
								String name = args[2].replace(' ', '_').toUpperCase();
								double damage = 1, rewards = 1;
								
								try
								{
									damage = Double.parseDouble(args[3]);
								}
								catch (NumberFormatException e)
								{
									Main.message(player, "§c" + args[3] + " is not a valid number.§r");
									Main.message(player, "/" + label + " create " + name + " [damage multipler] [reward multipler]");
									return true;
								}	
									
								try
								{
									rewards = Double.parseDouble(args[4]);
								}
								catch (NumberFormatException ex)
								{
									Main.message(player, "§c" + args[4] + " is not a valid number.§r");
									Main.message(player, "/" + label + " create " + name + " " + damage + " [reward multipler]");
									return true;
								}
								
								
								Region region = Region.createRegion(bounds.a, bounds.b, name, damage, rewards);
								
								if (region != null)
								{
									positions.put(player, null);
									
									Main.message(player, "§aRegion created!§c\n"+region.toString());
									Main.message(null, player.getName() + " has created a region:\n"+region.toString(), true);
								}
								else
								{
									Main.message(player, "§6" + name + "§c is already in use.§r");
									Main.message(player, "/" + label + " remove " + name);
								}
							}
						}
					}
					else if (args[1].equalsIgnoreCase("set"))
					{	
						if ((args.length < 3) || !(args[2].equalsIgnoreCase("damage") || args[2].equalsIgnoreCase("rewards")))
						{
							Main.message(player, "/" + label + " region set" + " [damage | rewards] [multiplier]");
						}
						else if (args.length < 4)
						{
							Main.message(player, "/" + label + " region set " + args[2] + " [multiplier]");
						}
						else
						{
							List<Region> regions = Region.getRegions(player.getLocation());
							
							boolean damage = args[2].equalsIgnoreCase("damage");
							
							double amount = 1;
							
							try
							{
								amount = Double.parseDouble(args[3]);
							}
							catch (NumberFormatException e)
							{
								Main.message(player, "§c" + args[3] + " is not a valid number.§r");
								Main.message(player, "/" + label + " region set" + (damage ? "damage" : "rewards") + " [multiplier]");
								
								return true;
							}
							
							if (regions == null)
							{
								Main.message(player, "§cYou must be within a region to perform that command.§r");
							}
							else
							{
								String modified = "";
								
								for (Region region : regions)
								{
									if (damage)
										region.setDamageMultiplier(amount);
									else
										region.setRewardMultiplier(amount);
									
									modified += "\n" + region.getName() + "\n  " + region.permsToString();
								}
								
								Main.message(player, "§6Region" + ((regions.size() > 1) ? "s have" : " has") + " been modified.§r" + modified);
							}
						}
					}
					else if (args[1].equalsIgnoreCase("info"))
					{
						if (args.length != 3)
						{
							List<Region> regions = Region.getRegions(player.getLocation());
							
							if (regions != null)
							{
								String msg = regions.get(0).toString();
								
								for (int i = 1; i < regions.size(); i++)
								{
									msg += "\n" + regions.get(i);
								}
								
								Main.message(player, "§6You are standing in" + ((regions.size() > 1) ? " the following regions" : "") + "§r:\n" + msg);
							}
							else
							{
								Main.message(player, "§cYou are not within a region. §6To lookup a region by name, use§r:\n/" + label + " region info <name>");
							}
						}
						else
						{
							Region region = Region.lookupRegion(args[2]);
							
							if (region != null)
							{
								Main.message(player, region.toString());
							}
							else
							{
								Main.message(player, "§cUnabled to find the region "+args[2]+".§r");
							}
						}
					}
					else if (args[1].equalsIgnoreCase("remove"))
					{
						if (args.length < 3)
						{
							List<Region> regions = Region.getRegions(player.getLocation());
							
							if (regions != null)
							{
								if (regions.size() > 1)
								{
									String msg = regions.get(0).toString();
									
									for (int i = 1; i < regions.size(); i++)
									{
										msg += "\n" + regions.get(i);
									}
									
									Main.message(player, "§cYou are standing in multiple regions§r:\n" + msg);
									
									Main.message(player, "§6Please specify which region you wish to remove§r:\n/" + label + " region remove <name>");
								}
								else
								{
									Region region = regions.get(0);
									
									Region.deleteRegion(region);
									
									Main.message(player, "§aRegion " + region.getName() + " has been removed.§r");
								}
							}
							else
							{
								Main.message(player, "§cYou are not within a region. §6To remove a region by name, use§r:\n/" + label + " region remove <name>");
							}
						}
						else
						{
							Region region = Region.lookupRegion(args[2]);
							
							if (region != null)
							{
								Region.deleteRegion(region);
								
								Main.message(player, "§aRegion " + region.getName() + " has been removed.§r");
							}
							else
							{
								Main.message(player, "§cUnable to find the region "+args[2]+".§r");
							}
						}
					}
					else if (args[1].equalsIgnoreCase("list"))
					{
						if (args.length > 2)
						{
							String name = args[2];
							World w = Main.server.getWorld(name);
							
							if (w == null)
							{
								if (name.equalsIgnoreCase("all"))
								{
									Main.message(player, Region.getRegionList(null));
								}
								else
								{
									Main.message(player, "§cUnable to find the world §r\""+name+"\"");
								}
							}
							else
							{
								Main.message(player, Region.getRegionList(w));
							}
						}
						else
						{
							Main.message(player, Region.getRegionList(null));
						}
					}
					else
					{
						Region region = (args.length >= 2) ? Region.lookupRegion(args[1]) : null;
						
						if (region != null)
						{
							if ((args.length >= 3) && args[2].equalsIgnoreCase("set"))
							{
								if ((args.length < 4) || !(args[3].equalsIgnoreCase("damage") || args[3].equalsIgnoreCase("rewards")))
								{
									Main.message(player, "/" + label + " region " + region.getName() + " set [damage | rewards] [multiplier]");
								}
								else if (args.length < 5)
								{
									Main.message(player, "/" + label + " region " + region.getName() + " set " + args[3] + " [multiplier]");
								}
								else
								{
									boolean damage = args[3].equalsIgnoreCase("damage");
									
									double amount = 1;
									
									amount = Double.parseDouble(args[4]);
									
									if (damage)
										region.setDamageMultiplier(amount);
									else
										region.setRewardMultiplier(amount);
									
									Main.message(player, "§aRegion " + region.getName() + " has been modified.§r\n"+region.permsToString());
								}
							}
							else if ((args.length >= 3) && (args[2].equalsIgnoreCase("remove")))
							{
								Region.deleteRegion(region);
								
								Main.message(player, "§aRegion " + region.getName() + " has been removed.§r");
							}
							else
							{
								Main.message(player, region.toString());
								Main.message(player, "/" + label + " region " + region.getName() + " [set | remove]");
							}
						}
						else
						{
							Main.message(player, "/" + label + " region [pos1 | pos2 | create | set | remove | info]");
						}
					}
				}
				else
				{
					Main.message(player, HelpText.MissingPermission);
				}
			}
			else if (args[0].equalsIgnoreCase("custom"))
			{
				if (sender.hasPermission(HelpText.PermissionPrefix + "custom"))
				{
					if ((args.length == 1) || args[1].equalsIgnoreCase("help") || args[1].equals("?"))
					{
						Main.message(sender, "/" + label + " custom [add | remove | list | info]");
					}
					else if (args[1].equalsIgnoreCase("add"))
					{
						if (args.length < 6)
						{
							Main.message(sender, "/" + label + " custom add \"<name>\" <rarity 0-3> <reward> <multiplier> \"<prefix>\"");
						}
						else
						{
							String prefix = "", name = args[2];
							int rarity = 0, multiplier = 0;
							double reward = 0;
							
							boolean failed = false;
							
							try
							{
								rarity = Integer.parseInt(args[3]);
								
								failed = ((rarity < 0) || (rarity > 3));
							}
							catch (NumberFormatException ex)
							{
								failed = true;
							}
							
							if (failed)
							{
								Main.message(sender, "§6Use a number between 0 and 3 for rarity.§r");
								Main.message(sender, "/" + label + " custom add \"" + name + "§r\" <rarity 0-3> <reward> <multiplier> \"<prefix>\"");
							
								return true;
							}
							
							try
							{
								reward = Double.parseDouble(args[4]);
								
								failed = (reward < 0);
							}
							catch (NumberFormatException ex)
							{
								failed = true;
							}
							
							if (failed)
							{
								Main.message(sender, "§6You must specify a minimum reward of 0.§r");
								Main.message(sender, "/" + label + " custom add \"" + name + "§r\" " + MobType.getRarityColorFromInt(rarity) + rarity + "§r <reward> <multiplier> \"<prefix>\"");
							
								return true;
							}
							
							try
							{
								multiplier = Integer.parseInt(args[5]);
								
								failed = (multiplier < 1);
							}
							catch (NumberFormatException ex)
							{
								failed = true;
							}
							
							if (failed)
							{
								Main.message(sender, "§6The reward multiplier must be at least 1.§r");
								Main.message(sender, "/" + label + " custom add \"" + name + "§r\" " + MobType.getRarityColorFromInt(rarity) + rarity + "§r " + reward + " <multiplier> \"<prefix>\"");
							
								return true;
							}
							
							if (args.length == 7)
							{
								prefix = args[6];
							}
							
							Main.message(sender, CustomMob.addCustomMob(name, rarity, reward, multiplier, prefix));
							
							return true;
						}
						
					}
					else if (args[1].equalsIgnoreCase("remove"))
					{
						if (args.length < 3)
						{
							Main.message(sender, "/" + label + " custom remove \"<name>\"");
						}
						else
						{
							Main.message(sender, CustomMob.removeCustomMob(args[2]));
						}
					}
					else if (args[1].equalsIgnoreCase("list"))
					{
						if (args.length < 3)
						{
							String list = "Custom Mobs:\n";
							for (String mob : CustomMob.getMobList())
							{
								list += mob +"§r, ";
							}
							Main.message(sender, list.substring(0, list.length()-2));
						}
					}
					else if (args[1].equalsIgnoreCase("info"))
					{
						if ((args.length < 3)  && (player != null))
						{
							List<Entity> entities = player.getNearbyEntities(5, 5, 5);

							Entity target = null;
							BlockIterator bItr = new BlockIterator(player, 5);
							Block block;
							Location loc;
							int bx, by, bz;
							double ex, ey, ez;
							// loop through player's line of sight
							while (bItr.hasNext())
							{
								block = bItr.next();
								bx = block.getX();
								by = block.getY();
								bz = block.getZ();
								// check for entities near this block in the line of sight
								for (Entity e : entities)
								{
									loc = e.getLocation();
									ex = loc.getX();
									ey = loc.getY();
									ez = loc.getZ();
									if ((bx-.75 <= ex && ex <= bx+1.75) && (bz-.75 <= ez && ez <= bz+1.75) && (by-1 <= ey && ey <= by+2.5))
									{
											// entity is close enough, set target and stop
											target = e;
											break;
									}
								}
							}
						
							if (target != null)
							{
								String name = target.getCustomName();
								if ((name != null) && (!name.isEmpty()))
								{
									CustomMob mob = CustomMob.getByName(name);
									
									if (mob != null)
									{
										Main.message(sender, String.format("%1$s§r \"%2$s\"\nRarity: %3$s§r\nReward: §2$%4$.2f§r\nMultiplier: %5$d", mob.displayName, mob.displayName.replaceAll("§",  "&"), MobType.getRarityColorFromInt(mob.rarity) + mob.rarity, mob.reward, mob.multiplier));
										if (!mob.prefix.isEmpty())
										{
											Main.message(sender, "Prefix: " + mob.prefix);
										}
									}
									else
									{
										Main.message(sender, "Entity Name: " + name.replaceAll("§", "&"));
									}
								}
							}
						}
						else if (args.length >= 3)
						{
							String name = args[2];
							for (int i = 3; i < args.length; i++)
							{
								name += " " + args[i];
							}
							
							CustomMob mob = CustomMob.getByName(name);
							
							if (mob == null)
							{
								mob = CustomMob.getByNameIgnoreColor(name);
							}
							
							if (mob == null)
							{
								String list = "Custom Mobs:\n";
								for (String s : CustomMob.getMobList())
								{
									list += s +"§r, ";
								}
								Main.message(sender, list.substring(0, list.length()-2));
							}
							else
							{
								Main.message(sender, String.format("%1$s§r \"%2$s\"\nRarity: %3$s§r\nReward: §2$%4$.2f§r\nMultiplier: %5$d", mob.displayName, mob.displayName.replaceAll("§",  "&"), MobType.getRarityColorFromInt(mob.rarity) + mob.rarity, mob.reward, mob.multiplier));
								if (!mob.prefix.isEmpty())
								{
									Main.message(sender, "Prefix: " + mob.prefix);
								}
							}
						}
					}
					else
					{
						Main.message(sender, "/" + label + " custom [add | remove | list | info]");
					}
				}
				else
				{
					Main.message(sender, HelpText.MissingPermission);
				}
			}
			else
			{
				commandHelp(sender, label);
			}
		}
		else
		{
			commandHelp(sender, label);
		}
		
		return true;
	}
	
	private static void commandHelp(CommandSender sender, String label)
	{	
		List<String> perms = new ArrayList<String>();
		
		perms.add("progress");
		
		if (sender.hasPermission(HelpText.PermissionPrefix + "reload")) perms.add("reload");
		if (sender.hasPermission(HelpText.PermissionPrefix + "heads")) perms.add("heads");
		if (sender.hasPermission(HelpText.PermissionPrefix + "custom")) perms.add("custom");
		if (sender.hasPermission(HelpText.PermissionPrefix + "region")) perms.add("region");
		
		if (perms.size() == 0)
		{
			Main.message(sender, HelpText.MissingPermission);
			return;
		}
		
		String cmds = perms.get(0);
		
		for (int i = 1; i < perms.size(); i++)
		{
			cmds += " | " + perms.get(i);
		}
		
		if (perms.size() > 1)
		{
			cmds = "[" + cmds + "]";
		}
		
		Main.message(sender, "/" + label + " " + cmds);
	}
}
