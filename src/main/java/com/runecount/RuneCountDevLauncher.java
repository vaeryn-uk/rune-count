package com.runecount;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/** Launches RuneLite with Rune Count registered as a built-in development plugin. */
public class RuneCountDevLauncher
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RuneCountPlugin.class);
		RuneLite.main(args);
	}
}
