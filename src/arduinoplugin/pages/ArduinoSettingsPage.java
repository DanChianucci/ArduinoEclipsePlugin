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

	@Override
	public void createControl(Composite parent) 
	{
		Composite composite = new Composite(parent, SWT.NULL);
		spl.draw(composite);
		setControl(composite);
		spl.cb.addListener(SWT.Modify, completeListener);
		
	}

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
