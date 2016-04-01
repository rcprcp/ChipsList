package com.cottagecoders.chipslist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

public class DeleteUnusedItems extends ActionBarActivity {

	Context ctx;
	Activity act;
	DatabaseCode myDB;
	int cutOffDate[] = new int[4];
	RadioGroup radioGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.delete_unused_items);

		//this.setTheme(R.style.CustomTheme);
		ctx = this.getApplicationContext();
		act = this;
		myDB = new DatabaseCode(ctx);

		act.setTitle("Delete Unused Items");
		ActionBar a = getSupportActionBar();
		a.setDisplayHomeAsUpEnabled(true);

		// if the date is a zero,  it's never been used. 
		// since we delete less than this value, we'll get records 
		// which have never had a date. 
		cutOffDate[0] = 1;
		cutOffDate[1] = DatabaseCode.minusDays(30);
		cutOffDate[2] = DatabaseCode.minusDays(90);
		cutOffDate[3] = DatabaseCode.minusDays(180);
		Log.v(ChipsList.TAG, "dates "+cutOffDate[0] + " "+ cutOffDate[1] + " " + cutOffDate[2] + " " + cutOffDate[3]);

		radioGroup = (RadioGroup) findViewById(R.id.radio_group);
		Button delete = (Button) findViewById(R.id.delete);
		delete.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// figure out which radio button is set...
				int rbId = radioGroup.getCheckedRadioButtonId();
				View rb = radioGroup.findViewById(rbId);
				int ix = radioGroup.indexOfChild(rb);
				if(ix >= 1 && ix <=4) {
					ix--;
					dialogReallyDelete(cutOffDate[ix]);
				} else { 
					Toast.makeText(ctx, "Invalid selection " + ix, Toast.LENGTH_LONG)
							.show();
				}
			}
		});

	}

	private void dialogReallyDelete(int cutOffDate) {

		final int cutOff = cutOffDate;
		AlertDialog.Builder b = new AlertDialog.Builder(act)
				.setTitle(getString(R.string.really_delete))
				.setMessage(getString(R.string.really_delete3))
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(final DialogInterface dialog,
									final int which) {
								// User clicked OK so do something
								int count = myDB.deleteByDate(cutOff);
								Toast.makeText(
										ctx,
										Integer.toString(count)
												+ " sample items deleted.",
										Toast.LENGTH_LONG).show();
								dialog.dismiss();
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
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(RESULT_OK, getIntent());
			finish();
			break;

		}
		return true;
	}
}
