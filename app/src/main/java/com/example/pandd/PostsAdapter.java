package com.example.pandd;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pandd.models.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    protected Context context;
    protected List<Post> posts;

    private int primaryColor;


    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> list) {
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
        Post post = posts.get(position);
        holder.bind(post, position);
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvUsername;
        private TextView tvDescription;
        private TextView tvTime;
        private TextView tvStore;
        private TextView tvBarcode;
        private TextView tvProduct;
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
        }
        public void bind(Post post, int position) {
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