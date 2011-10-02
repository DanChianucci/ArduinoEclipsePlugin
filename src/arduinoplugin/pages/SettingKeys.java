package arduinoplugin.pages;

import org.eclipse.osgi.util.NLS;

public class SettingKeys extends NLS {
	private static final String BUNDLE_NAME = "arduinoplugin.pages.SettingKeys"; //$NON-NLS-1$
	public static String UploadPort;
	public static String ProgrammerProtocolKey;
	public static String ProgrammerSpeedKey;
	public static String ProgrammerKey;
	public static String uploadVerboseKey;
	public static String disableFlushingKey;
	public static String BoardNameKey;
	public static String ArduinoPathKey;
	public static String BoardTypeKey;
	public static String FrequencyKey;
	public static String OptimizeKey;
	public static String ProcessorTypeKey;
	public static String UploadProtocolKey;
	public static String UploadSpeedKey;
	public static String uploadMaxSizeKey;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, SettingKeys.class);
	}

	private SettingKeys() {
	}
}
