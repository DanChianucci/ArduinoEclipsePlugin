package arduinoplugin.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class Configuration extends SourceViewerConfiguration {
	private DoubleClickStrategy doubleClickStrategy;
	private TagScanner tagScanner;
	private Scanner scanner;
	private ColorManager colorManager;

	/**
	 *tells viewer what  content types their are
	 *sets double click strategy
	 *reconciles the presentation
	 */
	public Configuration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}

	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE,
				PartitionScanner.COMMENT, PartitionScanner.TAG };
	}

	public ITextDoubleClickStrategy getDoubleClickStrategy(
			ISourceViewer sourceViewer, String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new DoubleClickStrategy();
		return doubleClickStrategy;
	}

	protected Scanner getScanner() {
		if (scanner == null) {
			scanner = new Scanner(colorManager);
			scanner.setDefaultReturnToken(new Token(new TextAttribute(
					colorManager.getColor(IColorConstants.DEFAULT))));
		}
		return scanner;
	}

	protected TagScanner getTagScanner() {
		if (tagScanner == null) {
			tagScanner = new TagScanner(colorManager);
			tagScanner.setDefaultReturnToken(new Token(new TextAttribute(
					colorManager.getColor(IColorConstants.TAG))));
		}
		return tagScanner;
	}

	/**
	 * Sets which scanners scan where
	 */
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getTagScanner());
		reconciler.setDamager(dr, PartitionScanner.TAG);
		reconciler.setRepairer(dr, PartitionScanner.TAG);

		dr = new DefaultDamagerRepairer(getScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(
				new TextAttribute(colorManager.getColor(IColorConstants.COMMENT)));
		reconciler.setDamager(ndr, PartitionScanner.COMMENT);
		reconciler.setRepairer(ndr, PartitionScanner.COMMENT);

		return reconciler;
	}

}