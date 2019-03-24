package parser;

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

    // Parses int if successful; null Integer if not
    public static Integer parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch( NumberFormatException e ) {
            return null;
        }
    }

    // Splits String to parts by number of spaces in each space group
    // Ex. "a   b   c  d  e f g" where n = 3
    // @Returns ["a", "b", "c  d..."]
    public static String[] splitByMinSpace( String text, int minSpace ) {
        return null;
    }
}