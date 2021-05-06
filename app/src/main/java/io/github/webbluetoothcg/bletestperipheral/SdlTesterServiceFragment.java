package io.github.webbluetoothcg.bletestperipheral;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.util.Arrays;
import java.util.UUID;

public class SdlTesterServiceFragment extends ServiceFragment {
    private static final UUID SDL_TESTER_SERVICE_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String SDL_TESTER_DESCRIPTION = "Current binary outgoing message.";

    private ServiceFragmentDelegate mDelegate;

    private TextView mMessageToDisplay;
    private Button mSendToSdlButton;

    private final OnClickListener mNotifyButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mDelegate.sendNotificationToDevices(mSdlServiceCharacteristic);
        }
    };

    // GATT
    private BluetoothGattService mSdlService;
    private BluetoothGattCharacteristic mSdlServiceCharacteristic;

    public SdlTesterServiceFragment() {
        mSdlServiceCharacteristic =
                new BluetoothGattCharacteristic(SDL_TESTER_SERVICE_UUID,
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_READ);

        mSdlServiceCharacteristic.addDescriptor(
                Peripheral.getClientCharacteristicConfigurationDescriptor());

        mSdlServiceCharacteristic.addDescriptor(
                Peripheral.getCharacteristicUserDescriptionDescriptor(SDL_TESTER_DESCRIPTION));

        mSdlService = new BluetoothGattService(SDL_TESTER_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mSdlService.addCharacteristic(mSdlServiceCharacteristic);
    }

    // Lifecycle callbacks
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sdl_tester, container, false);

        mSendToSdlButton = (Button) view.findViewById(R.id.send_to_sdl_button);
        mSendToSdlButton.setOnClickListener(mNotifyButtonListener);
        mMessageToDisplay = (TextView) view.findViewById(R.id.display_message_label);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mDelegate = (ServiceFragmentDelegate) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ServiceFragmentDelegate");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    public BluetoothGattService getBluetoothGattService() {
        return mSdlService;
    }

    @Override
    public ParcelUuid getServiceUUID() {
        return new ParcelUuid(SDL_TESTER_SERVICE_UUID);
    }

    @Override
    public void notificationsEnabled(BluetoothGattCharacteristic characteristic, boolean indicate) {
        if (characteristic.getUuid() != SDL_TESTER_SERVICE_UUID) {
            return;
        }
        if (indicate) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), R.string.notificationsEnabled, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    public void notificationsDisabled(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid() != SDL_TESTER_SERVICE_UUID) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), R.string.notificationsNotEnabled, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
