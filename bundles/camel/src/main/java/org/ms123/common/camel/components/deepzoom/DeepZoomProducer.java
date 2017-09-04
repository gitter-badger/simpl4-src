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
import static org.apache.commons.lang3.StringUtils.isEmpty;
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
		if( isEmpty(vfsRoot) ) vfsRoot = "p.vfsroot";
		if( isEmpty(pdfFile) ) pdfFile = "p.pathname";
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

