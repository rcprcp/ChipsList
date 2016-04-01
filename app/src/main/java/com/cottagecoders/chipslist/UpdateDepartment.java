package com.cottagecoders.chipslist;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class UpdateDepartment extends ActionBarActivity {

	Context ctx;
	Activity act;
	DatabaseCode myDB;
	int storeNumber;
	TableLayout table;
	int  locationSerialNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_department);

		//this.setTheme(R.style.CustomTheme);
		ctx = this.getApplicationContext();
		act = this;
		myDB = new DatabaseCode(ctx);

		act.setTitle("Setup Department Order");
		ActionBar a = getSupportActionBar();
		a.setDisplayHomeAsUpEnabled(true);


		// get the extra info we were started with -
		// this should be the store number we're working with.
		storeNumber = getIntent().getIntExtra(ChipsList.INTENT_STORE_NUM, 1);

		Button save = (Button) findViewById(R.id.save);
		save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateStuff(v);
				setResult(RESULT_OK, getIntent());
				finish();
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(ChipsList.TAG, "updateDepartment   onStart called");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(ChipsList.TAG, "updateDepartment  onPause called");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(ChipsList.TAG, "updateDepartment   onResume called");
		fillTable();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i(ChipsList.TAG, "updateDepartment  onStop called");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(ChipsList.TAG, "updateDepartment  onDestroy called");
	}

	private void fillTable() {

		table = (TableLayout) findViewById(R.id.table);
		// must clear table here.
		table.removeAllViews();
		ArrayList<DepartmentRecord> depts = myDB.getUniqueDepartments(
				storeNumber, ChipsList.STORE_ORDER_BY_SEQUENCE);
		for (DepartmentRecord d : depts) {
			TableRow row = new TableRow(ctx);
			row.setPadding(ChipsList.PADDING, ChipsList.PADDING,
					ChipsList.PADDING, ChipsList.PADDING);

			EditText ed = new EditText(ctx);
			ed.setTextColor(getResources().getColor(R.color.Black));
			ed.setInputType(InputType.TYPE_CLASS_NUMBER);
			ed.setMinEms(2);
			ed.setText(Integer.toString(d.getSequence()));
			ed.setTextSize(ChipsList.TEXT_FONT_SIZE);
			ed.setTag(d);

			row.addView(ed);

			TextView tv = new TextView(ctx);
			// put this stuff into the row...
			tv.setTextSize(ChipsList.TEXT_FONT_SIZE);
			tv.setTextColor(getResources().getColor(R.color.Black));
			tv.setText(d.getLocation());
			tv.setTag(d);
			row.addView(tv);

			// add the department delete button.
			if (!d.getLocation().equals("")) {
				ImageButton btn = new ImageButton(ctx);
				btn.setImageDrawable(getResources().getDrawable(
						R.drawable.delete2));
				btn.setTag(d);
				btn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						DepartmentRecord d = (DepartmentRecord) v.getTag();
						if (!d.getLocation().equals("")) {
							locationSerialNumber = d.getSerial();
							dialogReallyDelete(d.getLocation());
						} else {
							dialogCantDeleteBlankDepartment();
						}
					}
				});

				row.addView(btn);
				table.addView(row);
			} 
		}
	}

	private void dialogCantDeleteBlankDepartment() {

		AlertDialog.Builder b = new AlertDialog.Builder(act)
				.setTitle(R.string.select_sort)
				.setMessage(R.string.cant_delete_blank_department)
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

	private void updateStuff(View v) {

		for (int i = 0; i < table.getChildCount(); i++) {
			TableRow r = (TableRow) table.getChildAt(i);
			EditText e = (EditText) r.getChildAt(0);
			DepartmentRecord d = (DepartmentRecord) e.getTag();

			int num;
			try {
				num = Integer.parseInt(e.getText().toString());
			} catch (Exception ex) {
				Log.i(ChipsList.TAG, "save_button: parseInt failed " + ex);
				num = 1;
			}

			if (num != d.getSequence()) {
				myDB.updateSequence(d.getSerial(), num);
			}
		}

	}

	private void dialogReallyDelete(String location) {

		AlertDialog.Builder b = new AlertDialog.Builder(act)
				.setTitle(getString(R.string.really_delete))
				.setMessage(getString(R.string.really_delete2) + " " + location)
		.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// User clicked OK so do something
						myDB.deleteDepartment(locationSerialNumber);
						dialog.dismiss();
						fillTable();
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
			
		case R.id.help:
			Intent intent = new Intent(ctx, Help.class);
			intent.putExtra(ChipsList.HELP_STRING, getString(R.string.HELP_update_department));
			startActivity(intent);
			break;

		}
		
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.help_only, menu);
		return true;
	}

}