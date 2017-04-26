package tech.alvarez.dondeestoy.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import tech.alvarez.dondeestoy.R;
import tech.alvarez.dondeestoy.model.Lugar;


public class LugaresAdapter extends ArrayAdapter<Lugar> {

    public LugaresAdapter(Context context, List<Lugar> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_lugar, parent, false);
        }

        TextView nombreTextView = (TextView) convertView.findViewById(R.id.nombreTextView);
        TextView direccionTextView = (TextView) convertView.findViewById(R.id.direccionTextView);
        TextView probabilidadTextView = (TextView) convertView.findViewById(R.id.probabilidadTextView);
        TextView calificacionTextView = (TextView) convertView.findViewById(R.id.calificacionTextView);

        Lugar u = getItem(position);

        nombreTextView.setText(u.getNombre());
        direccionTextView.setText(u.getDireccion());
        probabilidadTextView.setText(String.valueOf(u.getProbabilidad()));
        if (u.getCalificacion() < 0) {
            calificacionTextView.setText(R.string.ninguna);
        } else {
            calificacionTextView.setText(String.valueOf(u.getCalificacion()));
        }

        GradientDrawable magnitudeCircle = (GradientDrawable) probabilidadTextView.getBackground();
        int magnitudeColor = obtenerColorProbabilidad(u.getProbabilidad());
        magnitudeCircle.setColor(magnitudeColor);

        return convertView;
    }

    private int obtenerColorProbabilidad(float probabilidad) {
        int recursoId = 0;
        if (probabilidad > 0.4) {
            recursoId = R.color.colorProbabilidadAlta;
        } else if (probabilidad > 0.1) {
            recursoId = R.color.colorProbabilidadMedia;
        } else {
            recursoId = R.color.colorProbabilidadBaja;
        }
        return ContextCompat.getColor(getContext(), recursoId);
    }
}
