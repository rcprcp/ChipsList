package com.cottagecoders.chipslist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.cottagecoders.chipslist.util.IabHelper;
import com.cottagecoders.chipslist.util.IabResult;
import com.cottagecoders.chipslist.util.Inventory;
import com.cottagecoders.chipslist.util.Purchase;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;

public class ChipsList extends AppCompatActivity {
    public static final String TAG = "ChipsList";

    //   public static final int RECOGNIZER = 1001;
    public static final int RECOGNIZE_ITEM = 1111;
    public static final int RECOGNIZE_LOCATION = 1112;
    public static final int RECOGNIZE_SIZE = 1113;
    public static final int RECOGNIZE_PRICE = 1114;
    public static final int RECOGNIZE_QUANTITY = 1115;
    // public static final int RECOGNIZE_SEQUENCE = 1116;

    // tags for the intents - these numbers are
    // returned when the intent finishes.
    // if it was started with startActivityForResult()
    public static final int INTENT_FIND_ITEM = 2001;
    public static final int INTENT_ADD_ITEM = 2002;
    // public static final int INTENT_UPDATE_STORE = 2003;
    // public static final int INTENT_UPDATE_OR_DELETE_STORE = 2004;
    public static final int INTENT_TRIP_SUMMARY = 2005;
    public static final int INTENT_DEPARTMENT_UPDATE = 2006;
    // public static final int INTENT_UPDATE_ITEM = 2007;
    // public static final int INTENT_DELETE_SHOPPING = 2008;
    public static final int INTENT_PRICE_COMPARE = 2009;
    // public static final int INTENT_SCANNER = 2010;
    public static final int INTENT_SHARE = 2011;
    public static final int INTENT_STORE = 2012;
    public static final int INTENT_MESSAGE_CONTROL = 1090;

    public static final int PLAY_STORE_REQUEST = 9876;


    public static final String INTENT_STORE_NUM = "intent_store_number_tag";
    // public static final String INTENT_STORE_NAME = "intent_store_name_tag";
    public static final String INTENT_ITEM_NUM = "intent_item_number_tag";
    public static final String INTENT_ITEM_NAME = "intent_item_name";
    public static final String INTENT_POSITION_TAG = "intent_position_tag";
    public static final String INTENT_AUTOCOMPLETE_STRING = "intent_autocomplete_string";
    public static final String INTENT_INITIAL_SIZE = "intent_initial_size";
    public static final String INTENT_INITIAL_PRICE = "intent_initial_price";
    public static final String INTENT_MESSAGE_TEXT = "intent_message_text";
    public static final String INTENT_DEST_NUMBER = "intent_dest_number";
    public static final String INTENT_DIALOG_REQUIRED = "intent_dialog_required";

    public static final String SENDSMS = "send_sms";
    public static final String RETRIEVESMS = "retrieve_sms";
    public static final String ACTION_TYPE = "action_type";

    public static final String HELP_STRING = "help_string";

    public static final int PADDING = 5;
    // padding... LR = left, right
    public static final int TABLE_SPACE_LR = 4;
    // padding... TB = top, bottom
    public static final int TABLE_SPACE_TB = 4;

    public static final String RCODE = "rcode";
    public static final String INTENT_RETURN_ITEM_NUMBER = "item_number";


    public static final int STORE_ORDER_BY_ITEM_NAME = 0;
    public static final int STORE_ORDER_BY_LOCATION = 1;
    public static final int STORE_ORDER_BY_SEQUENCE = 2;

    //public static final int IS_CHECKED = 1;
    //public static final int SORT_BY_ITEM_NAME = 0;
    //public static final int SORT_BY_LOCATION = 1;

    //  public static final float TITLE_FONT_SIZE = (float) 24.0;
    public static final float TEXT_FONT_SIZE = (float) 20.0;
    public static final float HELP_TEXT_FONT_SIZE = (float) 18.0;

    public static final int NOT_OK = 1;
    public static final int OK = 0;

