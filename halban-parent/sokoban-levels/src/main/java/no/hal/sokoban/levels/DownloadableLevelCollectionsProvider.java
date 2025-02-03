package no.hal.sokoban.levels;

import java.net.URI;
import java.net.URISyntaxException;
import no.hal.sokoban.level.SokobanLevel;

public abstract class DownloadableLevelCollectionsProvider implements SokobanLevel.CollectionsProvider {

    private final String baseUrl;

    protected DownloadableLevelCollectionsProvider(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUri() {
        return baseUrl;
    }

    // utility methods

    public static String encodeUriPath(String path) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c <= 32 || "%$&+,/:;=?@<>#%'".indexOf(c) >= 0) {
                result.append('%');
                result.append(Integer.toString(c, 16));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static class LabelAdapter implements no.hal.fx.LabelAdapter {

        @Override
        public Class<?> forClass() {
            return DownloadableLevelCollectionsProvider.class;
        }
        @Override
        public String getText(Object o) {
            if (o instanceof DownloadableLevelCollectionsProvider collectionsProvider) {
                var baseUri = collectionsProvider.getBaseUri();
                try {
                    return new URI(baseUri).getHost();
                } catch (URISyntaxException e) {
                    return baseUri;
                }
            }
            return null;
        }
    }
}
