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

import java.time.Duration;

import static net.kyori.adventure.title.Title.Times.times;
import static net.kyori.adventure.title.Title.title;
import static ru.mrbrikster.chatty.util.ComponentSerializers.GSON_SERIALIZER;

/**
 * Represents a title that appears at the center of the screen.
 *
 * @author Luca
 */
public class Title {
  private final net.kyori.adventure.title.Title title;

  /**
   * Constructs a {@link Title} object.
   *
   * @param title    The text of the main title.
   * @param subtitle The text of the subtitle.
   * @param fadeIn   The fade-in time of the title (in ticks).
   * @param stay     The stay time of the title (in ticks).
   * @param fadeOut  The fade-out time of the title (in ticks).
   */
  public Title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    this.title = title(
      GSON_SERIALIZER.deserialize(title),
      GSON_SERIALIZER.deserialize(subtitle),
      times(
        Duration.ofMillis(50L * fadeIn),
        Duration.ofMillis(50L * stay),
        Duration.ofMillis(50L * fadeOut)
      )
    );
  }

  /**
   * Sends the title to a specific player.
   *
   * @param player The player to send the title to.
   */
  public void send(Player player) {
    Chatty.audiences().player(player).showTitle(title);
  }
}
