package com.example.hairsalonbookingapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class Appointment extends AppCompatActivity {
    FloatingActionButton fbb;


    EditText date_in;
    EditText time_in;
    Spinner locationName, barberName;
    Button btnInsertData, btnChooseData;
    FirebaseAuth fAuth;
    ArrayAdapter<CharSequence> locationAdapter, barberAdapter;
    private String selectedBarber = "";

    public FirebaseFirestore db;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);
        fbb=findViewById(R.id.fbb);

        fbb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Appointment.this,Home.class);
                startActivity(intent);
            }
        });

        if (getSupportActionBar() != null)  //remove top actionbar
        {
            getSupportActionBar().hide();
        }
        date_in = findViewById(R.id.date_input);
        time_in = findViewById(R.id.time_input);

        locationName = findViewById(R.id.location);
        barberName = findViewById(R.id.barber);

        btnInsertData = findViewById(R.id.book_btn);
        btnChooseData = findViewById(R.id.detail_btn);

        locationAdapter = ArrayAdapter.createFromResource(this, R.array.Locations, android.R.layout.simple_spinner_item);

        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationName.setAdapter(locationAdapter);

        db = FirebaseFirestore.getInstance();

        btnChooseData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Appointment.this, Notification.class));
            }
        });

        locationName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String location = locationName.getSelectedItem().toString().trim();
                int parentID = parent.getId();
                if (parentID == R.id.location && !location.equals("Select a Location")) {
                    ArrayAdapter<CharSequence> barberAdapter;
                    switch (location) {
                        case "Padil": barberAdapter = ArrayAdapter.createFromResource(parent.getContext(), R.array.Barber_Padil, android.R.layout.simple_spinner_item);
                            break;
                        case "Kannur": barberAdapter = ArrayAdapter.createFromResource(parent.getContext(), R.array.Barber_kannur, android.R.layout.simple_spinner_item);
                            break;
                        case "Adyar": barberAdapter = ArrayAdapter.createFromResource(parent.getContext(), R.array.Barber_Adyar, android.R.layout.simple_spinner_item);
                            break;
                        case "Nantoor": barberAdapter = ArrayAdapter.createFromResource(parent.getContext(), R.array.Barber_Nantoor, android.R.layout.simple_spinner_item);
                            break;
                        case "Statebank": barberAdapter = ArrayAdapter.createFromResource(parent.getContext(), R.array.Barber_Statebank, android.R.layout.simple_spinner_item);
                            break;

                        default: barberAdapter = ArrayAdapter.createFromResource(parent.getContext(), R.array.Barber, android.R.layout.simple_spinner_item);
                            break;
                    }
                    barberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    barberName.setAdapter(barberAdapter);

                    // Get the selected barber from the spinner
                    selectedBarber = barberName.getSelectedItem().toString().trim();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Handle the case when nothing is selected
            }
        });

        btnInsertData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth fAuth = FirebaseAuth.getInstance();
                String id = fAuth.getCurrentUser().getUid();
                String date = date_in.getText().toString().trim();
                String time = time_in.getText().toString().trim();
                String location = locationName.getSelectedItem().toString().trim();
                String barber = barberName.getSelectedItem().toString().trim();
                if (barber.equals("Select a Barber Shop")) {
                    Toast.makeText(Appointment.this, "Please select a barber name", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveToFirestore(id, date, time, location, barber);
            }
        });

        date_in.setInputType(InputType.TYPE_NULL);
        time_in.setInputType(InputType.TYPE_NULL);

        date_in.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                showDateDialog(date_in);
            }
        });

        time_in.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                showTimeDialog(time_in);
            }
        });
    }

    private void saveToFirestore(String id, String date, String time, String location, String barber) {

        if (location.equals("Select a Location")) {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!date.isEmpty() && !time.isEmpty()) {
            Calendar selectedDate = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            try {
                selectedDate.setTime(dateFormat.parse(date));
                Calendar currentDate = Calendar.getInstance();
                if (selectedDate.before(currentDate)) {
                    Toast.makeText(this, "Please select a future date", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("date", date);
            map.put("time", time);
            map.put("location", location);
            map.put("barber", barber);

            db.collection("Documents").document(id).set(map)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Appointment.this, "Congrats! Your appointment is booked successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Appointment.this, "Appointment booking failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showTimeDialog(final EditText time_in) {
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                time_in.setText(simpleDateFormat.format(calendar.getTime()));
            }
        };

        new TimePickerDialog(Appointment.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showDateDialog(final EditText date_in) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                date_in.setText(simpleDateFormat.format(calendar.getTime()));
            }
        };
        new DatePickerDialog(Appointment.this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();

    }
}
