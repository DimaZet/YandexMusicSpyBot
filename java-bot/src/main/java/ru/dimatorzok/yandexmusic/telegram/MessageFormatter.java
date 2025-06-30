package ru.dimatorzok.yandexmusic.telegram;

import java.util.List;

public class MessageFormatter {

    public static String buildPlaylistUpdateMessage(String playlistOwnerName, Long telegramUserId) {
        StringBuilder sb = new StringBuilder();
        sb.append("📝 Обновление плейлиста [")
                .append(escapeMarkdownV2(playlistOwnerName))
                .append("](tg://user?id=")
                .append(telegramUserId)
                .append(")\n\n");
        return sb.toString();
    }

    private static String formatArtists(List<String> artists) {
        return String.join(", ", artists);
    }

    public static String escapeMarkdownV2(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    public static String link(String text, String url) {
        String escapedText = escapeMarkdownV2(text);
        String escapedUrl = url.replace(")", "%29"); // минимальная защита от MarkdownV2-ошибок
        return "[" + escapedText + "](" + escapedUrl + ")";
    }
}
