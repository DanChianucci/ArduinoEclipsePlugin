package arduinoplugin.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.swt.widgets.Button;

import arduinoplugin.base.SettingsManager;
import arduinoplugin.natures.ArduinoProjectNature;

//TODO Doesn't actually save settings, nore does it do any error checking... 

public class ArduinoPropertyPage extends PropertyPage
{
	SettingsPageLayout s = new SettingsPageLayout();
	
	private Listener completeListener = new Listener() {
		public void handleEvent(Event e) 
		{
			boolean valid = s.isPageComplete();
			Button b = getApplyButton();
			b.setVisible(valid);			
			setValid(valid);
		}
	};

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
			//TODO get the project from a selection
			IProject p = selectProject();
			SettingsManager.saveBothSetting(SettingKeys.ArduinoPathKey,s.getArduinoPath(),p);
			SettingsManager.saveBothSetting(SettingKeys.BoardTypeKey,s.getBoardType(),p);
			SettingsManager.saveBothSetting(SettingKeys.OptimizeKey,s.getOptimizeSetting(),p);
			SettingsManager.saveBothSetting(SettingKeys.FrequencyKey,s.getFrequency(),p);
			SettingsManager.saveBothSetting(SettingKeys.ProcessorTypeKey,s.getProcessor(),p);
			SettingsManager.saveBothSetting(SettingKeys.UploadProtocolKey,s.getUploadProtocall(),p);
			SettingsManager.saveBothSetting(SettingKeys.UploadSpeedKey,s.getUploadBaud(),p);
			SettingsManager.saveWorkspaceSetting(SettingKeys.ProgrammerKey, s.getUploadUsing());
			SettingsManager.saveWorkspaceSetting(SettingKeys.UploadPort, s.getUploadPort());
		}
		return isValid();
	}

	private IProject selectProject(){
		for(IProject p :ResourcesPlugin.getWorkspace().getRoot().getProjects())
		{
			try {
				if( p.hasNature(ArduinoProjectNature.NATURE_ID));
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				return p;
		}
		return null;
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
		Composite composite = new Composite(parent, SWT.NULL);
		s.draw(composite);
		setControl(parent);
		Dialog.applyDialogFont(parent);
		s.cb.addListener(SWT.Selection, completeListener);
		Control c = parent;
		return c;
	}
}