package com.example.BehaveMonitor;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.example.BehaveMonitor.fragments.FolderFragment;
import com.example.BehaveMonitor.fragments.NavigationDrawerFragment;
import com.example.BehaveMonitor.fragments.SessionFragment;
import com.example.BehaveMonitor.fragments.TemplateFragment;

import java.io.File;


public class HomeActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * Used to store the fragment currently being displayed. Defaults to -1 to show the initial fragment.
     */
    private int fragmentDisplayed = -1;

    private File activeFolder = null;
    private Template activeTemplate = null;
    private Session activeSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FileHandler.setRootDirectory();
        DBHelper db = DBHelper.getInstance(this);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        FileHandler.checkFoldersExist();

        if (savedInstanceState != null) {
            String path = savedInstanceState.getString("activeFolderString");
            String tString = savedInstanceState.getString("activeTemplateString");
            if (path != null) activeFolder = new File(path);
            if (tString != null) activeTemplate = new Template(tString);
        }

        if (activeFolder == null) {
            activeFolder = new File(FileHandler.getSessionsDirectory(), db.getFolder());
        }

        if (activeTemplate == null) {
            String templateString = db.getTemplate();
            if (templateString != null) {
                activeTemplate = new Template(templateString);
            }
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String tString = bundle.getString("activeTemplateString");
            if (tString != null) activeTemplate = new Template(tString);
        }

        Drawable drawable = getResources().getDrawable(R.drawable.ic_action_discard_black);
        drawable.setColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY);

        drawable = getResources().getDrawable(R.drawable.ic_action_new_black);
        drawable.setColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY);
    }

    /**
     * @return The name of the active folder, or "" if no folder has been selected.
     */
    public String getFolderName() {
        if(activeFolder == null) return "";
        return activeFolder.getName();
    }

    /**
     * @return The name of the active template, or "" if no template has been selected.
     */
    public String getTemplateName() {
        if (activeTemplate == null) return "";
        return activeTemplate.name;
    }

    public void setActiveFolder(File file) {
        activeFolder = file;
    }

    public void setActiveTmp(Template tmp) {
        activeTemplate = tmp;
    }

    public void setActiveSession(Session session) {
        activeSession = session;
    }

    public Session getActiveSession(){
        return activeSession;
    }

    public Session makeSession(String name, String loc) {
        activeSession = new Session(name, loc, activeFolder.getAbsolutePath());
        activeSession.setTemplate(activeTemplate);
        return activeSession;
    }

    public String getActivePath() {
        return activeFolder.getAbsolutePath();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (fragmentDisplayed != position) {
            displayFragment(position);
        }
    }

    public void displayFragment(int position) {
        Fragment fragment = null;
        switch(position) {
            case 0:
                fragment = new SessionFragment();
                break;
            case 1:
                fragment = new FolderFragment();
                break;
            case 2:
                fragment = new TemplateFragment();
                break;
            default:
                break;
        }

        if (fragment == null) {
            Log.e("Behave", "Error displaying fragment");
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        fragmentDisplayed = position;
        if  (mNavigationDrawerFragment != null) {
            mNavigationDrawerFragment.setItemChecked(position);
        }
    }

    @Override
    public void onBackPressed() {
        if (fragmentDisplayed != 0) {
            displayFragment(0);
        } else {
            super.onBackPressed();
        }
    }
}
