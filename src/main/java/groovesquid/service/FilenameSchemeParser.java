package groovesquid.service;

import groovesquid.model.Song;

import java.text.DecimalFormat;
import java.util.Locale;

public class FilenameSchemeParser {
    public static final String DEFAULT_FILENAME_SCHEME = "<Artist?<Artist>/><Album?<Album>/><##?<##> - ><Title>.mp3";
    public static final char DEFAULT_WHITESPACE_REPLACE_CHAR = ' ';
    public static final char DEFAULT_ILLEGAL_REPLACE_CHAR = '_';

    private static final char BRACKET_OPEN = '<';
    private static final char BRACKET_CLOSE = '>';
    private static final char QUESTIONMARK = '?';
    private static final String TAG_ARTIST = "Artist";
    private static final String TAG_ALBUM = "Album";
    private static final String TAG_TITLE = "Title";

    private String filenameScheme = DEFAULT_FILENAME_SCHEME;
    private char whitespaceReplaceChar = DEFAULT_WHITESPACE_REPLACE_CHAR;
    private char illegalReplaceChar = DEFAULT_ILLEGAL_REPLACE_CHAR;

    public String getFilenameScheme() {
        return filenameScheme;
    }

    public void setFilenameScheme(String filenameScheme) {
        this.filenameScheme = filenameScheme;
    }

    public char getWhitespaceReplaceChar() {
        return whitespaceReplaceChar;
    }

    public void setWhitespaceReplaceChar(char whitespaceReplaceChar) {
        this.whitespaceReplaceChar = whitespaceReplaceChar;
    }

    public char getIllegalReplaceChar() {
        return illegalReplaceChar;
    }

    public void setIllegalReplaceChar(char illegalReplaceChar) {
        this.illegalReplaceChar = illegalReplaceChar;
    }

    public String parse(Song song) throws IllegalArgumentException {
        return parse(song, this.filenameScheme);
    }

    public String parse(Song song, String filenameScheme) throws IllegalArgumentException {
        if (filenameScheme == null || filenameScheme.trim().isEmpty())
            throw new IllegalArgumentException("scheme may not be empty");
        return parse0(song, filenameScheme);
    }

    private String parse0(Song song, String filenameScheme) throws IllegalArgumentException {
        int len = filenameScheme.length();
        int pos = 0;
        int bracketLevel = 0;
        int startBracketPos = -1;
        StringBuilder result = new StringBuilder();

        while (pos < len) {
            char c = filenameScheme.charAt(pos);
            if (c == BRACKET_OPEN) {
                if (bracketLevel == 0) {
                    startBracketPos = pos + 1;
                }
                bracketLevel++;
            } else if (c == BRACKET_CLOSE) {
                --bracketLevel;
                if (bracketLevel == 0) {
                    result.append(parseBrackets(song, filenameScheme.substring(startBracketPos, pos)));
                } else if (bracketLevel < 0) {
                    throw new IllegalArgumentException("unbalanced < >");
                }
            } else if (bracketLevel == 0) {
                result.append(c);
            }
            pos++;
        }

        if (bracketLevel != 0)
            throw new IllegalArgumentException("unbalanced < >");

        return result.toString();
    }

    // parse text inside "<" ... ">".
    private String parseBrackets(Song song, String s) {
        int questionPos = s.indexOf(QUESTIONMARK);
        if (questionPos == 0)
            throw new IllegalArgumentException("invalid sequence: <?");

        String tag = s.substring(0, questionPos > 0 ? questionPos : s.length());
        if (tag.isEmpty())
            throw new IllegalArgumentException("invalid sequence: <>");

        String conditionalText = null;
        if (questionPos > 0)
            conditionalText = s.substring(questionPos + 1);

        String result = matchNumberTag(tag, song, conditionalText);
        if (result == null)
            result = matchTag(TAG_ARTIST, tag, song.getArtist().getName(), conditionalText, song);
        if (result == null)
            result = matchTag(TAG_ALBUM, tag, song.getAlbum().getName(), conditionalText, song);
        if (result == null)
            result = matchTag(TAG_TITLE, tag, song.getName(), conditionalText, song);
        if (result == null)
            throw new IllegalArgumentException("unknown tag: " + tag);

        return result;
    }

    private String matchNumberTag(String tag, Song song, String conditionalText) {
        if (!tag.matches("#+"))
            return null;
        String parsedConditionalText = null;
        if (conditionalText != null)
            parsedConditionalText = parse0(song, conditionalText);
        if (conditionalText != null)
            return parsedConditionalText;
        Long trackNum = song.getTrackNum();
        if (trackNum != null) {
            String formatMask = tag.replace('#', '0');
            return new DecimalFormat(formatMask).format(trackNum);
        } else {
            return "";
        }
    }

    private String matchTag(String tagName, String actualTag, String entryValue, String conditionalText, Song song) {
        boolean isSameTag = actualTag.equals(tagName);
        boolean isUpperCaseTag = actualTag.equals(tagName.toUpperCase(Locale.US));
        boolean isLowerCaseTag = actualTag.equals(tagName.toLowerCase(Locale.US));
        if (!(isSameTag || isUpperCaseTag || isLowerCaseTag))
            return null;
        String parsedConditionalText = null;
        if (conditionalText != null)
            parsedConditionalText = parse0(song, conditionalText);
        if (entryValue == null || entryValue.isEmpty())
            return "";
        if (conditionalText != null)
            return parsedConditionalText;
        if (isUpperCaseTag)
            return encode(entryValue.toUpperCase()); // note: using user locale here
        if (isLowerCaseTag)
            return encode(entryValue.toLowerCase()); // note: using user locale here
        return encode(entryValue);
    }

    private String encode(String name) {
        if (name == null)
            return "";

        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?'
                    || c == '"' || c == '<' || c == '>' || c == '|' || c == '.') {
                sb.append(illegalReplaceChar);
            } else if (Character.isWhitespace(c)) {
                sb.append(whitespaceReplaceChar);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
