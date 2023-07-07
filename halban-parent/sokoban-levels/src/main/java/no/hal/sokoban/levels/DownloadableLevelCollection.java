package no.hal.sokoban.levels;

import java.io.IOException;
import java.net.URI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;

public abstract class DownloadableLevelCollection extends LoadableLevelCollection {

    protected final URI downloadHref;
    protected MetaData metaData;

    protected DownloadableLevelCollection(URI downloadHref, MetaData metaData) {
        this.downloadHref = downloadHref;
        this.metaData = SokobanLevel.metaDataOf(() -> metaData, () -> super.getMetaData());
    }

    @Override
    public String toString() {
        return "[SokobanLevel.Collection @ %s]".formatted(downloadHref);
    }

    @Override
    public MetaData getMetaData() {
        return this.metaData;
    }

    @Override
    protected SokobanLevel.Collection loadSokobanLevelsCollection() throws IOException {
        var document = Jsoup.parse(downloadHref.toURL(), 2000);
        return extractSokobanLevelsCollection(document);
    }

    protected SokobanLevel.Collection extractSokobanLevelsCollection(Document document) {
        return null;
    }
}
