package com.example.controlenotas;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.MenuPopupWindow;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ControleNotasAddActivity extends AppCompatActivity {

    private static final String TAG = "AddNotas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_add_notas);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Spinner nomeMateriaSp = findViewById(R.id.spnomeMateria);
        TextInputEditText notaCredET = findViewById(R.id.notaCredET);
        TextInputEditText notaTrabET = findViewById(R.id.notaTrabET);
        TextInputEditText notaListaET = findViewById(R.id.notaListaET);
        TextInputEditText notaProvaET = findViewById(R.id.notaProvaET);

        MaterialButton addNotas = findViewById(R.id.addNota);

        carregarMaterias(nomeMateriaSp);

        addNotas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String notaPreciso;

                String nomeMateria = nomeMateriaSp.getSelectedItem().toString().trim();
                // Verificar se a matéria é a primeira ("Selecione uma matéria")
                if (nomeMateria.equals("Selecione uma matéria")) {
                    Toast.makeText(ControleNotasAddActivity.this, "Por favor, selecione uma matéria válida!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String notaCred = Objects.requireNonNull(notaCredET.getText()).toString().trim();
                String notaTrab = Objects.requireNonNull(notaTrabET.getText()).toString().trim();
                String notaList = Objects.requireNonNull(notaListaET.getText()).toString().trim();
                String notaPro = Objects.requireNonNull(notaProvaET.getText()).toString().trim();

                if (nomeMateria.isEmpty() || notaCred.isEmpty() || notaTrab.isEmpty() || notaList.isEmpty() || notaPro.isEmpty()) {
                    Toast.makeText(ControleNotasAddActivity.this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    float ntPreciso, ntCred, ntTrab, ntList;

                    ntCred = Float.parseFloat(notaCred);
                    ntTrab = Float.parseFloat(notaTrab);
                    ntList = Float.parseFloat(notaList);

                    float ntSoma = ntCred + ntTrab + ntList;

                    if (ntSoma < 6) {
                        ntPreciso = 6 - ntSoma;
                        notaPreciso = String.valueOf(ntPreciso);
                    } else {
                        notaPreciso = "Não precisa de pontos para passar!!";
                        Toast.makeText(ControleNotasAddActivity.this, "Não está de recuperação!!", Toast.LENGTH_SHORT).show();
                    }

                }

                Map<String, Object> notas = new HashMap<>();
                notas.put("nomeMateria", nomeMateria);
                notas.put("cred", Objects.requireNonNull(notaCredET.getText()).toString());
                notas.put("trab", Objects.requireNonNull(notaTrabET.getText()).toString());
                notas.put("list", Objects.requireNonNull(notaListaET.getText()).toString());
                notas.put("pre", Objects.requireNonNull(notaPreciso));
                notas.put("prova", Objects.requireNonNull(notaProvaET.getText()).toString());

                db.collection("notas").add(notas).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(ControleNotasAddActivity.this, "Notas adicionadas com sucesso!!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ControleNotasAddActivity.this, "Falha Ao Tentar Adicionar Matéria: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Erro ao adicionar matéria", e);
                    }
                });
            }
        });

    }

    private void carregarMaterias(Spinner spinner) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("materias").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Lista que incluirá o texto neutro no início
                ArrayList<String> listaMaterias = new ArrayList<>();
                listaMaterias.add("Selecione uma matéria");  // Adiciona o texto neutro

                // Adiciona as matérias reais da coleção
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nomeMateria = document.getString("nomeMateria");
                    if (nomeMateria != null) {
                        listaMaterias.add(nomeMateria);
                    }
                }

                // Criar um ArrayAdapter customizado
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listaMaterias) {
                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView textView = (TextView) view;
                        // Modificar a cor do texto no dropdown
                        textView.setTextColor(Color.BLACK);  // Cor do texto no dropdown
                        // Modificar a cor de fundo do dropdown
                        view.setBackgroundColor(Color.WHITE); // Cor de fundo no dropdown
                        return view;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView textView = (TextView) view;
                        // Modificar a cor do texto no item selecionado
                        textView.setTextColor(Color.BLACK);  // Cor do texto no item selecionado
                        return view;
                    }
                };

                // Definir o layout do dropdown
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

            } else {
                Toast.makeText(this, "Erro ao carregar matérias: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}