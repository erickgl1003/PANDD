package com.example.pandd;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.browse.MediaBrowser;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.contract.ActivityResultContract;
import java.lang.Object;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pandd.models.Post;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.SphericalUtil;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;

import im.delight.android.location.SimpleLocation;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    protected Context context;
    protected List<Post> posts;

    private int primaryColor;
    private FusedLocationProviderClient fusedLocationClient;

    private double userLat;
    private double userLong;

    private SimpleLocation location;

    public PostsAdapter(Context context, List<Post> posts, double latitude, double longitude) {
        this.context = context;
        this.posts = posts;
        userLat = latitude;
        userLong = longitude;
        location = new SimpleLocation(context);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> list, double latitude, double longitude) {
        userLat = latitude;
        userLong = longitude;
        posts.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        //Get primary color from theme
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        primaryColor = typedValue.data;



        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post, position);

    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUsername;
        private TextView tvDescription;
        private TextView tvTime;
        private TextView tvStore;
        private TextView tvBarcode;
        private TextView tvProduct;
        private TextView tvDistance;
        private ImageView ivImage;
        private ImageView ivProfile;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvStore = itemView.findViewById(R.id.tvStore);
            tvBarcode = itemView.findViewById(R.id.tvBarcode);
            tvProduct = itemView.findViewById(R.id.tvProduct);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }

        public void bind(Post post, int position) {
            tvBarcode.setVisibility(View.GONE);
            ivImage.setVisibility(View.GONE);

            LatLng storell = new LatLng(post.getStore().getDouble("lat"), post.getStore().getDouble("long"));

            Log.i("Adapter",String.valueOf(storell.latitude) + " " + String.valueOf(storell.longitude)  + " " + String.valueOf(userLat)  + " " + String.valueOf(userLong));

            double dist = SimpleLocation.calculateDistance(storell.latitude, storell.longitude, userLat, userLong);

            ParseUser author = post.getUser();
            String username = author.getUsername();
            String description = post.getDescription();
            String store = "at " + post.getStore().getString("name");
            String product = "Product: " + post.getProduct();
            String barcode = "Barcode: " + post.getBarcode();

            Spannable spannableStore = customize(store, 2, store.length());
            tvStore.setText(spannableStore, TextView.BufferType.SPANNABLE);
            tvDescription.setText(description);
            tvUsername.setText(username);
            String units = " m ";
            if(dist >= 1000){
                dist /= 1000;
                units = " km ";
            }
            String result = String.format("%.2f", dist);
            tvDistance.setText(result + units +"away from you");

            if(post.getProduct() != null) {
                Spannable spannableProduct = customize(product, 8, product.length());
                tvProduct.setText(spannableProduct, TextView.BufferType.SPANNABLE);
                tvProduct.setVisibility(View.VISIBLE);
            }

            if(post.getBarcode() != null) {
                Spannable spannableBarcode = customize(barcode,8,barcode.length());
                tvBarcode.setText(spannableBarcode, TextView.BufferType.SPANNABLE);
                tvBarcode.setVisibility(View.VISIBLE);
            }


            Date createdAt = post.getCreatedAt();
            String timeAgo = Post.calculateTimeAgo(createdAt);

            tvTime.setText(timeAgo);

            ParseFile profile = (ParseFile) author.getParseFile("profile");
            if (profile != null) {
                Glide.with(context).load(profile.getUrl()).apply(RequestOptions.circleCropTransform()).into(ivProfile);
            }

            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
                ivImage.setVisibility(View.VISIBLE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });


        }
    }



    private Spannable customize(String text, int start, int end){
        Spannable spannableText = new SpannableString(text);
        spannableText.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new ForegroundColorSpan(primaryColor),start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableText;
    }

}