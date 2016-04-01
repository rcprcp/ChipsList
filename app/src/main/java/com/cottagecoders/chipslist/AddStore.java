package com.cottagecoders.chipslist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class AddStore extends ActionBarActivity {
    Context ctx;
    Activity act;
    DatabaseCode myDB;
    EditText edit;
    EditText tax;
    Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_store);
        Log.i(ChipsList.TAG, "add_store onCreate() got here");

        ctx = this.getApplicationContext();
        act = this;
        act.setTitle(R.string.add_store);
        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);

        myDB = new DatabaseCode(ctx);
        boolean popup = getIntent().getBooleanExtra(ChipsList.INTENT_DIALOG_REQUIRED, false);
        if (popup) {
            ArrayList<StoreRecord> stores = myDB.getAllStores();
            if (stores.size() == 0) {
                dialogNoStores();
            }
        }

        //TODO: pop up the keyboard?
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        edit = (EditText) findViewById(R.id.edit);
        saveBtn = (Button) findViewById(R.id.save);
        tax = (EditText) findViewById(R.id.tax);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // so - the save button was clicked - we want to
                // verify the data and add the new store to the list.
                if (edit.getText().toString().equals("") ) {
                    // TODO: not a valid store name... do something.

                } else {
                    double f;
                    try {
                        f = Double.parseDouble(tax.getText().toString());
                    } catch (Exception e) {
                        f = (float) 0.0;
                    }

                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(edit.getWindowToken(), 0);

                    //TODO: convert this to a StoreRecord object.
                    myDB.insertStore(edit.getText().toString(), f);
                    setResult(RESULT_OK, getIntent());
                    finish();

                }
            }
        });
     }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(edit.getWindowToken(), 0);

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(edit.getWindowToken(), 0);
                setResult(RESULT_OK, getIntent());
                finish();
                return true;

            case R.id.help:
                Intent intent;
                intent = new Intent(ctx, Help.class);
                intent.putExtra(ChipsList.HELP_STRING, getString(R.string.HELP_add_store));
                startActivity(intent);
                return true;

        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.help_only, menu);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(ChipsList.TAG, "AddStore  onStart called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(ChipsList.TAG, "AddStore onPause called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(ChipsList.TAG, "AddStore  onResume called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(ChipsList.TAG, "AddStore onStop called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(ChipsList.TAG, "AddStore onDestroy called");
    }


    private void dialogNoStores() {

        AlertDialog.Builder b = new AlertDialog.Builder(act)
                .setTitle(R.string.no_stores)
                .setMessage(R.string.no_stores_mess)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                // User clicked OK so do nothing.
                            }
                        });
        b.create();
        b.show();
    }
}
