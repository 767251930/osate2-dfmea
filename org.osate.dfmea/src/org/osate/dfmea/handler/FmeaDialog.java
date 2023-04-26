package org.osate.dfmea.handler;

import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class FmeaDialog extends TitleAreaDialog {

	private String target;

	private String focusComponent;
	private List<String> componentnames;
	private Combo focusComponentCombo;


	private Button onlyfinalfailureBox;
	private Boolean onlyfinalfailure = false;

	private Button onlyfailuremodesBox;
	private Boolean onlyfailuremodes = false;

	private Button showinstancenameBox;
	private Boolean showinstancename = false;

	private Button graphicViewBox;
	private Boolean isGraphicView = false;

	public static Boolean OKpressed = false;


	public FmeaDialog(Shell parentShell) {
		super(parentShell);
	}

	public void setFocusComponents(List<String> sg) {
		componentnames = sg;
	}

	public void setfocusComponent(String in) {
		focusComponent = in;
	}

	public void setonlyfinalfailure(Boolean in) {
		onlyfinalfailure = in;
	}

	public void setonlyfailuremodes(Boolean in) {
		onlyfailuremodes = in;
	}

	public void setshowinstancename(Boolean in) {
		showinstancename = in;
	}

	public void setisGraphicView(Boolean in) {
		isGraphicView = in;
	}

	public void setTarget(String targetname) {
		target = targetname;
	}

	@Override
	public void create() {
		super.create();
		OKpressed = false;
		setTitle("Failure Mode and Effect Analysis" + (target.isEmpty() ? "" : " for " + target));
		setMessage("Select focus component for Failure Mode and Effect Analysis.", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		Composite comboContainer = new Composite(area, SWT.NONE);
		GridLayout comboLayout = new GridLayout(2, false);
		comboContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comboContainer.setLayout(comboLayout);

		Label labelfocuscomponent = new Label(comboContainer, SWT.NONE);
		labelfocuscomponent.setText("Select Focus Component :  ");

		focusComponentCombo = new Combo(comboContainer, SWT.READ_ONLY | SWT.BORDER);
		String val[] = new String[componentnames.size()];
		for (int i = 0; i < componentnames.size(); i++) {
			val[i] = componentnames.get(i);
		}
		focusComponentCombo.setItems(val);
		focusComponentCombo.select(componentnames.indexOf(focusComponent));

		Composite ButtonContainer = new Composite(area, SWT.NONE);
		GridLayout ButtonLayout = new GridLayout(1, false);
		ButtonContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ButtonContainer.setLayout(ButtonLayout);

		onlyfailuremodesBox = new Button(ButtonContainer, SWT.CHECK);
		onlyfailuremodesBox.setText(
				"Only show failure modes (state&&out propagation)(include intermediate failure with FMEA property)");
		onlyfailuremodesBox.setSelection(onlyfailuremodes);

		onlyfinalfailureBox = new Button(ButtonContainer, SWT.CHECK);
		onlyfinalfailureBox
				.setText("Only show final failure (effect&&cause)(include intermediate failure with FMEA property)");
		onlyfinalfailureBox.setSelection(onlyfinalfailure);

		showinstancenameBox = new Button(ButtonContainer, SWT.CHECK);
		showinstancenameBox.setText("Show component name in functional analysis and risk analysis ");
		showinstancenameBox.setSelection(showinstancename);

		graphicViewBox = new Button(ButtonContainer, SWT.CHECK);
		graphicViewBox.setText("Show the whole failure net in graphical view");
		graphicViewBox.setSelection(isGraphicView);

		return area;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		focusComponent = focusComponentCombo.getText();
		onlyfailuremodes = onlyfailuremodesBox.getSelection();
		onlyfinalfailure = onlyfinalfailureBox.getSelection();
		showinstancename = showinstancenameBox.getSelection();
		isGraphicView = graphicViewBox.getSelection();
		OKpressed = true;
		super.okPressed();
	}

	public String getFocusComponent() {
		return focusComponent;
	}

	public Boolean getonlyfinalfailure() {
		return onlyfinalfailure;
	}

	public Boolean getonlyfailuremodes() {
		return onlyfailuremodes;
	}

	public Boolean getshowinstancename() {
		return showinstancename;
	}

	public Boolean getisGraphicView() {
		return isGraphicView;
	}


}
