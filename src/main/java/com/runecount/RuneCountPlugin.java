package com.runecount;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.eventbus.Subscribe;

@PluginDescriptor(
	name = "Rune Count",
	description = "Shows runes currently available from your inventory, rune pouch, and equipped staves.",
	tags = {"runes", "rune pouch", "magic", "infobox"}
)
public class RuneCountPlugin extends Plugin
{
	/** Maps the rune-pouch slot's compact rune index to its item ID. */
	private static final int RUNE_POUCH_RUNE_ENUM = 982;
	private static final int[] POUCH_RUNE_VARBITS = {
		Varbits.RUNE_POUCH_RUNE1, Varbits.RUNE_POUCH_RUNE2, Varbits.RUNE_POUCH_RUNE3,
		Varbits.RUNE_POUCH_RUNE4, Varbits.RUNE_POUCH_RUNE5, Varbits.RUNE_POUCH_RUNE6
	};
	private static final int[] POUCH_AMOUNT_VARBITS = {
		Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3,
		Varbits.RUNE_POUCH_AMOUNT4, Varbits.RUNE_POUCH_AMOUNT5, Varbits.RUNE_POUCH_AMOUNT6
	};

	@Inject private Client client;
	@Inject private ItemManager itemManager;
	@Inject private InfoBoxManager infoBoxManager;
	@Inject private RuneCountConfig config;
	@Inject private ClientThread clientThread;

	private final Map<RuneType, RuneCountInfoBox> infoBoxes = new EnumMap<>(RuneType.class);

	@Provides
	RuneCountConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RuneCountConfig.class);
	}

	@Override
	protected void startUp()
	{
		requestRefresh();
	}

	@Override
	protected void shutDown()
	{
		clientThread.invokeLater(this::removeInfoBoxes);
	}

	private void removeInfoBoxes()
	{
		infoBoxes.values().forEach(infoBoxManager::removeInfoBox);
		infoBoxes.clear();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		refresh();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.INVENTORY.getId()
			|| event.getContainerId() == InventoryID.EQUIPMENT.getId())
		{
			refresh();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		refresh();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		requestRefresh();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if ("runecount".equals(event.getGroup()) && "showEmpty".equals(event.getKey()))
		{
			requestRefresh();
		}
	}

	private void requestRefresh()
	{
		clientThread.invokeLater(this::refresh);
	}

	private void refresh()
	{
		Map<RuneType, Integer> quantities = getRuneQuantities();
		EnumSet<RuneType> infiniteRunes = getInfiniteRunes();

		for (RuneType rune : RuneType.values())
		{
			int quantity = quantities.get(rune);
			boolean visible = config.showEmpty() || quantity > 0 || infiniteRunes.contains(rune);
			RuneCountInfoBox infoBox = infoBoxes.get(rune);
			if (!visible)
			{
				if (infoBox != null)
				{
					infoBoxManager.removeInfoBox(infoBox);
					infoBoxes.remove(rune);
				}
				continue;
			}

			if (infoBox == null)
			{
				BufferedImage image = itemManager.getImage(rune.getItemId());
				infoBox = new RuneCountInfoBox(image, this);
				infoBoxes.put(rune, infoBox);
				infoBoxManager.addInfoBox(infoBox);
			}

			String amount = infiniteRunes.contains(rune) ? "∞" : Integer.toString(quantity);
			infoBox.update(amount, rune.getDisplayName() + " rune: " + amount);
		}
	}

	private Map<RuneType, Integer> getRuneQuantities()
	{
		Map<RuneType, Integer> quantities = new EnumMap<>(RuneType.class);
		for (RuneType rune : RuneType.values())
		{
			quantities.put(rune, 0);
		}
		addRunes(quantities, client.getItemContainer(InventoryID.INVENTORY));

		EnumComposition runePouchRunes = client.getEnum(RUNE_POUCH_RUNE_ENUM);
		for (int slot = 0; slot < POUCH_RUNE_VARBITS.length; slot++)
		{
			int runeIndex = client.getVarbitValue(POUCH_RUNE_VARBITS[slot]);
			int runeItemId = runeIndex == 0 || runePouchRunes == null ? -1 : runePouchRunes.getIntValue(runeIndex);
			int amount = client.getVarbitValue(POUCH_AMOUNT_VARBITS[slot]);
			for (RuneType rune : RuneType.values())
			{
				if (rune.getItemId() == runeItemId)
				{
					quantities.put(rune, quantities.get(rune) + amount);
					break;
				}
			}
		}
		return quantities;
	}

	private void addRunes(Map<RuneType, Integer> quantities, ItemContainer container)
	{
		if (container == null)
		{
			return;
		}
		for (Item item : container.getItems())
		{
			for (RuneType rune : RuneType.values())
			{
				if (item.getId() == rune.getItemId())
				{
					quantities.put(rune, quantities.get(rune) + item.getQuantity());
					break;
				}
			}
		}
	}

	private EnumSet<RuneType> getInfiniteRunes()
	{
		EnumSet<RuneType> supplied = EnumSet.noneOf(RuneType.class);
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)
		{
			return supplied;
		}
		Item[] equipmentItems = equipment.getItems();
		addTomeRunes(supplied, equipmentItems);
		if (equipmentItems.length <= 3)
		{
			return supplied;
		}

		Item weapon = equipmentItems[3];
		ItemComposition composition = itemManager.getItemComposition(weapon.getId());
		if (composition == null)
		{
			return supplied;
		}

		String name = composition.getName().toLowerCase(java.util.Locale.ENGLISH);
		if (name.contains("staff of the dead"))
		{
			supplied.add(RuneType.FIRE);
		}
		if (name.contains("bryophyta's staff"))
		{
			supplied.add(RuneType.NATURE);
		}
		if (name.contains("staff") || name.contains("battlestaff"))
		{
			addStaffRunes(supplied, name);
		}
		return supplied;
	}

	private static void addTomeRunes(EnumSet<RuneType> supplied, Item[] equipment)
	{
		// The shield slot is index 5. Empty tomes have separate item IDs and
		// deliberately do not provide unlimited runes.
		if (equipment.length <= 5)
		{
			return;
		}
		int offhandId = equipment[5].getId();
		if (offhandId == ItemID.TOME_OF_FIRE || offhandId == ItemID.TOME_OF_FIRE_27358)
		{
			supplied.add(RuneType.FIRE);
		}
		if (offhandId == ItemID.TOME_OF_WATER)
		{
			supplied.add(RuneType.WATER);
		}
	}

	private static void addStaffRunes(EnumSet<RuneType> supplied, String name)
	{
		if (name.contains("steam")) { supplied.add(RuneType.WATER); supplied.add(RuneType.FIRE); }
		if (name.contains("mist")) { supplied.add(RuneType.AIR); supplied.add(RuneType.WATER); }
		if (name.contains("dust")) { supplied.add(RuneType.AIR); supplied.add(RuneType.EARTH); }
		if (name.contains("smoke")) { supplied.add(RuneType.AIR); supplied.add(RuneType.FIRE); }
		if (name.contains("mud")) { supplied.add(RuneType.WATER); supplied.add(RuneType.EARTH); }
		if (name.contains("lava")) { supplied.add(RuneType.EARTH); supplied.add(RuneType.FIRE); }
		if (name.contains("air")) { supplied.add(RuneType.AIR); }
		if (name.contains("water")) { supplied.add(RuneType.WATER); }
		if (name.contains("earth")) { supplied.add(RuneType.EARTH); }
		if (name.contains("fire")) { supplied.add(RuneType.FIRE); }
	}
}
