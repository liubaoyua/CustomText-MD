package liubaoyua.customtext.utils;

import android.widget.Filter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import liubaoyua.customtext.adapters.AppRecyclerAdapter;
import liubaoyua.customtext.entity.AppInfo;

/**
 * Created by liubaoyua on 2015/6/20 0020.
 * this is a filter of appInfo class.
 */
public class AppListFilter extends Filter {
    private AppRecyclerAdapter adapter;
    private List<AppInfo> appList;

    public AppListFilter(AppRecyclerAdapter adapter, List<AppInfo> appList){
        super();
        this.adapter = adapter;
        this.appList = appList;
    }

    public void setAppList(List<AppInfo> newList){
        this.appList = newList;
    }

    public List<AppInfo> getAppList(){
        return this.appList;
    }

    @Override
    protected FilterResults performFiltering(final CharSequence constraint){
        List<AppInfo> items = new ArrayList<>();
        items.clear();
//               判断 包名 和 应用名 里面是否含有 constraint
//               并且在中文下进行拼音和拼音首字过滤
        FilterResults results = new FilterResults();
        if(constraint != null && constraint.length()>0){
            for(int i = 0; i < appList.size() ; i++ ){
                AppInfo appInfo = appList.get(i);
                if(appInfo.packageName.contains(constraint)){
                    items.add(appInfo);
                }else if(appInfo.appName.contains(constraint)){
                    items.add(appInfo);
                }else if(Locale.getDefault().equals(Locale.CHINA)||Locale.getDefault().equals(Locale.TAIWAN)){
                    if(appInfo.appNamePinYin.contains(constraint)){
                        items.add(appInfo);
                    }else if(appInfo.appNameHeadChar.contains(constraint)){
                        items.add(appInfo);
                    }
                }
            }
            Collections.sort(items, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo lhs, AppInfo rhs) {
                    int tag = 0;
                    if (lhs.appName.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                        tag += 1;
                    }
                    if (rhs.appName.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                        tag += 2;
                    }
                    switch (tag) {
                        // 0 ,3 为都不是或都是以cons..开头
                        case 0: case 3:  return Collator.getInstance(Locale.getDefault()).compare(lhs.appName, rhs.appName);
                        case 1:          return -1;
                        case 2: default: return  1;
                    }
                }
            });
            results.values = items;
            results.count = items.size();
            return results;
        }else{
            results.values = appList;
            results.count = appList.size();
            return results;
        }

    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        // NOTE: this function is *always* called from the UI thread.
        if(adapter != null){
            adapter.setAppList((List<AppInfo>) results.values);
            adapter.notifyDataSetChanged();
        }
    }
}