package liubaoyua.customtext.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import liubaoyua.customtext.interfaces.FragmentCommunicator;
import liubaoyua.customtext.utils.AppInfo;
import liubaoyua.customtext.R;
import liubaoyua.customtext.ui.SetText;
import liubaoyua.customtext.adapters.AppRecyclerAdapter;
import liubaoyua.customtext.utils.AppListFilter;
import liubaoyua.customtext.utils.Common;

public class AppListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private AppRecyclerAdapter appRecyclerAdapter;
    private SwipeRefreshLayout mSwipeRefreshWidget;
    private FragmentCommunicator communicator;
    private List<AppInfo> appList = new ArrayList<>();

    public void setAppList(List<AppInfo> appList) {
        this.appList = appList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

//    public void prepareAdapter(){
//        if(appRecyclerAdapter == null){
//            if(Common.DEBUG){
//                Log.d(Common.TAG,"appRecyclerAdapter is null");
//            }
//            appRecyclerAdapter = new AppRecyclerAdapter(getActivity(),appList);
//        }
//    }

    @Override
    public void onAttach(Activity activity) {
        communicator = (FragmentCommunicator)activity;
        if(appRecyclerAdapter == null){
            appRecyclerAdapter = new AppRecyclerAdapter(getActivity(),appList);
        }
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSwipeRefreshWidget = (SwipeRefreshLayout)inflater.inflate(R.layout.list_fragment_app, container, false);
        mRecyclerView = (RecyclerView)mSwipeRefreshWidget.findViewById(R.id.recycler_view);
        return mSwipeRefreshWidget;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSwipeRefreshWidget.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext(), LinearLayout.VERTICAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
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
        mSwipeRefreshWidget.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                communicator.reFreshList();
            }
        });
    }

    public SwipeRefreshLayout getSwipeRefreshWidget() {
        return mSwipeRefreshWidget;
    }

    public AppRecyclerAdapter getAppRecyclerAdapter() {
        return appRecyclerAdapter;
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
//        appList.clear();
//        mSwipeRefreshWidget = null;
//        communicator = null;
//        mRecyclerView = null;
//        appRecyclerAdapter = null;
    }
}
