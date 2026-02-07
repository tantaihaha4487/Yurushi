package net.thanachot.yurushi.util;

public final class MinotarUtil {

    private static final String BASE_URL = "https://minotar.net";
    private static final String STEVE = "MHF_Steve";
    private static final int DEFAULT_SIZE = 100;

    private MinotarUtil() {
    }

    public static String getAvatarUrl(String username) {
        return getAvatarUrl(username, DEFAULT_SIZE);
    }

    public static String getAvatarUrl(String username, int size) {
        String name = (username == null || username.isBlank()) ? STEVE : username;
        return BASE_URL + "/helm/" + name + "/" + size + ".png";
    }

    public static String getSteveAvatarUrl() {
        return getAvatarUrl(STEVE, DEFAULT_SIZE);
    }

    public static String getSteveAvatarUrl(int size) {
        return getAvatarUrl(STEVE, size);
    }
}
