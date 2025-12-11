package com.mt.mytutors.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mt.mytutors.R;
import com.mt.mytutors.models.Tema;

import java.util.List;

/**
 * Adaptador para mostrar la lista de temas en un RecyclerView
 */
public class TemaAdapter extends RecyclerView.Adapter<TemaAdapter.TemaViewHolder> {

    private List<Tema> temasList;
    private OnTemaClickListener listener;

    public interface OnTemaClickListener {
        void onTemaClick(Tema tema);
    }

    public TemaAdapter(List<Tema> temasList, OnTemaClickListener listener) {
        this.temasList = temasList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tema, parent, false);
        return new TemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemaViewHolder holder, int position) {
        Tema tema = temasList.get(position);
        holder.bind(tema);
    }

    @Override
    public int getItemCount() {
        return temasList.size();
    }

    class TemaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTipo, tvNombre, tvDescripcion, tvMateria, tvCreador;

        public TemaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvMateria = itemView.findViewById(R.id.tvMateria);
            tvCreador = itemView.findViewById(R.id.tvCreador);
        }

        public void bind(Tema tema) {
            // Nombre y descripción
            tvNombre.setText(tema.getNombre());
            tvDescripcion.setText(tema.getDescripcion());

            // Tipo (Oferta/Demanda)
            if ("tutor".equalsIgnoreCase(tema.getRol())) {
                tvTipo.setText("OFERTA");
                tvTipo.setBackgroundResource(R.drawable.badge_oferta);
            } else {
                tvTipo.setText("DEMANDA");
                tvTipo.setBackgroundResource(R.drawable.badge_demanda);
            }

            // Materia
            if (tema.getNombreMateria() != null) {
                tvMateria.setText(tema.getNombreMateria());
            } else {
                tvMateria.setText("Sin materia");
            }

            // Creador
            if (tema.getNombreCreador() != null) {
                tvCreador.setText(tema.getNombreCreador());
            } else {
                tvCreador.setText("Anónimo");
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTemaClick(tema);
                }
            });
        }
    }

    /**
     * Actualiza la lista de temas
     */
    public void updateList(List<Tema> newList) {
        this.temasList = newList;
        notifyDataSetChanged();
    }
}
