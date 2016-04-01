package com.cottagecoders.chipslist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class UpdateItem extends ActionBarActivity {

	static Context ctx;
	static Activity act;
	DatabaseCode myDB;

	AutoCompleteTextView location;
	EditText itemName;
	EditText size;
	EditText price;
	EditText quantity;
	CheckBox isTaxed;
	int itemNum;
	int storeNum;
	NumberFormat nf;

	boolean changed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.update_item);
		Log.i(ChipsList.TAG, "UpdateItem - onCreate() got here");

		ctx = this.getApplicationContext();
		act = this;
		myDB = new DatabaseCode(ctx);
		nf = new DecimalFormat("####.##");

		storeNum = getIntent().getIntExtra(ChipsList.INTENT_STORE_NUM, 0);
		itemNum = getIntent().getIntExtra(ChipsList.INTENT_ITEM_NUM, 0);
		FoodRecord fr = myDB.getItem(storeNum, itemNum);
		Log.v(ChipsList.TAG, "UpdateItem: onCreate(): item, store, fr "
				+ itemNum + " " + storeNum + " " + fr);

		act.setTitle(R.string.update_item);
		ActionBar a = getSupportActionBar();
		a.setDisplayHomeAsUpEnabled(true);

		itemName = (EditText) findViewById(R.id.item);
		itemName.setText(fr.getItemName());
		itemName.addTextChangedListener(watcher);

		location = (AutoCompleteTextView) findViewById(R.id.location);
		location.setText(fr.getLocation());
		location.addTextChangedListener(watcher);
		ArrayList<DepartmentRecord> dept = myDB.getUniqueDepartments(storeNum,
				ChipsList.STORE_ORDER_BY_LOCATION);

		ArrayList<String> departments = new ArrayList<>();
		for (DepartmentRecord d : dept) {
			departments.add(d.getLocation());
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, departments);
		location.setAdapter(adapter);
		location.setThreshold(1);

		size = (EditText) findViewById(R.id.size);
		size.setText(fr.getSize());
		size.addTextChangedListener(watcher);

		price = (EditText) findViewById(R.id.price);
		price.setText(nf.format(fr.getPrice()));
		price.addTextChangedListener(watcher);

		quantity = (EditText) findViewById(R.id.quantity);
		quantity.setText(nf.format(fr.getQuantity()));
		quantity.addTextChangedListener(watcher);

		isTaxed = (CheckBox) findViewById(R.id.is_taxed);
		int id = Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android");
		isTaxed.setButtonDrawable(id);

		if (fr.isTaxed() == true) {
			isTaxed.setChecked(true);
		} else {
			isTaxed.setChecked(false);
		}

		// set up all the voice buttons...
		ImageButton voiceItem = (ImageButton) findViewById(R.id.voice_item);
		voiceItem.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startVoice(ChipsList.RECOGNIZE_ITEM,
						getString(R.string.voice_prompt_item));
			}
		});

		ImageButton voiceLocation = (ImageButton) findViewById(R.id.voice_location);
		voiceLocation.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startVoice(ChipsList.RECOGNIZE_LOCATION,
						getString(R.string.voice_prompt_location));
			}
		});

		ImageButton voiceSize = (ImageButton) findViewById(R.id.voice_size);
		voiceSize.setOnClickListener(new View.OnClickListener() {
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
				Log.i(ChipsList.TAG, "onCreate(): save button clicked.");
				mySave();
			}
		});
	}

	TextWatcher watcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			changed = true;
			Log.d(ChipsList.TAG, "TextWatcher: onTextChanged(): got here.");
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(ChipsList.TAG, "onActivityResult(): requestCode " + requestCode
				+ " resultCode " + resultCode);

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
			// TODO: what to do here? maybe try again...
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(ChipsList.TAG, "UpdateItem  onStart called");
	}

	@Override
	public void onBackPressed() {
		Log.i(ChipsList.TAG, "UpdateItem: onBackPressed called.  changed = "
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
		Log.i(ChipsList.TAG, "UpdateItem onPause called");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(ChipsList.TAG, "UpdateItem  onResume called");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i(ChipsList.TAG, "UpdateItem onStop called");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(ChipsList.TAG, "UpdateItem onDestroy called");
	}

	private void startVoice(int which, String prompt) {
		Log.i(ChipsList.TAG, "startVoice(): which " + which + " prompt "
				+ prompt);

		Intent recognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		recognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		recognizer.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
		startActivityForResult(recognizer, which);
	}

	private void dialogLocation(String lll) {
		final String loc = DatabaseCode.toTitleCase(lll);

		final AlertDialog dlg = new AlertDialog.Builder(act)
				.setTitle("Set Location?").setCancelable(false).setMessage(loc)
				.setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
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

	private void dialogSize(String sss) {
		final String size_s = DatabaseCode.toTitleCase(sss);

		final AlertDialog dlg = new AlertDialog.Builder(act)
				.setTitle("Set Size?").setCancelable(false).setMessage(size_s)
				.setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// User clicked OK so do some stuff
						size.setText(size_s);
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

	private void dialogPrice(String ppp) {
		final String prix = ppp;
		final AlertDialog dlg = new AlertDialog.Builder(act)
				.setTitle("Set Price?").setMessage(ppp).setCancelable(false)
				.setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// User clicked OK so do some stuff
						Double p = Double.valueOf(prix);
						// force price to be positive.
						Math.abs(p);
						// display it.
						price.setText(p.toString());
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

	private void dialogQuantity(String qqq) {
		final String qty = qqq;
		final AlertDialog dlg = new AlertDialog.Builder(act)
				.setTitle("Set Quantity?").setMessage(qqq).setCancelable(false)
				.setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// User clicked OK so do some stuff
						Double myQty = Double.valueOf(qty);
						if (myQty <= 0.0)
							myQty = 1.0;
						quantity.setText(myQty.toString());
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
			if (changed) {
				dialogUnsavedChanges();
				break;
			}

			setResult(RESULT_OK, getIntent());
			finish();
			break;

		case R.id.help:
			Intent intent = new Intent(ctx, Help.class);
			intent.putExtra(ChipsList.HELP_STRING,
					getString(R.string.HELP_update_item));
			startActivity(intent);
			break;

		}
		return true;
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
						mySave(); // this routine exits to parent activity.
					}
				}).setNegativeButton(R.string.exit, new OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// User clicked exit - so let's exit.
						dialog.dismiss();
						getIntent().putExtra(
								ChipsList.INTENT_RETURN_ITEM_NUMBER, 0);
						setResult(RESULT_OK, getIntent());
						finish();
					}
				}).create();
		dlg.show();
	}

	private void mySave() {

		EditText tempEd;

		tempEd = (EditText) findViewById(R.id.item);
		String item = tempEd.getText().toString();

		tempEd = (EditText) findViewById(R.id.location);
		String location = tempEd.getText().toString();

		tempEd = (EditText) findViewById(R.id.size);
		String size = tempEd.getText().toString();

		tempEd = (EditText) findViewById(R.id.price);
		double price = 0.0;
		try {
			price = Double.parseDouble(tempEd.getText().toString());
		} catch (Exception e) {
			Log.i(ChipsList.TAG, "save button: price parseDouble failed " + e);
		}
		if (price < 0.0)
			price = 0.0;

		tempEd = (EditText) findViewById(R.id.quantity);
		double quantity = 0;
		try {
			quantity = Double.parseDouble(tempEd.getText().toString());
		} catch (Exception e) {
			Log.i(ChipsList.TAG, "save button: quantity parseInt failed " + e);
		}
		if (quantity <= 0.0)
			quantity = 1.0;

		CheckBox box = (CheckBox) findViewById(R.id.is_taxed);
		int isTaxed = 0;
		if (box.isChecked() == true) {
			isTaxed = 1;
		}

		// update fields.
		myDB.toggleMasterList(isTaxed, 0);
		int rcode = myDB.updateEverything(itemNum, item, storeNum,
				location, size, price, isTaxed, quantity);
		if (rcode == 1) {
			// duplicate key. probably itemName.
			dialogDuplicateName();
			
		} else {
			Intent data = new Intent();
			data.putExtra(ChipsList.INTENT_RETURN_ITEM_NUMBER, itemNum);
			setResult(RESULT_OK, data);
			finish();
		}

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


}
