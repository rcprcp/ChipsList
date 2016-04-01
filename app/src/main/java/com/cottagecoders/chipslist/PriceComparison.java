package com.cottagecoders.chipslist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class PriceComparison extends AppCompatActivity {

    Context ctx;
    Activity act;
    DatabaseCode myDB;
    NumberFormat nf;

    Button move;
    //Button ok;

    int itemNum;
    int storeNum;
    String theName;
    static int destinationStore;
    boolean masterMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.price_comparison);

        ctx = this.getApplicationContext();
        act = this;
        myDB = new DatabaseCode(ctx);

        theName = getIntent().getStringExtra(ChipsList.INTENT_ITEM_NAME);
        itemNum = getIntent().getIntExtra(ChipsList.INTENT_ITEM_NUM, -1);
        storeNum = getIntent().getIntExtra(ChipsList.INTENT_STORE_NUM, -1);

        Log.d(ChipsList.TAG, "PriceComparison: onCreate(): store number in price comparison = "
                + storeNum);
        if (storeNum == -1) {
            masterMode = true;
        } else {
            masterMode = false;
        }

        act.setTitle("Price Comparison");
        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);

        nf = new DecimalFormat("####.##");

        /**********
         * we may not need this.
        ok = (Button) findViewById(R.id.ok);
        ok.setVisibility(View.INVISIBLE);
        ok.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
         ********************/

        move = (Button) findViewById(R.id.move);
        move.setVisibility(View.VISIBLE);
        move.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                myDB.removeItemFromAllStores(itemNum);
                TableLayout tl = (TableLayout) findViewById(R.id.table);
                for (int i = 0; i < tl.getChildCount(); i++) {
                    TableRow row = (TableRow) tl.getChildAt(i);
                    CheckBox chk = (CheckBox) row.getChildAt(0);
                    if (chk.isChecked()) {
                        PriceRecord p = (PriceRecord) chk.getTag();
                        myDB.moveItemToStore(itemNum, p.getStoreNum());
                    }
                }
                finish();
            }
        });

        ((TextView) findViewById(R.id.itemName)).setText(theName);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(ChipsList.TAG, "PriceComparison  onStart called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(ChipsList.TAG, "PriceComparison onPause called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(ChipsList.TAG, "PriceComparison  onResume called");
        fillTable();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(ChipsList.TAG, "PriceComparison onStop called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(ChipsList.TAG, "PriceComparison onDestroy called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.help_only, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {

            case android.R.id.home:
                setResult(RESULT_OK, getIntent());
                finish();
                break;

            case R.id.help:
                intent = new Intent(ctx, Help.class);
                intent.putExtra(ChipsList.HELP_STRING,
                        getString(R.string.HELP_find_item));
                startActivityForResult(intent, 0);
                break;

            default:
                Log.i(ChipsList.TAG,
                        "PriceComparison: onOptionsItemSelected():  invalid item "
                                + item.getItemId());
                break;
        }
        return true;

    }

    private void fillTable() {

        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String price;

        Log.v(ChipsList.TAG, "fillTable(): mastermode, itemNum, storeNum "
                + masterMode + " " + itemNum + " " + storeNum);

        /*
        if (masterMode) {
            f = myDB.getMasterItem(0, itemNum);
        } else {
            f = myDB.getItem(storeNum, itemNum);
        }*/

        String message = "default message";

        // clear out the table.
        TableLayout tl = (TableLayout) findViewById(R.id.table);
        tl.removeAllViews();

        ArrayList<StoreRecord> store = myDB.getAllStores();

        //get master information about the item...
        FoodRecord foodMaster = myDB.getMasterItem(0, itemNum);
        String itemName = foodMaster.getItemName();

        for (StoreRecord s : store) {
            PriceRecord p = myDB.getItemForStore(s.getStoreNumber(), itemNum);
            FoodRecord f = myDB.getItem(s.getStoreNumber(), itemNum);

            String mess = s.getStoreName() + ": ";
            if (p != null) {
                price = formatter.format(p.getPrice());

                if (p.getPrice() != (double) 0.0) {
                    mess += price + " ";
                } else {
                    mess += " No Price ";
                }
                if (!p.getSize().equals("")) {
                    mess += "per " + p.getSize();
                }
            } else {
                p = new PriceRecord(itemNum, s.getStoreNumber(), 0, 0, "");
                mess += " No Price";
            }

            TableRow row = new TableRow(ctx);

            CheckBox chk = new CheckBox(ctx);
            int id = Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android");
            chk.setButtonDrawable(id);
            if (f != null) {
                chk.setChecked(true);
            }

            /*if (storeNum == s.getStoreNumber())
                 chk.setChecked(true);
            */

            chk.setTag(p);
            row.addView(chk);

            TextView tv = new TextView(ctx);
            tv.setText(mess);
            tv.setTextSize(ChipsList.TEXT_FONT_SIZE);
    tv.setTextColor(getResources().getColor(R.color.Black));
            row.addView(tv);
            tl.addView(row);
        }


        message = "Move button will add " + itemName
                + " to checked stores, and will also remove the item"
                + " from the unchecked stores.";
        move.setText("Move " + itemName + "?");
        move.setVisibility(View.VISIBLE);

        TextView tv = (TextView) findViewById(R.id.bottom);
        tv.setText(message);
    }
}
