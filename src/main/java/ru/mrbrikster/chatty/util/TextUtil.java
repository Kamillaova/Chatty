package ru.mrbrikster.chatty.util;

import net.md_5.bungee.api.ChatColor;
import ru.mrbrikster.chatty.json.LegacyConverter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

public final class TextUtil {
  private static final Pattern HEX_COLORS_PATTERN = Pattern.compile("\\{#([a-fA-F0-9]{6})}");
  private static final Pattern HEX_GRADIENT_PATTERN = Pattern.compile("\\{#([a-fA-F0-9]{6})(:#([a-fA-F0-9]{6}))+( )([^{}])*(})");
  private static final Pattern HEX_SPIGOT_PATTERN = Pattern.compile("§[xX](§[a-fA-F0-9]){6}");

  private static final List<ChatColor> FORMAT_COLORS = List.of(
    ChatColor.BOLD,
    ChatColor.ITALIC,
    ChatColor.UNDERLINE,
    ChatColor.MAGIC,
    ChatColor.STRIKETHROUGH,
    ChatColor.RESET
  );

  private TextUtil() { }

  public static ChatColor parseChatColor(String string) {
    checkArgument(string != null, "string cannot be null");
    return ChatColor.of(string);
  }

  public static boolean isColor(ChatColor color) {
    for (var formatColor : FORMAT_COLORS) {
      if (formatColor == color) {
        return false;
      }
    }

    return true;
  }

  public static boolean isFormat(ChatColor color) {
    return !isColor(color);
  }

  /**
   * Removes spigot hex-codes from string
   *
   * @param str string to strip hex
   * @return stripped string
   */
  public static String stripHex(String str) {
    if (str == null) {
      return null;
    }

    var matcher = HEX_SPIGOT_PATTERN.matcher(str);
    return matcher.replaceAll("");
  }

  /**
   * Finds simple and gradient hex patterns in string and converts it to Spigot format
   *
   * @param text string to stylish
   * @return stylished string
   */
  public static String stylish(String text) {
    if (text == null) {
      return null;
    }

    var matcher = HEX_GRADIENT_PATTERN.matcher(text);

    var stringBuffer = new StringBuilder();

    while (matcher.find()) {
      var gradient = matcher.group();

      var groups = 0;
      for (var i = 1; gradient.charAt(i) == '#'; i += 8) {
        groups++;
      }

      var colors = new Color[groups];
      for (var i = 0; i < groups; i++) {
        colors[i] = ChatColor.of(gradient.substring((8 * i) + 1, (8 * i) + 8)).getColor();
      }

      var substring = gradient.substring((groups - 1) * 8 + 9, gradient.length() - 1);

      var chars = substring.toCharArray();

      var gradientBuilder = new StringBuilder();

      var colorLength = chars.length / (colors.length - 1);
      int lastColorLength;
      if (colorLength == 0) {
        colorLength = 1;
        lastColorLength = 1;
        colors = Arrays.copyOfRange(colors, 0, chars.length);
      } else {
        lastColorLength = chars.length % (colorLength * (colors.length - 1)) + colorLength;
      }

      List<ChatColor> currentStyles = new ArrayList<>();
      for (var i = 0; i < (colors.length - 1); i++) {
        var currentColorLength = ((i == colors.length - 2) ? lastColorLength : colorLength);
        for (var j = 0; j < currentColorLength; j++) {
          var color = calculateGradientColor(j + 1, currentColorLength, colors[i], colors[i + 1]);
          var chatColor = ChatColor.of(color);

          var charIndex = colorLength * i + j;
          if (charIndex + 1 < chars.length) {
            if (chars[charIndex] == '&' || chars[charIndex] == '§') {
              if (chars[charIndex + 1] == 'r') {
                currentStyles.clear();
                j++;
                continue;
              }

              var style = ChatColor.getByChar(chars[charIndex + 1]);
              if (style != null) {
                currentStyles.add(style);
                j++;
                continue;
              }
            }
          }

          var builder = gradientBuilder.append(chatColor.toString());

          for (var currentStyle : currentStyles) {
            builder.append(currentStyle.toString());
          }

          builder.append(chars[charIndex]);
        }
      }

      matcher.appendReplacement(stringBuffer, gradientBuilder.toString());
    }

    matcher.appendTail(stringBuffer);
    text = stringBuffer.toString();

    matcher = HEX_COLORS_PATTERN.matcher(text);
    stringBuffer = new StringBuilder();

    while (matcher.find()) {
      var hexColorString = matcher.group();
      matcher.appendReplacement(stringBuffer, ChatColor.of(hexColorString.substring(1, hexColorString.length() - 1)).toString());
    }

    matcher.appendTail(stringBuffer);

    return ChatColor.translateAlternateColorCodes('&', stringBuffer.toString());
  }

  public static String fixMultilineFormatting(String text) {
    return text.replaceAll("\n$", "").replaceAll("\n", "\n&r");
  }

  public static String getLastColors(String text) {
    return new LegacyConverter(text).toFancyMessage().getLastColors();
  }

  private static Color calculateGradientColor(int x, int parts, Color from, Color to) {
    var p = (double) (parts - x + 1) / (double) parts;

    return new Color(
      (int) (from.getRed() * p + to.getRed() * (1 - p)),
      (int) (from.getGreen() * p + to.getGreen() * (1 - p)),
      (int) (from.getBlue() * p + to.getBlue() * (1 - p))
    );
  }
}
