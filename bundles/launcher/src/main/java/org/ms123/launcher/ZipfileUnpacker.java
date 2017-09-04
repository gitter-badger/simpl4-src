/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2017] [Manfred Sattler] <manfred@ms123.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ms123.launcher;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import java.net.URI;
import java.io.File;
import java.io.IOException;

public class ZipfileUnpacker {

	private final FileSystemManager fileSystemManager;

	private final URI packLocation;

	public ZipfileUnpacker(final URI packLocation) throws FileSystemException {
		this.fileSystemManager = VFS.getManager();
		this.packLocation = packLocation;
	}

	public void unpack(final File outputDir) throws IOException {
		outputDir.mkdirs();
		final FileObject packFileObject = fileSystemManager.resolveFile(packLocation.toString());
		try {
			final FileObject zipFileSystem = fileSystemManager.createFileSystem(packFileObject);
			try {
				fileSystemManager.toFileObject(outputDir).copyFrom(zipFileSystem, new AllFileSelector());
			} finally {
				AbstractFileSystem fs = (AbstractFileSystem)zipFileSystem.getFileSystem();
				zipFileSystem.close();
				fs.close();
			}
		} finally {
			packFileObject.close();
		}
	}
}
