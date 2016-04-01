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
import android.widget.Button;
import android.widget.EditText;


public class UpdateStore extends ActionBarActivity {

	Context ctx;
	Activity act;
	DatabaseCode myDB;
	StoreRecord sr;
	EditText name;
	EditText taxRate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_store);

		ActionBar a = getSupportActionBar();
		a.setDisplayHomeAsUpEnabled(true);

		//this.setTheme(R.style.CustomTheme);
		ctx = this.getApplicationContext();
		act = this;
		myDB = new DatabaseCode(ctx);

		int store = getIntent().getIntExtra(ChipsList.INTENT_STORE_NUM, 0);

		sr = myDB.getStoreInfo(store);
		if (sr == null) {
			Log.i(ChipsList.TAG, "UpdateOrDeleteStore(): bad store number.");
			setResult(RESULT_OK, getIntent());
			finish();
		}

		act.setTitle("Update or Delete");

		name = (EditText) findViewById(R.id.name);
		taxRate = (EditText) findViewById(R.id.tax_rate);

		name.setText(sr.getStoreName());
		taxRate.setText(Double.toString(sr.getTaxRate()));

		// now set up the delete button.
		Button delbtn = (Button)findViewById(R.id.delete);
		delbtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogReallyDelete(sr);
			}
		});

		Button save = (Button) findViewById(R.id.save);
		save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				name.setText(DatabaseCode
						.toTitleCase(name.getText().toString()));
				sr.setStoreName(name.getText().toString());

				int rcode = ChipsList.OK;

				double tax = 0.0;
				try {
					tax = Double.parseDouble(taxRate.getText().toString());
				} catch (Exception e) {
					rcode = ChipsList.NOT_OK;
					Log.e(ChipsList.TAG,
							"UpdateOrDeleteStore(): parseDouble failed " + e);
					dialogTaxRateFail(taxRate.getText().toString());
				}

				if (rcode == ChipsList.OK) {
					sr.setTaxRate(tax);
					if (myDB.updateStore(sr) == ChipsList.NOT_OK) {
						rcode = ChipsList.NOT_OK;
						Log.e(ChipsList.TAG,
								"UpdateOrDeleteStore(): updateStore() failed. ");
						dialogDupeStoreName(name.getText().toString());
					}

					if (rcode == ChipsList.OK) {
						setResult(RESULT_OK, getIntent());
						finish();
					}
				}
			}
		});
	}

	private void dialogReallyDelete(final StoreRecord sr) {

		AlertDialog.Builder b = new AlertDialog.Builder(act)
				.setTitle(getString(R.string.really_delete))
				.setMessage(
						getString(R.string.really_delete2) + " " + sr.getStoreName())
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(final DialogInterface dialog,
												final int which) {
								// User clicked OK so do something
								myDB.deleteStore(sr.getStoreNumber());
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

	private void dialogTaxRateFail(String tr) {

		AlertDialog.Builder b = new AlertDialog.Builder(act)
				.setTitle(getString(R.string.tax_rate_fail))
				.setMessage(
						tr + " " + getString(R.string.tax_rate_fail_message))
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(final DialogInterface dialog,
									final int which) {
								// User clicked OK so do nothing
							}
						});
		b.create();
		b.show();
	}

	private void dialogDupeStoreName(String name) {

		AlertDialog.Builder b = new AlertDialog.Builder(act)
				.setTitle(getString(R.string.update_fail))
				.setMessage(
						name + " " + getString(R.string.update_fail_message))
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(final DialogInterface dialog,
									final int which) {
								// User clicked OK so do nothing
							}
						});
		b.create();
		b.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.help_only, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(RESULT_OK, getIntent());
			finish();
			break;

		case R.id.help:
			Intent intent = new Intent(ctx, Help.class);
			intent.putExtra(ChipsList.HELP_STRING,
					getString(R.string.HELP_update_or_delete_store));
			startActivity(intent);
			break;
		}
		return true;
	}
}