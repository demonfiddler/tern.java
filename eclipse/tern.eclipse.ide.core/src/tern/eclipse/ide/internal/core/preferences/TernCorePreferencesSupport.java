package tern.eclipse.ide.internal.core.preferences;

import org.eclipse.core.runtime.Preferences;

import tern.eclipse.ide.core.ITernServerType;
import tern.eclipse.ide.core.TernCoreConstants;
import tern.eclipse.ide.core.TernCorePlugin;
import tern.eclipse.ide.core.preferences.PreferencesSupport;

public class TernCorePreferencesSupport {

	private static final String NODES_QUALIFIER = TernCorePlugin.PLUGIN_ID;
	private static final Preferences store = TernCorePlugin.getDefault()
			.getPluginPreferences();
	private PreferencesSupport preferencesSupport;

	private TernCorePreferencesSupport() {
		preferencesSupport = new PreferencesSupport(TernCorePlugin.PLUGIN_ID,
				store);
	}

	private static TernCorePreferencesSupport instance = null;

	public static TernCorePreferencesSupport getInstance() {
		if (instance == null) {
			instance = new TernCorePreferencesSupport();
		}
		return instance;
	}

	public ITernServerType getServerType() {
		String id = preferencesSupport
				.getWorkspacePreferencesValue(TernCoreConstants.TERN_SERVER_TYPE);
		return TernCorePlugin.getTernServerTypeManager().findTernServerType(id);
	}

}