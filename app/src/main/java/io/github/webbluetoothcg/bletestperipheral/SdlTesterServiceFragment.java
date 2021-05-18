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
    private static final String TAG = SdlTesterServiceFragment.class.getCanonicalName();

    private static final UUID SDL_TESTER_SERVICE_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final UUID MOBILE_REQUEST_CHARACTERISTIC = UUID
            .fromString("00001102-0000-1000-8000-00805f9b34fb");
    private static final UUID MOBILE_RESPONSE_CHARACTERISTIC = UUID
            .fromString("00001103-0000-1000-8000-00805f9b34fb");
    private static final String MOBILE_REQUEST_DESCRIPTOR = "Current binary message to SDL.";
    private static final String MOBILE_RESPONSE_DESCRIPTOR = "Current binary message from SDL.";
    private static final String INITIAL_MESSAGE = "Hello SDL";

    private ServiceFragmentDelegate mDelegate;

    private TextView mMessageToDisplay;
    private EditText mEditTextMessage;
    private Button mSendToSdlButton;

    private final OnClickListener mNotifyButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setMobileRequestMessage(mEditTextMessage.getText().toString());
            mDelegate.sendNotificationToDevices(mMobileRequestCharacteristic);
        }
    };

    // GATT
    private BluetoothGattService mSdlService;
    private BluetoothGattCharacteristic mMobileRequestCharacteristic;
    private BluetoothGattCharacteristic mMobileResponseCharacteristic;

    public SdlTesterServiceFragment() {
        mMobileRequestCharacteristic =
                new BluetoothGattCharacteristic(MOBILE_REQUEST_CHARACTERISTIC,
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        /* No permissions */ 0);

        mMobileRequestCharacteristic.addDescriptor(
                Peripheral.getClientCharacteristicConfigurationDescriptor());

        mMobileRequestCharacteristic.addDescriptor(
                Peripheral.getCharacteristicUserDescriptionDescriptor(MOBILE_REQUEST_DESCRIPTOR));

        mMobileResponseCharacteristic =
                new BluetoothGattCharacteristic(MOBILE_RESPONSE_CHARACTERISTIC,
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

        mMobileResponseCharacteristic.addDescriptor(
                Peripheral.getClientCharacteristicConfigurationDescriptor());

        mMobileResponseCharacteristic.addDescriptor(
                Peripheral.getCharacteristicUserDescriptionDescriptor(MOBILE_RESPONSE_DESCRIPTOR));

        mSdlService = new BluetoothGattService(SDL_TESTER_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mSdlService.addCharacteristic(mMobileRequestCharacteristic);
        mSdlService.addCharacteristic(mMobileResponseCharacteristic);
    }

    // Lifecycle callbacks
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sdl_tester, container, false);

        mSendToSdlButton = (Button) view.findViewById(R.id.send_to_sdl_button);
        mSendToSdlButton.setOnClickListener(mNotifyButtonListener);
        mMessageToDisplay = (TextView) view.findViewById(R.id.display_message_label);
        mEditTextMessage = (EditText) view.findViewById(R.id.message_to_send);
        mEditTextMessage.setText(INITIAL_MESSAGE);
        setMobileRequestMessage(INITIAL_MESSAGE);

        return view;
    }

    private void setMobileRequestMessage(String message) {
        mMobileRequestCharacteristic.setValue(message);
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
    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, byte[] value) {
        if (offset != 0) {
            return BluetoothGatt.GATT_INVALID_OFFSET;
        }
        final String message = new String(value);
        mMobileResponseCharacteristic.setValue(value);
        Log.d(TAG, "Received: " + message + " of length " + value.length);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessageToDisplay.setText(message);
            }
        });

        return BluetoothGatt.GATT_SUCCESS;
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
