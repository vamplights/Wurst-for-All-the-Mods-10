/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.wurstclient.test.WurstE2ETestClient;

@Mod(value = "wurst", dist = Dist.CLIENT)
public final class WurstInitializer
{
	private static boolean initialized;
	
	public WurstInitializer()
	{
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		
		if(initialized)
			throw new RuntimeException(
				"WurstInitializer constructor ran twice!");
		
		WurstClient.INSTANCE.initialize();
		initialized = true;
		new WurstE2ETestClient().startIfEnabled();
	}
}
