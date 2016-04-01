package com.cottagecoders.chipslist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class FindItem extends ActionBarActivity {

    private static Context ctx;
    private static Activity act;
    private static DatabaseCode myDB;

    private static EditText searchTerm;
    private static TextView numRecords;
    private static int recordCount;
    private static int storeNum;
    private static int numChecked = 0;
    private static Boolean checkedOnly = false;

    // this is used to (try to) position the ListView
    // to a place that makes sense. without this, we
    // would just re-start the ListView at record 0, each time.
    private static int firstVisible;

    private static Button add;
    private static Button move;
    CustomListAdapter adapter;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate the UI.
        setContentView(R.layout.find_item);
        ctx = this.getApplicationContext();
        act = this;
        myDB = new DatabaseCode(ctx);

        storeNum = getIntent().getIntExtra(ChipsList.INTENT_STORE_NUM, 1);
        StoreRecord sr = myDB.getStoreInfo(storeNum);

        firstVisible = getIntent()
                .getIntExtra(ChipsList.INTENT_POSITION_TAG, 0);

        String storeName = sr.getStoreName();
        //TODO:  crashes here due to text message debugging where
        // we delete the items and the store.
        if (storeName == null) {
            storeName = "Invalid Store";
        }

        // create the title.
        String name = "Add to " + storeName + " List";
        act.setTitle(name);
        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);

        // set up the field we'll use to get user
        // input for our "pseudo" autocomplete
        numRecords = (TextView) findViewById(R.id.num_records);
        searchTerm = (EditText) findViewById(R.id.search);

        // set up the add button.
        add = (Button) findViewById(R.id.add);
        // add a monitor to watch each character appended
        // (and removed) from the string.
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                add.setEnabled(false);
                add.setVisibility(View.INVISIBLE);
                checkedOnly = false;
                fillTable();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable ed) {

            }
        };
        searchTerm.addTextChangedListener(watcher);

        // set up the move button.
        move = (Button) findViewById(R.id.move);
        move.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myDB.copyItemsToStore(storeNum);
                myDB.uncheckMasterList();

                getIntent().putExtra(ChipsList.INTENT_POSITION_TAG,
                        firstVisible);
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(ctx, AddItem.class);
                intent.putExtra(ChipsList.INTENT_AUTOCOMPLETE_STRING,
                        searchTerm.getText().toString());
                intent.putExtra(ChipsList.INTENT_STORE_NUM, storeNum);
                startActivityForResult(intent, ChipsList.INTENT_ADD_ITEM);
            }
        });
        numChecked = myDB.getCheckedCount(storeNum);
        dispMoveCheck();

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(ChipsList.TAG, "FindItem  onStart() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(ChipsList.TAG, "FindItem onPause() called");
    }

    @Override
    public void onBackPressed() {
        // get here when the user has pressed the "real" back button
        // on the lower part of the keyboard.
        Log.i(ChipsList.TAG, "FindItem onBackPressed() called");
        // save this for the calling routine (Store.java)
        getIntent().putExtra(ChipsList.INTENT_POSITION_TAG, firstVisible);
        setResult(RESULT_OK, getIntent());
        finish();
        return;

        // never need this since we have the return code above...
        // super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(ChipsList.TAG, "FindItem  onResume() called");
        checkedOnly = false;
        fillTable();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(ChipsList.TAG, "FindItem onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(ChipsList.TAG, "FindItem onDestroy called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this also adds items to the action bar.
        getMenuInflater().inflate(R.menu.find_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // get here when the user selects from th options menu or the action
        // bar.
        Intent intent;

        switch (item.getItemId()) {
            case R.id.show_checked:

                clearField();
                checkedOnly = true;
                fillTable();
                return true;
            case R.id.delete_checked:
                // actually, really, really DELETE from the database.
                dialogDeleteChecked();
                checkedOnly = false;
                fillTable();
                return true;

            case R.id.clear_checked:
                // clear all the checked items in the master list...
                myDB.uncheckMasterList();
                checkedOnly = false;
                fillTable();
                break;

            case android.R.id.home:
                // this is the < to the left of the icon in the action bar.
                getIntent().putExtra(ChipsList.INTENT_POSITION_TAG, firstVisible);
                setResult(RESULT_OK, getIntent());
                finish();
                break;

            case R.id.database_cleanup:
                // delete unused sample data.
                intent = new Intent(ctx, DeleteUnusedItems.class);
                startActivity(intent);
                break;

            case R.id.help:
                // user selected the help item in the options menu.
                intent = new Intent(ctx, Help.class);
                intent.putExtra(ChipsList.HELP_STRING,
                        getString(R.string.HELP_find_item));
                startActivity(intent);
                break;

            default:
                // whaaaat?
                Log.i(ChipsList.TAG, "onOptionsItemSelected():  invalid item "
                        + item.getItemId());
                break;
        }

        return true;

    }

    /**
     * get here when an activity we started with startActivityForResult()
     * returns.
     */
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.v(ChipsList.TAG, "onActivityResult(): GOT HERE.  requestCode "
                + requestCode + " resultCode " + resultCode + " intent "
                + intent);

        checkedOnly = false;
        fillTable();
        clearField();
    }

    // dialog box asking if we should delete checked items from the database?
    public void dialogDeleteChecked() {

        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle(getString(R.string.dialog_remove_checked_title))
                .setMessage(getString(R.string.perm_del)).setCancelable(false)
                .setPositiveButton(R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        // User clicked OK so delete 'em
                        myDB.deleteCheckedItems();
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                        checkedOnly = false;
                        fillTable();
                    }
                }).setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        // User clicked Cancel, so do nothing
                        dialog.dismiss();
                    }
                }).create();
        dlg.show();
    }

    /**
     * static function to return the store number to the CustomListAdpater which
     * needs to know the store number so it can build the necessary data for the
     * ListView
     *
     * @return storeNum storeNumber we're working with.
     */

    public static int getStoreNum() {
        return storeNum;
    }

    /**
     * @return
     */
    public static Boolean getCheckedOnly() {
        return checkedOnly;
    }

    /**
     * here, we fill the ListView for the user to select items to move to a
     * store list. this is implemented as ListView and NOT a TableLayout for 2
     * reasons.
     * <p/>
     * First, building a table of 3000 items is *slow* and memory intensive.
     * The process takes well over 20 seconds. Some checking indicates the
     * slow part is building the table in the UI, and getting the data from
     * the database is (relatively) fast. Although it's still too slow,
     * it takes less than 5 seconds to get the full 3000 records from the database.
     * <p/>
     * In addition to being much faster since we don't build the whole UI table up
     * front, a ListView with CustomListAdapter provides section
     * indexing (which is an a/b/c selection thing when you hold the scroll bar
     * on the right).
     * <p/>
     * switch to the CustomListAdapter class for the details.
     */
    private void fillTable() {
        // find the UI item as defined in the XML
        lv = (ListView) findViewById(R.id.listview);

        // create the adapter. this is most of the work to get this
        // set up.
        adapter = new CustomListAdapter(FindItem.this);

        // "connect" the ListView and the Adapter.
        lv.setAdapter(adapter);

        // pick the correct location in the ListView that will make sense to the
        // user.
        lv.setSelection(firstVisible);

        if (lv.getCount() == 0) {
            Log.d(ChipsList.TAG, "fillTable(): ListView childcount = 0");
            add.setEnabled(true);
            add.setVisibility(View.VISIBLE);
        }

        if (searchTerm.getText().length() > 0) {
            numRecords.setText("" + recordCount);
        } else {
            numRecords.setText(" ");
        }
        // debugging trace.
        Log.v(ChipsList.TAG, "FindItem: fillTable(): setSelection "
                + firstVisible);
    }

    /**
     * static method that is called from the child routines, including AddItem
     * and CustomListAdapter.  This method (tries to) put the new row in the center of the screen.
     *
     * @param ix the row number to position in the center of display.
     */
    public static void saveIndex(int ix) {
        if (firstVisible - 4 > 0)
            firstVisible -= 4;
        firstVisible = ix;
    }

    public static String getAutoCompleteString() {
        return searchTerm.getText().toString();
    }

    public static void setRecordCount(int count) {
        recordCount = count;
    }

    public static void clearField()

    {
        searchTerm.setText("");
        clearButton();
        numChecked = myDB.getCheckedCount(storeNum);
        dispMoveCheck();
    }

    public static void clearButton() {
        add.setEnabled(true);
        add.setVisibility(View.VISIBLE);
    }

    public static void dispMoveCheck() {
        // 0 means initialize other values will be 1 or -1

        if (numChecked > 0) {
            move.setText("Move " + numChecked + " items");
            move.setVisibility(View.VISIBLE);
        } else
            move.setVisibility(View.INVISIBLE);
    }
}
