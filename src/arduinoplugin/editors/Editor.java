package arduinoplugin.editors;

import org.eclipse.ui.editors.text.TextEditor;

public class Editor extends TextEditor {

	private ColorManager colorManager;

	public Editor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new Configuration(colorManager));
		setDocumentProvider(new DocumentProvider());
	}
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

}
