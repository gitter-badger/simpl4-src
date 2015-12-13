/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
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
