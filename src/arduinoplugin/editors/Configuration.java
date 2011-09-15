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
	private PreProcScanner preProcScanner;
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
				PartitionScanner.COMMENT, PartitionScanner.PREPROC };
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


	/**
	 * Sets which scanners scan where
	 */
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getPreProcScanner());
		reconciler.setDamager(dr, PartitionScanner.PREPROC);
		reconciler.setRepairer(dr, PartitionScanner.PREPROC);

		dr = new DefaultDamagerRepairer(getScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(
				new TextAttribute(colorManager.getColor(IColorConstants.MULTI_LINE_COMMENT)));
		reconciler.setDamager(ndr, PartitionScanner.COMMENT);
		reconciler.setRepairer(ndr, PartitionScanner.COMMENT);

		return reconciler;
	}

	protected PreProcScanner getPreProcScanner() {
		if (preProcScanner == null) {
			preProcScanner = new PreProcScanner(colorManager);
			preProcScanner.setDefaultReturnToken(new Token(new TextAttribute(
					colorManager.getColor(IColorConstants.PREPROC_INSTRUCTION))));
		}
		return preProcScanner;
	}

}