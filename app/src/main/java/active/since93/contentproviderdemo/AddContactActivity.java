package active.since93.contentproviderdemo;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import active.since93.contentproviderdemo.imageview.CircleImageView;

/**
 * Created by darshan.parikh on 24-Sep-15.
 */
public class AddContactActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextNumber;
    private Button addContactBtn;
    private CircleImageView contactImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        editTextName = (EditText) findViewById(R.id.edtTxtName);
        editTextNumber = (EditText) findViewById(R.id.edtTxtNumber);
        addContactBtn = (Button) findViewById(R.id.addContactBtn);
        contactImage = (CircleImageView) findViewById(R.id.contactImage);

        final String nameStr = getIntent().getStringExtra("name");
        final String numberStr = getIntent().getStringExtra("number");
        final String idStr = getIntent().getStringExtra("id");

        if(nameStr != null && numberStr != null && idStr  != null) {
            editTextName.setText(nameStr);
            editTextNumber.setText(numberStr);
            addContactBtn.setText("UPDATE");

            Bitmap bitmap = getIntent().getParcelableExtra("image");
//            byte[] byteArray = getIntent().getByteArrayExtra("image");
            if(bitmap != null) {
//                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                contactImage.setImageBitmap(bitmap);
            }
        } else {
            addContactBtn.setText("ADD");
        }

        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String number = editTextNumber.getText().toString().trim();

                if(nameStr == null || numberStr == null || idStr  == null) {
                    if (insertContact(name, number) && !(name.isEmpty() || number.isEmpty())) {
                        snackBar(v, "Contact added.");
                    } else {
                        snackBar(v, "Failed to add contact.");
                    }
                } else {
                    if(updateContact(idStr, name, number)) {
                        snackBar(v, "Contact updated.");
                    } else {
                        snackBar(v, "Failed to update contact.");
                    }
                }
                editTextName.setText("");
                editTextNumber.setText("");
            }
        });
    }

    // Insert contact
    public boolean insertContact(String firstName, String mobileNumber) {
        ContentResolver contentResolver = this.getContentResolver();
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, firstName).build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    void snackBar(View v, String str) {
        Snackbar.make(v, str, Snackbar.LENGTH_LONG).show();
    }

    boolean updateContact(String id, String name, String number) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE
                        + "=?", new String[]{id, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE
                        + "=? AND " + ContactsContract.CommonDataKinds.Organization.TYPE + "=?"
                        , new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        , String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)})
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                .build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}