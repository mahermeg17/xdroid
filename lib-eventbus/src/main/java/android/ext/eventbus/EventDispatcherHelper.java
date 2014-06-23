package android.ext.eventbus;

import android.ext.core.Objects;
import android.os.Bundle;

/**
 * @author Oleksii Kropachov (o.kropachov@shamanland.com)
 */
public final class EventDispatcherHelper {
    public static void onCreate(EventDispatcherOwner owner, Bundle state) {
        onCreate(owner, state, false);
    }

    public static void onCreate(EventDispatcherOwner owner, Bundle state, boolean keepLastEvent) {
        if (owner.getEventDispatcherXmlId() != 0) {
            final EventDispatcher dispatcher = EventDispatcherInflater.getInstance().inflate(owner.getContext(), owner.getEventDispatcherXmlId());
            owner.putCustomService(EventDispatcher.class.getName(), dispatcher);

            Bundle event = owner.extractInitialEvent();
            if (event != null) {
                EventBus.send(owner.getContext(), event);
            }

            if (keepLastEvent) {
                owner.putCustomService(EventDispatcher.class.getName(), new KeepLastEventDispatcher(dispatcher, state));
            }
        }
    }

    public static void onSaveInstanceState(EventDispatcherOwner owner, Bundle state) {
        Object dispatcher = owner.getCustomService(EventDispatcher.class.getName());
        if (dispatcher instanceof KeepLastEventDispatcher) {
            ((KeepLastEventDispatcher) dispatcher).onSaveInstanceState(state);
        }
    }

    private EventDispatcherHelper() {
        // disallow public access
    }

    static class KeepLastEventDispatcher implements EventDispatcher {
        private static final String KEY_LAST = KeepLastEventDispatcher.class.getName() + ".last";

        private final EventDispatcher mBase;
        private Bundle mLast;

        public KeepLastEventDispatcher(EventDispatcher base, Bundle state) {
            mBase = Objects.notNull(base);

            if (state != null) {
                mLast = state.getBundle(KEY_LAST);
                if (mLast != null) {
                    mBase.onNewEvent(EventBus.getEventId(mLast), mLast);
                }
            }
        }

        @Override
        public boolean onNewEvent(int eventId, Bundle event) {
            mLast = EventBus.prepare(eventId, event);
            return mBase.onNewEvent(eventId, event);
        }

        public void onSaveInstanceState(Bundle state) {
            state.putBundle(KEY_LAST, mLast);
        }
    }
}
