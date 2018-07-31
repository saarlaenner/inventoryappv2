package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.CatalogActivity;
import com.example.android.inventoryapp.R;

/**
 * Created by Daniel on 28.06.2018.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the product name for the current inventory can be set on the name TextView
     * in the list item layout.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView productNameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        final int productNameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
        final int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY);
        final int id = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));

        String inventoryProductName = cursor.getString(productNameColumnIndex);
        final String inventoryPrice = cursor.getString(priceColumnIndex);
        final String inventoryQuantityStr = String.valueOf(cursor.getInt(quantityColumnIndex));
        final int inventoryQuantity = cursor.getInt(quantityColumnIndex);

        productNameTextView.setText(inventoryProductName);
        priceTextView.setText("Price: " + inventoryPrice);
        quantityTextView.setText("Quantity: " + inventoryQuantityStr);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CatalogActivity catalogActivity = (CatalogActivity) context;
                Uri updateUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (inventoryQuantity >= 1) {
                    int tmpQuantity = inventoryQuantity - 1;
                    values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY, tmpQuantity);
                    resolver.update(updateUri, values, null, null);
                    context.getContentResolver().notifyChange(updateUri, null);
                } else {
                    Toast.makeText(context, "Item quantity could not be lower than 0", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}
