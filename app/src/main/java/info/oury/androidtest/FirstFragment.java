package info.oury.androidtest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FirstFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.garage_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BleManager bleManager = BleManager.getInstance();
                if (bleManager.getScanBleResult() != null && bleManager.getScanBleResult().isGarageConnected()) {
                    bleManager.sendGarageCommand();
                }
            }
        });
    }


}