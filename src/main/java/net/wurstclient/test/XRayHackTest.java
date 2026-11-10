/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.test;

import static net.wurstclient.test.WurstClientTestHelper.*;

import java.time.Duration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.screens.EditBlockListScreen;
import net.wurstclient.hacks.XRayHack;
import net.wurstclient.util.BlockUtils;

public enum XRayHackTest
{
	;
	
	public static void testXRayHack()
	{
		System.out.println("Testing X-Ray hack");
		buildTestRig();
		clearChat();
		
		// Enable X-Ray with default settings
		runWurstCommand("setcheckbox X-Ray only_show_exposed off");
		runWurstCommand("setslider X-Ray opacity 0");
		runWurstCommand("t X-Ray on");
		takeScreenshot("xray_default", Duration.ofMillis(300));
		testLiveBlockSelection();
		runWurstCommand("t X-Ray off");
		clearChat();
		
		// Exposed only
		runWurstCommand("setcheckbox X-Ray only_show_exposed on");
		runWurstCommand("setslider X-Ray opacity 0");
		runWurstCommand("t X-Ray on");
		takeScreenshot("xray_exposed_only", Duration.ofMillis(300));
		runWurstCommand("t X-Ray off");
		clearChat();
		
		// Opacity mode
		runWurstCommand("setcheckbox X-Ray only_show_exposed off");
		runWurstCommand("setslider X-Ray opacity 0.5");
		runWurstCommand("t X-Ray on");
		takeScreenshot("xray_opacity", Duration.ofMillis(300));
		runWurstCommand("t X-Ray off");
		clearChat();
		
		// Exposed only + opacity
		runWurstCommand("setcheckbox X-Ray only_show_exposed on");
		runWurstCommand("setslider X-Ray opacity 0.5");
		runWurstCommand("t X-Ray on");
		takeScreenshot("xray_exposed_only_opacity", Duration.ofMillis(300));
		runWurstCommand("t X-Ray off");
		clearChat();
		
		// Clean up
		runChatCommand("fill ~-7 ~ ~-7 ~7 ~30 ~7 air");
		runWurstCommand("setcheckbox X-Ray only_show_exposed off");
		runWurstCommand("setslider X-Ray opacity 0");
		runWurstCommand("t X-Ray off");
		clearChat();

		testFullbrightInDarkness();
	}

	private static void testFullbrightInDarkness()
	{
		runChatCommand("fill ~-3 ~-1 ~-3 ~3 ~5 ~8 stone hollow");
		runChatCommand("setblock ~ ~2 ~5 diamond_ore");
		BlockPos ore = submitAndGet(mc -> mc.player.blockPosition().offset(0, 2, 5));
		waitUntil("the buried ore has no sky or block light", mc ->
			mc.level.getBlockState(ore).is(net.minecraft.world.level.block.Blocks.DIAMOND_ORE)
				&& mc.level.getBrightness(LightLayer.SKY, ore) == 0
				&& mc.level.getBrightness(LightLayer.BLOCK, ore) == 0);

		runWurstCommand("t X-Ray on");
		takeScreenshot("xray_dark_cave", Duration.ofMillis(600));
		runWurstCommand("t Fullbright on");
		takeScreenshot("xray_fullbright_cave", Duration.ofMillis(900));
		runWurstCommand("t Fullbright off");
		runWurstCommand("t X-Ray off");
		runChatCommand("fill ~-3 ~ ~-3 ~3 ~5 ~8 air");
		clearChat();
	}

	private static void testLiveBlockSelection()
	{
		XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
		String moddedOre = "allthemodium:allthemodium_ore";
		String blockId = BlockUtils.getBlockFromNameOrID(moddedOre) != null
			? moddedOre : "minecraft:amethyst_block";
		Block block = BlockUtils.getBlockFromName(blockId);
		if(submitAndGet(mc -> xray.isVisible(block, null)))
			runWurstCommand("blocklist X-Ray ores remove " + blockId);
		waitUntil("custom block is excluded", mc -> !xray.isVisible(block, null));
		runChatCommand("setblock ~0 ~2 ~6 " + blockId);
		submitAndWait(mc -> xray.openBlockListEditor(null));
		waitForScreen(EditBlockListScreen.class);
		clickButton("Browse Blocks");
		setTextFieldText(0, blockId.substring(blockId.indexOf(':') + 1));
		takeScreenshot("xray_block_picker", Duration.ZERO);
		submitAndWait(mc -> mc.screen.mouseClicked(mc.screen.width / 2, 45, 0));
		clickButton("Add");
		clickButton("Done");
		waitUntil("custom block is visible with X-Ray still enabled",
			mc -> xray.isVisible(block, null));
		takeScreenshot("xray_custom_block", Duration.ofMillis(600));
		runWurstCommand("blocklist X-Ray ores remove " + blockId);
		waitUntil("custom block is hidden again", mc -> !xray.isVisible(block, null));
	}
	
	private static void buildTestRig()
	{
		// Stone wall (9 wide, 5 high, 3 deep)
		runChatCommand("fill ~-5 ~ ~5 ~5 ~5 ~7 stone");
		
		// Ores (1 exposed and 1 hidden each)
		runChatCommand("fill ~-4 ~1 ~5 ~-4 ~1 ~6 minecraft:coal_ore");
		runChatCommand("fill ~-2 ~1 ~5 ~-2 ~1 ~6 minecraft:iron_ore");
		runChatCommand("fill ~0 ~1 ~5 ~0 ~1 ~6 minecraft:gold_ore");
		runChatCommand("fill ~2 ~1 ~5 ~2 ~1 ~6 minecraft:diamond_ore");
		runChatCommand("fill ~4 ~1 ~5 ~4 ~1 ~6 minecraft:emerald_ore");
		runChatCommand("fill ~-4 ~3 ~5 ~-4 ~3 ~6 minecraft:lapis_ore");
		runChatCommand("fill ~-2 ~3 ~5 ~-2 ~3 ~6 minecraft:redstone_ore");
		runChatCommand("fill ~0 ~3 ~5 ~0 ~3 ~6 minecraft:copper_ore");
		runChatCommand("fill ~2 ~3 ~5 ~2 ~3 ~6 minecraft:nether_gold_ore");
		runChatCommand("fill ~4 ~3 ~5 ~4 ~3 ~6 minecraft:nether_quartz_ore");
		
		// Fluids
		runChatCommand("setblock ~1 ~0 ~6 minecraft:water");
		runChatCommand("setblock ~-1 ~0 ~6 minecraft:lava");
	}
}
