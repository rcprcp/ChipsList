package com.cottagecoders.chipslist;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.SparseIntArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseCode extends SQLiteOpenHelper {

    private static final String DB_NAME = "chipslist.sqlite";
    private static final int VERSION = 1;
    private static final String T_STORES = "stores";
    private static final String T_ITEMS = "items";
    private static final String T_DEPTS = "departments";
    private static final String T_SHOPPING_LIST = "shopping_list";
    private static final String T_LOCATIONS = "locations";
    private static final String T_PREFS = "preferences";

    private static Context ctx;
    private static SQLiteDatabase db = null;

    public DatabaseCode(Context context) {
        super(context, DB_NAME, null, VERSION);
        ctx = context;
    }

    /**
     * create all the tables... this will be called when the database does not
     * exist.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String stmt = "CREATE TABLE "
                + T_PREFS
                + " (store_sort SHORTINT, master_sort SHORTINT, tax_rate double, show_ads SHORTINT)";
        myExecSQL(db, "onCreate()", stmt);

        stmt = "INSERT INTO " + T_PREFS + " (store_sort, tax_rate, show_ads) VALUES ( "
                + ChipsList.STORE_ORDER_BY_ITEM_NAME + ", 6.0, 1)";
        myExecSQL(db, "onCreate()", stmt);

        // create stores table -- info about the stores.
        stmt = "CREATE TABLE "
                + T_STORES
                + " (store_num INTEGER PRIMARY KEY AUTOINCREMENT, store_name VARCHAR(30), tax_rate double)";
        myExecSQL(db, "onCreate()", stmt);

        // stores - a list of the stores...
        stmt = "CREATE UNIQUE INDEX stores_ix1 ON " + T_STORES
                + " (store_name)";
        myExecSQL(db, "onCreate()", stmt);

        // shopping list... this is the per store item list.
        // items are added and removed from this list to represent
        // a shopping list for a particular store.
        // the user can check or uncheck the item to represent
        // when it's been put in the cart.
        stmt = "CREATE TABLE " + T_SHOPPING_LIST + " (store_num INTEGER, "
                + "item_num INTEGER, " + "item_name VARCHAR(40), "
                + "ischecked SHORTINT)";
        myExecSQL(db, "onCreate()", stmt);

        stmt = "CREATE UNIQUE INDEX shopping_list_ix1 ON " + T_SHOPPING_LIST
                + " (item_num, store_num)";
        myExecSQL(db, "onCreate()", stmt);

        stmt = "CREATE UNIQUE INDEX shopping_list_ix2 ON " + T_SHOPPING_LIST
                + " (store_num, item_name)";
        myExecSQL(db, "onCreate()", stmt);

        stmt = "CREATE TABLE "
                + T_DEPTS
                + " (store_num INTEGER, dept_name VARCHAR(40), serial INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " sequence INTEGER) ";
        myExecSQL(db, "onCreate()", stmt);

        stmt = "CREATE UNIQUE INDEX " + T_DEPTS + "_ix1 ON " + T_DEPTS
                + " (dept_name, store_num)";
        myExecSQL(db, "onCreate()", stmt);

        /**
         * T_LOCATIONS is item locations within a stores - aisle 12B or
         * "Pet Products" entries are not deleted from this table...
         *
         * note that the items are qualified by the store number, so we can have
         * a record for each store/item combination. this is because some items
         * are in different locations in different stores. items can be added to
         * the shopping list by their presence in the T_SHOPPING_LIST table, the
         * entries in this table save the price, location and other information
         * we need to keep for the item. then when we re-add it to the shopping
         * list, the saved values are available.
         */
        stmt = "CREATE TABLE " + T_LOCATIONS
                + " (item_num INTEGER, store_num INTEGER, dept_link INTEGER, "
                + " quantity DOUBLE, "
                + " size VARCHAR(40), isTaxed SHORTINT, price DOUBLE)";
        myExecSQL(db, "onCreate()", stmt);

        /**
         * index to maintain store/item are unique. this index supports lookups
         * for all items for a store.
         */
        stmt = "CREATE UNIQUE INDEX locations_ix1 ON " + T_LOCATIONS
                + " (store_num, item_num)";
        myExecSQL(db, "onCreate()", stmt);

        /**
         * maintain store/item are unique, but quicker lookup for item number.
         */
        stmt = "CREATE UNIQUE INDEX locations_ix2 ON " + T_LOCATIONS
                + " (item_num, store_num)";
        myExecSQL(db, "onCreate()", stmt);

        stmt = "CREATE INDEX locations_ix4 ON " + T_LOCATIONS
                + " (store_num, dept_link)";
        myExecSQL(db, "onCreate()", stmt);

        /**
         * T_ITEMS is the master list of all items. we preload items into this
         * table. the users can add new items to this table, then the items can
         * be added to a particular store by copying to the T_SHOPPING_LIST
         * table.
         *
         * ischecked in this table permits the user to check a bunch of items,
         * and move them all to a store with one click in the UI.
         */
        stmt = "CREATE TABLE "
                + T_ITEMS
                + " (item_num INTEGER PRIMARY KEY AUTOINCREMENT, item_name VARCHAR(40), "
                + " istaxed SHORTINT, ischecked SHORTINT, location VARCHAR(40) DEFAULT \"\", "
                + " size VARCHAR(40) DEFAULT \"\", quantity DOUBLE, price DOUBLE, last_used INTEGER, sample SHORTINT)";
        myExecSQL(db, "onCreate()", stmt);

        stmt = "CREATE UNIQUE INDEX items_ix1 ON "
                + T_ITEMS
                + " (item_name, location, item_num, size, price, quantity, istaxed, ischecked, last_used, sample)";
        myExecSQL(db, "onCreate()", stmt);

        /**
         * lookup all ischecked items.
         */
        stmt = "CREATE UNIQUE INDEX items_ix2 ON " + T_ITEMS
                + " (ischecked, item_name)";
        myExecSQL(db, "onCreate()", stmt);

        /**
         * this index permits deleting of the sample data when the user decides
         * they don't want it anymore. from the UI you can delete data that has
         * never been used, or has not been used within a specific time frame.
         */
        stmt = "CREATE UNIQUE INDEX items_ix3 ON " + T_ITEMS
                + " (last_used DESC, item_num)";
        myExecSQL(db, "onCreate()", stmt);

        /**
         * this index would permit sorting the master list by location... but i
         * don't this it's used at this point.
         */
        stmt = "CREATE UNIQUE INDEX items_ix4 ON " + T_ITEMS
                + " (location, item_num)";
        myExecSQL(db, "onCreate()", stmt);

        /**
         * this index permits the autocomplete to work correctly, and it stops
         * duplicate item names.  duplicate names look awful in the UI.
         */
        stmt = "CREATE UNIQUE INDEX items_ix5 ON " + T_ITEMS + " (item_name)";
        myExecSQL(db, "onCreate()", stmt);

        loadDatabase(db);

    }

    /**
     * simple routine to execute an SQL statement and handle errors. this little
     * routine makes the code a bit more concise.
     * <p/>
     * obviously this will only work for statements without host variables.
     *
     * @param db   database object.
     * @param rtn  calling routine name for the log statement in case of trouble.
     * @param stmt the SQL statement to execute.
     */
    private void myExecSQL(SQLiteDatabase db, String rtn, String stmt) {
        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.i(ChipsList.TAG, rtn + " myExecSQL(): " + stmt + " failed " + e);
        }

    }

    /**
     * Loads all the initial data into the database when it is first created.
     * Called from the onCreate routine only.
     *
     * @param db database object.
     */

    private void loadDatabase(SQLiteDatabase db) {

        double startTime = System.currentTimeMillis();
        String stmt;

        /**
         * add some sample data to the various tables.
         *
         * first, add some stores... 101 starts the serial number for this
         * table, it made it a little easier for debugging.
         */
        try {
            db.execSQL("INSERT INTO " + T_STORES
                    + " (store_num, store_name, tax_rate) VALUES (101, \"WalMart\", 7)");
            db.execSQL("INSERT INTO " + T_STORES
                    + " (store_name, tax_rate) VALUES (\"Wegmans\", 7)");
            db.execSQL("INSERT INTO " + T_STORES
                    + " (store_name, tax_rate) VALUES (\"Whole Foods\", 7)");
            db.execSQL("INSERT INTO " + T_STORES
                    + " (store_name, tax_rate) VALUES (\"Aldi\", 7)");
            db.execSQL("INSERT INTO " + T_STORES
                    + " (store_name, tax_rate) VALUES (\"Trader Joe\'s\", 7)");
            db.execSQL("INSERT INTO " + T_STORES
                    + " (store_name, tax_rate) VALUES (\"Albertsons\", 7)");
            db.execSQL("INSERT INTO " + T_STORES
                    + " (store_name, tax_rate) VALUES (\"Stop-n-Shop\", 7)");
            db.execSQL("INSERT INTO " + T_STORES
                    + " (store_name, tax_rate) VALUES (\"FoodTown\", 7)");
            db.execSQL("INSERT INTO " + T_STORES
                    + " (store_name, tax_rate) VALUES (\"Costco\", 7)");
        } catch (Exception e) {
            Log.i(ChipsList.TAG, "loadDatabase():  duplicate key for stores"
                    + e);
        }

        /**
         * add some food to the T_ITEMS table. the food records are actually
         * INSERT statements in a file saved in the res/assets directory of the
         * APK file. accessing this file is different - we have to access it
         * using the Android API - so we can't access this file like a "normal"
         * file in the file system.
         */
        BufferedReader br = null;
        try {
            AssetManager am = ctx.getAssets();
            InputStream in = am.open("foodlist.sql");
            br = new BufferedReader(new InputStreamReader(in));

        } catch (Exception e) {
            Log.e(ChipsList.TAG,
                    "loadDatabase(): BufferedReader create failed " + e);
        }
        if (br == null) {
            //TODO: fatal error if we can't load the foodList.sql file.
            System.exit(1);
        }

        // got the file...
        String sql;
        int dupes = 0;
        while (true) {
            try {
                sql = br.readLine();
            } catch (Exception e) {
                try {
                    br.close();
                } catch (IOException e1) {
                    Log.e(ChipsList.TAG, "loadDatabase(): br.close() failed "
                            + e1);
                }
                break;
            }

            // check for end of file.
            if (sql == null)
                break;

            // execute the statement.
            try {
                db.execSQL(sql);
            } catch (SQLiteConstraintException e) {
                dupes++;
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "loadDatabase(): execSQL failed: " + sql
                        + " " + e);
            }
        }

        // close file.
        try {
            br.close();
        } catch (Exception e) {
            // this should never happen.
            Log.e(ChipsList.TAG, "loadDatabase(): in.close() failed " + e);
        }

        /**
         * data verification phase... go through the T_ITEMS table and apply
         * toTitleCase logic to the strings.
         */

        // also, set "sample" to 1, so we know this was sample data and we can
        // delete the sample data at some
        // future date.
        stmt = "SELECT item_num, item_name, location, size  FROM " + T_ITEMS;
        Cursor tt;
        try {
            tt = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "loadDatabase() stmt " + stmt + " failed " + e);
            return;
        }

        if (tt == null) {
            Log.e(ChipsList.TAG, "loadDatabase() stmt " + stmt + " returned a null cursor");
            return;
        }

        while (tt.moveToNext()) {
            String item_name = toTitleCase(tt.getString(1));
            String location = toTitleCase(tt.getString(2));
            String size = toTitleCase(tt.getString(3));

            sql = "UPDATE " + T_ITEMS + " SET location = \"" + location
                    + "\", size = \"" + size + "\", item_name = \"" + item_name
                    + "\", " + "sample = 1, last_used = 0 WHERE item_num = "
                    + tt.getString(0);
            myExecSQL(db, "loadDatabase()", sql);
        }
        tt.close();
        double elapsed = System.currentTimeMillis() - startTime;
        Log.v(ChipsList.TAG,
                "loadDatabase(): Done loading sample data.  Elapsed time. "
                        + elapsed + "ms dupes " + dupes);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // implement migration code here.
        Log.i(ChipsList.TAG, "onUpgrade() - GOT HERE.");

    }

    /**
     * fetches all the necessary information form the T_STORES table to build
     * the StoreRecord objects.
     *
     * @return ArrayList of StoreRecord objects for all the stores in the
     * T_STORES table.
     */
    public ArrayList<StoreRecord> getAllStores() {

        // select the tab names from the database...
        if (db == null) {
            db = getWritableDatabase();
        }
        Cursor c;
        String stmt = "SELECT store_name, store_num, tax_rate FROM " + T_STORES
                + " ORDER BY store_name";
        ArrayList<StoreRecord> ans = new ArrayList<>();
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getAllStores() " + stmt + " failed " + e);
            return ans;
        }
        if (c.getCount() == 0) {
            c.close();
            return ans;
        }
        while (c.moveToNext()) {
            StoreRecord sr = new StoreRecord(c.getString(0), c.getInt(1),
                    c.getDouble(2));
            ans.add(sr);
        }
        c.close();
        return ans;
    }

    /**
     * delete a specific store by store number. also, additionally clean up the
     * T_LOCATIONS table, by deleting all the records for this store.
     *
     * @param storeNumber store number to delete.
     */

    public void deleteStore(int storeNumber) {

        // delete the store record from the database.
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt;

        stmt = "DELETE FROM " + T_SHOPPING_LIST + " WHERE store_num = "
                + storeNumber;
        myExecSQL(db, "deleteStore()", stmt);

        stmt = "DELETE FROM " + T_LOCATIONS + " WHERE store_num = "
                + storeNumber;
        myExecSQL(db, "deleteStore()", stmt);

        stmt = "DELETE FROM " + T_DEPTS + " WHERE store_num = " + storeNumber;
        myExecSQL(db, "deleteStore()", stmt);

        stmt = "DELETE FROM " + T_STORES + " WHERE store_num = "
                + storeNumber;
        myExecSQL(db, "deleteStore()", stmt);

    }

    /**
     * clear the checked items in the T_ITEMS table - this might be more
     * efficiently done as a single UPDATE statement. i think there was a bug
     * that led to this implementation.
     */
    public void uncheckMasterList() {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT item_num FROM " + T_ITEMS
                + " WHERE isChecked = 1";
        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "uncheckMasterList(): " + stmt + " failed "
                    + e);
        }

        //TODO: this is a fatal error.
        if (c == null) {
            Log.d(ChipsList.TAG, "uncheckMasterList(): Cursor c == null");
            System.exit(1);
        }
        while (c.moveToNext()) {
            stmt = "UPDATE " + T_ITEMS + " SET ischecked = 0 WHERE item_num = "
                    + c.getString(0);
            try {
                db.execSQL(stmt);
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "uncheckMasterList(): " + stmt + " " + e);
            }
        }
        c.close();
    }

    /**
     * INSERT a store into the T_STORES table... the store number is a serial
     * and will be assigned by the database.
     *
     * @param storeName store name as entered by the user.
     * @param taxRate   tax rate in percent (eg: 7.0 for 7%)
     */

    public void insertStore(String storeName, double taxRate) {

        if (db == null) {
            db = getWritableDatabase();
        }

        storeName = toTitleCase(storeName);
        try {
            db.execSQL("INSERT INTO " + T_STORES
                    + " (store_name, tax_rate) VALUES (\"" + storeName + "\", "
                    + taxRate + ")");
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "insertStore(): insert failed: " + e);
        }
    }

    /**
     * UPDATE an existing store - by store number. we will force the update on
     * the store name and the tax rate.
     *
     * @param sr StoreRecord object.
     * @return ok/not ok from the parameters.
     */
    public int updateStore(StoreRecord sr) {

        if (db == null) {
            db = getWritableDatabase();
        }

        int rcode = ChipsList.OK;
        String stmt = "UPDATE " + T_STORES + " set store_name = \""
                + sr.getStoreName() + "\", tax_rate  = " + sr.getTaxRate()
                + " WHERE store_num = " + sr.getStoreNumber();
        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "insertStore(): update failed: " + e);
            rcode = ChipsList.NOT_OK;
        }
        return rcode;
    }

    /**
     * selects all items for a specific store, but this routine is called when
     * the sorting sequence is not alphabetical. we can also sort the food items
     * from the database alphabetically by department or by the user's
     * customized sort for this store, which is called "order by sequence".
     *
     * @param storeNum  store whose items we want to retreive.
     * @param sortOrder parameter - either by location or "sort by sequence"
     * @return ArrayList of all the FoodRecord items in the appropriate sequence
     * for the specified sort.
     */
    public ArrayList<FoodRecord> getAllFoodNotAlpha(int storeNum, int sortOrder) {
        if (db == null) {
            db = getWritableDatabase();
        }

        // initialize this...
        ArrayList<FoodRecord> ans = new ArrayList<>();

        // build SQL based on the specified sort order.
        String stmt = "SELECT dept_name, serial, sequence FROM " + T_DEPTS
                + " WHERE store_num = " + storeNum;
        if (sortOrder == ChipsList.STORE_ORDER_BY_LOCATION) {
            stmt += " ORDER BY store_num, dept_name";
        } else if (sortOrder == ChipsList.STORE_ORDER_BY_SEQUENCE) {
            stmt += " ORDER BY store_num, sequence";
        }

        Cursor c;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getAllFoodNotAlpha(): " + stmt + "failed "
                    + e);
            // if the SQL was bad we'll return an empty list.
            return ans;
        }

        // if no records we returned we'll return an empty list.
        if (c.getCount() == 0) {
            c.close();
            Log.e(ChipsList.TAG, "getAllFoodNotAlpha(): " + stmt
                    + " no records");
            return ans;
        }

        // iterate through the records....
        while (c.moveToNext()) {
            Cursor cc;

            // this is a join to get the data we want from two tables with one
            // SQL statement - it gets quantity, size, price, taxed from the
            // T_LOCATIONS
            // and it gets item_num, ischecked and item_name from the
            // T_SHOPPING_LIST table.
            // the dept_link was returned from the SQL statement above, and
            // passed in as the c.getString below.
            String join = "SELECT sl.item_name, sl.isChecked, loc.quantity, "
                    + "sl.item_num, loc.size, loc.price, loc.isTaxed "
                    + "FROM shopping_list sl, locations loc "
                    + "WHERE sl.item_num = loc.item_num "
                    + "AND sl.store_num = loc.store_num "
                    + "AND loc.dept_link = " + c.getString(1)
                    + " ORDER BY sl.item_name";

            try {
                cc = db.rawQuery(join, null);
            } catch (Exception e) {
                // bad SQL - return empty list.
                Log.e(ChipsList.TAG, "getAllFoodNotAlpha(): " + join
                        + " failed " + e);
                return ans;
            }

            if (cc.getCount() == 0) {
                // no records for this department, go around for the next one...
                Log.i(ChipsList.TAG,
                        "getAllFoodNotAlpha(): no records for join - store, dept_link, dept_name "
                                + storeNum + " " + c.getString(1) + " "
                                + c.getString(0));
                continue;
            }

            while (cc.moveToNext()) {
                // fix blank location... this will happen when there is ino
                // location specified
                // on the record. this will look better in the UI.
                String location = "(None)";
                if (!c.getString(0).equals("")) {
                    location = c.getString(0);
                }

                // create a new FoodRecod from the data we're retrieved...
                FoodRecord food = new FoodRecord(cc.getString(0), // item_name
                        location, // location
                        cc.getString(4), // size
                        cc.getDouble(5), // price
                        cc.getInt(3), // item
                        storeNum, // store
                        cc.getInt(1), // checked
                        cc.getInt(6), // taxed
                        cc.getDouble(2), // quantity
                        c.getInt(2)); // store sequence
                // add it to the list...
                ans.add(food);
            }

            // close the join's cursor.
            cc.close();
        }

        // close the department sequence cursor.
        c.close();
        return ans;
    }

    // TODO: continue documentation here.

    public ArrayList<FoodRecord> getAllFoodAlpha(int storeNum) {
        if (db == null) {
            db = getWritableDatabase();
        }

        ArrayList<FoodRecord> ans = new ArrayList<>();
        // first, select by the correct order
        String stmt = "SELECT item_name, ischecked, item_num FROM "
                + T_SHOPPING_LIST + "  WHERE store_num = " + storeNum
                + " ORDER BY store_num, item_name";

        Cursor c;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getAllFoodAlpha(): " + stmt + " failed " + e);
            return ans;
        }

        if (c.getCount() == 0) {
            c.close();
            return ans;
        }

        String stmt2 = "SELECT size, price, isTaxed, dept_link, quantity FROM "
                + T_LOCATIONS + "  WHERE store_num = " + storeNum
                + " AND item_num = ? ";

        while (c.moveToNext()) {

            Cursor loc_tab;
            try {
                loc_tab = db.rawQuery(stmt2, new String[]{c.getString(2)});
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "getAllFoodAlpha(): " + stmt2 + " " + e);
                return ans;
            }

            String size;
            double price;
            int isTaxed;
            int dept_link;
            double quantity;

            if (loc_tab.getCount() > 0) {
                loc_tab.moveToFirst();
                size = loc_tab.getString(0);
                price = loc_tab.getDouble(1);
                isTaxed = loc_tab.getInt(2);
                dept_link = loc_tab.getInt(3);
                quantity = loc_tab.getDouble(4);
            } else {
                Log.e(ChipsList.TAG,
                        "getAllFoodAlpha(): no location record store, item_num, item_name"
                                + storeNum + " " + c.getInt(2) + " "
                                + c.getString(0));
                loc_tab.close();
                continue;
            }

            // another thing - get the department name.
            DepartmentRecord rec = getDepartmentRecord(dept_link);

            String itemName;
            int isChecked;
            int itemNum;

            itemName = c.getString(0);
            isChecked = c.getInt(1);
            itemNum = c.getInt(2);

            FoodRecord fr = new FoodRecord(itemName, rec.getLocation(), size,
                    price, itemNum, storeNum, isChecked, isTaxed, quantity,
                    rec.getSerial());
            ans.add(fr);

            loc_tab.close();
        }

        c.close();
        return ans;
    }

    public ArrayList<FoodRecord> getAvailableItems(int storeNum) {

        if (db == null) {
            db = getWritableDatabase();
        }
        Cursor c;

        // part 1 - get all the records in the items table...
        ArrayList<FoodRecord> ans = new ArrayList<>();
        /* TODO: this SQL is not qualified by storeNum */
        String stmt = "SELECT item_name, location, size, price, item_num, isChecked, isTaxed, quantity FROM "
                + T_ITEMS + " ORDER BY item_name ASC";

        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getMasterList(): " + stmt + " failed: " + e);
            return ans;
        }

        if (c.getCount() == 0) {
            c.close();
            return ans;
        }

        // get all the data that's in the shopping_list for this store...
        // we load it into a sparse array and then we can check if it's on the
        // shopping list without an SQL statement.

        SparseIntArray shopping_list = new SparseIntArray();
        stmt = "SELECT item_num FROM " + T_SHOPPING_LIST
                + " WHERE store_num = " + storeNum;
        Cursor x = null;
        boolean fail = false;
        try {
            x = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getAvailableItems(): " + stmt + " failed "
                    + e);
            fail = true;
        }

        if (!fail) {
            while (x.moveToNext()) {
                shopping_list.put(x.getInt(0), x.getInt(0));
            }
            x.close();
        }

        // now, we get all the data thats in the location table for this
        // store...
        // we load it into a sparse array and then we can check if there is data
        // we want to use to "over write" the data from the "master list".
        // this also reduces the number of SQL statements.

        SparseIntArray locations = new SparseIntArray();
        stmt = "SELECT item_num FROM " + T_LOCATIONS + " WHERE store_num = "
                + storeNum;
        fail = false;
        try {
            x = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getAvailableItems(): " + stmt + " failed "
                    + e);
            fail = true;
        }

        if (!fail) {
            while (x.moveToNext()) {
                locations.put(x.getInt(0), x.getInt(0));
            }
            x.close();
        }

        Cursor d;
        while (c.moveToNext()) {
            // if it is in the shopping list we want to skip it...
            if (shopping_list.get(c.getInt(4)) != 0) {
                continue; // this one exists in the T_SHOPPING_LIST already, so
                // skip.
            }

            int itemNum = c.getInt(4);
            String itemName = c.getString(0);
            double price;
            String location;
            String size;
            int isTaxed;
            int isChecked = c.getInt(5);
            double quantity;
            int dept_link = 0;

            if (locations.get(c.getInt(4)) != 0) {
                // get all the data we'll need to create a food record.
                stmt = "SELECT price, dept_link, size, isTaxed, quantity FROM "
                        + T_LOCATIONS
                        + " WHERE store_num = ?  AND item_num = ? ";
                try {
                    d = db.rawQuery(
                            stmt,
                            new String[]{Integer.toString(storeNum),
                                    c.getString(4)});
                } catch (Exception e) {
                    Log.e(ChipsList.TAG, "getAvailableItems(): " + stmt
                            + " failed: " + e);
                    continue;
                }

                d.moveToFirst();
                price = d.getDouble(0);

                dept_link = d.getInt(1);
                DepartmentRecord dept = getDepartmentRecord(dept_link);
                location = dept.getLocation();

                size = d.getString(2);
                isTaxed = d.getInt(3);
                quantity = d.getDouble(4);
                try {
                    d.close();
                } catch (Exception e) {
                    Log.e(ChipsList.TAG,
                            "getAvailableItems(): d.close() failed " + e);
                }
            } else {
                price = c.getDouble(3);
                location = c.getString(1);
                size = c.getString(2);
                isTaxed = c.getInt(6);
                quantity = c.getDouble(7);
            }

            FoodRecord fr = new FoodRecord(itemName, location, size, price,
                    itemNum, storeNum, isChecked, isTaxed, quantity, dept_link);

            ans.add(fr);
        }
        c.close();

        return ans;
    }

    public ArrayList<FoodRecord> getAutoCompleteItems(int storeNum,
                                                      String autocomplete, Boolean checkedOnly) {

        if (db == null) {
            db = getWritableDatabase();
        }
        Cursor c;

        // part 1 - get all the records in the items table...
        ArrayList<FoodRecord> ans = new ArrayList<>();
        String stmt = "SELECT item_name, location, size, price, item_num, isChecked, isTaxed, quantity FROM "
                + T_ITEMS;
        if (!autocomplete.equals("")) {
            stmt += " WHERE item_name LIKE \"" + autocomplete + "%\" "
                    + " OR item_name LIKE \"% " + autocomplete + "%\" ";
            if (checkedOnly)
                stmt += " and isChecked = 1 ";
        } else if (checkedOnly)
            stmt += " WHERE ischecked = 1";

        stmt += " ORDER BY item_name ASC";

        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getMasterList(): " + stmt + " failed: " + e);
            return ans;
        }

        if (c.getCount() == 0) {
            c.close();
            return ans;
        }

        // get all the data that's in the shopping_list for this store...
        // we load it into a sparse array and then we can check if it's on the
        // shopping list without an SQL statement.

        SparseIntArray shopping_list = new SparseIntArray();
        stmt = "SELECT item_num FROM " + T_SHOPPING_LIST
                + " WHERE store_num = " + storeNum;
        Cursor x = null;
        boolean fail = false;
        try {
            x = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getAvailableItems(): " + stmt + " failed "
                    + e);
            fail = true;
        }

        if (!fail) {
            while (x.moveToNext()) {
                shopping_list.put(x.getInt(0), x.getInt(0));
            }
            x.close();
        }

        // now, we get all the data thats in the location table for this
        // store...
        // we load it into a sparse array and then we can check if there is data
        // we want to use to "over write" the data from the "master list".
        // this also reduces the number of SQL statements.

        SparseIntArray locations = new SparseIntArray();
        stmt = "SELECT item_num FROM " + T_LOCATIONS + " WHERE store_num = "
                + storeNum;
        fail = false;
        try {
            x = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getAvailableItems(): " + stmt + " failed "
                    + e);
            fail = true;
        }

        if (!fail) {
            while (x.moveToNext()) {
                locations.put(x.getInt(0), x.getInt(0));
            }
            x.close();
        }

        Cursor d;
        while (c.moveToNext()) {
            // if it is in the shopping list we want to skip it...
            if (shopping_list.get(c.getInt(4)) != 0) {
                continue; // this one exists in the T_SHOPPING_LIST already, so
                // skip.
            }

            int itemNum = c.getInt(4);
            String itemName = c.getString(0);
            double price;
            String location;
            String size;
            int isTaxed;
            int isChecked = c.getInt(5);
            double quantity;
            int dept_link;

            if (locations.get(c.getInt(4)) != 0) {
                // get all the data we'll need to create a food record.
                stmt = "SELECT price, dept_link, size, isTaxed, quantity FROM "
                        + T_LOCATIONS
                        + " WHERE store_num = ?  AND item_num = ? ";
                try {
                    d = db.rawQuery(
                            stmt,
                            new String[]{Integer.toString(storeNum),
                                    c.getString(4)});
                } catch (Exception e) {
                    Log.e(ChipsList.TAG, "getAvailableItems(): " + stmt
                            + " failed: " + e);
                    continue;
                }

                d.moveToFirst();
                price = d.getDouble(0);

                dept_link = d.getInt(1);
                DepartmentRecord dept = getDepartmentRecord(dept_link);
                location = dept.getLocation();

                size = d.getString(2);
                isTaxed = d.getInt(3);
                quantity = d.getDouble(4);
                try {
                    d.close();
                } catch (Exception e) {
                    Log.e(ChipsList.TAG,
                            "getAvailableItems(): d.close() failed " + e);
                }
            } else {
                price = c.getDouble(3);
                location = c.getString(1);
                size = c.getString(2);
                isTaxed = c.getInt(6);
                quantity = c.getDouble(7);
                dept_link = 0;
            }

            FoodRecord fr = new FoodRecord(itemName, location, size, price,
                    itemNum, storeNum, isChecked, isTaxed, quantity, dept_link);

            ans.add(fr);
        }

        c.close();
        return ans;
    }

    public PriceRecord getItemForStore(int storeNum, int itemNum) {
        // note does not return a record for a store if
        // the price is 0.0 or the store matched the storeNum passed in.

        if (db == null) {
            db = getWritableDatabase();
        }
        Cursor c;

        String stmt = "SELECT store_num, price, quantity, size" + " FROM "
                + T_LOCATIONS + " WHERE item_num = " + itemNum
                + " AND store_num = " + storeNum;

        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getItemForStore(): " + stmt + " failed " + e);
            e.printStackTrace();
            return null;
        }

        try {
            c.moveToFirst();
        } catch (Exception e) {
            Log.d(ChipsList.TAG, "getItemForStore(): c.moveToFirst() failed " + e);
            e.printStackTrace();
            return null;
        }

        if (c.getCount() == 0) {
            c.close();
            return null;
        }

        int thisStore = c.getInt(0);
        double price = c.getDouble(1);
        double quantity = c.getDouble(2);

        String size = c.getString(3);
        if (c.isNull(3)) {
            Log.v(ChipsList.TAG, "getall... c.isNull(3) true.");
            size = "";
        }

        PriceRecord p = new PriceRecord(itemNum, thisStore, price,
                quantity, size);
        c.close();
        return p;
    }

    public FoodRecord getItem(int storeNum, int itemNum) {

        if (db == null) {
            db = getWritableDatabase();
        }
        Cursor c;

        String stmt = "SELECT item_name, isChecked  FROM " + T_SHOPPING_LIST
                + " WHERE store_num = " + storeNum + " AND item_num = "
                + itemNum;

        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getItem(): " + stmt + " failed: " + e);
            return null;
        }

        if (c.getCount() != 1) {
            c.close();
            return null;
        }

        c.moveToFirst();
        String itemName = c.getString(0);
        int isChecked = c.getInt(1);
        Cursor d;

        stmt = "SELECT size, price, isTaxed, dept_link, quantity FROM "
                + T_LOCATIONS + " WHERE store_num = " + storeNum
                + " AND item_num = " + itemNum;
        try {
            d = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getItem(): " + stmt + " failed " + e);
            return null;
        }

        String size = "";
        double price = 0.0;
        double quantity = 0;
        int isTaxed = 0;
        int sequence = 0;

        if (d.getCount() == 1) {
            d.moveToFirst();
            size = d.getString(0);
            price = d.getDouble(1);
            isTaxed = d.getInt(2);
            sequence = d.getInt(3);
            quantity = d.getDouble(4);
        }

        DepartmentRecord dept = getDepartmentRecord(sequence);
        String location = dept.getLocation();

        c.close();
        d.close();

        return new FoodRecord(itemName, location, size, price, itemNum,
                storeNum, isChecked, isTaxed, quantity, sequence);
    }

    public FoodRecord getMasterItem(int storeNum, int itemNum) {
        Log.i(ChipsList.TAG, "getMasterItem(): got here.");
        if (db == null) {
            db = getWritableDatabase();
        }

        Cursor c;

        String stmt = "SELECT dept_link, size, price, istaxed, quantity FROM "
                + T_LOCATIONS + " WHERE item_num = " + itemNum
                + " AND store_num = " + storeNum;

        String location = "";
        String size = "";
        double price = 0.0;
        double quantity = 0;
        int isTaxed = 0;
        boolean override = false;

        Cursor s = null;
        try {
            s = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getMasterItem(): " + stmt + " failed " + e);
        }

        if (s.getCount() == 0) {
            Log.i(ChipsList.TAG, "getMasterItem(): getCount() is 0");
        } else {
            override = true;
            s.moveToFirst();
            DepartmentRecord dept = getDepartmentRecord(s.getInt(0));
            location = dept.getLocation();
            size = s.getString(1);
            price = s.getDouble(2);
            isTaxed = s.getInt(3);
            quantity = s.getDouble(4);
            Log.v(ChipsList.TAG, "getMasterItem(): override size, price "
                    + override + " " + size + " " + price);
        }
        s.close();

        // get all the fields from the T_ITEMS table...
        stmt = "SELECT item_name, item_num, location, size, price, quantity, isTaxed, isChecked FROM "
                + T_ITEMS + " WHERE item_num = " + itemNum;

        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getMasterItem(): " + stmt + " failed: " + e);
            return null;
        }

        if (c.getCount() != 1) {
            c.close();
            return null;
        }

        c.moveToFirst();
        String itemName = c.getString(0);
        int isChecked = c.getInt(7);
        int sequence = 0;
        if (!override) {
            location = c.getString(2);
            size = c.getString(3);
            price = c.getDouble(4);
            quantity = c.getDouble(5);
            isTaxed = c.getInt(6);
        }
        c.close();

        return new FoodRecord(itemName, location, size, price, itemNum,
                storeNum, isChecked, isTaxed, quantity, sequence);
    }

    public void removeCheckedItemsFromStore(int storeNum) {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "DELETE FROM " + T_SHOPPING_LIST + " WHERE store_num = "
                + storeNum + " AND ischecked = 1";

        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "removeCheckedItemsFromStore() " + stmt
                    + "exception: " + e);
        }
    }

    public void toggleItem(int storeNum, int itemNum, int checked) {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "UPDATE " + T_SHOPPING_LIST + " SET ischecked = "
                + checked + " WHERE store_num = " + storeNum
                + " AND item_num = " + itemNum;

        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "toggleItem() error: " + stmt + " failed " + e);
        }
    }

    public void toggleMasterList(int itemNum, int checked) {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "UPDATE " + T_ITEMS + " SET ischecked = " + checked
                + " WHERE item_num = " + itemNum;

        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "toggleMasterList() error: " + stmt
                    + " failed " + e);
        }
    }

    public static String toTitleCase(String str) {


        str = str.replace("\r", "");
        str = str.replace("\n", "");
        str = str.replace("\t", "");
        str = str.replace("\"", "");
        str = str.replace("\'", "");
        str = str.replace(";", "");

        //TODO: why does wipe out the String iff the arg is the value of a function?
        //toTitleCase(f.getItemName());
        //  str = str.replace("", "");

        str = str.replace("~", "");
        str = str.replace("^", "");

        str = str.replace("}", "");
        str = str.replace("{", "");
        str = str.replace("<", "");
        str = str.replace(">", "");
        str = str.replace("[", "");
        str = str.replace("]", "");

        str = str.toLowerCase(Locale.getDefault());
        str = str.trim();


        String[] arr = str.split(" ");
        if (arr.length == 0)
            return "";

        StringBuilder sb = new StringBuilder();
        for (String anArr : arr) {
            if (anArr.length() == 0)
                continue;

            sb.append(Character.toUpperCase(anArr.charAt(0)))
                    .append(anArr.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    public void removeStoresItemsFromList(int storeNum) {

        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "DELETE FROM " + T_SHOPPING_LIST + " WHERE store_num = "
                + storeNum;
        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "removeItemsFromList() -- SQL failed -- "
                    + stmt + " " + e);
        }

    }

    public void removeItemFromStore(int storeNum, int itemNum) {

        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "DELETE FROM " + T_SHOPPING_LIST + " WHERE item_num = "
                + itemNum + " AND store_num = " + storeNum;
        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "removeItemFromStore() -- SQL failed -- "
                    + stmt + " " + e);
        }
    }

    public void deleteCheckedItems() {

        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT item_num FROM " + T_ITEMS
                + " WHERE ischecked = 1";
        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "deleteCheckedItems(): " + stmt + " failed "
                    + e);
        }
        while (c.moveToNext()) {
            int itemNum = c.getInt(0);
            Log.i(ChipsList.TAG, "deleteItem(): in loop. item " + itemNum);
            stmt = "DELETE FROM " + T_SHOPPING_LIST + " WHERE item_num = "
                    + itemNum;
            try {
                db.execSQL(stmt);
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "deleteItem(): execSQL failed: " + stmt
                        + " " + e);
            }

            stmt = "DELETE FROM " + T_ITEMS + " WHERE item_num = " + itemNum;
            try {
                db.execSQL(stmt);
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "deleteItem(): execSQL failed: " + stmt
                        + " " + e);
            }

            stmt = "DELETE FROM " + T_LOCATIONS + " WHERE item_num = "
                    + itemNum;
            try {
                db.execSQL(stmt);
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "deleteItem(): execSQL failed: " + stmt
                        + " " + e);
            }
        }
    }

    public int updateEverything(int itemNum, String itemName, int storeNum,
                                String location, String size, double price, int isTaxed,
                                double quantity) {

        if (db == null) {
            db = getWritableDatabase();
        }

        // update the itemName in the master list.
        String stmt = "UPDATE " + T_ITEMS + " SET item_name = \"" + itemName
                + "\" WHERE item_num = " + itemNum;
        //Log.d(ChipsList.TAG, "updateEverything(): stmt: " + stmt);
        try {
            db.execSQL(stmt);
        } catch (SQLiteConstraintException e) {
            Log.d(ChipsList.TAG, "updateEverything(): duplicate key. itemNum "
                    + itemNum);
            return 1;
        } catch (Exception e) {
            Log.d(ChipsList.TAG, "updateEverything(): exception stmt " + stmt
                    + " exception " + e);
            return 1;
        }
        // update the itemName in the shopping list.
        stmt = "UPDATE " + T_SHOPPING_LIST + " SET item_name = \"" + itemName
                + "\" WHERE item_num = " + itemNum;
        try {
            db.execSQL(stmt);
        } catch (SQLiteConstraintException e) {
            Log.d(ChipsList.TAG, "updateEverything(): duplicate key. itemNum "
                    + itemNum);
            return 1;
        } catch (Exception e) {
            Log.d(ChipsList.TAG, "updateEverything(): exception stmt " + stmt
                    + " exception " + e);
            return 1;
        }

        // should never permit quantity < 1
        if (quantity < 1)
            quantity = 1;

        DepartmentRecord dept = getDepartmentRecord(storeNum, location);

        stmt = "UPDATE " + T_LOCATIONS + " SET dept_link = " + dept.getSerial()
                + ", size = \"" + size + "\", price = " + price
                + ", isTaxed = " + isTaxed + ", quantity = " + quantity
                + " WHERE item_num = " + itemNum + " AND store_num = "
                + storeNum;

        myExecSQL(db, "updateEverything()", stmt);
        return 0;
    }

    public int updateMasterItem(String item, int itemNum, int storeNum,
                                String location, String size, double price, int isTaxed,
                                double quantity) {

        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT item_num FROM " + T_LOCATIONS
                + " WHERE item_num = " + itemNum + " AND store_num = "
                + storeNum;
        Cursor x = null;
        try {
            x = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "updateMasterItem(): error " + stmt
                    + " failed " + e);
        }

        if (x.getCount() > 0) {
            x.close();

            DepartmentRecord dept = getDepartmentRecord(storeNum, location);
            stmt = "UPDATE " + T_LOCATIONS + " SET " + " dept_link = "
                    + dept.getSerial() + ", " + " size = \"" + size
                    + "\", price = " + price + ", isTaxed = " + isTaxed
                    + ", quantity = " + quantity + " WHERE item_num = "
                    + itemNum + " AND store_num = " + storeNum;

            int rowCount = 0;
            try {
                db.execSQL(stmt);
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "updateMasterItem() " + stmt + " failed "
                        + e);
            }
            Log.v(ChipsList.TAG, "after executeUpdateDelete() rowCount "
                    + rowCount);
        } else {

            stmt = "UPDATE " + T_ITEMS + " SET item_name = \"" + item
                    + "\", location = \"" + location + "\", " + " size = \""
                    + size + "\", price = " + price + ", isTaxed = " + isTaxed
                    + ", quantity = " + quantity + " WHERE item_num = "
                    + itemNum;
            try {
                db.execSQL(stmt);
            } catch (SQLiteConstraintException e) {
                Log.e(ChipsList.TAG, "updateMasterItem(): duplicate key "
                        + stmt + " " + e);
                return 1;

            } catch (Exception e) {
                Log.e(ChipsList.TAG, "updateMasterItem(): " + stmt + " failed "
                        + e);
                return 2;

            }
        }
        return 0;
    }

    /**
     * copy a single item from the master list to the T_SHOPPING_LIST and
     * T_LOCATIONS tables.
     *
     * @param itemNum  -- which item to move.
     * @param storeNum -- destination store number.
     */
    public void moveItemToStore(int itemNum, int storeNum) {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT item_name, location, size, quantity, price, isTaxed FROM "
                + T_ITEMS + " WHERE item_num = " + itemNum;
        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "moveitemToStore(): " + stmt);
        }

        if (c.getCount() != 1) {
            Log.e(ChipsList.TAG,
                    "moveitemToStore(): c.getCount() = " + c.getCount());
            c.close();
            return;
        }

        c.moveToFirst();
        stmt = "INSERT INTO " + T_SHOPPING_LIST
                + " (item_num, item_name, store_num, isChecked) VALUES ( "
                + itemNum + ", \"" + c.getString(0) + "\", " + storeNum
                + ", 0)";
        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "moveItemToStore(): " + " failed " + e + " stmt: " + stmt);
            return;
        }

        // this will fail is there is already a location record...
        DepartmentRecord dept = getDepartmentRecord(storeNum, c.getString(1));
        stmt = "INSERT INTO "
                + T_LOCATIONS
                + " (store_num, item_num, dept_link, size, quantity, price, isTaxed) VALUES ("
                + storeNum + ", " + itemNum + "," + dept.getSerial() + ", \""
                + c.getString(2) + "\", " + c.getString(3) + ", "
                + c.getString(4) + ", " + c.getString(5) + ")";

        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "moveItemToStore(): " + stmt + " failed " + e);
        }

        c.close();
        return;
    }

    public void copyItemsToStore(int storeNum) {

        if (db == null) {
            db = getWritableDatabase();
        }

        Cursor c = null;

        String stmt = "SELECT item_num, item_name, location, size, price, isTaxed, quantity FROM "
                + T_ITEMS + " WHERE ischecked = 1";

        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "copyItemsToStore(): " + stmt + " failed " + e);
        }

        if (c.getCount() == 0) {
            c.close();
            return;
        }

        while (c.moveToNext()) {

            // so, either way we need a record in the T_SHOPPING_LIST table...
            String stmt2 = "INSERT INTO " + T_SHOPPING_LIST
                    + " (store_num, item_num, item_name, ischecked) "
                    + "VALUES ( " + storeNum + ", " + c.getString(0) + ", \""
                    + c.getString(1) + "\", 0)";

            try {
                db.execSQL(stmt2);
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "copyItemsToStore(): " + stmt2
                        + " failed " + e);
            }

            // is there a T_LOCATIONS record for this item, store.
            stmt = "SELECT item_num FROM " + T_LOCATIONS + " WHERE item_num = "
                    + c.getString(0) + " AND store_num = " + storeNum;
            Cursor cc = null;
            try {
                cc = db.rawQuery(stmt, null);
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "copyItemsToStore(): " + stmt + " failed "
                        + e);
                continue;
            }

            if (cc.getCount() == 1) { // mmm
                // insert into T_STORES only.

            } else { // mmm

                String stmt3 = "INSERT INTO "
                        + T_LOCATIONS
                        + " (item_num, store_num, dept_link, size, price, isTaxed, quantity) "
                        + "VALUES ( " + c.getString(0) + ", " + storeNum + ", ";

                String location = ""; // mmm going in correctly?
                Log.i(ChipsList.TAG, "crasher item_num " + c.getString(0)
                        + " location " + c.getString(2));
                if (c.getString(2) != null) {
                    location = c.getString(2);
                }
                DepartmentRecord dept = getDepartmentRecord(storeNum, location);
                stmt3 += Integer.toString(dept.getSerial());

                // set up the size or a blank string.
                if (c.isNull(3)) {
                    stmt3 += ", \"\", ";
                } else {
                    stmt3 += ", \"" + c.getString(3) + "\", ";
                }

                // price
                stmt3 += c.getDouble(4) + ", ";

                int isTaxed = 0;
                if (!c.isNull(5)) {
                    isTaxed = c.getInt(5);
                }

                stmt3 += isTaxed + ", " + c.getString(6) + ")";

                try {
                    db.execSQL(stmt3);
                } catch (Exception e) {
                    Log.e(ChipsList.TAG, "copyItemsToStore(): " + stmt3
                            + " failed " + e);
                }

                stmt = "UPDATE " + T_ITEMS + " SET last_used = " + theDate()
                        + " WHERE item_num = " + c.getString(0);
                try {
                    db.execSQL(stmt);
                } catch (Exception e) {
                    Log.e(ChipsList.TAG, "copyItemsToStore(): " + stmt3
                            + " failed " + e);
                }
            }
        }
        c.close();
    }

    public int newItem(int storeNum, String itemName, String location,
                       String size, double price, int isTaxed, double quantity) {
        int rcode = 0;
        if (db == null) {
            db = getWritableDatabase();
        }

        itemName = toTitleCase(itemName);
        location = toTitleCase(location);
        size = toTitleCase(size);

        DepartmentRecord dept = getDepartmentRecord(storeNum, location);

        if (quantity <= 0)
            quantity = 1;

        String stmt = "INSERT INTO "
                + T_ITEMS
                + " (item_name, location, size, price, ischecked, isTaxed, last_used, quantity ) VALUES (\""
                + itemName + "\", \"" + dept.getLocation() + "\", \"" + size
                // hard coded 0 is last_used date.
                + "\", " + price + ", 1, " + isTaxed + ", 0," + quantity
                + ")";
        Log.d(ChipsList.TAG, "newItem(): sql: " + stmt);

        try {
            db.execSQL(stmt);
        } catch (SQLiteConstraintException e) {
            Log.e(ChipsList.TAG, "newItem(): duplicate key " + stmt + " " + e);
            rcode = 1;
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "newItem(): " + stmt + " failed " + e);
            rcode = 2;
        }

        return rcode;
    }

    public int getStoreSort() {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT store_sort FROM " + T_PREFS;
        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getStoreSort(): " + stmt + " failed " + e);
            return ChipsList.STORE_ORDER_BY_ITEM_NAME;
        }
        if (c.getCount() != 1) {
            c.close();
            Log.e(ChipsList.TAG, "getStoreSort(): record count != 1");
            return ChipsList.STORE_ORDER_BY_ITEM_NAME;
        }
        c.moveToFirst();
        int i = c.getInt(0);
        c.close();
        return i;
    }

    /**
     * function to get the DB value representing whether or not to show ads.
     *
     * @return true to display ads, false = no ads.
     */
    public boolean getShowAds() {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT show_ads FROM " + T_PREFS;
        Cursor c;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getShowAds(): " + stmt + " failed " + e);
            //sorry, but you're getting ads.
            return true;
        }

        if (c.getCount() == 0) {
            c.close();
            //then you're getting ads.
            return true;
        }
        c.moveToFirst();

        //set boolean based on int value from DB.
        boolean showAds = (c.getInt(0) == 1) ? true : false;
        c.close();
        return showAds;
    }

    /**
     * set the showAds value in the database.
     */
    public void setShowAds(boolean showAds) {
        if (db == null) {
            db = getWritableDatabase();
        }

        int val = (showAds == true) ? 1 : 0;

        String stmt = "UPDATE " + T_PREFS + " set show_ads = " + val;
        Cursor c;
        try {
            myExecSQL(db, "setShowAds", stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "setShowAds(): " + stmt + " failed " + e);
            return;
        }
        return;
    }

    public void setStoreSort(int sort) {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "UPDATE " + T_PREFS + " SET store_sort = " + sort;
        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "setStoreSort(): " + stmt + " failed " + e);
        }
    }

    public void setMasterSort(int sort) {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "UPDATE " + T_PREFS + " SET master_sort = " + sort;
        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "setMasterSort(): " + stmt + " failed " + e);
        }
    }

    public StoreRecord getStoreInfo(int storeNum) {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT store_name, tax_rate FROM " + T_STORES
                + " WHERE store_num = " + storeNum;
        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getStoreInfo(): " + stmt + " failed " + e);
            return null;
        }

        if (c.getCount() == 0) {
            Log.v(ChipsList.TAG, "getStoreInfo(): " + stmt + " no records!");
            try {
                c.close();
            } catch (Exception e) {
                // nothing to do here.
            }
            return null;
        }

        c.moveToFirst();
        StoreRecord sr = new StoreRecord(c.getString(0), storeNum,
                c.getDouble(1));
        c.close();
        return sr;
    }

    public ArrayList<DepartmentRecord> getUniqueDepartments(int storeNumber,
                                                            int sortOrder) {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT serial, sequence, dept_name FROM " + T_DEPTS
                + " WHERE store_num = " + storeNumber + " ORDER BY store_num, ";
        if (sortOrder == ChipsList.STORE_ORDER_BY_LOCATION) {
            stmt += " dept_name";
        } else if (sortOrder == ChipsList.STORE_ORDER_BY_SEQUENCE) {
            stmt += " sequence";
        }

        ArrayList<DepartmentRecord> ans = new ArrayList<DepartmentRecord>();

        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getDepartments(): " + stmt + " failed: " + e);
            return ans;
        }

        if (c.getCount() == 0) {
            c.close();
            return ans;
        }
        while (c.moveToNext()) {
            DepartmentRecord dept = new DepartmentRecord(c.getInt(0),
                    c.getInt(1), c.getString(2));
            ans.add(dept);
        }
        c.close();
        return ans;
    }

    public ArrayList<DepartmentRecord> getMasterListDepartments() {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT DISTINCT location FROM " + T_ITEMS
                + " ORDER BY location";

        ArrayList<DepartmentRecord> ans = new ArrayList<DepartmentRecord>();

        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getMasterListDepartments(): " + stmt
                    + " failed: " + e);
            return ans;
        }

        if (c.getCount() == 0) {
            c.close();
            return ans;
        }
        while (c.moveToNext()) {
            DepartmentRecord dept = new DepartmentRecord(0, 0, c.getString(0));
            ans.add(dept);
        }
        c.close();
        return ans;
    }

    public void updateSequence(int serial, int sequence) {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "UPDATE " + T_DEPTS + " SET sequence = " + sequence
                + " WHERE serial = " + serial;
        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "updateSequence(): " + stmt + " failed " + e);
        }
        return;
    }

    public DepartmentRecord getDepartmentRecord(int serial) {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT serial, sequence, dept_name FROM " + T_DEPTS
                + " WHERE serial = " + serial;
        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getDepartmentRecord(serial) " + stmt
                    + " failed: " + e);
            return new DepartmentRecord(0, 0, "Bad Location");
        }

        if (c.getCount() == 0) {
            Log.e(ChipsList.TAG, "getDepartmentRecord(serial) " + serial
                    + " no record found");
            return new DepartmentRecord(0, 0, "Bad Location " + serial);
        }

        c.moveToFirst();
        DepartmentRecord dept = new DepartmentRecord(c.getInt(0), c.getInt(1),
                c.getString(2));
        c.close();
        return dept;
    }

    public DepartmentRecord getDepartmentRecord(int storeNum, String location) {
        if (db == null) {
            db = getWritableDatabase();
        }

        location = toTitleCase(location);
        String stmt27 = "SELECT serial, sequence FROM " + T_DEPTS
                + " WHERE dept_name = \"" + location + "\" AND store_num = "
                + storeNum;
        Cursor cc = null;

        try {
            cc = db.rawQuery(stmt27, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getDepartmentRecord(): " + stmt27
                    + " failed: " + e);
        }

        int serial = 0;
        int sequence = 0;
        if (cc.getCount() == 0) {
            cc.close();
            String stmt = "INSERT INTO " + T_DEPTS
                    + " (dept_name, store_num, sequence) VALUES (\"" + location
                    + "\", " + storeNum + ", 1)";
            try {
                db.execSQL(stmt);
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "getDepartmentRecord(): " + stmt
                        + "failed " + e);
            }

            try {
                cc = db.rawQuery(stmt27, null);
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "getDepartmentRecord(): " + stmt27
                        + " failed: " + e);
            }
        }

        cc.moveToFirst();
        serial = cc.getInt(0);
        sequence = cc.getInt(1);

        cc.close();

        return new DepartmentRecord(serial, sequence, location);
    }

    public void itemTableDumper() {
        Log.v(ChipsList.TAG, "tableDumper(): got here.");
        // TODO: remove this chunk of mega-debugging. someday...

        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt;
        Cursor ff = null;
        stmt = "SELECT item_num, item_name, location, size, quantity, last_used, sample FROM "
                + T_ITEMS;
        try {
            ff = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "ff fail " + stmt + " " + e);
        }
        while (ff.moveToNext()) {
            Log.v(ChipsList.TAG,
                    "T_ITEMS: item_num, item_name, location, size, quantity, last_used, sample"
                            + ff.getString(0) + " " + ff.getString(1) + " "
                            + ff.getString(2) + " " + ff.getString(3) + " "
                            + ff.getString(4) + " " + ff.getString(5) + " "
                            + ff.getString(6));
        }

        try {
            ff.close();
        } catch (Exception e) {
        }

        stmt = "SELECT serial, sequence, store_num, dept_name FROM " + T_DEPTS;
        try {
            ff = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "ff fail " + stmt + " " + e);
        }

        while (ff.moveToNext()) {
            Log.v(ChipsList.TAG,
                    "T_DEPTS: serial, sequence, store_num, dept_name"
                            + ff.getString(0) + " " + ff.getString(1) + " "
                            + ff.getString(2) + " " + ff.getString(3));
        }

        try {
            ff.close();
        } catch (Exception e) {
        }
        stmt = "SELECT store_num, item_num, item_name, isChecked FROM "
                + T_SHOPPING_LIST;
        try {
            ff = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "ff fail " + stmt + " " + e);
        }
        while (ff.moveToNext()) {
            Log.v(ChipsList.TAG,
                    "T_SHOPPING_LIST: store_num, item_num, item_name, isChecked "
                            + ff.getString(0) + " " + ff.getString(1) + " "
                            + ff.getString(2) + " " + ff.getString(3));
        }
        try {
            ff.close();
        } catch (Exception e) {
        }

        stmt = "SELECT item_num, store_num, quantity, dept_link FROM "
                + T_LOCATIONS;
        try {
            ff = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "ff fail " + stmt + " " + e);
        }
        while (ff.moveToNext()) {
            Log.v(ChipsList.TAG,
                    "T_LOCATIONS: item_num, store_num, quantity, dept_link "
                            + ff.getString(0) + " " + ff.getString(1) + " "
                            + ff.getString(2) + ff.getString(3));
        }
        try {
            ff.close();
        } catch (Exception e) {
        }
    }

    public void deleteDepartment(int serial) {

        if (db == null) {
            db = getWritableDatabase();
        }

        // select the serial - figure out this store -
        String stmt = "SELECT serial, dept_name, store_num FROM " + T_DEPTS
                + " WHERE serial = " + serial;
        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "deleteDepartment(): " + stmt + " failed " + e);
        }
        if (c.getCount() != 1) {
            Log.e(ChipsList.TAG, "deleteDepartment(): no records for " + serial);
            return;
        }

        c.moveToFirst();
        String dept_name = c.getString(1);
        int storeNum = c.getInt(2);

        // TODO: we should handle this is the UI. can't delete blank department.
        if (dept_name.equals("")) {
            return;
        }

        // select the serial for the "" record for this store.
        // this method will insert one if there isn't already one.
        DepartmentRecord d = getDepartmentRecord(storeNum, "");

        // update the location table dept_link field with the "" records
        stmt = "UPDATE " + T_LOCATIONS + " SET dept_link = " + d.getSerial()
                + " WHERE store_num = " + storeNum + " AND dept_link = "
                + serial;

        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "deleteDepartment(): " + stmt + " failed " + e);
        }

        // finally delete the record with old sequence number.
        stmt = "DELETE FROM " + T_DEPTS + " WHERE serial = " + serial;
        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "deleteDepartment(): " + stmt + " failed " + e);
        }
    }

    public void removeItemFromAllStores(int itemNum) {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "DELETE FROM " + T_SHOPPING_LIST + " WHERE item_num = "
                + itemNum;
        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "removeItemFromAllStores(): " + stmt
                    + " failed " + e);
        }
    }

    /**
     * used to determine the total number of shopping list items.  this routine
     * usually controls whether to pop up th dialog box to give the user a hint about
     * how to add an item to the list.
     *
     * @return total number of shopping list items.
     */
    public int countTotalShoppingListItems() {

        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT COUNT(item_num) FROM " + T_SHOPPING_LIST;
        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "countShoppingListItems(): " + stmt
                    + " failed " + e);
        }

        if (c.getCount() == 0) {
            c.close();
            return 0;
        }

        c.moveToFirst();
        int i = c.getInt(0);
        c.close();
        Log.v(ChipsList.TAG, "countShoppingListItems(): " + i);
        return i;
    }

    /**
     * used to determine the total number of shopping list items for a specific store.
     * this routine is used to display the item count on the main menu page.
     *
     * @return number of items on this store's list.
     */
    public int numberOfItemsForStore(int storeNumber) {

        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT COUNT(item_num) FROM "
                + T_SHOPPING_LIST
                + " WHERE store_num = " + storeNumber;

        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "numberOfItemsForStore(): " + stmt
                    + " failed " + e);
        }

        if (c.getCount() == 0) {
            c.close();
            return 0;
        }

        c.moveToFirst();
        int i = c.getInt(0);
        c.close();

        return i;
    }

    public int getCheckedCount(int storeNum) {

        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT COUNT(item_num) FROM " + T_ITEMS
                + " where ischecked = 1";
        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "(): " + stmt + " failed " + e);
            return (0);
        }

        if (c.getCount() == 0) {
            c.close();
            return 0;
        }

        c.moveToFirst();
        int i = c.getInt(0);
        c.close();
        Log.v(ChipsList.TAG, "getcheckedcount (): " + i);
        return i;
    }

    public int getItemNum(String name) {
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT item_num FROM " + T_ITEMS
                + " WHERE item_name = \"" + name + "\"";
        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "getItemNum(): " + stmt + " failed: " + e);
            return 0;
        }

        if (c.getCount() == 0) {
            c.close();
            return 0;
        }

        c.moveToFirst();
        int num = c.getInt(0);
        c.close();
        return num;
    }

    public int deleteByDate(int cutOffDate) {
        Log.v(ChipsList.TAG, "deleteByDate(): start " + cutOffDate);
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT item_num FROM " + T_ITEMS + " WHERE last_used < "
                + cutOffDate + " AND sample = 1";
        Cursor c = null;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "deleteByDate(): " + stmt + " failed " + e);
            return 0;
        }

        while (c.moveToNext()) {
            try {
                db.delete(T_LOCATIONS, "item_num = ?",
                        new String[]{c.getString(0)});
            } catch (Exception e) {
                Log.e(ChipsList.TAG,
                        "deleteByDate(): delete T_LOCATIONS " + c.getString(0)
                                + " failed " + e);
            }

            try {
                db.delete(T_SHOPPING_LIST, "item_num = ?",
                        new String[]{c.getString(0)});
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "deleteByDate(): delete T_SHOPPING_LIST "
                        + c.getString(0) + " failed " + e);
            }
        }
        c.close();

        int count = 0;
        try {
            count = db.delete(T_ITEMS, "last_used < ? AND sample = 1",
                    new String[]{Integer.toString(cutOffDate)});
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "deleteByDate(): T_ITEMS " + cutOffDate
                    + " failed " + e);
        }

        Log.v(ChipsList.TAG, "deleteByDate(): end");
        return count;
    }

    public static int theDate() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());

        int dt = cal.get(Calendar.YEAR) * 10000 + (cal.get(Calendar.MONTH) + 1)
                * 100 + cal.get(Calendar.DATE);
        Log.v(ChipsList.TAG, "theDate(): " + dt);
        return dt;
    }

    public static int minusDays(int numDays) {
        // subtract numDays from today.
        Calendar cal = Calendar.getInstance(Locale.getDefault());

        // add is really subtract since the number of days is a negative number.
        cal.add(Calendar.DAY_OF_MONTH, -numDays);
        int dt = cal.get(Calendar.YEAR) * 10000 + (cal.get(Calendar.MONTH) + 1)
                * 100 + cal.get(Calendar.DATE);
        Log.v(ChipsList.TAG, "theDate(): " + dt + " numDays " + numDays);
        return dt;
    }

    public void deleteFromShoppingList(int itemNum, int storeNum) {
        Log.v(ChipsList.TAG, "deleteFromShoppingList(): GOT HERE");
        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "DELETE FROM " + T_SHOPPING_LIST + " WHERE store_num = "
                + storeNum + " and item_num = " + itemNum;

        try {
            db.execSQL(stmt);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "deleteFromShopping (): " + stmt + " " + e);
        }

    }

    /**
     * This routine checks if the specified itemNum is in the specified
     * storeNum's list.
     *
     * @param itemNum  the item number
     * @param storeNum which store
     * @return boolean true -- it's is on the store's list; false it's not.
     */
    public boolean isItemOnStoreList(int itemNum, int storeNum) {
        Log.v(ChipsList.TAG, "isItemOnStoreList(): GOT HERE");

        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT item_num FROM " + T_SHOPPING_LIST
                + " WHERE item_num = " + itemNum + " AND store_num = "
                + storeNum;

        Cursor tt = null;
        try {
            tt = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "isItemOnStoreList() stmt " + stmt
                    + " failed " + e);
        }
        // no cursor? total fail.
        if (tt == null) {
            Log.e(ChipsList.TAG, "isItemOnStoreList() no cursor! stmt " + stmt
                    + " failed ");
            return false;
        }

        boolean val = true;
        // zero records would mean that this itemNum is not in the list...
        if (tt.getCount() == 0) {
            Log.d(ChipsList.TAG, "isItemOnStoreList(): zero records in cursor. return false");
            val = false;
        }

        tt.close();
        return val;

    }

    public void incrementQuantity(int itemNum, int storeNum) {
        Log.v(ChipsList.TAG, "increaseQuantity(): GOT HERE");

        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "UPDATE " + T_LOCATIONS
                + " set quantity = quantity + 1.0 WHERE store_num = "
                + storeNum + " AND item_num = " + itemNum;
        myExecSQL(db, "incrementQuantity()", stmt);

        return;
    }

    public String adKeywords() {
        Log.v(ChipsList.TAG, "adKeywords(): GOT HERE");

        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT store_name FROM " + T_STORES;
        Cursor tt = null;
        try {
            tt = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "adKeywords() stmt " + stmt
                    + " failed " + e);
        }
        // no cursor? total fail.
        String answer = "Grocery Shopping, Shopping, ";
        if (tt == null) {
            Log.e(ChipsList.TAG, "adKeywords() stmt " + stmt + " returned a null cursor");
            return answer;
        }

        while (tt.moveToNext()) {
            answer += tt.getString(0) + ", ";
        }

        tt.close();
        return answer;
    }


    public FoodRecord matchFoodRecord(FoodRecord food) {
        //Log.v(ChipsList.TAG, "matchFoodRecord(): GOT HERE");

        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT item_num FROM "
                + T_ITEMS
                + " WHERE item_name = \"" + food.getItemName() + "\"";
        Cursor tt;
        try {
            tt = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "matchFoodRecord() stmt " + stmt
                    + " failed " + e);
            //TODO: fix this. return null is probably wrong.
            return null;
        }

        if (tt == null) {

        } else if (tt.getCount() == 0) {
            stmt = "INSERT INTO "
                    + T_ITEMS
                    + " (item_name, location, size, price, isChecked, isTaxed, last_used, quantity ) VALUES (\""
                    + food.getItemName() + "\", \"" + food.getLocation() + "\", \"" + food.getSize()

                    // hard coded 0s are isChecked and last_used date.
                    //food.isTaxed() returns boolean -- database needs an int - convert to 1 or 0.
                    + "\", " + food.getPrice() + ", 0, "
                    + (food.isTaxed() ? 1 : 0)
                    + ", 0," + food.getQuantity()
                    + ")";

            myExecSQL(db, "matchFoodRecords", stmt);

            stmt = "SELECT item_num FROM "
                    + T_ITEMS
                    + " WHERE item_name = \""
                    + food.getItemName()
                    + "\"";
            Cursor cc = null;
            try {
                cc = db.rawQuery(stmt, null);
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "matchFoodRecord() stmt " + stmt
                        + " failed " + e);
                //TODO: fix this. return null is probably wrong.
                return null;
            }
            if (cc == null) {
                Log.e(ChipsList.TAG, "matchFoodRecord() stmt " + stmt
                        + " failed.  Cursor cc == null");
                //TODO: fix this. return null is probably wrong.
                return null;
            } else {
                cc.moveToFirst();
                food.setItemNum(cc.getInt(0));
                cc.close();
            }
        } else {
            tt.moveToFirst();
            food.setItemNum(tt.getInt(0));
            tt.close();
        }
        return food;
    }

    public StoreRecord matchStoreRecord(StoreRecord sr) {
        Log.v(ChipsList.TAG, "matchStoreRecord(): GOT HERE");

        if (db == null) {
            db = getWritableDatabase();
        }

        String stmt = "SELECT store_num, tax_rate FROM "
                + T_STORES
                + " WHERE store_name = \"" + sr.getStoreName() + "\"";

        Cursor c;
        try {
            c = db.rawQuery(stmt, null);
        } catch (Exception e) {
            Log.e(ChipsList.TAG, "matchStoreRecord() stmt " + stmt
                    + " failed " + e);
            //TODO: fix this.  exiting is probably the wrong thing to do.
            System.exit(1);
            return null;  //make android studio happy.
        }

        if (c == null) {
            Log.d(ChipsList.TAG, "matchStoreRecord(): Cursor c == null - exit");
            //TODO: fix this.  this exiting is probably the wrong thing to do.
            System.exit(1);

        } else if (c.getCount() == 0) {
            stmt = "INSERT INTO "
                    + T_STORES
                    + " (store_name, tax_rate ) VALUES (\""
                    + sr.getStoreName() + "\", " + sr.getTaxRate() + ")";

            myExecSQL(db, "matchStoreRecord", stmt);

            stmt = "SELECT store_num FROM " + T_STORES + " WHERE store_name = \""
                    + sr.getStoreName() + "\"";

            Cursor cc;
            try {
                cc = db.rawQuery(stmt, null);
            } catch (Exception e) {
                Log.e(ChipsList.TAG, "matchStoreRecord() stmt " + stmt
                        + " failed " + e);
                //TODO: fix this. return null is probably wrong.
                return null;
            }

            if (cc == null) {
                Log.e(ChipsList.TAG, "matchStoreRecord() stmt " + stmt
                        + " failed.  Cursor cc == null");
                //TODO: fix this. return null is probably wrong.
                return null;
            } else {
                cc.moveToFirst();
                sr.setStoreNumber(cc.getInt(0));
                cc.close();
            }
        } else {
            c.moveToFirst();
            sr.setStoreNumber(c.getInt(0));
        }
        c.close();
        return sr;
    }

    public void deleteAnItem(int itemNum) {
        Log.v(ChipsList.TAG, "deleteAnItem(): GOT HERE");

        if (db == null) {
            db = getWritableDatabase();
        }

        // 1 - delete from location table.
        String stmt;
        stmt = "DELETE FROM " + T_LOCATIONS
                + " WHERE item_num = " + itemNum;
        Log.d(ChipsList.TAG, "deleteAnItem() sql #1 : " + stmt);
        myExecSQL(db, "deleteAnItem", stmt);

        // 2 - delete from the shopping list.
        stmt = "DELETE FROM " + T_SHOPPING_LIST
                + " WHERE item_num = " + itemNum;
        Log.d(ChipsList.TAG, "deleteAnItem() sql #2 : " + stmt);
        myExecSQL(db, "deleteAnItem", stmt);

        // 3 - delete the actual item record.
        stmt = "DELETE FROM " + T_ITEMS
                + " WHERE item_num = " + itemNum;
        Log.d(ChipsList.TAG, "deleteAnItem() sql #1 : " + stmt);
        myExecSQL(db, "deleteAnItem", stmt);

    }
}
