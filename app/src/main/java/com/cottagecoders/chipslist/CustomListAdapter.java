package com.cottagecoders.chipslist;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class CustomListAdapter extends BaseAdapter implements SectionIndexer {

	Context ctx;
	DatabaseCode myDB;
	
	// represents the row that each letter starts at in the ListView's data.
	HashMap<String, Integer> alphaIndexer;
	
	String[] sections;
	int storeNum;
	NumberFormat nf;
	Dialog dia;
    Boolean checkedOnly;
    
	static ArrayList<FoodRecord> food;

	public CustomListAdapter(Context ctx) {
		this.ctx = ctx;
		// open database connection...
		myDB = new DatabaseCode(ctx);
        
		// get store number from the calling program, via static method.
		storeNum = FindItem.getStoreNum();

		//get list from the database of all items we should put in the list.
		//old way... no autocomplete string
		// food = myDB.getAvailableItems(storeNum);
		//new way - trying autocomplete string.
		checkedOnly = FindItem.getCheckedOnly();
		food = myDB.getAutoCompleteItems(storeNum, FindItem.getAutoCompleteString(), checkedOnly);
		FindItem.setRecordCount(food.size());
		
		Log.d(ChipsList.TAG, "food size " + food.size());
		
		//end of new way.
		
		alphaIndexer = new HashMap<String, Integer>();

		int i = 0 ;
		for(FoodRecord f : food) {
			// are there ANY letters in the name?  Blank itemName would be "bad".
			if(f.getItemName().length() == 0 ) { 
				Log.d(ChipsList.TAG, "CustomListAdapter(): itemNum = " + f.getItemNum() + " has blank item_name.");
				continue;
			}
			// get first letter of item...
			String chx = f.getItemName().substring(0, 1);
			
			// force upper case... 
			chx = chx.toUpperCase(Locale.getDefault());
			
			// if this is a new letter, add this letter it's position
			// to the HashMap
			if (!alphaIndexer.containsKey(chx))  alphaIndexer.put(chx, i);
			i++;
		}

		// make a Set from the keys in the HashMap...
		Set<String> sectionLetters = alphaIndexer.keySet();
		ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
		
		//sort, but they are already supposed to be sorted from the database.
		Collections.sort(sectionList);
		sections = new String[sectionList.size()];
		sectionList.toArray(sections);
	}

	@Override
	public int getCount() {
		return food.size();
	}

	@Override
	public Object getItem(int position) {
		Log.v(ChipsList.TAG, "getItem(): got here.");
		return food.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	
	//TODO: what are the other parameters?  why are they not used?
	/** 
	 * this builds the records we put into the ListView. 
	 * it includes the checkboxes and special formatting.  
	 * also, we add the OnClick listener and LongClick listener.
	 * @param position represents the position in the ListView. 
	 * @param convertView  -- dunno what this is.  not used here.
	 * @param parent -- dunno what this is.  not used?
	 * @return
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {


		boolean hasPrice;

		// new layout for the row.
		LinearLayout ll = new LinearLayout(ctx);
		ll.setOrientation(LinearLayout.HORIZONTAL);

		// attach to entire FoodRecord to this row. 
		ll.setTag(food.get(position));
		
		// create a new checkbox.
		final CheckBox chk = new CheckBox(ctx);
		int id = Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android");
		chk.setButtonDrawable(id);
		// check it if necessary.
		if (food.get(position).isChecked()) {
			chk.setChecked(true);
		}
		
		// attach the FoodRecord to the checkbox, too. 
		chk.setTag(food.get(position));
		// and add a single click listener to toggle the checkbox...
		// and it will make sure the database is updated.
		chk.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// get here when check box is clicked some time
				// in the future....
				FoodRecord f = (FoodRecord) v.getTag();
				int itemNum = f.getItemNum();
				CheckBox b = (CheckBox) v;
				int val = 0;
				if (b.isChecked()) {
					val = 1;
				} else {
					val = 0;
				}
				myDB.toggleMasterList(itemNum, val);
				f.setChecked(b.isChecked());
				FindItem.saveIndex(mapItemToIndex(itemNum));
				FindItem.clearField();
			}
		});

		
		//add the checkbox to the layout.
		ll.addView(chk);

		// set up the line...
		String stuff = food.get(position).getItemName();

		// format the information - blah, blah...
		if (!food.get(position).getLocation().equals("")) {
			stuff += " -- " + food.get(position).getLocation();
		}
		
		// format the information - blah, blah...
		nf = new DecimalFormat("#0.00");
		if (food.get(position).getQuantity() > 1) {
			stuff += " " + nf.format(food.get(position).getQuantity());
		}
		stuff += " " + food.get(position).getSize();
		if (food.get(position).getPrice() > 0.0) {
			NumberFormat formatter = NumberFormat.getCurrencyInstance();
			String p = formatter.format(food.get(position).getPrice());
			stuff += " " + p;
			hasPrice = true;
		} else {
			hasPrice = false;
		}

		if (food.get(position).getQuantity() > 1.0) {
			stuff += " Each ";
		}

		if (food.get(position).isTaxed()) {
			stuff += " +Tax";
		}

		// create new TextView.
		TextView tv = new TextView(ctx);
		// add the text.
		tv.setText(stuff);

		// make the text green if it has a price.
		if(hasPrice) {
			tv.setTextColor(ctx.getResources().getColor(R.color.ForestGreen));
		} else {
            tv.setTextColor(ctx.getResources().getColor(R.color.Black));
        }

		//attach the entire FoodRecord.
		tv.setTag(food.get(position));

		//remember to set the font size.
		tv.setTextSize(ChipsList.TEXT_FONT_SIZE);

		//add long click listener to go to the price comparison routine.
		tv.setLongClickable(true);
		tv.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				FoodRecord f = (FoodRecord) v.getTag();
				
				int index = mapItemToIndex(f.getItemNum());
				FindItem.saveIndex(index);
			
				Intent intent = new Intent(ctx, PriceComparison.class);
				intent.putExtra(ChipsList.INTENT_ITEM_NUM, f.getItemNum());
				intent.putExtra(ChipsList.INTENT_ITEM_NAME, f.getItemName());
				intent.putExtra(ChipsList.INTENT_STORE_NUM, -1);   //no storeNum
				ctx.startActivity(intent);
				return true;
			}
		});

		//add single click listener to go to the update screen for this item.
		tv.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				FoodRecord f = (FoodRecord) v.getTag();

				int index = mapItemToIndex(f.getItemNum());
				FindItem.saveIndex(index);

				Intent intent = new Intent(ctx, UpdateMasterItem.class);
				intent.putExtra(ChipsList.INTENT_ITEM_NUM, f.getItemNum());
				intent.putExtra(ChipsList.INTENT_STORE_NUM, storeNum);
				ctx.startActivity(intent);
			}
		});

		// add TextView to the layout after (to the right of) the checkbox. 
		ll.addView(tv);

		return ll;
	}

	@Override
	public int getPositionForSection(int section) {
		return alphaIndexer.get(sections[section]);
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}

	@Override
	public Object[] getSections() {
		return sections;
	}

	
	/** 
	 * helper function to map the item number (which the rest of the system is using) 
	 * to the row number (which the ListView is using.) 
	 * we use this to help find the position in the ListView when we add a new record 
	 * to the database.
	 * @param itemNum item number.
	 * @return the item's position in the ListView.  returns 0 if it's an unknown item number.
	 */
	public static int mapItemToIndex(int itemNum){
		// for correct positioning of the ListView, we need to convert item number to row index in list view.
		for(int i=0 ; i < food.size(); i++){
			if(food.get(i).getItemNum() == itemNum) {
				return i;
			}
		}
		return 0;
	}
}
