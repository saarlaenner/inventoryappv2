package com.example.android.inventoryapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryDBHelper;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Daniel on 28.06.2018.
 */

public class EditorActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_INVENTORY_LOADER = 0;
    Uri currentInventoryUri;
    private InventoryDBHelper mDbHelper;
    /**
     * EditText field to enter the product name
     */
    private EditText mProductNameEditText;
    /**
     * EditText field to enter the product price
     */
    private EditText mPriceEditText;
    /**
     * EditText field to enter the inventory's weight
     */
    private EditText mQuantityEditText;
    /**
     * EditText field to enter the inventory's gender
     */
    private EditText mSupplierNameEditText;

    private EditText mSupplierPhoneNumberEditText;

    private Button mIncreaseButton;

    private Button mDecreaseButton;

    private Button mCallSupplier;

    private String inventoryPhoneNumber;

    private static final int PERMISSION_REQUEST_CODE = 1;

    private boolean mInventoryHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        currentInventoryUri = getIntent().getData();
        if (currentInventoryUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_inventory));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_inventory));
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mProductNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierPhoneNumberEditText = (EditText) findViewById(R.id.edit_supplier_phone_number);
        mIncreaseButton = (Button) findViewById(R.id.btn_increase);
        mDecreaseButton = (Button) findViewById(R.id.btn_decrease);
        mCallSupplier = (Button) findViewById(R.id.btn_call_supplier);

        mDbHelper = new InventoryDBHelper(this);

        mProductNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);

        mIncreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseQuantity();
            }
        });

        mDecreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseQuantity();
            }
        });

        mCallSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inventoryPhoneNumber = mSupplierPhoneNumberEditText.getText().toString();
                makeCall(inventoryPhoneNumber);
            }
        });

    }

    public void makeCall(String s)
    {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + s));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            requestForCallPermission();
        } else {
            startActivity(intent);
        }
    }

    public void requestForCallPermission()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CALL_PHONE)) {

        } else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall(inventoryPhoneNumber);
                }
                break;
        }
    }

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mInventoryHasChanged boolean to true.

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new inventory, hide the "Delete" menu item.
        if (currentInventoryUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog for better user experience
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // If user click "Keed Editiong", still stay on edit site
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the inventory hasn't changed, continue with handling back button press
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONENUMBER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentInventoryUri,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int productNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
            int supplierPhonenumberColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONENUMBER);

            String productName = cursor.getString(productNameColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhoneNumber = cursor.getString(supplierPhonenumberColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            final int quantity = cursor.getInt(quantityColumnIndex);

            mProductNameEditText.setText(productName);
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneNumberEditText.setText(supplierPhoneNumber);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));

        }
    }

    private void decreaseQuantity() {
        String previousValueString = mQuantityEditText.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            return;
        } else if (previousValueString.equals("0")) {
            return;
        } else {
            previousValue = Integer.parseInt(previousValueString);
            mQuantityEditText.setText(String.valueOf(previousValue - 1));
        }
    }

    private void increaseQuantity() {
        String previousValueString = mQuantityEditText.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = Integer.parseInt(previousValueString);
        }
        mQuantityEditText.setText(String.valueOf(previousValue + 1));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveInventory();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If the inventory hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mInventoryHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the inventory.
                deleteInventory();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the inventory.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the inventory in the database.
     */
    private void deleteInventory() {
        if (currentInventoryUri != null) {
            int rowsDeleted = getContentResolver().delete(currentInventoryUri, null, null);
            Toast.makeText(EditorActivity.this, R.string.editor_delete_inventory_successful, Toast.LENGTH_SHORT).show();
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_inventory_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_inventory_successful), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void saveInventory() {

        String inventoryPriceString = mPriceEditText.getText().toString().trim();
        String inventoryQuantityString = mQuantityEditText.getText().toString().trim();
        String inventoryProductName = mProductNameEditText.getText().toString().trim();
        String inventorySupplierName = mSupplierNameEditText.getText().toString().trim();
        String inventorySupplierPhonenumber = mSupplierPhoneNumberEditText.getText().toString().trim();

        //Checking for NULL values
        if (inventoryProductName.equals("") || inventoryQuantityString.equals("") || inventoryPriceString.equals("")
                || inventorySupplierName.equals("") || inventorySupplierPhonenumber.equals("")) {
            Toast.makeText(this, getString(R.string.all_data_required),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentInventoryUri == null &&
                TextUtils.isEmpty(inventoryProductName) &&
                TextUtils.isEmpty(inventorySupplierName) &&
                TextUtils.isEmpty(inventoryPriceString) &&
                TextUtils.isEmpty(inventoryQuantityString) &&
                TextUtils.isEmpty(inventorySupplierPhonenumber)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME, inventoryProductName);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME, inventorySupplierName);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONENUMBER, inventorySupplierPhonenumber);
        Double price = 0.0;
        if (!TextUtils.isEmpty(inventoryPriceString)) {
            price = Double.parseDouble(inventoryPriceString);
        }
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, price);
        int quantity = 0;
        if (!TextUtils.isEmpty(inventoryQuantityString)) {
            quantity = Integer.parseInt(inventoryQuantityString);
        }
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);

        if (currentInventoryUri == null) {
            try {
                Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
                Toast.makeText(this, getString(R.string.inventory_saved) + newUri, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("", e.getMessage());
                Toast.makeText(this, R.string.inventory_saving_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                int mRowsUpdated = getContentResolver().update(currentInventoryUri, values, null, null);
                Toast.makeText(this, "There was updaes made on rows: " + mRowsUpdated, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("", e.getMessage());
                Toast.makeText(this, R.string.inventory_saving_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
