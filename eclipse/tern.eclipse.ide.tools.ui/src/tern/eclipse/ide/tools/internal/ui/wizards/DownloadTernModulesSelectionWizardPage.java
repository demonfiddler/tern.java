/**
 *  Copyright (c) 2013-2014 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package tern.eclipse.ide.tools.internal.ui.wizards;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tern.eclipse.ide.core.ITernRepositoryManager;
import tern.eclipse.ide.core.TernCorePlugin;
import tern.eclipse.ide.tools.internal.ui.TernToolsUIMessages;
import tern.eclipse.ide.tools.internal.ui.TernToolsUIPlugin;
import tern.eclipse.ide.ui.controls.TernModulesBlock;
import tern.eclipse.ide.ui.viewers.TernRepositoryLabelProvider;
import tern.repository.ITernRepository;
import tern.server.ITernModule;
import tern.utils.TernRepositoryHelper;

/**
 * Wizard page used to load repository.json and select tern modules to download.
 *
 */
public class DownloadTernModulesSelectionWizardPage extends
		TernWizardPage<DownloadOptions> {

	private static final String PAGE = "DownloadTernModulesSelectionWizardPage";

	private TernModulesBlock modulesBlock;

	private Button refreshButton;

	private ComboViewer repositoryViewer;

	private Text repositoryURL;

	public DownloadTernModulesSelectionWizardPage() {
		super(PAGE);
		setTitle(TernToolsUIMessages.DownloadTernModulesSelectionWizardPage_title);
		setDescription(TernToolsUIMessages.DownloadTernModulesSelectionWizardPage_description);
	}

	@Override
	protected Composite createUI(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);

		// Local repository
		createLocalRepository(container);
		// Repository URL
		createRepositoryURL(container);
		// Tern Modules list
		modulesBlock = new TernModulesBlock(null, null);
		Control control = modulesBlock.createControl(container);
		modulesBlock
				.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						DownloadTernModulesSelectionWizardPage.this
								.dialogChanged();
					}
				});
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		control.setLayoutData(data);

		return container;
	}

	/**
	 * Create a combo which hosts local tern repository.
	 * 
	 * @param parent
	 */
	private void createLocalRepository(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);

		// Label
		Label repositoryNameLabel = new Label(container, SWT.NONE);
		repositoryNameLabel
				.setText(TernToolsUIMessages.DownloadTernModulesSelectionWizardPage_localRespositoryName_text);

		// Combo repository
		repositoryViewer = new ComboViewer(container, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		repositoryViewer.getControl().setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		repositoryViewer.setLabelProvider(TernRepositoryLabelProvider
				.getInstance());
		repositoryViewer.setContentProvider(ArrayContentProvider.getInstance());

		Collection<ITernRepository> repositories = TernCorePlugin
				.getTernRepositoryManager().getRepositories();
		repositoryViewer.setInput(repositories);

		final Text repositoryPath = new Text(container, SWT.WRAP
				| SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		repositoryPath.setLayoutData(data);

		repositoryViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						ITernRepository repository = (ITernRepository) ((IStructuredSelection) event
								.getSelection()).getFirstElement();
						repositoryPath.setText(repository
								.getTernBaseDirAsString());
					}
				});

		// Select and display info about the default tern repository
		ITernRepository defaultRepository = TernCorePlugin
				.getTernRepositoryManager().getRepository(
						ITernRepositoryManager.DEFAULT_REPOSITORY_NAME);
		if (defaultRepository != null) {
			repositoryViewer.setSelection(new StructuredSelection(
					defaultRepository));
			repositoryPath.setText(defaultRepository.getTernBaseDirAsString());
		}
	}

	/**
	 * Create the repository URL text field.
	 * 
	 * @param parent
	 */
	protected void createRepositoryURL(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);

		Label repositoryURLLabel = new Label(container, SWT.NONE);
		repositoryURLLabel
				.setText(TernToolsUIMessages.DownloadTernModulesSelectionWizardPage_repositoryURL_text);

		repositoryURL = new Text(container, SWT.BORDER);
		repositoryURL.setText(TernRepositoryHelper.DEFAULT_TERN_REPOSITORY_URL);
		repositoryURL.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		refreshButton = new Button(container, SWT.PUSH);
		refreshButton.setText(TernToolsUIMessages.Button_refresh);
		repositoryURL.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshButton.setEnabled(false);
				Job refreshJob = new RefreshRepositoryJob(repositoryURL
						.getText());
				refreshJob.schedule();

			}
		});
	}

	private class RefreshRepositoryJob extends Job {

		private final String url;

		public RefreshRepositoryJob(String url) {
			super(TernToolsUIMessages.RefreshRepositoryJob_name);
			this.url = url;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			monitor.beginTask(NLS.bind(
					TernToolsUIMessages.RefreshRepositoryJob_loading, url), 1);
			// load tern repository.json with HTTP client and refresh UI
			try {
				List<ITernModule> modules = TernRepositoryHelper
						.loadModules(url);
				monitor.worked(1);
				// repository is loaded correctly, refresh UI
				refreshModules(modules, null);

			} catch (Throwable e) {
				// error while loading repository.json
				refreshModules(Collections.EMPTY_LIST, e);
			}
			monitor.done();
			return Status.OK_STATUS;
		}

		private void refreshModules(final List<ITernModule> modules,
				final Throwable e) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refreshButton.setEnabled(true);
					modulesBlock.setTernModules(modules
							.toArray(ITernModule.EMPTY_MODULE));
					if (e != null) {
						IStatus status = new Status(
								IStatus.ERROR,
								TernToolsUIPlugin.PLUGIN_ID,
								IStatus.ERROR,
								NLS.bind(
										TernToolsUIMessages.DownloadTernModulesSelectionWizardPage_errorMessage,
										url), e);
						ErrorDialog
								.openError(
										Display.getDefault().getActiveShell(),
										TernToolsUIMessages.DownloadTernModulesSelectionWizardPage_errorTitle,
										NLS.bind(
												TernToolsUIMessages.DownloadTernModulesSelectionWizardPage_errorMessage,
												url), status);
					}
				}
			});
		}
	}

	@Override
	protected void initialize() {
		Job refreshJob = new RefreshRepositoryJob(repositoryURL.getText());
		refreshJob.schedule();
	}

	@Override
	protected String validate() {
		Object[] objModules = modulesBlock.getCheckedModules();
		if (objModules.length == 0) {
			return TernToolsUIMessages.DownloadTernModulesSelectionWizardPage_modules_selection_validation;
		}
		return null;
	}

	@Override
	protected void updateModel(DownloadOptions model) {
		Object[] objModules = modulesBlock.getCheckedModules();
		ITernModule[] modules = new ITernModule[objModules.length];
		ITernModule module = null;
		for (int i = 0; i < objModules.length; i++) {
			module = (ITernModule) objModules[i];
			modules[i] = module;
		}
		model.setRepository((ITernRepository) ((IStructuredSelection) repositoryViewer
				.getSelection()).getFirstElement());
		model.setTernModules(modules);
	}

}
