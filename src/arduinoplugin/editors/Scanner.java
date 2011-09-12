

package arduinoplugin.editors;

import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.*;


public class Scanner extends RuleBasedScanner {
	/**
	 * scans the regular (default partitions, and adds rules.
	 */
	public Scanner(ColorManager manager) {
		IToken comment = new Token(new TextAttribute(
				manager.getColor(IColorConstants.COMMENT)));
		
		IRule[] rules = new IRule[2];
		rules[0] = new SingleLineRule("//",null,comment);
		// Add generic whitespace rule.
		rules[1] = new WhitespaceRule(new WhitespaceDetector());

		setRules(rules);
	}
}
