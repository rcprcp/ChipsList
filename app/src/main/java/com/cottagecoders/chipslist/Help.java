package com.cottagecoders.chipslist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Help extends ActionBarActivity {

	Context ctx;
	Activity act;
	DatabaseCode myDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);

		//this.setTheme(R.style.CustomTheme);
		ctx = this.getApplicationContext();
		act = this;
		myDB = new DatabaseCode(ctx);

		act.setTitle("Help");
		ActionBar a = getSupportActionBar();
		a.setDisplayHomeAsUpEnabled(true);

		String message = getIntent().getStringExtra(ChipsList.HELP_STRING);
		TextView tv = (TextView) findViewById(R.id.message);
		if (message == null) {
			tv.setText("Help is not available for this page.");
		} else {
			tv.setText(message);
		}
		tv.setTextSize(ChipsList.HELP_TEXT_FONT_SIZE);

		Button ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK, getIntent());
				finish();
			}
		});
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