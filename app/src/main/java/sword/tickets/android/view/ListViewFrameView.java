package sword.tickets.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ListViewFrameView extends FrameLayout {

    private static final long LONG_CLICK_INTERVAL_MILLIS = 600;
    private static final int DIFF_FOR_CANCEL = 10;

    private final DownRunnable _downRunnable = new DownRunnable();

    private float _downX;
    private float _downY;
    private int _downAdapterPosition;
    private boolean _downRunnableRegistered;
    private boolean _longClickIntervalReached;
    private boolean _sortingStarted;
    private boolean _multiSelectionStarted;
    private LongClickListener _longClickListener;

    public ListViewFrameView(@NonNull Context context) {
        super(context);
    }

    public ListViewFrameView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ListViewFrameView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ListViewFrameView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void stopMultiSelection() {
        _multiSelectionStarted = false;
    }

    private ListView findListViewChild() {
        final int childCount = getChildCount();
        final View firstChild = (childCount > 0)? getChildAt(0) : null;
        return (firstChild instanceof ListView)? (ListView) firstChild : null;
    }

    private int findListViewIndex(ListView listView, float y) {
        if (listView != null) {
            final int visibleItemCount = listView.getChildCount();
            for (int i = 0; i < visibleItemCount; i++) {
                final View itemView = listView.getChildAt(i);
                if (itemView.getTop() < y && itemView.getBottom() > y) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        System.err.println("ListViewFrameView.onInterceptTouchEvent for action " + ev.getAction() + " in (" + ev.getX() + ", " + ev.getY() + ")");

        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            final ListView listView = findListViewChild();
            final int touchedIndex = findListViewIndex(listView, ev.getY());
            if (touchedIndex >= 0) {
                _downX = ev.getX();
                _downY = ev.getY();
                _downAdapterPosition = listView.getFirstVisiblePosition() + touchedIndex;

                if (!_downRunnableRegistered && !_multiSelectionStarted) {
                    _downRunnableRegistered = true;
                    postDelayed(_downRunnable, LONG_CLICK_INTERVAL_MILLIS);
                }
            }
        }
        else if (action == MotionEvent.ACTION_MOVE) {
            if (Math.abs(ev.getX() - _downX) >= DIFF_FOR_CANCEL || Math.abs(ev.getY() - _downY) >= DIFF_FOR_CANCEL) {
                if (_longClickIntervalReached && !_multiSelectionStarted) {
                    _sortingStarted = true;
                    _longClickListener.onSortingStart(_downAdapterPosition);
                    super.onInterceptTouchEvent(ev);
                    return true;
                }
                else {
                    if (_downRunnableRegistered) {
                        _downRunnableRegistered = false;
                        removeCallbacks(_downRunnable);
                    }
                }
            }
        }
        else if (action == MotionEvent.ACTION_CANCEL) {
            if (_downRunnableRegistered) {
                _downRunnableRegistered = false;
                removeCallbacks(_downRunnable);
            }
        }
        else if (action == MotionEvent.ACTION_UP) {
            if (_longClickIntervalReached && !_sortingStarted && !_multiSelectionStarted) {
                _longClickIntervalReached = false;
                _multiSelectionStarted = true;
                _longClickListener.onMultiSelectionStart(_downAdapterPosition);
            }
            else if (_downRunnableRegistered) {
                _downRunnableRegistered = false;
                removeCallbacks(_downRunnable);
                _longClickListener.onItemClick(_downAdapterPosition);
            }
            else if (_multiSelectionStarted) {
                _longClickListener.onItemClick(_downAdapterPosition);
            }
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        System.err.println("ListViewFrameView.onTouchEvent for action " + ev.getAction() + " in (" + ev.getX() + ", " + ev.getY() + ")");

        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_MOVE) {
            _longClickListener.onSortingMove((int) (ev.getY() - _downY));
            return true;
        }
        else if (action == MotionEvent.ACTION_UP) {
            _sortingStarted = false;
            _longClickListener.onSortingFinished((int) (ev.getY() - _downY));
            return true;
        }
        else if (action == MotionEvent.ACTION_CANCEL) {
            _sortingStarted = false;
            _longClickListener.onSortingFinished((int) (ev.getY() - _downY));
            return true;
        }

        return false;
    }

    public void setLongClickListener(LongClickListener listener) {
        _longClickListener = listener;
    }

    public interface LongClickListener {
        void onItemClick(int adapterPosition);
        void onLongClickStart(int adapterPosition);
        void onMultiSelectionStart(int adapterPosition);
        void onSortingStart(int adapterPosition);
        void onSortingMove(int diffY);
        void onSortingFinished(int diffY);
    }

    private final class DownRunnable implements Runnable {

        @Override
        public void run() {
            _downRunnableRegistered = false;
            _longClickIntervalReached = true;
            _longClickListener.onLongClickStart(_downAdapterPosition);
        }
    }
}
