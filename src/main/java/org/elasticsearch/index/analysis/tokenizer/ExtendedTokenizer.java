package org.elasticsearch.index.analysis.tokenizer;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.index.analysis.tokenizer.tokens.*;

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
    }

    private Term prevTerm;
    private List<Term> terms;
    private Iterator<Term> termsIterator;


    private final CharTermAttribute charTermAttr = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAttr = this.addAttribute(OffsetAttribute.class);
    private final PositionIncrementAttribute posIncrAttr = this.addAttribute(PositionIncrementAttribute.class);
    private final TypeAttribute typeAttr = this.addAttribute(TypeAttribute.class);


    public ExtendedTokenizer() {
    }

    @Override
    public final boolean incrementToken() throws IOException {
        this.charTermAttr.setEmpty();

        if (terms == null) {
            terms = new ArrayList<>();
            for (Token token : getTerms()) {
                if (token != null) {
                    terms.addAll(token.getTerms());
                }
            }
            termsIterator = terms.iterator();
        }


        if (!termsIterator.hasNext()) {
            return false;
        }

        Term term = termsIterator.next();
        int positionIncrement = 1;
        if (prevTerm != null) {
            positionIncrement = term.getPosition() - prevTerm.getPosition();
        }

        this.posIncrAttr.setPositionIncrement(positionIncrement);
        this.charTermAttr.append(term.value());
        this.offsetAttr.setOffset(term.getOffsetStart(), term.getOffsetEnd());
        this.typeAttr.setType(term.getType().toString());
        prevTerm = term;
        return true;
    }

    public TokenList getTerms() throws IOException {
        TokenList tokens = new TokenList();
        StringBuilder termBuilder = new StringBuilder();
        int charCode = input.read();
        int nextCharCode;
        int position = 0;
        int charPosition = 0;

        while(charCode != -1) {
            nextCharCode = input.read();
            termBuilder.append((char) charCode);
            charPosition++;
            if (isTypeChanged(charCode, nextCharCode)) {
                String term = termBuilder.toString();
                int offset  = charPosition - term.length();
                switch(getType(charCode)) {
                    case LETTER: tokens.add(new AlphaToken(term, offset, position)); break;
                    case DIGIT: tokens.add(new NumberToken(term, offset, position)); break;
                    case SYMBOL: tokens.add(new SymbolToken(term, offset, position)); position--; break;
                    default: tokens.add(null); position--; break;
                }
                position++;

                termBuilder.setLength(0);

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
        if (Character.isLetter(charCode)) {
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
        prevTerm = null;
        terms = null;
        termsIterator = null;
        clearAttributes();
    }
}
