package com.runecount;

import java.awt.Color;
import java.awt.image.BufferedImage;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

final class RuneCountInfoBox extends InfoBox
{
	private String text = "0";
	private Color textColor = Color.WHITE;

	RuneCountInfoBox(BufferedImage image, Plugin plugin)
	{
		super(image, plugin);
	}

	void update(String text, String tooltip, Color textColor)
	{
		this.text = text;
		this.textColor = textColor;
		setTooltip(tooltip);
	}

	@Override
	public String getText()
	{
		return text;
	}

	@Override
	public Color getTextColor()
	{
		return textColor;
	}
}
