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

        // Inicializa Firebase Auth y FirebaseUsuarios
        mAuth = FirebaseAuth.getInstance();
        firebaseUsuarios = new FirebaseUsuarios(this);

        // Vincula los elementos de la UI
        editTextEmail = findViewById(R.id.usernameEditText);
        editTextPassword = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        // Botón de registrarse
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LogIn.this, SingUp.class);
            startActivity(intent);
        });

        // Botón de login
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
        // Primero autenticamos con Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Si la autenticación es exitosa, buscamos datos adicionales en Realtime Database
                        firebaseUsuarios.getRegistroCuenta(email, new FirebaseUsuarios.OnUsuariosFoundListener() {
                            @Override
                            public void onUsuariosFound(List<Usuarios> usuarios) {
                                usr = usuarios.get(0); // Guardamos el usuario encontrado
                                Intent intent;
                                if ("Admin".equals(usr.getTipo())) {
                                    intent = new Intent(LogIn.this, MainActivity.class);
                                } else {
                                    intent = new Intent(LogIn.this, Usuario.class);
                                }
                                startActivity(intent);
                                finish(); // Cierra la actividad de login
                                Toast.makeText(LogIn.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onUsuariosNotFound() {
                                Toast.makeText(LogIn.this, "Usuario no encontrado en la base de datos", Toast.LENGTH_LONG).show();
                                editTextEmail.setText("");
                                editTextPassword.setText("");
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(LogIn.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                editTextEmail.setText("");
                                editTextPassword.setText("");
                            }
                        });
                    } else {
                        Toast.makeText(LogIn.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        editTextEmail.setText("");
                        editTextPassword.setText("");
                    }
                });
    }

    // Opcional: Verifica si el usuario ya está logueado al iniciar
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseUsuarios.getRegistroCuenta(currentUser.getEmail(), new FirebaseUsuarios.OnUsuariosFoundListener() {
                @Override
                public void onUsuariosFound(List<Usuarios> usuarios) {
                    usr = usuarios.get(0);
                    Intent intent;
                    if ("Admin".equals(usr.getTipo())) {
                        intent = new Intent(LogIn.this, MainActivity.class);
                    } else {
                        intent = new Intent(LogIn.this, Usuario.class);
                    }
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onUsuariosNotFound() {
                    // No hacemos nada, dejamos que el usuario inicie sesión manualmente
                }

                @Override
                public void onError(Exception e) {
                    Log.e("AuthError", "Error al verificar usuario logueado", e);
                }
            });
        }
    }
}