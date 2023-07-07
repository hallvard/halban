package no.hal.sokoban.levels;

import java.net.URI;
import java.net.URISyntaxException;

import no.hal.plugin.fx.LabelAdapter;

public class DownloadableLevelCollectionsProviderLabelAdapter implements LabelAdapter {
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
