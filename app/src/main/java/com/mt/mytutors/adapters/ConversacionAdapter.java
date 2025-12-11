package com.mt.mytutors.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mt.mytutors.R;
import com.mt.mytutors.models.Conversacion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ConversacionAdapter extends RecyclerView.Adapter<ConversacionAdapter.ConversacionViewHolder> {

    private List<Conversacion> conversaciones;
    private String currentUserId;
    private OnConversacionClickListener listener;

    public interface OnConversacionClickListener {
        void onConversacionClick(Conversacion conversacion);
    }

    public ConversacionAdapter(List<Conversacion> conversaciones, String currentUserId,
                               OnConversacionClickListener listener) {
        this.conversaciones = conversaciones;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversacion, parent, false);
        return new ConversacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversacionViewHolder holder, int position) {
        Conversacion conversacion = conversaciones.get(position);
        holder.bind(conversacion);
    }

    @Override
    public int getItemCount() {
        return conversaciones.size();
    }

    class ConversacionViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvNombre, tvUltimoMensaje, tvFecha;
        private View indicadorNoLeido;

        public ConversacionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvUltimoMensaje = itemView.findViewById(R.id.tvUltimoMensaje);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            indicadorNoLeido = itemView.findViewById(R.id.indicadorNoLeido);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onConversacionClick(conversaciones.get(pos));
                }
            });
        }

        public void bind(Conversacion conversacion) {
            // Nombre de la conversación
            tvNombre.setText(conversacion.getNombre() != null ?
                    conversacion.getNombre() : "Conversación");

            // Último mensaje
            String ultimoMensaje = conversacion.getUltimoMensaje();
            if (ultimoMensaje != null && !ultimoMensaje.isEmpty()) {
                tvUltimoMensaje.setText(ultimoMensaje);
                tvUltimoMensaje.setVisibility(View.VISIBLE);
            } else {
                tvUltimoMensaje.setText("Sin mensajes");
                tvUltimoMensaje.setVisibility(View.VISIBLE);
            }

            // Fecha formateada
            String fecha = conversacion.getFechaUltimoMensaje();
            if (fecha != null) {
                tvFecha.setText(formatearFecha(fecha));
                tvFecha.setVisibility(View.VISIBLE);
            } else {
                tvFecha.setVisibility(View.GONE);
            }

            // Indicador de no leído (oculto por defecto)
            if (indicadorNoLeido != null) {
                indicadorNoLeido.setVisibility(View.GONE);
            }
        }

        private String formatearFecha(String fechaStr) {
            try {
                SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date fecha = sdfInput.parse(fechaStr);

                if (fecha == null) return "";

                long diffMs = System.currentTimeMillis() - fecha.getTime();
                long diffDays = TimeUnit.MILLISECONDS.toDays(diffMs);

                if (diffDays == 0) {
                    // Hoy - mostrar hora
                    SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    return sdfHora.format(fecha);
                } else if (diffDays == 1) {
                    return "Ayer";
                } else if (diffDays < 7) {
                    // Esta semana - mostrar día
                    SimpleDateFormat sdfDia = new SimpleDateFormat("EEEE", Locale.getDefault());
                    return sdfDia.format(fecha);
                } else {
                    // Más de una semana - mostrar fecha
                    SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    return sdfFecha.format(fecha);
                }
            } catch (ParseException e) {
                return "";
            }
        }
    }
}