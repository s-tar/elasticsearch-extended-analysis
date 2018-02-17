package org.elasticsearch.index.analysis.tokenizer;

import org.elasticsearch.index.analysis.tokenizer.tokens.SymbolToken;
import org.elasticsearch.index.analysis.tokenizer.tokens.Term;
import org.elasticsearch.index.analysis.tokenizer.tokens.Token;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ExtendedTokenizerTests {

    private List<String> getTerms(String test) throws IOException {
        ExtendedTokenizer tokenizer = new ExtendedTokenizer();
        TokenList tokens;
        List<String> terms = new ArrayList<>();

        tokenizer.setReader(new StringReader(test));
        tokenizer.reset();

        tokens = tokenizer.getTokens();

        for (Token token : tokens) {
            if (token != null && !(token instanceof SymbolToken)) {
                for (Term term : token.getTerms()) {
                    terms.add(term.value());
                }
            }
        }
        return terms;
    }

    @Test
    public void testEmptyTokens() throws IOException {
        assertEquals(getTerms(""), Collections.emptyList());
        assertEquals(getTerms("        "), Collections.emptyList());
        assertEquals(getTerms(".."), Collections.emptyList());
        assertEquals(getTerms("/-&/"), Collections.emptyList());

    }

    @Test
    public void testBadInput() throws IOException {
        assertEquals(getTerms("/Word/"), Collections.singletonList("Word"));
        assertEquals(getTerms("*&&$^Word/'&#*"), Collections.singletonList("Word"));
        assertEquals(
            getTerms("....//Not...  //Sanitized.... +--..Text.."), Arrays.asList("Not", "Sanitized", "Text"));

    }

    @Test
    public void testAlphaTokens() throws IOException {
        assertEquals(getTerms("Word"), Collections.singletonList("Word"));
        assertEquals(getTerms("Test Few Words"), Arrays.asList("Test", "Few", "Words"));
        assertEquals(getTerms("'single' \"double\""), Arrays.asList("single", "double"));
    }

    @Test
    public void testNumTokens() throws IOException {
        assertEquals(getTerms("777"), Collections.singletonList("777"));
        assertEquals(getTerms("+23"), Arrays.asList("+23", "23"));
        assertEquals(getTerms("-23"), Collections.singletonList("-23"));
    }

    @Test
    public void testDotFloatingPointDecimalTokens() throws IOException {
        assertEquals(getTerms("5.2"), Arrays.asList("5", "5.2", "5,2"));
        assertEquals(getTerms("+5.2"), Arrays.asList("5", "5.2", "5,2", "+5.2", "+5,2"));
        assertEquals(getTerms("-5.2"), Arrays.asList("-5", "-5.2", "-5,2"));
    }

    @Test
    public void testCommaFloatingPointDecimalTokens() throws IOException {
        assertEquals(getTerms("5,2"), Arrays.asList("5", "5.2", "5,2"));
        assertEquals(getTerms("+5,2"), Arrays.asList("5", "5.2", "5,2", "+5.2", "+5,2"));
        assertEquals(getTerms("-5,2"), Arrays.asList("-5", "-5.2", "-5,2"));
    }

    @Test
    public void testPrefixedTokens() throws IOException {
        assertEquals(getTerms("°"), Collections.emptyList());
        assertEquals(getTerms("%"), Collections.emptyList());
        assertEquals(getTerms("+++"), Collections.emptyList());
        assertEquals(getTerms("---"), Collections.emptyList());

        assertEquals(getTerms("20°"), Arrays.asList("20°", "20"));
        assertEquals(getTerms("20%"), Arrays.asList("20%", "20"));
        assertEquals(getTerms("A+++"), Arrays.asList("A+++", "A"));
        assertEquals(getTerms("A--"), Arrays.asList("A--", "A"));
    }

    @Test
    public void testAlphaNumTransitionTokens() throws IOException {
        assertEquals(getTerms("HD12"), Arrays.asList("HD12", "HD", "12"));
        assertEquals(getTerms("12HD"), Arrays.asList("12HD", "12", "HD"));
        assertEquals(getTerms("HD12T"), Arrays.asList("HD12" ,"HD12T", "HD", "12", "T"));
    }

    @Test
    public void testDotConnectionTokens() throws IOException {
        assertEquals(getTerms("12.HD"), Arrays.asList("12.HD", "12", "HD"));
        assertEquals(getTerms("K.HD.9"), Arrays.asList("K.HD", "K.HD.9", "K", "HD", "9"));
        assertEquals(getTerms("K12.HD9"), Arrays.asList("K12", "K12.HD", "K12.HD9", "K", "12", "HD", "9"));
    }

    @Test
    public void testDecimalTokens() throws IOException {
        assertNotEquals(getTerms("12.44"), Arrays.asList("12.44", "12", "44"));
        assertEquals(getTerms("HD.+23"), Arrays.asList("HD.+23", "HD", "+23", "23"));
        assertEquals(getTerms("12.44Mhg"), Arrays.asList("12.44Mhg", "12", "12.44", "12,44", "Mhg"));
        assertEquals(
            getTerms("12.44.23.6"),
            Arrays.asList("12.44", "12.44.23", "12.44.23.6", "12", "44", "23", "6")
        );

        assertEquals(getTerms("12.44,23.6"), Arrays.asList("12", "12.44", "12,44", "23", "23.6", "23,6"));
        assertEquals(getTerms("12,44.23,6"), Arrays.asList("12", "44", "44.23", "44,23", "6"));
        assertEquals(
            getTerms("12.34-56.78"),
            Arrays.asList("12.34-56.78", "12", "12.34", "12,34", "56", "56.78", "56,78")
        );
    }

    @Test
    public void testSlashConnectionTokens() throws IOException {
        assertEquals(getTerms("12/HD"), Arrays.asList("12/HD", "12", "HD"));
        assertEquals(getTerms("K12/HD9"), Arrays.asList("K12", "K12/HD", "K12/HD9", "K", "12", "HD9", "HD", "9"));
        assertEquals(getTerms("20.5m/s"), Arrays.asList("20.5m", "20.5m/s", "20", "20.5", "20,5", "m", "s"));


        assertEquals(
            getTerms("K7/H-D1/9F2"),
            Arrays.asList(
                "K7", "K7/H", "K7/H-D", "K7/H-D1", "K7/H-D1/9", "K7/H-D1/9F", "K7/H-D1/9F2",
                "K", "7", "H-D", "H-D1", "H", "D", "1", "9F", "9F2", "9", "F", "2"
            )
        );
    }

    @Test
    public void testOtherConnectionTokens() throws IOException {
        assertEquals(getTerms("12-HD"), Arrays.asList("12-HD", "12", "HD"));
        assertEquals(getTerms("HD+12"), Arrays.asList("HD+12", "HD", "12"));
        assertEquals(getTerms("K12-HD9"), Arrays.asList("K12", "K12-HD", "K12-HD9", "K", "12", "HD", "9"));
        assertEquals(getTerms("K->HD"), Arrays.asList("K->HD", "K", "HD"));
        assertEquals(getTerms("1...3"), Arrays.asList("1...3", "1", "3"));
        assertEquals(getTerms("A,B"), Arrays.asList("A", "B"));
        assertEquals(getTerms("H&M"), Arrays.asList("H&M", "H", "M"));
        assertEquals(
            getTerms("LP156WH2-TLQB"),
            Arrays.asList("LP156", "LP156WH", "LP156WH2", "LP156WH2-TLQB", "LP", "156", "WH", "2", "TLQB")
        );
    }


}
