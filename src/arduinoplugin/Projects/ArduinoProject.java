package arduinoplugin.Projects;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import arduinoplugin.natures.ArduinoProjectNature;
import org.eclipse.core.runtime.IProgressMonitor;

public class ArduinoProject {
    /**
     * - create the default project
     * - add the custom project nature
     * - create the folder structure
     *
     * @param projectName
     * @param location
     * @param natureId
     * @return
     */
    public static IProject createProject(String projectName, URI location) 
    {
        Assert.isNotNull(projectName);
        Assert.isTrue(projectName.trim().length() > 0);

        IProject project = createBaseProject(projectName, location);
        try 
        {
        	//Adds the Project nature
        	addNature(project,ArduinoProjectNature.NATURE_ID);
       	
        	//TODO add files to the arduinoproject
        	String[] filePaths = {"c:/users/Dan/main.java"};//the files path on disk
        	String[] projectPaths = {projectName+".pde"};//the files path in the project
        	addFiles(project,filePaths,projectPaths);
        	
        	
        } catch (CoreException e) 
        {
        	e.printStackTrace();
        	project = null;
        }

        return project;
    }


	private static void addFiles(IProject project, String[] filePaths, String[] projectPaths) 
			throws CoreException 
	{
        for (int i=0; i < filePaths.length; i++) {
            IFile file = project.getFile(projectPaths[i]);
            try {
				createFile(file,filePaths[i]);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
        }
		
	}


	private static void createFile(IFile file, String path)throws FileNotFoundException, SecurityException, CoreException 
	{
        if (!file.exists()) 
        {
        	FileInputStream stream = new FileInputStream(path);
            file.create(stream, false, null);
        }
	}


	/**
     * Just do the basics: create a basic project.
     *
     * @param location
     * @param projectName
     */
    private static IProject createBaseProject(String projectName, URI location) {
        // it is acceptable to use the ResourcesPlugin class
        IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

        if (!newProject.exists()) {
            URI projectLocation = location;
            IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
            
            if (location != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) 
            {
                projectLocation = null;
            }

            desc.setLocationURI(projectLocation);
            try {
                newProject.create(desc, null);
                if (!newProject.isOpen()) {
                    newProject.open(null);
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }

        return newProject;
    }

//    private static void createFolder(IFolder folder) throws CoreException {
//        IContainer parent = folder.getParent();
//        if (parent instanceof IFolder) {
//            createFolder((IFolder) parent);
//        }
//        if (!folder.exists()) {
//            folder.create(false, true, null);
//        }
//    }

    /**
     * Create a folder structure with a parent root, overlay, and a few child
     * folders.
     *
     * @param newProject
     * @param paths
     * @throws CoreException
     */
//    private static void addToProjectStructure(IProject newProject, String[] paths) throws CoreException {
//        for (String path : paths) {
//            IFolder etcFolders = newProject.getFolder(path);
//            createFolder(etcFolders);
//        }
//    }

    private static void addNature(IProject project, String NatureID) throws CoreException {
        if (!project.hasNature(NatureID)) {
            IProjectDescription description = project.getDescription();
            String[] prevNatures = description.getNatureIds();
            String[] newNatures = new String[prevNatures.length + 1];
            System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
            newNatures[prevNatures.length] = NatureID;
            description.setNatureIds(newNatures);

            IProgressMonitor monitor = null;
            project.setDescription(description, monitor);
        }
    }



	
}


