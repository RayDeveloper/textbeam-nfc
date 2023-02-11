package com.example.textbeam_nfc;


import static android.nfc.NdefRecord.createMime;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements CreateNdefMessageCallback {

    @SuppressLint("NewApi")
    private static final SimpleDateFormat sdf3 = new SimpleDateFormat("HH:mm:ss");// time format


    NfcAdapter nfcAdapter;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



         textView = (TextView) findViewById(R.id.textBeam);// references the textview for entering text

        // Check for available NFC Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }//end if

        // Register callback
        nfcAdapter.setNdefPushMessageCallback(this, this);// if NFC is present then the call back will be used
        //Accepts a callback that contains a createNdefMessage() which is
        // called when a device is in range to beam data to. The callback lets you create the NDEF message only when necessary.

    }//end onCreate

    @SuppressLint("NewApi")
    @Override
    public NdefMessage createNdefMessage(NfcEvent event ) {//before the record can be sent it has to be in a message

        Date currentTime = Calendar.getInstance().getTime();// uses the time from the device
        String UserString = textView.getText().toString()+" \nTimestamp:" +sdf3.format(currentTime);// we get the text of the string that was entered in the textview
        byte[] StringBytes = UserString.getBytes(); // the string has to be converted to bytes.

        NdefRecord ndefRecordOut = new NdefRecord( // the record has to be created first then the message
                NdefRecord.TNF_MIME_MEDIA,// record type of text
                UserString.getBytes(), //string as bytes
                new byte[] {}, //empty byte array
                StringBytes);// text as bytes in the array

        NdefMessage ndefMessageout = new NdefMessage(ndefRecordOut);
        return ndefMessageout;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }


    void processIntent(Intent intent) {
        textView = (TextView) findViewById(R.id.textBeam);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES); //Extra containing an array of NdefMessage present on the discovered tag.
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        textView.setText(new String(msg.getRecords()[0].getPayload())); // sets the textview to the message
    }
}//end Main Activity