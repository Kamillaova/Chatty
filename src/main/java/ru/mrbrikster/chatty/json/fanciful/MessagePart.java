package ru.mrbrikster.chatty.json.fanciful;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.stream.JsonWriter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import ru.mrbrikster.chatty.util.TextUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static net.md_5.bungee.api.ChatColor.MAGIC;

/**
 * Internal class: Represents a component of a JSON-serializable {@link FancyMessage}.
 */
@SuppressWarnings("deprecation")
public final class MessagePart implements JsonRepresentedObject, ConfigurationSerializable, Cloneable {
  static final BiMap<ChatColor, String> stylesToNames;

  static {
    ImmutableBiMap.Builder<ChatColor, String> builder = ImmutableBiMap.builder();
    for (var style : ChatColor.values()) {
      if (TextUtil.isColor(style)) {
        continue;
      }

      String styleName;
      if (MAGIC.equals(style)) {
        styleName = "obfuscated";
      } else if (ChatColor.UNDERLINE.equals(style)) {
        styleName = "underlined";
      } else {
        styleName = style.name().toLowerCase();
      }

      builder.put(style, styleName);
    }
    stylesToNames = builder.build();
  }

  static {
    ConfigurationSerialization.registerClass(MessagePart.class);
  }

  public TextualComponent text;
  ChatColor color = ChatColor.WHITE;
  ArrayList<ChatColor> styles = new ArrayList<>();
  String clickActionName = null, clickActionData = null, hoverActionName = null;
  JsonRepresentedObject hoverActionData = null;
  String insertionData = null;
  ArrayList<JsonRepresentedObject> translationReplacements = new ArrayList<JsonRepresentedObject>();

  MessagePart(TextualComponent text) {
    this.text = text;
  }

  MessagePart() {
    this.text = null;
  }

  @SuppressWarnings("unchecked")
  public static MessagePart deserialize(Map<String, Object> serialized) {
    var part = new MessagePart((TextualComponent) serialized.get("text"));
    part.styles = (ArrayList<ChatColor>) serialized.get("styles");
    part.color = ChatColor.valueOf(serialized.get("color").toString());
    part.hoverActionName = (String) serialized.get("hoverActionName");
    part.hoverActionData = (JsonRepresentedObject) serialized.get("hoverActionData");
    part.clickActionName = (String) serialized.get("clickActionName");
    part.clickActionData = (String) serialized.get("clickActionData");
    part.insertionData = (String) serialized.get("insertion");
    part.translationReplacements = (ArrayList<JsonRepresentedObject>) serialized.get("translationReplacements");
    return part;
  }

  boolean hasText() {
    return text != null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public MessagePart clone() throws CloneNotSupportedException {
    var obj = (MessagePart) super.clone();
    obj.styles = (ArrayList<ChatColor>) styles.clone();
    if (hoverActionData instanceof JsonString) {
      obj.hoverActionData = new JsonString(((JsonString) hoverActionData).getValue());
    } else if (hoverActionData instanceof FancyMessage) {
      obj.hoverActionData = ((FancyMessage) hoverActionData).clone();
    }
    obj.translationReplacements = (ArrayList<JsonRepresentedObject>) translationReplacements.clone();
    return obj;

  }

  public void writeJson(JsonWriter json) {
    try {
      json.beginObject();
      text.writeJson(json);
      json.name("color").value(color.getName());
      for (var style : styles) {
        json.name(stylesToNames.get(style)).value(true);
      }
      if (clickActionName != null && clickActionData != null) {
        json.name("clickEvent")
          .beginObject()
          .name("action").value(clickActionName)
          .name("value").value(clickActionData)
          .endObject();
      }
      if (hoverActionName != null && hoverActionData != null) {
        json.name("hoverEvent")
          .beginObject()
          .name("action").value(hoverActionName)
          .name("value");
        hoverActionData.writeJson(json);
        json.endObject();
      }
      if (insertionData != null) {
        json.name("insertion").value(insertionData);
      }
      if (translationReplacements.size() > 0 && TextualComponent.isTranslatableText(text)) {
        json.name("with").beginArray();
        for (var obj : translationReplacements) {
          obj.writeJson(json);
        }
        json.endArray();
      }
      json.endObject();
    } catch (IOException e) {
      Bukkit.getLogger().log(Level.WARNING, "A problem occured during writing of JSON string", e);
    }
  }

  @Override
  public Map<String, Object> serialize() {
    var map = new HashMap<String, Object>();
    map.put("text", text);
    map.put("styles", styles);
    map.put("color", color.getName());
    map.put("hoverActionName", hoverActionName);
    map.put("hoverActionData", hoverActionData);
    map.put("clickActionName", clickActionName);
    map.put("clickActionData", clickActionData);
    map.put("insertion", insertionData);
    map.put("translationReplacements", translationReplacements);
    return map;
  }
}
