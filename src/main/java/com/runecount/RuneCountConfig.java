package com.runecount;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("runecount")
public interface RuneCountConfig extends Config
{
	@ConfigItem(
		keyName = "lowRuneDisplayThreshold",
		name = "Low-rune display threshold",
		description = "Only show rune infoboxes with this amount or fewer available. Set to 0 to disable this filter."
	)
	@Range(min = 0)
	default int lowRuneDisplayThreshold()
	{
		return 0;
	}
}
