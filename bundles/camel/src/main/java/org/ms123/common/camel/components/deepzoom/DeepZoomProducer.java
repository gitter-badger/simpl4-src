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
package org.ms123.common.camel.components.deepzoom;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultAsyncProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.ms123.common.camel.api.ExchangeUtils;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.info;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getFullPath;

@SuppressWarnings({ "unchecked", "deprecation" })
public class DeepZoomProducer extends DefaultAsyncProducer {

	public DeepZoomProducer(DeepZoomEndpoint endpoint) {
		super(endpoint);
	}

	@Override
	public DeepZoomEndpoint getEndpoint() {
		return (DeepZoomEndpoint) super.getEndpoint();
	}

	@Override
	public boolean process(Exchange exchange, AsyncCallback callback) {
		String pdfFile = getEndpoint().getPdfFile();
		String outdir = getEndpoint().getOutputDirectory();
		String vfsRoot = getEndpoint().getVfsRoot();
		String regex = getEndpoint().getHotspotRegex();
		double factor = getEndpoint().getFactor();
		pdfFile = ExchangeUtils.getParameter(pdfFile, exchange, String.class, "pdffile");
		outdir = ExchangeUtils.getParameter(outdir, exchange, String.class, "outputdirectory");
		vfsRoot = ExchangeUtils.getParameter(vfsRoot, exchange, String.class, "vfsroot");
		info(this, "VfsRoot:" + vfsRoot);

		outdir = Paths.get(vfsRoot, outdir).toString();
		info(this, "Outdir:" + outdir);
		File out = new File(outdir);
		if (!out.exists()) {
			out.mkdirs();
		}
		pdfFile = Paths.get(vfsRoot, pdfFile).toString();
		String pngFile = Paths.get(getFullPath(pdfFile), getBaseName(pdfFile) + ".png").toString();

		try {
			createImage(pdfFile, pngFile, 450);
		} catch (Exception e) {
			error(this, "process.error:%[exception]s", e);
			throw new RuntimeException("DeepZoomProducer.createPng", e);
		}

		DeepZoom dz = new DeepZoom();
		try {
			dz.processImageFile(new File(pngFile), new File(outdir));
		} catch (Exception e) {
			error(this, "process.error:%[exception]s", e);
			throw new RuntimeException("DeepZoomProducer.createImages", e);
		}

		HotspotCreator hc = new HotspotCreator();
		try {
			hc.process(pdfFile, outdir, regex, factor);
		} catch (Exception e) {
			error(this, "process.error:%[exception]s", e);
			throw new RuntimeException("DeepZoomProducer.createHotspots", e);
		}
		info(this, "Ready:" + pdfFile);
		return true;
	}

	private void createImage(String pdfFile, String pngFile, int dpi) throws Exception {
		info(this, "createImage:" + pdfFile + " -> " + pngFile);
		PDDocument document = PDDocument.load(new File(pdfFile));
		PDFRenderer renderer = new PDFRenderer(document);
		BufferedImage image = renderer.renderImageWithDPI(0, dpi, ImageType.GRAY);
		document.close();
		ImageIOUtil.writeImage(image, pngFile, dpi);
	}

}

