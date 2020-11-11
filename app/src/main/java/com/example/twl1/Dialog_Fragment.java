package com.example.twl1;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.twl.R;

public class Dialog_Fragment extends DialogFragment {

    private static final String TAG = "Dialog_Fragment";

    private EditText EnterBtName;
    private TextView DialogOk;
    String StrHint ="";
    String [] Hint ={"Enter BtName","Enter StartCharacter"};
    int Hintnum;

    public void Hint(int a){
        StrHint = Hint[a];
        Hintnum =a;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog,container,false);
        EnterBtName = view.findViewById(R.id.EnterBtName);
        DialogOk = view.findViewById(R.id.DialogOk);
        DialogOk.setHint(StrHint);

        DialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Ok Pressed");
                String Input = EnterBtName.getText().toString();
                if(!Input.equals("")){
                    ((MainActivity)getActivity()).preferenceEdit(Input,Hintnum);
                }
                getDialog().dismiss();
            }
        });


        return view;
    }
}
