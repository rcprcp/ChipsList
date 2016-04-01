package com.cottagecoders.chipslist;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

public class Parse {
    DatabaseCode myDB;
    Context ctx;

    public Parse(Context ctx) {
        this.ctx = ctx;
        myDB = new DatabaseCode(ctx);
    }

    public String encode(StoreRecord sr) {

        String answer;

        ArrayList<FoodRecord> food;
        food = myDB.getAllFoodAlpha(sr.getStoreNumber());

        if (food.size() > 0) {
            answer = header();
            answer += encodeStoreRecord(sr);

            for (int i = 0; i < food.size(); i++) {
                answer += encodeFoodRecord(food.get(i));
            }
        } else {
            answer = null;
        }
        return answer;
    }

    public String encode(StoreRecord sr, FoodRecord food) {
        String answer = header();
        answer += encodeStoreRecord(sr);
        answer += encodeFoodRecord(food);
        return answer;
    }

    public String header() {
        return " Shared from **ChipsList**.  Run ChipsList to import this information.  " +
                "ChipsList is available from the Google Play Store:  " +
                "https://play.gooogle.com/store/apps/com.cottagecoders.chipslist   ";
    }

    private String encodeFoodRecord(FoodRecord food) {
        return "^<" + food.getItemNum() +
                "><" + food.getItemName().trim() +
                "><" + food.getPrice() +
                "><" + food.getSize().trim() +
                "><" + food.getLocation().trim() +
                "><" + food.getQuantity() +
                "><" + food.getSequence() +
                "><" + food.getStoreNum() +
                "><" + food.isChecked() +
                "><" + food.isTaxed() +
                ">^";
    }

    public FoodRecord decodeFoodRecord(String record) {

        int itemNum = 0;
        String itemName = "";
        double price = 0.0;
        String size = "";
        String location = "";
        double quantity = 0.0;
        int sequence = 0;
        int storeNum = 0;
        int checked = 0;
        int taxed = 0;

        for (int i = 0; i < 10; i++) {
            int st;
            int ee;
            if (record.contains("<")) {
                st = record.indexOf("<");
                ee = record.indexOf(">");
                String temp = record.substring(st + 1, ee).trim();
                if (i == 0) {
                    try {
                        itemNum = Integer.parseInt(temp);
                    } catch (Exception e) {
                        itemNum = 0;
                    }
                } else if (i == 1) {
                    itemName = temp.trim();
                } else if (i == 2) {
                    try {
                        price = Double.parseDouble(temp);
                    } catch (Exception e) {
                        price = 0.0;
                    }
                } else if (i == 3) {
                    size = temp.trim();
                } else if (i == 4) {
                    location = temp.trim();
                } else if (i == 5) {
                    try {
                        quantity = Double.parseDouble(temp.trim());
                    } catch (Exception e) {
                        quantity = 1.0;
                    }
                } else if (i == 6) {
                    try {
                        sequence = Integer.parseInt(temp.trim());
                    } catch (Exception e) {
                        sequence = 0;
                    }
                } else if (i == 7) {
                    try {
                        storeNum = Integer.parseInt(temp.trim());
                    } catch (Exception e) {
                        storeNum = 0;
                    }
                } else if (i == 8) {
                    checked = 0;
                    if (temp.trim().contains("true")) {
                        checked = 1;
                    }
                } else if (i == 9) {
                    taxed = 0;
                    if (temp.trim().contains("true")) {
                        taxed = 1;
                    }
                }
            } else {
                break;
            }
            ee++;
            record = record.substring(ee).trim();
        }

        return new FoodRecord(itemName, location, size, price,
                itemNum, storeNum, checked, taxed, quantity, sequence);
    }

    private String encodeStoreRecord(StoreRecord sr) {
        return "~<" + sr.getStoreName().trim() +
                "><" + sr.getStoreNumber() +
                "><" + sr.getTaxRate() + ">~";
    }

    public StoreRecord decodeStoreRecord(String record) {
        String name = "";
        int number = 0;
        double taxRate = 0.0;

        int start = record.indexOf("~");
        start++;
        int end = record.indexOf("~", start);
        record = record.substring(start, end);

        for (int i = 0; i < 3; i++) {
            int ee;
            if (record.contains("<")) {
                ee = record.indexOf(">");
                String temp = record.substring(1, ee);
                Log.d(ChipsList.TAG, "decodeStoreRecord(): " + i + " \"" + temp + "\"");
                if (i == 0) {
                    if (temp == null) {
                        name = "";
                    } else {
                        name = temp.trim();
                    }
                } else if (i == 1) {
                    try {
                        number = Integer.parseInt(temp.trim());
                    } catch (Exception e) {
                        number = 0;
                    }
                } else if (i == 2) {
                    try {
                        taxRate = Double.parseDouble(temp.trim());
                    } catch (Exception e) {
                        taxRate = 0.0;
                    }
                }
            } else {
                break;
            }
            record = record.substring(ee + 1);
        }
        return new StoreRecord(name.trim(), number, taxRate);
    }
}
