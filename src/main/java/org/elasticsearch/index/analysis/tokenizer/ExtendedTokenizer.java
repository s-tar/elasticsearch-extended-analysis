package org.elasticsearch.index.analysis.tokenizer;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.index.analysis.tokenizer.tokens.AlphaToken;
import org.elasticsearch.index.analysis.tokenizer.tokens.NumberToken;
import org.elasticsearch.index.analysis.tokenizer.tokens.SymbolToken;
import org.elasticsearch.index.analysis.tokenizer.tokens.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ExtendedTokenizer extends Tokenizer {
    private enum CharType {
        LETTER,
        DIGIT,
        SYMBOL,
        OTHER,
        END_OF_TERM,
    }

    private List<String> tokens = null;
    private Iterator<String> tokensIterator = null;

    private final CharTermAttribute charTermAttr = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = this.addAttribute(OffsetAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = this.addAttribute(PositionIncrementAttribute.class);
    private final TypeAttribute typeAtt = this.addAttribute(TypeAttribute.class);


    public ExtendedTokenizer() {
    }

    @Override
    public final boolean incrementToken() throws IOException {
        this.charTermAttr.setEmpty();

        if (tokens == null) {
            tokens = new ArrayList<>();
            for (Token token : getTokens()) {
                if (token != null) {
                    tokens.addAll(token.getTerms());
                }
            }
            tokensIterator = tokens.iterator();
        }
        if (!tokensIterator.hasNext()) {
            return false;
        }

        String token = tokensIterator.next();
        this.charTermAttr.append(token);
        this.posIncrAtt.setPositionIncrement(1);
//        this.offsetAtt.setOffset(token.getTerm(), token.getEndOffset());

        return true;
    }

    public TokenList getTokens() throws IOException {
        TokenList tokens = new TokenList();
        StringBuilder term = new StringBuilder();
        int charCode = input.read();
        int nextCharCode;

        while(charCode != -1) {
            nextCharCode = input.read();
            term.append((char) charCode);
            if (isTypeChanged(charCode, nextCharCode)) {
                switch(getType(charCode)) {
                    case LETTER: tokens.add(new AlphaToken(term.toString())); break;
                    case DIGIT: tokens.add(new NumberToken(term.toString())); break;
                    case SYMBOL: tokens.add(new SymbolToken(term.toString())); break;
                    default: tokens.add(null); break;
                }

                term.setLength(0);

            }
            charCode = nextCharCode;
        }
        tokens.complicateTokens();
        return tokens;
    }

    private boolean isTypeChanged(int charCode, int nextCharCode) {
        CharType currentType = getType(charCode);
        CharType nextType = getType(nextCharCode);

        return currentType != nextType || (
            currentType == CharType.SYMBOL &&
            charCode != nextCharCode
        );
    }

    private CharType getType(int charCode) {
        if (Character.isSpaceChar(charCode) || charCode == -1) {
            return CharType.END_OF_TERM;
        } else if (Character.isLetter(charCode)) {
            return CharType.LETTER;
        } else if (Character.isDigit(charCode)) {
            return CharType.DIGIT;
        } else if (SymbolToken.isAvailableSymbol(charCode)) {
            return CharType.SYMBOL;
        } else {
            return CharType.OTHER;
        }
    }
    
    @Override
    public final void reset() throws IOException {
        super.reset();
        tokens = null;
        tokensIterator = null;
        clearAttributes();
    }
}
