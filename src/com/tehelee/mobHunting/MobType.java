package com.tehelee.mobHunting;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.PacketDataSerializer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

// Create your own custom head texture tutorial:
// https://bukkit.org/threads/create-your-own-custom-head-texture.424286/

public enum MobType
{
	BAT,
	BLAZE,
	CAVE_SPIDER,
	CHICKEN,
	COW,
	CREEPER,
	ENDER_DRAGON,
	ENDERMAN,
	ENDERMITE,
	GHAST,
	GIANT,
	GUARDIAN,
	HORSE,
	IRON_GOLEM,
	MAGMA_CUBE,
	MUSHROOM_COW,
	OCELOT,
	PIG,
	PIG_ZOMBIE,
	POLAR_BEAR,
	RABBIT,
	SHEEP,
	SHULKER,
	SILVERFISH,
	SKELETON,
	SLIME,
	SNOWMAN,
	SPIDER,
	SQUID,
	VILLAGER,
	WITCH,
	WITHER,
	WOLF,
	ZOMBIE;
	
	private static final Base64 base64 = new Base64();
	
	private static Random random = new Random();
	
	private static SkullMeta baseSkullMeta;
	
	private static List<ArrayList<String>> lore = new ArrayList<ArrayList<String>>();
	
	private static Map<String,Pair<MobType,Integer>> REGISTRY = new HashMap<String, Pair<MobType,Integer>>();
	private static Map<Pair<MobType,Integer>,SkullMeta> CACHE = new HashMap<Pair<MobType,Integer>,SkullMeta>();
	
	private static Map<String,String> CONVERSION = new HashMap<String,String>();
	
	public static String getHeadCodeComment()
	{
		String result = "Mob Head Codes";
		
		for (String s : REGISTRY.keySet())
		{
			Pair<MobType,Integer> info = REGISTRY.get(s);
			result += String.format("\n \"%1$s\" - %2$s", s, info.a.getDisplayName(info.b));
		}
		
		return result;
	}
	
	static
	{	
		ArrayList<String> common = new ArrayList<String>();
		common.add("§7Common Mob Hunting Reward§r");
		
		ArrayList<String> uncommon = new ArrayList<String>();
		uncommon.add("§aUncommon Mob Hunting Reward§r");
		
		ArrayList<String> rare = new ArrayList<String>();
		rare.add("§bRare Mob Hunting Reward§r");
		
		ArrayList<String> legendary = new ArrayList<String>();
		legendary.add("§dLegendary Mob Hunting Reward§r");
		
		lore.add(0, common);
		lore.add(1, uncommon);
		lore.add(2, rare);
		lore.add(3, legendary);
		
		register("URL_Bat", BAT);
		register("URL_Blaze", BLAZE);
		register("URL_CaveSpider", CAVE_SPIDER);
		register("URL_Chicken", CHICKEN);
		register("URL_Cow", COW);
		register("URL_Creeper", CREEPER);
		register("URL_CCreeper", CREEPER, 1);
		register("URL_Dragon", ENDER_DRAGON);
		register("URL_Enderman", ENDERMAN);
		register("URL_Endermite", ENDERMITE);
		register("URL_Ghast", GHAST);
		register("URL_GZombie", GIANT);
		register("URL_Guardian", GUARDIAN);
		register("URL_EGuardian", GUARDIAN, 1);
		register("URL_Horse", HORSE);
		register("URL_Golem", IRON_GOLEM);
		register("URL_LavaSlime", MAGMA_CUBE);
		register("URL_MushroomCow", MUSHROOM_COW);
		register("URL_Ocelot", OCELOT);
		register("URL_BlackCat", OCELOT, 1);
		register("URL_TabbyCat", OCELOT, 2);
		register("URL_SiameseCat", OCELOT, 3);
		register("URL_Pig", PIG);
		register("URL_PigZombie", PIG_ZOMBIE);
		register("URL_PolarBear", POLAR_BEAR);
		register("URL_Rabbit", RABBIT);
		register("URL_WRabbit", RABBIT, 1);
		register("URL_BRabbit", RABBIT, 2);
		register("URL_BWRabbit", RABBIT, 3);
		register("URL_GRabbit", RABBIT, 4);
		register("URL_SPRabbit", RABBIT, 5);
		register("URL_KRabbit", RABBIT, 99);
		register("URL_Sheep", SHEEP);
		register("URL_Shulker", SHULKER);
		register("URL_Silverfish", SILVERFISH);
		register("URL_Skeleton", SKELETON);
		register("URL_WSkeleton", SKELETON, 1);
		register("URL_SSkeleton", SKELETON, 2);
		register("URL_Slime", SLIME);
		register("URL_SnowGolem", SNOWMAN);
		register("URL_Spider", SPIDER);
		register("URL_Squid", SQUID);
		register("URL_Villager", VILLAGER);
		register("URL_Witch", WITCH);
		register("URL_Wither", WITHER);
		register("URL_Wolf", WOLF);
		register("URL_Zombie", ZOMBIE);
		register("URL_VZombie", ZOMBIE, 1);
		register("URL_HZombie", ZOMBIE, 6);
		register("URL_BZombie", ZOMBIE, 99);
	}
	
	public static List<String> getDefaultKillCounter()
	{
		String[] keys = REGISTRY.keySet().toArray(new String[0]);
		
		List<String> stats = new ArrayList<String>();
		
		for (String key : keys) stats.add(key+" 0");
		
		return stats;
	}
	
	public static Pair<MobType,Integer> getFromRegistryId(String key)
	{
		if (!REGISTRY.containsKey(key)) return null;
		
		return REGISTRY.get(key);
	}
	
	
	
	private static void register(String id, MobType type)
	{
		register(id, type, 0);
	}
	
	private static void register(String id, MobType type, int modifier)
	{
		REGISTRY.put(id, new Pair<MobType, Integer>(type, modifier));
	}
	
	private static ConfigurationSection rewards, challenges;
	
	public static void writeDefaultConfig()
	{
		if (!Main.config.isConfigurationSection("MobRewards"))
			Main.config.createSection("MobRewards");
		
		if (!Main.config.isConfigurationSection("EnabledChallenges"))
			Main.config.createSection("EnabledChallenges");
		
		rewards = Main.config.getConfigurationSection("MobRewards");
		
		challenges = Main.config.getConfigurationSection("EnabledChallenges");
		
		MobType[] mobs = MobType.values();
		
		String name;
		
		for (MobType mob : mobs)
		{
			name = mob.getName();
			
			double reward = mob.getDefaultReward();
			
			rewards.addDefault(name, reward);
			
			challenges.addDefault(name, (reward > 0));
		}
	}
	
