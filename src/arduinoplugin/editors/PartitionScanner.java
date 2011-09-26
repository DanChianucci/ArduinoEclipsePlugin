package arduinoplugin.editors;

import org.eclipse.jface.text.rules.*;
/**
 * splits the document up into partitions
 * seems like partition scanner is the multiline stuff
 * 
 */
public class PartitionScanner extends RuleBasedPartitionScanner {
	public final static String COMMENT = "__comment";
	public final static String PREPROC = "__preprocessor";

	public PartitionScanner() {

		IToken comment = new Token(COMMENT);
		IToken preprocessor = new Token(PREPROC);

		IPredicateRule[] rules = new IPredicateRule[2];

		//TODO Multiline comments should turn color when the 
		//opening sequence is detected... not when both are detected.
		rules[0] = new PatternRule("/*", "*/", comment, '\0', false, true);
		rules[1] = new SingleLineRule("#",null,preprocessor);//new TagRule(preprocessor);

		setPredicateRules(rules);
	}
}
