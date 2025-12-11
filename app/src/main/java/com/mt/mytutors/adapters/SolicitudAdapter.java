package com.mt.mytutors.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mt.mytutors.R;
import com.mt.mytutors.models.SolicitudTutoria;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder> {

    public interface OnSolicitudActionListener {
        void onAceptar(SolicitudTutoria solicitud);
        void onRechazar(SolicitudTutoria solicitud);
    }

    private List<SolicitudTutoria> solicitudes;
    private String currentUserId;
    private boolean esRecibidas;
    private OnSolicitudActionListener listener;

    public SolicitudAdapter(List<SolicitudTutoria> solicitudes, String currentUserId,
                            boolean esRecibidas, OnSolicitudActionListener listener) {
        this.solicitudes = solicitudes;
        this.currentUserId = currentUserId;
        this.esRecibidas = esRecibidas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solicitud, parent, false);
        return new SolicitudViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        SolicitudTutoria solicitud = solicitudes.get(position);
        holder.bind(solicitud);
    }

    @Override
    public int getItemCount() {
        return solicitudes.size();
    }

    public void updateData(List<SolicitudTutoria> newList, boolean esRecibidas) {
        this.solicitudes = newList;
        this.esRecibidas = esRecibidas;
        notifyDataSetChanged();
    }

    class SolicitudViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombre, tvTema, tvEstado, tvFecha;
        private LinearLayout llAcciones;
        private MaterialButton btnAceptar, btnRechazar;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvTema = itemView.findViewById(R.id.tvTema);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            llAcciones = itemView.findViewById(R.id.llAcciones);
            btnAceptar = itemView.findViewById(R.id.btnAceptar);
            btnRechazar = itemView.findViewById(R.id.btnRechazar);
        }

        public void bind(SolicitudTutoria solicitud) {
            tvNombre.setText(solicitud.getNombreSolicitante() != null ?
                    solicitud.getNombreSolicitante() : "Usuario");
            tvTema.setText(solicitud.getNombreTema() != null ?
                    solicitud.getNombreTema() : "Sin tema");

            // Estado
            tvEstado.setText(solicitud.getEstadoTexto());
            if (solicitud.isPendiente()) {
                tvEstado.setBackgroundResource(R.drawable.badge_pendiente);
            } else if (solicitud.isAceptada()) {
                tvEstado.setBackgroundResource(R.drawable.badge_oferta);
            } else {
                tvEstado.setBackgroundResource(R.drawable.badge_demanda);
            }

            // Fecha
            if (solicitud.getFechaSolicitud() != null) {
                tvFecha.setText(formatearFecha(solicitud.getFechaSolicitud()));
            }

            // Mostrar botones solo para solicitudes recibidas pendientes
            if (esRecibidas && solicitud.isPendiente()) {
                llAcciones.setVisibility(View.VISIBLE);
                btnAceptar.setOnClickListener(v -> {
                    if (listener != null) listener.onAceptar(solicitud);
                });
                btnRechazar.setOnClickListener(v -> {
                    if (listener != null) listener.onRechazar(solicitud);
                });
            } else {
                llAcciones.setVisibility(View.GONE);
            }
        }

        private String formatearFecha(String fecha) {
            // Simplificar la fecha para mostrar
            if (fecha.contains("T")) {
                return fecha.split("T")[0];
            }
            return fecha;
        }
    }
}
