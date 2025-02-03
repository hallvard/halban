package no.hal.sokoban.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;
import no.hal.sokoban.parser.SokobanFactory;
import no.hal.sokoban.parser.SokobanParser;

public class SokobanFactoryImpl extends SokobanFactory {

    @Override
    public SokobanGrid createSokobanGrid(List<CellKind[]> cellLines) {
        return new SokobanGridImpl(cellLines.toArray(new CellKind[cellLines.size()][]));
    }

    @Override
    public SokobanLevel createSokobanLevel(MetaData metaData, SokobanGrid sokobanGrid) {
        return new SokobanLevelImpl(metaData, sokobanGrid);
    }

    @Override
    public SokobanLevel.Collection createSokobanLevelCollection(Map<String, String> metaData, List<SokobanLevel> levels) {
        return new SokobanLevelImpl.CollectionImpl(metaData, levels);
    }

    //

    public static void main(String[] args) throws IOException {
        var parser = new SokobanParser(new SokobanFactoryImpl());
        var section1 = parser.parse("""
            #######
            #.@ # #
            #$* $ #
            #   $ #
            # ..  #
            #  *  #
            #######
            """);
        System.out.println(section1);
        var section2 = parser.parse("#######|#.@ # #|#$* $ #|#   $ #|# ..  #|#  *  #|#######", null);
        System.out.println(section2);
        System.out.println(section1.equals(section2));

        var section3 = parser.parse("""
            Title: AC-Smileys
            Description: A small collection in the shape of Smileys, arranged from very easy to not so
                         easy, the harder ones probably requre a little imagination to see the Smileys,
                         on the other hand, those are more interesting to solve ;-)
            Author: Andrej Cerjak
            Email: ACSokoban@Yandex.com

            """);
        System.out.println(section3);

        var section4 = parser.parse("""
            #########
            #       #
            #  $ $ $  #
           #  $ $ $ $  #
           # $ #...# $ #
          ##  ###.###  ##
          ## $ #...# $ ##
           #  $..#..$  #
           # $ *.#.* $ #
           #  $..#..$  #
            #  #. .#  #
            # $ ### $ #
             #   @   #
              #######
          Title: AC_Smiley01
          """);
        System.out.println(section4);

        var section5 = parser.parse("""
            Title: AC-Smileys
            Description: A small collection in the shape of Smileys, arranged from very easy to not so
                         easy, the harder ones probably requre a little imagination to see the Smileys,
                         on the other hand, those are more interesting to solve ;-)
            Author: Andrej Cerjak
            Email: ACSokoban@Yandex.com
            
                #######
               #       #
              #  $ $ $  #
             #  $ $ $ $  #
             # $ #...# $ #
            ##  ###.###  ##
            ## $ #...# $ ##
             #  $..#..$  #
             # $ *.#.* $ #
             #  $..#..$  #
              #  #. .#  #
              # $ ### $ #
               #   @   #
                #######
            Title: AC_Smiley01
            """);
        System.out.println(section5);
    }
}