	public static void populateConversion()
	{
		File file = new File(Main.instance.getDataFolder(), "//" + "conversion.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		if (config.isList("HeadConversions"))
		{
			List<String> headConversions = config.getStringList("HeadConversions");
			
			for (String translation : headConversions)
			{
				String[] split = translation.split("=");
				if (split.length == 2)
				{
					String oldHead = split[0].trim();
					String newHead = split[1].trim();
					
					CONVERSION.put(oldHead, newHead);
				}
			}
		}
		else
		{
			List<String> headConversions = new ArrayList<String>();
			
			headConversions.add("Enderr_"			+ "=" + "URL_PolarBear");
			headConversions.add("MHF_PolarBear"		+ "=" + "URL_PolarBear");
			headConversions.add("POLAR_BEAR"		+ "=" + "URL_PolarBear");
			headConversions.add("Gabriel333"		+ "=" + "URL_HZombie");
			headConversions.add("HUSK"				+ "=" + "URL_HZombie");
			headConversions.add("JeansenDK"			+ "=" + "URL_SSkeleton");
			headConversions.add("MHF_Stray"			+ "=" + "URL_SSkeleton");
			headConversions.add("STRAY"				+ "=" + "URL_SSkeleton");
			headConversions.add("MHF_Shulker"		+ "=" + "URL_Shulker");
			headConversions.add("SHULKER"			+ "=" + "URL_Shulker");
			headConversions.add("MHF_Endermite"		+ "=" + "URL_Endermite");
			headConversions.add("ENDERMITE"			+ "=" + "URL_Endermite");
			headConversions.add("MHF_Guardian"		+ "=" + "URL_Guardian");
			headConversions.add("GUARDIAN"			+ "=" + "URL_Guardian");
			headConversions.add("MHF_EGuardian"		+ "=" + "URL_EGuardian");
			headConversions.add("ELDER_GUARDIAN"	+ "=" + "URL_EGuardian");
			headConversions.add("MHF_KillerRabbit"	+ "=" + "URL_KRabbit");
			headConversions.add("KILLERRABBIT"		+ "=" + "URL_KRabbit");
			headConversions.add("MHF_Slime"			+ "=" + "URL_Slime");
			headConversions.add("SLIME"				+ "=" + "URL_Slime");
			headConversions.add("MHF_LavaSlime"		+ "=" + "URL_LavaSlime");
			headConversions.add("MAGMA_CUBE"		+ "=" + "URL_LavaSlime");
			headConversions.add("MHF_Ghast"			+ "=" + "URL_Ghast");
			headConversions.add("GHAST"				+ "=" + "URL_Ghast");
			headConversions.add("MHF_Blaze"			+ "=" + "URL_Blaze");
			headConversions.add("BLAZE"				+ "=" + "URL_Blaze");
			headConversions.add("MHF_Creeper"		+ "=" + "URL_Creeper");
			headConversions.add("CREEPER"			+ "=" + "URL_Creeper");
			headConversions.add("MHF_Enderman"		+ "=" + "URL_Enderman");
			headConversions.add("ENDERMAN"			+ "=" + "URL_Enderman");
			headConversions.add("MHF_Silverfish"	+ "=" + "URL_Silverfish");
			headConversions.add("SILVERFISH"		+ "=" + "URL_Silverfish");
			headConversions.add("MHF_Skeleton"		+ "=" + "URL_Skeleton");
			headConversions.add("SKELETON"			+ "=" + "URL_Skeleton");
			headConversions.add("MHF_WSkeleton"		+ "=" + "URL_WSkeleton");
			headConversions.add("WITHERSKELETON"	+ "=" + "URL_WSkeleton");
			headConversions.add("MHF_Spider"		+ "=" + "URL_Spider");
			headConversions.add("SPIDER"			+ "=" + "URL_Spider");
			headConversions.add("MHF_CaveSpider"	+ "=" + "URL_CaveSpider");
			headConversions.add("CAVE_SPIDER"		+ "=" + "URL_CaveSpider");
			headConversions.add("ScrafBrothers4"	+ "=" + "URL_Witch");
			headConversions.add("MHF_Witch"			+ "=" + "URL_Witch");
			headConversions.add("WITCH"				+ "=" + "URL_Witch");
			headConversions.add("MHF_Wither"		+ "=" + "URL_Wither");
			headConversions.add("WITHER"			+ "=" + "URL_Wither");
			headConversions.add("Pig_Zombie"		+ "=" + "URL_PigZombie");
			headConversions.add("MHF_PigZombie"		+ "=" + "URL_PigZombie");
			headConversions.add("PIG_ZOMBIE"		+ "=" + "URL_PigZombie");
			headConversions.add("MHF_Zombie"		+ "=" + "URL_Zombie");
			headConversions.add("ZOMBIE"			+ "=" + "URL_Zombie");
			headConversions.add("MHF_Golem"			+ "=" + "URL_Golem");
			headConversions.add("IRON_GOLEM"		+ "=" + "URL_Golem");
			headConversions.add("Bat"				+ "=" + "URL_Bat");
			headConversions.add("MHF_Bat"			+ "=" + "URL_Bat");
			headConversions.add("BAT"				+ "=" + "URL_Bat");
			headConversions.add("MHF_Chicken"		+ "=" + "URL_Chicken");
			headConversions.add("CHICKEN"			+ "=" + "URL_Chicken");
			headConversions.add("MHF_Cow"			+ "=" + "URL_Cow");
			headConversions.add("COW"				+ "=" + "URL_Cow");
			headConversions.add("Lion"				+ "=" + "URL_Horse");
			headConversions.add("MHF_Horse"			+ "=" + "URL_Horse");
			headConversions.add("HORSE"				+ "=" + "URL_Horse");
			headConversions.add("MHF_MushroomCow"	+ "=" + "URL_MushroomCow");
			headConversions.add("MUSHROOM_COW"		+ "=" + "URL_MushroomCow");
			headConversions.add("MHF_Ocelot"		+ "=" + "URL_Ocelot");
			headConversions.add("OCELOT"			+ "=" + "URL_Ocelot");
			headConversions.add("MHF_Pig"			+ "=" + "URL_Pig");
			headConversions.add("PIG"				+ "=" + "URL_Pig");
			headConversions.add("MHF_Rabbit"		+ "=" + "URL_Rabbit");
			headConversions.add("RABBIT"			+ "=" + "URL_Rabbit");
			headConversions.add("MHF_Sheep"			+ "=" + "URL_Sheep");
			headConversions.add("SHEEP"				+ "=" + "URL_Sheep");
			headConversions.add("Snowman"			+ "=" + "URL_SnowGolem");
			headConversions.add("MHF_Snowman"		+ "=" + "URL_SnowGolem");
			headConversions.add("MHF_SnowGolem"		+ "=" + "URL_SnowGolem");
			headConversions.add("SNOWMAN"			+ "=" + "URL_SnowGolem");
			headConversions.add("MHF_Squid"			+ "=" + "URL_Squid");
			headConversions.add("SQUID"				+ "=" + "URL_Squid");
			headConversions.add("MHF_Villager"		+ "=" + "URL_Villager");
			headConversions.add("VILLAGER"			+ "=" + "URL_Villager");
			headConversions.add("MHF_Wolf"			+ "=" + "URL_Wolf");
			headConversions.add("WOLF"				+ "=" + "URL_Wolf");
			headConversions.add("MHF_Giant"			+ "=" + "URL_GZombie");
			headConversions.add("GIANT"				+ "=" + "URL_GZombie");
			headConversions.add("MHF_EnderDragon"	+ "=" + "URL_Dragon");
			headConversions.add("ENDER_DRAGON"		+ "=" + "URL_Dragon");
			
			for (String translation : headConversions)
			{
				String[] split = translation.split("=");
				if (split.length == 2)
				{
					String oldHead = split[0].trim();
					String newHead = split[1].trim();
					
					CONVERSION.put(oldHead, newHead);
				}
			}
			
			config.set("HeadConversions", headConversions);
		}
		
		try
		{
			config.save(file);
		}
		catch (IOException e)
		{
			Main.message(null, "Failed to save conversion!", true);
		}
	}
	
	public static void populateCache()
	{
		Iterator<Pair<MobType,Integer>> values = REGISTRY.values().iterator();
		
		File fileMeta = new File(Main.instance.getDataFolder(), "//" + "cache.yml");
		FileConfiguration configMeta = YamlConfiguration.loadConfiguration(fileMeta);
		
		if (!configMeta.contains("BaseSkullMeta"))
		{
			baseSkullMeta = (SkullMeta)(new ItemStack(Material.SKULL_ITEM, 1, (short) 3).getItemMeta());
			configMeta.set("BaseSkullMeta", baseSkullMeta);
		}
		else
		{
			baseSkullMeta = (SkullMeta) configMeta.get("BaseSkullMeta");
		}
		
		while (values.hasNext())
		{
			Pair<MobType,Integer> pair = values.next();
			
			if ((pair == null) || (pair.a == null) || (pair.b == null)) break;
			
			String key = pair.a.getName() + "_" + pair.b;
			
			SkullMeta meta;
			
			if (!configMeta.contains(key))
			{
				meta = (SkullMeta) MobType.createSkull(pair.a, pair.b, false).getItemMeta();
				configMeta.set(key, meta);
				
			}
			else
			{
				meta = (SkullMeta) configMeta.get(key);
				
				SkullMeta update = (SkullMeta) MobType.createSkull(pair.a, pair.b, false).getItemMeta();
				
				Class<?> uSkullClass = update.getClass();
				GameProfile uProfile = Reflections.getField(uSkullClass, "profile", GameProfile.class).get(update);
				PropertyMap uProperties = uProfile.getProperties();
				
				Class<?> SkullClass = meta.getClass();
				GameProfile profile = Reflections.getField(SkullClass, "profile", GameProfile.class).get(meta);
				PropertyMap properties = uProfile.getProperties();
				
				Object[] props = uProperties.get("textures").toArray();
				
				if (props.length == 1) 
					properties.put("textures", (Property)props[0]);
				
				Reflections.getField(SkullClass, "profile", GameProfile.class).set(meta, profile);
			}
			
			CACHE.put(pair, meta);
		}
		
		try
		{
			configMeta.save(fileMeta);
		}
		catch (IOException e)
		{
			Main.message(null, "Failed to save cache!", true);
		}
	}
	
	public static Pair<MobType, Integer> getFromEntity(LivingEntity entity)
	{
		EntityType type = entity.getType();
		
		switch(type)
		{
		case BAT:			return new Pair<MobType, Integer>(BAT, 0);
		case BLAZE:			return new Pair<MobType, Integer>(BLAZE, 0);
		case CAVE_SPIDER:	return new Pair<MobType, Integer>(CAVE_SPIDER, 0);
		case CHICKEN:		return new Pair<MobType, Integer>(CHICKEN, 0);
		case COW:			return new Pair<MobType, Integer>(COW, 0);
		case CREEPER:		return new Pair<MobType, Integer>(CREEPER, ((Creeper) entity).isPowered() ? 1 : 0);
		case ENDER_DRAGON:	return new Pair<MobType, Integer>(ENDER_DRAGON, 0);
		case ENDERMAN:		return new Pair<MobType, Integer>(ENDERMAN, 0);
		case ENDERMITE:		return new Pair<MobType, Integer>(ENDERMITE, 0);
		case GHAST:			return new Pair<MobType, Integer>(GHAST, 0);
		case GIANT:			return new Pair<MobType, Integer>(GIANT, 0);
		case GUARDIAN:		return new Pair<MobType, Integer>(GUARDIAN, ((Guardian) entity).isElder() ? 1 : 0);
		case HORSE:			return new Pair<MobType, Integer>(HORSE, 0);
		case IRON_GOLEM:	return new Pair<MobType, Integer>(IRON_GOLEM, 0);
		case MAGMA_CUBE:	return new Pair<MobType, Integer>(MAGMA_CUBE, ((MagmaCube) entity).getSize());
		case MUSHROOM_COW:	return new Pair<MobType, Integer>(MUSHROOM_COW, 0);
		case OCELOT:
			switch(((Ocelot) entity).getCatType())
			{
			default:			return new Pair<MobType, Integer>(OCELOT, 0);
			case BLACK_CAT:		return new Pair<MobType, Integer>(OCELOT, 1);
			case RED_CAT:		return new Pair<MobType, Integer>(OCELOT, 2);
			case SIAMESE_CAT:	return new Pair<MobType, Integer>(OCELOT, 3);
			}
		case PIG:			return new Pair<MobType, Integer>(PIG, 0);
		case PIG_ZOMBIE:	return new Pair<MobType, Integer>(PIG_ZOMBIE, 0);
		case POLAR_BEAR:	return new Pair<MobType, Integer>(POLAR_BEAR, 0);
		case RABBIT:
			switch(((Rabbit) entity).getRabbitType())
			{
			default:				return new Pair<MobType, Integer>(RABBIT, 0);
			case WHITE:				return new Pair<MobType, Integer>(RABBIT, 1);
			case BLACK:				return new Pair<MobType, Integer>(RABBIT, 2);
			case BLACK_AND_WHITE:	return new Pair<MobType, Integer>(RABBIT, 3);
			case GOLD:				return new Pair<MobType, Integer>(RABBIT, 4);
			case SALT_AND_PEPPER:	return new Pair<MobType, Integer>(RABBIT, 5);
			case THE_KILLER_BUNNY:	return new Pair<MobType, Integer>(RABBIT, 99);
			
			}
		case SHEEP:			return new Pair<MobType, Integer>(SHEEP, 0);
		case SHULKER:		return new Pair<MobType, Integer>(SHULKER, 0);
		case SILVERFISH:	return new Pair<MobType, Integer>(SILVERFISH, 0);
		case SKELETON:
			switch(((Skeleton) entity).getSkeletonType())
			{
			default:		return new Pair<MobType, Integer>(SKELETON, 0);
			case WITHER:	return new Pair<MobType, Integer>(SKELETON, 1);
			case STRAY:		return new Pair<MobType, Integer>(SKELETON, 2);
			}
			
		case SLIME:			return new Pair<MobType, Integer>(SLIME, 0);
		case SNOWMAN:		return new Pair<MobType, Integer>(SNOWMAN, 0);
		case SPIDER:		return new Pair<MobType, Integer>(SPIDER, 0);
		case SQUID:			return new Pair<MobType, Integer>(SQUID, 0);
		case VILLAGER:		return new Pair<MobType, Integer>(VILLAGER, 0);
		case WITCH:			return new Pair<MobType, Integer>(WITCH, 0);
		case WITHER:		return new Pair<MobType, Integer>(WITHER, 0);
		case WOLF:			return new Pair<MobType, Integer>(WOLF, 0);
		case ZOMBIE:
			switch(((Zombie) entity).getVillagerProfession())
			{
			default:
				if (((Zombie) entity).isBaby())
					return new Pair<MobType, Integer>(ZOMBIE, 99);
				else
					return new Pair<MobType, Integer>(ZOMBIE, 0);
			case FARMER:		return new Pair<MobType, Integer>(ZOMBIE, 1);
			case LIBRARIAN:		return new Pair<MobType, Integer>(ZOMBIE, 2);
			case PRIEST:		return new Pair<MobType, Integer>(ZOMBIE, 3);
			case BLACKSMITH:	return new Pair<MobType, Integer>(ZOMBIE, 4);
			case BUTCHER:		return new Pair<MobType, Integer>(ZOMBIE, 5);
			case HUSK:			return new Pair<MobType, Integer>(ZOMBIE, 6);
			}
		default:			return null;
		}
	}
	
	public EntityType getEntityType()
	{
		return getEntityType(this);
	}
	
	public static EntityType getEntityType(MobType type)
	{
		switch(type)
		{
		case BAT:			return EntityType.BAT;
		case BLAZE:			return EntityType.BLAZE;
		case CAVE_SPIDER:	return EntityType.CAVE_SPIDER;
		case CHICKEN:		return EntityType.CHICKEN;
		case COW:			return EntityType.COW;
		case CREEPER:		return EntityType.CREEPER;
		case ENDER_DRAGON:	return EntityType.ENDER_DRAGON;
		case ENDERMAN:		return EntityType.ENDERMAN;
		case ENDERMITE:		return EntityType.ENDERMITE;
		case GHAST:			return EntityType.GHAST;
		case GIANT:			return EntityType.GIANT;
		case GUARDIAN:		return EntityType.GUARDIAN;
		case HORSE:			return EntityType.HORSE;
		case IRON_GOLEM:	return EntityType.IRON_GOLEM;
		case MAGMA_CUBE:	return EntityType.MAGMA_CUBE;
		case MUSHROOM_COW:	return EntityType.MUSHROOM_COW;
		case OCELOT:		return EntityType.OCELOT;
		case PIG:			return EntityType.PIG;
		case PIG_ZOMBIE:	return EntityType.PIG_ZOMBIE;
		case POLAR_BEAR:	return EntityType.POLAR_BEAR;
		case RABBIT:		return EntityType.RABBIT;
		case SHEEP:			return EntityType.SHEEP;
		case SHULKER:		return EntityType.SHULKER;
		case SILVERFISH:	return EntityType.SILVERFISH;
		case SKELETON:		return EntityType.SKELETON;
		case SLIME:			return EntityType.SLIME;
		case SNOWMAN:		return EntityType.SNOWMAN;
		case SPIDER:		return EntityType.SPIDER;
		case SQUID:			return EntityType.SQUID;
		case VILLAGER:		return EntityType.VILLAGER;
		case WITCH:			return EntityType.WITCH;
		case WITHER:		return EntityType.WITHER;
		case WOLF:			return EntityType.WOLF;
		case ZOMBIE:		return EntityType.ZOMBIE;
		default:			return null;
		}
	}
	
	public String getName()
	{
		return getName(this);
	}
	
	public static String getName(MobType type)
	{
		switch(type)
		{
		case BAT:			return "BAT";
		case BLAZE:			return "BLAZE";
		case CAVE_SPIDER:	return "CAVE_SPIDER";
		case CHICKEN:		return "CHICKEN";
		case COW:			return "COW";
		case CREEPER:		return "CREEPER";
		case ENDER_DRAGON:	return "ENDER_DRAGON";
		case ENDERMAN:		return "ENDERMAN";
		case ENDERMITE:		return "ENDERMITE";
		case GHAST:			return "GHAST";
		case GIANT:			return "GIANT";
		case GUARDIAN:		return "GUARDIAN";
		case HORSE:			return "HORSE";
		case IRON_GOLEM:	return "IRON_GOLEM";
		case MAGMA_CUBE:	return "MAGMA_CUBE";
		case MUSHROOM_COW:	return "MUSHROOM_COW";
		case OCELOT:		return "OCELOT";
		case PIG:			return "PIG";
		case PIG_ZOMBIE:	return "PIG_ZOMBIE";
		case POLAR_BEAR:	return "POLAR_BEAR";
		case RABBIT:		return "RABBIT";
		case SHEEP:			return "SHEEP";
		case SHULKER:		return "SHULKER";
		case SILVERFISH:	return "SILVERFISH";
		case SKELETON:		return "SKELETON";
		case SLIME:			return "SLIME";
		case SNOWMAN:		return "SNOWMAN";
		case SPIDER:		return "SPIDER";
		case SQUID:			return "SQUID";
		case VILLAGER:		return "VILLAGER";
		case WITCH:			return "WITCH";
		case WITHER:		return "WITHER";
		case WOLF:			return "WOLF";
		case ZOMBIE:		return "ZOMBIE";
		default:			return "";
		}
	}
	
	private String getEggName()
	{
		return getEggName(this);
	}
	
	private static String getEggName(MobType type)
	{
		switch(type)
		{
		case BAT:			return "Bat";
		case BLAZE:			return "Blaze";
		case CAVE_SPIDER:	return "CaveSpider";
		case CHICKEN:		return "Chicken";
		case COW:			return "Cow";
		case CREEPER:		return "Creeper";
		case ENDER_DRAGON:	return "EnderDragon";
		case ENDERMAN:		return "Enderman";
		case ENDERMITE:		return "Endermite";
		case GHAST:			return "Ghast";
		case GIANT:			return "Giant";
		case GUARDIAN:		return "Guardian";
		case HORSE:			return "Horse";
		case IRON_GOLEM:	return "IronGolem";
		case MAGMA_CUBE:	return "MagmaCube";
		case MUSHROOM_COW:	return "MushroomCow";
		case OCELOT:		return "Ocelot";
		case PIG:			return "Pig";
		case PIG_ZOMBIE:	return "PigZombie";
		case POLAR_BEAR:	return "PolarBear";
		case RABBIT:		return "Rabbit";
		case SHEEP:			return "Sheep";
		case SHULKER:		return "Shulker";
		case SILVERFISH:	return "Silverfish";
		case SKELETON:		return "Skeleton";
		case SLIME:			return "Slime";
		case SNOWMAN:		return "Snowman";
		case SPIDER:		return "Spider";
		case SQUID:			return "Squid";
		case VILLAGER:		return "Villager";
		case WITCH:			return "Witch";
		case WITHER:		return "Wither";
		case WOLF:			return "Wolf";
		case ZOMBIE:		return "Zombie";
		default:			return "";
		}
	}
	
	public ItemStack getSpawnEgg()
	{
		return getSpawnEgg(this);
	}
	
	public ItemStack getSpawnEgg(int modifier)
	{
		return getSpawnEgg(this, modifier);
	}
	
	public static ItemStack getSpawnEgg(MobType type)
	{
		return getSpawnEgg(type, 0);
	}
	
	public static ItemStack getSpawnEgg(MobType type, int modifier)
	{	
		ItemStack item = new ItemStack(Material.MONSTER_EGG, 1);
		
		ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName(type.getDisplayName(modifier) + getRarityColorFromInt(3) + " Spawn Egg§r");
		
		meta.setLore(lore.get(3));
		
		item.setItemMeta(meta);
		
		net.minecraft.server.v1_10_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = null;
		if (!nmsStack.hasTag())
		{
			tag = new NBTTagCompound();
			nmsStack.setTag(tag);
		}
		if (tag == null) tag = nmsStack.getTag();
		
		NBTTagCompound EntityTag = null;
		if (!tag.hasKey("EntityTag"))
		{
			EntityTag = new NBTTagCompound();
		}
		else
		{
			EntityTag = tag.getCompound("EntityTag");
		}
		
		EntityTag.setString("id", type.getEggName());
		
		switch(type)
		{
		case CREEPER:
			EntityTag.setByte("powered", (byte) modifier);
			break;
			
		case GUARDIAN:
			EntityTag.setByte("Elder", (byte) modifier);
			break;
			
		case RABBIT:
			EntityTag.setInt("RabbitType", modifier);
			break;
		
		case SKELETON:
			EntityTag.setByte("SkeletonType", (byte) modifier);
			break;
			
		case ZOMBIE:
			if (modifier == 6) EntityTag.setInt("ZombieType", 6);
			else if ((modifier > 0) && (modifier < 6)) EntityTag.setInt("ZombieType", random.nextInt(4)+1);
			else EntityTag.setInt("ZombieType", 0);
			break;
			
		default:
			break;
		}
		
		tag.set("EntityTag", EntityTag);
		
		nmsStack.setTag(tag);
		
		item = CraftItemStack.asCraftMirror(nmsStack);
		
		return item;
	}
	
	public boolean isChallengeEnabled()
	{
		return isChallengeEnabled(this);
	}
	
	public static boolean isChallengeEnabled(MobType type)
	{
		return challenges.getBoolean(type.getName());
	}
	
	private int getBaseRarity()
	{
		return getBaseRarity(this);
	}
	
	private static int getBaseRarity(MobType type)
	{
		switch(type)
		{
		case BAT:			return 0;
		case BLAZE:			return 1;
		case CAVE_SPIDER:	return 1;
		case CHICKEN:		return 0;
		case COW:			return 0;
		case CREEPER:		return 0;
		case ENDER_DRAGON:	return 3;
		case ENDERMAN:		return 2;
		case ENDERMITE:		return 1;
		case GHAST:			return 2;
		case GIANT:			return 3;
		case GUARDIAN:		return 1;
		case HORSE:			return 0;
		case IRON_GOLEM:	return 0;
		case MAGMA_CUBE:	return 1;
		case MUSHROOM_COW:	return 1;
		case OCELOT:		return 0;
		case PIG:			return 0;
		case PIG_ZOMBIE:	return 1;
		case POLAR_BEAR:	return 2;
		case RABBIT:		return 0;
		case SHEEP:			return 0;
		case SHULKER:		return 2;
		case SILVERFISH:	return 1;
		case SKELETON:		return 0;
		case SLIME:			return 1;
		case SNOWMAN:		return 0;
		case SPIDER:		return 0;
		case SQUID:			return 0;
		case VILLAGER:		return 0;
		case WITCH:			return 1;
		case WITHER:		return 3;
		case WOLF:			return 0;
		case ZOMBIE:		return 0;
		default:			return 0;
		}
	}
	
	public int getRarity()
	{
		return getRarity(this, 0);
	}
	
	public int getRarity(int modifier)
	{
		return getRarity(this, modifier);
	}
	
	public static int getRarity(MobType type, int modifier)
	{
		int rare = type.getBaseRarity();
		
		switch(type)
		{
		case CREEPER:
			if (modifier == 1) rare += 1;
			break;
			
		case GUARDIAN:
			if (modifier == 1) rare = 3;
			break;
			
		case RABBIT:
			if (modifier == 99) rare += 1;
		
		case SKELETON:
			if ((modifier > 0) && (modifier < 3)) rare += 1;
			break;
			
		case ZOMBIE:
			if (((modifier > 0) && (modifier < 7)) || (modifier == 99)) rare += 1;
			break;
			
		default:
			break;
		}
		
		return Math.min(3, rare);
	}

	public String getRarityColor()
	{
		return getRarityColor(this);
	}
	
	public String getRarityColor(int modifier)
	{
		return getRarityColor(this, modifier);
	}
	
	public static String getRarityColor(MobType type)
	{
		return getRarityColor(type, 0);
	}
	
	public static String getRarityColor(MobType type, int modifier)
	{
		return getRarityColorFromInt(getRarity(type, modifier));
	}
	
	public static String getRarityColorFromInt(int rarity)
	{
		switch(rarity)
		{
		default:	return "§f";
		case 1:		return "§a";
		case 2:		return "§b";
		case 3:		return "§d";
		}
	}
	
	public static List<String> getLoreFromInt(int rarity)
	{
		return lore.get(rarity);
	}
	
	public List<String> getLore()
	{
		return getLore(this);
	}
	
	public List<String> getLore(int modifier)
	{
		return getLore(this, modifier);
	}
	
	public static List<String> getLore(MobType type)
	{
		return getLore(type, 0);
	}
	
	public static List<String> getLore(MobType type, int modifier)
	{
		return lore.get(type.getRarity(modifier));
	}
	
	public double getDefaultReward()
	{
		return getDefaultReward(this);
	}
	
	public static double getDefaultReward(MobType type)
	{
		switch(type)
		{
		case BAT:			return 0;
		case BLAZE:			return 225;
		case CAVE_SPIDER:	return 150;
		case CHICKEN:		return 25;
		case COW:			return 25;
		
		case CREEPER:		return 150; // x5 charged
			
		case ENDER_DRAGON:	return 2500;
		case ENDERMAN:		return 250;
		case ENDERMITE:		return 50;
		case GHAST:			return 250;
		case GIANT:			return 375;
		
		case GUARDIAN:		return 225; // x10
		
		case HORSE:			return 25;
		case IRON_GOLEM:	return 0;
		
		case MAGMA_CUBE:	return 50;
		
		case MUSHROOM_COW:	return 25;
		case OCELOT:		return 25;
		case PIG:			return 25;
		case PIG_ZOMBIE:	return 225;
		case POLAR_BEAR:	return 225;
		case RABBIT:		return 25;
		case SHEEP:			return 25;
		case SHULKER:		return 150;
		case SILVERFISH:	return 50;
		
		case SKELETON:		return 125;
		
		case SLIME:			return 25;
		
		case SNOWMAN:		return 0;
		case SPIDER:		return 100;
		case SQUID:			return 25;
		case VILLAGER:		return 0;
		case WITCH:			return 150;
		case WITHER:		return 625;
		case WOLF:			return 25;
		
		case ZOMBIE:		return 100;
		
		default:			return 0;
		}
	}
	
	public double getReward()
	{
		return getReward(this, 0);
	}
	
	public double getReward(int modifier)
	{
		return getReward(this, modifier);
	}
	
	public static double getReward(MobType type, int modifier)
	{
		double reward = rewards.getDouble(type.getName());
		
		switch(type)
		{
		case CREEPER:
			if (modifier != 0) reward *= 5;
			break;
			
		case GUARDIAN:
			if (modifier != 0) reward *= 10;
			break;
			
		case RABBIT:
			if (modifier == 5) reward = rewards.getDouble(MobType.SKELETON.getName());
			break;
			
		case SLIME:
		case MAGMA_CUBE:
			reward *= ((1+modifier) * 2);
			break;
		
		case SKELETON:
			if (modifier != 0) reward *= 2;
			break;
			
		case ZOMBIE:
			if (modifier == 6) reward *= 1.25;
			else if (modifier == 99) reward *= 2;
			else if (modifier != 0) reward *= 1.5;
			break;
			
		default:
			break;
		}
		
		return reward;
	}
	
	public boolean isDropAvailable()
	{
		return isDropAvailable(this);
	}
	
	public boolean isDropAvailable(int modifier)
	{
		return isDropAvailable(this, modifier);
	}
	
	public static boolean isDropAvailable(MobType type)
	{
		return isDropAvailable(type, 0);
	}
	
	public static boolean isDropAvailable(MobType type, int modifier)
	{
		if ((type == MobType.SKELETON) && (modifier == 1)) return false;
		
		int rarity = getRarity(type, modifier);
		
		double chance;
		
		switch(rarity)
		{
		default:
			chance = 0.05;
			break;
		case 1:
			chance = 0.11;
			break;
		case 2:
			chance = 0.25;
			break;
		case 3:
			chance = 1;
			break;
		}
		
		return (random.nextDouble() <= chance);
	}
	
	public String getDisplayName()
	{
		return getDisplayName(this);
	}
	
	public String getDisplayName(int modifier)
	{
		return getDisplayName(this, modifier);
	}
	
	public static String getDisplayName(MobType type)
	{
		return getDisplayName(type, 0);
	}
	
	public static String getDisplayName(MobType type, int modifier)
	{
		switch(type)
		{
		case BAT:			return "Bat";
		case BLAZE:			return "Blaze";
		case CAVE_SPIDER:	return "Cave Spider";
		case CHICKEN:		return "Chicken";
		case COW:			return "Cow";
		case CREEPER:		return (modifier == 1) ? "Charged Creeper" : "Creeper";
		case ENDER_DRAGON:	return "Ender Dragon";
		case ENDERMAN:		return "Enderman";
		case ENDERMITE:		return "Endermite";
		case GHAST:			return "Ghast";
		case GIANT:			return "Giant Zombie";
		case GUARDIAN:		return (modifier == 1) ? "Elder Guardian" : "Guardian";
		case HORSE:			return "Horse";
		case IRON_GOLEM:	return "Iron Golem";
		case MAGMA_CUBE:	return "Magma Cube";
		case MUSHROOM_COW:	return "Mooshroom";
		case OCELOT:
			switch(modifier)
			{
			default:		return "Ocelot";
			case 1:			return "Black Cat";
			case 2:			return "Tabby Cat";
			case 3:			return "Siamese Cat";
			}
		case PIG:			return "Pig";
		case PIG_ZOMBIE:	return "Pig Zombie";
		case POLAR_BEAR:	return "Polar Bear";
		case RABBIT:
			switch(modifier)
			{
				default:	return "Brown Rabbit";
				case 1:		return "White Rabbit";
				case 2:		return "Black Rabbit";
				case 3:		return "Black & White Rabbit";
				case 4:		return "Gold Rabbit";
				case 5:		return "Salt & Pepper Rabbit";
				case 99:	return "Killer Rabbit";
			}
		case SHEEP:			return "Sheep";
		case SHULKER:		return "Shulker";
		case SILVERFISH:	return "Silverfish";
		case SKELETON:
			switch(modifier)
			{
			default:		return "Skeleton";
			case 1:			return "Wither Skeleton";
			case 2:			return "Stray Skeleton";
			}
		case SLIME:			return "Slime";
		case SNOWMAN:		return "Snow Golem";
		case SPIDER:		return "Spider";
		case SQUID:			return "Squid";
		case VILLAGER:		return "Villager";
		case WITCH:			return "Witch";
		case WITHER:		return "Wither";
		case WOLF:			return "Wolf";
		case ZOMBIE:
			switch(modifier)
			{
			default:		return "Zombie";
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:			return "Zombie Villager";
			case 6:			return "Zombie Husk";
			case 99:		return "Baby Zombie";
			}
		default:			return "";
		}
	}
	
	public String getId()
	{
		return getId(this);
	}
	
	public String getId(int modifier)
	{
		return getId(this, modifier);
	}
	
	public static String getId(MobType type)
	{
		return getId(type, 0);
	}
	
	public static String getId(MobType type, int modifier)
	{
		switch(type)
		{
		case BAT:			return "URL_Bat";
		case BLAZE:			return "URL_Blaze";
		case CAVE_SPIDER:	return "URL_CaveSpider";
		case CHICKEN:		return "URL_Chicken";
		case COW:			return "URL_Cow";
		case CREEPER:		return (modifier == 1) ? "URL_CCreeper" : "URL_Creeper";
		case ENDER_DRAGON:	return "URL_Dragon";
		case ENDERMAN:		return "URL_Enderman";
		case ENDERMITE:		return "URL_Endermite";
		case GHAST:			return "URL_Ghast";
		case GIANT:			return "URL_GZombie";
		case GUARDIAN:		return (modifier == 1) ? "URL_EGuardian" : "URL_Guardian";
		case HORSE:			return "URL_Horse";
		case IRON_GOLEM:	return "URL_Golem";
		case MAGMA_CUBE:	return "URL_LavaSlime";
		case MUSHROOM_COW:	return "URL_MushroomCow";
		case OCELOT:
			switch(modifier)
			{
			default:		return "URL_Ocelot";
			case 1:			return "URL_BlackCat";
			case 2:			return "URL_TabbyCat";
			case 3:			return "URL_SiameseCat";
			}
		case PIG:			return "URL_Pig";
		case PIG_ZOMBIE:	return "URL_PigZombie";
		case POLAR_BEAR:	return "URL_PolarBear";
		case RABBIT:
			switch(modifier)
			{
			default:		return "URL_Rabbit";
			case 1:			return "URL_WRabbit";
			case 2:			return "URL_BRabbit";
			case 3:			return "URL_BWRabbit";
			case 4:			return "URL_GRabbit";
			case 5:			return "URL_SPRabbit";
			case 99:		return "URL_KRabbit";
			}
		case SHEEP:			return "URL_Sheep";
		case SHULKER:		return "URL_Shulker";
		case SILVERFISH:	return "URL_Silverfish";
		case SKELETON:
			switch(modifier)
			{
			default:		return "URL_Skeleton";
			case 1:			return "URL_WSkeleton";
			case 2:			return "URL_SSkeleton";
			}
		case SLIME:			return "URL_Slime";
		case SNOWMAN:		return "URL_SnowGolem";
		case SPIDER:		return "URL_Spider";
		case SQUID:			return "URL_Squid";
		case VILLAGER:		return "URL_Villager";
		case WITCH:			return "URL_Witch";
		case WITHER:		return "URL_Wither";
		case WOLF:			return "URL_Wolf";
		case ZOMBIE:
			switch(modifier)
			{
			default:		return "URL_Zombie";
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:			return "URL_VZombie";
			case 6:			return "URL_HZombie";
			case 99:		return "URL_BZombie";
			}
		default:			return "";
		}
	}
	
	public String getURL()
	{
		return getURL(this);
	}
	
	public String getURL(int modifier)
	{
		return getURL(this, modifier);
	}
	
	public static String getURL(MobType type)
	{
		return getURL(type, 0);
	}
	
	public static String getURL(MobType type, int modifier)
	{
		switch(type)
		{
		case BAT:			return "6ffd808f8127b4ad458d9d2e181c690adf489a6ad32ee2aa4acfa6341fe842";
		case BLAZE:			return "b78ef2e4cf2c41a2d14bfde9caff10219f5b1bf5b35a49eb51c6467882cb5f0";
		case CAVE_SPIDER:	return "41645dfd77d09923107b3496e94eeb5c30329f97efc96ed76e226e98224";
		case CHICKEN:		return "1638469a599ceef7207537603248a9ab11ff591fd378bea4735b346a7fae893";
		case COW:			return "5d6c6eda942f7f5f71c3161c7306f4aed307d82895f9d2b07ab4525718edc5";
		case CREEPER:		return (modifier == 1) ? "4d617e1b1825eec15b87e319eab1585f01013c9db6076de968667c1771e41cd" : "295ef836389af993158aba27ff37b6567185f7a721ca90fdfeb937a7cb5747";
		case ENDER_DRAGON:	return "74ecc040785e54663e855ef0486da72154d69bb4b7424b7381ccf95b095a";
		case ENDERMAN:		return "7a59bb0a7a32965b3d90d8eafa899d1835f424509eadd4e6b709ada50b9cf";
		case ENDERMITE:		return "9ae568ee5978349adc63a5bf37f082ef5512bb264cdb7598efecd71f42d13";
		case GHAST:			return "8b6a72138d69fbbd2fea3fa251cabd87152e4f1c97e5f986bf685571db3cc0";
		case GIANT:			return "ef19f717633d7ddb8e646e55bddb6ac1279f61bb7d2265506233ecf66269";
		case GUARDIAN:		return (modifier == 1) ? "4adc4a6f53afa116027b51d6f2e433ee7afa5d59b2ffa04780be464fa5d61a" : "dfb675cb5a7e3fd25e29da8258f24fc020b3fa950362b8bc8eb252e56e74";
		case HORSE:			return "61902898308730c4747299cb5a5da9c25838b1d059fe46fc36896fee662729";
		case IRON_GOLEM:	return "89091d79ea0f59ef7ef94d7bba6e5f17f2f7d4572c44f90f76c4819a714";
		case MAGMA_CUBE:	return "38957d5023c937c4c41aa2412d43410bda23cf79a9f6ab36b76fef2d7c429";
		case MUSHROOM_COW:	return "d0bc61b9757a7b83e03cd2507a2157913c2cf016e7c096a4d6cf1fe1b8db";
		case OCELOT:
			switch(modifier)
			{
			default:		return "5657cd5c2989ff97570fec4ddcdc6926a68a3393250c1be1f0b114a1db1";
			case 1:			return "18efa5ac86edb7aad271fb18b4f78785d0f49aa8fc7333ae2dbcbfca84b09b9f";
			case 2:			return "27d6a95324505bfac93f113d53e86cd85e3f3ff6d024a61923ec28802596d836";
			case 3:			return "e6e7c574be9777ccee1a97601cecc5bedd5ea6d66765b6424d6b2ebbfe6625b1";
			}
		case PIG:			return "621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4";
		case PIG_ZOMBIE:	return "74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb";
		case POLAR_BEAR:	return "d46d23f04846369fa2a3702c10f759101af7bfe8419966429533cd81a11d2b";
		case RABBIT:
			switch(modifier)
			{
			default:		return "5277a065834848457ca23512c29533a6ab6e304c9121ee9e42857b5ea9d12e";
			case 1:			return "f6dc2aa3527785fc4411ac8bd8d7540ac8c455622e7d1cf869255ca61546df";
			case 2:			return "77ab3c4bf08adda39c10c354cf95fcc1f648ca03483c988bfbc8c98d2b3ca";
			case 3:			return "8177e02cde9043f861a43550adbef0bbccf1f46d325d8554f4171c56377c749";
			case 4:			return "5ee955b449338419971a1f2bd8268cba95d2f646f3d40af8a72eb73c8a336";
			case 5:			return "f3ef69cec0f91e56c36468c63d6b8ae3287148a94f8925671d9ab56c6bd50";
			case 99:		return "afbaf0627ce3a8fa99a8faf3546471724e5924c3aeb3d2af5b7dc46bd58aaa";
			}
		case SHEEP:			return "f31f9ccc6b3e32ecf13b8a11ac29cd33d18c95fc73db8a66c5d657ccb8be70";
		case SHULKER:		return "dd34cb57b3ac6f322ec388963963167cf8afa57aded5dbc5013b3dee0ab22";
		case SILVERFISH:	return "6f7238b469c6d06a18eefbcaf843a4e571908558ad3dcaf8dc45ae1fc5f928";
		case SKELETON:
			switch(modifier)
			{
			default:		return "2e5be6a3c0159d2c1f3b1e4e1d8384b6f7ebac993d58b10b9f8989c78a232";
			case 1:			return "233b41fa79cd53a230e2db942863843183a70404533bbc01fab744769bcb";
			case 2:			return "286d4eabfd777955ceba8cca2abcb0df1c4385e4b14bc5b229e4e64d3f47fe";
			}
		case SLIME:			return "16ad20fc2d579be250d3db659c832da2b478a73a698b7ea10d18c9162e4d9b5";
		case SNOWMAN:		return "1fdfd1f7538c040258be7a91446da89ed845cc5ef728eb5e690543378fcf4";
		case SPIDER:		return "cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1";
		case SQUID:			return "01433be242366af126da434b8735df1eb5b3cb2cede39145974e9c483607bac";
		case VILLAGER:		return "822d8e751c8f2fd4c8942c44bdb2f5ca4d8ae8e575ed3eb34c18a86e93b";
		case WITCH:			return "2e139130d7efd41fbad53735f64f8aff265bd7c54977189c02babbec4b0d07b";
		case WITHER:		return "cdf74e323ed41436965f5c57ddf2815d5332fe999e68fbb9d6cf5c8bd4139f";
		case WOLF:			return "69d1d3113ec43ac2961dd59f28175fb4718873c6c448dfca8722317d67";
		case ZOMBIE:
			switch(modifier)
			{
			default:		return "56fc854bb84cf4b7697297973e02b79bc10698460b51a639c60e5e417734e11";
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:			return "a6224941314bca2ebbb66b10ffd94680cc98c3435eeb71a228a08fd42c24db";
			case 6:			return "a1bc8d6769c34f7148fc9b5c391a9012559df71b67f23e67e109694604b8e";
			case 99:		return "56fc854bb84cf4b7697297973e02b79bc10698460b51a639c60e5e417734e11";
			}
		default:			return "";
		}
	}
	
	public static void updateSkull(ItemStack skull)
	{
		SkullMeta meta = (SkullMeta)skull.getItemMeta();
		
		Short skullType = skull.getDurability();
		
		switch(skullType)
		{
		case 0:
			skull.setItemMeta(CACHE.get(REGISTRY.get("URL_Skeleton")));
			return;
		case 1:
			skull.setItemMeta(CACHE.get(REGISTRY.get("URL_WSkeleton")));
			return;
		case 2:
			skull.setItemMeta(CACHE.get(REGISTRY.get("URL_Zombie")));
			return;
		case 4:
			skull.setItemMeta(CACHE.get(REGISTRY.get("URL_Creeper")));
			return;
		case 5:
			skull.setItemMeta(CACHE.get(REGISTRY.get("URL_Dragon")));
			return;
		default:
			if (meta != null)
			{
				String owner = meta.getOwner();
				
				if ((owner != null) && !owner.isEmpty())
				{	
					Pair<MobType, Integer> info = REGISTRY.get(owner);
					
					if (info == null)
					{
						if (CONVERSION.containsKey(owner))
						{
							info = REGISTRY.get(CONVERSION.get(owner));
						}
						else
						{
							String display = PlayerHead.getPlayerHeadInfoByName(owner);
							
							if (display != null)
							{
								meta.setDisplayName(display);
								
								CustomMob mob = CustomMob.getByName(display);
								
								if (mob != null)
								{
									meta.setLore(MobType.getLoreFromInt(mob.rarity));
								}
								
								skull.setItemMeta(meta);
							}
						}
					}
					
					if (info != null)
					{
						if (CACHE.containsKey(info))
						{
							meta = CACHE.get(info);
						}
						else
						{
							meta.setDisplayName(info.a.getDisplayName(info.b) + " Head");
							meta.setLore(info.a.getLore(info.b));
						}
						
						skull.setItemMeta(meta);
					}
				}
			}
			break;
		}
	}
	
	public static ItemStack nativeConversion(ItemStack skull)
	{
		SkullMeta meta = (SkullMeta)skull.getItemMeta();
		
		if (meta != null)
		{
			String owner = meta.getOwner();
			
			if ((owner != null) && !owner.isEmpty())
			{	
				int SkullType = 3;
				
				if (owner.equalsIgnoreCase("URL_Skeleton"))
					SkullType = 0;
				else if (owner.equalsIgnoreCase("URL_Wither") || owner.equalsIgnoreCase("URL_WSkeleton"))
					SkullType = 1;
				else if (owner.equalsIgnoreCase("URL_Zombie"))
					SkullType = 2;
				else if (owner.equalsIgnoreCase("URL_Creeper"))
					SkullType = 4;
				else if (owner.equalsIgnoreCase("URL_Dragon"))
					SkullType = 5;
				
				if (SkullType != 3)
				{
					ItemStack replacement = new ItemStack(Material.SKULL_ITEM, skull.getAmount(), (short) SkullType);
					
					switch (SkullType)
					{
					case 0:
						replacement.setItemMeta(CACHE.get(REGISTRY.get("URL_Skeleton")));
						break;
					case 1:
						if (owner.equalsIgnoreCase("URL_WSkeleton"))
							replacement.setItemMeta(CACHE.get(REGISTRY.get("URL_WSkeleton")));
						else
							replacement.setItemMeta(CACHE.get(REGISTRY.get("URL_Wither")));
						break;
					case 2:
						replacement.setItemMeta(CACHE.get(REGISTRY.get("URL_Zombie")));
						break;
					case 4:
						replacement.setItemMeta(CACHE.get(REGISTRY.get("URL_Creeper")));
						break;
					case 5:
						replacement.setItemMeta(CACHE.get(REGISTRY.get("URL_Dragon")));
						break;
					}
					
					return replacement;
				}
			}
		}
		
		return skull;
	}
	
	// base64 decoder: https://www.base64decode.org/
	
	public ItemStack createSkull()
	{
		return createSkull(this);
	}
	
	public ItemStack createSkull(int modifier)
	{
		return createSkull(this, modifier);
	}
	
	public static ItemStack createSkull(MobType type)
	{
		return createSkull(type, 0);
	}
	
	public static ItemStack createSkull(MobType type, int modifier)
	{
		return createSkull(type, modifier, true);
	}
	
	public static ItemStack createSkull(MobType type, int modifier, boolean useCache)
	{
		if (useCache)
		{
			Pair<MobType,Integer> pair = new Pair<MobType,Integer>(type,modifier); 
			if (CACHE.containsKey(pair))
			{
				ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
				item.setItemMeta(CACHE.get(pair));
				return item;
			}
		}
		
		return createSkullFromURL(type.getURL(modifier), type.getDisplayName(modifier) + " Head", type.getId(modifier), type.getLore(modifier));
	}
	
	private static ItemStack createSkullFromURL(String url, String displayName, String nameURL, List<String> lore)
	{
		ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		
		UUID uuid = UUID.nameUUIDFromBytes(url.getBytes());
		
		GameProfile profile = new GameProfile(uuid, null);
		PropertyMap propertyMap = profile.getProperties();
		
		if (propertyMap == null) return null;
		
		String timestamp = Long.toString(System.currentTimeMillis());
		
		String format = "{\"timestamp\":%1$s,\"profileId\":\"%2$s\",\"profileName\":\"%3$s\",\"signatureRequired\":true,\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/%4$s\"}}}";
		
		byte[] encodedData = base64.encode(String.format(format, timestamp, uuid.toString().replaceAll("-", ""), nameURL, url).getBytes());
		
		byte[] buffer = new byte[32767];
		for(int i = 0; i < encodedData.length; i++)
		{
			buffer[i] = encodedData[i];
		}
		PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.wrappedBuffer(buffer));
		
		propertyMap.put("textures", new Property("textures", new String(encodedData), packetdataserializer.e(32767)));
		ItemMeta headMeta = baseSkullMeta.clone();
		
		SkullMeta skullMeta = (SkullMeta)headMeta;
		
		skullMeta.setDisplayName(displayName);
		skullMeta.setLore(lore);
		
		Class<?> skullMetaClass = skullMeta.getClass();
		Reflections.getField(skullMetaClass, "profile", GameProfile.class).set(skullMeta, profile);
		
		item.setItemMeta(skullMeta);
		
		net.minecraft.server.v1_10_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = null;
		if (!nmsStack.hasTag())
		{
			tag = new NBTTagCompound();
			nmsStack.setTag(tag);
		}
		if (tag == null) tag = nmsStack.getTag();
		NBTTagCompound SkullOwner = null;
		if (!nmsStack.hasTag())
		{
			SkullOwner = new NBTTagCompound();
			nmsStack.setTag(SkullOwner);
		}
		if (SkullOwner == null) SkullOwner = tag.getCompound("SkullOwner");
		
		SkullOwner.setString("Name", nameURL);
		
		tag.set("SkullOwner", SkullOwner);
		
		nmsStack.setTag(tag);
		
		return CraftItemStack.asCraftMirror(nmsStack);
	}
}
