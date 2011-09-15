package arduinoplugin.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

import arduinoplugin.base.SettingsManager;

//TODO Doesn't actually save settings, nore does it do any error checking... I would like to make this more 
//integrated with the settings page so as not to have so much copied code

public class ArduinoPropertyPage extends PropertyPage
{
	SettingsPageLayout s = new SettingsPageLayout();

	@Override
	public boolean isValid() 
	{		
		return s.isPageComplete();
	}

	@Override
    protected void performApply() 
	{
        performOk();
    }

    @Override
    public boolean performCancel() {
        return true;
    }

    @Override
    protected void performDefaults() 
    {
    	s.setToDefaults();
        updateApplyButton();
    }

	@Override
	public boolean performOk() {
		if(isValid())
		{
			//TODO get the project
			IProject p = null;
			SettingsManager.saveBothSetting("ArduinoPath",s.getArduinoPath(),p);
			SettingsManager.saveBothSetting("BoardType",s.getBoardType(),p);
			SettingsManager.saveBothSetting("Optimize",s.getOptimizeSetting(),p);
			SettingsManager.saveBothSetting("Frequency",s.getFrequency(),p);
			SettingsManager.saveBothSetting("MCU",s.getProcessor(),p);
			SettingsManager.saveBothSetting("UploadProtocall",s.getUploadProtocall(),p);
			SettingsManager.saveBothSetting("UploadBaud",s.getUploadBaud(),p);
		}
		return isValid();
	}


	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Control createContents(Composite parent) 
	{
		s.draw(parent);
		setControl(parent);
		Dialog.applyDialogFont(parent);	
		Control c = parent;
		return c;
	}
}