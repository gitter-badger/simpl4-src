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
package org.ms123.common.camel.components.deepzoom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.imageio.ImageIO;
import java.util.Vector;
import java.util.Iterator;
import org.imgscalr.*;
import org.imgscalr.Scalr.Mode;
import org.imgscalr.Scalr.Method;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.debug;
import static com.jcabi.log.Logger.error;

/**
 *
 */
@SuppressWarnings({"unchecked","deprecation"})
public class DeepZoom {

	static final String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	static final String schemaName = "http://schemas.microsoft.com/deepzoom/2008";

	private Boolean deleteExisting = true;
	private String tileFormat = "png";

	private int tileSize = 256;
	private int tileOverlap = 1;

	public DeepZoom() {
		this(256, 1);
	}

	public DeepZoom(int tileSize, int tileOverlap) {
		this.tileSize = tileSize;
		this.tileOverlap = tileOverlap;
	}

	/**
	 * Process the given image file, producing its Deep Zoom output files
	 * in a subdirectory of the given output directory.
	 * @param inFile the file containing the image
	 * @param outputDir the output directory
	 */
	public void processImageFile(File inFile, File outputDir) throws IOException {
		info(this, "Processing image file:" + inFile);

		String fileName = inFile.getName();
		String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
		String pathWithoutExtension = outputDir + File.separator + nameWithoutExtension;

		BufferedImage image = loadImage(inFile);

		int originalWidth = image.getWidth();
		int originalHeight = image.getHeight();

		double maxDim = Math.max(originalWidth, originalHeight);

		int nLevels = (int) Math.ceil(Math.log(maxDim) / Math.log(2));

		debug(this, "nLevels=" + nLevels);

		// Delete any existing output files and folders for this image

		File descriptor = new File(pathWithoutExtension + ".json");
		if (descriptor.exists()) {
			if (deleteExisting)
				deleteFile(descriptor);
			else
				throw new IOException("File already exists in output dir: " + descriptor);
		}

		File imgDir = new File(pathWithoutExtension);
		if (imgDir.exists()) {
			if (deleteExisting) {
				debug(this, "Deleting directory:" + imgDir);
				deleteDir(imgDir);
			} else {
				throw new IOException("Image directory already exists in output dir: " + imgDir);
			}
		}

		imgDir = createDir(outputDir, nameWithoutExtension);

		double width = originalWidth;
		double height = originalHeight;

		for (int level = nLevels; level >= 0; level--) {
			int nCols = (int) Math.ceil(width / tileSize);
			int nRows = (int) Math.ceil(height / tileSize);
			debug(this, "level=" + level + " w/h=" + width + "/" + height + " cols/rows=" + nCols + "/" + nRows);

			File dir = createDir(imgDir, Integer.toString(level));
			for (int col = 0; col < nCols; col++) {
				for (int row = 0; row < nRows; row++) {
					BufferedImage tile = getTile(image, row, col);
					saveImage(tile, dir + File.separator + col + '_' + row);
				}
			}

			// Scale down image for next level
			width = Math.ceil(width / 2);
			height = Math.ceil(height / 2);
			image = Scalr.resize(image, Method.ULTRA_QUALITY, (int) width, (int) height);
		}

		saveImageDescriptor(originalWidth, originalHeight, descriptor);
	}

	/**
	 * Delete a file
	 * @param path the path of the directory to be deleted
	 */
	private void deleteFile(File file) throws IOException {
		if (!file.delete())
			throw new IOException("Failed to delete file: " + file);
	}

	/**
	 * Recursively deletes a directory
	 * @param path the path of the directory to be deleted
	 */
	private void deleteDir(File dir) throws IOException {
		if (!dir.isDirectory())
			deleteFile(dir);
		else {
			for (File file : dir.listFiles()) {
				if (file.isDirectory())
					deleteDir(file);
				else
					deleteFile(file);
			}
			if (!dir.delete())
				throw new IOException("Failed to delete directory: " + dir);
		}
	}

	/**
	 * Creates a directory
	 * @param parent the parent directory for the new directory
	 * @param name the new directory name
	 */
	private File createDir(File parent, String name) throws IOException {
		assert (parent.isDirectory());
		File result = new File(parent + File.separator + name);
		if (!result.mkdir())
			throw new IOException("Unable to create directory: " + result);
		return result;
	}

