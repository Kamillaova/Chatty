package ru.mrbrikster.chatty.util;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.gson.legacyimpl.NBTLegacyHoverEventSerializer;
import org.bukkit.Material;

public final class ComponentSerializers {
  public static final GsonComponentSerializer GSON_SERIALIZER =
    is1_16()
    ? GsonComponentSerializer.builder()
      .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
      .build()
    : GsonComponentSerializer.builder()
      .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
      .emitLegacyHoverEvent()
      .downsampleColors()
      .build();

  private static boolean is1_16() {
    try {
      Enum.valueOf(Material.class, "NETHERITE_PICKAXE");
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private ComponentSerializers() { }
}
