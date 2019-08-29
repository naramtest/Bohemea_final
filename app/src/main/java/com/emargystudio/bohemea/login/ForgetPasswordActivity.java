package com.emargystudio.bohemea.login;

import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.helperClasses.Common;

public class ForgetPasswordActivity extends AppCompatActivity {

    Fragment ResetRequestFragment;
    Fragment UpdatePassword;
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        if (getIntent()!=null) {

            Intent appLinkIntent = getIntent();
            String appLinkAction = appLinkIntent.getAction();
            Uri appLinkData = appLinkIntent.getData();
            if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
                UpdatePassword = new UpdatePasswordFragment();
                Common.code=appLinkData.getLastPathSegment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.your_placeholder, UpdatePassword, getString(R.string.update_password_fragment));
                ft.commit();
            }
        }

            /*check if there is an fragment is shown already */
            if (savedInstanceState != null) {
                // get saved fragment and replace it
                fragment = getSupportFragmentManager().getFragment(savedInstanceState, getString(R.string.forget_password_saveInst));
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.your_placeholder, fragment);
                ft.commit();
            } else {
                //if there is'nt any saved instance display reset request fragment
                ResetRequestFragment = new ResetRequestFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.your_placeholder, ResetRequestFragment, getString(R.string.reset_request_fragment));
                ft.commit();
            }
       
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //check what fragment is showing in this moment and save it
        ResetRequestFragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.reset_request_fragment));
        UpdatePassword = getSupportFragmentManager().findFragmentByTag(getString(R.string.update_password_fragment));
        if (ResetRequestFragment != null && ResetRequestFragment.isVisible()) {
            getSupportFragmentManager().putFragment(outState, getString(R.string.forget_password_saveInst), ResetRequestFragment);
        }else if (UpdatePassword != null && UpdatePassword.isVisible()){
            getSupportFragmentManager().putFragment(outState, getString(R.string.forget_password_saveInst), UpdatePassword);
        }
        super.onSaveInstanceState(outState);
    }
}
