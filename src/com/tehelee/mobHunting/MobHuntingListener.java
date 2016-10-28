package com.tehelee.mobHunting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

public class MobHuntingListener implements Listener
{
	private static HashSet<UUID> spawnerMobs = new HashSet<UUID>();
	
	private static Hashtable<UUID,PlayerCombo> combos = new Hashtable<UUID,PlayerCombo>();
	private static Hashtable<UUID,ArrayList<LivingEntity>> playerDamagers = new Hashtable<UUID,ArrayList<LivingEntity>>();
	
	private static Hashtable<UUID,Hashtable<Pair<MobType,Integer>, GrindInfo>> grindLocations = new Hashtable<UUID,Hashtable<Pair<MobType,Integer>, GrindInfo>>();
	
	private static Hashtable<UUID,Hashtable<Pair<MobType,Integer>, GrindInfo>> grindArcheryLocations = new Hashtable<UUID,Hashtable<Pair<MobType,Integer>, GrindInfo>>();
	
	private static HashSet<Item> cashDrops = new HashSet<Item>();
	
	public static Integer[] challengeThresholds = null;
	
	private static List<List<String>> challengeRewards = null;
	
	private static Hashtable<UUID, Pair<BossBar, Long>> bossBars = new Hashtable<UUID, Pair<BossBar, Long>>();
	
	private static BukkitRunnable runner = new BukkitRunnable()
	{
		@Override
		public void run()
		{
			long current = System.currentTimeMillis();
			
			for (UUID id : bossBars.keySet())
			{
				Pair<BossBar, Long> bar = bossBars.get(id);
				
				if (bar.b < current)
				{
					bar.a.setVisible(false);
					bar.a.removeAll();
					bossBars.remove(id);
				}
			}
		}
	};
	
	@SuppressWarnings("unchecked")
	public static void writeDefaultConfig()
	{
		Main.config.addDefault("CashDrops", false);
		Main.config.addDefault("GrindingResetDelay", 60000);
		Main.config.addDefault("GrindMobRange", 4);
		Main.config.addDefault("GrindArcheryRange", 4);
		
		if (!Main.config.isList("LevelThresholds"))
		{
			List<Integer> tiers = new ArrayList<Integer>();
			
			tiers.add(0, 200);		// 200
			tiers.add(1, 600);		// 400
			tiers.add(2, 1400);		// 600
			tiers.add(3, 2200);		// 800
			tiers.add(4, 3200);		// 1000
			tiers.add(5, 4400);		// 1200
			tiers.add(6, 5800);		// 1400
			tiers.add(7, 7400);		// 1600
			tiers.add(8, 9200);		// 1800
			tiers.add(9, 11200);	// 2000
			
			Main.config.set("LevelThresholds", tiers);
			
			challengeThresholds = tiers.toArray(new Integer[0]);
		}
		else
		{
			challengeThresholds = ((List<Integer>) Main.config.getList("LevelThresholds")).toArray(new Integer[0]);
		}
		
		if (!Main.config.isList("LevelRewards"))
		{
			List<List<String>> rewards = new ArrayList<List<String>>();
			
			List<String> tier0 = new ArrayList<String>();
			tier0.add(0, "cash 1000");
			tier0.add(1, "item IRON_INGOT 16; Iron Ingots");
			rewards.add(0, tier0);
			
			List<String> tier1 = new ArrayList<String>();
			tier1.add(0, "cash 2500");
			tier1.add(1, "item IRON_INGOT 32; Iron Ingots");
			tier1.add(2, "item GOLD_INGOT 16; Gold Ingots");
			rewards.add(1, tier1);
			
			List<String> tier2 = new ArrayList<String>();
			tier2.add(0, "cash 5000");
			tier2.add(1, "item IRON_BLOCK 16; Iron Blocks");
			rewards.add(2, tier2);
			
			List<String> tier3 = new ArrayList<String>();
			tier3.add(0, "cash 10000");
			tier3.add(1, "item IRON_BLOCK 32; Iron Blocks");
			tier3.add(1, "item GOLD_BLOCK 16; Gold Blocks");
			rewards.add(3, tier3);
			
			List<String> tier4 = new ArrayList<String>();
			tier4.add(0, "cash 25000");
			tier4.add(1, "item DIAMOND 16; Diamonds");
			rewards.add(4, tier4);
			
			List<String> tier5 = new ArrayList<String>();
			tier5.add(0, "cash 50000");
			tier5.add(1, "item EMERALD 16; Emeralds");
			rewards.add(5, tier5);
			
			List<String> tier6 = new ArrayList<String>();
			tier6.add(0, "cash 100000");
			tier6.add(1, "item DIAMOND_BLOCK 16; Diamond Blocks");
			rewards.add(6, tier6);
			
			List<String> tier7 = new ArrayList<String>();
			tier7.add(0, "cash 250000");
			tier7.add(1, "item EMERALD_BLOCK 16; Emerald Blocks");
			rewards.add(7, tier7);
			
			List<String> tier8 = new ArrayList<String>();
			tier8.add(0, "cash 500000");
			tier8.add(1, "item DIAMOND_BLOCK 32; Diamond Blocks");
			tier8.add(2, "item EMERALD_BLOCK 32; Emerald Blocks");
			rewards.add(8, tier8);
			
			List<String> tier9 = new ArrayList<String>();
			tier9.add(0, "cash 1000000");
			tier9.add(1, "item SPAWN_EGG 1");
			rewards.add(9, tier9);
			
			Main.config.set("LevelRewards", rewards);
			
			challengeRewards = rewards;
		}
		else
		{
			challengeRewards = (List<List<String>>) Main.config.getList("LevelRewards");
		}
		
		Main.config.addDefault("BountyRewardPerPlayer", 500);
	}
	
	public static void populateSpawnerMobs()
	{
		File file = new File(Main.instance.getDataFolder(), "//" + "spawner_mobs.yml");
		FileConfiguration mobs = YamlConfiguration.loadConfiguration(file);
		
		List<String> mobList = mobs.getStringList("SpawnerMobs");
		
		if ((mobList != null) && (mobList.size() > 0))
		{
			for (String id : mobList)
			{
				spawnerMobs.add(UUID.fromString(id));
			}
		}
		
		file.delete();
		
		runner.runTaskTimer(Main.instance, 0, 5);
	}
	
