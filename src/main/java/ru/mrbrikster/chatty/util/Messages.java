package ru.mrbrikster.chatty.util;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.mrbrikster.baseplugin.config.BukkitConfiguration;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.util.Objects.requireNonNull;

public class Messages {
  private final Configuration localeConfiguration;
  private final Configuration inJarConfiguration;

  public Messages(Chatty chatty) {
    var localeDir = new File(chatty.getDataFolder(), "locale");

    var localeName = chatty.get(Configuration.class).getNode("general.locale")
      .getAsString("en");

    if (!localeDir.exists()) {
      if (!localeDir.mkdir()) {
        chatty.getLogger().warning("Cannot create \"locale\" directory");
      }
    }

    var localeFile = new File(localeDir, localeName + ".yml");
    if (!localeFile.exists()) {
      var localeFileUrl = getClass().getResource("/locale/" + localeName + ".yml");

      if (localeFileUrl == null) {
        chatty.getLogger().warning("Locale " + '"' + localeName + '"' + " not found. Using \"en\" locale.");

        var enLocaleFile = new File(localeDir, "en.yml");

        if (!enLocaleFile.exists()) {
          try {
            FileUtils.copyURLToFile(requireNonNull(getClass().getResource("/locale/en.yml")), enLocaleFile);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

        localeName = "en";
      } else {
        try {
          FileUtils.copyURLToFile(localeFileUrl, localeFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    this.localeConfiguration = chatty.getConfiguration("locale/" + localeName + ".yml");
    this.inJarConfiguration = new BukkitConfiguration(
      YamlConfiguration.loadConfiguration(
        new InputStreamReader(requireNonNull(chatty.getClass().getResourceAsStream("/locale/en.yml")))
      )
    );
  }

  public String get(String key) {
    return get(key, inJarConfiguration.getNode("messages." + key).getAsString("&cLocale message not found."));
  }

  public String get(String key, String def) {
    return TextUtil.stylish(
      localeConfiguration == null
        ? def
        : localeConfiguration.getNode("messages." + key).getAsString(def)
    );
  }
}
