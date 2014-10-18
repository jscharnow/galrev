package org.js.galleryreview.model.imgaccess;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.IImageMetadata.IImageMetadataItem;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExifExtractor {
	private static final String EXIF_IMAGE_WIDTH = "Exif Image Width";
	private static final String EXIF_IMAGE_LENGTH = "Exif Image Length";
	
	private static Logger logger = LoggerFactory.getLogger(ExifExtractor.class);

	public static ImageMetaData extractMetaData(final File file)
			throws ImageReadException, IOException {
		final IImageMetadata metadata = Sanselan.getMetadata(file);
		ImageMetaData mdata = null;

		if (metadata instanceof JpegImageMetadata) {
			mdata = new ImageMetaData();
			final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			Object tagVal=getTagValue(jpegMetadata,
					ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
			if (null != tagVal){
				LocalDateTime origDT=null;
				if (tagVal instanceof Date){
					Instant instant = Instant.ofEpochMilli(((Date)tagVal).getTime());
					origDT = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
				}else if (tagVal instanceof String){
					try {
					    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy:MM:dd kk:mm:ss");
					    origDT = LocalDateTime.parse(tagVal.toString().trim(), format);
					}
					catch (DateTimeParseException exc) {
						logger.error("Unsupported date time format: " + tagVal);
					    throw exc;
					}
				}
				mdata.setDateTimeOriginal(origDT);
			}
			mdata.setSizeBytes(file.length());

			@SuppressWarnings("unchecked")
			final List<IImageMetadataItem> items = jpegMetadata.getItems();
			for (int i = 0; i < items.size(); i++) {
				final IImageMetadataItem item = items.get(i);
				String desc = item.toString();
				if (desc.startsWith(EXIF_IMAGE_LENGTH)){
					String tmpString = desc.replace(EXIF_IMAGE_LENGTH, "").replace(":", "").trim();
					try{
						int length = Integer.valueOf(tmpString);
						mdata.setHeight(length);
					}catch (NumberFormatException nfe){}
				}
				if (desc.startsWith(EXIF_IMAGE_WIDTH)){
					String tmpString = desc.replace(EXIF_IMAGE_WIDTH, "").replace(":", "").trim();
					try{
						int width = Integer.valueOf(tmpString);
						mdata.setWidth(width);
					}catch (NumberFormatException nfe){}
				}
			}

		}
		return mdata;
	}

	private static Object getTagValue(final JpegImageMetadata jpegMetadata,
			final TagInfo tagInfo) {
		Object value;
		final TiffField field = jpegMetadata.findEXIFValue(tagInfo);
		if (field == null) {
			value = null;
		} else {
			try {
				value = field.getValue();
			} catch (ImageReadException e) {
				value = null;
			}
		}
		return value;
	}

}
