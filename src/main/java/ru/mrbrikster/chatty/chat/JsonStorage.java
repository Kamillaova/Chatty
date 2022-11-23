package ru.mrbrikster.chatty.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class JsonStorage {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  @SuppressWarnings("deprecation")
  private static final JsonParser JsonParser = new JsonParser();

  private final File storageFile;
  private final Configuration configuration;

  public JsonStorage(Chatty chatty) {
    this.configuration = chatty.get(Configuration.class);
    this.storageFile = new File(chatty.getDataFolder(), "storage.json");

    if (!storageFile.exists()) {
      try {
        if (!storageFile.createNewFile()) {
          throw new IOException("Cannot create storage.json");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @SuppressWarnings("deprecation")
  private void setProperty(String player, String property, JsonElement value) {
    JsonElement jsonObject = null;
    try {
      jsonObject = JsonParser.parse(read());
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (jsonObject == null ||
      !jsonObject.isJsonObject()
    ) {
      jsonObject = new JsonObject();
    } else {
      jsonObject = jsonObject.getAsJsonObject();
    }

    JsonElement propertyElement;
    if (((JsonObject) jsonObject).has(property)) {
      propertyElement = ((JsonObject) jsonObject).remove(property);

      if (propertyElement.isJsonObject()) {
        propertyElement = propertyElement.getAsJsonObject();
      } else {
        propertyElement = new JsonObject();
      }
    } else {
      propertyElement = new JsonObject();
    }

    if (((JsonObject) propertyElement).has(player)) { ((JsonObject) propertyElement).remove(player); }

    if (value != null) { ((JsonObject) propertyElement).add(player, value); }

    ((JsonObject) jsonObject).add(property, propertyElement);

    try {
      write(GSON.toJson(jsonObject));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setProperty(Player player, String property, JsonElement value) {
    if (configuration.getNode("general.uuid").getAsBoolean(false)) {
      setProperty(player.getUniqueId().toString(), property, value);
    } else {
      setProperty(player.getName(), property, value);
    }
  }

  @SuppressWarnings("deprecation")
  private Optional<JsonElement> getProperty(String player, String property) {
    try {
      var jsonObject = JsonParser.parse(read());

      if (!jsonObject.isJsonObject()) {
        return Optional.empty();
      }

      var propertyElement = ((JsonObject) jsonObject).get(property);

      if (propertyElement == null) {
        return Optional.empty();
      }

      if (propertyElement.isJsonObject()) {
        var playerPropertyElement = propertyElement.getAsJsonObject().get(player);

        return Optional.ofNullable(playerPropertyElement);
      }

      return Optional.empty();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

  public Optional<JsonElement> getProperty(Player player, String property) {
    if (configuration.getNode("general.uuid").getAsBoolean(false)) {
      return getProperty(player.getUniqueId().toString(), property);
    } else {
      return getProperty(player.getName(), property);
    }
  }

  public boolean isIgnore(CommandSender recipient, CommandSender sender) {
    if (sender != null) {
      var jsonElement = Chatty.instance().get(JsonStorage.class)
        .getProperty((Player) recipient, "ignore")
        .orElseGet(JsonArray::new);

      if (jsonElement.isJsonArray()) {
        for (var ignoreJsonElement : jsonElement.getAsJsonArray()) {
          if (sender.getName().equalsIgnoreCase(ignoreJsonElement.getAsString())) {
            return true;
          }
        }
      }

      return false;
    }

    return false;
  }

  private String read() throws IOException {
    var reader = new BufferedReader(new FileReader(storageFile));
    var stringBuilder = new StringBuilder();

    String line;
    while ((line = reader.readLine()) != null) {
      stringBuilder.append(line);
    }

    reader.close();

    return stringBuilder.toString();
  }

  private void write(String json) throws IOException {
    var writer = new BufferedWriter(new FileWriter(storageFile));

    writer.write(json);
    writer.flush();
    writer.close();
  }
}