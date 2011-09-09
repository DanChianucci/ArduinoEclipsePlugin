

package arduinoplugin.editors;

import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.*;


public class Scanner extends RuleBasedScanner {
	/**
	 * scans the regular (default partitions, and adds rules.
	 */
	public Scanner(ColorManager manager) {
		IToken procInstr = new Token(new TextAttribute(
				manager.getColor(IColorConstants.PROC_INSTR)));
		IToken comment = new Token(new TextAttribute(
				manager.getColor(IColorConstants.COMMENT)));
		
		IRule[] rules = new IRule[3];
		// Add rule for processing instructions
		rules[0] = new SingleLineRule("<?", "?>", procInstr);
		
		rules[1] = new SingleLineRule("//",null,comment);
		// Add generic whitespace rule.
		rules[2] = new WhitespaceRule(new WhitespaceDetector());

		setRules(rules);
	}
}
