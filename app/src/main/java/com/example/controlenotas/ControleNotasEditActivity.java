package com.example.controlenotas;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ControleNotasEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notas);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Spinner nomeMateriaSp = findViewById(R.id.spnomeMateria);
        TextInputEditText notaCredET = findViewById(R.id.notaCredET);
        TextInputEditText notaTrabET = findViewById(R.id.notaTrabET);
        TextInputEditText notaListaET = findViewById(R.id.notaListaET);
        TextInputEditText notaProvaET = findViewById(R.id.notaProvaET);

        TextView notaPrecisoTxt = findViewById(R.id.notaPrecisoTxt);

        MaterialButton save = findViewById(R.id.save);
        MaterialButton delete = findViewById(R.id.delete);

        // Preencher os campos com os valores atuais
        carregarMaterias(nomeMateriaSp); // Preencher o Spinner
        notaCredET.setText(App.notas.getCred());
        notaListaET.setText(App.notas.getList());
        notaTrabET.setText(App.notas.getTrab());
        notaPrecisoTxt.setText(App.notas.getPre());
        notaProvaET.setText(App.notas.getProva());

        Log.d("EditNotas", "nomeMateria: " + App.notas.getNomeMateria());
        Log.d("EditNotas", "notaCred: " + App.notas.getCred());
        Log.d("EditNotas", "notaLista: " + App.notas.getList());
        Log.d("EditNotas", "notaTrab: " + App.notas.getTrab());
        Log.d("EditNotas", "notaPreciso: " + App.notas.getPre());
        Log.d("EditNotas", "notaProva: " + App.notas.getProva());

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(ControleNotasEditActivity.this)
                        .setTitle("Confirmar Exclusão")
                        .setMessage("Tem certeza que deseja deletar essas notas?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                db.collection("notas").document(App.notas.getId()).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(ControleNotasEditActivity.this, "Notas Deletada Com Sucesso!!", Toast.LENGTH_SHORT).show();
                                                Intent resultIntent = new Intent();
                                                setResult(RESULT_OK, resultIntent);
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("EditNotas", "Erro Ao Deletar As Notas!!", e);
                                                Toast.makeText(ControleNotasEditActivity.this, "Erro Ao Deletar As Notas!!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("Não", null)
                        .show();
            }
        });

        save.setOnClickListener(v -> {
            new AlertDialog.Builder(ControleNotasEditActivity.this)
                    .setTitle("Confirmar Alteração")
                    .setMessage("Tem certeza que deseja salvar as alterações?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        String notaPreciso;

                        String nomeMateria = nomeMateriaSp.getSelectedItem().toString().trim(); // Obter a matéria selecionada
                        String notaCred = Objects.requireNonNull(notaCredET.getText()).toString().trim();
                        String notaTrab = Objects.requireNonNull(notaTrabET.getText()).toString().trim();
                        String notaList = Objects.requireNonNull(notaListaET.getText()).toString().trim();
                        String notaPro = Objects.requireNonNull(notaProvaET.getText()).toString().trim();

                        if (nomeMateria.isEmpty() || notaCred.isEmpty() || notaTrab.isEmpty() || notaList.isEmpty() || notaPro.isEmpty()) {
                            Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        float ntCred = Float.parseFloat(notaCred);
                        float ntTrab = Float.parseFloat(notaTrab);
                        float ntList = Float.parseFloat(notaList);
                        float ntSoma = ntCred + ntTrab + ntList;

                        notaPreciso = (ntSoma < 6) ? String.valueOf(6 - ntSoma) : "Não precisa de pontos para passar!!!";

                        Map<String, Object> notas = new HashMap<>();
                        notas.put("nomeMateria", nomeMateria);
                        notas.put("cred", notaCred);
                        notas.put("trab", notaTrab);
                        notas.put("list", notaList);
                        notas.put("pre", notaPreciso);
                        notas.put("prova", notaPro);

                        db.collection("notas").document(App.notas.getId()).update(notas)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Notas Alteradas Com Sucesso!!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("EditNotas", "Erro ao salvar as notas", e);
                                    Toast.makeText(this, "Erro ao salvar as notas", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });

        Log.d("EditNotas", "App.notas.getId(): " + App.notas.getId());

    }

    private void carregarMaterias(Spinner spinner) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("materias").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Lista que incluirá o texto neutro no início
                ArrayList<String> listaMaterias = new ArrayList<>();
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