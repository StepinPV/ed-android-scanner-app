package com.example.ed.edscannerapp.packing;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.ed.edscannerapp.entities.Products;


public class ProductsPagerAdapter extends FragmentPagerAdapter {

    private Products products;
    private long baseId = 0;

    public ProductsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setProducts(Products products){
        this.products = products;
    }

    @Override
    public Fragment getItem(int position) {
        return ProductFragment.newInstance(ProductsHelper.getUnscannedByIndex(products, position));
    }

    @Override
    public int getCount() {
        if (products != null) {
            return ProductsHelper.getUnscannedCount(products);
        }
        else {
            return 0;
        }
    }

    //this is called when notifyDataSetChanged() is called
    @Override
    public int getItemPosition(Object object) {
        // refresh all fragments when data set changed
        return ProductsPagerAdapter.POSITION_NONE;
    }


    @Override
    public long getItemId(int position) {
        // give an ID different from position when position has been changed
        return baseId + position;
    }

    /**
     * Notify that the position of a fragment has been changed.
     * Create a new ID for each position to force recreation of the fragment
     * @param n number of items which have been changed
     */
    public void notifyChangeInPosition(int n) {
        // shift the ID returned by getItemId outside the range of all previous fragments
        baseId += products.getList().size() + n;
    }
}
