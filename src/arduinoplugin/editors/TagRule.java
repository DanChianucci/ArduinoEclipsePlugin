package arduinoplugin.editors;

import org.eclipse.jface.text.rules.*;


public class TagRule extends MultiLineRule {
	/**
	 * sets rule for regular xml tag &lt &gt this is a multiline and so is a
	 * partition??? this is needed beacause &lt? is different from &lt! is diff from
	 * &lt 
	 */
	public TagRule(IToken token) {
		super("<", ">", token);
	}

	protected boolean sequenceDetected(ICharacterScanner scanner,
			char[] sequence, boolean eofAllowed) {
		int c = scanner.read();
		// /if open tag
		if (sequence[0] == '<') {
			if (c == '?') {
				// processing instruction - abort
				scanner.unread();
				return false;
			}
			if (c == '!') {
				scanner.unread();
				// comment - abort
				return false;
			}
		}

		else if (sequence[0] == '>') {
			scanner.unread();
		}

		return super.sequenceDetected(scanner, sequence, eofAllowed);
	}
}
