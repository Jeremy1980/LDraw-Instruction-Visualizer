package ldivisualizer.toolbox;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.memorynotfound.file.OSInfo;

/**
 * Work as singleton. Added enough protection against threads and serialization.
 * The method readResolve() will make sure the only instance will be returned, 
 * even when the object was serialized in a previous run of our program.
 * 
 * @author Members of stackoverflow.com
 * @see https://stackoverflow.com/questions/70689/what-is-an-efficient-way-to-implement-a-singleton-pattern-in-java
 *
 */
public final class IOAssistant implements Serializable {

	public static final String appShortName = "LDIVisualizer"; 
	public static final String appLongName = "LDraw Instruction Visualizer"; 
	public static final String appVersion =  "0.0.31.07";
	
	public static final String prefsFileName = "ldivisualizer.xml";
	
	public static final String KEY_LDRAWPATH_1 = "ldrawpath";
	public static final String KEY_LDRAWPATH_2 = "additionalpath";
	public static final String KEY_IMPORTPATH = "importpath";
	public static final String KEY_RECENTPATHS = "recentpaths";
	public static final String KEY_WINDOWGEOMETRY = "geometry";
	
	public static final FileNameExtensionFilter openFilter = new FileNameExtensionFilter("LDraw models","dat","ldr","mpd","l3b","lcd");
	
	private static final long serialVersionUID = 39503414202748884L;
	public static String modelName = "";
	
	private IOAssistant() {}
	
	// Wrapped in a inner static class so that loaded only when required
    private static class AssistantLoader {

        // And no more fear of threads
        private static final IOAssistant INSTANCE = new IOAssistant();
    }	
    
    public static IOAssistant sharedInstance() {
        return AssistantLoader.INSTANCE;
    }
    
    @SuppressWarnings("unused")
	private IOAssistant readResolve() {
        return AssistantLoader.INSTANCE;
    }    
    
    private static Path checkPath(String subfolder_1, String subfolder_2) {
    	String homeFolder = OSInfo.isWindows() ? System.getenv().get("APPDATA") : System.getProperty("user.home");
    	Path result = Paths.get(homeFolder,appShortName,subfolder_1,subfolder_2);
    	
    	if (!result.toFile().isDirectory()) result.toFile().mkdirs();
    	return result;
    }
    
    public static Path appDataPath() {
    	return checkPath("","");
    }
    
    public static Path modelCachePath() {
    	return checkPath(modelName,"");
    }
    
    public static Path finalImagePath() {
    	return checkPath(modelName,"Final_Images");
    }
    
    public static File getOpenFileName(String dialogTitle, String currentDirectory, int selectionMode) {
    	JFileChooser jfc = new JFileChooser(currentDirectory);
    	jfc.setDialogTitle(dialogTitle);
    	jfc.setFileSelectionMode(selectionMode);
    	jfc.setFileFilter(openFilter);
    	
    	int dialogResponse = jfc.showOpenDialog(null);
    	if (dialogResponse == JFileChooser.APPROVE_OPTION) {
			return jfc.getSelectedFile();
    	}
    	return null;
    }
    
    public static String getFileManager() {
    	if (OSInfo.OS.WINDOWS.equals(OSInfo.getOs()))
    		return "explorer.exe";
    	if (OSInfo.OS.MAC.equals(OSInfo.getOs()))
    		return "/usr/bin/open";
    	return "nautilus";
    }
    
    public static String getfileNameWithOutExt(String fileName) {
    	if (fileName != null)
    		return fileName.replaceFirst("[.][^.]+$", "");
    	return "";
    }
}
