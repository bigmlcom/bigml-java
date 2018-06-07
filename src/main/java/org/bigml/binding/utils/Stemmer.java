package org.bigml.binding.utils;

import org.apache.lucene.analysis.ar.ArabicNormalizer;
import org.apache.lucene.analysis.ar.ArabicStemmer;
import org.apache.lucene.analysis.cz.CzechStemmer;
import org.apache.lucene.analysis.fa.PersianNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.CatalanStemmer;
import org.tartarus.snowball.ext.DanishStemmer;
import org.tartarus.snowball.ext.DutchStemmer;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.FinnishStemmer;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.tartarus.snowball.ext.GermanStemmer;
import org.tartarus.snowball.ext.HungarianStemmer;
import org.tartarus.snowball.ext.ItalianStemmer;
import org.tartarus.snowball.ext.NorwegianStemmer;
import org.tartarus.snowball.ext.PortugueseStemmer;
import org.tartarus.snowball.ext.RomanianStemmer;
import org.tartarus.snowball.ext.RussianStemmer;
import org.tartarus.snowball.ext.SpanishStemmer;
import org.tartarus.snowball.ext.SwedishStemmer;
import org.tartarus.snowball.ext.TurkishStemmer;


public class Stemmer {
	
	// Logging
	static Logger logger = LoggerFactory.getLogger(Stemmer.class);
	
	
	public static StemmerInterface getStemmer(String lang) {
		
        if (lang == null) return newSnowball(null);

        if (lang.equals("ar")) return new LuceneArabicStemmer();
        if (lang.equals("ca")) return newApostrophe(new CatalanStemmer());
        if (lang.equals("cs")) return new LuceneCzechStemmer();
        if (lang.equals("da")) return newSnowball(new DanishStemmer());
        if (lang.equals("de")) return newSnowball(new GermanStemmer());
        if (lang.equals("en")) return newSnowball(new EnglishStemmer());
        if (lang.equals("es")) return newSnowball(new SpanishStemmer());
        if (lang.equals("fa")) return new LucenePersianStemmer();
        if (lang.equals("fi")) return newSnowball(new FinnishStemmer());
        if (lang.equals("fr")) return newApostrophe(new FrenchStemmer());
        if (lang.equals("hu")) return newSnowball(new HungarianStemmer());
        if (lang.equals("it")) return newApostrophe(new ItalianStemmer());
        if (lang.equals("nl")) return newSnowball(new DutchStemmer());
        if (lang.equals("nn")) return newSnowball(new NorwegianStemmer());
        if (lang.equals("pt")) return newApostrophe(new PortugueseStemmer());
        if (lang.equals("ro")) return newSnowball(new RomanianStemmer());
        if (lang.equals("ru")) return newSnowball(new RussianStemmer());
        if (lang.equals("sv")) return newSnowball(new SwedishStemmer());
        if (lang.equals("tr")) return newSnowball(new TurkishStemmer());

        return newSnowball(null);
    }

    public static SnowballStemmer newSnowball(SnowballProgram p) {
        return new SnowballStemmer(p);
    }
    
    public static SnowballStemmer newApostrophe(SnowballProgram p) {
        return new ApostropheStemmer(p);
    }
	
    public static class SnowballStemmer implements StemmerInterface {
        private final SnowballProgram stemmer;

        public SnowballStemmer(SnowballProgram sb) {
            stemmer = sb;
        }

        @Override
        public String getStem(String term) {
            if (stemmer != null) {
                stemmer.setCurrent(term);
                stemmer.stem();
                return stemmer.getCurrent();
            }
            else return term;
        }
    }
    
    public static class ApostropheStemmer extends SnowballStemmer {
        private static final String[][] prefixes = {
                {"l", "c", "d", "j", "m", "n", "s", "t", "d"},
                {"qu", "un", "gl", "li", "se", "te", "la"},
                {"san", "all", "dev"},
                {"sull", "bell", "sant", "nell", "dell", "fors", "tutt",
                    "mezz", "molt", "diss", "buon", "cent"},
                {"quant", "quest", "grand"}
        };

        public ApostropheStemmer(SnowballProgram sb) {
            super(sb);
        }

        private static boolean hasPrefix(String term, int prefixLength) {
            String prefix = term.substring(0, prefixLength).toLowerCase();
            String[] prefixesWithLength = prefixes[prefixLength - 1];

            for (int i = 0; i < prefixesWithLength.length; i++) {
                if (prefix.equals(prefixesWithLength[i]))
                    return true;
            }

            return false;
        }

        public static final String removePrefix(String input) {
            String term = input;

            if (input.contains("'")) {
                int maxPrefix = Math.min(prefixes.length + 1, input.length());
                int prefixLength = 0;

                for (int i = 1; i < maxPrefix; i++) {
                    if (input.charAt(i) == '\'' && hasPrefix(term, i))
                        prefixLength = i + 1;
                }

                if (prefixLength > 0)
                    term = input.substring(prefixLength);
            }

            return term;
        }

        @Override
        public String getStem(String input) {
            String term = removePrefix(input);
            return super.getStem(term);
        }
    }
	
    public static class LuceneArabicStemmer implements StemmerInterface {
        private ArabicStemmer stemmer;
        private ArabicNormalizer normalizer;

        public LuceneArabicStemmer() {
            stemmer = new ArabicStemmer();
            normalizer = new ArabicNormalizer();
        }

        @Override
        public String getStem(String term) {
            char[] buf = term.toCharArray();
            int normLen = normalizer.normalize(buf, buf.length);
            int stemLen = stemmer.stem(buf, normLen);
            return new String(buf, 0, stemLen);
        }
    }
    
    public static class LuceneCzechStemmer implements StemmerInterface {
        private CzechStemmer stemmer;

        public LuceneCzechStemmer() {
            stemmer = new CzechStemmer();
        }

        @Override
        public String getStem(String term) {
            char[] buf = term.toCharArray();
            int stemLen = stemmer.stem(buf, buf.length);
            return new String(buf, 0, stemLen);
        }
    }

    public static class LucenePersianStemmer implements StemmerInterface {
        private PersianNormalizer normalizer;

        public LucenePersianStemmer() {
            normalizer = new PersianNormalizer();
        }

        @Override
        public String getStem(String term) {
            char[] buf = term.toCharArray();
            int normLen = normalizer.normalize(buf, buf.length);
            return new String(buf, 0, normLen);
        }
    }
    
}


