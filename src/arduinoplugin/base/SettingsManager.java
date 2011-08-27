package arduinoplugin.base;
import java.io.File;

import org.eclipse.core.resources.IProject;


public class SettingsManager 
{
	
	IProject Project;
	File projectFolder;
	File workspaceFolder;
	
	SettingsManager(IProject p)
	{
		Project=p;
		workspaceFolder = new File(Project.getWorkspace().getRoot().getLocation().toOSString());
		projectFolder = new File(workspaceFolder,Project.getName());
	}
	
	//Read settings as a Map File
	//Set settings as
	
	
	
	
	
}
