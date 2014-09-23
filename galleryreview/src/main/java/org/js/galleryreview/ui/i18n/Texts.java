package org.js.galleryreview.ui.i18n;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Texts {
	private static ResourceBundle bundle;
	private static Logger logger = LoggerFactory.getLogger(Texts.class);
	private static HashSet<String> missingKeys = new HashSet<>();
	private static String localeName = "";
	private static MessageFormat formatter;

	public static String getText(String key) {
		return getText(key, (Object[]) null);
	}

	public static String getText(String key, Object... params) {
		String text = key;
		ResourceBundle bundle = getAppBundle();
		if (null != bundle) {
			if (!bundle.containsKey(key)) {
				if (!missingKeys.contains(key)) {
					logger.error("Missing key: " + key);
				}
			} else {
				text = bundle.getString(key);
				formatter.applyPattern(text);
				text = formatter.format(params);
			}
		}
		return text;
	}

	public static void setLocale(String locale) {
		Texts.localeName = locale;
	}

	public static ResourceBundle getAppBundle() {
		if (null == bundle) {
			Locale locale = Locale.getDefault();
			if (null != localeName && !localeName.isEmpty()) {
				if (4 == localeName.length()) {
					locale = new Locale(localeName.substring(0, 2),
							localeName.substring(2));
				} else if (2 == localeName.length()) {
					locale = new Locale(localeName);
				} else {
					throw new RuntimeException("Unsupported locale: "
							+ localeName);
				}
			}
			try {
				bundle = ResourceBundle
						.getBundle("org.js.galleryreview.ui.i18n.apptexts", locale);
				// // TODO: add locale support
				// InputStream inputStream = Texts.class
				// .getResourceAsStream("apptexts.properties");
				//
				// bundle = new PropertyResourceBundle(inputStream);
				// Locale locale = Locale.getDefault();
				// if (localeName != null && !localeName.isEmpty()) {
				// locale = new Locale(localeName);
				// }
				formatter = new MessageFormat("", locale);
			} catch (MissingResourceException e) {

				logger.error("Could not lot i18n bundle", e);
			}
		}
		return bundle;
	}
}
