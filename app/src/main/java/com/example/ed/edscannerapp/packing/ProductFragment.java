package com.example.ed.edscannerapp.packing;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cunoraz.gifview.library.GifView;
import com.example.ed.edscannerapp.Deferred;
import com.example.ed.edscannerapp.ImageLoader;
import com.example.ed.edscannerapp.R;
import com.example.ed.edscannerapp.entities.Product;

import java.util.Timer;

public class ProductFragment extends Fragment {

    private static final String PRODUCT_NAME = "product_name";
    private static final String PRODUCT_SECTION = "product_section";
    private static final String PRODUCT_WEIGHT = "product_weight";
    private static final String PRODUCT_UNIT = "product_unit";
    private static final String PRODUCT_MANUFACTURER = "product_manufacturer";
    private static final String PRODUCT_IMAGE = "product_image";
    private static final String PRODUCT_PACKING_QUANTITY = "product_packing_quantity";
    private static final String PRODUCT_NEEDED_QUANTITY = "product_needed_quantity";
    private static final String PRODUCT_HAS_WEIGHT = "product_has_weight";

    private ImageView overlayView;
    private GifView successView;
    private ImageView errorView;

    private int animationTime = 900;

    public static ProductFragment newInstance(Product product) {
        ProductFragment fragment = new ProductFragment();
        Bundle args = new Bundle();

        args.putString(PRODUCT_NAME, product.getName());
        args.putString(PRODUCT_SECTION, product.getSection());
        args.putString(PRODUCT_WEIGHT, product.getWeight());
        args.putString(PRODUCT_UNIT, product.getUnit());
        args.putString(PRODUCT_MANUFACTURER, product.getManufacturer());
        args.putString(PRODUCT_IMAGE, product.getImage());
        args.putInt(PRODUCT_PACKING_QUANTITY, product.getPackingQuantity());
        args.putInt(PRODUCT_NEEDED_QUANTITY, product.getNeededQuantity());
        args.putBoolean(PRODUCT_HAS_WEIGHT, product.hasWeight());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_activity_product, container, false);

        Bundle args = getArguments();

        ((TextView) rootView.findViewById(R.id.activity_product_name)).setText(args.getString(PRODUCT_NAME));
        ((TextView) rootView.findViewById(R.id.activity_product_manufacturer)).setText(args.getString(PRODUCT_MANUFACTURER));
        ((TextView) rootView.findViewById(R.id.activity_product_section)).setText(
                getString(R.string.activity_product_packing_section, args.getString(PRODUCT_SECTION))
        );
        ((TextView) rootView.findViewById(R.id.activity_product_amount)).setText(
                getString(R.string.activity_product_packing_amount,
                        args.getString(PRODUCT_WEIGHT),
                        args.getString(PRODUCT_UNIT))
        );

        ((TextView) rootView.findViewById(R.id.activity_product_quantity)).setText(
                getString(R.string.product_packing_quantity,
                        String.valueOf(args.getInt(PRODUCT_PACKING_QUANTITY)),
                        String.valueOf(args.getInt(PRODUCT_NEEDED_QUANTITY))
                )
        );

        ((TextView) rootView.findViewById(R.id.activity_product_has_weight)).setVisibility(args.getBoolean(PRODUCT_HAS_WEIGHT) ? View.VISIBLE : View.GONE);


        overlayView = (ImageView) rootView.findViewById(R.id.activity_product_overlay);
        successView = (GifView) rootView.findViewById(R.id.activity_product_success);
        errorView = (ImageView) rootView.findViewById(R.id.activity_product_error);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.activity_product_image);
        new ImageLoader(imageView).execute(args.getString(PRODUCT_IMAGE));

        return rootView;
    }

    public void showSuccess(Timer timer, Deferred def) {
        overlayView.setVisibility(View.VISIBLE);
        successView.setVisibility(View.VISIBLE);
        successView.play();

        def.addCallback(new Deferred.Callback() {
            @Override
            public void success() {
                overlayView.setVisibility(View.GONE);
                successView.setVisibility(View.GONE);
                successView.pause();
            }
        });

        timer.schedule(def, animationTime);
    }

    public void showError(Timer timer, Deferred def) {
        overlayView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.VISIBLE);

        def.addCallback(new Deferred.Callback() {
            @Override
            public void success() {
                overlayView.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
            }
        });

        timer.schedule(def, animationTime);
    }
}
