# Wurst Client for All the Mods 🧇✨

Wurst 7.51.2, brought to **Minecraft 1.21.1 + NeoForge**. Keep your favorite modpack, add Wurst, and get back to the fun. 🧰⛏️

This is a NeoForge port of Wurst Client. It carries over Wurst's feature and command registries, settings, keybinds, translations, and assets, with an X-Ray block picker that can search blocks by name or mod ID. Made for modded Minecraft setups; compatibility can vary by pack and by the mods you have installed. 🌍

## Get started 🚀

You'll need **Minecraft 1.21.1**, **NeoForge 21.1.185**, and **Java 21**.

1. Build the project with `gradlew.bat build` on Windows, or `./gradlew build` on macOS/Linux.
2. Grab `Wurst-Client-NeoForge-7.51.2-MC1.21.1.jar` from `build/libs`.
3. Drop it into your Minecraft profile's `mods` folder and launch with NeoForge.
4. In game, **Right Ctrl** opens ClickGUI; **Right Shift** opens Navigator. 🎮

## X-Ray block picker 🔎

Open **X-Ray → Ores → Browse Blocks**, search by block name or mod ID, choose a result, and hit **Add**. You can also enter a full ID directly, for example `allthemodium:allthemodium_ore`. Changes take effect while X-Ray is running. With Embeddium, X-Ray fully hides blocks outside your list.

## Build & run 🛠️

Use a Java 21 JDK on your path or set `JAVA_HOME`, then run these from the project folder:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runClient -PwurstE2E
```

The last command runs the inherited end-to-end suite in `run/e2e`. The normal development client uses `run`. On macOS/Linux, use `./gradlew`.

The upstream update check is disabled because its download feed targets Fabric. 🧩

## Credits & license 📜

Wurst Client is the work of its original authors and contributors. This port is based on Wurst 7.51.2 and is distributed under the GNU General Public License; see [LICENSE.txt](LICENSE.txt). The [E3418 NeoForge fork](https://github.com/E3418/Hack-Client-for-NeoForge-1.21.1) was used as a reference for the target loader.
