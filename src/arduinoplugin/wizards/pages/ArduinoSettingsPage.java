package arduinoplugin.wizards.pages;

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


public class ArduinoSettingsPage extends WizardPage implements IWizardPage {

	final Shell shell = new Shell();
	
	Text ArduinoPathInput;
	Button BrowseButton;	
	Combo BoardType;
	Combo Optimize;	
	Combo ProcessorCombo;
	Text ProcessorFrequency;	
	Combo UploadProtocall;
	Combo UploadBaud;
    
	private Listener fieldModifyListener = new Listener() {
        public void handleEvent(Event e) 
        {          
            setPageComplete(validatePage());                
        }
    };
    private Listener BoardModifyListener = new Listener(){
    	public void handleEvent(Event e)
    	{
    		 setEditableFields();
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

		// **********************************************************************************
		// ******************************Arduino Environment*********************************
		// **********************************************************************************
		new Label(composite, SWT.NONE).setText("Arduino Location");//TODO Find ArdEnv automatically
		ArduinoPathInput = new Text(composite, SWT.BORDER);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.horizontalSpan = (ncol - 2);
		gd.grabExcessHorizontalSpace = true;
		ArduinoPathInput.setLayoutData(gd);
		ArduinoPathInput.addListener(SWT.Modify, fieldModifyListener);

		BrowseButton = new Button(composite, SWT.NONE);
		BrowseButton.setText("Browse...");
		gd = new GridData();
		gd.horizontalAlignment = SWT.LEAD;
		BrowseButton.setLayoutData(gd);
		BrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String Path = new DirectoryDialog(shell).open();
				ArduinoPathInput.setText(Path);
			}
		});

		// **********************************************************************************
		// *************************************BoardType************************************
		// **********************************************************************************
		new Label(composite, SWT.NONE).setText("Board:");
		BoardType = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		BoardType.setLayoutData(gd);
		String[] Boards = PluginBase.getBoardsArray();
		BoardType.setItems(Boards);
		BoardType.setText(Boards[0]);//TODO set to whatever was used last time;
		BoardType.addListener(SWT.Modify,BoardModifyListener);
		
		// **********************************************************************************
		// ********************************Optimization*************************************
		// **********************************************************************************
		new Label(composite, SWT.NONE).setText("Optimization Level:");
		Optimize = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		Optimize.setLayoutData(gd);
		String OptimizeOptions[] = { "0","1", "2", "3", "s" };
		Optimize.setItems(OptimizeOptions);
		Optimize.setText("s");//TODO set to whatever is settings
		
		createLine(composite,ncol);
		
		
		// **********************************************************************************
		// *********************************Processor Type***********************************
		// **********************************************************************************
		
		new Label(composite, SWT.NONE).setText("Processor:");
		ProcessorCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		ProcessorCombo.setLayoutData(gd);
		String Processors[] = PluginBase.getProcessorArray();
		ProcessorCombo.setItems(Processors);//TODO Make better list
		ProcessorCombo.setText(Processors[0]);
		ProcessorCombo.addListener(SWT.Modify,fieldModifyListener);
		
		// **********************************************************************************
		// *********************************Processor Freq***********************************
		// **********************************************************************************
		new Label(composite, SWT.NONE).setText("Processor Frequency (Hz):");//TODO Check Frequency is always a number
		ProcessorFrequency = new Text(composite, SWT.BORDER);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		ProcessorFrequency.setLayoutData(gd);
		ProcessorFrequency.addListener(SWT.Modify, fieldModifyListener);
		
		
		// **********************************************************************************
		// *********************************Upload Protocall*********************************
		// **********************************************************************************
		
		new Label(composite, SWT.NONE).setText("Upload Protocall:");
		UploadProtocall = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		UploadProtocall.setLayoutData(gd);
		String Protocalls[] = {"","STK500v2"};//TODO set actual protocalls
		// Boards = getBoardTypesFromXML();
		UploadProtocall.setItems(Protocalls);
		UploadProtocall.setText(Protocalls[0]);//TODO set to whatever was used last time;
		UploadProtocall.addListener(SWT.Modify,fieldModifyListener);
		
		// **********************************************************************************
		// *********************************Uploader Baud************************************
		// **********************************************************************************
		new Label(composite, SWT.NONE).setText("Board:");
		UploadBaud = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		UploadBaud.setLayoutData(gd);
		String Bauds[] = { "152000","1654322" };//TODO Set actual bauds
		UploadBaud.setItems(Bauds);
		UploadBaud.setText(Bauds[0]);//TODO set to whatever was used last time;
		UploadBaud.addListener(SWT.Modify,fieldModifyListener);		
		
		setEditableFields();
		setPageComplete(validatePage());
		setWarnings();
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	private void setWarnings() 
	{
		setErrorMessage(null);
		setMessage(null);
	}

	private void setEditableFields() 
	{
		boolean editable = getBoardType().equals("Custom");
		ProcessorCombo.setEnabled(editable);
		ProcessorFrequency.setEnabled(editable);
		UploadProtocall.setEnabled(editable);
		UploadBaud.setEnabled(editable);
	}

	private boolean validatePage() 
	{
		boolean valid = true;
		if (getArduinoPath().equals(""))
		{
			valid = false;
		}
		if(getBoardType().equals("Custom"))
		{
			//TODO check everything in custom settings is ok
			valid = getProcessor()!="" && getFrequency()!=""
					&& getUploadProtocall()!=""  && getUploadBaud()!="";
		}
		setWarnings();
		return valid;
	}
	
	private void createLine(Composite parent, int ncol) 
	{
		Label line = new Label(parent, SWT.SEPARATOR|SWT.HORIZONTAL|SWT.BOLD);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = ncol;
		line.setLayoutData(gridData);
	}	
	

	public String getArduinoPath() {
		if (ArduinoPathInput == null)
			return "";
		return ArduinoPathInput.getText().trim();
	}
	public String getBoardType()
	{
		if (BoardType == null)
			return "";
		return BoardType.getText().trim();
	}
	public String getOptimizeSetting(){
		if (Optimize == null)
			return "";
		return Optimize.getText().trim();
	}
	public String getProcessor(){
		if (ProcessorCombo == null)
			return "";
		return ProcessorCombo.getText().trim();
	}
	public String getFrequency(){
		if (ProcessorFrequency == null)
			return "";
		return ProcessorFrequency.getText().trim();
	}
	public String getUploadProtocall()
	{
		if (UploadProtocall == null)
			return "";
		return UploadProtocall.getText().trim();
	}
	public String getUploadBaud(){
		if (UploadBaud == null)
			return "";
		return UploadBaud.getText().trim();
	}

}
