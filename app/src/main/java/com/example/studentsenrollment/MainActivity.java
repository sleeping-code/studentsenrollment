package com.example.studentsenrollment;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.ContentValues;

import com.example.studentsenrollment.R;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private final int MAX_CREDITS = 21;
    private final int MIN_CREDITS = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getWindow().getDecorView().setBackgroundColor(Color.parseColor("#FFF9C4"));

        // Initialize Database
        db = openOrCreateDatabase("student_db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS students (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS enrollment (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, subject TEXT, credits INTEGER);");

        // Subjects
        String[] subjects = new String[]{
                "DSA (3 credits)",
                "AI (3 credits)",
                "NM (3 credits)",
                "NS (3 credits)",
                "AW (3 credits)",
                "SE (3 credits)",
                "3D (3 credits)",
                "WMP (3 credits)"
        };

        // UI Elements
        Button registerButton = findViewById(R.id.registerButton);
        Button loginButton = findViewById(R.id.loginButton);
        Button enrollButton = findViewById(R.id.enrollButton);
        Button summaryButton = findViewById(R.id.summaryButton);
        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Spinner subjectSpinner = findViewById(R.id.subjectSpinner);
        TextView resultView = findViewById(R.id.resultView);

        // Set button background color to yellow
        int blueColor = Color.parseColor("#001F3F");
        registerButton.setBackgroundColor(blueColor);
        loginButton.setBackgroundColor(blueColor);
        enrollButton.setBackgroundColor(blueColor);
        summaryButton.setBackgroundColor(blueColor);

        // Populate Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(adapter);

        // Register
        registerButton.setOnClickListener(view -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (!isValidUsername(username)) {
                resultView.setText("Username must be 8-12 characters, lowercase letters and numbers only.");
                return;
            }

            if (!username.isEmpty() && !password.isEmpty()) {
                try {
                    ContentValues values = new ContentValues();
                    values.put("username", username);
                    values.put("password", password);
                    db.insertOrThrow("students", null, values);
                    resultView.setText("Registration successful!");
                } catch (Exception e) {
                    resultView.setText("User already exists!");
                }
            } else {
                resultView.setText("Please fill all fields.");
            }
        });

        // Login
        loginButton.setOnClickListener(view -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();
            Cursor cursor = db.rawQuery("SELECT * FROM students WHERE username=? AND password=?", new String[]{username, password});

            if (cursor.moveToFirst()) {
                resultView.setText("Login successful!");
            } else {
                resultView.setText("Invalid username or password.");
            }
            cursor.close();
        });

        // Enroll Subject
        enrollButton.setOnClickListener(view -> {
            String username = usernameInput.getText().toString();
            String selectedSubject = subjectSpinner.getSelectedItem().toString();
            int credits = getCreditsFromSubject(selectedSubject);

            Cursor cursor = db.rawQuery("SELECT SUM(credits) FROM enrollment WHERE username=?", new String[]{username});
            int totalCredits = 0;
            if (cursor.moveToFirst()) {
                totalCredits = cursor.getInt(0);
            }
            cursor.close();

            if (totalCredits + credits > MAX_CREDITS) {
                resultView.setText("Cannot enroll. Max credits exceeded!");
            } else {
                ContentValues values = new ContentValues();
                values.put("username", username);
                values.put("subject", selectedSubject);
                values.put("credits", credits);
                db.insert("enrollment", null, values);
                resultView.setText("Enrolled in: " + selectedSubject);
            }
        });

        // Enrollment Summary
        summaryButton.setOnClickListener(view -> {
            String username = usernameInput.getText().toString();
            Cursor cursor = db.rawQuery("SELECT subject, credits FROM enrollment WHERE username=?", new String[]{username});
            StringBuilder summary = new StringBuilder("Enrolled Subjects:\n");
            int totalCredits = 0;

            while (cursor.moveToNext()) {
                summary.append(cursor.getString(0)).append("\n");
                totalCredits += cursor.getInt(1);
            }
            cursor.close();

            if (totalCredits < MIN_CREDITS) {
                resultView.setText("You must enroll in at least " + MIN_CREDITS + " credits! Current: " + totalCredits);
            } else {
                summary.append("Total Credits: ").append(totalCredits);
                resultView.setText(summary.toString());
            }
        });
    }

    private int getCreditsFromSubject(String subject) {
        return Integer.parseInt(subject.replaceAll("[^0-9]", "").trim());
    }

    private boolean isValidUsername(String username) {
        return username.matches("^[a-z0-9]{8,12}$");
    }
}