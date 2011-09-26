package arduinoplugin.Toolbars;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import arduinoplugin.Preprocess.Sketch;
import arduinoplugin.builders.RunnerException;
import arduinoplugin.natures.ArduinoProjectNature;
import arduinoplugin.uploader.Serial;
import arduinoplugin.uploader.SerialException;

public class DropDownHandler extends AbstractHandler {
  private static final String PARM_MSG = "arduinoplugin.Toolbar.dropdownmsg";

  public Object execute(ExecutionEvent event) throws ExecutionException 
  {
    String msg = event.getParameter(PARM_MSG);
    if (msg == null) {
      System.out.println("Error");
    } 
    else if(msg.equals("UploadCommand"))
    {
    	for(String i : Serial.list())
    	{
    		System.out.print(i+"\n");
    	}
    	IProject projectToUpload;
		projectToUpload = selectProject();
    	String primaryClass = projectToUpload.getName()+".pde";
    	String buildPath = projectToUpload.getLocation().toOSString()+File.separator+"bin";
    	try {
			Sketch s = new Sketch();
			s.upload(buildPath, primaryClass, true, projectToUpload);
		} catch (RunnerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SerialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    }
    return null;
  }
  
  private IProject selectProject()
  {
	  //make a popup that lists all the projects with arduino nature
	  IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
	  List<IProject> arduinoProjects = new ArrayList<IProject>();
	  for(IProject p :projects)
	  {
		  try {
			if(p.hasNature(ArduinoProjectNature.NATURE_ID));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  	arduinoProjects.add(p);
	  }

	return arduinoProjects.get(0);
  }
}
