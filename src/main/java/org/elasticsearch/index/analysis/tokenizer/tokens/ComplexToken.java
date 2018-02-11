package org.elasticsearch.index.analysis.tokenizer.tokens;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ComplexToken extends Token {
    private List<Token> parts;

    public ComplexToken(List<Token> parts) {
        StringBuilder term = new StringBuilder();
        for (Token part : parts) {
            term.append(part.getTerm());
        }
        this.term = term.toString();
        this.parts = parts;
    }

    @Override
    public List<String> getTerms() {
        List<String> terms = new ArrayList<>();
        List<String> complexTerms = this.getComplexTerms();
        terms.addAll(this.getSimpleTerms());
        terms.addAll(complexTerms.subList(1, complexTerms.size()));
        return terms;
    }

    @Override
    public List<String> getSimpleTerms() {
        ArrayList<String> terms = new ArrayList<>();
        for (Token part : this.parts) {
            if (!(part instanceof SymbolToken)) {
                terms.addAll(part.getSimpleTerms());
            }
        }
        return terms;
    }

    @Override
    public List<String> getComplexTerms() {
        ArrayList<String> terms = new ArrayList<>();
        StringBuilder complexTerm = new StringBuilder();
        Iterator<Token> parts = this.parts.iterator();
        Token prevPart = null;
        while (parts.hasNext()) {
            Token part = parts.next();
            List<String> complexTokens = part.getComplexTerms();

            if (part instanceof ComplexToken && prevPart != null) {
                terms.addAll(complexTokens.subList(1, complexTokens.size()));
            }

            if (part instanceof SymbolToken) {
                SymbolToken symbol = (SymbolToken) part;
                if(complexTerm.length() == 0) {
                    continue;
                }

                if (!symbol.isPrefix() || prevPart instanceof SymbolToken || parts.hasNext()) {
                    complexTerm.append(symbol.getTerm());
                    continue;
                }
            }


            for (String term : complexTokens) {
                terms.add(complexTerm.toString() + term);
            }
            complexTerm.append(part.getTerm());
            prevPart = part;
        }
        return terms;
    }
}
