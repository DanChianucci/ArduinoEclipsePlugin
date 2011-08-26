package arduinoplugin.natures;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import arduinoplugin.builders.ArduinoProjectBuilder;

public class ArduinoProjectNature implements IProjectNature {

	public static final String NATURE_ID = "arduinoplugin.ProjectNature";
	public IProject project;
	
	public ArduinoProjectNature() {
		//Auto-generated constructor stub
	}

	@Override
	public void configure() throws CoreException 
	{
		addBuilder(ArduinoProjectBuilder.BUILDER_ID);
	}

	@Override
	public void deconfigure() throws CoreException 
	{
		removeBuilder(ArduinoProjectBuilder.BUILDER_ID);
	}

	private void removeBuilder(String builderId) throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderId)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i,
						commands.length - i - 1);
				description.setBuildSpec(newCommands);
				project.setDescription(description, null);			
				return;
			}
		}
		
	}

	@Override
	public IProject getProject() 
	{
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project=project;

	}
	
    private void addBuilder(String id) throws CoreException {
        IProjectDescription desc = getProject().getDescription();
        ICommand[] commands = desc.getBuildSpec();
        for (int i = 0; i < commands.length; ++i)
           if (commands[i].getBuilderName().equals(id))
              return;
        //add builder to project
        ICommand command = desc.newCommand();
        command.setBuilderName(id);
        ICommand[] nc = new ICommand[commands.length + 1];
        // Add it before other builders.
        System.arraycopy(commands, 0, nc, 1, commands.length);
        nc[0] = command;
        desc.setBuildSpec(nc);
        project.setDescription(desc, null);
     }
	


}
