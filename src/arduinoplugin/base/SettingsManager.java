package arduinoplugin.base;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;


public class SettingsManager 
{	
	static IProject p;
	
	static String PluginID = "ArduinoPlugin";
	
	SettingsManager()
	{
	}
	
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
		
		
		
		public static void saveProjectSetting(String key,String value) {
			  // saves plugin preferences at the project level
			  IEclipsePreferences prefs = new ProjectScope(p).getNode(PluginID); 

			  prefs.put(key,value);

			  try {
			    // prefs are automatically flushed during a plugin's "super.stop()".
			    prefs.flush();
			  } catch(BackingStoreException e) {
			    e.printStackTrace();
			  }
			}

			private static String getProjectSetting(String key) {
				 IEclipsePreferences prefs = new ProjectScope(p).getNode(PluginID);
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
					p=project;
				 	setting = getProjectSetting(key);
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
				p = project;
				
				saveProjectSetting(key,value);
				saveWorkspaceSetting(key,value);
				
			}
	
	
	
	
	
}
