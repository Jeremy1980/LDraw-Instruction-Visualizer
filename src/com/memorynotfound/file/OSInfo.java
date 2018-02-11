package com.memorynotfound.file;

import java.io.IOException;
import java.util.Locale;

public class OSInfo {
    public enum OS {
        WINDOWS,
        UNIX,
        POSIX_UNIX,
        MAC,
        OTHER;

        private String version;
		private String architecture;

        public String getVersion() {
            return version;
        }
        
        public String getArchitecture() {
        	return architecture;
        }

        public void setVersion(String version) {
            this.version = version;
        }

		public void setArchitecture(String architecture) {
			this.architecture = architecture;
		}
    }
    
    private static OS os = OS.OTHER;
    
    static {
        try {
            String osName = System.getProperty("os.name");
            if (osName == null) {
                throw new IOException("os.name not found");
            }
            osName = osName.toLowerCase(Locale.ENGLISH);
            if (osName.contains("windows")) {
                os = OS.WINDOWS;
            } else if (osName.contains("linux")
                    || osName.contains("mpe/ix")
                    || osName.contains("freebsd")
                    || osName.contains("irix")
                    || osName.contains("digital unix")
                    || osName.contains("unix")) {
                os = OS.UNIX;
            } else if (osName.contains("mac os")) {
                os = OS.MAC;
            } else if (osName.contains("sun os")
                    || osName.contains("sunos")
                    || osName.contains("solaris")) {
                os = OS.POSIX_UNIX;
            } else if (osName.contains("hp-ux") 
                    || osName.contains("aix")) {
                os = OS.POSIX_UNIX;
            } else {
                os = OS.OTHER;
            }

        } catch (Exception ex) {
            os = OS.OTHER;
        } finally {
            os.setVersion(System.getProperty("os.version"));
            os.setArchitecture(System.getProperty("os.arch"));
        }
    }

    public static OS getOs() {
        return os;
    }    
    
    public static boolean isWindows() {
    	return OSInfo.OS.WINDOWS.equals(OSInfo.getOs()); 
    }
}
