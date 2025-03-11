package com.example.gimnasio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gimnasio.db.FirebaseUsuarios;
import com.example.gimnasio.db.Usuarios;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class LogIn extends AppCompatActivity {

    private Button loginButton, signUpButton;
    private EditText editTextEmail, editTextPassword;
    private FirebaseUsuarios firebaseUsuarios;
    private FirebaseAuth mAuth;
    public static Usuarios usr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();
        firebaseUsuarios = new FirebaseUsuarios(this);

        editTextEmail = findViewById(R.id.usernameEditText);
        editTextPassword = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LogIn.this, SingUp.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LogIn.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("LoginDebug", "Autenticación exitosa con Firebase Auth");
                        firebaseUsuarios.getRegistroCuenta(email, new FirebaseUsuarios.OnUsuariosFoundListener() {
                            @Override
                            public void onUsuariosFound(List<Usuarios> usuarios) {
                                Log.d("LoginDebug", "Usuario encontrado en Realtime Database");
                                usr = usuarios.get(0);
                                Intent intent;
                                if ("Admin".equals(usr.getTipo())) {
                                    intent = new Intent(LogIn.this, MainActivity.class);
                                    Log.d("LoginDebug", "Navegando a MainActivity (Admin)");
                                } else {
                                    intent = new Intent(LogIn.this, Usuario.class);
                                    Log.d("LoginDebug", "Navegando a Usuario");
                                }
                                Toast.makeText(LogIn.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onUsuariosNotFound() {
                                Log.d("LoginDebug", "Usuario no encontrado en Realtime Database");
                                Toast.makeText(LogIn.this, "Usuario no encontrado en la base de datos", Toast.LENGTH_LONG).show();
                                editTextEmail.setText("");
                                editTextPassword.setText("");
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("LoginDebug", "Error al buscar usuario: " + e.getMessage());
                                Toast.makeText(LogIn.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                editTextEmail.setText("");
                                editTextPassword.setText("");
                            }
                        });
                    } else {
                        Log.e("LoginDebug", "Error de autenticación: " + task.getException().getMessage());
                        Toast.makeText(LogIn.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        editTextEmail.setText("");
                        editTextPassword.setText("");
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("LoginDebug", "Usuario ya autenticado: " + currentUser.getEmail());
            firebaseUsuarios.getRegistroCuenta(currentUser.getEmail(), new FirebaseUsuarios.OnUsuariosFoundListener() {
                @Override
                public void onUsuariosFound(List<Usuarios> usuarios) {
                    usr = usuarios.get(0);
                    Intent intent;
                    if ("Admin".equals(usr.getTipo())) {
                        intent = new Intent(LogIn.this, MainActivity.class);
                        Log.d("LoginDebug", "Redirigiendo a MainActivity desde onStart (Admin)");
                    } else {
                        intent = new Intent(LogIn.this, Usuario.class);
                        Log.d("LoginDebug", "Redirigiendo a Usuario desde onStart");
                    }
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onUsuariosNotFound() {
                    Log.d("LoginDebug", "Usuario autenticado pero no encontrado en DB");
                }

                @Override
                public void onError(Exception e) {
                    Log.e("LoginDebug", "Error al verificar usuario logueado: " + e.getMessage());
                }
            });
        } else {
            Log.d("LoginDebug", "No hay usuario autenticado al iniciar");
        }
    }
}