package arduinoplugin.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "arduinoplugin.wizards.messages"; //$NON-NLS-1$
	public static String NewProjectWizard_pageOne_Description;
	public static String NewProjectWizard_pageOne_Title;
	public static String NewProjectWizard_pageTwo_Description;
	public static String NewProjectWizard_pageTwo_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
