package com.example.controlenotas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ControleNotasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controle_notas);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = findViewById(R.id.recycler);

        FloatingActionButton add = findViewById(R.id.addNota);
        add.setOnClickListener(view -> startActivity(new Intent(ControleNotasActivity.this, ControleNotasAddActivity.class)));

        db.collection("notas").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(ControleNotasActivity.this, "Falha ao carregar dados: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<Notas> arrayList = new ArrayList<>();
                assert value != null;
                for (QueryDocumentSnapshot document : value) {
                    Notas notas = document.toObject(Notas.class);
                    notas.setId(document.getId());
                    arrayList.add(notas);
                }

                NotasAdapter adapter = new NotasAdapter(ControleNotasActivity.this, arrayList);
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(new NotasAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(Notas notas) {
                        App.notas = notas;
                        startActivity(new Intent(ControleNotasActivity.this, ControleNotasEditActivity.class));
                    }
                });
            }
        });
    }
}