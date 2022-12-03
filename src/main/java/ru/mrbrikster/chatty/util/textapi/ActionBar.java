/*
 This file is part of TextAPI 2.0.
 Copyright (c) 2015 Luca P. <https://github.com/TheLuca98>

 TextAPI is free software: you can redistribute it and/or modify it under the
 terms of the GNU Lesser General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any
 later version.

 TextAPI is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.

 You should have received a copy of the GNU Lesser General Public License along
 with TextAPI. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.mrbrikster.chatty.util.textapi;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Chatty;

import static com.google.common.base.Preconditions.checkNotNull;
import static ru.mrbrikster.chatty.util.ComponentSerializers.GSON_SERIALIZER;

/**
 * Represents a message displayed above the hotbar.
 *
 * @author Luca
 */
public class ActionBar {
  private Component text;

  /**
   * Constructs an {@link ActionBar} object based on plain text.
   *
   * @param text Text to display.
   */
  public ActionBar(String text) {
    checkNotNull(text);
    this.text = GSON_SERIALIZER.deserialize(text);
  }

  /**
   * This method has been kept just to ensure backwards compatibility with older versions of TextAPI.
   * It is not supported and will be removed in a future release.
   *
   * @param player  The player to send the message to.
   * @param message The message to send.
   * @deprecated Please create a new {@link ActionBar} instance instead.
   */
  @Deprecated
  public static void send(Player player, String message) {
    new ActionBar(message).send(player);
  }

  /**
   * Sends an action bar message to a specific player.
   *
   * @param player The player to send the message to.
   */
  public void send(Player player) {
    Chatty.audiences().player(player).sendActionBar(text);
  }

  /**
   * Changes the text to display.
   *
   * @param text Text to display.
   */
  public void setText(String text) {
    checkNotNull(text);
    this.text = GSON_SERIALIZER.deserialize(text);
  }
}
