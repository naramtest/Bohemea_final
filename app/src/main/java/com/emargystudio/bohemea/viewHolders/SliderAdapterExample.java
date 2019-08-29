package com.emargystudio.bohemea.viewHolders;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.emargystudio.bohemea.R;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SliderAdapterExample extends SliderViewAdapter<SliderAdapterExample.SliderAdapterVH> {

    private ArrayList<String> images;

    public SliderAdapterExample( ArrayList<String> images) {
        this.images = images;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, parent,false);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {

        switch (position) {
            case 0:
                if ((images.size()!=0 &&!images.get(0).isEmpty())){
                    Picasso.get().load(images.get(0)).fit().centerCrop().into(viewHolder.imageViewBackground);
                }else {
                    Picasso.get().load("https://www.incimages.com/uploaded_files/image/970x450/getty_509107562_2000133320009280346_351827.jpg").into(viewHolder.imageViewBackground);

                }
                break;
            case 1:
                if ((images.size()!=0 &&!images.get(1).isEmpty())){
                    Picasso.get().load(images.get(1)).fit().centerCrop().into(viewHolder.imageViewBackground);
                }else {
                    Picasso.get().load("https://www.incimages.com/uploaded_files/image/970x450/getty_509107562_2000133320009280346_351827.jpg").into(viewHolder.imageViewBackground);

                }
                break;

            case 2:
                if ((images.size()!=0 &&!images.get(2).isEmpty())){
                    Picasso.get().load(images.get(2)).fit().centerCrop().into(viewHolder.imageViewBackground);
                }else {
                    Picasso.get().load("https://www.incimages.com/uploaded_files/image/970x450/getty_509107562_2000133320009280346_351827.jpg").into(viewHolder.imageViewBackground);

                }
                break;

            case 3:
                if ((images.size()!=0 &&!images.get(3).isEmpty())){
                    Picasso.get().load(images.get(3)).fit().centerCrop().into(viewHolder.imageViewBackground);
                }else {
                    Picasso.get().load("https://www.incimages.com/uploaded_files/image/970x450/getty_509107562_2000133320009280346_351827.jpg").into(viewHolder.imageViewBackground);

                }

                break;
            default:
                break;

        }

    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return images.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;

        SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);

            this.itemView = itemView;
        }
    }
}