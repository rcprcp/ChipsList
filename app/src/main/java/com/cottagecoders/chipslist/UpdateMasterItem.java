package com.cottagecoders.chipslist;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

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

public class UpdateMasterItem extends ActionBarActivity {

	static Context ctx;
	static Activity act;
	DatabaseCode myDB;

	EditText item;
	AutoCompleteTextView location;
	EditText size;
	EditText price;
	EditText quantity;
	CheckBox isTaxed;
	int itemNum;
	int storeNum;
	NumberFormat nf;
	
	boolean changed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.update_master_item);

		ctx = this.getApplicationContext();
		act = this;
		myDB = new DatabaseCode(ctx);

		itemNum = getIntent().getIntExtra(ChipsList.INTENT_ITEM_NUM, 0);
		storeNum = getIntent().getIntExtra(ChipsList.INTENT_STORE_NUM, 0);

		FoodRecord fr = myDB.getMasterItem(storeNum, itemNum);

		act.setTitle(getString(R.string.update) + " " + fr.getItemName());
		ActionBar a = getSupportActionBar();
		a.setDisplayHomeAsUpEnabled(true);
		nf = new DecimalFormat("####.##");
		
		item = (EditText) findViewById(R.id.item);
		item.setText(fr.getItemName());
		item.addTextChangedListener(watcher);

		location = (AutoCompleteTextView) findViewById(R.id.location);
		location.setText(fr.getLocation());
		location.addTextChangedListener(watcher);
		ArrayList<DepartmentRecord> dept = myDB.getMasterListDepartments();
		String [] departments = new String[dept.size()];
		Log.v(ChipsList.TAG,"before loop - got here");
		int i = 0;
		for(DepartmentRecord d : dept) {
			departments[i] = d.getLocation();
			i++;
			Log.v(ChipsList.TAG, "AddItem:: "+i+" ["+d.getLocation()+"]");
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, departments);
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
				mySave();
			}
		});

		// TODO: pop up the keyboard?
		// InputMethodManager mgr = (InputMethodManager)
		// getSystemService(Context.INPUT_METHOD_SERVICE);
		// mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED,
		// InputMethodManager.HIDE_IMPLICIT_ONLY);

		// TODO: this is alleged to get rid of the soft keyboard.
		// and it compiles :)
		// ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
		// .hideSoftInputFromWindow(quantity.getWindowToken(), 0);

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
		if (resultCode == RESULT_OK) {
			ArrayList<String> res = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			if (requestCode == ChipsList.RECOGNIZE_ITEM) {
				if (res.size() > 0) {
					dialogItem(res.get(0));
				}
			} else if (requestCode == ChipsList.RECOGNIZE_LOCATION) {
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
			// TODO: what to do here? maybe try again?
		}
	}

	@Override
	public void onBackPressed() {
		Log.i(ChipsList.TAG, "updateMasterItem(): onBackPressed called.  changed = "
				+ changed);
		if (changed) {
			dialogUnsavedChanges();
			return;
		}
		super.onBackPressed();
	}
	@Override
	public void onStart() {
		super.onStart();
		Log.i(ChipsList.TAG, "updateMasterItem  onStart called");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(ChipsList.TAG, "updateMasterItem onPause called");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(ChipsList.TAG, "updateMasterItem  onResume called");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i(ChipsList.TAG, "updateMasterItem onStop called");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(ChipsList.TAG, "updateMasterItem onDestroy called");
	}

	private void startVoice(int which, String prompt) {
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
		double f = 0.0;
		try {
			f = Double.parseDouble(ppp);
		} catch (Exception e) {

		}
		final double price_f = f;

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

	private void dialogQuantity(String qqq) {
		final String qty = qqq;
		final AlertDialog dlg = new AlertDialog.Builder(act)
				.setTitle("Set Quantity?").setMessage(qqq).setCancelable(false)
				.setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// User clicked OK so do some stuff
						quantity.setText(qty);
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

	private void dialogItem(final String qqq) {
		final AlertDialog dlg = new AlertDialog.Builder(act)
				.setTitle("Set Item Name?").setMessage(qqq)
				.setCancelable(false)
				.setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// User clicked OK so do some stuff
						item.setText(qqq);
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.cancel, new OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// user clicked cancel, do nothing...
						dialog.dismiss();
					}
				}).create();
		dlg.show();
	}

	private void dialogDuplicateName(String name) {
		final AlertDialog dlg = new AlertDialog.Builder(act)
				.setTitle("Duplicate Name")
				.setMessage(
						name + " is a duplicate item name.  Please revise it.")
				.setCancelable(false)
				.setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// User clicked OK so do nothing.
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
						mySave();  // this routine exits to parent activity.
					}
				}).setNegativeButton(R.string.exit, new OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// User clicked exit - so let's exit.
						dialog.dismiss();
						setResult(RESULT_OK, getIntent());
						finish();
					}
				}).create();
		dlg.show();
	}

	private void mySave() {
		EditText tempEd = (EditText) findViewById(R.id.item);
		String itemName = tempEd.getText().toString();

		tempEd = (EditText) findViewById(R.id.location);
		String location = tempEd.getText().toString();

		tempEd = (EditText) findViewById(R.id.size);
		String size = tempEd.getText().toString();

		tempEd = (EditText) findViewById(R.id.price);
		double price = 0.0;
		try {
			price = Double.parseDouble(tempEd.getText().toString());
		} catch (Exception e) {
		}

		tempEd = (EditText) findViewById(R.id.quantity);
		double quantity = 0.0;
		try {
			quantity = Double.parseDouble(tempEd.getText().toString());
		} catch (Exception e) {
		}

		CheckBox box = (CheckBox) findViewById(R.id.is_taxed);
		int isTaxed = 0;
		if (box.isChecked() == true) {
			isTaxed = 1;
		}

		// update fields.
		int rcode = 0;
		rcode = myDB.updateMasterItem(itemName, itemNum, storeNum,
				location, size, price, isTaxed, quantity);
		myDB.toggleMasterList(itemNum, 1);
		if (rcode == 1) {
			dialogDuplicateName(itemName);
		} else {
			setResult(RESULT_OK, getIntent());
			finish();
		}
	}
}