package com.cottagecoders.chipslist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class DeleteCompareMenu extends AppCompatActivity {

    Context ctx;
    Activity act;
    DatabaseCode myDB;
    Button delete;
    Button compare;
    Button share;
    int itemNum;
    int storeNum;
    StoreRecord sr;
    String theName;
    TextView itemName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deletecompare);
        itemName = (TextView) findViewById(R.id.itemName);


        ctx = this.getApplicationContext();
        act = this;
        act.setTitle(R.string.deletecompareshare);
        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);

        myDB = new DatabaseCode(ctx);

        itemNum = getIntent().getIntExtra(ChipsList.INTENT_ITEM_NUM, -1);
        storeNum = getIntent().getIntExtra(ChipsList.INTENT_STORE_NUM, -1);
        sr = myDB.getStoreInfo(storeNum);

        theName = getIntent().getStringExtra(ChipsList.INTENT_ITEM_NAME);
        Log.i(ChipsList.TAG, "DeleteCompareMenu: onCreate() got here item name = theName");

        itemName.setText(theName);
        itemName.setTextSize(ChipsList.TEXT_FONT_SIZE);

        delete = (Button) findViewById(R.id.deleteshopping);
        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialogReallyDelete(storeNum, itemNum, theName);
            }
        });

        compare = (Button) findViewById(R.id.compareprice);
        compare.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ctx, PriceComparison.class);
                intent.putExtra(ChipsList.INTENT_ITEM_NUM, itemNum);
                intent.putExtra(ChipsList.INTENT_ITEM_NAME, theName);
                intent.putExtra(ChipsList.INTENT_STORE_NUM, storeNum);
                startActivityForResult(intent, ChipsList.INTENT_PRICE_COMPARE);
            }
        });

        if (!ChipsList.hasSMS || numberOfContacts() == 0) {
            share = (Button) findViewById(R.id.share);
            share.setVisibility(View.INVISIBLE);
        } else {  //no SMS.
            share = (Button) findViewById(R.id.share);
            share.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(ctx, SelectAddressBook.class);
                    StoreRecord sr = myDB.getStoreInfo(storeNum);
                    FoodRecord food = myDB.getItem(storeNum, itemNum);
                    Parse parse = new Parse(ctx);
                    String theMessage = parse.encode(sr, food);

                    /*
                    //TODO: debugging code to delete the store and item
                    Log.d(ChipsList.TAG, "DeleteCompareMenu: share: DEBUGGING!! DELETING STORE AND ITEM! ");
                    Log.d(ChipsList.TAG, "DeleteCompareMenu: share: store "
                            + sr.getStoreName()
                            + " "
                            + sr.getStoreNumber()
                            + " food: "
                            + food.getItemName()
                            + " "
                            + food.getItemNum());

                    myDB.deleteAnItem(food.getItemNum());
                    myDB.deleteStore(sr.getStoreNumber());
                    food = null;
                    sr = null;
       ********************/
                    Log.d(ChipsList.TAG, "share: theMessage: " + theMessage);
                    intent.putExtra(ChipsList.INTENT_MESSAGE_TEXT, theMessage);
                    startActivityForResult(intent, ChipsList.INTENT_SHARE);
                }
            });
         }
    }

    private int numberOfContacts() {

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor people = getContentResolver().query(uri, projection, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        Log.d(ChipsList.TAG,
                "SelectAddressBook: numberOfContacts(): number of contacts "
                        + people.getCount());
        int count = people.getCount();
        people.close();
        return count;
    }
    private void dialogReallyDelete(final int storeNum, final int itemNum, final String itemName) {

        AlertDialog.Builder b = new AlertDialog.Builder(act)
                .setTitle(getString(R.string.really_delete))
                .setMessage(
                        getString(R.string.really_delete2) + " " + itemName)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                // User clicked OK so do something
                                myDB.deleteFromShoppingList(itemNum, storeNum);
                                dialog.dismiss();
                                setResult(RESULT_OK, getIntent());
                                finish();

                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                // User clicked CANCEL so do nothing
                                dialog.dismiss();
                            }
                        });
        b.create();
        b.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

//		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
//		.hideSoftInputFromWindow(edit.getWindowToken(), 0);

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
//			((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
//			.hideSoftInputFromWindow(edit.getWindowToken(), 0);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
// regardless of the requestcode return to caller
        finish();

    }

}
