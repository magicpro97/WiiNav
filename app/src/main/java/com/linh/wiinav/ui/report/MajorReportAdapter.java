package com.linh.wiinav.ui.report;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linh.wiinav.R;
import com.linh.wiinav.ui.ReportDetailActivity;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class MajorReportAdapter extends ExpandableRecyclerViewAdapter<MajorReportViewHolder, MinorReportViewHolder>
{
    private static final String TAG = "MajorReportAdapter";
    Context context;

    public MajorReportAdapter(List<? extends ExpandableGroup> groups, final Context context) {
        super(groups);
        this.context = context;
    }

    @Override
    public MajorReportViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.list_item_majorreport, parent, false);
        return new MajorReportViewHolder(view);
    }

    @Override
    public MinorReportViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.list_item_minorreport, parent, false);

        return new MinorReportViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(final MinorReportViewHolder holder, final int flatPosition,
                                      final ExpandableGroup group, int childIndex) {

        final MinorReport minorrep = ((MajorReport) group).getItems().get(childIndex);
        holder.getChildTextView().setText(minorrep.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                Intent intent = new Intent(context, ReportDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("REPORT_TYPE",minorrep.getName());
                intent.putExtra("REPORT_ID", flatPosition);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public void onBindGroupViewHolder(MajorReportViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {

        holder.setMajorReportTitle(group);
    }
}
