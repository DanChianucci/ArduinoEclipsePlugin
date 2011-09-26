package arduinoplugin.base;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;


public class SettingsManager 
{	
	
	static String PluginID = "ArduinoPlugin";
	
	
	public static void saveWorkspaceSetting(String key,String value) {
		  // saves plugin preferences at the workspace level
		  IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(PluginID); // does all the above behind the scenes
		  
		  prefs.put(key,value);

		  try {
		    // prefs are automatically flushed during a plugin's "super.stop()".
		    prefs.flush();
		  } catch(BackingStoreException e) {
		    e.printStackTrace();
		  }
		}
	

		private static String getWorkspaceSetting(String key) {
			 IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(PluginID);
		  try {
			prefs.sync();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		  return prefs.get(key, null);
		}
		
		
		
		public static void saveProjectSetting(String key,String value,IProject project) {
			  // saves plugin preferences at the project level
			  IEclipsePreferences prefs = new ProjectScope(project).getNode(PluginID); 

			  prefs.put(key,value);

			  try {
			    // prefs are automatically flushed during a plugin's "super.stop()".
			    prefs.flush();
			  } catch(BackingStoreException e) {
			    e.printStackTrace();
			  }
			}

			private static String getProjectSetting(String key,IProject project) {
				 IEclipsePreferences prefs = new ProjectScope(project).getNode(PluginID);
			   try {
				prefs.sync();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			} 
			   
			  return prefs.get(key, null);
			}
			
			
			public static String getSetting(String key, IProject project) 
			{
				String setting = null;
				
				//get the project setting
				if(project != null)
				{
				 	setting = getProjectSetting(key,project);
				}
				
				//if project setting is missing or project is not specified, get the workspace setting
				if(setting == null || project==null)
				{
					setting = getWorkspaceSetting(key);
				}
				
				return setting;
			}

			public static void saveBothSetting(String key, String value, IProject project) 
			{
				if(project != null)
					saveProjectSetting(key,value,project);
				saveWorkspaceSetting(key,value);
				
			}
	
	
	
	
	
}
