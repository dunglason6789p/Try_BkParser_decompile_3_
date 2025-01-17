/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.tokenizer;

import java.util.ArrayList;
import java.util.List;

public class Regex {
    public static final String ELLIPSIS = "\\.{2,}";
    public static final String EMAIL = "([\\w\\d_\\.-]+)@(([\\d\\w-]+)\\.)*([\\d\\w-]+)";
    public static final String FULL_DATE = "(0?[1-9]|[12][0-9]|3[01])(\\/|-|\\.)(1[0-2]|(0?[1-9]))((\\/|-|\\.)\\d{4})";
    public static final String MONTH = "(1[0-2]|(0?[1-9]))(\\/)\\d{4}";
    public static final String DATE = "(0?[1-9]|[12][0-9]|3[01])(\\/)(1[0-2]|(0?[1-9]))";
    public static final String TIME = "(\\d\\d:\\d\\d:\\d\\d)|((0?\\d|1\\d|2[0-3])(:|h)(0?\\d|[1-5]\\d)(\u2019|'|p|ph)?)";
    public static final String MONEY = "\\p{Sc}\\d+([\\.,]\\d+)*|\\d+([\\.,]\\d+)*\\p{Sc}";
    public static final String PHONE_NUMBER = "(\\(?\\+\\d{1,2}\\)?[\\s\\.-]?)?\\d{2,}[\\s\\.-]?\\d{3,}[\\s\\.-]?\\d{3,}";
    public static final String URL = "(((https?|ftp):\\/\\/|www\\.)[^\\s/$.?#].[^\\s]*)|(https?:\\/\\/)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";
    public static final String NUMBER = "[-+]?\\d+([\\.,]\\d+)*";
    public static final String PUNCTUATION = ",|\\.|:|\\?|!|;|-|_|\"|'|\u201c|\u201d|\\||\\(|\\)|\\[|\\]|\\{|\\}|\u00e2\u0178\u00a8|\u00e2\u0178\u00a9|\u00c2\u00ab|\u00c2\u00bb|\\\\|\\/|\\\u00e2\u20ac\u02dc|\\\u00e2\u20ac\u2122|\\\u00e2\u20ac\u0153|\\\u00e2\u20ac\ufffd|\u00e2\u20ac\u00a6|\u2026|\u2018|\u2019|\u00b7";
    public static final String SPECIAL_CHAR = "\\~|\\@|\\#|\\^|\\&|\\*|\\+|\\-|\\\u00e2\u20ac\u201c|<|>|\\|";
    public static final String EOS_PUNCTUATION = "(\\.+|\\?|!|\u2026)";
    public static final String NUMBERS_EXPRESSION = "[-+]?\\d+([\\.,]\\d+)*([\\+\\-\\*\\/][-+]?\\d+([\\.,]\\d+)*)*";
    public static final String SHORT_NAME = "[\\p{Upper}]\\.([\\p{L}\\p{Upper}])*";
    public static final String ALLCAP = "[A-Z]+\\.[A-Z]+";
    private static List<String> regexes = null;
    private static List<String> regexIndex = null;

    public static List<String> getRegexList() {
        if (regexes == null) {
            regexes = new ArrayList<String>();
            regexIndex = new ArrayList<String>();
            regexes.add(ELLIPSIS);
            regexIndex.add("ELLIPSIS");
            regexes.add(EMAIL);
            regexIndex.add("EMAIL");
            regexes.add(URL);
            regexIndex.add("URL");
            regexes.add(FULL_DATE);
            regexIndex.add("FULL_DATE");
            regexes.add(MONTH);
            regexIndex.add("MONTH");
            regexes.add(DATE);
            regexIndex.add("DATE");
            regexes.add(TIME);
            regexIndex.add("TIME");
            regexes.add(MONEY);
            regexIndex.add("MONEY");
            regexes.add(PHONE_NUMBER);
            regexIndex.add("PHONE_NUMBER");
            regexes.add(SHORT_NAME);
            regexIndex.add("SHORT_NAME");
            regexes.add(NUMBERS_EXPRESSION);
            regexIndex.add("NUMBERS_EXPRESSION");
            regexes.add(NUMBER);
            regexIndex.add("NUMBER");
            regexes.add(PUNCTUATION);
            regexIndex.add("PUNCTUATION");
            regexes.add(SPECIAL_CHAR);
            regexIndex.add("SPECIAL_CHAR");
            regexes.add(ALLCAP);
            regexIndex.add("ALLCAP");
        }
        return regexes;
    }

    public static int getRegexIndex(String regex) {
        return regexIndex.indexOf(regex.toUpperCase());
    }
}

