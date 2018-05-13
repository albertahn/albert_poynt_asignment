package com.client.albert.clientposmerchant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean resultValChange;

    private Button payButton, clearBtn;
    private EditText paymentInput;
    private TextView resultShowText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultValChange = false;

        paymentInput = (EditText) findViewById(R.id.payment_input);
        payButton = (Button) findViewById(R.id.make_payment_btn);
        resultShowText = (TextView) findViewById(R.id.result_show_text);
        clearBtn = (Button) findViewById(R.id.clear_btn);

        //set click listener to trigger payment fragment on other poynt application
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int amountInCent = 100* Integer.parseInt(paymentInput.getText().toString()) ;
                Log.d(TAG, "cents: "+amountInCent);

                updateFirebasePay(""+amountInCent);

            }
        });


        //button to clear the
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultShowText.setText(getResources().getString(R.string.intro));
            }
        });


        //set socket listener for firebase that connects the poynt app
        setSocketListener();
    }//oncreate


    //sends amount and currency to poynt application to create payment fragment
    private void updateFirebasePay(String amountCents){

        //set socket for changes in result of payment
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //reference directory in the firebase database
        DatabaseReference resultsRef = database.getReference("poynt_pay");

        try {
            //set value in firebase realtime database
            resultsRef.child("amount").setValue(amountCents);
            //checks if the payment was initiated
            resultsRef.child("updated").setValue(true);
            //set currency usd as default
            resultsRef.child("currency").setValue("USD");

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /* sets listener to socket from firebase to receive the results of the payment fragment */
    private void setSocketListener(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference socketRef = database.getReference("poynt_payment_result");

        //socket reference connect and listen to changes
        socketRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
               //String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, resultValChange+ "dataSnapshot is: " +dataSnapshot.getValue());

                try{

                    if(resultValChange == true){

                        resultShowText.setText("result from payment: "+dataSnapshot.toString());

                    }else{

                        resultValChange = true;


                    }

                }catch (Exception e){

                    Log.e(TAG, e.toString());

                }

                resultValChange = true;



            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }
}