	public static void cacheSpawnerMobs()
	{
		File file = new File(Main.instance.getDataFolder(), "//" + "spawner_mobs.yml");
		FileConfiguration mobs = YamlConfiguration.loadConfiguration(file);
		
		List<String> mobList = new ArrayList<String>();
		
		for (UUID id : spawnerMobs)
		{
			mobList.add(id.toString());
		}
		
		mobs.set("SpawnerMobs", mobList);
		
		try
		{
			mobs.save(file);
		}
		catch (IOException e)
		{
			Main.message(null, "Failed to save spawner mobs!", true);
		}
	}
	
	public static void setBounty(UUID id, int count)
	{
		if (id == null) return;
		
		File file = new File(Main.instance.getDataFolder(), "//" + "player_bounty.yml");
		FileConfiguration progress = YamlConfiguration.loadConfiguration(file);
		
		progress.set(id.toString(), count);
		
		try
		{
			progress.save(file);
		}
		catch (IOException e)
		{
			Main.message(null, "Failed to save bounty for " + id.toString() + "!", true);
		}
	}
	
	public static int getBounty(UUID id)
	{
		if (id == null) return 0;
		
		File file = new File(Main.instance.getDataFolder(), "//" + "player_bounty.yml");
		FileConfiguration progress = YamlConfiguration.loadConfiguration(file);
		
		String key = id.toString();
		
		if (!progress.contains(key)) return 0;
		else return progress.getInt(key);
	}
	
	public static void updateChallengeProgress(UUID id, Hashtable<String,Integer> stats)
	{
		if ((id == null) || (stats == null) || (stats.size() <= 0)) return;
		
		File file = new File(Main.instance.getDataFolder(), "//" + "challenge_progress.yml");
		FileConfiguration progress = YamlConfiguration.loadConfiguration(file);
		
		List<String> store = new ArrayList<String>();
		
		for (String key : stats.keySet())
		{
			store.add(key + " " + stats.get(key));
		}
		
		progress.set(id.toString(), store);
		
		try
		{
			progress.save(file);
		}
		catch (IOException e)
		{
			Main.message(null, "Failed to save challenge progress for " + id.toString() + "!", true);
		}
	}
	
	public static Hashtable<String,Integer> getChallengeProgress(UUID id)
	{
		if (id == null) return null;
		
		File file = new File(Main.instance.getDataFolder(), "//" + "challenge_progress.yml");
		FileConfiguration progress = YamlConfiguration.loadConfiguration(file);
		
		String key = id.toString();
		
		List<String> value;
		
		if (progress.isList(key))
			value = progress.getStringList(key);
		else
			value = MobType.getDefaultKillCounter();
		
		Hashtable<String,Integer> stats = new Hashtable<String,Integer>();
		
		try
		{
			for (String val : value)
			{
				String[] split = val.split(" ");
				stats.put(split[0], Integer.parseInt(split[1]));
			}
		}
		catch(Exception ex)
		{
			Main.message(null, "Failed to load challenge progress for " + key + "!", true);
		}
		
		return stats;
	}
	
	public static void showChallengeBossBar(Player player, Pair<MobType,Integer> info, int level, int current, int next)
	{	
		UUID id = player.getUniqueId();
		
		String title = String.format("§2%1$s - Lvl %2$d: (%3$d / %4$d)", info.a.getDisplayName(info.b), level, current, next);
		double progress = current / (double)next;
		
		if (bossBars.containsKey(id))
		{
			Pair<BossBar, Long> bar = bossBars.get(id);
			
			bar.a.setTitle(title);
			bar.a.setProgress(progress);
			bar.b = System.currentTimeMillis() + 3000;
			
			bossBars.put(id, bar);
		}
		else
		{
			BossBar bar = Main.server.createBossBar(title, BarColor.GREEN, BarStyle.SEGMENTED_10);
			
			bar.setProgress(progress);
			
			bar.addPlayer(player);
			
			bar.setVisible(true);
			
			bossBars.put(id, new Pair<BossBar, Long>(bar, System.currentTimeMillis() + 3000));
		}
	}
	
	public static void incrementChallenge(UUID id, Pair<MobType,Integer> info)
	{
		Hashtable<String,Integer> progress = getChallengeProgress(id);
		
		String registry = MobType.getId(info.a, info.b);
		
		int count = 0;
		
		if (progress.containsKey(registry))
		{
			count = progress.get(registry);
		}
		
		count++;
		
		if (info.a.isChallengeEnabled())
		{
			Player player = Main.server.getPlayer(id);
			
			for (int i = 0; i < challengeThresholds.length; i++)
			{
				if (count == challengeThresholds[i])
				{
					Main.server.broadcastMessage(player.getDisplayName() + " has earned §6"+ChatColor.stripColor(info.a.getDisplayName(info.b))+" Hunter Level "+(i+1)+"§r!");
					Main.message(null, ChatColor.GRAY + ChatColor.stripColor(player.getDisplayName() + " has earned " + info.a.getDisplayName(info.b) + " Hunter Level "+(i+1)) + ChatColor.RESET, true);
	
					List<String> rewards = challengeRewards.get(i);
					
					if (rewards.size() > 0)
					{
						String msg = "%1$s recieved: ";
						
						PlayerInventory inv = player.getInventory();
						
						for (String reward : rewards)
						{
							if ((reward == null) || (reward.isEmpty())) break;
							String[] base = reward.split(";");
							String[] split = base[0].split(" ");
							if (split != null)
							{
								if (split[0].equalsIgnoreCase("cash") && (split.length == 2))
								{
									try
									{
										int cash = Integer.valueOf(split[1]);
										
										Main.giveMoney(player, cash);
										
										msg += "§2$"+cash+"§r, ";
									}
									catch (NumberFormatException ex) {}
								}
								else if (split[0].equalsIgnoreCase("item") && (split.length == 3))
								{
									if (split[1].equalsIgnoreCase("SPAWN_EGG"))
									{
										ItemStack item = info.a.getSpawnEgg(info.b);
										
										ItemMeta meta = item.getItemMeta();
										
										msg += meta.getDisplayName()+", ";
										
										inv.addItem(item);
									}
									else
									{
										Material mat = Material.matchMaterial(split[1]);
										if (mat != null)
										{
											try
											{
												int numberOfItems = Integer.valueOf(split[2]);
												
												ItemStack item = new ItemStack(mat, numberOfItems);
												
												inv.addItem(item);
												
												if (base.length == 2)
													msg += numberOfItems + base[1]+", ";
												else
													msg += numberOfItems + split[1]+", ";
												
											}
											catch (NumberFormatException ex) {}
										}
									}
								}
							}
						}
						
						msg = msg.substring(0, msg.length()-2);
						
						Main.message(player, String.format(msg, "You've"));
						Main.message(null, ChatColor.GRAY + ChatColor.stripColor(String.format(msg, player.getName())) + ChatColor.RESET);
					}
				}
				else if (count < challengeThresholds[i])
				{
					int min = 0, max = challengeThresholds[i];
					
					if (i > 0) min = challengeThresholds[i-1];
					
					showChallengeBossBar(player, info, i+1, count - min, max);
					
					break;
				}
			}
		}
		
		progress.put(registry, count);
		
		updateChallengeProgress(id, progress);
	}
	
