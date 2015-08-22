package liubaoyua.customtext.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import liubaoyua.customtext.R;
import liubaoyua.customtext.adapters.AppRecyclerAdapter;
import liubaoyua.customtext.entity.AppInfo;
import liubaoyua.customtext.entity.NewListEvent;
import liubaoyua.customtext.ui.SetTextActivity;
import liubaoyua.customtext.utils.Common;

public class AppListFragment extends Fragment {

    private static int count = 0;
    private RecyclerView mRecyclerView;
    private AppRecyclerAdapter appRecyclerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment_app, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        return mRecyclerView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext(), LinearLayout.VERTICAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        appRecyclerAdapter = new AppRecyclerAdapter(getActivity(), new ArrayList<AppInfo>());
        mRecyclerView.setAdapter(appRecyclerAdapter);
        appRecyclerAdapter.setOnItemClickListener(new AppRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, String packageName) {
                Intent intent = new Intent(getActivity(), SetTextActivity.class);
                intent.putExtra(Common.POSITION_ARG, position);
                intent.putExtra(Common.PACKAGE_NAME_ARG, packageName);
                getActivity().startActivityForResult(intent, Common.APP_REQUEST_CODE);
            }
        });
        // 两个界面都加载好后进行数据加载
        count ++;
        if(count == 2){
            EventBus.getDefault().post(new NewListEvent());
            count = 0;
        }
    }

    public void filter(String nameFilter){
        if(appRecyclerAdapter != null)
           appRecyclerAdapter.getFilter().filter(nameFilter);
    }

    public List<AppInfo> getAppList() {
        if (appRecyclerAdapter != null) {
            return appRecyclerAdapter.getFilter().getAppList();
        } else {
            throw new IllegalStateException("appRecyclerAdapter is null");
        }
    }

    public void setAppList(List<AppInfo> appList) {
        if(appRecyclerAdapter != null){
            appRecyclerAdapter.getFilter().setAppList(appList);
        }else{
            throw new IllegalStateException("appRecyclerAdapter is null");
        }
    }

    public List<AppInfo> getShowingAppList(){
        if(appRecyclerAdapter != null){
            return appRecyclerAdapter.getAppList();
        }else{
            throw new IllegalStateException("appRecyclerAdapter is null");
        }
    }

    public void notifyDataSetChanged(){
        if(appRecyclerAdapter != null)
            appRecyclerAdapter.notifyDataSetChanged();
    }

    public void stopScrolling() {
        if (mRecyclerView != null) {
            mRecyclerView.stopScroll();
        }
    }

    public void scrollToTopOrBottom() {
        if (mRecyclerView == null)
            return;

        if (mRecyclerView.canScrollVertically(-1)) {
            mRecyclerView.smoothScrollToPosition(0);
        } else {
            mRecyclerView.smoothScrollToPosition(appRecyclerAdapter.getItemCount() - 1);
        }

    }
}
