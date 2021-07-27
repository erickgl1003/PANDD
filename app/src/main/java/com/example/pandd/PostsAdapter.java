package com.example.pandd;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pandd.models.Post;
import com.example.pandd.models.Store;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.pedromassango.doubleclick.DoubleClick;
import com.pedromassango.doubleclick.DoubleClickListener;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import im.delight.android.location.SimpleLocation;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    protected Context context;
    protected List<Post> posts;

    private int primaryColor;

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

        //Get primary color from theme
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        primaryColor = typedValue.data;

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Bind every post to the recyclerView
        Post post = posts.get(position);
        holder.bind(post);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUsername;
        private TextView tvDescription;
        private TextView tvTime;
        private TextView tvStore;
        private TextView tvBarcode;
        private TextView tvProduct;
        private TextView tvExpiring;
        private TextView tvExpired;
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
            tvExpiring = itemView.findViewById(R.id.tvExpiring);
            tvExpired = itemView.findViewById(R.id.tvExpired);
        }

        public void bind(Post post) {
            //Sets both optional values as GONE, so they don't take space in the post.
            tvBarcode.setVisibility(View.GONE);
            ivImage.setVisibility(View.GONE);
            tvExpiring.setVisibility(View.GONE);
            tvExpired.setVisibility(View.GONE);

            Store storeObj = (Store) post.getStore();

            //Get all the info for the post
            ParseUser author = post.getUser();
            String username = author.getUsername();
            String description = post.getDescription();
            String store = "at " + storeObj.getName();

            //Set the store, description and username text
            Spannable spannableStore = customizeRed(store, 2, store.length());
            tvStore.setText(spannableStore, TextView.BufferType.SPANNABLE);
            tvDescription.setText(description);
            tvUsername.setText(username);

            //Calculate distance between user and post's store
            String result = calculateDistance(post);
            tvDistance.setText(result + "away from you");

            //Set the product and barcode (if they aren't null)
            String product = post.getProduct();
            product = product.substring(0,1).toUpperCase() + product.substring(1);
            tvProduct = setTextView(product,tvProduct,"Product: ");
            tvBarcode = setTextView(post.getBarcode(),tvBarcode,"Barcode: ");

            //Set post's expiring date
            Date expiring = post.getExpiring();
            if(expiring != null){
                Calendar cal = Calendar.getInstance();
                cal.setTime(expiring);
                tvExpiring = setTextView(formatDate(cal),tvExpiring,"Expires: ");

                Calendar today = Calendar.getInstance();
                if(cal.compareTo(today) < 1)
                    tvExpired.setVisibility(View.VISIBLE);
            }


            //Set post's timeAgo stamp
            Date createdAt = post.getCreatedAt();
            String timeAgo = Post.calculateTimeAgo(createdAt);
            tvTime.setText(timeAgo);

            //Set user's profile picture
            ParseFile profile = (ParseFile) author.getParseFile("profile");
            if (profile != null) {
                Glide.with(context).load(profile.getUrl()).apply(RequestOptions.circleCropTransform()).into(ivProfile);
            }

            //Sets post's image (if it has any)
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
                ivImage.setVisibility(View.VISIBLE);
            }

            //Set listener on the store field 1 click for store info, 2 for searching posts by store
            tvStore.setOnClickListener( new DoubleClick(new DoubleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Intent i = new Intent(context,StoreInfo.class);
                    i.putExtra("name",storeObj.getName());
                    i.putExtra("address",storeObj.getAddress());
                    i.putExtra("mapId",storeObj.getMapId());
                    String coordinates = getCoordinatesText(storeObj.getLat(),storeObj.getLong());
                    i.putExtra("coordinates",coordinates);
                    context.startActivity(i);
                }
                @Override
                public void onDoubleClick(View view) {
                    Intent intent = new Intent(context, SearchActivity.class);
                    intent.putExtra("field","store");
                    intent.putExtra("value",post.getStore().getObjectId());
                    context.startActivity(intent);
                }
            }));
        }
    }

    private String formatDate(Calendar cal) {
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();

        return dateFormatSymbols.getMonths()[cal.get(Calendar.MONTH)] + " "  + cal.get(Calendar.DAY_OF_MONTH)+ ", " + cal.get(Calendar.YEAR);
    }

    private String getCoordinatesText(Double lat, Double longitude) {
        return String.format("%.3f", lat) + ", " + String.format("%.3f", longitude);
    }

    private TextView setTextView(String postText, TextView textView, String preText) {
        if(postText != null) {
            String textField = preText + postText;
            Spannable spannableProduct = customizeBlack(textField, 8, textField.length());
            textView.setText(spannableProduct, TextView.BufferType.SPANNABLE);
            textView.setVisibility(View.VISIBLE);
        }
        return textView;
    }

    private String calculateDistance(Post post) {
        LatLng storell = new LatLng(post.getStore().getDouble("lat"), post.getStore().getDouble("long"));

        double dist = SimpleLocation.calculateDistance(storell.latitude, storell.longitude, userLat, userLong);

        String units = " m ";
        if(dist >= 1000){
            dist /= 1000;
            units = " km ";
        }
        return String.format("%.2f", dist) + units;
    }


    private Spannable customizeBlack(String text, int start, int end){
        Spannable spannableText = new SpannableString(text);
        spannableText.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new ForegroundColorSpan(context.getResources().getColor(android.R.color.black)),start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableText;
    }

    private Spannable customizeRed(String text, int start, int end){
        Spannable spannableText = new SpannableString(text);
        spannableText.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new ForegroundColorSpan(primaryColor),start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableText;
    }

}