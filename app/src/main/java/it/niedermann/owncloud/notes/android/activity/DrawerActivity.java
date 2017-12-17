package it.niedermann.owncloud.notes.android.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import it.niedermann.owncloud.notes.R;

/**
 * Created by scherzia on 16.12.2017.
 */
public abstract class DrawerActivity extends AppCompatActivity {
    private static final String TAG = DrawerActivity.class.getSimpleName();

    public static final int RESULT_CODE_SERVER_SETTINGS = 2;
    public static final int RESULT_CODE_ABOUT = 3;

    /**
     * Reference to the drawer layout.
     */
    protected DrawerLayout mDrawerLayout;

    /**
     * Reference to the drawer toggle.
     */
    protected ActionBarDrawerToggle mDrawerToggle;

    /**
     * Reference to the navigation view.
     */
    private NavigationView mNavigationView;

    /**
     * Id of the checked menu item.
     */
    private int mCheckedMenuItem;

    /**
     * runnable that will be executed after the drawer has been closed.
     */
    private Runnable pendingRunnable;

    /**
     * Toolbar setup that must be called in implementer's {@link #onCreate}
     * after {@link #setContentView} if they want to use the toolbar.
     */
    protected void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Initializes the drawer, its content and highlights the menu item with the given id.
     * This method needs to be called after the content view has been set.
     *
     * @param menuItemId the menu item to be checked/highlighted
     */
    protected void setupDrawer(int menuItemId) {
        setupDrawer();
        setDrawerMenuItemChecked(menuItemId);
    }

    /**
     * Initializes the drawer and its content.
     * This method needs to be called after the content view has been set.
     */
    protected void setupDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        if (mNavigationView != null) {
            setupDrawerMenu(mNavigationView);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupDrawerToggle();
    }

    /**
     * setup drawer content, basically setting the item selected listener.
     *
     * @param navigationView the drawers navigation view
     */
    protected void setupDrawerMenu(NavigationView navigationView) {
        navigationView.setItemIconTintList(null);

        // setup actions for drawer menu items
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();
                        // pending runnable will be executed after the drawer has been closed
                        pendingRunnable = new Runnable() {
                            @Override
                            public void run() {
                                selectNavigationItem(menuItem);
                            }
                        };
                        return true;
                    }
                });
    }

    private void selectNavigationItem(final MenuItem menuItem) {
        setDrawerMenuItemChecked(menuItem.getItemId());

        switch (menuItem.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, RESULT_CODE_SERVER_SETTINGS);
                break;
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivityForResult(aboutIntent, RESULT_CODE_ABOUT);
                break;
            default:
                Log.w(TAG, "Unknown drawer menu item clicked: " + menuItem.getTitle());
                break;
        }
    }

    /**
     * initializes and sets up the drawer toggle.
     */
    private void setupDrawerToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                supportInvalidateOptionsMenu();
                mDrawerToggle.setDrawerIndicatorEnabled(isDrawerIndicatorAvailable());

                if (pendingRunnable != null) {
                    new Handler().post(pendingRunnable);
                    pendingRunnable = null;
                }

                closeDrawer();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mDrawerToggle.setDrawerIndicatorEnabled(true);
                supportInvalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.getDrawerArrowDrawable().setColor(Color.WHITE);
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDrawerMenuItemChecked(mCheckedMenuItem);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean retval = true;
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (isDrawerOpen()) {
                    closeDrawer();
                } else {
                    openDrawer();
                }
                break;
            }
            default:
                retval = super.onOptionsItemSelected(item);
                break;
        }
        return retval;
    }

    /**
     * checks/highlights the provided menu item if the drawer has been initialized and the menu item exists.
     *
     * @param menuItemId the menu item to be highlighted
     */
    protected void setDrawerMenuItemChecked(int menuItemId) {
        if (mNavigationView != null && mNavigationView.getMenu() != null &&
                mNavigationView.getMenu().findItem(menuItemId) != null) {

            MenuItem item = mNavigationView.getMenu().findItem(menuItemId);
            item.setChecked(true);

            // reset all tinted icons
            for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
                MenuItem menuItem = mNavigationView.getMenu().getItem(i);
                if (menuItem.getIcon() != null) {
                    menuItem.getIcon().clearColorFilter();
                }
            }

            Drawable wrap = DrawableCompat.wrap(item.getIcon());
            wrap.setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP);
            mCheckedMenuItem = menuItemId;
        } else {
            Log.w(TAG, "setDrawerMenuItemChecked has been called with invalid menu-item-ID");
        }
    }

    /**
     * checks if the drawer exists and is opened.
     *
     * @return <code>true</code> if the drawer is open, else <code>false</code>
     */
    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    /**
     * opens the drawer.
     */
    public void openDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    /**
     * closes the drawer.
     */
    public void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    /**
     * always returns true and is provided to be overriden if an Activity
     * needs a more fine grained logic on if/when to show the drawer icon.
     *
     * @return true
     */
    public boolean isDrawerIndicatorAvailable() {
        return true;
    }
}
