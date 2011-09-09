package arduinoplugin.editors;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.rules.*;

public class TagScanner extends RuleBasedScanner {
/**
 * sets rules for strings and chars (probabley put rules for keywords in here*/
	public TagScanner(ColorManager manager) {
		IToken string = new Token(new TextAttribute(
				manager.getColor(IColorConstants.STRING)));

		IRule[] rules = new IRule[3];

		// Add rule for double quotes
		rules[0] = new SingleLineRule("\"", "\"", string, '\\');
		// Add a rule for single quotes
		rules[1] = new SingleLineRule("'", "'", string, '\\');
		// Add generic whitespace rule.
		rules[2] = new WhitespaceRule(new WhitespaceDetector());

		setRules(rules);
	}
}
