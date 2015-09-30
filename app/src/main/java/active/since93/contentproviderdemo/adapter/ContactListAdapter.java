package active.since93.contentproviderdemo.adapter;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import active.since93.contentproviderdemo.AddContactActivity;
import active.since93.contentproviderdemo.R;
import active.since93.contentproviderdemo.model.ContactItems;

/**
 * Created by darshan.parikh on 24-Sep-15.
 */
public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {

    List<ContactItems> contactItemsList;
    Context context;

    public ContactListAdapter(Context context, List<ContactItems> contactItemsList) {
        this.context = context;
        this.contactItemsList = contactItemsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_raw, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ContactItems contactItems = contactItemsList.get(position);
        holder.name.setText(contactItems.getName());
        holder.number.setText(contactItems.getNumber());
        holder.contactImage.setImageBitmap(contactItems.getImage());
        holder.id = contactItems.getId();
    }

    @Override
    public int getItemCount() {
        return (null == contactItemsList ? 0 : contactItemsList.size());
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        TextView name;
        TextView number;
        String id;
        ImageView contactImage;

        public ViewHolder(View itemView) {
            super(itemView);
            this.name = (TextView) itemView.findViewById(R.id.txtName);
            this.number = (TextView) itemView.findViewById(R.id.txtNumber);
            this.contactImage = (ImageView) itemView.findViewById(R.id.contactImage);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            contactImage.setDrawingCacheEnabled(false);
            contactImage.setDrawingCacheEnabled(true);
            Bitmap bitmap = contactImage.getDrawingCache();
            showDialog(getPosition(), number.getText().toString(), name.getText().toString(), id, bitmap);
            return false;
        }
    }

    void showDialog(final int position, final String number, final String name, final String id, final Bitmap bitmap) {
        String[] menuItems = {"Update", "Delete"};
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle("Select action");
        adb.setCancelable(true).setItems(menuItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent intent = new Intent(context, AddContactActivity.class);
                    intent.putExtra("image", bitmap/*getByteArray(bitmap)*/);
                    intent.putExtra("name", name);
                    intent.putExtra("id", id);
                    intent.putExtra("number", number);
                    context.startActivity(intent);
                    ((Activity)context).overridePendingTransition(R.anim.left_slide_in, R.anim.left_slide_out);
                } else {
                    showWarningDialog(position, number, id);
                }
            }
        }).show();
    }

    void showWarningDialog(final int position, final String number, final String id) {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle("Warning!")
                .setMessage("Do you want to delete this contact?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeAt(position);
                        deleteContact(id);
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    void removeAt(int position) {
        contactItemsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, contactItemsList.size());
    }

    public void deleteContact(String id) {
        ContentResolver contactHelper = context.getContentResolver();
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String[] args = new String[] { id };
        ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI).withSelection(RawContacts.CONTACT_ID + "=?", args).build());
        try {
            contactHelper.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
    byte[] getByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
}