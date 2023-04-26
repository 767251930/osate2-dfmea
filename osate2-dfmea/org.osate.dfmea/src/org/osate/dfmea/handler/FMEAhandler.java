/**
 * Copyright (c) 2004-2022 Carnegie Mellon University and others. (see Contributors file).
 * All Rights Reserved.
 *
 * NO WARRANTY. ALL MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY
 * KIND, EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE
 * OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT
 * MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Created, in part, with funding and support from the United States Government. (see Acknowledgments file).
 *
 * This program includes and/or can make use of certain third party source code, object code, documentation and other
 * files ("Third Party Software"). The Third Party Software that is used by this program is dependent upon your system
 * configuration. By using this program, You agree to comply with any and all relevant Third Party Software terms and
 * conditions contained in any such Third Party Software or separate license file distributed with such Third Party
 * Software. The parties who own the Third Party Software ("Third Party Licensors") are intended third party benefici-
 * aries to this license with respect to the terms applicable to their Third Party Software. Third Party Software li-
 * censes only apply to the Third Party Software and not any other portion of this program or this program as a whole.
 */

package org.osate.dfmea.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.ui.util.ResourceUtil;
import org.osate.aadl2.errormodel.faulttree.util.SiriusUtil;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.modelsupport.EObjectURIWrapper;
import org.osate.aadl2.modelsupport.resources.OsateResourceUtil;
import org.osate.dfmea.FmeaBuilder;
import org.osate.dfmea.export.FileExport;
import org.osate.dfmea.fixfta.FTAGenerator;
import org.osate.ui.dialogs.Dialog;


public final class FMEAhandler extends AbstractHandler {
	static String selectfocusname;
	static Boolean showfailurenet = true;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		InstanceObject object = getTarget(HandlerUtil.getCurrentSelection(event));
		if (object == null) {
			Dialog.showInfo("Failure Modes Effects and Analysis", "Please choose an instance model!");
			return IStatus.ERROR;
		}
		ComponentInstance target = object instanceof ComponentInstance ? (ComponentInstance) object
				: object.getSystemInstance();

//

//		// 获取Instance对象
////		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
//		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
//		IOutlineNode node = (IOutlineNode) selection.getFirstElement();
//		node.readOnly(state -> {
//			SystemInstance rootInstance = null;
//			EObject selectedObject = state;
//			if (selectedObject instanceof SystemInstance) {
//				rootInstance = (SystemInstance) selectedObject;
//			}
//			if (selectedObject instanceof ComponentImplementation) {
//				try {
//					rootInstance = InstantiateModel
//							.buildInstanceModelFile((ComponentImplementation) selectedObject);
//				} catch (Exception e) {
//					e.printStackTrace();
//					return null;
//				}
//			}
//			if (rootInstance == null) {
//				Dialog.showInfo("InstantiateModel Error", "The model was not instantiated successfully.");
//				return IStatus.ERROR;
//			}
//			ComponentInstance target = rootInstance;
			// 准备构建FMEA数据结构
			FmeaBuilder fb = new FmeaBuilder();
			// 构造结构树
			fb.Construct_structure_tree(target);
			// 从关注组件中获取表头
			fb.getHead();
			// ---- 窗口初始化 ----
			List<String> componentnamelist = new ArrayList<String>();
			for (ComponentInstance ci : fb.root_component.ci.getAllComponentInstances()) {
				componentnamelist.add(FmeaBuilder.getInstanceName(ci));
			}
			selectfocusname = fb.head.Focus_component_name;
//			showfailurenet = true;
//			FileExport.ONLY_FAILUREMODES = true;
//			FileExport.ONLY_FINAL_FAILURE = false;
//			FileExport.SHOW_INSTANCE_NAME = true;
			// 窗口展示
			final Display d = PlatformUI.getWorkbench().getDisplay();
			d.syncExec(() -> {
				IWorkbenchWindow window;
				Shell sh;

				window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				sh = window.getShell();

				FmeaDialog diag = new FmeaDialog(sh);
				diag.setFocusComponents(componentnamelist);
				diag.setfocusComponent(selectfocusname);
				diag.setonlyfailuremodes(FileExport.ONLY_FAILUREMODES);
				diag.setonlyfinalfailure(FileExport.ONLY_FINAL_FAILURE);
				diag.setshowinstancename(FileExport.SHOW_INSTANCE_NAME);
				diag.setisGraphicView(showfailurenet);
				diag.setTarget("'" + FmeaBuilder.getInstanceName(target) + "'");

				diag.open();
				selectfocusname = diag.getFocusComponent();
				FileExport.ONLY_FAILUREMODES = diag.getonlyfailuremodes();
				FileExport.ONLY_FINAL_FAILURE = diag.getonlyfinalfailure();
				FileExport.SHOW_INSTANCE_NAME = diag.getshowinstancename();
				showfailurenet = diag.getisGraphicView();
			});
			if (FmeaDialog.OKpressed) {
				// 同时构造故障网与功能网
				fb.BuildFailureAndFuncNet();
				// 填充AP值
				fb.FillAP(fb.root_component);
				// 打印数据结构
				fb.Print_Structure(fb.root_component, "");
				// 更新焦点组件
				fb.updateFocusComponent(selectfocusname);
				// 准备文件输出
				FileExport fe = new FileExport();
				fe.ExportFMEAreport(fb);
				System.out.println("FMEA FINISH!!");

				if (showfailurenet) {
					FTAGenerator.removeEventOrphans(fb.ftamodel);
					fb.saveFaultTree();
					SiriusUtil.INSTANCE.autoOpenModel(fb.ftamodel,
							ResourceUtil.getFile(target.eResource()).getProject(),
							"viewpoint:/org.osate.aadl2.errormodel.faulttree.design/FaultTree", "IconicFaultTree",
							"Fault Tree");

				}
			}
//			return Status.OK_STATUS;
//		});
		return Status.error("error");
	}

	private InstanceObject getTarget(ISelection currentSelection) {
		if (currentSelection instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) currentSelection;
			if (iss.size() == 1) {
				Object obj = iss.getFirstElement();
				if (obj instanceof InstanceObject) {
					return (InstanceObject) obj;
				}
				if (obj instanceof EObjectURIWrapper) {
					EObject eObject = new ResourceSetImpl().getEObject(((EObjectURIWrapper) obj).getUri(), true);
					if (eObject instanceof InstanceObject) {
						return (InstanceObject) eObject;
					}
				}
				if (obj instanceof IFile) {
					URI uri = OsateResourceUtil.toResourceURI((IFile) obj);
					Resource res = new ResourceSetImpl().getResource(uri, true);
					EList<EObject> rl = res.getContents();
					if (!rl.isEmpty()) {
						return (InstanceObject) rl.get(0);
					}
				}
			}
		}
		return null;
	}


}
