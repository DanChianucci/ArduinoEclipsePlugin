package arduinoplugin.editors;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.rules.*;
import org.eclipse.swt.SWT;

public class PreProcScanner extends RuleBasedScanner {
/**
 * scans the preprocessor partitions and adds rules.
 */
	public PreProcScanner(ColorManager manager) {
		IToken preProcessor = new Token(new TextAttribute(manager.getColor(IColorConstants.PROC_INSTR),null,SWT.BOLD));
		IToken dir = new Token(new TextAttribute(manager.getColor(IColorConstants.ProcDir)));
		
		IRule[] rules = new IRule[4];

		//TODO something funky if something is in the preproc part and not within these rules
		rules[0] = new SingleLineRule("#"," ", preProcessor);		
		rules[1] = new SingleLineRule("<",">", dir);
		rules[2] = new SingleLineRule("\"","\"", dir);
		// Add generic whitespace rule.
		rules[3] = new WhitespaceRule(new WhitespaceDetector());

		setRules(rules);
	}
}
