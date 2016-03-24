package com.lifesting.tool.encoding.properties;

import static com.lifesting.tool.encoding.Activator.ENCODINGS;
import static com.lifesting.tool.encoding.Activator.FROM_ENCODINGS;
import static com.lifesting.tool.encoding.Activator.DUMP;
import static com.lifesting.tool.encoding.Activator.FILE;
import static com.lifesting.tool.encoding.Activator.ALL_ENCODING;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

import com.lifesting.tool.encoding.Activator;
import com.lifesting.tool.encoding.Setting;

public class SettingPropertyPage extends PropertyPage {

	private List<Setting> settings = new ArrayList<Setting>();
	private List<Setting> settingsDefault;
	
	TableViewer viewer;
	public SettingPropertyPage() {
		super();
	}
	//加载项目设置信息
	private void loadSetting() {
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		Activator.loadSetting(project.getName(), settings);
		settingsDefault = new ArrayList<Setting>(settings);
	}
	//保存项目设置信息
	private void saveSetting() {
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		File file = Activator.getDefault().getStateLocation().append(project.getName()+"_"+FILE).toFile();
		ObjectOutputStream output = null;
		try {
			
			output = new ObjectOutputStream(new FileOutputStream(file));
			for (Setting ss : settings)
				output.writeObject(ss);
			output.writeObject(DUMP);
			output.flush();
		} catch (FileNotFoundException e) {
			Activator.logException(e);
		} catch (IOException e) {
			Activator.logException(e);			
		}
		finally
		{
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					Activator.logException(e);
				}
		}
	}
	protected Control createContents(Composite parent) {
		loadSetting();
		Composite composite = new Composite(parent, SWT.NONE);
		FormLayout layout = new FormLayout();
		layout.spacing = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.marginTop = 10;
		composite.setLayout(layout);
		Label title = new Label(composite, SWT.CENTER);
		FormData title_data = new FormData();
	
		title_data.left = new FormAttachment(0);
		title_data.right = new FormAttachment(100);
		title_data.height = 30;
		title.setLayoutData(title_data);
		title.setText("Encoding predefined");

		Button add = new Button(composite, SWT.NONE);
		add.setText("Add");
		Button remove = new Button(composite, SWT.NONE);
		remove.setText("Remove");

		FormData add_data = new FormData();

		add_data.right = new FormAttachment(100);
		add_data.width = 120;
		add_data.top = new FormAttachment(title);

		FormData remove_data = new FormData();
		remove_data.right = new FormAttachment(100);
		remove_data.width = 120;
		remove_data.top = new FormAttachment(add);

		add.setLayoutData(add_data);
		remove.setLayoutData(remove_data);

		viewer = new TableViewer(composite,
				SWT.FULL_SELECTION);
		viewer.setContentProvider(new ArrayContentProvider());
		add.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				settings.add(Activator.newDefaultSetting());
				viewer.refresh();
			}
		});
		remove.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (!selection.isEmpty()){
					settings.remove(selection.getFirstElement());
					viewer.refresh();
				}
			}
		});
		FormData viewer_data = new FormData();
		viewer_data.left = new FormAttachment(0);
		viewer_data.right = new FormAttachment(add);
		viewer_data.top = new FormAttachment(title);
		viewer_data.bottom = new FormAttachment(100);
		viewer.getControl().setLayoutData(viewer_data);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		// suffix
		TableViewerColumn col_suffix = new TableViewerColumn(viewer, SWT.NONE);
		col_suffix.getColumn().setText("Suffix");
		col_suffix.getColumn().setWidth(60);
		col_suffix.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Setting) element).getSuffix();
			}
		});
		col_suffix.setEditingSupport(new EditingSupport(viewer) {
			private TextCellEditor editor = new TextCellEditor(viewer
					.getTable());

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return editor;
			}

			@Override
			protected Object getValue(Object element) {
				return ((Setting) element).getSuffix();
			}

			@Override
			protected void setValue(Object element, Object value) {
				Setting setting = (Setting) element;
				String suffix = (String) value;
				if (suffix.trim().length() == 0) {
					MessageDialog.openError(getShell(), "Error",
							"File suffix is empty!");
					return;
				}
				if (existType(setting, suffix)) {
					MessageDialog.openError(getShell(), "Error",
							"Setting for type '" + value + "' exists!");
				} else {
					setting.setSuffix(suffix);
					viewer.update(element, null);
				}
			}
		});
		// current encoding
		TableViewerColumn col_current_encoding = new TableViewerColumn(viewer,
				SWT.NONE);
		col_current_encoding.getColumn().setText("File Encoding");
		col_current_encoding.getColumn().setWidth(120);
		col_current_encoding.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Setting) element).getCurrentEncoding();
			}
		});
		col_current_encoding.setEditingSupport(new EditingSupport(viewer) {

			private ComboBoxCellEditor encoding_editor = new ComboBoxCellEditor(
					viewer.getTable(), FROM_ENCODINGS.toArray(new String[FROM_ENCODINGS
					                    							.size()]), SWT.READ_ONLY);;

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return encoding_editor;
			}

			// Must be integer
			@Override
			protected Object getValue(Object element) {
				return FROM_ENCODINGS.indexOf(((Setting) element)
						.getCurrentEncoding());
				
			}

			@Override
			protected void setValue(Object element, Object value) {
				Setting s = (Setting) element;
				String current_encoding = FROM_ENCODINGS.get((Integer) value);
				if (s.getConvertedEncoding().equals(current_encoding)) {
					MessageDialog.openError(getShell(), "Error", "File encoding is same as the encoding to convert!");
				} else {
					s.setCurrentEncoding(current_encoding);
					viewer.update(element, null);
				}
			}
		});
		// encoding need convert
		TableViewerColumn col_be_encoding = new TableViewerColumn(viewer,
				SWT.NONE);
		col_be_encoding.getColumn().setText("Convert To");
		col_be_encoding.getColumn().setWidth(120);
		col_be_encoding.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Setting) element).getConvertedEncoding();
			}
		});
		col_be_encoding.setEditingSupport(new EditingSupport(viewer)
		{
			private ComboBoxCellEditor encoding_editor = new ComboBoxCellEditor(
					viewer.getTable(), ENCODINGS.toArray(new String[ENCODINGS
							.size()]), SWT.READ_ONLY);
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return encoding_editor;
			}

			@Override
			protected Object getValue(Object element) {
			return ENCODINGS.indexOf(((Setting)element).getConvertedEncoding());
			}

			@Override
			protected void setValue(Object element, Object value) {
				Setting s = (Setting) element;
				String converted = ENCODINGS.get((Integer)value);
				if (s.getCurrentEncoding().equals(converted))
				{
					MessageDialog.openError(getShell(), "Error", "Convert encoding  is same as file encoding!");
				}
				else
				{
					s.setConvertedEncoding(converted);
					viewer.update(element, null);
				}
			}
			
		});
		// convert character
		TableViewerColumn col_char_convert = new TableViewerColumn(viewer,
				SWT.NONE);
		col_char_convert.getColumn().setText("Convert Char");
		col_char_convert.getColumn().setWidth(100);
		col_char_convert.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(((Setting) element).isConvertCharacter());
			}
		});
		col_char_convert.setEditingSupport(new EditingSupport(viewer){
			private CheckboxCellEditor bool_editor = new CheckboxCellEditor(viewer.getTable());
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return bool_editor;
			}

			@Override
			protected Object getValue(Object element) {
				return ((Setting)element).isConvertCharacter();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((Setting)element).setConvertCharacter((Boolean)value);
				viewer.update(element, null);
			}
			
		});
		viewer.setInput(settings);
		
		return composite;
	}
	protected boolean existType(Setting setting, String suffix) {
		for (Setting s : settings) {
			if (s == setting)
				continue;
			if (s.getSuffix().equals(suffix)) {
				return true;
			}
		}
		return false;
	}

	protected void performDefaults() {
		settings.clear();
		settings.addAll(settingsDefault);
		viewer.refresh();
	}

	public boolean performOk() {
		saveSetting();
		return true;
	}


}