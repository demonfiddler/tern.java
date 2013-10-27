package tern.eclipse.swt.samples.nodejs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import tern.doc.IJSDocument;
import tern.eclipse.jface.nodejs.NodejsTernContentProposalProvider;
import tern.eclipse.swt.JSDocumentText;
import tern.eclipse.swt.samples.FileTreeContentProvider;
import tern.eclipse.swt.samples.FileTreeLabelProvider;
import tern.server.ITernServer;
import tern.server.TernDef;
import tern.server.nodejs.NodejsTernServer;
import tern.server.nodejs.process.NodejsProcess;
import tern.server.nodejs.process.PrintNodejsProcessListener;
import tern.utils.IOUtils;

public class NodejsTernEditorWithFiles {

	private CTabFolder tabFolder;
	private ITernServer server;

	public static void main(String[] args) {
		NodejsTernEditorWithFiles editor = new NodejsTernEditorWithFiles();
		try {
			editor.createUI();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createUI() throws IOException, InterruptedException {

		int port = 12345;

		File nodejsTernBaseDir = new File("../tern.server.nodejs");
		File projectDir = new File(".");
		NodejsProcess nodejs = new NodejsProcess(nodejsTernBaseDir, projectDir);
		nodejs.setPort(port);
		nodejs.addProcessListener(PrintNodejsProcessListener.getInstance());

		nodejs.start();

		this.server = new NodejsTernServer(12345);
		server.addDef(TernDef.browser);
		server.addDef(TernDef.ecma5);

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(500, 500);
		shell.setText("Tern SWT Eclipse");
		shell.setLayout(new GridLayout(2, true));

		final Button saveButton = new Button(shell, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.setEnabled(false);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		saveButton.setLayoutData(data);

		createTreeFiles(shell);
		createEditorsArea(shell);

		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// editor.setDirty(false);
			}
		});
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private void createTreeFiles(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(1, false));

		final TreeViewer tv = new TreeViewer(composite);
		tv.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		tv.setContentProvider(new FileTreeContentProvider());
		tv.setLabelProvider(new FileTreeLabelProvider());

		tv.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (event.getSelection().isEmpty()) {
					return;
				}
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event
							.getSelection();
					File file = (File) selection.getFirstElement();
					createEditor(tabFolder, file, server);
				}
			}
		});

		File baseDir = new File("scripts");
		long start = System.currentTimeMillis();
		loadJS(baseDir);
		System.err.println("load JS=" + (System.currentTimeMillis() - start)
				+ "ms");

		tv.setInput(baseDir); // pass a non-null that will be
								// ignored
	}

	private void loadJS(File baseDir) {
		if (baseDir.isFile()) {
			try {
				if (baseDir.getName().endsWith(".uncompressed.js")) {
					long start = System.currentTimeMillis();
					server.addFile(baseDir.getPath(),
							IOUtils.toString(new FileInputStream(baseDir)));
					// server.addFile(baseDir.getPath(), null);
					System.err.println(baseDir.getPath() + " =>"
							+ (System.currentTimeMillis() - start) + "ms");
				}
			} catch (Exception e) {
				System.err.println(baseDir.getPath() + " =>ERROR");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (!baseDir.getName().startsWith(".")
					&& !baseDir.getName().startsWith("test")) {
				for (File file : baseDir.listFiles()) {
					loadJS(file);
				}
			}
		}
	}

	private void createEditorsArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(1, false));

		this.tabFolder = new CTabFolder(composite, SWT.TOP);
		tabFolder.setBorderVisible(true);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	private void createEditor(CTabFolder tabFolder, File file,
			ITernServer server) {

		CTabItem tab = new CTabItem(tabFolder, SWT.NONE);
		tab.setText(file.getName());

		String js = "";
		try {
			js = readFile(file);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Text text = new Text(tabFolder, SWT.MULTI | SWT.BORDER);
		text.setText(js);

		tab.setControl(text);

		IJSDocument document = new JSDocumentText(file.getName(), server, text);

		// Les charactères qui déclenchent l'autocomplétion
		char[] autoActivationCharacters = new char[] { '.' };
		// La combinaison de touches qui déclenche l'autocomplétion
		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// La vraie chose !
		ContentProposalAdapter adapter = new ContentProposalAdapter(text,
				new TextContentAdapter(),
				new NodejsTernContentProposalProvider(document), keyStroke,
				autoActivationCharacters);
		// adapter.setLabelProvider(TernLabelProvider.getInstance());
		text.setLayoutData(new GridData(GridData.FILL_BOTH));

		tabFolder.setSelection(tab);
	}

	private String readFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}

		return stringBuilder.toString();
	}
}
