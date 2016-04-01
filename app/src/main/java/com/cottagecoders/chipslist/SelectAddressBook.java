package com.cottagecoders.chipslist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SelectAddressBook extends AppCompatActivity {

    Context ctx;
    Activity act;

    String theMessage;

    String[] contactName;
    String[] contactNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.selectadressbook);
        ctx = getApplicationContext();
        act = this;
        act.setTitle(getString(R.string.share));

        theMessage = getIntent().getStringExtra(ChipsList.INTENT_MESSAGE_TEXT);
    }

    @Override
    protected void onResume() {

        super.onResume();
        //fill_table();
        new FillTable().execute();
    }

    private class FillTable extends AsyncTask<String, Void, String> {

        ProgressBar spinner;
        @Override
        protected void onPostExecute(String result) {
            fill_table();
            spinner.setVisibility(View.INVISIBLE);
           }

        @Override
        protected void onPreExecute() {

            spinner = (ProgressBar) findViewById(R.id.progress);
            spinner.setIndeterminate(true);
            spinner.setVisibility(View.VISIBLE);

        }


        @Override
        protected String doInBackground(String... params) {
            getInfo();
            return "";
        }
    }

    protected void onActivityResult(int RequestCode, int ResultCode, Intent data) {

        Log.d(ChipsList.TAG, "onActivityResult(): Requestcode  "
                + RequestCode
                + " ResultCode "
                + ResultCode);

        setResult(RESULT_OK);
        finish();

    }

    private void getInfo() {

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor people = getContentResolver().query(uri, projection, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        contactName = new String[people.getCount()];
        contactNumber = new String[people.getCount()];

        int indexName = people
                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = people
                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        Log.d(ChipsList.TAG,
                "SelectAddressBook: getInfo(): number of people "
                        + people.getCount());

        if(people.getCount() == 0 ) {
            setResult(RESULT_CANCELED);
            finish();
        }

        int elementCount = 0;
        contactName[elementCount] = "This Phone";
        TelephonyManager tele = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        contactNumber[elementCount] = tele.getLine1Number();
        elementCount++;

        people.moveToFirst();
        do {
            String name = people.getString(indexName).trim();
            String number = people.getString(indexNumber).trim();
            Log.d(ChipsList.TAG, "getInfo():  name " + name + " number " + number);
            if (number.length() == 0) {
                continue;
            }

            contactNumber[elementCount] = PhoneNumberUtils
                    .stripSeparators(number);

            if (contactNumber[elementCount].length() > 9) {
                contactName[elementCount] = name;
                elementCount++;
            }

        } while (people.moveToNext());
        people.close();


    }

    /**
     * fill the table with the contact list and allow user to select one
     *
     */
    private void fill_table() {

        TableLayout theTable = (TableLayout) findViewById(R.id.table);
        theTable.removeAllViews();

        for (int i = 0; i < contactNumber.length; i++) {
            addToScreen(contactName[i], contactNumber[i], i, theTable);
        }
    }

    public void addToScreen(String name, String number, int rowCount,
                            TableLayout tab) {
        Log.d(ChipsList.TAG, "addtoScreen(): " + name + " number " + number);
        if (number == null || number.length() == 0) {
            return;
        }

        TableRow addressRow = new TableRow(ctx);
        makeScreenField(name, addressRow, rowCount);
        tab.addView(addressRow);

        TableRow addressRow2 = new TableRow(ctx);
        makeScreenField("     " + PhoneNumberUtils.formatNumber(number),
                addressRow2, rowCount);
        tab.addView(addressRow2);

    }

    private void makeScreenField(String name, TableRow addressRow, int rowCount) {
        TextView textField = new TextView(ctx);
        textField.setText(name);

        textField.setTextSize(ChipsList.TEXT_FONT_SIZE);
        textField.setTextColor(getResources().getColor(R.color.Black));
        textField.setTag(rowCount);

        textField.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                int addressIndex = (Integer) v.getTag();
                dialogReallySend(addressIndex);
            }
        });

        addressRow.addView(textField);
    }

    /**
     *  send the sms through the message control class
     */
    public void finishAndSend(int theIndex) {
        Log.d(ChipsList.TAG, "finishAndSend(): got here.");

        Intent sendInfo = new Intent(this, MessageControl.class);
        sendInfo.putExtra(ChipsList.INTENT_DEST_NUMBER, contactNumber[theIndex]);
        sendInfo.putExtra(ChipsList.ACTION_TYPE, ChipsList.SENDSMS);
        sendInfo.putExtra(ChipsList.INTENT_MESSAGE_TEXT, theMessage);

        startActivityForResult(sendInfo, 1090);
    }

    public void dialogReallySend(final int index) {
        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle(getString(R.string.really_send_title))
                .setMessage(getString(R.string.really_send_part1)
                        + contactName[index] + " (" + PhoneNumberUtils.formatNumber(contactNumber[index]) + ")?")
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                // User clicked OK so do some stuff
                                finishAndSend(index);
                                dialog.dismiss();
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
}
