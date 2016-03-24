package com.lifesting.tool.encoding;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ChangeFileEncodingAction implements IObjectActionDelegate{

	IWorkbenchPart part;
	private IStructuredSelection selection;
	public ChangeFileEncodingAction() {
		
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		part = targetPart;
	}

	public void run(IAction action) {
		SetEncodingJob job = new SetEncodingJob(this.selection);
		job.schedule();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = (IStructuredSelection) selection;		
		action.setEnabled(!this.selection.isEmpty());
	}
	public void reportException(Exception e)
	{
		MessageDialog.openError(part.getSite().getShell(), "ERROR", e.getLocalizedMessage());
	}
}
