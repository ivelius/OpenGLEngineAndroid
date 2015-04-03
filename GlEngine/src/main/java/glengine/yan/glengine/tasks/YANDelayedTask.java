package glengine.yan.glengine.tasks;

/**
 * Created by ybra on 12.12.2014.
 */
public class YANDelayedTask implements YANTask {

    public interface YANDelayedTaskListener {
        void onComplete();
    }


    private float mDurationSeconds;
    private float mOriginalDurationInSeconds;
    private YANDelayedTaskListener mDelayedTaskListener;

    public YANDelayedTask(float durationSeconds) {
        mDurationSeconds = durationSeconds;
    }

    public YANDelayedTask(float durationSeconds, YANDelayedTaskListener listener) {
        mDurationSeconds = durationSeconds;
        mOriginalDurationInSeconds = durationSeconds;
        mDelayedTaskListener = listener;
    }


    @Override
    public void onUpdate(float deltaSeconds) {
        mDurationSeconds -= deltaSeconds;
        if (mDurationSeconds <= 0) {
            finishTask();
        }
    }

    private void finishTask() {
        stop();
        if (mDelayedTaskListener != null) {
            mDelayedTaskListener.onComplete();
        }

        //reset duration in seconds to allow reuse this task
        mDurationSeconds = mOriginalDurationInSeconds;
    }

    @Override
    public void start() {
        YANTaskManager.getInstance().addTask(this);
    }

    @Override
    public void stop() {
        YANTaskManager.getInstance().removeTask(this);
        mDurationSeconds = 0;
    }

}
