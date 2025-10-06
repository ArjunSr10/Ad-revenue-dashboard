package main.dash.event;

import main.dash.enums.FileType;

/**
 * File Listener
 */
public interface FileListener {
	/**
	 * a callback when file changed
	 * @param path the changed file path
	 * @param fileType the type of file
	 */
		void fileChanged(String path, FileType fileType);
}
