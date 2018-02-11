package org.elasticsearch.index.analysis;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;


public class ExtendedAnalyserProvider extends AbstractIndexAnalyzerProvider<ExtendedAnalyzer> {

    private final ExtendedAnalyzer analyzer;

    public ExtendedAnalyserProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.analyzer = new ExtendedAnalyzer();
    }

    @Override
    public ExtendedAnalyzer get() {
        return this.analyzer;
    }
}
