package arduinoplugin.editors;

import org.eclipse.swt.graphics.RGB;

public interface IColorConstants {
    public static final RGB MULTI_LINE_COMMENT = new RGB(128, 0, 0);
    public static final RGB SINGLE_LINE_COMMENT = new RGB(128, 128, 0);
    
    public static final RGB KEYWORD = new RGB(0, 0, 128);
    public static final RGB TYPE = new RGB(0, 0, 128);
    
    public static final RGB STRING = new RGB(0, 128, 0);
    
    public static final RGB DEFAULT = new RGB(0, 0, 0);
    
	public static final RGB PREPROC_INSTRUCTION = new RGB(0,128,128);
	public static final RGB PREPROC_DIR = new RGB(0,128,128);
}