package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.index.analysis.tokenizer.ExtendedTokenizer;


public class ExtendedAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new ExtendedTokenizer();
        return new TokenStreamComponents(source);
    }
}
