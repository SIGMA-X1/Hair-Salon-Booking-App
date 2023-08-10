package com.example.hairsalonbookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Services extends AppCompatActivity {
    Button btn1,btn2,btn3,btn4,btn5;
    FloatingActionButton fbb;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        btn1=findViewById(R.id.btn1);
        btn2=findViewById(R.id.btn2);
        btn3=findViewById(R.id.btn3);
        btn4=findViewById(R.id.btn4);
        btn5=findViewById(R.id.btn5);
        fbb=findViewById(R.id.fbb);

        fbb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Services.this,Home.class);
                startActivity(intent);
            }
        });


        btn1.setOnClickListener(v -> {

            startActivity(new Intent(Services.this,Mencut.class));
        });
        btn2.setOnClickListener(v -> {

            startActivity(new Intent(Services.this,womencut.class));
        });
        btn3.setOnClickListener(v -> {

            startActivity(new Intent(Services.this,Trim.class));
        });
        btn4.setOnClickListener(v -> {

            startActivity(new Intent(Services.this,Tattoo.class));
        });
        btn5.setOnClickListener(v -> {
            startActivity(new Intent(Services.this,hairdye.class));
        });


        if (getSupportActionBar() != null)  //remove top actionbar
        {
            getSupportActionBar().hide();
        }
    }
}