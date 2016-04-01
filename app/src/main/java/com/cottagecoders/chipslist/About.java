package com.cottagecoders.chipslist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class About extends ActionBarActivity {

	Context ctx;
	Activity act;
	DatabaseCode myDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		//this.setTheme(R.style.CustomTheme);
		ctx = this.getApplicationContext();
		act = this;
		myDB = new DatabaseCode(ctx);

		act.setTitle("About ChipsList");
		ActionBar a = getSupportActionBar();
		a.setDisplayHomeAsUpEnabled(true);

		// make the URL look like a link.
		TextView tv = (TextView) findViewById(R.id.url);
		Spannable span = new SpannableString(tv.getText().toString());
		span.setSpan(new UnderlineSpan(), 0, span.length(), 0);
		tv.setText(span);
		tv.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Uri u = Uri.parse(getString(R.string.url));
			startActivity(new Intent(Intent.ACTION_VIEW, u));
			}
		});

		Button ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK, getIntent());
				finish();
			}
		});
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(RESULT_OK, getIntent());
			finish();
			break;
		default:
		}
		return true;
	}
}