package com.example.a1212072_1212011_encs5150;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReservationDialog {

    private Dialog dialog;
    private Context context;
    private TripModel trip;
    private String userEmail;
    private DatabaseHelper dbHelper;
    private int quantity = 1;

    public ReservationDialog(Context context, TripModel trip,
                             String userEmail, DatabaseHelper dbHelper) {
        this.context   = context;
        this.trip      = trip;
        this.userEmail = userEmail;
        this.dbHelper  = dbHelper;
    }

    public void show() {
        dialog = new Dialog(context);
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_reservation, null);
        dialog.setContentView(view);
        dialog.getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent);

        TextView tvTripName   = view.findViewById(R.id.tvTripName);
        TextView tvQuantity   = view.findViewById(R.id.tvQuantity);
        TextView tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        TextView btnMinus     = view.findViewById(R.id.btnMinus);
        TextView btnPlus      = view.findViewById(R.id.btnPlus);
        Button btnConfirm     = view.findViewById(R.id.btnConfirm);
        Button btnCancel      = view.findViewById(R.id.btnCancel);
        Spinner spinnerType   = view.findViewById(R.id.spinnerReservationType);

        tvTripName.setText(trip.getDestination() + " · " + trip.getCountry());

        String[] types = {"Standard", "Premium", "VIP"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        updatePrice(tvTotalPrice, tvQuantity);

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updatePrice(tvTotalPrice, tvQuantity);
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (quantity < 20) {
                quantity++;
                updatePrice(tvTotalPrice, tvQuantity);
            }
        });

        btnConfirm.setOnClickListener(v -> {
            String type  = spinnerType.getSelectedItem().toString();
            String date  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date());
            double total = trip.getPrice() * quantity;

            boolean success = dbHelper.insertReservation(
                    userEmail, trip.getId(), trip.getDestination(),
                    quantity, type, date, total, "Confirmed");

            if (success) {
                Toast.makeText(context,
                        "Reservation confirmed for " + trip.getDestination() + "!",
                        Toast.LENGTH_LONG).show();
                dialog.dismiss();
            } else {
                Toast.makeText(context,
                        "Failed. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updatePrice(TextView tvTotalPrice, TextView tvQuantity) {
        tvQuantity.setText(String.valueOf(quantity));
        double total = trip.getPrice() * quantity;
        tvTotalPrice.setText("$" + String.format("%.0f", total));
    }
}