    public static boolean hasSMS;


//    private static String SKU_UNLOCK = "triple.fusion.enable.beneficial.content";
//    private static final String b64EncodedKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwW6d5gWzA2hktg7OX4kDMBv6ZtFEmjLdEGbeBJBUB/Ea2kwvCWjrp7P1a+J3aFdI3SosHWywteAuCQh629r0Fv0+4475JQKqeBlAsXSLHVvpf6YBYP7maz+TJQKXHWRBezMtJeKB6zs9EbJQhq4/d9XmtVNZtRzG8DGQBb3AsZ0BZM2WwduSvN3HeQHsWH/kf49ftgra7plvb2r+9IrCBg5Y5hjFD3AK45Dm3D36tpvtgJD++BxhXtWR1nsHR6EYkVymd7OdF1c74+f92gfewhPNcrhXF6XpDmhrQuavZUDWpJ4gHaGb3j0wg0b82CWuYh7H/y2bpHiY4AsEAWUhUQIDAQAB";

    private static String SKU_UNLOCK = "remove.ads.from.chipslist";
    private static final String b64EncodedKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAy36N+rkq" +
            "H8OylFdBq+uKSfmIY8poxw/F48srzhm8A52BGDIT3uvhiWE6RWEmZRTlXCzoMrer0NVzs/PRbaAiv4Ryq" +
            "s8xa2kxyABHJ9A/RAosQAto7IPbdZMSHnhTg9rK2y0Ty94dM2XP+neueqc3lNmr4+JWD0hwaFC" +
            "waKq/ciae/V3DVUgKKGSTjjg0v/Vzzs0/Q5LqUuIhRjtRltB6NNlW00xXMAWHcqc2i77WzHbnh6g3" +
            "AOsTSYewG0t7hsdifUbe5f1n4ygi/acxksnMetzb8CqMA6n7Q13ixZSUTWCWUplq" +
            "UBtJ52HWO+z7dAz4DYdv43WcGmKbjFuaXEajEQIDAQAB";

    private static boolean showAds = true;
    private static boolean isIabEnabled = false;
    private static IabHelper iabHelper;
    private static boolean purchaseInProgress = false;

    public static Context ctx;
    private static Activity act;
    private static DatabaseCode myDB;
    private static boolean firstTime = true;
    private static ProgressDialog progress;
    private static InterstitialAd inter;

