/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.fml.ModList;
import com.google.common.collect.Lists;
import com.google.gson.JsonParseException;

public class WurstTranslator implements ResourceManagerReloadListener
{
	private final WurstClient wurst = WurstClient.INSTANCE;
	private ClientLanguage mcEnglish;
	
	private Map<String, String> currentLangStrings = Map.of();
	private Map<String, String> englishOnlyStrings = Map.of();
	
	@Override
	public void onResourceManagerReload(ResourceManager manager)
	{
		mcEnglish = ClientLanguage.loadFrom(manager,
			Lists.newArrayList("en_us"), false);
		
		HashMap<String, String> currentLangStrings = new HashMap<>();
		loadTranslations(getCurrentLangCodes(),
			currentLangStrings::put);
		this.currentLangStrings =
			Collections.unmodifiableMap(currentLangStrings);
		
		HashMap<String, String> englishOnlyStrings = new HashMap<>();
		loadTranslations(List.of("en_us"), englishOnlyStrings::put);
		this.englishOnlyStrings =
			Collections.unmodifiableMap(englishOnlyStrings);
	}
	
	/**
	 * Translates the given key with the given args into the current language,
	 * or into English if the "Force English" setting is enabled. Both Wurst and
	 * vanilla translations are supported.
	 */
	public String translate(String key, Object... args)
	{
		// Forced English
		if(isForcedEnglish())
			return translateEnglish(key, args);
		
		// Wurst translation
		String string = currentLangStrings.get(key);
		if(string != null)
			try
			{
				return String.format(string, args);
				
			}catch(IllegalFormatException e)
			{
				return key;
			}
		
		// Vanilla translation
		return translateMc(key, args);
	}
	
	/**
	 * Translates the given key with the given args into English, regardless of
	 * the current language. Both Wurst and vanilla translations are supported.
	 */
	public String translateEnglish(String key, Object... args)
	{
		String string = englishOnlyStrings.get(key);
		if(string == null)
			string = mcEnglish.getOrDefault(key);
		
		try
		{
			return String.format(string, args);
			
		}catch(IllegalFormatException e)
		{
			return key;
		}
	}
	
	/**
	 * Translates the given key with the given args into the current language,
	 * or into English if the "Force English" setting is enabled, using only
	 * Minecraft's own translations.
	 *
	 * @apiNote This method differs from
	 *          {@link I18n#get(String, Object...)} in that it does not
	 *          return "Format error" if the key contains a percent sign.
	 */
	public String translateMc(String key, Object... args)
	{
		if(I18n.exists(key))
			return I18n.get(key, args);
		
		return key;
	}
	
	/**
	 * Translates the given key with the given args into English, regardless of
	 * the current language, using only Minecraft's own translations.
	 *
	 * @apiNote This method differs from
	 *          {@link I18n#get(String, Object...)} in that it does not
	 *          return "Format error" if the key contains a percent sign.
	 */
	public String translateMcEnglish(String key, Object... args)
	{
		try
		{
			return String.format(mcEnglish.getOrDefault(key), args);
			
		}catch(IllegalFormatException e)
		{
			return key;
		}
	}
	
	public boolean isForcedEnglish()
	{
		return wurst.getOtfs().translationsOtf.getForceEnglish().isChecked();
	}
	
	/**
	 * Returns a translation storage for Minecraft's English strings, regardless
	 * of the current language. Does not include any of Wurst's translations.
	 */
	public ClientLanguage getMcEnglish()
	{
		return mcEnglish;
	}
	
	public Map<String, String> getMinecraftsCurrentLanguage()
	{
		return currentLangStrings;
	}
	
	public Map<String, String> getWurstsCurrentLanguage()
	{
		return isForcedEnglish() ? englishOnlyStrings
			: getMinecraftsCurrentLanguage();
	}
	
	private ArrayList<String> getCurrentLangCodes()
	{
		// Weird bug: Some users have their language set to "en_US" instead of
		// "en_us" for some reason. Last seen in 1.21.
		String mainLangCode = Minecraft.getInstance().getLanguageManager()
			.getSelected().toLowerCase(Locale.ROOT);
		
		ArrayList<String> langCodes = new ArrayList<>();
		langCodes.add("en_us");
		if(!"en_us".equals(mainLangCode))
			langCodes.add(mainLangCode);
		
		return langCodes;
	}
	
	private void loadTranslations(Iterable<String> langCodes,
		BiConsumer<String, String> entryConsumer)
	{
		var modFile = ModList.get().getModFileById("wurst").getFile();
		for(String langCode : langCodes)
		{
			if(!langCode.matches("[a-z0-9_-]+"))
				continue;

			// Read the bundled file directly so server packs cannot replace it.
			Path file = modFile.findResource("assets", "wurst", "translations",
				langCode + ".json");
			try(InputStream stream = Files.newInputStream(file))
			{
				Language.loadFromJson(stream, entryConsumer);
			}catch(NoSuchFileException e)
			{
				// Languages without bundled translations fall back to English.
			}catch(IOException | JsonParseException e)
			{
				System.out.println("Failed to load Wurst translations for " + langCode);
				e.printStackTrace();
			}
		}
	}
}
