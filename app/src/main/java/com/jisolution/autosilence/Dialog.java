package com.jisolution.autosilence;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class Dialog extends AppCompatDialogFragment {

    EditText editTextName;
    EditText editTextRadius;
    DialogListner dialogListner;

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.layout_dialog,null);
        editTextName= view.findViewById(R.id.name);
        editTextRadius=view.findViewById(R.id.radius);
        builder.setView(view)
                .setTitle("Enter Location Details")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String location_name=editTextName.getText().toString();
                        String radius=editTextRadius.getText().toString();
                        try{
                            dialogListner.applytext(location_name,radius);
                        }catch (Exception e){
                            Log.e("Dialog", e.getMessage());
                        }

                    }
                });


        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            dialogListner = (DialogListner) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()+
                    "must Implement DilogListner");
        }
    }
    public interface DialogListner{
        void applytext(String name,String radius);
    }
}
