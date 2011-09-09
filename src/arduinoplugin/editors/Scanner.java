

package arduinoplugin.editors;

import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.*;

/**
 * Adds rules for processor Instructions and whitespace
 * singleline stuff
 */
public class Scanner extends RuleBasedScanner {

	public Scanner(ColorManager manager) {
		IToken procInstr = new Token(new TextAttribute(
				manager.getColor(IColorConstants.PROC_INSTR)));

		IRule[] rules = new IRule[2];
		// Add rule for processing instructions
		rules[0] = new SingleLineRule("<?", "?>", procInstr);
		// Add generic whitespace rule.
		rules[1] = new WhitespaceRule(new WhitespaceDetector());

		setRules(rules);
	}
}
