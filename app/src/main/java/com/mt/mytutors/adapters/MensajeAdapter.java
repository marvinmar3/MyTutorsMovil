package com.mt.mytutors.adapters;

import android.view.LayoutInflater; //para inflar el diseño de los elementos de la lista
import android.view.View; //para manejar las vistas
import android.view.ViewGroup; //para manejar los grupos de vistas
import android.widget.TextView; //para manejar los elementos de texto

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mt.mytutors.R; //importar recursos de la aplicación
import com.mt.mytutors.models.Mensaje;

import java.text.ParseException; //para manejar excepciones de parseo
import java.text.SimpleDateFormat; //para formatear fechas
import java.util.Date;
import java.util.List;
import java.util.Locale; //para manejar configuraciones regionales
public class MensajeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int TIPO_MENSAJE_ENVIADO = 1;
    private static final int TIPO_MENSAJE_RECIBIDO = 2;

    private List<Mensaje> mensajes;
    private String usuarioActualId;

    public MensajeAdapter(List<Mensaje> mensajes, String usuarioActualId) {
        this.mensajes = mensajes;
        this.usuarioActualId = usuarioActualId;
    }

    @Override
    public int getItemViewType(int position) {
        Mensaje mensaje = mensajes.get(position);
        if (mensaje.getIdEmisor() != null && mensaje.getIdEmisor().equals((usuarioActualId))) {
            return TIPO_MENSAJE_ENVIADO;
        }
        return TIPO_MENSAJE_RECIBIDO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TIPO_MENSAJE_ENVIADO) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mensaje_enviado, parent, false);
            return new MensajeEnviadoViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mensaje_recibido, parent, false);
            return new MensajeRecibidoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mensaje mensaje = mensajes.get(position);
        if (holder instanceof MensajeEnviadoViewHolder) {
            ((MensajeEnviadoViewHolder) holder).bind(mensaje);
        } else {
            ((MensajeRecibidoViewHolder) holder).bind(mensaje);
        }
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    private String formatearHora(String fechaStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(fechaStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return "";
        }
    }

    class MensajeEnviadoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvContenido, tvHora;

        public MensajeEnviadoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContenido = itemView.findViewById(R.id.tvContenido);
            tvHora = itemView.findViewById(R.id.tvHora);
        }

        public void bind(Mensaje mensaje) {
            tvContenido.setText(mensaje.getContenido());
            tvHora.setText(formatearHora(mensaje.getFechaEnvio()));
        }
    }

    class MensajeRecibidoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvContenido, tvHora;

        public MensajeRecibidoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContenido = itemView.findViewById(R.id.tvContenido);
            tvHora = itemView.findViewById(R.id.tvHora);
        }

        public void bind(Mensaje mensaje) {
            tvContenido.setText(mensaje.getContenido());
            tvHora.setText(formatearHora(mensaje.getFechaEnvio()));
        }
    }
}
