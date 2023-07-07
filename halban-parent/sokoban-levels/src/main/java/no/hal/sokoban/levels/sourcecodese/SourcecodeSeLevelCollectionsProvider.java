package no.hal.sokoban.levels.sourcecodese;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;
import no.hal.sokoban.levels.DownloadableLevelCollection;
import no.hal.sokoban.levels.DownloadableLevelCollectionsProvider;
import no.hal.sokoban.parser.SokobanParser;

public class SourcecodeSeLevelCollectionsProvider extends DownloadableLevelCollectionsProvider {
    
    private final static String SOURCECODE_SE_HREF = "https://www.sourcecode.se/sokoban/levels.php";

    public SourcecodeSeLevelCollectionsProvider() {
        super(SOURCECODE_SE_HREF);
    }

    @Override
    public List<SokobanLevel.Collection> getSokobanLevelCollections() {
        List<SokobanLevel.Collection> collections = new ArrayList<>();
        try {
            var levelsUri = new URI(getBaseUri());
            Document collectionsDocument = Jsoup.parse(levelsUri.toURL(), 2000);
            var levelDivs = collectionsDocument.getElementsByClass("expand_lev");
            //<div class="expand_lev" id="I_0000" style="--coll_id:1568; --nr_levs:100;">
            //    <div class="Title clearfix">1,000,001 candlelights&nbsp;</div>
            //    <div class="LevDate clearfix">2021-12-28</div>
            //    <div class="NrLev clearfix">100&nbsp;&nbsp;</div>
            //    <div class="Auth clearfix">A1master</div>
            //</div>
            for (var levelsDiv : levelDivs) {
                var collectionId = getStyleAttr(levelsDiv.attr("style"), "--coll_id");
                var title = levelsDiv.getElementsByAttributeValueStarting("class", "Title").text().trim();
                var author = levelsDiv.getElementsByAttributeValueStarting("class", "Auth").text().trim();
                var slcInfoUri = levelsUri.resolve("level_func.php?act=buttons&id=" + collectionId);
                var metaData = SokobanParser.metaDataOf(Map.of("Title", title, "Author", author, "infoUrl", slcInfoUri.toString()));
                collections.add(new DownloadableSourcecodeSeLevelCollection(slcInfoUri, metaData));
            }
            return collections;
        } catch (IOException | URISyntaxException ex) {
            System.err.println(ex);
            return collections;
        }
    }


    private String getStyleAttr(String style, String attr) {
        if (style != null) {
            var pos = style.indexOf(attr + ":", 0);
            if (pos >= 0) {
                var pos2 = style.indexOf(";", pos);
                if (pos2 > pos) {
                    return style.substring(pos + attr.length() + 1, pos2);
                }
            }
        }
        return null;
    }

    /* https://www.sourcecode.se/sokoban/level_func.php?act=buttons&id=1460
     <div class='LevelButtons'>
        <a href="?act=dnl_level&file=Zone_26.slc&as_text=0" style="color: #2034E7" download>
            <img class="SkinButton" src="sites/default/files/dnl_x_btn.png" title="Download slc (xml)"/>
        </a>
        <a href="?act=dnl_level&file=Zone_26.slc&as_text=1" style="color: #2034E7" download>
            <img class="SkinButton" src="sites/default/files/dnl_t_btn.png" title="Download text levels"/>
        </a>
    </div>
    */

    private static class DownloadableSourcecodeSeLevelCollection extends DownloadableLevelCollection {

        protected DownloadableSourcecodeSeLevelCollection(URI downloadHref, MetaData metaData) {
            super(downloadHref, metaData);
        }

        private SokobanParser sokobanParser = new SokobanParser();

        @Override
        protected SokobanLevel.Collection extractSokobanLevelsCollection(Document document) {
            var links = document.getElementsByTag("a");
            if (links.size() >= 2) {
                var slcHref = links.get(1).attr("href");
                try {
                    var slcUri = new URI(SOURCECODE_SE_HREF).resolve("level_func.php" + slcHref);
                    try (var slcStream = slcUri.toURL().openStream()) {
                        return sokobanParser.parse(slcStream);
                    }
                } catch (URISyntaxException | IOException e) {
                    System.err.println(e);
                }
            }
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
         var collections = new SourcecodeSeLevelCollectionsProvider().getSokobanLevelCollections();
         for (var collection : collections) {
            System.out.println(collection.getSokobanLevels());
            break;
         }
    }
}
