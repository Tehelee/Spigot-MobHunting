package com.tehelee.mobHunting;

import org.bukkit.Location;

public class GrindInfo {
	public Location lastLocation;
	public MobType mobType;
	public int killCount;
	public boolean hasBeenWarned;
	public long lastKillTime;

	public GrindInfo(Location loc, MobType type) {
		this.lastLocation = loc;
		this.mobType = type;
		this.killCount = 0;
		this.hasBeenWarned = false;
		this.lastKillTime = System.currentTimeMillis();
	}

	public int hashCode()
	{
		int hasLoc = lastLocation != null ? lastLocation.hashCode() : 0;
		int hashType = mobType != null ? mobType.hashCode() : 0;

		return (hasLoc + hashType) * hasLoc + hashType;
	}

	public boolean equals(Object other)
	{
		if (other instanceof GrindInfo)
		{
			GrindInfo otherInfo = (GrindInfo) other;
			return 
			((  this.lastLocation == otherInfo.lastLocation ||
				( this.lastLocation != null && otherInfo.lastLocation != null &&
				  this.lastLocation.equals(otherInfo.lastLocation))) &&
			 (	this.mobType == otherInfo.mobType ||
				( this.mobType != null && otherInfo.mobType != null &&
				  this.mobType.equals(otherInfo.mobType))) );
		}

		return false;
	}

	public String toString()
	{ 
		   return "(" + this.lastLocation + ", " + this.mobType + ", " + this.killCount + ")"; 
	}
}
