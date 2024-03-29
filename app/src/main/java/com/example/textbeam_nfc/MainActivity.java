package com.example.textbeam_nfc;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Parcelable;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements CreateNdefMessageCallback {

    @SuppressLint("NewApi")
    private static final SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");// time format
    Date received_date= new Date();
    Date currentTime= new Date();
    Date currentTime_check= new Date();
    Date parsed_receivedDate= new Date();
    NfcAdapter nfcAdapter;
    EditText textBeam;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
             if (nfcAdapter == null) {// Check for available NFC Adapter
                 Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }//end if
            if (!nfcAdapter.isEnabled()){//NFC is not on.
                    Toast.makeText(this, "NFC is disabled. Please turn on in settings.", Toast.LENGTH_LONG).show();
            }else{//NFC is  on.
            Toast.makeText(this, "NFC is enabled. You can begin transfer.", Toast.LENGTH_LONG).show();
            }//end if

        textBeam = (EditText) findViewById(R.id.textBeam);// references the edittext for entering text

        nfcAdapter.setNdefPushMessageCallback(this, this);// if NFC is present then the call back will be used
            //Accepts a callback that contains a createNdefMessage() which is
            // called when a device is in range to beam data to. The callback lets you create the NDEF message only when necessary.
    }//end onCreate

    @SuppressLint("NewApi")
    @Override
    public NdefMessage createNdefMessage(NfcEvent event ) {//before the record can be sent it has to be in a message

        currentTime = Calendar.getInstance().getTime();// uses the time from the device
        String UserString = textBeam.getText().toString()+" \nTimestamp:"+" " +sdf3.format(currentTime);// we get the text of the string that was entered in the edittext
        System.out.println(sdf3.format(currentTime));
        System.out.println(UserString);//prints to log
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
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[] { intent }, 0);
        if (nfcAdapter == null)
            return;
        String[][] techList = new String[][] {};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null); //keeps the messaged received in the app
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null)
            return;
        nfcAdapter.disableForegroundDispatch(this);//keeps the messaged received in the app
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        processIntent(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void processIntent(Intent intent) {
        TextView textView = (TextView) findViewById(R.id.textView2);

        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES); //Extra containing an array of NdefMessage present on the discovered tag.
        NdefMessage msg = (NdefMessage) rawMsgs[0];// only one message sent during the beam
        String msg1 = new String(msg.getRecords()[0].getPayload());
        textView.setText(new String(msg.getRecords()[0].getPayload())); // sets the textview to the message
        System.out.println(new String(msg.getRecords()[0].getPayload()));//prints to log
        String[] arr = null;
        arr = msg1.split(" ");
        try {
            System.out.println(arr[arr.length - 1]);
             String received_date = arr[arr.length - 1];
            parsed_receivedDate = sdf3.parse(received_date);
            System.out.println(parsed_receivedDate);
            currentTime_check = Calendar.getInstance().getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String elapsed_Time = printDifference(parsed_receivedDate, currentTime_check);
        Toast.makeText(this, "Elapsed Time:"+ elapsed_Time, Toast.LENGTH_LONG).show();
    }//end processIntent


    public String printDifference(Date startDate, Date endDate) { //method to calculate the difference between the sending time and received time.

        long different = endDate.getTime() - startDate.getTime(); //milliseconds

        System.out.println("startDate : " + startDate);
        System.out.println("endDate : "+ endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        Long elapsedMillies = different % secondsInMilli;

        return elapsedDays+"days,"+elapsedHours+"hours," +elapsedMinutes+"minutes,"+elapsedSeconds+"seconds," ;
    }//end printDifference
}//end Main Activity