	/**
	 * Loads image from file
	 * @param file the file containing the image
	 */
	private BufferedImage loadImage(File file) throws IOException {
		BufferedImage result = null;
		try {
			result = ImageIO.read(file);
		} catch (Exception e) {
			throw new IOException("Cannot read image file: " + file);
		}
		return result;
	}

	/**
	 * Gets an image containing the tile at the given row and column
	 * for the given image.
	 * @param img - the input image from whihc the tile is taken
	 * @param row - the tile's row (i.e. y) index
	 * @param col - the tile's column (i.e. x) index
	 */
	private BufferedImage getTile(BufferedImage img, int row, int col) {
		int x = col * tileSize - (col == 0 ? 0 : tileOverlap);
		int y = row * tileSize - (row == 0 ? 0 : tileOverlap);
		int w = tileSize + (col == 0 ? 1 : 2) * tileOverlap;
		int h = tileSize + (row == 0 ? 1 : 2) * tileOverlap;

		if (x + w > img.getWidth()) {
			w = img.getWidth() - x;
		}
		if (y + h > img.getHeight()) {
			h = img.getHeight() - y;
		}

		debug(this, "getTile: row=" + row + ", col=" + col + ", x=" + x + ", y=" + y + ", w=" + w + ", h=" + h);

		assert (w > 0);
		assert (h > 0);

		BufferedImage result = new BufferedImage(w, h, img.getType());
		Graphics2D g = result.createGraphics();
		g.drawImage(img, 0, 0, w, h, x, y, x + w, y + h, null);

		return result;
	}

	/**
	 * Returns resized image
	 * NB - useful reference on high quality image resizing can be found here:
	 *   http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
	 * @param width the required width
	 * @param height the frequired height
	 * @param img the image to be resized
	 */
	private BufferedImage resizeImage(BufferedImage img, double width, double height) {
		int w = (int) width;
		int h = (int) height;
		BufferedImage result = new BufferedImage(w, h, img.getType());
		Graphics2D g = result.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(img, 0, 0, w, h, 0, 0, img.getWidth(), img.getHeight(), null);
		return result;
	}

	/**
	 * Saves image to the given file
	 * @param img the image to be saved
	 * @param path the path of the file to which it is saved (less the extension)
	 */
	private void saveImage(BufferedImage img, String path) throws IOException {
		File outputFile = new File(path + "." + tileFormat);
		try {
			ImageIO.write(img, tileFormat, outputFile);
		} catch (IOException e) {
			throw new IOException("Unable to save image file: " + outputFile);
		}
	}

	/**
	 * Write image descriptor XML file
	 * @param width image width
	 * @param height image height
	 * @param file the file to which it is saved
	 */
	private void _saveImageDescriptor(int width, int height, File file) throws IOException {
		Vector lines = new Vector();
		lines.add(xmlHeader);
		lines.add("<Image TileSize=\"" + tileSize + "\" Overlap=\"" + tileOverlap + "\" Format=\"" + tileFormat + "\" ServerFormat=\"Default\" xmlns=\"" + schemaName + "\">");
		lines.add("<Size Width=\"" + width + "\" Height=\"" + height + "\" />");
		lines.add("</Image>");
		saveText(lines, file);
	}

	private void saveImageDescriptor(int width, int height, File file) throws IOException {
		Vector lines = new Vector();
		lines.add("{");
		lines.add("\t\"tilesize\":" + tileSize + ",");
		lines.add("\t\"overlap\":" + tileOverlap + ",");
		lines.add("\t\"format\":\"" + tileFormat + "\",");
		lines.add("\t\"width\":" + width + ",");
		lines.add("\t\"height\":" + height);
		lines.add("}");
		saveText(lines, file);
	}

	/**
	 * Saves strings as text to the given file
	 * @param lines the image to be saved
	 * @param file the file to which it is saved
	 */
	private void saveText(Vector lines, File file) throws IOException {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			PrintStream ps = new PrintStream(fos);
			for (int i = 0; i < lines.size(); i++)
				ps.println((String) lines.elementAt(i));
		} catch (IOException e) {
			throw new IOException("Unable to write to text file: " + file);
		}
	}

}

