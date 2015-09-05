package liubaoyua.customtext.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.ArrayList;

import liubaoyua.customtext.R;
import liubaoyua.customtext.entity.CustomText;

/**
 * Created by liubaoyua on 2015/6/19 0019.
 */
public class TextRecyclerAdapter extends RecyclerView.Adapter<TextRecyclerAdapter.ViewHolder> {


    public boolean multiSelectMode = false;
    private Context mContext;
    private ArrayList<CustomText> data = new ArrayList<>();
    private boolean selectAll = false;
    private CustomText tempCT = new CustomText();
    private CustomText undo = new CustomText();
    private RecyclerView recyclerView;

    public TextRecyclerAdapter(Context mContext, ArrayList<CustomText> data, RecyclerView recyclerView) {
        this.mContext = mContext;
        setData(data);
        this.recyclerView = recyclerView;
    }

    @Override
    public TextRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_card_text, parent, false);
        return new ViewHolder(view);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final TextRecyclerAdapter.ViewHolder holder, int position) {
        CustomText customText = data.get(position);
        holder.oriEditText.setText(customText.oriText);
        holder.newEditText.setText(customText.newText);
        if(multiSelectMode){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(customText.isCheck);
        }else {
            holder.checkBox.setVisibility(View.INVISIBLE);
        }

    }

    public ArrayList<CustomText> getData() {
        return data;
    }

    public void setData(ArrayList<CustomText> data) {
        this.data = new ArrayList<>();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data==null?0:data.size();

    }

    public ArrayList<CustomText> getSelectedItem(){
        ArrayList<CustomText> list = new ArrayList<>();
        for(CustomText customText:data){
            if(customText.isCheck){
                list.add(new CustomText(customText));
            }
        }
        Snackbar.make(recyclerView, mContext.getString(android.R.string.copy) + " " +
                mContext.getString(R.string.succeed), Snackbar.LENGTH_LONG).show();
        return list;
    }

    public ArrayList<CustomText> cutSelectedItem(){
        ArrayList<CustomText> list = new ArrayList<>();
        for(int i = 0; i < data.size(); i++){
            if(data.get(i).isCheck){
                list.add(new CustomText(data.get(i)));
                data.remove(i);
                i--;
            }
        }
        notifyDataSetChanged();
        recyclerView.scrollToPosition(0);
        Snackbar.make(recyclerView, mContext.getString(android.R.string.cut) + " " +
                mContext.getString(R.string.succeed), Snackbar.LENGTH_LONG).show();
        return list;
    }

    public void deselectAllItem(){
        for(CustomText customText:data){
            customText.isCheck = false;
        }
        notifyDataSetChanged();
    }

    public void pasteClipBoard(ArrayList<CustomText> clipboard){
        for(int i=0; i<clipboard.size();i++){
            data.add(i,new CustomText(clipboard.get(i)));
        }
        notifyItemRangeInserted(0, clipboard.size());
        recyclerView.scrollToPosition(0);
        Snackbar.make(recyclerView, mContext.getString(android.R.string.paste) + " " +
                mContext.getString(R.string.succeed), Snackbar.LENGTH_LONG).show();
    }

    public void selectAll(){
        if(!selectAll){
            for(CustomText customText:data){
                customText.isCheck = true;
            }
            selectAll = true;
            notifyDataSetChanged();
        }else{
            selectAll = false;
            deselectAllItem();
        }
        notifyDataSetChanged();
    }

    public void onClickView(View view,final ViewHolder viewHolder) {
        if(view.getId()==R.id.button_serial){
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setTitle("(" + viewHolder.getAdapterPosition() + ") " + mContext.getString(R.string.dialog_title));
            final String[] texts = mContext.getResources().getStringArray(R.array.dialog_item);
            builder.setItems(texts, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String succeed = mContext.getString(R.string.succeed);
                            switch (which) {
                                case 0: {//<item>前排加一楼</item>
                                    CustomText now = data.get(viewHolder.getAdapterPosition());
                                    if (viewHolder.getAdapterPosition() == 0) {
                                        data.add(viewHolder.getAdapterPosition(), new CustomText());
                                    } else {
                                        CustomText prev = data.get(viewHolder.getAdapterPosition() - 1);
                                        data.add(viewHolder.getAdapterPosition(), new CustomText());
                                    }
                                    notifyItemInserted(viewHolder.getAdapterPosition());
                                    Snackbar.make(recyclerView, texts[which] + " " + succeed, Snackbar.LENGTH_LONG)
                                            .show();
                                    break;
                                }
                                case 1: {//<item>复制本楼</item>
                                    tempCT = new CustomText(data.get(viewHolder.getAdapterPosition()));
                                    Snackbar.make(recyclerView, texts[which] + " " + succeed, Snackbar.LENGTH_LONG)
                                            .show();
                                    break;
                                }
                                case 2: {//<item>删除本楼</item>
                                    final int position = viewHolder.getAdapterPosition();
                                    undo = data.get(position);
                                    data.remove(position);
                                    notifyItemRemoved(position);
                                    Snackbar.make(recyclerView, texts[which] + " " + succeed, Snackbar.LENGTH_LONG)
                                            .setAction(mContext.getString(R.string.undo), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    data.add(position, undo);
                                                    notifyItemInserted(position);
                                                }
                                            }).show();
                                    break;
                                }
                                case 3: {//<item>剪切本楼</item>
                                    final int position = viewHolder.getAdapterPosition();
                                    tempCT = new CustomText(data.get(viewHolder.getAdapterPosition()));
                                    undo = data.get(viewHolder.getAdapterPosition());
                                    data.remove(viewHolder.getAdapterPosition());
                                    notifyItemRemoved(viewHolder.getAdapterPosition());
                                    Snackbar.make(recyclerView, texts[which] + " " + succeed, Snackbar.LENGTH_LONG)
                                            .setAction(mContext.getString(R.string.undo), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    data.add(position, undo);
                                                    notifyItemInserted(position);
                                                }
                                            }).show();
                                    break;
                                }
                                case 4: {//<item>粘贴本楼</item>
                                    final CustomText now = data.get(viewHolder.getAdapterPosition());
                                    if (tempCT.isEmpty()) {
                                        Snackbar.make(recyclerView, mContext.getString(R.string.clipboard_is_empty), Snackbar.LENGTH_LONG)
                                                .show();
                                        break;
                                    } else {
                                        undo = new CustomText(now);
                                        now.oriText = tempCT.oriText;
                                        now.newText = tempCT.newText;
                                        notifyItemChanged(viewHolder.getAdapterPosition());
                                        Snackbar.make(recyclerView, texts[which] + " " + succeed, Snackbar.LENGTH_LONG)
                                                .setAction(mContext.getString(R.string.undo), new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        now.oriText = undo.oriText;
                                                        now.newText = undo.newText;
                                                        notifyItemChanged(viewHolder.getAdapterPosition());
                                                    }
                                                }).show();
                                        break;
                                    }
                                }
                                case 5: {// <item>后排加一楼</item>
                                    CustomText now = data.get(viewHolder.getAdapterPosition());
                                    if (viewHolder.getAdapterPosition() == data.size() - 1) {
                                        data.add(viewHolder.getAdapterPosition() + 1, new CustomText());
                                    } else {
                                        CustomText next = data.get(viewHolder.getAdapterPosition() + 1);
                                        data.add(viewHolder.getAdapterPosition() + 1, new CustomText());
                                    }
                                    notifyItemInserted(viewHolder.getAdapterPosition() + 1);
                                    Snackbar.make(recyclerView, texts[which] + " " + succeed, Snackbar.LENGTH_LONG)
                                            .show();
                                    break;
                                }
                            }
                        }
                    }

            );
            builder.show();
        }else if(view.getId() == R.id.button_clear){
            final CustomText customText = data.get(viewHolder.getAdapterPosition());
            undo = new CustomText(customText);
            customText.newText="";
            customText.oriText="";
            notifyItemChanged(viewHolder.getAdapterPosition());
            Snackbar.make(recyclerView, mContext.getString(R.string.clear) + " " +
                    mContext.getString(R.string.succeed), Snackbar.LENGTH_LONG)
                    .setAction(mContext.getString(R.string.undo), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            customText.oriText = undo.oriText;
                            customText.newText = undo.newText;
                            notifyItemChanged(viewHolder.getAdapterPosition());
                        }
                    }).show();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Button clearButton;
        public Button operateButton;
        public EditText oriEditText;
        public EditText newEditText;
        public CheckBox checkBox;

        public ViewHolder(final View view) {
            super(view);
            clearButton = (Button) view.findViewById(R.id.button_clear);
            operateButton = (Button) view.findViewById(R.id.button_serial);
            oriEditText = (EditText) view.findViewById(R.id.editText_original_text);
            newEditText = (EditText) view.findViewById(R.id.editText_new_text);
            checkBox = (CheckBox) view.findViewById(R.id.check_box);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    data.get(getAdapterPosition()).isCheck = b;
                }
            });
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickView(view, ViewHolder.this);
                }
            });
            oriEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    CustomText customText = data.get(getAdapterPosition());
                    customText.oriText = oriEditText.getText().toString();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            newEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    CustomText customText = data.get(getAdapterPosition());
                    customText.newText = newEditText.getText().toString();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            operateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickView(view, ViewHolder.this);
                }
            });
        }
    }
}
