package eastwind.ramd.support;

import java.util.concurrent.TimeUnit;

import io.netty.util.HashedWheelTimer;

/**
 * Created by jan.huang on 2017/10/17.
 */
public class HashedWheelTimerExecutor implements DelayedExecutor {

    private HashedWheelTimer hashedWheelTimer;

    public HashedWheelTimerExecutor(String name) {
    	hashedWheelTimer = new HashedWheelTimer(new NamedThreadFactory(name + "-hashed"));
        hashedWheelTimer.start();
    }

    public void delayExecute(DelayedTask delayedTask) {
        hashedWheelTimer.newTimeout(t -> execute(delayedTask), delayedTask.getDelay(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
    }

    public void cancel(DelayedTask delayedTask) {
        delayedTask.cancel();
    }

    public void shutdown() {
        hashedWheelTimer.stop();
    }

}
