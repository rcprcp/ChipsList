package com.cottagecoders.chipslist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;

public class AddItem extends ActionBarActivity {

    static Context ctx;
    static Activity act;
    static int storeNum;
    DatabaseCode myDB;

    EditText itemName;
    AutoCompleteTextView location;
    EditText pkg_sz;
    EditText price;
    EditText quantity;
    CheckBox tax;

    boolean changed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_item);
        Log.i(ChipsList.TAG, "AddItem - onCreate() got here");

        ctx = this.getApplicationContext();
        act = this;

        myDB = new DatabaseCode(ctx);
        storeNum = getIntent().getIntExtra(ChipsList.INTENT_STORE_NUM, 1);

        StoreRecord sr = myDB.getStoreInfo(storeNum);
        String storeName = sr.getStoreName();

        act.setTitle(getString(R.string.add_item) + " " + storeName + " List");
        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);

        itemName = (EditText) findViewById(R.id.item);
        // this was passed - the user tried autocomplete to lookup an item, or
        // the item name has been looked up by the Barcode Scanner.
        String temp = getIntent().getStringExtra(
                ChipsList.INTENT_AUTOCOMPLETE_STRING);
        Log.d(ChipsList.TAG, "AddItem(): temp \"" + temp + "\"");
        itemName.setText(temp);
        itemName.addTextChangedListener(watcher);

        location = (AutoCompleteTextView) findViewById(R.id.location);

        ArrayList<DepartmentRecord> dept = myDB.getMasterListDepartments();
        String[] departments = new String[dept.size()];
        int i = 0;
        for (DepartmentRecord d : dept) {
            departments[i] = d.getLocation();
            i++;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, departments);
        location.setAdapter(adapter);

        location.setThreshold(1);
        location.addTextChangedListener(watcher);

        pkg_sz = (EditText) findViewById(R.id.pkg_sz);
        temp = getIntent().getStringExtra(ChipsList.INTENT_INITIAL_SIZE);
        pkg_sz.setText(temp);
        pkg_sz.addTextChangedListener(watcher);

        price = (EditText) findViewById(R.id.price);
        double dtemp = getIntent().getDoubleExtra(
                ChipsList.INTENT_INITIAL_PRICE, 0.0);
        price.setText(Double.toString(dtemp));
        price.addTextChangedListener(watcher);

        quantity = (EditText) findViewById(R.id.quantity);
        quantity.addTextChangedListener(watcher);

        itemName.requestFocus();

        // set up all the voice buttons...
        ImageButton voiceLocation = (ImageButton) findViewById(R.id.voice_location);
        voiceLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startVoice(ChipsList.RECOGNIZE_LOCATION,
                        getString(R.string.voice_prompt_location));
            }
        });

        ImageButton voiceItem = (ImageButton) findViewById(R.id.voice_item);
        voiceItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startVoice(ChipsList.RECOGNIZE_ITEM,
                        getString(R.string.voice_prompt_item));
            }
        });

        ImageButton voicepkg_sz = (ImageButton) findViewById(R.id.voice_size);
        voicepkg_sz.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startVoice(ChipsList.RECOGNIZE_SIZE,
                        getString(R.string.voice_prompt_size));

            }
        });

        ImageButton voicePrice = (ImageButton) findViewById(R.id.voice_price);
        voicePrice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startVoice(ChipsList.RECOGNIZE_PRICE,
                        getString(R.string.voice_prompt_price));

            }
        });

        ImageButton voiceQuantity = (ImageButton) findViewById(R.id.voice_quantity);
        voiceQuantity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startVoice(ChipsList.RECOGNIZE_QUANTITY,
                        getString(R.string.voice_prompt_quantity));

            }
        });

        Button save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mySave();
            }
        });
    }

    TextWatcher watcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            changed = true;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable ed) {

        }
    };

    @Override
    public void onStart() {
        super.onStart();
        Log.i(ChipsList.TAG, "AddItem  onStart called");
    }

    @Override
    public void onBackPressed() {
        Log.i(ChipsList.TAG, "AddItem onBackPressed called.  changed = "
                + changed);
        if (changed) {
            dialogUnsavedChanges();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(ChipsList.TAG, "AddItem onPause called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(ChipsList.TAG, "AddItem  onResume called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(ChipsList.TAG, "AddItem onStop called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(ChipsList.TAG, "AddItem onDestroy called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.help_only, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(quantity.getWindowToken(), 0);

        // Handle presses on the action bar items
        Intent intent;
        switch (item.getItemId()) {

            case android.R.id.home:
                if (changed) {
                    dialogUnsavedChanges();
                    break;
                }

                // hide pop up keyboard.
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(quantity.getWindowToken(), 0);

                setResult(RESULT_OK, getIntent());
                finish();
                break;

            case R.id.help:
                intent = new Intent(ctx, Help.class);
                intent.putExtra(ChipsList.HELP_STRING,
                        getString(R.string.HELP_add_item));
                startActivity(intent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(ChipsList.TAG, "AddItem - onActivityResult(): requestCode "
                + requestCode + " resultCode " + resultCode);

        if (resultCode == RESULT_OK) {
            ArrayList<String> res = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (requestCode == ChipsList.RECOGNIZE_LOCATION) {
                if (res.size() > 0) {
                    dialogLocation(res.get(0));
                }
            } else if (requestCode == ChipsList.RECOGNIZE_SIZE) {
                if (res.size() > 0) {
                    dialogSize(res.get(0));
                }
            } else if (requestCode == ChipsList.RECOGNIZE_ITEM) {
                if (res.size() > 0) {
                    dialogItem(res.get(0));
                }
            } else if (requestCode == ChipsList.RECOGNIZE_PRICE) {
                if (res.size() > 0) {
                    dialogPrice(res.get(0));
                }
            } else if (requestCode == ChipsList.RECOGNIZE_QUANTITY) {
                if (res.size() > 0) {
                    dialogQuantity(res.get(0));
                }
            }
        } else {
            // TODO: what to do?  well, try again?
        }
    }

    public void startVoice(int which, String prompt) {
        Log.i(ChipsList.TAG, "AddItem: startVoice(): which " + which
                + " prompt " + prompt);

        Intent recognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        recognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizer.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        startActivityForResult(recognizer, which);
    }

    public void dialogItem(String iii) {
        final String item = DatabaseCode.toTitleCase(iii);

        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle("Set Item Name?").setCancelable(false)
                .setMessage(item)
                .setPositiveButton(R.string.ok, new OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        // User clicked OK so do some stuff
                        itemName.setText(item);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        /* Handle cancel clicked */
                        dialog.dismiss();
                    }
                }).create();
        dlg.show();
    }

    public void dialogLocation(String lll) {
        final String loc = DatabaseCode.toTitleCase(lll);

        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle("Set Location?").setCancelable(false).setMessage(loc)
                .setPositiveButton(R.string.ok, new OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        // User clicked OK so do some stuff
                        location.setText(loc);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
						/* Handle cancel clicked */
                        dialog.dismiss();
                    }
                }).create();
        dlg.show();
    }

    public void dialogSize(String sss) {
        final String size_s = DatabaseCode.toTitleCase(sss);

        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle("Set Size?").setCancelable(false).setMessage(size_s)
                .setPositiveButton(R.string.ok, new OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        // User clicked OK so do some stuff
                        pkg_sz.setText(size_s);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
						/* Handle cancel clicked */
                        dialog.dismiss();
                    }
                }).create();
        dlg.show();
    }

    public void dialogPrice(String ppp) {
        ppp = DatabaseCode.toTitleCase(ppp);
        double x;
        try {
            x = Double.parseDouble(ppp);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "dialogPrice(): invalid price " + ppp + " "
                    + e);
            x = 0.0;
        }
        final double price_f = x;

        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle("Set Price?").setMessage(Double.toString(price_f))
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        // User clicked OK so do some stuff
                        price.setText(Double.toString(price_f));
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
						/* Handle cancel clicked */
                        dialog.dismiss();
                    }
                }).create();
        dlg.show();
    }

    public void dialogQuantity(String qqq) {

        int q = 0;
        try {
            q = Integer.parseInt(qqq);
        } catch (Exception e) {
        }
        final int qty = q;

        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle("Set Quantity?").setMessage(Integer.toString(qty))
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        // User clicked OK so do some stuff
                        int myQty = qty;
                        if (myQty < 1)
                            myQty = 1;
                        quantity.setText(Integer.toString(myQty));
                        dialog.dismiss();
                    }

                }).setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
						/* Handle cancel clicked */
                        dialog.dismiss();
                    }
                }).create();
        dlg.show();
    }

    public void dialogDuplicateName() {

        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle(getString(R.string.duplicate_title))
                .setMessage(getString(R.string.duplicate_msg))
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        // User clicked OK so do some stuff
                        dialog.dismiss();
                    }
                }).create();
        dlg.show();
    }

    public void dialogBlankItemName() {

        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle(getString(R.string.blank_item_title))
                .setMessage(getString(R.string.blank_item_msg))
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        // User clicked OK so do some stuff
                        dialog.dismiss();
                    }
                }).create();
        dlg.show();
    }

    public void dialogUnsavedChanges() {

        final AlertDialog dlg = new AlertDialog.Builder(act)
                .setTitle(getString(R.string.unsaved_changes_title))
                .setMessage(getString(R.string.unsaved_changes_msg))
                .setCancelable(false)
                .setPositiveButton(R.string.save, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        // User clicked save, so save the data then exit.
                        dialog.dismiss();
                        mySave(); // this routine saves data then exits to
                        // parent activity.
                    }
                }).setNegativeButton(R.string.exit, new OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        // User clicked exit - so let's exit.
                        dialog.dismiss();
                        // getIntent().putExtra(
                        // ChipsList.INTENT_RETURN_ITEM_NUMBER, 0);
                        setResult(RESULT_OK, getIntent());
                        finish();
                    }
                }).create();
        dlg.show();
    }

    private void mySave() {
        Log.d(ChipsList.TAG, "mySave(): got here.");
        double prix = 0.0;
        try {
            prix = Double.parseDouble(price.getText().toString());
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "onCreate() price conversion problem " + e);
            prix = 0.0;
        }

        double qty = 1.0;
        try {
            qty = Double.parseDouble(quantity.getText().toString());
        } catch (Exception e) {
            qty = 1.0;
        }

        tax = (CheckBox) findViewById(R.id.is_taxed);
        int isTaxed = 0;
        if (tax.isChecked() == true) {
            isTaxed = 1;
        }

        if (itemName.getText().toString().trim().length() != 0) {
            int rcode = 0;

            rcode = myDB.newItem(storeNum, itemName.getText().toString().trim(),
                    location.getText().toString(), pkg_sz.getText().toString(),
                    prix, isTaxed, qty);
            if (rcode == 1) {
                dialogDuplicateName();
            } else {
                int itemNum = myDB.getItemNum(itemName.getText().toString().trim());

                int c = findDbItem(itemNum);
                FindItem.saveIndex(c);

                Log.v(ChipsList.TAG, "AddItem:: itemNum, c, itemName "
                        + itemNum + ", " + c + ", "
                        + itemName.getText().toString().trim());

                getIntent().putExtra(ChipsList.RCODE, "ok");
                setResult(RESULT_OK, getIntent());
                finish();
            }
        } else {
            dialogBlankItemName();
        }
    }

    private int findDbItem(int itemNum) {
        ArrayList<FoodRecord> recs = myDB.getAvailableItems(storeNum);
        int c = 0;
        for (FoodRecord f : recs) {
            if (f.getItemNum() == itemNum) {
                return c;
            }
            c++;
        }
        return 0;
    }
}
