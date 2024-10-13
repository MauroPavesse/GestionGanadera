package com.pequesystems.gestionganadera.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.pequesystems.gestionganadera.R;
import com.pequesystems.gestionganadera.models.Animal;
import com.pequesystems.gestionganadera.ui.AnimalEditActivity;

import java.util.List;

public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.ViewHolder> {

    private List<Animal> animalsList;
    private Context context;

    public AnimalAdapter(List<Animal> animalsList, Context context) {
        this.animalsList = animalsList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_animal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Animal data = animalsList.get(position);
        holder.itemAnimal_textView_name.setText(data.getName());
        holder.itemAnimal_textView_type.setText(data.getType());
        holder.itemAnimal_textView_sex.setText(data.getSex());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AnimalEditActivity.class);
            intent.putExtra("ID", data.getId());
            intent.putExtra("Name", data.getName());
            intent.putExtra("TypeId", data.getTypeId());
            intent.putExtra("Type", data.getType());
            intent.putExtra("Sex", data.getSex());
            intent.putExtra("DeviceId", data.getDeviceId());
            intent.putExtra("Birthdate", data.getBirthdate());
            intent.putExtra("isEditMode", true);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return animalsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemAnimal_textView_name, itemAnimal_textView_type, itemAnimal_textView_sex;

        public ViewHolder(View itemView) {
            super(itemView);
            itemAnimal_textView_name = itemView.findViewById(R.id.itemAnimal_textView_name);
            itemAnimal_textView_type = itemView.findViewById(R.id.itemAnimal_textView_type);
            itemAnimal_textView_sex = itemView.findViewById(R.id.itemAnimal_textView_sex);
        }
    }
}
