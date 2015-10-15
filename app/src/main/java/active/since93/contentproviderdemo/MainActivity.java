package active.since93.contentproviderdemo;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import active.since93.contentproviderdemo.adapter.ContactListAdapter;
import active.since93.contentproviderdemo.model.ContactItems;

/**
 * Created by darshan.parikh on 24-Sep-15.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView contactListRecyclerView;
    private List<ContactItems> contactItemsList;
    private ContactListAdapter contactListAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBarLoading);
        contactListRecyclerView = (RecyclerView) findViewById(R.id.contactsList);
        contactListRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        contactItemsList = new ArrayList<ContactItems>();

        new AsyncTaskGetContacts().execute();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(id == R.id.action_refresh) {
            new AsyncTaskGetContacts().execute();
        }

        return super.onOptionsItemSelected(item);
    }

    // Display all the contacts with numbers
    private List<ContactItems> displayContacts() {

        List<ContactItems> contactItemsList = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String phoneNo = "";
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Bitmap contactImage;
                    Bitmap default_photo = BitmapFactory.decodeResource(getApplicationContext().getResources(), android.R.drawable.ic_menu_myplaces);
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        phoneNo += pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + (pCur.isLast() ? "" : "\n");
                    }

                    Uri my_contact_Uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id);
                    InputStream photo_stream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), my_contact_Uri);
                    if(photo_stream != null){
                        BufferedInputStream buf = new BufferedInputStream(photo_stream);
                        Bitmap my_btmp = BitmapFactory.decodeStream(buf);
                        contactImage = my_btmp;
                    }else{
                        contactImage = default_photo;
                    }

                    contactItemsList.add(new ContactItems(id, name, phoneNo, contactImage));
                    pCur.close();
                }
            }
        }
        return contactItemsList;
    }

    // Sort contacts in alphabetical order
    public class CustomComparator implements Comparator<ContactItems> {
        @Override
        public int compare(ContactItems o1, ContactItems o2) {
            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
        }
    }

    class AsyncTaskGetContacts extends AsyncTask<Void, Void, List<ContactItems>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<ContactItems> doInBackground(Void... params) {
            List<ContactItems> contactItemsList = new ArrayList<>();
            contactItemsList = displayContacts();
            return contactItemsList;
        }

        @Override
        protected void onPostExecute(List<ContactItems> aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            Collections.sort(aVoid, new CustomComparator());
            contactListAdapter = new ContactListAdapter(MainActivity.this, aVoid);
            contactListRecyclerView.setAdapter(contactListAdapter);
        }
    }
}
