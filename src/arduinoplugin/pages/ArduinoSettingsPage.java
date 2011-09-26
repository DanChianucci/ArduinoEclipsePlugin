package arduinoplugin.pages;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import arduinoplugin.base.PluginBase;
import arduinoplugin.base.SettingsManager;
import arduinoplugin.base.Target;

public class ArduinoSettingsPage extends WizardPage implements IWizardPage {

	final Shell shell = new Shell();

	private Text ArduinoPathInput;
	private Button BrowseButton;
	private Combo BoardType;
	private Combo Optimize;
	private Combo ProcessorCombo;
	private Text ProcessorFrequency;
	private Combo UploadProtocall;
	private Combo UploadBaud;

	private String ArduinoPath;
	private String boardtxtPath;
	private Set<String> Boards = new HashSet<String>();
	private String[] Processors;

	private Listener fieldModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setPageComplete(validatePage());
		}
	};

	private Listener pathModifyListener = new Listener() {
		public void handleEvent(Event e) {
			if (arduinoPathIsValid()) {
				ArduinoPath = ArduinoPathInput.getText();
				boardtxtPath = ArduinoPath + File.separator + "hardware" //$NON-NLS-1$
						+ File.separator + "arduino"; //$NON-NLS-1$
				loadBoards();
			}
			setEditableFields();
			setPageComplete(validatePage());
		}
	};

	private Listener BoardModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setEditableFields();
			setOptionsForBoard();
			setPageComplete(validatePage());
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
	public void createControl(Composite parent) {

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
		// TODO Find ArdEnv automatically
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
		ProcessorCombo.setItems(Processors);// TODO Make better list
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
		// TODO set actual protocalls
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
		String Bauds[] = { "57600", "19200", "115200" };// TODO Set actual //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
														// usable bauds
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
						UploadBaud.setText(lastBaud);// TODO set to whatever was
														// used last time;
				}
				String opt = SettingsManager.getSetting("Optimize", null); //$NON-NLS-1$
				if (opt != null)
					Optimize.setText(opt);// TODO set to whatever is settings

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
	}

	public String getArduinoPath() {
		if (ArduinoPathInput == null)
			return ""; //$NON-NLS-1$
		return ArduinoPathInput.getText().trim();
	}

	public String getBoardType() {
		if (BoardType == null)
			return ""; //$NON-NLS-1$
		return BoardType.getText().trim();
	}

	public String getFrequency() {
		if (ProcessorFrequency == null)
			return ""; //$NON-NLS-1$
		return ProcessorFrequency.getText().trim();
	}

	public String getOptimizeSetting() {
		if (Optimize == null)
			return ""; //$NON-NLS-1$
		return Optimize.getText().trim();
	}

	public String getProcessor() {
		if (ProcessorCombo == null)
			return ""; //$NON-NLS-1$
		return ProcessorCombo.getText().trim();
	}

	public String getUploadBaud() {
		if (UploadBaud == null)
			return ""; //$NON-NLS-1$
		return UploadBaud.getText().trim();
	}

	public String getUploadProtocall() {
		if (UploadProtocall == null)
			return ""; //$NON-NLS-1$
		return UploadProtocall.getText().trim();
	}

	/**
	 * Sets which fields are editable by the user if arduino path isn't valid,
	 * none are editable. else, if board is custom, board settings become
	 * editablle as well
	 */
	private void setEditableFields() {
		boolean e = arduinoPathIsValid();
		BoardType.setEnabled(e);
		Optimize.setEnabled(e);
		if (e)
			setOptionsForBoard();
		boolean f = e && getBoardType().equals("Custom"); //$NON-NLS-1$
		ProcessorCombo.setEnabled(f);
		ProcessorFrequency.setEnabled(f);
		UploadProtocall.setEnabled(f);
		UploadBaud.setEnabled(f);
	}

	private void loadBoards() {
		// PluginBase should always have a valid arduinopath if this is called
		Target t = new Target(new File(boardtxtPath));
		Map<String, Map<String, String>> m = t.getBoards();
		for (String s : m.keySet()) {
			if (s != null)
				Boards.add(m.get(s).get("name")); //$NON-NLS-1$
		}
		Boards.add("Custom"); //$NON-NLS-1$
		String[] sBoards = new String[Boards.size()];
		Boards.toArray(sBoards);
		BoardType.setItems(sBoards);
	}

	/**
	 * Sets the uneditable items for a given board automatically
	 */
	private void setOptionsForBoard() {
		Target t = new Target(new File(boardtxtPath));
		String mapName = t.getBoardNamed(getBoardType());
		Map<String, String> settings = t.getBoardSettings(mapName);
		if (settings != null) {
			ProcessorCombo.setText(settings.get(SettingKeys.ProcessorTypeKey));
			ProcessorFrequency.setText(settings.get(SettingKeys.FrequencyKey));
			UploadBaud.setText(settings.get(SettingKeys.UploadSpeedKey));
			UploadProtocall.setText(settings.get(SettingKeys.UploadProtocolKey));

		}
	}

	private void setWarnings() {
		if (!arduinoPathIsValid())
			setErrorMessage("Arduino Path is not valid"); //$NON-NLS-1$
		setMessage(null);
	}

	/**
	 * Checks that the arduino path is valid, and if all custom fields are
	 * filled in
	 * <p>
	 * Also sets page warnings and errors
	 * 
	 * @return true if the page is valid, and false otherwise
	 */
	private boolean validatePage() {
		boolean valid = true;
		if (!arduinoPathIsValid()) { // check arduino path is correct
			valid = false;
		}
		if (getBoardType().equals("Custom")) { //$NON-NLS-1$
			valid = valid && getProcessor() != "" && getFrequency() != "" //$NON-NLS-1$ //$NON-NLS-2$
					&& getUploadProtocall() != "" && getUploadBaud() != ""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		setWarnings();
		return valid;
	}

	/**
	 * @return true if arduino.exe is found in the arduino path textbox
	 */
	private boolean arduinoPathIsValid() {
		File arduino = new File(ArduinoPathInput.getText(), "arduino.exe"); //$NON-NLS-1$
		return arduino.exists();
	}

}
