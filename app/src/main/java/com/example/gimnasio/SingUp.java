package com.example.gimnasio;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.gimnasio.db.FirebaseUsuarios;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.UUID;

public class SingUp extends AppCompatActivity {

    private EditText editTextPassword, editTextId, editTextNombre, editTextEmail, editTextDate;
    private Spinner spinnerGenero, spinnerTipo;
    private ImageView imageViewFoto;
    private ImageButton imageButtonCalendario;
    private Button btnFoto, loginButton, signUpButton;
    private FirebaseUsuarios firebaseUsuarios;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        // Inicializa FirebaseUsuarios
        firebaseUsuarios = new FirebaseUsuarios(this);

        // Vincula los elementos de la UI
        editTextId = findViewById(R.id.idUsuario);
        editTextPassword = findViewById(R.id.passwordUsuario);
        editTextNombre = findViewById(R.id.nombreUsuario);
        editTextEmail = findViewById(R.id.emailUsuario);
        editTextDate = findViewById(R.id.fechaUsuario);
        imageViewFoto = findViewById(R.id.imagenUsuario);
        imageButtonCalendario = findViewById(R.id.fechaSelectUsuario);
        btnFoto = findViewById(R.id.buttonSelectImageUsuario);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        // Configura los Spinners
        ArrayAdapter<CharSequence> generoAdapter = ArrayAdapter.createFromResource(this, R.array.sx, android.R.layout.simple_spinner_item);
        spinnerGenero = findViewById(R.id.spinnerGeneroUsuario);
        spinnerGenero.setAdapter(generoAdapter);
        spinnerGenero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    String genero = parent.getItemAtPosition(position).toString();
                    spinnerGenero.setTag(genero);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<CharSequence> tipoAdapter = ArrayAdapter.createFromResource(this, R.array.tipo, android.R.layout.simple_spinner_item);
        spinnerTipo = findViewById(R.id.spinnerTipoUsuario);
        spinnerTipo.setAdapter(tipoAdapter);
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    String tipo = parent.getItemAtPosition(position).toString();
                    spinnerTipo.setTag(tipo);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Botón para capturar foto
        btnFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
            } else {
                captureImage();
            }
        });

        // Botón para seleccionar fecha
        imageButtonCalendario.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String fechaSeleccionada = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        editTextDate.setText(fechaSeleccionada);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Botón de registrarse
        signUpButton.setOnClickListener(v -> registerUser());

        // Botón de ir al login
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(SingUp.this, LogIn.class);
            startActivity(intent);
        });
    }

    /*private void registerUser() {
        String idText = editTextId.getText().toString().trim();
        String nombre = editTextNombre.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fechaIngreso = editTextDate.getText().toString().trim();
        String genero = spinnerGenero.getTag() != null ? spinnerGenero.getTag().toString() : "";
        String tipo = spinnerTipo.getTag() != null ? spinnerTipo.getTag().toString() : "";

        if (idText.isEmpty() || nombre.isEmpty() || email.isEmpty() || password.isEmpty() || fechaIngreso.isEmpty() || genero.isEmpty() || tipo.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int id = Integer.parseInt(idText);
        Bitmap bitmap = ((BitmapDrawable) imageViewFoto.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Subir imagen a Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String imagePath = "images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(imagePath);

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();

                // Registrar usuario en Firebase Authentication y Realtime Database
                firebaseUsuarios.addRegistroCuenta(id, nombre, email, fechaIngreso, password, imageUrl, genero, tipo,
                        new FirebaseUsuarios.OnRegistroCompleteListener() {
                            @Override
                            public void onRegistroComplete(boolean success) {
                                Toast.makeText(SingUp.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SingUp.this, LogIn.class);
                                startActivity(intent);
                                finish(); // Cierra la actividad de registro
                            }

                            @Override
                            public void onRegistroError(Exception e) {
                                Toast.makeText(SingUp.this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            });
        }).addOnFailureListener(exception -> {
            Toast.makeText(SingUp.this, "Error al subir la imagen: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }*/


    private void registerUser() {
        String idText = editTextId.getText().toString().trim();
        String nombre = editTextNombre.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fechaIngreso = editTextDate.getText().toString().trim();
        String genero = spinnerGenero.getTag() != null ? spinnerGenero.getTag().toString() : "";
        String tipo = spinnerTipo.getTag() != null ? spinnerTipo.getTag().toString() : "";

        if (idText.isEmpty() || nombre.isEmpty() || email.isEmpty() || password.isEmpty() || fechaIngreso.isEmpty() || genero.isEmpty() || tipo.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int id = Integer.parseInt(idText);

        // Verificar si el ImageView tiene una imagen válida
        if (imageViewFoto.getDrawable() == null || !(imageViewFoto.getDrawable() instanceof BitmapDrawable)) {
            // Si no hay imagen, registrar sin imagen (o puedes mostrar un mensaje al usuario)
            Toast.makeText(this, "No se seleccionó una imagen. Registrando sin imagen...", Toast.LENGTH_SHORT).show();
            firebaseUsuarios.addRegistroCuenta(id, nombre, email, fechaIngreso, password, "", genero, tipo,
                    new FirebaseUsuarios.OnRegistroCompleteListener() {
                        @Override
                        public void onRegistroComplete(boolean success) {
                            Toast.makeText(SingUp.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SingUp.this, LogIn.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onRegistroError(Exception e) {
                            Toast.makeText(SingUp.this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
            return;
        }

        // Si hay imagen, proceder con la subida
        Bitmap bitmap = ((BitmapDrawable) imageViewFoto.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Subir imagen a Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String imagePath = "images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(imagePath);

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();

                // Registrar usuario en Firebase Authentication y Realtime Database
                firebaseUsuarios.addRegistroCuenta(id, nombre, email, fechaIngreso, password, imageUrl, genero, tipo,
                        new FirebaseUsuarios.OnRegistroCompleteListener() {
                            @Override
                            public void onRegistroComplete(boolean success) {
                                Toast.makeText(SingUp.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SingUp.this, LogIn.class);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onRegistroError(Exception e) {
                                Toast.makeText(SingUp.this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }).addOnFailureListener(exception -> {
                Toast.makeText(SingUp.this, "Error al obtener URL de la imagen: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            });
        }).addOnFailureListener(exception -> {
            Toast.makeText(SingUp.this, "Error al subir la imagen: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        });
    }










    @SuppressLint("QueryPermissionsNeeded")
    private void captureImage() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageViewFoto.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}