	public static void cleanupCashDrops()
	{
		for (Item cash : cashDrops)
		{
			if (cash.isValid())
			{
				cash.remove();
			}
		}
	}
	
	private String formatDouble(double value)
	{
		if (Math.floor(value) == value)
		{
			return String.format("%1$.0f", value);
		}
		else
		{
			return Double.toString(value);
		}
	}
	
	private boolean vowelCheck(String input, boolean vowelY)
	{
		if ((input == null) || input.isEmpty()) return false;
		
		switch(Character.toLowerCase(input.charAt(0)))
		{
		case 'a':
		case 'e':
		case 'i':
		case 'o':
		case 'u':	return true;
		case 'y':	return vowelY;
		default:	return false;
		}
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		registerPlayerCombo(p);
		
		PlayerHead.storePlayerHeadInfo(p.getName(), p.getDisplayName());
	}
	
	private void registerPlayerCombo(Player player)
	{
		if (null == player) return;
		
		UUID id = player.getUniqueId();
		
		if (combos.containsKey(id)) combos.remove(id);
		
		combos.put(id, new PlayerCombo(id));
	}
	
	@EventHandler
	private void onSlimeSplitEvent(SlimeSplitEvent e)
	{
		Slime parent = e.getEntity();
		
		if (spawnerMobs.contains(parent.getUniqueId()))
		{
			int count = e.getCount();
			Location location = parent.getLocation();
			World world = location.getWorld();
			int childSize = parent.getSize()-1;
			for (int i = 0; i < count; i++)
			{
				Slime child = (Slime)world.spawnEntity(location, parent.getType());
				child.setSize(childSize);
				spawnerMobs.add(child.getUniqueId());
			}
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent e)
	{
		removePlayerCombo(e.getPlayer());
	}
	
	private void removePlayerCombo(Player player)
	{
		if (null == player) return;
		
		UUID id = player.getUniqueId();
		
		if (combos.containsKey(id)) combos.remove(id);
	}
	
	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e)
	{
		Entity ent = e.getEntity();
		
		Entity damager = e.getDamager();
		
		if (ent == damager) return;
		
		if (Main.hasWorldGuard && (damager instanceof Player))
		{
			if (!(ent instanceof Player))
			{
				if (!WorldGuard.doesLocationAllowMobDamage(damager.getLocation()))
				{
					e.setDamage(0);
					e.setCancelled(true);
					
					return;
				}
			}
		}
		
		double damageMod = Math.min(Region.getDamageMultiplier(damager.getLocation()), Region.getDamageMultiplier(ent.getLocation()));
		
		if (damageMod != 1)
		{
			e.setDamage(e.getDamage() * damageMod);
			
			if (damageMod <= 0)
			{
				e.setCancelled(true);
				
				return;
			}
		}
		
		if ((e.getDamage() > 0) && (ent instanceof Player))
		{
			LivingEntity attacker = null;
			
			if (damager instanceof LivingEntity) attacker = (LivingEntity) damager;
			if (damager instanceof Projectile)
			{
				ProjectileSource source = ((Projectile) damager).getShooter();
				
				if (source instanceof LivingEntity)
				{
					attacker = (LivingEntity) source;
				}
			}
			
			if (attacker != null)
			{
				Pair<MobType,Integer> info = MobType.getFromEntity(attacker);
				
				boolean PvP = (attacker instanceof Player);
				
				if ((info != null) || PvP)
				{
					Player player = (Player) ent;
					
					UUID id = player.getUniqueId();
					
					PlayerCombo combo = combos.get(id);
					
					if ((combo != null) && (combo.combo > 0))
					{
						combo.resetCombo();
						
						if (info != null)
						{
							String mobName = info.a.getDisplayName(info.b).toLowerCase();
							String aOrAn = (vowelCheck(mobName, false) ? "An " : "A ");
						
							Main.message(player, String.format(HelpText.ComboReset, aOrAn, mobName), true);
						}
						else
						{
							Main.message(player, String.format(HelpText.ComboReset, "", ((Player)attacker).getDisplayName()), true);
						}
					}
					
					ArrayList<LivingEntity> damagers = new ArrayList<LivingEntity>();;
					
					if (playerDamagers.containsKey(id))
					{
						ArrayList<LivingEntity> old = playerDamagers.get(id);
						for (LivingEntity live : old)
						{
							if (live.isValid()) damagers.add(live);
						}
					}
					
					damagers.add(attacker);
					
					playerDamagers.put(id, damagers);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerPickupItemEvent(PlayerPickupItemEvent e)
	{
		Item pickup = e.getItem();
		
		ItemStack item = pickup.getItemStack();
		
		Material mat = item.getType();
		
		if (mat == Material.GOLD_BLOCK)
		{	
			if (pickup.isCustomNameVisible())
			{
				String name = pickup.getCustomName();
				
				if (name.startsWith(HelpText.CashPrefix))
				{
					double value = 0;
					
					try
					{
						int prefix = HelpText.CashPrefix.length();
						value = Double.valueOf(name.substring(prefix));
					}
					catch (NumberFormatException ex)
					{
						return;
					}
					
					Player player = e.getPlayer();
					
					if ((player != null) && (value > 0))
					{
						Main.giveMoney(player, value);
						
						Main.message(player, String.format(HelpText.PickupCash, "You", formatDouble(value)));
						Main.message(null, ChatColor.GRAY + ChatColor.stripColor(String.format(HelpText.PickupCash, player.getName(), formatDouble(value))) + ChatColor.RESET);
						
						if (cashDrops.contains(pickup)) cashDrops.remove(pickup);
						
						pickup.remove();
						
						e.setCancelled(true);
						return;
					}
				}
			}
		}
		else if (mat == Material.SKULL_ITEM)
		{
			MobType.updateSkull(item);
			
			pickup.setItemStack(MobType.nativeConversion(item));
		}
	}
	
	@EventHandler
	public void onItemMergeEvent(ItemMergeEvent e)
	{
		Item a = e.getEntity();
		Item b = e.getTarget();
		
		if ((a.getItemStack().getType() == Material.GOLD_BLOCK) || (b.getItemStack().getType() == Material.GOLD_BLOCK))
		{
			boolean aCustom = a.isCustomNameVisible();
			boolean bCustom = b.isCustomNameVisible();
			
			if (!aCustom || !bCustom)
			{
				e.setCancelled(true);
				return;
			}
			
			String aName = a.getCustomName();
			String bName = b.getCustomName();
			
			if (!aName.startsWith(HelpText.CashPrefix) || !bName.startsWith(HelpText.CashPrefix))
			{
				e.setCancelled(true);
				return;
			}
			
			double aValue, bValue;
			
			try
			{
				int prefix = HelpText.CashPrefix.length();
				aValue = Double.valueOf(aName.substring(prefix));
				bValue = Double.valueOf(bName.substring(prefix));
			}
			catch (NumberFormatException ex)
			{
				e.setCancelled(true);
				return;
			}
			
			String updated = HelpText.CashPrefix + formatDouble(aValue + bValue);
			
			ItemStack stack = b.getItemStack();
			
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(updated);
			
			stack.setItemMeta(meta);
			
			b.setItemStack(stack);

			b.setCustomName(updated);
			
			if (cashDrops.contains(a)) cashDrops.remove(a);
			
			a.remove();
			
			if (!cashDrops.contains(b)) cashDrops.add(b);
			
			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent e)
	{
		Player victim = e.getEntity();
		LivingEntity killer = victim.getKiller();
		
		if (killer == null)
		{
			EntityDamageEvent d = victim.getLastDamageCause();
			if ((d != null) && (d instanceof EntityDamageByEntityEvent))
			{
				EntityDamageByEntityEvent dmg = (EntityDamageByEntityEvent) d;
				Entity ent = dmg.getDamager();
				if (ent instanceof LivingEntity)
				{
					killer = (LivingEntity) ent;
				}
			}
		}
		
		if (killer != null)
		{
			if (killer instanceof Player)
			{
				e.setDeathMessage(null);
			}
			else
			{
				String name = killer.getCustomName();
				
				if ((name != null) && (!name.isEmpty()))
				{
					CustomMob mob = CustomMob.getByName(name);
					
					if (mob != null)
					{
						String mobName = mob.displayName;
						String prefix = mob.prefix;
						if (!prefix.isEmpty())
						{
							prefix = " "+prefix;
						}
						
						Main.message(victim, String.format(HelpText.DeathPvE, "You", "were", prefix, mobName));
						
						String msgBroadcast = String.format(HelpText.DeathPvE, victim.getDisplayName(), "were", prefix, mobName);
						
						for (Player p : Main.server.getOnlinePlayers())
						{
							if (p != victim)
							{
								Main.message(p, msgBroadcast);
							}
						}
						
						e.setDeathMessage(null);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e)
	{
		LivingEntity victim = e.getEntity();
		
		if (spawnerMobs.contains(victim.getUniqueId())) return;
		
		Pair<MobType,Integer> info = MobType.getFromEntity(victim);
		
		String customName = victim.getCustomName();

		CustomMob customMob = null;
		
		Player killer = victim.getKiller();

		double rewardMod = Region.getRewardMultiplier(victim.getLocation());
		
		if (Main.hasWorldGuard && (killer != null))
		{
			Location loc = killer.getLocation();
			if (!WorldGuard.doesLocationAllowMobSpawning(loc) || !WorldGuard.doesLocationAllowItemDrops(loc))
			{
				rewardMod = 0;
			}
		}
		
		if (killer != null)
		{
			Math.min(rewardMod, Region.getRewardMultiplier(killer.getLocation()));
		}
		
		if (customName != null)
		{
			customMob = CustomMob.getByName(customName);
			if (customMob != null) info = null;
		}
		
		boolean PvP = (victim instanceof Player);
		
		if (PvP && (((Player)victim) == killer))
		{
			Main.server.broadcastMessage("§6Player §c"+killer.getName()+"§6 has taken their own life.");
			return;
		}
		
		if ((info != null) || (PvP) || (customMob != null))
		{
			double bonus = 0;
			
			if (!PvP && (customMob == null) && ((info.a == MobType.ZOMBIE) || (info.a == MobType.PIG_ZOMBIE)))
			{
				EntityEquipment equipment = victim.getEquipment();
				if (equipment != null)
				{
					ItemStack a = equipment.getItemInMainHand();
					ItemStack b = equipment.getItemInOffHand();
					
					if ((a.getType() == Material.GOLD_BLOCK))
					{
						ItemMeta meta = a.getItemMeta();
						if (meta.hasDisplayName())
						{
							String name = meta.getDisplayName();
							
							if (name.startsWith(HelpText.CashPrefix))
							{
								try
								{
									int prefix = HelpText.CashPrefix.length();
									bonus += Double.valueOf(name.substring(prefix));
									
									equipment.setItemInMainHand(null);
								}
								catch (NumberFormatException ex) { }
							}
						}
					}
					
					if ((b.getType() == Material.GOLD_BLOCK))
					{
						ItemMeta meta = b.getItemMeta();
						if (meta.hasDisplayName())
						{
							String name = meta.getDisplayName();
							
							if (name.startsWith(HelpText.CashPrefix))
							{
								try
								{
									int prefix = HelpText.CashPrefix.length();
									bonus += Double.valueOf(name.substring(prefix));
									
									equipment.setItemInOffHand(null);
								}
								catch (NumberFormatException ex) { }
							}
						}
					}
				}
			}
			
			if (killer != null)
			{
				Location loc = killer.getLocation();
				
				Location tomb = victim.getLocation();
				
				UUID id = killer.getUniqueId();
				
				double grindNerf = 1, reward = 0, rewardMultiplier = 0;
				
				List<String> multipliers = new ArrayList<String>();
				
				boolean reset = true;
				
				if (PvP || (customMob != null))
				{
					reset = false;
				}
				else if (grindLocations.containsKey(id))
				{
					Hashtable<Pair<MobType,Integer>, GrindInfo> grindingLog = grindLocations.get(id);
					
					boolean hasGrindedBefore = grindingLog.containsKey(info);
					
					if (grindingLog.size() > 2)
					{
						Set<Pair<MobType,Integer>> array = grindingLog.keySet();
						
						if (hasGrindedBefore) array.remove(info);
						
						Random r = new Random();
						int index = 0, selection = r.nextInt(array.size());
						
						Iterator<Pair<MobType,Integer>> i = array.iterator();
						
						
						while (i.hasNext())
						{
							if (index == selection)
							{
								grindingLog.remove(i.next());
								break;
							}
							
							index++;
						}
					}
					
					if (hasGrindedBefore)
					{
						GrindInfo grind = grindingLog.get(info);
						
						if ((grind !=null) && (grind.lastLocation.getWorld() == tomb.getWorld()))
						{
							long currentTime = System.currentTimeMillis();
							
							double distance = grind.lastLocation.distance(tomb);
							
							if ((distance < 2) || ((currentTime - grind.lastKillTime) < Main.config.getLong("GrindingResetDelay", 60000)))
							{
								if (distance < Main.config.getInt("GrindMobRange"))
								{
									reset = false;
									
									grind.lastLocation = tomb;
									grind.killCount++;
									grind.mobType = info.a;
									grind.lastKillTime = currentTime;
									
									grindingLog.put(info, grind);
									grindLocations.put(id, grindingLog);
									
									if (grind.killCount >= 4)
									{
										grindNerf = Math.max(grindNerf - (grind.killCount-4) * 0.125, 0);
										
										if ((currentTime - grind.lastKillTime) < 1000) grindNerf = 0;
										
										if (grind.killCount >= 12)
										{
											if (!grind.hasBeenWarned)
											{
												Main.message(killer, String.format("%1$s%2$s§c rewards are temporarily disabled due to grinding.§r", MobType.getRarityColor(info.a, info.b), MobType.getDisplayName(info.a, info.b)), true);
												Main.message(killer, "§6If you are not grinding, pm one of our mods.§r", true);
												
												Main.message(null, ChatColor.GRAY + ChatColor.stripColor(String.format("%1$s has been flagged for grinding %2$s.", killer.getName(), info.a.getDisplayName())) + ChatColor.RESET);
												
												grind.hasBeenWarned = true;
											}
											
											return;
										}
									}
								}
							}
						}
					}
				}
				
				if (reset)
				{
					Hashtable<Pair<MobType,Integer>, GrindInfo> grindingLog = grindLocations.containsKey(id) ? grindLocations.get(id) : new Hashtable<Pair<MobType,Integer>, GrindInfo>();
					
					grindingLog.put(info, new GrindInfo(tomb,info.a));
					
					grindLocations.put(id, grindingLog);
				}
				
				EntityDamageEvent dmg = victim.getLastDamageCause();
				
				DamageCause cause = dmg.getCause();
				
				PlayerInventory inv = killer.getInventory();
				
				boolean rangedWeapon = false;
				
				ItemStack mainHand = inv.getItemInMainHand();
				
				if (mainHand != null)
				{
					Material mat = mainHand.getType();
					rangedWeapon = (mat == Material.BOW);
				}
				else
				{
					ItemStack offHand = inv.getItemInOffHand();
					Material mat = offHand.getType();
					rangedWeapon = (mat == Material.BOW);
				}
				
				reset = rangedWeapon;
				
				if (PvP || (customMob != null))
				{
					reset = false;
				}
				else if (grindArcheryLocations.containsKey(id))
				{
					Hashtable<Pair<MobType,Integer>, GrindInfo> grindingLog = grindArcheryLocations.get(id);
					
					boolean hasGrindedBefore = grindingLog.containsKey(info);
					
					if (grindingLog.size() > 2)
					{
						Set<Pair<MobType,Integer>> array = grindingLog.keySet();
						
						if (hasGrindedBefore) array.remove(info);
						
						Random r = new Random();
						int index = 0, selection = r.nextInt(array.size());
						
						Iterator<Pair<MobType,Integer>> i = array.iterator();
						
						
						while (i.hasNext())
						{
							if (index == selection)
							{
								grindingLog.remove(i.next());
								break;
							}
							
							index++;
						}
					}
					
					if (hasGrindedBefore)
					{
						GrindInfo grind = grindingLog.get(info);
						
						if ((grind != null) && (grind.lastLocation.getWorld() == loc.getWorld()))
						{
							long currentTime = System.currentTimeMillis();
							
							double distance = grind.lastLocation.distance(loc);
							
							if ((distance < 2) || ((currentTime - grind.lastKillTime) < Main.config.getLong("GrindingResetDelay", 60000)))
							{	
								if (distance < Main.config.getInt("GrindArcheryRange"))
								{
									reset = false;
									
									grind.lastLocation = loc;
									grind.killCount++;
									grind.mobType = info.a;
									grind.lastKillTime = currentTime;
									
									grindingLog.put(info, grind);
									grindArcheryLocations.put(id, grindingLog);
									
									if (grind.killCount >= 4)
									{
										grindNerf = Math.max(grindNerf - (grind.killCount-4) * 0.125, 0);
										
										if ((currentTime - grind.lastKillTime) < 1000) grindNerf = 0;
										
										if (grind.killCount >= 12)
										{
											if (!grind.hasBeenWarned)
											{
												Main.message(killer, String.format("%1$s%2$s§c rewards are temporarily disabled due to grinding.§r", MobType.getRarityColor(info.a, info.b), MobType.getDisplayName(info.a, info.b)), true);
												Main.message(killer, "§6If you are not grinding, pm one of our mods.§r", true);
												
												Main.message(null, ChatColor.GRAY + ChatColor.stripColor(String.format("%1$s has been flagged for grinding %2$s.", killer.getName(), info.a.getDisplayName())) + ChatColor.RESET);
												
												grind.hasBeenWarned = true;
											}
											
											return;
										}
									}
								}
							}
						}
					}
				}
				
				if (reset)
				{
					Hashtable<Pair<MobType,Integer>, GrindInfo> grindingLog = grindArcheryLocations.containsKey(id) ? grindArcheryLocations.get(id) : new Hashtable<Pair<MobType,Integer>, GrindInfo>();
					
					grindingLog.put(info, new GrindInfo(loc,info.a));
					
					grindArcheryLocations.put(id, grindingLog);
				}
				
				if (PvP)
				{
					reward = Main.config.getDouble("BountyRewardPerPlayer", 500);
				}
				else if (customMob != null)
				{
					reward = customMob.reward;
				}
				else
				{
					reward = info.a.getReward(info.b);
					
					if (reward > 0)
					{
						PlayerCombo combo = combos.get(id);
					
						if (combo == null)
						{
							registerPlayerCombo(killer);
		
							combo = combos.get(id);
							
							if (combo == null) return;
						}
						
						combo.incrementCombo();
						
						rewardMultiplier = combo.getMultiplier();
						
						if (rewardMultiplier > 0) multipliers.add(combo.getMultiplierName());
					}
				}
				
				boolean sprinting = killer.isSprinting();
				
				boolean oneHit = (dmg.getDamage() >= victim.getMaxHealth());
				
				double distance = tomb.distance(loc);
				
				boolean inAir = (killer.getFallDistance() >= 1);
				
				boolean untouched = false;
				
				if (playerDamagers.containsKey(id))
				{
					ArrayList<LivingEntity> damagers = playerDamagers.get(id);
					if (damagers != null)
					{
						untouched = !damagers.contains(victim);
					}
				}
				else
				{
					untouched = true;
				}
				
				boolean untouchable = false;
				
				if (!PvP && (customMob == null))
				{	
					switch(info.a)
					{
					case BLAZE:
					case CAVE_SPIDER:
					case ENDER_DRAGON:
					case GUARDIAN:
					case SHULKER:
					case WITHER:
						untouchable = untouched;
						break;
						
					case SKELETON:
						untouchable = untouched && (info.b != 1);
						break;
						
					default:
						untouchable = false;
						break;
					}
				}
				
				boolean primed = false;
				
				boolean mounted = false;
				boolean broadside = false;
				boolean gangster = false;
				boolean mailbox = false;
				boolean angel = false;
				
				Entity vehicle = killer.getVehicle();
				
				if (vehicle != null)
				{
					mounted = (vehicle instanceof Horse);
					broadside = (vehicle instanceof Boat);
					if (vehicle instanceof Minecart)
					{
						gangster = rangedWeapon;
						mailbox = !rangedWeapon;
					}
				}
				else
				{
					ItemStack chestPlate = inv.getChestplate();
					if (chestPlate != null)
					{
						angel = (chestPlate.getType() == Material.ELYTRA) && inAir;
					}
				}
				
				boolean falling = !angel && inAir && !killer.isFlying();
				
				boolean prosniper = rangedWeapon && (distance > 40);
				boolean sniper = rangedWeapon && !prosniper && (distance > 20);
				
				boolean blitzkrieg = !rangedWeapon && sprinting && (distance < 3) && oneHit;
				boolean blitz = !rangedWeapon && sprinting && !blitzkrieg && (distance < 3);
				
				boolean sneaky = false;
				
				try
				{
					Creature creature = (Creature)victim;
					
					if (creature != null)
					{
						Block block = loc.getBlock();
						byte light = block.getLightLevel();
						Material mat = block.getType();
						
						boolean sneaking = killer.isSneaking();
						sneaky = ((sneaking && (light < 7)) || (light < 3) || killer.hasPotionEffect(PotionEffectType.INVISIBILITY) || (((mat == Material.GRASS) || (mat == Material.LONG_GRASS) || (mat == Material.DOUBLE_PLANT)) && (((light < 12) && sneaking) || (light < 7))));
						
						LivingEntity target = creature.getTarget();
						if ((target != null) && (target instanceof Player))
						{
							if ((((Player) target) == killer))
								sneaky = false;
							
							if (!PvP && (customMob == null)) primed = ((info.a == MobType.CREEPER) && (target.getLocation().distance(victim.getLocation()) <= 2.5));
						}
					}
				}
				catch(Exception ex) { }
				
				boolean scorch = false;
				boolean lava = false;
				
				if (prosniper)
				{
					rewardMultiplier+=2;
					multipliers.add("§dPro Sniper§r");
				}
				if (sniper)
				{
					rewardMultiplier++;
					multipliers.add("§aSniper§r");
				}
				if (blitzkrieg)
				{
					rewardMultiplier+=2;
					multipliers.add("§dBlitzkrieg§r");
				}
				if (blitz)
				{
					rewardMultiplier++;
					multipliers.add("§aBlitz§r");
				}
				if (sneaky)
				{
					rewardMultiplier++;
					multipliers.add("§aSneaky§r");
				}
				if (untouchable)
				{
					rewardMultiplier+=2;
					multipliers.add("§dUntouchable§r");
				}
				if (mounted)
				{
					rewardMultiplier++;
					multipliers.add("§aMounted§r");
				}
				if (broadside)
				{
					rewardMultiplier++;
					multipliers.add("§aBroadside§r");
				}
				if (gangster)
				{
					rewardMultiplier++;
					multipliers.add("§aGangster§r");
				}
				if (mailbox)
				{
					rewardMultiplier+=2;
					multipliers.add("§dMailbox Baseball§r");
				}
				if (angel)
				{
					rewardMultiplier++;
					multipliers.add("§aAngel Of Death§r");
				}
				if (falling)
				{
					rewardMultiplier+=2;
					multipliers.add("§dFalling Star§r");
				}
				if (primed)
				{
					rewardMultiplier+=2;
					multipliers.add("§dBomb Defusal§r");
				}
				
				switch(cause)
				{
				case FIRE_TICK:
				case FIRE:
					scorch = true;
					break;
					
				case DROWNING:
					rewardMultiplier++;
					multipliers.add("§aAnchor§r");
					break;
					
				case FALL:
				case VOID:
					rewardMultiplier++;
					multipliers.add("§aSparta§r");
					break;
					
				case HOT_FLOOR:
					rewardMultiplier++;
					multipliers.add("§aFire Walking§r");
					break;
					
				case LIGHTNING:
					rewardMultiplier++;
					multipliers.add("§aThor§r");
					break;
					
				case LAVA:
					lava = true;
					break;
					
				case MAGIC:
					rewardMultiplier++;
					multipliers.add("§aWizard§r");
					break;
					
				default: break;
				}
				
				if (!lava)
				{
					Block block = tomb.getBlock();
					lava = ((block.getType() == Material.LAVA) || (block.getType() == Material.STATIONARY_LAVA));
				}
				
				if (lava)
				{
					rewardMultiplier+=2;
					multipliers.add("§dSmeagle§r");
				}
				
				scorch = (scorch || (victim.getFireTicks() > 0));
				
				if (!PvP && (customMob == null) && ((info.a == MobType.PIG_ZOMBIE) || (info.a == MobType.BLAZE) || (info.a == MobType.MAGMA_CUBE) || ((info.a == MobType.SKELETON) && (info.b == 1)))) scorch = false;
				
				if (scorch)
				{
					rewardMultiplier++;
					multipliers.add("§aScorch§r");
				}
				
				if (customMob != null) rewardMultiplier += customMob.multiplier;
				
				reward *= (rewardMultiplier+1);
				
				reward *= rewardMod;
				reward *= grindNerf;
				
				if (PvP)
				{
					Player v = (Player) victim;
					
					int count = getBounty(id);
					count++;
					setBounty(id, count);
					
					double bountyReward = count * Main.config.getDouble("BountyRewardPerPlayer", 500);
					
					reward *= getBounty(v.getUniqueId());
					
					setBounty(v.getUniqueId(), 0);
					
					reward = Math.min(Main.getMoney(v), reward);
					
					String msgServer, msgBroadcast, msgKiller, msgVictim;
					
					String killerName = killer.getName();
					String killerNick = killer.getDisplayName();
				
					String victimName = v.getName();
					String victimNick = v.getDisplayName();

					String weaponName = "";
					
					ItemStack weaponItemStack = inv.getItemInMainHand();
					
					if (weaponItemStack == null) weaponItemStack = inv.getItemInOffHand();
					
					Material weaponMaterial = (weaponItemStack != null) ? weaponItemStack.getType() : null;
					
					ItemMeta weaponMeta = (weaponItemStack != null) ? weaponItemStack.getItemMeta() : null;
					
					if (weaponMaterial == null)
						weaponName = " with their fist";
					else if ((weaponMeta != null) && (weaponMeta.hasDisplayName()))
						weaponName = " with their " + ChatColor.AQUA + "" + ChatColor.ITALIC + ChatColor.stripColor(weaponMeta.getDisplayName()) + ChatColor.RESET;
					
					String cash = formatDouble(reward);
					
					String combo = "";
					
					String[] multis = multipliers.toArray(new String[0]);
					
					for (int i = 0; i < multis.length; i++)
					{
						combo += " x "+multis[i]; 
					}
					
					String bounty = formatDouble(bountyReward);
					
					if (reward > 0)
					{
						msgServer = String.format(HelpText.PvPreward, killerName, victimName, (weaponName.length() > 0) ? " with their \"" + weaponName.substring(12,weaponName.length()) + "\"" : "", "claimed §2", cash, combo, "has", bounty);
						msgBroadcast = String.format(HelpText.PvPreward, killerNick, victimNick, weaponName, "claimed §2", cash, combo, "has", bounty);
						msgKiller = String.format(HelpText.PvPreward, "You", victimNick, weaponName.replaceFirst("their", "your"), "claimed §2", cash, combo, "have", bounty);
						msgVictim = String.format(HelpText.PvPreward, killerNick, "you", weaponName, "took §c", cash, combo, "has", bounty);
					}
					else
					{
						msgServer = String.format(HelpText.PvPinform, killerName, victimName, (weaponName.length() > 0) ? " with their \"" + weaponName.substring(12,weaponName.length()) + "\"" : "", "has", bounty);
						msgBroadcast = String.format(HelpText.PvPinform, killerNick, victimNick, weaponName, "has", bounty);
						msgKiller = String.format(HelpText.PvPinform, "You", victimNick, weaponName.replaceFirst("their", "your"), "have", bounty);
						msgVictim = String.format(HelpText.PvPinform, killerNick, "you", weaponName, "has", bounty);
					}
					
					Main.message(null, msgServer);
					
					Main.message(killer, msgKiller);
					
					Main.message(v, msgVictim);
					
					for (Player p : Main.server.getOnlinePlayers())
					{
						if ((p == killer) || (p == victim)) continue;
						
						Main.message(p, msgBroadcast);
					}

					if (reward > 0) tomb.getWorld().dropItemNaturally(tomb, PlayerHead.createPlayerHead(v));
				}
				else if (customMob != null)
				{
					String mobName = customMob.displayName;
					String rarity = MobType.getRarityColorFromInt(customMob.rarity);
					String cash = formatDouble(reward);
					String prefix = customMob.prefix;
					if (!prefix.isEmpty())
					{
						prefix = " "+prefix;
					}
					
					String msgServer, msgClient;
					
					if (reward > 0)
					{
						if (Main.config.getBoolean("CashDrops"))
						{
							msgServer = ChatColor.GRAY + ChatColor.stripColor(String.format(HelpText.CashDropMob, killer.getName(), prefix, rarity, mobName, cash)) + ChatColor.RESET;
							msgClient = String.format(HelpText.CashDropMob, "You've", prefix, rarity, mobName, cash);
							
							dropCashItem(tomb, reward+bonus);
						}
						else
						{
							msgServer = ChatColor.GRAY + ChatColor.stripColor(String.format(HelpText.RewardMob, killer.getDisplayName(), cash, prefix, rarity, mobName)) + ChatColor.RESET;
							msgClient = String.format(HelpText.RewardMob, "You've", cash, prefix, rarity, mobName);
							
							Main.giveMoney(killer, reward+bonus);
						}
						
						String[] multis = multipliers.toArray(new String[0]);
						
						if (multis.length > 0)
						{
							String msgCombo = "";
							
							for (int i = 0; i < multis.length; i++)
							{
								msgCombo += " x "+multis[i]; 
							}
							
							Main.message(null, msgServer + ChatColor.stripColor(msgCombo), true);
							Main.message(killer, msgClient + msgCombo, true);
						}
						else
						{
							Main.message(null, msgServer, true);
							Main.message(killer, msgClient, true);
						}
					}
					
					for (Player p : Main.server.getOnlinePlayers())
					{
						if (p != killer)
						{
							Main.message(p, String.format(HelpText.NamedMob, killer.getDisplayName(), prefix, mobName)+"!");
						}
					}
					
					Main.message(null, ChatColor.GRAY + ChatColor.stripColor(String.format(HelpText.NamedMob, killer.getName(), prefix, mobName) + ChatColor.RESET));
				}
				else
				{
					if (reward > 0) incrementChallenge(id, info);
					
					String mobName = info.a.getDisplayName(info.b).toLowerCase();
					String rarity = info.a.getRarityColor(info.b);
					String cash = formatDouble(reward);
					String aOrAn = (vowelCheck(mobName, false) ? " an" : " a");
					
					String msgServer, msgClient;
					
					if (reward > 0)
					{
						if (Main.config.getBoolean("CashDrops"))
						{
							msgServer = ChatColor.GRAY + ChatColor.stripColor(String.format(HelpText.CashDropMob, killer.getName(), aOrAn, rarity, mobName, cash)) + ChatColor.RESET;
							msgClient = String.format(HelpText.CashDropMob, "You've", aOrAn, rarity, mobName, cash);
							
							dropCashItem(tomb, reward+bonus);
						}
						else
						{
							msgServer = ChatColor.GRAY + ChatColor.stripColor(String.format(HelpText.RewardMob, killer.getDisplayName(), cash, aOrAn, rarity, mobName)) + ChatColor.RESET;
							msgClient = String.format(HelpText.RewardMob, "You've", cash, aOrAn, rarity, mobName);
							
							Main.giveMoney(killer, reward+bonus);
						}
						
						String[] multis = multipliers.toArray(new String[0]);
						
						if (multis.length > 0)
						{
							String msgCombo = "";
							
							for (int i = 0; i < multis.length; i++)
							{
								msgCombo += " x "+multis[i]; 
							}
							
							Main.message(null, msgServer + ChatColor.stripColor(msgCombo), true);
							Main.message(killer, msgClient + msgCombo, true);
						}
						else
						{
							Main.message(null, msgServer, true);
							Main.message(killer, msgClient, true);
						}
					}
					
					if ((reward > 0) && info.a.isDropAvailable(info.b))
					{
						ItemStack skull = info.a.createSkull(info.b);
						tomb.getWorld().dropItemNaturally(tomb, skull);
					}
				}
			}
		}
	}
	
	public void dropCashItem(Location loc, double value)
	{
		Item item = loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.GOLD_BLOCK, 1));
		
		ItemStack stack = item.getItemStack();
		
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(HelpText.CashPrefix+formatDouble(value));
		
		stack.setItemMeta(meta);
		
		item.setItemStack(stack);
		
		item.setCustomName(HelpText.CashPrefix+formatDouble(value));
		item.setCustomNameVisible(true);
		item.setInvulnerable(true);
		
		cashDrops.add(item);
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent e)
	{
		Item item = e.getEntity();
		
		ItemStack stack = item.getItemStack();
		
		if (stack.getType() == Material.SKULL_ITEM)
		{
			MobType.updateSkull(stack);
			
			item.setItemStack(MobType.nativeConversion(stack));
		}
	}
	
	@EventHandler
	public void onInventoryOpenEvent(InventoryOpenEvent e)
	{
		Inventory inv = e.getInventory();
		
		ItemStack[] contents = inv.getContents();
		
		for (int i = 0; i < contents.length; i++)
		{
			ItemStack item = contents[i];
			if (item == null) continue;
			if (item.getType() == Material.SKULL_ITEM)
			{
				MobType.updateSkull(item);
				
				contents[i] = MobType.nativeConversion(item);
			}
		}
		
		inv.setContents(contents);
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onSpawnerSpawnEvent(SpawnerSpawnEvent e)
	{
		LivingEntity entity = (LivingEntity) e.getEntity();
		Pair<MobType, Integer> type = MobType.getFromEntity(entity);
		
		if (type != null)
		{
			spawnerMobs.add(entity.getUniqueId());
		}
	}
}
