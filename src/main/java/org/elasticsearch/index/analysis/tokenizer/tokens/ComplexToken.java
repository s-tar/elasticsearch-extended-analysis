package org.elasticsearch.index.analysis.tokenizer.tokens;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ComplexToken extends Token {
    private List<Token> parts;

    public ComplexToken(List<Token> parts, int offset, int position) {
        StringBuilder term = new StringBuilder();
        for (Token part : parts) {
            term.append(part.term().value());
        }
        this.term = new Term(term.toString(), offset, position, Term.Type.COMPLEX);
        this.parts = parts;
    }

    @Override
    public List<Term> getTerms() {
        Token prevPart = null;
        List<Term> terms = new ArrayList<>();
        List<Term> complexTerms = this.getComplexTerms();
        terms.addAll(complexTerms.subList(1, complexTerms.size()));
        for (Token part : this.parts) {
            if (prevPart != null && part instanceof ComplexToken) {
                complexTerms = part.getComplexTerms();
                terms.addAll(complexTerms.subList(1, complexTerms.size()));
            }
            terms.addAll(part.getSingleTerms());
            prevPart = part;
        }
        return terms;
    }

    @Override
    public List<Term> getSingleTerms() {
        ArrayList<Term> terms = new ArrayList<>();
        for (Token part : this.parts) {
            terms.addAll(part.getSingleTerms());
        }
        return terms;
    }

    @Override
    public List<Term> getComplexTerms() {
        ArrayList<Term> terms = new ArrayList<>();
        StringBuilder complexTerm = new StringBuilder();
        Iterator<Token> parts = this.parts.iterator();
        Token prevPart = null;
        while (parts.hasNext()) {
            Token part = parts.next();
            if (part instanceof SymbolToken) {
                if(complexTerm.length() == 0) {
                    continue;
                }

                SymbolToken symbol = (SymbolToken) part;
                if (!symbol.isPrefix() || prevPart instanceof SymbolToken || parts.hasNext()) {
                    complexTerm.append(symbol.term().value());
                    continue;
                }
            }

            for (Term term : part.getComplexTerms()) {
                terms.add(new Term(
                    complexTerm.toString() + term.value(),
                    this.term.getOffsetStart(),
                    this.term.getPosition(),
                    Term.Type.COMPLEX
                ));
            }
            complexTerm.append(part.term().value());
            prevPart = part;
        }
        return terms;
    }
}
