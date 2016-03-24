package com.lifesting.tool.encoding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search2.internal.ui.SearchView;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class SearchViewSetAction implements IViewActionDelegate {

	private SearchView search;
	private Map<String, List<Setting>> cachedSetting = new HashMap<String,List<Setting>>();
	public SearchViewSetAction() {
		
	}

	public void init(IViewPart view) {
		search = (SearchView) view;
	}

	public void run(IAction action) {
		ISearchResult result = search.getCurrentSearchResult();
		if(result instanceof AbstractTextSearchResult)
		{
			AbstractTextSearchResult n_result = (AbstractTextSearchResult) result;
			Object[] elements = n_result.getElements();
			processElements(elements);
			
		}
	}

	private void processElements(final Object[] elements) {
		if(elements.length > 0)
		{
			try {
				search.getProgressService().run(true, true, new IRunnableWithProgress(){

					public void run(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Process setting encoding", elements.length);
						for (Object o : elements)
						{
							if (monitor.isCanceled())
								return;
							
							if (o instanceof IFile)
							{
								IFile file = (IFile) o;
								monitor.subTask("Process file "+file.getName());
								ChangeEncoding(file,monitor);
							}
						}
						monitor.done();
					}});
			} catch (InvocationTargetException e) {
				Activator.logException(e);
			} catch (InterruptedException e) {
				Activator.logException(e);
			}
		}
	}

	protected void ChangeEncoding(IFile file, IProgressMonitor monitor) {
		String project_name = file.getProject().getName();
		List<Setting> proj_settings = cachedSetting.get(project_name);
		if (proj_settings == null)
		{
			proj_settings = new ArrayList<Setting>();
			Activator.loadSetting(project_name,proj_settings);
			cachedSetting.put(project_name,proj_settings);
		}
		
		String suffix = file.getFileExtension();
		if (suffix != null && !file.isLinked() && ! file.isPhantom())
		{
			for (Setting setting : proj_settings)
			{
				if (setting.getSuffix().equals(suffix))
				{
					try {
						String encoding = file.getCharset();
						if (setting.getCurrentEncoding().equals(encoding) || setting.getCurrentEncoding().equals(Activator.ALL_ENCODING)) //change
						{	
							//文件内容需要转码，类似一个拷贝/设定/粘贴的过程
							if (setting.isConvertCharacter() && !file.isReadOnly())
							{
								InputStream inputstream = file.getContents();
								IFileStore store = EFS.getStore(file.getLocationURI());
								int file_size = (int) store.fetchInfo().getLength();
								byte[] buffer = new byte[file_size];
								inputstream.read(buffer);
								//文件的内容
								String orignal = new String(buffer,encoding);
								//按新编码转换后的内容
								ByteArrayInputStream byte_input = new ByteArrayInputStream(orignal.getBytes(setting.getConvertedEncoding()));
								//写入Eclipse文件
								file.setContents(byte_input,IFile.FORCE,monitor);
							}
							//设置新编码
							file.setCharset(setting.getConvertedEncoding(), monitor);
						}
					} catch (CoreException e) {
						Activator.logException(e);
					} catch (IOException e) {
						Activator.logException(e);
					}
					break;
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		
	}

}
