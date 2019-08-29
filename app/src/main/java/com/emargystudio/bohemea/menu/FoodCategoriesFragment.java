package com.emargystudio.bohemea.menu;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.emargystudio.bohemea.model.FoodCategory;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.viewHolders.FoodCategoryAdapter;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class FoodCategoriesFragment extends Fragment {


    private ArrayList<FoodCategory> foodCategories;
    private FoodCategoryAdapter foodCategoryAdapter;
    private ProgressBar progressBar;

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            // viewHolder.getItemId();
            // viewHolder.getItemViewType();
            FoodCategory foodCategory = foodCategories.get(position);
            Bundle args = new Bundle();
            args.putParcelable(getString(R.string.food_catrgory_model), foodCategory);


            FoodItemFragment foodItemFragment = new FoodItemFragment();
            foodItemFragment.setArguments(args);
            if (getActivity()!=null) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.your_placeholder, foodItemFragment, getString(R.string.foodItemFragment_name));
                ft.addToBackStack(getString(R.string.foodCategoriesFragment_name));
                ft.commit();
            }
        }
    };


    public FoodCategoriesFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_food_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.food_category_rv);
        foodCategories = new ArrayList<>();
        foodCategoryAdapter = new FoodCategoryAdapter(foodCategories,getContext());
        progressBar = view.findViewById(R.id.progress_bar);



        categoryQuery();


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(foodCategoryAdapter);
        foodCategoryAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setHasFixedSize(true);

    }


    private void categoryQuery(){
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.food_category_query,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){
                                JSONArray jsonObjectCategory =  jsonObject.getJSONArray("categorys");
                                for(int i = 0 ; i<jsonObjectCategory.length(); i++){
                                    JSONObject jsonObjectSingleCategory = jsonObjectCategory.getJSONObject(i);
                                    foodCategories.add(new FoodCategory(jsonObjectSingleCategory.getInt("id"),
                                            jsonObjectSingleCategory.getInt("item_count"),
                                            jsonObjectSingleCategory.getString("name"),
                                            jsonObjectSingleCategory.getString("ar_name"),
                                            jsonObjectSingleCategory.getString("image_url")));

                                }

                                foodCategoryAdapter.notifyDataSetChanged();

                            }else{
                                Toast.makeText(getContext(),getString(R.string.internet_off),Toast.LENGTH_LONG).show();
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(),getString(R.string.internet_off),Toast.LENGTH_LONG).show();
                    }
                }
        );

        VolleyHandler.getInstance(getContext()).addRequetToQueue(stringRequest);
    }



}
