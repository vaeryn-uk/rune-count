package com.runecount;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("runecount")
public interface RuneCountConfig extends Config
{
	@ConfigItem(
		keyName = "showEmpty",
		name = "Show unavailable runes",
		description = "Show zero-count infoboxes for runes you do not currently have."
	)
	default boolean showEmpty()
	{
		return false;
	}
}