    private static AsyncTask<Void, Void, Integer> task = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.chips_list);
        progress = new ProgressDialog(this);
        progress.setTitle("Initializing");
        progress.setMessage("Initializing - please wait");
        progress.setCancelable(false);
        progress.setIndeterminate(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();

        ctx = this.getApplicationContext();
        act = this;

        act.setTitle("ChipsList");
        //this.setTheme(R.style.CustomTheme);
        task = new Initialize().execute();
    }


    private class Initialize extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "AsyncTask - onPreExecute");
            //nothing.
        }

        @Override
        protected Integer doInBackground(Void... Voids) {
            Log.d(TAG, "AsyncTask - doInBackground");

            myDB = new DatabaseCode(ctx);
            ArrayList<StoreRecord> stores = myDB.getAllStores();
            if (stores.size() == 0) {
                addNewStore(true);
            }

		    /* set up the stuff we need for In-App Billing. */
            iabHelper = new IabHelper(ctx, ChipsList.b64EncodedKey);
            iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (result.isSuccess()) {
                        iabHelper.queryInventoryAsync(queryFinishedListener);
                        isIabEnabled = true;
                    } else {
                        isIabEnabled = false;
                        Log.d(TAG,
                                "onCreate(): Problem setting up In-app Billing: "
                                        + result.getMessage());
                    }
                }
            });

           // test to determine if we have SMS available...
            Cursor cPart = null;
            try {
                Uri uri = Uri.parse("content://sms/inbox");
                cPart = ctx.getContentResolver().query(uri, null, null,
                        null, null);
            } catch(Exception e) {
                cPart = null;
            }

            Log.d(TAG, "cPart " + cPart);
            if (cPart == null) {
                hasSMS = false;

            } else {
                cPart.close();
                hasSMS = true;

            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.d(TAG, "AsyncTask - onPostExecute");

            showAds = myDB.getShowAds();
            if (showAds) {
                requestNewInterstitial();
                // enable the play store code...
                findViewById(R.id.fake_ad).setSelected(true);
                findViewById(R.id.fake_ad).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (!purchaseInProgress) {
                            purchaseInProgress = true;
                            iabHelper.launchPurchaseFlow(act, ChipsList.SKU_UNLOCK,
                                    ChipsList.PLAY_STORE_REQUEST, purchaseFinishedListener);
                        }
                    }
                });

            } else {
                findViewById(R.id.fake_ad).setVisibility(View.GONE);

            }

            fillTable();

            if (hasSMS)
                getSMS();

            progress.dismiss();
            findViewById(R.id.fake_ad).setVisibility(View.VISIBLE);

         }
    }

    // Callback when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener =
            new IabHelper.OnIabPurchaseFinishedListener() {
                public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                    Log.d(TAG, "Purchase finished: " + result + ", purchase: "
                            + purchase);

                    // if we were disposed of in the meantime, quit.
                    if (iabHelper == null) {
                        Log.d(TAG, "purchaseFinishedListener(): iabHelper is null");

                    } else if (result.isFailure()) {
                        Log.d(TAG, "purchaseFinishedListener(): result.isFailure().  getMessage(): "
                                + result.getMessage());

                    } else if (purchase.getSku().equals(ChipsList.SKU_UNLOCK)) {
                        // bought the premium upgrade!
                        findViewById(R.id.fake_ad).setVisibility(View.GONE);
                        showAds = false;
                        myDB.setShowAds(false);
                        Log.d(TAG, "purchaseFinishedListener(): purchased: " + SKU_UNLOCK
                                + " showAds  " + showAds);
                    }
                    purchaseInProgress = false;

                }
            };

    // callback when a query for inventory is finished.
    IabHelper.QueryInventoryFinishedListener queryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {
            if (result.isFailure()) {
                Log.d(TAG,
                        "queryFinishedListener(): Query Inventory error " + result + " "
                                + result.getMessage());
            } else {
                boolean boughtIt = inventory.hasPurchase(SKU_UNLOCK);
                // boughtIt and showAds are opposite of one another.
                // showAds is true to indicate we need to showAds.
                // boughtIt tells if they bought the upgrade.
                showAds = !boughtIt;
                myDB.setShowAds(showAds);
                Log.d(TAG, "queryFinishedListener(): purchased: " + SKU_UNLOCK
                        + "??  boughtIt: " + boughtIt + "  " +
                        " showAds  " + showAds);
            }
        }
    };

    private void requestNewInterstitial() {

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("A6013A53A1FE2C6963442E61764D1945")
                .addTestDevice("B3B37744EA7552599DE790E2E5FA8757")
                .addTestDevice("7D4826863B8F7F80C3715162F9AF225F")
                .build();

        inter = new InterstitialAd(this);
        inter.setAdUnitId("ca-app-pub-8038632706967130/6057547606");
        inter.loadAd(adRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult(): -- got here. requestCode "
                + requestCode + " resultCode " + resultCode);

        if (requestCode == PLAY_STORE_REQUEST) {
            // Pass on the activity result to the helper for handling
            if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
                // not handled, so handle it ourselves (here's where you'd
                // perform any handling of activity results not related to in-app
                // billing...
                super.onActivityResult(requestCode, resultCode, data);
            } else {
                Log.d(TAG, "onActivityResult handled by IABUtil.");
            }
        } else if (requestCode == INTENT_STORE) {
            if (showAds) {
                Log.d(ChipsList.TAG, "showads is true.");

                if (inter != null) {
                    if (inter.isLoaded()) {
                        Log.d(ChipsList.TAG, "inter is losded -- show it. ");
                        inter.show();
                        requestNewInterstitial();

                        inter.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                // nothing here, for now.
                            }
                        });

                    } else if (!inter.isLoading()) {
                        Log.d(ChipsList.TAG, "inter is not loading");
                        requestNewInterstitial();
                    }

                } else {
                    Log.d(ChipsList.TAG, "start loading new interstitial");
                    requestNewInterstitial();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chips_list, menu);
        getMenuInflater().inflate(R.menu.action_bar_manage_stores, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.retrieve_SMS);

        if (!hasSMS) {
            // disabled
            item.setEnabled(false);
            item.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
        switch (item.getItemId()) {

            case R.id.action_bar_add:
                addNewStore(false);
                return true;

            case R.id.about:
                intent = new Intent(ctx, About.class);
                startActivity(intent);
                return true;

            case R.id.help:
                intent = new Intent(ctx, Help.class);
                intent.putExtra(HELP_STRING, getString(R.string.HELP_chipslist));
                startActivity(intent);
                return true;

            case R.id.retrieve_SMS:
                // check if there are any ChipsList messages in the SMS inbox:
                getSMS();
                return true;

            default:
                Log.i(TAG, "onOptionsItemSelected():  default case - item " + item.getItemId());
                break;
        }
        return true;

    }

    private void getSMS() {
        Log.i(TAG, "getSMS(): got here.");

        // check if there are any ChipsList messages in the SMS inbox:
        Intent sendInfo = new Intent(ctx, MessageControl.class);
        sendInfo.putExtra(ChipsList.ACTION_TYPE, ChipsList.RETRIEVESMS);
        startActivityForResult(sendInfo, INTENT_MESSAGE_CONTROL);

    }

    private void addNewStore(boolean popup) {
        // start activity to add a store.
        Intent intent = new Intent(ctx, AddStore.class);
        intent.putExtra(INTENT_DIALOG_REQUIRED, popup);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "ChipsList  onStart called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "ChipsList onPause called");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG, "ChipsList  onResume called");
        if(!(task.getStatus() == AsyncTask.Status.RUNNING)) {
            fillTable();
        }
        // progress.dismiss();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "ChipsList onStop called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "ChipsList onDestroy called");
        // stopService(myService);
    }

    private void fillTable() {

        TableLayout table;
        table = (TableLayout) findViewById(R.id.mainTable);
        table.removeAllViews();

        ArrayList<StoreRecord> stores = myDB.getAllStores();
        for (StoreRecord s : stores) {

            String name = s.getStoreName();
            int i = myDB.numberOfItemsForStore(s.getStoreNumber());
            if (i > 0) {
                String it = " items)";
                if (i == 1) {
                    it = " item)";
                }
                name += "    (" + i + it;
            }
            TextView tv = new TextView(ChipsList.ctx);
            tv.setTextColor(getResources().getColor(R.color.Black));
            tv.setTextSize(ChipsList.TEXT_FONT_SIZE);
            tv.setPadding(ChipsList.PADDING, ChipsList.PADDING,
                    ChipsList.PADDING, ChipsList.PADDING );
            tv.setText(name);
            tv.setTag(s);

            // create click handler...
            tv.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    StoreRecord s = (StoreRecord) v.getTag();
                    Intent intent = new Intent(ctx, Store.class);
                    intent.putExtra(INTENT_STORE_NUM, s.getStoreNumber());
                    startActivityForResult(intent, INTENT_STORE);
                }
            });

            // create long click handler...
            tv.setLongClickable(true);
            tv.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    StoreRecord s = (StoreRecord) v.getTag();

                    Intent intent = new Intent(ctx, UpdateStore.class);
                    intent.putExtra(INTENT_STORE_NUM, s.getStoreNumber());
                    startActivity(intent);
                    return true;
                }
            });

            // put this stuff into the row...
            TableRow row = new TableRow(ChipsList.ctx);
            row.addView(tv);
            table.addView(row);
        }
    }
}
