/**
 * 
 */
package arduinoplugin.wizards;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import arduinoplugin.Projects.ArduinoProject;
import arduinoplugin.base.SettingsManager;
import arduinoplugin.pages.ArduinoSettingsPage;
import arduinoplugin.pages.SettingKeys;



/**
 * @author Dan
 *
 */
public class NewProjectWizard extends Wizard implements INewWizard, IExecutableExtension {

	
	private WizardNewProjectCreationPage _pageOne;
	private ArduinoSettingsPage _pageTwo;//ArduinoSettingsPage _pageTwo;
	private IConfigurationElement _configurationElement;
	private static final String WIZARD_NAME = "New Arduino Project"; //$NON-NLS-1$
	private static final String PAGE_NAME = "Arduino Project Wizard"; //$NON-NLS-1$
	
	//Constructor
	public NewProjectWizard() 
	{
		setWindowTitle(WIZARD_NAME);
	}	
	
	@Override
	public void addPages() 
	{

	    _pageOne = new WizardNewProjectCreationPage(PAGE_NAME);
	    _pageOne.setTitle(Messages.NewProjectWizard_pageOne_Title);
	    _pageOne.setDescription(Messages.NewProjectWizard_pageOne_Description);
	    
	   _pageTwo = new ArduinoSettingsPage(PAGE_NAME);//new ArduinoSettingsPage(PAGE_NAME);
	   _pageTwo.setTitle(Messages.NewProjectWizard_pageTwo_Title);
	   _pageTwo.setDescription(Messages.NewProjectWizard_pageTwo_Description);
	    

	    addPage(_pageOne);
	    addPage(_pageTwo);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		//Auto-generated method stub

	}
	
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	    _configurationElement = config;
	}


	@Override
	public boolean performFinish() {
		//Create the Project
	    String name = _pageOne.getProjectName();
	    URI location = null;
	    if (!_pageOne.useDefaults()) {
	        location = _pageOne.getLocationURI();
	    } // else location == null
	    ArduinoProject.createProject(name, location);
	    
	    IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	    
	    
	    SettingsManager.saveBothSetting(SettingKeys.ArduinoPathKey,_pageTwo.getArduinoPath(),p);
	    SettingsManager.saveBothSetting(SettingKeys.BoardTypeKey,_pageTwo.getBoardType(),p);
	    SettingsManager.saveBothSetting(SettingKeys.OptimizeKey,_pageTwo.getOptimizeSetting(),p);
	    SettingsManager.saveBothSetting(SettingKeys.FrequencyKey,_pageTwo.getFrequency(),p);
	    SettingsManager.saveBothSetting(SettingKeys.ProcessorTypeKey,_pageTwo.getProcessor(),p);
	    SettingsManager.saveBothSetting(SettingKeys.UploadProtocolKey,_pageTwo.getUploadProtocall(),p);
	    SettingsManager.saveBothSetting(SettingKeys.UploadSpeedKey,_pageTwo.getUploadBaud(),p);	    
	    BasicNewProjectResourceWizard.updatePerspective(_configurationElement);
	    
	    return true;
	}

}


