package com.runecount;

import java.util.Locale;
import net.runelite.api.ItemID;

enum RuneType
{
	AIR("Air", ItemID.AIR_RUNE),
	WATER("Water", ItemID.WATER_RUNE),
	EARTH("Earth", ItemID.EARTH_RUNE),
	FIRE("Fire", ItemID.FIRE_RUNE),
	MIND("Mind", ItemID.MIND_RUNE),
	BODY("Body", ItemID.BODY_RUNE),
	COSMIC("Cosmic", ItemID.COSMIC_RUNE),
	CHAOS("Chaos", ItemID.CHAOS_RUNE),
	ASTRAL("Astral", ItemID.ASTRAL_RUNE),
	NATURE("Nature", ItemID.NATURE_RUNE),
	LAW("Law", ItemID.LAW_RUNE),
	DEATH("Death", ItemID.DEATH_RUNE),
	BLOOD("Blood", ItemID.BLOOD_RUNE),
	SOUL("Soul", ItemID.SOUL_RUNE),
	WRATH("Wrath", ItemID.WRATH_RUNE),
	SUNFIRE("Sunfire", ItemID.SUNFIRE_RUNE),
	AETHER("Aether", ItemID.AETHER_RUNE),
	MIST("Mist", ItemID.MIST_RUNE),
	DUST("Dust", ItemID.DUST_RUNE),
	MUD("Mud", ItemID.MUD_RUNE),
	SMOKE("Smoke", ItemID.SMOKE_RUNE),
	STEAM("Steam", ItemID.STEAM_RUNE),
	LAVA("Lava", ItemID.LAVA_RUNE);

	private final String displayName;
	private final int itemId;

	RuneType(String displayName, int itemId)
	{
		this.displayName = displayName;
		this.itemId = itemId;
	}

	String getDisplayName()
	{
		return displayName;
	}

	int getItemId()
	{
		return itemId;
	}

	boolean isNamedIn(String itemName)
	{
		String normalized = itemName.toLowerCase(Locale.ENGLISH);
		return normalized.contains(displayName.toLowerCase(Locale.ENGLISH));
	}
}
