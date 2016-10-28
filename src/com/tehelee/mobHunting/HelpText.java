package com.tehelee.mobHunting;

public class HelpText
{
	public static String PluginName = "§2[§aMob Hunt§2]§r: ";
	public static String PvPname = "§4[§cPvP§4]§r: ";
	
	public static String logStart = "Initialized";
	public static String logStop = "Shut Down";
	public static String logRestart = "Reloaded";
	
	public static String RewardMob = "%1$s§r recieved §2$%2$s§r for killing%3$s %4$s%5$s§r";
	public static String CashDropMob = "%1$s§r killed%2$s %3$s%4$s§r that dropped §2$%5$s§r";
	public static String NamedMob = "%1$s§r defeated%2$s %3$s";
	
	// killerName, victimName, weaponName, (claimed §2 / took §c), reward, combo, (has / have), bounty
	public static String PvPreward = PvPname + "%1$s§r killed %2$s§r%3$s§r and $%4$s$%5$s§r%6$s§r\n" + PvPname + "%1$s§r now %7$s§r a bounty of §2$%8$s§r";
	// killerName, victimName, weaponName, (has / have), bounty
	public static String PvPinform = PvPname + "%1$s§r killed %2$s§r%3$s§r\n" + PvPname + "%1$s§r now %4$s§r a bounty of §2$%5$s§r";
	
	public static String DeathPvE = "%1$s§r %2$s§r slain by%3$s§r %4$s§r";
	
	public static String PickupCash = "%1$s§r picked up §2$%2$s§r";
	public static String ComboReset = "%1$s%2$s has broken your combo!";
	
	public static String CashPrefix = "§a§l$";

	public static String MissingPermission = "§cYou don't have permission to use this command.";
	
	public static String PermissionPrefix = "permissions.mobHunting.";
	
	public static String MissingEconomy = "Failed! Missing Vault Economy";
}
