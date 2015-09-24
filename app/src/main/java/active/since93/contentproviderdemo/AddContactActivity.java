package active.since93.contentproviderdemo;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by darshan.parikh on 24-Sep-15.
 */
public class AddContactActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        editTextName = (EditText) findViewById(R.id.edtTxtName);
        editTextNumber = (EditText) findViewById(R.id.edtTxtNumber);
        Button addContactBtn = (Button) findViewById(R.id.addContactBtn);

        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String number = editTextNumber.getText().toString().trim();
                if(insertContact(name, number) && !(name.isEmpty() || number.isEmpty())) {
                    Snackbar.make(v, "Contact added.", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(v, "Failed to add contact.", Snackbar.LENGTH_LONG).show();
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
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName).build());
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
}