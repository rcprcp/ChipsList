package com.cottagecoders.chipslist;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

public class TripSummary extends ActionBarActivity {
	Context ctx;
	Activity act;
	DatabaseCode myDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trip_summary);
		Log.i(ChipsList.TAG, "TripSummary: onCreate() got here");

		ctx = this.getApplicationContext();
		act = this;

		// get the extra info we were started with -
		// this should be the store number we're working with.
		int store = getIntent().getIntExtra(ChipsList.INTENT_STORE_NUM, 1);

		myDB = new DatabaseCode(ctx);

		StoreRecord sr = myDB.getStoreInfo(store);
		if (sr == null) {
			act.setTitle("Invalid Store");
		} else {
			act.setTitle(sr.getStoreName() + " " + getString(R.string.summary));
		}
		ActionBar a = getSupportActionBar();
		a.setDisplayHomeAsUpEnabled(true);

		ArrayList<FoodRecord> food = myDB.getAllFoodAlpha(store);

		// process all records.
		double price = 0.0;
		double tax = 0.0;

		int totalItems = 0;
		int checkedItems = 0;

		double priceChecked = 0.0;
		double taxChecked = 0.0;
		int with_prices = 0;

		NumberFormat formatter = NumberFormat.getCurrencyInstance();

		totalItems = food.size();
		for (FoodRecord f : food) {
			if (f.getPrice() == 0)
				continue;
			if (f.getQuantity() == 0)
				continue;
			with_prices++;

			price += f.getPrice() * f.getQuantity();
			if (f.isTaxed()) {
				tax += (sr.getTaxRate() / 100) * f.getPrice() * f.getQuantity();
			}

			if (f.isChecked()) {
				checkedItems++;

				priceChecked += f.getPrice() * f.getQuantity();
				if (f.isTaxed()) {
					taxChecked += (sr.getTaxRate() / 100) * f.getPrice()
							* f.getQuantity();
				}
			}
		}

		// put the values in the appropriate spots.
		TextView tv;
		tv = (TextView) findViewById(R.id.num_items);
		tv.setText(Integer.toString(totalItems));

		tv = (TextView) findViewById(R.id.items_with_prices_and_qty);
		tv.setText(Integer.toString(with_prices));

		tv = (TextView) findViewById(R.id.all_item_price);
		tv.setText(formatter.format(price) + " " );

		// TODO: tax issue.
		if (sr.getTaxRate() == 0) {
			TableRow r = (TableRow) findViewById(R.id.tax_row);
			r.setVisibility(View.GONE);
		} else {
			tv = (TextView) findViewById(R.id.total_tax);
			tv.setText(formatter.format(tax));
		}

		tv = (TextView) findViewById(R.id.total_cost);
		tv.setText(formatter.format(price + tax));

		tv = (TextView) findViewById(R.id.checked_items);
		tv.setText(Integer.toString(checkedItems));

		tv = (TextView) findViewById(R.id.checked_price);
		tv.setText(formatter.format(priceChecked));

		// TODO: tax issue.
		if (sr.getTaxRate() == 0) {
			TableRow r = (TableRow) findViewById(R.id.checked_tax_row);
			r.setVisibility(View.GONE);
		} else {
			tv = (TextView) findViewById(R.id.checked_tax);
			tv.setText(formatter.format(taxChecked));
		}
		tv = (TextView) findViewById(R.id.checked_total);

		tv.setText(formatter.format(priceChecked + taxChecked));


		//TODO: add a message indicating the tax rate.
		//TODO: add a button to update the store if no tax rate.
		if(sr.getTaxRate() == 0.0) {
			tv = (TextView)findViewById(R.id.message);
			tv.setText("\n\nNote: " + sr.getStoreName() + " " + getString(R.string.no_tax_rate));
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(ChipsList.TAG, "TripSummary  onStart called");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(ChipsList.TAG, "TripSummary onPause called");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(ChipsList.TAG, "TripSummary  onResume called");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i(ChipsList.TAG, "TripSummary onStop called");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(ChipsList.TAG, "TripSummary onDestroy called");
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
					getString(R.string.HELP_trip_summary));
			startActivity(intent);
			break;
		}
		return true;
	}
}