package de.echtzeitraum.openpassword;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.Window;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Main activity of password generator app.
 * @author Marvin Rabe <me@marvinrabe.de>
 * @license GNU General Public License 3 <http://www.gnu.org/licenses/>
 */
public class MainView extends SherlockFragmentActivity {
	
	/**
	 * Name of shared preferences.
	 */
	public final String PREFS_NAME = "PasswordGeneratorSettings";

    /**
     * Instance of Password Generator
     */
	public static PasswordGenerator passwordGenerator = null;

    /**
     * Contain stored default values for the controls.
     */
	public static SharedPreferences settings = null;

	public ViewPager mPager;
	public ModeAdapter mAdapter;
	
	private SimpleFragment fSimple;
	private AdvancedFragment fAdvanced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// Updates App title
		this.setTitle(this.getResources().getString(R.string.app_title));
		
		// Enables progress icon if API level >= 11 */
		if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB ) {
			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			setSupportProgressBarIndeterminateVisibility(false);
		}
		
		MainView.passwordGenerator = new PasswordGenerator();
		MainView.settings = getSharedPreferences(PREFS_NAME, 0);
		
        setContentView(R.layout.main);

        mAdapter = new ModeAdapter(getSupportFragmentManager());

        // create fragments to use
        if (savedInstanceState != null) {
        	this.fSimple = (SimpleFragment) getSupportFragmentManager().getFragment(savedInstanceState, SimpleFragment.class.getName());
        	this.fAdvanced = (AdvancedFragment) getSupportFragmentManager().getFragment(savedInstanceState, AdvancedFragment.class.getName());
        }
        if (this.fSimple == null)
        	this.fSimple = new SimpleFragment();
        if (this.fAdvanced == null)
        	this.fAdvanced = new AdvancedFragment();
        
        mAdapter.add(this.fSimple);
        mAdapter.add(this.fAdvanced);

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.removeAllViews();
        mPager.setAdapter(mAdapter);

        int position = MainView.settings.getInt("fragment", 0);
        mPager.setCurrentItem(position);

	    final Button button = (Button) this.findViewById(R.id.ok);

	    button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	generatePassword();
            }
	    });

		final EditText pwField = (EditText) findViewById(R.id.password);
		pwField.setLongClickable(true);
		pwField.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				/* Copy text to clipboard manager */
				/* Use new ClipboardManager if API level >= 11 */
				if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB ) {
					android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					clipboard.setText(pwField.getText());
				} else {
					android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					clipboard.setText(pwField.getText());
				}

				/* Show message: Password copied to clipboard */
				Toast.makeText(getApplicationContext(), R.string.copy, Toast.LENGTH_SHORT).show();

				return true;
			}
		});
    }

    @Override
    protected void onPause() {
    	SharedPreferences.Editor editor = MainView.settings.edit();
        editor.putInt("fragment", mPager.getCurrentItem());
        editor.commit();
    	super.onPause();
    }

    public void generatePassword() {
    	ModeAdapter adapter = ((ModeAdapter)mPager.getAdapter());
    	FragmentForms fragment = (FragmentForms) adapter.getItem(mPager.getCurrentItem());
    	fragment.generatePassword();
	}
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
		final EditText pwField = (EditText) findViewById(R.id.password);
    	savedInstanceState.putString("password", pwField.getText().toString());
    	getSupportFragmentManager().putFragment(savedInstanceState, SimpleFragment.class.getName(), fSimple);
    	getSupportFragmentManager().putFragment(savedInstanceState, AdvancedFragment.class.getName(), fAdvanced);
    	super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
		final EditText pwField = (EditText) findViewById(R.id.password);
		pwField.setText(savedInstanceState.getString("password"));
    }

	/**
	 * Creates options menu.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}

	/**
	 * Action if option item was selected.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, AboutView.class));
                return true;
            case R.id.donate:
                startActivity(new Intent(this, DonateView.class));
                return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

    static final class ModeAdapter extends FragmentPagerAdapter {
        private final ArrayList<Fragment> mPages = new ArrayList<Fragment>();

        public ModeAdapter(FragmentManager fm) {
            super(fm);
        }

        public void add(Fragment fragmentClass) {
        	if(!mPages.contains(fragmentClass)) {
            	mPages.add(fragmentClass);
        	}
		}

		@Override
        public int getCount() {
            return mPages.size();
        }

        @Override
        public Fragment getItem(int position) {
        	return mPages.get(position);
        }
    }
}
