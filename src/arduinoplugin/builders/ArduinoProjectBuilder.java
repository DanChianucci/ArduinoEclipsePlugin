package arduinoplugin.builders;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import arduinoplugin.Preprocess.Sketch;
import arduinoplugin.base.PluginBase;

public class ArduinoProjectBuilder extends IncrementalProjectBuilder {

	// /private Compiler comp;

	public static final String BUILDER_ID = "arduinoplugin.builders.ArduinoBuilder";

	public ArduinoProjectBuilder() {
	}

	String outputFolder = "bin";

	private String getOutputPath() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.toOSString()
				+ getProject().getFullPath().makeAbsolute().toOSString()
				+ File.separator + outputFolder;
	}

	private String getPrimaryClass() {
		return getProject().getLocation().toOSString() + File.separator
				+ getProject().getName() + ".pde";
	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args,
			IProgressMonitor monitor) throws CoreException {

		String buildPath = getOutputPath();

		IFolder folder = getProject().getFolder(outputFolder);

		// if the output folder doesn't exist, create it
		if (!folder.exists()) {
			System.out.println("Creating Output Folder");
			folder.create(false, true, null);
		}
		String primaryClass = getPrimaryClass();
		boolean verbose = true;

		try {
			Sketch thisSketch = new Sketch(primaryClass, buildPath);
			thisSketch.build(verbose);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RunnerException e) {
			e.showStackTrace();
			e.printStackTrace();
		}

		return null;

		/*
		 * if (kind == IncrementalProjectBuilder.FULL_BUILD) {
		 * fullBuild(monitor); } else { IResourceDelta delta =
		 * getDelta(getProject()); if (delta == null) { fullBuild(monitor); }
		 * else { incrementalBuild(delta, monitor); } } return null;
		 */
	}

	protected void clean(IProgressMonitor monitor) throws CoreException 
	{
		PluginBase.removeDescendants(new File(getOutputPath()));
	}

	/*
	 * private void incrementalBuild(IResourceDelta delta, IProgressMonitor
	 * monitor) { System.out.println("incremental build on " + delta); try {
	 * delta.accept(new IResourceDeltaVisitor() { public boolean
	 * visit(IResourceDelta delta) { System.out.println("changed: " +
	 * delta.getResource().getRawLocation()); return true; // visit children too
	 * } }); } catch (CoreException e) { e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * private void fullBuild(IProgressMonitor monitor) {
	 * System.out.println("Full Build"); }
	 */

}
