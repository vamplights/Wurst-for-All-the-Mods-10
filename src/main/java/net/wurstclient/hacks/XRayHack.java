/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.clickgui.screens.EditBlockListScreen;
import net.wurstclient.events.GetAmbientOcclusionLightLevelListener;
import net.wurstclient.events.RenderBlockEntityListener;
import net.wurstclient.events.SetOpaqueCubeListener;
import net.wurstclient.events.ShouldDrawSideListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.ISimpleOption;
import net.wurstclient.settings.BlockListSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.ChatUtils;

@SearchTags({"XRay", "x ray", "OreFinder", "ore finder"})
public final class XRayHack extends Hack implements UpdateListener,
	SetOpaqueCubeListener, GetAmbientOcclusionLightLevelListener,
	ShouldDrawSideListener, RenderBlockEntityListener
{
	private final BlockListSetting ores = new BlockListSetting("Ores",
		"Blocks that X-Ray will show. Browse installed blocks or enter a block ID such as modid:block_name.",
		"minecraft:amethyst_cluster", "minecraft:ancient_debris",
		"minecraft:anvil", "minecraft:beacon", "minecraft:bone_block",
		"minecraft:bookshelf", "minecraft:brewing_stand",
		"minecraft:budding_amethyst", "minecraft:chain_command_block",
		"minecraft:chest", "minecraft:coal_block", "minecraft:coal_ore",
		"minecraft:command_block", "minecraft:copper_ore", "minecraft:crafter",
		"minecraft:crafting_table", "minecraft:decorated_pot",
		"minecraft:deepslate_coal_ore", "minecraft:deepslate_copper_ore",
		"minecraft:deepslate_diamond_ore", "minecraft:deepslate_emerald_ore",
		"minecraft:deepslate_gold_ore", "minecraft:deepslate_iron_ore",
		"minecraft:deepslate_lapis_ore", "minecraft:deepslate_redstone_ore",
		"minecraft:diamond_block", "minecraft:diamond_ore",
		"minecraft:dispenser", "minecraft:dropper", "minecraft:emerald_block",
		"minecraft:emerald_ore", "minecraft:enchanting_table",
		"minecraft:end_portal", "minecraft:end_portal_frame",
		"minecraft:ender_chest", "minecraft:furnace", "minecraft:glowstone",
		"minecraft:gold_block", "minecraft:gold_ore", "minecraft:hopper",
		"minecraft:iron_block", "minecraft:iron_ore", "minecraft:ladder",
		"minecraft:lapis_block", "minecraft:lapis_ore", "minecraft:lava",
		"minecraft:lodestone", "minecraft:mossy_cobblestone",
		"minecraft:nether_gold_ore", "minecraft:nether_portal",
		"minecraft:nether_quartz_ore", "minecraft:raw_copper_block",
		"minecraft:raw_gold_block", "minecraft:raw_iron_block",
		"minecraft:redstone_block", "minecraft:redstone_ore",
		"minecraft:repeating_command_block", "minecraft:sculk_catalyst",
		"minecraft:sculk_sensor", "minecraft:sculk_shrieker",
		"minecraft:spawner", "minecraft:suspicious_gravel",
		"minecraft:suspicious_sand", "minecraft:tnt", "minecraft:torch",
		"minecraft:trapped_chest", "minecraft:trial_spawner", "minecraft:vault",
		"minecraft:wall_torch", "minecraft:water");
	
	private final CheckboxSetting onlyExposed = new CheckboxSetting(
		"Only show exposed",
		"Only shows ores that would be visible in caves. This can help against"
			+ " anti-X-Ray plugins.",
		false);
	
	private final SliderSetting opacity = new SliderSetting("Opacity",
		"Opacity of hidden blocks. Embeddium uses full hiding regardless of this setting.",
		0, 0, 0.99, 0.01, ValueDisplay.PERCENTAGE.withLabel(0, "off"));
	
	private final String optiFineWarning;
	private final boolean embeddium = ModList.get().isLoaded("embeddium");
	private final String renderName =
		Math.random() < 0.01 ? "X-Wurst" : getName();
	
	private volatile RenderSettings renderSettings =
		new RenderSettings(List.of(), false, 0);
	private final ThreadLocal<BlockPos.MutableBlockPos> mutablePosForExposedCheck =
		ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
	
	public XRayHack()
	{
		super("X-Ray");
		setCategory(Category.RENDER);
		addSetting(ores);
		addSetting(onlyExposed);
		addSetting(opacity);
		optiFineWarning = checkOptiFine();
	}
	
	@Override
	public String getRenderName()
	{
		return renderName;
	}
	
	@Override
	protected void onEnable()
	{
		renderSettings = readRenderSettings();
		
		// add event listeners
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(SetOpaqueCubeListener.class, this);
		EVENTS.add(GetAmbientOcclusionLightLevelListener.class, this);
		EVENTS.add(ShouldDrawSideListener.class, this);
		EVENTS.add(RenderBlockEntityListener.class, this);
		
		// reload chunks
		MC.levelRenderer.allChanged();
		
		// display warning if OptiFine is detected
		if(optiFineWarning != null)
			ChatUtils.warning(optiFineWarning);

		if(embeddium && opacity.getValue() > 0)
			ChatUtils.warning("X-Ray uses full hiding with Embeddium.");
	}
	
	@Override
	protected void onDisable()
	{
		// remove event listeners
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(SetOpaqueCubeListener.class, this);
		EVENTS.remove(GetAmbientOcclusionLightLevelListener.class, this);
		EVENTS.remove(ShouldDrawSideListener.class, this);
		EVENTS.remove(RenderBlockEntityListener.class, this);
		
		// reload chunks
		MC.levelRenderer.allChanged();
		
		// reset gamma
		FullbrightHack fullbright = WURST.getHax().fullbrightHack;
		if(!fullbright.isChangingGamma())
			ISimpleOption.get(MC.options.gamma())
				.forceSetValue(fullbright.getDefaultGamma());
	}
	
	@Override
	public void onUpdate()
	{
		float renderOpacity = embeddium ? 0 : opacity.getValueF();
		RenderSettings previous = renderSettings;
		if(previous.opacity() != renderOpacity
			|| previous.onlyExposed() != onlyExposed.isChecked()
			|| !previous.blocks().equals(ores.getBlockNames()))
		{
			renderSettings = readRenderSettings();
			MC.levelRenderer.allChanged();
		}

		// force gamma to 16 so that ores are bright enough to see
		ISimpleOption.get(MC.options.gamma()).forceSetValue(16.0);
	}

	private RenderSettings readRenderSettings()
	{
		return new RenderSettings(List.copyOf(ores.getBlockNames()),
			onlyExposed.isChecked(), embeddium ? 0 : opacity.getValueF());
	}
	
	@Override
	public void onSetOpaqueCube(SetOpaqueCubeEvent event)
	{
		event.cancel();
	}
	
	@Override
	public void onGetAmbientOcclusionLightLevel(
		GetAmbientOcclusionLightLevelEvent event)
	{
		event.setLightLevel(1);
	}
	
	@Override
	public void onShouldDrawSide(ShouldDrawSideEvent event)
	{
		boolean visible =
			isVisible(event.getState().getBlock(), event.getPos());
		if(!visible && isOpacityMode())
			return;
		
		event.setRendered(visible);
	}
	
	@Override
	public void onRenderBlockEntity(RenderBlockEntityEvent event)
	{
		BlockPos pos = event.getBlockEntity().getBlockPos();
		if(!isVisible(BlockUtils.getBlock(pos), pos))
			event.cancel();
	}
	
	public boolean isVisible(Block block, BlockPos pos)
	{
		String name = BlockUtils.getName(block);
		RenderSettings settings = renderSettings;
		int index = Collections.binarySearch(settings.blocks(), name);
		boolean visible = index >= 0;
		
		if(visible && settings.onlyExposed() && pos != null)
			return isExposed(pos);
		
		return visible;
	}
	
	private boolean isExposed(BlockPos pos)
	{
		BlockPos.MutableBlockPos mutablePos = mutablePosForExposedCheck.get();
		for(Direction direction : Direction.values())
			if(!BlockUtils
				.isOpaqueFullCube(mutablePos.setWithOffset(pos, direction)))
				return true;
			
		return false;
	}
	
	public boolean isOpacityMode()
	{
		return isEnabled() && WURST.isEnabled() && renderSettings.opacity() > 0;
	}

	public boolean isRenderingFullbright()
	{
		return WURST.isEnabled() && isEnabled()
			&& WURST.getHax().fullbrightHack.isEnabled();
	}
	
	public int getOpacityColorMask()
	{
		return (int)(getOpacityFloat() * 255) << 24 | 0xFFFFFF;
	}
	
	public float getOpacityFloat()
	{
		return renderSettings.opacity();
	}
	
	/**
	 * Checks if OptiFine/OptiFabric is installed and returns a warning message
	 * if it is.
	 */
	private String checkOptiFine()
	{
		Stream<String> mods = ModList.get().getMods().stream()
			.map(IModInfo::getModId);
		
		Pattern optifine = Pattern.compile("opti(?:fine|fabric).*");
		
		if(mods.anyMatch(optifine.asPredicate()))
			return "OptiFine is installed. X-Ray will not work properly!";
		
		return null;
	}
	
	public void openBlockListEditor(Screen prevScreen)
	{
		MC.setScreen(new EditBlockListScreen(prevScreen, ores));
	}

	private record RenderSettings(List<String> blocks, boolean onlyExposed,
		float opacity)
	{
	}
}
