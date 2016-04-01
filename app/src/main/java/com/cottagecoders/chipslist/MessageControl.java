package com.cottagecoders.chipslist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MessageControl extends AppCompatActivity {

    Context ctx;
    Activity act;
    long searchDateTime;

    String number;

    //String body = "";

    // message components
    String fromNumber = "";
    String deviceId = "";
    String actionType = "";

    String theMessage;
    DatabaseCode myDB;

    static int nMsgParts;
    static boolean failed;
    static int MsgID = 0;
    final String SENT = "SENT";

    ArrayList<String> theSMSTexts = new ArrayList<>();

    private StoreRecord sr;
    private ArrayList<FoodRecord> foodRecords;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = getApplicationContext();
        setContentView(R.layout.message_control);

        //this.setTheme(R.style.CustomTheme);
        ctx = this.getApplicationContext();
        act = this;
        act.setTitle("SMS Receive");

        // get the low-level phone info we need...
        TelephonyManager tMgr = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
        fromNumber = tMgr.getLine1Number();
        deviceId = tMgr.getDeviceId();

        actionType = getIntent().getStringExtra(ChipsList.ACTION_TYPE);
        theMessage = getIntent().getStringExtra(ChipsList.INTENT_MESSAGE_TEXT);
        String toNumber = getIntent().getStringExtra(ChipsList.INTENT_DEST_NUMBER);

        myDB = new DatabaseCode(ctx);

        // the next 2 lines for testing only
        // searchDateTime = -1;
        //updateSharedPrefs();

        getSharedPrefs();

        Log.d(ChipsList.TAG, "MessageControl:  action type =" + actionType
                + "searchDateTime " + searchDateTime + " message: \""
                + theMessage + "\"");

        if (actionType.contains(ChipsList.SENDSMS)) {
            sendLongSmsMessage(toNumber, theMessage);
            goodBye();

        }

        if (actionType.contains(ChipsList.RETRIEVESMS)) {

            theSMSTexts.clear();
            retrieveData();

            // have any texts to work on?
            if (theSMSTexts.size() > 0) {
                dialogInstructions();
            } else {
                goodBye();
            }
        }
    }


    private void goodBye() {
        //   updateSharedPrefs();
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();

    }

    // ***************************************************************************************************************
    // debug routine used to see where the data resides
    // ***************************************************************************************************************
    public void getColumnsNames(Cursor c) {
        for (int i = 0; i < c.getColumnCount(); i++) {
            Log.d(ChipsList.TAG, " name of column " + c.getColumnName(i)
                    + " value " + c.getString(i));
        }
    }

    // *****************************************************************************************************************
    // retrieving the data from the MMS database via Uri
    // ******************************************************************************************************************
    public void retrieveData() {
        String localBody;
        //Log.d(ChipsList.TAG, "**** in retrieve data  " + searchDateTime);

        String selectionPart = "date > " + searchDateTime;
        String orderBy = "date";

        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cPart = ctx.getContentResolver().query(uri, null, selectionPart,
                null, orderBy);

        while (cPart.moveToNext()) {
            number = "";

            localBody = doSimplifiedRetrieve(cPart);

            if (localBody.contains("**ChipsList**")) {
                theSMSTexts.add(localBody);
            }
        }
        cPart.close();
    }

    public String doSimplifiedRetrieve(Cursor cursor) {

        String localBody = "";
        String[] columns = new String[]{"address", "thread_id", "date",
                "body", "type"};

        String type = cursor.getString(cursor.getColumnIndex(columns[4]));

        if (Integer.parseInt(type) == 1 || Integer.parseInt(type) == 2) {

            number = cursor.getString(cursor.getColumnIndex(columns[0]));

            localBody = cursor.getString(cursor.getColumnIndex(columns[3]));
            searchDateTime = Long.parseLong(cursor.getString(cursor
                    .getColumnIndex(columns[2])));
            updateSharedPrefs();

        }

        return localBody;
    }

    // ********************************************************************************************************************
    // get the sender telephone number
    // ********************************************************************************************************************
    private void getAddressNumber(int id) {
        number = "0";
        String uriStr2;
        uriStr2 = "content://sms/" + id + "/addr";

        String selectionAdd = "msg_id=" + id;

        Log.d(ChipsList.TAG, "getAddressNumber(): uriStr2 " + uriStr2);

        Uri uriAddress = Uri.parse(uriStr2);

        Cursor cAdd = getContentResolver().query(uriAddress, null,
                selectionAdd, null, null);

        if (cAdd == null) {
            Log.d(ChipsList.TAG,
                    "cursor in getaddress number is null  id = " + id
                            + " uistr " + uriStr2);
        } else {
            if (cAdd.moveToFirst()) {
                do {
                    // getColumnsNames(cAdd);
                    number = cAdd.getString(cAdd.getColumnIndex("address"));
                    if (number != null) {
                        // either 137 or 151
                        if (cAdd.getInt(cAdd.getColumnIndex("type")) == 137) {
                            // Log.d(ChipsList.TAG, "sender number is: " +
                            // number);
                            break;
                        }
                    }
                } while (cAdd.moveToNext());
            }
            cAdd.close();
        }
    }
    // ***********************************************************************************************************
    // for mms retrieve the message with input stream
    // **********************************************************************************************************

    private String getTheText(String id) {
        Boolean isNull = true;
        Uri partURI;

        partURI = Uri.parse("content://sms/part/" + id);

        InputStream is = null;
        StringBuilder sb = new StringBuilder();

        try {
            is = getContentResolver().openInputStream(partURI);
            if (is != null) {
                isNull = false;
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }

        } catch (IOException e) {
            Log.d(ChipsList.TAG, "getTheText(): getContentResolver().openInputStream() failed  " + e);
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.d(ChipsList.TAG, "is.close() failed. " + e);
                    e.printStackTrace();
                }
            }
        }
        if (isNull == true) {
            Log.d(ChipsList.TAG,
                    " in getTheText IsNull = true bad contentresolver "
                            + partURI);

        }
        return sb.toString();
    }

    public void getSharedPrefs() {
        SharedPreferences shared_preferences;
        SharedPreferences.Editor shared_preferences_editor;

        shared_preferences = ctx.getSharedPreferences(ChipsList.TAG,
                ctx.MODE_PRIVATE);
        searchDateTime = shared_preferences.getLong("date", -1);
        if (searchDateTime == -1)
            initSharedPrefs();

    }

    public void updateSharedPrefs() {

        SharedPreferences shared_preferences;
        SharedPreferences.Editor shared_preferences_editor;

        shared_preferences = ctx.getSharedPreferences(ChipsList.TAG,
                ctx.MODE_PRIVATE);

        // initialize for first time.
        shared_preferences_editor = shared_preferences.edit();
        shared_preferences_editor.putLong("date", searchDateTime);
        shared_preferences_editor.commit();

    }

    public void initSharedPrefs() {
        // find download date and time so we don't have to
        // search message database from beginning of time

        long installed = getDownLoadDateTime();

        // ************************************************************************************************************
        // we go back a week in case they received a text and then
        // downloaded the program  - we don't miss anything
        // ************************************************************************************************************
        if (installed != 0)
            installed = installed - (86400 * 1000) * 7;

        searchDateTime = installed;
        updateSharedPrefs();

    }

    public long getDownLoadDateTime() {
        long installed = 0;
        try {
            installed = ctx.getPackageManager().getPackageInfo(
                    "com.cottagecoders.chipslist", 0).firstInstallTime;
        } catch (Exception e) {
            // in case of failure we 'll just go with searchdatatime
        }

        return installed;
    }

    public void parseData() {

        String localBody = theSMSTexts.get(0);


        Parse parse = new Parse(ctx);
        sr = parse.decodeStoreRecord(localBody);

        // we're ready to parse the food records...
        foodRecords = new ArrayList<>();
        int start = 0;
        int end;
        while (true) {
            if (start >= localBody.length()) break;
            start = localBody.indexOf("^", start);
            if (start == -1) break;
            start += 1;

            end = localBody.indexOf("^", start + 1);
            if (end == -1) break;
            String temp = localBody.substring(start, end);
            start = end++;

            /*
            Log.d(ChipsList.TAG,
                    "parseData(): preparing to parse: start "
                            + start
                            + " end "
                            + end
                            + "\""
                            + temp
                            + "\"");
                            */
            foodRecords.add(parse.decodeFoodRecord(temp));
            start = end++;

        }
    }

    private void addRecords(StoreRecord sr, ArrayList<FoodRecord> food) {
        Log.d(ChipsList.TAG, "MessageControl: addRecords(): got here.");

        if (sr == null) {
            Log.d(ChipsList.TAG, " addRecords() sr is null");
        } else if (sr.getStoreName() == null) {
            Log.d(ChipsList.TAG, " addRecords() sr.getStoreName() is null");
        }

        // verify a matching store or create a new record...
        sr = myDB.matchStoreRecord(sr);

        // we're ready to insert the food record(s)...
        for (FoodRecord f : food) {
            f = myDB.matchFoodRecord(f);
            f.setStoreNum(sr.getStoreNumber());

            int itemNum = myDB.getItemNum(f.getItemName());
            myDB.moveItemToStore(itemNum, sr.getStoreNumber());
            myDB.updateEverything(itemNum, f.getItemName(), sr.getStoreNumber(),
                    f.getLocation(), f.getSize(), f.getPrice(),
                    f.isTaxed() ? 1 : 0,
                    f.getQuantity());
        }

    }

    private void sendLongSmsMessage(
            final String toNumber,
            final String theMessage) {

        // BroadcastReceiver is triggered when each part of the SMS has been sent
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // We need to make all the parts succeed before we say we have succeeded.
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.d(ChipsList.TAG, "SMS BroadcastReceiver: onReceive(): SUCCESS");
                        break;

                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "SMS BroadcastReceiver: onReceive(): Error - Generic failure", Toast.LENGTH_SHORT);
                        Log.d(ChipsList.TAG, "SMS BroadcastReceiver: onReceive(): Error - Generic failure");
                        failed = true;
                        break;

                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "SMS BroadcastReceiver: onReceive(): Error - No Service", Toast.LENGTH_SHORT);
                        Log.d(ChipsList.TAG, "SMS BroadcastReceiver: onReceive(): Error - No Service");
                        failed = true;
                        break;

                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "SMS BroadcastReceiver: onReceive(): Error - Null PDU", Toast.LENGTH_SHORT);
                        Log.d(ChipsList.TAG, "SMS BroadcastReceiver: onReceive(): Error - Null PDU");
                        failed = true;
                        break;

                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "SMS BroadcastReceiver: onReceive(): Error - Radio off", Toast.LENGTH_SHORT);
                        Log.d(ChipsList.TAG, "SMS BroadcastReceiver: onReceive(): Error - Radio off");
                        failed = true;
                        break;

                    default:
                        Log.d(ChipsList.TAG, "SMS BroadcastReceiver: onReceive(): UNNAMED failure");
                        failed = true;
                        break;

                }

                nMsgParts--;
                if (nMsgParts <= 0) {
                    // Stop us from getting any other broadcasts (may be for other messages)
                    Log.d(ChipsList.TAG, "SMS BroadcastReceiver: onReceive(): All message part responses received");
                    context.unregisterReceiver(this);

                    if (failed) {
                        Log.d(ChipsList.TAG, "SMS BroadcastReceiver: onReceive(): SMS Failure");
                    } else {
                        Log.d(ChipsList.TAG, "SMS BroadcastReceiver: onReceive(): SMS Success");
                    }
                    //TODO:  we should look into this.
                    // setResult(Activity.RESULT_OK);
                    goodBye();
                }
            }
        };


        MsgID++;

        getBaseContext().registerReceiver(broadcastReceiver, new IntentFilter(SENT + MsgID));

        SmsManager smsManager = SmsManager.getDefault();

        ArrayList<String> messageParts = smsManager.divideMessage(theMessage);
        ArrayList<PendingIntent> pendingIntents = new ArrayList<PendingIntent>(messageParts.size());
        nMsgParts = messageParts.size();
        failed = false;

        for (int i = 0; i < messageParts.size(); i++) {
            Intent sentIntent = new Intent(SENT + MsgID);
            pendingIntents.add(PendingIntent.getBroadcast(getBaseContext(), 0, sentIntent, 0));
        }

        Log.i(ChipsList.TAG, "sendLongSmsMessage(): About to send multi-part message Id: "
                + MsgID + " with " + nMsgParts + " parts");

        smsManager.sendMultipartTextMessage(toNumber, null, messageParts, pendingIntents, null);
        Log.d(ChipsList.TAG, "sendLongSmsMessage(): after sendMultiPartMessage()");
    }

    /*
 * mms replaced by post and sms private
 */
    private Intent tryOpenSMSConversation() {
        boolean isWorking = false;
        // Intent intent = new Intent(Intent.ACTION_MAIN);
        Intent intent = new Intent(Intent.ACTION_SENDTO);

        // DEFAULT ANDROID DEVICES
        intent.setComponent(new ComponentName("com.android.mms",
                "com.android.mms.ui.ConversationList"));
        Log.d(ChipsList.TAG, "trying conversationlist");
        isWorking = tryActivityIntent(this, intent);

        //1
        if (!isWorking) {
            // SAMSUNG DEVICES S3|S4|NOTE 2 etc.
            intent.setComponent(new ComponentName("com.android.mms",
                    "com.android.mms.ui.ConversationComposer"));
            Log.d(ChipsList.TAG, "trying conversationComposer");
            isWorking = tryActivityIntent(this, intent);
        }

        //2
        if (!isWorking) {
            // OPENS A NEW CREATE MESSAGE

            // intent = new Intent(Intent.ACTION_MAIN);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setType("vnd.android-dir/mms-sms");
            Log.d(ChipsList.TAG, "trying mms-sms");
            isWorking = tryActivityIntent(this, intent);
        }

        //3
        if (!isWorking) {
            intent = new Intent(Intent.ACTION_SEND);
            intent.setComponent(new ComponentName("com.android.mms",
                    "com.android.mms.ui.ComposeMessageActivity"));
            Log.d(ChipsList.TAG, "trying composemessageactivity");
            isWorking = tryActivityIntent(this, intent);
        }

        //4 - doh.
        if (!isWorking)
            return null;
        return intent;
    }


    public static boolean tryActivityIntent(Context context,
                                            Intent activityIntent) {

        // Verify that the intent will resolve to an activity
        try {
            if (activityIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(activityIntent);
                return true;
            }
        } catch (SecurityException e) {
            return false;
        }
        return false;
    }

    private void processReceivedSMSs() {

        if (theSMSTexts.size() > 0) {
            //wire the done button.
            Log.d(ChipsList.TAG, "processRecievedSMSs() - there are  " + theSMSTexts.size() + " to do.");
            findViewById(R.id.done).setEnabled(true);
            findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    findViewById(R.id.done).setEnabled(false);
                    TableLayout tl = (TableLayout) findViewById(R.id.table);
                    RadioGroup radioGroup = (RadioGroup) tl.getChildAt(0);

                    if (radioGroup.getCheckedRadioButtonId() != -1) {
                        int id = radioGroup.getCheckedRadioButtonId();
                        View radioButton = radioGroup.findViewById(id);
                        int radioId = radioGroup.indexOfChild(radioButton);
                        RadioButton rb = (RadioButton) radioGroup.getChildAt(radioId);

                        if (rb.isChecked()) {
                            parseData();
                            theSMSTexts.remove(0);

                            //need to get the storeRecord here for the one that's selected.
                            sr = (StoreRecord) rb.getTag();
                            Log.d(ChipsList.TAG, "done button - store is " + sr.getStoreName()
                                    + " theSMSTexts.size() " + theSMSTexts.size());
                            addRecords(sr, foodRecords);
                            if (theSMSTexts.size() > 0)
                                dialogInstructions();
                            else
                                goodBye();
                        }
                    }
                }
            });

            findViewById(R.id.done).setVisibility(View.INVISIBLE);
            fillTable();

        } else {
            goodBye();
        }
    }

    public void dialogInstructions() {

        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle("Select Store")
                .setMessage("SMS Received without a store - please select a store from the list.")
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                // User clicked OK so do some stuff
                                dialog.dismiss();
                                processReceivedSMSs();
                            }
                        }).create();
        dlg.show();
    }

    private void fillTable() {

        TableLayout table;
        table = (TableLayout) findViewById(R.id.table);

        // must clear table here.
        table.removeAllViews();

        RadioGroup radioGroup = new RadioGroup(ctx);
        ArrayList<StoreRecord> stores = myDB.getAllStores();
        for (StoreRecord s : stores) {
            TableRow row = new TableRow(ctx);
            row.setPadding(ChipsList.PADDING, ChipsList.PADDING,
                    ChipsList.PADDING, ChipsList.PADDING);

            RadioButton rb = new RadioButton(ctx);
            rb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    findViewById(R.id.done).setVisibility(View.VISIBLE);
                }
            });

                rb.setChecked(false);
            rb.setTag(s);


            rb.setTextSize(ChipsList.TEXT_FONT_SIZE);
            rb.setText(s.getStoreName());
            rb.setTextColor(getResources().getColor(R.color.Black));
            radioGroup.addView(rb);
            //row.addView(rb);
            //   table.addView(row);

        }
        table.addView(radioGroup);
    }
}
