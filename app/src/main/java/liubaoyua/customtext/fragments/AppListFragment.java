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

import liubaoyua.customtext.R;
import liubaoyua.customtext.adapters.AppRecyclerAdapter;
import liubaoyua.customtext.ui.SetText;
import liubaoyua.customtext.utils.AppInfo;
import liubaoyua.customtext.utils.Common;

public class AppListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private AppRecyclerAdapter appRecyclerAdapter;

    public void setAppList(List<AppInfo> appList) {
        if(appRecyclerAdapter != null){
            appRecyclerAdapter.getFilter().setAppList(appList);
        }else{
            throw new IllegalStateException("appRecyclerAdapter is null");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment_app, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
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
                Intent intent = new Intent(getActivity(), SetText.class);
                intent.putExtra(Common.POSITION_ARG, position);
                intent.putExtra(Common.PACKAGE_NAME_ARG, packageName);
                getActivity().startActivityForResult(intent, Common.APP_REQUEST_CODE);
            }
        });
    }

    public void filter(String nameFilter){
        if(appRecyclerAdapter != null)
           appRecyclerAdapter.getFilter().filter(nameFilter);
    }

    public List<AppInfo> getAppList(){
        if(appRecyclerAdapter != null){
            return appRecyclerAdapter.getFilter().getAppList();
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
}
