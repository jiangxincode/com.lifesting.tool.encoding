package com.lifesting.tool.encoding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.lifesting.tool.encoding";
	public static final String ALL_ENCODING="all";
	public final static List<String> ENCODINGS = Arrays.asList(new String[] { "GBK",
			"GB18030", "GB2312", "UTF-8", "ISO-8859-1", "US-ASCII", "UTF-16",
			"UTF-16BE", "UTF-16LE",
	});
	public final static List<String> FROM_ENCODINGS = Arrays.asList(new String[] { ALL_ENCODING,"GBK",
			"GB18030", "GB2312", "UTF-8", "ISO-8859-1", "US-ASCII", "UTF-16",
			"UTF-16BE", "UTF-16LE",
	});
	private static int suffix_counter = 0;
	public static final String FILE = "setting.dat123456";
	public static final String DUMP = "DUMP";
	// The shared instance
	private static Activator plugin;
	public static void logException(Exception e)
	{
		IStatus status = new Status(IStatus.ERROR,PLUGIN_ID,e.getLocalizedMessage(),e);
		getDefault().getLog().log(status);
	}
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	public static Setting newDefaultSetting()
	{
		suffix_counter++;
		return new Setting("s"+suffix_counter,"ISO-8859-1","GBK",true);
	}
	public static synchronized void loadSetting(String projectName,List<Setting> settings)
	{
		File file =getDefault().getStateLocation().append(projectName+"_"+FILE).toFile();
		if (file.exists())
		{
			ObjectInputStream input = null ;
			try {
				input = new ObjectInputStream(new FileInputStream(file));
				Object o;
				while (!(o= input.readObject()).equals(DUMP))
				{
					settings.add((Setting) o);
				}
			} catch (FileNotFoundException e) {
				Activator.logException(e);
			} catch (IOException e) {
				Activator.logException(e);
			} catch (ClassNotFoundException e) {
				Activator.logException(e);
			}
			finally
			{
				if (input != null)
					try {
						input.close();
					} catch (IOException e) {
						Activator.logException(e);
					}
			}
		}
	}
}
