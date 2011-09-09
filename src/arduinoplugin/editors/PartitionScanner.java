package arduinoplugin.editors;

import org.eclipse.jface.text.rules.*;
/**
 * adds rules for multiline comments and tag rule
 * seems like partition scanner is the multiline stuff
 * */
public class PartitionScanner extends RuleBasedPartitionScanner {
	public final static String COMMENT = "__comment";
	public final static String TAG = "__tag";

	public PartitionScanner() {

		IToken comment = new Token(COMMENT);
		IToken tag = new Token(TAG);

		IPredicateRule[] rules = new IPredicateRule[2];

		rules[0] = new MultiLineRule("/*", "*/", comment);
		rules[1] = new TagRule(tag);

		setPredicateRules(rules);
	}
}
