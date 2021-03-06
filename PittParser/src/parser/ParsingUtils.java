package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParsingUtils{

    // Ex: fullString = "assertion=present" split = "assertion="
    // @Returns "present"
    public static String splitRight(String fullString, String split) {
        if( fullString.contains(split) ) {
            // split only first occurrence
            String[] theResSplit = fullString.split(split, 2);
            return theResSplit[1];
        }

        return "";
    }

    public static Integer extractRecordFromFileName( String fileName ) {
        String[] firstSplit = fileName.split("_");

        if( firstSplit.length != 3 ) {
            return null;
        }

        String[] secondSplit = firstSplit[2].split("\\.");
        if( secondSplit.length != 2) {
            return null;
        }

        return Integer.parseInt( secondSplit[0] );
    }

    // Parses int if successful; null Integer if not
    public static Integer parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch( NumberFormatException e ) {
            return null;
        }
    }

    // Cleans up a block of text so it doesn't mess up the sql query
    public static String cleanInput( String input ) {
        StringBuilder res = new StringBuilder();
        for( int i = 0 ; i < input.length(); i++) {
            char c = input.charAt(i);
            if( c == '\'' ) {
                res.append( '\'' );
            }

            res.append(c);
        }

        return res.toString();
    }

    // Splits String to parts by number of spaces in each space group
    // Ex. "a   b   c  d  e f g" where n = 3
    // @Returns ["a", "b", "c  d..."]
    public static String[] splitByMinSpace( String text, int minSpace ) {
        List<String> stringList = new ArrayList<String>();
        int lBound = 0;

        for( int i = 0 ; i < text.length(); i++ ) {
            if( text.charAt(i) == ' ' ) {
                int spaceLength = spaceLength( text, i );
                if( spaceLength >= minSpace ) {
                    String cutString = text.substring( lBound, i );
                    stringList.add( cutString );

                    i += spaceLength;
                    lBound = i;
                }
            }
        }

        if( lBound != text.length() ) {
            String cutString = text.substring( lBound, text.length() );
            stringList.add( cutString );
        }

        String[] res = stringList.toArray( new String[stringList.size()] );
        return res;
    }

    // Returns the number of spaces starting at index
    public static int spaceLength( String text, int index ) {
        int spaceLength = 0;
        while( text.charAt(index) == ' ' ) {
            spaceLength++;
            index++;
        }

        return spaceLength;
    }
}