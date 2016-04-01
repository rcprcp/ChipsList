package com.cottagecoders.chipslist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class Store extends AppCompatActivity {
    static int saved;

    static Context ctx;
    static Activity act;
    static DatabaseCode myDB;
    static int storeNum;
    TableLayout table;
    TextView titleView;
    double cost = 0.0;
    int checkedItems = 0;
    int totalItems = 0;
    NumberFormat nf;
    static Dialog dialog;

    static int itemNum;
    static String itemName;

    static int positionInTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(ChipsList.TAG, "store in onCreate()");

        setContentView(R.layout.store);

        ctx = this.getApplicationContext();
        act = this;


        // get the extra info we were started with -
        // this should be the store number we're working with.
        storeNum = getIntent().getIntExtra(ChipsList.INTENT_STORE_NUM, 1);

        // open database connection...
        myDB = new DatabaseCode(ctx);

        StoreRecord sr = myDB.getStoreInfo(storeNum);
        if (sr != null) {
            act.setTitle(sr.getStoreName());
        } else {
            act.setTitle("Invalid Store");
        }

        try {
            ActionBar a = getSupportActionBar();
            a.setDisplayHomeAsUpEnabled(true);

        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getSupportedActiobBar() failed " + e);
            e.printStackTrace();
            System.exit(1);
        }

        table = (TableLayout) findViewById(R.id.table);
        titleView = (TextView) findViewById(R.id.titleView);
        nf = new DecimalFormat("####.##");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_store, menu);
        getMenuInflater().inflate(R.menu.store_menu, menu);
        if (!ChipsList.hasSMS || numberOfContacts() == 0) {
            //disable share button on the actionbar.
            MenuItem item = menu.findItem(R.id.action_bar_share_store_list);
            item.setVisible(false);
        }

        return true;
    }
    private int numberOfContacts() {

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor people = getContentResolver().query(uri, projection, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        Log.d(ChipsList.TAG,
                "Store: numberOfContacts(): number of contacts "
                        + people.getCount());
        int count = people.getCount();
        people.close();
        return count;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Intent intent;
        switch (item.getItemId()) {

            case R.id.action_bar_add_item:
                intent = new Intent(ctx, FindItem.class);
                intent.putExtra(ChipsList.INTENT_STORE_NUM, storeNum);
                intent.putExtra(ChipsList.INTENT_POSITION_TAG, positionInTable);
                startActivityForResult(intent, ChipsList.INTENT_FIND_ITEM);
                break;

            case R.id.action_bar_share_store_list:
                StoreRecord sr = myDB.getStoreInfo(storeNum);
                Parse parse = new Parse(ctx);
                String theMessage = parse.encode(sr);

                Log.d(ChipsList.TAG, "Store: share: theMessage: " + theMessage);
                intent = new Intent(ctx, SelectAddressBook.class);
                intent.putExtra(ChipsList.INTENT_MESSAGE_TEXT, theMessage);
                startActivityForResult(intent, ChipsList.INTENT_SHARE);
                break;

            case R.id.action_bar_trip_summary:
                intent = new Intent(ctx, TripSummary.class);
                intent.putExtra(ChipsList.INTENT_STORE_NUM, storeNum);
                startActivityForResult(intent, ChipsList.INTENT_TRIP_SUMMARY);
                break;

            case R.id.store_remove_checked:
                // count the checked records:
                int count = 0;
                TableLayout tl = (TableLayout) findViewById(R.id.table);
                for (int i = 0; i < tl.getChildCount(); i++) {
                    TableRow row = (TableRow) tl.getChildAt(i);
                    CheckBox chk;
                    try {
                        // not every row has a check box - so we may get
                        chk = (CheckBox) row.getChildAt(0);
                        // an exception...
                    } catch (ClassCastException e) {
                        continue; // next row, please.
                    }
                    if (chk.isChecked()) {
                        count++;
                    }
                }
                if (count > 0) {
                    dialogRemoveChecked(count);
                } else {
                    Toast.makeText(ctx, "No checked items.", Toast.LENGTH_LONG)
                            .show();
                }

                break;

            case R.id.store_clear_checked:
                count = 0;
                tl = (TableLayout) findViewById(R.id.table);
                for (int i = 0; i < tl.getChildCount(); i++) {
                    TableRow row = (TableRow) tl.getChildAt(i);
                    CheckBox chk;
                    try {
                        // not every row has a check box - so we may get
                        chk = (CheckBox) row.getChildAt(0);
                        // an exception...
                    } catch (ClassCastException e) {
                        continue; // next row, please.
                    }
                    if (chk.isChecked()) {
                        count++;
                    }
                }

                if (count > 0) {
                    dialogClearChecked();
                } else {
                    Toast.makeText(ctx, "No checked items.", Toast.LENGTH_LONG)
                            .show();
                }

                break;

            case R.id.store_set_sort_order:
                dialogStoreSort();
                break;

            case R.id.store_remove_all:
                dialogRemoveAll();
                break;

            case android.R.id.home:
                setResult(RESULT_OK, getIntent());
                finish();
                break;

            case R.id.department_update:
                intent = new Intent(ctx, UpdateDepartment.class);
                intent.putExtra(ChipsList.INTENT_STORE_NUM, storeNum);
                startActivityForResult(intent, ChipsList.INTENT_DEPARTMENT_UPDATE);
                break;

            case R.id.help:
                intent = new Intent(ctx, Help.class);
                intent.putExtra(ChipsList.HELP_STRING,
                        getString(R.string.HELP_store));
                startActivity(intent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ChipsList.INTENT_FIND_ITEM) {
            int positionInTable = data.getIntExtra(ChipsList.INTENT_POSITION_TAG, 0);
            Log.v(ChipsList.TAG, "Store:: onActivityResult(): positionInTable = " + positionInTable);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(ChipsList.TAG, "Store   onStart called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(ChipsList.TAG, "Store  onPause called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(ChipsList.TAG, "Store   onResume called");
        if (table != null) {
            fillTable();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(ChipsList.TAG, "Store  onStop called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(ChipsList.TAG, "Store  onDestroy called");
    }

    private void fillTable() {

        // must clear table here.
        if (table != null)
            table.removeAllViews();

        cost = 0.0;
        checkedItems = 0;
        totalItems = 0;
        //updateSummary();

        ArrayList<FoodRecord> food;

        int sortOrder = myDB.getStoreSort();
        if (sortOrder == ChipsList.STORE_ORDER_BY_ITEM_NAME) {
            food = myDB.getAllFoodAlpha(storeNum);

        } else if (sortOrder == ChipsList.STORE_ORDER_BY_LOCATION
                || sortOrder == ChipsList.STORE_ORDER_BY_SEQUENCE) {
            food = myDB.getAllFoodNotAlpha(storeNum, sortOrder);


        } else {
            Log.e(ChipsList.TAG, "store:fillTable(): invalid sortOrder = "
                    + sortOrder + " forced to Alpha.");
            food = myDB.getAllFoodAlpha(storeNum);
        }

        String dept = "";

        // check if there are any items on any list -
        // if no items, give a hint via dialog box.
        if (myDB.countTotalShoppingListItems() == 0) {
            dialogNoItems();
            return;
        }

        for (FoodRecord f : food) {

            //this chunk of code inserts breaks between the sections in the list
            if (sortOrder == ChipsList.STORE_ORDER_BY_LOCATION
                    || sortOrder == ChipsList.STORE_ORDER_BY_SEQUENCE) {

                if (!dept.equals(f.getLocation())) {
                    // do not use our own myTableRow() method because we
                    // don't want this row to be clickable.
                    TableRow row = new TableRow(ctx);

                    // this blank string takes the place of the CheckBox.
                    TextView tv = myTextView();
                    tv.setText(" ");
                    row.addView(tv);

                    // this displays the dept name - in place of the food item.
                    tv = myTextView();
                    String title = f.getLocation();
                    tv.setText(title);

                    tv.setBackgroundColor(getResources().getColor(R.color.LightGray));
                    tv.setTextColor(getResources().getColor(R.color.Black));
                    // add the to the table...
                    row.addView(tv);
                    table.addView(row);

                    // set location for next time around...
                    dept = f.getLocation();
                }
            }

            ArrayList<String> theText = new ArrayList<>();

            if (f.getQuantity() > 1.0) {
                theText.add("(" + nf.format(f.getQuantity()) + ") ");
            }

            theText.add(f.getItemName());

            if (sortOrder == ChipsList.STORE_ORDER_BY_ITEM_NAME) {
                theText.add("--");
                theText.add(f.getLocation().trim());
            }


            totalItems++;
            if (f.isChecked()) {
                checkedItems++;
                cost += f.getPrice() * f.getQuantity();
            }

            if (f.getPrice() > 0.0) {
                NumberFormat formatter;
                formatter = NumberFormat.getCurrencyInstance();
                theText.add(formatter.format(f.getPrice()).trim());

                if (f.isTaxed()) {
                    theText.add("+Tax");
                }

                if (f.getQuantity() > 0 && (!f.getSize().trim().equals(""))) {
                    theText.add("Per ");
                    theText.add(f.getSize().trim());
                }
            } else {
                theText.add(" " + f.getSize().trim());
            }

            TextView dummyTv = build(true, "");

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            //     int height = displaymetrics.heightPixels;
            int width = displaymetrics.widthPixels;

            int width85pct = (int) ((double) width * 0.85);

            boolean needCheckbox = true;
            boolean inColor = false;
            if (f.getPrice() > (float) 0.0) {
                inColor = true;
            }

            String temp = "";
            for (String t : theText) {
                temp += " " + t;

                if (textLength(dummyTv, temp) > width85pct) {
                    // line too long, display it.
                    TableRow row = myTableRow(f);
                    if (needCheckbox) {
                        CheckBox chk = myCheckBox(f);
                        row.addView(chk);
                        needCheckbox = false;
                    }

                    TextView tv = build(inColor, "");
                    row.addView(tv);
                    table.addView(row);
                    tv.setText(temp);
                    temp = "";
                }
            }

            if (temp.trim().equals("")) {
                //nothing
            } else {
                // got left over data, and we need to display it..
                TableRow row = myTableRow(f);
                if (needCheckbox) {
                    CheckBox chk = myCheckBox(f);
                    row.addView(chk);
                    needCheckbox = false;
                } else {
                    TextView chkReplacement = build(inColor, "");
                    row.addView(chkReplacement);
                }

                TextView tv = build(inColor, "");
                row.addView(tv);
                table.addView(row);
                tv.setText(temp);
            }
        }
        updateSummary();
    }

    private TextView build(boolean inColor, String stuff) {
        TextView tv = myTextView();
        if (inColor) {
            tv.setTextColor(getResources().getColor(R.color.ForestGreen));
            Typeface face = tv.getTypeface();
            tv.setTypeface(face, Typeface.ITALIC);
        } else {
            tv.setTextColor(getResources().getColor(R.color.Black));
        }
        tv.setPadding(ChipsList.PADDING, ChipsList.PADDING,
                ChipsList.PADDING, ChipsList.PADDING );
        tv.setText(stuff);
        return tv;
    }

    private void dialogStoreSort() {

        String[] sortModes = {getString(R.string.sort_alpha),
                getString(R.string.sort_location),
                getString(R.string.sort_sequence)};

        AlertDialog.Builder b = new AlertDialog.Builder(act)
                .setTitle(R.string.select_sort)
                .setSingleChoiceItems(sortModes, myDB.getStoreSort(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            // user clicked some item - we get here.
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                saved = which;

                            }
                        })
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                // User clicked OK so do some stuff
                                myDB.setStoreSort(saved);
                                dialog.dismiss();
                                fillTable();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                /* Handle cancel clicked */
                                dialog.dismiss();
                            }
                        });
        b.create();
        b.show();

    }

    CheckBox myCheckBox(FoodRecord f) {
        // create the checkbox...
        CheckBox chk = new CheckBox(ctx);
        chk.setChecked(f.isChecked());
         int id = Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android");
        chk.setButtonDrawable(id);

     //   chk.setBackgroundColor(getResources().getColor(R.color.LightBlue));
     //   chk.setTextColor(getResources().getColor(R.color.Black));
     //   chk.setButtonDrawable(R.drawable.check_box_selector);
      //  chk.setScaleX((float) .3);
       // chk.setScaleY((float) .3);
       // chk.setHeight(100);
       // chk.setWidth(100);

        // create checkbox check/un-check listener.
        chk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // get here when checkbox is clicked some time in the
                // future....
                FoodRecord f = (FoodRecord) v.getTag();
                int itemNum = f.getItemNum();
                int storeNum = f.getStoreNum();

                int val = 0;
                CheckBox b = (CheckBox) v;
                if (b.isChecked()) {
                    val = 1;
                    checkedItems++;
                    cost += f.getPrice() * f.getQuantity();
                } else {
                    checkedItems--;
                    cost -= f.getPrice() * f.getQuantity();
                }
                myDB.toggleItem(storeNum, itemNum, val);
                updateSummary();
            }
        });

        chk.setTag(f);
        return chk;
    }

    TableRow myTableRow(FoodRecord f) {
        // click handler for the row...
        TableRow row = new TableRow(ctx);

        // get the row ready...
        row.setTag(f);
        row.setClickable(true);

        row.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                FoodRecord f = (FoodRecord) v.getTag();

                Intent intent = new Intent(ctx, UpdateItem.class);
                intent.putExtra(ChipsList.INTENT_STORE_NUM, f.getStoreNum());
                intent.putExtra(ChipsList.INTENT_ITEM_NUM, f.getItemNum());
                startActivity(intent);

            }
        });

        row.setLongClickable(true);
        row.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                FoodRecord f = (FoodRecord) v.getTag();
                Intent intent = new Intent(ctx, DeleteCompareMenu.class);
                intent.putExtra(ChipsList.INTENT_ITEM_NUM, f.getItemNum());
                intent.putExtra(ChipsList.INTENT_ITEM_NAME, f.getItemName());
                intent.putExtra(ChipsList.INTENT_STORE_NUM, storeNum);
                startActivity(intent);
                return true;
            }
        });

        return row;
    }

    private TextView myTextView() {
        TextView tv = new TextView(ctx);
        tv.setTextSize(ChipsList.TEXT_FONT_SIZE);
        return tv;
    }

    private void updateSummary() {

        NumberFormat formatter;
        formatter = NumberFormat.getCurrencyInstance();
        String price;
        price = formatter.format(cost);

        titleView.setTextColor(getResources().getColor(R.color.Black));
        titleView.setText(checkedItems + " of " + totalItems + " Checked.  " + price
                + " For Checked Items");

        titleView.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                Intent intent = new Intent(ctx, TripSummary.class);
                intent.putExtra(ChipsList.INTENT_STORE_NUM, storeNum);
                startActivity(intent);
            }
        });
    }

    private void dialogNoItems() {

        AlertDialog.Builder b = new AlertDialog.Builder(act)
                .setTitle(R.string.no_items)
                .setMessage(R.string.no_items_mess)
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

    public void dialogRemoveChecked(int count) {

        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle(getString(R.string.dialog_remove_checked_title))
                .setMessage("Remove " + count + " checked items?")
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                // User clicked OK so do some stuff
                                myDB.removeCheckedItemsFromStore(storeNum);
                                dialog.dismiss();
                                fillTable();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                // User clicked Cancel, so do nothing
                                dialog.dismiss();
                            }
                        }).create();
        dlg.show();
    }

    public void dialogClearChecked() {

        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle("Uncheck Items?")
                .setMessage("Uncheck the checked items?")
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                // User clicked OK so do some stuff

                                TableLayout tl = (TableLayout) findViewById(R.id.table);
                                for (int i = 0; i < tl.getChildCount(); i++) {
                                    TableRow row = (TableRow) tl.getChildAt(i);
                                    CheckBox chk;
                                    try {
                                        // not every row has a check box - so we
                                        // may get
                                        chk = (CheckBox) row.getChildAt(0);
                                        // an exception...
                                    } catch (ClassCastException e) {
                                        continue; // next row, please.
                                    }
                                    if (chk.isChecked()) {
                                        FoodRecord f = (FoodRecord) chk
                                                .getTag();
                                        myDB.toggleItem(f.getStoreNum(),
                                                f.getItemNum(), 0);
                                    }
                                }

                                dialog.dismiss();
                                fillTable();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                // User clicked Cancel, so do nothing
                                dialog.dismiss();
                            }
                        }).create();
        dlg.show();
    }

    public void dialogRemoveAll() {

        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle(getString(R.string.dialog_remove_all_title))
                .setMessage("Remove ALL items from the list?")
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                // User clicked OK so do some stuff
                                myDB.removeStoresItemsFromList(storeNum);
                                dialog.dismiss();
                                fillTable();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                // User clicked Cancel, so do nothing.
                                dialog.dismiss();
                            }
                        }).create();
        dlg.show();
    }

    private int textLength(TextView text, String newText) {
        float textWidth = text.getPaint().measureText(newText);
        return (int) textWidth;
    }
}
