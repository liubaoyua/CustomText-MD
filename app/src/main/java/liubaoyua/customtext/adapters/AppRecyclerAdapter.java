package liubaoyua.customtext.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import liubaoyua.customtext.R;
import liubaoyua.customtext.entity.AppInfo;
import liubaoyua.customtext.utils.AppListFilter;
import liubaoyua.customtext.utils.Common;
import liubaoyua.customtext.utils.IconLoader;
import liubaoyua.customtext.utils.PicassoTools;

/**
 * Created by liubaoyua on 2015/6/20 0020.
 */
public class AppRecyclerAdapter extends RecyclerView.Adapter<AppRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<AppInfo> appList;
    private AppListFilter filter;
    private OnItemClickListener listener;
    private Drawable icon;
    private Drawable errorIcon;


    public AppRecyclerAdapter(Context mContext, List<AppInfo> appList) {
        this.mContext = mContext;
        this.appList = appList;
        filter = new AppListFilter(this, appList);
        icon = mContext.getResources().getDrawable(R.mipmap.ic_default);
        errorIcon = mContext.getResources().getDrawable(R.mipmap.ic_launcher);
    }

    public AppListFilter getFilter() {
        return filter;
    }

    @Override
    public AppRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_card_app, parent, false);
        return new ViewHolder(view, listener);
    }


    @Override
    public void onBindViewHolder(final AppRecyclerAdapter.ViewHolder holder, int position) {
        final AppInfo temp = appList.get(position);
        holder.appNameView.setText(temp.appName);
        holder.packageNameView.setText(temp.packageName);
        if (temp.state == AppInfo.ENABLED) {
            holder.appNameView.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
            holder.packageNameView.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
        } else {
            holder.appNameView.setTextColor(mContext.getResources().getColor(android.R.color.black));
            holder.packageNameView.setTextColor(mContext.getResources().getColor(android.R.color.black));
        }

        PicassoTools.getInstance()
                .load(IconLoader.SCHEME_PACKAGE_NAME + ":" + temp.packageName)
                .noFade().into(holder);
    }

    @Override
    public int getItemCount() {
        return appList == null ? 0 : appList.size();
    }

    public List<AppInfo> getAppList() {
        return appList;
    }

    public void setAppList(List<AppInfo> appList) {
        this.appList = appList;
    }

    /**
     * 设置监听方法
     *
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 内部接口回调方法
     */
    public interface OnItemClickListener {
        void onItemClick(int position, String packageName);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements Target {
        public final View mView;
        public TextView appNameView;
        public TextView packageNameView;
        public ImageView iconView;

        public ViewHolder(final View view, final OnItemClickListener listener) {
            super(view);
            mView = view;
            appNameView = (TextView) mView.findViewById(R.id.app_name);
            packageNameView = (TextView) mView.findViewById(R.id.package_name);
            iconView = (ImageView) mView.findViewById(R.id.image_icon);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null)
                        listener.onItemClick(getAdapterPosition(), appList.get(getAdapterPosition()).packageName);
                }
            });
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            if (!packageNameView.getText().toString().startsWith(Common.PACKAGE_NAME)) {
                iconView.setImageDrawable(icon);
            } else {
                iconView.setImageDrawable(errorIcon);
            }

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            iconView.setImageBitmap(bitmap);
        }
    }
}


