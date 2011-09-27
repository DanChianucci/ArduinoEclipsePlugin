package arduinoplugin.pages;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class ArduinoSettingsPage extends WizardPage implements IWizardPage {

	final Shell shell = new Shell();
	
	private SettingsPageLayout spl= new SettingsPageLayout();

	private Listener completeListener = new Listener() {
		public void handleEvent(Event e) 
		{
			setPageComplete(spl.isPageComplete());
		}
	};

	public ArduinoSettingsPage(String pageName) {
		super(pageName);
		setPageComplete(false);
	}

	public ArduinoSettingsPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setPageComplete(false);
	}

	// Adds buttons and shit to the page
	@Override
	public void createControl(Composite parent) 
	{
		Composite composite = new Composite(parent, SWT.NULL);
		spl.draw(composite);
		setControl(composite);
		spl.cb.addListener(SWT.Modify, completeListener);
		
	}
/*
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);
		GridData gd;
		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 4;
		gl.numColumns = ncol;
		composite.setLayout(gl);

		createLabel(composite, ncol, "Environment Settings"); //$NON-NLS-1$
		// **********************************************************************************
		// ************************** Arduino Environment
		// *********************************
		// **********************************************************************************
		new Label(composite, SWT.NONE).setText("Arduino Location"); //$NON-NLS-1$
		ArduinoPathInput = new Text(composite, SWT.BORDER);
		String a = SettingsManager.getSetting("ArduinoPath", null);
		if (a != null)
			ArduinoPathInput.setText(a);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.horizontalSpan = (ncol - 2);
		gd.grabExcessHorizontalSpace = true;
		ArduinoPathInput.setLayoutData(gd);
		ArduinoPathInput.addListener(SWT.Modify, pathModifyListener);

		BrowseButton = new Button(composite, SWT.NONE);
		BrowseButton.setText("Browse..."); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalAlignment = SWT.LEAD;
		BrowseButton.setLayoutData(gd);
		BrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String Path = new DirectoryDialog(shell).open();
				ArduinoPathInput.setText(Path);
			}
		});

		createLine(composite, ncol);
		createLabel(composite, ncol, "General Settings"); //$NON-NLS-1$

		// **********************************************************************************
		// *************************************BoardType************************************
		// **********************************************************************************
		new Label(composite, SWT.NONE).setText("Board:"); //$NON-NLS-1$
		BoardType = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		BoardType.setLayoutData(gd);
		BoardType.addListener(SWT.Selection, BoardModifyListener);
		BoardType.setEnabled(false);

		// **********************************************************************************
		// ********************************Optimization*************************************
		// **********************************************************************************
		new Label(composite, SWT.NONE).setText("Optimization Level:"); //$NON-NLS-1$
		Optimize = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		Optimize.setLayoutData(gd);
		String OptimizeOptions[] = { "0", "1", "2", "3", "s" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		Optimize.setItems(OptimizeOptions);
		Optimize.setEnabled(false);

		createLine(composite, ncol);
		createLabel(composite, ncol, "Processor Settings"); //$NON-NLS-1$

		// **********************************************************************************
		// ***************************** Processor Type
		// ***********************************
		// **********************************************************************************

		new Label(composite, SWT.NONE).setText("Processor:"); //$NON-NLS-1$
		ProcessorCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		ProcessorCombo.setLayoutData(gd);
		Processors = PluginBase.getProcessorArray();
		ProcessorCombo.setItems(Processors);
		ProcessorCombo.addListener(SWT.Selection, fieldModifyListener);
		ProcessorCombo.setEnabled(false);

		// **********************************************************************************
		// ***************************** ProcessorFreq
		// ***********************************
		// **********************************************************************************
		new Label(composite, SWT.NONE).setText("Processor Frequency (Hz):"); //$NON-NLS-1$
		ProcessorFrequency = new Text(composite, SWT.BORDER);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		ProcessorFrequency.setLayoutData(gd);
		ProcessorFrequency.addListener(SWT.Modify, fieldModifyListener);
		ProcessorFrequency.setEnabled(false);

		createLine(composite, ncol);
		createLabel(composite, ncol, "Upload Settings"); //$NON-NLS-1$

		// **********************************************************************************
		// ***************************** Upload Protocall
		// *********************************
		// **********************************************************************************

		new Label(composite, SWT.NONE).setText("Upload Protocall:"); //$NON-NLS-1$
		UploadProtocall = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		UploadProtocall.setLayoutData(gd);
		String Protocalls[] = { "stk500", "stk500v2" }; //$NON-NLS-1$ //$NON-NLS-2$
		UploadProtocall.setItems(Protocalls);
		UploadProtocall.addListener(SWT.Selection, fieldModifyListener);
		UploadProtocall.setEnabled(false);
		// **********************************************************************************
		// ****************************** Uploader Baud
		// ***********************************
		// **********************************************************************************
		new Label(composite, SWT.NONE).setText("Baud:"); //$NON-NLS-1$
		UploadBaud = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		UploadBaud.setLayoutData(gd);
		String Bauds[] = { "57600", "19200", "115200" };//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
														
		UploadBaud.setItems(Bauds);

		UploadBaud.addListener(SWT.Selection, fieldModifyListener);
		UploadBaud.setEnabled(false);

		// sets which fields can be edited
		if (a != null)// if arduino path setting was found
		{
			// check that a is the correct arduino path
			pathModifyListener.handleEvent(new Event());
			if (arduinoPathIsValid()) {// if it is the correct path
				String lastBoard = SettingsManager
						.getSetting(SettingKeys.BoardTypeKey, null);
				if (lastBoard != null)
					BoardType.setText(lastBoard);
				if (lastBoard != null && lastBoard.equals("Custom")) { //$NON-NLS-1$
					String lastProc = SettingsManager.getSetting(SettingKeys.ProcessorTypeKey, null);
					ProcessorCombo.setText(lastProc);
					String lastFreq = SettingsManager.getSetting(SettingKeys.FrequencyKey,
							null);
					if (lastFreq != null)
						ProcessorFrequency.setText(lastFreq);
					String lastProt = SettingsManager.getSetting(
							SettingKeys.UploadProtocolKey, null);
					if (lastProt != null)
						UploadProtocall.setText(lastProt);
					String lastBaud = SettingsManager.getSetting(SettingKeys.UploadSpeedKey,
							null);
					if (lastBaud != null)
						UploadBaud.setText(lastBaud);
				}
				String opt = SettingsManager.getSetting("Optimize", null); //$NON-NLS-1$
				if (opt != null)
					Optimize.setText(opt);

			}
		}
		setEditableFields();
		setPageComplete(validatePage());
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	private void createLabel(Composite parent, int ncol, String t) {
		Label line = new Label(parent, SWT.HORIZONTAL | SWT.BOLD);
		line.setText(t);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = ncol;
		line.setLayoutData(gridData);

	}

	private void createLine(Composite parent, int ncol) {
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL
				| SWT.BOLD);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = ncol;
		line.setLayoutData(gridData);
	}*/

	public String getArduinoPath() {
		return spl.getArduinoPath();
	}

	public String getBoardType() {
		return spl.getBoardType();
	}

	public String getFrequency() {
		return spl.getFrequency();
	}

	public String getOptimizeSetting() {
		return spl.getOptimizeSetting();
	}

	public String getProcessor() {
		return spl.getProcessor();
	}

	public String getUploadBaud() {
		return spl.getUploadBaud();
	}

	public String getUploadProtocall() {
		return spl.getUploadProtocall();
	}
}
