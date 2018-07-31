package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Daniel on 28.06.2018.
 */

public class InventoryDBHelper extends SQLiteOpenHelper {

    public final static String DATABASE_NAME = "inventory.db";
    public static final int DATABASE_VERSION = 2;

    public final static String TABLE = "inventory";

    public InventoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ENTRIES = "CREATE TABLE " + InventoryEntry.TABLE_NAME +
                " (" + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_INVENTORY_PRICE + " INTEGER," +
                InventoryEntry.COLUMN_INVENTORY_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONENUMBER + " TEXT NOT NULL)";

        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE;
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
