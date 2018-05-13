package com.tim.cubo.activity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;
import com.brightcove.player.view.BrightcovePlayer;
import com.tim.cubo.R;
import com.tim.cubo.TimeConverter;
import com.tim.cubo.internet.ApiRequest;
import com.tim.cubo.internet.CuboCallback;
import com.tim.cubo.model.Alert;
import com.tim.cubo.model.Block;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BrightcovePlayer {

    @BindView(R.id.videoView)
    BrightcoveExoPlayerVideoView videoView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.cursor)
    View cursor;

    private List<Block> blockList;
    private List<Alert> alertList;
    private Map<Alert, Integer> pointMap = new HashMap<>();

    private boolean hasPoint = false;
    private boolean hasDrag = false;


    private static final int REFRESH_MILLS = 5 * 60 * 1000;
    private static final int BLOCK_RANGE_MINUTES = 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        recyclerView
            .setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
        recyclerView.addItemDecoration(new BlockDecoration());
        recyclerView.setAdapter(new BlockAdapter());

        videoView.setMediaController((MediaController) null);
        videoView.getEventEmitter().on(EventType.BUFFERING_STARTED, new EventListener() {
            @Override
            public void processEvent(Event event) {
                progress.setVisibility(View.VISIBLE);
            }
        });
        videoView.getEventEmitter().on(EventType.BUFFERING_COMPLETED, new EventListener() {
            @Override
            public void processEvent(Event event) {
                progress.setVisibility(View.GONE);
            }
        });

        new ApiRequest(this).getAlerts(new CuboCallback() {
            @Override
            public void getAlerts(List<Alert> alerts) {
                super.getAlerts(alerts);

                //TODO: LiveStream Replacement
                if (alerts != null & alerts.get(0) != null) {
                    videoView.setVideoPath(alerts.get(0).getVideoUrl());
                    videoView.start();
                }

                alertList = alerts;
                timelineRequestLayout(alerts);

            }
        });
        new CountDownTimer(REFRESH_MILLS, REFRESH_MILLS) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                timelineRequestLayout(alertList);
                start();
            }
        }.start();

        recyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE: {
                        if (hasPoint && hasDrag) {
                            Alert targetAlert = null;
                            int scrollX = Integer.MAX_VALUE;
                            if (pointMap == null) {
                                return;
                            }
                            for (Map.Entry<Alert, Integer> entry : pointMap.entrySet()) {
                                int offset = entry.getValue() - (int) cursor.getX();
                                if (Math.abs(offset) < Math.abs(scrollX)) {
                                    targetAlert = entry.getKey();
                                    scrollX = offset;
                                }
                            }

                            if (targetAlert != null & scrollX != Integer.MAX_VALUE) {
                                recyclerView.smoothScrollBy(scrollX, 0);
                                hasDrag = false;
                                if (videoView != null) {
                                    videoView.setVideoPath(targetAlert.getVideoUrl());
                                    videoView.start();
                                }
                            }
                        }
                        break;
                    }
                    case RecyclerView.SCROLL_STATE_DRAGGING: {
                        hasDrag = true;
                        if (videoView != null) {
                            videoView.pause();
                        }
                        break;
                    }
                    case RecyclerView.SCROLL_STATE_SETTLING: {
                        break;
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void timelineRequestLayout(List<Alert> alerts) {
        long ts = TimeConverter
            .adjustTimestamp(Calendar.getInstance().getTimeInMillis() / 1000);
        long offset = -1;
        int rangeSeconds = BLOCK_RANGE_MINUTES * 60;
        if (alerts == null) {
            return;
        }
        SparseArray<List<Alert>> alertSparseArray = new SparseArray<>();
        for (Alert a : alerts) {
            long alertOffset = ts - a.getTs();
            int blockIndex = (int) (alertOffset / rangeSeconds) + 1;

            if (alertSparseArray.get(blockIndex) == null) {
                List<Alert> blockAlertList = new ArrayList<>();
                blockAlertList.add(a);
                alertSparseArray.put(blockIndex, blockAlertList);
            } else {
                alertSparseArray.get(blockIndex).add(a);
            }

            if (offset == -1) {
                offset = alertOffset;
            } else {
                offset = Math.max(offset, alertOffset);
            }

        }
        if (offset != -1) {
            int size = (int) (offset / rangeSeconds) + 2;
            long hour = TimeConverter.getHours(ts);
            long min = TimeConverter.getMinutes(ts);
            blockList = new ArrayList<>();
            int sizeOffset = 0;

            if (min % 10 == 0) {
                sizeOffset = 0;
            }
            for (int i = 0; i < size + sizeOffset; i++) {
                Block block = new Block();

                if (min % 10 == 0) {
                    block.setHour(hour);
                    block.setMin(min);
                } else {
                    min = min + (10 - (min % 10));
                    block.setHour(hour);
                    block.setMin(min);
                }

                if (min - 10 < 0) {
                    min = 50;
                    if (hour - 1 == 0) {
                        hour = 24;
                    } else {
                        hour -= 1;
                    }
                } else {
                    min -= 10;
                }

                if (alertSparseArray.get(i) != null) {
                    block.setAlertList(alertSparseArray.get(i));
                }
                blockList.add(block);
            }
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    class BlockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(viewType, parent, false);
            return new BlockView(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return blockList == null ? 0 : blockList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.item_block;
        }

        class BlockView extends RecyclerView.ViewHolder {

            BlockView(View itemView) {
                super(itemView);
            }
        }
    }

    class BlockDecoration extends ItemDecoration {

        @BindDimen(R.dimen.timeline_horizontal_padding)
        float horizontalPadding;
        @BindDimen(R.dimen.timeline_vertical_padding)
        float verticalPadding;
        @BindDimen(R.dimen.timeline_text_size)
        float textSize;
        @BindDimen(R.dimen.timeline_point_radius)
        float pointRadius;
        @BindDimen(R.dimen.timeline_line_height)
        float lineHeight;
        @BindDimen(R.dimen.timeline_text_padding)
        float textPadding;
        @BindDimen(R.dimen.timeline_width)
        float lineWidth;
        @BindDimen(R.dimen.fading_edge)
        float fadingEdge;
        private Paint textPaint;
        private Paint pointPaint;
        private Paint linePaint;

        public BlockDecoration() {
            ButterKnife.bind(this, MainActivity.this);
            textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(textSize);

            pointPaint = new Paint();
            pointPaint.setColor(Color.RED);
            pointPaint.setAntiAlias(true);

            linePaint = new Paint();
            linePaint.setColor(Color.WHITE);
            linePaint.setStrokeWidth(lineWidth);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.top = (int) verticalPadding;
            outRect.bottom = (int) verticalPadding;

            int index = parent.getChildAdapterPosition(view);
            if (index == 0) {
                outRect.right = parent.getMeasuredWidth() / 2;
                outRect.left = (int) horizontalPadding;
            } else if (index == blockList.size() - 1) {
                outRect.right = (int) horizontalPadding;
                outRect.left = parent.getMeasuredWidth() / 2;
            } else {
                outRect.right = (int) horizontalPadding;
                outRect.left = (int) horizontalPadding;
            }
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, State state) {
            super.onDrawOver(c, parent, state);

            int childCount = parent.getChildCount();
            float leftFadingRange = fadingEdge;
            float rightFadingRange = parent.getMeasuredWidth() - fadingEdge;
            hasPoint = false;
            for (int i = 0; i < childCount; i++) {
                View view = parent.getChildAt(i);
                int index = parent.getChildAdapterPosition(view);
                Block block = blockList.get(index);

                float textX = view.getX();
                float textY = view.getTop() - lineHeight - textPadding;
                float lineLeft = view.getX();
                float lineRight = view.getX() + lineWidth;
                float lineTop = view.getTop() - lineHeight;
                float lineBottom = view.getTop();

                int alphaRatio;
                if (view.getX() < 0 || view.getX() > parent.getMeasuredWidth()) {
                    alphaRatio = 0;
                } else if (view.getX() < leftFadingRange) {
                    alphaRatio = (int) (255 * (view.getX() / leftFadingRange));
                } else if (view.getX() > rightFadingRange) {
                    alphaRatio = (int) (255 * ((parent.getWidth() - view.getX())
                        / leftFadingRange));
                } else {
                    alphaRatio = 255;
                }

                linePaint.setAlpha(alphaRatio);
                textPaint.setAlpha(alphaRatio);
                pointPaint.setAlpha(alphaRatio);
                if (index == 0) {
                    c.drawText("LIVE", textX, textY, textPaint);
                    c.drawRect(lineLeft, lineTop, lineRight, lineBottom, linePaint);
                } else if (block.getMin() == 0) {
                    long value = block.getHour() % 12 == 0 ? 12 : block.getHour() % 12;
                    String des;
                    if (block.getHour() == 24) {
                        des = "AM";
                    } else if (block.getHour() == 12) {
                        des = "PM";
                    } else {
                        des = block.getHour() > 12 ? "PM" : "AM";
                    }
                    String displayHour = String.format("%02d", value) + des;
                    c.drawText(displayHour, textX, textY, textPaint);
                    c.drawRect(lineLeft, lineTop, lineRight, lineBottom, linePaint);
                }

                if (block.getAlertList() != null) {
                    for (Alert a : block.getAlertList()) {
                        hasPoint = true;
                        float xRatio = blockRatio(a.getTs());
                        float cx = view.getX() + horizontalPadding * 2 * xRatio;
                        float cy = view.getBottom() - view.getMeasuredHeight() / 2;
                        c.drawCircle(cx, cy, pointRadius, pointPaint);

                        int pointX = (int) (cx - pointRadius);
                        pointMap.put(a, pointX);
                    }
                }

            }
        }

        private float blockRatio(long ts) {
            long min = TimeConverter.getMinutes(ts) % 10;
            long second = TimeConverter.getSeconds(ts);
            return (min * 60 + second) / 600f;
        }
    }
}
