package org.cimsbioko.search

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.core.LowerCaseFilter
import org.apache.lucene.util.Version
import java.io.Reader

/**
 * A custom lucene [Analyzer] that processes input during indexing. It processes input into a
 * stream of lowercase alphanumeric tokens.
 */
class CustomAnalyzer : Analyzer() {
    override fun createComponents(fieldName: String, reader: Reader) = AlphaNumericTokenizer(Version.LUCENE_47, reader)
            .let { TokenStreamComponents(it, LowerCaseFilter(Version.LUCENE_47, it)) }
}