package org.elasticsearch.index.analysis.tokenizer.tokens;

public class AlphaToken extends Token {

    public AlphaToken(String term, int offset, int position) {
        this.term = new Term(term, offset, position, Term.Type.ALPHA);
    }
}
