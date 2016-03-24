package com.lifesting.tool.encoding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;

public class SetEncodingJob extends Job {

	private IStructuredSelection selection;
	private List<Setting> settings;
	class ChangeEncodingVisitor implements IResourceVisitor
	{
		private IProgressMonitor monitor;
		public ChangeEncodingVisitor(IProgressMonitor monitor)
		{
			this.monitor = monitor;
		}
		public boolean visit(IResource resource) throws CoreException {
			if (monitor.isCanceled())
				return false;
			return setEncoding(monitor, resource);
		}
		
	}
	public SetEncodingJob(IStructuredSelection selection) {
		super("Setting file encoding");
		this.selection = selection;
		settings = new ArrayList<Setting>();
		loadSetting();
	}

	private void loadSetting() {
		IProject proj = ((IResource)selection.getFirstElement()).getProject();	
		Activator.loadSetting(proj.getName(), settings);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Setting file encoding", IProgressMonitor.UNKNOWN);
		IResource[] resources = new IResource[selection.size()];
		try {
			System.arraycopy(selection.toArray(), 0, resources, 0, selection.size());
			new ResourceTraversal(resources,IResource.DEPTH_INFINITE,0).accept(new ChangeEncodingVisitor(monitor));
		} catch (CoreException e) {
			Activator.logException(e);
		}
		//刷新
		for (IResource s : resources)
		{
			try {
				s.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch (CoreException e) {
				Activator.logException(e);
			}
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	private boolean setEncoding(final IProgressMonitor monitor,IResource res) {
		if (res instanceof IFile)
		{
			IFile file = (IFile) res;
			monitor.subTask("Process file "+file.getName());
			String suffix = file.getFileExtension();
			if (suffix != null && !file.isLinked() && ! file.isPhantom())
			{
				for (Setting setting : settings)
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
			return false;
		}
		else
		{
			return true;
		}
	}

}
