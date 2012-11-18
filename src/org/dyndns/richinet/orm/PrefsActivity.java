package org.dyndns.richinet.orm;

import org.richinet.dyndns.orm.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity {
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		addPreferencesFromResource( R.xml.prefs );
	}

}
