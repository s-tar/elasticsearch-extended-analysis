package org.elasticsearch.index.analysis.tokenizer.tokens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Token {
    protected Term term;

    public List<Term> getSingleTerms() {
        return Collections.singletonList(this.term);
    }

    public List<Term> getComplexTerms() {
        return new ArrayList<>(Collections.singletonList(this.term));
    }

    public List<Term> getTerms() {
        ArrayList<Term> terms = new ArrayList<>();
        terms.add(this.term);
        return terms;
    }

    public Term term() {
        return term;
    }

    @Override
    public String toString() {
        return String.format(
            "%s(term: %s)", this.getClass().getSimpleName(), this.term
        );
    }
}
