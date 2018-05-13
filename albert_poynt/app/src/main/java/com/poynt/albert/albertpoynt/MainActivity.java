package com.poynt.albert.albertpoynt;

import android.os.Bundle;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.UUID;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionReference;
import co.poynt.api.model.TransactionReferenceType;
import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;
import co.poynt.os.model.PaymentStatus;

public class MainActivity extends Activity {

    //tag for logs and debug
    private static final String TAG = MainActivity.class.getSimpleName();
    // request code for payment service activity
    private static final int COLLECT_PAYMENT_REQUEST = 13132;

    //refference for uuid
    private String mReferenceId;

    //textview for showing results
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set view by id
        resultText = (TextView) findViewById(R.id.main_payment_result);

        //socket listener for changes on client side pos terminal that initiates the payment
        setSocketListener();


    }

    /* sets listener to socket from firebase  */
    public void setSocketListener(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //reference in realtime database for changes in value
        final DatabaseReference socketRef = database.getReference("poynt_pay");

        //set socket listener
        socketRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.child("amount").getValue(String.class);
                Log.d(TAG, "Value is: " + dataSnapshot.child("updated").getValue());

                boolean updated = (boolean) dataSnapshot.child("updated").getValue();

                String currency = (String) dataSnapshot.child("currency").getValue();

                //when creating activity
                if(updated){

                    //launches payment fragment
                    launchPayment(Long.parseLong(value, 10), currency);

                    //reset the boolean value to listen to changes
                    socketRef.child("updated").setValue(false);

                }

            }//data change

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        }); //sock ref


    }//setSocketListener



    //updates the payment results and send the payment data back to merchant client after the payment fragment has concluded
    public void updateFirebasePayResult(String paymentRes){

        //set socket for changes in result of payment
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //reference to key for socket
        DatabaseReference resultsRef = database.getReference("poynt_payment_result");

        try {

            resultsRef.setValue(paymentRes);

        } catch (Exception e) {

            e.printStackTrace();
        }


    }

    //for esspresso unit tests had to make the functions public
    //launched payment fragment after application receives ammount and currency from merchants client
    public void launchPayment(long amountPayCents, String currency) {

        mReferenceId = UUID.randomUUID().toString();

        Payment payment = new Payment();
        // amount in cents

        payment.setAmount(amountPayCents);

        TransactionReference posRefId = new TransactionReference();
        // "posReferenceId" is a predefined param which is searchable in Poynt HQ (merchant dashboard)
        posRefId.setCustomType("posReferenceId");
        posRefId.setType(TransactionReferenceType.CUSTOM);
        posRefId.setId(mReferenceId);

        payment.setReferences(Arrays.asList(posRefId));

        payment.setCurrency(currency);

        try {
            Intent collectPaymentIntent = new Intent(Intents.ACTION_COLLECT_PAYMENT);
            collectPaymentIntent.putExtra(Intents.INTENT_EXTRAS_PAYMENT, payment);
            startActivityForResult(collectPaymentIntent, COLLECT_PAYMENT_REQUEST);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Poynt Payment Activity not found - did you install PoyntServices?", ex);
        }
    }

    //result returns after payment fragment concludes
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "Received onActivityResult (" + requestCode + ")");
        // Check which request we're responding to
        if (requestCode == COLLECT_PAYMENT_REQUEST) {
            logData("Received onActivityResult from Payment Action");
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {

                    // see the bottom of this file for full JSON of the Payment object
                    Payment payment = data.getParcelableExtra(Intents.INTENT_EXTRAS_PAYMENT);
                    if (payment != null) {
                        Gson gson = new Gson();
                        Type paymentType = new TypeToken<Payment>(){}.getType();
                        String paymentString =   gson.toJson(payment, paymentType);
                        Log.d(TAG + "paymentOBJ", paymentString);

                        updateFirebasePayResult(paymentString);

                        //show result of payment object in view
                        try {
                            JSONObject object = new JSONObject(gson.toJson(payment, paymentType));

                            //showing results in pretty print
                            resultText.setText("payment result: " +object.toString(2));

                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                        // tip amounts may not be in the Transaction object as tip adjustment
                        // happens asynchronously, but Payment object carries the mapping between
                        // transaction UUID (transaction.getId) and the tip amount in cents
                        // Map<UUID, Long> tipAmounts =  payment.getTipAmounts();

                        for (Transaction t : payment.getTransactions()) {
                            Log.d(TAG, "Processor response: " + t.getProcessorResponse());
                        }

                        Log.d(TAG, "Received onPaymentAction from PaymentFragment w/ Status("
                                + payment.getStatus() + ")");
                        if (payment.getStatus().equals(PaymentStatus.COMPLETED)) {

                            logData("Payment Completed");
                        } else if (payment.getStatus().equals(PaymentStatus.DECLINED)) {
                            logData("Payment DECLINED");
                        } else if (payment.getStatus().equals(PaymentStatus.CANCELED)) {
                            logData("Payment Canceled");
                        } else if (payment.getStatus().equals(PaymentStatus.FAILED)) {
                            logData("Payment Failed");
                        } else if (payment.getStatus().equals(PaymentStatus.REFUNDED)) {
                            logData("Payment Refunded");
                        } else if (payment.getStatus().equals(PaymentStatus.VOIDED)) {
                            logData("Payment Voided");
                        } else {
                            logData("Payment Completed");
                        }
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                logData("Payment Canceled");
            }
        }
    }

    private void logData(String s) {
        Log.d(TAG, "logData: " + s);
    }
}

